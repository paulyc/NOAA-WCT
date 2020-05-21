package gov.noaa.ncdc.wct.export.raster;

import java.awt.geom.Rectangle2D.Double;
import java.awt.image.WritableRaster;

public class WCTRasterCopy implements WCTRaster {

	private WritableRaster writableRaster;
	private Double bounds;
	private double cellWidth;
	private double cellHeight;
	private double noDataValue;
	private boolean isNative;
	private boolean isEmptyGrid;
	private String units;
	private String longName;
	private long dateInMilliseconds;
	private String variableName;
	private String standardName;
	
	
	
	public static WCTRasterCopy getInstance(WCTRaster raster) {
		return new WCTRasterCopy(raster.getWritableRaster(), 
				raster.getBounds(), raster.getCellWidth(), raster.getCellHeight(), 
				raster.getNoDataValue(), raster.isNative(), raster.isEmptyGrid(), 
				raster.getUnits(), raster.getLongName(), raster.getDateInMilliseconds(), 
				raster.getVariableName(), raster.getStandardName());
	}
	
	
	
	

	public WCTRasterCopy(WritableRaster writableRaster, Double bounds, double cellWidth, double cellHeight,
			double noDataValue, boolean isNative, boolean isEmptyGrid, String units, String longName,
			long dateInMilliseconds, String variableName, String standardName) {

		super();
		this.writableRaster = writableRaster;
		this.bounds = bounds;
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
		this.noDataValue = noDataValue;
		this.isNative = isNative;
		this.isEmptyGrid = isEmptyGrid;
		this.units = units;
		this.longName = longName;
		this.dateInMilliseconds = dateInMilliseconds;
		this.variableName = variableName;
		this.standardName = standardName;
	}

	
	
	@Override
	public WritableRaster getWritableRaster() {
		return this.writableRaster;
	}

	@Override
	public void setWritableRaster(WritableRaster writableRaster) {
		this.writableRaster = writableRaster;
	}

	@Override
	public Double getBounds() {
		return bounds;
	}

	@Override
	public double getCellWidth() {
		return cellWidth;
	}

	@Override
	public double getCellHeight() {
		return cellHeight;
	}

	@Override
	public double getNoDataValue() {
		return noDataValue;
	}

	@Override
	public boolean isNative() {
		return isNative;
	}

	@Override
	public boolean isEmptyGrid() {
		return isEmptyGrid;
	}

	@Override
	public String getUnits() {
		return units;
	}

	@Override
	public void setUnits(String units) {
		this.units = units;
	}

	@Override
	public String getLongName() {
		return longName;
	}

	@Override
	public void setLongName(String longName) {
		this.longName = longName;
	}

	@Override
	public long getDateInMilliseconds() {
		return this.dateInMilliseconds;
	}

	@Override
	public void setDateInMilliseconds(long dateInMillis) {
		this.dateInMilliseconds = dateInMillis;		
	}

	@Override
	public String getVariableName() {
		return this.variableName;
	}

	@Override
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	@Override
	public String getStandardName() {
		return this.standardName;
	}

	@Override
	public void setStandardName(String standardName) {
		this.standardName = standardName;		
	}

}
