package wcanalysis.heuristic.policy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import wcanalysis.heuristic.BranchInstruction;
import wcanalysis.heuristic.Path;

/**
 * @author Kasper Luckow
 *
 */
public abstract class AdaptiveBranchPolicyBuilder {
//  private final Map<BranchInstruction, AdaptivePolicy.Builder> polBuilder;
//  
//  public AdaptiveBranchPolicyBuilder() {
//    this.polBuilder = new HashMap<>();
//  }
//  
//  public void addPolicy(BranchInstruction branch, Path history, int policyChoice) {
//    AdaptivePolicy.Builder branchPolBuilder = polBuilder.get(branch);
//    if(branchPolBuilder == null) {
//      branchPolBuilder = new AdaptivePolicy.Builder();
//      polBuilder.put(branch, branchPolBuilder);
//    }
//
//    branchPolBuilder.put(history, policyChoice);
//  }
//  
//  public T build() {
//    return build(this.pol, this.choice2counts);
//  }
//  
//  public abstract T build(Map<BranchInstruction, Map<Path, Set<Integer>>> pol, 
//      Map<BranchInstruction, Map<Integer, Integer>> choice2counts);
}
