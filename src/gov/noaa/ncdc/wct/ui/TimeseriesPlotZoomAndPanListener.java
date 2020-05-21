package gov.noaa.ncdc.wct.ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;

public class TimeseriesPlotZoomAndPanListener implements MouseWheelListener, MouseMotionListener, MouseListener {

	int lastX = -9999;
	long minMillis;
	long maxMillis;
	ChartPanel chartPanel;
	XYPlot plot;
	
	public TimeseriesPlotZoomAndPanListener(long minMillis, long maxMillis, ChartPanel chartPanel, XYPlot plot) {
		this.minMillis = minMillis;
		this.maxMillis = maxMillis;
		this.chartPanel = chartPanel;
		this.plot = plot;
	}
	
	
	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// reset initial pan location
		lastX = -9999;
	}

	@Override
	public void mouseDragged(MouseEvent evt) {
		
		if (lastX == -9999) {
			// first time
			lastX = evt.getX();
		}
		
		double amt = (lastX-evt.getX())/(double)chartPanel.getWidth();
		plot.panDomainAxes(amt, new PlotRenderingInfo(new ChartRenderingInfo()), evt.getPoint());	
		
//		System.out.println(evt.getX()+","+evt.getY()+ "   panning: "+amt+" evt.getPoint: "+evt.getPoint());
		lastX = evt.getX();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent evt) {
		
//		System.out.println(evt.getScrollAmount()+" "+evt.getWheelRotation()+" "+evt.getPreciseWheelRotation()+" "+evt.getScrollType());
//		System.out.println(plot.getDomainAxis().getRange().toString() + "  |||  " + 
//				  new Date((long)plot.getDomainAxis().getRange().getLowerBound())+","+
//				  new Date((long)plot.getDomainAxis().getRange().getUpperBound()));
		
		// limit amount of zoom out
		
		if ((long)plot.getDomainAxis().getRange().getLowerBound() < (minMillis - (maxMillis-minMillis)/2) ) {
			if (evt.getPreciseWheelRotation() > 0) {
//				System.out.println("  not zooming - returning");
				return;
			}
		}
		if ((long)plot.getDomainAxis().getRange().getUpperBound() > (maxMillis + (maxMillis-minMillis)/2) ) {					
			if (evt.getPreciseWheelRotation() > 0) {
//				System.out.println("  not zooming - returning");
				return;
			}
		}		
		

		
		double amt = evt.getPreciseWheelRotation();
		if (amt < .1) { amt = -.1; }
		if (amt > .1) { amt = .1; }
		
		// limit amount of zoom in
		if (( (long)plot.getDomainAxis().getRange().getUpperBound() - (long)plot.getDomainAxis().getRange().getLowerBound()) < 
				(maxMillis - minMillis)/100) {
			amt *= 0.1;
		}
		
		plot.zoomDomainAxes(1+amt, new PlotRenderingInfo(new ChartRenderingInfo()), evt.getPoint());				
	}


}
