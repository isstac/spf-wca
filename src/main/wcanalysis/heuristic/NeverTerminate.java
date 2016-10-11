package wcanalysis.heuristic;

import gov.nasa.jpf.search.Search;

/**
 * @author Kasper Luckow
 */
public class NeverTerminate implements TerminationStrategy {

  @Override
  public boolean terminateAnalysis(Search searchState, HeuristicStatistics statistics) {
    return false;
  }
}
