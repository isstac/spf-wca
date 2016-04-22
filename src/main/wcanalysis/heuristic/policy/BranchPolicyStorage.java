package wcanalysis.heuristic.policy;

import java.io.Serializable;
import java.util.Set;

import wcanalysis.heuristic.Path;

/**
 * @author Kasper Luckow
 *
 */
public interface BranchPolicyStorage extends Serializable {
  public Set<Integer> getChoicesForLongestPrefix(Path history);
  public int getCountsForChoice(int choice);
}
