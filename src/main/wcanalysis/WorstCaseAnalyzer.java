/*
 * MIT License
 *
 * Copyright (c) 2017 The ISSTAC Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package wcanalysis;

import java.awt.*;
import java.io.File;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFConfigException;
import gov.nasa.jpf.JPFShell;
import gov.nasa.jpf.listener.CoverageAnalyzer;
import wcanalysis.charting.DataCollection;
import wcanalysis.charting.DataSeries;
import wcanalysis.charting.WorstCaseChart;
import wcanalysis.fitting.FunctionFitter;
import wcanalysis.heuristic.HeuristicListener;
import wcanalysis.heuristic.HeuristicResultsPublisher;
import wcanalysis.heuristic.PathListener;
import wcanalysis.heuristic.PolicyGeneratorListener;
import wcanalysis.heuristic.PolicyResultsPublisher;
import wcanalysis.heuristic.ResultsPublisher;
import wcanalysis.heuristic.WorstCasePath;
import wcanalysis.heuristic.model.State;
import wcanalysis.heuristic.util.Util;

/**
 * @author Kasper Luckow
 */
public class WorstCaseAnalyzer implements JPFShell {

  private static final String POLICY_GEN_SIZE_CONF = "symbolic.worstcase.policy.inputsize";
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
    this.startAt = config.getInt(START_AT_CONF, 1);
    this.incr = config.getInt(INCR_CONF, 1);
    if (verbose) {
      this.auxDir = Util.createDirIfNotExist(rootDir, "verbose");
      this.policyDir = Util.createDirIfNotExist(auxDir, "policy");
      this.heuristicDir = Util.createDirIfNotExist(auxDir, "heuristic");
    }
  }

  @Override
  public void start(String[] args) {
    config.setProperty(PolicyGeneratorListener.SER_OUTPUT_PATH_CONF, serializedDir.getAbsolutePath());
    config.setProperty(HeuristicListener.SER_INPUT_PATH, serializedDir.getAbsolutePath());

    //Let's always show SEVERE log entries
    config.setProperty("log.level", "severe");

    //Setting this config will ouput the policy obtained from the worst case path of the HEURISTIC search (phase 2) 
    //-- here it will overwrite the previous policy
    //config.setProperty(HeuristicListener.SER_OUTPUT_PATH, serializedDir.getAbsolutePath());

    if (verbose) {
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

    int predictionModelSize = config.getInt(PREDICT_MODEL_SIZE_CONF, (int) (dataCollection.size() * 1.5));


    double xs[] = dataCollection.getX();
    double ys[] = dataCollection.getY();

    // Generate chart
    WorstCaseChart.ChartBuilder chartBuilder = new WorstCaseChart.ChartBuilder("Costs per input size",
        "Input Size",
        "Cost");
    Collection<DataSeries> predictionSeries = FunctionFitter.computePredictionSeries(xs, ys,
        predictionModelSize);

    for(DataSeries series : predictionSeries) {
      chartBuilder.addSeries(series);
    }

    DataSeries rawSeries = new DataSeries("Raw", xs.length);
    for(int i = 0; i < xs.length; i++) {
      rawSeries.add(xs[i], ys[i]);
    }
    chartBuilder.setRawSeries(rawSeries);

    WorstCaseChart chart = chartBuilder.build();
    chart.setPreferredSize(new Dimension(1024, 768));
    chart.pack();
    chart.setVisible(true);
  }

  private void getPolicy(Config jpfConf) {
    if (jpfConf.getBoolean(REUSE_POLICY_CONF, false)) // just skip if we reuse the policy already computed
      return;

    //We get an *array* of input sizes. There are two cases:
    //if there is only one value, then we just obtain the policy
    //for that input size and proceed to the heuristic search.
    //The other case is when there are two elements, denoting
    //the integer range with which policies are to be obtained
    //and *unified*
    int[] policyInputSizes = jpfConf.getIntArray(POLICY_GEN_SIZE_CONF);
    int inputSizeStart, inputSizeEnd;
    if (policyInputSizes.length > 2) {
      throw new JPFConfigException("Supply either one integer or two integers (format: a,b where " +
          "a<b) denoting the range" +
          " of the input sizes at which policies are computed (and unified). Set with config " +
          POLICY_GEN_SIZE_CONF);
    } else if (policyInputSizes.length == 2) {
      assert policyInputSizes[0] <= policyInputSizes[1];
      inputSizeStart = policyInputSizes[0];
      inputSizeEnd = policyInputSizes[1];
      logger.info("Unifying policies for input size " + inputSizeStart + "--" + inputSizeEnd);

      //Tell PolicyGeneratorListener to unify policies over the range
      jpfConf.setProperty(PolicyGeneratorListener.UNIFY_POLICIES_CONF, "true");
      logger.warning("Using unification of policies. Remember to delete old policy");

    } else { // must be policyInputSizes.length == 1
      inputSizeStart = inputSizeEnd = policyInputSizes[0];
    }
    for (int inputSize = inputSizeStart; inputSize <= inputSizeEnd; inputSize++) {
      if (verbose) {
        //apparently have to set this guy before instantiating the jpf object
        File coverageFile = new File(this.policyDir, "policy_coverage_input_size_" + inputSize + ".txt");
        jpfConf.setProperty("report.console.file", coverageFile.getAbsolutePath());
      }
      jpfConf.setProperty("target.args", "" + inputSize);
      JPF jpf = new JPF(jpfConf);
      jpf.addListener(new PolicyGeneratorListener(jpfConf, jpf)); //weird instantiation...

      if (verbose) {
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
      logger.info("Running policy generation for input size " + inputSize);
      //get policy
      runJPF(jpf);
    }
  }

  private DataCollection performAnalysis(Config jpfConf) {
    boolean noSolver = jpfConf.getBoolean(NO_SOLVER_HEURISTIC_CONF, false);
    if (noSolver) {
      jpfConf.setProperty("symbolic.dp", "no_solver");
    }
    int maxInput = jpfConf.getInt(MAX_INPUT_CONF);
    jpfConf.setProperty("report.console.class", HeuristicResultsPublisher.class.getName());

    DataCollection dataCollection = new DataCollection();

    for (int inputSize = this.startAt; inputSize <= maxInput; inputSize += this.incr) {//TODO: should maxInput be included?
      logger.info("Exploring with heuristic input size " + inputSize + "...");
      jpfConf.setProperty("target.args", "" + inputSize);
      JPF jpf = new JPF(jpfConf);
      HeuristicListener heuristic = new HeuristicListener(jpfConf, jpf);
      jpf.addListener(heuristic); //weird instantiation...

      //explore guided by policy
      long start = System.currentTimeMillis();
      runJPF(jpf);
      long end = System.currentTimeMillis();

      logger.info("Heuristic exploration at input size " + inputSize + " done. Took " + ((end - start) / 1000) + "s");

      WorstCasePath wcPath = heuristic.getWcPath();

      if (wcPath == null) {
        logger.severe("No worst case path found for input size " + inputSize);
      } else {
        State wcState = wcPath.getWCState();
        dataCollection.addDatapoint(inputSize, wcState.getWC());
      }
    }
    return dataCollection;
  }

  private void runJPF(JPF jpf) {
    try {
      jpf.run();
    } catch (Exception e) {
      throw new WCAException("jpf-core threw exception", e);
    }
  }
}
