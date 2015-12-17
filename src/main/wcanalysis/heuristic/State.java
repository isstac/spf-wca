package wcanalysis.heuristic;

import gov.nasa.jpf.symbc.numeric.PathCondition;
import isstac.structure.cfg.Block;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Kasper Luckow
 */
public class State implements Comparable<State> {

  public interface StateData { public StateData copy(); }
  
  public static class EndStateData implements StateData {
    private final PathCondition pc;
    private final Path path;
    private final int decisionHistorySize;
    
    private int pathMeasure = -1;
    public EndStateData(PathCondition pc, Path path, int decisionHistorySize) {
      this.pc = pc;
      this.path = path;
      this.decisionHistorySize = decisionHistorySize;
    }
    
    public EndStateData(EndStateData other) {
      this.pc = (other.getPC() != null) ? other.getPC().make_copy() : null;
      this.path = other.getPath().copy();
      this.decisionHistorySize = other.decisionHistorySize;
      this.pathMeasure = other.pathMeasure;
    }
    
    @Override
    public StateData copy() {
      return new EndStateData(this);
    }
    
    public PathCondition getPC() {
      return this.pc;
    }
    
    public Path getPath() {
      return this.path;
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
      for(Decision dec : this.path) {
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

  }
  
  private int depth = 0;
  private long instrExecuted = 0;
  private StateData stateData = null;
  
  //Experimental invariant
  private Map<Block, HeuristicDecisionCounts> blockCounts = new HashMap<>();
  
  public State() {}
  
  public State(State other) {
    this.depth = other.getDepth();
    this.instrExecuted = other.getInstrExecuted();
    
    //seems convoluted
    Map<Block, HeuristicDecisionCounts> otherBlockCounts = other.getBlockCounts();
    for(Block bl : otherBlockCounts.keySet()) {
      this.blockCounts.put(bl, new HeuristicDecisionCounts(otherBlockCounts.get(bl)));
    }
    if(other.getStateData() != null)
      this.setStateData(other.getStateData().copy());
  }
  
  public Map<Block, HeuristicDecisionCounts> getBlockCounts() {
    return blockCounts;
  }
  
  public HeuristicDecisionCounts getBlockCounts(Block bl) {
    HeuristicDecisionCounts c = blockCounts.get(bl);
    if(c == null) {
      c = new HeuristicDecisionCounts();
      blockCounts.put(bl, c);
    }
    return c;
  }
  
  public void incDepth(int i) {
    this.depth += i;
  }
  
  public void incInstrExecuted(long i) {
    this.instrExecuted += i;
  }
  
  public int getDepth() {
    return this.depth;
  }
  
  public long getInstrExecuted() {
    return this.instrExecuted;
  }
  
  public void setStateData(StateData data) {
    this.stateData = data;
  }
  
  public boolean hasStateData() {
    return this.stateData != null;
  }
  
  public StateData getStateData() {
    return this.stateData;
  }
  
  public State copy() {
    return new State(this);
  }
  
  @Override
  public int compareTo(State other) {
    if(other == null)
      return 1;
  
    assert this.stateData != null;
    assert other.getStateData() != null;
    assert this.stateData instanceof EndStateData;
    assert other.getStateData() instanceof EndStateData;
    
    
    //if two states have same depth, a "better" worse case state, is the one,
    //where the policy performs better, i.e. where it is capable of resolving
    //more choices
    int depthDiff = this.depth - other.getDepth();
    if(depthDiff != 0) {
      return depthDiff;
    }
    
    int thisPathMeasure = ((EndStateData)this.stateData).getPathmeasure();
    int otherPathMeasure = ((EndStateData)other.getStateData()).getPathmeasure();
    
    return thisPathMeasure - otherPathMeasure;
  }
}
