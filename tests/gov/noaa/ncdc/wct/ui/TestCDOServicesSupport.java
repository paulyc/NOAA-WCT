package gov.noaa.ncdc.wct.ui;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SchemaException;
import org.json.simple.parser.ParseException;
import org.junit.Test;

public class TestCDOServicesSupport {

	@Test
	public void testGetStationFeatures() throws IOException, ParseException, java.text.ParseException, FactoryConfigurationError, SchemaException {
		CDOServicesSupport cdo = new CDOServicesSupport();
		
		// https://www.ncdc.noaa.gov/cdo-web/api/v2/stations?extent=47.0204,-122.3047,47.9139,-122.0065&startdate=2012-03-01&enddate=2013-03-01&limit=300
		
		double minX = -122.3047;
		double minY = 47.0204;
		double maxX = -122.0065;
		double maxY = 47.2139;
		FeatureCollection fc = FeatureCollections.newCollection();
		Rectangle2D extent = new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		cdo.getStationFeatures(fc, extent, sdf.parse("2013-03-01"));
		
		
	}
}
