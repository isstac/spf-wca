package wcanalysis.heuristic;

import java.io.Serializable;

public class Resolution implements Serializable {
  public static enum ResolutionType implements Serializable {
    PERFECT,
    HISTORY, 
    INVARIANT,
    UNRESOLVED,
    NEW_CHOICE;
  }
  
  private static final long serialVersionUID = 2247935610676857227L;
  public final ResolutionType type;
  public final int choice;
  public Resolution(int choice, ResolutionType type) {
    this.choice = choice;
    this.type = type;
  }
}