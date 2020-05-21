package gov.noaa.ncdc.wct.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.xml.parsers.ParserConfigurationException;

import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXHyperlink;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.WCTProperties;

public class GoesBDPAccessPanel extends JPanel implements BDPAccessSubPanel {

	public final static String AWS_BUCKET_ROOT_GOES16 = "https://s3.amazonaws.com/noaa-goes16";
	public final static String AWS_BUCKET_ROOT_GOES17 = "https://s3.amazonaws.com/noaa-goes17";
//	https://s3.amazonaws.com/noaa-goes16
	public final static int AWS_BUCKET_SEARCH_MAX_RESULTS = 1000;
	
	public final static SimpleDateFormat SDF_YYYYMMDD = new SimpleDateFormat("yyyyMMdd");
	static {
		SDF_YYYYMMDD.setTimeZone(TimeZone.getTimeZone("GMT"));		
	}
	public final static SimpleDateFormat SDF_YYYYDDD = new SimpleDateFormat("yyyyDDD");
	static {
		SDF_YYYYDDD.setTimeZone(TimeZone.getTimeZone("GMT"));		
	}
	static HashMap<String, String> prodCodeLookup = new HashMap<String, String>();
	static {
		
		
		prodCodeLookup.put("ABI-L1b-RadF", "Radiances, Full Disk, every 45 min.");
		prodCodeLookup.put("ABI-L1b-RadC", "Radiances, Con. U.S., every 5 min.");
		prodCodeLookup.put("ABI-L1b-RadM", "Radiances, Mesoscale, every 30 sec.");
		prodCodeLookup.put("ABI-L2-ACHAC", "");
		prodCodeLookup.put("ABI-L2-ACHAF", "");
		prodCodeLookup.put("ABI-L2-ACHAM", "");
		prodCodeLookup.put("ABI-L2-ACHTF", "");
		prodCodeLookup.put("ABI-L2-ACHTM", "");
		prodCodeLookup.put("ABI-L2-ACMC", "");
		prodCodeLookup.put("ABI-L2-ACMF", "");
		prodCodeLookup.put("ABI-L2-ACMM", "");
		prodCodeLookup.put("ABI-L2-ACTPC", "");
		prodCodeLookup.put("ABI-L2-ACTPF", "");
		prodCodeLookup.put("ABI-L2-ACTPM", "");
		prodCodeLookup.put("ABI-L2-ADPC", "");
		prodCodeLookup.put("ABI-L2-ADPF", "");
		prodCodeLookup.put("ABI-L2-ADPM", "");
		prodCodeLookup.put("ABI-L2-AODC", "");
		prodCodeLookup.put("ABI-L2-AODF", "");
		prodCodeLookup.put("ABI-L2-CMIPF", "CMIPF, Cloud and Moisture Imagery Products, Full Disk");
		prodCodeLookup.put("ABI-L2-CMIPC", "CMIPC, Cloud and Moisture Imagery Products, Con. U.S.");
		prodCodeLookup.put("ABI-L2-CMIPM", "CMIPM, Cloud and Moisture Imagery Products, Mesoscale");
		prodCodeLookup.put("ABI-L2-CODC", "");
		prodCodeLookup.put("ABI-L2-CODF", "");
		prodCodeLookup.put("ABI-L2-CPSC", "");
		prodCodeLookup.put("ABI-L2-CPSF", "");
		prodCodeLookup.put("ABI-L2-CPSM", "");
		prodCodeLookup.put("ABI-L2-CTPC", "");
		prodCodeLookup.put("ABI-L2-CTPF", "");
		prodCodeLookup.put("ABI-L2-DMWC", "");
		prodCodeLookup.put("ABI-L2-DMWF", "");
		prodCodeLookup.put("ABI-L2-DMWM", "");
		prodCodeLookup.put("ABI-L2-DSIC", "");	
		prodCodeLookup.put("ABI-L2-DSIF", "");	
		prodCodeLookup.put("ABI-L2-DSIM", "");	
		prodCodeLookup.put("ABI-L2-DSRC", "");	
		prodCodeLookup.put("ABI-L2-DSRF", "");	
		prodCodeLookup.put("ABI-L2-DSRM", "");	
		prodCodeLookup.put("ABI-L2-FDCC", "FDCC, Fire Detection, Con. U.S.");
		prodCodeLookup.put("ABI-L2-FDCF", "FDCF, Fire Detection, Full Disk");
		prodCodeLookup.put("ABI-L2-LSTC", "");	
		prodCodeLookup.put("ABI-L2-LSTF", "");	
		prodCodeLookup.put("ABI-L2-LSTM", "");	
		prodCodeLookup.put("ABI-L2-LVMPC", "");	
		prodCodeLookup.put("ABI-L2-LVMPF", "");	
		prodCodeLookup.put("ABI-L2-LVMPM", "");	
		prodCodeLookup.put("ABI-L2-LVTPC", "");	
		prodCodeLookup.put("ABI-L2-LVTPF", "");	
		prodCodeLookup.put("ABI-L2-LVTPM", "");	
		prodCodeLookup.put("ABI-L2-MCMIPF", "MCMIPF, Multichannel CMIP, Full Disk");
		prodCodeLookup.put("ABI-L2-MCMIPC", "MCMIPC, Multichannel CMIP, Con. U.S.");
		prodCodeLookup.put("ABI-L2-MCMIPM", "MCMIPM, Multichannel CMIP, Mesoscale");
		prodCodeLookup.put("ABI-L2-RRQPEF", "");	
		prodCodeLookup.put("ABI-L2-RSRC", "");
		prodCodeLookup.put("ABI-L2-RSRF", "");
		prodCodeLookup.put("ABI-L2-SSTF", "");
		prodCodeLookup.put("ABI-L2-TPWC", "");
		prodCodeLookup.put("ABI-L2-TPWF", "");
		prodCodeLookup.put("ABI-L2-TPWM", "");
		prodCodeLookup.put("ABI-L2-VAAF", "");
		prodCodeLookup.put("GLM-L2-LCFA", "Global Lightning Mapper");
		prodCodeLookup.put("SUVI-L1b-Fe", "");
		prodCodeLookup.put("SUVI-L1b-Fe093", "");
		prodCodeLookup.put("SUVI-L1b-Fe13", "");
		prodCodeLookup.put("SUVI-L1b-Fe131", "");
		prodCodeLookup.put("SUVI-L1b-Fe171", "");
		prodCodeLookup.put("SUVI-L1b-Fe195", "");
		prodCodeLookup.put("SUVI-L1b-Fe284", "");
		prodCodeLookup.put("SUVI-L1b-He303", "");		
		
	}

	static HashMap<String, String> chanCodeLookup = new HashMap<String, String>();
	static {
		chanCodeLookup.put("C01","\"Blue\" Band (Visible) ");
		chanCodeLookup.put("C02","\"Red\" Band (Visible) ");
		chanCodeLookup.put("C03","\"Veggie\" Band (Near-IR) ");
		chanCodeLookup.put("C04","\"Cirrus\" Band (Near-IR) ");
		chanCodeLookup.put("C05","\"Snow/Ice\" Band (Near-IR) ");
		chanCodeLookup.put("C06","\"Cloud Particle Size\" Band (Near-IR) ");
//		chanCodeLookup.put("C07","\"Shortwave Window\" Band (IR (with reflected daytime Component)) ");
		chanCodeLookup.put("C07","\"Shortwave Window\" Band ");
		chanCodeLookup.put("C08","\"Upper-Level Tropo. Water Vapor\" Band (IR) ");
		chanCodeLookup.put("C09","\"Mid-Level Tropo. Water Vapor\" Band (IR) ");
		chanCodeLookup.put("C10","\"Lower-Level Water Vapor\" Band (IR) ");
		chanCodeLookup.put("C11","\"Cloud-Top Phase\" Band (IR) ");
		chanCodeLookup.put("C12","\"Ozone\" Band (IR) ");
		chanCodeLookup.put("C13","\"Clean\" Longwave Window Band (IR) ");
		chanCodeLookup.put("C14","Longwave Window Band (IR) ");
		chanCodeLookup.put("C15","\"Dirty\" Longwave Window Band (IR) ");
		chanCodeLookup.put("C16","\"CO2\" Longwave Window Band (IR) ");
	}

	private JComboBox<String> hourList, productList, providerList, channelList;
	private JLabel statusLabel = new JLabel();
	private final JXDatePicker picker = new JXDatePicker(new Date(), Locale.US);

	private WCTViewer viewer;
	private ActionListener listButtonListener;

	public GoesBDPAccessPanel(WCTViewer viewer, ActionListener listButtonListener) throws ParserConfigurationException {
		// super(viewer, "NOAA Big Data Project - Access Tool", false);
		super(new RiverLayout());
		this.viewer = viewer;		
		this.listButtonListener = listButtonListener;
		init();
		createGUI();
		// pack();
	}

	private void init() throws ParserConfigurationException {
		providerList = new JComboBox<String>(new String[] { "Amazon" });
	}

	private void createGUI() {

		channelList = new JComboBox<String>();
		listChannels();
		String lastChannelProp = WCTProperties.getWCTProperty("bdpAWS_GOES16_Channel");
		if (lastChannelProp != null) {
			for (int n=0; n<channelList.getItemCount(); n++) {
				if (channelList.getItemAt(n).startsWith(lastChannelProp)) {
					channelList.setSelectedIndex(n);
				}
			}   
		}


		hourList = new JComboBox<String>( new String[] { 
				"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", 
				"12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" });

		
		productList = new JComboBox<String>();
		listProducts();
		productList.addItemListener(new ItemListener() {
		    @Override
		    public void itemStateChanged(ItemEvent event) {
		       if (event.getStateChange() == ItemEvent.SELECTED) {
		          channelList.setEnabled( ! productList.getSelectedItem().toString().startsWith("ABI-L2-M") );
		       }
		    }  
		});
		String lastProductProp = WCTProperties.getWCTProperty("bdpAWS_GOES16_Product");
		if (lastProductProp != null) {
			for (int n=0; n<productList.getItemCount(); n++) {
				if (productList.getItemAt(n).startsWith(lastProductProp)) {
					productList.setSelectedIndex(n);
				}
			}   
		}
		
		picker.setTimeZone(TimeZone.getTimeZone("GMT"));
		picker.getMonthView().setUpperBound(new Date());
		picker.getMonthView().setFlaggedDayForeground(Color.BLUE.darker());
		String lastDateProp = WCTProperties.getWCTProperty("bdpAWS_GOES16_Date");
		if (lastDateProp != null) {
			try {
				picker.setDate(SDF_YYYYMMDD.parse(lastDateProp));
			} catch (ParseException e1) {
				picker.setDate(new Date());
				e1.printStackTrace();
			}
		}
		else {
			picker.setDate(new Date());
		}
		picker.addPropertyChangeListener(new PropertyChangeListener() {			
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if ("date".equals(e.getPropertyName())) {
					// refresh upper bound, in case the next GMT day becomes available after picker has been created
					picker.getMonthView().setUpperBound(new Date());
					selectDate(picker.getDate());
				}
			}
		});
		picker.getMonthView().addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent evt) {
			}
			@Override
			public void mouseMoved(MouseEvent evt) {
				Date d = picker.getMonthView().getDayAtLocation(evt.getX(), evt.getY());
				if (d != null) {
					picker.getMonthView().clearFlaggedDates();
					picker.getMonthView().setFlaggedDates(d);
				}
			}
		});


		JXHyperlink bdpLink = new JXHyperlink();
		bdpLink.setText("[ ? ]");
		bdpLink.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WCTUiUtils.browse(viewer, "https://www.noaa.gov/big-data-project", "Error browsing to: https://www.noaa.gov/big-data-project");
			}
		});


		JXHyperlink awsLink = new JXHyperlink();
		awsLink.setText("Amazon Documentation");
		awsLink.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) { 
				WCTUiUtils.browse(viewer, "https://aws.amazon.com/public-datasets/goes/", 
						"Error browsing to: https://aws.amazon.com/public-datasets/goes/");   		
			}
		});
		
		JXHyperlink nceiNewsLink = new JXHyperlink();
		nceiNewsLink.setText("NCEI News Article");
		nceiNewsLink.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WCTUiUtils.browse(viewer, "https://www.ncdc.noaa.gov/news/partnering-amazon-web-services-big-data",
				"Error browsing to: https://www.ncdc.noaa.gov/news/partnering-amazon-web-services-big-data");   
			}
		});

		JXHyperlink browseAws16Link = new JXHyperlink();
		browseAws16Link.setText("G16");
		browseAws16Link.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		       	WCTUiUtils.browse(viewer, "https://s3.amazonaws.com/noaa-goes16/index.html",
		       			"Error browsing to: https://s3.amazonaws.com/noaa-goes16/index.html");    			        
			}
		});
		JXHyperlink browseAws17Link = new JXHyperlink();
		browseAws17Link.setText("G17");
		browseAws17Link.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		       	WCTUiUtils.browse(viewer, "https://s3.amazonaws.com/noaa-goes17/index.html",
		       			"Error browsing to: https://s3.amazonaws.com/noaa-goes17/index.html");    			        
			}
		});

		JButton listButton = new JButton("  List Files  ");
		listButton.addActionListener(listButtonListener);
        listButton.setMnemonic(KeyEvent.VK_L);


		this.add("left", providerList);
		this.add(awsLink);
		this.add(new JLabel("/"));
		this.add(nceiNewsLink);
//		this.add(new JLabel("/ S3 Explorer: "));
//		this.add(browseAws16Link);
//		this.add(new JLabel(" | "));
//		this.add(browseAws17Link);
		this.add("p left", productList);
		this.add(channelList);
		this.add(picker);
//		this.add(new JLabel("Hour", SwingConstants.LEFT));       
//		this.add(hourList);
		this.add(new JLabel("(GMT/UTC)", SwingConstants.LEFT));       
		this.add(statusLabel);


		this.setSize(700, 600);


	}

	
	
	private void selectDate(Date date) {
		System.out.println(date);
	}

	private void listProducts() {

		try {

			DefaultComboBoxModel<String> listModel = new DefaultComboBoxModel<String>();
			listModel.addElement("ABI-L1b-RadC");
			listModel.addElement("ABI-L1b-RadF");
			listModel.addElement("ABI-L1b-RadM");
			listModel.addElement("ABI-L2-ACHAC");
			listModel.addElement("ABI-L2-ACHAF");
			listModel.addElement("ABI-L2-ACHAM");
			listModel.addElement("ABI-L2-ACHTF");
			listModel.addElement("ABI-L2-ACHTM");
			listModel.addElement("ABI-L2-ACMC");
			listModel.addElement("ABI-L2-ACMF");
			listModel.addElement("ABI-L2-ACMM");
			listModel.addElement("ABI-L2-ACTPC");
			listModel.addElement("ABI-L2-ACTPF");
			listModel.addElement("ABI-L2-ACTPM");
			listModel.addElement("ABI-L2-ADPC");
			listModel.addElement("ABI-L2-ADPF");
			listModel.addElement("ABI-L2-ADPM");
			listModel.addElement("ABI-L2-AODC");
			listModel.addElement("ABI-L2-AODF");
			listModel.addElement("ABI-L2-CMIPC");	
			listModel.addElement("ABI-L2-CMIPF");	
			listModel.addElement("ABI-L2-CMIPM");	
			listModel.addElement("ABI-L2-CODC");
			listModel.addElement("ABI-L2-CODF");
			listModel.addElement("ABI-L2-CPSC");
			listModel.addElement("ABI-L2-CPSF");
			listModel.addElement("ABI-L2-CPSM");
			listModel.addElement("ABI-L2-CTPC");
			listModel.addElement("ABI-L2-CTPF");
			listModel.addElement("ABI-L2-DMWC");
			listModel.addElement("ABI-L2-DMWF");
			listModel.addElement("ABI-L2-DMWM");
			listModel.addElement("ABI-L2-DSIC");	
			listModel.addElement("ABI-L2-DSIF");	
			listModel.addElement("ABI-L2-DSIM");	
			listModel.addElement("ABI-L2-DSRC");	
			listModel.addElement("ABI-L2-DSRF");	
			listModel.addElement("ABI-L2-DSRM");	
			listModel.addElement("ABI-L2-FDCC");	
			listModel.addElement("ABI-L2-FDCF");	
			listModel.addElement("ABI-L2-LSTC");	
			listModel.addElement("ABI-L2-LSTF");	
			listModel.addElement("ABI-L2-LSTM");	
			listModel.addElement("ABI-L2-LVMPC");	
			listModel.addElement("ABI-L2-LVMPF");	
			listModel.addElement("ABI-L2-LVMPM");	
			listModel.addElement("ABI-L2-LVTPC");	
			listModel.addElement("ABI-L2-LVTPF");	
			listModel.addElement("ABI-L2-LVTPM");	
			listModel.addElement("ABI-L2-MCMIPC");	
			listModel.addElement("ABI-L2-MCMIPF");	
			listModel.addElement("ABI-L2-MCMIPM");	
			listModel.addElement("ABI-L2-RRQPEF");	
			listModel.addElement("ABI-L2-RSRC");
			listModel.addElement("ABI-L2-RSRF");
			listModel.addElement("ABI-L2-SSTF");
			listModel.addElement("ABI-L2-TPWC");
			listModel.addElement("ABI-L2-TPWF");
			listModel.addElement("ABI-L2-TPWM");
			listModel.addElement("ABI-L2-VAAF");
			listModel.addElement("GLM-L2-LCFA");
			listModel.addElement("SUVI-L1b-Fe");
			listModel.addElement("SUVI-L1b-Fe093");
			listModel.addElement("SUVI-L1b-Fe13");
			listModel.addElement("SUVI-L1b-Fe131");
			listModel.addElement("SUVI-L1b-Fe171");
			listModel.addElement("SUVI-L1b-Fe195");
			listModel.addElement("SUVI-L1b-Fe284");
			listModel.addElement("SUVI-L1b-He303");
			productList.setModel(listModel);

		} catch (Exception e) {
			// siteListCombo =
			e.printStackTrace();
		}
	}
	
	private void listChannels() {

		try {

			DefaultComboBoxModel<String> listModel = new DefaultComboBoxModel<String>();
			listModel.addElement("(C01) "+chanCodeLookup.get("C01"));
			listModel.addElement("(C02) "+chanCodeLookup.get("C02"));
			listModel.addElement("(C03) "+chanCodeLookup.get("C03"));
			listModel.addElement("(C04) "+chanCodeLookup.get("C04"));
			listModel.addElement("(C05) "+chanCodeLookup.get("C05"));
			listModel.addElement("(C06) "+chanCodeLookup.get("C06"));
			listModel.addElement("(C07) "+chanCodeLookup.get("C07"));
			listModel.addElement("(C08) "+chanCodeLookup.get("C08"));
			listModel.addElement("(C09) "+chanCodeLookup.get("C09"));
			listModel.addElement("(C10) "+chanCodeLookup.get("C10"));
			listModel.addElement("(C11) "+chanCodeLookup.get("C11"));
			listModel.addElement("(C12) "+chanCodeLookup.get("C12"));
			listModel.addElement("(C13) "+chanCodeLookup.get("C13"));
			listModel.addElement("(C14) "+chanCodeLookup.get("C14"));
			listModel.addElement("(C15) "+chanCodeLookup.get("C15"));
			listModel.addElement("(C16) "+chanCodeLookup.get("C16"));
			channelList.setModel(listModel);

		} catch (Exception e) {
			// siteListCombo =
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Use range and/or list expansion built into DiretoryScanner class, so a single URL can be used to request multiple concatenated list queries to AWS.
	 * 
	 * satID can be 'GOES-16' or 'GOES-17'
	 * @return
	 */
	@Override
	public String getDataPath(String satID) {
		
//		http://noaa-goes16.s3.amazonaws.com/ABI-L1b-RadC/2017/191/21/OR_ABI-L1b-RadC-M3C02_G16_s20171912127189_e20171912129562_c20171912129595.nc
//		"http://noaa-goes16.s3.amazonaws.com/?"
//		+ "prefix="+product+"/"+yyyy+"/"+julDay+"/21"
//		+ "&max-keys="+maxKeys
		
		
		Date d = picker.getMonthView().getFirstSelectionDate();

		String yyyy = SDF_YYYYDDD.format(d).substring(0, 4);
		String ddd = SDF_YYYYDDD.format(d).substring(4, 7);
		String hour = hourList.getSelectedItem().toString();
		String product = productList.getSelectedItem().toString();
		
		String chStr =  channelList.getSelectedItem().toString();
		String channel = chStr.substring(1, 4);
		String product2 = (product.endsWith("M1") || product.endsWith("M2")) ? product.substring(0, product.length()-1) : product;
		String bucketRoot = (satID.equals("GOES-16")) ? AWS_BUCKET_ROOT_GOES16 : AWS_BUCKET_ROOT_GOES17;

		// save properties because this indicates a dir listing
        WCTProperties.setWCTProperty("bdpAWS_GOES16_Product", product);
        WCTProperties.setWCTProperty("bdpAWS_GOES16_Channel", channel);
        WCTProperties.setWCTProperty("bdpAWS_GOES16_Date", SDF_YYYYMMDD.format(d));
        
//        https://s3.amazonaws.com/noaa-goes16/?list-type=2&prefix=ABI-L1b-RadM/2019/241/23/OR_ABI-L1b-RadM1-M6C13
//        ABI-L1b-RadM/2019/241/23/OR_ABI-L1b-RadM1-M6C13_G16_s20192412301240_e20192412301309_c20192412301371.nc
        
//		return AWS_BUCKET_ROOT + "/?list-type=2&prefix=" + product + "/" + yyyy + "/" + ddd + "/" + hour + "/";
//        OR_ABI-L2-CMIPF-M3C01_G16_s20190060545360_e20190060556127_c20190060556195.nc
//        OR_ABI-L2-MCMIPC-M6_G16_s20190502136131_e20190502138504_c20190502139016.nc
//        OR_GLM-L2-LCFA_G16_s20191841816000_e20191841816200_c20191841816228.nc	
        // check each hour, and check 
        if (product.startsWith("ABI-L2-CMIP") || product.startsWith("ABI-L1")) {    	
        	return bucketRoot + "/?list-type=2&prefix=" + product2 + "/" + yyyy + "/" + ddd + "/${00-23}/OR_"+product+"-M${3,4,6}"+channel;
        }
        else if (product.startsWith("GLM-L2")) {
        	// no channel
        	return bucketRoot + "/?list-type=2&prefix=" + product2 + "/" + yyyy + "/" + ddd + "/${00-23}/OR_"+product;       	
        }
        else {
        	// no channel
        	return bucketRoot + "/?list-type=2&prefix=" + product2 + "/" + yyyy + "/" + ddd + "/${00-23}/OR_"+product+"-M${3,4,6}";   
        }
	}
	
	@Override
	public void setDate(String yyyymmdd) throws ParseException {
		picker.setDate(SDF_YYYYMMDD.parse(yyyymmdd));
	}
	
//	public void setSite(String siteid) throws WCTException {
//		boolean foundMatch = false;
//		for (int n=0; n<siteList.getItemCount(); n++) {
//			if (siteList.getItemAt(n).startsWith(siteid)) {
//				siteList.setSelectedIndex(n);
//				foundMatch = true;
//			}
//		}
//		if (! foundMatch) {
//			throw new WCTException("The site "+siteid+" is not currently included in the NOAA Big Data project.");
//		}
//	}
	
	@Override
	public void setStatus(String status) {
		statusLabel.setText(status);

		if (status.trim().length() > 0) {
            statusLabel.setIcon(new ImageIcon(WCTViewer.class.getResource("/icons/ajax-loader.gif")));            
		}
		else {
			statusLabel.setIcon(null);
		}
	}
	
}
