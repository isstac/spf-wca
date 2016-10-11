package wcanalysis.heuristic;

import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;

import gov.nasa.jpf.search.Search;

/**
 * @author Kasper Luckow
 */
public class TimeBoundedTerminationStrategy implements TerminationStrategy {

  private final Stopwatch stopwatch;
  private static final int TIME_BOUND = 15;
  private static final TimeUnit TIME_UNIT = TimeUnit.MINUTES;

  public TimeBoundedTerminationStrategy() {
    this.stopwatch = Stopwatch.createStarted();
  }

  @Override
  public boolean terminateAnalysis(Search searchState, HeuristicStatistics statistics) {
    return this.stopwatch.elapsed(TIME_UNIT) > TIME_BOUND;
  }
}
