package wcanalysis;

/**
 * @author Kasper Luckow
 * From http://stackoverflow.com/questions/17592139/trend-lines-regression-curve-fitting-java-library
 */
public interface TrendLine {
  public void setValues(double[] y, double[] x);
  public double predict(double inputSize);
  public String getFunction();
  public double getRSquared();
  public double getAdjustedRSquared();
}