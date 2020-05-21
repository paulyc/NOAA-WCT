package gov.noaa.ncdc.wct.ui;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Timer;
import java.util.TimerTask;

public class WCTResizeListener implements ComponentListener {

	WCTViewer viewer = null;
	TimerTask task = null;
	Timer timer = new Timer();
	int waitTime = 1000;   

	
	public WCTResizeListener(WCTViewer viewer) {
		this.viewer = viewer;
	}
	
	/**
	 *Component Listener implementation
	 */
	@Override
	public void componentResized(ComponentEvent evt) {
		
		// if events are rapidly created, wait 1 sec for any new events, then execute last event reported.
		
		if (task != null) {
			try {
				task.cancel();
			} catch (Exception e) {
			}
		}
		task = new TimerTask() {
			public void run() {
				viewer.refreshAfterResize();
			}
		};
		timer.purge();
		try {
			timer.schedule(task , waitTime);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			timer = new Timer();
			timer.schedule(task , waitTime);
		}
	}



	
	@Override
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}


	@Override
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

}
