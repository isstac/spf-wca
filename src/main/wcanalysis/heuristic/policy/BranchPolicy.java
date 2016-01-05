package wcanalysis.heuristic.policy;

import java.io.Serializable;

import wcanalysis.heuristic.BranchInstruction;

/**
 * @author Kasper Luckow
 *
 */
public interface BranchPolicy extends Serializable {
  public int getCountsForChoice(BranchInstruction branch, int choice);
}
