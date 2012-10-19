package ch.imbApp.cash_next_door;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class GPSListener implements LocationListener {
	
	public static double latitude;
	public static double longitude;
    
	public void onLocationChanged(Location loc) {

		loc.getLatitude();  
        loc.getLongitude();  
        latitude=loc.getLatitude();  
        longitude=loc.getLongitude();  
	}

	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

}
