package bane.dataservice.timetables;

import java.io.IOException;
import java.io.InputStream;
import java.util.MissingFormatArgumentException;
import java.util.Properties;


public class RESTTimetableRedisDataService extends TimetableRedisDataService {

	private static RESTTimetableRedisDataService instance = null;

	protected RESTTimetableRedisDataService() throws MissingFormatArgumentException, IOException {
		super();
	}

	@Override
	public void loadConfig() throws MissingFormatArgumentException, IOException {
		Properties prop = new Properties();
		InputStream input = null;
		input = getClass().getClassLoader().getResourceAsStream("../service_config.properties");
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
		input.close();
	}

	public static RESTTimetableRedisDataService getInstance() {
		if (instance == null) {
			try {
				instance = new RESTTimetableRedisDataService();
			} catch (MissingFormatArgumentException | IOException e) {
				e.printStackTrace();
			}
		}
		return instance;
	}
}
