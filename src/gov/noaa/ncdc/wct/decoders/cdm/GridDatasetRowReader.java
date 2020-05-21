package gov.noaa.ncdc.wct.decoders.cdm;

import java.util.ArrayList;
import java.util.HashMap;

import ucar.ma2.Range;
import ucar.nc2.dt.GridDatatype;

public class GridDatasetRowReader {
	

	private int lastRowRead = -1;
	private double[] rowData = null;	

	private HashMap<Integer, double[]> rowDataMap = new HashMap<Integer, double[]>();
	private ArrayList<Integer> rowCacheOrder = new ArrayList<Integer>();	

	private int numFileReads = 0;
	private int numCacheReads = 0;
	

	
	/**
	 * Number of rows per chunk
	 */
	public static final int ROW_CHUNK_SIZE = 1000;
	/**
	 * Number of row chunks to cache
	 */
	public static final int ROW_CACHE_SIZE = 3;
	
	
	
	/**
	 * Clears row cache HashMap objects and resets file and cache counters.
	 */
	public void clearAndResetCache() {
		rowDataMap.clear();
		rowCacheOrder.clear();
		numFileReads = 0;
		numCacheReads = 0;
		lastRowRead = -1;
	}
	

	/**
	 * Read a chunk of rows of data from disk.  Cache the rows to a HashMap.  If the number
	 * of cached rows exceeds the ROW_CACHE_SIZE value, remove the oldest row from
	 * cache.
	 * @param row
	 * @return
	 * @throws Exception 
	 */
	public double[] getRowDataChunk(GridDatatype grid, int timeIndex, int runtimeIndex, int zIndex, 
			int row, int rowChunkSize) throws Exception {


		int imgWidth = grid.getXDimension().getLength();
		int imgHeight = grid.getYDimension().getLength();
		
		int rowChunkIndex = row / rowChunkSize;

		int minrow = rowChunkIndex * rowChunkSize;
		int maxrow = ((rowChunkIndex+1) * rowChunkSize) - 1;
		maxrow = (maxrow > imgHeight) ? imgHeight-1 : maxrow;
		
		
		lastRowRead = row;

		// is chunk cached?
		if (! rowDataMap.containsKey(rowChunkIndex)) {
			

            Range rtRange = (runtimeIndex >= 0) ? new Range(runtimeIndex, runtimeIndex) : new Range(0, 0);
            Range timeRange = (timeIndex >= 0) ? new Range(timeIndex, timeIndex) : new Range(0, 0);
            Range zRange = (zIndex >= 0) ? new Range(zIndex, zIndex) : new Range(0, 0);
            
            if (grid.getRunTimeDimension() == null) {
            	rtRange = null;
            }
            if (grid.getTimeDimension() == null) {
            	timeRange = null;
            }
            if (grid.getZDimension() == null) {
            	zRange = null;
            }
            

//			System.out.println("reading from disk: "+minrow + " to "+maxrow+ " chunkIndex="+rowChunkIndex);
//			System.out.println("timeIndex="+timeIndex+"  zIndex="+zIndex);
					
			GridDatatype subsetGrid = grid.makeSubset(
					rtRange, // runtime 
					null, // e
					timeRange, 
					zRange, 
					new Range(minrow, maxrow), 
					null // x
					);

//			System.out.println(subsetGrid);

			rowData = (double[]) subsetGrid.readDataSlice(-1, -1, -1, -1).get1DJavaArray(Double.class);
			int rowsRead = maxrow-minrow+1;
			
			
			if (rowData.length != rowsRead*imgWidth) {
//				System.out.println("read rows "+minrow+" to "+maxrow+"   rowChunkSize="+rowChunkSize);
				
				throw new Exception("Data array size read from disk ( "+rowData.length+" ) doesn't match expected: "+rowChunkSize*imgWidth);
			}

				
			if (! rowDataMap.containsKey(rowChunkIndex)) {
				
				rowDataMap.put(rowChunkIndex, rowData);
				rowCacheOrder.add(rowChunkIndex);

				//  clear oldest if needed
				if (rowDataMap.size() > ROW_CACHE_SIZE) {
//					logger.fine("CACHE SIZE REACHED, REMOVING ROW CHUNK: "+rowCacheOrder.get(0));
					System.out.println("CACHE SIZE REACHED, REMOVING ROW CHUNK: "+rowCacheOrder.get(0));
					rowDataMap.remove(rowCacheOrder.get(0));
					rowCacheOrder.remove(0);
				}
				
			}

			numFileReads++;
			
		}
		else {
			
//			System.out.println("reading from cache: "+row);						
			numCacheReads++;
			rowData = (double[])rowDataMap.get(new Integer(rowChunkIndex));
		}
		
		return rowData;
	}
   
	
	
}
