package wcanalysis.fitting;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import wcanalysis.charting.DataCollection;

/**
 * @author Kasper Luckow
 *
 */
public class FunctionFitter {
  public static XYSeriesCollection computeSeries(DataCollection rawData, int predictionModelSize) {
    XYSeriesCollection dataset = new XYSeriesCollection();

    XYSeries rawSeries = new XYSeries("Raw");
    double[] rxs = rawData.getX();
    double[] rys = rawData.getY();
    for(int i = 0; i < rawData.size(); i++) //ugly conversion and ugly non-iterable
      rawSeries.add(rxs[i], rys[i]);


    DecimalFormat df = new DecimalFormat("#.00000");
    List<TrendModelData> trendLines = new ArrayList<>();

    //The prediction models we are considering
    trendLines.add(new TrendModelData(new PolyTrendLine(1), "1st poly"));
    trendLines.add(new TrendModelData(new PolyTrendLine(2), "2nd poly"));
    trendLines.add(new TrendModelData(new PolyTrendLine(3), "3rd poly"));
    trendLines.add(new TrendModelData(new ExpTrendLine(), "exp"));
    trendLines.add(new TrendModelData(new PowerTrendLine(), "pow"));
    trendLines.add(new TrendModelData(new LogTrendLine(), "log"));
    trendLines.add(new TrendModelData(new NLogTrendLine(), "nlog"));
    
    HashMap<TrendModelData, XYSeries> trend2series = new HashMap<>();
    
    for(TrendModelData trendData : trendLines) {
      trendData.trendLine.setValues(rawData.getY(), rawData.getX());
      trend2series.put(trendData, new XYSeries(trendData.desc + ": "  + 
          trendData.trendLine.getFunction() + " (r^2="+df.format(trendData.trendLine.getRSquared()) + ")"));
    }

    double[] xPredict = new double[predictionModelSize];
    double[] xs = rawData.getX();
    System.arraycopy(xs, 0, xPredict, 0, xs.length);
    for(int i = xs.length; i < predictionModelSize; i++)
      xPredict[i] = xPredict[i-1] + 1.0;

    for(int i = 0; i < predictionModelSize; i++) {
      double x = xPredict[i];
      for(TrendModelData trendData : trendLines) {
        XYSeries series = trend2series.get(trendData);
        if(trendData.trendLine.getDomainPredicate().apply(x)) {
          double yPred = trendData.trendLine.predict(x);
          series.add(x, yPred);
        }
      }
    }
    
    dataset.addSeries(rawSeries);
    for(XYSeries series : trend2series.values()) {
      dataset.addSeries(series);
    }
    return dataset;
  }
}
