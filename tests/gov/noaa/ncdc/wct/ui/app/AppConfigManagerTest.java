package gov.noaa.ncdc.wct.ui.app;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.xml.sax.SAXException;

public class AppConfigManagerTest {

	@Test
	public void testAppConfig() throws NumberFormatException, XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		AppConfigManager appConfig = AppConfigManager.getInstance();
		System.out.println(appConfig.getAppList());
	}
}
