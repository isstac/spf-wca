package wcanalysis.heuristic;

import isstac.structure.cfg.CFG;
import isstac.structure.cfg.CFGGenerator;
import isstac.structure.cfg.CachingCFGGenerator;
import isstac.structure.cfg.util.CFGToDOT;
import wcanalysis.heuristic.ContextManager.CGContext;
import wcanalysis.heuristic.model.DepthState;
import wcanalysis.heuristic.model.State;
import wcanalysis.heuristic.model.StateBuilder;
import wcanalysis.heuristic.policy.HistoryBasedPolicy;
import wcanalysis.heuristic.policy.Policy;
import wcanalysis.heuristic.policy.PolicyGenerator;
import wcanalysis.heuristic.policy.PolicyManager;
import wcanalysis.heuristic.util.PathVisualizer;
import wcanalysis.heuristic.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;

import att.grappa.Graph;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
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
  
  private Logger logger = JPF.getLogger(PathListener.class.getName());

  /*
   * Configuration
   */
  //JPF conf strings
  //Measured methods
  public final static String MEASURED_METHODS = "symbolic.heuristic.measuredmethods";
  public final static String SYMBOLIC_METHODS = "symbolic.method";
  protected Set<String> measuredMethods;
  protected Set<String> symbolicMethods;
  
  public final static String POLICY_GENERATOR_CLS_CONF = "symbolic.wc.policygenerator";
  public final static String HISTORY_SIZE_CONF = "symbolic.wc.policy.history.size";
  protected final static int DEF_HISTORY_SIZE = 0; 

  //Visualization
  public final static String SHOW_INSTRS_CONF = "symbolic.wc.visualizer.showinstructions";  
  
  //Notion of worst case
  public final static String WORST_CASE_STATE_BLDR_CONF = "symbolic.wc.statebuilder";
  
  /*
   * State
   */
  //Conf state
  
  private final Config jpfConf;
  //Visualization
  private File visDir;
  private final boolean showInstrs;
  
  //State
  private StateBuilder stateBuilder;
  protected WorstCasePath wcPath;
  protected ContextManager ctxManager;
  
  protected PolicyGenerator<?> policyGenerator;
  protected WorstCasePath.Builder worstCasePathBuilder;
  
  private PolicyManager policyManager;
  //TODO: History should not be set here -- it is related to the policy (historyless, stateful, etc).
  //in fact the path measure computation is policy dependent! Extract path measure computation from WorstCasePath someday...
  private int historySize;
  
  private Policy policy;
  
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
    
    this.policyManager = new PolicyManager(this.getPolicyBaseDir(jpfConf));
    if(this.visualize(this.jpfConf))
      this.visDir = this.getVisualizationDir(this.jpfConf);
    
    this.showInstrs = jpfConf.getBoolean(SHOW_INSTRS_CONF, false);
    
    this.ctxManager = new ContextManager();
    
    //Initialize state
    if(jpfConf.hasValue(WORST_CASE_STATE_BLDR_CONF)) {
      this.stateBuilder = jpfConf.getInstance(WORST_CASE_STATE_BLDR_CONF, StateBuilder.class);
    } else
      this.stateBuilder = new DepthState.DepthStateBuilder();
    
    if(jpfConf.hasValue(POLICY_GENERATOR_CLS_CONF)) {
      this.policyGenerator = jpfConf.getInstance(POLICY_GENERATOR_CLS_CONF, PolicyGenerator.class);
      this.worstCasePathBuilder = new WorstCasePath.Builder(ctxManager);
    } else {
      HistoryBasedPolicy.Builder histGenerator = new HistoryBasedPolicy.Builder();
      if(jpfConf.hasValue(HISTORY_SIZE_CONF)) {
        this.historySize = jpfConf.getInt(HISTORY_SIZE_CONF);
        histGenerator.setMaxHistorySize(historySize);
        this.worstCasePathBuilder = new WorstCasePath.Builder(ctxManager, historySize);
      } else { // adaptive by default
        histGenerator.setAdaptive(true);
        this.worstCasePathBuilder = new WorstCasePath.Builder(ctxManager);
      }
      this.policyGenerator = histGenerator;
    }

    this.wcPath = null;
  }
  
  private Set<String> getMeasuredMethods(Config jpfConf) {
    String[] measMeth = jpfConf.getStringArray(MEASURED_METHODS, jpfConf.getStringArray(SYMBOLIC_METHODS));
    return Util.extractSimpleMethodNames(measMeth);
  }
  
  private Set<String> getSymbolicMethods(Config jpfConf) {
    String[] symMeth = jpfConf.getStringArray(SYMBOLIC_METHODS, jpfConf.getStringArray(MEASURED_METHODS));
    return Util.extractSimpleMethodNames(symMeth); 
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
  
  @Override
  public void choiceGeneratorAdvanced (VM vm, ChoiceGenerator<?> currentCG) {
    //TODO: check if there is a difference between the following cg and currentCg passed to this method
    ChoiceGenerator<?> cg = vm.getSystemState().getChoiceGenerator();
    if(cg instanceof PCChoiceGenerator) {
      if(isInMeasuredMethodCallStack(vm, vm.getCurrentThread()))
        this.stateBuilder.handleChoiceGeneratorAdvanced(vm, currentCG);
      CGContext ctx = this.ctxManager.getContext(cg);
      if(ctx == null) {        
        this.ctxManager.addContext(cg, vm.getCurrentThread().getCallerStackFrame(), this.stateBuilder.copy());
      } else {
        this.stateBuilder = ctx.stateBuilder.copy();
      }
    }
  }
  
  public Policy getComputedPolicy() {
    return this.policy;
  }
  
  @Override
  public void searchFinished(Search search) {
    checkExecutionPath(search.getVM());
    searchFinished(this.wcPath);
  }
  
  public void searchFinished(WorstCasePath wcPath) {
    if(wcPath == null)
      return;
    
    this.policy = this.policyGenerator.generate(this.measuredMethods, wcPath);
    
    if(serialize(jpfConf)) {
      try {
        this.policyManager.savePolicy(this.policy);
      } catch (IOException e) {
        logger.severe(e.getMessage());
        throw new RuntimeException(e);
      }
    }
    
    if(visualize(jpfConf)) {      
      //we project the worst case path on the cfg and output it
      String[] classpaths = jpfConf.getProperty("classpath").split(",");
      String pathSeparator = System.getProperties().getProperty("path.separator");
      String[] javaCl = System.getProperties().getProperty("java.class.path").split(pathSeparator);
      String[] bootCl = System.getProperties().getProperty("sun.boot.class.path").split(pathSeparator);
      String[] stdLibCl = (String[])ArrayUtils.addAll(javaCl, bootCl);
      String[] completeCl = (String[])ArrayUtils.addAll(classpaths, stdLibCl);
      
      CFGGenerator cfgGen = new CachingCFGGenerator(completeCl);
      PathProjector sequenceVisualizer = new PathVisualizer(cfgGen);
      Collection<CFG> transformedCFGs = sequenceVisualizer.projectPath(wcPath);
      if(transformedCFGs != null) {
        for(CFG cfg : transformedCFGs) {
          visualize(cfg, this.showInstrs, new File(this.visDir, getBaseFileName(cfg) + ".pdf"));
        }
      }
      
      //output the path to text file
      String tgtOutputfileName = "";
      for(String measuredMethod : measuredMethods)
        tgtOutputfileName += measuredMethod;
      
      // Prune to prevent FileNotFoundException (File name too long) (max 255 chars)
      if (tgtOutputfileName.length()>244)
    	  tgtOutputfileName = tgtOutputfileName.substring(0, 243);
      
      visualize(wcPath, new File(this.visDir, "wcpath_" + tgtOutputfileName + "_inputsize_" + jpfConf.getString("target.args") + ".txt"));
      visualize(policy, new File(this.visDir, "policy_" + tgtOutputfileName + "_inputsize_" + jpfConf.getString("target.args") + ".txt"));
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
    //TODO: check iserrorstate here as was done originally?
    if(search.isEndState()) {
      checkExecutionPath(search.getVM());
    }
  }
  
  @Override
  public void objectCreated(VM vm, ThreadInfo ti, ElementInfo ei) {
	  if(isInMeasuredMethodCallStack(vm, vm.getCurrentThread())) {
		  this.stateBuilder.handleObjectCreated(vm, ti, ei);
	  }
  }

  @Override
  public void objectReleased(VM vm, ThreadInfo ti, ElementInfo ei) {
	  if(isInMeasuredMethodCallStack(vm, vm.getCurrentThread())) {
		  this.stateBuilder.handleObjectReleased(vm, ti, ei);
	  }
  }
  
  @Override
  public void methodEntered (VM vm, ThreadInfo ti, MethodInfo mi) {
	  if(isInMeasuredMethodCallStack(vm, vm.getCurrentThread())) {
		  this.stateBuilder.handleMethodEntered(vm, ti, mi);
	  }
  }
  
  @Override
  public void methodExited (VM vm, ThreadInfo ti, MethodInfo mi) {
	  if(isInMeasuredMethodCallStack(vm, vm.getCurrentThread())) {
		  this.stateBuilder.handleMethodExited(vm, ti, mi);
	  }
  }

  private void checkExecutionPath(VM vm) {
    PathCondition pc = PathCondition.getPC(vm);
    
    PathCondition pcNew = null;
    if(pc != null) {
      pcNew = pc.make_copy();
    }
    State currentState = this.stateBuilder.build(pcNew);
    WorstCasePath currentWcPath = this.worstCasePathBuilder.build(currentState, vm.getSystemState().getChoiceGenerator());
//    if(currentWcPath.size() == 20)
//      System.out.println(currentWcPath.toString());
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
  
  protected void visualize(Policy pol, File polFile) {
    logger.info("writing policy to file: " + polFile.getAbsolutePath());
    try(PrintWriter out = new PrintWriter(polFile)) {
      out.println(pol.toString());
    } catch (FileNotFoundException e) {
      logger.warning(e.getMessage());
    }    
  }
  
  protected void visualize(Path path, File wcPathFile) {
    logger.info("writing wc path to file: " + wcPathFile.getAbsolutePath());
    try(PrintWriter out = new PrintWriter(wcPathFile)) {
      out.println(path.toString());
    } catch (FileNotFoundException e) {
      logger.warning(e.getMessage());
    }    
  }
  
  protected void visualize(CFG cfg, boolean showInstrs, File outputFile) {
    CFGToDOT dotVis = new CFGToDOT();
    //Write dot file
    Graph dotGraph = dotVis.build(cfg, showInstrs);
    try {
      dotGraph.printGraph(new FileOutputStream(outputFile));
      logger.info("writing dot file to: " + outputFile.getAbsolutePath());
      try {
        //this will fail on windows -- we just catch the exception and continue
        CFGToDOT.dot2pdf(outputFile);
      } catch(Exception e) {
        logger.warning(e.getMessage());
      }
    } catch (FileNotFoundException e) {
      logger.warning(e.getMessage());
    }
  }
  
  public abstract boolean visualize(Config jpfConf);
  public abstract File getVisualizationDir(Config jpfConf);
  public abstract boolean serialize(Config jpfConf);
  public abstract File getPolicyBaseDir(Config jpfConf);
}
