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
public class DepthStateBuilder implements StateBuilder {

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
  public void handleExecuteInstruction(VM vm, ThreadInfo currentThread, Instruction instructionToExecute) {
    //Nothing to be done here
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
