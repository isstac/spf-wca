package wcanalysis.heuristic.policy;

import java.util.Set;

import wcanalysis.heuristic.WorstCasePath;

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
  public HistoryBasedPolicy generate(Set<String> measuredMethods, WorstCasePath path) {
    return new HistoryBasedPolicy(path, measuredMethods, this.maxHistSize);
  }
}
