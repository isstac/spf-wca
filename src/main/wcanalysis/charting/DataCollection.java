package wcanalysis.charting;

import java.util.ArrayList;

/**
 * @author Kasper Luckow
 */
public class DataCollection {
  
  private ArrayList<Double> xs = new ArrayList<>();
  private ArrayList<Double> ys = new ArrayList<>();
  
  public void addDatapoint(double x, double y) {
    this.xs.add(x);
    this.ys.add(y);
  }
  
  public double[] getX() {
    return convert(xs);
  }
  
  public double[] getY() {
    return convert(ys);
  }
  
  private static double[] convert(ArrayList<Double> d) {
    double[] dc = new double[d.size()];
    for(int i=0; i < d.size(); i++)
      dc[i] = d.get(i);
    return dc;
  }
  
  public int size() {
    return xs.size();
  }
  
}
