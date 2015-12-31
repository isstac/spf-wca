package wcanalysis.heuristic;

import isstac.structure.cfg.Block;
import isstac.structure.cfg.CFG;
import isstac.structure.cfg.CFGGenerator;
import isstac.structure.cfg.util.CFGToDOT;
import isstac.structure.cfg.util.DotAttribute;
import isstac.structure.serialize.GraphSerializer;
import isstac.structure.serialize.JavaSerializer;
import wcanalysis.heuristic.ContextManager.CGContext;
import wcanalysis.heuristic.DecisionCollection.FalseDecisionCollection;
import wcanalysis.heuristic.DecisionCollection.TrueDecisionCollection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import att.grappa.Attribute;
import att.grappa.Graph;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.DoubleCompareInstruction;
import gov.nasa.jpf.jvm.bytecode.FCMPL;
import gov.nasa.jpf.jvm.bytecode.IfInstruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.bytecode.FCMPG;
import gov.nasa.jpf.symbc.bytecode.LCMP;
import gov.nasa.jpf.symbc.bytecode.IF_ICMPEQ;
import gov.nasa.jpf.symbc.bytecode.IF_ICMPGE;
import gov.nasa.jpf.symbc.bytecode.IF_ICMPGT;
import gov.nasa.jpf.symbc.bytecode.IF_ICMPLE;
import gov.nasa.jpf.symbc.bytecode.IF_ICMPLT;
import gov.nasa.jpf.symbc.bytecode.IF_ICMPNE;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public abstract class PathListener extends PropertyListenerAdapter {

  //marks whether block is a symbolic decision
  private static class Covered implements DotAttribute, Serializable {
    private static final long serialVersionUID = -1276855202384536042L;

    private static class SeqPair implements Serializable {
      private static final long serialVersionUID = -5924282283709670350L;
      private int seqNum;
      private char choice;
      public SeqPair(int seqNum, char choice) {
        this.seqNum = seqNum;
        this.choice = choice;
      }
      @Override
      public String toString() {
        return "[" + seqNum + "," + choice + "]";
      }
    }
    private List<SeqPair> visitSequence = new ArrayList<>();
    
    public void addToSequence(int num, char choice) {
      this.visitSequence.add(new SeqPair(num, choice));
    }
    
    @Override
    public Attribute getAttribute() {
      return new Attribute(Attribute.NODE, Attribute.COLOR_ATTR, "red");
    }

    @Override
    public String getLabelString() {
      StringBuilder sb = new StringBuilder();
      int breakat = 10;
      Iterator<SeqPair> seq = visitSequence.iterator();
      int i = 0;
      while(seq.hasNext()) {
        sb.append(seq.next());
        if(seq.hasNext())
          sb.append(',');
        if(++i % breakat == 0)
          sb.append('\n');
      }
      if(sb.charAt(sb.length()-1) != '\n')
        sb.append('\n');
      return sb.toString();
    }
  }
  
  private Logger logger = JPF.getLogger(PathListener.class.getName());

  /*
   * Configuration
   */
  //JPF conf strings
  //Measured methods
  public final static String MEASURED_METHODS = "symbolic.heuristic.measuredmethods";
  private final static String SYMBOLIC_METHODS = "symbolic.method";
  private Set<String> measuredMethods;
  private Set<String> symbolicMethods;
  
  public final static String SERIALIZER_CONF = "symbolic.wc.serializer";
  public final static String HISTORY_SIZE_PATH = "symbolic.wc.history.size";
  protected final static int DEF_HISTORY_SIZE = 0; 
  
  //Visualization
  public final static String SHOW_INSTRS_CONF = "symbolic.wc.visualizer.showinstructions";
  public final static String SHOW_BB_SEQ_CONF = "symbolic.wc.visualizer.showseq";
  
  
  //Notion of worst case
  public final static String WORST_CASE_STATE_BLDR_CONF = "symbolic.wc.statebuilder";
  
  /*
   * State
   */
  //Conf state
  //Object from which we obtain CFGs
  private final CFGGenerator cfgExtractor;
  
  private final Config jpfConf;
  //Visualization
  private File visDir;
  private final boolean showBbSeq;
  private final boolean showInstrs;
  
  //Serialization
  private File serDir;
  private GraphSerializer serializer;
  
  protected final int decisionHistorySize;
  
  //State
  private StateBuilder stateBuilder;
  protected WorstCasePath wcPath;
  private ContextManager ctxManager;
  //protected State currentState;

  public PathListener(Config jpfConf, JPF jpf) {
    this.jpfConf = jpfConf;
    
    //TODO: we can make this even more precise by also allowing specifying the EXACT 
    //call (e.g. src line number + class) after which count collection should start 
    if(!jpfConf.hasValue(MEASURED_METHODS) && !jpfConf.hasValue(SYMBOLIC_METHODS)) {
      RuntimeException e = new RuntimeException("Must set either " + MEASURED_METHODS + " or " + SYMBOLIC_METHODS);
      logger.severe(e.getMessage());
      throw e;
    }
    this.measuredMethods = getMeasuredMethods(this.jpfConf);
    this.symbolicMethods = getSymbolicMethods(this.jpfConf);
    
    logger.info("Measured methods: " + this.measuredMethods.toString());
  
    logger.info("Serialization set!");
    this.serializer = (jpfConf.hasValue(SERIALIZER_CONF)) ? 
        jpfConf.getInstance(SERIALIZER_CONF, GraphSerializer.class) : 
          new JavaSerializer();
    logger.info("Using " + this.serializer.getClass().getName() + " for serialization");
    this.serDir = this.getSerializationDir(jpfConf);

    this.cfgExtractor = getCFGGenerator(jpfConf);
    this.decisionHistorySize = jpfConf.getInt(HISTORY_SIZE_PATH, DEF_HISTORY_SIZE);
    
    if(this.visualize(this.jpfConf))
      this.visDir = this.getVisualizationDir(this.jpfConf);
    
    this.showInstrs = jpfConf.getBoolean(SHOW_INSTRS_CONF, false);
    this.showBbSeq = jpfConf.getBoolean(SHOW_BB_SEQ_CONF, false);
    
    //Initialize state
    if(jpfConf.hasValue(WORST_CASE_STATE_BLDR_CONF)) {
      this.stateBuilder = jpfConf.getInstance(WORST_CASE_STATE_BLDR_CONF, TimeStateBuilder.class);
    } else
      this.stateBuilder = new TimeStateBuilder();
    this.ctxManager = new ContextManager();
    this.wcPath = null;
  }
  
  private Set<String> getMeasuredMethods(Config jpfConf) {
    String[] measMeth = jpfConf.getStringArray(MEASURED_METHODS, jpfConf.getStringArray(SYMBOLIC_METHODS));
    return extractSimpleMethodNames(measMeth);
  }
  
  private Set<String> getSymbolicMethods(Config jpfConf) {
    String[] symMeth = jpfConf.getStringArray(SYMBOLIC_METHODS, jpfConf.getStringArray(MEASURED_METHODS));
    return extractSimpleMethodNames(symMeth); 
  }
  
  private Set<String> extractSimpleMethodNames(String[] jpfMethodSpecs) {
    //FIXME: This also means that we do not distinguish between overloaded methods
    String[] processedMethods = new String[jpfMethodSpecs.length];
    System.arraycopy(jpfMethodSpecs, 0, processedMethods, 0, jpfMethodSpecs.length);
    for(int i = 0; i < jpfMethodSpecs.length; i++) {
      String meth = jpfMethodSpecs[i];
      int sigBegin = meth.indexOf('(');
      if(sigBegin >= 0)
        processedMethods[i] = meth.substring(0, sigBegin);
    }
    return new HashSet<String>(Arrays.asList(processedMethods));
  }
  
  protected boolean isInCallStack(VM vm, ThreadInfo thread, Set<String> tgts) {
    for(StackFrame frame : thread.getInvokedStackFrames()) {
      String meth = frame.getMethodInfo().getBaseName();
      if(tgts.contains(meth))
        return true;
    }
    return false; 
  }
  
  protected boolean isInSymbolicCallStack(VM vm, ThreadInfo thread) {
    return isInCallStack(vm, thread, this.symbolicMethods);
  }
  
  protected boolean isInMeasuredMethodCallStack(VM vm, ThreadInfo thread) {
    return isInCallStack(vm, thread, this.measuredMethods);
  }
  
  //frustrating to have to write something like this... 

  public GraphSerializer getSerializer() {
    return serializer;
  }

  public CFGGenerator getCfgExtractor() {
    return cfgExtractor;
  }
  
  public int getDecisionHistorySize() {
    return decisionHistorySize;
  }
  
  @Override
  public void choiceGeneratorAdvanced (VM vm, ChoiceGenerator<?> currentCG) {
    //TODO: check if there is a difference between the following cg and currentCg passed to this method
    ChoiceGenerator<?> cg = vm.getSystemState().getChoiceGenerator();
    if(cg instanceof PCChoiceGenerator) {
      this.stateBuilder.handleChoiceGeneratorAdvanced(vm, currentCG);
      CGContext ctx = this.ctxManager.getContext(cg);
      if(ctx == null) {
        //Convert JPF instruction to our own serializable
        //Instruction representation
       // Instruction jpfInstr = currentCG.getInsn();
       // String clsName = jpfInstr.getMethodInfo().getClassName();
        //String methodName = normalizeJPFMethodName(jpfInstr.getMethodInfo());
        
        //BranchInstruction instr = new BranchInstruction(jpfInstr.getMnemonic(), 
         //   clsName, methodName, jpfInstr.getInstructionIndex(), jpfInstr.getLineNumber());
        //dec = new Decision(instr, this.currDec, this.currentState.copy(), vm.getCurrentThread().getCallerStackFrame());
        
        //TODO: Should it be CG or currentCG here?
        this.ctxManager.addContext(cg, vm.getCurrentThread().getCallerStackFrame(), this.stateBuilder.copy());
      } else {
        this.stateBuilder = ctx.stateBuilder;
      }
    }
  }
  
  @Override
  public void searchFinished(Search search) {
    searchFinished(this.wcPath);
  }
  
  public void searchFinished(WorstCasePath wcPath) {
    if(wcPath == null)
      return;
    if(visualize(jpfConf)) {
      //If we visualize, then we'd like to show
      //both block sequence and decision histories
      projectPath(wcPath, Arrays.asList(new BlockSequenceProjector(), new DecisionUpdater()));
    } else if(serialize(jpfConf)) {
      //If we only want to serialize
      //we only need to project the decision histories
      projectPath(wcPath, new DecisionUpdater());
    }
    
    //Now, output visualization and/or serialized CFGs with updated histories
    if(visualize(jpfConf)) {
      Set<CFG> cfgs = this.cfgExtractor.getAllGeneratedCFGs();
      for(CFG cfg : cfgs) {
        String baseFileName = getBaseFileName(cfg);
        visualize(cfg, new File(this.visDir, baseFileName + ".dot").getAbsolutePath());
        visualize(wcPath, new File(this.visDir, baseFileName + "_wc_path.txt").getAbsolutePath());
      }
    }

    if(serialize(jpfConf)) {
      Set<CFG> cfgs = this.cfgExtractor.getAllGeneratedCFGs();
      for(CFG cfg : cfgs) {
        File of = new File(this.serDir, cfg.getFqMethodName().replaceAll("/", "_") + "." + serializer.getFileExtension());
        FileOutputStream fo = null;
        try {
          fo = new FileOutputStream(of);
          this.serializer.serialize(cfg, fo);
        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        } finally {
          try {
            if(fo != null)
              fo.close();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
  }

  @Override
  public void executeInstruction(VM vm, ThreadInfo currentThread, Instruction instructionToExecute) {
    if(isInMeasuredMethodCallStack(vm, currentThread)) {
      if(!currentThread.isFirstStepInsn()) {
        this.stateBuilder.handleExecuteInstruction(vm, currentThread, instructionToExecute);
      }
    }
  }
  
  @Override
  public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction, Instruction executedInstruction) {
    if(isInMeasuredMethodCallStack(vm, currentThread) && !currentThread.isFirstStepInsn()) {
      this.stateBuilder.handleInstructionExecuted(vm, currentThread, nextInstruction, executedInstruction);
    }
  }

  @Override
  public void exceptionThrown(VM vm, ThreadInfo currentThread, ElementInfo thrownException) {
    checkExecutionPath(vm);
  }

  @Override
  public void searchConstraintHit(Search search) {
    if(!search.isEndState() && !search.isErrorState()) {
      checkExecutionPath(search.getVM());
    }
  }

  @Override
  public void stateAdvanced(Search search) {
    if(search.isEndState() && !search.isIgnoredState()) {
      checkExecutionPath(search.getVM());
    }
  }

  private void checkExecutionPath(VM vm) {
    PathCondition pc = PathCondition.getPC(vm);
    
    PathCondition pcNew = null;
    if(pc != null) {
      pcNew = pc.make_copy();
    }
    
    State currentState = this.stateBuilder.build(pcNew);
    WorstCasePath currentWcPath = WorstCasePath.generateWorstCasePath(currentState, vm.getSystemState().getChoiceGenerator(), this.ctxManager);
    
    if(currentWcPath.compareTo(this.wcPath) > 0) {
      this.wcPath = currentWcPath;
    }
  }

  public WorstCasePath getWcPath() {
    return wcPath;
  }
  
  protected String getBaseFileName(CFG cfg) {
    String baseFileName = cfg.getFqMethodName().replaceAll("[^\\p{Alpha}]+","");
    if(jpfConf.hasValue("target.args")) //we assume that the single parameter denotes the input size
      baseFileName += "_inputsize_" + jpfConf.getString("target.args");
    return baseFileName;
  }
  
  protected void visualize(Path path, String wcPathFileName) {
    File wcPathFile = new File(wcPathFileName);
    logger.info("writing wc path to file: " + wcPathFile.getAbsolutePath());
    try(PrintWriter out = new PrintWriter(wcPathFile)) {
      out.println(path.toString());
    } catch (FileNotFoundException e) {
      logger.warning(e.getMessage());
    }
  }
  
  protected void visualize(CFG cfg, String outputFile) {
    CFGToDOT dotVis = new CFGToDOT();
    //Write dot file
    Graph dotGraph = dotVis.build(cfg, this.showInstrs);
    try {
      File dotFile = new File(outputFile);
      dotGraph.printGraph(new FileOutputStream(dotFile));
      logger.info("writing dot file to: " + dotFile.getAbsolutePath());
      try {
        //this will fail on windows likely -- we just catch the exception and continue
        CFGToDOT.dot2pdf(dotFile);
      } catch(Exception e) {
        logger.warning(e.getMessage());
      }
    } catch (FileNotFoundException e) {
      logger.warning(e.getMessage());
    }
  }
  
  private interface BlockProjector {
    public void process(Block condBlock, Decision dec, DecisionHistory currentHistory);
  }
  
  private class BlockSequenceProjector implements BlockProjector {
    private int sequenceNumber = 0;

    @Override
    public void process(Block condBlock, Decision dec, DecisionHistory currentHistory) {
      Integer choice = dec.getChoice();

      if(showBbSeq) {
        Covered cov = condBlock.getAttribute(Covered.class);
        if(cov == null) {
          cov = new Covered();
          condBlock.setAttribute(cov);
        }
        cov.addToSequence(sequenceNumber++, (choice == 1) ? 'T' : 'F');
      }
    }
  }
  
  private class DecisionUpdater implements BlockProjector {

    @Override
    public void process(Block condBlock, Decision dec, DecisionHistory currentHistory) {
      Integer choice = dec.getChoice();
      
      //TODO: alternatively, store ONE hashmap, that maps each decision
      //history to a choice. This is more succinct than
      //having the two separate true and false sets, but
      //probably also less flexible.
      DecisionCollection decColl = null;
      if(choice == 0) {//false branch
        decColl = condBlock.getAttribute(FalseDecisionCollection.class);
        if(decColl == null) {
          decColl = new FalseDecisionCollection();
          condBlock.setAttribute(decColl);
        }
      } else { //true branch, was if(choice == 1) before
          decColl = condBlock.getAttribute(TrueDecisionCollection.class);
          if(decColl == null) {
            decColl = new TrueDecisionCollection();
            condBlock.setAttribute(decColl);
          }
      }
//      else
//        throw new UnsupportedOperationException("Currently only support for binary choices, i.e, floating point comparisons are not supported yet");
      
      decColl.addHistory(currentHistory.copy());
    }
  }

  protected void projectPath(Path path, Collection<BlockProjector> projectors) {
    if(path.size() < 1)
      return;
    CFG cfg = null;
    Decision prevDecision = null;
    for(Decision dec : path) {
      
      DecisionHistory history = dec.generateCtxPreservingDecisionHistory(this.decisionHistorySize);
      
      BranchInstruction currInstruction = dec.getInstruction();
      //We optimize cfg extraction a bit here...
      if(prevDecision == null ||
          (prevDecision != null && !currInstruction.getMethodName().equals(prevDecision.getInstruction().getMethodName()))) {
        cfg = this.cfgExtractor.getCFG(currInstruction.getClassName(), currInstruction.getMethodName());
      }
      assert cfg != null;
      
      int instrIdx = currInstruction.getInstructionIndex();      
      Block condBlock = cfg.getBlockWithIndex(instrIdx);
      
      for(BlockProjector projector : projectors) {
        projector.process(condBlock, dec, history);
      }

      prevDecision = dec;
    }
  }
  
  protected void projectPath(Path path, BlockProjector projector) {
    projectPath(path, Collections.singleton(projector));
  }

  public abstract CFGGenerator getCFGGenerator(Config jpfConf);
  
  public abstract boolean visualize(Config jpfConf);
  public abstract File getVisualizationDir(Config jpfConf);
  public abstract boolean serialize(Config jpfConf);
  public abstract File getSerializationDir(Config jpfConf);
}
