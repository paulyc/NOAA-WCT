package gov.noaa.ncdc.wct.decoders.cdm.conv;

import java.io.IOException;
import java.net.URL;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.Feature;

import com.vividsolutions.jts.geom.Point;

import gov.noaa.ncdc.common.FeatureCache;
import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.CF;
import ucar.nc2.constants._Coordinate;
import ucar.nc2.dataset.CoordSysBuilder;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.util.CancelTask;

public class ClimDivConvention extends CoordSysBuilder {

	  public ClimDivConvention() {
		  
		    this.conventionName = "ClimDiv-1.0";
	  }
	
//    /**
//     *
//     * @param ncfile test this NetcdfFile
//     * @return true if we think this is a ... file.
//     */
//    public static boolean isMine(NetcdfFile ncfile) {
//        
//        System.out.println("in 'isMine' for "+ncfile.getLocation());
//        
//        //String cs = ncfile.findAttValueIgnoreCase(null, "Conventions", null);
//        //if (cs != null) return false;
//
//        // TODO
//        // Change this to find a one-dimensional variable that has a standard_name of 'division ID'
//        if (ncfile.getDimensions().size() == 2 &&
//        		ncfile.findDimension("time") != null && 
//        		ncfile.findDimension("division") != null) {
//        	
//        	return true;
//        }
//        
//
////        System.out.println("isMine? returning false...");
//        return false;
//    }
//    

    @Override
    public void augmentDataset(NetcdfDataset ncDataset, CancelTask cancelTask) throws IOException {

        System.out.println("Using CoordSysBuilder: "+this.getClass());
        
        
        // 1. Load shapefile of climate divisions to use to look up 
        //    geometries of divisions.  Polygons will be added as well-known text in
        //    a variable attribute.  The centroid of the division will be used as the
        //    'station' location, to be used in the time-series CF structure.

        URL mapDataURL = new URL(WCTConstants.MAP_DATA_JAR_URL);
		URL url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/climate-divisions.shp", null);
		ShapefileDataStore ds = new ShapefileDataStore(url);
//		FeatureCollection features = ds.getFeatureSource("climate-divisions").getFeatures().collection();
		FeatureCache featureCache = new FeatureCache(ds.getFeatureSource("climate-divisions"), "DIVISION_I");
		int maxDescStringLen = 0;
		
		if (ncDataset.getRootGroup().getGroups().size() > 0) {
		
			for (Group group : ncDataset.getRootGroup().getGroups()) {
				processGroup(ncDataset, group, featureCache);			
			}
			ncDataset.addAttribute(null, new Attribute("featureType", "timeSeries"));
//			 we set the conventions back to CF-1.6, since we've 'fixed' it now and need to trigger the CDM read of it as point features.
			ncDataset.addAttribute(null, new Attribute("Conventions", "CF-1.6"));

			return;
		}
	
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		Variable divVar = ncDataset.findVariable("division");
		divVar.addAttribute(new Attribute("standard_name", "platform_id"));
		divVar.addAttribute(new Attribute("long_name", "division 4-digit IDs"));
		divVar.addAttribute(new Attribute("cf_role", "timeseries_id"));
		Array divisionArray = divVar.read();

		int numLocations = divisionArray.getShape()[0];
		ArrayFloat.D1 latArray=(ArrayFloat.D1)Array.factory(DataType.FLOAT, new int[]{ divVar.getShape(0) });
		ArrayFloat.D1 lonArray=(ArrayFloat.D1)Array.factory(DataType.FLOAT, new int[]{ divVar.getShape(0) });
		String[] infoArray = new String[numLocations];
		for (int n=0; n<divisionArray.getShape()[0]; n++) {
			int divId = divisionArray.getInt(n);
			Feature divFeature = featureCache.getFeature(new Integer(divId));
			if (divFeature == null) {
				System.err.println("No feature lookup found for ID="+divId);
			}
			else {
				Point centroid = divFeature.getDefaultGeometry().getCentroid();					
				latArray.set(n, (float)centroid.getY());
				lonArray.set(n, (float)centroid.getX());
				
				String desc = divFeature.getAttribute("ST") + " - " + divFeature.getAttribute("NAME");
				infoArray[n] = desc;
				maxDescStringLen = Math.max(maxDescStringLen, desc.length());
			}
		}
		


		ncDataset.addDimension(null, new Dimension("desc_strlen", maxDescStringLen, true));
		
		Variable locationInfoVar = new Variable(ncDataset, null, null, "division_info");
		locationInfoVar.setDimensions("division desc_strlen");
		locationInfoVar.setDataType(DataType.CHAR);
		locationInfoVar.addAttribute( new Attribute("long_name", "The division description"));
		locationInfoVar.addAttribute( new Attribute("standard_name", "platform_name"));

		ArrayChar infoCharArray = new ArrayChar(new int[]{numLocations, maxDescStringLen});
		Index infoIdx = infoCharArray.getIndex();
		for (int locationIndex=0; locationIndex<numLocations; locationIndex++) {
			if (infoArray[locationIndex] != null) {
				char[] ca = infoArray[locationIndex].toCharArray();
				for (int i=0; i<ca.length; i++) { 
					//							System.out.println(infoIdx.toString()+" "+locationIndex+","+i+" max:"+maxInfoStrLength+" ");
					infoCharArray.setChar(infoIdx.set(locationIndex, i), ca[i]); 
				}
			}
		}
		locationInfoVar.setCachedData(infoCharArray, false);
		ncDataset.addVariable(null, new VariableDS(null, locationInfoVar, true));
		
		
		
		// set time unlimited to false 
		// NOT SURE IF NEEDED
//		for (CoordinateAxis coordAxis : ncDataset.getCoordinateAxes()) {
//			System.out.println("CHECKING COORD AXIS: "+coordAxis.getAxisType().toString());
//			if (coordAxis.getAxisType() == AxisType.Time) {
//				System.out.println("PROCESSING TIME COORD");
//				coordAxis.getDimension(0).setUnlimited(false);
//				ncDataset.findDimension(coordAxis.getDimension(0).getShortName()).setUnlimited(false);
//		        coordAxis.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Time.name()));
//		        coordAxis.addAttribute(new Attribute(_Coordinate.Axes, coordAxis.getFullName()));
//				ncDataset.addCoordinateAxis(coordAxis);
//			}
//		}
		
		ncDataset.findDimension("time").setUnlimited(false);
		
		Variable timeVar = ncDataset.findVariable("time");
        timeVar.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Time.toString()));
//        timeVar.addAttribute(new Attribute(_Coordinate.Axes, "time"));
        
		
		CoordinateAxis1D latVar = new CoordinateAxis1D(ncDataset, null, "lat", DataType.FLOAT, "division", "degrees_north", "Division centroid latitude value");
        latVar.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Lat.toString()));
//        latVar.addAttribute(new Attribute(_Coordinate.Axes, latVar.getFullName()));
        latVar.setCaching(true);
		latVar.setCachedData(latArray, false);
		ncDataset.addCoordinateAxis(latVar);

		CoordinateAxis1D lonVar = new CoordinateAxis1D(ncDataset, null, "lon", DataType.FLOAT, "division", "degrees_east", "Division centroid longitude value");
        lonVar.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Lon.toString()));
//        lonVar.addAttribute(new Attribute(_Coordinate.Axes, lonVar.getFullName()));
        lonVar.setCaching(true);
		lonVar.setCachedData(lonArray, false);
		ncDataset.addCoordinateAxis(lonVar);

		
		for (Variable var : ncDataset.getVariables()) {
//			System.out.println("checking: "+var);
			if (var.getShape().length == 2 && (
					(var.getDimension(0).getShortName().equals("division") && var.getDimension(1).getShortName().equals("time")) ||
					(var.getDimension(0).getShortName().equals("time") && var.getDimension(1).getShortName().equals("division"))
				)) {
				
				System.out.println("adding lookup to variable: "+var);
				var.addAttribute(new Attribute("coordinates", "time lat lon"));
				var.addAttribute(new Attribute("_CoordinateAxes", "time lat lon"));
				var.addAttribute(new Attribute("shape_lookup", "climate-divisions.shp"));
				var.addAttribute(new Attribute("shape_lookup_id_attribute", "ID"));
			}
		}
		
		ncDataset.addAttribute(null, new Attribute("featureType", "timeSeries"));
//		 we set the conventions back to CF-1.6, since we've 'fixed' it now and need to trigger the CDM read of it as point features.
		ncDataset.addAttribute(null, new Attribute("Conventions", "CF-1.6"));

//        ncDataset.finish();
//        System.out.println("-------------------------");
//        System.out.println(ncDataset.toString());
//        System.out.println("-------------------------");
//        ncDataset.debugDump(new PrintWriter(System.out), ncDataset);
//        System.out.println("-------------------------");

//        super.augmentDataset(ncDataset, cancelTask);
//        this.buildCoordinateSystems(ncDataset);
        
//        CF1Convention conv = new CF1Convention();
//        conv.augmentDataset(ncDataset, cancelTask);
        
        ncDataset.finish();
    }
    
    
    /**
     * The attribute "coordinates" is an alias for _CoordinateAxes.
     */
    protected void findCoordinateAxes(NetcdfDataset ds) {

      // coordinates is an alias for _CoordinateAxes
      for (VarProcess vp : varList) {
        if (vp.coordAxes == null) { // dont override if already set
          String coordsString = ds.findAttValueIgnoreCase(vp.v, CF.COORDINATES, null);
          if (coordsString != null) {
            vp.coordinates = coordsString;
          }
        }
      }

      super.findCoordinateAxes(ds);
    }
    
    
    private void processGroup(NetcdfDataset ncDataset, Group group, FeatureCache featureCache) throws IOException {
		
		int maxDescStringLen = -1;
		
		Variable divVar = ncDataset.findVariable(group, "division");
		divVar.addAttribute(new Attribute("standard_name", "platform_id"));
		divVar.addAttribute(new Attribute("long_name", "division 4-digit IDs"));
		divVar.addAttribute(new Attribute("cf_role", "timeseries_id"));
		Array divisionArray = divVar.read();

		int numLocations = divisionArray.getShape()[0];
		ArrayFloat.D1 latArray=(ArrayFloat.D1)Array.factory(DataType.FLOAT, new int[]{ divVar.getShape(0) });
		ArrayFloat.D1 lonArray=(ArrayFloat.D1)Array.factory(DataType.FLOAT, new int[]{ divVar.getShape(0) });
		String[] infoArray = new String[numLocations];
		for (int n=0; n<divisionArray.getShape()[0]; n++) {
			int divId = divisionArray.getInt(n);
			Feature divFeature = featureCache.getFeature(new Integer(divId));
			if (divFeature == null) {
				System.err.println("No feature lookup found for ID="+divId);
			}
			else {
				Point centroid = divFeature.getDefaultGeometry().getCentroid();					
				latArray.set(n, (float)centroid.getY());
				lonArray.set(n, (float)centroid.getX());
				
				String desc = divFeature.getAttribute("ST") + " - " + divFeature.getAttribute("NAME");
				infoArray[n] = desc;
				maxDescStringLen = Math.max(maxDescStringLen, desc.length());
			}
		}
		


		ncDataset.addDimension(group, new Dimension("desc_strlen", maxDescStringLen, true));
		
		Variable locationInfoVar = new Variable(ncDataset, group, null, "division_info");
		locationInfoVar.setDimensions("division desc_strlen");
		locationInfoVar.setDataType(DataType.CHAR);
		locationInfoVar.addAttribute( new Attribute("long_name", "The division description"));
		locationInfoVar.addAttribute( new Attribute("standard_name", "platform_name"));

		ArrayChar infoCharArray = new ArrayChar(new int[]{numLocations, maxDescStringLen});
		Index infoIdx = infoCharArray.getIndex();
		for (int locationIndex=0; locationIndex<numLocations; locationIndex++) {
			if (infoArray[locationIndex] != null) {
				char[] ca = infoArray[locationIndex].toCharArray();
				for (int i=0; i<ca.length; i++) { 
					//							System.out.println(infoIdx.toString()+" "+locationIndex+","+i+" max:"+maxInfoStrLength+" ");
					infoCharArray.setChar(infoIdx.set(locationIndex, i), ca[i]); 
				}
			}
		}
		locationInfoVar.setCachedData(infoCharArray, false);
		ncDataset.addVariable(group, new VariableDS(null, locationInfoVar, true));
		
		
		
		// set time unlimited to false 
		// NOT SURE IF NEEDED
//		for (CoordinateAxis coordAxis : ncDataset.getCoordinateAxes()) {
//			System.out.println("CHECKING COORD AXIS: "+coordAxis.getAxisType().toString());
//			if (coordAxis.getAxisType() == AxisType.Time) {
//				System.out.println("PROCESSING TIME COORD");
//				coordAxis.getDimension(0).setUnlimited(false);
//				ncDataset.findDimension(coordAxis.getDimension(0).getShortName()).setUnlimited(false);
//		        coordAxis.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Time.name()));
//		        coordAxis.addAttribute(new Attribute(_Coordinate.Axes, coordAxis.getFullName()));
//				ncDataset.addCoordinateAxis(coordAxis);
//			}
//		}
		
		group.findDimension("time").setUnlimited(false);
		
		Variable timeVar = ncDataset.findVariable(group, "time");
        timeVar.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Time.toString()));
//        timeVar.addAttribute(new Attribute(_Coordinate.Axes, "time"));
        
		
		CoordinateAxis1D latVar = new CoordinateAxis1D(ncDataset, group, "lat", DataType.FLOAT, "division", "degrees_north", "Division centroid latitude value");
        latVar.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Lat.toString()));
//        latVar.addAttribute(new Attribute(_Coordinate.Axes, latVar.getFullName()));
        latVar.setCaching(true);
		latVar.setCachedData(latArray, false);
		ncDataset.addCoordinateAxis(latVar);

		CoordinateAxis1D lonVar = new CoordinateAxis1D(ncDataset, group, "lon", DataType.FLOAT, "division", "degrees_east", "Division centroid longitude value");
        lonVar.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Lon.toString()));
//        lonVar.addAttribute(new Attribute(_Coordinate.Axes, lonVar.getFullName()));
        lonVar.setCaching(true);
		lonVar.setCachedData(lonArray, false);
		ncDataset.addCoordinateAxis(lonVar);

		
		for (Variable var : ncDataset.getVariables()) {
//			System.out.println("checking: "+var);
//			if (var.getShape().length == 2 && 
//					var.getDimension(0).getShortName().equals("division") &&
//					var.getDimension(1).getShortName().equals("time") ) {
			if (var.getShape().length == 2 && (
					(var.getDimension(0).getShortName().equals("division") && var.getDimension(1).getShortName().equals("time")) ||
					(var.getDimension(0).getShortName().equals("time") && var.getDimension(1).getShortName().equals("division"))
				)) {
						
//				System.out.println("adding lookup to variable: "+var);
				var.addAttribute(new Attribute("coordinates", "time lat lon"));
				var.addAttribute(new Attribute("_CoordinateAxes", "time lat lon"));
				var.addAttribute(new Attribute("shape_lookup", "climate-divisions.shp"));
			}
		}
		

    }
    
    
    
    
    
    
    
    
//    @Override
//    protected AxisType getAxisType( NetcdfDataset ncDataset, VariableEnhanced v) {
//    	
//    	System.out.println("------- in getAxisType: var="+v.getFullName());
//    	
//    	
//    	  String unit = v.getUnitsString();
//    	  if (unit == null)
//    	    return null;
//    	  if ( unit.equalsIgnoreCase("degrees_east") ||
//    	   unit.equalsIgnoreCase("degrees_E") ||
//    	   unit.equalsIgnoreCase("degreesE") ||
//    	   unit.equalsIgnoreCase("degree_east") ||
//    	   unit.equalsIgnoreCase("degree_E") ||
//    	   unit.equalsIgnoreCase("degreeE"))
//    	     return AxisType.Lon;
//    	  
//    	  if ( unit.equalsIgnoreCase("degrees_north") ||
//    	    unit.equalsIgnoreCase("degrees_N") ||
//    	    unit.equalsIgnoreCase("degreesN") ||
//    	    unit.equalsIgnoreCase("degree_north") ||
//    	    unit.equalsIgnoreCase("degree_N") ||
//    	    unit.equalsIgnoreCase("degreeN"))
//    	      return AxisType.Lat;
//    	      
//    	  if (SimpleUnit.isDateUnit(unit) || SimpleUnit.isTimeUnit(unit)) {
//    		  System.out.println("------- FOUND TIME VAR: "+v);
//    	    return AxisType.Time;
//    	  }
//    	 
//    	    // look for other z coordinate
//    	  if (SimpleUnit.isCompatible("m", unit))
//    	    return AxisType.Height;
//    	  if (SimpleUnit.isCompatible("mbar", unit))
//    	    return AxisType.Pressure;
//    	  if (unit.equalsIgnoreCase("level") || unit.equalsIgnoreCase("layer") || unit.equalsIgnoreCase("sigma_level"))
//    	    return AxisType.GeoZ;
//    	   
//    	  String positive = ncDataset.findAttValueIgnoreCase((Variable) v, "positive", null);
//    	  if (positive != null) {
//    	    if (SimpleUnit.isCompatible("m", unit))
//    	      return AxisType.Height;
//    	    else
//    	      return AxisType.GeoZ;
//    	  }
//    	  return null;
//    	}
    
}
