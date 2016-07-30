package wcanalysis.heuristic.model;

import java.util.Set;

import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import wcanalysis.heuristic.util.Util;

/**
 * @author Rody Kersten
 * instruction counting cost model
 */
public final class MethodCallCountState extends State {
  public final static class MethodCallCountStateBuilder extends StateBuilderAdapter {

	public final static String COUNTED_METHODS = "symbolic.wc.statebuilder.countedmethods";
	  
    private long numCalls = 0;
    Set<String> countedMethods;

    public MethodCallCountStateBuilder() {
    	String[] countedMeth = VM.getVM().getConfig().getStringArray(COUNTED_METHODS, new String[0]);
    	countedMethods = Util.extractSimpleMethodNames(countedMeth);
    	if (countedMethods.isEmpty()) 
    		System.err.println("No methods to count. Please set " + COUNTED_METHODS + " option.");
    }
    
    private MethodCallCountStateBuilder(long numCalls, Set<String> countedMethods) {
      this.numCalls = numCalls;
      this.countedMethods = countedMethods;
    }

    @Override
    public void handleMethodEntered(VM vm, ThreadInfo ti, MethodInfo ei) {
        if(countedMethods.contains(ei.getBaseName())) {
        	numCalls++;
        }
    }

    @Override
    public StateBuilder copy() {
      return new MethodCallCountStateBuilder(numCalls,countedMethods);
    }
    
    @Override
    public State build(PathCondition resultingPC) {
      return new MethodCallCountState(numCalls, resultingPC);
    }
  }
  
  private final long numCalls;
  
  private MethodCallCountState(long numCalls, PathCondition pc) {
    super(pc);
    this.numCalls = numCalls;
  }

  @Override
  public String getCSVHeader() {
    return "numCalls";
  }

  @Override
  public String getCSV() {
    return String.valueOf(numCalls);
  }

  @Override
  public int compareTo(State o) {
    if(!(o instanceof MethodCallCountState)) {
      throw new IllegalStateException("Expected state of type " + MethodCallCountState.class.getName());
    }
   
    MethodCallCountState other = (MethodCallCountState)o;
    return this.numCalls < other.numCalls ? -1:
      this.numCalls > other.numCalls ? 1 : 0;
  }

  public long getNumCalls() {
    return this.numCalls;
  }
  
  @Override
  public double getWC() {
    return this.getNumCalls();
  }
}
