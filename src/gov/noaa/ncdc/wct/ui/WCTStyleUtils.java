package gov.noaa.ncdc.wct.ui;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;

import org.geotools.feature.FeatureType;
import org.geotools.filter.BetweenFilter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.IllegalFilterException;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;

import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.decoders.ColorsAndValues;

public class WCTStyleUtils {



	public static Style getStyle(FeatureType schema, String attName, double minVal, double maxVal, boolean isPolygon) throws WCTException {
		Color[] colors = new Color[] {
				new Color(122, 0, 179),
				Color.BLUE,
				Color.CYAN,
				Color.GREEN.brighter(),
				Color.GREEN,
				Color.GREEN.darker(),
				Color.YELLOW,
				Color.ORANGE,
				Color.RED,
				Color.RED.darker().darker(),
				new Color(155, 50, 50)
		};

		return getStyle(schema, attName, colors, minVal, maxVal, isPolygon);
	}

	public static Style getStyle(FeatureType schema, String attName, Color[] colors, double minVal, double maxVal, boolean isPolygon) throws WCTException {
		int defaultAlpha = 160;
		return getStyle(schema, attName, colors, minVal, maxVal, isPolygon, defaultAlpha);
	}

	public static Style getStyle(FeatureType schema, String attName, Color[] colors, double minVal, double maxVal, boolean isPolygon, int alpha) throws WCTException {

		System.out.println("minVal: "+minVal + "  maxVal: "+maxVal + " alpha: "+alpha);

		Collections.reverse(Arrays.asList(colors));
		
		ColorsAndValues cav = getColorsAndValues(colors, minVal, maxVal);
		cav = ColorsAndValues.calculateEqualColorsAndValues(cav);

		return getStyle(schema, attName, cav, isPolygon, alpha);
	}

	public static Style getStyle(FeatureType schema, String attName, ColorsAndValues colorsAndValues, boolean isPolygon) {

		int defaultAlpha = 160;
		return getStyle(schema, attName, colorsAndValues, isPolygon, defaultAlpha);

	}
	
	public static ColorsAndValues getColorsAndValues(Color[] colors, double minVal, double maxVal) {
		Color[] currentSymbolizedColors = colors;
		Double[] currentSymbolizedValues = new Double[currentSymbolizedColors.length];

		// (-1) to allow last value in value array to equal max value
		double interval = (maxVal-minVal)/(colors.length-1);

		//		System.out.println("minVal: "+minVal + "  maxVal: "+maxVal + " interval: "+interval);

		for (int i=0; i<currentSymbolizedValues.length; i++) {
			currentSymbolizedValues[i] = new Double(minVal + i*interval);
			//			System.out.println(i+" "+currentSymbolizedValues[i]);
		}

		// don't pass in units, yet...
		ColorsAndValues cav = new ColorsAndValues(colors, currentSymbolizedValues, null);
		return cav;
	}

	public static Style getStyle(FeatureType schema, String attName, ColorsAndValues colorsAndValues, boolean isPolygon, int alpha) {

		StyleBuilder sb = new StyleBuilder();
		Color[] currentSymbolizedColors = colorsAndValues.getColors();
		Double[] currentSymbolizedValues = colorsAndValues.getValues();

		Style style = sb.createStyle();


		if (currentSymbolizedColors.length <= 1 && currentSymbolizedValues.length <= 1) {
			return style;
		}

		try {
			FilterFactory ffi = FilterFactory.createFilterFactory();

			// add catch-all for everything less than the lowest value given
			style.addFeatureTypeStyle(
					getFeatureTypeStyle(sb, ffi, schema, currentSymbolizedColors[0], attName,
							Double.NEGATIVE_INFINITY, 
							currentSymbolizedValues[0] < currentSymbolizedValues[1] ? 
									currentSymbolizedValues[0] : currentSymbolizedValues[currentSymbolizedValues.length-1], 
									isPolygon, alpha)
					);	

			for (int i = 0; i < currentSymbolizedColors.length-1; i++) {
				style.addFeatureTypeStyle(
						getFeatureTypeStyle(sb, ffi, schema, currentSymbolizedColors[i], attName,
								currentSymbolizedValues[i], currentSymbolizedValues[i+1], isPolygon, alpha)
						);
			}

			// add catch-all for everything less than the lowest value given
			style.addFeatureTypeStyle(
					getFeatureTypeStyle(sb, ffi, schema, currentSymbolizedColors[currentSymbolizedColors.length-1], attName,
							currentSymbolizedValues[0] < currentSymbolizedValues[1] ? 
									currentSymbolizedValues[currentSymbolizedValues.length-1] : currentSymbolizedValues[0], 
									Double.POSITIVE_INFINITY, isPolygon, alpha)
					);	


		} catch (Exception e) {
			e.printStackTrace();
		}

		return style;      
	}

	public static FeatureTypeStyle getFeatureTypeStyle(StyleBuilder sb, FilterFactory ffi, FeatureType schema,
			Color c, String attName, double minVal, double maxVal, boolean isPolygon, int alpha) throws IllegalFilterException {

		System.out.println("Building featuretypestyle for "+schema.toString());
		System.out.println("attName: "+attName+" min/max: "+minVal+"/"+maxVal);
		
		BetweenFilter filters = ffi.createBetweenFilter();
		Rule rules = null;

		if (isPolygon) {
			PolygonSymbolizer polysymb = sb.createPolygonSymbolizer(c, c, 1);
			polysymb.getFill().setOpacity(sb.literalExpression(alpha/255.0));
			polysymb.getStroke().setOpacity(sb.literalExpression(alpha/255.0));
			rules = sb.createRule(polysymb);
		}
		else {				
			Mark plmark = sb.createMark(StyleBuilder.MARK_CIRCLE, c, c, .5);
//			Mark plmark = sb.createMark(StyleBuilder.MARK_CIRCLE, c, c.darker().darker().darker(), 5);
			plmark.getFill().setOpacity(sb.literalExpression(alpha/255.0));
//			plmark.getStroke().setOpacity(sb.literalExpression(alpha/255.0));
			plmark.getStroke().setOpacity(sb.literalExpression(1.0));

			Graphic plgr = sb.createGraphic(null, plmark, null);
			plgr.setSize(sb.literalExpression(10.0));
			PointSymbolizer plps = sb.createPointSymbolizer(plgr);

			rules = sb.createRule(plps);
		}

		//		filters[i].addLeftValue(sb.literalExpression(minVal+(i*interval) ));
		//		filters[i].addRightValue(sb.literalExpression(minVal+((i+1)*interval) ));
		//		filters[i].addMiddleValue(ffi.createAttributeExpression(schema, "value"));
		//		currentSymbolizedValues[i] = minVal+(i*interval);

		filters.addLeftValue(sb.literalExpression(minVal));
		filters.addRightValue(sb.literalExpression(maxVal));
		filters.addMiddleValue(ffi.createAttributeExpression(schema, attName));

		//		System.out.println(filters);



		rules.setFilter(filters);

		return sb.createFeatureTypeStyle(null, rules);





		//              org.geotools.styling.Font gtFont = BaseMapManager.GT_FONT_ARRAY[0];
		//              DeclutterType declutterType = DeclutterType.FULL;
		//              double minScaleDenom = 150.0;
		//              double maxScaleDenom = BaseMapStyleInfo.NO_MAX_SCALE;
		//              
		//          	String attName = "label";
		//        	String[] attNameArray = attName.split(",");
		//
		//
		//        	
		//        	double rotationAsDeclutterFlag = 0;
		//        	if (declutterType == DeclutterType.NONE) {
		//        		rotationAsDeclutterFlag = 720;
		//        	}
		//        	else if (declutterType == DeclutterType.SIMPLE) {
		//        		rotationAsDeclutterFlag = 360;
		//        	}
		//        	
		//    		double yDisplacment = -5;
		//    		Symbolizer[] symbArray = new Symbolizer[attNameArray.length];
		//        	for (int n=0; n<attNameArray.length; n++) {
		//        		TextSymbolizer ts = sb.createTextSymbolizer(color[i], gtFont, attNameArray[n]);
		//        		ts.setHalo(sb.createHalo(color[0], .7, 2.2));
		//        		ts.setLabelPlacement(sb.createPointPlacement(0.0, 0.0, 5.0, yDisplacment, rotationAsDeclutterFlag));
		//        		yDisplacment += 10;    		
		//        		symbArray[n] = ts;
		//        	}
		//        	
		//
		//			labelRules[i] = sb.createRule(symbArray);	            
		//            labelRules[i].setFilter(filters[i]);
		//            
		//            FeatureTypeStyle fts = sb.createFeatureTypeStyle(symbArray, minScaleDenom, maxScaleDenom);
		//            fts.addRule(labelRules[i]);
		//    		style.addFeatureTypeStyle(fts);

	}

	/*
	   public Style getNexradPolygonStyle(NexradHeader header) {
	         // Create Filters and Style for NEXRAD Polygons!
	         Color[] color = NexradColorFactory.getColors(header.getProductCode());

	         Rule rules[] = new Rule[color.length];
	         Style nexradStyle = sb.createStyle();
	         try {
	            BetweenFilter filters[] = new BetweenFilter[color.length];
	            FilterFactory ffi = FilterFactory.createFilterFactory();

	            for (int i = 0; i < color.length; i++) {

	               filters[i] = ffi.createBetweenFilter();
	               PolygonSymbolizer polysymb = sb.createPolygonSymbolizer(color[i], color[i], 1);
	               polysymb.getFill().setOpacity(sb.literalExpression(nexradAlphaChannelValue/255.0));
	               polysymb.getStroke().setOpacity(sb.literalExpression(nexradAlphaChannelValue/255.0));
	               rules[i] = sb.createRule(polysymb);

	               filters[i].addLeftValue(sb.literalExpression(i));
	               filters[i].addRightValue(sb.literalExpression(i + 1));
	               filters[i].addMiddleValue(ffi.createAttributeExpression(nexradSchema, "colorIndex"));
	               rules[i].setFilter(filters[i]);

	               nexradStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, rules[i]));
	            }
	         } catch (Exception e) {
	            e.printStackTrace();
	         }

	         return nexradStyle;      
	   }

	 */
	


	public static Style getDefaultStyle() {
		StyleBuilder sb = new StyleBuilder();
		Mark plmark = sb.createMark(StyleBuilder.MARK_CIRCLE, Color.GREEN.darker().darker().darker(), Color.GREEN, .5);
		Graphic plgr = sb.createGraphic(null, plmark, null);
		plgr.setSize(sb.literalExpression(10.0));
		PointSymbolizer plps = sb.createPointSymbolizer(plgr);

		return sb.createStyle(plps);
	}

	public static Style getBlankTransparentStyle() {
		StyleBuilder sb = new StyleBuilder();
		Mark plmark = sb.createMark(StyleBuilder.MARK_CIRCLE, Color.GREEN.darker().darker().darker(), Color.GREEN, .5);
		//        plmark.setSize(sb.literalExpression(0.0));
		plmark.getFill().setOpacity(sb.literalExpression(0.0));
		plmark.getStroke().setOpacity(sb.literalExpression(0.0));
		Graphic plgr = sb.createGraphic(null, plmark, null);
		PointSymbolizer plps = sb.createPointSymbolizer(plgr);

		return sb.createStyle(plps);
	}

	public static Style getDefaultSelectedPointStyle() throws IllegalFilterException {

		StyleBuilder sb = new StyleBuilder();
		Mark plmark = sb.createMark(StyleBuilder.MARK_CIRCLE, Color.BLACK, Color.YELLOW, 3.5);
		plmark.getFill().setOpacity(sb.literalExpression(0.0));
		//        plmark.setSize(sb.literalExpression(100));
		Graphic plgr = sb.createGraphic(null, plmark, null);
		plgr.setSize(sb.literalExpression(20.0));
		PointSymbolizer plps = sb.createPointSymbolizer(plgr);
		Style style = sb.createStyle(plps);

		String attName = "label";
		String[] attNameArray = attName.split(",");	        	
		double yDisplacment = -5;
		Symbolizer[] symbArray = new Symbolizer[attNameArray.length];
		for (int n=0; n<attNameArray.length; n++) {
			TextSymbolizer ts = sb.createTextSymbolizer(
					Color.GREEN.darker(), BaseMapManager.GT_FONT_ARRAY[0], attNameArray[n]);
			ts.setHalo(sb.createHalo(new Color(10, 10, 10), .7, 2.2));
			ts.setLabelPlacement(sb.createPointPlacement(0.0, 0.0, 5.0, yDisplacment, 0));
			yDisplacment += 10;    		
			symbArray[n] = ts;
		}

		style.addFeatureTypeStyle(sb.createFeatureTypeStyle(symbArray, 
				BaseMapStyleInfo.NO_MIN_SCALE, BaseMapStyleInfo.NO_MAX_SCALE));

		return style;
	}

	public static Style getDefaultSelectedPolygonStyle() throws IllegalFilterException {

		StyleBuilder sb = new StyleBuilder();
		LineSymbolizer symb = sb.createLineSymbolizer(Color.YELLOW, 3.0);
		return sb.createStyle(symb);
	}

}
