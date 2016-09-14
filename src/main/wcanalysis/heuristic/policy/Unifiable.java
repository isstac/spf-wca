package wcanalysis.heuristic.policy;

/**
 * @author Kasper Luckow
 */
public interface Unifiable {
  public void unifyWith(BranchPolicy other) throws PolicyUnificationException;
}
