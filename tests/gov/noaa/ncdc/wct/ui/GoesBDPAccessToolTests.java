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

public class GoesBDPAccessToolTests {

	
	@Ignore
	@Test
	public void checkGoesBucketJdom_AWS() {
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
	
	
	
	
	
	
	
	

	@Test
	public void checkGoesBucketJdom_OCC() {
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
				int maxKeys = 10;
						
			// Getting error because of self-signed cert at OCC, I'm guessing.  
			// Possible way around this by modifying: 
			// http://www.nakov.com/blog/2009/07/16/disable-certificate-validation-in-java-ssl-connections/
			// but would need to modify to whitelist OCC and not open to all.

		    	URL url = new URL("https://osdc.rcc.uchicago.edu/noaa-goes16?"
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
