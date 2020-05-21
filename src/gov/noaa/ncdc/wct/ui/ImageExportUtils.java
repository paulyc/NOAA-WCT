package gov.noaa.ncdc.wct.ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

public class ImageExportUtils {

	public static final String DENSITY_UNITS_NO_UNITS = "00";
	public static final String DENSITY_UNITS_PIXELS_PER_INCH = "01";
	public static final String DENSITY_UNITS_PIXELS_PER_CM = "02";



	/**
	 * Save images with custom DPI.  Default Java DPI is 72.
	 * from: http://stackoverflow.com/questions/321736/how-to-set-dpi-information-in-an-image/4833697#4833697
	 *   and: http://stackoverflow.com/questions/39985175/how-to-increase-jpeg-image-ppi-or-dpi-in-java/39994920#39994920
	 *
	 * @param buffImage
	 * @param formatName  can only be jpeg or png
	 * @param output
	 * @param dpi
	 * @throws IOException
	 */
	public static void saveImage(BufferedImage buffImage, String formatName, File output, int dpi) throws IOException {
		
		if (! formatName.equals("png") && ! formatName.equals("jpeg")) {
			throw new IIOException("Only png and jpeg format names are supported for non-standard DPI");
		}
		
	    output.delete();

	    for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext();) {
	       ImageWriter writer = iw.next();
	       ImageWriteParam writeParam = writer.getDefaultWriteParam();
	       ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
	       IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
	       if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
	          continue;
	       }

	       if (formatName.equals("png")) {
	    	   setPngDPI(metadata, dpi);
	       }
	       else {
	    	   setJpegDPI(metadata, dpi);
	       }

	       final ImageOutputStream stream = ImageIO.createImageOutputStream(output);
	       try {
	          writer.setOutput(stream);
	          writer.write(metadata, new IIOImage(buffImage, null, metadata), writeParam);
	       } finally {
	          stream.close();
	       }
	       break;
	    }
	 }

	private static void setJpegDPI(IIOMetadata metadata, int dpi) throws IIOInvalidTreeException {
	    String metadataFormat = "javax_imageio_jpeg_image_1.0";
	    IIOMetadataNode root = new IIOMetadataNode(metadataFormat);
	    IIOMetadataNode jpegVariety = new IIOMetadataNode("JPEGvariety");
	    IIOMetadataNode markerSequence = new IIOMetadataNode("markerSequence");

	    IIOMetadataNode app0JFIF = new IIOMetadataNode("app0JFIF");
	    app0JFIF.setAttribute("majorVersion", "1");
	    app0JFIF.setAttribute("minorVersion", "2");
	    app0JFIF.setAttribute("thumbWidth", "0");
	    app0JFIF.setAttribute("thumbHeight", "0");
	    app0JFIF.setAttribute("resUnits", DENSITY_UNITS_PIXELS_PER_INCH);
	    app0JFIF.setAttribute("Xdensity", String.valueOf(dpi));
	    app0JFIF.setAttribute("Ydensity", String.valueOf(dpi));

	    root.appendChild(jpegVariety);
	    root.appendChild(markerSequence);
	    jpegVariety.appendChild(app0JFIF);

	    metadata.mergeTree(metadataFormat, root);
	}
	


	 private static void setPngDPI(IIOMetadata metadata, int dpi) throws IIOInvalidTreeException {

		 double inch_2_cm = 2.54;
	    // for PMG, it's dots per millimeter
	    double dotsPerMilli = 1.0 * dpi / 10 / inch_2_cm;

	    IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
	    horiz.setAttribute("value", Double.toString(dotsPerMilli));

	    IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
	    vert.setAttribute("value", Double.toString(dotsPerMilli));

	    IIOMetadataNode dim = new IIOMetadataNode("Dimension");
	    dim.appendChild(horiz);
	    dim.appendChild(vert);

	    IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
	    root.appendChild(dim);

	    metadata.mergeTree("javax_imageio_1.0", root);
	 }
}
