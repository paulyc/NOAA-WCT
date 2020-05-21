package gov.noaa.ncdc.wct.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.table.TableModel;

import gov.noaa.ncdc.common.SwingWorker;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.io.ScanResults;

public class WCTUiUtils {

	public static void browse(Frame parent, String urlString, String messageIfError) {
		try {
			Desktop.getDesktop().browse(new URI(urlString));
		} catch (Exception ex) {
			showErrorMessage(parent, messageIfError, ex);
		}
	}
	
	public static void browse(Dialog parent, String urlString, String messageIfError) {
		try {
			Desktop.getDesktop().browse(new URI(urlString));
		} catch (Exception ex) {
			showErrorMessage(parent, messageIfError, ex);
		}
	}


	
    public static void showErrorMessage(Frame parent, String specificMessage, Exception ex) {
    	ex.printStackTrace();
    	String message = "An error has occurred:\n (specific: "+specificMessage+")\n"+ex.getMessage()+"\n\n"
    			+ "If this persists, please contact NOAA/NCEI support at ncdc.orders@noaa.gov";
//    	message = WCTUtils.splitIntoMultipleLines(message, 90);
    	JOptionPane.showMessageDialog(parent, message, "Error Message", JOptionPane.ERROR_MESSAGE);
    }

    public static void showErrorMessage(Dialog parent, String specificMessage, Exception ex) {
    	ex.printStackTrace();
    	String message = "An error has occurred:\n (specific: "+specificMessage+")\n"+ex.getMessage()+"\n\n"
    			+ "If this persists, please contact NOAA/NCEI support at ncdc.orders@noaa.gov";
//    	message = WCTUtils.splitIntoMultipleLines(message, 90);
    	JOptionPane.showMessageDialog(parent, message, "Error Message", JOptionPane.ERROR_MESSAGE);
    }

	
    public static String getVersion() {
        Properties props = new Properties();
        try {
//            URL url = ResourceUtils.getInstance().getJarResource(new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.RESOURCE_CACHE_DIR, "/config/version.properties", null);
//            URL url = WCTUiUtils.class.getResource("/version.properties");
//            InputStream in = url.openStream();
//            props.load(in);
//            in.close();
//            return props.getProperty("version");
            return "4.5.0";
        } catch (Exception e) {
            e.printStackTrace();
            return "?";
        }
    }

    public static String checkCurrentStableVersion() {
        return checkVersion("https://www.ncdc.noaa.gov/wct/app/version-stable.dat");
    }
    public static String checkCurrentBETAVersion() {
        return checkVersion("https://www.ncdc.noaa.gov/wct/app/version-beta.dat");
    }
    private static String checkVersion(String versionUrlString) {
        BufferedReader in = null;
        try {
            URLConnection urlConn = new URL(versionUrlString).openConnection();
            urlConn.setConnectTimeout(1000);
            urlConn.setReadTimeout(1000);
            in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            String currentVersion;
            if ((currentVersion = in.readLine()) == null) {
                return "?";
            }
            return currentVersion.trim();
        } catch (Exception e) {
            return "?";
        }
    }
    
    
    public static void flashStatusMessage(final WCTViewer viewer, final String message, final long durationInMillis) {
		SwingWorker worker = new SwingWorker() {
			public Object construct() {
	    		viewer.getStatusBar().setProgressText(message);
	    		try {
	    			Thread.sleep(durationInMillis);
	    		} catch (InterruptedException ex) {
	    			ex.printStackTrace();
	    		}
	    		viewer.getStatusBar().setProgressText("");
	    		return "Done";
			}
		};
		worker.start();
    }

	
	
    /**
     * Gets text size in pixels using default font
     * @param text
     */
    public static Rectangle2D getTextBounds(String text) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D)image.getGraphics();
        FontRenderContext fc = g.getFontRenderContext();
        return g.getFont().createGlyphVector(fc, text).getVisualBounds();
    }
    
    /**
     * Gets text size in pixels using given font
     * @param font
     * @param text
     */
    public static Rectangle2D getTextBounds(Font font, String text) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D)image.getGraphics();
        FontRenderContext fc = g.getFontRenderContext();
        return font.createGlyphVector(fc, text).getVisualBounds();
    }
    
    
    /**
     * Creates a titled border
     * @param title
     * @param borderSize
     * @return
     */
    // We can do this because the same border object can be reused.
    public static Border myTitledBorder(String title, int borderSize) {
        return myTitledBorder(title, borderSize, borderSize, borderSize, borderSize);
    }//end myTitledBorder
    
    /**
     * Creates a titled border
     * @param title
     * @param borderSize
     * @return
     */
    // We can do this because the same border object can be reused.
    public static Border myTitledBorder(String title, int top, int left, int bottom, int right) {
        Border empty10Border = BorderFactory.createEmptyBorder(top, left, bottom, right);
        Border etchedBorder  = BorderFactory.createEtchedBorder();

        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(etchedBorder, title), 
                empty10Border);

    }//end myTitledBorder
    
    // We can do this because the same border object can be reused.
    public static Border myBorder(int top, int left, int bottom, int right) {
        Border empty10Border = BorderFactory.createEmptyBorder(top, left, bottom, right);
        Border etchedBorder  = BorderFactory.createEtchedBorder();

        return BorderFactory.createCompoundBorder(etchedBorder, empty10Border);

    }//end myTitledBorder
    
    
    /**
     * titleJustification an integer specifying the justification of the title -- one of the following: 
     * <ul> 
     * <li> TitledBorder.LEFT 
     * <li> TitledBorder.CENTER 
     * <li> TitledBorder.RIGHT 
     * <li> TitledBorder.LEADING 
     * <li> TitledBorder.TRAILING 
     * <li> TitledBorder.DEFAULT_JUSTIFICATION (leading)
     * </ul> 
	 * titlePosition an integer specifying the vertical position of the text in relation to the border -- one of the following:
	 * <ul> 
     * <li> TitledBorder.ABOVE_TOP 
     * <li> TitledBorder.TOP (sitting on the top line) 
     * <li> TitledBorder.BELOW_TOP 
     * <li> TitledBorder.ABOVE_BOTTOM 
     * <li> TitledBorder.BOTTOM (sitting on the bottom line) 
     * <li> TitledBorder.BELOW_BOTTOM 
     * <li> TitledBorder.DEFAULT_POSITION (top) 
	 * </ul>
     * @param title
     * @param top
     * @param left
     * @param bottom
     * @param right
     * @param titleJustification
     * @param titlePosition
     * @return
     */
    public static Border myTitledTopBorder(String title, int top, int left, int bottom, int right, int titleJustification, int titlePosition) {
        Border empty10Border = BorderFactory.createEmptyBorder(top, left, bottom, right);
        Border topBorder  = BorderFactory.createMatteBorder(0, 0, 0, 0, Color.BLACK);

        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(topBorder, title, titleJustification, titlePosition), 
                empty10Border);

    }//end myTitledBorder
    
    
    
    
    /**
     * Fills list model with 'getDisplayName' from each ScanResult.  This presets the size of the list model for faster loading.
     * @param listModel
     * @param scanResults
     * @return
     */
    public static DefaultListModel<String> fillListModel(DefaultListModel<String> listModel, ScanResults[] scanResults) {
        
        System.out.println("clearing list model...");        
//        listModel.clear();
//        listModel.setSize(0);
        listModel = new DefaultListModel<String>();
        System.out.println("setting list model size... ["+scanResults.length+"]");        
        listModel.setSize(scanResults.length);
        System.out.println("setting list model data...");        

        // Add to list
        for (int n=0; n<scanResults.length; n++) {
            
//            System.out.println("scanResult: "+n+" of "+scanResults.length+" ::: "+scanResults[n]);
            
            if (scanResults[n] != null) {
            	if (scanResults[n].getLongName().equals("DIRECTORY")) {
            		listModel.set(n, scanResults[n].getFileName());  
            	}
            	else if (scanResults[n].getLongName() != null && ! scanResults[n].getDisplayName().trim().equals("null")) {
                    listModel.set(n, scanResults[n].getLongName());                    
                }
                else {
                    listModel.set(n, scanResults[n].getFileName());
                }
            }
            else {
                System.out.println("null scanResult: "+n+" of "+scanResults.length);
            }
        }
        
        return listModel;
    }

    public static int getComponentIndex(Container cont, Component comp) {

    	int index = -1;
    	for ( int k = 0, count = cont.getComponentCount(); k < count && index == -1; k++ ) {
    		if ( cont.getComponent( k ) == comp ) {
    			index = k;
    		}
    	}
    	return index;
	}
    
    
    



    public static boolean classExists (String className) {
        try {
            Class.forName (className);
            return true;
        }
        catch (ClassNotFoundException exception) {
            return false;
        }
    } 

    
	public static void setCleanTableModelValue(TableModel tableModel, int row, int c, String data) {
		
//		System.out.println(tableModel.getColumnClass(c).getName()+"  isInstance of Double.class? "+tableModel.getColumnClass(c).getName().equals("java.lang.Double")+" data="+data);
		
		if (tableModel.getColumnClass(c).getName().equals("java.lang.Double")) {
			try {
				tableModel.setValueAt(new Double(data), row, c);
			} catch (Exception e) {
				tableModel.setValueAt(Double.NaN, row, c);
			}
		}
//		else if (tableModel.getColumnClass(c).isInstance(Float.class)) {
		else if (tableModel.getColumnClass(c).getName().equals("java.lang.Float")) {
			try {
				tableModel.setValueAt(new Float(data), row, c);
			} catch (Exception e) {
				tableModel.setValueAt(Float.NaN, row, c);
			}
		}
//		else if (tableModel.getColumnClass(c).isInstance(Integer.class)) {
		else if (tableModel.getColumnClass(c).getName().equals("java.lang.Integer")) {
			try {
				tableModel.setValueAt(new Integer(data), row, c);
			} catch (Exception e) {
				tableModel.setValueAt(Integer.MIN_VALUE, row, c);
			}
		}
//		else if (tableModel.getColumnClass(c).isInstance(Short.class)) {
		else if (tableModel.getColumnClass(c).getName().equals("java.name.Short")) {
			try {
				tableModel.setValueAt(new Short(data), row, c);
			} catch (Exception e) {
				tableModel.setValueAt(Short.MIN_VALUE, row, c);
			}
		}
		else {
			tableModel.setValueAt(data, row, c);
		}
	}
	

	public static void autoFitColumns(JTable table, String[] columnNames) {

		if (columnNames == null) {
			return;
		}

		int rowSampleSize = 6;
		int rows = table.getRowCount();
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = (Graphics2D)image.getGraphics();
		FontRenderContext fc = g.getFontRenderContext();

		for (int c=0; c<table.getColumnCount(); c++) {
			int maxWidth = -1;
			rowSampleSize = Math.min(rowSampleSize, rows);

			for (int r=0; r<rowSampleSize; r++) {
				int row = (int)(Math.random()*rows);
//				System.out.println("checking random row: "+row+" for column "+c);
				TableModel m = table.getModel();
				try {
					String value = table.getModel().getValueAt(r, c).toString();
					int width = (int) table.getFont().
							createGlyphVector(fc, value).getVisualBounds().getWidth();
					maxWidth = Math.max(width, maxWidth);
				} catch (Exception e) {
					System.err.println("row: "+row+" of "+rows+" , col: "+c+" of "+table.getColumnCount()+" :: error: "+e.getMessage());
					//					r--;
				}
			}
			int width = (int) table.getFont().
					createGlyphVector(fc, columnNames[c]).getVisualBounds().getWidth();
			maxWidth = Math.max(width, maxWidth);

			table.getColumnModel().getColumn(c).setPreferredWidth(maxWidth+15);
		}
	}
    

	public static void formatTextArea(JTextArea textArea) {
		int[] colLengths = null;
		String[] lines = textArea.getText().split("\n");
		for (int n=0; n<lines.length; n++) {
			if (lines[n].startsWith("##")) {
				continue;
			}
			// System.out.println(lines[n]);

			String[] cols = lines[n].split(",");
			if (colLengths == null) {
				colLengths = new int[cols.length];
			}
			if (colLengths.length != cols.length) {
				colLengths = new int[cols.length];
			}
			for (int i=0; i<cols.length; i++) {
				colLengths[i] = Math.max(colLengths[i], cols[i].length());
			}
		}
		// System.out.println(Arrays.toString(colLengths));

		StringBuilder sb = new StringBuilder();
		for (int n=0; n<lines.length; n++) {
			if (lines[n].startsWith("##")) {
				sb.append(lines[n]).append("\n");
				continue;
			}
			String[] cols = lines[n].split(",");
			for (int i=0; i<cols.length; i++) {
				sb.append(WCTUtils.getPad(cols[i], colLengths[i]));
				if (i < cols.length-1) {
					sb.append(",");
				}
			}
			sb.append("\n");
		}
		textArea.setText(sb.toString());
	}
	
	
    public static void setPanelEnabled(JPanel panel, boolean isEnabled) {
        panel.setEnabled(isEnabled);

        Component[] components = panel.getComponents();

        for (Component component : components) {
            if (component instanceof JPanel) {
                setPanelEnabled((JPanel) component, isEnabled);
            }
            component.setEnabled(isEnabled);
            if (component instanceof JButton) System.out.println("Button: "+((JButton)component).getText() + " isEnabled: "+((JButton)component).isEnabled());
        }
    }

	
}
