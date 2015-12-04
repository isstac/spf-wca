package wcanalysis;

import heuristic.HeuristicListener;
import heuristic.HeuristicResultsPublisher;
import heuristic.ResultsPublisher;
import heuristic.PathChoiceCounterListener;
import heuristic.PathListener;
import heuristic.PolicyResultsPublisher;
import heuristic.State;
import heuristic.util.Util;
import isstac.structure.serialize.JavaSerializer;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFShell;
import gov.nasa.jpf.listener.CoverageAnalyzer;
import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class WorstCaseAnalyzer implements JPFShell {

  private static final String HEURISTIC_SIZE_CONF = "symbolic.worstcase.policy.inputsize";
  private static final String MAX_INPUT_CONF = "symbolic.worstcase.input.max";
  private static final String VERBOSE_CONF = "symbolic.worstcase.verbose";
  private static final String OUTPUT_DIR_CONF = "symbolic.worstcase.outputpath";

  private static final String PREDICTION_MODEL_DATA_POINTS_CONF = "symbolic.worstcase.datapointsnum";

  private static final String PREDICT_MODEL_SIZE_CONF = "symbolic.worstcase.predictionmodel.size";
  private static final String MAX_INPUT_REQ_CONF = "symbolic.worstcase.req.maxinputsize";
  private static final String MAX_RES_REQ_CONF = "symbolic.worstcase.req.maxres";

  private final Logger logger;
  private final Config config;
  private final boolean verbose;

  private final File rootDir;
  private final File serializedDir;
  private File auxDir;
  private File policyDir;
  private File heuristicDir;
  
  public WorstCaseAnalyzer(Config config) {
    this.logger = JPF.getLogger(WorstCaseAnalyzer.class.getName());
    this.config = config;
    this.verbose = config.getBoolean(VERBOSE_CONF, true);
    this.rootDir = Util.createDirIfNotExist(config.getString(OUTPUT_DIR_CONF, ""));
    this.serializedDir = Util.createDirIfNotExist(rootDir, "serialized");
    if(verbose) {
      this.auxDir = Util.createDirIfNotExist(rootDir, "verbose");
      this.policyDir = Util.createDirIfNotExist(auxDir, "policy");
      this.heuristicDir = Util.createDirIfNotExist(auxDir, "heuristic");
    }
  }

  @Override
  public void start(String[] args) {
    config.setProperty(PathListener.SERIALIZER_CONF, JavaSerializer.class.getName());
    config.setProperty(PathChoiceCounterListener.SER_OUTPUT_PATH_CONF, serializedDir.getAbsolutePath());
    config.setProperty(HeuristicListener.SER_INPUT_PATH, serializedDir.getAbsolutePath());
    
    if(verbose) {
      config.setProperty(ResultsPublisher.SMTLIB_CONF, "true");
      config.setProperty(ResultsPublisher.OMEGA_CONF, "true");
      config.setProperty(PolicyResultsPublisher.RESULTS_DIR_CONF, policyDir.getAbsolutePath());
      config.setProperty(HeuristicResultsPublisher.RESULTS_DIR_CONF, heuristicDir.getAbsolutePath());
      
      config.setProperty(PathListener.SHOW_INSTRS_CONF, "false");
      config.setProperty(PathListener.SHOW_BB_SEQ_CONF, "true");
      
      File visDirHeurstic = Util.createDirIfNotExist(heuristicDir, "visualizations");
      config.setProperty(HeuristicListener.VIS_OUTPUT_PATH_CONF, visDirHeurstic.getAbsolutePath());
      
      File visDirPolicy = Util.createDirIfNotExist(policyDir, "visualizations");
      config.setProperty(PathChoiceCounterListener.VIS_OUTPUT_PATH_CONF, visDirPolicy.getAbsolutePath());
    }
    

    //Step 1: get the policy to guide the search. We will get this at the inputsize
    //corresponding to symbolic.worstcase.policy.inputsize
    getPolicy(config);
    logger.info("step 1 done");

    //Step 2: get "results" with exploration guided by policy obtained from step 1.
    //It continues from input size 0 to MAX_INPUT_CONF
    DataCollection dataCollection = performAnalysis(config);
    logger.info("step 2 done");

    int dataPointsNum = config.getInt(PREDICTION_MODEL_DATA_POINTS_CONF, 100);
    XYSeriesCollection dataset = computeSeries(dataCollection, dataPointsNum);
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

  private class TrendModelData {
    final TrendLine trendLine;
    final String desc;
    public TrendModelData(TrendLine trend, String desc) {
      this.trendLine = trend;
      this.desc = desc;
    }
  }
  
  private XYSeriesCollection computeSeries(DataCollection rawData, int numberOfDataPoints) {
    XYSeriesCollection dataset = new XYSeriesCollection();

    XYSeries rawSeries = new XYSeries("Raw");
    for(int i = 0; i < rawData.x.length; i++) //ugly conversion and ugly non-iterable
      rawSeries.add(rawData.x[i], rawData.y[i]);


    DecimalFormat df = new DecimalFormat("#.00000");
    Set<TrendModelData> trendLines = new HashSet<>();

    //The prediction models we are considering
    trendLines.add(new TrendModelData(new PolyTrendLine(1), "1st poly"));
    trendLines.add(new TrendModelData(new PolyTrendLine(2), "2nd poly"));
    trendLines.add(new TrendModelData(new PolyTrendLine(3), "3rd poly"));
    trendLines.add(new TrendModelData(new ExpTrendLine(), "exp"));
    trendLines.add(new TrendModelData(new PowerTrendLine(), "pow"));
    trendLines.add(new TrendModelData(new LogTrendLine(), "log"));
    
    HashMap<TrendModelData, XYSeries> trend2series = new HashMap<>();
    
    for(TrendModelData trendData : trendLines) {
      trendData.trendLine.setValues(rawData.y, rawData.x);
      trend2series.put(trendData, new XYSeries(trendData.desc + ": "  + 
          trendData.trendLine.getFunction() + " (r^2="+df.format(trendData.trendLine.getRSquared()) + ")"));
    }

    int predictionModelSize = config.getInt(PREDICT_MODEL_SIZE_CONF, (int)(rawData.size*1.5));
    double[] xPredict = new double[predictionModelSize];
    System.arraycopy(rawData.x, 0, xPredict, 0, rawData.x.length);
    for(int i = rawData.x.length; i < predictionModelSize; i++)
      xPredict[i] = xPredict[i-1] + 1.0;

    for(int i = 0; i < predictionModelSize; i++) {
      double x = xPredict[i];
      for(TrendModelData trendData : trendLines) {
        XYSeries series = trend2series.get(trendData);
        series.add(x, trendData.trendLine.predict(x));
      }
    }
    
    dataset.addSeries(rawSeries);
    for(XYSeries series : trend2series.values()) {
      dataset.addSeries(series);
    }
    return dataset;
  }

  private void getPolicy(Config jpfConf) {
    int policyInputSize = jpfConf.getInt(HEURISTIC_SIZE_CONF);
    if(verbose) {
      //apparently have to set this guy before instantiating the jpf object
      File coverageFile = new File(this.policyDir, "policy_coverage_input_size_" + policyInputSize + ".txt");
      jpfConf.setProperty("report.console.file", coverageFile.getAbsolutePath());
    }
    jpfConf.setProperty("target.args", ""+policyInputSize);
    JPF jpf = new JPF(jpfConf);
    jpf.addListener(new PathChoiceCounterListener(jpfConf, jpf)); //weird instantiation...
    
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
    int maxInput = jpfConf.getInt(MAX_INPUT_CONF);
    jpfConf.setProperty("report.console.class", HeuristicResultsPublisher.class.getName());
    
    DataCollection dataCollection = new DataCollection(maxInput + 1);

    for(int inputSize = 0; inputSize <= maxInput; inputSize++) {//TODO: should maxInput be included?
      System.out.println("Exploring with heuristic input size " + inputSize);
      jpfConf.setProperty("target.args", ""+inputSize);
      JPF jpf = new JPF(jpfConf);
      HeuristicListener heuristic = new HeuristicListener(jpfConf, jpf);
      jpf.addListener(heuristic); //weird instantiation...

      //explore guided by policy
      jpf.run();
      State wcState = heuristic.getWcState();
      dataCollection.addDatapoint(inputSize, wcState.getDepth());
    }
    return dataCollection;
  }
}
