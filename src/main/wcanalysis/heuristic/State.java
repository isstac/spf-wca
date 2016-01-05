package wcanalysis.heuristic;

import gov.nasa.jpf.symbc.numeric.PathCondition;

/**
 * @author Kasper Luckow
 */
public abstract class State implements CSVable, Comparable<State> {
  private final PathCondition pc;
  
  public State(PathCondition pc) {
    this.pc = pc;
  }
  
  public PathCondition getPathCondition() {
    return this.pc;
  }
  
  public abstract int getWC();
}
