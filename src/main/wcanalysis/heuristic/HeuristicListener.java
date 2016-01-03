package wcanalysis.heuristic;

import isstac.structure.cfg.Block;
import isstac.structure.cfg.CFG;
import isstac.structure.cfg.CFGGenerator;
import isstac.structure.cfg.CachingCFGGenerator;
import wcanalysis.heuristic.DecisionCollection.FalseDecisionCollection;
import wcanalysis.heuristic.DecisionCollection.TrueDecisionCollection;
import wcanalysis.heuristic.Policy.Resolution;
import wcanalysis.heuristic.Policy.ResolutionType;
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
  private Policy policy;
  
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
   // countsInvariants.put("benchmarks.java15.util.Arrays.mergeSort([Ljava/lang/Object;[Ljava/lang/Object;III)V", block2Invariant);
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
      Resolution res = policy.resolve(cg, ctxManager);
      
      boolean ignoreState = ((PCChoiceGenerator)cg).getNextChoice() != res.choice;
      
      switch(res.type) {
      case UNRESOLVED:
        this.unresolvedChoices++;
        break;
      case NEW_CHOICE:
        this.newChoices++;
        break;
      case PERFECT:
        if(ignoreState)
          this.resolvedPerfectChoices++;
        break;
      case HISTORY:
        if(ignoreState)
          this.resolvedHistoryChoices++;
        break;
      case INVARIANT:
        if(ignoreState)
          this.resolvedInvariantChoices++;
        break;
       default:
         throw new IllegalStateException("Unhandled resolution type");
      }
      
      if(ignoreState)
        vm.getSystemState().setIgnored(true);      
    }
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
