package wcanalysis.fitting;

import com.google.common.base.Predicate;

/**
 * @author Kasper Luckow
 * From http://stackoverflow.com/questions/17592139/trend-lines-regression-curve-fitting-java-library
 */
public interface TrendLine {

  public Predicate<Double> getDomainPredicate();
  public Predicate<Double> getRangePredicate();
  public void setValues(double[] y, double[] x);
  public double predict(double inputSize);
  public String getFunction();
  public double getRSquared();
  public double getAdjustedRSquared();
}