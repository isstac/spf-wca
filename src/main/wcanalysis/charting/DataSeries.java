package wcanalysis.charting;

/**
 * @author Kasper Luckow
 */
public class DataSeries {
  private DataCollection data = new DataCollection();
  private final String seriesName;
  private String r2;
  private String function;

  public DataSeries(String seriesName) {
    this.seriesName = seriesName;
  }


  public String getSeriesName() {
    return seriesName;
  }

  public void add(double x, double y) {
    this.data.addDatapoint(x, y);
  }

  public double[] getY() {
    return this.data.getY();
  }

  public double[] getX() {
    return this.data.getX();
  }

  public int size() {
    return this.data.size();
  }

  public String getR2() {
    return r2;
  }

  public void setR2(String r2) {
    this.r2 = r2;
  }

  public String getFunction() {
    return function;
  }

  public void setFunction(String function) {
    this.function = function;
  }
}
