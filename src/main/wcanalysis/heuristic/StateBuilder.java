package wcanalysis.heuristic;

import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 *
 */
public abstract class StateBuilder {

  private PathCondition pc;
  
  public StateBuilder(StateBuilder other) {
    
  }
  
  public void setResultingpPC(PathCondition pc) {
    
  }
  
  public abstract void handleChoiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> currentCG);
  public abstract void handleExecuteInstruction(VM vm, ThreadInfo currentThread, Instruction instructionToExecute);
  public abstract void handleInstructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction, Instruction executedInstruction);

  public abstract State build();
}
