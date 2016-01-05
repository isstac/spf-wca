package wcanalysis.heuristic;

import java.io.Serializable;

/**
 * @author Kasper Luckow
 *
 */
public interface BranchPolicy extends Serializable {
  public int getCountsForChoice(BranchInstruction branch, int choice);
}
