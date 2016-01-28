package wcanalysis.fitting;

/**
 * @author Kasper Luckow
 *
 */
public class TrendModelData {
  final TrendLine trendLine;
  final String desc;
  public TrendModelData(TrendLine trend, String desc) {
    this.trendLine = trend;
    this.desc = desc;
  }
}
