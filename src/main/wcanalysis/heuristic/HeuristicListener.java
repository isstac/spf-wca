package wcanalysis.heuristic;

import isstac.structure.cfg.Block;
import isstac.structure.cfg.CFG;
import isstac.structure.cfg.CFGGenerator;
import isstac.structure.cfg.CachingCFGGenerator;
import wcanalysis.heuristic.DecisionCollection.FalseDecisionCollection;
import wcanalysis.heuristic.DecisionCollection.TrueDecisionCollection;
import wcanalysis.heuristic.util.Util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.util.Predicate;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public class HeuristicListener extends PathListener {
  
  //private Logger logger = JPF.getLogger(HeuristicListener.class.getName());
  
  public final static String VIS_OUTPUT_PATH_CONF = "symbolic.wc.heuristic.visualizer.outputpath";
  public final static String SER_OUTPUT_PATH = "symbolic.wc.heuristic.serializer.outputpath";
  
  public static final String SER_INPUT_PATH = "symbolic.wc.heuristic.serializer.inputpath";
  
  //statistics
  private long unresolvedChoices = 0;
  private long resolvedPerfectChoices = 0;
  private long resolvedHistoryChoices = 0;
  private long resolvedInvariantChoices = 0;
  private long newChoices = 0;
  
  private Map<String, Map<Long, CountsInvariant>> countsInvariants = new HashMap<>();
  
  public HeuristicListener(Config jpfConf, JPF jpf) {
    super(jpfConf, jpf);
    
    //TODO: This stuff is just experimental!
    //we should make a way of specifying in the jpf file the invariants.
    //should be fairly easy to make support for
    Map<Long, CountsInvariant> block2Invariant = new HashMap<>();
    CountsInvariant mergeSortInvariant = new CountsInvariant() {
      @Override
      public boolean apply(long trueCount, long falseCount) {
        return trueCount < falseCount;
      }
    };
    //16 is the basic block id
    block2Invariant.put(16L, mergeSortInvariant);
    countsInvariants.put("benchmarks.java15.util.Arrays.mergeSort([Ljava/lang/Object;[Ljava/lang/Object;III)V", block2Invariant);
  }
  
  private String getSerializedInputDir(Config jpfConfig) {
    String policyInputPath = jpfConfig.getString(SER_INPUT_PATH);
    if(policyInputPath == null)
      policyInputPath = jpfConfig.getString(PolicyGeneratorListener.SER_OUTPUT_PATH_CONF, 
          PolicyGeneratorListener.SER_OUTPUT_PATH_DEF);
    return policyInputPath;
  }
  
  @Override
  public void choiceGeneratorAdvanced (VM vm, ChoiceGenerator<?> currentCG) {
    super.choiceGeneratorAdvanced(vm, currentCG);
    ChoiceGenerator<?> cg = vm.getSystemState().getChoiceGenerator();
    if(cg instanceof PCChoiceGenerator) {
      
      Instruction ifInstr = ((PCChoiceGenerator) cg).getInsn();
      CFG cfg = getCfgExtractor().getCFG(ifInstr);      
      
      Block bl = cfg.getBlockWithIndex(ifInstr.getInstructionIndex());      
      
      boolean takeFalseBranch = bl.hasAttribute(FalseDecisionCollection.class);
      boolean takeTrueBranch = bl.hasAttribute(TrueDecisionCollection.class);
      int currChoice = ((PCChoiceGenerator) cg).getNextChoice();
      
      //Check if decision can be resolved "perfectly" i.e. only counts on one decision:
      if(ignoreState(takeTrueBranch, takeFalseBranch, currChoice)) {
        vm.getSystemState().setIgnored(true);
        this.resolvedPerfectChoices++;
        return;
      } else if(matchState(takeTrueBranch, takeFalseBranch, currChoice)) {
        State state = getCurrentState();
        HeuristicDecisionCounts counts = state.getBlockCounts(bl);
        counts.incrementForChoice(currChoice);
        return;
      }
      
      //counts on both branches
      if(takeTrueBranch && takeFalseBranch) { 
        //We have to call getPrev here on the previous decision, because in reality we haven't
        //made the prevDec decision yet -- specifically we may backtrack according to the decision
        //sets based on history. Agreed, this is pretty ugly, but seems we have to set prevDec in
        //choicegeneratoradvanced for everything to work correctly with JPFs exploration
        DecisionHistory currDecisionHistory = null;
        if(currDec != null)
          currDecisionHistory = currDec.generateCtxPreservingDecisionHistory(decisionHistorySize);
        else //bogus
          currDecisionHistory = new DecisionHistory(0); 
        
        FalseDecisionCollection falseDecColl = bl.getAttribute(FalseDecisionCollection.class);
        boolean matchFalseHistories = falseDecColl.contains(currDecisionHistory);
        
        TrueDecisionCollection trueDecColl = bl.getAttribute(TrueDecisionCollection.class);
        boolean matchTrueHistories = trueDecColl.contains(currDecisionHistory);
        
        //Check if decision can be resolved if the current decision history only matches recorded
        //decisions for one branch
        if(ignoreState(matchTrueHistories, matchFalseHistories, currChoice)) {
          vm.getSystemState().setIgnored(true);
          this.resolvedHistoryChoices++;
          return;
        } else if(matchState(matchTrueHistories, matchFalseHistories, currChoice)) {
          State state = getCurrentState();
          HeuristicDecisionCounts counts = state.getBlockCounts(bl);
          counts.incrementForChoice(currChoice);
          return;
        }
        
        //Check if decision can be resolved by checking
        //the count invariant for that decision
        State state = getCurrentState();
        HeuristicDecisionCounts counts = state.getBlockCounts(bl);
        Map<Long, CountsInvariant> blId2countsInv = countsInvariants.get(cfg.getFqMethodName());
        if(blId2countsInv != null) {
          CountsInvariant countsInvariant = blId2countsInv.get(bl.getId());
          if(countsInvariant != null) {
            int tmpFalseCount = counts.falseCount;
            int tmpTrueCount = counts.trueCount;
            if(((PCChoiceGenerator) cg).getNextChoice() == 1) //the decision will be true branch
              tmpTrueCount++;
            else
              tmpFalseCount++;
            
            if(!countsInvariant.apply(tmpTrueCount, tmpFalseCount)) {
              //Invariant does not hold, so we prune
              this.resolvedInvariantChoices++;
              vm.getSystemState().setIgnored(true);
              return;
            }
          }
        }
        
        counts.incrementForChoice(currChoice);
        this.unresolvedChoices++;
      } else if((!bl.hasAttribute(FalseDecisionCollection.class) && !bl.hasAttribute(TrueDecisionCollection.class)) ||
          (!takeTrueBranch && !takeFalseBranch)) { //branches we did not explore when obtaining counts
        this.newChoices++;
      }
    }
  }
  
  private boolean ignoreState(boolean takeTrueBranch, boolean takeFalseBranch, int currentChoice) {
    return (takeFalseBranch && !takeTrueBranch && (currentChoice == 1) || //1 is true branch
        (takeTrueBranch && !takeFalseBranch && currentChoice == 0)); //0 is false branch
  }
  
  private boolean matchState(boolean takeTrueBranch, boolean takeFalseBranch, int currentChoice) {
    return (takeTrueBranch && !takeFalseBranch && (currentChoice == 1) || //1 is true branch
        (!takeTrueBranch && takeFalseBranch && currentChoice == 0)); //0 is false branch
  }

  public long getNumberOfUnresolvedChoices() {
    return this.unresolvedChoices;
  }

  public long getNumberOfInvariantResolvedChoices() {
    return this.resolvedInvariantChoices;
  }
  
  public long getNumberOfPerfectlyResolvedChoices() {
    return this.resolvedPerfectChoices;
  }
  
  public long getNumberOfHistoryResolvedChoices() {
    return this.resolvedHistoryChoices;
  }
  
  public long getTotalNumberOfResolvedChoices() {
    return this.resolvedInvariantChoices +
        this.resolvedHistoryChoices +
        this.resolvedPerfectChoices;
  }

  public long getNumberOfNewChoices() {
    return this.newChoices;
  }

  @Override
  public CFGGenerator getCFGGenerator(Config jpfConf) {
    String[] classpaths = jpfConf.getProperty("classpath").split(",");
    return CachingCFGGenerator
        .buildFromDeserializedCFGs(classpaths, 
            getSerializedInputDir(jpfConf), getSerializer());
  }

  @Override
  public boolean visualize(Config jpfConf) {
    return jpfConf.hasValue(VIS_OUTPUT_PATH_CONF);
  }

  @Override
  public File getVisualizationDir(Config jpfConf) {
    File out = Util.createDirIfNotExist(jpfConf.getString(VIS_OUTPUT_PATH_CONF, "./vis/heuristic"));
    return out;
  }

  @Override
  public boolean serialize(Config jpfConf) {
    return jpfConf.hasValue(SER_OUTPUT_PATH);
  }

  @Override
  public File getSerializationDir(Config jpfConf) {
    File out = Util.createDirIfNotExist(jpfConf.getString(SER_OUTPUT_PATH, "./ser/policy"));
    return out;
  }
}
