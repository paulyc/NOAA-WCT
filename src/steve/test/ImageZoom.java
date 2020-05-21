package steve.test;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.ResampleOp;

public class ImageZoom {

    private JFrame frmImageZoomIn;
//    private static final String inputImage = "C:/Users/steve.ansari/Downloads/EDDI_02mn_20161230.png"; // give image path here
    private JLabel label1 = null; 
    private JLabel label2 = null; 
    private JLabel label3 = null; 
    private JLabel label4 = null; 
    
    JScrollPane scrollPane1 = new JScrollPane();
    JScrollPane scrollPane2 = new JScrollPane();
    JScrollPane scrollPane3 = new JScrollPane();
    JScrollPane scrollPane4 = new JScrollPane();

    private double zoom = 1.0;  // zoom factor
    private BufferedImage image = null;
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ImageZoom window = new ImageZoom();
                    window.setSource(new File("C:/Users/steve.ansari/Downloads/EDDI_02mn_20161230.png").toURI().toURL());
                    window.frmImageZoomIn.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     * @throws IOException 
     */
    public ImageZoom() throws IOException {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     * @throws IOException 
     */
    private void initialize() throws IOException {
        frmImageZoomIn = new JFrame();
        frmImageZoomIn.setTitle("Image Zoom In and Zoom Out");
        frmImageZoomIn.setBounds(100, 100, 450, 300);
        frmImageZoomIn.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        
        
//        image = ImageIO.read(new File(inputImage));
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setLayout(new GridLayout(2, 2));
        
        // layout places scrollpanes as follows:
        //  1  2
        //  3  4

        frmImageZoomIn.getContentPane().add(panel, BorderLayout.CENTER);

        
        // display image as icon 
//        Icon imageIcon = new ImageIcon(inputImage);
        label1 = new JLabel();
        label2 = new JLabel();
        label3 = new JLabel();
        label4 = new JLabel();
        scrollPane1.add(label1);
        scrollPane2.add(label2);
        scrollPane3.add(label3);
        scrollPane4.add(label4);
        panel.add(scrollPane1);
        panel.add(scrollPane2);
        panel.add(scrollPane3);
        panel.add(scrollPane4);
        
        MouseWheelListener mhl = new MouseWheelListener() {
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
        };
        
        ChangeListener cl = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				System.out.println("Scrolling: "+((JViewport) evt.getSource()).getViewPosition());
				scrollPane1.getViewport().removeChangeListener(this);
				scrollPane2.getViewport().removeChangeListener(this);
				scrollPane3.getViewport().removeChangeListener(this);
				scrollPane4.getViewport().removeChangeListener(this);
				JViewport source = (JViewport) evt.getSource();
				scrollPane1.getViewport().setViewPosition(source.getViewPosition());
				scrollPane2.getViewport().setViewPosition(source.getViewPosition());
				scrollPane3.getViewport().setViewPosition(source.getViewPosition());
				scrollPane4.getViewport().setViewPosition(source.getViewPosition());
				scrollPane1.getViewport().addChangeListener(this);
				scrollPane2.getViewport().addChangeListener(this);
				scrollPane3.getViewport().addChangeListener(this);
				scrollPane4.getViewport().addChangeListener(this);
			}
		};
		scrollPane1.getViewport().addChangeListener(cl);
		scrollPane2.getViewport().addChangeListener(cl);
		scrollPane3.getViewport().addChangeListener(cl);
		scrollPane4.getViewport().addChangeListener(cl);
        
        scrollPane1.addMouseWheelListener(mhl);
        scrollPane2.addMouseWheelListener(mhl);
        scrollPane3.addMouseWheelListener(mhl);
        scrollPane4.addMouseWheelListener(mhl);
    }
    
    public void setZoom(double zoom) {
    	this.zoom = zoom;
    	resizeImage();
    }
    
    public void setSource(URL url) throws IOException {
    	image = ImageIO.read(url);
    	resizeImage();
    }
    
    public void resizeImage() {
           System.out.println(zoom);
           
           ResampleOp  resampleOp1 = new ResampleOp((int)(image.getWidth()*zoom), (int)(image.getHeight()*zoom));
           BufferedImage resizedIcon1 = resampleOp1.filter(image, null);
           Icon imageIcon1 = new ImageIcon(resizedIcon1);

//           MultiStepRescaleOp  resampleOp2 = new MultiStepRescaleOp((int)(image.getWidth()*zoom), (int)(image.getHeight()*zoom));
//           BufferedImage resizedIcon2 = resampleOp2.filter(image, null);
//           Icon imageIcon2 = new ImageIcon(resizedIcon2);

           ResampleOp  resampleOp3 = new ResampleOp((int)(image.getWidth()*zoom), (int)(image.getHeight()*zoom));
           resampleOp3.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Normal);
           BufferedImage resizedIcon3 = resampleOp3.filter(image, null);
           Icon imageIcon3 = new ImageIcon(resizedIcon3);

//           ResampleOp  resampleOp4 = new ResampleOp((int)(image.getWidth()*zoom), (int)(image.getHeight()*zoom));
//           resampleOp4.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.VerySharp);
//           BufferedImage resizedIcon4 = resampleOp4.filter(image, null);
//           Icon imageIcon4 = new ImageIcon(resizedIcon4);

           
           label1.setIcon(imageIcon1);
           label2.setIcon(imageIcon1);
           label3.setIcon(imageIcon3);
           label4.setIcon(imageIcon3);
           scrollPane1.setViewportView(label1);
           scrollPane2.setViewportView(label2);
           scrollPane3.setViewportView(label3);
           scrollPane4.setViewportView(label4);
        }


}