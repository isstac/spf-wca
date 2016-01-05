package wcanalysis.heuristic.policy;

import java.util.Set;

import wcanalysis.heuristic.WorstCasePath;

/**
 * @author Kasper Luckow
 *
 */
public interface PolicyGenerator<T extends Policy> {
  public T generate(Set<String> measuredMethods, WorstCasePath path);
}
