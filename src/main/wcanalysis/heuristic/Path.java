package wcanalysis.heuristic;

import java.util.Iterator;
import java.util.LinkedList;

class Path extends LinkedList<Decision> {
  
  private static final long serialVersionUID = -1691414612424473640L;
  
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
            ((cur.getChoice() == 1) ? 'T' : (cur.getChoice() == 0) ? 'F' : cur.getChoice()) + "]" + cur.getContext() + "]");
      if(iter.hasNext())
        pathBuilder.append(", ");
    }
    return pathBuilder.toString();
  }
}