package wcanalysis.heuristic;

/**
 * @author Kasper Luckow
 */
class HeuristicDecisionCounts {
  int trueCount = 0;
  int falseCount = 0;
  public HeuristicDecisionCounts() { }
  
  public void incrementForChoice(int choice) {
    if(choice == 1)
      trueCount++;
    else if(choice == 0)
      falseCount++;
    else
      throw new UnsupportedOperationException("Decision " + choice + " not supported");
  }
  
  public HeuristicDecisionCounts(HeuristicDecisionCounts other) {
    this.trueCount = other.trueCount;
    this.falseCount = other.falseCount;
  }
}