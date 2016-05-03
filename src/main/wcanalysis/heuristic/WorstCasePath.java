package wcanalysis.heuristic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import gov.nasa.jpf.vm.ChoiceGenerator;
import wcanalysis.heuristic.PathMeasureComputation.Result;
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
  private Result pathMeasure = null;
  private final PathMeasureComputation pathMeasureComp;
  
  private WorstCasePath(State endState, ChoiceGenerator<?> endCG, ContextManager ctxManager, PathMeasureComputation pathMeasureComp) {
    super(endCG, ctxManager);
    this.finalState = endState;
    this.pathMeasureComp = pathMeasureComp;
  }
  
  public State getWCState() {
    return this.finalState;
  }
  
  public Result getPathmeasure() {
    if(this.pathMeasure == null) { //caching of result
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
    
    //We favor a path that has more choices that can be made memoryless
    //this will be the policy that is "most general" -- we should refine this notion here
    long memorylessDecisionsDiff = getPathmeasure().getMemorylessResolution() - o.getPathmeasure().getMemorylessResolution();
    if(memorylessDecisionsDiff > 0) {
      return 1;
    } else if(memorylessDecisionsDiff < 0)
      return -1;
    
    //paths must have same memoryless resolutions if we reach this point
    
    //otherwise we will select the path that has the highest number of general resolutions
    long pathMeasureDiff = this.getPathmeasure().getResolutions() - o.getPathmeasure().getResolutions();
    
    return (int)pathMeasureDiff;
  }
}
