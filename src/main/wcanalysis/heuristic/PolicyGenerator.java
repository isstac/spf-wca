package wcanalysis.heuristic;

import java.util.Set;

/**
 * @author Kasper Luckow
 *
 */
public interface PolicyGenerator<T extends Policy> {
  public T generate(Set<String> measuredMethods, WorstCasePath path);
}
