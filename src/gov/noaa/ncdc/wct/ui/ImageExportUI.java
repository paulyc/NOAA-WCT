package gov.noaa.ncdc.wct.ui;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jidesoft.hints.FileIntelliHints;
import com.jidesoft.swing.FolderChooser;
import com.jidesoft.swing.SelectAllUtils;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;
import gov.noaa.ncdc.wct.export.raster.WCTRasterExport;

public class ImageExportUI {
	
	private WCTViewer viewer;
	private String historyPropertyKey_Dir;
	private String historyPropertyKey_File;
	
	public ImageExportUI(WCTViewer viewer,
			String historyPropertyKey_Dir, String historyPropertyKey_File) {
		
		this.viewer = viewer;
		this.historyPropertyKey_Dir = historyPropertyKey_Dir;
		this.historyPropertyKey_File = historyPropertyKey_File;
	}


	public void showSaveImageDialog(final BufferedImage bimage) {

		final JTextField jtfOutDir = new JTextField(25);
		final JTextField jtfOutFile = new JTextField(10);
		final JTextField jtfDPI = new JTextField("72", 6);

        String dir = WCTProperties.getWCTProperty(historyPropertyKey_Dir);
        if (dir == null) {
            dir = "";
        }
        String file = WCTProperties.getWCTProperty(historyPropertyKey_File);
        if (file == null) {
        	file = "";
        }
        jtfOutDir.setText(dir);
        jtfOutFile.setText(file);
        
        // JIDE stuff
        jtfOutDir.setName("File IntelliHint");
        SelectAllUtils.install(jtfOutDir);
        new FileIntelliHints(jtfOutDir).setFolderOnly(true);

        
        
		final JComboBox<AWTImageExport.Type> jcomboFormat = new JComboBox<AWTImageExport.Type>(AWTImageExport.Type.values());
		jcomboFormat.removeItem(AWTImageExport.Type.ANIMATED_GIF);
		jcomboFormat.removeItem(AWTImageExport.Type.AVI);
		jcomboFormat.removeItem(AWTImageExport.Type.AVI_JPEG);
		jcomboFormat.removeItem(AWTImageExport.Type.AVI_PNG);
		jcomboFormat.removeItem(AWTImageExport.Type.AVI_RAW);
		jcomboFormat.removeItem(AWTImageExport.Type.AVI_RLE8);
		jcomboFormat.removeItem(AWTImageExport.Type.QUICKTIME);
		
		final JButton jbBrowse = new JButton("Browse");
		final Component finalThis = viewer;
		final String lastDir = dir;
		jbBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {                
                
                FolderChooser folderChooser = new FolderChooser() {
                    protected JDialog createDialog(Component parent)
                    	throws HeadlessException {
                    	JDialog dlg = super.createDialog(parent);
                    	dlg.setLocation((int)jbBrowse.getLocationOnScreen().getX()+jbBrowse.getWidth()+4, 
                    			(int)jbBrowse.getLocationOnScreen().getY());
                    	return dlg;
                    }
                };
                folderChooser.setRecentListVisible(false);
                folderChooser.setAvailableButtons(FolderChooser.BUTTON_REFRESH | FolderChooser.BUTTON_DESKTOP 
                		| FolderChooser.BUTTON_MY_DOCUMENTS);
                if (lastDir.length() > 0) {
                    File lastFolderFile = folderChooser.getFileSystemView().createFileObject(lastDir);
                    folderChooser.setCurrentDirectory(lastFolderFile);
                }
                folderChooser.setFileHidingEnabled(true);
                folderChooser.setPreferredSize(new Dimension(320, 500));
                int result = folderChooser.showOpenDialog(finalThis);
                if (result == FolderChooser.APPROVE_OPTION) {
                    File selectedFile = folderChooser.getSelectedFile();
                    if (selectedFile != null) {
                    	jtfOutDir.setText(selectedFile.toString());
                    }
                    else {
                    	jtfOutDir.setText("");
                    }

                }
                
            }
        });
		final JDialog exportDialog = new JDialog(viewer.getMapSelector(), "Saving Image...", ModalityType.DOCUMENT_MODAL);
		JPanel mainPanel = new JPanel();
//		mainPanel.setBorder(WCTUiUtils.myTitledTopBorder("Select Format and Location", 0, 5, 5, 5, TitledBorder.CENTER, TitledBorder.TOP));
		
		mainPanel.setLayout(new RiverLayout());
		mainPanel.add("p", new JLabel("Output Location: "));				
		mainPanel.add(jtfOutDir, "br left hfill");
		mainPanel.add(jbBrowse, "right");
		mainPanel.add(new JLabel("Format: "), "p left");
		mainPanel.add(jcomboFormat, "tab hfill");
		mainPanel.add(new JLabel("DPI (png/jpeg only): "), "p left");
		mainPanel.add(jtfDPI, "tab hfill");
		mainPanel.add(new JLabel("Filename: "), "p");
		mainPanel.add(jtfOutFile, "tab hfill");
		
		JButton jbExport = new JButton("Save");
		jbExport.setPreferredSize(new Dimension(60, (int)jbExport.getPreferredSize().getHeight()));		
		JButton jbCancel = new JButton("Cancel");
		jbCancel.setPreferredSize(new Dimension(60, (int)jbCancel.getPreferredSize().getHeight()));
		
		jbExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					doImageExportInBackground(bimage, jtfOutDir.getText(), jtfOutFile.getText(), 
							(AWTImageExport.Type)jcomboFormat.getSelectedItem(), Integer.parseInt(jtfDPI.getText()));

					exportDialog.dispose();
				} catch (Exception e) {
					reportException(e);
				}
			}			
		});		
		jbCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
//				process = false;
				exportDialog.dispose();
			}			
		});
		mainPanel.add(jbExport, "p center");
		mainPanel.add(jbCancel);
		
		exportDialog.add(mainPanel);
		
		exportDialog.pack();
		exportDialog.setLocationRelativeTo(viewer);
		exportDialog.setVisible(true);
		
		
		
//		System.out.println(jtfOutDir);
//		System.out.println(jtfOutFile);
//		System.out.println(jcomboFormat.getSelectedItem());
	}
	
	
	private void doImageExportInBackground(final BufferedImage bimage, final String outDir, final String outFile, 
			final AWTImageExport.Type format, final int dpi) throws Exception {
		
            foxtrot.Worker.post(new foxtrot.Task() {
                public Object run() {

                    try {
                    	doImageExport(bimage, outDir, outFile, format, dpi);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        reportException(ex);
                    }

                    return "DONE";
                }
            });

	}
	
	
	private void doImageExport(final BufferedImage bimage, final String outDir, String outFile, 
			final AWTImageExport.Type format, int dpi) throws WCTException {

		if (outFile == null || outFile.trim().length() == 0) {
			throw new WCTException("Please specify output file");
		}
		
        WCTProperties.setWCTProperty(this.historyPropertyKey_Dir, outDir);
        WCTProperties.setWCTProperty(this.historyPropertyKey_File, outFile);

		
		final String finalOutFile = outFile;
    	WCTRasterExport rasterExport = new WCTRasterExport();
    	rasterExport.addGeneralProgressListener(new GeneralProgressListener() {
			@Override
			public void started(GeneralProgressEvent event) {
//				progressBar.setString(event.getStatus());
			}
			@Override
			public void ended(GeneralProgressEvent event) {
//				progressBar.setValue(0);
//				progressBar.setString("");
			}
			@Override
			public void progress(GeneralProgressEvent event) {
//				progressBar.setValue((int)event.getProgress());
//				progressBar.setString("Saving File: "+(int)event.getProgress()+"% complete");
			}    		
    	});

    		
    		AWTImageExport.saveImage(bimage, new File(outDir + File.separator + outFile), format, dpi);
	}

	
	
	
	
	
	private void reportException(Exception ex) {
        JOptionPane.showMessageDialog(viewer, ex.getMessage());
	}
}
