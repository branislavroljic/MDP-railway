package bane.dataservice.users;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.MissingFormatArgumentException;
import java.util.Properties;

public class GUIUserXMLDataService extends UserXMLDataService {

	private static GUIUserXMLDataService instance = null;
	private static String ROOT_RESOURCE = "./resources/";

	private GUIUserXMLDataService() throws MissingFormatArgumentException, IOException {
		super();
		// TODO Auto-generated constructor stub
	}

	public static GUIUserXMLDataService getInstance() {
		if (instance == null) {
			try {
				instance = new GUIUserXMLDataService();
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

		input = new FileInputStream(ROOT_RESOURCE + "czsmdp_gui_config.properties");
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
