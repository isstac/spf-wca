package wcanalysis.heuristic.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import att.grappa.Attribute;
import isstac.structure.cfg.Block;
import isstac.structure.cfg.CFGGenerator;
import isstac.structure.cfg.util.DotAttribute;
import wcanalysis.heuristic.Decision;
import wcanalysis.heuristic.PathProjector;

/**
 * @author Kasper Luckow
 *
 */
public class PathVisualizer extends PathProjector {

  //marks whether block is a symbolic decision
  private static class Covered implements DotAttribute, Serializable {
    private static final long serialVersionUID = -1276855202384536042L;

    private static class SeqPair implements Serializable {
      private static final long serialVersionUID = -5924282283709670350L;
      private int seqNum;
      private String choice;
      public SeqPair(int seqNum, String choice) {
        this.seqNum = seqNum;
        this.choice = choice;
      }
      @Override
      public String toString() {
        return "[" + seqNum + "," + choice + "]";
      }
    }
    private List<SeqPair> visitSequence = new ArrayList<>();
    
    public void addToSequence(int num, String choice) {
      this.visitSequence.add(new SeqPair(num, choice));
    }
    
    @Override
    public Attribute getAttribute() {
      return new Attribute(Attribute.NODE, Attribute.COLOR_ATTR, "red");
    }

    @Override
    public String getLabelString() {
      StringBuilder sb = new StringBuilder();
      int breakat = 10;
      Iterator<SeqPair> seq = visitSequence.iterator();
      int i = 0;
      while(seq.hasNext()) {
        sb.append(seq.next());
        if(seq.hasNext())
          sb.append(',');
        if(++i % breakat == 0)
          sb.append('\n');
      }
      if(sb.charAt(sb.length()-1) != '\n')
        sb.append('\n');
      return sb.toString();
    }
  }
  
  private int sequenceNumber = 0;
  public PathVisualizer(CFGGenerator cfgGenerator) {
    super(cfgGenerator);
  }
  
  @Override
  protected void projectDecision(Block basicBlock, Decision dec) {
    Integer choice = dec.getChoice();
    Covered cov = basicBlock.getAttribute(Covered.class);
    if(cov == null) {
      cov = new Covered();
      basicBlock.setAttribute(cov);
    }
    cov.addToSequence(this.sequenceNumber++, (choice == 1) ? "T" : (choice == 0) ? "F" : ""+choice);
  }
}
