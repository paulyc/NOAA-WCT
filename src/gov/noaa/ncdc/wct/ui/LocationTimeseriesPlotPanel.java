package gov.noaa.ncdc.wct.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;

import ucar.ma2.StructureData;
import ucar.ma2.StructureMembers;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.PointFeatureIterator;
import ucar.nc2.ft.StationTimeSeriesFeature;
import ucar.nc2.ft.StationTimeSeriesFeatureCollection;
import ucar.unidata.geoloc.Station;

public class LocationTimeseriesPlotPanel extends JPanel {

	private PlotRequestInfo plotRequestInfo;
	private ChartPanel chartPanel = null;
	private TimeSeriesCollection[] dataset;
	

	private long minMillis;
	private long maxMillis;
	
	private final static Color[] PLOT_COLORS = new Color[] {
			Color.BLUE, Color.GREEN.darker(), Color.DARK_GRAY, Color.CYAN, Color.ORANGE, Color.MAGENTA, Color.GRAY,
			Color.BLUE, Color.GREEN.darker(), Color.DARK_GRAY, Color.CYAN, Color.ORANGE, Color.MAGENTA, Color.GRAY,
			Color.BLUE, Color.GREEN.darker(), Color.DARK_GRAY, Color.CYAN, Color.ORANGE, Color.MAGENTA, Color.GRAY,
			Color.BLUE, Color.GREEN.darker(), Color.DARK_GRAY, Color.CYAN, Color.ORANGE, Color.MAGENTA, Color.GRAY,
		};


	public void setPlot(StationTimeSeriesFeatureCollection stsfc, 
			Station station, PlotRequestInfo plotRequestInfo) 
					throws PlotException, IOException {


		this.plotRequestInfo = plotRequestInfo;
		ArrayList<String> uniqueUnitsList = new ArrayList<String>();
		for (String u : plotRequestInfo.getUnits()) {
			if (u != null && ! uniqueUnitsList.contains(u)) {
				uniqueUnitsList.add(u);
			}
		}

		String[] variables = plotRequestInfo.getVariables();
		String[] units = plotRequestInfo.getUnits();
		dataset = new TimeSeriesCollection[variables.length];
		for (int n=0; n<dataset.length; n++) {
			dataset[n] = new TimeSeriesCollection(TimeZone.getTimeZone("GMT"));
		}
		
		{
			TimeSeries varSeries = new TimeSeries(variables[0], units[0], "Time");
			varSeries.setDescription(variables[0]);
			String[] plotInfo = addToTimeSeries(stsfc,
					station, 
					variables[0], varSeries);
			dataset[0].addSeries(varSeries);
			
			minMillis = varSeries.getTimePeriod(0).getFirstMillisecond();
			maxMillis = varSeries.getTimePeriod(varSeries.getItemCount()-1).getLastMillisecond();
		}

		

		String graphTitle = station.getName() + ": "+station.getDescription();
		//    String graphTitle = plotInfo[0];
		String domainTitle = "Date/Time (GMT)";


		final JFreeChart chart = ChartFactory.createTimeSeriesChart(graphTitle,// chart title
				domainTitle, // x axis label
				uniqueUnitsList.get(0), // y axis label
				dataset[0], // data
				// PlotOrientation.VERTICAL,
				true, // include legend
				true, // tooltips
				false // urls
				);

		// Set the time zone for the date axis labels
		((DateAxis)chart.getXYPlot().getDomainAxis(0)).setTimeZone(TimeZone.getTimeZone("GMT"));
		
		

//		chart.getXYPlot().setDomainCrosshairLockedOnData(true);
//		chart.getXYPlot().setDomainCrosshairVisible(true);
//		chart.getXYPlot().setDomainCrosshairValue(value, notify);Visible(true);
		
		// chart.setBackgroundImage(javax.imageio.ImageIO.read(new
		// URL("http://mesohigh/img/noaaseagullbkg.jpg")));

		// NOW DO SOME OPTIONAL CUSTOMIZATION OF THE CHART...
		chart.setBackgroundPaint(new Color(250, 250, 250));
		chart.getXYPlot().setBackgroundPaint(Color.WHITE);
		chart.getXYPlot().setDomainGridlinePaint(new Color(220, 220, 220));
		chart.getXYPlot().setRangeGridlinePaint(new Color(220, 220, 220));
		//        LegendTitle legend = (LegendTitle) chart.getLegend();
		//        legend.setDisplaySeriesShapes(true);

		// get a reference to the plot for further customization...
		final XYPlot plot = chart.getXYPlot();
		
		Range rangeBounds = dataset[0].getRangeBounds(false);		
		System.out.println(0+" range ="+rangeBounds);
		if (rangeBounds != null) {
			double buffer = rangeBounds.getLength()*.25;
			Range bufferedRangeBounds = new Range(rangeBounds.getLowerBound()-buffer, rangeBounds.getUpperBound()+buffer);
			plot.getRangeAxis(0).setRange(bufferedRangeBounds);
			plot.getRangeAxis(0).setDefaultAutoRange(bufferedRangeBounds);
		}
		
		
		//			for (int n=1; n<uniqueUnitsList.size(); n++) {
		//
		//				for (int i=0; i<variables.length; i++) {
		//
		//					if (units[i].equals(uniqueUnitsList.get(n))) {
		//						TimeSeries varSeries = new TimeSeries(variables[i], FixedMillisecond.class);
		//
		//						String[] plotInfo = addToTimeSeries(
		//								station, 
		//								variables[n], varSeries);
		//
		//						dataset[n].addSeries(varSeries);
		//
		//						// AXIS 2
		//						NumberAxis axis2 = new NumberAxis(uniqueUnitsList.get(n));
		//						axis2.setFixedDimension(10.0);
		//						axis2.setAutoRangeIncludesZero(false);
		//						axis2.setLabelPaint(PLOT_COLORS[n-1]);
		//						axis2.setTickLabelPaint(PLOT_COLORS[n-1]);
		//						plot.setRangeAxis(n, axis2);
		//						plot.setRangeAxisLocation(n, AxisLocation.BOTTOM_OR_LEFT);
		//
		//						plot.setDataset(n, dataset[n]);
		//						plot.mapDatasetToRangeAxis(n, n);
		//
		//						XYItemRenderer renderer2 = new StandardXYItemRenderer();
		//						//        XYBarRenderer renderer2 = new XYBarRenderer();
		//						renderer2.setSeriesPaint(0, PLOT_COLORS[n-1]);
		//						plot.setRenderer(n, renderer2);
		//
		//
		//					}
		//				}



		for (int n=0; n<uniqueUnitsList.size(); n++) {

			if (n > 0) {
				NumberAxis axis2 = new NumberAxis(uniqueUnitsList.get(n));
//				axis2.setFixedDimension(10.0);
				axis2.setAutoRangeIncludesZero(false);
				axis2.setLabelPaint(Color.BLACK);
				axis2.setTickLabelPaint(Color.BLACK);
				plot.setRangeAxis(n, axis2);
				plot.setRangeAxisLocation(n, AxisLocation.BOTTOM_OR_LEFT);
				axis2.setLabelInsets(new RectangleInsets(0, 10, 0, 0));
			}

			for (int i=1; i<variables.length; i++) {
				
				String shortName = variables[i].split("\\s+")[0];
				if (units[i].equals(uniqueUnitsList.get(n))) {
					TimeSeries varSeries = new TimeSeries(shortName, units[i], "Time");
					varSeries.setDescription(shortName);
					
					String[] plotInfo = addToTimeSeries(stsfc,
							station, 
							shortName, varSeries);

					dataset[i].addSeries(varSeries);
					
					

				}
				plot.setDataset(i, dataset[i]);
				plot.mapDatasetToRangeAxis(i, n);
				rangeBounds = dataset[i].getRangeBounds(false);
				
				System.out.println(n+" range ="+rangeBounds);
				if (rangeBounds != null) {
					double buffer = rangeBounds.getLength()*.25;
					Range bufferedRangeBounds = new Range(rangeBounds.getLowerBound()-buffer, rangeBounds.getUpperBound()+buffer);
					plot.getRangeAxis(n).setRange(bufferedRangeBounds);
					plot.getRangeAxis(n).setDefaultAutoRange(bufferedRangeBounds);
				}

				XYItemRenderer renderer2 = new StandardXYItemRenderer();
//				        XYBarRenderer renderer2 = new XYBarRenderer();
				renderer2.setSeriesPaint(2, PLOT_COLORS[n]);
				plot.setRenderer(i, renderer2);

			}

		}

//		for (int n=0; n<dataset.length; n++) {
//			Range rangeBounds = dataset[n].getRangeBounds(false);
//			System.out.println(n+" range ="+rangeBounds);
//			if (rangeBounds != null) {
//				double buffer = rangeBounds.getLength()*.25;
//				Range bufferedRangeBounds = new Range(rangeBounds.getLowerBound()-buffer, rangeBounds.getUpperBound()+buffer);
//				plot.getRangeAxis(n).setRange(bufferedRangeBounds);
//				plot.getRangeAxis(n).setDefaultAutoRange(bufferedRangeBounds);
//			}
//		}

		if (chartPanel == null) {
			chartPanel = new ChartPanel(chart);
		}
		else {
			chartPanel.setChart(chart);
		}


		// clear out any previous listeners
//		for (MouseWheelListener l : chartPanel.getMouseWheelListeners()) {
//			chartPanel.removeMouseWheelListener(l);
//		}	
//		for (MouseMotionListener l : chartPanel.getMouseMotionListeners()) {
//			chartPanel.removeMouseMotionListener(l);
//		}
//		for (MouseListener l : chartPanel.getMouseListeners()) {
//			chartPanel.removeMouseListener(l);
//		}
		
		
		
		final CrosshairOverlay crosshairOverlay = new CrosshairOverlay();
		chartPanel.addOverlay(crosshairOverlay);
		chartPanel.setMouseWheelEnabled(true);
		TimeseriesPlotAnnotationListener annotationListener = new TimeseriesPlotAnnotationListener(chartPanel, plot, crosshairOverlay, dataset);
		chartPanel.addChartMouseListener(annotationListener);
		
		
		plot.setDomainPannable(true);
		chartPanel.setRangeZoomable(false);
		chartPanel.setMouseZoomable(false);
		
		TimeseriesPlotZoomAndPanListener zoomPanListener = new TimeseriesPlotZoomAndPanListener(minMillis, maxMillis, chartPanel, plot);
		chartPanel.addMouseListener(zoomPanListener);
		chartPanel.addMouseMotionListener(zoomPanListener);
		chartPanel.addMouseWheelListener(zoomPanListener);
		
//		System.out.println(chartPanel.getMouseMotionListeners().length + " chart motion listeners");
		
		this.removeAll();
		this.setLayout(new BorderLayout());
		this.add(new JLabel("[ scroll to zoom, double-click to map selected time ]"), BorderLayout.NORTH);
		this.add(chartPanel, BorderLayout.CENTER);
	}
	

	
	/**
	 * 
	 * @param station
	 * @param variable
	 * @param varSeries
	 * @return [0]=station name and description if available
	 *   [1]=units
	 * @throws IOException
	 */
	public String[] addToTimeSeries(StationTimeSeriesFeatureCollection stsfc, 
			Station station, String variable, TimeSeries varSeries) throws IOException, PlotException {
		// Station station = stsfc.getStation(stationName);
		String[] plotInfo = new String[3];
		plotInfo[0] = station.getName();
		if (station.getDescription() != null &&
				station.getDescription().trim().length() > 0) {
			plotInfo[0] += " ("+station.getDescription()+")";
		}
		StationTimeSeriesFeature sf = stsfc.getStationFeature(station);

		System.out.println("Station: "+station.toString());
		System.out.println("Location: "+sf.getLatLon());
//		textArea.append("## Station: "+station.toString()+" -- "+sf.getLatLon() +" \n");

		boolean firstTime = true;
		PointFeatureIterator pfIter = sf.getPointFeatureIterator(1*1024*1024);
		// iterate through data for each station
		while (pfIter.hasNext()) {
			PointFeature pf = pfIter.next();

			// System.out.println( pf.getObservationTimeAsDate() + " -- " + pf.getLocation().toString());
			StructureData sdata = pf.getDataAll();
			StructureMembers smembers = sdata.getStructureMembers();
			// System.out.println( smembers.getMemberNames().toString() );
			if (firstTime) {
				plotInfo[1] = smembers.getMember(smembers.getMemberNames().indexOf(variable)).getUnitsString();
				firstTime = false;
			}

			try {

				Date date = pf.getObservationTimeAsCalendarDate().toDate();
				double val = Double.parseDouble(sdata.getScalarObject(variable).toString());
				if (val < -100) {
					val = Double.NaN;
				}
				varSeries.addOrUpdate(new FixedMillisecond(date.getTime()), val);

			} catch (NumberFormatException nfe) {
				throw new PlotException("Plot Error: Only numeric fields are supported.");
			}

		}
		return plotInfo;
	}
	public PlotRequestInfo getPlotRequestInfo() {
		return this.plotRequestInfo;
	}
	
	public ChartPanel getChartPanel() {
		return this.chartPanel;
	}
	
	public TimeSeriesCollection[] getTimeSeriesDatasets() {
		return this.dataset;
	}

}
