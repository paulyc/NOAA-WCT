package gov.noaa.ncdc.wct.decoders.nexrad;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import gov.noaa.ncdc.wct.decoders.DecodeException;

public class DecodeMeltingLayerTest {

	@Test
	public void testDecode1() throws DecodeException, IOException {
		DecodeL3Header header = new DecodeL3Header();
//		header.decodeHeader(new File("C:\\work\\melting-layer\\Level3_GSP_N0M_20150318_0340.nids").toURI().toURL());
		header.decodeHeader(new URL("file:/C:/work/melting-layer/example-2013/data/KLWX_SDUS81_N0MLWX_201311261434"));
		

		DecodeMeltingLayer decoder = new DecodeMeltingLayer(header);
		decoder.decodeData();
		
		
		
		
	}
	
}
