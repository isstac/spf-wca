package wcanalysis.heuristic.policy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wcanalysis.heuristic.BranchInstruction;
import wcanalysis.heuristic.Path;

/**
 * @author Kasper Luckow
 *
 */
public class HistoryBasedBranchPolicy implements BranchPolicy {

  private static final long serialVersionUID = 4478984808375928385L;

  public static class Builder implements BranchPolicyBuilder<HistoryBasedBranchPolicy> {
    private final Map<BranchInstruction, TrieStorage.Builder> branch2polbuilder;
    public Builder() {
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
    public HistoryBasedBranchPolicy build() {
      Map<BranchInstruction, BranchPolicyStorage> branch2pol = new HashMap<>();
      for(Map.Entry<BranchInstruction, TrieStorage.Builder> entry : branch2polbuilder.entrySet()) {
        branch2pol.put(entry.getKey(), entry.getValue().build());
      }
      return build(branch2pol);
    }
    
    public HistoryBasedBranchPolicy build(Map<BranchInstruction, BranchPolicyStorage> pol) {
      return new HistoryBasedBranchPolicy(pol);
    }
  }
  
  private final Map<BranchInstruction, BranchPolicyStorage> pol;
  
  private HistoryBasedBranchPolicy(Map<BranchInstruction, BranchPolicyStorage> pol) {
    this.pol = pol;
  }
  
  @Override
  public Set<Integer> resolve(BranchInstruction branch, Path history) {
    if(pol.containsKey(branch)) {
      return pol.get(branch).getChoices(history);
    }
    return new HashSet<>();
  }

  @Override
  public int getCountsForChoice(BranchInstruction branch, int choice) {
    if(pol.containsKey(branch)) {
      return pol.get(branch).getCountsForChoice(choice);
    }
    return 0;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    List<BranchInstruction> bList = new ArrayList<>(pol.keySet());
    Collections.sort(bList, new Comparator<BranchInstruction>() {
      @Override
      public int compare(BranchInstruction o1, BranchInstruction o2) {
        return Integer.compare(o1.getLineNumber(), o2.getLineNumber());
      }
    });
    
    for(BranchInstruction branch : bList) {
      sb.append(branch.toString()).append(":\n");
      BranchPolicyStorage histories = pol.get(branch);
      sb.append(histories.toString()).append("\n");
    }
    return sb.toString();
  }
}
