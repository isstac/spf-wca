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
public abstract class State implements Comparable<State> {
  private final PathCondition pc;
  
  public State(PathCondition pc) {
    this.pc = pc;
  }
  
  public PathCondition getPathCondition() {
    return this.pc;
  }
}
