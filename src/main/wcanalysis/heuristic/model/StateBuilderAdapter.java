package wcanalysis.heuristic.model;

import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 * 
 */
public abstract class StateBuilderAdapter implements StateBuilder {

  @Override
  public void handleChoiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> currentCG) {  }

  @Override
  public void handleExecuteInstruction(VM vm, ThreadInfo currentThread, Instruction instructionToExecute) {  }

  @Override
  public void handleInstructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction,
      Instruction executedInstruction) { }

  @Override
  public void handleObjectCreated(VM vm, ThreadInfo ti, ElementInfo ei) {  }

  @Override
  public void handleObjectReleased(VM vm, ThreadInfo ti, ElementInfo ei) {  }

  @Override
  public void handleMethodEntered(VM vm, ThreadInfo ti, MethodInfo ei) { }
  
  @Override
  public void handleMethodExited(VM vm, ThreadInfo ti, MethodInfo ei) { }
}
