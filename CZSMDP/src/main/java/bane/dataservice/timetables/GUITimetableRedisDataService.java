package bane.dataservice.timetables;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.MissingFormatArgumentException;
import java.util.Properties;


public class GUITimetableRedisDataService extends TimetableRedisDataService {

	private static GUITimetableRedisDataService instance = null;
	private static String ROOT_RESOURCE = "./resources/";

	protected GUITimetableRedisDataService() throws MissingFormatArgumentException, IOException {
		super();
		// TODO Auto-generated constructor stub
	}

	public static GUITimetableRedisDataService getInstance() {
		if (instance == null) {
			try {
				instance = new GUITimetableRedisDataService();
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

		String value = prop.getProperty("SKEY");
		if (value != null) {
			SKEY = value;
		} else {
			throw new MissingFormatArgumentException("SKEY nije definisan u config fajlu.");
		}
		value = prop.getProperty("REDIS_ADDRESS");
		if (value != null) {
			REDIS_ADDRESS = value;
		} else {
			throw new MissingFormatArgumentException("REDIS_ADDRESS nije definisan u config fajlu.");
		}
	}

	
}
