package steve.test.zonalstats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import ucar.nc2.Dimension;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.util.CancelTask;

public class GridDatasetMaskReducer {

	public final static DecimalFormat FMT_0D00 = new DecimalFormat("0.00");

	public static void main(String[] args) {

		//		String gridName = "tmax";
		//    	String infile = "mask2.nc";
		//    	String maskName = "attribute_mask_states_STATE_FIPS";

		try {
			if (args.length != 4 && args.length != 5) {
				System.err.println("Input arguments are: ");
				System.err.println("  Arg1=Input File, Arg2=Grid Variable Name(s) (comma-separated), Arg3=Mask Variable Name, Arg4=Output CSV file");
				System.err.println(" OR: ");
				System.err.println("  Arg1=Data Input File, Arg2=Grid Variable Name(s) (comma-separated), Arg3=Mask Input File, Arg4=Mask Variable Name, Arg5=Output CSV file");
				return;
			}

			if (args.length == 4) {
				reduceGrid(args[0], args[1].split(","), args[0], args[2], args[3]);
			}
			else if (args.length == 5) {
				reduceGrid(args[0], args[1].split(","), args[2], args[3], args[4]);
			}


		} catch (Exception e) {
			e.printStackTrace();
			System.exit(9);
		}
	}

	public static HashMap<Integer, Double> reduceGrid(String dataFile, String[] dataGridNames, String maskFile, String maskGridName, String outFile) 
			throws Exception {

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outFile)));
		

		HashMap<Integer, Double> summaryMap = new HashMap<Integer, Double>();



		// get bounds from NetCDF file
		Formatter fmter = new Formatter();
		CancelTask cancelTask = new CancelTask() {
			@Override
			public boolean isCancel() {
				return false;
			}
			@Override
			public void setError(String arg0) {
			}
			@Override
			public void setProgress(String arg0, int arg1) {
			}
		};
		GridDataset gds = (GridDataset) FeatureDatasetFactoryManager.open(
				ucar.nc2.constants.FeatureType.GRID, maskFile, cancelTask, fmter);

		GridDatatype mask = gds.findGridDatatype(maskGridName);
		System.out.println(gds.getGrids());
		System.out.println(mask);
		int[] maskData = (int[])mask.readVolumeData(0).get1DJavaArray(Integer.class);

		if (! dataFile.equals(maskFile)) {
			gds.close();
			gds = (GridDataset) FeatureDatasetFactoryManager.open(
					ucar.nc2.constants.FeatureType.GRID, dataFile, cancelTask, fmter);
		}


		ArrayList<Dimension> timeDimList = new ArrayList<Dimension>();
		ArrayList<Dimension> zDimList = new ArrayList<Dimension>();
		List<CalendarDate> dateList = null;
		for (String dataGridName : dataGridNames) {

			GridDatatype grid = gds.findGridDatatype(dataGridName);
			GridCoordSystem gcs = grid.getCoordinateSystem();
			if (dateList == null) {
				dateList = gcs.getCalendarDates();
			}

			if (! gcs.isLatLon()) {
				throw new Exception("ERROR: Data grid ["+dataGridName+"] is not lat/lon, only lat/lon gridded data are supported.");
			}

			// check that every grid has same time dimension
			if (grid.getTimeDimension() != null && ! timeDimList.contains(grid.getTimeDimension())) {
				timeDimList.add(grid.getTimeDimension());
			}
			if (timeDimList.size() > 1) {
				throw new Exception("ERROR: Time dimension must be the same for every data grid.  Data grid ["+dataGridName+"] has different time dimension");
			}

			// check that every grid has same time dimension
			if (grid.getZDimension() != null && ! zDimList.contains(grid.getZDimension())) {
				zDimList.add(grid.getZDimension());
			}
			if (zDimList.size() > 1) {
				throw new Exception("ERROR: Z dimension must be the same for every data grid.  Data grid ["+dataGridName+"] has different Z dimension");
			}
		}




		long startMillis = System.currentTimeMillis();


		int tLen = (timeDimList.size() == 1) ? timeDimList.get(0).getLength() : 1;
		int zLen = (zDimList.size() == 1) ? zDimList.get(0).getLength() : 1;


		if (timeDimList.size() == 1 && zDimList.size() == 1) {
			bw.write("date_time, z, mask_value");
		}
		else if (timeDimList.size() == 1) {
			bw.write("date_time, mask_value");        	
		}
		else {
			bw.write("mask_value");
		}
		for (String dataGridName : dataGridNames) {
			bw.write(","+dataGridName+"_sum,"+dataGridName+"_count,"+dataGridName+"_avg");
		}
		bw.write("\n");


		
		// to allow scalable low-memory use, only cache one time and z slice at a time.
		StringBuilder sb = new StringBuilder();
		for (int t=0; t<tLen; t++) {
			for (int z=0; z<zLen; z++) {


				// we store the data sum and count maps for each desired grid variable so we can 'pivot' these out to columns
				ArrayList<HashMap<Integer, Double>> sumMapList = new ArrayList<HashMap<Integer, Double>>();
				ArrayList<HashMap<Integer, Integer>> countMapList = new ArrayList<HashMap<Integer, Integer>>();
				for (String dataGridName : dataGridNames) {

					GridDatatype grid = gds.findGridDatatype(dataGridName);

					double[] data = (double[])grid.readDataSlice(t, z, -1, -1).get1DJavaArray(Double.class);
					HashMap<Integer, Double> sumMap = new HashMap<Integer, Double>();
					HashMap<Integer, Integer> countMap = new HashMap<Integer, Integer>();
					if (data.length != maskData.length) {
						throw new Exception("data and mask grid dimensions are not equal.  "
								+ "data.length: "+data.length+"  maskData.length: "+maskData.length);
					}

					for (int n=0; n<data.length; n++) {
						int maskVal = maskData[n];
						double dataVal = data[n];

						if (! Double.isNaN(dataVal) && ! grid.isMissingData(dataVal)) {
							Integer maskEntry = countMap.get(maskVal);
							int count = maskEntry != null ? maskEntry : 0;
							Double dataEntry = sumMap.get(maskVal);
							double existingData = dataEntry != null ? dataEntry : 0;

							sumMap.put(maskVal, existingData+dataVal);
							countMap.put(maskVal, ++count);
						}
					}

					sumMapList.add(sumMap);
					countMapList.add(countMap);

					//        		System.out.println(dataMap);
					//        		System.out.println(countMap);
					//        		System.out.println(gcs.getTimeAxis1D().getCalendarDates().get(t));


				} // end gridname loop

				String date = (dateList != null) ? dateList.get(t).toString() : null;


				// Loop through lists of ordered maps and print comma-separated data as follows:
				// date_time (if available), mask_value, grid[0]_sum, grid[0]_count, grid[0]_avg, ... grid[n]_sum, grid[n]_count, grid[n]_avg
				sb.setLength(0);
				Set<Integer> keys = new TreeMap<Integer, Integer>(countMapList.get(0)).keySet();
				Iterator<Integer> keyIter = keys.iterator();
				while (keyIter.hasNext()) {

					int key = keyIter.next();
					if (date != null) {
						sb.append(date).append(",").append(key).append(",");
					}
					else {
						sb.append(key).append(",");
					}
					for (int n=0; n<dataGridNames.length; n++) {	
						double sum = sumMapList.get(n).get(key);
						int count = countMapList.get(n).get(key);

						sb.append(FMT_0D00.format(sum)).append(",").append(count).append(",");
						sb.append(FMT_0D00.format(sum/count));
					}		
					sb.append("\n");		
				}
				bw.write(sb.toString());
			}

			long elapsedMillis = System.currentTimeMillis()-startMillis;
			System.out.println("Took "+elapsedMillis + " ms");
		}

		gds.close();
		bw.close();

		return summaryMap;

	}
}