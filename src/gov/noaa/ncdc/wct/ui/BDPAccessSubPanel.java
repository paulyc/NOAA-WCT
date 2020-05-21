package gov.noaa.ncdc.wct.ui;

import java.text.ParseException;

public interface BDPAccessSubPanel {

	public void setDate(String yyyymmdd) throws ParseException;
	public void setStatus(String status);
	public String getDataPath(String datasetID);
}
