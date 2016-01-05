package wcanalysis.heuristic;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Kasper Luckow
 *
 */
public class HistoryBasedBranchPolicy implements BranchPolicy {

  private static final long serialVersionUID = 4478984808375928385L;

  public static class Builder extends BranchPolicyBuilder<HistoryBasedBranchPolicy> {
    @Override
    public HistoryBasedBranchPolicy build(Map<BranchInstruction, Map<Path, Set<Integer>>> pol,
        Map<BranchInstruction, Map<Integer, Integer>> choice2counts) {
      return new HistoryBasedBranchPolicy(pol, choice2counts);
    }
  }
  
  private final Map<BranchInstruction, Map<Path, Set<Integer>>> pol;
  private final Map<BranchInstruction, Map<Integer, Integer>> choice2counts;
  
  protected HistoryBasedBranchPolicy(Map<BranchInstruction, Map<Path, Set<Integer>>> pol, 
      Map<BranchInstruction, Map<Integer, Integer>> choice2counts) {
    this.pol = pol;
    this.choice2counts = choice2counts;
  }
  
  public Set<Integer> resolve(BranchInstruction branch, Path history) {
    if(pol.containsKey(branch)) {
      return pol.get(branch).get(history);
    }
    return null;
  }

  @Override
  public int getCountsForChoice(BranchInstruction branch, int choice) {
    if(choice2counts.containsKey(branch)) {
      Integer count = choice2counts.get(branch).get(choice);
      return (count != null) ? count : 0;
    }
    return 0;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for(BranchInstruction branch : pol.keySet()) {
      sb.append(branch.toString()).append("\n");
      Map<Path, Set<Integer>> histories = pol.get(branch);
      for(Path p : histories.keySet()) {
        sb.append('\t').append(p.toString()).append(" --> {");
        Set<Integer> choices = histories.get(p);
        Iterator<Integer> choiceIter = choices.iterator();
        while(choiceIter.hasNext()) {
          sb.append(choiceIter.next());
          if(choiceIter.hasNext())
            sb.append(",");
        }
        sb.append("}\n");
      }
    }
    return sb.toString();
  }
}
