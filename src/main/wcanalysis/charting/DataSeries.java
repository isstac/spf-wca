package wcanalysis.charting;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kasper Luckow
 */
public class DataSeries {
  private ArrayList<Double> X = new ArrayList<>();
  private ArrayList<Double> Y = new ArrayList<>();
  private final String seriesName;

  public DataSeries(String seriesName) {
    this.seriesName = seriesName;
  }


  public String getSeriesName() {
    return seriesName;
  }

  public void add(double x, double y) {
    X.add(x);
    Y.add(y);
  }

  public double[] getY() {
    return toArray(Y);
  }

  public double[] getX() {
    return toArray(X);
  }

  private double[] toArray(ArrayList<Double> data) {
    double[] arr = new double[data.size()];
    for(int i = 0; i < data.size(); i++) {
      arr[i] = data.get(i);
    }
    return arr;
  }
}
