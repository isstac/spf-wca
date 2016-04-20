package wcanalysis.heuristic;

/**
 * @author Kasper Luckow
 *
 */
public interface HistoryGenerator {
  public Path generateHistoryFromDecIdx(WorstCasePath path, int decIdx);
}
