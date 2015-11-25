package wcanalysis;

import java.text.DecimalFormat;

/**
 * @author Kasper Luckow
 * TODO: check that the output of getFunction is correct
 */
public class PowerTrendLine extends OLSTrendLine {
  @Override
  protected double[] xVector(double x) {
    return new double[]{1,Math.log(x)};
  }

  @Override
  protected boolean logY() {return true;}

  @Override
  public String getFunction() {
    StringBuilder functionSb = new StringBuilder();
    DecimalFormat df = new DecimalFormat("#.00");
    
    double b = super.coef.getColumn(0)[1];
    functionSb.append(df.format(super.coef.getColumn(0)[0]))
              .append(" + ").append("x^" + df.format(b));
    return functionSb.toString();
  }

}
