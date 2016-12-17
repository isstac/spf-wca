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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;

import javax.swing.*;

import wcanalysis.fitting.FunctionFitter;

/**
 * @author Kasper Luckow
 */
public class WorstCaseChart {
  public static JFrame createChartPanel(Collection<DataSeries> series) {

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
    for(DataSeries s : series) {
      chart.addSeries(s.getSeriesName(), s.getX(), s.getY());
    }
    return createFrame(chart, series);
  }

  public static JFrame createChartPanel(Collection<DataSeries> series, double inputReq,
                                         double resReq) {
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
    for(DataSeries s : series) {
      chart.addSeries(s.getSeriesName(), s.getX(), s.getY());
    }

    //We can take any series to get max X---supposedly
    DataSeries fst = series.iterator().next();
    double[] xs = fst.getX();
    double maxX = xs[xs.length-1];

    double maxY = -1;
    for(DataSeries o : series) {
      double[] ys = fst.getY();
      if(ys[ys.length - 1] > maxY) {
        maxY = ys[ys.length - 1];
      }
    }

    //add input req line:
    chart.addSeries("Input Requirement", new double[] {inputReq, inputReq}, new double[] {0, maxY});

    //add resource req line:
    chart.addSeries("Resource Requirement",
        new double[] {0, maxX}, new double[] {0, resReq});
    return createFrame(chart, series);
  }

  private static JFrame createFrame(XYChart chart, Collection<DataSeries> series) {

    JPanel panel = new XChartPanel<>(chart);

    final Map<JCheckBox, DataSeries> box2series = new HashMap<>();

    ItemListener listener = e -> {
      if(e.getItem() instanceof JCheckBox) {
        JCheckBox checkbox = (JCheckBox)e.getItem();
        if(!checkbox.isSelected()) {
          String series1 = box2series.get(checkbox).getSeriesName();
          chart.removeSeries(series1);
        } else {
          DataSeries series1 = box2series.get(checkbox);
          chart.addSeries(series1.getSeriesName(), series1.getX(), series1.getY());
        }
        panel.revalidate();
        panel.repaint();
      }
    };

    // Create and set up the window.
    JFrame frame = new JFrame("Worst Case Prediction Model");
    frame.setLayout(new BorderLayout());
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel checkPanel = new JPanel(new GridLayout(0, 1));
    for(DataSeries ser : series) {
      JCheckBox checkbox = new JCheckBox(ser.getSeriesName());
      checkbox.setSelected(true);
      checkbox.addItemListener(listener);
      checkPanel.add(checkbox);
      box2series.put(checkbox, ser);
    }

    frame.add(checkPanel, BorderLayout.LINE_START);
    frame.add(panel, BorderLayout.CENTER);

    return frame;
  }
}
