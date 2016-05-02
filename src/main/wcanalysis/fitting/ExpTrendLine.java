package wcanalysis.fitting;

import java.text.DecimalFormat;

import com.google.common.base.Predicate;

import gov.nasa.jpf.util.Pair;

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
  public String getFunction() {
    StringBuilder functionSb = new StringBuilder();
    DecimalFormat df = new DecimalFormat("#.00");
    
    double b = super.coef.getColumn(0)[1];
    functionSb.append("e^(").append(df.format(super.coef.getColumn(0)[0]))
              .append(" + ").append(df.format(b) + "*x)");
    return functionSb.toString();
  }

  @Override
  public Predicate<Double> getDomainPredicate() {
    return new Predicate<Double>() {
      @Override
      public boolean apply(Double arg0) {
        return true;
      }
    };
  }

  @Override
  public Predicate<Double> getRangePredicate() {
    return new Predicate<Double>() {
      @Override
      public boolean apply(Double arg0) {
        return arg0 > 0.0;
      }
    };
  }  
}
