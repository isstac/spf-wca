package wcanalysis.heuristic.util;

import gov.nasa.jpf.symbc.numeric.PathCondition;

/**
 * @author Kasper Luckow
 */
public interface ConstraintsConverter {
  public String convert(PathCondition pc);
}
