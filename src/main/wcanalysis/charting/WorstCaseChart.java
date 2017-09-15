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

package wcanalysis.charting;

import com.google.common.base.Preconditions;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import javax.swing.*;

import wcanalysis.WorstCaseAnalyzer;

/**
 * @author Kasper Luckow
 */
public class WorstCaseChart extends ApplicationFrame {
  public static class ChartBuilder {
    private final String chartTitle;
    private final String xAxisLabel;
    private final String yAxisLabel;

    private final Collection<DataSeries> seriesCollection = new ArrayList<>();
    private DataSeries rawSeries = null;

    public ChartBuilder(String title, String xAxisLabel, String yAxisLabel) {
      this.chartTitle = title;
      this.xAxisLabel = xAxisLabel;
      this.yAxisLabel = yAxisLabel;
    }

    public ChartBuilder addSeries(DataSeries series) {
      this.seriesCollection.add(series);
      return this;
    }

    public ChartBuilder setRawSeries(DataSeries series) {
      this.rawSeries = series;
      return this;
    }

    public WorstCaseChart build() {
      return new WorstCaseChart(this.chartTitle, this.xAxisLabel, this.yAxisLabel, this.seriesCollection,
          this.rawSeries);
    }
  }

  private static final long serialVersionUID = 1760145418311574070L;

  public WorstCaseChart(String chartTitle, String xAxisLabel, String yAxisLabel, Collection<DataSeries>
      seriesCollection, DataSeries rawSeries) {
    super(WorstCaseAnalyzer.class.getName());

    createSeries(chartTitle, xAxisLabel, yAxisLabel, seriesCollection, rawSeries);

  }

  private void createSeries(String chartTitle, String xAxisLabel, String yAxisLabel,
                            Collection<DataSeries> seriesCollection, DataSeries rawSeries) {
    XYSeriesCollection ccDataset = new XYSeriesCollection();


    final int SERIES_ID = 0;
    //construct the plot
    XYPlot plot = new XYPlot();
    plot.setDataset(SERIES_ID, ccDataset);

    //customize the plot with renderers and axis
    XYLineAndShapeRenderer xyLineRenderer1 = new XYLineAndShapeRenderer();
    xyLineRenderer1.setSeriesShapesVisible(SERIES_ID, true);
    plot.setRenderer(SERIES_ID, xyLineRenderer1);

    // fill paint
    // for first series
    XYLineAndShapeRenderer xyLineRenderer2 = new XYLineAndShapeRenderer();
    xyLineRenderer2.setSeriesShapesVisible(SERIES_ID, false);

    plot.setRangeAxis(SERIES_ID, new NumberAxis(yAxisLabel));
    plot.setDomainAxis(new NumberAxis(xAxisLabel));

    //Map the data to the appropriate axis
    plot.mapDatasetToRangeAxis(SERIES_ID, SERIES_ID);

    //generate the chart
    JFreeChart timeSeriesChart = new JFreeChart(chartTitle, getFont(), plot, true);
    timeSeriesChart.setBorderPaint(Color.white);


    // Make panel for chart
    ChartPanel chartPanel = new ChartPanel(timeSeriesChart);

    final Map<JCheckBox, XYSeries> box2series = new HashMap<>();

    // Item listener enabling toggling of plots
    ItemListener listener = e -> {
      if (e.getItem() instanceof JCheckBox) {
        JCheckBox checkbox = (JCheckBox) e.getItem();
        if (!checkbox.isSelected()) {
          XYSeries series1 = box2series.get(checkbox);
          ccDataset.removeSeries(series1);
        } else {
          XYSeries series1 = box2series.get(checkbox);
          ccDataset.addSeries(series1);
        }
      }
    };

    // Create and set up the window.
    JFrame frame = new JFrame("Worst Case Prediction Model");
    frame.setLayout(new BorderLayout());
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel checkPanel = new JPanel(new GridLayout(0, 1));
    List<DataSeries> sortedSeries = new ArrayList<>(seriesCollection);

    sortedSeries.sort((o1, o2) -> o1.getSeriesName().compareTo(o2.getSeriesName()));

    if(rawSeries != null) {
      JPanel rawPanel = new JPanel(new BorderLayout());
      rawPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(Color
          .BLUE, Color.BLACK), rawSeries.getSeriesName()));
      JCheckBox rawCheckbox = new JCheckBox("Toggle Plot");
      rawCheckbox.setSelected(true);
      rawPanel.add(rawCheckbox);
      rawCheckbox.addItemListener(listener);



      XYSeries rawXYSeries = new XYSeries("Raw");
      for(int i = 0; i < rawSeries.getX().length; i++) {
        rawXYSeries.add(rawSeries.getX()[i], rawSeries.getY()[i]);
      }
      ccDataset.addSeries(rawXYSeries);
      box2series.put(rawCheckbox, rawXYSeries);
      checkPanel.add(rawPanel);
    }

    for(DataSeries ser : sortedSeries) {
      JPanel boxPanel = new JPanel(new GridLayout(0, 1));
      boxPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
          ser.getSeriesName()));
      JCheckBox checkbox = new JCheckBox("Toggle Plot");
      checkbox.setSelected(true);
      checkbox.addItemListener(listener);
      boxPanel.add(checkbox);
      boxPanel.add(new JLabel("Function: " + ser.getFunction()));
      boxPanel.add(new JLabel("r^2: " + ser.getR2()));


      XYSeries xySeries = new XYSeries(ser.getSeriesName());
      for(int i = 0; i < ser.getX().length; i++) {
        xySeries.add(ser.getX()[i], ser.getY()[i]);
      }
      ccDataset.addSeries(xySeries);
      checkPanel.add(boxPanel);
      box2series.put(checkbox, xySeries);
    }

    JPanel mainPanel = new JPanel(new BorderLayout());

    mainPanel.add(checkPanel, BorderLayout.LINE_START);
    mainPanel.add(chartPanel, BorderLayout.CENTER);
    frame.add(mainPanel);

    setContentPane(mainPanel);
  }
}
