package wcanalysis.heuristic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Kasper Luckow
 *
 */
public class DefaultPathMeasure implements PathMeasureComputation {
  
  private final HistoryGenerator historyGenerator;

  public DefaultPathMeasure(HistoryGenerator generator) {
    this.historyGenerator = generator;
  }
  
  @Override
  public Result compute(WorstCasePath path) {
    /*
     * A lot of interesting things happen here. An end state is considered "better"
     * if it passes the following checks (in that order):
     * 1. It has a larger depth
     * 2. Path measure is greater (i.e. more choices can resolved perfectly with the policy)
     * The path measure is computed by the following computation:
     * 1. If a decision has only counts on one branch, it can be resolved perfectly
     * 2. If counts are present on both branches, use the history of decisions to determine
     * if it can be resolved perfectly
     * 3. How many choices can be resolved with invariant pruning
     */
    
    Map<BranchInstruction, Map<Integer, Set<Path>>> branch2histories = new HashMap<>();
    Set<BranchInstruction> branchInstructions = new HashSet<>();

    
    Decision currentDecision = null;
    int decIdx = path.size() - 1;
    
    ListIterator<Decision> decIter = path.listIterator(path.size());
    while(decIter.hasPrevious()) {
      currentDecision = decIter.previous();
      Path history = historyGenerator.generateHistoryFromDecIdx(path, decIdx);
      
      BranchInstruction currInstruction = currentDecision.getInstruction();
      branchInstructions.add(currInstruction);
      
      Map<Integer, Set<Path>> historiesForChoice = branch2histories.get(currInstruction);
      if(historiesForChoice == null) {
        historiesForChoice = new HashMap<>();
        branch2histories.put(currInstruction, historiesForChoice);
      }
      
      Set<Path> histories = historiesForChoice.get(currentDecision.getChoice());
      if(histories == null) {
        histories = new HashSet<>();
        historiesForChoice.put(currentDecision.getChoice(), histories);
      }
      
      histories.add(history);
      decIdx--;
    }
    
    int resolutions = 0;
    int memoryLessResolutions = 0;
    for(BranchInstruction branchInstr : branchInstructions) {
      Map<Integer, Set<Path>> histories = branch2histories.get(branchInstr);
      if(histories.keySet().size() == 1) { //resolved perfectly
        resolutions++;
        memoryLessResolutions++;
      } else { //now we check based on histories
        Set<Path> union = new HashSet<>();
        for(Set<Path> historiesForChoice : histories.values()) {
          union.addAll(historiesForChoice);
        }
        
        Set<Path> intersection = new HashSet<>(union);
        for(Set<Path> historiesForChoice : histories.values()) {
          intersection.retainAll(historiesForChoice);
        }
        
        //the measure is updated with the number of decisions we can uniquely resolve!
        resolutions += union.size() - intersection.size();
      }
      //TODO Take into account the invariant pruning here in the path measure
    }
    return new Result(resolutions, memoryLessResolutions);
  }
}
