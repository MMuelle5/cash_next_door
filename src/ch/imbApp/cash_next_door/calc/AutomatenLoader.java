package ch.imbApp.cash_next_door.calc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import ch.imbApp.cash_next_door.bean.BankOmat;

public class AutomatenLoader implements Runnable {

	public List<BankOmat> machineList = new ArrayList<BankOmat>();
	public List<BankOmat> completeMachineList = new ArrayList<BankOmat>();
	public double longitude;
	public double latitude;
	public String preferedMachine;

	public static JSONObject makeHttpJsonRequest(String url) {
		StringBuilder myContent = new StringBuilder();
		JSONObject jsonObject = null;

		try {
			URL requestUrl = new URL(url);
			URLConnection connection = requestUrl.openConnection();
				
		   BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "ISO-8859-1"));
	 
		   String inputLine;
	 
		   while ((inputLine = br.readLine()) != null){
		      myContent.append(inputLine);
		   }
		   br.close();

		   jsonObject = new JSONObject(myContent.toString());

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return jsonObject;

	}

	public void run() {

		machineList = new ArrayList<BankOmat>();

		String requestUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
		String location = latitude + "," + longitude;
		String rankby = "distance";
		String types = "atm";
		String sensor = "true";
		String apiKey = "AIzaSyC5yZvpQSXq8e1x1LZTPXsSt6o0YohBNns"; // my
																	// personal
																	// API
																	// Key... is
																	// limited!!!

		requestUrl += "location=" + location + "&rankby=" + rankby + "&types="
				+ types + "&sensor=" + sensor + "&key=" + apiKey;

//		 JSONObject jsonObject = makeHttpJsonRequest(requestUrl);
		JSONObject jsonObject = makeHttpJsonRequest("http://der-esel.ch/stuff/hszt/handheld/json_response.json");

		try {
			JSONArray resultArray = (JSONArray) jsonObject.get("results");

			for (int i = 0; i < resultArray.length(); i++) {
				JSONObject result = resultArray.getJSONObject(i);

				Iterator<?> keys = result.keys();

				BankOmat machine = new BankOmat();

				while (keys.hasNext()) {
					String key = (String) keys.next();

					if ("name".equals(key)) {
						machine.setBankName((String) result.get(key));
					}

					if ("vicinity".equals(key)) {
						machine.setBankAddress((String) result.get(key));
					}

					if ("geometry".equals(key)) {
						JSONObject locationObject = (JSONObject) result
								.get(key);
						JSONObject coordinates = (JSONObject) locationObject
								.get("location");
						Location loc = new Location("google");
						loc.setLatitude((Double) coordinates.get("lat"));
						loc.setLongitude((Double) coordinates.get("lng"));

						machine.setLocation(loc);
					}
				}
				completeMachineList.add(machine);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if(preferedMachine != null) {
			for(BankOmat mach : completeMachineList) {
				machineList.add(mach);
			}
		}
		
		for(int i = 0; i < completeMachineList.size() && i < 6; i++) {
			machineList.add(completeMachineList.get(i));
		}
		
	}

}
