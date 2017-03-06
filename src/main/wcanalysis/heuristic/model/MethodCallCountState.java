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
