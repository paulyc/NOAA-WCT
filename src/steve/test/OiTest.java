package steve.test;

import java.io.IOException;

import gov.noaa.ncdc.wct.WCTUtils;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;

public class OiTest {

	public static void main(String[] args) {
		
		try {
			NetcdfDataset ncd = NetcdfDataset.acquireDataset("C:/work/wct/oi-test/avhrr-only-v2.20170409_preliminary.nc", WCTUtils.getSharedCancelTask());
			
//			System.out.println( ncd.toString() );
			
			String coordList = CoordinateSystem.makeName(ncd.getCoordinateAxes());
			System.out.println(coordList);
			
//			System.out.println(ncd.getCoordinateAxes());
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
