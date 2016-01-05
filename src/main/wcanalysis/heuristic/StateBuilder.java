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
public interface StateBuilder {
  public StateBuilder copy();
  
  public void handleChoiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> currentCG);
  public void handleExecuteInstruction(VM vm, ThreadInfo currentThread, Instruction instructionToExecute);
  public void handleInstructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction, Instruction executedInstruction);
  
  public State build(PathCondition resultingPC);
}
