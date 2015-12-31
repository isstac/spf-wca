package wcanalysis.heuristic;

import gov.nasa.jpf.symbc.numeric.PathCondition;

/**
 * @author Kasper Luckow
 *
 */
public class TimeState extends State {
  private final int depth;
  private final long instrExecuted;
  
  public TimeState(PathCondition pc, int depth, long instrExecuted) {
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
  public int compareTo(State o) {
    if(!(o instanceof TimeState)) {
      throw new IllegalStateException("Expected state fo type " + TimeState.class.getName());
    }
    return this.depth - ((TimeState)o).depth;
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
