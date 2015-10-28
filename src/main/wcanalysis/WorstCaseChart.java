package wcanalysis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleEdge;

/**
 * @author Kasper Luckow
 */
public class WorstCaseChart extends ApplicationFrame implements ChartMouseListener {

  private static final long serialVersionUID = 7777887151534005094L;

  private Crosshair xCrosshair;
  private Crosshair yCrosshair;
  private ChartPanel chartPanel;
  
  public WorstCaseChart(XYSeriesCollection dataCollection) {
    super("Worst case");
    JFreeChart chart = createChart(dataCollection);
    createChartPanel(chart);
  }

  public WorstCaseChart(XYSeriesCollection dataCollection, double maxInputReq, double maxResReq) {
    super("Worst case");
    JFreeChart chart = createChart(dataCollection);
    XYPlot plot = chart.getXYPlot();
    ValueMarker vertMarker = new ValueMarker(maxInputReq); 
    vertMarker.setPaint(Color.red);
    plot.addDomainMarker(vertMarker); // vertical line

    ValueMarker horizMarker = new ValueMarker(maxResReq);
    horizMarker.setPaint(Color.red);
    plot.addRangeMarker(horizMarker); // horizontal line

    createChartPanel(chart);
  }

  private void createChartPanel(JFreeChart chart) {
    chartPanel = new ChartPanel(chart);
    
    chartPanel.addChartMouseListener(this);
    
    CrosshairOverlay crosshairOverlay = new CrosshairOverlay();
    xCrosshair = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
    xCrosshair.setLabelVisible(true);
    yCrosshair = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
    yCrosshair.setLabelVisible(true);
    crosshairOverlay.addDomainCrosshair(xCrosshair);
    crosshairOverlay.addRangeCrosshair(yCrosshair);
    chartPanel.addOverlay(crosshairOverlay);
    
    chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
    setContentPane(chartPanel);
  }

  private JFreeChart createChart(XYDataset dataset) {
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
    XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(Color.lightGray);
    //plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
    plot.setDomainGridlinePaint(Color.white);
    plot.setRangeGridlinePaint(Color.white);

    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    //renderer.setSeriesLinesVisible(0, false);
    //renderer.setSeriesShapesVisible(1, false);
    plot.setRenderer(renderer);
    
    //Add "crosshair"
    
    /*plot.setDomainCrosshairVisible(true);
    
    plot.setDomainCrosshairLockedOnData(false);
    plot.setRangeCrosshairVisible(true);
    plot.setRangeCrosshairLockedOnData(false);*/
    
    // change the auto tick unit selection to integer units only...
    final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    return chart;
  }


  @Override
  public void chartMouseClicked(ChartMouseEvent arg0) {
    //ignore
  }
  
  @Override
  public void chartMouseMoved(ChartMouseEvent event) {
    Rectangle2D dataArea = this.chartPanel.getScreenDataArea();
    JFreeChart chart = event.getChart();
    XYPlot plot = (XYPlot) chart.getPlot();
    ValueAxis xAxis = plot.getDomainAxis();
    double x = xAxis.java2DToValue(event.getTrigger().getX(), dataArea, 
            RectangleEdge.BOTTOM);
    ValueAxis yAxis = plot.getRangeAxis();
    double y = yAxis.java2DToValue(event.getTrigger().getY(), dataArea, 
        RectangleEdge.LEFT);
    
    //Alternatively, obtain y for one of the subplots:
    //double y = DatasetUtilities.findYValue(plot.getDataset(), 0, x);
    this.xCrosshair.setValue(x);
    this.yCrosshair.setValue(y);
  }
}
