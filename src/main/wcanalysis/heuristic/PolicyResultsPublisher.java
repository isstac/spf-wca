package wcanalysis.heuristic;

import java.io.File;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.report.Reporter;
import wcanalysis.heuristic.util.Util;

/**
 * @author Kasper Luckow
 *
 */
public class PolicyResultsPublisher extends ResultsPublisher {

  public static final String RESULTS_DIR_CONF = "report.console.wc.policy.resultsdir";
  
  public PolicyResultsPublisher(Config conf, Reporter reporter) {
    super(conf, reporter);
  }

  @Override
  protected String getCustomCSVColumnHeader() {
    return "newChoicesNum";
  }

  @Override
  protected String getCustomCSVColumnData() {
    PolicyGeneratorListener listener = reporter.getVM().getNextListenerOfType(PolicyGeneratorListener.class, null);
    if(listener == null)
      return "";
    return Long.toString(listener.getNumberOfNewChoices());
  }

  @Override
  protected File getResultsDir(Config conf) {
    return Util.createDirIfNotExist(conf.getString(RESULTS_DIR_CONF, "./results/policy"));
  }
  
  @Override
  protected PathListener getListener() {
    return reporter.getVM().getNextListenerOfType(PolicyGeneratorListener.class, null);
  }
}
