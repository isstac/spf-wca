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

import org.apache.commons.math3.exception.MathIllegalArgumentException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import gov.nasa.jpf.util.JPFLogger;
import wcanalysis.charting.DataSeries;

/**
 * @author Kasper Luckow
 *
 */
public class FunctionFitter {
  
  public static final Logger logger = JPFLogger.getLogger(FunctionFitter.class.getName());
  
  public static Collection<DataSeries> computePredictionSeries(double[] xs, double[] ys,
                                                     int predictionModelSize) {

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

    // Create data series
    while(tmIter.hasNext()) {
      TrendModelData trendData = tmIter.next();
      try {
        trendData.trendLine.setValues(ys, xs);
        DataSeries s = new DataSeries(trendData.desc, predictionModelSize);
        s.setFunction(trendData.trendLine.getFunction());
        s.setR2(df.format(trendData.trendLine.getRSquared()));

        trend2series.put(trendData, s);
      } catch(MathIllegalArgumentException e) {
        logger.severe(e.getMessage());
        tmIter.remove();
      }
    }

    double[] xPredict = new double[predictionModelSize];
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

    return trend2series.values();

//    Collection<XYSeries> predictionSeries = new HashSet<>();
//    // Add raw data
//    XYSeries rawSeries = new XYSeries("raw");
//    for(int i = 0; i < xs.length; i++) {
//      rawSeries.add(xs[i], ys[i]);
//    }
//    predictionSeries.add(rawSeries);
//
//    // Add all the other series
//    for(DataSeries s : trend2series.values()) {
//      XYSeries jfreeSeries = new XYSeries(s.getSeriesName() + ": " +
//          s.getFunction() + "(r^2=" + s.getR2() + ")");
//      double xss[] = s.getX();
//      double yss[] = s.getY();
//      for(int i = 0; i < xss.length; i++) {
//        jfreeSeries.add(xss[i], yss[i]);
//      }
//      predictionSeries.add(jfreeSeries);
//    }
//    return predictionSeries;
  }
}
