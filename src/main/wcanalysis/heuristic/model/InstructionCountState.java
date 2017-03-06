/*
 * MIT License
 *
 * Copyright (c) 2017 The ISSTAC Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
