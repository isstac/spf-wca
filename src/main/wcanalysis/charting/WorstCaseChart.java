package wcanalysis.charting;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
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
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RefineryUtilities;

import wcanalysis.fitting.FunctionFitter;

/**
 * @author Kasper Luckow
 */
public class WorstCaseChart extends ApplicationFrame implements ChartMouseListener {

  private static final long serialVersionUID = 7777887151534005094L;

  private Crosshair xCrosshair;
  private Crosshair yCrosshair;
  private ChartPanel chartPanel;
  
  public static void main(String[] args) throws IOException {
    if(args.length < 1 || args.length > 2) {
      System.err.print("Accepts 2 args: path to csv file and optionally \"output\" which will output the data set of the models");
      System.exit(-1);
    }
    
    boolean output = false;
    if(args[args.length - 1].equalsIgnoreCase("output")) {
      output = true;
    }

    if(args[0].equalsIgnoreCase("batch")) {
      String csvFile = args[1];
      processBatchResultsCSV(csvFile, output);
    } else {
      String csvFile = args[0];
      processStandardResultsCSV(csvFile, output);
    }
  }

  private static void processBatchResultsCSV(String csvFile, boolean output) throws IOException {
    Reader in = new FileReader(csvFile);
    Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
    boolean first = true;
    for(CSVRecord rec : records) {
      if(rec.toString().startsWith("#") && first) { //probably the most ridiculous csv library when
        // you have to do
        // this?
        first = false;
        continue;
      }

      DataCollection dataCollection = new DataCollection();
      double inputSize = 1;
      for(String element : rec) {
        double y = Double.parseDouble(element);
        dataCollection.addDatapoint(inputSize++, y);
      }
      visualizeDataCollection(dataCollection, output, new File(csvFile).getParentFile());
    }

  }

  private static void visualizeDataCollection(DataCollection dataCollection, boolean output,
                                              File baseDir) throws IOException {
    XYSeriesCollection series = FunctionFitter.computeSeries(dataCollection, 10);
    if(output) {
      for(XYSeries ser : (List<XYSeries>)series.getSeries()) {
        String fileName = ser.getDescription() + ".csv";
        File f = new File(baseDir, fileName);
        Writer w = new FileWriter(f);
        FunctionFitter.seriesToCSV(ser, w);
      }
    }
    WorstCaseChart wcChart = new WorstCaseChart(series);

    wcChart.pack();
    RefineryUtilities.centerFrameOnScreen(wcChart);
    wcChart.setVisible(true);
  }

  private static void processStandardResultsCSV(String csvFile, boolean output) throws IOException {

    Reader in = new FileReader(csvFile);
    DataCollection dataCollection = new DataCollection();
    Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
    boolean first = true;
    for(CSVRecord rec : records) {
      if(first) { //probably the most ridiculous csv library when you have to do this?
        first = false;
        continue;
      }
      int x = Integer.parseInt(rec.get(0));
      int y = Integer.parseInt(rec.get(1));
      dataCollection.addDatapoint(x, y);
    }

    visualizeDataCollection(dataCollection, output, new File(csvFile).getParentFile());
  }
  
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
        true,                     
        true,                     
        false
        );

    chart.setBackgroundPaint(Color.white);

    XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(Color.lightGray);
    plot.setDomainGridlinePaint(Color.white);
    plot.setRangeGridlinePaint(Color.white);

    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    plot.setRenderer(renderer);

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

    //Alternatively, obtain y for one of the subplots, which would be very neat.
    //We should find the "nearest" subplot to the cursor -- this is easy
    //double y = DatasetUtilities.findYValue(plot.getDataset(), 0, x);
    this.xCrosshair.setValue(x);
    this.yCrosshair.setValue(y);
  }
}
