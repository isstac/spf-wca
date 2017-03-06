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
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 * Simple statebuilder for tracking memory allocations along paths
 */
public final class HeapAllocState extends State {
  public final static class HeapAllocStateBuilder extends StateBuilderAdapter {

    private long heapAlloced = 0;

    //For now, we only keep this guy for statistics purposes...
    private long allocNum = 0;
    
    public HeapAllocStateBuilder() { }
    
    private HeapAllocStateBuilder(long memAlloced, long allocNum) {
      this.heapAlloced = memAlloced;
      this.allocNum = allocNum;
    }
    
    @Override
    public void handleObjectCreated(VM vm, ThreadInfo ti, ElementInfo ei) {
      this.heapAlloced += ei.getHeapSize();
      this.allocNum++;
    }
    
    @Override
    public StateBuilder copy() {
      return new HeapAllocStateBuilder(heapAlloced, allocNum);
    }
    
    @Override
    public State build(PathCondition resultingPC) {
      return new HeapAllocState(heapAlloced, allocNum, resultingPC);
    }
  }
  
  private final long heapAlloced;
  private final long allocNum;
  
  private HeapAllocState(long heapAlloced, long allocNum, PathCondition pc) {
    super(pc);
    this.heapAlloced = heapAlloced;
    this.allocNum = allocNum;
  }

  @Override
  public String getCSVHeader() {
    return "memAlloced,allocNum";
  }

  @Override
  public String getCSV() {
    return String.valueOf(heapAlloced) + "," + String.valueOf(allocNum);
  }

  @Override
  public int compareTo(State o) {
    if(!(o instanceof HeapAllocState)) {
      throw new IllegalStateException("Expected state of type " + HeapAllocState.class.getName());
    }
    
    //The notion of worst case is ONLY determined by the total size of allocations along the path; not the number of allocs
    HeapAllocState other = (HeapAllocState)o;
    return this.heapAlloced < other.heapAlloced ? -1:
      this.heapAlloced > other.heapAlloced ? 1 : 0;
  }

  public long getHeapAlloced() {
    return this.heapAlloced;
  }
  
  @Override
  public double getWC() {
    return this.getHeapAlloced();
  }
}
