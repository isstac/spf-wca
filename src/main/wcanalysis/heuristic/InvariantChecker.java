package wcanalysis.heuristic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;

/**
 * @author Kasper Luckow
 *
 */
public class InvariantChecker {

  private final Map<BranchInstruction, CountsInvariant> invariants;
  private final Map<BranchInstruction, Map<Integer, Integer>> currentCounts;
  
  public InvariantChecker(Map<BranchInstruction, CountsInvariant> invariants) {
    this.invariants = invariants;
    this.currentCounts = new HashMap<>();
  }
  
  public void choiceMade(BranchInstruction instr, int choiceMade) {
    CountsInvariant invariant = invariants.get(instr);
    if(invariant != null) {
      Map<Integer, Integer> choice2counts = currentCounts.get(instr);
      if(choice2counts == null) {
        choice2counts = new HashMap<>();
        currentCounts.put(instr, choice2counts);
      }
      int count = 0;
      if(choice2counts.containsKey(choiceMade))
        count = choice2counts.get(choiceMade);
      choice2counts.put(choiceMade, count);
    }
  }
  
  public Set<Integer> getChoices(BranchInstruction instr, PCChoiceGenerator cg) {
    CountsInvariant invariant = invariants.get(instr);
    if(invariant != null) {
      Map<Integer, Integer> choice2counts = currentCounts.get(instr);
      if(choice2counts == null) {
        choice2counts = new HashMap<>();
      }
      Set<Integer> allowedChoices = new HashSet<>();
      for(int choiceToBeMade : cg.getUnprocessedChoices()) {
        Map<Integer, Integer> tempChoice2Counts = new HashMap<>(choice2counts);
        int freq = 0;
        if(tempChoice2Counts.containsKey(choiceToBeMade)) {
          freq = tempChoice2Counts.get(choiceToBeMade);
        }
        tempChoice2Counts.put(choiceToBeMade, freq);
        if(invariant.applies(tempChoice2Counts)) {
          allowedChoices.add(choiceToBeMade);
        }
      }
      return allowedChoices;
    }
    return null; //a bit bad, but signifies that no unique choice could be found
  }
}
