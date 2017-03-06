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

package wcanalysis.heuristic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.report.Reporter;
import gov.nasa.jpf.report.Statistics;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import wcanalysis.heuristic.model.State;
import wcanalysis.heuristic.util.OmegaConverter;
import wcanalysis.heuristic.util.SMTLibConverter;

/**
 * @author Kasper Luckow
 */
public abstract class ResultsPublisher extends Publisher {

  private Logger logger = JPF.getLogger(ResultsPublisher.class.getName());
  
  private File file;
  private FileOutputStream fos;
  private int cfgInputSize;
  
  private File baseDir;
  private boolean generateSMTLibFormat = false;
  private boolean generateOmegaFormat = false;
  
  public static final String CFG_INPUT_SIZE_CONF = "report.console.wc.cfginputsize";
  
  public static final String SMTLIB_CONF = "report.console.wc.smtlib";
  public static final String OMEGA_CONF = "report.console.wc.omega";
  
  private Config conf;
  public ResultsPublisher(Config conf, Reporter reporter) {
    super(conf, reporter);
    this.conf = conf;
    this.cfgInputSize = conf.getInt(CFG_INPUT_SIZE_CONF, -1);
    String resultsFile = conf.getString("target") + ".csv";

    this.generateSMTLibFormat = conf.getBoolean(SMTLIB_CONF, false);
    this.generateOmegaFormat = conf.getBoolean(OMEGA_CONF, false);
    baseDir = getResultsDir(conf);
    
    file = new File(baseDir, resultsFile);
  }

  @Override
  public String getName() {
    return "console";
  }

  @Override
  protected void openChannel(){

    if(file != null) {
      try {        
        fos = new FileOutputStream(file, true);
        out = new PrintWriter(fos);
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  protected void closeChannel() {
    if (fos != null){
      out.close();
    }
  }

  @Override
  public void publishStart() {
    super.publishStart();
  }

  @Override
  public void publishFinished() {
    super.publishFinished();
  }

  public synchronized void printStatistics (PrintWriter pw){

  }

  @Override
  public void publishStatistics() {

  }
  
  private static boolean isFileEmpty(File file) {
    try(BufferedReader br = new BufferedReader(new FileReader(file))) {
        if(br.readLine() == null) {
          return true;
        } else
          return false;
    } catch (IOException e1) {
      e1.printStackTrace();
      return false;
    }
  }
  
  @Override
  public void publishResult() {
    PathListener pathListener = getListener();
    if(pathListener != null) {
      
      Statistics stat = reporter.getStatistics();
      
      WorstCasePath wcPath = pathListener.getWcPath();
      if(wcPath == null)
        return;
      State wcState = wcPath.getWCState();
      if(isFileEmpty(file)) {
        //write header
        
        String stateCSVHeader = wcState.getCSVHeader();
        if(!stateCSVHeader.endsWith(","))
          stateCSVHeader += ",";
        String header = "inputSize," + 
            ((this.cfgInputSize >= 0) ? "cfgInputSize," : "") + 
            stateCSVHeader +
            "analysisTime,mem,paths," +
            getCustomCSVColumnHeader() +
            ",wcConstraint";
        
        out.println(header);
      }
      
      out.print(conf.getString("target.args") + ",");
      if(cfgInputSize >= 0)
        out.print(cfgInputSize + ",");
      String stateCSVRes = wcState.getCSV();
      if(!stateCSVRes.endsWith(","))
        stateCSVRes += ",";
      out.print(stateCSVRes);
      out.print((reporter.getElapsedTime()/1000) + ",");
      out.print((stat.maxUsed >> 20) + ",");
      out.print(stat.endStates + ",");
      out.print(this.getCustomCSVColumnData() + ",");
      
      if(wcState.getPathCondition() == null) {
        logger.severe("Worst case result state does not have associated constraints! Is it the worst case state at all?");
        out.println("CONSTRAINT N/A");
      } else {
        PathCondition pc = wcState.getPathCondition();
        if(generateSMTLibFormat) {
          String smtLibPc = new SMTLibConverter().convert(pc);
          writeConstraintsFile(smtLibPc, ".smt2");
        }
        if(generateOmegaFormat) {
          String omegaConstraints = new OmegaConverter().convert(pc);
          writeConstraintsFile(omegaConstraints, ".omega");
        }
        String pcStr = (pc != null && pc.header != null) ? pc.header.stringPC() : "";
        out.println(pcStr);
      }
    }

  }
  
  private void writeConstraintsFile(String constraints, String fileExt) {
    try(FileWriter fw = new FileWriter(new File(baseDir, outputFileBaseName() + fileExt))) {
      fw.write(constraints);
      fw.flush();
      fw.close();
    } catch (IOException e) {
      e.printStackTrace();
      logger.severe(e.getMessage());
    }
  }
  
  private String outputFileBaseName() {
    return conf.getString("target") + "_" + conf.getString("target.args");
  }
  
  //This is super ugly. Quick fix
  protected abstract PathListener getListener();
  protected abstract String getCustomCSVColumnHeader();
  protected abstract String getCustomCSVColumnData();
  protected abstract File getResultsDir(Config conf);
}
