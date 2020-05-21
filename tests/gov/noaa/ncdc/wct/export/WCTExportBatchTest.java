package gov.noaa.ncdc.wct.export;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class WCTExportBatchTest {
	
//	public final static String TEST_OUTPUT_DIR = "test-export-output";
//	public final static String TEST_CONFIG_LOCATION = "build"+File.separator+"helphtml"+File.separator+"wctBatchConfig.xml";
	

	
	@Before
	public void init() {
//		File testdir = new File(TEST_OUTPUT_DIR);
//		testdir.mkdir();

	}
	
	@After
	public void teardown() throws IOException {
//		FileUtils.deleteDirectory(new File(TEST_OUTPUT_DIR));
	}

	
	@Test
	public void testExport_cappi() {

		File file = new File("C:\\work\\wct\\batch\\testdata\\KDMX20180719_233927_V06");
		File configfile = new File("C:\\work\\wct\\batch\\testconfigs\\wctBatchConfig-CAPPI.xml");
		File outdir = new File("C:\\work\\wct\\batch\\testoutput");
		outdir.mkdir();
				
		
		WCTExportBatch.main( new String[] {
				file.toString(),
				outdir.toString()+File.separator+file.getName()+"_cappi",
				"nc",
				configfile.toString()
		} );
			
	}
	
	@Test
	public void testExport_multicappi() {

		File file = new File("C:\\work\\wct\\batch\\testdata\\KDMX20180719_233927_V06");
		File configfile = new File("C:\\work\\wct\\batch\\testconfigs\\wctBatchConfig-multiCAPPI.xml");
		File outdir = new File("C:\\work\\wct\\batch\\testoutput");
		outdir.mkdir();
				
		
		WCTExportBatch.main( new String[] {
				file.toString(),
				outdir.toString()+File.separator+file.getName()+"_multicappi",
				"nc",
				configfile.toString()
		} );
			
	}
		
	@Test
	public void testExport_geojson() {

		File file = new File("C:\\work\\wct\\batch\\testdata\\KDMX20180719_233927_V06");
		File configfile = new File("C:\\work\\wct\\batch\\testconfigs\\wctBatchConfig-geojson.xml");
		File outdir = new File("C:\\work\\wct\\batch\\testoutput");
		outdir.mkdir();
				
		
		WCTExportBatch.main( new String[] {
				file.toString(),
				outdir.toString()+File.separator+file.getName()+"_geojson",
				"json",
				configfile.toString()
		} );
			
	}
		
		
		
	
	
	
}
