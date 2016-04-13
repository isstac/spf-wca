package wcanalysis.heuristic.model;

import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Heap;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 * Simple statebuilder for tracking memory allocations along paths
 */
public final class MaxLiveHeapState extends State {
  public final static class MaxLiveHeapStateBuilder extends StateBuilderAdapter {

    private long maxLiveHeap = 0;
    
    public MaxLiveHeapStateBuilder() {}
    
    private MaxLiveHeapStateBuilder(long maxLiveHeap) {
      this.maxLiveHeap = maxLiveHeap;
    }
    

    @Override
    public void handleObjectCreated(VM vm, ThreadInfo ti, ElementInfo ei) {
      
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
      
      Heap heap = vm.getHeap();
      int currHeapSize = 0;
      for(ElementInfo info : heap.liveObjects()) {
        currHeapSize += info.getHeapSize();
      }
      if(currHeapSize > maxLiveHeap)
        maxLiveHeap = currHeapSize;
    }
    
    @Override
    public StateBuilder copy() {
      return new MaxLiveHeapStateBuilder(maxLiveHeap);
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
