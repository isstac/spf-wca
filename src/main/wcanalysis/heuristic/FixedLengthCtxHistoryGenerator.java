package wcanalysis.heuristic;

/**
 * @author Kasper Luckow
 *
 */
public class FixedLengthCtxHistoryGenerator implements HistoryGenerator {

  private final int maxHistorySize;
  
  public FixedLengthCtxHistoryGenerator(int maxHistorySize) {
    this.maxHistorySize = maxHistorySize;
  }
  
  @Override
  public Path generateHistoryFromDecIdx(WorstCasePath path, int decIdx) {
    return path.generateCtxPreservingHistoryFromIdx(decIdx, maxHistorySize);
  }
}
