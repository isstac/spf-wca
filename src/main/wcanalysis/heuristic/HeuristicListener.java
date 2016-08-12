package wcanalysis.heuristic;

import wcanalysis.WorstCaseAnalyzer;
import wcanalysis.heuristic.Resolution.ResolutionType;
import wcanalysis.heuristic.policy.ChoiceListener;
import wcanalysis.heuristic.policy.Policy;
import wcanalysis.heuristic.policy.PolicyManager;
import wcanalysis.heuristic.policy.PolicyManagerException;
import wcanalysis.heuristic.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public class HeuristicListener extends PathListener {
  
  private Logger logger = JPF.getLogger(HeuristicListener.class.getName());
  
  public final static String VIS_OUTPUT_PATH_CONF = "symbolic.wc.heuristic.visualizer.outputpath";
  public final static String SER_OUTPUT_PATH = "symbolic.wc.heuristic.serializer.outputpath";
  
  public static final String SER_INPUT_PATH = "symbolic.wc.heuristic.serializer.inputpath";
  
  //statistics
  private long unresolvedChoices = 0;
  private long resolvedPerfectChoices = 0;
  private long resolvedHistoryChoices = 0;
  private long resolvedInvariantChoices = 0;
  private long newChoices = 0;
  
  private boolean policiesEnabled;
  private Policy policy;
  
  public HeuristicListener(Config jpfConf, JPF jpf) {
    super(jpfConf, jpf);

    policiesEnabled = jpfConf.getBoolean(WorstCaseAnalyzer.ENABLE_POLICIES, WorstCaseAnalyzer.ENABLE_POLICIES_DEF);

    if (policiesEnabled) {
      PolicyManager policyManager = new PolicyManager(new File(getSerializedInputDir(jpfConf)));

      try {
        this.policy = policyManager.loadPolicy(measuredMethods, Policy.class);
      } catch (IOException | PolicyManagerException e) {
        logger.severe(e.getMessage());
        throw new RuntimeException(e);
      }
    }
  }

  public HeuristicListener(Config jpfConf, Policy policy) {
    super(jpfConf, null);

    policiesEnabled = jpfConf.getBoolean(WorstCaseAnalyzer.ENABLE_POLICIES, WorstCaseAnalyzer.ENABLE_POLICIES_DEF);
    if(policiesEnabled) {
      this.policy = policy;
    }
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
    	
      Resolution res;
      if (policiesEnabled)
    	  res = policy.resolve(cg, ctxManager);
      else
    	  res = new Resolution(-1, ResolutionType.NEW_CHOICE);
      
      boolean ignoreState = false;
      if(!res.type.equals(ResolutionType.UNRESOLVED) && !res.type.equals(ResolutionType.NEW_CHOICE)) {
        ignoreState = ((PCChoiceGenerator)cg).getNextChoice() != res.choice;
        if(ignoreState)
          vm.getSystemState().setIgnored(true);
      }
      
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
      
      if(!ignoreState) {
        if(policy instanceof ChoiceListener) {
          PCChoiceGenerator pccg = (PCChoiceGenerator)cg;
          ((ChoiceListener)policy).choiceMade(pccg, pccg.getNextChoice());
        }
      }
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
  public File getPolicyBaseDir(Config jpfConf) {
    File out = Util.createDirIfNotExist(jpfConf.getString(SER_OUTPUT_PATH, "./ser/policy"));
    return out;
  }
}
