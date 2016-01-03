package wcanalysis.heuristic;

import java.util.ArrayList;

/**
 * @author Kasper Luckow
 *
 */
public class HistoryBasedPolicyGenerator implements PolicyGenerator<HistoryBasedPolicy> {

  private final int maxHistSize;
  
  public HistoryBasedPolicyGenerator(int maxHistSize) {
    this.maxHistSize = maxHistSize;
  }
  
  public HistoryBasedPolicyGenerator() {
    this.maxHistSize = HistoryBasedPolicy.NO_LIMIT;
  }
  
  @Override
  public HistoryBasedPolicy generate(WorstCasePath path) {
    return new HistoryBasedPolicy(path, this.maxHistSize);
  }
}
