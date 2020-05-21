package gov.noaa.ncdc.wct.decoders.cdm;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.ColorLutReaders;
import gov.noaa.ncdc.wct.decoders.ColorsAndValues;
import gov.noaa.ncdc.wct.decoders.goes.GoesColorFactory;

public class GridDatasetColorFactory {

    public static Color[] getColors(String name, boolean isFlipped) throws Exception {
    	
//		if (name.startsWith("Satellite: ") || name.startsWith("Radar: ") || name.startsWith("Drought: ")) {
//			ColorsAndValues cav = GridDatasetColorFactory.getColorsAndValues(
//					name.substring(name.indexOf(" ")+1, name.length()));
//			return cav
    	
    	
        Color[] colors = null;
        if (name.equalsIgnoreCase("rainbow")) {
            colors = new Color[] { Color.BLUE.darker().darker(), Color.BLUE, Color.BLUE.brighter().brighter(), Color.GREEN, 
                    Color.YELLOW, Color.ORANGE, Color.RED };                       
        }
        else if (name.equalsIgnoreCase("rainbow2")) {
            colors = new Color[] { Color.BLUE.darker().darker(), Color.BLUE.brighter(), Color.GREEN, Color.WHITE,
                    Color.YELLOW, Color.ORANGE, Color.RED };                       
        }
        else if (name.equalsIgnoreCase("rainbow3")) {
            colors = new Color[] { new Color(75, 0, 130), Color.BLUE, Color.CYAN, Color.GREEN,
                    Color.YELLOW, Color.ORANGE, Color.RED };                       
        }
        else if (name.equalsIgnoreCase("Dry (Red) - Wet (Blue)")) {
            colors = new Color[] { new Color(0, 0, 255), new Color(65, 105, 225), new Color(29, 124, 255), 
            		new Color(0, 191, 255), new Color(140, 205, 239), 
            		Color.WHITE, 
            		new Color(255, 255, 0), new Color(255, 211, 127),
                    new Color(255, 170, 0), new Color(230, 0, 0), new Color(115, 0, 0) };                       
        }
        else if (name.equalsIgnoreCase("grayscale")) {
            colors = new Color[] { Color.BLACK, Color.WHITE };
        }
        else if (name.equalsIgnoreCase("blue-white-red")) {
            colors = new Color[] { Color.BLUE.darker(), Color.WHITE, Color.RED.darker() };
        }
        else if (name.equalsIgnoreCase("blue-black-red")) {
            colors = new Color[] { Color.BLUE.brighter(), Color.BLACK, Color.RED.brighter() };
        }
        else if (name.equalsIgnoreCase("blue-red")) {
            colors = new Color[] { Color.BLUE.darker(), Color.RED.darker() };
        }
        else if (name.equalsIgnoreCase("blue-green-red")) {
            colors = new Color[] { Color.BLUE.darker(), Color.GREEN, Color.RED.darker() };
        }
        else if (name.equalsIgnoreCase("brown-white-green")) {
            colors = new Color[] { new Color(184, 153, 125), Color.WHITE, new Color(118, 229, 119) };
        }
        else if (name.equalsIgnoreCase("brown-white-green (darker)")) {
            colors = new Color[] { new Color(184, 153, 125).darker().darker(), Color.WHITE, new Color(118, 229, 119).darker().darker() };
        }
        else if (name.equalsIgnoreCase("white-green")) {
            colors = new Color[] { Color.WHITE, Color.GREEN.darker() };
        }
        else if (name.equalsIgnoreCase("black-green")) {
            colors = new Color[] { Color.BLACK, Color.GREEN.darker() };
        }
        else if (name.equalsIgnoreCase("white-blue")) {
            colors = new Color[] { Color.WHITE, Color.BLUE.darker() };
        }
        else if (name.equalsIgnoreCase("black-blue")) {
            colors = new Color[] { Color.BLACK, Color.CYAN };
        }
        else if (name.equalsIgnoreCase("white-red")) {
            colors = new Color[] { Color.WHITE, Color.RED.darker() };
        }
        else if (name.equalsIgnoreCase("black-red")) {
            colors = new Color[] { Color.BLACK, Color.RED.darker() };
        }
        else if (name.equalsIgnoreCase("yellow-purple")) {
            colors = new Color[] { Color.YELLOW, new Color(122, 55, 139) };
        }
        else if (name.equalsIgnoreCase("yellow-blue-purple")) {
            colors = new Color[] { Color.YELLOW, Color.BLUE, new Color(122, 55, 139) };
        }
        else if (name.equalsIgnoreCase("yellow-blue-purple-red")) {
            colors = new Color[] { Color.YELLOW, Color.BLUE, new Color(122, 55, 139), Color.RED.darker() };
        }
        else if (name.equalsIgnoreCase("yellow-red-purple-blue")) {
            colors = new Color[] { Color.YELLOW, Color.RED.darker(), new Color(122, 55, 139), Color.BLUE };
        }
        else if (name.equalsIgnoreCase("yellow-blue")) {
            colors = new Color[] { Color.YELLOW, Color.ORANGE, Color.CYAN, Color.BLUE.darker().darker() };
        }
        else {
            throw new Exception("Color table name of '"+name+"' is not recognized.");
        }
        
        if (isFlipped) {
            colors = WCTUtils.flipArray(colors);            
        }
        return colors;
    }
    
    
    public static ColorsAndValues getColorsAndValues(String colorTableName) throws MalformedURLException, WCTException, IOException {
    	
    	GoesColorFactory goesColorFactory = GoesColorFactory.getInstance();
    	
    	if (colorTableName.equalsIgnoreCase("Water Vapor (Default)")) {
    		URL url = ResourceUtils.getInstance().getJarResource(
                    new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                    "/config/colormaps/goes-IR3.cmap", null);
    		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            ColorsAndValues cav = ColorsAndValues.calculateEqualColorsAndValues(ColorLutReaders.parseCmapFormat(br));
//            cav.flip();
            br.close();
            return cav;
    	}
    	else if (colorTableName.equalsIgnoreCase("Infrared Window (Default)")) {
    		URL url = ResourceUtils.getInstance().getJarResource(
                    new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                    "/config/colormaps/goes-IR4.cmap", null);
    		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            ColorsAndValues cav = ColorsAndValues.calculateEqualColorsAndValues(ColorLutReaders.parseCmapFormat(br));
//            cav.flip();
            br.close();
//     System.out.println(cav);
            return cav;
    	}    	
    	else if (colorTableName.equalsIgnoreCase("McIDAS_TSTORM1.ET")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
                    goesColorFactory.readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-TSTORM1.ET", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("Water Vapor 1 (McIDAS_VAPOR1.ET)")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
            		goesColorFactory.readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-VAPOR1.ET", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("Water Vapor 2 (McIDAS_WVRBT3.ET)")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
            		goesColorFactory.readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-WVRBT3.ET", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("McIDAS_TSTORM2.ET")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
            		goesColorFactory.readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-TSTORM2.ET", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("McIDAS_TEMPS1.ET")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
            		goesColorFactory.readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-TEMPS1.ET", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("McIDAS_SSTS.ET")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
            		goesColorFactory.readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-SSTS.ET", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("McIDAS_CA.ET")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
            		goesColorFactory.readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-ca.et", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("McIDAS_BB.ET")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
            		goesColorFactory.readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-bb.et", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("McIDAS_BD.ET")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
            		goesColorFactory.readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-bd.et", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("Reflectivity")) {

        	URL url = ResourceUtils.getInstance().getJarResource(
        			new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
        			"/config/colormaps/nexrad_dp_z.pal", null);

        	BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        	ColorsAndValues rawCav = ColorLutReaders.parseGR2A(br);
        	br.close();
        	ColorsAndValues cav = ColorsAndValues.calculateEqualColorsAndValues(rawCav, 19);
        	return cav;
        }
        else if (colorTableName.equalsIgnoreCase("Diff. Reflectivity")) {

        	URL url = ResourceUtils.getInstance().getJarResource(
        			new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
        			"/config/colormaps/nexrad_dp_zdr.pal", null);

        	BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        	ColorsAndValues rawCav = ColorLutReaders.parseGR2A(br);
        	br.close();
        	ColorsAndValues cav = ColorsAndValues.calculateEqualColorsAndValues(rawCav, 19);
        	return cav;
        }
        else if (colorTableName.equalsIgnoreCase("Palmer Drought Indices")) {

        	URL url = ResourceUtils.getInstance().getJarResource(
        			new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
        			"/config/colormaps/palmer-wrcc.wctpal", null);

        	BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        	ColorsAndValues rawCav = ColorLutReaders.parseWCTPal(br)[0]; // 1st element is color ranges, 2nd element are unique values, 
        	br.close();
        	
        	// TODO: Investigate why .wctpal files have to start with low values, then end with high, and how 
        	//       there may be a bug in flipping the values.
        	
        	ColorsAndValues cav = ColorsAndValues.calculateEqualColorsAndValues(rawCav, 19);
        	return cav;
        } 
        else if (colorTableName.equalsIgnoreCase("Standardized Precip. Index (SPI)")) {

        	URL url = ResourceUtils.getInstance().getJarResource(
        			new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
        			"/config/colormaps/spi-ncei.wctpal", null);

        	BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        	ColorsAndValues rawCav = ColorLutReaders.parseWCTPal(br)[0]; // 1st element is color ranges, 2nd element are unique values, 
        	br.close();
        	
        	// TODO: Investigate why .wctpal files have to start with low values, then end with high, and how 
        	//       there may be a bug in flipping the values.
        	
        	ColorsAndValues cav = ColorsAndValues.calculateEqualColorsAndValues(rawCav, 19);
        	return cav;
        }
        else {
            throw new WCTException("Color table name of '"+colorTableName+"' is not recognized.");
        }
    	
    }
}
