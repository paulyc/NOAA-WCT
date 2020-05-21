package gov.noaa.ncdc.wct.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;

import org.jdesktop.swingx.JXHyperlink;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.WCTProperties;

public class BDPAccessPanel extends JPanel {


	private JComboBox<String> datasetList;

	private WCTViewer viewer;
	private ActionListener listButtonListener;
	
	private JPanel datasetPanel = new JPanel(new BorderLayout());
	private NexradBDPAccessPanel nexradPanel = new NexradBDPAccessPanel(viewer, listButtonListener);
	private GoesBDPAccessPanel goesPanel = new GoesBDPAccessPanel(viewer, listButtonListener);

	public BDPAccessPanel(WCTViewer viewer, ActionListener listButtonListener) throws ParserConfigurationException {
		// super(viewer, "NOAA Big Data Project - Access Tool", false);
		super(new RiverLayout());
		this.viewer = viewer;		
		this.listButtonListener = listButtonListener;
		init();
		createGUI();
		// pack();
	}

	private void init() throws ParserConfigurationException {
		datasetList = new JComboBox<String>(new String[] { "NEXRAD Level-II", "GOES-16", "GOES-17" });
	}

	private void createGUI() {



		JXHyperlink bdpLink = new JXHyperlink();
		bdpLink.setText("[ ? ]");
		bdpLink.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WCTUiUtils.browse(viewer, "https://data-alliance.noaa.gov/", "Error browsing to: https://data-alliance.noaa.gov/");
			}
		});
		
		JButton listButton = new JButton("  List Files  ");
		listButton.addActionListener(listButtonListener);
        listButton.setMnemonic(KeyEvent.VK_L);

        datasetList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String dataset = datasetList.getSelectedItem().toString();
				if (dataset.equals("NEXRAD Level-II")) {
					datasetPanel.removeAll();
					datasetPanel.add(nexradPanel, BorderLayout.CENTER);
				}
				if (dataset.equals("GOES-16") || dataset.equals("GOES-17")) {
					datasetPanel.removeAll();
					datasetPanel.add(goesPanel, BorderLayout.CENTER);
				}
				WCTProperties.setWCTProperty("bdp_selected_dataset", dataset);
				validate();
			}        	
        });
        
        String previousDataset = WCTProperties.getWCTProperty("bdp_selected_dataset");
        if (previousDataset != null) {
        	datasetList.setSelectedItem(previousDataset);
        }
        else {
        	datasetPanel.add(nexradPanel, BorderLayout.CENTER);
        }

		this.add("center", new JLabel("Access to data available from cloud collaborators in the NOAA Big Data Project"));
		this.add(bdpLink);
		
		JPanel leftPanel = new JPanel(new RiverLayout()); 
		leftPanel.add("left ", datasetList);
		leftPanel.add("p", listButton);
		
		JPanel selectionPanel = new JPanel();
		selectionPanel.add(leftPanel);
		selectionPanel.add(datasetPanel);
		
		this.add("br left", selectionPanel);

		this.setSize(700, 600);


	}

	public void setBDPDataset(String dataset) {
		if (! dataset.equals(datasetList.getSelectedItem().toString())) {
			datasetList.setSelectedItem(dataset);
		}
	}
	
	
	public String getDataPath() {
		
		if (datasetList.getSelectedItem().toString().equals("NEXRAD Level-II")) {
			return nexradPanel.getDataPath("NEXRAD2");
		}
		else if (datasetList.getSelectedItem().toString().equals("GOES-16")) {
			return goesPanel.getDataPath("GOES-16");
		}
		else if (datasetList.getSelectedItem().toString().equals("GOES-17")) {
			return goesPanel.getDataPath("GOES-17");
		}
		else {
			return null;
		}
	}
	
	
	public void setStatusText(String status) {
		
		if (datasetList.getSelectedItem().toString().equals("NEXRAD Level-II")) {
			nexradPanel.setStatus(status);
		}
		else if (datasetList.getSelectedItem().toString().startsWith("GOES-")) {
			goesPanel.setStatus(status);
		}
	}
	
	
	
	public NexradBDPAccessPanel getNexradBDPAccessPanel() {
		return nexradPanel;
	}
	public GoesBDPAccessPanel getGoesBDPAccessPanel() {
		return goesPanel;
	}
}
