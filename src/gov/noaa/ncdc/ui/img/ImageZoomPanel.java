package gov.noaa.ncdc.ui.img;

import java.awt.BorderLayout;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mortennobel.imagescaling.ResampleOp;

public class ImageZoomPanel extends JPanel {

	BufferedImage bimage = null;
	JLabel label = new JLabel();
	double zoom = 1.0;
	
	public ImageZoomPanel() {
		init();
	}
	
	
	
	private void init() {

        this.setLayout(new BorderLayout());
        
        // display image as icon 
//        Icon imageIcon = new ImageIcon(inputImage);
//        label = new JLabel( imageIcon );
        this.add(label, BorderLayout.CENTER);
        
        this.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                int notches = e.getWheelRotation();
                double temp = zoom - (notches * 0.2);
                // minimum zoom factor is 1.0
                temp = Math.max(temp, 0.1);
                if (temp != zoom) {
                    zoom = temp;
                    resizeImage();
                }
            }
        });
	}
	
	
	public void setImage(URL imageURL) throws IOException {
		bimage = ImageIO.read(imageURL);
		resizeImage();
	}
	
    
    public void resizeImage() {
    	System.out.println(zoom);
    	ResampleOp  resampleOp = new ResampleOp((int)(bimage.getWidth()*zoom), (int)(bimage.getHeight()*zoom));
    	BufferedImage resizedIcon = resampleOp.filter(bimage, null);
    	Icon imageIcon = new ImageIcon(resizedIcon);
    	label.setIcon(imageIcon);
    }

	
}
