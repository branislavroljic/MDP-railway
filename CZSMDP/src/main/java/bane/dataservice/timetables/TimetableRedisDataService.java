package bane.dataservice.timetables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.Set;

import com.google.gson.Gson;

import bane.model.Station;
import bane.model.Timetable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

//apstraktna klasa za rad sa redovima voznje
public abstract class TimetableRedisDataService {

	private JedisPool pool;
	private Jedis jedis;
	protected String SKEY;
	protected String REDIS_ADDRESS;

	private Gson gson = new Gson();

	protected TimetableRedisDataService() throws MissingFormatArgumentException, IOException {
		loadConfig();
		pool = new JedisPool(REDIS_ADDRESS);
		jedis = pool.getResource();
	}

	public abstract void loadConfig() throws MissingFormatArgumentException, IOException;

	public ArrayList<Timetable> getTimetables() {
		ArrayList<Timetable> timetables = new ArrayList<Timetable>();
		ScanParams params = new ScanParams();
		Set<String> matchingKeys = new HashSet<>();
		params.match(SKEY + "*");
		// trazim sve stanice koristeci regex timetable:*
		String nextCursor = "0";
		do {
			ScanResult<String> scanResult = jedis.scan(nextCursor, params);
			List<String> keys = scanResult.getResult();
			nextCursor = scanResult.getStringCursor();

			matchingKeys.addAll(keys);
		} while (!nextCursor.equals("0"));

		for (String key : matchingKeys) {
			timetables.add(gson.fromJson(jedis.get(key), Timetable.class));
		}
		return timetables;
	}
	//

	// timetable:ID jsonString
	// ako je uspjesno -> ima dati kljuc
	public boolean addTimetable(Timetable timetable) {
		jedis.set(SKEY + timetable.getID(), gson.toJson(timetable));
		jedis.save();
		return jedis.exists(SKEY + timetable.getID());
	}

	public boolean update(String timetableID, Station station) {
		Timetable timetable = gson.fromJson(jedis.get(SKEY + timetableID), Timetable.class);

		List<Station> stations = timetable.getStations();
		int index = stations.indexOf(station);
		if (index >= 0) {
			station.setName(stations.get(index).getName());
			station.setExpectedTime(stations.get(index).getExpectedTime());
			stations.set(index, station);

			jedis.set(SKEY + timetableID, gson.toJson(timetable));
			jedis.save();
			return true;
		} else
			return false;

	}

	public boolean deleteTimetable(String ID) {
		return jedis.del(SKEY + ID) == 0 ? false : true;
	}

}
