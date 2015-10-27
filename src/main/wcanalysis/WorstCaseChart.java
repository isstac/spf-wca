package wcanalysis;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

/**
 * @author Kasper Luckow
 */
public class WorstCaseChart extends ApplicationFrame {

  private static final long serialVersionUID = 7777887151534005094L;

  public WorstCaseChart(XYSeriesCollection dataCollection) {
    super("Worst case");
    final JFreeChart chart = createChart(dataCollection);
    final ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
    setContentPane(chartPanel);
  }
 
  private JFreeChart createChart(final XYDataset dataset) {
    //Create the chart
    final JFreeChart chart = ChartFactory.createXYLineChart(
        "Worst Case Prediction Model",
        "Input Size",
        "Depth",
        dataset,
        PlotOrientation.VERTICAL,
        true,                     // include legend
        true,                     // tooltips
        false                     // urls
        );

    chart.setBackgroundPaint(Color.white);

    //    final StandardLegend legend = (StandardLegend) chart.getLegend();
    //      legend.setDisplaySeriesShapes(true);

    // get a reference to the plot for further customisation...
    final XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(Color.lightGray);
    //plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
    plot.setDomainGridlinePaint(Color.white);
    plot.setRangeGridlinePaint(Color.white);

    final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    //renderer.setSeriesLinesVisible(0, false);
    //renderer.setSeriesShapesVisible(1, false);
    plot.setRenderer(renderer);

    // change the auto tick unit selection to integer units only...
    final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    return chart;
  }
}
