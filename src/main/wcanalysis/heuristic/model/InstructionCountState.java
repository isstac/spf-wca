package wcanalysis.heuristic.model;

import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Rody Kersten
 * instruction counting cost model
 */
public final class InstructionCountState extends State {
  public final static class InstructionCountStateBuilder extends StateBuilderAdapter {

    private long instrCount = 0;
    
    public InstructionCountStateBuilder() { }
    
    private InstructionCountStateBuilder(long instrCount) {
      this.instrCount = instrCount;
    }
    
    @Override
    public void handleInstructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction,
        Instruction executedInstruction) {
	      this.instrCount++;
    }
    
    @Override
    public StateBuilder copy() {
      return new InstructionCountStateBuilder(instrCount);
    }
    
    @Override
    public State build(PathCondition resultingPC) {
      return new InstructionCountState(instrCount, resultingPC);
    }
  }
  
  private final long instrCount;
  
  private InstructionCountState(long instrCount, PathCondition pc) {
    super(pc);
    this.instrCount = instrCount;
  }

  @Override
  public String getCSVHeader() {
    return "instrCount";
  }

  @Override
  public String getCSV() {
    return String.valueOf(instrCount);
  }

  @Override
  public int compareTo(State o) {
    if(!(o instanceof InstructionCountState)) {
      throw new IllegalStateException("Expected state of type " + InstructionCountState.class.getName());
    }
    InstructionCountState other = (InstructionCountState)o;
    return this.instrCount < other.instrCount ? -1:
      this.instrCount > other.instrCount ? 1 : 0;
  }

  public long getInstructionCount() {
    return this.instrCount;
  }
  
  @Override
  public double getWC() {
    return this.getInstructionCount();
  }
}
