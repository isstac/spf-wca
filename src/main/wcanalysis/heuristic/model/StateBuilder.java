/*
 * Copyright 2017 Carnegie Mellon University Silicon Valley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
