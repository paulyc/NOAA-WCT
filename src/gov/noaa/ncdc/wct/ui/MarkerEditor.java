/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 * 
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE 
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.  
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */


package gov.noaa.ncdc.wct.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.commons.io.FileUtils;
import org.geotools.ct.MathTransform;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.IllegalFilterException;
import org.geotools.gui.swing.tables.FeatureTableModel;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.pt.CoordinatePoint;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.jdesktop.swingx.border.DropShadowBorder;
import org.jfree.ui.FontChooserDialog;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import gov.noaa.ncdc.common.OpenFileFilter;
import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.common.RoundedBorder;
import gov.noaa.ncdc.gis.WCTGeocoder;
import gov.noaa.ncdc.gis.WCTGeocoder.GeocodeResult;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.nexrad.WCTProjections;
import gov.noaa.ncdc.wct.export.vector.StreamingAttributeExport;
import gov.noaa.ncdc.wct.export.vector.StreamingShapefileExport;
import gov.noaa.ncdc.wct.export.vector.StreamingWKTExport;

public class MarkerEditor extends JDialog implements ActionListener {

	public final static Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 18);
	public final static Color DEFAULT_COLOR = new Color(30, 144, 255);
	private Font markerFont = DEFAULT_FONT;
	private Color markerColor = DEFAULT_COLOR;

	public final static AttributeType[] MARKER_ATTRIBUTES = {
			AttributeTypeFactory.newAttributeType("geom", Geometry.class),
			AttributeTypeFactory.newAttributeType("label1", String.class),
			AttributeTypeFactory.newAttributeType("label2", String.class),
			AttributeTypeFactory.newAttributeType("label3", String.class),
			AttributeTypeFactory.newAttributeType("longitude", Double.class),
			AttributeTypeFactory.newAttributeType("latitude", Double.class)
	};

	private final static GeometryFactory geoFactory = new GeometryFactory();

	private JButton jbColor;
	private JCheckBox jcbVisible, jcbFillInfo;
	private JCheckBox jcbAutoReverseGeocode = new JCheckBox("Auto Reverse Geocode?");
	private JButton jbAdd, jbEdit, jbRemove, jbRemoveAll, jbExport, jbImport;
	private JButton jbStyle, jbInputUnits;
	private JTextField jtfLat, jtfLon, jtfLabel_1, jtfLabel_2, jtfLabel_3;
	private JTextField jtfLatDeg, jtfLonDeg, jtfLatMin, jtfLonMin, jtfLatSec, jtfLonSec;
	private JComboBox<String> jcomboDatums;
	private JComboBox<String> jcomboSize;
	private JPanel unitsPanel;
	private JTable markerTable;

	private FeatureCollection markerFeatures;
	private DefaultMapLayer markerMapLayer;
	private FeatureType marker_schema;
	private FeatureTableModel featureTableModel = new FeatureTableModel();

	private WCTViewer viewer;

	//private StyleEditor styleEditor;

	private int geoIndex = 0;

	private DecimalFormat fmt4 = new DecimalFormat("0.0000");

	private Vector<MarkerInfo> markerInfoVector = new Vector<MarkerInfo>();
	public static final File MARKER_OBJECT_FILE = new File(WCTConstants.getInstance().getCacheLocation()+File.separator+
			"objdata"+File.separator+MarkerEditor.class.getName()+".data");


	public MarkerEditor(DefaultMapLayer markerMapLayer, FeatureCollection markerFeatures, WCTViewer viewer) {
		super(viewer, "Marker Editor", false);
		this.markerFeatures = markerFeatures;
		this.markerMapLayer = markerMapLayer;
		this.viewer = viewer;

		init();
		createGUI();

		if (viewer.getGoogleEarthBrowserInternal() != null) {
			viewer.getGoogleEarthBrowserInternal().refreshView(false);
		}
		if (viewer.getGoogleEarthBrowserExternal() != null) {
			viewer.getGoogleEarthBrowserExternal().refreshView(false);
		}

	}

	private void init() {
		try {
			//markerFeatures = markerMapLayer.getFeatureSource().getFeatures().collection();
			//markerMapLayer.setStyle(getDefaultStyle()); 
			marker_schema = FeatureTypeFactory.newFeatureType(MARKER_ATTRIBUTES, "Marker Attributes");
		} catch (Exception e) {
			e.printStackTrace();
			javax.swing.JOptionPane.showMessageDialog(this, "Marker Init Exception: "+e, 
					"MARKER INIT EXCEPTION", javax.swing.JOptionPane.ERROR_MESSAGE);
		}

		try {
			loadObjectData();
		} catch (Exception e) {
			e.printStackTrace();
			javax.swing.JOptionPane.showMessageDialog(this, "Marker Cache Exception: "+e, 
					"MARKER CACHE EXCEPTION", javax.swing.JOptionPane.ERROR_MESSAGE);
		}

		markerFont = getDefaultFont();
		markerColor = getDefaultColor();

	}

	private void createGUI() {



		//------------------------------------------------------------------------------
		// Create top marker input panel
		//------------------------------------------------------------------------------

		JPanel inputPanel = new JPanel(new RiverLayout());

		// Input lat and lon panel
		JPanel coordPanel = new JPanel();
		jbInputUnits = new JButton("DMS");
		jbInputUnits.addActionListener(this);

		DocumentListener coordDocumentListener = new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent evt) {
				jcbAutoReverseGeocode.setSelected(false);				
			}

			@Override
			public void insertUpdate(DocumentEvent evt) {
			}

			@Override
			public void removeUpdate(DocumentEvent evt) {
			}
		};

		final Dialog finalThis = this;
		jcbAutoReverseGeocode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (jcbAutoReverseGeocode.isSelected() && 
						jtfLat.getText().trim().length() > 0 && 
						jtfLon.getText().trim().length() > 0) {

					try {
						WCTGeocoder geocoder = new WCTGeocoder();
						GeocodeResult ggr = geocoder.locationSearch(jtfLat.getText()+","+jtfLon.getText(), viewer.getCurrentExtent()).get(0);
						setMarker(ggr.getLon(), ggr.getLat(), ggr.getFormattedAddress(), "", "");
					} catch (Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(finalThis, "Error using Google Geocoding Service.  " +
								"Please validate input search location and verify internet connection", 
								"Geocoding Service Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		JPanel deciPanel = new JPanel();
		jtfLat = new JTextField(10);
		jtfLon = new JTextField(10);
		jtfLat.getDocument().addDocumentListener(coordDocumentListener);
		jtfLon.getDocument().addDocumentListener(coordDocumentListener);
		deciPanel.add(new JLabel("LAT: "));
		deciPanel.add(jtfLat);
		deciPanel.add(new JLabel(" LON: "));
		deciPanel.add(jtfLon);

		jtfLatDeg = new JTextField(3);
		jtfLonDeg = new JTextField(3);
		jtfLatMin = new JTextField(2);
		jtfLonMin = new JTextField(2);
		jtfLatSec = new JTextField(2);
		jtfLonSec = new JTextField(2);
		jtfLatDeg.getDocument().addDocumentListener(coordDocumentListener);
		jtfLonDeg.getDocument().addDocumentListener(coordDocumentListener);
		jtfLatMin.getDocument().addDocumentListener(coordDocumentListener);
		jtfLonMin.getDocument().addDocumentListener(coordDocumentListener);
		jtfLatSec.getDocument().addDocumentListener(coordDocumentListener);
		jtfLonSec.getDocument().addDocumentListener(coordDocumentListener);

		JPanel dmsPanel = new JPanel();
		dmsPanel.add(new JLabel("LAT:"));
		dmsPanel.add(jtfLatDeg);
		dmsPanel.add(jtfLatMin);
		dmsPanel.add(jtfLatSec);
		dmsPanel.add(new JLabel(" LON:"));
		dmsPanel.add(jtfLonDeg);
		dmsPanel.add(jtfLonMin);
		dmsPanel.add(jtfLonSec);

		unitsPanel = new JPanel();
		unitsPanel.setLayout(new CardLayout());
		unitsPanel.add(deciPanel, "Deci");
		unitsPanel.add(dmsPanel, "DMS");

		jcomboDatums = new JComboBox<String>(new String[] {"NAD83", "WGS84", "NAD27"});



		coordPanel.add(jbInputUnits);
		coordPanel.add(unitsPanel);
		coordPanel.add(jcomboDatums);
		coordPanel.add(new JSeparator());
		coordPanel.add(jcbAutoReverseGeocode);

		// Input label
		jtfLabel_1 = new JTextField(40);
		jtfLabel_2 = new JTextField(40);
		jtfLabel_3 = new JTextField(40);


		// Action Buttons
		JPanel optionsPanel = new JPanel();
		jcbVisible = new JCheckBox("Visible", true);
		jcbVisible.addActionListener(this);
		jcbFillInfo = new JCheckBox("Auto-Fill Labels", true);

		JButton jbFont = new JButton("Font");
		jbFont.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				handleFontChooser();
			}
		});
		JLabel jlColorLabel = new JLabel(" Color: ");
		jbColor = new JButton("Color");
		// set colors for button
		handleColorChooser(markerColor);
		jbColor.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent evt) {
				handleColorChooser();
			}
		});        

		optionsPanel.add(jcbVisible);
		optionsPanel.add(jcbFillInfo);
		//        optionsPanel.add(jlColorLabel);
		optionsPanel.add(jbColor);
		optionsPanel.add(jbFont);


		JPanel buttonPanel = new JPanel();
		jbImport = new JButton("Import");
		jbImport.addActionListener(this);
		jbExport = new JButton("Export");
		jbExport.addActionListener(this);
		jbAdd = new JButton("Add");
		jbAdd.addActionListener(this);
		jbEdit = new JButton("Edit");
		jbEdit.addActionListener(this);
		jbRemove = new JButton("Remove");
		jbRemove.addActionListener(this);
		jbRemoveAll = new JButton("Remove All");
		jbRemoveAll.addActionListener(this);

		buttonPanel.add(jbAdd);
		buttonPanel.add(jbEdit);
		buttonPanel.add(jbRemove);
		buttonPanel.add(jbRemoveAll);
		buttonPanel.add(new JLabel(" "));
		buttonPanel.add(jbImport);
		buttonPanel.add(jbExport);

		// TEMP STUFF FOR LINE CROSS
		jcomboSize = new JComboBox<String>(new String[] {"0.001", "0.005", "0.01", "0.05", "0.10", "0.20", "0.40"});
		jcomboSize.setSelectedItem("0.05");
		jcomboSize.setEditable(true);
		//buttonPanel.add(new JLabel("Size: "));
		//buttonPanel.add(jcomboSize);


		// Add to main panel

		inputPanel.add(coordPanel, "left");
		inputPanel.add(new JLabel("LABEL 1: "), "left br");
		inputPanel.add(jtfLabel_1, "hfill");
		inputPanel.add(new JLabel("LABEL 2: "), "left br");
		inputPanel.add(jtfLabel_2, "hfill");
		inputPanel.add(new JLabel("LABEL 3: "), "left br");
		inputPanel.add(jtfLabel_3, "hfill");
		inputPanel.add(optionsPanel, "br center");
		inputPanel.add(buttonPanel, "br center");

		//------------------------------------------------------------------------------
		// Create bottom JTable for markers
		//------------------------------------------------------------------------------
		markerTable = new JTable();
		markerTable.setModel(featureTableModel);
		final JScrollPane scrollPane = new JScrollPane(markerTable);      
		scrollPane.setPreferredSize(new java.awt.Dimension(50, 200));
		//        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


		final JPanel scrollPanePanel = new JPanel(new BorderLayout());
		scrollPanePanel.add(scrollPane, BorderLayout.CENTER);

		// Set up Borders
		DropShadowBorder dropShadowBorder = new DropShadowBorder(Color.BLACK, 7, 0.8f, 5, false, false, true, true);
		Border border2 = BorderFactory.createCompoundBorder(dropShadowBorder, BorderFactory.createEmptyBorder(2, 2, 0, 0));
		Border mainBorder = BorderFactory.createCompoundBorder(border2, new RoundedBorder(new Color(10, 36, 106, 150), 0, 0));

		inputPanel.setBorder(mainBorder);
		scrollPanePanel.setBorder(mainBorder);


		//------------------------------------------------------------------------------
		// Finish it up!
		//------------------------------------------------------------------------------
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(inputPanel, BorderLayout.NORTH);
		if (markerFeatures.size() > 0) {
			getContentPane().add(scrollPanePanel, BorderLayout.CENTER);
		}



		featureTableModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				if (markerFeatures.size() == 0) {
					if (getContentPane().getComponentCount() > 1) {
						getContentPane().remove(scrollPanePanel);
						pack();
					}
				}
				else {
					if (scrollPanePanel.getComponentCount() == 1) {
						getContentPane().add(scrollPanePanel, BorderLayout.CENTER);
						pack();
					}
				}
			}
		});




		JRootPane rootPane = this.getRootPane();
		InputMap iMap = rootPane.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");

		ActionMap aMap = rootPane.getActionMap();
		aMap.put("escape", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
	}


	private void handleFontChooser() {
		FontChooserDialog fontChooser = new FontChooserDialog(this, "Choose Font", true, markerFont); 
		fontChooser.pack();
		fontChooser.setVisible(true);
		System.out.println("new font is: "+fontChooser.getSelectedFont());
		markerFont = fontChooser.getSelectedFont();

		for (StyleChangeListener scl : styleChangeListeners) {
			try {
				scl.styleChanged(buildStyle(markerFont, markerColor));

			} catch (IllegalFilterException e) {
				e.printStackTrace();
			}
		}

		WCTProperties.setWCTProperty("wct_markers_font_name", markerFont.getFontName());
		WCTProperties.setWCTProperty("wct_markers_font_style", String.valueOf(markerFont.getStyle()));
		WCTProperties.setWCTProperty("wct_markers_font_size", String.valueOf(markerFont.getSize()));
	}

	private void handleColorChooser() {
		Color newColor = JColorChooser.showDialog(this, "Choose Marker Color", markerColor);
		if (newColor != null) {
			handleColorChooser(newColor);
		}
	}
	
	private void handleColorChooser(Color newColor) {
		markerColor = newColor;
		jbColor.setBackground(markerColor);

		// https://stackoverflow.com/questions/9780632/how-do-i-determine-if-a-color-is-closer-to-white-or-black
		// if closer to black, set text to white
		double Y = 0.2126*markerColor.getRed() + 0.7152*markerColor.getGreen() + 0.0722*markerColor.getBlue();
		Color textColor = Y > 128 ? Color.BLACK : Color.WHITE;
		jbColor.setForeground(textColor);

		for (StyleChangeListener scl : styleChangeListeners) {
			try {
				scl.styleChanged(buildStyle(markerFont, markerColor));

			} catch (IllegalFilterException e) {
				e.printStackTrace();
			}
		}
		
		WCTProperties.setWCTProperty("wct_markers_color", String.valueOf(markerColor.getRGB()));
	}


	public void actionPerformed(ActionEvent evt) {
		Object source = evt.getSource();
		if (source == jbInputUnits) {
			flipUnitsInput();
		}
		else if (source == jbEdit) {
			editFeature();
		}
		else if (source == jbAdd) {
			addFeature();
			markerMapLayer.setVisible(! jcbVisible.isSelected());
			markerMapLayer.setVisible(jcbVisible.isSelected());
			featureTableModel.setFeatureCollection(markerFeatures);
		}
		else if (source == jcbVisible) {

			MapContext map = viewer.getMapContext();

			markerMapLayer.setVisible(! jcbVisible.isSelected());
			markerMapLayer.setVisible(jcbVisible.isSelected());
			map.moveLayer(map.indexOf(markerMapLayer), map.getLayerCount()-1);

			if (viewer.getGoogleEarthBrowserInternal() != null) {
				viewer.getGoogleEarthBrowserInternal().refreshView(false);
			}
			if (viewer.getGoogleEarthBrowserExternal() != null) {
				viewer.getGoogleEarthBrowserExternal().refreshView(false);
			}
		}
		else if (source == jbRemove) {

			try {
				removeFeature();
			} catch (Exception e) {
				e.printStackTrace();
				javax.swing.JOptionPane.showMessageDialog(this, "Marker Exception: "+e, 
						"MARKER INIT EXCEPTION", javax.swing.JOptionPane.ERROR_MESSAGE);
			}

			featureTableModel.setFeatureCollection(markerFeatures);

			if (viewer.getGoogleEarthBrowserInternal() != null) {
				viewer.getGoogleEarthBrowserInternal().refreshView(false);
			}
			if (viewer.getGoogleEarthBrowserExternal() != null) {
				viewer.getGoogleEarthBrowserExternal().refreshView(false);
			}
		}
		else if (source == jbRemoveAll) {

			markerFeatures.clear();
			featureTableModel.setFeatureCollection(markerFeatures);

			try {
				saveObjectData();
			} catch (Exception e) {
				e.printStackTrace();
				javax.swing.JOptionPane.showMessageDialog(this, "Marker Exception: "+e, 
						"MARKER INIT EXCEPTION", javax.swing.JOptionPane.ERROR_MESSAGE);
			}

			if (viewer.getGoogleEarthBrowserInternal() != null) {
				viewer.getGoogleEarthBrowserInternal().refreshView(false);
			}
			if (viewer.getGoogleEarthBrowserExternal() != null) {
				viewer.getGoogleEarthBrowserExternal().refreshView(false);
			}
		}
		else if (source == jbExport) {
			exportMarkers();
		}
		else if (source == jbImport) {
			importMarkers();
		}

		viewer.getMapPane().repaint();

	}

	private void flipUnitsInput() {
		try {
			if (jbInputUnits.getText().equals("DMS")) {
				CardLayout cardLayout = (CardLayout)(unitsPanel.getLayout());
				cardLayout.show(unitsPanel, "DMS");
				jbInputUnits.setText("Deci");
			}
			else {
				CardLayout cardLayout = (CardLayout)(unitsPanel.getLayout());
				cardLayout.show(unitsPanel, "Deci");
				jbInputUnits.setText("DMS");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Font getDefaultFont() {
		try {
			String markerFontName = WCTProperties.getWCTProperty("wct_markers_font_name");
			String markerFontStyle = WCTProperties.getWCTProperty("wct_markers_font_style");
			String markerFontSize = WCTProperties.getWCTProperty("wct_markers_font_size");
			if (markerFontName == null || markerFontStyle == null || markerFontSize == null) {
				return DEFAULT_FONT;
			}
			else {
				return new Font(markerFontName, Integer.parseInt(markerFontStyle), Integer.parseInt(markerFontSize));
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return DEFAULT_FONT;
		}
	}
	
	public static Color getDefaultColor() { 
		try {
			String markerColor = WCTProperties.getWCTProperty("wct_markers_color");
			if (markerColor == null) {
				return DEFAULT_COLOR;
			}
			else {
				return new Color(Integer.parseInt(markerColor));
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return DEFAULT_COLOR;
		}
	}
	
	public static Style buildStyle(Font markerFont, Color markerColor) throws IllegalFilterException {

		StyleBuilder sb = new StyleBuilder();
		Style style = sb.createStyle();

		Mark mark1 = sb.createMark(StyleBuilder.MARK_CIRCLE, Color.BLACK, Color.BLACK, 6*markerFont.getSize()/10.0);
		Graphic gr1 = sb.createGraphic(null, mark1, null);
		PointSymbolizer pntSymbolizer1 = sb.createPointSymbolizer(gr1);

		Mark mark2 = sb.createMark(StyleBuilder.MARK_CIRCLE, markerColor, markerColor, 4*markerFont.getSize()/10.0);
		Graphic gr2 = sb.createGraphic(null, mark2, null);
		PointSymbolizer pntSymbolizer2 = sb.createPointSymbolizer(gr2);

		Mark mark3 = sb.createMark(StyleBuilder.MARK_CIRCLE, Color.WHITE, Color.WHITE, 1*markerFont.getSize()/10.0);
		Graphic gr3 = sb.createGraphic(null, mark3, null);
		PointSymbolizer pntSymbolizer3 = sb.createPointSymbolizer(gr3);

		//        try {
		//            ExternalGraphic eg = sb.createExternalGraphic(new URL("http://maps.google.com/mapfiles/kml/pal4/icon50.png"), "image/png");        
		//            Graphic gr4 = sb.createGraphic(eg, null, null);
		//            pntSymbolizer3 = sb.createPointSymbolizer(gr4);
		//        } catch (Exception e) {
		//            e.printStackTrace();
		//        }


		/*
      org.geotools.styling.ExternalGraphicImpl egi = new org.geotools.styling.ExternalGraphicImpl();
      java.net.URL markerURL = MarkerEditor.class.getResource("/icons/bluepin.gif");
      egi.setLocation(markerURL);
      gr.addExternalGraphic(egi);

      PointSymbolizer pointSymbolizer = sb.createPointSymbolizer(gr);

      LineSymbolizer lineSymbolizer00 = sb.createLineSymbolizer(Color.BLACK, 3.0);
      LineSymbolizer lineSymbolizer01 = sb.createLineSymbolizer(Color.WHITE, 1.0);
      LineSymbolizer lineSymbolizer = sb.createLineSymbolizer(defaultColor, 2.0);
		 */


		org.geotools.styling.Font font = sb.createFont(markerFont);
		TextSymbolizer textSymbolizer1 = sb.createTextSymbolizer(Color.white, font, "label1");
		textSymbolizer1.setLabelPlacement(sb.createPointPlacement(0.0, 0.0, markerFont.getSize(), markerFont.getSize()*-1.0, 0.0));
		textSymbolizer1.setHalo(sb.createHalo(Color.BLACK, .7, 2.2));
		TextSymbolizer textSymbolizer2 = sb.createTextSymbolizer(Color.white, font, "label2");
		textSymbolizer2.setLabelPlacement(sb.createPointPlacement(0.0, 0.0, markerFont.getSize(), 0.0, 0.0));
		textSymbolizer2.setHalo(sb.createHalo(Color.BLACK, .7, 2.2));
		TextSymbolizer textSymbolizer3 = sb.createTextSymbolizer(Color.white, font, "label3");
		textSymbolizer3.setLabelPlacement(sb.createPointPlacement(0.0, 0.0, markerFont.getSize(), markerFont.getSize(), 0.0));
		textSymbolizer3.setHalo(sb.createHalo(Color.BLACK, .7, 2.2));

		/*      
      style.addFeatureTypeStyle(sb.createFeatureTypeStyle("Marker Attributes", 
         //new Symbolizer[] { pointSymbolizer, textSymbolizer }));
         new Symbolizer[] { lineSymbolizer00, lineSymbolizer, lineSymbolizer01, 
            textSymbolizer1, textSymbolizer2, textSymbolizer3 }));
		 */
		style.addFeatureTypeStyle(sb.createFeatureTypeStyle("Marker Attributes", 
				//new Symbolizer[] { pointSymbolizer, textSymbolizer }));
				new Symbolizer[] { pntSymbolizer1, pntSymbolizer2, pntSymbolizer3, 
						textSymbolizer1, textSymbolizer2, textSymbolizer3 }));
		return style;         

		//return sb.createStyle(pointSymbolizer);
	}


	// matches decimal degrees with dms
	private void syncUnits() {
		try {
			double latdeg = Double.parseDouble(jtfLatDeg.getText());
			double latmin = Double.parseDouble(jtfLatMin.getText());
			double latsec = Double.parseDouble(jtfLatSec.getText());
			double londeg = Double.parseDouble(jtfLonDeg.getText());
			double lonmin = Double.parseDouble(jtfLonMin.getText());
			double lonsec = Double.parseDouble(jtfLonSec.getText());
			jtfLat.setText(""+(latdeg+latmin/60.0+latsec/3600.0));
			jtfLon.setText(""+(londeg+lonmin/60.0+lonsec/3600.0));
		} catch (Exception e) {
		}
	}

	/**
	 * Populates marker dialog information but does NOT add to map.
	 * @param lon
	 * @param lat
	 * @param label_1
	 * @param label_2
	 * @param label_3
	 */
	public void setMarker(double lon, double lat, String label_1, String label_2, String label_3) {
		try {
			if (jbInputUnits.getText().equals("Deci")) {
				flipUnitsInput();
			}
			jcomboDatums.setSelectedItem("NAD83");

			jtfLat.setText(fmt4.format(lat));
			jtfLon.setText(fmt4.format(lon));
			jtfLabel_1.setText(label_1);
			jtfLabel_1.requestFocus();
			jtfLabel_2.setText(label_2);
			jtfLabel_3.setText(label_3);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Populates marker dialog information and adds marker to feature collection and map.
	 * @param lon
	 * @param lat
	 * @param label_1
	 * @param label_2
	 * @param label_3
	 */
	public void addMarker(double lon, double lat, String label_1, String label_2, String label_3) {
		setMarker(lon, lat, label_1, label_2, label_3);
		addFeature();
	}

	public boolean isFillLabelWithInfo() {
		return jcbFillInfo.isSelected();
	}

	public void setFillLabelWithInfo(boolean fillLabelWithInfo) {
		jcbFillInfo.setSelected(fillLabelWithInfo); 
	}

	public boolean isAutoReverseGeocode() {
		return jcbAutoReverseGeocode.isSelected();
	}

	public boolean getMarkerVisibility() {
		return jcbVisible.isSelected();
	}

	public void setMarkerVisibility(boolean isVisible) {
		jcbVisible.setSelected(isVisible);

		if (viewer.getGoogleEarthBrowserInternal() != null) {
			viewer.getGoogleEarthBrowserInternal().refreshView(false);
		}
		if (viewer.getGoogleEarthBrowserExternal() != null) {
			viewer.getGoogleEarthBrowserExternal().refreshView(false);
		}
	}

	private void editFeature() {
		Feature f = null;
		
		int[] selectedRows = markerTable.getSelectedRows();
		FeatureIterator fi = markerFeatures.features();
		int index = 0;
		while (fi.hasNext()) {
			Feature feature = fi.next();
			if (index == selectedRows[0]) {
				f = feature;
			}
			index++;
		}
		
		jcomboDatums.setSelectedItem("NAD83");
		jtfLat.setText(f.getAttribute("latitude").toString());
		jtfLon.setText(f.getAttribute("longitude").toString());
		jtfLabel_1.setText(f.getAttribute("label1").toString());
		jtfLabel_2.setText(f.getAttribute("label2").toString());
		jtfLabel_3.setText(f.getAttribute("label3").toString());
	}

	// Creates feature from lat, lon and label text fields and adds to collection
	private void addFeature() {
		try {
			if (! jbInputUnits.getText().equals("DMS")) {
				if (jtfLatMin.getText().trim().length() == 0) {
					jtfLatMin.setText("0");
				}
				if (jtfLatSec.getText().trim().length() == 0) {
					jtfLatSec.setText("0");
				}
				if (jtfLonMin.getText().trim().length() == 0) {
					jtfLonMin.setText("0");
				}
				if (jtfLonSec.getText().trim().length() == 0) {
					jtfLonSec.setText("0");
				}

			}

			//syncUnits();
			double lat, lon;
			if (jbInputUnits.getText().equals("DMS")) {
				if (jtfLat.getText().trim().length() == 0 || 
						jtfLon.getText().trim().length() == 0) {
					throw new WCTException("Please enter valid lat/lon values");
				}
				lat = Double.parseDouble(jtfLat.getText());
				lon = Double.parseDouble(jtfLon.getText());
			}
			else {
				lat = Double.parseDouble(jtfLatDeg.getText());
				lon = Double.parseDouble(jtfLonDeg.getText());
				if (lat < 0) {
					lat -= Double.parseDouble(jtfLatMin.getText())/60.0;
					lat -= Double.parseDouble(jtfLatSec.getText())/3600.0;
				}
				else {
					lat += Double.parseDouble(jtfLatMin.getText())/60.0;
					lat += Double.parseDouble(jtfLatSec.getText())/3600.0;
				}
				if (lon < 0) {
					lon -= Double.parseDouble(jtfLonMin.getText())/60.0;
					lon -= Double.parseDouble(jtfLonSec.getText())/3600.0;
				}
				else {
					lon += Double.parseDouble(jtfLonMin.getText())/60.0;
					lon += Double.parseDouble(jtfLonSec.getText())/3600.0;
				}
			}

			// If lat/lon is not in NAD83, convert datum coordinates
			if (jcomboDatums.getSelectedItem().toString().equals("WGS84")) {
				// Use Geotools Proj4 implementation to get MathTransform object
				MathTransform nexradTransform = 
						WCTProjections.getMathTransform(WCTProjections.WGS84_WKT, WCTProjections.NAD83_WKT);
				double[] coords = (nexradTransform.transform(new CoordinatePoint(lon, lat), null)).getCoordinates();
				lat = coords[1];
				lon = coords[0];
			}
			else if (jcomboDatums.getSelectedItem().toString().equals("NAD27")) {
				// Use Geotools Proj4 implementation to get MathTransform object
				MathTransform nexradTransform = 
						WCTProjections.getMathTransform(WCTProjections.NAD27_WKT, WCTProjections.WGS84_WKT);
				double[] coords = (nexradTransform.transform(new CoordinatePoint(lon, lat), null)).getCoordinates();
				lat = coords[1];
				lon = coords[0];
			}


			Coordinate coord = new Coordinate(lon, lat);


			Feature feature = marker_schema.create(new Object[] {
					geoFactory.createPoint(coord),
					jtfLabel_1.getText(), jtfLabel_2.getText(), 
					jtfLabel_3.getText(), fmt4.format(lon), fmt4.format(lat)
			}, new Integer(geoIndex++).toString());



			// add to collection
			markerFeatures.add(feature);


			// add marker to google earth view
			if (viewer.getGoogleEarthBrowserInternal() != null) {
				viewer.getGoogleEarthBrowserInternal().refreshView(false);
			}
			if (viewer.getGoogleEarthBrowserExternal() != null) {
				viewer.getGoogleEarthBrowserExternal().refreshView(false);
			}


			System.out.println("ADDING MARKER: \n"+ feature.toString());
			saveObjectData();

		} catch (Exception e) {
			e.printStackTrace();
			javax.swing.JOptionPane.showMessageDialog(this, "Marker Creation Exception: "+e, 
					"MARKER CREATION EXCEPTION", javax.swing.JOptionPane.ERROR_MESSAGE);

		}
	}


	private void removeFeature() throws IOException {
		int[] rowToRemove = markerTable.getSelectedRows();

		FeatureCollection tmpFc = FeatureCollections.newCollection();
		FeatureIterator fi = markerFeatures.features();
		int index = 0;
		while (fi.hasNext()) {
			Feature feature = fi.next();
			boolean addFeature = true;
			for (int n=0; n<rowToRemove.length; n++) {
				if (index == rowToRemove[n]) {
					addFeature = false;
				}
			}
			if (addFeature) {
				tmpFc.add(feature);
			}
			index++;
		}

		markerFeatures.clear();
		fi = tmpFc.features();
		while (fi.hasNext()) {
			markerFeatures.add(fi.next());
		}

		saveObjectData();

	}








	/**
	 * Returns number of markers imported
	 * @param markerCSVFile
	 * @return
	 * @throws IOException
	 * @throws IllegalAttributeException
	 */
	private int importMarkers(File markerCSVFile) throws IOException, IllegalAttributeException {

		List<String> lines = FileUtils.readLines(markerCSVFile);

		// we are a little flexible, column order doesn't matter but columns must 
		// be named label1, label2, label3, latitude and longitude

		int label1Index = -1;
		int label2Index = -1;
		int label3Index = -1;
		int latIndex = -1;
		int lonIndex = -1;

		// checking first line for header
		String[] headerCols = lines.get(0).split(",");
		for (int n=0; n<headerCols.length; n++) {
			if (headerCols[n].equalsIgnoreCase("label1")) {
				label1Index = n;
			}
			else if (headerCols[n].equalsIgnoreCase("label2")) {
				label2Index = n;
			}
			else if (headerCols[n].equalsIgnoreCase("label3")) {
				label3Index = n;
			}
			else if (headerCols[n].equalsIgnoreCase("latitude")) {
				latIndex = n;
			}
			else if (headerCols[n].equalsIgnoreCase("longitude")) {
				lonIndex = n;
			}
		}

		if (latIndex < 0 || lonIndex < 0) {
			throw new IOException("Latitude or longitude columns were not found.\n" +
					"The marker file be comma-separated, with a header of\n" +
					"column names on the first line.");
		}

		int importCount = 0;

		for (int i=1; i<lines.size(); i++) {

			try {

				//    			String[] cols = lines.get(i).split(",");
				List<String> cols = WCTUtils.customSplitCSV(lines.get(i));

				double lat = Double.parseDouble(cols.get(latIndex).replaceAll("\"", ""));
				double lon = Double.parseDouble(cols.get(lonIndex).replaceAll("\"", ""));
				Coordinate coord = new Coordinate(lon, lat);
				Feature feature = marker_schema.create(new Object[] {
						geoFactory.createPoint(coord),
						(label1Index >= 0) ? cols.get(label1Index).replaceAll("\"", "") : "", 
								(label2Index >= 0) ? cols.get(label2Index).replaceAll("\"", "") : "",
										(label3Index >= 0) ? cols.get(label3Index).replaceAll("\"", "") : "",                		
												fmt4.format(lon), fmt4.format(lat)
				}, new Integer(geoIndex++).toString());

				// add to collection
				markerFeatures.add(feature);

				featureTableModel.setFeatureCollection(markerFeatures);

				importCount++;

			} catch (Exception e) {
				System.out.println("Error adding marker feature: "+e.getMessage());
			}

		}

		return importCount;

	}

	private void importMarkers() {

		try {
			// Set up File Chooser
			String startDir = WCTProperties.getWCTProperty("jne_export_dir");

			JFileChooser fc = new JFileChooser(startDir);
			fc.setDialogTitle("Choose Input Marker File");
			//            OpenFileFilter shpFilter = new OpenFileFilter("shp", true, "Shapefile");
			//            OpenFileFilter wktFilter = new OpenFileFilter("txt", true, "Well-Known Text");
			OpenFileFilter csvFilter = new OpenFileFilter("csv", true, "Comma Separated Text (CSV)");
			//            fc.addChoosableFileFilter(shpFilter);
			//            fc.addChoosableFileFilter(wktFilter);
			fc.addChoosableFileFilter(csvFilter);

			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(csvFilter);


			JPanel optionsPanel = new JPanel(new RiverLayout());
			JComboBox<String> operationCombo = new JComboBox<String>(new String[]{ "Append", "Replace/Overwrite" });
			optionsPanel.add(new JLabel("<html>There are "+markerFeatures.size()+" existing markers.<br>" +
					"Append or Replace/Overwrite<br>with imported markers? </html>"));
			optionsPanel.add(operationCombo, "br");

			if (markerFeatures.size() > 0) {
				fc.setAccessory(optionsPanel);
			}


			int returnVal = fc.showOpenDialog(this);
			File file = null;
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				int choice = JOptionPane.YES_OPTION;

				// initialize to YES!
				file = fc.getSelectedFile();

				// clear markers if needed
				if (operationCombo.getSelectedItem().equals("Replace/Overwrite")) {
					markerFeatures.clear();
					geoIndex = 0;
				}
				int importCount = importMarkers(file);

				String message = "Successful import of " + importCount+ " markers.";
				JOptionPane.showMessageDialog(this, (Object) message,
						"MARKER IMPORT RESULTS", JOptionPane.INFORMATION_MESSAGE);
			}

		} catch (Exception e) {
			e.printStackTrace();
			//            String message = "Error reading \n" +
			//            "<html><font color=red>" + file + "</font></html>";
			String message = "Error importing marker file...";
			JOptionPane.showMessageDialog(this, (Object) message,
					"MARKER IMPORT ERROR", JOptionPane.ERROR_MESSAGE);
		}

	}

	private void exportMarkers() {
		try {
			// Set up File Chooser
			String startDir = WCTProperties.getWCTProperty("jne_export_dir");

			JFileChooser fc = new JFileChooser(startDir);
			fc.setDialogTitle("Choose Output File");
			OpenFileFilter shpFilter = new OpenFileFilter("shp", true, "Shapefile");
			OpenFileFilter wktFilter = new OpenFileFilter("txt", true, "Well-Known Text");
			OpenFileFilter csvFilter = new OpenFileFilter("csv", true, "Comma Separated Text (CSV)");
			fc.addChoosableFileFilter(shpFilter);
			fc.addChoosableFileFilter(wktFilter);
			fc.addChoosableFileFilter(csvFilter);

			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(csvFilter);

			int returnVal = fc.showSaveDialog(this);
			File file = null;
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				int choice = JOptionPane.YES_OPTION;

				// initialize to YES!
				file = fc.getSelectedFile();
				String fstr = file.toString();

				// Add extension if needed
				if (fc.getFileFilter() == shpFilter) {
					if (! fstr.endsWith(".shp")) {
						file = new File(fstr+".shp");
					}
				}
				else if (fc.getFileFilter() == wktFilter) {
					if (! fstr.endsWith(".txt")) {
						file = new File(fstr+".txt");
					}
				}
				else if (fc.getFileFilter() == csvFilter) {
					if (! fstr.endsWith(".csv")) {
						file = new File(fstr+".csv");
					}
				}

				// Check for existing file
				if (file.exists()) {
					String message = "<html><font color=red>" + file + "</font></html>\n" +
							"already exists.\n\n" +
							"Do you want to proceed and OVERWRITE?";
					choice = JOptionPane.showConfirmDialog(this, (Object) message,
							"OVERWRITE PROJECT FILE", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
				}


				if (choice == JOptionPane.YES_OPTION) {

					String saveDir = fc.getSelectedFile().getParent();
					WCTProperties.setWCTProperty("jne_export_dir", saveDir);

					try {
						StreamingProcess exportProcess = null;
						if (fc.getFileFilter() == shpFilter) {
							exportProcess = new StreamingShapefileExport(file);
						}
						else if (fc.getFileFilter() == wktFilter) {
							exportProcess = new StreamingWKTExport(file);
						}
						else {
							exportProcess = new StreamingAttributeExport(file);
						}


						FeatureIterator fi = markerFeatures.features();
						while (fi.hasNext()) { 
							exportProcess.addFeature(fi.next());
						}
						exportProcess.close();


					} catch (Exception e) {
						e.printStackTrace();
						String message = "Error writing \n" +
								"<html><font color=red>" + file + "</font></html>";
						JOptionPane.showMessageDialog(this, (Object) message,
								"MARKER EXPORT ERROR", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			// END if(choice == YES_OPTION)


		} catch (Exception e) {
			e.printStackTrace();
		}
	}










	public static FeatureCollection loadMarkerObjectData(File objFile, FeatureCollection markerFeatures) 
			throws IllegalAttributeException, IOException, ClassNotFoundException, FactoryConfigurationError, SchemaException {


		if (markerFeatures == null) {
			markerFeatures = FeatureCollections.newCollection();
		}

		System.out.println("LOADING FROM: "+objFile);

		if (! objFile.exists()) {
			System.out.println("NO MARKER FEATURE COLLECTION OBJECT EXISTS...");
		}
		else {
			// Read from disk using FileInputStream.
			FileInputStream fin = new FileInputStream (objFile);

			// Read object using ObjectInputStream.
			ObjectInputStream objIn = new ObjectInputStream (fin);

			// Read an object.
			Object obj = objIn.readObject();

			// Is the object that you read in, say, an instance
			// of the Vector class?
			if (obj instanceof Vector) {
				// Cast object to a Vector
				//              FeatureCollection tmpMarkerFeatures = (FeatureCollection) obj;
				//              markerFeatures.clear();
				//              markerFeatures.addAll(tmpMarkerFeatures);

				//                markerInfoVector.clear();

				Vector<MarkerInfo> markerInfoVector = (Vector)obj;
				markerFeatures.clear();
				int geoIndex = 0;
				DecimalFormat fmt4 = new DecimalFormat("0.0000");
				FeatureType marker_schema = FeatureTypeFactory.newFeatureType(MARKER_ATTRIBUTES, "Marker Attributes");

				for (int n=0; n<markerInfoVector.size(); n++) {

					//                    System.out.println(markerInfoVector.get(n).toString());

					Coordinate coord = new Coordinate(Double.parseDouble(markerInfoVector.get(n).getLon()), 
							Double.parseDouble(markerInfoVector.get(n).getLat()));

					Feature feature = marker_schema.create(new Object[] {
							geoFactory.createPoint(coord),
							markerInfoVector.get(n).getLabel1(), 
							markerInfoVector.get(n).getLabel2(),
							markerInfoVector.get(n).getLabel3(),
							fmt4.format(coord.x), fmt4.format(coord.y)                        
					}, new Integer(geoIndex++).toString());

					markerFeatures.add(feature);                
				}

			}
			else {
				System.err.println("COULD NOT LOAD MARKER FEATURE COLLECTION OBJECT\n" +
						objFile+" IS OF TYPE: "+obj.toString());

			}
			objIn.close();
			fin.close();
		}

		return markerFeatures;
	}



	public void loadObjectData() throws IOException, ClassNotFoundException, IllegalAttributeException, FactoryConfigurationError, SchemaException {

		System.out.println("LOADING FROM: "+MARKER_OBJECT_FILE);

		if (! MARKER_OBJECT_FILE.exists()) {
			System.out.println("NO MARKER FEATURE COLLECTION OBJECT EXISTS...");
		}
		else {
			//            // Read from disk using FileInputStream.
			//            FileInputStream fin = new FileInputStream (objFile);
			//
			//            // Read object using ObjectInputStream.
			//            ObjectInputStream objIn = new ObjectInputStream (fin);
			//
			//            // Read an object.
			//            Object obj = objIn.readObject();
			//
			//            // Is the object that you read in, say, an instance
			//            // of the Vector class?
			//            if (obj instanceof Vector) {
			//              // Cast object to a Vector
			////              FeatureCollection tmpMarkerFeatures = (FeatureCollection) obj;
			////              markerFeatures.clear();
			////              markerFeatures.addAll(tmpMarkerFeatures);
			//
			////                markerInfoVector.clear();
			//                
			//                markerInfoVector = (Vector)obj;
			//                markerFeatures.clear();
			//                for (int n=0; n<markerInfoVector.size(); n++) {
			//
			////                    System.out.println(markerInfoVector.get(n).toString());
			//                    
			//                    Coordinate coord = new Coordinate(Double.parseDouble(markerInfoVector.get(n).getLon()), 
			//                            Double.parseDouble(markerInfoVector.get(n).getLat()));
			//                    
			//                    Feature feature = marker_schema.create(new Object[] {
			//                        geoFactory.createPoint(coord),
			//                        markerInfoVector.get(n).getLabel1(), 
			//                        markerInfoVector.get(n).getLabel2(),
			//                        markerInfoVector.get(n).getLabel3(),
			//                        fmt4.format(coord.x), fmt4.format(coord.y)                        
			//                    }, new Integer(geoIndex++).toString());
			//                    
			//                    markerFeatures.add(feature);                
			//                }

			loadMarkerObjectData(MARKER_OBJECT_FILE, markerFeatures);
			featureTableModel.setFeatureCollection(markerFeatures);
			

			//            }
			//            else {
			//                System.err.println("COULD NOT LOAD MARKER FEATURE COLLECTION OBJECT\n" +
			//                        objFile+" IS OF TYPE: "+obj.toString());
			//                
			//            }
			//            objIn.close();
			//            fin.close();
		}
	}

	public void saveObjectData() throws IOException {

		// 1. build marker info vector
		markerInfoVector.clear();

		FeatureIterator fi = markerFeatures.features();
		while (fi.hasNext()) { 
			Feature f = fi.next();
			markerInfoVector.add(new 
					MarkerInfo(f.getAttribute("label1").toString(), 
							f.getAttribute("label2").toString(),
							f.getAttribute("label3").toString(),
							f.getAttribute("latitude").toString(),
							f.getAttribute("longitude").toString())
					);
		}

		// 2. save this vector to serialized object


		// create dir if needed
		if (! MARKER_OBJECT_FILE.getParentFile().exists()) {
			MARKER_OBJECT_FILE.getParentFile().mkdirs();
		}

		// Use a FileOutputStream to send data to a file
		FileOutputStream fout = new FileOutputStream (MARKER_OBJECT_FILE);

		// Use an ObjectOutputStream to send object data to the
		// FileOutputStream for writing to disk.
		ObjectOutputStream objOut = new ObjectOutputStream(fout);

		// Pass our object to the ObjectOutputStream's
		// writeObject() method to cause it to be written out
		// to disk.



		objOut.writeObject(markerInfoVector);

		objOut.close();
		fout.close();
	}


	// allow a listener to handle changes to the style of the markers.  In the WCT, this is used by WCTViewer to 
	// respond to changes in color and font

	private ArrayList<StyleChangeListener> styleChangeListeners = new ArrayList<StyleChangeListener>();
	public void addStyleChangeListener(StyleChangeListener l) {
		styleChangeListeners.add(l);
	}

	public void removeStyleChangeListener(StyleChangeListener l) {
		styleChangeListeners.remove(l);
	}
	public StyleChangeListener[] getStyleChangeListeners() {
		return styleChangeListeners.toArray(new StyleChangeListener[styleChangeListeners.size()]);
	}









}
