package wcanalysis.heuristic;

import java.util.HashMap;
import java.util.Map;

import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.StackFrame;

/**
 * @author Kasper Luckow
 *
 */
public class ContextManager {
  private Map<Integer, StackFrame> contextMap = new HashMap<>();
  
  public void addContext(ChoiceGenerator<?> cg, StackFrame caller) {
    if(contextMap.containsKey(cg.getStateId()))
      throw new IllegalStateException("Attempt to override context map");
    contextMap.put(cg.getStateId(), caller);
  }
  
  public StackFrame getContext(ChoiceGenerator<?> cg) {
    return this.contextMap.get(cg.getStateId());
  }
}
