/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 * 
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE 
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.  
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */

package gov.noaa.ncdc.wct.decoders.nexrad;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.decoders.ColorLutReaders;
import gov.noaa.ncdc.wct.decoders.ColorsAndValues;
import gov.noaa.ncdc.wct.decoders.SampleDimensionAndLabels;

public class NexradSampleDimensionFactory {


	private static HashMap<String, ColorsAndValues[]> paletteOverrideMap = 
		new HashMap<String, ColorsAndValues[]>();
	
	public static void setPaletteOverride(String paletteName, ColorsAndValues[] cavOverride) {
		paletteOverrideMap.put(paletteName, cavOverride);
	}
	
	public static void removePaletteOverride(String paletteName) {
		paletteOverrideMap.remove(paletteName);
	}
	
	
	
	
	
	

	/**
	 * If levels = -1, then don't calculate equal colors and values - just use colors as stated.
	 * @param paletteName
	 * @param levels
	 * @return
	 * @throws Exception 
	 */
	public static SampleDimensionAndLabels getSampleDimensionAndLabels(String paletteName) throws Exception {

		
//		System.out.println("PALETTE OVERRIDE MAP: "+paletteOverrideMap);
		
		if (paletteOverrideMap.get(paletteName) != null) {
			ColorsAndValues[] cav = paletteOverrideMap.get(paletteName);
			return ColorLutReaders.convertToSampleDimensionAndLabels(cav[0], cav[1]);
		}

		ColorsAndValues[] cavs = getColorsAndValues(paletteName);
		if (cavs.length == 1) {
			return ColorLutReaders.convertToSampleDimensionAndLabels(cavs[0]);			
		}
		else {
			return ColorLutReaders.convertToSampleDimensionAndLabels(cavs[0], cavs[1]);			
		}
	}

	
	
	public static ColorsAndValues[] getColorsAndValues(String paletteName) throws Exception {
	
		BufferedReader br = null;
		try {
			
			URL url = ResourceUtils.getInstance().getJarResource(
					new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
					"/config/colormaps/"+paletteName, null);

			br = new BufferedReader(new InputStreamReader(url.openStream()));
			
			if (paletteName.endsWith(".pal")) {
				ColorsAndValues cav = ColorLutReaders.parseGR2A(br);
				br.close();
				return new ColorsAndValues[] { cav };
			}
			else {
				ColorsAndValues[] cav = ColorLutReaders.parseWCTPal(br);
				br.close();
//				System.out.println("CAV UNITS: "+cav[0].getUnits()+"   "+paletteName);
				return cav;
			}
			
			
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}



	public static SampleDimensionAndLabels getSampleDimensionAndLabels(String variableName, boolean classify) throws Exception {
		if (variableName.equalsIgnoreCase("Reflectivity") || 
				variableName.equalsIgnoreCase("TotalReflectivityDZ") ||
				variableName.startsWith("Total_Power") ||
				variableName.equalsIgnoreCase("TotalPower")) {
			return getSampleDimensionAndLabels(NexradHeader.LEVEL2_REFLECTIVITY, classify);
		}
		else if (variableName.equalsIgnoreCase("RadialVelocity") || 
				variableName.equalsIgnoreCase("Velocity") ||
				variableName.equalsIgnoreCase("RadialVelocityVR")) {
			return getSampleDimensionAndLabels(NexradHeader.LEVEL2_VELOCITY, classify);
		}
		else if (variableName.equalsIgnoreCase("SpectrumWidth") || 
				variableName.equalsIgnoreCase("Width") ||
				variableName.equalsIgnoreCase("SpectrumWidthSW")) {
			return getSampleDimensionAndLabels(NexradHeader.LEVEL2_SPECTRUMWIDTH, classify);
		}
		else if (variableName.equals("DifferentialReflectivity")) {
			return getSampleDimensionAndLabels(NexradHeader.LEVEL2_DIFFERENTIALREFLECTIVITY, classify);
		}
		else if (variableName.equals("CorrelationCoefficient")) {
			return getSampleDimensionAndLabels(NexradHeader.LEVEL2_CORRELATIONCOEFFICIENT, classify);
		}
		else if (variableName.equals("DifferentialPhase")) {
			return getSampleDimensionAndLabels(NexradHeader.LEVEL2_DIFFERENTIALPHASE, classify);
		}
		else {
			return getSampleDimensionAndLabels(NexradHeader.UNKNOWN, classify);
		}
	}


	public static SampleDimensionAndLabels getSampleDimensionAndLabels(int productCode) throws Exception {
		return getSampleDimensionAndLabels(productCode, true);
	}

	public static SampleDimensionAndLabels getSampleDimensionAndLabels(int productCode, boolean classify) throws Exception {
		return getSampleDimensionAndLabels(productCode, classify, 0.0f);
	}

	public static SampleDimensionAndLabels getSampleDimensionAndLabels(int productCode, boolean classify, float productVersion) throws Exception {

		String palName = getDefaultPaletteName(productCode, productVersion);
		SampleDimensionAndLabels sd = null;
		if (palName != null) {
			sd = getSampleDimensionAndLabels(palName);
		}
		return sd;

	} // END static getColors(productCode)     

	

	
	
	
//	public static String getDefaultPaletteName(int productCode) {
//		return getDefaultPaletteName(productCode, "0");
//	}

	public static String getDefaultPaletteName(int productCode, float version) {


		String palName = null;

//		try {
//			String name = "nexrad_l3_p"+productCode+".wctpal";
//			URL url = ResourceUtils.getInstance().getJarResource(
//					new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
//					"/config/colormaps/"+name, null);
//			
//			if (url != null) {
//				System.out.println("found l3 palette of: "+name);
//				return name;
//			}
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		
		switch(productCode) {          

		case NexradHeader.UNKNOWN:
			palName = "spi-ncei.wctpal";
			break;

			
			
			
		case NexradHeader.LEVEL2_REFLECTIVITY:
		case NexradHeader.L3PC_LONG_RANGE_BASE_REFLECTIVITY_8BIT:
		case NexradHeader.L3PC_DIGITAL_HYBRID_SCAN:
			palName = "nexrad_dp_z.wctpal";
			break;
			

		case NexradHeader.LEVEL2_DIFFERENTIALREFLECTIVITY:
		case NexradHeader.L3PC_DIGITAL_DIFFERENTIAL_REFLECTIVITY:
			palName = "nexrad_dp_zdr.wctpal";
			break;
		case NexradHeader.LEVEL2_DIFFERENTIALPHASE:
			palName = "nexrad_dp_phi.wctpal";
			break;
		case NexradHeader.LEVEL2_CORRELATIONCOEFFICIENT:
		case NexradHeader.L3PC_DIGITAL_CORRELATION_COEFFICIENT:
			palName = "nexrad_dp_rho.wctpal";
			break;
			

		case NexradHeader.LEVEL2_VELOCITY:
		case NexradHeader.L3PC_LONG_RANGE_BASE_VELOCITY_8BIT:
		case NexradHeader.L3PC_TDWR_BASE_VELOCITY_8BIT:
			palName = "nexrad_bvel.wctpal";
			break;

		case NexradHeader.LEVEL2_SPECTRUMWIDTH:
			palName = "nexrad_spec.wctpal";
			break;

			
		case NexradHeader.L3PC_DIGITAL_SPECIFIC_DIFFERENTIAL_PHASE:
			palName = "nexrad_l3_dkd.wctpal";
			break;			
		case NexradHeader.L3PC_ONE_HOUR_ACCUMULATION:
			palName = "nexrad_l3_oha_daa.wctpal";
			break;
		case NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION:
			palName = "nexrad_l3_oha_daa.wctpal";
			break;
		case NexradHeader.L3PC_DIGITAL_USER_SELECTABLE_ACCUMULATION:
			palName = "nexrad_l3_oha_daa.wctpal";
			break;
		case NexradHeader.L3PC_DIGITAL_HYDROMETEOR_CLASSIFICATION:
		case NexradHeader.L3PC_DIGITAL_HYBRID_HYDROMETEOR_CLASSIFICATION:
//			System.out.println("HHC Version: "+version);
			if (Math.round(version) >= 1) {
				palName = "nexrad_l3_dhc_v1.wctpal";
			}
			else {
				palName = "nexrad_l3_dhc.wctpal";
			}
			break;

		case NexradHeader.L3PC_STORM_TOTAL_ACCUMULATION:
			palName = "nexrad_l3_sta_dsa.wctpal";
			break;
		case NexradHeader.L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION:
		case NexradHeader.L3PC_DIGITAL_HYBRID_PRECIP:
			palName = "nexrad_l3_sta_dsa.wctpal";
			break;
		case NexradHeader.L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION_DIFFERENCE:
		case NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION_DIFFERENCE:
			palName = "nexrad_l3_ohd_dsd.wctpal";
			break;
			
		case NexradHeader.L3PC_DIGITAL_INSTANTANEOUS_PRECIP_RATE:
			palName = "nexrad_l3_dpr.wctpal";
			break;
			
		case NexradHeader.L3PC_MET_SIGNAL:
			palName = "nexrad_l3_metsignal.wctpal";
			break;
		
		default: 
			palName = "nexrad_l3_p"+productCode+".wctpal";
			
		} // END switch


		return palName;

	} // END static getColors(productCode)     
	
	

} // END class
