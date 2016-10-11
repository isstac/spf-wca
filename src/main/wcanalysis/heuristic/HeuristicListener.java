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

  public static final String UNIFY_POLICIES_CONF = "symbolic.wc.heuristic.unifypolicies";
  public static final boolean UNIFY_POLICIES_CONF_DEF = false;

  public final static String VIS_OUTPUT_PATH_CONF = "symbolic.wc.heuristic.visualizer.outputpath";
  public final static String SER_OUTPUT_PATH = "symbolic.wc.heuristic.serializer.outputpath";
  
  public static final String SER_INPUT_PATH = "symbolic.wc.heuristic.serializer.inputpath";

  // Termination
  public static final String TERMINATION_STRATEGY_CONF = "symbolic.wc.heuristic.termination";
  public static final String DEFAULT_TERMINATION_STRATEGY = NeverTerminate.class.getName();

  private final TerminationStrategy terminationStrategy;


  //statistics
  private HeuristicStatistics statistics = new HeuristicStatistics();
  
  private boolean policiesEnabled;
  private Policy heuristicPolicy;
  
  public HeuristicListener(Config jpfConf, JPF jpf) {
    super(jpfConf, jpf);

    policiesEnabled = jpfConf.getBoolean(WorstCaseAnalyzer.ENABLE_POLICIES, WorstCaseAnalyzer.ENABLE_POLICIES_DEF);

    if (policiesEnabled) {
      PolicyManager policyManager = new PolicyManager(new File(getSerializedInputDir(jpfConf)));

      try {
        this.heuristicPolicy = policyManager.loadPolicy(measuredMethods, Policy.class);
      } catch (IOException | PolicyManagerException e) {
        logger.severe(e.getMessage());
        throw new RuntimeException(e);
      }
    }

    // Set termination strategy
    this.terminationStrategy = jpfConf.getInstance(TERMINATION_STRATEGY_CONF,
        TerminationStrategy.class, DEFAULT_TERMINATION_STRATEGY);
  }

  public HeuristicListener(Config jpfConf, Policy heuristicPolicy) {
    super(jpfConf, null);

    policiesEnabled = jpfConf.getBoolean(WorstCaseAnalyzer.ENABLE_POLICIES, WorstCaseAnalyzer.ENABLE_POLICIES_DEF);
    if(policiesEnabled) {
      this.heuristicPolicy = heuristicPolicy;
    }

    // Set termination strategy
    this.terminationStrategy = jpfConf.getInstance(TERMINATION_STRATEGY_CONF,
        TerminationStrategy.class, DEFAULT_TERMINATION_STRATEGY);
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
    	  res = heuristicPolicy.resolve(cg, ctxManager);
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
        statistics.unresolvedChoices++;
        break;
      case NEW_CHOICE:
        statistics.newChoices++;
        break;
      case PERFECT:
        if(ignoreState)
          statistics.resolvedPerfectChoices++;
        break;
      case HISTORY:
        if(ignoreState)
          statistics.resolvedHistoryChoices++;
        break;
      case INVARIANT:
        if(ignoreState)
          statistics.resolvedInvariantChoices++;
        break;
       default:
         throw new IllegalStateException("Unhandled resolution type");
      }
      
      if(!ignoreState) {
        if(heuristicPolicy instanceof ChoiceListener) {
          PCChoiceGenerator pccg = (PCChoiceGenerator)cg;
          ((ChoiceListener) heuristicPolicy).choiceMade(pccg, pccg.getNextChoice());
        }
      }
    }
  }
  
  public HeuristicStatistics getStatistics() {
    return this.statistics;
  }


  @Override
  protected void checkExecutionPath(VM vm) {
    super.checkExecutionPath(vm);

    if(terminationStrategy.terminateAnalysis(vm.getSearch(), this.statistics)) {
      vm.getSearch().terminate();
    }
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
  public boolean unifyPolicies(Config jpfConf) {
    return jpfConf.getBoolean(UNIFY_POLICIES_CONF, UNIFY_POLICIES_CONF_DEF);
  }

  @Override
  public File getPolicyBaseDir(Config jpfConf) {
    File out = Util.createDirIfNotExist(jpfConf.getString(SER_OUTPUT_PATH, "./ser/heuristicPolicy"));
    return out;
  }
}
