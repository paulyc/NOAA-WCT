package gov.noaa.ncdc.wct.ui;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.junit.Ignore;
import org.junit.Test;

public class NexradBDPAccessToolTests {
//
//	@Test
//	public void checkBucket() throws SAXException, IOException, XPathExpressionException, ParserConfigurationException {
//		
//		String yyyy = "2015";
//		String mm = "10";
//		String dd = "30";
//		String siteID = "KABR";
//				
//		
//		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
//		domFactory.setNamespaceAware(true); // never forget this!
//
//		DocumentBuilder builder = domFactory.newDocumentBuilder();
//
//		XPathFactory factory = XPathFactory.newInstance();
//		XPath xpath = factory.newXPath();
//
//
//    	URL url = new URL("http://noaa-nexrad-level2.s3.amazonaws.com/?prefix="+yyyy+"/"+mm+"/"+dd+"/"+siteID+"/&max-keys=600");
//		Document doc = builder.parse(url.toString());
//		System.out.println(url);
//    	ArrayList<String> keyList = WCTUtils.getXPathValues(doc, xpath, "//ListBucketResult/Contents/Key/text()"); 
//    	System.out.println(keyList);
//    	
//  
//
//    	String xpathString = "count(//ListBucketResult/Contents[namespace-uri()='http://s3.amazonaws.com/doc/2006-03-01/'])";
////    	String xpathString = "count(//ListBucketResult/Contents/Key/text())";
//    	XPathExpression expr = xpath.compile(xpathString);
//
//    	System.out.println(expr.evaluate(doc, XPathConstants.NUMBER));
//    	
//    	
//    	ArrayList list  = (ArrayList)xpath.evaluate(xpathString, new InputSource(url.openStream()),XPathConstants.NODESET);
//    	System.out.println(list);
////    	for (int i = 0; i < list.getLength(); i++) {
////    		String value = list.item(i).getTextContent();
////    		System.out.println(value);
////    	}
//    	
//    	
//    	
////    	String size = WCTUtils.getXPathValue(doc, xpath, xpathString); 
////    	System.out.println(size);
//	}
	
	@Ignore
	@Test
	public void checkBucketJdom() {
		 try {

				String yyyy = "2015";
				String mm = "10";
				String dd = "30";
//				String siteID = "KABR";
				String siteID = null;
				int maxKeys = 1000;
						
			

		    	URL url = new URL("http://noaa-nexrad-level2.s3.amazonaws.com/?"
		    			+ "prefix="+yyyy+"/"+mm+"/"+dd+"/"
		    			+ ((siteID == null) ? "" : (siteID+"/"))
		    			+ "&max-keys="+maxKeys);
			

			  SAXBuilder builder = new SAXBuilder();
				Document document = (Document) builder.build(url);
				
				Element rootNode = document.getRootElement();
				Namespace ns = rootNode.getNamespace();				
				List<Element> list = rootNode.getChildren("Contents", ns);
				for (int i = 0; i < list.size(); i++) {

				   Element node = (Element) list.get(i);

				   System.out.print(i+": "+node.getName());
				   System.out.print("  |  Key : " + node.getChildText("Key", ns));
				   System.out.println("  |  Size : " + node.getChildText("Size", ns));

				}

			  } catch (IOException io) {
				System.out.println(io.getMessage());
			  } catch (JDOMException jdomex) {
				System.out.println(jdomex.getMessage());
			  }
	}
	
	
	
	
	@Test
	public void checkGoesBucketJdom() {
		 try {
			 
//			 All data files from GOES-16 (formerly GOES-R) are provided in netCDF4 format. 
//			 The GOES-16 data is hosted in the noaa-goes16 Amazon S3 bucket in S3's 
//			 us-east-1 region. Individual files are availabe in the netCDF format with the 
//			 following schema:
//
//			 <Product>/<Year>/<Day of Year>/<Hour>/<Filename>
//			 https://www.ncdc.noaa.gov/data-access/satellite-data/goes-r-series-satellites/glossary

				String yyyy = "2017";
				String mm = "08";
				String dd = "01";
				String julDay = "191";
//				String siteID = "KABR";
				String product = "ABI-L1b-RadC";
				int maxKeys = 1000;
						
			

		    	URL url = new URL("http://noaa-goes16.s3.amazonaws.com/?"
		    			+ "prefix="+product+"/"+yyyy+"/"+julDay+"/21"
		    			+ "&max-keys="+maxKeys);
		    	
			System.out.println(url);

			  SAXBuilder builder = new SAXBuilder();
				Document document = (Document) builder.build(url);
				
				Element rootNode = document.getRootElement();
				Namespace ns = rootNode.getNamespace();				
				List<Element> list = rootNode.getChildren("Contents", ns);
				for (int i = 0; i < list.size(); i++) {

				   Element node = (Element) list.get(i);

				   System.out.print(i+": "+node.getName());
				   System.out.print("  |  Key : " + node.getChildText("Key", ns));
				   System.out.println("  |  Size : " + node.getChildText("Size", ns));

				}

			  } catch (IOException io) {
				System.out.println(io.getMessage());
			  } catch (JDOMException jdomex) {
				System.out.println(jdomex.getMessage());
			  }
	}
}
