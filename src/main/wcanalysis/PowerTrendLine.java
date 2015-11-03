package wcanalysis;

/**
 * @author Kasper Luckow
 */
public class PowerTrendLine extends OLSTrendLine {
  @Override
  protected double[] xVector(double x) {
    return new double[]{1,Math.log(x)};
  }

  @Override
  protected boolean logY() {return true;}

  @Override
  protected String getFunction() {
    // TODO Auto-generated method stub
    return null;
  }

}
