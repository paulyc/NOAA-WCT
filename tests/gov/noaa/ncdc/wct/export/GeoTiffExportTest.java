package gov.noaa.ncdc.wct.export;

import java.io.File;

import ucar.ma2.Array;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.geotiff.GeoTiffWriter2;
import ucar.nc2.geotiff.GeotiffWriter;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

public class GeoTiffExportTest {

	public static void main(String[] args) {
		try {
			
			File f = new File("C:\\work\\wct-tests\\gallo\\AVHRR-Land_v004_AVH13C1_NOAA-18_20131231_c20140415040725.nc");
			File outfile1 = new File("C:\\work\\wct-tests\\gallo\\AVHRR-Land_v004_AVH13C1_NOAA-18_20131231_c20140415040725.test11.tif");
			File outfile2 = new File("C:\\work\\wct-tests\\gallo\\AVHRR-Land_v004_AVH13C1_NOAA-18_20131231_c20140415040725.test2.tif");
			String varName = "NDVI";
			LatLonRect llr = new LatLonRect(new LatLonPointImpl(-90.000,-180.000), new LatLonPointImpl(90.00, 180.00));

			
			GridDataset gds = GridDataset.open(f.toString());
			GridDatatype grid = gds.findGridByName("NDVI");
			GeotiffWriter writer1 = new GeotiffWriter(outfile1.toString());
			Array data = grid.readDataSlice(0, 0, -1, -1);
			System.out.println(grid.getMinMaxSkipMissingData(data));
		    writer1.writeGrid(gds, grid, data, false); 
		    writer1.close();
		        
			
	        GeoTiffWriter2 writer2 = new GeoTiffWriter2(outfile2.toString());
	        writer2.writeGrid(f.toString(), varName, 0, 0, false, llr); 
	        writer2.close();
	        
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
