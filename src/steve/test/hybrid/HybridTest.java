package steve.test.hybrid;

import java.awt.geom.Rectangle2D;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import ftp.FtpBean;
import ftp.FtpException;
import ftp.FtpObserver;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.cdm.GridDatasetNativeRaster;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Nexrad;
import gov.noaa.ncdc.wct.decoders.nexrad.RadarHashtables;
import gov.noaa.ncdc.wct.decoders.nexrad.RadarHashtables.MaxRangeUnits;
import gov.noaa.ncdc.wct.decoders.nexrad.RadarHashtables.SearchFilter;
import gov.noaa.ncdc.wct.export.WCTExportNoDataException;
import gov.noaa.ncdc.wct.export.raster.RasterMathOpException;
import gov.noaa.ncdc.wct.export.raster.WCTMathOp;
import gov.noaa.ncdc.wct.export.raster.WCTRasterExport;
import gov.noaa.ncdc.wct.export.raster.WCTRasterExport.GeoTiffType;
import gov.noaa.ncdc.wct.export.raster.WCTRasterizer;
import gov.noaa.ncdc.wct.io.WCTTransfer;
import ucar.ma2.InvalidRangeException;

public class HybridTest {

	
//	private static String[] SITELIST = new String[] { "KDFX", "KEWX", "KGRK", "KHGX", "KLCH", "KPOE", "KSHV", "KLZK", "KLIX", "KFWS", "KMOB", "KEVX" };
	private static String[] SITELIST = new String[] { "KILN", "KJKL", "KGSP", "KCAE", "KMRX", "KFCX", "KRAX", "KAKQ", "KLTX", "KMHX", "KCLX", "KRLX" };
//	private static String[] SITELIST = new String[] { "KEAX", "KDMX", "KDVN", "KILX", "KLOT", "KMKX", "KARX", "KGRB", "KGRR", "KAPX", "KIWX", "KDTX" };
	private static Rectangle2D.Double MRMS_BOUNDS = new Rectangle2D.Double(-130, 20, 70, 35);
	private static File MRMS_CACHE_DIR = new File("C:\\work\\wct\\hybrid-tests\\nws-data-cache");
	
	public static void main(String[] args) {
		try {
//			processSimpleMosaic();
//			processMosaicWithHydrometeorFiltering();
//			buildSiteLookupGrid();
			new HybridTest().buildSiteLookupGridMultiThread();
			processMosaicWithHydrometeorAndDistanceFiltering();
			processMosaicWithHydrometeorFiltering();
//			processIndividual();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void processIndividual() throws DecodeException, IOException, WCTExportNoDataException, InvalidRangeException, RasterMathOpException {
		
		
		WCTRasterizer rasterizer = new WCTRasterizer(3500, 7000, -999.0f);
		// new Rectangle2D.Double(-180, -90, 360, 180)
		rasterizer.setBounds(MRMS_BOUNDS);        
        
        for (String site : SITELIST) {       	  
        	System.out.println("Processing: "+site);
            
            URL dataURL = new URL("ftp://anonymous:wct.ncdc.at.noaa.gov@tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.32dhr/SI."+site.toLowerCase()+"/sn.last");
            dataURL =  WCTTransfer.getURL(dataURL, null, true);
            DecodeL3Header header = new DecodeL3Header();
            header.decodeHeader(dataURL);
            
            DecodeL3Nexrad decoder = new DecodeL3Nexrad(header);
            decoder.decodeData(new StreamingProcess[] { rasterizer });
            
            WCTRasterExport rasterExport = new WCTRasterExport();
            rasterizer.setDateInMilliseconds(header.getScanMilliseconds());
            rasterExport.saveNetCDF(new File("C:\\work\\wct\\hybrid-tests\\dhr-3500x7000-"+site+".nc"), rasterizer);
            
            rasterizer.clearRaster();
            
            // HHC
            URL dataURLHHC = new URL("ftp://anonymous:wct.ncdc.at.noaa.gov@tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.177hh/SI."+site.toLowerCase()+"/sn.last");
            dataURLHHC =  WCTTransfer.getURL(dataURLHHC, null, true);
            DecodeL3Header headerHHC = new DecodeL3Header();
            headerHHC.decodeHeader(dataURLHHC);
            
            DecodeL3Nexrad decoderHHC = new DecodeL3Nexrad(headerHHC);
            decoderHHC.decodeData(new StreamingProcess[] { rasterizer });
            
            rasterizer.setDateInMilliseconds(headerHHC.getScanMilliseconds());            
            rasterExport.saveNetCDF(new File("C:\\work\\wct\\hybrid-tests\\hhc-3500x7000-"+site+".nc"), rasterizer);
            
            rasterizer.clearRaster();
        }
        
      
        		
	}
	

	private static void processSimpleMosaic() throws DecodeException, IOException, WCTExportNoDataException, InvalidRangeException, RasterMathOpException {
		
		
		WCTRasterizer rasterizer = new WCTRasterizer(3500, 7000, -999.0f);
		// new Rectangle2D.Double(-180, -90, 360, 180)
		rasterizer.setBounds(MRMS_BOUNDS);        

		WCTRasterizer tmpRasterizer = new WCTRasterizer(3500, 7000, -999.0f);
		// new Rectangle2D.Double(-180, -90, 360, 180)
		tmpRasterizer.setBounds(MRMS_BOUNDS);
        
        for (String site : SITELIST) {       	  
        	System.out.println("Processing: "+site);
            
            URL dataURL = new URL("ftp://anonymous:wct.ncdc.at.noaa.gov@tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.32dhr/SI."+site.toLowerCase()+"/sn.last");
            dataURL =  WCTTransfer.getURL(dataURL, null, true);
            DecodeL3Header header = new DecodeL3Header();
            header.decodeHeader(dataURL);
            
            DecodeL3Nexrad decoder = new DecodeL3Nexrad(header);
            decoder.decodeData(new StreamingProcess[] { tmpRasterizer });
            
            // assimilate into the mosaic keeping the max values
            WCTMathOp.max(rasterizer, rasterizer, tmpRasterizer, tmpRasterizer.getNoDataValue());
            tmpRasterizer.clearRaster();
        }
        
      
        WCTRasterExport rasterExport = new WCTRasterExport();
        rasterizer.setDateInMilliseconds(new Date().getTime());
        rasterExport.saveGeoTIFF(new File("C:\\work\\wct\\hybrid-tests\\dhr-3500x7000.tif"), rasterizer, GeoTiffType.TYPE_32_BIT);
        		
	}
	
	
	
	private static void processMosaicWithHydrometeorFiltering() throws Exception {
		
		
		WCTRasterizer rasterizer = new WCTRasterizer(3500, 7000, -999.0f);
		// new Rectangle2D.Double(-180, -90, 360, 180)
		rasterizer.setBounds(MRMS_BOUNDS);        

		WCTRasterizer tmpRasterizer = new WCTRasterizer(3500, 7000, -999.0f);
		// new Rectangle2D.Double(-180, -90, 360, 180)
		tmpRasterizer.setBounds(MRMS_BOUNDS);		

		WCTRasterizer hhcRasterizer = new WCTRasterizer(3500, 7000, -999.0f);
		// new Rectangle2D.Double(-180, -90, 360, 180)
		hhcRasterizer.setBounds(MRMS_BOUNDS);
		        
		MRMS_CACHE_DIR.mkdirs();
		cacheRealTimeFilesFromNWS(MRMS_CACHE_DIR);
		
        for (String site : SITELIST) {       	  
        	System.out.println("Processing: "+site);
            
        	// DHR
//            URL dataURL = new URL("ftp://anonymous:wct.ncdc.at.noaa.gov@tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.32dhr/SI."+site.toLowerCase()+"/sn.last");
//            dataURL =  WCTTransfer.getURL(dataURL, null, true);
            URL dataURL = new File(MRMS_CACHE_DIR+File.separator+site+"_32dhr_sn.last").toURI().toURL();
            DecodeL3Header header = new DecodeL3Header();
            header.decodeHeader(dataURL);
            
            // if a file is more than 15 min old, don't use it
            if ((System.currentTimeMillis() - header.getScanMilliseconds()) > 1000L*60*15) {
            	System.err.println(site+"'s DHR is more than 15 min old: "+header.getDate()+" "+header.getHour()+":"+header.getMinute());
            	header.close();
            	continue;
            }
            

            DecodeL3Nexrad decoder = new DecodeL3Nexrad(header);
            decoder.decodeData(new StreamingProcess[] { tmpRasterizer });
            
            // HHC
//            URL dataURLHHC = new URL("ftp://anonymous:wct.ncdc.at.noaa.gov@tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.177hh/SI."+site.toLowerCase()+"/sn.last");
//            dataURLHHC =  WCTTransfer.getURL(dataURLHHC, null, true);
            URL dataURLHHC = new File(MRMS_CACHE_DIR+File.separator+site+"_177hh_sn.last").toURI().toURL();
            DecodeL3Header headerHHC = new DecodeL3Header();
            headerHHC.decodeHeader(dataURLHHC);
            
            DecodeL3Nexrad decoderHHC = new DecodeL3Nexrad(headerHHC);
            decoderHHC.decodeData(new StreamingProcess[] { hhcRasterizer });
            
            
            WritableRaster dhrRaster = tmpRasterizer.getWritableRaster();
            WritableRaster hhcRaster = hhcRasterizer.getWritableRaster();
            int width = dhrRaster.getWidth();
            int height = dhrRaster.getHeight();
            double noDataValue = rasterizer.getNoDataValue();
            
            long hhcPixel = 0;
            long hhcNoData = Math.round(hhcRasterizer.getNoDataValue());
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                	// only keep DHR pixels where HHC is not AP/Clutter(2), Biological(1), RF(15)
                	hhcPixel = Math.round(hhcRaster.getSample(i, j, 0));
                	if (hhcPixel == 1 || hhcPixel == 2 || hhcPixel == 15 || hhcPixel == hhcNoData) {
//                    if (hhcPixel >= 3 && hhcPixel <= 14) {
//                    if ((hhcPixel == 1 )) {
                		dhrRaster.setSample(i, j, 0, noDataValue);
                	}
                }
            }
            
            
            
            
            WCTMathOp.max(rasterizer, rasterizer, tmpRasterizer, tmpRasterizer.getNoDataValue());
            tmpRasterizer.clearRaster();
        }
        
      
        WCTRasterExport rasterExport = new WCTRasterExport();
        rasterizer.setDateInMilliseconds(new Date().getTime());
        rasterExport.saveNetCDF(new File("C:\\work\\wct\\hybrid-tests\\dhr-hhc-3500x7000.nc"), rasterizer);
        		
	}
	
	
	
	
	
	private static void processMosaicWithHydrometeorAndDistanceFiltering() throws Exception {
		
		
		WCTRasterizer rasterizer = new WCTRasterizer(3500, 7000, -999.0f);
		// new Rectangle2D.Double(-180, -90, 360, 180)
		rasterizer.setBounds(MRMS_BOUNDS);        

		WCTRasterizer tmpRasterizer = new WCTRasterizer(3500, 7000, -999.0f);
		// new Rectangle2D.Double(-180, -90, 360, 180)
		tmpRasterizer.setBounds(MRMS_BOUNDS);		

		WCTRasterizer hhcRasterizer = new WCTRasterizer(3500, 7000, -999.0f);
		// new Rectangle2D.Double(-180, -90, 360, 180)
		hhcRasterizer.setBounds(MRMS_BOUNDS);
		

        GridDatasetNativeRaster closestSiteGrid = new GridDatasetNativeRaster();
        closestSiteGrid.setNativeGridSizeCacheLimit(3500*7000L);
        closestSiteGrid.process("C:\\work\\wct\\hybrid-tests\\closestSite-3500x7000.nc");

        GridDatasetNativeRaster closestSiteGrid2 = new GridDatasetNativeRaster();
        closestSiteGrid2.setNativeGridSizeCacheLimit(3500*7000L);
        closestSiteGrid2.process("C:\\work\\wct\\hybrid-tests\\closestSite2-3500x7000.nc");
        
        
		MRMS_CACHE_DIR.mkdirs();
		cacheRealTimeFilesFromNWS(MRMS_CACHE_DIR);
		
        for (int n=0; n<SITELIST.length; n++) {     
        	
        	try {
        	
        	String site = SITELIST[n];
        	
        	System.out.println("Processing: "+site);
            
        	// DHR
//            URL dataURL = new URL("ftp://anonymous:wct.ncdc.at.noaa.gov@tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.32dhr/SI."+site.toLowerCase()+"/sn.last");
//            dataURL =  WCTTransfer.getURL(dataURL, null, true);
            URL dataURL = new File(MRMS_CACHE_DIR+File.separator+site+"_32dhr_sn.last").toURI().toURL();
            DecodeL3Header header = new DecodeL3Header();
            header.decodeHeader(dataURL);
            
            // if a file is more than 15 min old, don't use it
            if ((System.currentTimeMillis() - header.getScanMilliseconds()) > 1000L*60*15) {
            	System.err.println(site+"'s DHR is more than 15 min old: "+header.getDate()+" "+header.getHour()+":"+header.getMinute());
            	header.close();
            	continue;
            }
            

            DecodeL3Nexrad decoder = new DecodeL3Nexrad(header);
            decoder.decodeData(new StreamingProcess[] { tmpRasterizer });
            
            // HHC
//            URL dataURLHHC = new URL("ftp://anonymous:wct.ncdc.at.noaa.gov@tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.177hh/SI."+site.toLowerCase()+"/sn.last");
//            dataURLHHC =  WCTTransfer.getURL(dataURLHHC, null, true);
            URL dataURLHHC = new File(MRMS_CACHE_DIR+File.separator+site+"_177hh_sn.last").toURI().toURL();
            DecodeL3Header headerHHC = new DecodeL3Header();
            headerHHC.decodeHeader(dataURLHHC);
            
            DecodeL3Nexrad decoderHHC = new DecodeL3Nexrad(headerHHC);
            decoderHHC.decodeData(new StreamingProcess[] { hhcRasterizer });
            
            
            WritableRaster dhrRaster = tmpRasterizer.getWritableRaster();
            WritableRaster hhcRaster = hhcRasterizer.getWritableRaster();
            WritableRaster closestSiteRaster = closestSiteGrid.getWritableRaster();
            WritableRaster closestSiteRaster2 = closestSiteGrid2.getWritableRaster();
            int width = dhrRaster.getWidth();
            int height = dhrRaster.getHeight();
            double noDataValue = rasterizer.getNoDataValue();
            
            long hhcPixel = 0;
            long hhcNoData = Math.round(hhcRasterizer.getNoDataValue());
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                	// only keep DHR pixels where HHC is not AP/Clutter(2), Biological(1), RF(15)
                	hhcPixel = Math.round(hhcRaster.getSample(i, j, 0));
                	if (hhcPixel == 1 || hhcPixel == 2 || hhcPixel == 15 || hhcPixel == hhcNoData) {
//                    if (hhcPixel >= 3 && hhcPixel <= 14) {
//                    if ((hhcPixel == 1 )) {

            			dhrRaster.setSample(i, j, 0, noDataValue);
                		
                	}
                	
                	
                	


            		int closestSiteIndex = closestSiteRaster.getSample(i, j, 0);
            		int closestSiteIndex2 = closestSiteRaster2.getSample(i, j, 0);
//            		System.out.println("n="+n+" closestSiteIndex="+closestSiteIndex);
            		
            		
            		// also filter to only use values from closest site
            		if (closestSiteIndex != n && closestSiteIndex2 != n) {
            			dhrRaster.setSample(i, j, 0, noDataValue);
            		}
                }
            }
            
            
            
            
            WCTMathOp.max(rasterizer, rasterizer, tmpRasterizer, tmpRasterizer.getNoDataValue());
            tmpRasterizer.clearRaster();
            
        } catch (Exception e) {
        	System.err.println("Error processing data files for "+SITELIST[n]+": "+e.getMessage());
        }
        	
        } 
        
      
        WCTRasterExport rasterExport = new WCTRasterExport();
        rasterizer.setDateInMilliseconds(new Date().getTime());
        rasterExport.saveNetCDF(new File("C:\\work\\wct\\hybrid-tests\\dhr-hhc-distfilt-3500x7000.nc"), rasterizer);
        		
	}
	
	
//	private static void maxUsingDistanceFilter(WritableRaster returnRaster, 
//			WritableRaster raster1, WritableRaster raster2,	int noData,
//			WritableRaster closestSiteIndexRaster, WritableRaster closestSiteDistance)
//			          throws RasterMathOpException {
////			      checkRasters(returnRaster, raster1, raster2);
//
//			      for (int i = 0; i < returnRaster.getWidth(); i++) {
//			         for (int j = 0; j < returnRaster.getHeight(); j++) {
//			            if (raster1.getSample(i, j, 0) == noData) {
//			               returnRaster.setSample(i, j, 0, raster2.getSample(i, j, 0));
//			            }
//			            else if (raster2.getSample(i, j, 0) == noData) {
//			               returnRaster.setSample(i, j, 0, raster1.getSample(i, j, 0));
//			            }
//			            else {
//			               if (raster1.getSample(i, j, 0) > raster2.getSample(i, j, 0)) {
//			                  returnRaster.setSample(i, j, 0, raster1.getSample(i, j, 0));
//			               }
//			               else {
//			                  returnRaster.setSample(i, j, 0, raster2.getSample(i, j, 0));
//			               }
//			            }
//			         }
//			      }
//			   }
//	}
	
	
	
	
	private static void cacheRealTimeFilesFromNWS(File cacheDir) throws IOException, FtpException {
		
		String nwsFtpHost = "tgftp.nws.noaa.gov";		

        FtpBean ftp = new FtpBean();
        ftp.ftpConnect(nwsFtpHost, "anonymous", "wct.ncdc.at.noaa.gov");     
        
		FileUtils.cleanDirectory(cacheDir);
		for (String site : SITELIST) {
			for (String product : new String[] { "32dhr", "177hh" }) {
			
				// /SL.us008001/DF.of/DC.radar/DS.32dhr/SI.kgrk/sn.last
//				   /SL.us008001/DF.of/DC.radar/DS.32dhr/SI.kdfx/sn.last
				
				String remoteFile = "/SL.us008001/DF.of/DC.radar/DS."+product+"/SI."+site.toLowerCase()+"/sn.last";
		        final File localTmpFile = new File(cacheDir.toString()+File.separator+site+"_"+product+"_sn.last.ftp");
		        final File localFile = new File(cacheDir.toString()+File.separator+site+"_"+product+"_sn.last");

		        System.out.print("Downloading: "+remoteFile+" to "+localTmpFile+" .... ");
		        ftp.getBinaryFile(remoteFile, localTmpFile.toString(), new FtpObserver() {
					@Override
					public void byteWrite(int arg0) {
					}
					
					@Override
					public void byteRead(int arg0) {
					}
					
					@Override
					public boolean abortTransfer() {
						return false;
					}
				});
		        
		        
		        System.out.println("Done!");
                localTmpFile.renameTo(localFile);                       
			}
		}
		ftp.close();
	}
	
	
	private static void buildSiteLookupGrid() throws WCTExportNoDataException, IOException, InvalidRangeException {
		WCTRasterizer siteLutRasterizer = new WCTRasterizer(3500, 7000, -999.0f);
		// new Rectangle2D.Double(-180, -90, 360, 180)
		siteLutRasterizer.setBounds(MRMS_BOUNDS);
		
        WritableRaster siteLutRaster = siteLutRasterizer.getWritableRaster();
        int width = siteLutRaster.getWidth();
        int height = siteLutRaster.getHeight();
        double lat = 0;
        double lon = 0;
        double cellsize = MRMS_BOUNDS.getWidth()/width;
        String closestSite = null;
        List<String> siteList = Arrays.asList(SITELIST);
        Integer siteIndex;
        
        RadarHashtables sites = RadarHashtables.getSharedInstance();
        HashMap<String, Integer> lutHash = new HashMap<String, Integer>();
        for (int n=0; n<SITELIST.length; n++) {
        	lutHash.put(SITELIST[n], n);
        }

    	for (int j = 0; j < height; j++) {
        	System.out.println("processing row "+j+" of "+height);
        	for (int i = 0; i < width; i++) {
        		
        		lon = MRMS_BOUNDS.getMinX()+i*cellsize;
        		lat = MRMS_BOUNDS.getMinY()+j*cellsize;
        		closestSite = sites.getClosestICAO(lat, lon, 0.1, SearchFilter.NEXRAD_ONLY_NO_TEST_SITES, SITELIST);
        		if (closestSite != null) {

//        			System.out.println(closestSite+"   "+i+","+j+"   "+lon+","+lat);
//        			siteIndex = siteList.indexOf(closestSite);
        			siteIndex = lutHash.get(closestSite);
        			if (siteIndex != null) {
        				siteLutRaster.setSample(i, j, 0, siteIndex);
        			}
        		}
        	}
        }

        WCTRasterExport rasterExport = new WCTRasterExport();
        rasterExport.saveNetCDF(new File("C:\\work\\wct\\hybrid-tests\\closestSite-3500x7000.nc"), siteLutRasterizer);        
        siteLutRasterizer.clearRaster();
		
	}
	
	
	
	private void buildSiteLookupGridMultiThread() throws WCTExportNoDataException, IOException, InvalidRangeException, InterruptedException {
		WCTRasterizer siteLutRasterizer = new WCTRasterizer(3500, 7000, -999.0f);
		// new Rectangle2D.Double(-180, -90, 360, 180)
		siteLutRasterizer.setBounds(MRMS_BOUNDS);
		
		WCTRasterizer siteDistRasterizer = new WCTRasterizer(3500, 7000, -999.0f);
		// new Rectangle2D.Double(-180, -90, 360, 180)
		siteDistRasterizer.setBounds(MRMS_BOUNDS);
		
		WCTRasterizer siteLut2Rasterizer = new WCTRasterizer(3500, 7000, -999.0f);
		// new Rectangle2D.Double(-180, -90, 360, 180)
		siteLut2Rasterizer.setBounds(MRMS_BOUNDS);
		
		WCTRasterizer siteDist2Rasterizer = new WCTRasterizer(3500, 7000, -999.0f);
		// new Rectangle2D.Double(-180, -90, 360, 180)
		siteDist2Rasterizer.setBounds(MRMS_BOUNDS);
		
        WritableRaster siteLutRaster = siteLutRasterizer.getWritableRaster();
        WritableRaster siteDistRaster = siteDistRasterizer.getWritableRaster();
        WritableRaster siteLut2Raster = siteLut2Rasterizer.getWritableRaster();
        WritableRaster siteDist2Raster = siteDist2Rasterizer.getWritableRaster();
        int width = siteLutRaster.getWidth();
        int height = siteLutRaster.getHeight();
        double lat = 0;
        double lon = 0;
        double cellsize = MRMS_BOUNDS.getWidth()/width;
        String closestSite = null;
        List<String> siteList = Arrays.asList(SITELIST);
        Integer siteIndex;
        
        RadarHashtables sites = RadarHashtables.getSharedInstance();
        HashMap<String, Integer> lutHash = new HashMap<String, Integer>();
        for (int n=0; n<SITELIST.length; n++) {
        	lutHash.put(SITELIST[n], n);
        }
        
        long startMillis = System.currentTimeMillis();
        
        
        // multithread example from:  http://www.vogella.com/tutorials/JavaConcurrency/article.html
        ExecutorService executor = Executors.newFixedThreadPool(3);

    	for (int j = 0; j < height; j++) {
//        	System.out.println("processing row "+j+" of "+height);   
            executor.execute(new RowProcessRunnable(j, width, cellsize, lutHash, 
            		siteLutRaster, siteDistRaster, siteLut2Raster, siteDist2Raster));
        	
//        	new RowProcessRunnable(j, width, cellsize, lutHash, siteLutRaster).run();
            
        }
    	
    	

        // This will make the executor accept no new threads
        // and finish all existing threads in the queue
        executor.shutdown();
        // Wait until all threads are finish
        executor.awaitTermination(10, TimeUnit.MINUTES);
        System.out.println("Finished all threads - elapsed sec: "+(System.currentTimeMillis()-startMillis)/1000.0);
        
        

        WCTRasterExport rasterExport = new WCTRasterExport();
        rasterExport.saveNetCDF(new File("C:\\work\\wct\\hybrid-tests\\closestSite-3500x7000.nc"), siteLutRasterizer); 
        rasterExport.saveNetCDF(new File("C:\\work\\wct\\hybrid-tests\\closestDist-3500x7000.nc"), siteDistRasterizer);        
        rasterExport.saveNetCDF(new File("C:\\work\\wct\\hybrid-tests\\closestSite2-3500x7000.nc"), siteLut2Rasterizer); 
        rasterExport.saveNetCDF(new File("C:\\work\\wct\\hybrid-tests\\closestDist2-3500x7000.nc"), siteDist2Rasterizer);        
        siteLutRasterizer.clearRaster();       
        siteDistRasterizer.clearRaster();
        siteLut2Rasterizer.clearRaster();       
        siteDist2Rasterizer.clearRaster();
		
	}
	
	
	private static void processTimeAdjustedMosaic() {
		
		
		// 1. determine files closest to target time (before and after)
		
		// 2. filter individual dhr files using hhc
		
		// 3. get morph vectors from hrrr storm motion u/v
		
		// 4. apply morphing to get interpolated dhr at target time
		
		// 5. mosiac resulting grid coverages
		//     - use simple max value
		//     - or use closest site LUT
		//     - or use 'best radar to use LUT' from external source/analysis
		
		
		
	}
	
	
//	private static void getClosestFiles()
	
	
	
	
	class RowProcessRunnable implements Runnable {
		
		int j;
		double width, cellsize;
		RadarHashtables sites = RadarHashtables.getSharedInstance();
		String closestSite, closestSite2;
		Integer siteIndex, siteIndex2;
		HashMap<String, Integer> lutHash = null;
		WritableRaster siteLutRaster = null;
		WritableRaster siteDistRaster = null;
		WritableRaster siteLut2Raster = null;
		WritableRaster siteDist2Raster = null;
		
		public RowProcessRunnable(int j, double width, double cellsize, 
				HashMap<String, Integer> lutHash, 
				WritableRaster siteLutRaster, WritableRaster siteDistRaster,
				WritableRaster siteLut2Raster, WritableRaster siteDist2Raster) {
			this.width = width;
			this.cellsize = cellsize;
			this.lutHash = lutHash;
			this.siteLutRaster = siteLutRaster;
			this.siteDistRaster = siteDistRaster;
			this.siteLut2Raster = siteLut2Raster;
			this.siteDist2Raster = siteDist2Raster;
			this.j = j;
			
//			System.out.println("width="+width+" cellsize="+cellsize+" j="+j+" maxY="+MRMS_BOUNDS.getMaxY());
		}

		@Override
		public void run() {
			double lat, lon;
			
			if (j%10==0) System.out.println("Processing in thread - row: "+j);
			
			
			for (int i = 0; i < width; i++) {

				lon = MRMS_BOUNDS.getMinX()+i*cellsize;
				lat = MRMS_BOUNDS.getMaxY()-j*cellsize;
				String[][] vals = sites.getClosestTwoWithDistance(lat, lon, 
						230000, MaxRangeUnits.METERS,
						SearchFilter.NEXRAD_ONLY_NO_TEST_SITES, SITELIST);
							
				if (vals != null) {
					closestSite = vals[0][0];	
					closestSite2 = vals[1][0];
					
//					System.out.println(Arrays.toString(vals));
					
//					    			System.out.println(closestSite+"   "+i+","+j+"   "+lon+","+lat);
					//    			siteIndex = siteList.indexOf(closestSite);
					siteIndex = lutHash.get(closestSite);
					siteIndex2 = lutHash.get(closestSite2);
					if (siteIndex != null) {
						siteLutRaster.setSample(i, j, 0, siteIndex);
						siteDistRaster.setSample(i, j, 0, Double.parseDouble(vals[0][1]));
					}
					if (siteIndex2 != null) {
						siteLut2Raster.setSample(i, j, 0, siteIndex2);
						siteDist2Raster.setSample(i, j, 0, Double.parseDouble(vals[1][1]));
					}
				}
			}		
		}

	}
	
}

