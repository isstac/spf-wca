package wcanalysis.fitting;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import gov.nasa.jpf.util.JPFLogger;
import wcanalysis.charting.DataCollection;

/**
 * @author Kasper Luckow
 *
 */
public class FunctionFitter {
  
  public static final Logger logger = JPFLogger.getLogger(FunctionFitter.class.getName());
  
  public static XYSeriesCollection computeSeries(DataCollection rawData, int predictionModelSize) {
    XYSeriesCollection dataset = new XYSeriesCollection();

    XYSeries rawSeries = new XYSeries("Raw");
    rawSeries.setDescription("Raw");
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
    trendLines.add(new TrendModelData(new FactorialTrendline(), "fac"));
    
    HashMap<TrendModelData, XYSeries> trend2series = new HashMap<>();
    
    Iterator<TrendModelData> tmIter = trendLines.iterator();
    while(tmIter.hasNext()) {
      TrendModelData trendData = tmIter.next();
      try {
        trendData.trendLine.setValues(rawData.getY(), rawData.getX());
        String func = trendData.trendLine.getFunction() + " (r^2="+df.format(trendData.trendLine.getRSquared()) + ")";
        XYSeries s = new XYSeries(trendData.desc + ": "  + func);
        s.setDescription(trendData.desc);
        trend2series.put(trendData, s);
      } catch(MathIllegalArgumentException e) {
        logger.severe(e.getMessage());
        tmIter.remove();
      }
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
  
  private static final Object[] FILE_HEADER = {"x","y"};
  public static void seriesToCSV(XYSeries series, Writer w) throws IOException {
    
    CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
    try(CSVPrinter csvFilePrinter = new CSVPrinter(w, csvFileFormat)) {
      csvFilePrinter.printRecord(FILE_HEADER);
      for(int i = 0; i < series.getItemCount(); i++) {
        csvFilePrinter.printRecord(series.getX(i), series.getY(i));
      }
    }
  }
}
