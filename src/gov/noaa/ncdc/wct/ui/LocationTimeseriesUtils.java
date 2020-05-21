package gov.noaa.ncdc.wct.ui;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.geotools.filter.IllegalFilterException;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTUtils;
import ucar.ma2.DataType;
import ucar.ma2.StructureData;
import ucar.ma2.StructureMembers;
import ucar.ma2.StructureMembers.Member;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.PointFeatureIterator;
import ucar.nc2.ft.StationTimeSeriesFeature;
import ucar.nc2.ft.StationTimeSeriesFeatureCollection;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateUnit;
import ucar.unidata.geoloc.Station;

public class LocationTimeseriesUtils {
	/**
	 * Find timeseries index that is the closest to given date (in milliseconds).  The timeseries datasets
	 * must all use the same shared array (spacing and range) of date values.
	 * @param dataset
	 * @param xMillis
	 * @return
	 */
	public static int getClosestTimeSeriesIndex(TimeSeriesCollection[] dataset, long xMillis) {
		long xDiff = Long.MAX_VALUE;
		int closestIndex = 0;
		TimeSeries timeSeries = ((TimeSeries)dataset[0].getSeries().get(0));
		for (int n=0; n<timeSeries.getItemCount(); n++) {
			long timeValue = timeSeries.getDataItem(n).getPeriod().getMiddleMillisecond();
			if (Math.abs(timeValue-xMillis) < xDiff) {
				xDiff = Math.abs(timeValue-xMillis);
				closestIndex = n;
			}
		}
		return closestIndex;
	}
	
	
	/**
	 * Creates list of values at a specified index. First item in list is the formatted matching date.  
	 * The date of the first timeseries is used, so it is assumed that all timeseries share the same
	 * matching 'array' of date values (range and spacing).
	 * @param dataset
	 * @param index
	 * @return
	 */
	public static List<String> getTimeSeriesInfoString(TimeSeriesCollection[] dataset, long xMillis) {		
		return getTimeSeriesInfoString(dataset, getClosestTimeSeriesIndex(dataset, xMillis));		
	}
	
	
	/**
	 * Creates list of values at a specified index. First item in list is the formatted matching date.  
	 * The date of the first timeseries is used, so it is assumed that all timeseries share the same
	 * matching 'array' of date values (range and spacing).
	 * @param dataset
	 * @param index
	 * @return
	 */
	public static List<String> getTimeSeriesInfoString(TimeSeriesCollection[] dataset, int index) {		

		TimeSeries timeSeries = ((TimeSeries)dataset[0].getSeries().get(0));
		ArrayList<String> valList = new ArrayList<String>();
		valList.add(WCTUtils.DATE_HOUR_MIN_FORMATTER.format(new Date(timeSeries.getDataItem(index).getPeriod().getMiddleMillisecond())));
		for (TimeSeriesCollection tsc : dataset) {
			for (Object ts : tsc.getSeries()) {
				double val = ((TimeSeries)ts).getDataItem(index).getValue().doubleValue();
				if (val > 100000 || val < -100000 || 
						(val > 0 && val < 0.00001) ||
						(val < 0 && val > -0.00001))  {
					
					valList.add(((TimeSeries)ts).getDescription()+"="+WCTUtils.DECFMT_SCI.format(val));					
				}
				else {
					valList.add(((TimeSeries)ts).getDescription()+"="+WCTUtils.DECFMT_pDpppp.format(val));
				}	
			}
		}
		
		return valList;
	}
	
	
	
	public static String[] getPlottableVariables(PointFeatureIterator pfIter) throws WCTException, IOException {

		if (pfIter.hasNext()) {
			PointFeature pf = pfIter.next();
			StructureData sdata = pf.getDataAll();
			StructureMembers smembers = sdata.getStructureMembers();
			List<String> nameList = smembers.getMemberNames();

			ArrayList<String> plottableList = new ArrayList<String>();
			for (int n=0; n<nameList.size(); n++) {

				if ((smembers.getMember(n).getDataType() == DataType.BYTE ||
						smembers.getMember(n).getDataType() == DataType.DOUBLE ||
						smembers.getMember(n).getDataType() == DataType.FLOAT ||
						smembers.getMember(n).getDataType() == DataType.INT ||
						smembers.getMember(n).getDataType() == DataType.LONG ||
						smembers.getMember(n).getDataType() == DataType.SHORT) ) {				

					String s = smembers.getMember(n).getDescription();
					String desc = "";
					if (s != null) {
						if (s.length() > 40) {
							s = s.substring(0, 40) + "...";
						}
						desc = "  ("+s+")";
					}
					plottableList.add(nameList.get(n) + desc);
				}
			}
			return plottableList.toArray(new String[plottableList.size()]);

			//			String[] varArray = nameList.toArray(new String[nameList.size()]);
			//			for (int n=0; n<varArray.length; n++) {
			//				varArray[n] = varArray[n].concat(" "+smembers.getMember(n).getDescription());
			//			}
			//			
			//			return varArray;
		}
		else {
			throw new WCTException("No variables found for this station");
		}
	}
	
	
	

	/**
	 * 
	 * @param fdp
	 * @param var
	 * @return
	 * @throws NoSharedTimeDimensionException
	 * @throws IOException 
	 */
	public static List<String> getUniqueTimeList(FeatureDatasetPoint fdp, Variable var) throws NoSharedTimeDimensionException, IOException {
		Attribute coordAtt = var.findAttribute("coordinates");
		if (coordAtt == null) {
			throw new NoSharedTimeDimensionException(
					"The coordinates attribute was not found for the data variable "+var.getShortName()+"  \n" +
							"Only station data structures with the one-dimensional time dimension can be quickly \n" +
							"sliced at a specific time across all stations.  Please refer to the Type #1 of \n" +
					"Discrete Sampling Geometries in the CF-documentation.");
		}
		System.out.println(coordAtt.getStringValue());
		String[] coordinateVariableArray = coordAtt.getStringValue().split(" ");


		// check:
		// 1) for each coordinate variable listed, which one has standard_name='time'?
		// 2) for time coordinate variable, is it one-dimensional?
		// 3) perhaps also check that the same dimension is used in the data variable and coord variable (probably named 'time')

		for (String coordVarName : coordinateVariableArray) {
			Variable coordVariable = fdp.getNetcdfFile().findVariable(coordVarName);
			if (coordVariable.findAttribute("standard_name").getStringValue().equals("time")) {
				System.out.println("time coord. variable is: "+coordVariable.getFullName());
				if (coordVariable.getRank() != 1) {
					throw new NoSharedTimeDimensionException(
							"The time coordinate variable was found, but is not one-dimensional.  \n\n" +
									"Only station data structures with the one-dimensional time dimension can be quickly \n" +
									"sliced at a specific time across all stations.  Please refer to the Type #1 of \n" +
							"Discrete Sampling Geometries in the CF-documentation.");
				}
				System.out.println("  and it is only-one dimensional!  proceed...");

				if (var.findDimensionIndex(coordVariable.getDimension(0).getShortName()) < 0) {
					throw new NoSharedTimeDimensionException(
							"The time coordinate variable was found, the dimensions of the data variable and time \n" +
									"coordinate variable do not match.  \n\n" +
									"Only station data structures with the one-dimensional time dimension can be quickly \n" +
									"sliced at a specific time across all stations.  Please refer to the Type #1 of \n" +
							"Discrete Sampling Geometries in the CF-documentation.");
				}

				System.out.println("found matching dimension too!");

				CalendarDateUnit cdu = CalendarDateUnit.of(null, coordVariable.findAttribute("units").getStringValue());
				double[] data = (double[])coordVariable.read().get1DJavaArray(Double.class);
				ArrayList<String> uniqueDates = new ArrayList<String>();
				uniqueDates.ensureCapacity(data.length);

				for (double val : data) {
					//					System.out.println(val);
					CalendarDate cd = cdu.makeCalendarDate(val);
					//					System.out.println(cd);
					uniqueDates.add(cd.toString());
				}				

				return uniqueDates;
			}
		}

		// if we get to this, throw exception
		throw new NoSharedTimeDimensionException("No shared time dimension could be found.  \n" +
				"Only station data structures with the shared time dimension can be quickly \n" +
				"sliced at a specific time across all stations.  Please refer to the Type #1 of \n" +
				"Discrete Sampling Geometries in the CF-documentation.");

	}

	static class NoSharedTimeDimensionException extends Exception {	
		public NoSharedTimeDimensionException(String message) {
			super(message);
		}
	}

	
	

	public static String[] getStationPlottableVariables(StationTimeSeriesFeatureCollection stsfc, Station station) throws WCTException, IOException {
		if (stsfc == null) {
			throw new WCTException("No data file has been loaded");
		}
		StationTimeSeriesFeature sf = stsfc.getStationFeature(station);
		PointFeatureIterator pfIter = sf.getPointFeatureIterator(1*1024*1024);

		return getPlottableVariables(pfIter);
	}



	public static String[] getStationUnits(StationTimeSeriesFeatureCollection stsfc, String id, String[] vars) throws WCTException, IOException {
		return getStationUnits(stsfc, stsfc.getStation(id), vars);
	}
	public static String[] getStationUnits(StationTimeSeriesFeatureCollection stsfc, Station station, String[] vars) throws WCTException, IOException {
		if (stsfc == null) {
			throw new WCTException("No data file has been loaded");
		}
		StationTimeSeriesFeature sf = stsfc.getStationFeature(station);
		PointFeatureIterator pfIter = sf.getPointFeatureIterator(1*1024*1024);
		if (pfIter.hasNext()) {
			PointFeature pf = pfIter.next();
			StructureData sdata = pf.getDataAll();
			StructureMembers smembers = sdata.getStructureMembers();
			List<Member> memberList = smembers.getMembers();
			//			System.out.println(memberList);
			//			System.out.println(smembers.getMemberNames());
			String[] unitsArray = new String[vars.length];
			for (int i=0; i<vars.length; i++) {
				for (int n=0; n<memberList.size(); n++) {
					if (memberList.get(n).getName().equals(vars[i])) {
						unitsArray[i] = memberList.get(n).getUnitsString();
						if (unitsArray[i] == null) {
							unitsArray[i] = "No Units";
						}
					}
				}
			}
			return unitsArray;
		}
		else {
			throw new WCTException("No variable units found for this station");
		}
	}





	public static String getStationListingString(Station station) {
		return station.getName() + " ("+station.getDescription()+")";
	}

	public static String[] getStationVariables(StationTimeSeriesFeatureCollection stsfc, String id) throws WCTException, IOException {
		return getStationVariables(stsfc, stsfc.getStation(id));
	}
	public static String[] getStationVariables(StationTimeSeriesFeatureCollection stsfc, Station station) throws WCTException, IOException {
		if (stsfc == null) {
			throw new WCTException("No data file has been loaded");
		}
		StationTimeSeriesFeature sf = stsfc.getStationFeature(station);
		PointFeatureIterator pfIter = sf.getPointFeatureIterator(1*1024*1024);
		if (pfIter.hasNext()) {
			PointFeature pf = pfIter.next();
			StructureData sdata = pf.getDataAll();
			StructureMembers smembers = sdata.getStructureMembers();
			List<String> nameList = smembers.getMemberNames();
			return nameList.toArray(new String[nameList.size()]);

			//			String[] varArray = nameList.toArray(new String[nameList.size()]);
			//			for (int n=0; n<varArray.length; n++) {
			//				varArray[n] = varArray[n].concat(" "+smembers.getMember(n).getDescription());
			//			}
			//			
			//			return varArray;
		}
		else {
			throw new WCTException("No variables found for this station");
		}
	}

	


}
