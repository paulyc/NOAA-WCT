package gov.noaa.ncdc.gis;

import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

public class WCTGeocoder {

	public final static String GEOCODE_SERVICE_URL_BASE = 
		"https://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer/findAddressCandidates?";

	private JSONParser jsonParser = new JSONParser();

	public List<GeocodeResult> locationSearch(String location, Rectangle2D.Double currentExtent) 
	throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, ParseException {

		
//		http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer/findAddressCandidates
//			?singleLine=mcdonalds&outFields=city,type&searchExtent=-117.172026,32.706517,-117.152498,32.725514&f=pjson
		
//		{
//			 "spatialReference": {
//			  "wkid": 4326,
//			  "latestWkid": 4326
//			 },
//			 "candidates": [
//			  {
//			   "address": "McDonald's",
//			   "location": {
//			    "x": -117.15385999999995,
//			    "y": 32.718620000000044
//			   },
//			   "score": 100,
//			   "attributes": {
//			    "type": "Burgers",
//			    "city": "San Diego"
//			   },
//			   "extent": {
//			    "xmin": -117.15885999999995,
//			    "ymin": 32.713620000000041,
//			    "xmax": -117.14885999999996,
//			    "ymax": 32.723620000000047
//			   }
//			  }
//			 ]
//			}		
		

		String queryString = "singleLine="+URLEncoder.encode(location, "UTF-8")+
				"&searchExtent="+URLEncoder.encode(currentExtent.getMinX()+
					","+currentExtent.getMinY()+
					","+currentExtent.getMaxX()+
					","+currentExtent.getMaxY(), "UTF-8")+
				"&f=pjson";
		

		URL url = new URL(GEOCODE_SERVICE_URL_BASE+queryString);
//		url = new URL("https://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer/findAddressCandidates?singleLine=asheville%20nc&f=pjson");
		URLConnection conn = url.openConnection();
		InputStreamReader reader = new InputStreamReader(conn.getInputStream());
		Object obj = jsonParser.parse(new BufferedReader(reader));
		reader.close();
		
		System.out.println(url);
		System.out.println(obj.toString());
//		System.out.println(obj.getClass());
		JSONObject jobj = (JSONObject)obj;		

		JSONArray resultsJson = (JSONArray)jobj.get("candidates");
		
		if (resultsJson == null) {
			throw new IOException("Geocoder did not find any results: \n"+url);
		}


//		System.out.println("GEOCODE STATUS: "+conn.get);
		
		ArrayList<GeocodeResult> resultList = new ArrayList<GeocodeResult>();
		for (int n=0; n<resultsJson.size(); n++) {

			try {
				
				JSONObject o = (JSONObject)resultsJson.get(n);
		

		//	        String formattedAddress = WCTUtils.getXPathValue(doc, xpath, "//GeocodeResponse/result/formatted_address/text()");
		//	        String lat = WCTUtils.getXPathValue(doc, xpath, "//GeocodeResponse/result/geometry/location/lat/text()");
		//	        String lon = WCTUtils.getXPathValue(doc, xpath, "//GeocodeResponse/result/geometry/location/lng/text()");
		//	        System.out.println(location+": "+lat+" , "+lon);


				JSONObject locationObj = (JSONObject) o.get("location");

				String formattedAddress = o.get("address").toString();
				double lat = Double.parseDouble(locationObj.get("y").toString());
				double lon = Double.parseDouble(locationObj.get("x").toString());

			GeocodeResult ggr = new GeocodeResult();
			ggr.setFormattedAddress(formattedAddress);
			ggr.setLat(lat);
			ggr.setLon(lon);
			resultList.add(ggr);

			
			} catch (Exception e) {
				e.printStackTrace();
//				JOptionPane.showMessageDialog(this, "Error adding station: "+station.getName()+" ("+station.getDescription()+")");
			}
			
		}

		return resultList;

	}


	public class GeocodeResult {
		private String formattedAddress;
		private double lat;
		private double lon;

		public void setFormattedAddress(String formattedAddress) {
			this.formattedAddress = formattedAddress;
		}
		public String getFormattedAddress() {
			return formattedAddress;
		}
		public void setLat(double lat) {
			this.lat = lat;
		}
		public double getLat() {
			return lat;
		}
		public void setLon(double lon) {
			this.lon = lon;
		}
		public double getLon() {
			return lon;
		}
	}

}
