package wcanalysis.heuristic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import gov.nasa.jpf.vm.ChoiceGenerator;
import wcanalysis.heuristic.model.State;

/**
 * @author Kasper Luckow
 *
 */
public class WorstCasePath extends Path implements Comparable<WorstCasePath> {

  private static final long serialVersionUID = -6739423849594132561L;
  
  public static class Builder {
    private final ContextManager ctxManager;
    private final PathMeasureComputation pathMeasureComp;
    
    public Builder(ContextManager ctxManager, int maxHistory) {
      this.pathMeasureComp = new DefaultPathMeasure(new FixedLengthCtxHistoryGenerator(maxHistory));
      this.ctxManager = ctxManager;
    }
    
    public Builder(ContextManager ctxManager) {
      this.pathMeasureComp = new DefaultPathMeasure(new MaxLengthCtxHistoryGenerator());
      this.ctxManager = ctxManager;
    }
    
    public WorstCasePath build(State endState, ChoiceGenerator<?> endCG) {
      return new WorstCasePath(endState, endCG, ctxManager, pathMeasureComp);
    }    
  }
  
  private final State finalState;
  private int pathMeasure = -1;
  private final PathMeasureComputation pathMeasureComp;
  
  private WorstCasePath(State endState, ChoiceGenerator<?> endCG, ContextManager ctxManager, PathMeasureComputation pathMeasureComp) {
    super(endCG, ctxManager);
    this.finalState = endState;
    this.pathMeasureComp = pathMeasureComp;
  }
  
  public State getWCState() {
    return this.finalState;
  }
  
  public int getPathmeasure() {
    if(this.pathMeasure == -1) { //caching of result
      this.pathMeasure = pathMeasureComp.compute(this);
    }
    return this.pathMeasure;
  }


  @Override
  public int compareTo(WorstCasePath o) {
    if(o == null) //TODO: makes sense?
      return 1;
    int comp = this.finalState.compareTo(o.getWCState());
    if(comp != 0) //final states are different, e.g.  have different depth
      return comp;
    
    //otherwise we will select the path that has the highest path measure
    return this.getPathmeasure() - o.getPathmeasure();
  }
}
