package steve.test.climateengine;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ClimateEngineAPI {

	public static void main(String[] args) {
		try {
			process();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void process() throws MalformedURLException, IOException {
		
//		
//		The API documentation for Climate Engine
//		API_key=028beedee93bd02065a4a57b108bcdee006efb91
//		Some quick examples: 
//		Time series extraction for a point: 
//		In a browser enter: 
//		https://app.climateengine.org/api?toolAction=getTimeSeriesOverDateRange&timeSeriesCalc=days&scaleTS=4000&productTS=G&variableTS=pr&statisticTS=Total&unitsTS=metric&dateStartTS=2018-09-04&dateEndTS=2018-11-02&geom_geoms=-90.7373%2C37.7398&geom_types=point&geom_meta_types=point&API_key=028beedee93bd02065a4a57b108bcdee006efb91
//
//		In a terminal in Unix/Linux, enter: 
//
//		wget -nc -c -nd -O outputClimateEngine.json "https://app.climateengine.org/api?toolAction=getTimeSeriesOverDateRange&timeSeriesCalc=days&scaleTS=4000&productTS=G&variableTS=pr&statisticTS=Total&unitsTS=metric&dateStartTS=2018-09-04&dateEndTS=2018-11-02&geom_geoms=-90.7373%2C37.7398&geom_types=point&geom_meta_types=point&API_key=028beedee93bd02065a4a57b108bcdee006efb91"
//
//		In a terminal on a Mac, enter: 
//
//		cURL -o outputClimateEngine.json "https://app.climateengine.org/api?toolAction=getTimeSeriesOverDateRange&timeSeriesCalc=days&scaleTS=4000&productTS=G&variableTS=pr&statisticTS=Total&unitsTS=metric&dateStartTS=2018-09-04&dateEndTS=2018-11-02&geom_geoms=-90.7373%2C37.7398&geom_types=point&geom_meta_types=point&API_key=028beedee93bd02065a4a57b108bcdee006efb91"
//
//		The API documentation also has an example of retrieving a tiff of a raster map generated from Climate Engine. For example: 
//		https://app.climateengine.org/api?toolAction=downloadRectangleSubset&product=G&variable=pr&statistic=Total&calculation=value&units=metric&dateStart=2018-09-08&dateEnd=2018-11-06&NELat=40.6266&NELong=-106.9043&SWLat=38.2277&SWLong=-108.7598&scale=4000&downloadProjection=EPSG%3A4326&downloadFilename=myGeoTiff&API_key=028beedee93bd02065a4a57b108bcdee006efb91
//

		
		
		String str = "https://app.climateengine.org/api?"+
				"toolAction=getTimeSeriesOverDateRange&"+
				"timeSeriesCalc=days&"+
				"scaleTS=4000&"+
				"productTS=G&"+
				"variableTS=pr&"+
				"statisticTS=Total&"+
				"unitsTS=metric&"+
				"dateStartTS=2018-01-04&"+
				"dateEndTS=2018-11-02&"+
//				"geom_geoms=-90.7373%2C37.7398&"+
				"geom_geoms=-90.7373,37.7398&"+
				"geom_types=point&"+
				"geom_meta_types=point&"+
				"API_key=028beedee93bd02065a4a57b108bcdee006efb91";
				
				
		URL url = new URL(str);
		
		 InputStream in = url.openStream();
		 try {
		   String jsonString = IOUtils.toString( in );
		   JSONParser parser = new JSONParser();
		   JSONObject json = (JSONObject)parser.parse(jsonString);
		   System.out.println( json.toString() );
		   
		   ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		   Object jsonObj = mapper.readValue(jsonString, Object.class);
		   String indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObj);
		   System.out.println(indented);
		   
		 } catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		   IOUtils.closeQuietly(in);
		 }
	}
}
