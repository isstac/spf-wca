package wcanalysis.heuristic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Kasper Luckow
 *
 */
public abstract class BranchPolicyBuilder<T extends HistoryBasedBranchPolicy> {
  private final Map<BranchInstruction, Map<Path, Set<Integer>>> pol;
  private final Map<BranchInstruction, Map<Integer, Integer>> choice2counts;
  
  public BranchPolicyBuilder() {
    this.pol = new HashMap<>();
    this.choice2counts = new HashMap<>();
  }
  
  public void addPolicy(BranchInstruction branch, Path history, int policyChoice) {
    Map<Path, Set<Integer>> branchPol = pol.get(branch);
    if(branchPol == null) {
      branchPol = new HashMap<>();
      pol.put(branch, branchPol);
    }

    Set<Integer> decisions = branchPol.get(history);
    if(decisions == null) {
      decisions = new HashSet<>();
      branchPol.put(history, decisions);
    }
    decisions.add(policyChoice);
    
    //add the count
    Map<Integer, Integer> choices = choice2counts.get(branch);
    if(choices == null) {
      choices = new HashMap<>();
      choice2counts.put(branch, choices);
    }
    if(!choices.containsKey(policyChoice)) {
      choices.put(policyChoice, 1);
    } else {
      int currentCount = choices.get(policyChoice);
      choices.put(policyChoice, ++currentCount);
    }
  }
  public T build() {
    return build(this.pol, this.choice2counts);
  }
  
  public abstract T build(Map<BranchInstruction, Map<Path, Set<Integer>>> pol, 
      Map<BranchInstruction, Map<Integer, Integer>> choice2counts);
}
