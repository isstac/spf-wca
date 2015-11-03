package wcanalysis;

import heuristic.HeuristicListener;
import heuristic.HeuristicResultsPublisher;
import heuristic.PathChoiceCounterListener;
import heuristic.State;
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

  public WorstCaseAnalyzer(Config config) {
    this.logger = JPF.getLogger(WorstCaseAnalyzer.class.getName());
    this.config = config;
    this.verbose = config.getBoolean(VERBOSE_CONF);
  }

  @Override
  public void start(String[] args) {
    File root = createDirIfNotExist(config.getString(OUTPUT_DIR_CONF, ""));

    File serializedDir = createDirIfNotExist(root, "serialized");
    config.setProperty("symbolic.cfg.serializer", JavaSerializer.class.getName());
    config.setProperty("symbolic.cfg.serializer.outputpath", serializedDir.getAbsolutePath());

    if(verbose) {
      createDirIfNotExist(root, "serialized");
      File auxDir = createDirIfNotExist(root, "aux");
      config.setProperty("report.console.heuristics.resultsdir", auxDir.getAbsolutePath());
      config.setProperty("report.console.heuristics.smtlib", "true");
      config.setProperty("symbolic.cfg.visualizer.showinstructions", "false");
      config.setProperty("symbolic.cfg.visualizer.showseq", "true");
      File vizDir = createDirIfNotExist(auxDir, "visualizations");
      config.setProperty("symbolic.cfg.visualizer.outputpath", vizDir.getAbsolutePath());
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
    jpfConf.setProperty("target.args", ""+jpfConf.getInt(HEURISTIC_SIZE_CONF));
    JPF jpf = new JPF(jpfConf);
    jpf.addListener(new PathChoiceCounterListener(jpfConf, jpf)); //weird instantiation...

    //get policy
    jpf.run();
  }

  private DataCollection performAnalysis(Config jpfConf) {
    int maxInput = jpfConf.getInt(MAX_INPUT_CONF);
    jpfConf.setProperty("report.console.class", HeuristicResultsPublisher.class.getName());

    DataCollection dataCollection = new DataCollection(maxInput);

    for(int inputSize = 0; inputSize < maxInput; inputSize++) {//TODO: should maxInput be included?
      jpfConf.setProperty("target.args", ""+inputSize);
      JPF jpf = new JPF(jpfConf);
      HeuristicListener heuristic = new HeuristicListener(jpfConf, jpf);
      jpf.addListener(heuristic); //weird instantiation...

      //explore guided by policy
      jpf.run();
      State<?> wcState = heuristic.getWcState();
      dataCollection.addDatapoint(inputSize, wcState.getDepth());
    }
    return dataCollection;
  }

  private static File createDirIfNotExist(String root) {
    File sub = new File(root);
    if(!sub.exists())
      sub.mkdirs();
    return sub;
  }

  private static File createDirIfNotExist(File root, String subDir) {
    File sub = new File(root, subDir);
    if(!sub.exists())
      sub.mkdirs();
    return sub;
  }
}
