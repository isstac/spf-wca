/*
 * Copyright 2017 Carnegie Mellon University Silicon Valley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;

import javax.swing.*;

/**
 * @author Kasper Luckow
 */
public class WorstCaseChart {
  public static JFrame createChartPanel(Collection<DataSeries> series) {
    return createFrame(buildBasicChart(series), series);
  }

  public static JFrame createChartPanel(Collection<DataSeries> series, double inputReq,
                                        double resReq) {
    final XYChart chart = buildBasicChart(series);


    // Add lines to the chart for resource and input requirements

    //We can take any series to get max X---supposedly
    DataSeries fst = series.iterator().next();
    double[] xs = fst.getX();
    double maxX = xs[xs.length - 1];

    double maxY = -1;
    for (DataSeries o : series) {
      double[] ys = fst.getY();
      if (ys[ys.length - 1] > maxY) {
        maxY = ys[ys.length - 1];
      }
    }

    //add input req line:
    chart.addSeries("Input Requirement", new double[]{inputReq, inputReq}, new double[]{0, maxY});

    //add resource req line:
    chart.addSeries("Resource Requirement",
        new double[]{0, maxX}, new double[]{resReq, resReq});
    return createFrame(chart, series);
  }

  private static XYChart buildBasicChart(Collection<DataSeries> series) {
    Preconditions.checkNotNull(series);
    Preconditions.checkArgument(!series.isEmpty());

    final XYChart chart = new XYChartBuilder()
        .width(800)
        .height(600)
        .title("Worst Case Prediction Model")
        .xAxisTitle("Input Size")
        .yAxisTitle("Cost")
        .build();

    // Customize Chart
    chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
    chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);

    // Add Series
    for (DataSeries s : series) {
      chart.addSeries(s.getSeriesName(), s.getX(), s.getY());
    }
    return chart;
  }

  private static JFrame createFrame(XYChart chart, Collection<DataSeries> series) {

    XChartPanel chartPanel = new XChartPanel<>(chart);

    final Map<JCheckBox, DataSeries> box2series = new HashMap<>();

    // Item listener enabling toggling of plots
    ItemListener listener = e -> {
      if (e.getItem() instanceof JCheckBox) {
        JCheckBox checkbox = (JCheckBox) e.getItem();
        if (!checkbox.isSelected()) {
          String series1 = box2series.get(checkbox).getSeriesName();
          chart.removeSeries(series1);
        } else {
          DataSeries series1 = box2series.get(checkbox);
          chart.addSeries(series1.getSeriesName(), series1.getX(), series1.getY());
        }
        chartPanel.revalidate();
        chartPanel.repaint();
      }
    };

    // Create and set up the window.
    JFrame frame = new JFrame("Worst Case Prediction Model");
    frame.setLayout(new BorderLayout());
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel checkPanel = new JPanel(new GridLayout(0, 1));
    List<DataSeries> sortedSeries = new ArrayList<>();
    DataSeries rawSeries = null;
    for (DataSeries ds : series) {
      if (ds.getSeriesName().equalsIgnoreCase("raw")) {
        rawSeries = ds;
      } else {
        sortedSeries.add(ds);
      }
    }

    assert rawSeries != null;

    sortedSeries.sort((o1, o2) -> o1.getSeriesName().compareTo(o2.getSeriesName()));

    JPanel rawPanel = new JPanel(new BorderLayout());
    rawPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(Color
            .BLUE, Color.BLACK),
        rawSeries.getSeriesName()));
    JCheckBox rawCheckbox = new JCheckBox("Toggle Plot");
    rawCheckbox.setSelected(true);
    rawPanel.add(rawCheckbox);
    rawCheckbox.addItemListener(listener);

    box2series.put(rawCheckbox, rawSeries);
    checkPanel.add(rawPanel);

    for (DataSeries ser : sortedSeries) {
      JPanel boxPanel = new JPanel(new GridLayout(0, 1));
      boxPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
          ser.getSeriesName()));
      JCheckBox checkbox = new JCheckBox("Toggle Plot");
      checkbox.setSelected(true);
      checkbox.addItemListener(listener);
      boxPanel.add(checkbox);
      boxPanel.add(new JLabel("Function: " + ser.getFunction()));
      boxPanel.add(new JLabel("r^2: " + ser.getR2()));
      checkPanel.add(boxPanel);
      box2series.put(checkbox, ser);
    }

    JPanel mainPanel = new JPanel(new BorderLayout());

    mainPanel.add(checkPanel, BorderLayout.LINE_START);
    mainPanel.add(chartPanel, BorderLayout.CENTER);
    frame.add(mainPanel);


    return frame;
  }
}
