package wcanalysis.fitting;

import java.text.DecimalFormat;

import com.google.common.base.Predicate;

/**
 * @author Kasper Luckow
 * TODO: check that the output of getFunction is correct
 */
public class NLogTrendLine extends OLSTrendLine {
  @Override
  protected double[] xVector(double x) {
    return new double[]{1,x, x*Math.log(x)};
  }

  @Override
  protected boolean logY() {
    return false;
  }

  @Override
  public String getFunction() {
    StringBuilder functionSb = new StringBuilder();
    DecimalFormat df = new DecimalFormat("#.00");
    double a = super.coef.getColumn(0)[0];
    double b = super.coef.getColumn(0)[1];
    double c = super.coef.getColumn(0)[2];
    
    if(a != 0.0)
      functionSb.append(df.format(a)).append(" + ");
    if(b != 0.0)
      functionSb.append(df.format(b) + "x").append(" + ");
    if(c != 0.0)    
      functionSb.append(df.format(c) + "x*log(x)");
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
