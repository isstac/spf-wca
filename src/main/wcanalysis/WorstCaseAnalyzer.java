package wcanalysis;

import wcanalysis.charting.DataCollection;
import wcanalysis.charting.WorstCaseChart;
import wcanalysis.fitting.ExpTrendLine;
import wcanalysis.fitting.FunctionFitter;
import wcanalysis.fitting.LogTrendLine;
import wcanalysis.fitting.NLogTrendLine;
import wcanalysis.fitting.PolyTrendLine;
import wcanalysis.fitting.PowerTrendLine;
import wcanalysis.fitting.TrendLine;
import wcanalysis.heuristic.HeuristicListener;
import wcanalysis.heuristic.HeuristicResultsPublisher;
import wcanalysis.heuristic.PolicyGeneratorListener;
import wcanalysis.heuristic.PathListener;
import wcanalysis.heuristic.PolicyResultsPublisher;
import wcanalysis.heuristic.ResultsPublisher;
import wcanalysis.heuristic.WorstCasePath;
import wcanalysis.heuristic.model.State;
import wcanalysis.heuristic.util.Util;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFShell;
import gov.nasa.jpf.listener.CoverageAnalyzer;

/**
 * @author Kasper Luckow
 */
public class WorstCaseAnalyzer implements JPFShell {

  private static final String HEURISTIC_SIZE_CONF = "symbolic.worstcase.policy.inputsize";
  private static final String MAX_INPUT_CONF = "symbolic.worstcase.input.max";
  private static final String VERBOSE_CONF = "symbolic.worstcase.verbose";
  private static final String OUTPUT_DIR_CONF = "symbolic.worstcase.outputpath";

  private static final String PREDICT_MODEL_SIZE_CONF = "symbolic.worstcase.predictionmodel.size";
  private static final String MAX_INPUT_REQ_CONF = "symbolic.worstcase.req.maxinputsize";
  private static final String MAX_RES_REQ_CONF = "symbolic.worstcase.req.maxres";
  
  private static final String NO_SOLVER_HEURISTIC_CONF = "symbolic.worstcase.heuristic.nosolver";
  
  private static final String REUSE_POLICY_CONF = "symbolic.worstcase.reusepolicy";
  
  private static final String INCR_CONF = "symbolic.worstcase.increment";
  private static final String START_AT_CONF = "symbolic.worstcase.startat";
  
  public static final String ENABLE_POLICIES = "symbolic.wc.heuristic.enablepolicies";
  public static final boolean ENABLE_POLICIES_DEF = true;

  private static final Logger logger = JPF.getLogger(WorstCaseAnalyzer.class.getName());
  static {
    logger.setLevel(Level.ALL);
  }
  
  private final Config config;
  private final boolean verbose;

  private final File rootDir;
  private final File serializedDir;
  private File auxDir;
  private File policyDir;
  private File heuristicDir;
  private int incr;
  private int startAt;
  
  public WorstCaseAnalyzer(Config config) {
    this.config = config;
    this.verbose = config.getBoolean(VERBOSE_CONF, true);
    this.rootDir = Util.createDirIfNotExist(config.getString(OUTPUT_DIR_CONF, ""));
    this.serializedDir = Util.createDirIfNotExist(rootDir, "serialized");
    this.startAt= config.getInt(START_AT_CONF,1);
    this.incr = config.getInt(INCR_CONF,1);
    if(verbose) {
      this.auxDir = Util.createDirIfNotExist(rootDir, "verbose");
      this.policyDir = Util.createDirIfNotExist(auxDir, "policy");
      this.heuristicDir = Util.createDirIfNotExist(auxDir, "heuristic");
    }
  }

  @Override
  public void start(String[] args) {
    config.setProperty(PolicyGeneratorListener.SER_OUTPUT_PATH_CONF, serializedDir.getAbsolutePath());
    config.setProperty(HeuristicListener.SER_INPUT_PATH, serializedDir.getAbsolutePath());
    
    //Setting this config will ouput the policy obtained from the worst case path of the HEURISTIC search (phase 2) 
    //-- here it will overwrite the previous policy
    //config.setProperty(HeuristicListener.SER_OUTPUT_PATH, serializedDir.getAbsolutePath());
    
    if(verbose) {
      config.setProperty(ResultsPublisher.SMTLIB_CONF, "true");
      config.setProperty(ResultsPublisher.OMEGA_CONF, "true");
      config.setProperty(PolicyResultsPublisher.RESULTS_DIR_CONF, policyDir.getAbsolutePath());
      config.setProperty(HeuristicResultsPublisher.RESULTS_DIR_CONF, heuristicDir.getAbsolutePath());
      
      config.setProperty(PathListener.SHOW_INSTRS_CONF, "false");
      
      File visDirHeurstic = Util.createDirIfNotExist(heuristicDir, "visualizations");
      config.setProperty(HeuristicListener.VIS_OUTPUT_PATH_CONF, visDirHeurstic.getAbsolutePath());
      
      File visDirPolicy = Util.createDirIfNotExist(policyDir, "visualizations");
      config.setProperty(PolicyGeneratorListener.VIS_OUTPUT_PATH_CONF, visDirPolicy.getAbsolutePath());
    }
    

    //Step 1: get the policy to guide the search. We will get this at the inputsize
    //corresponding to symbolic.worstcase.policy.inputsize
    if (config.getBoolean(WorstCaseAnalyzer.ENABLE_POLICIES, WorstCaseAnalyzer.ENABLE_POLICIES_DEF))
    	getPolicy(config);
    logger.info("step 1 done");

    //Step 2: get "results" with exploration guided by policy obtained from step 1.
    //It continues from input size 0 to MAX_INPUT_CONF
    DataCollection dataCollection = performAnalysis(config);
    logger.info("step 2 done");

    int predictionModelSize = config.getInt(PREDICT_MODEL_SIZE_CONF, (int)(dataCollection.size()*1.5));
    XYSeriesCollection dataset = FunctionFitter.computeSeries(dataCollection, predictionModelSize);
    logger.info("Computing prediction models done");

    WorstCaseChart chart;
    if(config.hasValue(MAX_RES_REQ_CONF)) //We have a defined "budget" requirement
      chart = new WorstCaseChart(dataset, config.getDouble(MAX_INPUT_REQ_CONF), config.getDouble(MAX_RES_REQ_CONF));
    else
      chart = new WorstCaseChart(dataset);
    logger.info("Creating chart done");

    //Let's show the panel
    chart.pack();
    RefineryUtilities.centerFrameOnScreen(chart);
    chart.setVisible(true);
  }

  private void getPolicy(Config jpfConf) {
    if(jpfConf.getBoolean(REUSE_POLICY_CONF, false)) // just skip if we reuse the policy already computed
      return;
      
    int policyInputSize = jpfConf.getInt(HEURISTIC_SIZE_CONF);
    if(verbose) {
      //apparently have to set this guy before instantiating the jpf object
      File coverageFile = new File(this.policyDir, "policy_coverage_input_size_" + policyInputSize + ".txt");
      jpfConf.setProperty("report.console.file", coverageFile.getAbsolutePath());
    }
    jpfConf.setProperty("target.args", ""+policyInputSize);
    JPF jpf = new JPF(jpfConf);
    jpf.addListener(new PolicyGeneratorListener(jpfConf, jpf)); //weird instantiation...
    
    if(verbose) {
      //We store (structural) coverage metrics for the exhaustive exploration when the policy was extracted
      //We can use it for providing some confidence in how "good" the policy is --
      //it does not account for infeasible paths however, so branch coverage might be a bit distorted      
      jpfConf.setProperty("coverage.show_methods", "true");
      jpfConf.setProperty("coverage.show_bodies", "false");
      jpfConf.setProperty("coverage.exclude_handlers", "false");
      jpfConf.setProperty("coverage.show_branches", "true");
      jpfConf.setProperty("coverage.loaded_only", "true");
      jpfConf.setProperty("coverage.show_requirements", "false");
      
      jpf.addListener(new CoverageAnalyzer(jpfConf, jpf));
    }
    //get policy
    jpf.run();
  }

  private DataCollection performAnalysis(Config jpfConf) {
    boolean noSolver = jpfConf.getBoolean(NO_SOLVER_HEURISTIC_CONF, false);
    if(noSolver) {
      jpfConf.setProperty("symbolic.dp", "no_solver");
    }
    int maxInput = jpfConf.getInt(MAX_INPUT_CONF);
    jpfConf.setProperty("report.console.class", HeuristicResultsPublisher.class.getName());
    
    DataCollection dataCollection = new DataCollection();

    for(int inputSize = this.startAt; inputSize <= maxInput; inputSize += this.incr) {//TODO: should maxInput be included?
      logger.info("Exploring with heuristic input size " + inputSize + "...");
      jpfConf.setProperty("target.args", ""+inputSize);
      JPF jpf = new JPF(jpfConf);
      HeuristicListener heuristic = new HeuristicListener(jpfConf, jpf);
      jpf.addListener(heuristic); //weird instantiation...

      //explore guided by policy
      long start = System.currentTimeMillis();
      jpf.run();
      long end = System.currentTimeMillis();
      
      logger.info("Heuristic exploration at input size " + inputSize + " done. Took " + ((end-start)/1000) + "s");
      
      WorstCasePath wcPath = heuristic.getWcPath();

      if(wcPath == null) {
        logger.severe("No worst case path found for input size " + inputSize);
      } else {
        State wcState = wcPath.getWCState();
        dataCollection.addDatapoint(inputSize, wcState.getWC());
      }
    }
    return dataCollection;
  }
}
