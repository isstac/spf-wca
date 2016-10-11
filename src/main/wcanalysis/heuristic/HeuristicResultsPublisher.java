package wcanalysis.heuristic;

import java.io.File;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.report.Reporter;
import wcanalysis.heuristic.util.Util;

/**
 * @author Kasper Luckow
 */
public class HeuristicResultsPublisher extends ResultsPublisher {
  
  public static final String RESULTS_DIR_CONF = "report.console.wc.heuristic.resultsdir";
  
  public HeuristicResultsPublisher(Config conf, Reporter reporter) {
    super(conf, reporter);
  }

  @Override
  protected String getCustomCSVColumnHeader() {
    return "resolvedChoicesNum,resolvedPerfectChoicesNum,resolvedHistoryChoicesNum,resolvedInvariantChoicesNum,unresolvedChoicesNum,newChoicesNum";
  }

  @Override
  protected String getCustomCSVColumnData() {
    HeuristicListener listener = reporter.getVM().getNextListenerOfType(HeuristicListener.class, null);
    if(listener == null)
      return "";

    HeuristicStatistics statistics = listener.getStatistics();
    String out = (statistics.resolvedHistoryChoices + statistics.resolvedInvariantChoices +
        statistics.resolvedPerfectChoices) + "," +
        statistics.resolvedPerfectChoices + "," +
        statistics.resolvedHistoryChoices + "," +
        statistics.resolvedInvariantChoices + "," +
        statistics.unresolvedChoices + "," +
        statistics.newChoices;
    return out;
  }
  
  @Override
  protected File getResultsDir(Config conf) {
    return Util.createDirIfNotExist(conf.getString(RESULTS_DIR_CONF, "./results/heuristic"));
  }

  @Override
  protected PathListener getListener() {
    return reporter.getVM().getNextListenerOfType(HeuristicListener.class, null);
  }
}
