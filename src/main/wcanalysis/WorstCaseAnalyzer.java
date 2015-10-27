package wcanalysis;

import heuristic.HeuristicListener;
import heuristic.HeuristicResultsPublisher;
import heuristic.PathChoiceCounterListener;
import heuristic.State;
import isstac.structure.serialize.JavaSerializer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
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
  
  private static final String PREDICTION_MODEL_DATA_POINTS_CONF = "symbolic.worstcase.datapoints";
  
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
      File auxDir = createDirIfNotExist(root, "aux_files");
      config.setProperty("report.console.heuristics.resultsdir", auxDir.getAbsolutePath());
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
    
    WorstCaseChart chart = new WorstCaseChart(dataset);
    logger.info("Creating chart done");
    chart.pack();
    RefineryUtilities.centerFrameOnScreen(chart);
    chart.setVisible(true);
  }
  
  private XYSeriesCollection computeSeries(DataCollection rawData, int numberOfDataPoints) {
    XYSeriesCollection dataset = new XYSeriesCollection();
    
    //ugly -- make a loop instead...
    PolyTrendLine poly1 = new PolyTrendLine(1);
    final XYSeries poly1series = new XYSeries("1st poly");
    poly1.setValues(rawData.y, rawData.x);
    
    PolyTrendLine poly2 = new PolyTrendLine(2);
    final XYSeries poly2series = new XYSeries("2nd poly");
    poly2.setValues(rawData.y, rawData.x);
    
    PolyTrendLine poly3 = new PolyTrendLine(3);
    final XYSeries poly3series = new XYSeries("3rd poly");
    poly3.setValues(rawData.y, rawData.x);
    
    for(int i = 0; i < rawData.size; i++) {
      poly1series.add(rawData.x[i], poly1.predict(rawData.x[i]));
      poly2series.add(rawData.x[i], poly2.predict(rawData.x[i]));
      poly3series.add(rawData.x[i], poly3.predict(rawData.x[i]));
    }

    dataset.addSeries(poly1series);
    dataset.addSeries(poly2series);
    dataset.addSeries(poly3series);
    
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
