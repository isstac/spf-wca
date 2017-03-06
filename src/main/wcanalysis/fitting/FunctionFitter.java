/*
 * MIT License
 *
 * Copyright (c) 2017 The ISSTAC Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package wcanalysis.fitting;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.math3.exception.MathIllegalArgumentException;

import gov.nasa.jpf.util.JPFLogger;
import wcanalysis.charting.DataCollection;
import wcanalysis.charting.DataSeries;

/**
 * @author Kasper Luckow
 *
 */
public class FunctionFitter {
  
  public static final Logger logger = JPFLogger.getLogger(FunctionFitter.class.getName());
  
  public static Collection<DataSeries> computeSeries(DataCollection rawData, int predictionModelSize) {
    Collection<DataSeries> series = new HashSet<>();


    DataSeries rawSeries = new DataSeries("Raw");
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
    
    HashMap<TrendModelData, DataSeries> trend2series = new HashMap<>();
    
    Iterator<TrendModelData> tmIter = trendLines.iterator();
    while(tmIter.hasNext()) {
      TrendModelData trendData = tmIter.next();
      try {
        trendData.trendLine.setValues(rawData.getY(), rawData.getX());
        DataSeries s = new DataSeries(trendData.desc);
        s.setFunction(trendData.trendLine.getFunction());
        s.setR2(df.format(trendData.trendLine.getRSquared()));

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
        DataSeries s = trend2series.get(trendData);
        if(trendData.trendLine.getDomainPredicate().apply(x)) {
          double yPred = trendData.trendLine.predict(x);
          s.add(x, yPred);
        }
      }
    }

    series.add(rawSeries);
    for(DataSeries s : trend2series.values()) {
      series.add(s);
    }
    return series;
  }
  
  private static final Object[] FILE_HEADER = {"x","y"};

  public static void seriesToCSV(DataSeries series, Writer w) throws IOException {
    
    CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
    try(CSVPrinter csvFilePrinter = new CSVPrinter(w, csvFileFormat)) {
      csvFilePrinter.printRecord(FILE_HEADER);
      double[] x = series.getX();
      double[] y = series.getY();
      for(int i = 0; i < x.length; i++) {
        csvFilePrinter.printRecord(x[i], y[i]);
      }
    }
  }
}
