package wcanalysis.heuristic.model;

import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 * 
 * TODO:This is starting to look more and more like propertylisteneradapter
 * Maybe this class should just subclass it and in some way be added as
 * a listener similar to e.g. PathListener.
 */
public interface StateBuilder {
  public StateBuilder copy();
  
  public void handleChoiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> currentCG);
  public void handleExecuteInstruction(VM vm, ThreadInfo currentThread, Instruction instructionToExecute);
  public void handleInstructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction, Instruction executedInstruction);
  
  public void handleObjectCreated(VM vm, ThreadInfo ti, ElementInfo ei);
  public void handleObjectReleased(VM vm, ThreadInfo ti, ElementInfo ei);  
  public void handleMethodEntered(VM vm, ThreadInfo ti, MethodInfo ei);
  public void handleMethodExited(VM vm, ThreadInfo ti, MethodInfo ei);
  
  public State build(PathCondition resultingPC);
}
