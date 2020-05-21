package gov.noaa.ncdc.wct.io;

public enum SupportedDataType { 
	UNKNOWN,
	RADIAL,
	GRIDDED,
	NEXRAD_LEVEL3,
	NEXRAD_LEVEL3_NWS,
	NEXRAD_XMRG,
	GOES_SATELLITE_AREA_FORMAT,
	
	POINT_TIMESERIES,
	
	SHAPEFILE,
	IMAGEFILE,
	TEXTFILE,
	ARCHIVE
}