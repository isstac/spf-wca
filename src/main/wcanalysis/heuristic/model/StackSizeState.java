package wcanalysis.heuristic.model;

import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Rody Kersten
 * instruction counting cost model
 */
public final class StackSizeState extends State {
  public final static class StackSizeStateBuilder extends StateBuilderAdapter {

    private long maxStackSize = 0;
    private long initialStackSize = -1;
    
    public StackSizeStateBuilder() { }
    
    private StackSizeStateBuilder(long maxStackSize) {
      this.maxStackSize = maxStackSize;
    }

    @Override
    public void handleMethodEntered(VM vm, ThreadInfo ti, MethodInfo ei) {
    	long currStackSize = 0;
    	for(StackFrame frame : ti.getInvokedStackFrames()) {
    		// stack frame has fixed number of slots for locals and operands
    		// each slot is an int, so multiply by 4 to get #bytes
    		currStackSize += frame.getSlots().length;
    	}
    	if (initialStackSize == -1) {    		
    		initialStackSize = currStackSize;
    	}
    	currStackSize -= initialStackSize;
    	if(currStackSize > maxStackSize)
    		maxStackSize = currStackSize;
    }

    @Override
    public StateBuilder copy() {
      return new StackSizeStateBuilder(maxStackSize);
    }
    
    @Override
    public State build(PathCondition resultingPC) {
      return new StackSizeState(maxStackSize, resultingPC);
    }
  }
  
  private final long maxStackSize;
  
  private StackSizeState(long maxStackSize, PathCondition pc) {
    super(pc);
    this.maxStackSize = maxStackSize;
  }

  @Override
  public String getCSVHeader() {
    return "maxStackSize";
  }

  @Override
  public String getCSV() {
    return String.valueOf(maxStackSize);
  }

  @Override
  public int compareTo(State o) {
    if(!(o instanceof StackSizeState)) {
      throw new IllegalStateException("Expected state of type " + StackSizeState.class.getName());
    }
   StackSizeState other = (StackSizeState)o;
    return this.maxStackSize < other.maxStackSize ? -1:
      this.maxStackSize > other.maxStackSize ? 1 : 0;
  }

  public long getMaxStackSize() {
    return this.maxStackSize;
  }
  
  @Override
  public double getWC() {
    return this.getMaxStackSize();
  }
}
