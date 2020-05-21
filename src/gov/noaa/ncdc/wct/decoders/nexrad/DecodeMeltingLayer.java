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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.ct.MathTransform;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.pt.CoordinatePoint;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.MaxGeographicExtent;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.StreamingProcessException;

/**
 * Decodes NHI NEXRAD Level-III Hail Index alphanumeric product.
 *  
 * From 2620003J.pdf 19.1:
 *  
 * "This product shall provide, for each storm cell identified by the 
 * Storm Cell Identification and Tracking algorithm, the Probability 
 * of Hail, the Probability of Severe Hail, and the Maximum Expected 
 * Hail Size. The hail probabilities and size shown for each storm cell 
 * shall be generated by the Hail Algorithm. This product shall be 
 * produced in a tabular format of alphanumeric values, as a stand alone 
 * graphic product, and in a format for generating graphic overlays to 
 * other products. This product shall include a standard set of annotations. 
 * Upon user request, all site adaptable parameters identified as inputs 
 * to the algorithm(s) used to generate data for this product shall be 
 * available at the alphanumeric display."
 * 
 * @author steve.ansari
 * @created April 14, 2015
 */
public class DecodeMeltingLayer implements DecodeL3Alpha {

    private static final Logger logger = Logger.getLogger(DecodeHail.class.getName());

    private FeatureCollection emptyFc = FeatureCollections.newCollection();
    
    private String[] metaLabelString = new String[3];
    private FeatureCollection fc = FeatureCollections.newCollection();
    private FeatureType schema = null;
    private GeometryFactory geoFactory = new GeometryFactory();
    private java.awt.geom.Rectangle2D.Double wsrBounds;
    private DecodeL3Header header;
    private String[] supplementalData = new String[2];
    private WCTProjections nexradProjection = new WCTProjections();
    private MathTransform nexradTransform;
    private String datetime;

    private HashMap<String, Object> decodeHints = new HashMap<String, Object>();


    /**
     * Constructor
     * 
     * @param header
     *            Description of the Parameter
     * @throws IOException 
     */
    public DecodeMeltingLayer(DecodeL3Header header) throws DecodeException, IOException {
        this.header = header;
        this.wsrBounds = getExtent();
        decodeData();
    }

    /**
     * Returns the feature types used for these features
     * 
     * @return The featureType value
     */
    public FeatureType[] getFeatureTypes() {
        return new FeatureType[] {schema};
    }

    /**
     * Returns the LineFeatureType -- Always null in this case
     * 
     * @return Always null for this alphanumeric product
     */
    public FeatureType getLineFeatureType() {
        return null;
    }

    /**
     * Returns Rectangle.Double Bounds for the NEXRAD Site calculated during
     * decode. (unique to product) Could be 248, 124 or 32 nmi.
     * 
     * @return The nexradExtent value
     */
    public java.awt.geom.Rectangle2D.Double getNexradExtent() {
        return wsrBounds;
    }

    /**
     * Returns default display symbol type
     * 
     * @return The symbol type
     */
    public String getDefaultSymbol() {
        return org.geotools.styling.StyleBuilder.MARK_TRIANGLE;
    }

    /**
     * Returns the specified supplemental text data (index=1 == Block2, index2 ==
     * Block3, etc...)
     */
    public String getSupplementalData(int index) {
        if (supplementalData[index] != null) {
            return supplementalData[index];
        }
        else {
            return new String("NO DATA");
        }
    }

    /** 
     * Returns the supplemental text data array  
     */
    public String[] getSupplementalDataArray() {
        return supplementalData;
    }

    public String getMetaLabel(int index) {
        if (metaLabelString[index] == null) {
            return "";
        }
        else {
            return metaLabelString[index];
        }
    }

    private void makeMetaLabelStrings() {

        FeatureIterator fi = fc.features();
        if (fi.hasNext()) { // only use first and thus strongest reading
            Feature f = fi.next();
            
//            String id = f.getAttribute("color").toString().trim();
//            String prob = f.getAttribute("colorcode").toString().trim();
//            
//            metaLabelString[0] = "COLOR: " + id;
//            metaLabelString[1] = "CONTOUR: " + prob + (prob.equals("N/A") ? "" : "%");
            
            metaLabelString[0] = "ELEV ANGLE: " + WCTUtils.DECFMT_0D00.format((double) ((DecodeL3Header)header).getProductSpecificValue(2) / 10.0) + " �";
            
        }
        else {
            metaLabelString[0] = "NO DATA PRESENT";
        }

    }







//    @Override
    public Map<String, Object> getDecodeHints() {
        return decodeHints;
    }

//    @Override
    public void setDecodeHint(String hintKey, Object hintValue)
    throws DecodeHintNotSupportedException {
        throw new DecodeHintNotSupportedException("DecodeHail", hintKey, decodeHints);

    }


    /**
     * Decodes data and stores with in-memory FeatureCollection
     * @return
     * @throws DecodeException
     */
//    @Override
    public void decodeData() throws DecodeException, IOException {
        fc.clear();

        StreamingProcess process = new StreamingProcess() {
            public void addFeature(Feature feature)
            throws StreamingProcessException {
                fc.add(feature);
            }
            public void close() throws StreamingProcessException {
                logger.info("STREAMING PROCESS close() ::: fc.size() = "+fc.size());
            }    		
        };

        decodeData(new StreamingProcess[] { process } ); 	
    }


    @Override
    public void decodeData(final StreamingProcess[] processArray)
    throws DecodeException, IOException {

        decodeData(processArray, true);

    }

    @Override
    public void decodeData(final StreamingProcess[] processArray, final boolean autoClose)
    throws DecodeException, IOException {


        try {
            // Use Geotools Proj4 implementation to get MathTransform object
            nexradTransform = nexradProjection.getRadarTransform(header);
        } catch (Exception e) {
            throw new DecodeException("PROJECTION TRANSFORM ERROR", header.getDataURL());
        }

        datetime = header.getDate() + header.getHourString() + header.getMinuteString() + header.getSecondString();

        // Set up attribute table
        try {
            AttributeType geom = AttributeTypeFactory.newAttributeType("geom", Polygon.class);
            AttributeType wsrid = AttributeTypeFactory.newAttributeType("wsrid", String.class, true, 5);
            AttributeType datetime = AttributeTypeFactory.newAttributeType("datetime", String.class, true, 15);
            AttributeType colorCode = AttributeTypeFactory.newAttributeType("colorCode", Integer.class, true, 3);
            AttributeType contourValue = AttributeTypeFactory.newAttributeType("contourVal", Integer.class, true, 3);
            AttributeType[] attTypes = { geom, wsrid, datetime, colorCode, contourValue };
            schema = FeatureTypeFactory.newFeatureType(attTypes, "Hail Data");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Reset feature index counter
        int geoIndex = 0;

        // Initiate binary buffered read
        try {

            ArrayList<Coordinate> coords = new ArrayList<Coordinate>();

            
            
            // Decode the text blocks (block 2 and 3)
            PacketCodeListener pcl = new PacketCodeListener() {

				int colorCode = -1; // initial value
				int contourValue = -1;
	            ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
	            
	            int numCoords = -1;
            	int geoIndex = 0;
            	
            	@Override
				public void processPacket(String packetHexCode, String contents) {
					if (packetHexCode.equalsIgnoreCase("0802")) {
//						System.out.println("processPacket: "+contents);
						// check if this indicates a new contour so we can finish the previous contour
//						if (colorCode >= 0) {
//							finishContourFeature();
//						}
						
						
						String[] cols = contents.split(",");
						colorCode = Integer.parseInt(cols[0]);
						contourValue = Integer.parseInt(cols[1]);
						
					}
					else if (packetHexCode.equalsIgnoreCase("0E03")) {
//						System.out.println("processPacket: "+contents);
						
						try {
						
						String[] cols = contents.split(",");
						if (cols.length == 3) {
                            double[] geoXY = (nexradTransform.transform(new CoordinatePoint(
                            		Double.parseDouble(cols[0])*1000.0/4.0, Double.parseDouble(cols[1])*1000.0/4.0), null)).getCoordinates();
//                            coords.add(new Coordinate(geoXY[0], geoXY[1]));
                            numCoords = Integer.parseInt(cols[2])/2;
						}
						if (cols.length == 4) {
                            double[] geoXY = (nexradTransform.transform(new CoordinatePoint(
                            		Double.parseDouble(cols[0])*1000.0/4.0, Double.parseDouble(cols[1])*1000.0/4.0), null)).getCoordinates();
                            coords.add(new Coordinate(geoXY[0], geoXY[1]));

                            if (coords.size() == numCoords) {
                            	finishContourFeature();
                            }

                            double[] geoXY2 = (nexradTransform.transform(new CoordinatePoint(
                            		Double.parseDouble(cols[2])*1000.0/4.0, Double.parseDouble(cols[3])*1000.0/4.0), null)).getCoordinates();
                            
                                                      
                            
                            coords.add(new Coordinate(geoXY2[0], geoXY2[1]));
//                            System.out.println(coords.size()+"/"+numCoords);
                            
                            if (coords.size() == numCoords) {
                            	finishContourFeature();
                            }
						}
						
						
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
				}
				@Override
				public void processPacket(int packetCode, String contents) {
				}
				
				/**
				 * Close last contour feature to polygon
				 */
				@Override
				public void finishPacket() {
					finishContourFeature();
				}
				
				public void	finishContourFeature() {
					
					// close the ring by adding the first coord to the end
					coords.add(coords.get(0));
					
//					System.out.println("colorCode: "+colorCode+" contour:"+contourValue+" coords:"+coords.toString());
					
					try {
			          // create the feature
                    Feature feature = schema.create(
                            new Object[]{
                            		geoFactory.createPolygon(
                            				geoFactory.createLinearRing((Coordinate[])(coords.toArray(new Coordinate[coords.size()]))),
                            				null),
                                    header.getICAO(),
                                    datetime,
                                    colorCode,
                                    contourValue
                            },
                            new Integer(geoIndex++).toString());
                    
//                  // add to streaming processes
                  for (int s=0; s<processArray.length; s++) {
                      processArray[s].addFeature(feature);
                  }
                  logger.info("ADDED FEATURE: "+feature);
                    
                  coords.clear();
                    
					} catch (Exception e) {
						e.printStackTrace();
					}

				};
			};
			
            DecodeL3AlphaGeneric decoder = new DecodeL3AlphaGeneric();
            decoder.addPacketListener(pcl);
            decoder.decode(header);

//            logger.info("----------- VERSION: "+header.getVersion()+" ------------ \n");
//            logger.info("----------- BLOCK 1 ----------- \n"+decoder.getBlock1Text());
//            logger.info("----------- BLOCK 2 ----------- \n"+decoder.getBlock2Text());
//            logger.info("----------- BLOCK 3 ----------- \n"+decoder.getBlock3Text());
//            
//            System.out.println("----------- VERSION: "+header.getVersion()+" ------------ \n");
//            System.out.println("----------- BLOCK 1 ----------- \n"+decoder.getBlock1Text());
//            System.out.println("----------- BLOCK 2 ----------- \n"+decoder.getBlock2Text());
//            System.out.println("----------- BLOCK 3 ----------- \n"+decoder.getBlock3Text());
            
//
//            // Extract values and add to feature collection
//            for (int n = 0; n < ids.size(); n++) {
//                try {
//                    // create the feature
//                    Feature feature = schema.create(
//                            new Object[]{
//                                    geoFactory.createPoint((Coordinate) coords.get(n)),
//                                    header.getICAO(),
//                                    datetime,
//                                    (Double) lats.get(n),
//                                    (Double) lons.get(n),
//                                    ((String) ids.get(n)).trim(),
//                                    (Double) ranges.get(n),
//                                    (Double) azims.get(n),
//                                    ((String) sevprobs.get(n)).trim(),
//                                    ((String) probs.get(n)).trim(),
//                                    ((String) maxsizes.get(n)).trim()
//                            },
//                            new Integer(geoIndex++).toString());
//
//                    // add to streaming processes
//                    for (int s=0; s<processArray.length; s++) {
//                        processArray[s].addFeature(feature);
//                    }
//                    //logger.info("ADDED FEATURE: "+feature);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }


            makeMetaLabelStrings();

        } catch (Exception e) {
            e.printStackTrace();
            throw new DecodeException("DECODE EXCEPTION IN HAIL FILE - CODE 1 ", header.getDataURL());
        }
    }

    /**
     * Gets the features attribute of the DecodeHail object
     * 
     * @return The features value
     */
    public FeatureCollection getFeatures() {
        return emptyFc;
//    	return fc;
    }

    /**
     * Gets the line features attribute of the DecodeHail object
     * 
     * @return The features value
     */
    public FeatureCollection getLineFeatures() {
        return fc;
    }

    private java.awt.geom.Rectangle2D.Double getExtent() {
        return (MaxGeographicExtent.getNexradExtent(header.getLat(), header.getLon()));
    }

    public DecodeL3Header getHeader() {
    	return header;
    }
}
