package wcanalysis.heuristic.policy;

import java.util.Map;
import java.util.Set;

import wcanalysis.heuristic.BranchInstruction;
import wcanalysis.heuristic.Path;

/**
 * @author Kasper Luckow
 *
 */
public class HistoryLessBranchPolicy extends HistoryBasedBranchPolicy {
  
  private static final long serialVersionUID = -767196606158062320L;

  public static class Builder extends HistoryBranchPolicyBuilder<HistoryLessBranchPolicy> {

    public void addPolicy(BranchInstruction branch, int policyChoice) {
      super.addPolicy(branch, new Path(), policyChoice); // here we make an empty history, i.e. it is stateless
    }
    
    @Override
    public HistoryLessBranchPolicy build(Map<BranchInstruction, BranchPolicyStorage> branch2pol) {
      return new HistoryLessBranchPolicy(branch2pol);
    }
  }
  
  protected HistoryLessBranchPolicy(Map<BranchInstruction, BranchPolicyStorage> policy) {
    super(policy);
  }
  
  public Set<Integer> resolve(BranchInstruction branch) {
    return super.resolve(branch, new Path()); //again we match with empty history i.e. stateless matching
  }
}
