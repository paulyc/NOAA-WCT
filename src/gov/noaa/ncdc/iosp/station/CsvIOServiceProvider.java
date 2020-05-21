//package gov.noaa.ncdc.iosp.station;
//
//import java.io.IOException;
//import java.net.URL;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.TimeZone;
//
//import org.geotools.data.shapefile.ShapefileDataStore;
//import org.geotools.feature.Feature;
//
//import com.vividsolutions.jts.geom.Point;
//
//import gov.noaa.ncdc.common.FeatureCache;
//import gov.noaa.ncdc.wct.ResourceUtils;
//import gov.noaa.ncdc.wct.WCTConstants;
//import gov.noaa.ncdc.wct.WCTUtils;
//import ucar.ma2.Array;
//import ucar.ma2.ArrayChar;
//import ucar.ma2.ArrayFloat;
//import ucar.ma2.ArrayInt;
//import ucar.ma2.DataType;
//import ucar.ma2.Index;
//import ucar.ma2.InvalidRangeException;
//import ucar.ma2.Section;
//import ucar.nc2.Attribute;
//import ucar.nc2.Dimension;
//import ucar.nc2.Variable;
//import ucar.nc2.constants.AxisType;
//import ucar.nc2.constants._Coordinate;
//import ucar.nc2.iosp.AbstractIOServiceProvider;
//import ucar.unidata.io.RandomAccessFile;
//
///** IOService Provider for CSV data */
//
//public class CsvIOServiceProvider extends AbstractIOServiceProvider {      
//	ucar.unidata.io.RandomAccessFile raf;
//	ArrayList<Variable> varList=new ArrayList<Variable>();
//	Map<String, Array> resultMap=new HashMap<String, Array> (); 
//
//	
//	
//	/**  Open existing file. */
//
//	public void open(ucar.unidata.io.RandomAccessFile raf, ucar.nc2.NetcdfFile ncfile,
//			ucar.nc2.util.CancelTask cancelTask) throws IOException {
//		
//		System.out.println("NAME="+raf.getLocation());
//		String filename=raf.getLocation();  
//		
//	//------------------------------------------------------------------------------------ 
//
//		// Define the number of lines of input file so we know the time dimension size
//		// Scan file and track number of unique locations and times
//		
//		
//		HeaderInfo headerInfo = scanHeader(raf);
//		CsvInfo csvInfo = scanFile(raf);
//		
//		System.out.println("LINES="+csvInfo.getLineCount());
//
//		
//		
//			
//			
//
//			raf.seek(0);  
//			for (int n=0; n<csvInfo.getLineCount(); n++) {
//				
//				if (WCTUtils.getSharedCancelTask().isCancel()) {
//					return;
//				}
//				
//				int len=0;  int pos0=0;
//				String line = raf.readLine(); //System.out.println("i="+i+" "+q);  
//
//				
//				String location = line.substring(0, 4);
////				TimeSeriesValues tsv = dataCache.get(location);
////				if (tsv == null) {
////					tsv = new TimeSeriesValues(location);
////				}
//				
////				datatype = line.substring(4, 6);  
//				String year = line.substring(6, 10);  
//				
////				try {
////					
////				float janData = Float.parseFloat(line.substring(10, 17));
////				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"01"), janData));
////				float febData = Float.parseFloat(line.substring(17, 24));
////				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"02"), febData));
////				float marData = Float.parseFloat(line.substring(24, 31));
////				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"03"), marData));
////				float aprData = Float.parseFloat(line.substring(31, 38));
////				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"04"), aprData));
////				float mayData = Float.parseFloat(line.substring(38, 45));
////				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"05"), mayData));
////				float junData = Float.parseFloat(line.substring(45, 52));
////				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"06"), junData));
////				float julData = Float.parseFloat(line.substring(52, 59));
////				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"07"), julData));
////				float augData = Float.parseFloat(line.substring(59, 66));
////				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"08"), augData));
////				float sepData = Float.parseFloat(line.substring(66, 73));
////				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"09"), sepData));
////				float octData = Float.parseFloat(line.substring(73, 80));
////				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"10"), octData));
////				float novData = Float.parseFloat(line.substring(80, 87));
////				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"11"), novData));
////				float decData = Float.parseFloat(line.substring(87, 94));
////				tsv.getDateValuePairList().add(new DateValuePair(sdf.parse(year+"12"), decData));
////							
////				dataCache.put(location, tsv);
////				
////				} catch (Exception e) {
////					e.printStackTrace();
////				}
//			}
//			
//			
//			
//			
//	        URL mapDataURL = new URL(WCTConstants.MAP_DATA_JAR_URL);
//			URL url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/climate-divisions.shp", null);
//			ShapefileDataStore ds = new ShapefileDataStore(url);
//			FeatureCache featureCache = new FeatureCache(ds.getFeatureSource("climate-divisions"), "DIVISION_I");
//			System.out.println(featureCache.getFeatureCount() + " features in feature cache");
////			System.out.println(featureCache.getKeys());
//			
//			
//			
//			
////			int numLocations = dataCache.size();
////			String[] locationArray = dataCache.keySet().toArray(new String[numLocations]);
////			Arrays.sort(locationArray);
////			String[] infoArray = new String[numLocations];
////			int maxLocationStrLength = 0, maxInfoStrLength = 0;
////			for (int n=0; n<numLocations; n++) {
////				maxLocationStrLength = Math.max(maxLocationStrLength, locationArray[n].length());
////				Feature divFeature = featureCache.getFeature(new Integer(locationArray[n]));
////				if (divFeature == null) {
////					System.err.println("No feature lookup found for ID="+locationArray[n]);
////				}
////				else {
////					infoArray[n] = divFeature.getAttribute("ST").toString() + " - " + divFeature.getAttribute("NAME").toString();
////					maxInfoStrLength = Math.max(maxInfoStrLength, infoArray[n].length());
////				}
////			}
//			
//			// count number of dates in first location.  assume all locations have same number of dates.
////			int numDates = dataCache.get(locationArray[0]).getDateValuePairList().size();
//			
//			
//			int numLocations = csvInfo.getUniqueIdList().size();
//			int numDates = csvInfo.getUniqueDateList().size();
//			
//			// Add dimensions and variables, including string-length dimensions
//			if (headerInfo.getStationIdIndex() >= 0) {
//				DataColumn headerCol = headerInfo.getDataColumnList().get(headerInfo.getStationIdIndex());
//				String dimName = headerCol.getVarName();
//				ncfile.addDimension(null, new Dimension(dimName, numLocations, true));					
//				
//				int maxStringLength = headerCol.getMaxSize();
//				ncfile.addDimension(null, new Dimension(dimName+"_strlen", maxStringLength, false));					
//
//				Variable idVar = new Variable(ncfile, null, null, dimName);
//				idVar.setDimensions(dimName+" "+dimName+"_strlen");
//				idVar.setDataType(DataType.CHAR);
//				idVar.addAttribute( new Attribute("long_name", headerCol.getLongName()));
//				idVar.addAttribute( new Attribute("standard_name", "platform_id"));
//				idVar.addAttribute( new Attribute("cf_role", "timeseries_id"));
//				
//				ArrayChar charArray = new ArrayChar(new int[]{numLocations, maxStringLength});
//				Index idx = charArray.getIndex();
//				for (int locationIndex=0; locationIndex<numLocations; locationIndex++) {
//					if (csvInfo.getUniqueIdList().get(locationIndex) != null) {
//						char[] ca = csvInfo.getUniqueIdList().get(locationIndex).toCharArray();
//						System.out.println("setting location ID: " + csvInfo.getUniqueIdList().get(locationIndex));
//						for (int i=0; i<maxStringLength; i++) { charArray.setChar(idx.set(locationIndex,i), ca[i]); }
//					}
//				}
//				idVar.setCachedData(charArray, false);
//				ncfile.addVariable(null, idVar);
//			}
//			
//			if (headerInfo.getDateIndex() >= 0) {
//				DataColumn headerCol = headerInfo.getDataColumnList().get(headerInfo.getDateIndex());
//				
//				String dimName = headerCol.getVarName();
//				ncfile.addDimension(null, new Dimension(dimName, csvInfo.getUniqueIdList().size(), false));		
//				
//				//  ID(timeseries_id|"Station Name"),
//				//    lat(degrees_north|"Station Latitude"),lon(degrees_east|"Station Longitude",
//				//    alt(m|"vertical distance above the surface",
//				//    time(#yyyyMMdd|"Observation Date and Time")
//				
//				// date/time format is in units position
//				String dateFormatString = headerCol.getUnits();
//				SimpleDateFormat sdf = new SimpleDateFormat(dateFormatString);
//				sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
//				
//				Variable timeVar = new Variable(ncfile, null, null, dimName);
//				timeVar.setDimensions(dimName);
//				timeVar.setDataType(DataType.INT);
//				timeVar.addAttribute(new Attribute("standard_name", "time"));
//				timeVar.addAttribute(new Attribute("long_name", " The local calendar date of the values."));
//				timeVar.addAttribute(new Attribute("units", "days since 1970-01-01 00:00:00"));
//				timeVar.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Time.toString()));
//				timeVar.addAttribute(new Attribute("axis", "T"));
//				timeVar.setCachedData(timeArray, false);
//				ncfile.addVariable( null, timeVar);
//				varList.add(timeVar);
//			}
//			
//			ArrayList<DataColumn> dataColList = headerInfo.getDataColumnList();
//			for (DataColumn dataCol : dataColList) {
//				if (dataCol.getDataColumnType() == DataColumnType.STRING) {
//					Dimension dim = new Dimension(dataCol.getVarName()+"_strlen", dataCol.getMaxSize(), false);
//					ncfile.addDimension(null, dim);
//					System.out.println("Adding dimension: "+dim);
//				}
//			}
//			
//			
//			
//
////			int[] shape = new int[] {1, lines};
////			int[] sh = new int[]{1}; 
////			int[] nsh = new int[]{1,6};  
////			int[] wsh = new int[]{1,5};
//
////		    char station_id(station=4818, id_len=4);
////		      :long_name = "Station id";
////		      :reference = "sfmetar_sa.tbl";
////
////		    char station_description(station=4818, location_len=39);
////		      :long_name = "Geographic station description";
////		      :standard_name = "region";
////		      :reference = "sfmetar_sa.tbl";
//
//			for (DataColumn dataCol : dataColList) {
//				if (dataCol.getDataColumnType() == DataColumnType.STRING) {
//					Variable var = new Variable(ncfile, null, null, dataCol.getVarName());
//					var.setDimensions(dimName);
//					var.setDataType(DataType.INT);
//					timeVar.addAttribute(new Attribute("standard_name", "time"));
//					timeVar.addAttribute(new Attribute("long_name", " The local calendar date of the values."));
//					timeVar.addAttribute(new Attribute("units", "days since 1970-01-01 00:00:00"));
//					timeVar.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Time.toString()));
//					timeVar.addAttribute(new Attribute("axis", "T"));
//					timeVar.setCachedData(timeArray, false);
//					ncfile.addVariable( null, timeVar);
//					varList.add(timeVar);
//					System.out.println("Adding variable: "+var);
//				}
//			}
//
//			Variable locationIdVar = new Variable(ncfile, null, null, "location_name");
//			locationIdVar.setDimensions("location id_strlen");
//			locationIdVar.setDataType(DataType.CHAR);
//			locationIdVar.addAttribute( new Attribute("long_name", "The location ID"));
//			locationIdVar.addAttribute( new Attribute("standard_name", "platform_id"));
//			locationIdVar.addAttribute( new Attribute("cf_role", "timeseries_id"));
//			ArrayChar charArray = new ArrayChar(new int[]{numLocations, maxLocationStrLength});
//			Index idx = charArray.getIndex();
//			for (int locationIndex=0; locationIndex<numLocations; locationIndex++) {
//				if (locationArray[locationIndex] != null) {
//					char[] ca = locationArray[locationIndex].toCharArray();
//					System.out.println("setting location ID: " + locationArray[locationIndex]);
//					for (int i=0; i<maxLocationStrLength; i++) { charArray.setChar(idx.set(locationIndex,i), ca[i]); }
//				}
//			}
//			locationIdVar.setCachedData(charArray, false);
//			ncfile.addVariable(null, locationIdVar);
//			varList.add(locationIdVar);
//			//NCdump.printArray(nameArray, "station_name", System.out, null);
//			resultMap.put("location_name", charArray); 
//
//
//			Variable locationInfoVar = new Variable(ncfile, null, null, "location_info");
//			locationInfoVar.setDimensions("location name_strlen");
//			locationInfoVar.setDataType(DataType.CHAR);
//			locationInfoVar.addAttribute( new Attribute("long_name", "The location info"));
//			locationInfoVar.addAttribute( new Attribute("standard_name", "platform_name"));
//
//			ArrayChar infoCharArray = new ArrayChar(new int[]{numLocations, maxInfoStrLength});
//			Index infoIdx = infoCharArray.getIndex();
//			for (int locationIndex=0; locationIndex<numLocations; locationIndex++) {
//				if (infoArray[locationIndex] != null) {
//					char[] ca = infoArray[locationIndex].toCharArray();
//					for (int i=0; i<ca.length; i++) { 
//						//							System.out.println(infoIdx.toString()+" "+locationIndex+","+i+" max:"+maxInfoStrLength+" ");
//						infoCharArray.setChar(infoIdx.set(locationIndex, i), ca[i]); 
//					}
//				}
//			}
//			locationInfoVar.setCachedData(infoCharArray, false);
//			ncfile.addVariable(null, locationInfoVar);
//			varList.add(locationInfoVar);
//			//NCdump.printArray(nameArray, "station_name", System.out, null);
//			resultMap.put("location_desc", infoCharArray); 
//
//
//
//			ArrayFloat.D1 latArray=(ArrayFloat.D1)Array.factory(DataType.FLOAT, new int[]{ numLocations });
//			ArrayFloat.D1 lonArray=(ArrayFloat.D1)Array.factory(DataType.FLOAT, new int[]{ numLocations });
//
//
//				for (int locationIndex=0; locationIndex<numLocations; locationIndex++) {
//
//					// calculate feature centroid for division
//					String divId = locationArray[locationIndex].trim();
//					if (divId.startsWith("0")) {
//						divId = divId.substring(1);
//					}
//					Feature divFeature = featureCache.getFeature(new Integer(divId));
//					if (divFeature == null) {
//						System.err.println("No feature lookup found for ID="+divId);
//					}
//					else {
//						Point centroid = divFeature.getDefaultGeometry().getCentroid();					
//						latArray.set(locationIndex, (float)centroid.getY());
//						lonArray.set(locationIndex, (float)centroid.getX());
//					}
//				}
//				
//				
//				Variable var = new Variable(ncfile, null, null, "lat");
//				var.setDimensions(coordDimList);
//				var.setDataType(DataType.FLOAT);
//				var.addAttribute( new Attribute("long_name", "Division centroid latitude coordinate"));
//				var.addAttribute( new Attribute("units", "degrees_north"));
//				var.addAttribute( new Attribute(_Coordinate.AxisType, AxisType.Lat.toString()));
//				var.setCachedData(latArray, false);
//				ncfile.addVariable( null, var);
//				varList.add(var);
//				resultMap.put("lat", latArray); 
//				// NCdump.printArray(latArray, "latitude", System.out, null);
//
//				var = new Variable(ncfile, null, null, "lon");
//				var.setDimensions(coordDimList);
//				var.setDataType(DataType.FLOAT);
//				var.addAttribute( new Attribute("long_name", "Division centroid longitude coordinate"));
//				var.addAttribute( new Attribute("units", "degrees_east"));
//				var.addAttribute( new Attribute(_Coordinate.AxisType, AxisType.Lon.toString()));
//				var.setCachedData(lonArray, false);
//				ncfile.addVariable( null, var);
//				varList.add(var);
//				resultMap.put("lon", lonArray); 
//				// NCdump.printArray(lonArray, "longitude", System.out, null); 
//
//				// time shared across all stations
//				ArrayInt.D1 timeArray=(ArrayInt.D1)Array.factory(DataType.INT, new int[] { numDates });
//				// time unique to each station - makes slicing across stations at specific time much harder
//				//ArrayInt.D2 timeArray=(ArrayInt.D2)Array.factory(DataType.INT, new int[] { numLocations, numDates });
//				Index tIx=timeArray.getIndex();  
//				ArrayFloat.D2 valArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, new int[] { numLocations, numDates });
//				Index valIdx=valArray.getIndex();  
//
//
//				for (int i=0; i<numLocations; i++) {
//					TimeSeriesValues tsv = dataCache.get(locationArray[i]);
//					for (int t=0; t<tsv.getDateValuePairList().size(); t++) {
//						DateValuePair dvp = tsv.getDateValuePairList().get(t);			
////						int daysSince = (int)(dvp.getDate().getTime() / 1000 / 60 / 60 / 24);
////						timeArray.setInt(tIx.set(i, t), daysSince);					
//						valArray.set(valIdx.set(i, t), (float)dvp.getValue());
//					}
//				}
//				
//				TimeSeriesValues tsv = dataCache.get(locationArray[0]);
//				for (int t=0; t<tsv.getDateValuePairList().size(); t++) {
//					DateValuePair dvp = tsv.getDateValuePairList().get(t);			
//					int daysSince = (int)(dvp.getDate().getTime() / 1000 / 60 / 60 / 24);
//					timeArray.setInt(tIx.set(t), daysSince);				
//				}
//			
//
//			Variable timeVar = new Variable(ncfile, null, null, "time");
//			timeVar.setDimensions(Arrays.asList(new Dimension[] { obsDim }));
//			timeVar.setDataType(DataType.INT);
//			timeVar.addAttribute(new Attribute("standard_name", "time"));
//			timeVar.addAttribute(new Attribute("long_name", " The local calendar date of the values."));
//			timeVar.addAttribute(new Attribute("units", "days since 1970-01-01 00:00:00"));
//			timeVar.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Time.toString()));
//			timeVar.addAttribute(new Attribute("axis", "T"));
//			timeVar.setCachedData(timeArray, false);
//			ncfile.addVariable( null, timeVar);
//			varList.add(timeVar);
//			resultMap.put("time", timeArray); 
//			//NCdump.printArray(timeArray, "time", System.out, null);
//
//			HashMap<String, Attribute> varInfo = VAR_LUT.get(datatype);
//			Variable dataVar = new Variable(ncfile, null, null, varInfo.get("variable_name").getStringValue());
//			dataVar.setDimensions(dataDimList);
//			dataVar.setDataType(DataType.FLOAT);
//			dataVar.addAttribute(new Attribute("shape_lookup", "climate-divisions.shp"));
////			dataVar.addAttribute(new Attribute("long_name", "The value: TODO: use LUT to get description from dataset code in data."));
//			dataVar.addAttribute(varInfo.get("long_name"));
//			dataVar.addAttribute(varInfo.get("_FillValue"));
//			dataVar.addAttribute(varInfo.get("valid_min"));
//			dataVar.addAttribute(varInfo.get("valid_max"));
//			dataVar.addAttribute(varInfo.get("units"));
//			dataVar.addAttribute(new Attribute("coordinates", "time lat lon"));
//			dataVar.setCachedData(valArray, false);
//			ncfile.addVariable( null, dataVar);
//			varList.add(var);
//			resultMap.put("VALUE", valArray); 
//			// NCdump.printArray(crxArray, "CRX_VN", System.out, null); 
//
//			// working example?
////		    int station_id(station=1);
////		      :long_name = "station identification code";
////		      :standard_name = "platform_id";
////
////		    char station_name(station=1, id_strlen=15);
////		      :long_name = "3-part station name (state, location, vector)";
////		      :standard_name = "platform_name";
////		      :cf_role = "timeseries_id";
//
//			ncfile.addAttribute(null, new Attribute("station_description", "location_info"));
//			ncfile.addAttribute(null, new Attribute("Conventions", "CF-1.4"));
//			ncfile.addAttribute(null, new Attribute("CF:featureType", "timeSeries"));
//			ncfile.addAttribute(null, new Attribute("title", "nClimDiv)"));
//			ncfile.addAttribute(null, new Attribute("description", "nClimDiv dataset from: ftp://ftp.ncdc.noaa.gov/pub/data/cirs/climdiv/"));
////			String s0=date[0].substring(0,4)+"-"+date[0].substring(4,6)+"-"+date[0].substring(6);
////			ncfile.addAttribute(null, new Attribute("time_coverage_start", s0));
////			String s1=date[lines-1].substring(0,4)+"-"+date[lines-1].substring(4,6)+"-"+
////			date[lines-1].substring(6);
////			ncfile.addAttribute(null, new Attribute("time_coverage_end", ""+s1));
//
//
//
//			//System.out.println("VARS="+varList.size());
//			//for (int j=0; j< varList.size(); j++) {
//			//System.out.println("j="+(j+1)+" NAME: "+varList.get(j).getName()); 
//			//}
//
//			System.out.println("RESULT="+resultMap.size());
//
//			featureCache.clear();
//			ncfile.finish();
//
//
//	}
//	//------------------------------------------------------------------------------------------------------------
//
//
//	/** Calculate the number of lines in input ISD data file  */
//	int linecount(ucar.unidata.io.RandomAccessFile raf) throws IOException {
//		long pos = raf.getFilePointer();
//		raf.seek(0);
//		int sz=(int)raf.length();	
//		int lines = 0;
//
//		while (pos < sz) {  
//			try { 				
//				if (raf.readByte() == '\n') {  
//					lines++; pos = raf.getFilePointer();}
//			} catch (java.io.EOFException e) { e.printStackTrace();
//			break;
//			}
//		}	raf.seek(raf.getFilePointer());			
//		return lines;
//	}
//	
//	
//	public HeaderInfo scanHeader(ucar.unidata.io.RandomAccessFile raf) throws IOException {
//		
//		//  ID(timeseries_id|"Station Name"),
//		//    lat(degrees_north|"Station Latitude"),lon(degrees_east|"Station Longitude",
//		//    alt(m|"vertical distance above the surface",
//		//    time(#yyyyMMdd|"Observation Date and Time")
//		raf.seek(0);
//		String headerLine = raf.readLine();
//		HeaderInfo headerInfo = new HeaderInfo();
//		String[] headerCols = headerLine.split(",");
//		headerInfo.setNumberOfColumns(headerCols.length);
//		for (int n=0; n<headerCols.length; n++) {
//			String s = headerCols[n].trim();
//			String varName = s.substring(0, s.indexOf("("));
//			String[] attributes = s.substring(s.indexOf("("), s.indexOf(")")).split("\\|");
//			if (attributes[0].equals("timeseries_id")) {
//				headerInfo.setStationIdIndex(n);
//			}
//			else if (varName.equals("date") || varName.equals("time")) {
//				headerInfo.setDateIndex(n);
//			}
//			else if (varName.equals("lat") || varName.equals("latitude")) {
//				headerInfo.setLatIndex(n);
//			}
//			else if (varName.equals("lon") || varName.equals("longitude")) {
//				headerInfo.setLonIndex(n);
//			}
//			else if (varName.equals("alt") || varName.equals("height") || varName.equals("z")) {
//				headerInfo.setAltIndex(n);
//			}
//			else {
//				// ex:  precip(mm|"total accumulation"|precipitation_amount)
//				DataColumn dataColumn = new DataColumn();
//				dataColumn.setColumnIndex(n);
//				dataColumn.setVarName(varName);
//				dataColumn.setUnits(attributes[0]);
//				dataColumn.setLongName(attributes[1]);
//				if (attributes.length > 2) {
//					dataColumn.setStandardName(attributes[2]);
//				}
//				headerInfo.addDataColumn(dataColumn);
//			}
//		}
//		
//		return headerInfo;
//	}
//	
//	
//	
//	public CsvInfo scanFile(ucar.unidata.io.RandomAccessFile raf) throws IOException {
//		
//		HeaderInfo headerInfo = scanHeader(raf); 
//		
//		int lineCount = 0;
//		ArrayList<String> uniqueIdList = new ArrayList<String>();
//		ArrayList<String> uniqueDateList = new ArrayList<String>();
//		raf.seek(0);
//		// skip header
//		raf.readLine();
//		
//		String line = null;
//		while ( (line=raf.readLine()) != null) {
//			String[] cols = line.split(",");
//			if (cols.length != headerInfo.getNumberOfColumns()) {
//				System.out.println("Number of columns in this line doesn't match header.  "
//						+ "Expected "+headerInfo.getNumberOfColumns()+" cols.  This line: "+line);
//				continue;  // skip this line
//			}
//			
//			// for each column that isn't 'special' id/lat/lon/time/alt, 
//			//  check if column value is a string, decimal or int.  If string, track max length.
//			ArrayList<DataColumn> dataColumnList = headerInfo.getDataColumnList();
//			
//			// check first time
//			if (lineCount == 0) {
//				for (DataColumn dc : dataColumnList) {
//					String data = cols[dc.getColumnIndex()];
//					// check for decimal
//					if (data.matches("^[0-9]*$")) {
//						dc.setDataColumnType(DataColumnType.INT);
//					}
//					// check for int
//					else if (data.matches("^[0-9.]*$")) {
//						dc.setDataColumnType(DataColumnType.DECIMAL);
//					}
//					else {
//						dc.setDataColumnType(DataColumnType.STRING);					
//					}
//				}
//			}
//			
//			// check first 100 lines, then check once every 50 after
//			if (lineCount < 100 || lineCount % 50 == 0) {
//				for (DataColumn dc : dataColumnList) {
//					String data = cols[dc.getColumnIndex()];
//					if (dc.getMaxSize() < data.length()) {
//						dc.setMaxSize(data.length());
//					}
//				}
//			}
//			
//			if (! uniqueIdList.contains(cols[headerInfo.getStationIdIndex()].trim())) {
//				uniqueIdList.add(cols[headerInfo.getStationIdIndex()].trim());
//			}
//			if (! uniqueDateList.contains(cols[headerInfo.getDateIndex()].trim())) {
//				uniqueDateList.add(cols[headerInfo.getDateIndex()].trim());
//			}
//			lineCount++;
//		}
//		
//		CsvInfo csvInfo = new CsvInfo();
//		csvInfo.setLineCount(lineCount);
//		csvInfo.setUniqueIdList(uniqueIdList);
//		csvInfo.setUniqueDateList(uniqueDateList);
//		
//		return csvInfo;		
//	}
//	
//	//-------------------------------------------------------------------------
//
//	
//	
//	public void close() throws IOException { /*this.raf.close();*/ }
//
//	public String getDetailInfo() { return "ClimDiv IOServiceProvider."; }
//	public String getFileTypeId(){ return "ClimDiv"; }
//	public String getFileTypeDescription(){ return " "; }
//
//	/** A way to communicate arbitrary information to an iosp. */
//	public Object sendIospMessage(Object message) {
//		return super.sendIospMessage(message);
//	}
//
//	/**
//	 * Checks to see if an CRN file is indeed valid
//	 * Read a name of input file  and define if there is
//	 * the "CRN" sequence.
//	 *
//	 * @param raf  the file to determine ownership
//	 */
//	public boolean isValidFile(final RandomAccessFile raf) throws IOException {
//		String filename=raf.getLocation();
////		System.out.println(filename);
//		return (filename.contains("climdiv-") && filename.matches(".*-[0-9]{8}$"));     
//	}
//	//-----------------------------------------------------------------------------------------------------
//
//	/** Read data from a top level Variable and return a memory resident Array */
//	public Array readData(ucar.nc2.Variable v2, Section wantSection) 
//	throws IOException, InvalidRangeException {
//		String varName=v2.getName();
//		Array arr=resultMap.get(varName);       
//		return arr.section(wantSection.getRanges());
//	}
//
//	/** Read data from a Variable that is nested in one or more Structures */
//	public Array readNestedData(Variable v2, List section) throws IOException, InvalidRangeException {
//		return null;
//	}
//
//	/** Extend the file if needed in a way that is compatible with the current metadata */
//	public boolean syncExtend() throws IOException {
//		return false;
//	}
//
//	/** Check if file has changed, and reread metadata if needed */
//	public boolean sync() throws IOException {
//		return false;
//	}
//
//	/** A way to communicate arbitrary information to an iosp */
//	public void setSpecial(Object special) {  }
//	public String toStringDebug(Object o) {
//		return null;
//	}
//
//	
//	
//	class TimeSeriesValues {
//		private String locationID;
//		private ArrayList<DateValuePair> dateValuePairList = new ArrayList<DateValuePair>();
//		
//		public TimeSeriesValues(String locationID) {
//			this.locationID = locationID;
//		}
//		
//		public String getLocationID() {
//			return locationID;
//		}
//		public void setLocationID(String locationID) {
//			this.locationID = locationID;
//		}
//		public ArrayList<DateValuePair> getDateValuePairList() {
//			return dateValuePairList;
//		}
//		public void setDateValuePairList(ArrayList<DateValuePair> dateValuePairList) {
//			this.dateValuePairList = dateValuePairList;
//		}
//	}
//	
//	class DateValuePair {
//		private Date date;
//		private double value;
//		
//		public DateValuePair(Date date, double value) {
//			this.date = date;
//			this.value = value;
//		}
//		
//		public Date getDate() {
//			return date;
//		}
//		public void setDate(Date date) {
//			this.date = date;
//		}
//		public double getValue() {
//			return value;
//		}
//		public void setValue(double value) {
//			this.value = value;
//		}
//	}
//	
//	
//	
//	
//	class HeaderInfo {
//		private int stationIdIndex = -1;
//		private int latIndex = -1;
//		private int lonIndex = -1;
//		private int altIndex = -1;
//		private int dateIndex = -1;
//		private int numberOfColumns;
//		private ArrayList<DataColumn> dataColumnList = new ArrayList<DataColumn>();
//		
//		public int getStationIdIndex() {
//			return stationIdIndex;
//		}
//		public void setStationIdIndex(int stationIdIndex) {
//			this.stationIdIndex = stationIdIndex;
//		}
//		public int getLatIndex() {
//			return latIndex;
//		}
//		public void setLatIndex(int latIndex) {
//			this.latIndex = latIndex;
//		}
//		public int getLonIndex() {
//			return lonIndex;
//		}
//		public void setLonIndex(int lonIndex) {
//			this.lonIndex = lonIndex;
//		}
//		public int getAltIndex() {
//			return altIndex;
//		}
//		public void setAltIndex(int altIndex) {
//			this.altIndex = altIndex;
//		}
//		public int getNumberOfColumns() {
//			return numberOfColumns;
//		}
//		public void setNumberOfColumns(int numberOfColumns) {
//			this.numberOfColumns = numberOfColumns;
//		}
//		public int getDateIndex() {
//			return dateIndex;
//		}
//		public void setDateIndex(int dateIndex) {
//			this.dateIndex = dateIndex;
//		}
//
//		public ArrayList<DataColumn> getDataColumnList() {
//			return dataColumnList;
//		}
//		public void addDataColumn(DataColumn dataColumn) {
//			this.dataColumnList.add(dataColumn);
//		}
//	}
//	
//	class CsvInfo {
//		private int lineCount;
//		private ArrayList<String> uniqueIdList;
//		private ArrayList<String> uniqueDateList;
//		
//		public int getLineCount() {
//			return lineCount;
//		}
//		public void setLineCount(int lineCount) {
//			this.lineCount = lineCount;
//		}
//		public ArrayList<String> getUniqueIdList() {
//			return uniqueIdList;
//		}
//		public void setUniqueIdList(ArrayList<String> uniqueIdList) {
//			this.uniqueIdList = uniqueIdList;
//		}
//		public ArrayList<String> getUniqueDateList() {
//			return uniqueDateList;
//		}
//		public void setUniqueDateList(ArrayList<String> uniqueDateList) {
//			this.uniqueDateList = uniqueDateList;
//		}
//	}
//	
//
//	private static enum DataColumnType { DECIMAL, INT, STRING };
//	class DataColumn {
//		private int columnIndex = -1;
//		private String varName;
//		private String units;
//		private String longName;
//		private String standardName;
//		private DataColumnType dataColumnType;
//		private int maxSize;
//		
//		public int getColumnIndex() {
//			return columnIndex;
//		}
//		public int getMaxSize() {
//			return maxSize;
//		}
//		public void setMaxSize(int maxSize) {
//			this.maxSize = maxSize;
//		}
//		public void setColumnIndex(int columnIndex) {
//			this.columnIndex = columnIndex;
//		}
//		public String getVarName() {
//			return varName;
//		}
//		public void setVarName(String varName) {
//			this.varName = varName;
//		}
//		public String getUnits() {
//			return units;
//		}
//		public void setUnits(String units) {
//			this.units = units;
//		}
//		public String getLongName() {
//			return longName;
//		}
//		public void setLongName(String longName) {
//			this.longName = longName;
//		}
//		public String getStandardName() {
//			return standardName;
//		}
//		public void setStandardName(String standardName) {
//			this.standardName = standardName;
//		}
//		public DataColumnType getDataColumnType() {
//			return dataColumnType;
//		}
//		public void setDataColumnType(DataColumnType dataColumnType) {
//			this.dataColumnType = dataColumnType;
//		}
//	}
//}
