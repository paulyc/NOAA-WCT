package gov.noaa.ncdc.wct.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.jdesktop.swingx.JXHyperlink;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.plot.XYPlot;

import com.jidesoft.swing.RangeSlider;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import gov.noaa.ncdc.common.FeatureCache;
import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.cdm.PointCollectionDecoder;
import gov.noaa.ncdc.wct.export.WCTExportDialog;
import gov.noaa.ncdc.wct.ui.LocationTimeseriesUtils.NoSharedTimeDimensionException;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.StructureData;
import ucar.ma2.StructureMembers;
import ucar.ma2.StructureMembers.Member;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.PointFeatureIterator;
import ucar.nc2.ft.StationTimeSeriesFeature;
import ucar.nc2.ft.StationTimeSeriesFeatureCollection;
import ucar.nc2.ft.point.standard.StandardPointCollectionImpl;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.ui.PointFeatureDatasetViewer;
import ucar.nc2.units.DateFormatter;
import ucar.nc2.units.DateUnit;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.geoloc.Station;
import ucar.util.prefs.PreferencesExt;
import ucar.util.prefs.XMLStore;

public class LocationTimeseriesProperties extends JDialog {

	public final static AttributeType[] LOCATION_STATION_ATTRIBUTES = {
			AttributeTypeFactory.newAttributeType("geom", Geometry.class),
			AttributeTypeFactory.newAttributeType("value", Double.class),
			AttributeTypeFactory.newAttributeType("station_id", String.class),
			AttributeTypeFactory.newAttributeType("station_desc", String.class),
			AttributeTypeFactory.newAttributeType("lat", Double.class),
			AttributeTypeFactory.newAttributeType("lon", Double.class),
			AttributeTypeFactory.newAttributeType("alt", Double.class),
			AttributeTypeFactory.newAttributeType("label", String.class)
	};
	public static final int DEFAULT_ALPHA_PERCENT = 40;
	public static enum DataTableViewType { STATION_TIMESERIES, STATION_TIMESLICE, POINT_COLLECTION };

	private FeatureType schema = null;
	private GeometryFactory geoFactory = new GeometryFactory();



	private WCTViewer viewer = null;
	private static LocationTimeseriesProperties dialog = null;
	private FeatureDataset fd = null;
	private StationTimeSeriesFeatureCollection stsfc = null;
	private PointCollectionDecoder pcProcessor = new PointCollectionDecoder();
	private List<Station> stationList = null;
	private ArrayList<String> plottedVariableList = new ArrayList<String>();
	// private String[] currentVariableUnits = null;
	
	private JPanel timeseriesSelectionPanel = new JPanel(new RiverLayout());
	private JPanel pointSelectionPanel = new JPanel(new RiverLayout());
	private JPanel chartButtonPanel = new JPanel(new RiverLayout());
	private JPanel timeseriesChartButtonPanel = new JPanel(new RiverLayout());
	
	private RangeSlider jsliderDateRange = new RangeSlider();
	private JLabel sliderMinLabel = new JLabel("min");
	private JLabel sliderMaxLabel = new JLabel("max");
	
	private JComboBox<String> jcomboVariables = new JComboBox<String>(new String[]{});
	private JComboBox<String> jcomboUniqueDates = new JComboBox<String>();
	private JLabel jlDoubleClickInstructions = new JLabel("[ double-click on location to load time series ]");

	
	private JTabbedPane tabPane = new JTabbedPane();

	private JTextArea textArea = new JTextArea(30, 80);
	private JTable dataTable = new JTable() {
		//		@Override
		//		public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		////			System.out.println(row + " ||| " + column);
		//			Component cell = super.prepareRenderer(renderer, row, column);
		//			if (row % 2 == 0 && ! isCellSelected(row, column)) {
		//				cell.setBackground(new Color(235, 235, 250));
		//			}
		//			else if (isCellSelected(row, column)){
		//				cell.setBackground(getSelectionBackground());
		//			}
		//			else {
		//				cell.setBackground(getBackground());
		//			}
		//			return cell;
		//		}
	};
	JButton jbCancel = null;
	private boolean cancel = false;
	private DateFormatter df = new DateFormatter();

	private String[] columnNames = null;
	private String[] columnInfo = null;

	private boolean isPolygonShapeLookup = false;

	private JComboBox<String> jcomboStationIds = new JComboBox<String>();

	private List<String> uniqueDateListForSelectedVariable = null;
	private String currentDataSliceDateString = null;
	private Variable currentVariable = null;
	private Color[] currentSymbolizedColors = null;
	private Double[] currentSymbolizedValues = null;
	private double lastProcessedMinValue = Double.NaN;
	private double lastProcessedMaxValue = Double.NaN;
	private DataTableViewType currentDataTableViewType = DataTableViewType.STATION_TIMESERIES;
	private double validRangeMinValue = Double.NaN;
	private double validRangeMaxValue = Double.NaN;
	private boolean hasValidRange = false;

	private LocationTimeseriesProperties(WCTViewer viewer) {
		super(viewer, "Location Timeseries Data");
		this.viewer = viewer;

		init();

		// Show tool tips immediately
		ToolTipManager.sharedInstance().setInitialDelay(0);

		createUI();
		pack();
		//		setSize(new Dimension(getSize().width+100, getSize().height));
		setSize(new Dimension(512, Math.min(viewer.getSize().height, 650) ));
		setLocation(viewer.getX()+25, viewer.getY()+25);
	}

	public static LocationTimeseriesProperties getInstance(WCTViewer viewer) {
		if (dialog == null) {
			dialog = new LocationTimeseriesProperties(viewer);
		}
		return dialog;
	}



	private void init() {
		try {
			schema = FeatureTypeFactory.newFeatureType(LOCATION_STATION_ATTRIBUTES, "Station/Point Attributes");
		} catch (Exception e) {
			e.printStackTrace();
			javax.swing.JOptionPane.showMessageDialog(this, "Point Feature Init Exception: "+e, 
					"POINT FEATURE INIT EXCEPTION", javax.swing.JOptionPane.ERROR_MESSAGE);
		}
	}


	
	private void createUI() {
		this.setLayout(new RiverLayout());

		// Create top selection panel for station/location timeseries types:
		//   Includes selection for station/location and unique date
		
		timeseriesSelectionPanel.add(new JLabel("<html><b>Select Location: </b></html>"));
		timeseriesSelectionPanel.add(jcomboStationIds, "hfill");

		timeseriesSelectionPanel.add(new JLabel("<html><b> -- OR -- </b></html>"), "br");
		timeseriesSelectionPanel.add(new JLabel("<html><b>Select Date: </b></html>"), "br");
		timeseriesSelectionPanel.add(jcomboUniqueDates, "hfill");


		// Create top selection panel for non-station point types (like lightning strikes)
		// NOT READY YET
//		pointSelectionPanel.add(new JLabel("<html><b>Filter by Date/Time:</b></html>"));
//		pointSelectionPanel.add(jsliderDateRange, "hfill br");
//		pointSelectionPanel.add(sliderMinLabel, "br left");
//		pointSelectionPanel.add(new JLabel(""), "hfill");
//		pointSelectionPanel.add(sliderMaxLabel, "right");
		
		
		// Add to selection panel, which will switch out these subpanels depending on 
		//  current data type (timeseries or point) displayed.

//		selectionPanel.add(timeseriesSelectionPanel, "hfill");

		
		
		this.add(timeseriesSelectionPanel, "hfill br");
		


		JPanel tablePanel = new JPanel(new RiverLayout());
		JPanel textPanel = new JPanel(new RiverLayout());
		textPanel.add(new JScrollPane(textArea), "br hfill vfill");
		tablePanel.add(jlDoubleClickInstructions, "br hfill");
		tablePanel.add(new JScrollPane(dataTable), "br hfill vfill");
		tabPane.addTab("Table", tablePanel);
		tabPane.addTab("Text", textPanel);
		// tabPane.addTab("Plot", plotPanel);
		// tabPane.setShowCloseButtonOnTab(true);
		this.add(tabPane, "br hfill vfill");

		final JDialog finalThis = this;
		jcomboStationIds.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadCurrentlySelectedStation();
			}
		});

		jbCancel = new JButton("Cancel");
		jbCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancel = true;
			}
		});
		
		jsliderDateRange.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				RangeSlider slider = (RangeSlider) evt.getSource();
				sliderMinLabel.setText(CalendarDate.of(new Date(slider.getLowValue()*1000L)).toString());
				sliderMaxLabel.setText(CalendarDate.of(new Date(slider.getHighValue()*1000L)).toString());
				if (!slider.getValueIsAdjusting()) {
					int value = slider.getValue();
					System.out.println(slider.getLowValue()+" -- to -- "+slider.getHighValue());
				}
			}			
		});



		jcomboUniqueDates.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				foxtrot.Worker.post(new foxtrot.Job() {
					public Object run() {
						String selectedTime = null;
						try {
							if (jcomboUniqueDates.getSelectedItem() != null) {						

								selectedTime = jcomboUniqueDates.getSelectedItem().toString();
								//								readUniqueTime(selectedTime);	
								readUniqueTime(jcomboUniqueDates.getSelectedIndex());
								//								setViewerExtent(s);
								//								setViewerSelectedStation(s);

							}
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (NoDataException e) {
							if (selectedTime != null) {
								JOptionPane.showMessageDialog(finalThis, "No observations are present for this time: "+selectedTime);
							}
						} catch (InvalidRangeException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchElementException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAttributeException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return "DONE";
					}
				});
			}
		});

		jcomboVariables.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				FeatureDatasetPoint fdp = null;
				if (fd != null && fd.getFeatureType().isPointFeatureType()) {
					fdp = (FeatureDatasetPoint)fd;
				}
				else {
					WCTUiUtils.showErrorMessage(finalThis, "Data does not appear to be of CDM type 'FeatureDatasetPoint'  ", new IOException());
				}
				
				if (currentDataTableViewType == DataTableViewType.POINT_COLLECTION) {
					String shortName = jcomboVariables.getSelectedItem().toString().split("\\s+")[0];
					currentVariable = fdp.getNetcdfFile().findVariable(shortName);
					System.out.println("CURRENT VARIABLE: "+currentVariable);
					
					System.out.println("current feature att: "+getCurrentStationFeatureAttribute());
					lastProcessedMinValue = pcProcessor.getAttributeMinMaxMap().get(getCurrentStationFeatureAttribute()).min;
					lastProcessedMaxValue = pcProcessor.getAttributeMinMaxMap().get(getCurrentStationFeatureAttribute()).max;
					int alpha = viewer.getMapSelector().getLocationStationTransparency();
					if (alpha < 0) {
						alpha = DEFAULT_ALPHA_PERCENT;
					}
					viewer.setLocationStationColorTable(viewer.getMapSelector().getLocationStationColorTableName(), 
							alpha);
					

					return;
				}

				//		get unique date list		
				try {
					String shortName = jcomboVariables.getSelectedItem().toString().split("\\s+")[0];
					Variable currentVariable = fdp.getNetcdfFile().findVariable(shortName);
					uniqueDateListForSelectedVariable = LocationTimeseriesUtils.getUniqueTimeList(((FeatureDatasetPoint)fd), currentVariable);
					System.out.println(currentVariable);

					int selectedDateIndex = jcomboUniqueDates.getSelectedIndex();
					jcomboUniqueDates.setModel(new DefaultComboBoxModel<String>(
							uniqueDateListForSelectedVariable.toArray(new String[uniqueDateListForSelectedVariable.size()])
							));
					jcomboUniqueDates.setEnabled(true);
					// this fires an event that calls
					jcomboUniqueDates.setSelectedIndex(selectedDateIndex);

				} catch (NoSharedTimeDimensionException ex) {
					ex.printStackTrace();
					jcomboUniqueDates.setModel(new DefaultComboBoxModel<String>(new String[] { "Not Supported (Requires Shared Time Dimension)" } ));
					jcomboUniqueDates.setEnabled(false);
				} catch (IOException ex) {
					WCTUiUtils.showErrorMessage(finalThis, "Error obtaining unique time list", ex);
				}

			}
		});

		//		jeksTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
		//			@Override
		//			public void valueChanged(ListSelectionEvent e) {
		//				if (! e.getValueIsAdjusting()) {
		//					if (jcomboVariables.getSelectedItem() != null) {
		//						// col '0' is the date/time column 
		//						if (jeksTable.getSelectedColumn() > 0) {
		//							jcomboVariables.setSelectedIndex(jeksTable.getSelectedColumn()-1);
		//						}
		//					}
		//				}
		//			}
		//		});


		dataTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					if (currentDataTableViewType == DataTableViewType.STATION_TIMESERIES) {
						jcomboUniqueDates.setSelectedIndex(dataTable.convertRowIndexToModel(dataTable.getSelectedRow()));
					}
					else {
						jcomboStationIds.setSelectedIndex(dataTable.convertRowIndexToModel(dataTable.getSelectedRow()));
					}
				}
			}
		});






		// String[] vars = null;
		//        try {
		//         vars = getStationVariables(jcomboStationIds.getSelectedItem().toString().split(" ")[0]);
		//     } catch (WCTException e1) {
		// e1.printStackTrace();
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// }
		//
		// if (vars == null) {
		// JOptionPane.showMessageDialog(this, "Could not list variables for this station", 
		// "Error", JOptionPane.ERROR_MESSAGE);
		// }
		// final JComboBox jcbVariables = new JComboBox(vars);
		final JButton jbChart = new JButton("New Chart");
		jbChart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					newPlot();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (WCTException e) {
					e.printStackTrace();
				} catch (PlotException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(jbChart, e.getMessage());
				}
			}
		});
		final JButton jbAddChart = new JButton("Add to Chart");
		jbAddChart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					addToPlot();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (WCTException e) {
					e.printStackTrace();
				} catch (PlotException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					JOptionPane.showMessageDialog(jbChart, e.getMessage());
				}
			}
		});



		JCheckBox jcbShowMissingColumns = new JCheckBox("Show Missing Fields?");
		JCheckBox jcbLabelOnMap = new JCheckBox("Label on Map?");
		JButton jbColor = new JButton("   ");

		//		this.add(jcbShowMissingColumns, "br");
		//		this.add(jcbLabelOnMap);
		//		this.add(new JLabel("Color: "));
		//		this.add(jbColor);

		JButton jbColumnInfo = new JButton("Column Info");
		jbColumnInfo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				StringBuilder sb = new StringBuilder();
				if (columnInfo != null) {
					for (int n=0; n<columnInfo.length; n++) {
						sb.append("\n"+columnNames[n]+": \n");
						String[] vals = columnInfo[n].split("<br>");
						//						sb.append(Arrays.toString(vals)+"\n");
						//						sb.append("Name = '"+vals[0]+"'\n");
						sb.append("Description = '"+vals[1]+"'\n");
						if (vals.length == 3 && ! vals[2].trim().equals("null")) {
							sb.append("Units = '"+vals[2]+"'\n");
						}
					}
				}
				else {
					sb.append("Please select a station to load observations.");
				}
				WCTTextDialog dialog = new WCTTextDialog(finalThis, sb.toString(), 
						"Column Info", true);
			}			
		});





		this.add("p", new JLabel("<html><b>Select Variable:</b></html>"));
		this.add(jcomboVariables, "hfill");
		
		timeseriesChartButtonPanel.add("br", new JLabel("Time Series Chart:"));
		timeseriesChartButtonPanel.add(jbChart);
		timeseriesChartButtonPanel.add(jbAddChart);
		timeseriesChartButtonPanel.add(new JSeparator());
		chartButtonPanel.add(timeseriesChartButtonPanel);
		this.add("br hfill", chartButtonPanel);
		
		this.add("right", jbColumnInfo);

		//		JComboBox<String> jcomboUniqueDates = new JComboBox<String>();
		//		JButton exportDate = new JButton("Export Data");
		//		exportDate.addActionListener(new ActionListener() {
		//			@Override
		//			public void actionPerformed(ActionEvent evt) {	
		//				try {
		//					exportData();
		//				} catch (NoSuchElementException e) {
		//					// TODO Auto-generated catch block
		//					e.printStackTrace();
		//				} catch (StreamingProcessException e) {
		//					// TODO Auto-generated catch block
		//					e.printStackTrace();
		//				} catch (IOException e) {
		//					// TODO Auto-generated catch block
		//					e.printStackTrace();
		//				}
		//			}			
		//		});


		//		this.add("p", new JLabel("Symbolize Date on Map: "));
		//		//		this.add(jcomboUniqueDates);
		//		this.add(exportDate);
		//		this.add(new JLabel(" (all stations for selected date)"));
		//
		//		JButton exportTimeSeries = new JButton("Export Time Series");
		//		this.add("p", exportTimeSeries);
		//		this.add(new JLabel("  [selected station]  "));

		JButton exportButton = new JButton("Export Data (CSV, JSON, SHP)");
		exportButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {

				try {

					WCTExportDialog wizard = new WCTExportDialog("Data Export Wizard", viewer);
					wizard.pack();
					wizard.setLocationRelativeTo(finalThis);
					wizard.setVisible(true);                    

					viewer.getDataSelector().checkCacheStatus();

				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(finalThis, ex.getMessage());
				}

			}
		});
		this.add("p", exportButton);






		JButton jbCopy = new JButton("Copy");
		jbCopy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TextUtils.getInstance().copyToClipboard(textArea.getText());
			}
		});

		JButton jbPrint = new JButton("Print");
		jbPrint.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//                try {
				//                    TextUtils.getInstance().print("Identification Results", textArea.getText());
				//                } catch (JetException e1) {
				//                    e1.printStackTrace();
				//                }
				try {
					TextUtils.getInstance().print(textArea);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		JButton jbSave = new JButton("Save");
		jbSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					TextUtils.getInstance().save(getContentPane(), textArea.getText(), "txt", "Text File");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});


		JXHyperlink jxlinkLaunchToolsUIView = new JXHyperlink();
		jxlinkLaunchToolsUIView.setText("View in ToolsUI Point Obs Window");
		jxlinkLaunchToolsUIView.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {

				try {

					String prefStore = ucar.util.prefs.XMLStore.makeStandardFilename(".unidata", "NetcdfUI22.xml");
					XMLStore store = ucar.util.prefs.XMLStore.createFromFile(prefStore, null);
					PreferencesExt prefs = store.getPreferences();


					PointFeatureDatasetViewer dv = new PointFeatureDatasetViewer(prefs, new JPanel());
					//    			dv.setDataset((PointObsDataset)tdataset);
					dv.setDataset((FeatureDatasetPoint)fd);


					JDialog dialog = new JDialog(viewer, "Point Obs Panel");
					dialog.add(dv);
					dialog.pack();
					dialog.setVisible(true);

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});



		this.add("hfill right", new JPanel());
		this.add("right", jxlinkLaunchToolsUIView);

		//		this.add("br center", jbCopy);
		//		this.add(jbPrint);
		//		this.add(jbSave);
		//		this.add(jbCancel);

		textPanel.add("br center", jbCopy);
		textPanel.add(jbPrint);
		textPanel.add(jbSave);
		//		textPanel.add(jbCancel);



	}



	private void loadCurrentlySelectedStation() {
		final JDialog finalThis = this;

		foxtrot.Worker.post(new foxtrot.Job() {
			public Object run() {
				Station s = null;
				try {
					if (jcomboStationIds.getSelectedItem() != null) {
						String id = jcomboStationIds.getSelectedItem().toString().split("\\(")[0].trim();								
						s = stsfc.getStation(id);

						readStationData(s);								
						setViewerExtentIfNotVisible(viewer.getCurrentExtent(), s);
						setViewerSelectedStation(s);

					}
				} catch (WCTException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (NoDataException e) {
					if (s != null) {
						JOptionPane.showMessageDialog(finalThis, "No observations are present for this station: "+
								s.getName()+" ("+s.getDescription()+")");
					}
				}
				return "DONE";
			}
		});
	}






	public void newPlot() throws IOException, WCTException, PlotException {

		Station station = stationList.get(jcomboStationIds.getSelectedIndex());
		// this.currentVariableUnits = getStationUnits(station);
		PlotRequestInfo plotRequestInfo = new PlotRequestInfo();
		String shortName = jcomboVariables.getSelectedItem().toString().split("\\s+")[0];
		plotRequestInfo.setVariables(new String[] { shortName });
		plotRequestInfo.setUnits(LocationTimeseriesUtils.getStationUnits(stsfc, station, plotRequestInfo.getVariables()));
		final LocationTimeseriesPlotPanel plotPanel = new LocationTimeseriesPlotPanel();
		plotPanel.setPlot(stsfc, station, plotRequestInfo);
		for (int n=0; n<tabPane.getTabCount(); n++) {
			if (tabPane.getTitleAt(n).equalsIgnoreCase("Chart")) {
				tabPane.removeTabAt(n);	
			}
		}
		plotPanel.getChartPanel().addChartMouseListener(new ChartMouseListener() {			
			@Override
			public void chartMouseMoved(ChartMouseEvent event) {
			}

			@Override
			public void chartMouseClicked(ChartMouseEvent event) {
				Point2D p = plotPanel.getChartPanel().translateScreenToJava2D(event.getTrigger().getPoint());
				Rectangle2D plotArea = plotPanel.getChartPanel().getScreenDataArea();
				XYPlot plot = (XYPlot) plotPanel.getChartPanel().getChart().getPlot(); // your plot
				double chartX = plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
				int closestIndex = LocationTimeseriesUtils.getClosestTimeSeriesIndex(plotPanel.getTimeSeriesDatasets(), Math.round(chartX));

				if (event.getTrigger().getClickCount() > 1) {
					jcomboUniqueDates.setSelectedIndex(closestIndex);						
				}
			}
		});
		tabPane.addTab("Chart", plotPanel);
		tabPane.setSelectedComponent(plotPanel);
	}

	public void addToPlot() throws IOException, WCTException, PlotException {

		PlotRequestInfo plotRequestInfo = new PlotRequestInfo();
		Station station = stationList.get(jcomboStationIds.getSelectedIndex());
		// this.currentVariableUnits = getStationUnits(station);
		if (tabPane.getSelectedIndex() == 0) {
			newPlot();
			return;
		}

		// PlotPanel plotPanel = (PlotPanel)(tabPane.getTabComponentAt(tabPane.getSelectedIndex()));
		LocationTimeseriesPlotPanel plotPanel = (LocationTimeseriesPlotPanel)(tabPane.getSelectedComponent());

		System.out.println(tabPane.getTabCount());
		System.out.println(tabPane.getSelectedIndex());
		System.out.println(plotPanel.getClass());
		System.out.println(plotPanel.getPlotRequestInfo());
		String[] vars = plotPanel.getPlotRequestInfo().getVariables();
		ArrayList<String> varList = new ArrayList<String>(Arrays.asList(vars));
		String newVar = jcomboVariables.getSelectedItem().toString().split("\\s+")[0];
		if (! varList.contains(newVar)) {
			varList.add(newVar);
		}
		plotRequestInfo.setVariables(varList.toArray(new String[varList.size()]));
		// plotRequestInfo.setUnits(currentVariableUnits);
		plotRequestInfo.setUnits(LocationTimeseriesUtils.getStationUnits(stsfc, station, plotRequestInfo.getVariables()));
		System.out.println("previous variables to plot: "+Arrays.toString(vars));
		System.out.println(" current variables to plot: "+varList);
		plotPanel.setPlot(stsfc, station, plotRequestInfo);
		for (int n=0; n<tabPane.getTabCount(); n++) {
			if (tabPane.getTitleAt(n).equalsIgnoreCase("Chart")) {
				tabPane.removeTabAt(n);	
			}
		}
		tabPane.addTab("Chart", plotPanel);
		tabPane.setSelectedComponent(plotPanel);
	}

	/**
	 * Load a file/URL as a Station dataset.
	 * @param dataURL
	 * @throws IOException
	 * @throws WCTException
	 * @throws NoSharedTimeDimensionException 
	 * @throws SchemaException 
	 * @throws FactoryConfigurationError 
	 * @throws IllegalAttributeException 
	 * @throws DecodeException 
	 * @throws NoDataException 
	 */
	public void process(URL dataURL) throws IOException, WCTException, NoSharedTimeDimensionException, IllegalAttributeException, FactoryConfigurationError, SchemaException, DecodeException, NoDataException {
		textArea.setText("");
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		textArea.setForeground(Color.GRAY);
		textArea.setCaretPosition(0);

		try {

			Formatter fmter = new Formatter();
			FeatureDataset fd = FeatureDatasetFactoryManager.open(null, dataURL.toString(), WCTUtils.getSharedCancelTask(), fmter);
			if (fd != null && fd.getFeatureType().isPointFeatureType()) {

			}
			else {
				System.out.println(fmter.toString());
				throw new IOException("Data does not appear to be of CDM type 'FeatureDatasetPoint' - "+fmter.toString());
			}
			process(fd);

		} finally {
			// DON'T CLOSE - keep open for methods that query the dataset
//			fd.close();
		}
	}

	public void process(FeatureDataset fd) throws IOException, WCTException, NoSharedTimeDimensionException, IllegalAttributeException, FactoryConfigurationError, SchemaException, DecodeException, NoDataException {
		this.fd = fd;

		FeatureDatasetPoint fdp = null;
		if (fd != null && fd.getFeatureType().isPointFeatureType()) {
			fdp = (FeatureDatasetPoint)fd;
		}
		else {
			throw new IOException("Data does not appear to be of CDM type 'FeatureDatasetPoint'  ");
		}



		List<ucar.nc2.ft.FeatureCollection> pfcList = fdp.getPointFeatureCollectionList();
		if (pfcList.get(0).getClass().getName().equals("ucar.nc2.ft.point.standard.StandardStationCollectionImpl")) {

			System.out.println("STATIONS: "+fdp.getPointFeatureCollectionList().size());
	

			this.remove(timeseriesSelectionPanel);
			this.remove(pointSelectionPanel);
			this.add(timeseriesSelectionPanel, "hfill br", 0);
			chartButtonPanel.add(timeseriesChartButtonPanel);
			process((StationTimeSeriesFeatureCollection)(pfcList.get(0)));
			

		}
		else if (pfcList.get(0).getClass().getName().equals("ucar.nc2.ft.point.standard.StandardPointCollectionImpl")) {

//			System.out.println("POINTS: "+fdp.get);

			this.remove(timeseriesSelectionPanel);
			this.remove(pointSelectionPanel);
			this.add(pointSelectionPanel, "hfill br", 0);
			chartButtonPanel.removeAll();

			process((StandardPointCollectionImpl)(pfcList.get(0)));
			



			//			throw new WCTException("StandardPointCollectionImpl type of NetCDF Feature Collections is not supported yet: "+
			//					pfcList.get(0).getClass().getName());
		}
		else {
			throw new WCTException("This type of NetCDF Feature Collections is not supported yet: "+
					pfcList.get(0).getClass().getName());
		}


	}

	public void process(StandardPointCollectionImpl spci) throws IOException, WCTException, NoSharedTimeDimensionException, IllegalAttributeException, FactoryConfigurationError, SchemaException, DecodeException, NoDataException {
		//		spci.get		// init variables
		jcomboVariables.setModel(new DefaultComboBoxModel<String>(LocationTimeseriesUtils.getPlottableVariables(spci.getPointFeatureIterator(10*1024*1024))));
		// this.currentVariableUnits = getStationUnits(stationList.get(0));


		isPolygonShapeLookup = false;
		viewer.getStationPointMapLayer().setStyle(WCTStyleUtils.getDefaultStyle());

		// update on map
		FeatureCollection viewerStationPointFeatures = viewer.getStationPointFeatures();
		viewerStationPointFeatures.clear();
		viewer.clearData();

		pcProcessor.setStandardPointCollection(spci);
		ArrayList<Feature> featureList = pcProcessor.getDecodedFeatureList();
		viewerStationPointFeatures.addAll(featureList);

		PointFeatureIterator pfi = spci.getPointFeatureIterator(1024*1000);
		processCDMPointFeatureIterator(pfi, spci.getTimeUnit(), featureList.size());
		
		
		System.out.println(" int: "+(int)(pcProcessor.getMinDate().getMillis()));
		System.out.println("long: "+pcProcessor.getMinDate().getMillis());
		
		this.jsliderDateRange.setMinimum((int)(pcProcessor.getMinDate().getMillis()/1000L));
		this.jsliderDateRange.setLowValue((int)(pcProcessor.getMinDate().getMillis()/1000L));
		this.jsliderDateRange.setMaximum((int)(pcProcessor.getMaxDate().getMillis()/1000L));
		this.jsliderDateRange.setHighValue((int)(pcProcessor.getMaxDate().getMillis()/1000L));
		
		currentDataTableViewType = DataTableViewType.POINT_COLLECTION;
		//		stationList.clear();
		jcomboStationIds.removeAllItems();
	}








	public void process(StationTimeSeriesFeatureCollection stsfc) throws IOException, WCTException, NoSharedTimeDimensionException {

		this.stsfc = stsfc;
		stationList = stsfc.getStations();

		jcomboStationIds.removeAllItems();
		String[] stationListingArray = new String[stationList.size()];
		for (int n=0; n<stationList.size(); n++) {
			stationListingArray[n] = LocationTimeseriesUtils.getStationListingString(stationList.get(n));
		}
		jcomboStationIds.setModel(new DefaultComboBoxModel<String>(stationListingArray));		// init variables
		jcomboVariables.setModel(new DefaultComboBoxModel<String>(LocationTimeseriesUtils.getStationPlottableVariables(stsfc, stationList.get(0))));
		// this.currentVariableUnits = getStationUnits(stationList.get(0));


		//        AttributeTypeFactory.newAttributeType("geom", Geometry.class),
		//        AttributeTypeFactory.newAttributeType("station_id", String.class),
		//        AttributeTypeFactory.newAttributeType("station_desc", String.class),
		//        AttributeTypeFactory.newAttributeType("lat", Double.class),
		//        AttributeTypeFactory.newAttributeType("lon", Double.class),
		//        AttributeTypeFactory.newAttributeType("alt", Double.class)


		// update on map
		FeatureCollection viewerStationPointFeatures = viewer.getStationPointFeatures();
		viewerStationPointFeatures.clear();
		viewer.clearData();


		String shortName = jcomboVariables.getSelectedItem().toString().split("\\s+")[0];
		Variable var = ((FeatureDatasetPoint)fd).getNetcdfFile().findVariable(shortName);
		if (var == null) {
			throw new WCTException("The variable "+jcomboVariables.getSelectedItem().toString()+" cannot be added to chart.");
		}
		// build lookup cache if needed
		Attribute shapeAtt = var.findAttribute("shape_lookup");
		Attribute shapeAttributeAtt = var.findAttribute("shape_lookup_id_attribute");

		FeatureCache featureCache = null;
		if (shapeAtt != null) {
			String shapefileName = shapeAtt.getStringValue();
			System.out.println("Variable: "+var.getNameAndDimensions()+" : shape_lookup="+shapeAtt.getStringValue()+
					"  id attribute: "+shapeAttributeAtt.getStringValue());

			URL mapDataURL = new URL(WCTConstants.MAP_DATA_JAR_URL);
			URL url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/"+shapefileName, null);
			ShapefileDataStore ds = new ShapefileDataStore(url);
			featureCache = new FeatureCache(ds.getFeatureSource(shapefileName.substring(0, shapefileName.lastIndexOf("."))), shapeAttributeAtt.getStringValue());
			System.out.println(featureCache.getDupKeyFeatures().length + " DUPLICATE FEATURES EXCLUDED FROM CACHE");
			System.out.println(featureCache.getNoKeyFeatures().length + " FEATURES MISSING KEY ATTRIBUTE AND EXCLUDED FROM CACHE");


			isPolygonShapeLookup = true;
			viewer.getStationPointMapLayer().setStyle(WCTStyleUtils.getBlankTransparentStyle());
		}
		else {
			isPolygonShapeLookup = false;
			viewer.getStationPointMapLayer().setStyle(WCTStyleUtils.getDefaultStyle());

		}











		ArrayList<Feature> tmpList = new ArrayList<Feature>(stationList.size());
		tmpList.ensureCapacity(stationList.size());
		System.out.println("tmpList.size(): "+tmpList.size()+" stationList.size():"+stationList.size());

		int geoIndex = 0;
		for (Station station : stationList) {

			try {

				Geometry geom = null;
				if (shapeAtt != null) {
					Feature f = featureCache.getFeature(station.getName());
					if (f == null) {
						System.err.println("FeatureCache is null for "+station.getName() + "("+station.getLongitude()+", "+station.getLatitude()+")");
						geom = geoFactory.createPoint(new Coordinate(station.getLongitude(), station.getLatitude()));
						//						geoIndex++; // must increment so it matches the station index
						//						continue;
					}
					else {

						//						if (Integer.parseInt(f.getAttribute("DIVISION_I").toString()) == 3006) {
						//							System.out.println("mark: found 3006 in feature lookup: "+station.getName());
						//						}

						geom = f.getDefaultGeometry();

						//						System.out.println("checking for duplications for: "+station.getName());
						// check for 'duplicate' features that need to be joined (i.e. islands that should have been a multipolygon, but are rather
						for (Feature df : featureCache.getDupKeyFeatures()) {
							//							if (station.getName().equals("1501")) {
							//								System.out.println("checking for duplications for: "+station.getName() +" vs "+ df.getAttribute("DIVISION_I"));
							//							}

							if (df.getAttribute(shapeAttributeAtt.getStringValue()) != null && 
									df.getAttribute(shapeAttributeAtt.getStringValue()).toString().equals(station.getName())) {

								System.out.println("  ... merging geometry for: "+df.getAttribute(shapeAttributeAtt.getStringValue()));
								geom = geom.union(df.getDefaultGeometry());
							}
						}
					}
				}
				else {
					geom = geoFactory.createPoint(new Coordinate(station.getLongitude(), station.getLatitude()));
				}


				//				public final static AttributeType[] POINT_ATTRIBUTES = {
				//						AttributeTypeFactory.newAttributeType("geom", Geometry.class),
				//						AttributeTypeFactory.newAttributeType("value", Double.class),
				//						AttributeTypeFactory.newAttributeType("station_id", String.class),
				//						AttributeTypeFactory.newAttributeType("station_desc", String.class),
				//						AttributeTypeFactory.newAttributeType("lat", Double.class),
				//						AttributeTypeFactory.newAttributeType("lon", Double.class),
				//						AttributeTypeFactory.newAttributeType("alt", Double.class),
				//						AttributeTypeFactory.newAttributeType("label", String.class)
				//					};

				Feature feature = schema.create(new Object[] {
						geom,
						new Double(-99999.999),
						station.getName(), 
						station.getDescription(), 
						new Double(station.getLatitude()), new Double(station.getLongitude()),
						new Double(station.getAltitude()), ""
				}, new Integer(geoIndex++).toString());

				//			viewerStationPointFeatures.add(feature);
				tmpList.add(feature);

				//				System.out.println(station);
				//				System.out.println(station.getName()+" .... "+station.getDescription());
				//				System.out.println(feature);


			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Error adding station: "+station.getName()+" ("+station.getDescription()+")");
				return;
			}
		}

		viewerStationPointFeatures.addAll(tmpList);




		//		get unique date list		
		try {
			String varName = jcomboVariables.getSelectedItem().toString().split("\\s+")[0];
			Variable currentVariable = fd.getNetcdfFile().findVariable(varName);
			uniqueDateListForSelectedVariable = LocationTimeseriesUtils.getUniqueTimeList(((FeatureDatasetPoint)fd), currentVariable);
			//			System.out.println(currentVariable);

			int previousSelectedDateIndex = jcomboUniqueDates.getSelectedIndex();			
			jcomboUniqueDates.setModel(new DefaultComboBoxModel<String>(
					uniqueDateListForSelectedVariable.toArray(new String[uniqueDateListForSelectedVariable.size()])
					));
			jcomboUniqueDates.setEnabled(true);

			// plot first variable and time
			jcomboUniqueDates.setSelectedIndex(previousSelectedDateIndex > 0 ? previousSelectedDateIndex : 0);

		} catch (NoSharedTimeDimensionException e) {
			e.printStackTrace();
			jcomboUniqueDates.setModel(new DefaultComboBoxModel<String>(new String[] { "Not Supported (Requires Shared Time Dimension)" } ));
			jcomboUniqueDates.setEnabled(false);
		}


		if (stationList.size() == 1) {
			loadCurrentlySelectedStation();
		}
	}


	//	private void getTimeSlice(Variable var, int timeIndex) {
	//		//		var.getDi
	//	}





	public void setViewerExtentIfNotVisible(Rectangle2D currentExtent, Station s) {
		double lat = s.getLatitude();
		double lon = s.getLongitude();

		if (! currentExtent.contains(lon, lat)) {
			setViewerExtent(s);
		}
	}



	public void setViewerExtent(Station s) {
		double lat = s.getLatitude();
		double lon = s.getLongitude();
		double extentBuffer = 4.0;
		viewer.setCurrentExtent(new Rectangle.Double(lon-extentBuffer, lat-extentBuffer, extentBuffer*2, extentBuffer*2));
	}

	public void setViewerSelectedStation(Station station) {

		try {

			viewer.getStationPointSelectedFeatures().clear();

			//			Feature feature = schema.create(new Object[] {
			//					geoFactory.createPoint(new Coordinate(station.getLongitude(), station.getLatitude())),
			//					station.getName(), 
			//					station.getDescription(), 
			//					new Double(station.getLatitude()), new Double(station.getLongitude()),
			//					new Double(station.getAltitude()), new Double(-1), 
			//					station.getName() + " ("+station.getDescription()+")"
			//			}, new Integer(0).toString());

			FeatureIterator iter = viewer.getStationPointFeatures().features();
			while (iter.hasNext()) {
				Feature f = iter.next();
				if (f.getAttribute("station_id").toString().equals(station.getName())) {
					viewer.getStationPointSelectedFeatures().add(f);
				}
				//				else {
				//					"Attempt to set selected station failed"
				//				}
			}

			if (isPolygonShapeLookup) {
				viewer.getStationPointSelectedMapLayer().setStyle(WCTStyleUtils.getDefaultSelectedPolygonStyle());
			}
			else {
				viewer.getStationPointSelectedMapLayer().setStyle(WCTStyleUtils.getDefaultSelectedPointStyle());
			}

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error adding station selection: "+station.getName()+" ("+station.getDescription()+")");
			return;
		}

	}

	/**
	 * Limited to 2 deg search box around selected location
	 * @param fromLocation
	 */
	public void selectClosestStation(Point2D fromLocation) {
		try {

			double searchRange = 1.0;
			LatLonRect latlonRect = new LatLonRect(
					new LatLonPointImpl(fromLocation.getY()-searchRange, fromLocation.getX()-searchRange),
					new LatLonPointImpl(fromLocation.getY()+searchRange, fromLocation.getX()+searchRange));

			System.out.print("before search based station subset...");
			List<Station> stations = this.stsfc.getStations(latlonRect);
			System.out.println("   done.");

			Station closestStation = null;
			double closestDistance = 10000000;
			int index = 0;
			for (Station s : stations) {
				double dist = fromLocation.distance(s.getLongitude(), s.getLatitude());
				if (dist < closestDistance) {
					closestStation = s;
					closestDistance = dist;
				}
				index++;
			}

			//			int stationIndex = this.stsfc.getStations().indexOf(closestID);
			//			Station selectedStation = this.stsfc.getStation(closestID);

			//			jcomboStationIds.setSelectedIndex(closestIndex);

			System.out.println("fromLocation: "+fromLocation);
			//			System.out.println("stationList: "+stationList);
			System.out.println("closestStation: "+closestStation);
			//			System.out.println("stationIndex: "+stationIndex);
			//			System.out.println("selectedStation: "+selectedStation);

			jcomboStationIds.setSelectedItem(LocationTimeseriesUtils.getStationListingString(closestStation));


		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error selecting closest station: "+fromLocation);
			return;
		}
	}






	//	public void readUniqueTime(String time) throws IOException, NoDataException {
	//
	//		PointFeatureCollection pfc = this.stsfc.flatten(null, new CalendarDateRange(
	//				CalendarDate.parseISOformat(null, jcomboUniqueDates.getSelectedItem().toString()), 100));
	//
	//		PointFeatureIterator pfIter = pfc.getPointFeatureIterator(1*1024*1024);
	//		processCDMPointFeatureIterator(pfIter);
	//	}

	public void readUniqueTime(int timeIndex) throws IOException, NoDataException, InvalidRangeException, NoSuchElementException, IllegalAttributeException {

		viewer.getStationPointSelectedFeatures().clear();

		List<Variable> varList = ((FeatureDatasetPoint)fd).getNetcdfFile().getVariables();

		// 1. find coord. variable that has standard_name='time'
		// 2. then find the dimension  used in that coord. variable
		// 3. then select the variables that use this dimension
		// 4. subset those variables based on the time index
		List<String> dimNameList = new ArrayList<String>();
		for (Variable var : varList) {
			Attribute att = var.findAttribute("standard_name");
			if (att != null && att.getStringValue().equals("time")) {
				System.out.println("found a time variable named: "+var.getShortName());
				if (var.getRank() != 1) {
					throw new NoDataException("time coord. variable is not 1-D");
				}
				dimNameList.add(var.getDimension(0).getShortName());
			}
		}

		if (dimNameList.size() == 0) {
			throw new NoDataException("no time dimension found");
		}
		// just get variables with time
		ArrayList<Variable> varListWithTime = new ArrayList<Variable>();
		varListWithTime.ensureCapacity(varList.size());
		for (Variable var : varList) {
			for (String dimName : dimNameList) {
				if (var.findDimensionIndex(dimName) >= 0) {
					varListWithTime.add(var);
				}
			}
		}

		System.out.println(dimNameList.toString());
		System.out.println(varListWithTime.toString());



		System.out.println("reading data slice...");
		int timeDimIdx = -1;
		String shortName = jcomboVariables.getSelectedItem().toString().split("\\s+")[0];
		Variable var = ((FeatureDatasetPoint)fd).getNetcdfFile().findVariable(shortName);
		for (String dimName : dimNameList) {
			if (var.findDimensionIndex(dimName) >= 0) {
				timeDimIdx = var.findDimensionIndex(dimName);
			}
		}


		currentVariable = var;
		currentDataSliceDateString = uniqueDateListForSelectedVariable.get(timeIndex);

		double[] stationValues = (double[])(var.slice(timeDimIdx, timeIndex).read().get1DJavaArray(Double.class));

		Attribute missingValueAtt = var.findAttribute("missing_value");
		double missingValue = Double.MAX_VALUE;
		double fillValue = Double.MAX_VALUE;
		if (missingValueAtt != null) {
			fillValue = missingValueAtt.getNumericValue().doubleValue();
			missingValue = missingValueAtt.getNumericValue().doubleValue();
		}
		Attribute fillValueAtt = var.findAttribute("_FillValue");
		if (fillValueAtt != null) {
			fillValue = fillValueAtt.getNumericValue().doubleValue();
			missingValue = fillValueAtt.getNumericValue().doubleValue();
		}




		// read valid min/max
		Attribute validMinAtt = var.findAttribute("valid_min");
		Attribute validMaxAtt = var.findAttribute("valid_max");
		if (validMinAtt != null && validMaxAtt != null) {
			hasValidRange = true;
			validRangeMinValue = validMinAtt.getNumericValue().doubleValue();
			validRangeMaxValue = validMaxAtt.getNumericValue().doubleValue();
			if (viewer.getMapSelector().isLocationStationValidRangeMinMaxSelected()) {
				viewer.getMapSelector().setLocationStationColorTableMinMaxValue(validRangeMinValue, validRangeMaxValue);
			}
		}
		else {
			hasValidRange = false;
			validRangeMinValue = Double.NaN;
			validRangeMaxValue = Double.NaN;
		}





		System.out.println(missingValueAtt);
		System.out.println(fillValueAtt);



		StringBuilder sb = new StringBuilder();

		System.out.println("setting feature attribute with value...");
		FeatureCollection viewerStationPointFeatures = viewer.getStationPointFeatures();
		FeatureIterator iter = viewerStationPointFeatures.features();




		textArea.setText("");
		DefaultTableModel tableModel = null;
		boolean firstTime = true;
		//		int idx = 0;
		while (iter.hasNext()) {
			final Feature f = iter.next();

			if (firstTime) {

				columnNames = new String[f.getNumberOfAttributes()];
				columnInfo = new String[f.getNumberOfAttributes()];
				for (int n=0; n<f.getNumberOfAttributes(); n++) {
					//					System.out.println(f.getFeatureType().getAttributeType(n).getName());

					AttributeType attType = f.getFeatureType().getAttributeType(n);
					columnNames[n] = attType.getName();
					columnInfo[n] = attType.getName()+"<br>type="+attType.getType().getSimpleName();
				}
				//				System.out.println("Initing table model: "+stationValues.length+" x "+columnNames.length + " : "+Arrays.toString(columnNames));

				//				public final static AttributeType[] POINT_ATTRIBUTES = {
				//	0					AttributeTypeFactory.newAttributeType("geom", Geometry.class),
				//	1					AttributeTypeFactory.newAttributeType("value", Double.class),
				//	2					AttributeTypeFactory.newAttributeType("station_id", String.class),
				//	3					AttributeTypeFactory.newAttributeType("station_desc", String.class),
				//	4					AttributeTypeFactory.newAttributeType("lat", Double.class),
				//	5					AttributeTypeFactory.newAttributeType("lon", Double.class),
				//	6					AttributeTypeFactory.newAttributeType("alt", Double.class),
				//	7					AttributeTypeFactory.newAttributeType("label", String.class)
				//					};



				tableModel = new DefaultTableModel(stationValues.length, f.getNumberOfAttributes()) {
					@Override
					public Class getColumnClass(int column) {
						switch (column) {
						case 1:
						case 4:
						case 5:
						case 6:
							return Double.class;
						default:
							return String.class;
						}
					}
					@Override
					public String getColumnName(int column) {
						//		            	System.out.println("returning column name "+column+" = "+columnNames[column]);
						if (column == 0) {
							return "time";
						}
						else if (column == 1) {
							return currentVariable.getShortName();
						}
						else {
							return columnNames[column];
						}
					}
					@Override
					public boolean isCellEditable(int row, int column) {
						return false;
					}

				};

				firstTime = false;				
			}

			// if we had no data to read, firstTime will still be false
			if (firstTime) {
				throw new NoDataException("Station feature collection is empty.");
			}



			//			if (idx > 310) {
			//				System.out.println("mark: idx>310: "+stationValues[idx]);
			//			}

			// Feature ID should match array index value of station/location dimension
			int geoIndex = Integer.parseInt(f.getID());

			f.setAttribute("value", new Double(stationValues[geoIndex]));
			f.setAttribute("label", WCTUtils.DECFMT_0D0.format(stationValues[geoIndex]));

			if (Double.isNaN(stationValues[geoIndex])) {
				f.setAttribute("label", "");
				//				idx++;
				//				continue;				
			}

			if (missingValueAtt != null && stationValues[geoIndex] == missingValueAtt.getNumericValue().doubleValue()) {
				f.setAttribute("label", "");
				//				idx++;
				//				continue;
			}

			if (fillValueAtt != null && stationValues[geoIndex] == fillValueAtt.getNumericValue().doubleValue()) {
				f.setAttribute("label", "");
				//				idx++;
				//				continue;
			}




			//			public final static AttributeType[] POINT_ATTRIBUTES = {
			//					AttributeTypeFactory.newAttributeType("geom", Geometry.class),
			//					AttributeTypeFactory.newAttributeType("value", Double.class),
			//					AttributeTypeFactory.newAttributeType("station_id", String.class),
			//					AttributeTypeFactory.newAttributeType("station_desc", String.class),
			//					AttributeTypeFactory.newAttributeType("lat", Double.class),
			//					AttributeTypeFactory.newAttributeType("lon", Double.class),
			//					AttributeTypeFactory.newAttributeType("alt", Double.class),
			//					AttributeTypeFactory.newAttributeType("label", String.class)
			//				};

			//			if (geoIndex > 310) 
			//				System.out.println(">310 geoindex");
			//			System.out.println(jcomboUniqueDates.getItemAt(timeIndex) + " " + stationValues[geoIndex]);
			//			System.out.println(f);

			// set table values here!
			tableModel.setValueAt(jcomboUniqueDates.getItemAt(timeIndex), geoIndex, 0);
			sb.append(jcomboUniqueDates.getItemAt(timeIndex));
			for (int c=1; c<f.getNumberOfAttributes(); c++) {
				String data = f.getAttribute(c).toString();

				if (c == 1 || c == 4 || c == 5 || c == 6) {
					//					if (c == 1) System.out.println("idx="+geoIndex+" "+data);
					try {
						tableModel.setValueAt(new Double(data), geoIndex, c);
					} catch (Exception e) {
						tableModel.setValueAt(Double.NaN, geoIndex, c);
					}
				}
				else if (c == 2 || c == 3) {
					// add some padding to this column for visual purposes
					tableModel.setValueAt("  "+data, geoIndex, c);
				}
				else {
					tableModel.setValueAt(data, geoIndex, c);
				}
				sb.append(", "+data);
			}
			sb.append("\n");



			//			System.out.println(tableModel.getValueAt(idx, 0) + " | " + tableModel.getValueAt(idx, 1));


			if (cancel) {
				return;
			}


			if (geoIndex % 100 == 0) {
				System.out.println(".... processed "+geoIndex+" rows");
				textArea.append(sb.toString());
				sb.setLength(0);
			}			


			

			//			idx++;
		}

		// add remaining text
		textArea.append(sb.toString());
		sb.setLength(0);

		dataTable.setModel(tableModel);
		dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		dataTable.setRowSelectionAllowed(true);
		currentDataTableViewType = DataTableViewType.STATION_TIMESLICE;
		jlDoubleClickInstructions.setText("[ double-click on location to load time series ]");

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(dataTable.getModel());
		dataTable.setRowSorter(sorter);


		List<RowSorter.SortKey> sortKeys = new ArrayList<>();
		//         sortKeys.add(new RowSorter.SortKey(4, SortOrder.ASCENDING));
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);		

		System.out.println("after setTableModel cols="+Arrays.toString(columnNames));




		WCTUiUtils.autoFitColumns(dataTable, columnNames);

		System.out.println("after autofit");






		System.out.println("setting map layer style");
		lastProcessedMinValue = Double.MAX_VALUE;
		lastProcessedMaxValue = Double.MIN_VALUE;
		for (double v : stationValues) {
			//			System.out.println(v + " fill="+fillValue+"  missingValue="+missingValue);

			if (! Double.isNaN(v) &&
					! isRelativelyEqual(v, missingValue) && ! isRelativelyEqual(v, fillValue)) {
				lastProcessedMinValue = Math.min(lastProcessedMinValue, v);
				lastProcessedMaxValue = Math.max(lastProcessedMaxValue, v);
				//				System.out.println("minVal="+minVal+"  maxVal="+maxVal);
			}
		}

		try {
			System.out.println(viewer.getMapSelector().getLocationStationColorTableName());
			viewer.setLocationStationColorTable(viewer.getMapSelector().getLocationStationColorTableName());

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("done");
	}

	public static boolean isEqual(double d1, double d2) {
		return d1 == d2 || isRelativelyEqual(d1,d2);
	}

	private static boolean isRelativelyEqual(double d1, double d2) {
		double delta = 0.001;
		return delta > Math.abs(d1- d2) / Math.max(Math.abs(d1), Math.abs(d2));
	}

	public String getCurrentDataSliceDateString() {
		return this.currentDataSliceDateString;
	}

	public Variable getCurrentVariable() {
		return this.currentVariable;
	}

	public boolean isCurrentPolygonLookup() {
		return this.isPolygonShapeLookup;
	}





	public void readStationData(String id) throws WCTException, IOException, NoDataException {
		readStationData(stsfc.getStation(id));
	}
	public void readStationData(Station station) throws WCTException, IOException, NoDataException {
		if (this.stsfc == null) {
			throw new WCTException("No data file has been loaded");
		}

		// for (Station station : stationList) {
		StationTimeSeriesFeature sf = stsfc.getStationFeature(station);

		System.out.println("Station: "+station.toString());
		System.out.println("Location: "+sf.getLatLon());
		textArea.append("## Station: "+station.toString()+" -- "+sf.getLatLon() +" \n");



		PointFeatureIterator pfIter = sf.getPointFeatureIterator(1*1024*1024*1024);
		processCDMPointFeatureIterator(pfIter, stsfc.getTimeUnit(), jcomboUniqueDates.getModel().getSize());
	}



	private void processCDMPointFeatureIterator(PointFeatureIterator pfIter, DateUnit dateUnit, int numberOfRows) throws NoDataException, IOException {



		DefaultTableModel tableModel = null;
		int c = 1;

		cancel = false;

		int row = 0;
		boolean firstTime = true;
		StringBuilder sb = new StringBuilder();

		textArea.setText("");


		// iterate through data for each station
		while (pfIter.hasNext()) {
			PointFeature pf = pfIter.next();

			// System.out.println( pf.getObservationTimeAsDate() + " -- " + pf.getLocation().toString());
			StructureData sdata = pf.getDataAll();
			StructureMembers smembers = sdata.getStructureMembers();
			// System.out.println( smembers.getMemberNames().toString() );


			if (firstTime) {
				List<String> nameList = smembers.getMemberNames();
				columnNames = new String[nameList.size()+1];
				columnInfo = new String[nameList.size()+1];
				final Class[] colTypes = new Class[nameList.size()+1];
				//                2010-03-23T12:34:00Z
				textArea.append("#datetime           ");
				columnNames[0] = "Date/Time";
				columnInfo[0] = "Date/Time<br>Time Zone: UTC";
				colTypes[0] = String.class;

				for (int n=0; n<nameList.size(); n++) {
					columnNames[n+1] = nameList.get(n);
					Member member = smembers.getMember(n);
					textArea.append(", "+member.getName());
					columnInfo[n+1] = member.getName()+"<br>"+
							member.getDescription()+"<br>"+
							member.getUnitsString();
					DataType dt = member.getDataType();
					colTypes[n+1] = dt.getPrimitiveClassType();
					System.out.println(dt + " prim="+dt.getPrimitiveClassType());
				}

				textArea.append("\n");
				// jeksTable.getTableHeader().getColumnModel().getColumn(0).setHeaderValue("Date");
				// Force the header to resize and repaint itself
				// jeksTable.getTableHeader().resizeAndRepaint();


				//				System.out.println("Initing table model: "+jcomboUniqueDates.getModel().getSize()+" x "+columnNames.length + " : "+Arrays.toString(columnNames));
				tableModel = new DefaultTableModel(numberOfRows, columnNames.length) {
					@Override
					public Class getColumnClass(int column) {
						//						return colTypes[column];
						if (colTypes[column].toString().equals("float") || colTypes[column].toString().equals("double")) {
							return Double.class;
						}
						else if (colTypes[column].toString().equals("short") || colTypes[column].toString().equals("int")
								|| colTypes[column].toString().equals("byte") || colTypes[column].toString().startsWith("unsigned")) {
							return Integer.class;
						}
						else {
							return String.class;
						}
					}
					@Override
					public boolean isCellEditable(int row, int column) {
						return false;
					}
				};

				firstTime = false;
			}




			//			textArea.append(stsfc.getTimeUnit().makeStandardDateString( pf.getObservationTime() ));
			sb.append(dateUnit.makeStandardDateString( pf.getObservationTime() ));
			tableModel.setValueAt(dateUnit.makeStandardDateString( pf.getObservationTime() ), row, 0);
			c = 1;
			for (String col : smembers.getMemberNames()) {
				String data = sdata.getScalarObject(col).toString();
				WCTUiUtils.setCleanTableModelValue(tableModel, row, c, data);



				//				 System.out.print(col+"["+c+"]="+data+" ");
				// ---- textArea.append(","+data + getPad(col, data));



				//				textArea.append(", "+data);
				sb.append(", "+data);
				c++;
			}
			//			textArea.append("\n");
			sb.append("\n");


			if (cancel) {
				return;
			}


			row++;

			if (row % 100 == 0) {
				System.out.println(".... processed "+row+" rows");
				textArea.append(sb.toString());
				sb.setLength(0);
			}
		}

		// add remaining text
		textArea.append(sb.toString());
		sb.setLength(0);

		// only format if there is not a excess of data
		if (row < 10000) {
			WCTUiUtils.formatTextArea(textArea);
		}
		textArea.setForeground(Color.BLACK);




		if (columnNames == null) {
			throw new NoDataException("No Data Found");
		}
		//		textArea.append(sb.toString());

		// System.out.println("before tablemodel copy");
		//		JeksTableModel jeksTableModel2 = new JeksTableModel(row, c, columnNames);
		//		for (int i=0; i<c; i++) {
		//			for (int j=0; j<row; j++) {
		//				jeksTableModel2.setValueAt(jeksTableModel.getValueAt(j, i), j, i);
		//			}
		//		}
		// System.out.println("after tablemodel copy");

		//		jeksTable.setModel(jeksTableModel2);
		dataTable.setModel(tableModel);
		dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		currentDataTableViewType = DataTableViewType.STATION_TIMESERIES;
		jlDoubleClickInstructions.setText("[ double-click on date to load and map data for all locations ]");

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(dataTable.getModel());
		dataTable.setRowSorter(sorter);

		List<RowSorter.SortKey> sortKeys = new ArrayList<>();
		//         sortKeys.add(new RowSorter.SortKey(4, SortOrder.ASCENDING));
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);		


		System.out.println("after setTableModel cols="+Arrays.toString(columnNames));
		WCTUiUtils.autoFitColumns(dataTable, columnNames);

		// textArea.append("\nDONE PROCESSING: "+station.getName());
		ColumnHeaderToolTips tips = new ColumnHeaderToolTips();
		for (int i = 0; i < dataTable.getColumnCount(); i++) {
			TableColumn col = dataTable.getColumnModel().getColumn(i);
			tips.setToolTip(col, "<html>"+columnInfo[i]+"</html>");
			col.setHeaderValue(columnNames[i]);
		}
		dataTable.getTableHeader().addMouseMotionListener(tips);
	}





	public Color[] getCurrentSymbolizedColors() {
		return this.currentSymbolizedColors;
	}
	public void setCurrentSymbolizedColors(Color[] currentSymbolizedColors) {
		this.currentSymbolizedColors = currentSymbolizedColors;
	}

	public Double[] getCurrentSymbolizedValues() {
		return this.currentSymbolizedValues;
	}
	public void setCurrentSymbolizedValues(Double[] currentSymbolizedValues) {
		this.currentSymbolizedValues = currentSymbolizedValues; 
	}



















	public FeatureType getFeatureType() {
		return schema;
	}

	public double getLastProcessedMinValue() {
		return lastProcessedMinValue;
	}
	public double getLastProcessedMaxValue() {
		return lastProcessedMaxValue;
	}
	public double getLastProcessedValidRangeMinValue() {
		return validRangeMinValue;
	}
	public double getLastProcessedValidRangeMaxValue() {
		return validRangeMaxValue;
	}
	public boolean hasValidRangeAttributes() {
		return hasValidRange;
	}

	public DataTableViewType getCurrentDataTableViewType() {
		return currentDataTableViewType;
	}


	class ColumnHeaderToolTips extends MouseMotionAdapter {
		TableColumn curCol;
		Map<TableColumn, String> tips = new HashMap<TableColumn, String>();
		public void setToolTip(TableColumn col, String tooltip) {
			if (tooltip == null) {
				tips.remove(col);
			} else {
				tips.put(col, tooltip);
			}
		}
		public void mouseMoved(MouseEvent evt) {
			JTableHeader header = (JTableHeader) evt.getSource();
			JTable table = header.getTable();
			TableColumnModel colModel = table.getColumnModel();
			int vColIndex = colModel.getColumnIndexAtX(evt.getX());
			TableColumn col = null;
			if (vColIndex >= 0) {
				col = colModel.getColumn(vColIndex);
			}
			if (col != curCol) {
				header.setToolTipText((String) tips.get(col));
				curCol = col;
			}
		}
	}




	class NoDataException extends Exception {
		public NoDataException(String message) {
			super(message);
		}
	}



	public FeatureType getCurrentFeatureType() {
		if (currentDataTableViewType == DataTableViewType.STATION_TIMESERIES ||
				currentDataTableViewType == DataTableViewType.STATION_TIMESLICE) {
			return schema;
		}
		else {
			Feature f = viewer.getStationPointFeatures().features().next();
			return f.getFeatureType();
		}
	}

	public String getCurrentStationFeatureAttribute() {
		if (currentDataTableViewType == DataTableViewType.STATION_TIMESERIES ||
				currentDataTableViewType == DataTableViewType.STATION_TIMESLICE) {
			return "value";
		}
		else {
			Feature f = viewer.getStationPointFeatures().features().next();
			System.out.println(f.getFeatureType().toString());
			System.out.println("getting attribute name from index "+jcomboVariables.getSelectedIndex());
			// first index is always 'geom'
			String attName = f.getFeatureType().getAttributeType(jcomboVariables.getSelectedIndex()+1).getName();
			System.out.println("found: "+attName);
			return attName;
		}
	}


}

