package wcanalysis.heuristic.model;

import java.util.List;
import java.util.Set;

import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.LocalVarInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import wcanalysis.heuristic.util.Util;

/**
 * @author Rody Kersten
 * output stream bytes written cost model
 */
public final class OutputStreamState extends State {
  public final static class OutputStreamStateBuilder extends StateBuilderAdapter {

	public final static String MEASURED_STREAMS = "symbolic.wc.statebuilder.measuredstreams";
	  
    private long bytesWritten = 0;
    Set<String> measuredStreams;

    public OutputStreamStateBuilder() {
    	String[] def = {"java.io.OutputStream"};
    	String[] meas = VM.getVM().getConfig().getStringArray(MEASURED_STREAMS, def);
    	measuredStreams = Util.extractSimpleMethodNames(meas);
    }
    
    private OutputStreamStateBuilder(long bytesWritten, Set<String> measuredStreams) {
      this.bytesWritten = bytesWritten;
      this.measuredStreams = measuredStreams;
    }

    @Override
    public void handleMethodEntered(VM vm, ThreadInfo ti, MethodInfo ei) {
    	for (String stream : measuredStreams) {
    		if (ei.getClassInfo().isInstanceOf(stream) && ei.getName().equals("write")) {

    			
    			/*
    			 *  There are 3 write methods: write(byte[] b), 
    			 *  write(byte[] b, int off, int len) and write(int b);
    			 *  
    			 *  We have to be careful to not double count writes. Many OutputStream classes
    			 *  have write methods that call eachother, e.g. write(byte[] b) calls write(int b) b.length times.
    			 *  We handle this by looking at the stack frame below the current one.
    			 */
    			List<StackFrame> stack = ti.getInvokedStackFrames();
    			if (stack.get(stack.size()-2).getMethodInfo().getClassInfo().isInstanceOf("java.io.OutputStream")) {
    				return;
    			}
    			
    			String args[] = ei.getArgumentTypeNames();
    			if (args[0].equals("int")) {
    				bytesWritten += 1; // even though it's int, only one byte is written according to documentation
    			} else if (args[0].equals("byte[]")){
 
    				// TODO get length of array (problem: could be symbolic)
    				LocalVarInfo varinfo = ei.getArgumentLocalVars()[1];
    				System.out.println("VARINFO: " + varinfo.toString());
    				bytesWritten++;
    			} else {
    				System.out.println("Don't know how to handle this type of write method");
    			}
    		}
        }
    }

    @Override
    public StateBuilder copy() {
      return new OutputStreamStateBuilder(bytesWritten,measuredStreams);
    }
    
    @Override
    public State build(PathCondition resultingPC) {
      return new OutputStreamState(bytesWritten, resultingPC);
    }
  }
  
  private final long bytesWritten;
  
  private OutputStreamState(long bytesWritten, PathCondition pc) {
    super(pc);
    this.bytesWritten = bytesWritten;
  }

  @Override
  public String getCSVHeader() {
    return "bytesWritten";
  }

  @Override
  public String getCSV() {
    return String.valueOf(bytesWritten);
  }

  @Override
  public int compareTo(State o) {
    if(!(o instanceof OutputStreamState)) {
      throw new IllegalStateException("Expected state of type " + OutputStreamState.class.getName());
    }
   
    OutputStreamState other = (OutputStreamState)o;
    return this.bytesWritten < other.bytesWritten ? -1:
      this.bytesWritten > other.bytesWritten ? 1 : 0;
  }

  public long getBytesWritten() {
    return this.bytesWritten;
  }
  
  @Override
  public double getWC() {
    return this.getBytesWritten();
  }
}
