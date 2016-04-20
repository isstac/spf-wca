package wcanalysis.heuristic.policy;

import java.util.HashMap;
import java.util.Map;
import wcanalysis.heuristic.BranchInstruction;
import wcanalysis.heuristic.Path;

/**
 * @author Kasper Luckow
 *
 */
public abstract class HistoryBranchPolicyBuilder<T extends HistoryBasedBranchPolicy> implements BranchPolicyBuilder<T> {
  private final Map<BranchInstruction, TrieStorage.Builder> branch2polbuilder;
  
  public HistoryBranchPolicyBuilder() {
    branch2polbuilder = new HashMap<>();
  }
  
  @Override
  public void addPolicy(BranchInstruction branch, Path history, int policyChoice) {
    TrieStorage.Builder bldr = this.branch2polbuilder.get(branch);
    if(bldr == null) {
      bldr = new TrieStorage.Builder();
      this.branch2polbuilder.put(branch, bldr);
    }
    
    bldr.put(history, policyChoice);
  }
  @Override
  public T build() {
    Map<BranchInstruction, BranchPolicyStorage> branch2pol = new HashMap<>();
    for(Map.Entry<BranchInstruction, TrieStorage.Builder> entry : branch2polbuilder.entrySet()) {
      branch2pol.put(entry.getKey(), entry.getValue().build());
    }
    
    return build(branch2pol);
  }
  
  public abstract T build(Map<BranchInstruction, BranchPolicyStorage> branch2pol);
}
