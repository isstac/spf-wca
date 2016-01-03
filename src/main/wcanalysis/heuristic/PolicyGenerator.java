package wcanalysis.heuristic;

import org.apache.commons.io.output.WriterOutputStream;

/**
 * @author Kasper Luckow
 *
 */
public interface PolicyGenerator<T extends Policy> {
  public T generate(WorstCasePath path);
}
