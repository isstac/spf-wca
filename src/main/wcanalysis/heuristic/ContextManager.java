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

package wcanalysis.heuristic;

import java.util.HashMap;
import java.util.Map;

import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.StackFrame;
import wcanalysis.heuristic.model.StateBuilder;

/**
 * @author Kasper Luckow
 * TODO: maybe we can just use threadinfo on the ChoiceGenerator
 * instead of explicitly storing a reference to the stackframe
 */
public class ContextManager {
  
  static class CGContext {
    final StackFrame stackFrame;
    final StateBuilder stateBuilder;
    
    public CGContext(StackFrame sf, StateBuilder stateBuilder) {
      this.stackFrame = sf;
      this.stateBuilder = stateBuilder;
    }
  }
  
  private Map<Integer, CGContext> contextMap = new HashMap<>();
  
  public void addContext(ChoiceGenerator<?> cg, StackFrame caller, StateBuilder stateBuilder) {
    if(contextMap.containsKey(cg.getStateId()))
      throw new IllegalStateException("Attempt to override context map");
    this.contextMap.put(cg.getStateId(), new CGContext(caller, stateBuilder));
  }
  
  public CGContext getContext(ChoiceGenerator<?> cg) {
    return this.contextMap.get(cg.getStateId());
  }
}
