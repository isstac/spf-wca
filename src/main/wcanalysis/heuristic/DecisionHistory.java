package wcanalysis.heuristic;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Iterator;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import att.grappa.Attribute;
import isstac.structure.cfg.util.DotAttribute;

/**
 * @author Kasper Luckow
 */
public class DecisionHistory extends ArrayDeque<Decision> implements DotAttribute, Serializable {
  private static final long serialVersionUID = 5700782521206267839L;
  private int maxSize;

  public DecisionHistory(int size) {
    super();
    this.maxSize = size;
  }


  @Override
  public void push(Decision dec) {
    if(this.maxSize == 0)
      return;
    //If the stack is too big, remove elements until it's the right size.
    while(this.size() >= this.maxSize) {
      this.removeLast();
    }
    super.push(dec);
  }
    
  private void addIt(Decision dec) {
    //If the stack is too big, remove elements until it's the right size.
    while(this.size() >= this.maxSize) {
      this.removeLast();
    }
    super.push(dec);
  }

  @Override
  public Attribute getAttribute() {
    return null;
  }

  @Override
  public String getLabelString() {
    return toString();
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('<');
    Iterator<Decision> decIter = this.iterator();
    while(decIter.hasNext()) {
      sb.append(decIter.next().toString());
      if(decIter.hasNext())
        sb.append(", ");
    }
    sb.append('>');
    return sb.toString();
  }

  @Override
  public int hashCode(){
    HashCodeBuilder bldr = new HashCodeBuilder();
    for(Decision dec : this) {
      bldr.append(dec);
    }
    return bldr.toHashCode();
  }

  @Override
  public boolean equals(Object obj){
    if(obj instanceof DecisionHistory){
      final DecisionHistory other = (DecisionHistory)obj;
      
      if(this.size() != other.size())
        return false;
      
      EqualsBuilder bldr = new EqualsBuilder();
      Iterator<Decision> decisions = this.iterator();
      Iterator<Decision> otherDecisions = other.iterator();
      while(decisions.hasNext()) { //this and other history has same size
        bldr.append(decisions.next(), otherDecisions.next());
      }
      return bldr.isEquals();
    } else{
      return false;
    }
  }
  
  public DecisionHistory copy() {
    DecisionHistory newHistory = new DecisionHistory(this.maxSize);
    for(Decision dec : this)
      newHistory.push(dec.copy());
    return newHistory;
  }
  
  
}