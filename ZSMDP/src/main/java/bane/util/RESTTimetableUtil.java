package bane.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import bane.model.Timetable;

public class RESTTimetableUtil {

	
	private static Gson gson = new Gson();

	/*
	 * slanje HTTP GET zahtjeva na korespodentni URL, 
	 */
	public static ArrayList<Timetable> getTimetables(String urlString) {
		ArrayList<Timetable> timetables = null;;
		try {
			URL url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/json");

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("Failed : Http error code: " + connection.getResponseCode());
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
			String jsonString = sb.toString();

			timetables = gson.fromJson(jsonString, new TypeToken<ArrayList<Timetable>>(){}.getType());
			in.close();
			connection.disconnect();
		} catch (IOException e) {
			Logger.getLogger(RESTTimetableUtil.class.getName()).log(Level.WARNING, null, e);
		}

		return timetables;
	}

	public static boolean markPassage(String urlString, int stationID, String time) {
		try {
			URL url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept", "application/json");

			JSONObject stationJObject = new JSONObject(
					"{ \"ID\":" + (stationID + "") + ",\"passsed\":true, \"passageTime\":" + "\"" + time + "\"" + "}");

			OutputStream out = connection.getOutputStream();
			out.write(stationJObject.toString().getBytes());
			out.flush();

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
			}

			out.close();
			connection.disconnect();
			return true;
		} catch (IOException e) {
			Logger.getLogger(RESTTimetableUtil.class.getName()).log(Level.WARNING, null, e);
			return false;
		}
	}
}
