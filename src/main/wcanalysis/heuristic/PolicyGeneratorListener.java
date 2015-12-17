package wcanalysis.heuristic;

import isstac.structure.cfg.Block;
import isstac.structure.cfg.CFG;
import isstac.structure.cfg.CFGGenerator;
import isstac.structure.cfg.CachingCFGGenerator;
import isstac.structure.cfg.util.CFGToDOT;
import isstac.structure.cfg.util.DotAttribute;
import wcanalysis.heuristic.DecisionCollection.FalseDecisionCollection;
import wcanalysis.heuristic.DecisionCollection.TrueDecisionCollection;
import wcanalysis.heuristic.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import att.grappa.Attribute;
import att.grappa.Graph;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 */
public class PolicyGeneratorListener extends PathListener {
  
  public final static String VIS_OUTPUT_PATH_CONF = "symbolic.wc.policy.visualizer.outputpath";
  public final static String SER_OUTPUT_PATH_CONF = "symbolic.wc.policy.serializer.outputpath";
  public final static String SER_OUTPUT_PATH_DEF = "./ser/policy";
  
  //for statistics
  private long newChoices = 0;

  public PolicyGeneratorListener(Config jpfConf, JPF jpf) {
    super(jpfConf, jpf);
  }
  
  @Override
  public void stateAdvanced(Search search) {
    super.stateAdvanced(search);
    ChoiceGenerator<?> cg = search.getVM().getSystemState().getChoiceGenerator();
    if(cg instanceof PCChoiceGenerator) {
      this.newChoices++;
    }
  }
  
  public long getNumberOfNewChoices() {
    return newChoices;
  }

  @Override
  public CFGGenerator getCFGGenerator(Config jpfConf) {
    String[] classpaths = jpfConf.getProperty("classpath").split(",");
    return new CachingCFGGenerator(classpaths);
  }

  @Override
  public boolean visualize(Config jpfConf) {
    return jpfConf.hasValue(VIS_OUTPUT_PATH_CONF);
  }

  @Override
  public File getVisualizationDir(Config jpfConf) {
    File out = Util.createDirIfNotExist(jpfConf.getString(VIS_OUTPUT_PATH_CONF, "./vis/policy"));
    return out;
  }

  @Override
  public boolean serialize(Config jpfConf) {
    //we serialize i.e. store the policy always!
    return true; 
  }

  @Override
  public File getSerializationDir(Config jpfConf) {
    File out = Util.createDirIfNotExist(jpfConf.getString(SER_OUTPUT_PATH_CONF, SER_OUTPUT_PATH_DEF));
    return out;
  }
}
