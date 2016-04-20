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
  public int getCountsForChoice(BranchInstruction branch, int choice);
  public Set<Integer> resolve(BranchInstruction branch, Path history);
}
