package wcanalysis.heuristic.policy;

import java.io.Serializable;
import java.util.Set;

import wcanalysis.heuristic.BranchInstruction;
import wcanalysis.heuristic.Path;

/**
 * @author Kasper Luckow
 *
 */
public interface BranchPolicy extends Serializable {
  public int getCountsForChoice(int choice);
  public Set<Integer> resolve(Path history);
  public int getMaxHistorySize();
}
