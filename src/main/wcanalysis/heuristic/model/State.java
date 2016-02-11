package wcanalysis.heuristic.model;

import gov.nasa.jpf.symbc.numeric.PathCondition;
import wcanalysis.heuristic.util.CSVable;

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
  
  // Should this return a double, or would we like to parameterize this (e.g. to subclasses of Number)?
  public abstract double getWC();
}
