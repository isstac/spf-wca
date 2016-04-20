package wcanalysis.heuristic.policy;

import java.util.Set;

import wcanalysis.heuristic.WorstCasePath;

/**
 * @author Kasper Luckow
 *
 */
public class HistoryBasedPolicyGenerator implements PolicyGenerator<HistoryBasedPolicy> {

  private HistoryBasedPolicy.Builder bldr;
  
  private HistoryBasedPolicyGenerator() {
    bldr = new HistoryBasedPolicy.Builder();
  }
  
  public HistoryBasedPolicyGenerator(int maxHistSize) {
    this();
    bldr.setMaxHistorySize(maxHistSize);
  }
  
  public HistoryBasedPolicyGenerator(boolean adaptive) {
    this();
    bldr.setAdaptive(adaptive);
  }
  
  @Override
  public HistoryBasedPolicy generate(Set<String> measuredMethods, WorstCasePath path) {
    return bldr.build(path, measuredMethods);
  }
}
