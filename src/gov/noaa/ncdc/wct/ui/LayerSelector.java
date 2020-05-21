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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.media.jai.WritableRenderedImageAdapter;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
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
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.cv.SampleDimension;
import org.geotools.gc.GridCoverage;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.StackedBox;
import org.jdesktop.swingx.StackedBox.BoxEvent;
import org.jdesktop.swingx.StackedBox.BoxListener;
import org.xml.sax.SAXException;

import gov.noaa.ncdc.common.OpenFileFilter;
import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.ColorLutReaders;
import gov.noaa.ncdc.wct.decoders.ColorsAndValues;
import gov.noaa.ncdc.wct.decoders.SampleDimensionAndLabels;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradSampleDimensionFactory;
import gov.noaa.ncdc.wct.export.raster.WCTGridCoverageSupport;
import gov.noaa.ncdc.wct.ui.WCTViewer.CurrentViewType;
import gov.noaa.ncdc.wms.WMSPanel;
import ucar.units.Unit;
import ucar.units.UnitDB;
import ucar.units.UnitDBException;
import ucar.units.UnitDBManager;


public class LayerSelector extends JDialog implements ActionListener {

	public static enum DataType { RADAR, SATELLITE, GRIDDED, LOCATION_STATION };
	private DataType lastIsolatedType = null;



	private int numLayers = WCTViewer.NUM_LAYERS;
	private WCTViewer viewer;
	private WMSPanel wmsPanel;
	private JTabbedPane tabPane;
	private JPanel jnxPanel, mainPanel;
	private JPanel dataPanel = new JPanel();
	private JPanel overlayPanel = new JPanel();
	private JPanel snapshotPanel = new JPanel();
	private JPanel gePanel = new JPanel();
	private ArrayList<JPanel> layersPanel = new ArrayList<JPanel>();
	private ArrayList<JPanel> attPanel = new ArrayList<JPanel>();
	private ArrayList<JButton> jbColor = new ArrayList<JButton>();
	private ArrayList<JCheckBox> jcbVisible = new ArrayList<JCheckBox>();
	private ArrayList<JCheckBox> jcbLabel = new ArrayList<JCheckBox>();
	private ArrayList<JComboBox<String>> styleOptions = new ArrayList<JComboBox<String>>();
	private JButton jbLoadShapefile, jbBackground;
	private JCheckBox jcbDataLayersOnTop;
	private File lastFolder;
	private int localThemeCounter = 0;

	// Background color bogus panels
	private JLabel backgroundLabel;
	private JCheckBox jcbBogus;
	private JPanel apanel = new JPanel();
	private JPanel bpanel = new JPanel();


	private JCheckBox jcbRadar, jcbGridSatellite, jcbLocationStation;
	private final JComboBox<String> radTransparency = new JComboBox<String>(new String[] {
			"  0 %", " 10 %", " 20 %", " 30 %", " 40 %", " 50 %", 
			" 60 %", " 70 %", " 80 %", " 90 %", "100 %"
	});      
	private final JComboBox<String> gridSatTransparency = new JComboBox<String>(new String[] {
			" Default" , "  0 %", " 10 %", " 20 %", " 30 %", " 40 %", " 50 %", 
			" 60 %", " 70 %", " 80 %", " 90 %", "100 %"
	});          
	private final JComboBox<String> locationStationTransparency = new JComboBox<String>(new String[] {
			" Default" , "  0 %", " 10 %", " 20 %", " 30 %", " 40 %", " 50 %", 
			" 60 %", " 70 %", " 80 %", " 90 %", "100 %"
	});  
	private final JComboBox<String> radLegend = new JComboBox<String>(new String[] {
			"None", "Large"
	});      
	private final JComboBox<String> gridSatLegend = new JComboBox<String>(new String[] {
			"None", "Medium"
	});        
	private final JComboBox<String> locationStationLegend = new JComboBox<String>(new String[] {
			"None", "Medium"
	});  
	private final JComboBox<String> gridSatUnits = new JComboBox<String>(new String[] {
			"Native (no conversion)"
	});

	private final JComboBox<String> satColorTableCombo = new JComboBox<String>(new String[] {
			"Default", "Auto Grayscale Flat", "Auto Grayscale - Black/Trans to White/Opaque", 
			"Auto Grayscale - Gray/Trans to White/Opaque",
			"McIDAS_TSTORM1.ET", "McIDAS_TSTORM2.ET", "Water Vapor 1 (McIDAS_VAPOR1.ET)", "Water Vapor 2 (McIDAS_WVRBT3.ET)",
			"McIDAS_TEMPS1.ET", "McIDAS_SSTS.ET", "McIDAS_CA.ET",
			"McIDAS_BB.ET", "McIDAS_BD.ET"
	});

	private final JComboBox<String> gridColorTableCombo = new JComboBox<String>(new String[] {
			"Default", "Rainbow", "Rainbow2", "Rainbow3", "Grayscale", 
			"Dry (Red) - Wet (Blue)",
			"Blue-White-Red", "Blue-Black-Red", "Blue-Red", "Blue-Green-Red", "Brown-White-Green", "Brown-White-Green (Darker)",
			"Yellow-Blue", "Yellow-Blue-Purple", "Yellow-Blue-Purple-Red", "Yellow-Red-Purple-Blue", "Yellow-Purple",
			"White-Green", "Black-Green",
			"White-Blue", "Black-Blue", "White-Red", "Black-Red",
			"Drought: Palmer Drought Indices", "Drought: Standardized Precip. Index (SPI)",
			"Radar: Reflectivity", "Radar: Diff. Reflectivity",
			"Satellite: Water Vapor (Default)", "Satellite: Infrared Window (Default)",
			"Satellite: McIDAS_TSTORM1.ET", "Satellite: McIDAS_TSTORM2.ET", 
			"Satellite: Water Vapor 1 (McIDAS_VAPOR1.ET)", "Satellite: Water Vapor 2 (McIDAS_WVRBT3.ET)",
			"Satellite: McIDAS_TEMPS1.ET", "Satellite: McIDAS_SSTS.ET", "Satellite: McIDAS_CA.ET",
			"Satellite: McIDAS_BB.ET", "Satellite: McIDAS_BD.ET"
	});

	private final JComboBox<String> locationStationColorTableCombo = new JComboBox<String>(new String[] {
			"Rainbow", "Rainbow2", "Rainbow3", "Grayscale", 
			"Dry (Red) - Wet (Blue)",
			"Blue-White-Red", "Blue-Black-Red", "Blue-Red", "Blue-Green-Red", "Brown-White-Green", "Brown-White-Green (Darker)",
			"Yellow-Blue", "Yellow-Blue-Purple", "Yellow-Blue-Purple-Red", "Yellow-Red-Purple-Blue", "Yellow-Purple",
			"White-Green", "Black-Green",
			"White-Blue", "Black-Blue", "White-Red", "Black-Red",
			//            "Drought: Palmer (WestWide Drought Tracker)"
	});

	private final JComboBox<String> radColorTableCombo = new JComboBox<String>(new String[] {
			"Default"
	});
	private final JXHyperlink radEditColorTableLink = new JXHyperlink();


	//    private final JRadioButton jrbGridValidRangeMinMax = new JRadioButton("Valid Range", true);
	//    private final JRadioButton jrbGridAutoMinMax = new JRadioButton("Auto", false);
	//    private final JRadioButton jrbGridCustomMinMax = new JRadioButton("Custom", false);
	private final JComboBox<String> jcomboGridMinMax = 
			new JComboBox<String>(new String[] {
					"Auto", "Center on Zero", "Valid Range", "Custom"	
			});

	private final JComboBox<String> jcomboLocationStationMinMax = 
			new JComboBox<String>(new String[] {
					"Auto", "Center on Zero", "Valid Range", "Custom"	
			});


	private final JCheckBox jcbFlipGridColorTable = new JCheckBox("Flip?");
	private final JTextField jtfGridMinValue = new JTextField(10);
	private final JTextField jtfGridMaxValue = new JTextField(10);

	private final JCheckBox jcbFlipLocationStationColorTable = new JCheckBox("Flip?");
	private final JTextField jtfLocationStationMinValue = new JTextField(10);
	private final JTextField jtfLocationStationMaxValue = new JTextField(10);

	private final JSpinner radSmoothing = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
	private final JSpinner gridSatSmoothing = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
	private Timer timer = new Timer();

	private JPanel gridSatellitePanel = new JPanel();
	private JPanel gridColorTablePanel = new JPanel();
	private JPanel satColorTablePanel = new JPanel();
	private JPanel locationStationPanel = new JPanel();
	private JPanel locationStationColorTablePanel = new JPanel();

	private final StackedBox dataLayersStack = new StackedBox();
	private final StackedBox snapshotLayersStack = new StackedBox();
	private int snapshotLayerCounter = 0;

	public LayerSelector(WCTViewer viewer) throws ParserConfigurationException, SAXException, IOException, Exception {
		super(viewer, "Map Layer Selector", false);
		this.viewer = viewer;
		sizeVectors(numLayers);
		createGUI();
		pack();
		//        setSize(getSize().width, getSize().height+20);
		setSize(495, getSize().height+20);
	}

	// Initialize the vectors!
	private void sizeVectors(int num) {
		layersPanel.ensureCapacity(num);
		attPanel.ensureCapacity(num);
		jbColor.ensureCapacity(num);
		jcbVisible.ensureCapacity(num);
		jcbLabel.ensureCapacity(num);
		styleOptions.ensureCapacity(num);
		for (int n=0; n<num; n++) {
			layersPanel.add(null);
			attPanel.add(null);
			jbColor.add(null);
			jcbVisible.add(null);
			jcbLabel.add(null);
			styleOptions.add(null);
		}
	}

	private void createGUI() throws ParserConfigurationException, SAXException, IOException, Exception {

		// Create Panel for each Map Layer

		makeMapPanel(WCTViewer.COUNTRIES, "Countries", viewer.isLayerVisible(WCTViewer.COUNTRIES));
		makeMapPanel(WCTViewer.COUNTRIES_OUT, "Country Borders", viewer.isLayerVisible(WCTViewer.COUNTRIES_OUT));   
		makeMapPanel(WCTViewer.STATES, "States", viewer.isLayerVisible(WCTViewer.STATES));
		makeMapPanel(WCTViewer.STATES_OUT, "State Borders", viewer.isLayerVisible(WCTViewer.STATES_OUT));
		makeMapPanel(WCTViewer.COUNTIES, "Counties", viewer.isLayerVisible(WCTViewer.COUNTIES));
		makeMapPanel(WCTViewer.HWY_INT, "Major Highways", viewer.isLayerVisible(WCTViewer.HWY_INT));
		makeMapPanel(WCTViewer.RIVERS, "Rivers", viewer.isLayerVisible(WCTViewer.RIVERS));
		makeMapPanel(WCTViewer.WSR, "NEXRAD Sites", viewer.isLayerVisible(WCTViewer.WSR));
		makeMapPanel(WCTViewer.TDWR, "TDWR Sites", viewer.isLayerVisible(WCTViewer.TDWR));
		makeMapPanel(WCTViewer.CITY_SMALL, "Cities (< 10k)", viewer.isLayerVisible(WCTViewer.CITY_SMALL));
		makeMapPanel(WCTViewer.CITY10, "Cities (10k - 35k)", viewer.isLayerVisible(WCTViewer.CITY10));
		makeMapPanel(WCTViewer.CITY35, "Cities (35k - 100k)", viewer.isLayerVisible(WCTViewer.CITY35));
		makeMapPanel(WCTViewer.CITY100, "Cities (100k - 250k)", viewer.isLayerVisible(WCTViewer.CITY100));
		makeMapPanel(WCTViewer.CITY250, "Cities (Pop > 250k)", viewer.isLayerVisible(WCTViewer.CITY250));
		makeMapPanel(WCTViewer.AIRPORTS, "Airports", viewer.isLayerVisible(WCTViewer.AIRPORTS));
		makeMapPanel(WCTViewer.ASOS, "ASOS/AWOS", viewer.isLayerVisible(WCTViewer.ASOS));
		makeMapPanel(WCTViewer.CLIMATE_DIV, "Climate Divisions", viewer.isLayerVisible(WCTViewer.CLIMATE_DIV));
		makeMapPanel(WCTViewer.CRN, "CRN", viewer.isLayerVisible(WCTViewer.CRN));


		// Create main panel
		jnxPanel = new JPanel();
		jnxPanel.setLayout(new BoxLayout(jnxPanel, BoxLayout.Y_AXIS));
		// Add the column labels
		jnxPanel.add(makeColumnLabels());
		// Add each map layer's panel in reverse order so top layer is on top of panel
		for (int n = numLayers - 1; n >= 0; n--) {
			try {
				jnxPanel.add(layersPanel.get(n));
			} catch (NullPointerException e) {}
		}
		// Add background color panel
		jnxPanel.add(makeBackgroundPanel());

		JScrollPane scrollPane = new JScrollPane(jnxPanel);

		// Create Load Shapefile Button
		jcbDataLayersOnTop = new JCheckBox("Data Layers On Top");
		jcbDataLayersOnTop.addActionListener(this);

		jbLoadShapefile = new JButton("Load Shapefile");
		jbLoadShapefile.addActionListener(this);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(jcbDataLayersOnTop);
		buttonPanel.add(jbLoadShapefile);

		// temp location ---------------------------
		final LayerSelector finalThis = this;
		//      JButton loadSatButton = new JButton("Load GOES Satellite");
		//      loadSatButton.addActionListener(new ActionListener() {
		//      public void actionPerformed(ActionEvent arg0) {
		//      finalThis.loadSatelliteLayer();
		//      }
		//      });
		//      buttonPanel.add(loadSatButton);




		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(scrollPane, "Center");
		mainPanel.add(buttonPanel, "South");

		overlayPanel.setLayout(new BorderLayout());
		overlayPanel.add(mainPanel, "Center");


		wmsPanel = new WMSPanel(viewer);
		viewer.getMapPaneZoomChange().setWMSPanel(wmsPanel);








		dataLayersStack.setTitleBackgroundColor(this.getContentPane().getBackground());
		dataLayersStack.setSeparatorColor(this.getContentPane().getBackground().darker());
		dataLayersStack.setBackground(this.getContentPane().getBackground());        

		snapshotLayersStack.setTitleBackgroundColor(this.getContentPane().getBackground());
		snapshotLayersStack.setSeparatorColor(this.getContentPane().getBackground().darker());
		snapshotLayersStack.setBackground(this.getContentPane().getBackground());


		// put it in a scrollpane
		JScrollPane dataLayersScrollPane = new JScrollPane(dataLayersStack);
		dataLayersScrollPane.setBorder(null);

		JScrollPane snapshotLayersScrollPane = new JScrollPane(snapshotLayersStack);
		snapshotLayersScrollPane.setBorder(null);




		//        JPanel dataLayersPanel = new JPanel();
		//        dataLayersPanel.setLayout(new RiverLayout());


		radColorTableCombo.setPreferredSize(new Dimension(260, radColorTableCombo.getPreferredSize().height));
		gridColorTableCombo.setPreferredSize(new Dimension(260, radColorTableCombo.getPreferredSize().height));
		satColorTableCombo.setPreferredSize(new Dimension(260, radColorTableCombo.getPreferredSize().height));


		JPanel radarPanel = new JPanel();
		//        radarPanel.setBorder(WCTUiUtils.myTitledBorder("Radar", 7));
		radarPanel.setLayout(new RiverLayout());

		jcbRadar = new JCheckBox("Visible", true);
		jcbRadar.setActionCommand("Radar");
		jcbRadar.addActionListener(new DataLayersListener());

		radTransparency.addActionListener(new ActionListener() {
			//            @Override
			public void actionPerformed(ActionEvent e) {
				int percent = Integer.parseInt(((String)radTransparency.getSelectedItem()).replaceAll("%","").trim());
				viewer.setRadarTransparency(255 - ((int)((percent/100.0)*255)));
			}
		});
		radTransparency.setEditable(true);

		radSmoothing.addChangeListener(new RadarSmoothingChangeListener());
		radSmoothing.setEnabled(false);

		radLegend.setSelectedItem("Large");
		radLegend.addActionListener(new ActionListener() {
			//            @Override
			public void actionPerformed(ActionEvent e) {
				if (radLegend.getSelectedItem().toString().equals("None")) {
					viewer.setLegendSidePanelVisibility(false);
					viewer.refreshAfterResize();
				}
				else if (radLegend.getSelectedItem().toString().equals("Large")) {
					viewer.setLegendSidePanelVisibility(true);
					viewer.refreshAfterResize();
				}
			}
		});

		JPanel radTransPanel = new JPanel();
		radTransPanel.setLayout(new RiverLayout());
		radTransPanel.add(new JLabel("Transparency"), "center");
		radTransPanel.add(radTransparency, "br center");

		JPanel radSmoothingPanel = new JPanel();
		radSmoothingPanel.setLayout(new RiverLayout());
		radSmoothingPanel.add(new JLabel("Smoothing"), "center");
		radSmoothingPanel.add(radSmoothing, "br center");


		final String smoothingText = 
				"<html><b>NOTE:</b> The smoothing option must be used with caution.  The smoothing <br>" +
						"algorithm is basic non-scientific pixel smoothing over the resampled image, similar to smoothing <br>" +
						"photographs.  Smoothing can be helpful in making the images visually appealing and allow easier <br>" +
						"visual detection of features such as hook echoes.  However, excessive smoothing can eliminate <br>" +
						"important features in the data and reduce the intensity of small events.  </html>"
						;

		MouseListener mouseListener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JOptionPane.showMessageDialog(finalThis, smoothingText, "Smoothing Warning", JOptionPane.WARNING_MESSAGE);
			}
		};
		JLabel infoLabel = new JLabel(new ImageIcon(LayerSelector.class.getResource("/icons/question-mark.png")));
		infoLabel.setToolTipText(smoothingText);
		infoLabel.addMouseListener(mouseListener);
		radSmoothingPanel.add(infoLabel, "right");

		JPanel radLegendPanel = new JPanel();
		radLegendPanel.setLayout(new RiverLayout());
		radLegendPanel.add(new JLabel("Legend"), "center");
		radLegendPanel.add(radLegend, "br center");


		radColorTableCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (radColorTableCombo.getSelectedItem() == null) {
					return;
				}
				System.out.println("CHOSE: "+radColorTableCombo.getSelectedItem().toString());
				try {
					updateRadarColorTable();
				} catch (Exception e1) {				
					e1.printStackTrace();
					WCTUiUtils.showErrorMessage(finalThis, "Error updating radar color table", e1);
				}
			}
		});   
		refreshRadarColorTableList();


		radEditColorTableLink.setText("Edit");
		radEditColorTableLink.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ColorTableEditorUI ctEditorUI = new ColorTableEditorUI(viewer, radColorTableCombo.getSelectedItem().toString());
				ctEditorUI.setVisible(true);
				ctEditorUI.pack();
			}
		});

		JPanel radColorTablePanel = new JPanel();
		radColorTablePanel.setLayout(new RiverLayout());
		radColorTablePanel.add(new JLabel("Color Table: "), "left");
		radColorTablePanel.add(radColorTableCombo, "hfill");
		radColorTablePanel.add(radEditColorTableLink, "");



		radarPanel.add(jcbRadar);
		radarPanel.add(radTransPanel, "tab");
		radarPanel.add(radSmoothingPanel, "tab");
		radarPanel.add(radLegendPanel, "tab");
		radarPanel.add(radColorTablePanel, "br left");

		jcbRadar.setEnabled(false);
		radTransparency.setEnabled(false);
		radLegend.setEnabled(false);
		radColorTableCombo.setEnabled(false);
		radEditColorTableLink.setEnabled(false);








		gridSatellitePanel.setLayout(new RiverLayout());

		jcbGridSatellite = new JCheckBox("Visible", true);
		jcbGridSatellite.setActionCommand("Satellite");
		jcbGridSatellite.addActionListener(new DataLayersListener());




		gridSatellitePanel.setEnabled(false);

		gridSatTransparency.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (gridSatTransparency.getSelectedItem().toString().trim().equalsIgnoreCase("Default")) {
					viewer.setGridSatelliteTransparency(-1);
				}
				else {
					int percent = Integer.parseInt(((String)gridSatTransparency.getSelectedItem()).replaceAll("%","").trim());
					viewer.setGridSatelliteTransparency(255 - ((int)((percent/100.0)*255)));
				}
				viewer.setGridSatelliteColorTable(satColorTableCombo.getSelectedItem().toString());
			}
		});
		gridSatTransparency.setEditable(true);

		gridSatSmoothing.addChangeListener(new SatelliteSmoothingChangeListener());
		gridSatSmoothing.setEnabled(false);

		gridSatLegend.setSelectedItem("Medium");
		gridSatLegend.setEnabled(false);
		gridSatLegend.addActionListener(new ActionListener() {
			//            @Override
			public void actionPerformed(ActionEvent e) {
				///  TODO: add legend handling stuff here
				if (gridSatLegend.getSelectedItem().toString().equals("Medium")) {
					viewer.setGridSatelliteLegendVisibility(true);
				}
				else {
					viewer.setGridSatelliteLegendVisibility(false);
				}
			}
		});

		JXHyperlink gridSatLegendEdit = new JXHyperlink();
		gridSatLegendEdit.setText("Edit");
		gridSatLegendEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LegendEditor legendEditor = LegendEditor.getInstance(viewer, viewer);
				legendEditor.setVisible(true);
			}
		});




		gridSatUnits.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					if (viewer.getGridDatasetRaster() != null) {
						String nativeUnits = gridSatUnits.getSelectedItem().toString();
						viewer.getGridDatasetRaster().setDestinationConversionUnits(nativeUnits);


						UnitDB unitDB = UnitDBManager.instance();
						// try by name and then by symbol
						Unit natUnit = unitDB.getByName(nativeUnits);
						if (natUnit == null) {
							natUnit = unitDB.getBySymbol(nativeUnits);
						}
						if (natUnit != null) {
							WCTProperties.setWCTProperty(natUnit.getDerivedUnit().getName(), natUnit.getName());
						}

						viewer.loadData();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}        	
		});


		jtfGridMinValue.setEnabled(false);
		jtfGridMaxValue.setEnabled(false);

		satColorTableCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("CHOSE: "+satColorTableCombo.getSelectedItem().toString());
				viewer.setGridSatelliteColorTable(satColorTableCombo.getSelectedItem().toString());
			}
		});   

		gridColorTableCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("CHOSE: "+gridColorTableCombo.getSelectedItem().toString());
				viewer.setGridSatelliteColorTable(gridColorTableCombo.getSelectedItem().toString());
			}
		});   
		KeyListener minMaxListener = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					if (jtfGridMinValue.getText().trim().length() > 0 &&
							jtfGridMaxValue.getText().trim().length() > 0) {

						viewer.setGridSatelliteColorTable(satColorTableCombo.getSelectedItem().toString());
					}
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
				}
			}
			@Override
			public void keyTyped(KeyEvent e) {
			}            
		};
		jtfGridMinValue.addKeyListener(minMaxListener);
		jtfGridMaxValue.addKeyListener(minMaxListener);


		JPanel gridSatTransPanel = new JPanel();
		gridSatTransPanel.setLayout(new RiverLayout());
		gridSatTransPanel.add(new JLabel("Transparency"), "center");
		gridSatTransPanel.add(gridSatTransparency, "br center");

		//        JPanel satSmoothingPanel = new JPanel();
		//        satSmoothingPanel.setLayout(new RiverLayout());
		//        satSmoothingPanel.add(new JLabel("Smoothing"), "center");
		//        satSmoothingPanel.add(satSmoothing, "br center");
		//        JLabel satInfoLabel = new JLabel(new ImageIcon(MapSelector.class.getResource("/icons/question-mark.png")));
		//        satInfoLabel.setToolTipText(smoothingText);
		//        satInfoLabel.addMouseListener(mouseListener);
		//        satSmoothingPanel.add(satInfoLabel, "right");

		JPanel gridSatLegendPanel = new JPanel();
		gridSatLegendPanel.setLayout(new RiverLayout());
		gridSatLegendPanel.add(new JLabel("Legend"), "center");
		gridSatLegendPanel.add(gridSatLegend, "br center");
		//        gridSatLegendPanel.add(gridSatLegendEdit);


		jcbFlipGridColorTable.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				viewer.setGridSatelliteColorTable(satColorTableCombo.getSelectedItem().toString());
			}
		});



		jcomboGridMinMax.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String selectedItem = jcomboGridMinMax.getSelectedItem().toString();
				System.out.println("CHOSE: "+selectedItem);
				try {
					if (selectedItem.equals("Auto")) {
						jtfGridMinValue.setEnabled(false);
						jtfGridMaxValue.setEnabled(false);

						jtfGridMinValue.setText(WCTUtils.DECFMT_0D0000.format( viewer.getGridDatasetRaster().getGridMinValue() ));
						jtfGridMaxValue.setText(WCTUtils.DECFMT_0D0000.format( viewer.getGridDatasetRaster().getGridMaxValue() ));
						viewer.setGridSatelliteColorTable(satColorTableCombo.getSelectedItem().toString());

					}
					else if (selectedItem.equals("Center on Zero")) {
						jtfGridMinValue.setEnabled(false);
						jtfGridMaxValue.setEnabled(false);

						boolean isMaxTheAbsMax = Math.abs(viewer.getGridDatasetRaster().getGridMaxValue()) > Math.abs(viewer.getGridDatasetRaster().getGridMinValue());
						if (isMaxTheAbsMax) {
							jtfGridMinValue.setText(WCTUtils.DECFMT_0D0000.format( -1*viewer.getGridDatasetRaster().getGridMaxValue() ));
							jtfGridMaxValue.setText(WCTUtils.DECFMT_0D0000.format( viewer.getGridDatasetRaster().getGridMaxValue() ));
						}
						else {
							jtfGridMinValue.setText(WCTUtils.DECFMT_0D0000.format( -1*viewer.getGridDatasetRaster().getGridMinValue() ));
							jtfGridMaxValue.setText(WCTUtils.DECFMT_0D0000.format( viewer.getGridDatasetRaster().getGridMinValue() ));
						}
						viewer.setGridSatelliteColorTable(satColorTableCombo.getSelectedItem().toString());

					}
					else if (selectedItem.equals("Valid Range")) {
						jtfGridMinValue.setEnabled(false);
						jtfGridMaxValue.setEnabled(false);

						if (viewer.getGridDatasetRaster().hasValidRangeAttributes()) {                		
							jtfGridMinValue.setText(WCTUtils.DECFMT_0D0000.format( viewer.getGridDatasetRaster().getValidRangeMinValue() ));
							jtfGridMaxValue.setText(WCTUtils.DECFMT_0D0000.format( viewer.getGridDatasetRaster().getValidRangeMaxValue() ));
						}
						else {
							jcomboGridMinMax.setSelectedItem("Auto");
							JOptionPane.showMessageDialog(viewer, "The valid_range attributes are not present for this variable.", "General Warning", JOptionPane.WARNING_MESSAGE);
						}
						viewer.setGridSatelliteColorTable(satColorTableCombo.getSelectedItem().toString());

					}
					else if (selectedItem.equals("Custom")) {
						jtfGridMinValue.setEnabled(true);
						jtfGridMaxValue.setEnabled(true);
					}
					else {
						System.err.println("invalid selection: "+selectedItem);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});  


		jtfGridMinValue.setEnabled(false);
		jtfGridMaxValue.setEnabled(false);
		gridColorTablePanel.setLayout(new RiverLayout());
		gridColorTablePanel.add(new JLabel("Color Table: "), "left");
		gridColorTablePanel.add(gridColorTableCombo, "hfill");
		gridColorTablePanel.add(jcbFlipGridColorTable, "");
		gridColorTablePanel.add(jcomboGridMinMax, "br left");
		gridColorTablePanel.add(new JLabel("Min: "));
		gridColorTablePanel.add(jtfGridMinValue);
		gridColorTablePanel.add(new JLabel("Max: "));
		gridColorTablePanel.add(jtfGridMaxValue);

		//        gridColorTablePanel.add(new JLabel("Units: "), "br left");
		//        gridColorTablePanel.add(gridSatUnits, "left");



		satColorTablePanel.setLayout(new RiverLayout());
		satColorTablePanel.add(new JLabel("Color Table: "), "left");
		satColorTablePanel.add(satColorTableCombo, "hfill");

		gridSatellitePanel.add(jcbGridSatellite);
		gridSatellitePanel.add(gridSatTransPanel, "tab");
		//        gridSatellitePanel.add(satSmoothingPanel, "tab");
		gridSatellitePanel.add(gridSatLegendPanel, "tab");
		gridSatellitePanel.add(satColorTablePanel, "br left");

		jcbGridSatellite.setEnabled(false);
		gridSatTransparency.setEnabled(false);
		gridSatSmoothing.setEnabled(false);
		gridSatLegend.setEnabled(false);
		satColorTableCombo.setEnabled(false);

















		locationStationPanel.setLayout(new RiverLayout());

		jcbLocationStation = new JCheckBox("Visible", true);
		jcbLocationStation.setActionCommand("Location/Station");
		jcbLocationStation.addActionListener(new DataLayersListener());




		locationStationPanel.setEnabled(false);

		locationStationTransparency.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (locationStationTransparency.getSelectedItem().toString().trim().equalsIgnoreCase("Default")) {
					viewer.setLocationStationTransparency(LocationTimeseriesProperties.DEFAULT_ALPHA_PERCENT);
				}
				else {
					int percent = Integer.parseInt(((String)locationStationTransparency.getSelectedItem()).replaceAll("%","").trim());
					viewer.setLocationStationTransparency(255 - ((int)((percent/100.0)*255)));
				}
				viewer.setLocationStationColorTable(locationStationColorTableCombo.getSelectedItem().toString());
			}
		});
		locationStationTransparency.setEditable(true);



		jtfLocationStationMinValue.setEnabled(false);
		jtfLocationStationMaxValue.setEnabled(false);
		locationStationLegend.setSelectedItem("Medium");
		locationStationLegend.setEnabled(false);

		locationStationColorTableCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("CHOSE: "+locationStationColorTableCombo.getSelectedItem().toString());
				viewer.setLocationStationColorTable(locationStationColorTableCombo.getSelectedItem().toString());
			}
		});   

		KeyListener locationStationMinMaxListener = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					if (jtfLocationStationMinValue.getText().trim().length() > 0 &&
							jtfLocationStationMaxValue.getText().trim().length() > 0) {

						viewer.setLocationStationColorTable(locationStationColorTableCombo.getSelectedItem().toString());
					}
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
				}
			}
			@Override
			public void keyTyped(KeyEvent e) {
			}            
		};
		jtfLocationStationMinValue.addKeyListener(locationStationMinMaxListener);
		jtfLocationStationMaxValue.addKeyListener(locationStationMinMaxListener);


		JPanel locationStationTransPanel = new JPanel();
		locationStationTransPanel.setLayout(new RiverLayout());
		locationStationTransPanel.add(new JLabel("Transparency"), "center");
		locationStationTransPanel.add(locationStationTransparency, "br center");

		JPanel locationStationLegendPanel = new JPanel();
		locationStationLegendPanel.setLayout(new RiverLayout());
		locationStationLegendPanel.add(new JLabel("Legend"), "center");
		locationStationLegendPanel.add(locationStationLegend, "br center");


		jcbFlipLocationStationColorTable.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				viewer.setLocationStationColorTable(locationStationColorTableCombo.getSelectedItem().toString());
			}
		});

		locationStationLegend.addActionListener(new ActionListener() {
			//          @Override
			public void actionPerformed(ActionEvent e) {
				///  TODO: add legend handling stuff here
				if (locationStationLegend.getSelectedItem().toString().equals("Medium")) {
					viewer.setLocationStationLegendVisibility(true);
				}
				else {
					viewer.setLocationStationLegendVisibility(false);
				}
			}
		});


		jcomboLocationStationMinMax.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String selectedItem = jcomboLocationStationMinMax.getSelectedItem().toString();
				System.out.println("CHOSE: "+selectedItem);
				try {
					if (selectedItem.equals("Auto")) {
						jtfLocationStationMinValue.setEnabled(false);
						jtfLocationStationMaxValue.setEnabled(false);

						jtfLocationStationMinValue.setText(WCTUtils.DECFMT_0D0000.format( viewer.getStationPointProperties().getLastProcessedMinValue() ));
						jtfLocationStationMaxValue.setText(WCTUtils.DECFMT_0D0000.format( viewer.getStationPointProperties().getLastProcessedMaxValue() ));
						viewer.setLocationStationColorTable(locationStationColorTableCombo.getSelectedItem().toString());

					}
					else if (selectedItem.equals("Valid Range")) {
						jtfLocationStationMinValue.setEnabled(false);
						jtfLocationStationMaxValue.setEnabled(false);

						if (viewer.getStationPointProperties().hasValidRangeAttributes()) {                		
							jtfLocationStationMinValue.setText(WCTUtils.DECFMT_0D0000.format( viewer.getStationPointProperties().getLastProcessedValidRangeMinValue() ));
							jtfLocationStationMaxValue.setText(WCTUtils.DECFMT_0D0000.format( viewer.getStationPointProperties().getLastProcessedValidRangeMaxValue() ));
						}
						else {
							jcomboLocationStationMinMax.setSelectedItem("Auto");
							JOptionPane.showMessageDialog(viewer, "The valid_range attributes are not present for this variable.", "General Warning", JOptionPane.WARNING_MESSAGE);
						}
						viewer.setLocationStationColorTable(locationStationColorTableCombo.getSelectedItem().toString());

					}
					else if (selectedItem.equals("Custom")) {
						jtfLocationStationMinValue.setEnabled(true);
						jtfLocationStationMaxValue.setEnabled(true);
					}
					else {
						System.err.println("invalid selection: "+selectedItem);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});  


		// keep 'un'enabled until data is loaded
		jcbLocationStation.setSelected(false);
		jcbLocationStation.setEnabled(false);
		locationStationTransparency.setEnabled(false);
		locationStationLegend.setEnabled(false);
		locationStationColorTableCombo.setEnabled(false);
		jcomboLocationStationMinMax.setEnabled(false);
		jcbFlipLocationStationColorTable.setEnabled(false);
		jtfLocationStationMaxValue.setEnabled(false);
		jtfLocationStationMinValue.setEnabled(false);

		locationStationColorTablePanel.setLayout(new RiverLayout());
		locationStationColorTablePanel.add(new JLabel("Color Table: "), "left");
		locationStationColorTablePanel.add(locationStationColorTableCombo, "hfill");
		locationStationColorTablePanel.add(jcbFlipLocationStationColorTable, "");
		locationStationColorTablePanel.add(jcomboLocationStationMinMax, "br left");
		locationStationColorTablePanel.add(new JLabel("Min: "));
		locationStationColorTablePanel.add(jtfLocationStationMinValue);
		locationStationColorTablePanel.add(new JLabel("Max: "));
		locationStationColorTablePanel.add(jtfLocationStationMaxValue);

		//        gridColorTablePanel.add(new JLabel("Units: "), "br left");
		//        gridColorTablePanel.add(gridSatUnits, "left");




		locationStationPanel.add(jcbLocationStation);
		locationStationPanel.add(locationStationTransPanel, "tab");
		locationStationPanel.add(locationStationLegendPanel, "tab");
		locationStationPanel.add(locationStationColorTablePanel, "br left");
























		//NOT USED?        
		//        dataLayersPanel.add(radarPanel, "hfill");
		//        dataLayersPanel.add(gridSatellitePanel, "br hfill");
		//        dataLayersPanel.add(locationStationPanel, "br hfill");
		//        dataLayersPanel.add(smoothingDisclaimer, "br");





		JButton snapshotLayerButton = new JButton("Add Snapshot Layer");
		snapshotLayerButton.addActionListener(new SnapshotActionListener());

		JButton clearButton = new JButton("Clear Active Layer");
		clearButton.addActionListener(new ClearActiveLayerActionListener());


		// duplicate buttons for snapshot panel
		JButton snapshotLayerButton2 = new JButton("Add Snapshot Layer");
		snapshotLayerButton2.addActionListener(new SnapshotActionListener());

		JButton clearButton2 = new JButton("Clear Active Layer");
		clearButton2.addActionListener(new ClearActiveLayerActionListener());

		JButton clearSnapshotsButton = new JButton("Clear Snapshots");
		clearSnapshotsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				while (viewer.getSnapshotLayers().size() > 0) {
					viewer.removeSnapshotLayer(viewer.getSnapshotLayers().get(0));
				}
				snapshotLayersStack.removeAllBoxes();
				tabPane.setTitleAt(1, "Snapshot Layers (0)");

				//            	List<SnapshotLayer> layers = viewer.getSnapshotLayers();
				//            	for (SnapshotLayer l : layers) {
				//            		viewer.removeSnapshotLayer(l);
				//
				//            	}
				snapshotPanel.revalidate();
			}
		});

		JXHyperlink filterLink = new JXHyperlink();
		filterLink.setText("Data Filter");
		filterLink.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				viewer.getFilterGUI().setVisible(true);
				viewer.getFilterGUI().setLocation(getX()+getWidth()-20, getY()+38);
				viewer.getFilterGUI().pack();
			}
		});




		dataLayersScrollPane.setBorder(WCTUiUtils.myBorder(0, 0, 2, 0));        
		dataPanel.setLayout(new RiverLayout());
		dataPanel.add(dataLayersScrollPane, "hfill vfill");
		dataPanel.add(snapshotLayerButton, "br left");
		dataPanel.add(clearButton, "left");
		dataPanel.add(new JPanel(), "left hfill");
		dataPanel.add(filterLink, "right");


		snapshotLayersScrollPane.setBorder(WCTUiUtils.myBorder(0, 0, 2, 0));
		snapshotPanel.setLayout(new RiverLayout());
		snapshotPanel.add(snapshotLayersScrollPane, "hfill vfill");
		//        snapshotPanel.add(snapshotLayerButton2, "br left");
		snapshotPanel.add(new JLabel(
				"<html>NOTE: Snapshot Layers display underneath or below the 'Active' Data Layers.<br>"
						+ "Setting transparency or clearing the 'Active' Data Layer may be <br>"
						+ "necessary to see data snapshots.</html>"), "br left");
		snapshotPanel.add(clearSnapshotsButton, "br left");
		snapshotPanel.add(clearButton2, "left");












		dataLayersStack.addBox("Radar (Active)", radarPanel);
		dataLayersStack.addBox("Grid/Satellite (Active)", gridSatellitePanel);
		dataLayersStack.addBox("Location/Station (Active)", locationStationPanel);

		snapshotLayersStack.addBoxListener(new BoxListener() {
			@Override
			public void addedBox(BoxEvent e) {
				tabPane.setTitleAt(1, "Snapshot Layers ("+snapshotLayersStack.getBoxNameList().size()+")");
			}
			@Override
			public void removedBox(BoxEvent e) {
				//                System.out.println("removed box");
				List<SnapshotLayer> layers = viewer.getSnapshotLayers();
				for (SnapshotLayer l : layers) {
					System.out.println("snapshot layer name: "+l.getName()+" , event - box title: "+e.getTitle());
					if (l.getName().equals(e.getTitle())) {
						System.out.println("removed box layer: "+l.getName());
						viewer.removeSnapshotLayer(l);
						snapshotPanel.revalidate();
						tabPane.setTitleAt(1, "Snapshot Layers ("+snapshotLayersStack.getBoxNameList().size()+")");
						return;
					}
				}
			}
		});








		tabPane = new JTabbedPane();      

		tabPane.add(dataPanel, "Data Layers");
		tabPane.add(snapshotPanel, "Snapshot Layers");
		tabPane.add(new JScrollPane(overlayPanel), "Overlay Selector");
		tabPane.add(wmsPanel, "Background Maps (WMS)");

		getContentPane().add(tabPane);











		// A global way to add a key listener
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			public boolean dispatchKeyEvent(KeyEvent e) {
				//                System.out.println(e.getKeyCode()+"  ESCAPE=" + KeyEvent.VK_ESCAPE+" ENTER="+KeyEvent.VK_ENTER+" D="+KeyEvent.VK_D);
				//                System.out.println(e.getModifiers()+"  CTRL=" + KeyEvent.CTRL_DOWN_MASK);
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {                
					viewer.screenCapture();
				}
				return false;
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


	} // END METHOD createGUI()




	public void addSnapshotLayer(final SnapshotLayer snapshotLayer) {
		snapshotLayerCounter++;
		snapshotLayersStack.addBox(snapshotLayer.getName(), createSnapshotLayerPanel(snapshotLayer), true, 0);
		snapshotPanel.validate();
	}

	public int getSnapshotLayerCounter() {
		return snapshotLayerCounter;
	}

	private JPanel createSnapshotLayerPanel(final SnapshotLayer snapshotLayer) {
		return new SnapshotPanel(viewer, snapshotLayersStack, snapshotLayer);
	}



	public boolean isGridValidRangeMinMaxSelected() {
		return jcomboGridMinMax.getSelectedItem().toString().equals("Valid Range");
	}
	public boolean isLocationStationValidRangeMinMaxSelected() {
		return jcomboLocationStationMinMax.getSelectedItem().toString().equals("Valid Range");
	}


	public void setGridColorTableMinMaxValue(double minValue, double maxValue) {
		if (Math.abs(minValue) > 10000 || 
				( Math.abs(minValue) > 0 && Math.abs(minValue) < 0.01)) {
			jtfGridMinValue.setText(WCTUtils.DECFMT_SCI.format(minValue));
		}
		else {
			jtfGridMinValue.setText(WCTUtils.DECFMT_0D0000.format(minValue));
		}
		if (Math.abs(maxValue) > 10000 || 
				( Math.abs(maxValue) > 0 && Math.abs(maxValue) < 0.01)) {
			jtfGridMaxValue.setText(WCTUtils.DECFMT_SCI.format(maxValue));
		}
		else {
			jtfGridMaxValue.setText(WCTUtils.DECFMT_0D0000.format(maxValue));
		}
	}
	public void setLocationStationColorTableMinMaxValue(double minValue, double maxValue) {
		if (Math.abs(minValue) > 10000 || 
				( Math.abs(minValue) > 0 && Math.abs(minValue) < 0.01)) {
			jtfLocationStationMinValue.setText(WCTUtils.DECFMT_SCI.format(minValue));
		}
		else {
			jtfLocationStationMinValue.setText(WCTUtils.DECFMT_0D0000.format(minValue));
		}
		if (Math.abs(maxValue) > 10000 || 
				( Math.abs(maxValue) > 0 && Math.abs(maxValue) < 0.01)) {
			jtfLocationStationMaxValue.setText(WCTUtils.DECFMT_SCI.format(maxValue));
		}
		else {
			jtfLocationStationMaxValue.setText(WCTUtils.DECFMT_0D0000.format(maxValue));
		}
	}
	public boolean isGridAutoMinMaxSelected() {
		return jcomboGridMinMax.getSelectedItem().toString().equals("Auto");
	}

	public boolean isLocationStationAutoMinMaxSelected() {
		return jcomboLocationStationMinMax.getSelectedItem().toString().equals("Auto");
	}
	public void setGridAutoMinMax(boolean isAutoMinMax) {
		if (isAutoMinMax) {
			jcomboGridMinMax.setSelectedItem("Auto");
		}
		else {
			jcomboGridMinMax.setSelectedItem("Custom");
		}
	}

	public double getGridColorTableMinValue() {
		try {
			return Double.parseDouble(jtfGridMinValue.getText());
		} catch (Exception e) {
			return 0;
		}
	}
	public double getGridColorTableMaxValue() {
		try {
			return Double.parseDouble(jtfGridMaxValue.getText());
		} catch (Exception e) {
			return 0;
		}
	}
	public double getLocationStationColorTableMinValue() {
		try {
			return Double.parseDouble(jtfLocationStationMinValue.getText());
		} catch (Exception e) {
			return 0;
		}
	}
	public double getLocationStationColorTableMaxValue() {
		try {
			return Double.parseDouble(jtfLocationStationMaxValue.getText());
		} catch (Exception e) {
			return 0;
		}
	}
	public String getGridColorTableName() {
		return gridColorTableCombo.getSelectedItem().toString();
	}

	public boolean isGridColorTableFlipped() {
		return jcbFlipGridColorTable.isSelected();
	}

	public void setGridColorTableFlipped(boolean isFlipped) {
		jcbFlipGridColorTable.setSelected(isFlipped);
	}

	public boolean isLocationStationColorTableFlipped() {
		return jcbFlipLocationStationColorTable.isSelected();
	}

	public void setLocationColorTableFlipped(boolean isFlipped) {
		jcbFlipLocationStationColorTable.setSelected(isFlipped);
	}



	public void setRadarLegend(String type) {
		if (type.trim().equalsIgnoreCase("None")) {
			radLegend.setSelectedItem("None");
		}
		else if (type.trim().equalsIgnoreCase("Small")) {
			radLegend.setSelectedItem("Small");
		}
		else if (type.trim().equalsIgnoreCase("Medium")) {
			radLegend.setSelectedItem("Medium");
		}
		else if (type.trim().equalsIgnoreCase("Large")) {
			radLegend.setSelectedItem("Large");
		}
	}

	public String getRadarLegendType() {
		return radLegend.getSelectedItem().toString();
	}

	public void setGridSatelliteLegend(String type) {
		if (type.trim().equalsIgnoreCase("None")) {
			gridSatLegend.setSelectedItem("None");
		}
		else if (type.trim().equalsIgnoreCase("Small")) {
			gridSatLegend.setSelectedItem("Small");
		}
		else if (type.trim().equalsIgnoreCase("Medium")) {
			gridSatLegend.setSelectedItem("Medium");
		}
		else if (type.trim().equalsIgnoreCase("Large")) {
			gridSatLegend.setSelectedItem("Large");
		}
	}

	public String getGridSatelliteLegendType() {
		return gridSatLegend.getSelectedItem().toString();
	}


	public int getGridSatelliteTransparency() {
		if (gridSatTransparency.getSelectedItem().toString().trim().equalsIgnoreCase("Default")) {
			return -1;
		}
		else {
			return Integer.parseInt(gridSatTransparency.getSelectedItem().toString().replaceAll("%", "").trim());
		}
	}

	public void setGridSatelliteTransparency(int transparency) {
		if (transparency == -1) {
			gridSatTransparency.setSelectedItem("Default");
		}
		else {
			gridSatTransparency.setSelectedItem(" "+transparency+"%");
		}
	}

	public int getRadarTransparency() {
		if (radTransparency.getSelectedItem().toString().trim().equalsIgnoreCase("Default")) {
			return -1;
		}
		else {
			return Integer.parseInt(radTransparency.getSelectedItem().toString().replaceAll("%", "").trim());
		}
	}

	/**
	 * If == -1, then set to 'Default'
	 * @param transparencyString
	 */
	public void setRadarTransparency(int transparency) {
		if (transparency == -1) {
			radTransparency.setSelectedItem("Default");
		}
		else {
			radTransparency.setSelectedItem(" "+transparency+"%");
		}
	}

	public int getLocationStationTransparency() {
		if (locationStationTransparency.getSelectedItem().toString().trim().equalsIgnoreCase("Default")) {
			return -1;
		}
		else {
			return Integer.parseInt(locationStationTransparency.getSelectedItem().toString().replaceAll("%", "").trim());
		}
	}

	/**
	 * If == -1, then set to 'Default'
	 * @param transparencyString
	 */
	public void setLocationStationTransparency(int transparency) {
		if (transparency == -1) {
			locationStationTransparency.setSelectedItem("Default");
		}
		else {
			locationStationTransparency.setSelectedItem(" "+transparency+"%");
		}
	}

	public String getLocationStationLegendType() {
		return locationStationLegend.getSelectedItem().toString();
	}


	public int getRadarSmoothingFactor() {
		return Integer.parseInt(radSmoothing.getValue().toString());
	}

	public void setRadarSmoothingFactor(int smoothingFactor) {
		radSmoothing.setValue(new Integer(smoothingFactor));
	}

	public void refreshAvailableUnitConversions(String nativeUnits) 
			throws UnitDBException, IllegalAccessException, InstantiationException {

		if (nativeUnits == null) {
			return;
		}

		String selectedValue = gridSatUnits.getSelectedItem().toString();

		UnitDB unitDB = UnitDBManager.instance();
		Iterator iter = unitDB.getIterator();
		Vector<String> compatibleUnits = new Vector<String>();
		compatibleUnits.add("Native -- no conversion ("+viewer.getGridDatasetRaster().getUnits()+")");

		// try by name and then by symbol
		Unit natUnit = unitDB.getByName(nativeUnits);
		if (natUnit == null) {
			natUnit = unitDB.getBySymbol(nativeUnits);
		}

		if (natUnit == null) {
			return;
		}

		// store native and derived unit
		System.out.println("-------------------------");
		String derivedUnitName = natUnit.getDerivedUnit().getName();
		//		WCTProperties.setWCTProperty(derivedUnitName, natUnit.getName());

		System.out.println(natUnit.getDerivedUnit().getName());
		//		System.out.println(natUnit.getDerivedUnit().getDimension().get);
		//		System.out.println(natUnit.getDerivedUnit().getQuantityDimension().);
		System.out.println("-------------------------");


		while (iter.hasNext() && natUnit != null) {
			Unit unit = (Unit)(iter.next());
			if (natUnit.isCompatible(unit)) {
				System.out.println(unit);
				//				compatibleUnits.add(unit.getName());
				compatibleUnits.add(unit.getName());
			}
		}

		gridSatUnits.setModel(new DefaultComboBoxModel<String>(compatibleUnits));

		ActionListener[] al = gridSatUnits.getActionListeners();
		for (ActionListener a : al) {
			gridSatUnits.removeActionListener(a);
		}

		//		gridSatUnits.setSelectedItem(selectedValue);
		String preferenceUnit = WCTProperties.getWCTProperty(natUnit.getDerivedUnit().getName());
		if (preferenceUnit != null) {
			gridSatUnits.setSelectedItem(preferenceUnit);
		}

		for (ActionListener a : al) {
			gridSatUnits.addActionListener(a);
		}

	}













	public void setSelectedTab(int index) {
		tabPane.setSelectedIndex(index);
	}




	private JPanel makeColumnLabels() {
		JPanel jpanel = new JPanel();
		jpanel.setLayout(new GridLayout(1, 2));
		JPanel apanel = new JPanel();
		apanel.setLayout(new GridLayout(1, 3));

		apanel.add(new JLabel("Color", JLabel.CENTER));
		apanel.add(new JLabel("Size", JLabel.CENTER));
		apanel.add(new JLabel("Label", JLabel.CENTER));

		jpanel.add(new JLabel("Layer", JLabel.CENTER));
		jpanel.add(apanel);

		return jpanel;

	} // END METHOD makeMapPanel(int index, String Default)

	private JPanel makeLocalColumnLabels() {
		JPanel jpanel = new JPanel();
		jpanel.setLayout(new GridLayout(1, 2));
		JPanel apanel = new JPanel();
		apanel.setLayout(new GridLayout(1, 3));

		apanel.add(new JLabel("Color", JLabel.CENTER));
		apanel.add(new JLabel("Size", JLabel.CENTER));
		apanel.add(new JLabel("Remove", JLabel.CENTER));

		jpanel.add(new JLabel("Local Map Layer", JLabel.CENTER));
		jpanel.add(apanel);
		jpanel.setForeground(Color.blue);

		return jpanel;

	} // END METHOD makeMapPanel(int index, String Default)



	private void makeMapPanel(int index, String name, boolean status) {

		layersPanel.set(index, new JPanel());
		layersPanel.get(index).setLayout(new GridLayout(1, 2));
		attPanel.set(index, new JPanel());
		attPanel.get(index).setLayout(new GridLayout(1, 3));

		// Create  On/Off Button
		jcbVisible.set(index, new JCheckBox(name, status));
		jcbVisible.get(index).addActionListener(this);
		jcbVisible.get(index).setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		jcbVisible.get(index).setFont(((JCheckBox)jcbVisible.get(index)).getFont().deriveFont(Font.BOLD));


		//      System.out.println("MAKE MAP PANEL: index="+index+" name="+name+" status="+status+" color="+viewer.getThemeFillColor(index));      

		// Create Change Color Button
		JButton button = new JButton("");
		jbColor.set(index, button);
		jbColor.get(index).addActionListener(this);

		// Create Change Style
		String[] options = new String[5];
		options[0] = "1";
		options[1] = "2";
		options[2] = "3";
		options[3] = "4";
		options[4] = "5";
		styleOptions.set(index, new JComboBox<String>(options));
		styleOptions.get(index).setSelectedIndex(viewer.getLayerLineWidth(index) - 1);
		styleOptions.get(index).addActionListener(this);
		styleOptions.get(index).setBackground(Color.white);
		styleOptions.get(index).setPreferredSize(new Dimension(20, 20));

		// Create  Label On/Off Checkbox
		jcbLabel.set(index, new JCheckBox("", false));
		jcbLabel.get(index).addActionListener(this);
		jcbLabel.get(index).setHorizontalAlignment(SwingConstants.CENTER);


		// Set background colors      
		if (index <= 1) {
			Color c = viewer.getLayerFillColor(index);
			jbColor.get(index).setBackground(c);
			jcbLabel.get(index).setBackground(c);
		}
		else {
			Color c = viewer.getLayerLineColor(index);
			((JButton)jbColor.get(index)).setBackground(c);
			//            ((JCheckBox)jcbVisible.get(index)).setBackground(c);
			((JCheckBox)jcbLabel.get(index)).setBackground(c);
			//            ((JCheckBox)jcbVisible.get(index)).setForeground(invertColor(c));
		}
		((JButton)jbColor.get(index)).setPreferredSize(new Dimension(20, 20));


		// Disable if not applicable
		if (index <= 1) {
			((JComboBox<String>)styleOptions.get(index)).setEnabled(false);
			((JCheckBox)jcbLabel.get(index)).setEnabled(false);
		}

		// Disable if theme did not load
		if (! viewer.getLayerStatus(index)) {
			((JCheckBox)jcbVisible.get(index)).setEnabled(false);
			((JButton)jbColor.get(index)).setEnabled(false);
			((JButton)jbColor.get(index)).setBackground(null);
			((JComboBox<String>)styleOptions.get(index)).setEnabled(false);
			((JComboBox<String>)styleOptions.get(index)).setBackground(null);
			((JCheckBox)jcbLabel.get(index)).setEnabled(false);
		}

		attPanel.get(index).add((JButton)jbColor.get(index));
		attPanel.get(index).add((JComboBox<String>)styleOptions.get(index));
		attPanel.get(index).add((JCheckBox)jcbLabel.get(index));

		layersPanel.get(index).add((JCheckBox)jcbVisible.get(index));
		layersPanel.get(index).setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.GRAY));

		layersPanel.get(index).add(attPanel.get(index));

		//layersPanel[index].setPreferredSize(new Dimension(200,40));

		return ;
		//return layersPanel.get(index));

	} // END METHOD makeMapPanel()


	private JPanel makeBackgroundPanel() {

		jbBackground = new JButton();
		jcbBogus = new JCheckBox("", true);
		//        jcbBogus.setBackground(viewer.getBackgroundColor());
		jcbBogus.setEnabled(false);
		jcbBogus.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		JComboBox<String> jcomboBogus = new JComboBox<String>(new String[] {"1"});
		jcomboBogus.setEnabled(false);
		//        JCheckBox jcbBogus1 = new JCheckBox("", false);
		//        jcbBogus1.setBackground(viewer.getBackgroundColor());
		//        jcbBogus1.setEnabled(false);

		JPanel jpanel = new JPanel();
		jpanel.setLayout(new GridLayout(1, 2));
		apanel.setLayout(new GridLayout(1, 3));

		//apanel.add(jcbBogus1, JLabel.CENTER);
		//apanel.add(jcomboBogus, JLabel.CENTER);
		apanel.add(jbBackground, JLabel.CENTER);
		apanel.add(new JLabel("", JLabel.CENTER));
		apanel.add(new JLabel("", JLabel.CENTER));
		//        apanel.setBackground(viewer.getBackgroundColor());

		backgroundLabel = new JLabel("Background");
		backgroundLabel.setFont(backgroundLabel.getFont().deriveFont(Font.BOLD));
		//        backgroundLabel.setBackground(viewer.getBackgroundColor());
		//        backgroundLabel.setForeground(invertColor(viewer.getBackgroundColor()));


		bpanel.setLayout(new BoxLayout(bpanel, BoxLayout.X_AXIS));
		bpanel.add(jcbBogus);
		bpanel.add(backgroundLabel);
		//        bpanel.setBackground(viewer.getBackgroundColor());
		//      jpanel.add(new JLabel("Background"));
		jpanel.add(bpanel);
		jpanel.add(apanel);

		jpanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));


		// Create Change Color Button
		jbBackground.addActionListener(this);
		jbBackground.setBackground(viewer.getMapBackgroundColor());
		jbBackground.setPreferredSize(new Dimension(20, 20));

		return jpanel;

	} // END METHOD makeBackgroundPanel()



	public void addLocalLayerPanel(String name) {
		sizeVectors(++numLayers);
		if (numLayers == (WCTViewer.NUM_LAYERS + 1)) // only add once
			jnxPanel.add(makeLocalColumnLabels());
		makeMapPanel((numLayers - 1), name, true); // 0-based
		jnxPanel.add(layersPanel.get(numLayers - 1));
		return ;
	} // END METHOD addLocalThemePanel()

	public void removeLocalLayerPanel(int index) {

		jnxPanel.remove((JPanel)layersPanel.get(index));
		jnxPanel.repaint();
		pack();
		return ;
	} // END METHOD removeLocalThemePanel()


	public String getLayerName(int index) {
		try {
			return ((JCheckBox)jcbVisible.get(index)).getText();
		} catch (Exception e) {
			return null;
		}
	}

	public Color getLayerColor(int index) {
		try {
			return ((JButton)jbColor.get(index)).getBackground();
		} catch (Exception e) {
			return null;
		}
	} // END METHOD getThemeColor()

	public void setLayerColor(int index, Color c) {
		try {
			((JButton)jbColor.get(index)).setBackground(c);
			((JCheckBox)jcbLabel.get(index)).setBackground(c);
			processLayerColorChange(index, c);

			//    		if (index == WCTViewer.STATES || index == WCTViewer.COUNTRIES || 
			//    				index == WCTViewer.COUNTRIES_USA) {
			//    			System.out.println("SETTING STATES OR COUNTRIES TO: "+c);
			//    			viewer.setLayerFillColor(index, c);
			//    		}
			//    		else {
			//    			viewer.setLayerLineColor(index, c);
			//    		}
		} catch (Exception e) {
		}      
	} // END METHOD setThemeColor()

	public int getLayerLineWidth(int index) {
		try {
			return ((JComboBox<String>)styleOptions.get(index)).getSelectedIndex() + 1;
		} catch (Exception e) {
			return -1;
		}
	} // END METHOD getThemeLineWidth()

	public void setLayerLineWidth(int index, int newWidth) {
		try {
			((JComboBox<String>)styleOptions.get(index)).setSelectedIndex(newWidth-1);
			actionPerformed(new ActionEvent(((JComboBox<String>)styleOptions.get(index)), 0, "Manual Set Layer Line Width"));
		} catch (Exception e) {
		}
	} // END METHOD setThemeLineWidth()

	public boolean getLayerVisibility(int index) {
		try {
			return ((JCheckBox)jcbVisible.get(index)).isSelected();
		} catch (Exception e) {
			return false;
		}
	} // END METHOD isThemeVisible()

	public synchronized void setLayerVisibility(int index, boolean visible) {
		try {
			// wait a tad, in case multiple layers are drawn so the last one can complete
			if (visible) Thread.sleep(300L);
			jcbVisible.get(index).setSelected(visible);
			actionPerformed(new ActionEvent(jcbVisible.get(index), 0, "Manual Set Layer Visibility"));
		} catch (Exception e) {
		}
	} // END METHOD setIsThemeVisible()

	public boolean getLabelVisibility(int index) {
		try {
			return ((JCheckBox)jcbLabel.get(index)).isSelected();
		} catch (Exception e) {
			return false;
		}
	} // END METHOD getLabelVisibility()

	public synchronized void setLabelVisibility(int index, boolean visible) {
		try {
			jcbLabel.get(index).setSelected(visible);
			actionPerformed(new ActionEvent(jcbLabel.get(index), 0, "Manual Set Label Visibility"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // END METHOD setLabelVisibility()

	public Color getMapBackgroundColor() {
		return jbBackground.getBackground();
	}

	public void setMapBackgroundColor(Color c) {
		jbBackground.setBackground(c);
		viewer.setMapBackgroundColor(c);
	}



	public WMSPanel getWMSPanel() {
		return wmsPanel;
	}

	public void setDataLayersOnTop(boolean isOnTop) {
		jcbDataLayersOnTop.setSelected(isOnTop);
		if (jcbDataLayersOnTop.isSelected()) {
			viewer.getRadarRenderedGridCoverage().setZOrder(400+0.9f);
			viewer.getGridSatelliteRenderedGridCoverage().setZOrder(400+0.8f);
		}
		else {
			viewer.getRadarRenderedGridCoverage().setZOrder(1+0.9f);
			viewer.getGridSatelliteRenderedGridCoverage().setZOrder(1+0.8f);
		}
	}

	public boolean isDataLayersOnTop() {
		return jcbDataLayersOnTop.isSelected();
	}

	// Implementation of ActionListener interface.
	public void actionPerformed(ActionEvent event) {

		Object source = event.getSource();
		if (source == jbLoadShapefile) {
			loadShapefile();
		}
		else if (source == jcbDataLayersOnTop) {
			setDataLayersOnTop(jcbDataLayersOnTop.isSelected());
		}
		else if (source == jbBackground) {
			Color newColor = JColorChooser.showDialog(LayerSelector.this,
					"Choose Map Layer Color",
					jbBackground.getBackground());
			if (newColor != null) {
				jbBackground.setBackground(newColor);
				viewer.setMapBackgroundColor(newColor);
			}
			return;
		}


		for (int n = 0; n < numLayers; n++) {
			//for (int n=0; n<5; n++) {
			if (source == jcbVisible.get(n)) {


				if (jcbVisible.get(n).isSelected()) {
					viewer.setLayerVisibility(n, true);
					if (jcbLabel.get(n).isSelected()) {
						viewer.setLabelVisibility(n, true);
					}
				} else {
					viewer.setLayerVisibility(n, false);
					viewer.setLabelVisibility(n, false);
				}
				n = numLayers;
			} // END if
			// Color changed
			else if (source == jbColor.get(n)) {
				Color newColor = JColorChooser.showDialog(LayerSelector.this,
						"Choose Map Layer Color",
						((JButton)jbColor.get(n)).getBackground());
				if (newColor != null) {

					processLayerColorChange(n, newColor);
				}
				n = numLayers;
			} // END if
			// Size changed
			else if (source == styleOptions.get(n)) {
				if (n == WCTViewer.STATES || n == WCTViewer.COUNTRIES) {
					//                    viewer.setLayerLineWidth(n, ((JComboBox<String>)styleOptions.get(n)).getSelectedIndex() + 1 );
				}
				else {
					viewer.setLayerLineWidth(n, ((JComboBox<String>)styleOptions.get(n)).getSelectedIndex() + 1 );
				}

				n = numLayers;
			} // END if
			// Label/Remove Checkbox
			else if (source == jcbLabel.get(n)) {
				// Label things
				if (n < WCTViewer.NUM_LAYERS) {
					if (((JCheckBox)jcbLabel.get(n)).isSelected() &&
							((JCheckBox)jcbVisible.get(n)).isSelected()) {
						viewer.setLabelVisibility(n, true);
					} else {
						viewer.setLabelVisibility(n, false);
					}
				}
				// Remove local layer
				else {
					// Remove from view
					viewer.removeLayer(n);
					// Remove panel from this frame
					removeLocalLayerPanel(n);
				}
				n = numLayers;
			} // END if



		} // END for



	} // actionPerformed






	private void processLayerColorChange(int n, Color newColor) {

		final int fn = n;
		final Color fnewColor = newColor;
		gov.noaa.ncdc.common.SwingWorker worker = new gov.noaa.ncdc.common.SwingWorker() {
			public Object construct() {


				//                ((JCheckBox)jcbVisible.get(fn)).setForeground(invertColor(fnewColor));
				//                ((JCheckBox)jcbVisible.get(fn)).setBackground(fnewColor);
				((JButton)jbColor.get(fn)).setBackground(fnewColor);
				((JCheckBox)jcbLabel.get(fn)).setBackground(fnewColor);

				if (fn == WCTViewer.STATES || fn == WCTViewer.COUNTRIES) {
					viewer.setLayerFillAndLineColor(fn, fnewColor, fnewColor);
				}
				else if (viewer.getLayerShapeType(fn) == SimpleShapefileLayer.POINT ||
						viewer.getLayerShapeType(fn) == SimpleShapefileLayer.MULTIPOINT ) {

					viewer.setLayerFillAndLineColor(fn, fnewColor, fnewColor);
				}
				else {
					viewer.setLayerLineColor(fn, fnewColor);
				}

				return "DONE";
			}
		};
		worker.start();                  
	}



	/**
	 * Opens dialog for adding shapefile to NexradIAViewer
	 */
	public void loadShapefile() {
		// Set up File Chooser
		if (lastFolder == null) {
			//lastFolder = new File(System.getProperty("user.home"));
			String jnxprop = WCTProperties.getWCTProperty("local_overlay_dir");
			if (jnxprop != null) {
				lastFolder = new File(jnxprop);
			}
			else {
				lastFolder = new File(System.getProperty("user.home"));
			}
		}

		JFileChooser fc = new JFileChooser(lastFolder);

		FileFilter shpFilter = new OpenFileFilter("shp", true, "ESRI Shapefiles");
		fc.addChoosableFileFilter(shpFilter);
		//fc.addChoosableFileFilter(new OpenFileFilter("zip", true, "ESRI Shapefiles ZIP Archive"));
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(shpFilter);

		int returnVal = fc.showOpenDialog(jbLoadShapefile);
		File file = null;
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			lastFolder = file.getParentFile();
			WCTProperties.setWCTProperty("local_overlay_dir", lastFolder.toString());
			//This is where a real application would open the file.
			System.out.println("Opening: " + file.getName() + ".");

			// Check for matching .prj projection file
			String prj = "GEOGCS[\"GCS_North_American_1983\"," +
					"DATUM[\"D_North_American_1983\"," +
					"SPHEROID[\"GRS_1980\",6378137,298.257222101]]," +
					"PRIMEM[\"Greenwich\",0]," +
					"UNIT[\"Degree\",0.0174532925199433]]";

			String prj2 = "GEOGCS[\"GCS_North_American_1983\"," +
					"DATUM[\"D_North_American_1983\"," +
					"SPHEROID[\"GRS_1980\",6378137.0,298.257222101]]," +
					"PRIMEM[\"Greenwich\",0.0]," +
					"UNIT[\"Degree\",0.0174532925199433]]";

			String prj3 = "GEOGCS[\"GCS_WGS_1984\"," +
					"DATUM[\"D_WGS_1984\"," +
					"SPHEROID[\"WGS_1984\",6378137,298.257223563]]," +
					"PRIMEM[\"Greenwich\",0.0]," +
					"UNIT[\"Degree\",0.0174532925199433]]";

			String prj4 = "GEOGCS[\"GCS_WGS_1984\"," +
					"DATUM[\"D_WGS_1984\"," +
					"SPHEROID[\"WGS_1984\",6378137.0,298.257223563]]," +
					"PRIMEM[\"Greenwich\",0.0]," +
					"UNIT[\"Degree\",0.0174532925199433]]";

			String prj5 = "GEOGCS[\"GCS_WGS_1984\","+
					"DATUM[\"D_WGS_1984\","+
					"SPHEROID[\"WGS_1984\",6378137,298.257223563]],"+
					"PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.0174532925199433]]";

			System.out.println(prj3);

			String prjfile = file.toString();
			prjfile = prjfile.substring(0, prjfile.length() - 4);
			int choice = JOptionPane.YES_OPTION;
			BufferedReader bw = null;
			try {
				bw = new BufferedReader(new FileReader(prjfile + ".prj"));
				//if (! (bw.readLine()).equals(prj)) {
				String str = bw.readLine();
				if (! ((str.trim()).equals(prj) || (str.trim()).equals(prj2) || (str.trim()).equals(prj3) 
						|| (str.trim()).equals(prj4) || (str.trim()).equals(prj5))) {

					String message = "The projection file \n" +
							"<html><font color=red>" + prjfile + ".prj</font></html>\n" +
							"does not match the required projection.\n" +
							"The data will NOT be correctly geolocated.\n\n" +
							"Required projection parameters:\n" +
							"GEOGCS[\"GCS_WGS_1984\",\n" +
							"DATUM[\"D_WGS_1984\",\n" +
							"SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],\n" +
							"PRIMEM[\"Greenwich\",0.0],\n" +
							"UNIT[\"Degree\",0.0174532925199433]]\n\n"+                        
							"--OR--\n\n"+
							"GEOGCS[\"GCS_North_American_1983\",\n" +
							"DATUM[\"D_North_American_1983\",\n" +
							"SPHEROID[\"GRS_1980\",6378137,298.257222101]],\n" +
							"PRIMEM[\"Greenwich\",0],\n" +
							"UNIT[\"Degree\",0.0174532925199433]]\n\n" +
							"The data may be INCORRECTLY geolocated.\n\n" +
							"Do you want to proceed?";
					choice = JOptionPane.showConfirmDialog(this, (Object) message,
							"PROJECTION FILES DO NOT MATCH", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
					//      "Please reproject or use different local data.";
					//JOptionPane.showMessageDialog(null, (Object) message,
					//      "PROJECTION FILES DO NOT MATCH", JOptionPane.ERROR_MESSAGE);
					//choice = JOptionPane.NO_OPTION;
				}
			} catch (Exception e) {
				String message = "The projection file \n" +
						"<html><font color=red>" + prjfile + ".prj</font></html>\n" +
						"could not be found.\n" +
						"No projection is defined for this shapefile.\n" +
						"The data may be INCORRECTLY geolocated.\n\n" +
						"Do you want to proceed?";
				choice = JOptionPane.showConfirmDialog(this, (Object) message,
						"PROJECTION FILE NOT FOUND", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
			} finally {
				try {
					bw.close();
				} catch (Exception e) {
				}
			}

			if (choice == JOptionPane.YES_OPTION) {

				// Initialize progress frame
				/*
				 *  Progress info;
				 *  f = new Frame();
				 *  info = new Progress(f, false);
				 *  info.setTitle("File Reading Progress");
				 *  status = new Label("Loading: " + file.getName());
				 *  info.add(status, "Center");
				 *  info.show();
				 *  status.setForeground(new Color(57, 24, 198));
				 */
				loadCustomShapefile(file);

			}
			// END if (choice == JOptionPane.YES_OPTION)
		}
		// END if (returnVal == JFileChooser.APPROVE_OPTION)

		else {
			System.out.println("Open command cancelled by user.");
		}

	}




	private Color invertColor(Color c) {
		int red = c.getRed();
		int green = c.getGreen();
		int blue = c.getBlue();
		int flipRed = 255-red;
		int flipGreen = 255-green;
		int flipBlue = 255-blue;

		int redDiff = Math.abs(red-flipRed);
		int greenDiff = Math.abs(green-flipGreen);
		int blueDiff = Math.abs(blue-flipBlue);

		int threshold = 80;

		if (redDiff < threshold && greenDiff < threshold && blueDiff < threshold) {
			flipRed-=threshold;
			flipGreen-=threshold;
			flipBlue-=threshold;
		}

		return (new Color(flipRed, flipGreen, flipBlue));

	}




	public void loadCustomShapefile(File file) {
		// Check for a shapefile smaller than xxx Bytes
		if (file.length() < 105000000) {
			// Open up the file with a Shapefile Reader
			try {
				Color localColor;
				int x = localThemeCounter % 8;
				switch (x) {
				case 0:
					localColor = new Color(36, 191, 230);
					break;
				case 1:
					localColor = new Color(255, 10, 230);
					break;
				case 2:
					localColor = new Color(38, 135, 0);
					break;
				case 3:
					localColor = new Color(185, 135, 0);
					break;
				case 4:
					localColor = new Color(255, 179, 48);
					break;
				case 5:
					localColor = Color.red;
					break;
				case 6:
					localColor = Color.blue;
					break;
				case 7:
					localColor = Color.green;
					break;
				default:
					localColor = new Color(185, 56, 0);
					break;
				}


				viewer.loadLocalShapefile(file.toURI().toURL(), localColor);
				localThemeCounter++;

				setSelectedTab(2);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		// END if(file.length < xxx)

		else {
			//info.dispose();
			String message = "The shapefile\n" +
					"<html><font color=red>" + file + "</font></html>\n" +
					"has a filesize of\n" +
					"<html><font color=red>" + file.length() / 1000000.0 + "</font></html>" +
					"megabytes\nwhich exceeds the maximum size of 105 megabytes.\n\n" +
					"Please load a smaller shapefile.";

			JOptionPane.showMessageDialog(null, (Object) message,
					"LOCAL SHAPEFILE TOO LARGE", JOptionPane.ERROR_MESSAGE);
		}
		// END else
	}






























	public void isolateRadar() {
		viewer.setGridSatelliteVisibility(false);
		viewer.setRadarGridCoverageVisibility(true);
		viewer.setShowAlphanumericLayers(true);
		viewer.getStationPointMapLayer().setVisible(false);
		viewer.getStationPointSelectedMapLayer().setVisible(false);
		viewer.setLocationStationLegendVisibility(false);

		if (radLegend.getSelectedItem().toString().equals("None")) {
			viewer.setLegendSidePanelVisibility(false);
		}
		else if (radLegend.getSelectedItem().toString().equals("Large")) {
			viewer.setLegendSidePanelVisibility(true);
		}
		jcbRadar.setSelected(true);
		jcbRadar.setEnabled(true);
		radTransparency.setEnabled(true);
		radSmoothing.setEnabled(true);
		radLegend.setEnabled(true);
		viewer.setRangeRingVisibility(true);
		radColorTableCombo.setEnabled(true);
		radEditColorTableLink.setEnabled(true);

		jcbGridSatellite.setSelected(false);
		jcbGridSatellite.setEnabled(false);
		gridSatTransparency.setEnabled(false);
		gridSatSmoothing.setEnabled(false);
		gridSatLegend.setEnabled(false);
		satColorTableCombo.setEnabled(false);
		gridColorTableCombo.setEnabled(false);
		jcomboGridMinMax.setEnabled(false);
		jcbFlipGridColorTable.setEnabled(false);
		jtfGridMaxValue.setEnabled(false);
		jtfGridMinValue.setEnabled(false);

		jcbLocationStation.setSelected(false);
		jcbLocationStation.setEnabled(false);
		locationStationTransparency.setEnabled(false);
		locationStationLegend.setEnabled(false);
		locationStationColorTableCombo.setEnabled(false);
		jcomboLocationStationMinMax.setEnabled(false);
		jcbFlipLocationStationColorTable.setEnabled(false);
		jtfLocationStationMaxValue.setEnabled(false);
		jtfLocationStationMinValue.setEnabled(false);

		lastIsolatedType = DataType.RADAR;
	}

	public void isolateGridSatellite(boolean isSatData) {
		viewer.setGridSatelliteVisibility(true);
		viewer.setRadarGridCoverageVisibility(false);
		viewer.setLegendSidePanelVisibility(false);
		viewer.setShowAlphanumericLayers(false);
		viewer.getStationPointMapLayer().setVisible(false);
		viewer.getStationPointSelectedMapLayer().setVisible(false);
		viewer.setLocationStationLegendVisibility(false);
		if (gridSatLegend.getSelectedItem().toString().equals("None")) {
			viewer.setGridSatelliteLegendVisibility(false);
		}
		else if (gridSatLegend.getSelectedItem().toString().equals("Medium")) {
			viewer.setGridSatelliteLegendVisibility(true);
		}
		jcbRadar.setSelected(false);
		jcbRadar.setEnabled(false);
		radTransparency.setEnabled(false);
		radSmoothing.setEnabled(false);
		radLegend.setEnabled(false);
		viewer.setRangeRingVisibility(false);
		radColorTableCombo.setEnabled(false);
		radEditColorTableLink.setEnabled(false);


		jcbGridSatellite.setSelected(true);
		jcbGridSatellite.setEnabled(true);
		gridSatTransparency.setEnabled(true);
		//        satSmoothing.setEnabled(true);
		gridSatLegend.setEnabled(true);
		satColorTableCombo.setEnabled(true);
		gridColorTableCombo.setEnabled(true);
		jcomboGridMinMax.setEnabled(true);
		jcbFlipGridColorTable.setEnabled(true);
		if (jcomboGridMinMax.getSelectedItem().toString().equals("Custom")) {
			jtfGridMaxValue.setEnabled(true);
			jtfGridMinValue.setEnabled(true);
		}

		if (isSatData) {
			if (gridSatellitePanel.getComponent(gridSatellitePanel.getComponentCount()-1) == satColorTablePanel) {
				System.out.println("found existing sat color table panel ");
				return;
			}
			gridSatellitePanel.remove(gridColorTablePanel);
			gridSatellitePanel.add(satColorTablePanel, "br left");
			gridSatellitePanel.revalidate();
			lastIsolatedType = DataType.SATELLITE;        
		}
		else {
			if (gridSatellitePanel.getComponent(gridSatellitePanel.getComponentCount()-1) == gridColorTablePanel) {
				System.out.println("found existing sat color table panel ");
				return;
			}
			gridSatellitePanel.remove(satColorTablePanel);
			gridSatellitePanel.add(gridColorTablePanel, "br left");
			gridSatellitePanel.revalidate();
			lastIsolatedType = DataType.GRIDDED;
		}


		jcbLocationStation.setSelected(false);
		jcbLocationStation.setEnabled(false);
		locationStationTransparency.setEnabled(false);
		locationStationLegend.setEnabled(false);
		locationStationColorTableCombo.setEnabled(false);
		jcomboLocationStationMinMax.setEnabled(false);
		jcbFlipLocationStationColorTable.setEnabled(false);
		jtfLocationStationMaxValue.setEnabled(false);
		jtfLocationStationMinValue.setEnabled(false);

	}

	public void isolateLocationStation() {
		viewer.setGridSatelliteVisibility(false);
		viewer.setRadarGridCoverageVisibility(false);
		viewer.setShowAlphanumericLayers(false);
		viewer.getStationPointMapLayer().setVisible(true);
		viewer.getStationPointSelectedMapLayer().setVisible(true);
		if (locationStationLegend.getSelectedItem().toString().equals("None")) {
			viewer.setLocationStationLegendVisibility(false);
		}
		else if (locationStationLegend.getSelectedItem().toString().equals("Medium")) {
			viewer.setLocationStationLegendVisibility(true);
		}
		jcbRadar.setSelected(false);
		jcbRadar.setEnabled(false);
		radTransparency.setEnabled(false);
		radSmoothing.setEnabled(false);
		radLegend.setEnabled(false);
		viewer.setRangeRingVisibility(false);
		radColorTableCombo.setEnabled(false);
		radEditColorTableLink.setEnabled(false);

		jcbGridSatellite.setSelected(false);
		jcbGridSatellite.setEnabled(false);
		gridSatTransparency.setEnabled(false);
		gridSatSmoothing.setEnabled(false);
		gridSatLegend.setEnabled(false);
		satColorTableCombo.setEnabled(false);
		gridColorTableCombo.setEnabled(false);
		jcomboGridMinMax.setEnabled(false);
		jcbFlipGridColorTable.setEnabled(false);
		jtfGridMaxValue.setEnabled(false);
		jtfGridMinValue.setEnabled(false);

		jcbLocationStation.setSelected(true);
		jcbLocationStation.setEnabled(true);
		locationStationTransparency.setEnabled(true);
		locationStationLegend.setEnabled(true);
		locationStationColorTableCombo.setEnabled(true);
		jcomboLocationStationMinMax.setEnabled(true);
		jcbFlipLocationStationColorTable.setEnabled(true);
		jtfLocationStationMaxValue.setEnabled(true);
		jtfLocationStationMinValue.setEnabled(true);

		lastIsolatedType = DataType.LOCATION_STATION;
	}



	public DataType getLastIsolatedDataType() {
		return lastIsolatedType;
	}


	public void setCurrentViewType(CurrentViewType viewType) {
		tabPane.removeAll();
		if (viewType == CurrentViewType.GEOTOOLS) {
			tabPane.add(dataPanel, "Data Layers");
			tabPane.add(snapshotPanel, "Snapshot Layers");
			tabPane.add(new JScrollPane(overlayPanel), "Overlay Selector");
			tabPane.add(wmsPanel, "Background Maps (WMS)");

		}
		else if (viewType == CurrentViewType.GOOGLE_EARTH) {
			tabPane.add(dataPanel, "Data Layers");
			gePanel = viewer.getGoogleEarthBrowserInternal().createLayerSelectionPanel();
			tabPane.add(gePanel, "Google Earth");
		}
		else if (viewType == CurrentViewType.NCDC_NCS) {
			tabPane.add(dataPanel, "Data Layers");
		}
		else if (viewType == CurrentViewType.FOUR_MAP_PANE) {
			tabPane.add(dataPanel, "Data Layers");
			tabPane.add(snapshotPanel, "Snapshot Layers");
			tabPane.add(new JScrollPane(overlayPanel), "Overlay Selector");
			tabPane.add(wmsPanel, "Background Maps (WMS)");
			tabPane.add(new JLabel("Four Pane properties"), "Four-Panel");
		}
		else {
			tabPane.add(dataPanel, "Data Layers");
			tabPane.add(snapshotPanel, "Snapshot Layers");
			tabPane.add(new JScrollPane(overlayPanel), "Overlay Selector");
			tabPane.add(wmsPanel, "Background Maps (WMS)");
		}
	}


	public String getSatelliteColorTableName() {
		return satColorTableCombo.getSelectedItem().toString();
	}

	public void setSatelliteColorTableName(String colorTableName) {
		satColorTableCombo.setSelectedItem(colorTableName);
	}

	public void setGridColorTableName(String gridColorTableName) {
		gridColorTableCombo.setSelectedItem(gridColorTableName);
	}

	public void setLocationStationColorTableName(String locationStationColorTableName) {
		locationStationColorTableCombo.setSelectedItem(locationStationColorTableName);
	}

	public String getLocationStationColorTableName() {
		return locationStationColorTableCombo.getSelectedItem().toString();
	}













	public void updateRadarColorTable() throws Exception {
		
		System.out.println("updateRadarColorTable 1");
		
		if (radColorTableCombo.getSelectedItem() == null || viewer.getNexradHeader() == null) {
			return;
		}
		
		String colorTable = radColorTableCombo.getSelectedItem().toString();	
		String defaultPalName = NexradSampleDimensionFactory.getDefaultPaletteName(
				viewer.getNexradHeader().getProductCode(), viewer.getNexradHeader().getVersion());
		SampleDimensionAndLabels sd = null;
		ColorsAndValues[] cavArray = null;
		
		if (colorTable.equalsIgnoreCase("Default")) {
			
			
			URL url = ResourceUtils.getInstance().getJarResource(
        			new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
        			"/config/colormaps/"+defaultPalName, null);
			cavArray = ColorLutReaders.parseWCTPal(new BufferedReader(new InputStreamReader(url.openStream())));
			NexradSampleDimensionFactory.removePaletteOverride(defaultPalName);
		}
		else {

			File cacheFolder = new File(WCTConstants.getInstance().getCacheLocation()+File.separator+
					"config"+File.separator+"user-colortables");

			File customPalFile = new File(cacheFolder.toString() + File.separator + colorTable);
			cavArray = ColorLutReaders.parseWCTPal(new BufferedReader(new StringReader(FileUtils.readFileToString(customPalFile))));
			NexradSampleDimensionFactory.setPaletteOverride(defaultPalName, cavArray);
		}

		sd = ColorLutReaders.convertToSampleDimensionAndLabels(cavArray[0], cavArray[1]);
		
		int percent = Integer.parseInt(((String)radTransparency.getSelectedItem()).replaceAll("%","").trim());
		int radarAlphaChannelValue = 255 - ((int)((percent/100.0)*255));
		SampleDimension[] sdArray = WCTGridCoverageSupport.setSampleDimensionAlpha(
				new SampleDimension[] { sd.getSampleDimension() }, radarAlphaChannelValue);
		
		

		viewer.getRadarLegendImageProducer().setSampleDimensionAndLabels(sd);

		GridCoverage gc = viewer.getRadarGridCoverage();
		WritableRenderedImageAdapter img = (WritableRenderedImageAdapter)(gc.getRenderedImage());
		WritableRaster data = (WritableRaster)img.getData();

		System.out.println("color table used: "+colorTable);
		System.out.println(" radar alpha: "+radarAlphaChannelValue);
		
		
		viewer.setRadarGridCoverage(new GridCoverage(
				gc.getName(null), data, GeographicCoordinateSystem.WGS84, null, gc.getEnvelope(), sdArray ));

		viewer.getLargeLegendPanel().setLegendImage(viewer.getRadarLegendImageProducer());

		gc.dispose();

		viewer.fireRenderCompleteEvent();

	}

	public void refreshRadarColorTableList() {
		
		Object currentSelection = radColorTableCombo.getSelectedItem();
		
		radColorTableCombo.removeAllItems();
		radColorTableCombo.addItem("Default");

		File cacheFolder = new File(WCTConstants.getInstance().getCacheLocation()+File.separator+
				"config"+File.separator+"user-colortables");
		File[] colorTableFiles = cacheFolder.listFiles();
		if (colorTableFiles == null) {
			return;
		}
		
		
		for (File f : colorTableFiles) {
			radColorTableCombo.addItem(f.getName());
		}
		
		radColorTableCombo.setSelectedItem(currentSelection);

	}





	private class DataLayersListener implements ActionListener {

		//        @Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("Radar")) {
				viewer.setRadarGridCoverageVisibility(jcbRadar.isSelected());

				if (viewer.getRadarGridCoverage() == null) {
					return;                    
				}


				if (jcbRadar.isSelected()) {
					viewer.getStatusBar().setNexradHeader(viewer.getNexradHeader());
					viewer.getMapPaneZoomChange().setRadarActive(true);
					viewer.refreshRadarData();
					if (radLegend.getSelectedItem().toString().equals("None")) {
						viewer.setLegendSidePanelVisibility(false);
					}
					else if (radLegend.getSelectedItem().toString().equals("Large")) {
						viewer.setLegendSidePanelVisibility(true);
					}
				}
				else {
					viewer.getStatusBar().setNexradHeader(null);
					viewer.getMapPaneZoomChange().setRadarActive(false);
					viewer.setLegendSidePanelVisibility(false);
				}
			}
			else if (e.getActionCommand().equals("Satellite")) {
				viewer.setGridSatelliteVisibility(jcbGridSatellite.isSelected());
				if (jcbGridSatellite.isSelected()) {
					viewer.refreshGridOrSatellite();
				}
			}
			else if (e.getActionCommand().equals("Location/Station")) {
				viewer.getStationPointMapLayer().setVisible(jcbLocationStation.isSelected());
				viewer.getStationPointSelectedMapLayer().setVisible(jcbLocationStation.isSelected());
				if ((! jcbLocationStation.isSelected()) || locationStationLegend.getSelectedItem().toString().equals("None")) {
					viewer.setLocationStationLegendVisibility(false);
				}
				else {
					viewer.setLocationStationLegendVisibility(true);
				}
			}
		}

	}


	private class RadarSmoothingChangeListener implements ChangeListener {
		private int waitTime = 1000;

		TimerTask task = null;

		//        @Override
		public void stateChanged(ChangeEvent e) {
			if (task != null) {
				try {
					task.cancel();
				} catch (Exception ex) {
				}
			}
			task = new TimerTask() {
				public void run() {
					System.out.println(" EXECUTING SMOOTHING CHANGE EVENT ");
					viewer.setRadarSmoothFactor(Double.parseDouble(radSmoothing.getValue().toString()));
					if (jcbRadar.isSelected()) {
						viewer.refreshRadarData();
					}
				}
			};
			timer.schedule(task , waitTime);
		}
	}

	private class SatelliteSmoothingChangeListener implements ChangeListener {
		private int waitTime = 1000;

		TimerTask task = null;

		//        @Override
		public void stateChanged(ChangeEvent e) {
			if (task != null) {
				try {
					task.cancel();
				} catch (Exception ex) {
				}
			}
			task = new TimerTask() {
				public void run() {
					System.out.println(" EXECUTING SMOOTHING CHANGE EVENT ");
					viewer.setSatelliteSmoothFactor(Double.parseDouble(gridSatSmoothing.getValue().toString()));
					if (jcbGridSatellite.isSelected()) {
						viewer.refreshGridOrSatellite();
					}
				}
			};
			timer.schedule(task , waitTime);
		}
	}



	private class SnapshotActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			viewer.snapshotCurrentLayer();

			//                System.out.println(viewer.getSnapshotLayers().toString());
			//                RenderedLayer[] layers = ((StyledMapRenderer)viewer.getMapPane().getRenderer()).getLayers();
			//                for (int n=0; n<layers.length; n++) {
			//                    System.out.println("LAYER "+n+":  "+layers[n].getName(layers[n].getLocale())+"  "+layers[n].isVisible());
			//                }
		}        
	}

	private class ClearActiveLayerActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			viewer.clearAllData();

			viewer.setGridSatelliteVisibility(false);
			viewer.setRadarGridCoverageVisibility(false);
			//            viewer.setLegendVisibility(false);
			viewer.setShowAlphanumericLayers(false);

			jcbRadar.setSelected(false);
			jcbRadar.setEnabled(false);
			radTransparency.setEnabled(false);
			radSmoothing.setEnabled(false);
			radLegend.setEnabled(false);
			viewer.setRangeRingVisibility(false);

			jcbGridSatellite.setSelected(false);
			jcbGridSatellite.setEnabled(false);
			gridSatTransparency.setEnabled(false);
			gridSatSmoothing.setEnabled(false);
			gridSatLegend.setEnabled(false);
			satColorTableCombo.setEnabled(false);
			gridColorTableCombo.setEnabled(false);
			jcomboGridMinMax.setEnabled(false);
			jcbFlipGridColorTable.setEnabled(false);
			jtfGridMaxValue.setEnabled(false);
			jtfGridMinValue.setEnabled(false);


			jcbLocationStation.setSelected(false);
			jcbLocationStation.setEnabled(false);
			locationStationColorTableCombo.setEnabled(false);
			locationStationLegend.setEnabled(false);
			locationStationTransparency.setEnabled(false);
			jcbFlipLocationStationColorTable.setEnabled(false);
			jtfLocationStationMaxValue.setEnabled(false);
			jtfLocationStationMinValue.setEnabled(false);


		}

	}



} // END CLASS
