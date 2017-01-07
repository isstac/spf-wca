/*
 * Copyright 2017 Carnegie Mellon University Silicon Valley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
