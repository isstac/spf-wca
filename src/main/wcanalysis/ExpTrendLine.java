package wcanalysis;

import java.text.DecimalFormat;

/**
 * @author Kasper Luckow
 * TODO: check that the output of getFunction is correct
 */
public class ExpTrendLine extends OLSTrendLine {
  @Override
  protected double[] xVector(double x) {
    return new double[]{1,x};
  }

  @Override
  protected boolean logY() {
    return true;
  }

  @Override
  protected String getFunction() {
    StringBuilder functionSb = new StringBuilder();
    DecimalFormat df = new DecimalFormat("#.00");
    
    double b = super.coef.getColumn(0)[1];
    functionSb.append(df.format(super.coef.getColumn(0)[0]))
              .append(" + ").append(df.format(b) + "^x");
    return functionSb.toString();
  }
}
