package bane.dataservice.users;

import java.io.IOException;
import java.io.InputStream;
import java.util.MissingFormatArgumentException;
import java.util.Properties;

public class SOAPUserXMLDataService extends UserXMLDataService {

	private static SOAPUserXMLDataService instance = null;

	private SOAPUserXMLDataService() throws MissingFormatArgumentException, IOException {
		super();
		// TODO Auto-generated constructor stub
	}

	public static SOAPUserXMLDataService getInstance() {
		if (instance == null) {
			try {
				instance = new SOAPUserXMLDataService();
			} catch (MissingFormatArgumentException | IOException e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	@Override
	public void loadConfig() throws MissingFormatArgumentException, IOException {
		
		Properties prop = new Properties();
		InputStream input = null;
		input = getClass().getClassLoader().getResourceAsStream("../service_config.properties");
		// load a properties file
		prop.load(input);

		String value = prop.getProperty("USERS_XML_FILE_PATH");
		if (value != null) {
			USERS_XML_FILE_PATH = value;
		} else {
			throw new MissingFormatArgumentException("USERS_XML_FILE_PATH nije definisan u config fajlu.");
		}
		input.close();
		
	}
}
