package gov.noaa.ncdc.wct.decoders;

import org.geotools.cv.SampleDimension;

import gov.noaa.ncdc.wct.WCTException;

public class SampleDimensionAndLabels {

	private SampleDimension sampleDimension;
	private String[] labels;
	private String units;

	
	public SampleDimensionAndLabels(SampleDimension sampleDimension, String[] labels, String units) throws WCTException {
		this.sampleDimension = sampleDimension;
		this.labels = labels;
		this.units = units;
		
//		if (sampleDimension.getCategories().size() != labels.length-1) {
//			throw new WCTException("The number of catgories in the sample dimension must" +
//					" be one less than the number of labels in the label array");
//		}
	}
	
	
	public SampleDimension getSampleDimension() {
		return sampleDimension;
	}
	public String[] getLabels() {
		return labels;
	}
	public String getUnits() {
		return units;
	}
}
