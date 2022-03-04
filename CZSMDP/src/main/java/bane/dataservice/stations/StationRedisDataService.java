package bane.dataservice.stations;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.Properties;
import java.util.Set;

import com.google.gson.Gson;

import bane.model.Station;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

//pomocna klasa za rad informacijama o Stanicama koje su smejstene u Redis bazi 
public class StationRedisDataService {

	private JedisPool pool;
	private Jedis jedis;
	private static String STATION_KEY;

	public static String ROOT_RESOURCE = "./resources/";

	private Gson gson = new Gson();

	public StationRedisDataService() throws MissingFormatArgumentException, IOException {
		loadConfig();
		pool = new JedisPool("localhost");
		jedis = pool.getResource();
	}

	public void loadConfig() throws MissingFormatArgumentException, IOException {
		Properties prop = new Properties();
		InputStream input = null;

		input = new FileInputStream(ROOT_RESOURCE + "czsmdp_gui_config.properties");
		// load a properties file
		prop.load(input);

		String value = prop.getProperty("STATION_KEY");
		if (value != null) {
			STATION_KEY = value;
		} else {
			throw new MissingFormatArgumentException("STATION_KEY nije definisan u config fajlu.");
		}
	}

	public ArrayList<Station> getStations() {
		ArrayList<Station> stations = new ArrayList<>();
		ScanParams params = new ScanParams();
		Set<String> matchingKeys = new HashSet<>();
		params.match(STATION_KEY + "*");
		// trazim sve stanice koristeci regex station:*
		String nextCursor = "0";

		do {
			ScanResult<String> scanResult = jedis.scan(nextCursor, params);
			List<String> keys = scanResult.getResult();
			nextCursor = scanResult.getStringCursor();

			matchingKeys.addAll(keys);
		} while (!nextCursor.equals("0"));

		for (String key : matchingKeys) {
			stations.add(gson.fromJson(jedis.get(key), Station.class));
		}
		return stations;
	}

	// dodajem stanicu po formatu station:ID jsonString
	public boolean addStation(Station station) {
		jedis.set(STATION_KEY + station.getID(), gson.toJson(station));
		jedis.save();
		// ako je dodata stanica -> postoji kljuc
		return jedis.exists(STATION_KEY + station.getID());
	}

	public boolean deleteStation(String ID) {
		return jedis.del(STATION_KEY + ID) == 0 ? false : true;
	}

}
