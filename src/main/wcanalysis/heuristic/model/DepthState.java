package wcanalysis.heuristic.model;

import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 *
 */
public final class DepthState extends State {
  public final static class DepthStateBuilder extends StateBuilderAdapter {

    private int depth = 0;
    private long instrExecuted = 0;
    
    public DepthStateBuilder() { }
    
    private DepthStateBuilder(int depth, long instrExecuted) {
      this.depth = depth;
      this.instrExecuted = instrExecuted;
    }
    
    @Override
    public void handleChoiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> currentCG) {
      if(currentCG instanceof PCChoiceGenerator)
        this.depth++;
    }

    @Override
    public void handleInstructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction,
        Instruction executedInstruction) {
      this.instrExecuted++;
    }

    @Override
    public StateBuilder copy() {
      return new DepthStateBuilder(this.depth, this.instrExecuted);
    }

    @Override
    public State build(PathCondition resultingPC) {
      return new DepthState(resultingPC, this.depth, this.instrExecuted);
    }
  }
  
  private final int depth;
  private final long instrExecuted;
  
  DepthState(PathCondition pc, int depth, long instrExecuted) {
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
  public double getWC() {
    return getDepth();
  }

  @Override
  public int compareTo(State o) {
    if(!(o instanceof DepthState)) {
      throw new IllegalStateException("Expected state of type " + DepthState.class.getName());
    }
    return (int)(this.depth - ((DepthState)o).depth);
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
