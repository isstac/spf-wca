package wcanalysis.heuristic.policy;

/**
 * @author Kasper Luckow
 *
 */
public class PolicyUnificationException extends Exception {

  private static final long serialVersionUID = 2131L;
  public PolicyUnificationException(String msg) {
    super(msg);
  }

  public PolicyUnificationException(Exception e) {
    super(e);
  }
}
