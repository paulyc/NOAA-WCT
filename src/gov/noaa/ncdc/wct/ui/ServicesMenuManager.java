package gov.noaa.ncdc.wct.ui;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import gov.noaa.ncdc.wct.io.WCTDataSourceDB;
import gov.noaa.ncdc.wct.ui.ServicesMenuEntry.LocationType;

public class ServicesMenuManager {

    private static ServicesMenuManager config = null;
    private Document doc;
    private XPath xpath;
//    private ArrayList<ServicesMenuEntry> wmsList = new ArrayList<ServicesMenuEntry>();
    
    private static int entryID = 0;

    private HashMap<String, ServicesMenuEntry> entryMap = new HashMap<String, ServicesMenuEntry>();
    
    private WCTViewer viewer = null;
    private JMenu viewerParentMenu = null;
    private JPopupMenu viewerServicesPopupMenu = null;
    
    private ActionListener servicesMenuItemListener = new ServicesMenuItemListener();
    
    
    private ServicesMenuManager(WCTViewer viewer, JMenu viewerParentMenu, JPopupMenu servicesPopupMenu) 
    		throws ParserConfigurationException, SAXException, IOException {
    	
    	this.viewer = viewer;
    	this.viewerParentMenu = viewerParentMenu;
    	this.viewerServicesPopupMenu = servicesPopupMenu;
    }
    
    
    public static ServicesMenuManager getInstance(WCTViewer viewer, JMenu viewerParentMenu, JPopupMenu servicesPopupMenu) 
    		throws ParserConfigurationException, SAXException, IOException, NumberFormatException, XPathExpressionException {
       
    	if (config == null) {
            config = new ServicesMenuManager(viewer, viewerParentMenu, servicesPopupMenu);
            try {
//                config.addConfig(ResourceUtils.getInstance().getJarResource(
//                        new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, "/config/servicesEntry.xml", null));
            } catch (Exception e) {
//            	e.printStackTrace();
            	System.err.println(e.getMessage());
            }
        }
        return config;
    }

    
    
    public void addConfig(String menuTitle, URL url) throws SAXException, IOException, ParserConfigurationException, NumberFormatException, XPathExpressionException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();

        System.out.println("ServicesMenuManager::: LOADING: "+url);
        this.doc = builder.parse(url.toString());

        XPathFactory factory = XPathFactory.newInstance();
        this.xpath = factory.newXPath();

//        <?xml version="1.0"?>
//        <servicesMenu>
//        	<!-- type: REMOTE_DIR, THREDDS_CATALOG, DIRECT_URL -->        	
//        	<entry name="NOAA/NCDC Climate Data Records">
//        	
//        		<entry name="GridSat" type="DIRECT_URL" 
//        			location="http://data.ncdc.noaa.gov/thredds/dodsC/cdr/gridsat/gridsat-aggregation-physical.ncml" />
//        	
//        		<entry name="PATMOS-X" type="THREDDS_CATALOG"
//        			location="http://data.ncdc.noaa.gov/thredds/patmosxCatalog.xml" />        	
//        	</entry>
//        </servicesMenu>
        


        JMenu parentMenu = new JMenu(menuTitle);
        JMenu parentPopupMenu = new JMenu(menuTitle);
        
        
        NodeList result = 
        		(NodeList) xpath.compile("//servicesMenu").evaluate(doc, XPathConstants.NODESET);
        parseNodeList(parentMenu, result, null);
        parseNodeList(parentPopupMenu, result, null);
        
        
        if (viewerParentMenu != null) {
        	viewerParentMenu.add(parentMenu, 0);
        }
        if (viewerServicesPopupMenu != null) {
        	// insert after title and separator
        	viewerServicesPopupMenu.add(parentPopupMenu, 2);
        }
        
        
    }

    private void parseNodeList(JMenu parentMenu, NodeList nodeList, HashMap<String, String> substitutionMap) {
        for (int n=0; n<nodeList.getLength(); n++) {
        	Node node = nodeList.item(n);
        	if (node.getNodeType() == Node.COMMENT_NODE) {
        		continue;
        	}
//        	System.out.println("node: "+node.getNodeName());
        	
        	
        	if ((node.getNodeName().equals("servicesMenu") || node.getNodeName().equals("folder"))
        			&& node.hasChildNodes()) {
        		
        		if (node.getNodeName().equals("servicesMenu")) {
        			parseNodeList(parentMenu, node.getChildNodes(),  new HashMap<String, String>());
        		}
        		else {
        	
//        			Some folders can specify multiple folders where values will be substituted into child entries
//  				<folder name="KGSP|KCAE|KMRX" delimRegex="\\|" substitution="site" tolower="true">

//        			System.out.println(" <creating folder for "+node.getAttributes().getNamedItem("name"));
        			String delim = null;
        			if (node.getAttributes().getNamedItem("delimRegex") != null && node.getAttributes().getNamedItem("delimRegex").getNodeValue().trim().length() > 0) {
        				delim = node.getAttributes().getNamedItem("delimRegex").getNodeValue();
        			}

        			boolean nameToUpper = false;
        			if (node.getAttributes().getNamedItem("toupper") != null && node.getAttributes().getNamedItem("toupper").getNodeValue().trim().equals("true")) {
        				nameToUpper = true;
        			}
        			boolean nameToLower = false;
        			if (node.getAttributes().getNamedItem("tolower") != null && node.getAttributes().getNamedItem("tolower").getNodeValue().trim().equals("true")) {
        				nameToLower = true;
        			}

        			String substitution = null;
        			if (node.getAttributes().getNamedItem("substitution") != null && node.getAttributes().getNamedItem("substitution").getNodeValue().trim().length() > 0) {
        				substitution = node.getAttributes().getNamedItem("substitution").getNodeValue();
        			}
        			        			
        			
        			
        			String nameString = node.getAttributes().getNamedItem("name").getNodeValue();
        			String[] nameArray = null;
        			if (delim != null) {
        				nameArray = nameString.split(delim);
        			}
        			else {
        				nameArray = new String[] { nameString };
        			}
        			
        			for (String name : nameArray) {
        			

        				if (substitutionMap != null && substitution != null) {
        					substitutionMap.clear();
        					substitutionMap.put(substitution, name);
        					if (nameToLower) substitutionMap.put("tolower", "true");
        					if (nameToUpper) substitutionMap.put("toupper", "true");
        				}
        				
        				
        				JMenu menu = new JMenu(name);
        				parentMenu.add(menu);

        				//        			System.out.println(" parsing child nodes of "+node.getNodeName());
        				NodeList childNodes = node.getChildNodes();
        				parseNodeList(menu, childNodes, substitutionMap);

        			}
        			
        			
        		}
        	}
        	else if (node.getNodeName().equals("entry")) {
        		
//				<entry name="Base Reflectivity (elev 0)" type="DIRECT_URL" location="https://tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.p94r0/SI.${site}/sn.last"  />
        		
//        		System.out.println("  << creating entry for "+node.getAttributes().getNamedItem("name"));
        		
        		String location = node.getAttributes().getNamedItem("location").getNodeValue();
        		if (location.contains("${") && substitutionMap != null) {

        			int begIndex = location.indexOf("${");
        			int endIndex = location.indexOf("}", begIndex);
        			String locBefore = location.substring(0, begIndex);
        			String locAfter = location.substring(endIndex+1);
        			String subKey = location.substring(begIndex+2, endIndex);
        			String subVal = substitutionMap.get(subKey);
        			if (substitutionMap.get("tolower") != null) subVal = subVal.toLowerCase();
        			if (substitutionMap.get("toupper") != null) subVal = subVal.toUpperCase();
        			
        			location = locBefore + subVal + locAfter;
        			
//        			System.out.println("LOCATION SUB:::::: "+location);        			
        		}
        		
        		ServicesMenuEntry entry = new ServicesMenuEntry();
        		entry.setId(String.valueOf(entryID++));
        		entry.setName(node.getAttributes().getNamedItem("name").getNodeValue());
        		entry.setLocationType(getLocationType(
        				node.getAttributes().getNamedItem("type").getNodeValue()));
        		entry.setLocation(location);
        		
        		
        		JMenuItem menuItem = new JMenuItem(entry.getName());
        		menuItem.setToolTipText(entry.getLocation());
        		menuItem.setActionCommand(entry.getId());
        		if (servicesMenuItemListener != null) {
        			menuItem.addActionListener(servicesMenuItemListener);
        		}
        		
        		parentMenu.add(menuItem);        		  		
        		
        		entryMap.put(entry.getId(), entry);        		       		
        		
        	}
        }
    }

    
    
    public LocationType getLocationType(String locationTypeString) {
    	if (locationTypeString.equals("THREDDS_CATALOG")) {
    		return LocationType.THREDDS_CATALOG;
    	}
    	else if (locationTypeString.equals("DIRECT_URL")) {
    		return LocationType.DIRECT_URL;
    	}
    	else if (locationTypeString.equals("REMOTE_DIR")) {
    		return LocationType.REMOTE_DIR;
    	}
    	else {
    		return LocationType.UNKNOWN;
    	}
    }



	public HashMap<String, ServicesMenuEntry> getEntryMap() {
		return entryMap;
	}

	public void setEntryMap(HashMap<String, ServicesMenuEntry> entryMap) {
		this.entryMap = entryMap;
	}

	
	
	class ServicesMenuItemListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			ServicesMenuEntry entry = entryMap.get(e.getActionCommand());
			
			try {
			
				if (entry.getLocationType() == LocationType.REMOTE_DIR) {
					viewer.getDataSelector().getDataSourcePanel().setDataLocation(
							WCTDataSourceDB.URL_DIRECTORY, entry.getLocation());
					viewer.getDataSelector().getDataSourcePanel().setDataType(WCTDataSourceDB.URL_DIRECTORY);
					viewer.showDataSelector();
					viewer.getDataSelector().submitListFiles();
				}
				else if (entry.getLocationType() == LocationType.DIRECT_URL) {
					viewer.getDataSelector().getDataSourcePanel().setDataLocation(
							WCTDataSourceDB.SINGLE_FILE, entry.getLocation());
					viewer.getDataSelector().getDataSourcePanel().setDataType(WCTDataSourceDB.SINGLE_FILE);
					viewer.showDataSelector();
					viewer.getDataSelector().loadData();
				}
				else if (entry.getLocationType() == LocationType.THREDDS_CATALOG) {
					viewer.getDataSelector().getDataSourcePanel().setDataLocation(
							WCTDataSourceDB.THREDDS, entry.getLocation());
					viewer.getDataSelector().getDataSourcePanel().setDataType(WCTDataSourceDB.THREDDS);
					viewer.showDataSelector();
					viewer.getDataSelector().submitListFiles();
				}
				else if (entry.getLocationType() == LocationType.UNKNOWN) {
					Desktop.getDesktop().browse(new URI(entry.getLocation()));
				}
			
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(viewer, ex.getMessage(), 
						"Services Load Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
	}
}
