package steve.test.hybrid;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.xml.sax.SAXException;

import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.io.ScanResults;
import gov.noaa.ncdc.wct.io.WCTDirectoryScanner;
import ucar.unidata.io.UncompressInputStream;

public class HybridUtils {

	public static void extractFromTarfiles(String tarDir, String outputDir) {
		
		IOFileFilter filter = new IOFileFilter() {
			
			@Override
			public boolean accept(File f, String str) {
				System.out.println("accept 1: "+f+"  "+str);
				return false;
			}
			
			@Override
			public boolean accept(File f) {
//				System.out.println("accept 2: "+f);
				return (f.toString().endsWith(".tar.gz"));
			}
		};
				
		
		System.out.println("Scanning "+tarDir+" for tar.gz files...");
//		Collection<File> files = FileUtils.listFiles(new File(tarDir), new String[] {".tar.gz"}, true);
		Collection<File> files = FileUtils.listFiles(new File(tarDir), filter, filter);
		
		System.out.println("Found "+files.size()+" .tar.gz files");
		
		for (File f : files) {
			try {		

				System.out.println("processing "+f);
				
		        TarArchiveInputStream tis = getTarInputStream(f);
		        
	            TarArchiveEntry tarEntry = null;
	            while ((tarEntry = tis.getNextTarEntry()) != null) {

	            	

                    long size = tarEntry.getSize();
                    int sizeRead = 0;

//                    if (! quiet) outStream.println(tarEntry.getName() + " : "+tarEntry.getSize()+ " : "+tis.available());
	            	 final byte[] data = new byte[(int) size];
	                    if (tarEntry.getSize() == 0) {
	                        System.err.println("EMPTY 0 BYTE TAR ENTRY: " + tarEntry.getName());
	                    }
	                    else {
	                        while (sizeRead < size){
	                        	sizeRead += tis.read(data, sizeRead, data.length);
	                        }
	                        if (sizeRead != size) {
	                        	System.err.println("ERROR READING ENTRY: " + tarEntry.getName());
	                        }
	                        

	                        // Read header - if error, update in error column
//	                        ucar.unidata.io.RandomAccessFile raf = new InMemoryRandomAccessFile(
//	                                tarEntry.getName(), data);
//	                        raf.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
	                        

	                        // Check for product column in database
	                        // 012345678901234567890123456789012345
	                        // KABQ_SDUS65_NTVFDX_200410052253
//	                        if (! quiet) outStream.println("FOUND ENTRY: "+entryString);

	                        String entryString = tarEntry.getName();

	                        String productCode = entryString.substring(12, 15);
	                        
	                        
	                        if (productCode.equalsIgnoreCase("DHR") || productCode.equalsIgnoreCase("HHC")) {
		                        System.out.println(productCode+"  "+entryString);
	                        	FileUtils.writeByteArrayToFile(new File(outputDir+File.separator+entryString), data);
	                        }
	                        
	                    }






	            }
	            
	            
	            
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	public static TarArchiveInputStream getTarInputStream(File f) throws MalformedURLException, IOException {
        TarArchiveInputStream tis;
        UncompressInputStream uis = null;
        GZIPInputStream gis = null;        

        InputStream is = f.toURI().toURL().openStream();
        
        // uncompress UNIX .Z if needed
        if (f.toString().endsWith(".Z") || f.toString().endsWith(".z")) {
            uis = new UncompressInputStream(is);
            tis = new TarArchiveInputStream(uis);
        }
        // uncompress gunzip .gz if needed
        else if (f.toString().endsWith(".GZ") || f.toString().endsWith(".gz")) {
            gis = new GZIPInputStream(is);
            tis = new TarArchiveInputStream(gis);
        }
        else {
            tis = new TarArchiveInputStream(is);
        }

        return tis;
	}
	
	
	public static ScanResults getClosestFile(String sourceID, Date targetDateTime, ScanResults[] productList) throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException, DecodeException, SQLException, ParseException {
		ScanResults[] beforeAfter = getBeforeAndAfterFiles(sourceID, targetDateTime, productList);

		long targetMillis = targetDateTime.getTime();

		long beforeMillis = WCTUtils.SCAN_RESULTS_FORMATTER.parse(beforeAfter[0].getTimestamp()).getTime();
		long afterMillis = WCTUtils.SCAN_RESULTS_FORMATTER.parse(beforeAfter[0].getTimestamp()).getTime();
		
		if ((targetMillis - beforeMillis) < (afterMillis - targetMillis)) {
			return beforeAfter[0];
		}
		else {
			return beforeAfter[1];
		}
	}
	
	public static ScanResults[] getBeforeAndAfterFiles(String sourceID, Date targetDateTime, ScanResults[] productList) throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException, DecodeException, SQLException, ParseException {
		

//		System.out.println(productList.toString(10));
		
		long targetMillis = targetDateTime.getTime();
		ScanResults beforeFile = null;
		long beforeDiff = Long.MAX_VALUE;
		ScanResults afterFile = null;
		long afterDiff = Long.MAX_VALUE;
		
		for (ScanResults sr : productList) {	
			
			if (sourceID != null && ! sourceID.equalsIgnoreCase(sr.getSourceID())) {
				continue;
			}
			
			long fileMillis = WCTUtils.SCAN_RESULTS_FORMATTER.parse(sr.getTimestamp()).getTime();
			
			System.out.println("fileMillis: "+fileMillis+"  targetMillis: "+targetMillis+"  "+sr);
			
			long diff = fileMillis - targetMillis;
			if (diff == 0) {
				beforeDiff = 0;
				beforeFile = sr;
				afterDiff = 0;
				afterFile = sr;
			} 
			else if (diff < 0) {
				if (Math.abs(diff) < beforeDiff) {
					beforeDiff = Math.abs(diff);
					beforeFile = sr;
				}
			} 
			else {
				if (diff < afterDiff) {
					afterDiff = diff;
					afterFile = sr;
				}
			}
		}

		System.out.println("BEFORE: ("+beforeDiff+")  "+beforeFile+
				" \n AFTER: ("+afterDiff+")  "+afterFile);
				
		return new ScanResults[] { beforeFile, afterFile };		
	}
	
	
	public List<String> getUniqueSourceIDs(ScanResults[] results) {
		ArrayList<String> uniqueList = new ArrayList<String>();
		for (ScanResults sr : results) {
			if (! uniqueList.contains(sr.getSourceID())) {
				uniqueList.add(sr.getSourceID());
			}
		}
		return uniqueList;
	}
	
	
	
	public static void main(String[] args) {
		System.setProperty("javax.xml.xpath.XPathFactory:" +XPathConstants.DOM_OBJECT_MODEL, "net.sf.saxon.xpath.XPathFactoryImpl");

		try {
			
//			extractFromTarfiles("C:\\work\\nxinv", "C:\\work\\wct\\hybrid-tests\\untar");
//			KABR HHC 20170508 16:46:00
			
			WCTDirectoryScanner scanner = new WCTDirectoryScanner();			
			ScanResults[] productList = scanner.scanLocalDirectory(new File("C:\\work\\wct\\hybrid-tests\\untar"));
			ScanResults[] closestFiles = getBeforeAndAfterFiles("KABR", WCTUtils.SCAN_RESULTS_FORMATTER.parse("20170508 16:47:00"), productList);
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
}
