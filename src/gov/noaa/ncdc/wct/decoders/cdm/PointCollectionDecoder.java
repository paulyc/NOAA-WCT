package gov.noaa.ncdc.wct.decoders.cdm;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;

import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.StreamingDecoder;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.StreamingProcessException;
import ucar.ma2.MAMath.MinMax;
import ucar.ma2.StructureData;
import ucar.ma2.StructureMembers.Member;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.PointFeatureIterator;
import ucar.nc2.ft.point.standard.StandardPointCollectionImpl;
import ucar.nc2.time.CalendarDate;

/**
 * Decoder for the most basic point collection (like lightning strikes).  
 * @author Steve.Ansari
 *
 */
public class PointCollectionDecoder implements StreamingDecoder {


	private GeometryFactory geoFactory = new GeometryFactory();
	private CalendarDate minDate = null;
	private CalendarDate maxDate = null;
	private HashMap<String, MinMax> attributeMinMaxMap = new HashMap<String, MinMax>();
	
	private FeatureType schema = null;
	private StandardPointCollectionImpl spci = null;
	
	
	@Override
	public FeatureType[] getFeatureTypes() {
		return new FeatureType[] { schema };
	}

	@Override
	public void decodeData(StreamingProcess[] streamingProcessArray) throws DecodeException, IOException {
		
		if (spci == null) {
			throw new DecodeException("The StandardPointCollectionImpl is null.  Use setStandardPointCollection(...) before starting decode.");
		}
		
		PointFeatureIterator pfi = spci.getPointFeatureIterator(1024*1000);
		
		System.out.println("count: "+pfi.getCount());

		
		
		int geoIndex = 0;
		while (pfi.hasNext()) {
			PointFeature pf = pfi.next();
			processDateMinMax(pf.getObservationTimeAsCalendarDate());

			
			try {
			
				if (schema == null) {
					schema = buildSchema(pf.getFeatureData());
				}


				Geometry geom = geoFactory.createPoint(
						new Coordinate(pf.getLocation().getLongitude(), pf.getLocation().getLatitude())
						);

				Object[] atts = buildAttributes(geom, pf.getFeatureData());

				Feature feature = schema.create(atts, new Integer(geoIndex++).toString());

				//			System.out.println(feature);

				for (StreamingProcess streamingProcess : streamingProcessArray) {
					streamingProcess.addFeature(feature);
				}

				//			System.out.println("location: "+pf.getLocation().getLatLon().toString());
				//			System.out.println(pf.getFeatureData().getMembers().toString());
				//			if (geoIndex++ > 10) break;

			
			} catch (StreamingProcessException | IllegalAttributeException | FactoryConfigurationError | SchemaException e) {
				throw new DecodeException(e.getMessage());
			}
			
		}
	

	}
	
	
	
	
	
	public ArrayList<Feature> getDecodedFeatureList() throws IOException, IllegalAttributeException, FactoryConfigurationError, SchemaException, DecodeException {

		
		final ArrayList<Feature> featureList = new ArrayList<Feature>(10000);
		featureList.ensureCapacity(10000);

		
		System.out.println(spci.getBoundingBox());
		System.out.println(spci.getNobs());
		
		StreamingProcess featureListProcess = new StreamingProcess() {
			@Override
			public void addFeature(Feature feature) throws StreamingProcessException {
				featureList.add(feature);
			}
			@Override
			public void close() throws StreamingProcessException {
			}
		};
		
		decodeData(new StreamingProcess[] { featureListProcess });

		
		featureList.trimToSize();
		System.out.println("Returning list size: "+featureList.size());
		
		return featureList;
	}
	
	
	
	private Object[] buildAttributes(Geometry geom, StructureData structureData) {
		List<Member> members = structureData.getStructureMembers().getMembers();
		Object[] atts = new Object[members.size()+1];
		atts[0] = geom;
		for (int n=0; n<members.size(); n++) {
			atts[n+1] = structureData.getScalarObject(members.get(n));
			
			if (members.get(n).getDataType().isNumeric()) {
				processAttributeMinMax(members.get(n).getName(), new Double(structureData.getScalarObject(members.get(n)).toString()).doubleValue());
			}
		}
		return atts;
	}

	
	private void processAttributeMinMax(String attName, double val) {
		MinMax attMinMax = attributeMinMaxMap.get(attName);
		if (attMinMax == null) {
			attributeMinMaxMap.put(attName, new MinMax(val, val));
		}
		else {
			MinMax minMax = attributeMinMaxMap.get(attName);
			minMax.min = (val < minMax.min) ? val : minMax.min;
			minMax.max = (val > minMax.max) ? val : minMax.max;
		}
	}
	private void processDateMinMax(CalendarDate calDate) {
		if (this.minDate == null || this.maxDate == null) {
			this.minDate = calDate;
			this.maxDate = calDate;
		}
		else {
			if (calDate.isBefore(minDate)) minDate = calDate;
			if (calDate.isAfter(maxDate)) maxDate = calDate;
		}
	}

	private FeatureType buildSchema(StructureData structureData) throws FactoryConfigurationError, SchemaException {

		List<Member> members = structureData.getStructureMembers().getMembers();
		AttributeType[] attArray = new AttributeType[members.size()+1];
		
		attArray[0] = AttributeTypeFactory.newAttributeType("geom", Geometry.class);
		for (int n=0; n<members.size(); n++) {
//			attArray[n+1] = AttributeTypeFactory.newAttributeType(
//					members.get(n).getName(), members.get(n).getDataType().getClassType());
			

			attArray[n+1] = AttributeTypeFactory.newAttributeType(
					members.get(n).getName(), 	
					structureData.getScalarObject(members.get(n)).getClass());
			
			System.out.println("Building attribute: "+attArray[n+1]);
		}
//			AttributeTypeFactory.newAttributeType("geom", Geometry.class),
//			AttributeTypeFactory.newAttributeType("value", Double.class),
	
		return FeatureTypeFactory.newFeatureType(attArray, "Point Attributes");

	}

	
	
	public CalendarDate getMinDate() {
		return minDate;
	}	
	public CalendarDate getMaxDate() {
		return maxDate;
	}
	
	/**
	 * Entries are not created for attributes that are not numeric
	 * @return
	 */
	public HashMap<String, MinMax> getAttributeMinMaxMap() {
		return attributeMinMaxMap;
	}

	public StandardPointCollectionImpl getStandardPointCollection() {
		return spci;
	}

	/**
	 * Also resets FeatureType to null
	 * @param spci
	 */
	public void setStandardPointCollection(StandardPointCollectionImpl spci) {
		this.spci = spci;
		this.schema = null;
	}


}
