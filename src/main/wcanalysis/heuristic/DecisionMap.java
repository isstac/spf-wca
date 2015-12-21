package wcanalysis.heuristic;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import att.grappa.Attribute;
import isstac.structure.cfg.util.DotAttribute;

/**
 * @author Rody Kersten
 */
public abstract class DecisionMap implements DotAttribute, Serializable, Iterable<DecisionHistory> {
  private static final long serialVersionUID = 3896815168451849460L;

  private HashMap<DecisionHistory,Integer> decHists = new HashMap<DecisionHistory,Integer>();
  
  @Override
  public Attribute getAttribute() {
    return null;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for(DecisionHistory decHist : decHists.keySet()) {
      sb.append(decHist.toString()).append('\n');
    }
    return sb.toString();
  }
  
  //TODO: We could perform some caching here if it turns out to problematic
  public boolean contains(DecisionHistory history) {
    return this.decHists.containsKey(history);
  }
  
  public Integer put(DecisionHistory history, Integer i) {
    return this.decHists.put(history,i);
  }
  
  public int getNumberOfDecisionHistories() {
    return this.decHists.size();
  }
  
  public Collection<DecisionHistory> getDecisionHistories() {
    return this.decHists.keySet();
  }
  
  @Override
  public Iterator<DecisionHistory> iterator() {
    return this.decHists.keySet().iterator();
  }
}