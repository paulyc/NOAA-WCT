package gov.noaa.ncdc.wct.decoders.cdm;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import org.geotools.feature.Feature;

import gov.noaa.ncdc.wct.WCTUtils;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.point.standard.StandardPointCollectionImpl;

public class PointCollectionProcessorTests {


	public static void main(String[] args) {
		try {

			Formatter fmter = new Formatter();
//			URL dataURL = new URL("file:/C:/work/wct-tests/glm/OR_GLM-L2-LCFA_G16_s20191620006000_e20191620006200_c20191620006226.nc");
			URL dataURL = new URL("file:/C:/work/wct-tests/icoads-netcdf/ICOADS_R3.0.0_1947-02.nc");
			FeatureDataset fd = FeatureDatasetFactoryManager.open(null, dataURL.toString(), WCTUtils.getSharedCancelTask(), fmter);
			if (fd != null && fd.getFeatureType().isPointFeatureType()) {
				System.out.println("File is a point feature type...");
			}
			else {
				System.out.println(fmter.toString());
				throw new IOException("Data does not appear to be of CDM type 'FeatureDatasetPoint' - "+fmter.toString());
			}
			

//			System.out.println(((FeatureDatasetPoint)fd).getDataVariables());
			
			
			
			List<ucar.nc2.ft.FeatureCollection> pfcList = ((FeatureDatasetPoint)fd).getPointFeatureCollectionList();
			System.out.println("pfcList.size(): "+pfcList.size());
			
			PointCollectionDecoder pcp = new PointCollectionDecoder();
			pcp.setStandardPointCollection((StandardPointCollectionImpl)(pfcList.get(0)));
			ArrayList<Feature> featureList = pcp.getDecodedFeatureList();
			
			System.out.println("created "+featureList.size()+" features!");
			
			System.out.println("date range min/max: "+pcp.getMinDate()+" / "+pcp.getMaxDate());
			System.out.println("att map min/max: "+pcp.getAttributeMinMaxMap());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
