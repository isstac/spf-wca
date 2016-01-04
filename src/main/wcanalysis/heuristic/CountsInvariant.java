package wcanalysis.heuristic;

import java.util.Map;

/**
 * @author Kasper Luckow
 */
public interface CountsInvariant {
  public boolean applies(Map<Integer, Integer> choice2counts);
}
