package gov.noaa.ncdc.wct.io;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import ftp.FtpException;
import gov.noaa.ncdc.common.FTPList;
import gov.noaa.ncdc.common.SSHDirectory;
import gov.noaa.ncdc.common.URLDirectory;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;
import gov.noaa.ncdc.wct.io.FileInfo.Type;
import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvCatalogImpl;
import thredds.catalog.InvDataset;
import thredds.catalog.ServiceType;

/**
 *  Scans a directory (HAS, local disk, remote URL, THREDDS) and returns either
 *  an array of URLs 
 *
 * @author     steve.ansari
 * @created    September 23, 2004
 */
public class DirectoryScanner {


	public static final String NCDC_HAS_HTTP_SERVER = "https://www1.ncdc.noaa.gov";
	public static final String NCDC_HAS_HTTP_DIRECTORY = "pub/has";
	public static final String NCDC_HAS_FTP_SERVER = "ftp.ncdc.noaa.gov";
	public static final String NCDC_HAS_FTP_DIRECTORY = "pub/has";
	
	public static final String NCDC_HAS_HTTP_SERVER_BACKUP = "http://ftp3.ncdc.noaa.gov";
	public static final String NCDC_HAS_HTTP_DIRECTORY_BACKUP = "pub/has";
	public static final String NCDC_HAS_FTP_SERVER_BACKUP = "ftp3.ncdc.noaa.gov";
	public static final String NCDC_HAS_FTP_DIRECTORY_BACKUP = "pub/has";
	
	
	
	//   public static final String CLASS_SERVER_PRIMARY = "http://www.class.ncdc.noaa.gov";
	//   public static final String CLASS_SERVER_BACKUP  = "http://www.class.ngdc.noaa.gov";
//	public static final String CLASS_SERVER_PRIMARY = "ftp.class.ncdc.noaa.gov";
//	public static final String CLASS_SERVER_BACKUP  = "ftp.class.ngdc.noaa.gov";
	public static final String CLASS_SERVER_PRIMARY = "ftp.avl.class.noaa.gov";
	public static final String CLASS_SERVER_BACKUP  = "ftp.bou.class.noaa.gov";

	

	private Session sshSession;

    protected Vector<GeneralProgressListener> listeners = new Vector<GeneralProgressListener>();
    protected GeneralProgressEvent event = new GeneralProgressEvent(this);

    
    public void addProgressListener(GeneralProgressListener listener) {
        listeners.add(listener);
    }
    
    public void removeProgressListener(GeneralProgressListener listener) {
        listeners.remove(listener);
    }
    
    public void clearProgressListeners() {
        listeners.clear();
    }
    
	
	
	/**
	 * As an alternative for using the continuation token, this method will take an http(s) s3 URL and expand
	 * the following syntax into multiple requests: <br>
	 * Range: ${00-03}  (inclusive) <br>
	 * List: ${A,B,C} <br>
	 * https://s3.amazonaws.com/noaa-goes16/?list-type=2&prefix=ABI-L1b-RadM/2017/240/${00-23}/OR_ABI-L1b-RadM1-M3C01_G16 <br>
	 * 
	 * @param prefixURL
	 * @return
	 * @throws MalformedURLException
	 */
	protected URL[] expandAWS(String prefixURL) throws MalformedURLException {
		// https://s3.amazonaws.com/noaa-goes16/?list-type=2&prefix=ABI-L1b-RadM/2017/240/01/OR_ABI-L1b-RadM1-M3C01_G16
		
		// https://s3.amazonaws.com/noaa-goes16/?list-type=2&prefix=ABI-L1b-RadM/2017/240/${00-23}/OR_ABI-L1b-RadM1-M3C01_G16
		
		// 1. check for range or lists in URL
		if (! prefixURL.contains("${")) {
			return new URL[] { new URL(prefixURL) };
		}
		
		// 2. extract out range or list
		int begIndex = prefixURL.indexOf("${");
		int endIndex = prefixURL.indexOf("}", begIndex);
		String urlBefore = prefixURL.substring(0, begIndex);
		String urlAfter = prefixURL.substring(endIndex+1);
		String rangeOrList = prefixURL.substring(begIndex+2, endIndex);
		URL[] urls = null;
		
		// 3. handle range or list
		if (rangeOrList.contains("-")) {
			String[] rangeElems = rangeOrList.split("-");
			// this must be a numeric range, like 00-23.  
			// the max number of characters is to used to format all numbers. 
			//  ex) 00, 01, 02, 03, 04
			int begRange = Integer.parseInt(rangeElems[0]);
			int endRange = Integer.parseInt(rangeElems[1]);
			String fmtString = "";
			for (int n=0; n<rangeElems[1].length(); n++) {
				fmtString += "0";
			}
			DecimalFormat fmt = new DecimalFormat(fmtString);
			urls = new URL[endRange-begRange+1];
			int idx=0;
			for (int n=begRange; n<=endRange; n++) {
				urls[idx++] = new URL(urlBefore + fmt.format(n) + urlAfter);
			}
		}
		else if (rangeOrList.contains(",")) {
			String[] listElems = rangeOrList.split(",");
			urls = new URL[listElems.length];
			for (int n=0; n<urls.length; n++) {
				urls[n] = new URL(urlBefore + listElems[n] + urlAfter);
			}
		}
		else {
			urls = new URL[] { new URL(prefixURL) };
		}
		
		
		while (urlArrayHasExpansionVariable(urls)) {
			System.out.println("Recursive round of expansion - starting with "+urls.length);
			ArrayList<URL> finalUrlList = new ArrayList<URL>();
			for (URL url : urls) {
				finalUrlList.addAll(Arrays.asList(expandAWS(url.toString())));
			}
			URL[] finalUrlArray = new URL[finalUrlList.size()];
			System.out.println("Recursive round of expansion - ending with "+finalUrlArray.length);			
			urls = finalUrlList.toArray(finalUrlArray);
		}
		
		return urls;
		
	}
	
	/**
	 * Does URL have a ${X1-Xn} range or ${X,Y,Z} list expansion variable
	 * @param urls
	 * @return
	 */
	public static boolean urlArrayHasExpansionVariable(URL[] urls) {
		boolean expansion = false;
		for (URL url : urls) {
			if (url.toString().contains("${")) {
				expansion = true;
			}
		}
		return expansion;
	}
	
	
	public ListResults listAWSBucket(String prefixURL) throws JDOMException, IOException {
		
		ListResults listResults = new ListResults();

		// http://noaa-nexrad-level2.s3.amazonaws.com/?prefix=2000/01/01/KGSP/&max-keys=500
//		URL url = new URL(prefixURL);
		SAXBuilder builder = new SAXBuilder();
		
		ArrayList<URL[]> urlArrayList = new ArrayList<URL[]>();
		ArrayList<FileInfo[]> fileInfoArrayList = new ArrayList<FileInfo[]>();
		
		URL[] expandedURLs = expandAWS(prefixURL);
		
		int totalCount = 0;
		int idx = 0;
		for (URL url : expandedURLs) {

	    	event.setProgress(totalCount*100/expandedURLs.length);
	        event.setStatus("Listing AWS Bucket ("+ (++idx) + " of "+expandedURLs.length+")...");
	        for (int n=0; n<listeners.size(); n++) {
	            listeners.get(n).progress(event);
	        }
			
			
			Document document = (Document) builder.build(url);
			Element rootNode = document.getRootElement();
			Namespace ns = rootNode.getNamespace();
			List<Element> list = rootNode.getChildren("Contents", ns);	

			FileInfo[] fileInfoArray = new FileInfo[list.size()];
			URL[] urlArray = new URL[list.size()];

			for (int i = 0; i < list.size(); i++) {
				Element node = (Element) list.get(i);
				String size = FileUtils.byteCountToDisplaySize(Long.parseLong(node.getChildText("Size", ns)));
				String key = node.getChildText("Key", ns);

				// only display filename, not folder structure
				String filename = key.substring(key.lastIndexOf("/") + 1);
				String path = key.substring(0, key.lastIndexOf("/"));
				long lsize = Long.parseLong(node.getChildText("Size", ns));
				fileInfoArray[i] = new FileInfo(Type.FILE, filename, path, "", lsize);
				urlArray[i] = new URL(url.toString().substring(0, url.toString().indexOf("?"))+key);
				if (i==0) {
					System.out.println("first url: "+urlArray[i]);
				}
			}
			System.out.println("... from: "+url);
			
			totalCount += urlArray.length;
			fileInfoArrayList.add(fileInfoArray);
			urlArrayList.add(urlArray);
			
			System.out.println("... processed "+totalCount+" ...");
		
		}
		
		URL[] totalUrlArray = new URL[totalCount];
		FileInfo[] totalFileInfoArray = new FileInfo[totalCount];
		
		int copyIndex = 0;
		for (int n=0; n<urlArrayList.size(); n++) {
			System.arraycopy(urlArrayList.get(n), 0, totalUrlArray, copyIndex, urlArrayList.get(n).length);
			System.arraycopy(fileInfoArrayList.get(n), 0, totalFileInfoArray, copyIndex, fileInfoArrayList.get(n).length);
			copyIndex += urlArrayList.get(n).length;
		}

		listResults.setUrlList(totalUrlArray);
		listResults.setFileInfoList(totalFileInfoArray);
		listResults.setLocationType(ListResults.LOCATION_AWS);

		return listResults;
	}

	
	/**
	 * NOT WORKING CORRECTLY YET....
	 * @param prefixURL
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 */
	public ListResults listAWSBucket_WithContinuation(String prefixURL) throws JDOMException, IOException {
		
		ListResults listResults = new ListResults();

		// http://noaa-nexrad-level2.s3.amazonaws.com/?prefix=2000/01/01/KGSP/&max-keys=500
		URL url = new URL(prefixURL);
		SAXBuilder builder = new SAXBuilder();
		
		ArrayList<URL[]> urlArrayList = new ArrayList<URL[]>();
		ArrayList<FileInfo[]> fileInfoArrayList = new ArrayList<FileInfo[]>();
		
		String continuationToken = null;
		boolean firstTime = true;
		int totalCount = 0;
		while (firstTime || continuationToken != null) {

			Document document = (Document) builder.build(url);
			Element rootNode = document.getRootElement();
			Namespace ns = rootNode.getNamespace();
			List<Element> list = rootNode.getChildren("Contents", ns);

			// if we have more than 1000 files available, keep looping until we have all files
			// TODO - work out bugs before debugging
			Element contTokenElem = rootNode.getChild("NextContinuationToken", ns);
			continuationToken = contTokenElem != null ? contTokenElem.getText().trim() : null;
			
		System.out.println("continuation? "+continuationToken);	
		
		
		

			FileInfo[] fileInfoArray = new FileInfo[list.size()];
			URL[] urlArray = new URL[list.size()];

			for (int i = 0; i < list.size(); i++) {
				Element node = (Element) list.get(i);
				String size = FileUtils.byteCountToDisplaySize(Long.parseLong(node.getChildText("Size", ns)));
				String key = node.getChildText("Key", ns);

				// only display filename, not folder structure
				String filename = key.substring(key.lastIndexOf("/") + 1);
				String path = key.substring(0, key.lastIndexOf("/"));
				long lsize = Long.parseLong(node.getChildText("Size", ns));
				fileInfoArray[i] = new FileInfo(Type.FILE, filename, path, "", lsize);
				urlArray[i] = new URL(url.toString().substring(0, url.toString().indexOf("?"))+key);
				if (i==0) {
					System.out.println("first url: "+urlArray[i]);
				}
			}
			System.out.println("... from: "+url);
			
			totalCount += urlArray.length;
			fileInfoArrayList.add(fileInfoArray);
			urlArrayList.add(urlArray);
			
			System.out.println("... processed "+totalCount+" ...");
		
			if (continuationToken != null) {
				url = new URL(prefixURL+"&continuation-token="+continuationToken);	
			}
			firstTime = false;
		}
		
		URL[] totalUrlArray = new URL[totalCount];
		FileInfo[] totalFileInfoArray = new FileInfo[totalCount];
		
		int copyIndex = 0;
		for (int n=0; n<urlArrayList.size(); n++) {
			System.arraycopy(urlArrayList.get(n), 0, totalUrlArray, copyIndex, urlArrayList.get(n).length);
			System.arraycopy(fileInfoArrayList.get(n), 0, totalFileInfoArray, copyIndex, fileInfoArrayList.get(n).length);
			copyIndex += urlArrayList.get(n).length;
		}

		listResults.setUrlList(totalUrlArray);
		listResults.setFileInfoList(totalFileInfoArray);
		listResults.setLocationType(ListResults.LOCATION_AWS);

		return listResults;
	}
	
	public ListResults listAWSBucketSimple(String prefixURL) throws JDOMException, IOException {
		
		ListResults listResults = new ListResults();

		// http://noaa-nexrad-level2.s3.amazonaws.com/?prefix=2000/01/01/KGSP/&max-keys=500
		URL url = new URL(prefixURL);

		SAXBuilder builder = new SAXBuilder();
		Document document = (Document) builder.build(url);

		Element rootNode = document.getRootElement();
		Namespace ns = rootNode.getNamespace();
		List<Element> list = rootNode.getChildren("Contents", ns);

		FileInfo[] fileInfoArray = new FileInfo[list.size()];
		URL[] urlArray = new URL[list.size()];
		
		for (int i = 0; i < list.size(); i++) {
			Element node = (Element) list.get(i);
			String size = FileUtils.byteCountToDisplaySize(Long.parseLong(node.getChildText("Size", ns)));
			String key = node.getChildText("Key", ns);

			// only display filename, not folder structure
			String filename = key.substring(key.lastIndexOf("/") + 1);
			String path = key.substring(0, key.lastIndexOf("/"));
			long lsize = Long.parseLong(node.getChildText("Size", ns));
			fileInfoArray[i] = new FileInfo(Type.FILE, filename, path, "", lsize);
			urlArray[i] = new URL(url.toString().substring(0, url.toString().indexOf("?"))+key);
			System.out.println(urlArray[i]);
		}
		
		// if we have more than 1000 files available, keep looping until we have all files
//		String continuationToken = rootNode.getChildTextTrim("NextContinuationToken");
//		if (continuationToken != null) {
//			url = new URL(prefixURL+"&NextContinuationToken="+continuationToken);
//			
//		}
		

		listResults.setUrlList(urlArray);
		listResults.setFileInfoList(fileInfoArray);
		listResults.setLocationType(ListResults.LOCATION_AWS);

		return listResults;
	}
	
	
	/**
	 * Only works with remote servers that return a list of directory contents as HTML.
	 * May not work with some server settings.  FTP URLs will only work with anonymous
	 * FTP.
	 *
	 * @param  directoryURL               Description of the Parameter
	 * @return                            Description of the Return Value
	 * @exception  MalformedURLException  Description of the Exception
	 * @exception  FtpException           Description of the Exception
	 * @exception  IOException            Description of the Exception
	 */
	public ListResults listURL(URL directoryURL)
	throws MalformedURLException, FtpException, IOException {

		ListResults listResults = new ListResults();

		String dirString = prepURL(directoryURL).toString();

		// Redirect THREDDS traffic
		if (dirString.endsWith("/catalog.xml")) {

			return listTHREDDSDirectory(directoryURL);
		}
		else if (directoryURL.getProtocol().equals("ftp")) {

			return listFtpDirectory(directoryURL.getHost(), "anonymous", "wct.ncdc.at.noaa.gov", directoryURL.getPath());
		}
		else if (directoryURL.getProtocol().equals("file")) {

			// Convert to java.io.File
			File dir = new File(directoryURL.getFile());

			return listLocalDirectory(dir);
		}
		else {

			listResults.setUrlList(URLDirectory.getURLs(new URL(dirString)));

		}


		return listResults;
	}

	/**
	 *  Lists directory contents of FTP directory
	 *
	 * @param  host       
	 * @param  user       
	 * @param  pass       
	 * @param  directory  
	 * @return            
	 */
	public ListResults listFtpDirectory(String host, String user, String pass, String directory) 
	throws FtpException, IOException {

		return listFtpDirectory(host, user, pass, new String[] { directory });
	}

	/**
	 *  Lists directory contents of FTP directory
	 *
	 * @param  host       
	 * @param  user       
	 * @param  pass       
	 * @param  directory  
	 * @return            
	 */
	public ListResults listFtpDirectory(String host, String user, String pass, String[] directoryArray) 
	throws FtpException, IOException {

		ListResults listResults = new ListResults();

		for (int n=0; n<directoryArray.length; n++) {
			if (! directoryArray[n].startsWith("/")) {
				directoryArray[n] = "/"+directoryArray[n];
			}
		}

		FTPList ftpList = new FTPList();
		FileInfo[] files = ftpList.getDirectoryList(host, user, pass, "bin", directoryArray);

		// assemble the URL[]
		// use pattern such as: ftp://anonymous:wct@noaa.gov@ftp.ncdc.noaa.gov/
		URL[] fileURLs = new URL[files.length];
		for (int n = 0; n < files.length; n++) {         
			fileURLs[n] = new URL("ftp://"+user+":"+pass+"@"+host+""+files[n].getPath()+"/"+files[n].getName());
		}

		listResults.setUrlList(fileURLs);
		listResults.setFileInfoList(files);
		listResults.setLocationType(ListResults.LOCATION_FTP);

		return listResults;

	}


	/**
	 *  Lists contents of local directory
	 *
	 * @param  dir  Description of the Parameter
	 * @return      Description of the Return Value
	 */
	public ListResults listLocalDirectory(File dir) throws MalformedURLException {

		ListResults listResults = new ListResults();

		// Exclude directories
		FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return false;
				}
				else {
					return true;
				}
			}
		};

		File[] files = dir.listFiles(filter);
//		File[] files = dir.listFiles();
		if (files == null) {
			files = new File[0]; 
		}
		
		FileInfo[] fileInfo = new FileInfo[files.length];
		for (int n=0; n<fileInfo.length; n++) {
			fileInfo[n] = new FileInfo(files[n].isDirectory() ? Type.DIRECTORY : Type.FILE, 
					files[n].getName(), files[n].getPath(), 
					WCTUtils.ISO_DATE_FORMATTER.format(new Date(files[n].lastModified())), files[n].getTotalSpace());
		}
		

	

		// assemble the URL[]
		URL[] fileURLs = new URL[files.length];
		for (int n = 0; n < files.length; n++) {
			fileURLs[n] = files[n].toURI().toURL();
		}

		listResults.setUrlList(fileURLs);
		listResults.setFileInfoList(fileInfo);
		listResults.setLocationType(ListResults.LOCATION_LOCAL);

		return listResults;

	}



	/**
	 *  Lists contents of HAS directory on NCDC FTP server
	 *
	 * @param  hasNumber  Description of the Parameter
	 * @return            Description of the Return Value
	 */
	public ListResults listHASDirectory(String hasJobString) throws FtpException, IOException {
		return listHASDirectory(hasJobString, NCDC_HAS_HTTP_SERVER, NCDC_HAS_HTTP_DIRECTORY);
	}

	/**
	 *  Lists contents of HAS directory on NCDC FTP server
	 *
	 * @param  hasNumber  Description of the Parameter
	 * @param  server     Location of server to query
	 * @return            Description of the Return Value
	 */
	public ListResults listHASDirectory(String hasJobString, String server, String dir) throws FtpException, IOException {
		

		// Check for weird input string. ex: HAS0000124231 or 124231 instead of 0000124231
		// 1. Strip "HAS" off of string
		if (hasJobString.substring(0, 3).toUpperCase().equals("HAS")) {
			hasJobString = hasJobString.substring(3, hasJobString.length());
		}
		// 2. Add '0's to beginning of string         
		else if (hasJobString.length() != 9) {
			String haswork = "";
			for (int s=0; s<(9-hasJobString.length()); s++) {
				haswork += "0";
			}
			hasJobString = haswork + hasJobString;
		}


		System.out.println("Before listing attempt");

//		ListResults listResults = listURL(new URL(server+"/"+dir+"/HAS"+hasJobString));
		
		// attempt to load from a 'fileList.txt' file first, then try to list dir if it doesn't exist
		ListResults listResults = null;
		try {
			listResults = parseFileList(new URL(server+"/"+dir+"/HAS"+hasJobString));
		} catch (Exception e) {
//			e.printStackTrace();
			System.out.println("No fileList.txt found, listing from: "+new URL(server+"/"+dir+"/HAS"+hasJobString+"/"));
			listResults = listURL(new URL(server+"/"+dir+"/HAS"+hasJobString+"/"));
		}
		
		listResults.setLocationType(ListResults.LOCATION_HAS);

		System.out.println("After successful listing attempt... "+listResults.getCount()+" results found!");


		return listResults;
	}

	public ListResults parseFileList(URL baseDir) throws MalformedURLException, IOException, FtpException {
		URL cachedListURL = WCTTransfer.getURL(new URL(baseDir+"/fileList.txt"), true);
		ListResults results = null;
		if (cachedListURL == null) {
			System.out.println("No fileList.txt file found in order dir of "+baseDir+".  Listing directory...");
			results = listURL(baseDir);
		}
		else {
			System.out.println("Building order list from fileList.txt !");
		    results = new ListResults();
			List<String> listLines = FileUtils.readLines(FileUtils.toFile(cachedListURL));
			URL[] urlArray = new URL[listLines.size()];
			for (int n=0; n<listLines.size(); n++) {
				urlArray[n] = new URL(baseDir+"/"+listLines.get(n));
			}
			results.setUrlList(urlArray);
		}
		
		return results;
	}





	/**
	 *  Lists contents of HAS directory on NCDC FTP server
	 *
	 * @param  hasNumber  Description of the Parameter
	 * @return            Description of the Return Value
	 */
	public ListResults listCLASSDirectory(String classOrderString, String classServer) throws FtpException, IOException {

		
        DecimalFormat fmt3 = new DecimalFormat("000");
        ListResults totalListResults = new ListResults();        

        ListResults listResults = new ListResults();
        boolean keepLookingForSubDirs = true;
        int subdir = 1;
        while (keepLookingForSubDirs) {
        	
        	try {
        		listResults = listFtpDirectory(classServer, "ftp", "wct-at-noaa.gov", "/"+classOrderString+"/"+fmt3.format(subdir));
        		listResults.setLocationType(ListResults.LOCATION_CLASS);

        		totalListResults = sumListResults(totalListResults, listResults);
        		
        	} catch (FtpException e) {
//        		System.err.println(e.getMessage());
        		keepLookingForSubDirs = false;
        	}
        	
        	subdir++;
        	
//        	System.out.println(listResults.getCount() + " scan results for dir "+subdir);
        
        }
        
		return totalListResults;
	}

	
	
	
	
	
	/**
	 * The locationtype of results1 is used for the return location type.
	 * @param results1
	 * @param results2
	 * @return
	 */
	public ListResults sumListResults(ListResults results1, ListResults results2) {
		ListResults returnResults = new ListResults();
		
		URL[] results1Urls = results1.getUrlList();
		FileInfo[] results1FileInfo = results1.getFileInfoList();
		URL[] results2Urls = results2.getUrlList();
		FileInfo[] results2FileInfo = results2.getFileInfoList();
		
		URL[] returnUrls = (URL[]) ArrayUtils.addAll(results1Urls, results2Urls);
		FileInfo[] returnFileInfos = (FileInfo[]) ArrayUtils.addAll(results1FileInfo, results2FileInfo);
		
		returnResults.setUrlList(returnUrls);
		returnResults.setFileInfoList(returnFileInfos);  
		returnResults.setLocationType(results1.getLocationType());
		
		
		return returnResults;
	}





	/**
	 *  Lists contents of THREDDS catalog given URL to catalog.xml file
	 *  Currently only supports HTTPServer ServiceType
	 *
	 * @param  catalogURL  URL to catalog.xml file
	 * @return            Description of the Return Value
	 */
	public ListResults listTHREDDSDirectory(URL catalogURL) throws IOException {

		//       System.out.println("LISTING THREDDS: "+catalogURL);

		ListResults listResults = new ListResults();

		InvCatalogFactory factory = new InvCatalogFactory("default", true);
		InvCatalogImpl catalog = (InvCatalogImpl) factory.readXML(catalogURL.toString());

		if (catalog == null) {
			System.err.println("THREDDS Error: no catalog found");
			throw new IOException("THREDDS Error: no catalog found at: "+catalogURL);
		}

		URL[] fileURLs = new URL[0];

		List dirDatasetList = catalog.getDatasets();

		//      System.out.println(catalog);
		//      catalog.writeXML(System.out);
		System.out.println("THREDDS DATASET SIZE: "+dirDatasetList.size());


		// only process the first dataset
		for (int n=0; n<dirDatasetList.size() && n<1; n++) {

			//          System.out.println("DIR DATASET: "+dirDatasetList.get(n));

			InvDataset dirDataset = (InvDataset)(dirDatasetList.get(n));
			List datasetList = dirDataset.getDatasets();


			ArrayList<URL> urlList = new ArrayList<URL>();

			for (int i=0; i<datasetList.size(); i++) {

				//             System.out.println(((InvDataset)datasetList.get(i)).getCatalogUrl());

				InvDataset dataset = (InvDataset)(datasetList.get(i));
				InvAccess access = dataset.getAccess(ServiceType.HTTPServer);

				//System.out.println("ACCESS getUrlPath: "+access.getUrlPath());
				//System.out.println("ACCESS getStandardUrlName:  "+access.getStandardUrlName());

				if (access != null) {
					urlList.add(new URL(access.getStandardUrlName()));
				}
			}         

			if (urlList.size() == 0) {
				throw new IOException("No HttpServer files found: "+catalogURL);
			}

			fileURLs = (URL[]) urlList.toArray(new URL[urlList.size()]);
		}

		listResults.setUrlList(fileURLs);
		listResults.setLocationType(ListResults.LOCATION_THREDDS);

		return listResults;
	}



	public ListResults listSSHDirectory(String server, String user, String password, String directory) 
	throws IOException, JSchException {
		SSHDirectory ssh = new SSHDirectory();

		// Get SSH Session
		if (sshSession == null) {
			sshSession = ssh.getSession(user, server);
		}

		String[] files = ssh.getFileNames(sshSession, directory);

		URL[] sshURLs = new URL[files.length];
		ListResults results = new ListResults();
		for (int n=0; n<files.length; n++) {
			sshURLs[n] = new URL("ssh://"+directory+"/"+files[n]);
		}
		results.setUrlList(sshURLs);
		results.setLocationType(ListResults.LOCATION_SSH);

		return results;

	}





	/**
	 *  Description of the Method
	 *
	 * @param  directoryURL               Description of the Parameter
	 * @return                            Description of the Return Value
	 * @exception  MalformedURLException  Description of the Exception
	 */
	private URL prepURL(URL directoryURL) throws MalformedURLException {

		String dirString = directoryURL.toString();

		// Add trailing '/' if needed
		if (!dirString.endsWith("/")) {
			dirString.concat("/");
		}

		// Sort by modification time if this is data from NWS server (sn.XXXX, etc...)
		if (dirString.startsWith("http://weather.noaa.gov/pub/SL.us008001/DF.of/DC.radar/")) {
			dirString = dirString.concat("?M=D");
		}

		return new URL(dirString);
	}







	/**
	 *  A simple object that contains info on dirname, file count, etc...
	 *
	 * @author    steve.ansari
	 */
	public class ListResults {

		/**
		 *  Description of the Field
		 */
		public final static int LOCATION_LOCAL = 1;
		/**
		 *  Description of the Field
		 */
		public final static int LOCATION_URL = 2;
		/**
		 *  Description of the Field
		 */
		public final static int LOCATION_FTP = 3;
		/**
		 *  Description of the Field
		 */
		public final static int LOCATION_THREDDS = 4;
		public final static int LOCATION_HAS = 5;
		public final static int LOCATION_CLASS = 6;
		public final static int LOCATION_SSH = 7;
		public static final int LOCATION_AWS = 8;

		private URL[] urlList = new URL[0];
		private FileInfo[] fileInfoList = new FileInfo[0];
		private int locationType;


		/**
		 * Returns the value of urlList.
		 *
		 * @return    The urlList value
		 */
		public URL[] getUrlList() {
			return urlList;
		}


		/**
		 * Sets the value of urlList.
		 *
		 * @param  urlList  The value to assign urlList.
		 */
		public void setUrlList(URL[] urlList) {
			this.urlList = urlList;
		}


		/**
		 * Sets the list of FileInfo objects - the array index location will match the URL list
		 * @param fileInfoList
		 */
		public void setFileInfoList(FileInfo[] fileInfoList) {
			this.fileInfoList = fileInfoList;
		}

		/**
		 * Gets the FileInfo array which gives more info (timestamp, filesize) about each URL.
		 * This is optional, and may return 'null' if no additional info is available.
		 * @return
		 */
		public FileInfo[] getFileInfoList() {
			return this.fileInfoList;
		}

		/**
		 * Returns the value of count.
		 *
		 * @return    The count value
		 */
		public int getCount() {
			return urlList.length;
		}


		/**
		 * Returns the value of locationType.
		 *
		 * @return    The locationType value
		 */
		public int getLocationType() {
			return locationType;
		}


		/**
		 * Sets the value of locationType.
		 *
		 * @param  locationType  The value to assign locationType.
		 */
		public void setLocationType(int locationType) {
			this.locationType = locationType;
		}




		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("DirectoryScanner.ListResults ::: count="+getCount()+" locationType="+getLocationType()+"\n\n");
			for (int n=0; n<getCount(); n++) {
				sb.append(urlList[n].toString()+"\n");  
			}
			return sb.toString();
		}

		public String toString(int numToPrint) {
			StringBuffer sb = new StringBuffer();
			sb.append("DirectoryScanner.ListResults ::: count="+getCount()+" locationType="+getLocationType()+"\n\n");
			for (int n=0; n<getCount() && n<numToPrint; n++) {
				sb.append(urlList[n].toString()+"\n");  
			}
			return sb.toString();
		}

	}






}

