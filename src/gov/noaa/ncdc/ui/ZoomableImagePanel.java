package gov.noaa.ncdc.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.JScrollPane;


public class ZoomableImagePanel extends javax.swing.JPanel {

	private Dimension preferredSize = new Dimension(400, 400);    
	private Rectangle2D[] rects = new Rectangle2D[50];
	private double currentZoomFactor = 1;
	private double currentZoom = 0;
	private int offX = 0, offY = 0;
	private int lastDragX = -1, lastDragY = -1;
	private int panX = 0, panY = 0;

	Rectangle2D.Double backgroundRect = new Rectangle2D.Double();
	
	public static void main(String[] args) {        
		JFrame jf = new JFrame("test");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setSize(400, 400);
		jf.add(new JScrollPane(new ZoomableImagePanel()));
		jf.setVisible(true);
	}    

	public ZoomableImagePanel() {
		// generate rectangles with pseudo-random coords
		//    for (int i=0; i<rects.length; i++) {
		//        rects[i] = new Rectangle2D.Double(
		//                Math.random()*.8, Math.random()*.8, 
		//                Math.random()*.2, Math.random()*.2);
		//    }
		// mouse listener to detect scrollwheel events
		addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				updatePreferredSize(e.getWheelRotation(), e.getPoint());
			}
		});

		addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseDragged(MouseEvent e) {
				processPan(e.getX(), e.getY());
				repaint();
			}
		});


		addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {
				lastDragX = -1;
				lastDragY = -1;
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent e) {
//				processPan(e.getX(), e.getY());
				if (e.getButton() == MouseEvent.BUTTON1) {
					updatePreferredSize(-1, e.getPoint());
				}
				else {
					updatePreferredSize(1, e.getPoint());
				}
			}
		});

		setDoubleBuffered(true);
	}

	private void processPan(int x, int y) {
//		System.out.println("panning: "+x+" "+y);
		if (lastDragX >= 0 || lastDragY >= 0) {
			panX += (x-lastDragX);
			panY += (y-lastDragY);
		}
		lastDragX = x;
		lastDragY = y;
	}


	private void updatePreferredSize(double n, Point p) {

		System.out.println("updating: "+n+"  "+p);

		double d = (double) n * 1.08;
		d = (n > 0) ? 1 / d : -d;

		currentZoom += n;

		// limit to not zoom out farther than initial zoom
		if (currentZoom > 0) {
			currentZoom = 0;
			panX=0;
			panY=0;
		}

		if (currentZoom == 0) {
			currentZoomFactor = 1;
		}
		else if (currentZoom > 0) {
			currentZoomFactor = 1 / ((double) currentZoom * 1.02);
//			processPan((int)p.getX(), (int)p.getY());
		}
		else {
			//    	currentZoomFactor = -(double) currentZoom * 1.02;
			currentZoomFactor = 1+ (-(double) currentZoom * 1.02 / 6);
//			processPan((int)p.getX(), (int)p.getY());
		}

		
		
		
		
//		    if (d != 0) {
//		    	currentZoomFactor = (currentZoom == 0) ? 1 : (double) currentZoom * 1.08;
//		    	currentZoomFactor = (currentZoom > 0) ? 1 / currentZoomFactor : -currentZoomFactor;
//		    }
//
//		    int w = (int) (getWidth() * d);
//		    int h = (int) (getHeight() * d);
//		    preferredSize.setSize(w, h);
//		
//		    offX = (int)(p.x * d) - p.x;
//		    offY = (int)(p.y * d) - p.y;
//		    setLocation(getLocation().x-offX,getLocation().y-offY);
//		
//		    getParent().doLayout();
		
		revalidate();
		repaint();
		repaint(0, 0, this.getWidth(), this.getHeight());
	}

	public Dimension getPreferredSize() {
		return preferredSize;
	}

	private Rectangle2D r = new Rectangle2D.Float();

	
	public void paint(Graphics g) {
//		    super.paint(g);
		    g.setColor(getBackground());
//		    g.setColor(Color.MAGENTA);
		    int w = getWidth();
		    int h = getHeight();
		    backgroundRect.setFrame(0, 0, w, h);
		    ((Graphics2D)g).fill(backgroundRect);
//		    System.out.println("fill rect: "+backgroundRect);
		    
//		    if (true) return;
		//    for (Rectangle2D rect : rects) {
		//        r.setRect(rect.getX() * w, rect.getY() * h, 
		//                rect.getWidth() * w, rect.getHeight() * h);
		//        
		//        ((Graphics2D)g).draw(r);
		//    }       
		//    


//		System.out.println("current zoom: "+currentZoom+" factor="+currentZoomFactor+" location: "+panX+","+panY);
		//    ((Graphics2D)g).getTransform().setToScale(currentZoomFactor, currentZoomFactor);

		//    AffineTransform at = AffineTransform.getTranslateInstance(offX, offY);
		//    AffineTransform at = AffineTransform.getTranslateInstance(0, 0);
		AffineTransform at = AffineTransform.getTranslateInstance(panX, panY);
		at.scale(currentZoomFactor, currentZoomFactor);
		((Graphics2D)g).transform(at);

		super.paint(g);
	}
}