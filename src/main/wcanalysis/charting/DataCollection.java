package wcanalysis.charting;

/**
 * @author Kasper Luckow
 */
public class DataCollection {
  public final double[] x;
  public final double[] y;
  
  public int size;
  
  private int index = 0;
  public DataCollection(int size) {
    this.x = new double[size];
    this.y = new double[size];
    this.size = size;
  }
  
  public void addDatapoint(double x, double y) {
    this.x[index] = x;
    this.y[index] = y;
    index++;
  }
}
