package steve.test.grass;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import steve.test.grass.GRASSGoogleEarthBrowser.ViewController;

public class GRASSViewer extends JFrame {

	public static void main(String[] args) {
		NativeInterface.open();
		GRASSViewer viewer = new GRASSViewer();
		
		
        System.out.println("mark 1");
        NativeInterface.runEventPump();
        System.out.println("mark 2");
	}
	
	
	public GRASSViewer() {
        SwingUtilities.invokeLater(new Runnable() {
           public void run() {
		
		createUI();
		setSize(600, 500);
		setVisible(true);
		
           }
        });
	}
	
	private void createUI() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
//		try {
//			GRASSImageServer grassImageServer = new GRASSImageServer(8765);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		GRASSGoogleEarthBrowser geBrowser = new GRASSGoogleEarthBrowser(8765, ViewController.GEOTOOLS, true, this, false);
		mainPanel.add(geBrowser, BorderLayout.CENTER);
		
		this.getContentPane().add(mainPanel);
	}
}
