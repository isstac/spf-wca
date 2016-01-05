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

  public static class Builder extends BranchPolicyBuilder<HistoryLessBranchPolicy> {

    public void addPolicy(BranchInstruction branch, int policyChoice) {
      super.addPolicy(branch, new Path(), policyChoice); // here we make an empty history, i.e. it is stateless
    }
    
    @Override
    public HistoryLessBranchPolicy build(Map<BranchInstruction, Map<Path, Set<Integer>>> pol,
        Map<BranchInstruction, Map<Integer, Integer>> choice2counts) {
      return new HistoryLessBranchPolicy(pol, choice2counts);
    }
  }
  
  protected HistoryLessBranchPolicy(Map<BranchInstruction, Map<Path, Set<Integer>>> pol,
      Map<BranchInstruction, Map<Integer, Integer>> choice2counts) {
    super(pol, choice2counts);
  }
  
  public Set<Integer> resolve(BranchInstruction branch) {
    return super.resolve(branch, new Path()); //again we match with empty history i.e. stateless matching
  }
}
