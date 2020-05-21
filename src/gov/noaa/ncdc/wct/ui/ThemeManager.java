package gov.noaa.ncdc.wct.ui;

import java.awt.Color;

public class ThemeManager {

	public static void setViewerTheme(WCTViewer viewer, String themeName) {
		
		if (themeName.equalsIgnoreCase("Dark")) {

			LayerSelector mapSelect = viewer.getMapSelector();
			mapSelect.getWMSPanel().setSelectedWMS(0, "None");
			mapSelect.getWMSPanel().setSelectedWMS(1, "None");
			mapSelect.getWMSPanel().setSelectedWMS(2, "None");
			mapSelect.getWMSPanel().refreshWMS();

			for (int n=0; n<WCTViewer.NUM_LAYERS; n++) {
				mapSelect.setLayerVisibility(n, false);
				mapSelect.setLabelVisibility(n, false);
			}                

			mapSelect.setMapBackgroundColor(new Color(0, 0, 55));
			mapSelect.setLayerColor(WCTViewer.COUNTRIES, Color.BLACK);
			mapSelect.setLayerColor(WCTViewer.STATES, Color.BLACK);
//			viewer.setLayerFillColor(WCTViewer.COUNTRIES, Color.BLACK);
//			viewer.setLayerFillColor(WCTViewer.STATES, Color.BLACK);
//			viewer.setLayerLineColor(WCTViewer.COUNTRIES, Color.WHITE);
//			viewer.setLayerLineColor(WCTViewer.STATES, Color.WHITE);
			mapSelect.setLayerColor(WCTViewer.COUNTRIES_OUT, new Color(5, 167, 0));
			mapSelect.setLayerColor(WCTViewer.STATES_OUT, new Color(235, 167, 0));
			mapSelect.setLayerColor(WCTViewer.HWY_INT, Color.RED);
			
			mapSelect.setLayerLineWidth(WCTViewer.COUNTRIES, 1);
			mapSelect.setLayerLineWidth(WCTViewer.STATES, 1);
			mapSelect.setLayerLineWidth(WCTViewer.COUNTRIES_OUT, 1);
			mapSelect.setLayerLineWidth(WCTViewer.STATES_OUT, 2);
			mapSelect.setLayerLineWidth(WCTViewer.HWY_INT, 1);
			
			
			
			mapSelect.setLayerVisibility(WCTViewer.COUNTRIES, true);			
			mapSelect.setLayerVisibility(WCTViewer.STATES, true);
			mapSelect.setLayerVisibility(WCTViewer.COUNTRIES_OUT, true);
			mapSelect.setLayerVisibility(WCTViewer.STATES_OUT, true);
			mapSelect.setLayerVisibility(WCTViewer.HWY_INT, true);
			
			mapSelect.setRadarTransparency(-1);
			mapSelect.setGridSatelliteTransparency(-1);
			mapSelect.setLocationStationTransparency(-1);
			
		}
		else if (themeName.equalsIgnoreCase("Light")) {

			LayerSelector mapSelect = viewer.getMapSelector();
			mapSelect.getWMSPanel().setSelectedWMS(0, "None");
			mapSelect.getWMSPanel().setSelectedWMS(1, "None");
			mapSelect.getWMSPanel().setSelectedWMS(2, "None");
			mapSelect.getWMSPanel().refreshWMS();
			
			
			for (int n=0; n<WCTViewer.NUM_LAYERS; n++) {
				mapSelect.setLayerVisibility(n, false);
				mapSelect.setLabelVisibility(n, false);
			}                

			mapSelect.setMapBackgroundColor(new Color(204, 204, 255));
			mapSelect.setLayerColor(WCTViewer.COUNTRIES, Color.WHITE);
			mapSelect.setLayerColor(WCTViewer.STATES, Color.WHITE);
//			viewer.setLayerFillColor(WCTViewer.COUNTRIES, Color.WHITE);
//			viewer.setLayerFillColor(WCTViewer.STATES, Color.WHITE);
//			viewer.setLayerLineColor(WCTViewer.COUNTRIES, Color.WHITE);
//			viewer.setLayerLineColor(WCTViewer.STATES, Color.WHITE);
			mapSelect.setLayerColor(WCTViewer.COUNTRIES_OUT, new Color(140, 140, 140));
			mapSelect.setLayerColor(WCTViewer.STATES_OUT, Color.BLACK);
			mapSelect.setLayerColor(WCTViewer.HWY_INT, new Color(255, 204, 204));
			
			mapSelect.setLayerLineWidth(WCTViewer.COUNTRIES, 1);
			mapSelect.setLayerLineWidth(WCTViewer.STATES, 1);
			mapSelect.setLayerLineWidth(WCTViewer.COUNTRIES_OUT, 1);
			mapSelect.setLayerLineWidth(WCTViewer.STATES_OUT, 1);
			mapSelect.setLayerLineWidth(WCTViewer.HWY_INT, 1);
			

			mapSelect.setLayerVisibility(WCTViewer.COUNTRIES, true);
			mapSelect.setLayerVisibility(WCTViewer.STATES, true);
			mapSelect.setLayerVisibility(WCTViewer.COUNTRIES_OUT, true);
			mapSelect.setLayerVisibility(WCTViewer.STATES_OUT, true);
			mapSelect.setLayerVisibility(WCTViewer.HWY_INT, true);

			mapSelect.setRadarTransparency(-1);
			mapSelect.setGridSatelliteTransparency(-1);
			mapSelect.setLocationStationTransparency(-1);

		}
		else if (themeName.equalsIgnoreCase("Gray")) {

			LayerSelector mapSelect = viewer.getMapSelector();
			mapSelect.setMapBackgroundColor(new Color(160, 160, 160));
			
			for (int n=0; n<WCTViewer.NUM_LAYERS; n++) {
				mapSelect.setLayerVisibility(n, false);
				mapSelect.setLabelVisibility(n, false);
			}                

			mapSelect.getWMSPanel().setSelectedWMS(0, "World Roads (ESRI)");
			mapSelect.getWMSPanel().setIsBlackAndWhite(0, true);
			mapSelect.getWMSPanel().setSelectedWMS(1, "None");
			mapSelect.getWMSPanel().setSelectedWMS(2, "None");
			
			mapSelect.getWMSPanel().refreshWMS();
			
			mapSelect.setLayerColor(WCTViewer.COUNTRIES_OUT, new Color(20, 20, 20));
			mapSelect.setLayerColor(WCTViewer.STATES_OUT, new Color(20, 20, 20));
			mapSelect.setLayerLineWidth(WCTViewer.COUNTRIES_OUT, 2);
			mapSelect.setLayerLineWidth(WCTViewer.STATES_OUT, 2);
			mapSelect.setLayerVisibility(WCTViewer.COUNTRIES_OUT, true);
			mapSelect.setLayerVisibility(WCTViewer.STATES_OUT, true);
			
			mapSelect.setRadarTransparency(40);
			mapSelect.setGridSatelliteTransparency(40);
			mapSelect.setLocationStationTransparency(40);
			
		}
		else if (themeName.equalsIgnoreCase("Streets")) {

			LayerSelector mapSelect = viewer.getMapSelector();
			mapSelect.setMapBackgroundColor(new Color(160, 160, 160));
			
			for (int n=0; n<WCTViewer.NUM_LAYERS; n++) {
				mapSelect.setLayerVisibility(n, false);
				mapSelect.setLabelVisibility(n, false);
			}                

			mapSelect.getWMSPanel().setSelectedWMS(0, "World Roads (ESRI)");
			mapSelect.getWMSPanel().setIsBlackAndWhite(0, false);
			mapSelect.getWMSPanel().setSelectedWMS(1, "None");
			mapSelect.getWMSPanel().setSelectedWMS(2, "None");
			
			mapSelect.getWMSPanel().refreshWMS();
			
			mapSelect.setLayerColor(WCTViewer.COUNTRIES_OUT, new Color(20, 20, 20));
			mapSelect.setLayerColor(WCTViewer.STATES_OUT, new Color(20, 20, 20));
			mapSelect.setLayerLineWidth(WCTViewer.COUNTRIES_OUT, 2);
			mapSelect.setLayerLineWidth(WCTViewer.STATES_OUT, 2);
			mapSelect.setLayerVisibility(WCTViewer.COUNTRIES_OUT, true);
			mapSelect.setLayerVisibility(WCTViewer.STATES_OUT, true);
			
			mapSelect.setRadarTransparency(40);
			mapSelect.setGridSatelliteTransparency(40);
			mapSelect.setLocationStationTransparency(40);
			
		}
		else if (themeName.equalsIgnoreCase("Earth")) {

			LayerSelector mapSelect = viewer.getMapSelector();
			mapSelect.setMapBackgroundColor(new Color(160, 160, 160));
			for (int n=0; n<WCTViewer.NUM_LAYERS; n++) {
				mapSelect.setLayerVisibility(n, false);
				mapSelect.setLabelVisibility(n, false);
			}                

			mapSelect.getWMSPanel().setSelectedWMS(0, "World Imagery (ESRI)");
			mapSelect.getWMSPanel().setIsBlackAndWhite(0, false);
			mapSelect.getWMSPanel().setSelectedWMS(1, "None");
			mapSelect.getWMSPanel().setSelectedWMS(2, "None");
			
			mapSelect.getWMSPanel().refreshWMS();
			
			mapSelect.setLayerColor(WCTViewer.COUNTRIES_OUT, new Color(160, 160, 160));
			mapSelect.setLayerColor(WCTViewer.STATES_OUT, new Color(160, 160, 160));
			mapSelect.setLayerLineWidth(WCTViewer.COUNTRIES_OUT, 2);
			mapSelect.setLayerLineWidth(WCTViewer.STATES_OUT, 2);
			mapSelect.setLayerVisibility(WCTViewer.COUNTRIES_OUT, true);
			mapSelect.setLayerVisibility(WCTViewer.STATES_OUT, true);

			mapSelect.setRadarTransparency(40);
			mapSelect.setGridSatelliteTransparency(40);
			mapSelect.setLocationStationTransparency(40);
		}
		else if (themeName.equalsIgnoreCase("World Relief")) {

			LayerSelector mapSelect = viewer.getMapSelector();
			mapSelect.setMapBackgroundColor(new Color(160, 160, 160));
			for (int n=0; n<WCTViewer.NUM_LAYERS; n++) {
				mapSelect.setLayerVisibility(n, false);
				mapSelect.setLabelVisibility(n, false);
			}                

			mapSelect.getWMSPanel().setSelectedWMS(0, "Color Shaded Relief (NOAA ETOPO1)");
			mapSelect.getWMSPanel().setIsBlackAndWhite(0, true);
			mapSelect.getWMSPanel().setSelectedWMS(1, "None");
			mapSelect.getWMSPanel().setSelectedWMS(2, "None");
			
			mapSelect.getWMSPanel().refreshWMS();
			
			mapSelect.setLayerColor(WCTViewer.COUNTRIES_OUT, new Color(20, 20, 20));
			mapSelect.setLayerColor(WCTViewer.STATES_OUT, new Color(20, 20, 20));
			mapSelect.setLayerLineWidth(WCTViewer.COUNTRIES_OUT, 2);
			mapSelect.setLayerLineWidth(WCTViewer.STATES_OUT, 2);
			mapSelect.setLayerVisibility(WCTViewer.COUNTRIES_OUT, true);
			mapSelect.setLayerVisibility(WCTViewer.STATES_OUT, true);

			mapSelect.setRadarTransparency(40);
			mapSelect.setGridSatelliteTransparency(40);
			mapSelect.setLocationStationTransparency(40);
		}

	}
}
