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
import com.vividsolutions.jts.geom.Point;

import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.MaxGeographicExtent;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.StreamingProcessException;

/**
 *  Decodes NME NEXRAD Level-III legacy Mesocyclone alphanumeric product.
 *  
 *  From 2620003J.pdf 20.1: 
 *  
 *  "The Mesocyclone (M) product shall provide information about identified shear 
 *  and mesocyclone features. This product shall be generated from the output of 
 *  the Legacy Mesocyclone Detection Algorithm. This product shall be generated in 
 *  a format that can be used to generate an alphanumeric tabular display for an 
 *  identified feature or all simultaneously, a graphic display or a graphic overlay 
 *  to other products. This product shall be updated once per volume scan time. If 
 *  on a particular volume scan there is no output from the Legacy Mesocyclone 
 *  Detection Algorithm (i.e., no features of any type are identified), a version of 
 *  the product shall be produced that exhibits the negative condition. This product 
 *  shall include annotations for the product name, radar ID, date and time of volume 
 *  scan, radar position, radar elevation above MSL, and radar operational mode. Upon 
 *  user request, all site adaptable parameters identified as input to the algorithm(s) 
 *  used to generate data for this product shall be available at the alphanumeric display."
 *
 * @author    steve.ansari
 */
public class DecodeMeso implements DecodeL3Alpha {

    private static final Logger logger = Logger.getLogger(DecodeMeso.class.getName());

    private String[] metaLabelString = new String[3];
    private FeatureCollection features = FeatureCollections.newCollection();
    private FeatureType schema = null;
    private GeometryFactory geoFactory = new GeometryFactory();
    private java.awt.geom.Rectangle2D.Double wsrBounds;

    private HashMap<String, Object> decodeHints = new HashMap<String, Object>();

    private DecodeL3Header header;
    private String[] supplementalData = new String[2];

    private WCTProjections nexradProjection = new WCTProjections();
    private MathTransform nexradTransform;

    private String datetime;


    /**
     * Constructor
     *
     * @param  header                     Description of the Parameter
     * @exception  DecodeException  Description of the Exception
     * @throws IOException 
     */
    public DecodeMeso(DecodeL3Header header) throws DecodeException, IOException {
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
     * @return    Always null for this alphanumeric product
     */
    public FeatureType getLineFeatureType() {
        return null;
    }



    /**
     * Returns Rectangle.Double Bounds for the NEXRAD Site calculated during decode. (unique to product)
     * Could be 248, 124 or 32 nmi.
     *
     * @return    The nexradExtent value
     */
    public java.awt.geom.Rectangle2D.Double getNexradExtent() {
        return wsrBounds;
    }


    /**
     * Returns default display symbol type
     *
     * @return    The symbol type
     */
    public String getDefaultSymbol() {
        return org.geotools.styling.StyleBuilder.MARK_CIRCLE;
    }


    /** 
     * Returns the specified supplemental text data (index=1 == Block2, index2 == Block3, etc...)  
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
     *  Gets the metaLabel attribute of the DecodeMeso object
     *
     * @param  index  Description of the Parameter
     * @return        The metaLabel value
     */
    public String getMetaLabel(int index) {
        if (metaLabelString[index] == null) {
            return "";
        }
        else {
            return metaLabelString[index];
        }
    }

    /** 
     * Returns the supplemental text data array  
     */
    public String[] getSupplementalDataArray() {
        return supplementalData;
    }


    /**
     *  Description of the Method
     */
    private void makeMetaLabelStrings() {

        FeatureIterator fi = features.features();
        if (fi.hasNext()) {
            // only use first and thus strongest reading
            Feature f = fi.next();
            metaLabelString[0] = "MAX ID: " + f.getAttribute("id").toString().trim();
            metaLabelString[1] = "BASE/TOP: " + f.getAttribute("basehgt").toString().trim() + "/" +
            f.getAttribute("tophgt").toString().trim() + " (kft)";
            metaLabelString[2] = "SHEAR: " + f.getAttribute("shear").toString().trim() + " (E-3/s)";
        }
        else {
            metaLabelString[0] = "NO MESO PRESENT";
        }

    }



//    @Override
    public Map<String, Object> getDecodeHints() {
        return decodeHints;
    }

//    @Override
    public void setDecodeHint(String hintKey, Object hintValue)
    throws DecodeHintNotSupportedException {
        throw new DecodeHintNotSupportedException("DecodeMeso", hintKey, decodeHints);

    }


    /**
     * Decodes data and stores with in-memory FeatureCollection
     * @return
     * @throws DecodeException
     */
//    @Override
    public void decodeData() throws DecodeException, IOException {
        features.clear();

        StreamingProcess process = new StreamingProcess() {
            public void addFeature(Feature feature)
            throws StreamingProcessException {
                features.add(feature);
            }
            public void close() throws StreamingProcessException {
                logger.info("STREAMING PROCESS close() ::: features.size() = "+features.size());
            }           
        };

        decodeData(new StreamingProcess[] { process } );    
    }


//    @Override
    public void decodeData(StreamingProcess[] processArray)
    throws DecodeException, IOException {

        decodeData(processArray, true);

    }





//    @Override
    public void decodeData(StreamingProcess[] processArray, boolean autoClose)
    throws DecodeException, IOException {


        try {
            // Use Geotools Proj4 implementation to get MathTransform object
            nexradTransform = nexradProjection.getRadarTransform(header);
        } catch (Exception e) {
            throw new DecodeException("PROJECTION TRANSFORM ERROR", header.getDataURL());
        }

        datetime = header.getDate()+header.getHourString()+header.getMinuteString()+header.getSecondString();

        // Set up attribute table
        try {
            AttributeType geom = AttributeTypeFactory.newAttributeType("geom", Point.class);
            AttributeType wsrid = AttributeTypeFactory.newAttributeType("wsrid", String.class, true, 5);
            AttributeType datetime = AttributeTypeFactory.newAttributeType("datetime", String.class, true, 15);
            AttributeType lat = AttributeTypeFactory.newAttributeType("lat", Double.class, true, 10);
            AttributeType lon = AttributeTypeFactory.newAttributeType("lon", Double.class, true, 10);
            AttributeType id = AttributeTypeFactory.newAttributeType("id", String.class, true, 3);
            AttributeType type = AttributeTypeFactory.newAttributeType("type", String.class, true, 5);
            AttributeType range = AttributeTypeFactory.newAttributeType("range", Double.class, true, 7);
            AttributeType azim = AttributeTypeFactory.newAttributeType("azim", Double.class, true, 7);
            AttributeType basehgt = AttributeTypeFactory.newAttributeType("basehgt", String.class, true, 5);
            AttributeType tophgt = AttributeTypeFactory.newAttributeType("tophgt", String.class, true, 5);
            AttributeType height = AttributeTypeFactory.newAttributeType("height", String.class, true, 5);
            AttributeType radius = AttributeTypeFactory.newAttributeType("radius", String.class, true, 5);
            AttributeType azdia = AttributeTypeFactory.newAttributeType("azdia", String.class, true, 5);
            AttributeType shear = AttributeTypeFactory.newAttributeType("shear", String.class, true, 5);
            AttributeType[] attTypes = {geom, wsrid, datetime, lat, lon, id, type, range, azim, basehgt, tophgt, height, radius, azdia, shear};
            schema = FeatureTypeFactory.newFeatureType(attTypes, "Mesocyclone Data");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Reset index counter
        int geoIndex = 0;

        // Initiate binary buffered read
        try {

            // Decode the text blocks (block 2 and 3)
            DecodeL3AlphaGeneric decoder = new DecodeL3AlphaGeneric();
            decoder.decode(header);

            logger.info("----------- VERSION: "+header.getVersion()+" ------------ \n");
            logger.info("----------- BLOCK 2 ----------- \n"+decoder.getBlock2Text());
            logger.info("----------- BLOCK 3 ----------- \n"+decoder.getBlock3Text());



            // Build text for block 2 data           
            StringBuffer sb = new StringBuffer();

            // Lets make a custom legend for this block
            sb.append("  MESOCYCLONE SUPPLEMENTAL DATA 1\n\n");
            sb.append("  ABBREVIATIONS:\n");
            sb.append("  AZ    = Azimuth Angle From Radar \n");
            sb.append("          (In Degrees where 0 deg = North, 90 = East, 180 = South, etc...)\n");
            sb.append("  RAN   = Range (Distance) From Radar (In Nautical Miles (nmi))\n");
            sb.append("  BASE  = Elevation of Mesocyclone Base (kft)\n");
            sb.append("  TOP   = Elevation of Mesocyclone Top (kft)\n");
            sb.append("  RAD   = Radius of Mesocyclone (nmi)\n");
            sb.append("  AZDIA = Radius of Mesocyclone (nmi)\n\n");

            sb.append(decoder.getBlock2Text());
            supplementalData[0] = sb.toString();
            sb.append("\n\n");

            // Build text for block 3 data
            sb = new StringBuffer();
            sb.append("  MESOCYCLONE SUPPLEMENTAL DATA 2\n\n");                  
            sb.append(decoder.getBlock3Text());
            sb.append("\n\n");
            supplementalData[1] = sb.toString();

            String block3Text = decoder.getBlock3Text();
            String[] lines = block3Text.split("\n");

            if (lines.length == 0) {
                metaLabelString[0] = "NO MESO PRESENT";
                return;      
            }

            if (header.getVersion() > 1.0) {
                throw new DecodeException("UNKNOWN NEXRAD MESO FILE VERSION: " + header.getVersion(), header
                        .getDataURL());
            }

            for (int n = 0; n < lines.length; n++) {

                String str = lines[n];

                // advance past empty lines
                if (str.trim().length() == 0) {
                    continue;
                }

                //  0         1         2         3         4         5         6         7
                //  012345678901234567890123456789012345678901234567890123456789012345678901234567
                //     1 - 28    MESO       0    5.5    25.1    83/ 61    5.5    1.3   2.9     30   (VERSION 0)
                //       1   -  Q4   MESO        5.5    22.6   222/ 60    5.5    2.2   2.5     22   (VERSION 1)

                String featureID = null;
                String stormType = null;
                String baseKFT;
                String topKFT;
                String hgt;
                String diamRad;
                String diamAz;
                String shear;


                int type;
                // Check for a string of storm data of various types (changes with date of file)
                if (str.charAt(5) == '-' && str.charAt(46) == '/') {
                    type = 0;
                    featureID = str.substring(7, 9);
                    stormType = str.substring(12, 21);
                }
                else if (str.charAt(9) == '-' && str.charAt(46) == '/') {
                    type = 1;
                    featureID = str.substring(12, 14);
                    stormType = str.substring(16, 25);
                }
                else {
                    type = -999;
                }


                if (type >= 0) {

                    logger.info("ADDING: "+featureID);
                    
                    baseKFT = str.substring(26, 32).trim();
                    topKFT = str.substring(33, 40).trim();
                    double azim = Double.parseDouble(str.substring(43, 46));
                    double range = Double.parseDouble(str.substring(47, 50));
                    hgt = str.substring(51, 57).trim();
                    diamRad = str.substring(58, 64).trim();
                    diamAz = str.substring(65, 70).trim();
                    shear = str.substring(71, 77).trim();

                    // Correct for an azim of 0
                    if (azim == 0.0 || azim == 180.0) {
                        azim += 0.000001;
                    }
                    // Convert from nautical mi to lat/lon
                    double[] geoXY = (nexradTransform.transform(
                            new CoordinatePoint(range * Math.sin(Math.toRadians(azim)) * 1852.0, range
                                    * Math.cos(Math.toRadians(azim)) * 1852.0), null)).getCoordinates();

                    try {
                        // create the feature
                        Feature feature = schema.create(new Object[] {
                                geoFactory.createPoint(new Coordinate(geoXY[0], geoXY[1])), header.getICAO(), datetime,
                                new Double(geoXY[1]), new Double(geoXY[0]), featureID, stormType, new Double(range),
                                new Double(azim), baseKFT, topKFT, hgt, diamRad, diamAz, shear }, new Integer(
                                        geoIndex++).toString());
                        // add to collection
                        features.add(feature);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            makeMetaLabelStrings();
            return;
        } // END try
        catch (Exception e) {
            e.printStackTrace();
            throw new DecodeException("DECODE EXCEPTION IN MESO FILE", header.getDataURL());
        }
    } // END METHOD decodeData




    /**
     *  Gets the features attribute of the DecodeHail object
     *
     * @return    The features value
     */
    public FeatureCollection getFeatures() {
        return features;
    }


    /**
     *  Gets the line features attribute of the DecodeMeso object
     *
     * @return    The features value
     */
    public FeatureCollection getLineFeatures() {
        return null;
    }


    /**
     * Implementation of NexradDecoder
     *
     * @param  features  The new features value
     */
    public void setFeatures(FeatureCollection features) {
        this.features = features;
    }



    private java.awt.geom.Rectangle2D.Double getExtent() {
        return (MaxGeographicExtent.getNexradExtent(header.getLat(), header.getLon()));
    }


    public DecodeL3Header getHeader() {
    	return header;
    }
}

