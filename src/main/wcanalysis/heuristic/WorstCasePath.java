package wcanalysis.heuristic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import gov.nasa.jpf.vm.ChoiceGenerator;
import wcanalysis.heuristic.model.State;

/**
 * @author Kasper Luckow
 *
 */
public class WorstCasePath extends Path implements Comparable<WorstCasePath> {

  private static final long serialVersionUID = -6739423849594132561L;
  private final State finalState;
  private int pathMeasure = -1;
  private final int maxHistorySize;
  
  public WorstCasePath(State endState, ChoiceGenerator<?> endCG, ContextManager ctxManager, int maxHistorySize) {
    super(endCG, ctxManager);
    this.finalState = endState;
    this.maxHistorySize = maxHistorySize;
  }
  
  public State getWCState() {
    return this.finalState;
  }
  
  public int getPathmeasure() {
    if(this.pathMeasure == -1) { //caching of result
      this.pathMeasure = computePathMeasure();
    }
    return this.pathMeasure;
  }
  
  private int computePathMeasure() {
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
    int decIdx = this.size() - 1;
    
    ListIterator<Decision> decIter = this.listIterator(this.size());
    while(decIter.hasPrevious()) {
      currentDecision = decIter.previous();
      Path history = this.generateCtxPreservingHistoryFromIdx(decIdx, this.maxHistorySize);
      
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
    
    int pathMeasure = 0;
    for(BranchInstruction branchInstr : branchInstructions) {
      Map<Integer, Set<Path>> histories = branch2histories.get(branchInstr);
      if(histories.keySet().size() == 1) { //resolved perfectly
        pathMeasure++;
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
        pathMeasure += union.size() - intersection.size();
      }
      //FIXME!!!!!!! Take into account the invariant pruning here in the path measure
    }
    return pathMeasure;
  }


  @Override
  public int compareTo(WorstCasePath o) {
    if(o == null) //TODO: makes sense?
      return 1;
    int comp = this.finalState.compareTo(o.getWCState());
    if(comp != 0) //final states are different, e.g.  have different depth
      return comp;
    
    //otherwise we will select the path that has the highest path measure
    return this.getPathmeasure() - o.getPathmeasure();
  }
}
