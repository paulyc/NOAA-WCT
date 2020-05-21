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

package gov.noaa.ncdc.nexradiv.legend;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;
 
public class WCTLegendPanel extends JNXLegend {       

   private boolean usingHeader = false;
   
   private Image logo = null;
   private Image legendImage;
   
   private String[] dpaLabel = // must have same size as color array for dpa 
      {"< 0.10", "0.10 - 0.25", "0.25 - 0.50", "0.50 - 0.75",
       "0.75 - 1.00", "1.00 - 1.50", "1.50 - 2.00", "2.00 - 2.50",
       "2.50 - 3.00", "3.00 - 4.00", "> 4.00"};
   
   public WCTLegendPanel() {
      
      try {
         java.net.URL imageURL = WCTLegendPanel.class.getResource("noaalogo.gif");
         if (imageURL != null) {
             this.logo = Toolkit.getDefaultToolkit().createImage(imageURL);
         } else {
             System.err.println("Splash image not found");
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      
   }
   
   
   /**
    * Resets the legend image from the provided image producer.  This should be called if legend size changes, etc... 
    * @param legend
    * @throws Exception 
    */
   public void setLegendImage(CategoryLegendImageProducer legend) throws Exception {
//       this.legendImage = getLegendImage(legend);
	   this.legendImage = legend.createLargeLegendImage(getPreferredSize());
       super.repaint();      //clears the background
       repaint();
      
   }

   
   public void setIsUsingHeader(boolean usingHeader) {
      this.usingHeader = usingHeader;
   }
   
   public boolean isUsingHeader() {
      return usingHeader;
   }
   
   public void paintComponent(Graphics g) {
      super.paintComponent(g);      //clears the background

      if (legendImage != null) {
          g.setColor(Color.WHITE);
          g.fillRect(0, 0, legendImage.getWidth(this), legendImage.getHeight(this));
          g.drawImage(legendImage, 0, 0, null);
      }
      
      
   } // END paintComponent        
   
   @Override
   public java.awt.Dimension getPreferredSize() {
      return new java.awt.Dimension(200, getHeight());
   }
   
   
            
} // END class
