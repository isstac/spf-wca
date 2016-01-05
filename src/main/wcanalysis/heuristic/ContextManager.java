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
