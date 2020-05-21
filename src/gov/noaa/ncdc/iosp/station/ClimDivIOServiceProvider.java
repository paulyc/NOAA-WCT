package gov.noaa.ncdc.iosp.station;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.Feature;

import com.vividsolutions.jts.geom.Point;

import gov.noaa.ncdc.common.FeatureCache;
import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants._Coordinate;
import ucar.nc2.iosp.AbstractIOServiceProvider;
import ucar.unidata.io.RandomAccessFile;

/** IOService Provider for ClimDiv data */

public class ClimDivIOServiceProvider extends AbstractIOServiceProvider {      
	
	public static enum DivisionType { STATE, CLIMATEDIV, COUNTY };
	
	ucar.unidata.io.RandomAccessFile raf = null;
	ArrayList<Variable> varList = new ArrayList<Variable>();
	Map<String, Array> resultMap = new HashMap<String, Array> (); 
	DivisionType divisionType = null;
	final static HashMap<String, HashMap<String, Attribute>> VAR_LUT = new HashMap<String, HashMap<String, Attribute>>();
	static {
		
		// from: ftp://ftp.ncdc.noaa.gov/pub/data/cirs/climdiv/divisional-readme.txt
		
//		ELEMENT CODE        5-6      
//		01 = Precipitation
//      02 = Average Temperature
//      05 = PDSI
//      06 = PHDI
//      07 = ZNDX
//      08 = PMDI
//      25 = Heating Degree Days
//      26 = Cooling Degree Days
//      27 = Maximum Temperature
//      28 = Minimum Temperature
//		   71 = 1-month Standardized Precipitation Index
//		   72 = 2-month Standardized Precipitation Index
//      73 = 3-month Standardized Precipitation Index
//      74 = 6-month Standardized Precipitation Index
//      75 = 9-month Standardized Precipitation Index
//      76 = 12-month Standardized Precipitation Index
//      77 = 24-month Standardized Precipitation Index
		
		
//JAN-VALUE          11-17     Palmer Drought Index format (f7.2)
//        Range of values -20.00 to 20.00. Decimal point
//        retains a position in 7-character field.
//        Missing values in the latest year are indicated
//        by -99.99.
//
//        Monthly Divisional Temperature format (f7.2)
//        Range of values -50.00 to 140.00 degrees Fahrenheit.
//        Decimals retain a position in the 7-character
//        field.  Missing values in the latest year are
//        indicated by -99.90.
//
//        Monthly Divisional Precipitation format (f7.2)
//        Range of values 00.00 to 99.99.  Decimal point
//        retains a position in the 7-character field.
//        Missing values in the latest year are indicated
//        by -9.99.
//
//        Monthly Divisional Degree Day format (f7.0)
//        Range of values 0000. to 9999.  Decimal point
//        retains a position in the 7-character field.
//        Missing values in the latest year are indicated
//        by -9999..
//
//        Standardized Precipitation Index format (f7.2).
//        Range of values -4.00 to 4.00.  Decimal
//        point retains a position in 7-character field.
//        Missing values in the latest year are indicated
//        by -99.99.
		
		{
			final HashMap<String, Attribute> varAttributeMap = new HashMap<String, Attribute>();		
			varAttributeMap.put("variable_name", new Attribute("variable_name", "cddcdv"));	
			varAttributeMap.put("long_name", new Attribute("long_name", "Cooling Degree Days"));
			varAttributeMap.put("valid_min", new Attribute("valid_min", 0));
			varAttributeMap.put("valid_max", new Attribute("valid_max", 9999));
			varAttributeMap.put("units", new Attribute("units", "count"));
			varAttributeMap.put("_FillValue", new Attribute("_FillValue", "-9999"));
			VAR_LUT.put("26", varAttributeMap);
		}
		{
			final HashMap<String, Attribute> varAttributeMap = new HashMap<String, Attribute>();		
			varAttributeMap.put("variable_name", new Attribute("variable_name", "hddcdv"));	
			varAttributeMap.put("long_name", new Attribute("long_name", "Heating Degree Days"));
			varAttributeMap.put("valid_min", new Attribute("valid_min", 0));
			varAttributeMap.put("valid_max", new Attribute("valid_max", 9999));
			varAttributeMap.put("units", new Attribute("units", "count"));
			varAttributeMap.put("_FillValue", new Attribute("_FillValue", "-9999"));
			VAR_LUT.put("25", varAttributeMap);
		}
		{
			final HashMap<String, Attribute> varAttributeMap = new HashMap<String, Attribute>();		
			varAttributeMap.put("variable_name", new Attribute("variable_name", "pcpndv"));	
			varAttributeMap.put("long_name", new Attribute("long_name", "Monthly Precipitation"));
			varAttributeMap.put("valid_min", new Attribute("valid_min", 0));
			varAttributeMap.put("valid_max", new Attribute("valid_max", 99.99));
			varAttributeMap.put("units", new Attribute("units", "in"));
			varAttributeMap.put("_FillValue", new Attribute("_FillValue", "-9.99"));
			VAR_LUT.put("01", varAttributeMap);
		}
		{
			final HashMap<String, Attribute> varAttributeMap = new HashMap<String, Attribute>();		
			varAttributeMap.put("variable_name", new Attribute("variable_name", "pdsidv"));	
			varAttributeMap.put("long_name", new Attribute("long_name", "Palmer Drought Severity Index (PDSI)"));
			varAttributeMap.put("valid_min", new Attribute("valid_min", -20));
			varAttributeMap.put("valid_max", new Attribute("valid_max", 20));
			varAttributeMap.put("units", new Attribute("units", "index"));
			varAttributeMap.put("_FillValue", new Attribute("_FillValue", "-99.99"));
			VAR_LUT.put("05", varAttributeMap);
		}
		{
			final HashMap<String, Attribute> varAttributeMap = new HashMap<String, Attribute>();		
			varAttributeMap.put("variable_name", new Attribute("variable_name", "phdidv"));	
			varAttributeMap.put("long_name", new Attribute("long_name", "Palmer Hydrologic Drought Index (PHDI)"));
			varAttributeMap.put("valid_min", new Attribute("valid_min", -20));
			varAttributeMap.put("valid_max", new Attribute("valid_max", 20));
			varAttributeMap.put("units", new Attribute("units", "index"));
			varAttributeMap.put("_FillValue", new Attribute("_FillValue", "-99.99"));
			VAR_LUT.put("06", varAttributeMap);
		}
		{
			final HashMap<String, Attribute> varAttributeMap = new HashMap<String, Attribute>();		
			varAttributeMap.put("variable_name", new Attribute("variable_name", "pmdidv"));	
			varAttributeMap.put("long_name", new Attribute("long_name", "Palmer Modified Drought Index (PMDI)"));
			varAttributeMap.put("valid_min", new Attribute("valid_min", -20));
			varAttributeMap.put("valid_max", new Attribute("valid_max", 20));
			varAttributeMap.put("units", new Attribute("units", "index"));
			varAttributeMap.put("_FillValue", new Attribute("_FillValue", "-99.99"));
			VAR_LUT.put("08", varAttributeMap);
		}
		{
			final HashMap<String, Attribute> varAttributeMap = new HashMap<String, Attribute>();		
			varAttributeMap.put("variable_name", new Attribute("variable_name", "zndxdv"));	
			varAttributeMap.put("long_name", new Attribute("long_name", "Palmer Z Index (ZNDX)"));
			varAttributeMap.put("valid_min", new Attribute("valid_min", -20));
			varAttributeMap.put("valid_max", new Attribute("valid_max", 20));
			varAttributeMap.put("units", new Attribute("units", "index"));
			varAttributeMap.put("_FillValue", new Attribute("_FillValue", "-99.99"));
			VAR_LUT.put("07", varAttributeMap);
		}
		{
			final HashMap<String, Attribute> varAttributeMap = new HashMap<String, Attribute>();		
			varAttributeMap.put("variable_name", new Attribute("variable_name", "sp01dv"));	
			varAttributeMap.put("long_name", new Attribute("long_name", "1-month Standardized Precipitation Index"));
			varAttributeMap.put("valid_min", new Attribute("valid_min", -4));
			varAttributeMap.put("valid_max", new Attribute("valid_max", 4));
			varAttributeMap.put("units", new Attribute("units", "index"));
			varAttributeMap.put("_FillValue", new Attribute("_FillValue", "-99.99"));
			VAR_LUT.put("71", varAttributeMap);
		}
		{
			final HashMap<String, Attribute> varAttributeMap = new HashMap<String, Attribute>();		
			varAttributeMap.put("variable_name", new Attribute("variable_name", "sp02dv"));	
			varAttributeMap.put("long_name", new Attribute("long_name", "2-month Standardized Precipitation Index"));
			varAttributeMap.put("valid_min", new Attribute("valid_min", -4));
			varAttributeMap.put("valid_max", new Attribute("valid_max", 4));
			varAttributeMap.put("units", new Attribute("units", "index"));
			varAttributeMap.put("_FillValue", new Attribute("_FillValue", "-99.99"));
			VAR_LUT.put("72", varAttributeMap);
		}
		{
			final HashMap<String, Attribute> varAttributeMap = new HashMap<String, Attribute>();		
			varAttributeMap.put("variable_name", new Attribute("variable_name", "sp03dv"));	
			varAttributeMap.put("long_name", new Attribute("long_name", "3-month Standardized Precipitation Index"));
			varAttributeMap.put("valid_min", new Attribute("valid_min", -4));
			varAttributeMap.put("valid_max", new Attribute("valid_max", 4));
			varAttributeMap.put("units", new Attribute("units", "index"));
			varAttributeMap.put("_FillValue", new Attribute("_FillValue", "-99.99"));
			VAR_LUT.put("73", varAttributeMap);
		}
		{
			final HashMap<String, Attribute> varAttributeMap = new HashMap<String, Attribute>();		
			varAttributeMap.put("variable_name", new Attribute("variable_name", "sp06dv"));	
			varAttributeMap.put("long_name", new Attribute("long_name", "6-month Standardized Precipitation Index"));
			varAttributeMap.put("valid_min", new Attribute("valid_min", -4));
			varAttributeMap.put("valid_max", new Attribute("valid_max", 4));
			varAttributeMap.put("units", new Attribute("units", "index"));
			varAttributeMap.put("_FillValue", new Attribute("_FillValue", "-99.99"));
			VAR_LUT.put("74", varAttributeMap);
		}
		{
			final HashMap<String, Attribute> varAttributeMap = new HashMap<String, Attribute>();		
			varAttributeMap.put("variable_name", new Attribute("variable_name", "sp09dv"));	
			varAttributeMap.put("long_name", new Attribute("long_name", "9-month Standardized Precipitation Index"));
			varAttributeMap.put("valid_min", new Attribute("valid_min", -4));
			varAttributeMap.put("valid_max", new Attribute("valid_max", 4));
			varAttributeMap.put("units", new Attribute("units", "index"));
			varAttributeMap.put("_FillValue", new Attribute("_FillValue", "-99.99"));
			VAR_LUT.put("75", varAttributeMap);
		}
		{
			final HashMap<String, Attribute> varAttributeMap = new HashMap<String, Attribute>();		
			varAttributeMap.put("variable_name", new Attribute("variable_name", "sp12dv"));	
			varAttributeMap.put("long_name", new Attribute("long_name", "12-month Standardized Precipitation Index"));
			varAttributeMap.put("valid_min", new Attribute("valid_min", -4));
			varAttributeMap.put("valid_max", new Attribute("valid_max", 4));
			varAttributeMap.put("units", new Attribute("units", "index"));
			varAttributeMap.put("_FillValue", new Attribute("_FillValue", "-99.99"));
			VAR_LUT.put("76", varAttributeMap);
		}
		{
			final HashMap<String, Attribute> varAttributeMap = new HashMap<String, Attribute>();		
			varAttributeMap.put("variable_name", new Attribute("variable_name", "sp24dv"));	
			varAttributeMap.put("long_name", new Attribute("long_name", "24-month Standardized Precipitation Index"));
			varAttributeMap.put("valid_min", new Attribute("valid_min", -4));
			varAttributeMap.put("valid_max", new Attribute("valid_max", 4));
			varAttributeMap.put("units", new Attribute("units", "index"));
			varAttributeMap.put("_FillValue", new Attribute("_FillValue", "-99.99"));
			VAR_LUT.put("77", varAttributeMap);
		}
		{
			final HashMap<String, Attribute> varAttributeMap = new HashMap<String, Attribute>();		
			varAttributeMap.put("variable_name", new Attribute("variable_name", "tmpcdv"));	
			varAttributeMap.put("long_name", new Attribute("long_name", "Monthly Average Temperature"));
			varAttributeMap.put("valid_min", new Attribute("valid_min", -50));
			varAttributeMap.put("valid_max", new Attribute("valid_max", 140));
			varAttributeMap.put("units", new Attribute("units", "degF"));
			varAttributeMap.put("_FillValue", new Attribute("_FillValue", "-99.90"));
			VAR_LUT.put("02", varAttributeMap);
		}
		{
			final HashMap<String, Attribute> varAttributeMap = new HashMap<String, Attribute>();		
			varAttributeMap.put("variable_name", new Attribute("variable_name", "tmindv"));	
			varAttributeMap.put("long_name", new Attribute("long_name", "Monthly Minimum Temperature"));
			varAttributeMap.put("valid_min", new Attribute("valid_min", -50));
			varAttributeMap.put("valid_max", new Attribute("valid_max", 140));
			varAttributeMap.put("units", new Attribute("units", "degF"));
			varAttributeMap.put("_FillValue", new Attribute("_FillValue", "-99.90"));
			VAR_LUT.put("28", varAttributeMap);
		}
		{
			final HashMap<String, Attribute> varAttributeMap = new HashMap<String, Attribute>();		
			varAttributeMap.put("variable_name", new Attribute("variable_name", "tmaxdv"));	
			varAttributeMap.put("long_name", new Attribute("long_name", "Monthly Maximum Temperature"));
			varAttributeMap.put("valid_min", new Attribute("valid_min", -50));
			varAttributeMap.put("valid_max", new Attribute("valid_max", 140));
			varAttributeMap.put("units", new Attribute("units", "degF"));
			varAttributeMap.put("_FillValue", new Attribute("_FillValue", "-99.90"));
			VAR_LUT.put("27", varAttributeMap);
		}
		
	}
	
	
	
//	public static void main(String[] args) { 
//		String infile=" ";
//		if (args.length == 1) {
//			infile=args[0];
//		} else { System.out.println("Usage: java CrnIOServiceProvider inputFile");
//		System.exit(0);  
//		}   	   
//		try {   
//			NetcdfFile.registerIOProvider(ClimDivIOServiceProvider.class);
//			NetcdfFile ncfile=NetcdfFile.open(infile);
//			System.out.println("ncfile = \n"+ncfile);
//			String outfile="C:\\Documents and Settings\\Nina.Stroumentova\\crn_netcdf\\data_out\\crnout.nc";
//			NetcdfFile outFile = ucar.nc2.FileWriter.writeToFile(ncfile, outfile, false, 0);                
//			ncfile.close();
//			outFile.close();
//
//		} catch (Exception e) {
//			System.out.println("MAIN!!!   "+e.toString());
//			e.printStackTrace(); }
//	}
	//-------------------------------------------------------------------------------


	
	
	
	
	
	
	/**  Open existing file. */

	public void open(ucar.unidata.io.RandomAccessFile raf, ucar.nc2.NetcdfFile ncfile,
			ucar.nc2.util.CancelTask cancelTask) throws IOException {
		
		System.out.println("NAME="+raf.getLocation());
		String filename=raf.getLocation();  
		
		// replace with tighter regex
		if (raf.getLocation().contains("dv-v")) {
			divisionType = DivisionType.CLIMATEDIV;
		}
		else if (raf.getLocation().contains("st-v")) {
			divisionType = DivisionType.STATE;
		}
		else if (raf.getLocation().contains("cy-v")) {
			divisionType = DivisionType.COUNTY;
		}
		else {
			throw new IOException("Could not determine division type from filename.");
		}
		
	//------------------------------------------------------------------------------------ 
		
		HashMap<String, TimeSeriesValues> dataCache = new HashMap<String, TimeSeriesValues>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

			// Define the number of lines of input file          
			int lineCount = linecount(raf); System.out.println("LINES="+lineCount);
			String datatype = null;
			
//			String[] location = new String[lines];
//			String[] datatype = new String[lines];
//			String[] year = new String[lines];
//			float[] janData = new float[lines];
//			float[] febData = new float[lines];
//			float[] marData = new float[lines];
//			float[] aprData = new float[lines];
//			float[] mayData = new float[lines];
//			float[] junData = new float[lines];
//			float[] julData = new float[lines];
//			float[] augData = new float[lines];
//			float[] sepData = new float[lines];
//			float[] octData = new float[lines];
//			float[] novData = new float[lines];
//			float[] decData = new float[lines];

			raf.seek(0);  
	        FeatureCache featureCache = null;
			for (int i = 0; i < lineCount; i++) {
				
				if (WCTUtils.getSharedCancelTask().isCancel()) {
					return;
				}
				
				int len=0;  int pos0=0;
				String line = raf.readLine(); //System.out.println("i="+i+" "+q);  

// documentation:
//  ftp://ftp.ncdc.noaa.gov/pub/data/cirs/climdiv/divisional-readme.txt
//		or
//	https://www1.ncdc.noaa.gov/pub/data/cirs/climdiv/state-readme.txt
// example file:
//  ftp://ftp.ncdc.noaa.gov/pub/data/cirs/climdiv/climdiv-sp02dv-v1.0.0-20170206
//				
// 0         1         2         3         4         5         6         7         8         9				
// 01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890				
// 0101721895 -99.99  -0.22  -0.51  -0.10  -1.14  -0.41   0.13   0.18   0.17  -0.18  -1.10  -0.50
//			
//
//ELEMENT CODE        5-6      01 = Precipitation
//                             02 = Average Temperature
//                             05 = PDSI
//                             06 = PHDI
//                             07 = ZNDX
//                             08 = PMDI
//                             25 = Heating Degree Days
//                             26 = Cooling Degree Days
//                             27 = Maximum Temperature
//                             28 = Minimum Temperature
//			  				   71 = 1-month Standardized Precipitation Index
//							   72 = 2-month Standardized Precipitation Index
//                             73 = 3-month Standardized Precipitation Index
//                             74 = 6-month Standardized Precipitation Index
//                             75 = 9-month Standardized Precipitation Index
//                             76 = 12-month Standardized Precipitation Index
//                             77 = 24-month Standardized Precipitation Index
				
				
// 0010071895   2.35  -1.77   1.08  -1.17  -0.18   1.73  -0.30   1.19  -2.35  -0.28  -2.08  -1.07  
// 02017011951   1.09   0.44   0.45   1.16   0.56   0.00   1.21   3.10   0.84   1.07   0.96   1.72
				
				
				String location = null;
				if (divisionType == DivisionType.STATE) {
					location = line.substring(0, 3);
					if (location.charAt(0) != '0') {
//						System.err.println("skipping non-state region with location id of: "+location);
//						continue;
//						location = "999";
					}
				}
				else if (divisionType == DivisionType.COUNTY) {
					location = line.substring(0, 5);
					// after parsing 5 digit location, 
					// shift everything back one char so monthly data columns line up.
					line = line.substring(1); 
				}
				else {
					location = line.substring(0, 4);
				}
				
				
				
				TimeSeriesValues tsv = dataCache.get(location);
				if (tsv == null) {
					tsv = new TimeSeriesValues(location);
				}
				
				datatype = line.substring(4, 6);  
				String year = line.substring(6, 10);  
				
				try {
					
				float janData = Float.parseFloat(line.substring(10, 17));
				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"01"), janData));
				float febData = Float.parseFloat(line.substring(17, 24));
				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"02"), febData));
				float marData = Float.parseFloat(line.substring(24, 31));
				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"03"), marData));
				float aprData = Float.parseFloat(line.substring(31, 38));
				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"04"), aprData));
				float mayData = Float.parseFloat(line.substring(38, 45));
				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"05"), mayData));
				float junData = Float.parseFloat(line.substring(45, 52));
				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"06"), junData));
				float julData = Float.parseFloat(line.substring(52, 59));
				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"07"), julData));
				float augData = Float.parseFloat(line.substring(59, 66));
				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"08"), augData));
				float sepData = Float.parseFloat(line.substring(66, 73));
				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"09"), sepData));
				float octData = Float.parseFloat(line.substring(73, 80));
				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"10"), octData));
				float novData = Float.parseFloat(line.substring(80, 87));
				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"11"), novData));
				float decData = Float.parseFloat(line.substring(87, 94));
				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"12"), decData));
							
				dataCache.put(location, tsv);
				
				} catch (Exception e) {
					System.err.println("line: \n"+line);
					e.printStackTrace();
				}
			}
			
			
			
			
	        URL mapDataURL = new URL(WCTConstants.MAP_DATA_JAR_URL);
	        if (featureCache == null) {
	        	if (divisionType == DivisionType.CLIMATEDIV) {
	        		URL url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/climate-divisions.shp", null);
	        		ShapefileDataStore ds = new ShapefileDataStore(url);
	        		featureCache = new FeatureCache(ds.getFeatureSource("climate-divisions"), "ID");
	        		System.out.println(featureCache.getFeatureCount() + " features in climate division feature cache");
	        	}
	        	else if (divisionType == DivisionType.STATE) {
	        		// https://www1.ncdc.noaa.gov/pub/data/cirs/climdiv/state-readme.txt
	        		URL url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/states_from_cb_2017_us_county_500k.shp", null);
	        		ShapefileDataStore ds = new ShapefileDataStore(url);
	        		featureCache = new FeatureCache(ds.getFeatureSource("states_from_cb_2017_us_county_500k"), "NCEI_STATE");
	        		System.out.println(featureCache.getFeatureCount() + " features in state feature cache");
	        	}
	        	else if (divisionType == DivisionType.COUNTY) {
	        		URL url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/cb_2017_us_county_500k.shp", null);
	        		ShapefileDataStore ds = new ShapefileDataStore(url);
	        		featureCache = new FeatureCache(ds.getFeatureSource("cb_2017_us_county_500k"), "NCEI_CNTY");
	        		System.out.println(featureCache.getFeatureCount() + " features in county feature cache");
	        	}
	        }
			System.out.println(featureCache.getKeys());
			
			
			
			
			int numLocations = dataCache.size();
			String[] locationArray = dataCache.keySet().toArray(new String[numLocations]);
			Arrays.sort(locationArray);
			String[] infoArray = new String[numLocations];
			int maxLocationStrLength = 0, maxInfoStrLength = 0;
			
			
			
			for (int n=0; n<numLocations; n++) {
				maxLocationStrLength = Math.max(maxLocationStrLength, locationArray[n].length());
				Feature divFeature = featureCache.getFeature(locationArray[n]);
				
			
				
				if (divFeature == null) {
					System.err.println("No feature lookup found for ID="+locationArray[n]);
				}
				else {
					if (divisionType == DivisionType.CLIMATEDIV) {
						infoArray[n] = divFeature.getAttribute("ST").toString() + " - " + divFeature.getAttribute("NAME").toString();
						maxInfoStrLength = Math.max(maxInfoStrLength, infoArray[n].length());
					} 
					else if (divisionType == DivisionType.STATE) {
						infoArray[n] = divFeature.getAttribute("NAME").toString();
						maxInfoStrLength = Math.max(maxInfoStrLength, infoArray[n].length());					
					}
					else if (divisionType == DivisionType.COUNTY) {
						infoArray[n] = divFeature.getAttribute("NAME").toString();
						maxInfoStrLength = Math.max(maxInfoStrLength, infoArray[n].length());						
					}
				}
				
				

				
			}
			
			// count number of dates in first location.  assume all locations have same number of dates.
			int numDates = dataCache.get(locationArray[0]).getDateValuePairList().size();
			
			System.out.println("data cache - num locations: "+numLocations + " ; num dates: "+numDates);
			System.out.println("max locations strlen: "+maxLocationStrLength+" ; info strlen: "+maxInfoStrLength);

			// Define Dimensions, Variables, Attributes for NetCDF file
			List<Dimension> dataDimList=new ArrayList<Dimension> ();  
			List<Dimension> idList=new ArrayList<Dimension> ();
			List<Dimension> coordDimList=new ArrayList<Dimension> ();
			List<Dimension> descList=new ArrayList<Dimension> ();
			Dimension obsDim=new Dimension("obs", numDates, true);
			Dimension stnDim=new Dimension("location", numLocations, true);
			Dimension nameDim=new Dimension("id_strlen", maxLocationStrLength, true);
			Dimension wDim=new Dimension("name_strlen", maxInfoStrLength, true);
			dataDimList.add(stnDim);  dataDimList.add(obsDim);   // for numeric variables
			coordDimList.add(stnDim);                     // for lon, lat
			idList.add(stnDim); idList.add(nameDim);       // for char id
			descList.add(stnDim); descList.add(wDim);        // for char desc

			ncfile.addDimension(null, obsDim);
			ncfile.addDimension(null, stnDim);
			ncfile.addDimension(null, nameDim);
			ncfile.addDimension(null, wDim);

//			int[] shape = new int[] {1, lines};
//			int[] sh = new int[]{1}; 
//			int[] nsh = new int[]{1,6};  
//			int[] wsh = new int[]{1,5};

//		    char station_id(station=4818, id_len=4);
//		      :long_name = "Station id";
//		      :reference = "sfmetar_sa.tbl";
//
//		    char station_description(station=4818, location_len=39);
//		      :long_name = "Geographic station description";
//		      :standard_name = "region";
//		      :reference = "sfmetar_sa.tbl";



			Variable locationIdVar = new Variable(ncfile, null, null, "location_name");
			locationIdVar.setDimensions("location id_strlen");
			locationIdVar.setDataType(DataType.CHAR);
			locationIdVar.addAttribute( new Attribute("long_name", "The location ID"));
			locationIdVar.addAttribute( new Attribute("standard_name", "platform_id"));
			locationIdVar.addAttribute( new Attribute("cf_role", "timeseries_id"));
			ArrayChar charArray = new ArrayChar(new int[]{numLocations, maxLocationStrLength});
			Index idx = charArray.getIndex();
			for (int locationIndex=0; locationIndex<numLocations; locationIndex++) {
				if (locationArray[locationIndex] != null) {
					char[] ca = locationArray[locationIndex].toCharArray();
					System.out.println("setting location ID: " + locationArray[locationIndex]);
					for (int i=0; i<maxLocationStrLength; i++) { charArray.setChar(idx.set(locationIndex,i), ca[i]); }
				}
			}
			locationIdVar.setCachedData(charArray, false);
			ncfile.addVariable(null, locationIdVar);
			varList.add(locationIdVar);
			//NCdump.printArray(nameArray, "station_name", System.out, null);
			resultMap.put("location_name", charArray); 


			Variable locationInfoVar = new Variable(ncfile, null, null, "location_info");
			locationInfoVar.setDimensions("location name_strlen");
			locationInfoVar.setDataType(DataType.CHAR);
			locationInfoVar.addAttribute( new Attribute("long_name", "The location info"));
			locationInfoVar.addAttribute( new Attribute("standard_name", "platform_name"));

			ArrayChar infoCharArray = new ArrayChar(new int[]{numLocations, maxInfoStrLength});
			Index infoIdx = infoCharArray.getIndex();
			for (int locationIndex=0; locationIndex<numLocations; locationIndex++) {
				if (infoArray[locationIndex] != null) {
					char[] ca = infoArray[locationIndex].toCharArray();
					for (int i=0; i<ca.length; i++) { 
						//							System.out.println(infoIdx.toString()+" "+locationIndex+","+i+" max:"+maxInfoStrLength+" ");
						infoCharArray.setChar(infoIdx.set(locationIndex, i), ca[i]); 
					}
				}
			}
			locationInfoVar.setCachedData(infoCharArray, false);
			ncfile.addVariable(null, locationInfoVar);
			varList.add(locationInfoVar);
			//NCdump.printArray(nameArray, "station_name", System.out, null);
			resultMap.put("location_desc", infoCharArray); 



			ArrayFloat.D1 latArray=(ArrayFloat.D1)Array.factory(DataType.FLOAT, new int[]{ numLocations });
			ArrayFloat.D1 lonArray=(ArrayFloat.D1)Array.factory(DataType.FLOAT, new int[]{ numLocations });


				for (int locationIndex=0; locationIndex<numLocations; locationIndex++) {

					// calculate feature centroid for division
					String divId = locationArray[locationIndex].trim();
//					if (divId.startsWith("0")) {
//						divId = divId.substring(1);
//					}

					Feature divFeature = featureCache.getFeature(divId);
							
							
//					if (divisionType == DivisionType.CLIMATEDIV) {
//						divFeature = featureCache.getFeature(new Integer(divId));
//					}
//					else if (divisionType == DivisionType.STATE) {
//						divFeature = featureCache.getFeature(divId);
//						
//					}
					
					if (divFeature == null) {
						System.err.println("No feature lookup found for ID="+divId);
					}
					else {
						Point centroid = divFeature.getDefaultGeometry().getCentroid();					
						latArray.set(locationIndex, (float)centroid.getY());
						lonArray.set(locationIndex, (float)centroid.getX());
					}
				}
				
				
				
				
				
				Variable var = new Variable(ncfile, null, null, "lat");
				var.setDimensions(coordDimList);
				var.setDataType(DataType.FLOAT);
				var.addAttribute( new Attribute("long_name", "Division centroid latitude coordinate"));
				var.addAttribute( new Attribute("units", "degrees_north"));
				var.addAttribute( new Attribute(_Coordinate.AxisType, AxisType.Lat.toString()));
				var.setCachedData(latArray, false);
				ncfile.addVariable( null, var);
				varList.add(var);
				resultMap.put("lat", latArray); 
				// NCdump.printArray(latArray, "latitude", System.out, null);

				var = new Variable(ncfile, null, null, "lon");
				var.setDimensions(coordDimList);
				var.setDataType(DataType.FLOAT);
				var.addAttribute( new Attribute("long_name", "Division centroid longitude coordinate"));
				var.addAttribute( new Attribute("units", "degrees_east"));
				var.addAttribute( new Attribute(_Coordinate.AxisType, AxisType.Lon.toString()));
				var.setCachedData(lonArray, false);
				ncfile.addVariable( null, var);
				varList.add(var);
				resultMap.put("lon", lonArray); 
				// NCdump.printArray(lonArray, "longitude", System.out, null); 

				// time shared across all stations
				ArrayInt.D1 timeArray=(ArrayInt.D1)Array.factory(DataType.INT, new int[] { numDates });
				// time unique to each station - makes slicing across stations at specific time much harder
				//ArrayInt.D2 timeArray=(ArrayInt.D2)Array.factory(DataType.INT, new int[] { numLocations, numDates });
				Index tIx=timeArray.getIndex();  
				ArrayFloat.D2 valArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, new int[] { numLocations, numDates });
				Index valIdx=valArray.getIndex();  


				for (int i=0; i<numLocations; i++) {
					TimeSeriesValues tsv = dataCache.get(locationArray[i]);
					for (int t=0; t<tsv.getDateValuePairList().size(); t++) {
						DateValuePair dvp = tsv.getDateValuePairList().get(t);			
//						int daysSince = (int)(dvp.getDate().getTime() / 1000 / 60 / 60 / 24);
//						timeArray.setInt(tIx.set(i, t), daysSince);				
						
						
//						valArray.set(valIdx.set(i, t), (float)dvp.getValue());	
						
						// TODO - fix this
						try {
							valArray.set(valIdx.set(i, t), (float)dvp.getValue());
						} catch (Exception e) {
							System.err.println("valArray.set(valIdx.set("+i+", "+t+"), "+(float)dvp.getValue()+")");
							e.printStackTrace();
//							System.err.println(e.printStackTrace());
							System.err.println("Num of dates: "+tsv.getDateValuePairList().size());
						}
					}
				}
				
				TimeSeriesValues tsv = dataCache.get(locationArray[0]);
				for (int t=0; t<tsv.getDateValuePairList().size(); t++) {
					DateValuePair dvp = tsv.getDateValuePairList().get(t);			
					int daysSince = (int)(dvp.getDate().getTime() / 1000 / 60 / 60 / 24);
					timeArray.setInt(tIx.set(t), daysSince);				
				}
			

			Variable timeVar = new Variable(ncfile, null, null, "time");
			timeVar.setDimensions(Arrays.asList(new Dimension[] { obsDim }));
			timeVar.setDataType(DataType.INT);
			timeVar.addAttribute(new Attribute("standard_name", "time"));
			timeVar.addAttribute(new Attribute("long_name", " The local calendar date of the values."));
			timeVar.addAttribute(new Attribute("units", "days since 1970-01-01 00:00:00"));
			timeVar.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Time.toString()));
			timeVar.addAttribute(new Attribute("axis", "T"));
			timeVar.setCachedData(timeArray, false);
			ncfile.addVariable( null, timeVar);
			varList.add(timeVar);
			resultMap.put("time", timeArray); 
			//NCdump.printArray(timeArray, "time", System.out, null);

			HashMap<String, Attribute> varInfo = VAR_LUT.get(datatype);
			Variable dataVar = new Variable(ncfile, null, null, varInfo.get("variable_name").getStringValue());
			dataVar.setDimensions(dataDimList);
			dataVar.setDataType(DataType.FLOAT);
			if (divisionType == DivisionType.CLIMATEDIV) {
				dataVar.addAttribute(new Attribute("shape_lookup", "climate-divisions.shp"));
				dataVar.addAttribute(new Attribute("shape_lookup_id_attribute", "ID"));
			}
			else if (divisionType == DivisionType.STATE) {
				dataVar.addAttribute(new Attribute("shape_lookup", "states_from_cb_2017_us_county_500k.shp"));
				dataVar.addAttribute(new Attribute("shape_lookup_id_attribute", "NCEI_STATE"));		
			}
			else if (divisionType == DivisionType.COUNTY) {
				dataVar.addAttribute(new Attribute("shape_lookup", "cb_2017_us_county_500k.shp"));
				dataVar.addAttribute(new Attribute("shape_lookup_id_attribute", "NCEI_CNTY"));				
			}
			
//			dataVar.addAttribute(new Attribute("long_name", "The value: TODO: use LUT to get description from dataset code in data."));
			dataVar.addAttribute(varInfo.get("long_name"));
			dataVar.addAttribute(varInfo.get("_FillValue"));
			dataVar.addAttribute(varInfo.get("valid_min"));
			dataVar.addAttribute(varInfo.get("valid_max"));
			dataVar.addAttribute(varInfo.get("units"));
			dataVar.addAttribute(new Attribute("coordinates", "time lat lon"));
			dataVar.setCachedData(valArray, false);
			ncfile.addVariable( null, dataVar);
			varList.add(var);
			resultMap.put("VALUE", valArray); 
			// NCdump.printArray(crxArray, "CRX_VN", System.out, null); 

			// working example?
//		    int station_id(station=1);
//		      :long_name = "station identification code";
//		      :standard_name = "platform_id";
//
//		    char station_name(station=1, id_strlen=15);
//		      :long_name = "3-part station name (state, location, vector)";
//		      :standard_name = "platform_name";
//		      :cf_role = "timeseries_id";

			ncfile.addAttribute(null, new Attribute("station_description", "location_info"));
			ncfile.addAttribute(null, new Attribute("Conventions", "CF-1.4"));
			ncfile.addAttribute(null, new Attribute("CF:featureType", "timeSeries"));
			ncfile.addAttribute(null, new Attribute("title", "nClimDiv)"));
			ncfile.addAttribute(null, new Attribute("description", "nClimDiv dataset from: ftp://ftp.ncdc.noaa.gov/pub/data/cirs/climdiv/"));
//			String s0=date[0].substring(0,4)+"-"+date[0].substring(4,6)+"-"+date[0].substring(6);
//			ncfile.addAttribute(null, new Attribute("time_coverage_start", s0));
//			String s1=date[lines-1].substring(0,4)+"-"+date[lines-1].substring(4,6)+"-"+
//			date[lines-1].substring(6);
//			ncfile.addAttribute(null, new Attribute("time_coverage_end", ""+s1));



			//System.out.println("VARS="+varList.size());
			//for (int j=0; j< varList.size(); j++) {
			//System.out.println("j="+(j+1)+" NAME: "+varList.get(j).getName()); 
			//}

			System.out.println("RESULT="+resultMap.size());

			featureCache.clear();
			ncfile.finish();


	}
	//------------------------------------------------------------------------------------------------------------


	/** Calculate the number of lines in input ISD data file  */
	int linecount(ucar.unidata.io.RandomAccessFile raf) throws IOException {
		long pos = raf.getFilePointer();
		raf.seek(0);
		int sz=(int)raf.length();	
		int lines = 0;

		while (pos < sz) {  
			try { 				
				if (raf.readByte() == '\n') {  
					lines++; pos = raf.getFilePointer();}
			} catch (java.io.EOFException e) { e.printStackTrace();
			break;
			}
		}	raf.seek(raf.getFilePointer());			
		return lines;
	}
	//-------------------------------------------------------------------------

	
	
	public void close() throws IOException { /*this.raf.close();*/ }

	public String getDetailInfo() { return "ClimDiv IOServiceProvider."; }
	public String getFileTypeId(){ return "ClimDiv"; }
	public String getFileTypeDescription(){ return " "; }

	/** A way to communicate arbitrary information to an iosp. */
	public Object sendIospMessage(Object message) {
		return super.sendIospMessage(message);
	}

	/**
	 * Checks to see if an CRN file is indeed valid
	 * Read a name of input file  and define if there is
	 * the "CRN" sequence.
	 *
	 * @param raf  the file to determine ownership
	 */
	public boolean isValidFile(final RandomAccessFile raf) throws IOException {
		String filename=raf.getLocation();
//		System.out.println(filename);
		return (filename.contains("climdiv-") && filename.matches(".*-[0-9]{8}$"));     
	}
	//-----------------------------------------------------------------------------------------------------

	/** Read data from a top level Variable and return a memory resident Array */
	public Array readData(ucar.nc2.Variable v2, Section wantSection) 
	throws IOException, InvalidRangeException {
		String varName=v2.getName();
		Array arr=resultMap.get(varName);       
		return arr.section(wantSection.getRanges());
	}

	/** Read data from a Variable that is nested in one or more Structures */
	public Array readNestedData(Variable v2, List section) throws IOException, InvalidRangeException {
		return null;
	}

	/** Extend the file if needed in a way that is compatible with the current metadata */
	public boolean syncExtend() throws IOException {
		return false;
	}

	/** Check if file has changed, and reread metadata if needed */
	public boolean sync() throws IOException {
		return false;
	}

	/** A way to communicate arbitrary information to an iosp */
	public void setSpecial(Object special) {  }
	public String toStringDebug(Object o) {
		return null;
	}

	
	
	class TimeSeriesValues {
		private String locationID;
		private ArrayList<DateValuePair> dateValuePairList = new ArrayList<DateValuePair>();
		
		public TimeSeriesValues(String locationID) {
			this.locationID = locationID;
		}
		
		public String getLocationID() {
			return locationID;
		}
		public void setLocationID(String locationID) {
			this.locationID = locationID;
		}
		public ArrayList<DateValuePair> getDateValuePairList() {
			return dateValuePairList;
		}
		public void setDateValuePairList(ArrayList<DateValuePair> dateValuePairList) {
			this.dateValuePairList = dateValuePairList;
		}
	}
	
	class DateValuePair {
		private Date date;
		private double value;
		
		public DateValuePair(Date date, double value) {
			this.date = date;
			this.value = value;
		}
		
		public Date getDate() {
			return date;
		}
		public void setDate(Date date) {
			this.date = date;
		}
		public double getValue() {
			return value;
		}
		public void setValue(double value) {
			this.value = value;
		}
	}
}
