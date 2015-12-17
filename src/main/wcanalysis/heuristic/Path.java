package wcanalysis.heuristic;

import java.util.Iterator;
import java.util.LinkedList;

class Path implements Iterable<Decision> {
  private LinkedList<Decision> path = new LinkedList<>();
  public void appendDecision(Decision dec) {
    path.add(dec);
  }
  
  public Path() { }
  
  public Path(Path other) {
    Decision cur = (other.getSize() > 0) ? other.getLast().copy() : null;
    while(cur != null) {
      this.prependDecision(cur);
      cur = cur.getPrev();
    }
  }
  
  public Path copy() {
    return new Path(this);
  }
  
  public void prependDecision(Decision dec) {
    path.addFirst(dec);
  }
  
  public Decision getLast() {
    return this.path.getLast();
  }
  
  public int getSize() {
    return this.path.size();
  }
  
  @Override
  public Iterator<Decision> iterator() {
    return path.iterator();
  }
  
  @Override
  public String toString() {
    StringBuilder pathBuilder = new StringBuilder();
    Iterator<Decision> iter = this.iterator();
    Decision cur = null;
    while(iter.hasNext()) {
      cur = iter.next();
      pathBuilder.append("[[" +  
            "l:" + cur.getInstruction().getLineNumber() + "(o:" +
            cur.getInstruction().getInstructionIndex() + "), " + 
            ((cur.getChoice() == 1) ? 'T' : 'F') + "]" + cur.frame + "]");
      if(iter.hasNext())
        pathBuilder.append(", ");
    }
    return pathBuilder.toString();
  }
}