package wcanalysis.heuristic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.StackFrame;
import wcanalysis.heuristic.ContextManager.CGContext;

/**
 * @author Kasper Luckow
 *
 */
public class WorstCasePath extends Path implements Comparable<WorstCasePath> {

  
  public static WorstCasePath generateWorstCasePath(State endState, ChoiceGenerator<?> endCG, ContextManager ctxManager) {
    WorstCasePath wcPath = new WorstCasePath(endState);
    
    PCChoiceGenerator[] pcs = endCG.getAllOfType(PCChoiceGenerator.class);
    for(int i = 0; i < pcs.length; i++) {
      PCChoiceGenerator currPc = pcs[i];
      CGContext cgCtx = ctxManager.getContext(currPc);
      Decision dec = new Decision(new BranchInstruction(currPc.getInsn()), currPc.getNextChoice(), cgCtx.stackFrame);
      wcPath.add(dec);
    }
    return wcPath;
  }
  
  private final State finalState;
  private int pathMeasure = -1;
  
  private WorstCasePath(State finalState) {
    this.finalState = finalState;
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

    //TODO: this is very similar to the path projector in PathListener -- merge?
    Map<BranchInstruction, Set<DecisionHistory>> branchInstr2TrueDecisions = new HashMap<>();
    Map<BranchInstruction, Set<DecisionHistory>> branchInstr2FalseDecisions = new HashMap<>();
    Set<BranchInstruction> branchInstructions = new HashSet<>();
    for(Decision dec : this) {
      DecisionHistory history = dec.generateCtxPreservingDecisionHistory(this.decisionHistorySize);
      BranchInstruction currInstruction = dec.getInstruction();
      branchInstructions.add(currInstruction);
      Set<DecisionHistory> histories = null;
      
      if(dec.getChoice() == 0) { //false decision
        histories = branchInstr2FalseDecisions.get(currInstruction);
        if(histories == null) {
          histories = new HashSet<>();
          branchInstr2FalseDecisions.put(currInstruction, histories);
        }
      } else {  //true decision, was if(dec.getChoice() == 1) before
          histories = branchInstr2TrueDecisions.get(currInstruction);
          if(histories == null) {
            histories = new HashSet<>();
            branchInstr2TrueDecisions.put(currInstruction, histories);
          }
      }
      // else
       //throw new IllegalStateException();
      histories.add(history);
    }
    
    int pathMeasure = 0;
    for(BranchInstruction branchInstr : branchInstructions) {
      Set<DecisionHistory> trueHistories = branchInstr2TrueDecisions.get(branchInstr);
      Set<DecisionHistory> falseHistories = branchInstr2FalseDecisions.get(branchInstr);
      if((trueHistories == null && falseHistories != null) || //resolved perfectly 
          (falseHistories == null && trueHistories != null)) {
        pathMeasure++;
      } else if(falseHistories != null && trueHistories != null) { //now we check based on histories
        Set<DecisionHistory> union = new HashSet<>(trueHistories);
        union.addAll(falseHistories);
        
        Set<DecisionHistory> intersection = new HashSet<DecisionHistory>(trueHistories); // use the copy constructor
        intersection.retainAll(falseHistories);
        
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
    return this.computePathMeasure() - o.computePathMeasure();
  }
}
