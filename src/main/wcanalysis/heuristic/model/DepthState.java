package wcanalysis.heuristic.model;

import gov.nasa.jpf.symbc.numeric.PathCondition;

/**
 * @author Kasper Luckow
 *
 */
public class DepthState extends State {
  private final int depth;
  private final long instrExecuted;
  
  public DepthState(PathCondition pc, int depth, long instrExecuted) {
    super(pc);
    this.depth = depth;
    this.instrExecuted = instrExecuted;
  }
  
  public int getDepth() {
    return depth;
  }

  public long getInstrExecuted() {
    return instrExecuted;
  }

  @Override
  public int getWC() {
    return getDepth();
  }

  @Override
  public int compareTo(State o) {
    if(!(o instanceof DepthState)) {
      throw new IllegalStateException("Expected state fo type " + DepthState.class.getName());
    }
    return this.depth - ((DepthState)o).depth;
  }

  @Override
  public String getCSVHeader() {
    return "wcDepth,wcInstrExec";
  }

  @Override
  public String getCSV() {
    return this.depth + "," + this.instrExecuted;
  }
}
