package gov.noaa.ncdc.wct.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.ImageIcon;

import gov.noaa.ncdc.nexradiv.legend.CategoryLegendImageProducer;
import gov.noaa.ncdc.wct.WCTUtils;

public class NdmcDroughtMonitor implements WmsResource {
    
// 	https://gis.ncdc.noaa.gov/maps/proxy?wms=
//	   http://ndmc-001.unl.edu:8080/cgi-bin/mapserv.exe?
//	   map=/ms4w/apps/usdm/service/usdm_20170228_wms.map&SERVICE=WMS&VERSION=1.3.0
//	   &REQUEST=GetMap&LAYERS=usdm20170228&WIDTH=640&HEIGHT=480&crs=EPSG:3857
//	   &styles=default&format=image/png&bbox=-18367715.9809,1689200.13961,-6679169.4476,15538711.0963
	
    
	// https://gis.ncdc.noaa.gov/maps/proxy?wms=
	
    // defaults to first map
    private String yyMMdd = "20000104";
    private String name = "";
    private float zIndex = 0.05f;
    private Rectangle imageSize = new Rectangle(640, 480);
    private Image nidisLogo = null;
    
    
    public ArrayList<String> getDates() throws Exception {
        String startDate = "20000104";
        int interval = 7;
        int delay = 2;
        
        
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date curDate = new Date(new Date().getTime()-delay*24*60*60*1000);
        Date prodDate = WCTUtils.YYYYMMDD_LOCALTZ_FORMATTER.parse(startDate);
        
        ArrayList<String> dates = new ArrayList<String>();
        while (prodDate.before(curDate)) {
//            System.out.println(sdf.format(prodDate));
            dates.add(WCTUtils.YYYYMMDD_LOCALTZ_FORMATTER.format(prodDate));            
            prodDate = new Date(prodDate.getTime()+interval*24*60*60*1000);
        }
        return dates;
    }
    
    public void setYYMMDD(String yyMMdd) {
        this.yyMMdd = yyMMdd;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setZIndex(float zIndex) {
        this.zIndex = zIndex;
    }

    public void setImageSize(Rectangle imageSize) {
        this.imageSize = imageSize;
    }
    
    
    @Override
    public URL getWmsUrl(Rectangle2D bounds) throws MalformedURLException {
        
//        URL url = new URL("https://gis.ncdc.noaa.gov/maps/proxy?wms=http://torka.unl.edu:8080/cgi-bin/mapserv.exe?"+
//            "map=/ms4w/apps/dm/service/usdm"+yyMMdd+"_wms.map&"+
//            "SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&LAYERS=usdm"+yyMMdd+"&"+
//            "WIDTH="+(int)imageSize.getWidth()+"&HEIGHT="+(int)imageSize.getHeight()+
//            "&srs=EPSG:4326&styles=default&format=image/png&"+
//            "transparent=true&"+
//            "bbox="+bounds.getMinX()+","+bounds.getMinY()+","+
//            bounds.getMaxX()+","+bounds.getMaxY());

// 	https://gis.ncdc.noaa.gov/maps/proxy?wms=
// 	   http://ndmc-001.unl.edu:8080/cgi-bin/mapserv.exe?
// 	   map=/ms4w/apps/usdm/service/usdm_20170228_wms.map&SERVICE=WMS&VERSION=1.3.0
// 	   &REQUEST=GetMap&LAYERS=usdm20170228&WIDTH=640&HEIGHT=480&crs=EPSG:3857
// 	   &styles=default&format=image/png&bbox=-18367715.9809,1689200.13961,-6679169.4476,15538711.0963
 	

        URL url = new URL("https://gis.ncdc.noaa.gov/maps/proxy?wms=http://ndmc-001.unl.edu:8080/cgi-bin/mapserv.exe?"+
                "map=/ms4w/apps/usdm/service/usdm_"+yyMMdd+"_wms.map&SERVICE=WMS&VERSION=1.3.0"+
                "&REQUEST=GetMap&LAYERS=usdm"+yyMMdd+"&"+
                "WIDTH="+(int)imageSize.getWidth()+"&HEIGHT="+(int)imageSize.getHeight()+
                "&crs=EPSG:4326&styles=default&format=image/png&"+
                "transparent=true&"+
                "bbox="+bounds.getMinY()+","+bounds.getMinX()+","+
                bounds.getMaxY()+","+bounds.getMaxX());
        
        
        return url;
        
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public float getZIndex() {
        return zIndex;
    }
    
    @Override
    public Image getLegendImage() throws Exception {
//        return ImageIO.read(new URL(
//                "http://torka.unl.edu:8080/cgi-bin/mapserv.exe?"+
//                "map=/ms4w/apps/dm/service/usdm"+yyMMdd+"_wms.map&"+
//                "version=1.1.1&service=WMS&request=GetLegendGraphic&"+
//                "layer=USDM"+yyMMdd+"&format=image/png"));
        
        CategoryLegendImageProducer legendProducer = new CategoryLegendImageProducer();
        legendProducer.setBackgroundColor(new Color(255, 255, 255, 230));
        legendProducer.setDrawBorder(true);
        
        legendProducer.setCategoryColors(new Color[] {
                new Color(255, 255, 0),
                new Color(255, 211, 127),
                new Color(255, 170, 0),
                new Color(230, 0, 0),
                new Color(115, 0, 0)
            });
        legendProducer.setCategoryLabels(new String[] {
                "D0 Abnormally Dry",
                "D1 Drought - Moderate",
                "D2 Drought - Severe",
                "D3 Drought - Extreme",
                "D4 Drought - Exceptional"
            });
        
//        SimpleDateFormat sdfIn = new SimpleDateFormat("yyMMdd");
        SimpleDateFormat sdfOut = new SimpleDateFormat("MMM dd, yyyy");
        String dateString = sdfOut.format(WCTUtils.YYYYMMDD_LOCALTZ_FORMATTER.parse(yyMMdd));
        legendProducer.setLegendTitle(new String[] {
                "U.S. Drought Monitor",
                dateString
            });
        
        
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D)image.getGraphics();
        FontRenderContext fc = g.getFontRenderContext();
        int textHeight = (int) g.getFont().createGlyphVector(fc, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789").getVisualBounds().getHeight();
//        System.out.println("TEXT HEIGHT:  "+textHeight);
        
//        return legendProducer.createLargeLegendImage(new Dimension(225, 120));
        return legendProducer.createLargeLegendImage(new Dimension(225, 6*textHeight+55));
    }

    @Override
    public String getInfo() {
        return "<html>The Monitor is produced by a rotating group of authors from <br>" +
        		"the U.S. Department of Agriculture, the National Oceanic and Atmospheric <br>" +
        		"Administration, and the National Drought Mitigation Center. It incorporates <br>" +
        		"review from a group of 250 climatologists, extension agents, and others <br>" +
        		"across the nation. Each week the author revises the previous map based on <br>" +
        		"rain, snow and other events, observers' reports of how drought is affecting <br>" +
        		"crops, wildlife and other indicators. Authors balance conflicting data and <br>" +
        		"reports to come up with a new map every Wednesday afternoon. It is released <br>" +
        		"the following Thursday morning <br><br>" +
        		"For more information, please refer to http://www.drought.gov <html>.";
    }

    @Override
    public Image getResourceLogo() {

        if (nidisLogo == null) {
            URL imageURL = WCTViewer.class.getResource("/images/nidislogo.png");
            if (imageURL != null) {
                this.nidisLogo = new ImageIcon(imageURL).getImage();
            }
            else {
                System.err.println("NIDIS Logo image not found");
            }
        }
        return nidisLogo;
    }
    
    
    
}
