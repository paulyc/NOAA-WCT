package gov.noaa.ncdc.wct.ui;

import org.geotools.gc.GridCoverage;

import gov.noaa.ncdc.wct.io.FileScanner;

public class DecoderHelper {

    
    
    public GridCoverage getGridCoverage(FileScanner scannedFile, java.awt.geom.Rectangle2D.Double extent, int alpha) {
        
        GridCoverage gc = null;
        
//        if (scannedFile != null && scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED) {
//            
//        }
//        else if (scannedFile != null && scannedFile.getLastScanResult().getDataType() == SupportedDataType.GOES_SATELLITE_AREA_FORMAT) {
//            
//        }
//        else if (scannedFile != null && scannedFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
//            
//        }
//        
        return gc;
    }
}
