package wcanalysis.heuristic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import att.grappa.Attribute;
import isstac.structure.cfg.util.DotAttribute;

/**
 * @author Kasper Luckow
 */
public abstract class DecisionCollection implements DotAttribute, Serializable, Iterable<DecisionHistory> {
  private static final long serialVersionUID = 3896815168451849461L;

  private List<DecisionHistory> decHists = new ArrayList<>();
  
  @Override
  public Attribute getAttribute() {
    return null;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for(DecisionHistory decHist : getUniqueDecisionHistories()) {
      sb.append(decHist.toString()).append('\n');
    }
    return sb.toString();
  }
  
  //TODO: We could perform some caching here if it turns out to problematic
  public boolean contains(DecisionHistory history) {
    return this.decHists.contains(history);
  }
  
  public boolean addHistory(DecisionHistory history) {
    return this.decHists.add(history);
  }
  
  public int getNumberOfDecisionHistories() {
    return this.decHists.size();
  }
  
  public int getNumberOfUniqueDecisionHistories() {
    return new HashSet<>(this.decHists).size();
  }
  
  public Collection<DecisionHistory> getUniqueDecisionHistories() {
    return new HashSet<>(this.decHists);
  }
  
  public Collection<DecisionHistory> getAllDecisionHistories() {
    return this.decHists;
  }
  
  @Override
  public Iterator<DecisionHistory> iterator() {
    return this.decHists.iterator();
  }

  public static class TrueDecisionCollection extends DecisionCollection {
    private static final long serialVersionUID = -811709669444247531L;
    
    @Override
    public String getLabelString() {
      return "True (unique) policies. #histories: " + 
          super.getNumberOfDecisionHistories() + 
          " (#unique:" + super.getNumberOfUniqueDecisionHistories() + ")\n" 
          + super.toString();
    }
  }
  
  public static class FalseDecisionCollection extends DecisionCollection {
    private static final long serialVersionUID = -3540685838370460156L;
    
    @Override
    public String getLabelString() {
      return "False (unique) policies. #histories: " +
          super.getNumberOfDecisionHistories() + 
          " (#unique:" + super.getNumberOfUniqueDecisionHistories() + ")\n" + 
          super.toString();
    }
  }
}