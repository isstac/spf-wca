package wcanalysis.heuristic;

import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 *
 */
public class TimeStateBuilder implements StateBuilder {

  @Override
  public void handleChoiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> currentCG) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void handleExecuteInstruction(VM vm, ThreadInfo currentThread, Instruction instructionToExecute) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void handleInstructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction,
      Instruction executedInstruction) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public StateBuilder copy() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public State build() {
    // TODO Auto-generated method stub
    return null;
  }

}
