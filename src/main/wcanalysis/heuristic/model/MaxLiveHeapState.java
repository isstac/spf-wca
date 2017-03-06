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
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Heap;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow, Rody Kersten
 * Simple statebuilder for tracking memory allocations along paths
 */
public final class MaxLiveHeapState extends State {
  public final static class MaxLiveHeapStateBuilder extends StateBuilderAdapter {

    private long maxLiveHeap = 0;
    private long initialHeap = -1;
    
    /**
     * TODO: maintain list of allocated objects, then go over list to check which are live.
     */
    
    public MaxLiveHeapStateBuilder() {}
    
    private MaxLiveHeapStateBuilder(long maxLiveHeap, long initialHeap) {
      this.maxLiveHeap = maxLiveHeap;
      this.initialHeap = initialHeap;
    }
    
    /**
     * This is extremely expensive: the size of the live objects in the heap, are analyzed
     * every time an instruction has been executed.
     * A much better idea is to only conduct this check whenever a scope will be exited
     * after the instruction is executed.
     * One way of doing that, is to generate the cfg of the method (with proper caching,
     * lazy construction, etc) and then to check if the instruction executed is the last
     * in a basic block and if the next block has multiple incoming edges. In that case
     * it is a candidate for checking live objects (still an over-approximation though -- 
     * but better than now)
     */
    @Override
    public void handleInstructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction,
    		Instruction executedInstruction) {

    	//force garbage collect before initial heap size measurement
    	if (initialHeap == -1) {
    		vm.activateGC();vm.activateGC();vm.activateGC();vm.activateGC();vm.activateGC();
    	}

    	Heap heap = vm.getHeap();
    	int currHeapSize = 0;
    	for(ElementInfo info : heap.liveObjects()) {
    		currHeapSize += info.getHeapSize();
    	}
    	System.out.println("Inst: " + executedInstruction);
    	System.out.println("CURRENT HEAP SIZE: " + currHeapSize);
    	if (initialHeap == -1) {
    		initialHeap = currHeapSize;
    	}
    	currHeapSize -= initialHeap;
    	System.out.println("MINUS " + initialHeap + " INITIAL = " + currHeapSize);
    	if(currHeapSize > maxLiveHeap)
    		maxLiveHeap = currHeapSize;
    }
    
    @Override
    public StateBuilder copy() {
      return new MaxLiveHeapStateBuilder(maxLiveHeap,initialHeap);
    }
    
    @Override
    public State build(PathCondition resultingPC) {
      return new MaxLiveHeapState(maxLiveHeap, resultingPC);
    }
  }
  
  private final long maxLiveHeap;
  
  private MaxLiveHeapState(long maxLiveHeap, PathCondition pc) {
    super(pc);
    this.maxLiveHeap = maxLiveHeap;
  }

  @Override
  public String getCSVHeader() {
    return "maxLiveHeap";
  }

  @Override
  public String getCSV() {
    return String.valueOf(maxLiveHeap);
  }

  @Override
  public int compareTo(State o) {
    if(!(o instanceof MaxLiveHeapState)) {
      throw new IllegalStateException("Expected state of type " + MaxLiveHeapState.class.getName());
    }
    
    MaxLiveHeapState other = (MaxLiveHeapState)o;
    return this.maxLiveHeap < other.maxLiveHeap ? -1:
      this.maxLiveHeap > other.maxLiveHeap ? 1 : 0;
  }

  public long getMaxHeapSize() {
    return this.maxLiveHeap;
  }
  
  @Override
  public double getWC() {
    return this.getMaxHeapSize();
  }
}
