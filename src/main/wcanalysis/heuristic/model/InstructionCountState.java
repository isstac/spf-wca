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
