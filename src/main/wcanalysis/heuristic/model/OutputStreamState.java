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
