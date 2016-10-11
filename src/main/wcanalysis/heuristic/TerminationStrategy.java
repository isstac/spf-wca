package wcanalysis.heuristic;

import gov.nasa.jpf.search.Search;

/**
 * @author Kasper Luckow
 */
public interface TerminationStrategy {

  boolean terminateAnalysis(Search searchState, HeuristicStatistics statistics);
}
