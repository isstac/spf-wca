package wcanalysis.heuristic.policy;

import wcanalysis.heuristic.BranchInstruction;
import wcanalysis.heuristic.Path;

/**
 * @author Kasper Luckow
 *
 */
public interface BranchPolicyBuilder<T extends BranchPolicy> {
 
  public void addPolicy(Path history, int policyChoice);
  public T build();
}
