package wcanalysis.heuristic;

/**
 * @author Kasper Luckow
 *
 */
public class MaxLengthCtxHistoryGenerator implements HistoryGenerator {

  @Override
  public Path generateHistoryFromDecIdx(WorstCasePath path, int decIdx) {
    return path.generateCtxPreservingHistoryFromIdx(decIdx);
  }
}
