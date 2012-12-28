package ch.imbApp.cash_next_door.calc;

import android.location.Location;
import ch.imbApp.cash_next_door.bean.BankOmat;
import java.util.ArrayList;
import java.util.List;

public class AutomatenLoader {

	public static List<BankOmat> getBankomaten(Location loc) {
		List<BankOmat> retList = new ArrayList<BankOmat>();

		String requestUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
		//String location = loc.getLatitude() + "," + loc.getLongitude();
		String location = "47.392132,8.518366";
		String rankby = "distance";
		String types = "atm";
		String sensor = "true";
		String key = "AIzaSyC5yZvpQSXq8e1x1LZTPXsSt6o0YohBNns"; // my personal API Key... is limited!!!

		requestUrl += "location=" + location
				+ "&rankby=" + rankby
				+ "&types=" + types
				+ "&sensor=" + sensor
				+ "&key=" + key;

		/*
		 * Request for Google Places API
		 * 
		 * https://maps.googleapis.com/maps/api/place/nearbysearch/json?&location=47.392132,8.518366&rankby=distance&types=atm&sensor=true&key=AIzaSyC5yZvpQSXq8e1x1LZTPXsSt6o0YohBNns
		 * 
		 * response: (only first 5 items)
		 */
		/*
		
		{
		"html_attributions" : [],
		"next_page_token" : "ClROAAAAC8hKYBtFyFh_N2MSx2exyZBIsBYPmGQwBb0F7Y0LXJmP_ELNinDAftFN24OXrsUqqCkVEfwZ8ewd5JqLt2WzPmoEe9kU1nIHUIFt7xtNNHkSEGGjT-DBzuttfYgj5y6GTQMaFA9PyFCTlkugNajespSuMv2BBCuE",
		"results" : [
		{
		 "geometry" : {
		    "location" : {
		       "lat" : 47.3901120,
		       "lng" : 8.517485000000001
		    }
		 },
		 "icon" : "http://maps.gstatic.com/mapfiles/place_api/icons/generic_business-71.png",
		 "id" : "cd45874ffc959eff4c6ac69462161d7e19bf184a",
		 "name" : "Postomat",
		 "reference" : "CpQBgwAAAGdq1Q7ygcf02fhVNZ-PHR3t0LKBAYiCkfxeO_07eVqokh2k4L0BN-wYJBIRgjHRylKk2rXHfTtuZOTluw6R1esWfZOdAt1VlnNSX0Tltd-gPqPpG0qt_Hkd5W31UuqHmLxZ5GdK-fxy0glUPZtAfFDz10DXbjMP15aox-RMer803jctRRBYmze_ja_xekdCxRIQzXA25ebB68RsJR1fb8dA2hoUqoKgYIEF5uLFYFjV0OYDXHSw920",
		 "types" : [ "atm", "finance", "establishment" ],
		 "vicinity" : "Giessereistrasse 18, Zürich"
		},
		{
		 "geometry" : {
		    "location" : {
		       "lat" : 47.3927540,
		       "lng" : 8.523764999999999
		    }
		 },
		 "icon" : "http://maps.gstatic.com/mapfiles/place_api/icons/generic_business-71.png",
		 "id" : "c9adfda7bb6665ec98727b7e914d617983a8f111",
		 "name" : "Postomat",
		 "reference" : "CoQBgAAAAPOhsDpP1J_dXWvftZ8H2VjZREv5B83io74P4le7UEmWSHrrWTkVroh2qOyPfOXnkVhKbMKvQUxEhZORijZ_nCVAIFMkVrK_6TVPvkMRbLTez3vK4WbjBqEmbtFHpytqGz6eEXjFqvY3eca3rG81E8VpzfoEfTihQdRTxY3iP4xKEhDsgzgiBSutLanWuBT_jEGxGhQrM50CPAsZZhj2sK2KuZWZotqfXg",
		 "types" : [ "atm", "finance", "establishment" ],
		 "vicinity" : "Wipkingerplatz 7, Zürich"
		},
		{
		 "geometry" : {
		    "location" : {
		       "lat" : 47.3902660,
		       "lng" : 8.5234880
		    }
		 },
		 "icon" : "http://maps.gstatic.com/mapfiles/place_api/icons/generic_business-71.png",
		 "id" : "85df7b7292b37873b44f52d432eeb411db5feec8",
		 "name" : "Postomat",
		 "reference" : "CpQBgQAAAMDXh1BqNvmJPBvpbs4e2CutbcFr9ot-OTTf_sQJz9ZzGH9iMxewq5vFcybipxuK5MN00xThnh3hEhiJZOvYNgFq9D-UkQ_dldNyE4z4i8_n7JH8c_HwmcAhuKZoeTDakVEcjuRkW1U34a6kf6zzmL1fh0dwI4og62aej1YteYE40UVh5J9wuRh1MFBT7IL1exIQitqQa9SAjUXCreYsFBBsRxoUJadYVV67LqarpBZf41MbITSy7Zk",
		 "types" : [ "atm", "finance", "establishment" ],
		 "vicinity" : "Limmatstrasse 310, Zürich"
		},
		{
		 "geometry" : {
		    "location" : {
		       "lat" : 47.3869550,
		       "lng" : 8.519660999999999
		    }
		 },
		 "icon" : "http://maps.gstatic.com/mapfiles/place_api/icons/generic_business-71.png",
		 "id" : "dd35bb4d742fa9e1ad88140c0a940800a0ff3625",
		 "name" : "Zürcher Kantonalbank",
		 "reference" : "CoQBewAAAFW9XMgED7hrTOy0aP8SlaATA8h6THEa_JUYsCYEQqrjUWhplB5W-coV3n5mw413MYxyloFValfxRC6l7CFZqEmrWDV5rWqaNBwZ6PBzJa9-34C5NdnjxMH8BTKdPFwvM08zNbP53RcT86jte42h7jwDjgqZsclQohhSIKqQiWvhEhC7jJyCy6NB_BSnk6s9GV0mGhRwSj9ZtBbg-YV5IWP3_k4iGD4gSw",
		 "types" : [ "atm", "finance", "establishment" ],
		 "vicinity" : "Escher Wyss, Zürich"
		},
		{
		 "geometry" : {
		    "location" : {
		       "lat" : 47.3840520,
		       "lng" : 8.532590000000001
		    }
		 },
		 "icon" : "http://maps.gstatic.com/mapfiles/place_api/icons/generic_business-71.png",
		 "id" : "195b8a895bedd94d8f0d94a6a309edc18fb93f10",
		 "name" : "Postomat",
		 "reference" : "CpQBgQAAAN2himixuZs5NyuCQxY24lLGTCZecExFiVH3CPV77AvHps1z8eJKbIHjsO8oU-cqM_QLhdVEq57cidNfHnWgped64H-YffbL0190NM1aI-Q7-Rb8zowRXDilIL6yrigQw56jVmIBoS-tyl9UrFvpMx9Z_UHTPDvSX7LZ_wJoV6PPXbgU9-mwhXxmqmI19RuedBIQJ2dpH7gdk_1Kf38iQ7hPERoUeZ-iGnf2wEOofuynyxymVo3zJNA",
		 "types" : [ "atm", "finance", "establishment" ],
		 "vicinity" : "Limmatstrasse 118, Zürich"
		}
		],
		"status" : "OK"
		}		 
		*/

		BankOmat val = new BankOmat();
		val.setLocation(new Location("dummy"));
		val.getLocation().setLatitude(47.377847d);
		val.getLocation().setLongitude(8.532292d);
		val.setBankName("UUUBEEE���SS");
		retList.add(val);

		val = new BankOmat();
		val.setLocation(new Location("dummy"));
		val.setBankName("CEEEE�SS");
		val.getLocation().setLatitude(47.377847d);
		val.getLocation().setLongitude(8.540078d);
		retList.add(val);

		return retList;
	}
}
