package wcanalysis.fitting;

import java.text.DecimalFormat;

import com.google.common.base.Predicate;

/**
 * @author Kasper Luckow
 * TODO: check that the output of getFunction is correct
 */
public class LogTrendLine extends OLSTrendLine {
  @Override
  protected double[] xVector(double x) {
    return new double[]{1,Math.log(x)};
  }

  @Override
  protected boolean logY() {
    return false;
  }

  @Override
  public String getFunction() {
    StringBuilder functionSb = new StringBuilder();
    DecimalFormat df = new DecimalFormat("#.00");
    
    double b = super.coef.getColumn(0)[1];
    functionSb.append(df.format(super.coef.getColumn(0)[0]))
              .append(" + ").append("log(" + df.format(b) + ")");
    return functionSb.toString();
  }

  @Override
  public Predicate<Double> getDomainPredicate() {
    return new Predicate<Double>() {
      @Override
      public boolean apply(Double arg0) {
        return arg0 > 0.0;
      }
    };
  }

  @Override
  public Predicate<Double> getRangePredicate() {
    return new Predicate<Double>() {
      @Override
      public boolean apply(Double arg0) {
        return true;
      }
    };
  }
}
