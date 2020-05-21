package gov.noaa.ncdc.wct.ui;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.time.TimeSeriesCollection;

public class TimeseriesPlotAnnotationListener implements ChartMouseListener {

	private ChartPanel chartPanel;
	private XYPlot plot;
	private CrosshairOverlay crosshairOverlay;
	private TimeSeriesCollection[] dataset;
	
	public TimeseriesPlotAnnotationListener(ChartPanel chartPanel, XYPlot plot, CrosshairOverlay crosshairOverlay, TimeSeriesCollection[] dataset) {
		this.chartPanel = chartPanel;
		this.plot = plot;
		this.crosshairOverlay = crosshairOverlay;
		this.dataset = dataset;
	}
	
	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
//        ChartEntity entity = event.getEntity();
//        if (!(entity instanceof CategoryItemEntity)) {
//            plot.getRenderer().setHighlightedItem(-1, -1);
//            return;
//        }
//        CategoryItemEntity cie = (CategoryItemEntity) entity;
//        CategoryDataset dataset = cie.getDataset();
//        this.renderer.setHighlightedItem(dataset.getRowIndex(cie.getRowKey()),
//                dataset.getColumnIndex(cie.getColumnKey()));	
		

//		   ChartEntity entity = event.getEntity();
//		   if (entity != null && entity.getClass().toString().equals("class org.jfree.chart.entity.XYItemEntity")) {
//		   
//			   // Get entity details
//			   String tooltip = ((XYItemEntity)entity).getToolTipText();
//			   XYDataset dataset = ((XYItemEntity)entity).getDataset();
//			   int seriesIndex = ((XYItemEntity)entity).getSeriesIndex();
//			   int item = ((XYItemEntity)entity).getItem();
//
//			   // You have the dataset the data point belongs to, the index of the series in that dataset of the data point, and the specific item index in the series of the data point.
//			   TimeSeries series = ((TimeSeriesCollection)dataset).getSeries(seriesIndex);
//			   TimeSeriesDataItem xyItem = series.getDataItem(item);
////			   System.out.println(xyItem.getPeriod() + " , " + xyItem.getValue());
//			   
////			   plot.addDomainMarker(new ValueMarker(xyItem.getPeriod().getFirstMillisecond()));
//
//		   }
//		   else if (entity != null && entity.getClass().toString().equals("class org.jfree.chart.entity.PlotEntity")) {
//			   // Get entity details
////			   String tooltip = ((PlotEntity)entity).getToolTipText();
////			   String coords = ((PlotEntity)entity).getShapeCoords();
////			   XYDataset dataset = ((PlotEntity)entity).getPlot().getDataset();
////			   int seriesIndex = ((PlotEntity)entity).getSeriesIndex();
////			   int item = ((PlotEntity)entity).getItem();
////			   System.out.println("plot entity found: "+tooltip+" , "+coords);
////
////			   // You have the dataset the data point belongs to, the index of the series in that dataset of the data point, and the specific item index in the series of the data point.
////			   TimeSeries series = ((TimeSeriesCollection)dataset).getSeries(seriesIndex);
////			   TimeSeriesDataItem xyItem = series.getDataItem(item);
////			   System.out.println(xyItem.getPeriod() + " , " + xyItem.getValue());
//			   return;
//		   }
//		   else {
////			   System.out.println("entity is : "+entity.getClass().toString());
//			   return;
//		   }
		   
//		   String tooltip = ((JFreeChartEntity)entity).getToolTipText();
//		   XYDataset dataset = ((JFreeChartEntity)entity).getChart().getXYPlot().getDataset();
//		   int seriesIndex = ((JFreeChartEntity)entity).getChart().getXYPlot().indexOf(dataset);
//		   ((JFreeChartEntity)entity).getChart().getXYPlot().addDomainMarker(new ValueMarker(10));
////		   int item = 
//
//		   // You have the dataset the data point belongs to, the index of the series in that dataset of the data point, and the specific item index in the series of the data point.
//		   XYSeries series = ((XYSeriesCollection)dataset).getSeries(seriesIndex);
////		   XYDataItem xyItem = series.getDataItem(item);
////		   System.out.println(tooltip + " : " + xyItem.getXValue() + " , " + xyItem.getYValue());
//		   System.out.println(tooltip);
		   
		   Point2D p = chartPanel.translateScreenToJava2D(event.getTrigger().getPoint());
		   Rectangle2D plotArea = chartPanel.getScreenDataArea();
		   double chartX = plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
		   double chartY = plot.getRangeAxis().java2DToValue(p.getY(), plotArea, plot.getRangeAxisEdge());
		   
//		   System.out.println(new Date(Math.round(chartX)) + " , "+chartY);
//		   plot.clearDomainMarkers();
//		   plot.addDomainMarker(new ValueMarker(chartX));
		   setCrosshairLocation(crosshairOverlay, chartX, chartY);
		   List<String> overlayInfo = LocationTimeseriesUtils.getTimeSeriesInfoString(dataset, Math.round(chartX));
		   
		   

		   plot.clearAnnotations();
//		   plot.clearRangeMarkers();
		   
		   Range range = plot.getRangeAxis().getRange();
		   // find height of pixels for this annotation
		   XYTextAnnotation ann = new XYTextAnnotation("test", 20, 20);
		   double annotationPixelHeight = WCTUiUtils.getTextBounds(ann.getFont(), "0123456789").getHeight();
		   // now determine what percentage of chart area each line takes up
		   double pcntPerLine = annotationPixelHeight/plotArea.getHeight();
		   // use line spacing of 40% of a line
		   double pcntPerLineSpacing = pcntPerLine*.40;
		   // start annotation plotting at 5% above bottom
		   double paddingFromBottom = range.getLowerBound()+range.getLength()*0.05;
		   for (int n=0; n<overlayInfo.size(); n++) {
//			   System.out.println(overlayInfo.get(n));
			   plot.addAnnotation(new XYTextAnnotation(overlayInfo.get(n), chartX, 
					   paddingFromBottom + n*range.getLength()*pcntPerLine + n*range.getLength()*pcntPerLineSpacing) );
		   }
		   

		   // Interesting Idea, but not the right choice for now
//		   int closestIndex = LocationTimeseriesUtils.getClosestTimeSeriesIndex(dataset, Math.round(chartX));
//		   for (int i=0; i<dataset.length; i++) {
//			   for (int j=0; j<dataset[i].getSeriesCount(); j++) {
//
//				   TimeSeries timeSeries = ((TimeSeries)dataset[i].getSeries().get(j));
//				   Marker marker = new ValueMarker(timeSeries.getDataItem(closestIndex).getValue().doubleValue());
//				   marker.setPaint(Color.BLACK);
//				   marker.setOutlinePaint(Color.GRAY);
//				   plot.addRangeMarker(marker);
//
//			   }
//		   }
	}
	
	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
	}

	
	protected void setCrosshairLocation(CrosshairOverlay crosshairOverlay, double x, double y) {
	    Crosshair domainCrosshair;
	    List domainCrosshairs = crosshairOverlay.getDomainCrosshairs();
	    if (domainCrosshairs.isEmpty()) {
	        domainCrosshair = new Crosshair();
	        domainCrosshair.setPaint(new Color(210, 210, 210, 150));
//	        domainCrosshair.setStroke(stroke);
//	        domainCrosshair.setLabelGenerator(new StandardCrosshairLabelGenerator());
//	        domainCrosshair.setLabelVisible(true);
//	        domainCrosshair.setLabelPaint(Color.BLACK);
	        crosshairOverlay.addDomainCrosshair(domainCrosshair);
	    }
	    else {
	        // We only have one at a time
	        domainCrosshair = (Crosshair) domainCrosshairs.get(0);
	    }
	    domainCrosshair.setValue(x);

//	    if (y != null) {
//	        Crosshair rangeCrosshair;
//	        List rangeCrosshairs = crosshairOverlay.getRangeCrosshairs();
//	        if (rangeCrosshairs.isEmpty()) {
//	            rangeCrosshair = new Crosshair();
//	            rangeCrosshair.setPaint(new Color(0, 0, 0, 0));
//	            crosshairOverlay.addRangeCrosshair(rangeCrosshair);
//	            rangeCrosshair.setLabelGenerator(new StandardCrosshairLabelGenerator());
//	            rangeCrosshair.setLabelVisible(true);
//	            rangeCrosshair.setLabelPaint(Color.BLACK);
//	        }
//	        else {
//	            // We only have one at a time
//	            rangeCrosshair = (Crosshair) rangeCrosshairs.get(0);
//	        }
//
//	        rangeCrosshair.setValue(y);
//	    }
	}

}
