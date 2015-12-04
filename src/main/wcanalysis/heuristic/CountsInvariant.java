package wcanalysis.heuristic;

/**
 * @author Kasper Luckow
 */
public interface CountsInvariant {
  public boolean apply(long trueCount, long falseCount);
}
