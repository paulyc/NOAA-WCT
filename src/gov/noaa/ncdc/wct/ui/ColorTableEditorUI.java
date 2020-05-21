package gov.noaa.ncdc.wct.ui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

import javax.media.jai.WritableRenderedImageAdapter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.io.FileUtils;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.cv.SampleDimension;
import org.geotools.gc.GridCoverage;

import gov.noaa.ncdc.common.OpenFileFilter;
import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.decoders.ColorLutReaders;
import gov.noaa.ncdc.wct.decoders.ColorsAndValues;
import gov.noaa.ncdc.wct.decoders.SampleDimensionAndLabels;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradSampleDimensionFactory;

public class ColorTableEditorUI extends JDialog {

    private WCTViewer viewer = null;
    private String initialColorTableName = null;
    private JTextArea textArea = new JTextArea(25, 60);

    private boolean isProcessCanceled = false;

    public final static ImageIcon LOADING_ICON = new ImageIcon(WCTViewer.class.getResource("/icons/ajax-loader.gif"));     

	public static final File CACHE_FOLDER = new File(WCTConstants.getInstance().getCacheLocation()+File.separator+
			"config"+File.separator+"user-colortables");

    
    public ColorTableEditorUI(WCTViewer viewer, String initialColorTableName) {
        super(viewer, "Color Table Editor");
        this.viewer = viewer;
        this.initialColorTableName = initialColorTableName;
        createUI();
    }

    private void createUI() {
    	
    	final Dialog finalThis = this;

    	this.setLayout(new RiverLayout());
    	
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setCaretPosition(0);

//        SampleDimension sd = viewer.getRadarGridCoverage().getSampleDimensions()[0];

        
        
        if (! CACHE_FOLDER.exists()) {
        	CACHE_FOLDER.mkdirs();
        }
        File[] colorTableFiles = CACHE_FOLDER.listFiles();

        
        
        String defaultPalName = NexradSampleDimensionFactory.getDefaultPaletteName(viewer.getNexradHeader().getProductCode(), viewer.getNexradHeader().getVersion());
		BufferedReader br = null;
		try {
			
			if (initialColorTableName.equalsIgnoreCase("Default")) {
				
				
				URL url = ResourceUtils.getInstance().getJarResource(
	        			new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
	        			"/config/colormaps/"+defaultPalName, null);
				br = new BufferedReader(new InputStreamReader(url.openStream()));
				NexradSampleDimensionFactory.removePaletteOverride(defaultPalName);
			}
			else {

				File cacheFolder = new File(WCTConstants.getInstance().getCacheLocation()+File.separator+
						"config"+File.separator+"user-colortables");

				File customPalFile = new File(cacheFolder.toString() + File.separator + initialColorTableName);
				br = new BufferedReader(new StringReader(FileUtils.readFileToString(customPalFile)));
			}

			

			String str = null;
			while ( (str = br.readLine()) != null) {
				textArea.append(str+"\n");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

    
//		viewer.addRenderCompleteListener(new RenderCompleteListener() {
//			@Override
//			public void renderComplete() {
//				try {
//					update();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//
//			@Override
//			public void renderProgress(int progressPercent) {
//			}
//		});
		
        
        
        
        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					update();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (WCTException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
        });
        
        JComboBox<String> nameCombo = new JComboBox<String>();
        nameCombo.setEditable(true);
        nameCombo.setPreferredSize(new Dimension(220, (int)nameCombo.getPreferredSize().getHeight()));
        for (File f : colorTableFiles) {
        	nameCombo.addItem(f.getName());
        }

		if (initialColorTableName.equalsIgnoreCase("Default")) {
			nameCombo.setSelectedItem("<enter name>");
		}
		else {
			nameCombo.setSelectedItem(initialColorTableName);
		}
        
        
        nameCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				
//		System.out.println("action on nameCombo");
				
			if (nameCombo.getSelectedItem() == null || nameCombo.getSelectedItem().toString().trim().length() == 0 
				|| nameCombo.getSelectedItem().toString().trim().equals("<enter name>")) {
					return;
				}
				String filename = nameCombo.getSelectedItem().toString().trim();
				File file = new File(CACHE_FOLDER+File.separator+filename);
				
				// a change of focus after typing in a new file name will trigger an action before the save button is pressed
				if (! file.exists()) {
					return;
				}
				
				
				try {
					String s = FileUtils.readFileToString(file);
					textArea.setText(s);
					update();
				} catch (Exception e) {
					e.printStackTrace();
					WCTUiUtils.showErrorMessage(finalThis, "Error saving this custom color table.", e);
				}
			}
        });
        
        nameCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent ie) {
				System.out.println("item state changed");
				try {
					viewer.getMapSelector().refreshRadarColorTableList();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        	
        });
        
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (nameCombo.getSelectedItem() == null || nameCombo.getSelectedItem().toString().trim().length() == 0 
						|| nameCombo.getSelectedItem().toString().trim().equals("<enter name>")) {
					nameCombo.setSelectedItem("<enter name>");
					return;
				}

				String filename = nameCombo.getSelectedItem().toString().trim();
				if (! filename.endsWith(".wctpal")) filename = filename+".wctpal";
				
				File file = new File(CACHE_FOLDER+File.separator+filename);
				try {
					FileUtils.writeStringToFile(file, textArea.getText());
					nameCombo.removeItem(filename);
		        	nameCombo.addItem(filename);
		        	nameCombo.setSelectedItem(filename);
				} catch (IOException e) {
					e.printStackTrace();
					WCTUiUtils.showErrorMessage(finalThis, "Error saving this custom color table.", e);
				}
			}
        });
        
        JButton saveAsButton = new JButton("Save As");
        saveAsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
	            try {

	                TextUtils.getInstance().save(finalThis, textArea.getText(), "wctpal", "WCT Color Palette File (.wctpal)", "wctpaldir");

	            } catch (Exception e) {
	                e.printStackTrace();
					WCTUiUtils.showErrorMessage(finalThis, "Error saving this custom color table.", e);
	            }
			}
		});
        

        JButton loadButton = new JButton("Load");
        loadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
	            try {

	    	        String wctprop = WCTProperties.getWCTProperty("wctpaldir");
	    	        File lastSaveFolder = null;
	    	        if (wctprop != null) {
	    	            lastSaveFolder = new File(wctprop);
	    	        }
	    	        else {
	    	            lastSaveFolder = new File(System.getProperty("user.home"));
	    	        }

	    			JFileChooser fc = new JFileChooser(lastSaveFolder);
	    	        fc.setDialogTitle("Open a WCT Color Table");
	    			fc.setAcceptAllFileFilterUsed(true);
	    			OpenFileFilter wctFilter = new OpenFileFilter("wctpal", true, "NOAA Weather and Climate Toolkit Favorites (.wctpal)");
	    			fc.addChoosableFileFilter(wctFilter);
	    	        fc.setFileFilter(wctFilter);
	    	        
	    	        
	    	        int returnVal = fc.showOpenDialog(finalThis);
	    	        fc.setDialogType(JFileChooser.SAVE_DIALOG);
	    	        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	    	            lastSaveFolder = fc.getSelectedFile().getParentFile();
	    	            
	    	            String str = FileUtils.readFileToString(fc.getSelectedFile());
	    	            textArea.setText(str);
	    	            
	    				WCTProperties.setWCTProperty("wctpaldir", lastSaveFolder.toString());
	    	        }
	            } catch (Exception e) {
	                e.printStackTrace();
					WCTUiUtils.showErrorMessage(finalThis, "Error saving this custom color table.", e);
	            }
			}
		});
        
        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
	            try {

	                TextUtils.getInstance().copyToClipboard(textArea.getText());

	            } catch (Exception e) {
	                e.printStackTrace();
					WCTUiUtils.showErrorMessage(finalThis, "Error copying this custom color table to clipboard.", e);
	            }
			}
		});

        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
	            try {
					if (nameCombo.getSelectedItem() == null || nameCombo.getSelectedItem().toString().trim().length() == 0 
							|| nameCombo.getSelectedItem().toString().trim().equals("<enter name>")) {
						nameCombo.setSelectedItem("<enter name>");
						return;
					}
	            	
					String filename = nameCombo.getSelectedItem().toString().trim();
					File file = new File(CACHE_FOLDER+File.separator+filename);
					if (! file.delete()) {
						throw new WCTException("Error deleting: "+file);
					}
					nameCombo.removeItem(filename);

	            } catch (Exception e) {
	                e.printStackTrace();
					WCTUiUtils.showErrorMessage(finalThis, "Error removing this custom color table.", e);
	            }
			}
		});

        
        this.add(new JScrollPane(textArea), "p hfill vfill");
//        this.add(updateButton, "p left");
        this.add(new JLabel("Custom Color Tables:"), "p left");
        this.add(nameCombo, "hfill");
        this.add(saveButton, "center br");
        this.add(saveAsButton);
        this.add(copyButton);
        this.add(loadButton);
        this.add(removeButton);
    }
    
    
    
    public void update() throws Exception {
    	
		ColorsAndValues[] cavArray = 
			ColorLutReaders.parseWCTPal(new BufferedReader(new StringReader(textArea.getText())));
		
		SampleDimensionAndLabels sd = ColorLutReaders.convertToSampleDimensionAndLabels(
				cavArray[0], cavArray[1]);
	
		viewer.getRadarLegendImageProducer().setSampleDimensionAndLabels(sd);
		
		GridCoverage gc = viewer.getRadarGridCoverage();
        WritableRenderedImageAdapter img = (WritableRenderedImageAdapter)(gc.getRenderedImage());
        WritableRaster data = (WritableRaster)img.getData();

		viewer.setRadarGridCoverage(new GridCoverage(
				gc.getName(null), data, GeographicCoordinateSystem.WGS84, null, gc.getEnvelope(), 
				new SampleDimension[] { sd.getSampleDimension() }));
				
		viewer.getLargeLegendPanel().setLegendImage(viewer.getRadarLegendImageProducer());
		
		gc.dispose();

		
		
		
        String palName = NexradSampleDimensionFactory.getDefaultPaletteName(viewer.getNexradHeader().getProductCode(), viewer.getNexradHeader().getVersion());
        NexradSampleDimensionFactory.setPaletteOverride(palName, cavArray);
		
		
//		WCTGridCoverageSupport.setSampleDimensionAlpha(new SampleDimension[] { sd }, alpha);

    }
}
