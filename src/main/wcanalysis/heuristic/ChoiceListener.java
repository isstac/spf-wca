package wcanalysis.heuristic;

import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;

/**
 * @author Kasper Luckow
 *
 */
public interface ChoiceListener {
  public void choiceMade(PCChoiceGenerator cg, int choiceMade);
}
