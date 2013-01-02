package ch.imbApp.cash_next_door.service;

import java.util.ArrayList;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class GpsService extends Service implements LocationListener {
	
	private static final String TAG = "GpsService";
    private NotificationManager nm;

    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    int mValue = 0; // Holds last value set by a client.
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_SET_STRING_VALUE = 4;
    final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.

	public final static String NEW_LOCATION ="neui.location";

	private LocationManager locationManager;
	public Location location;
	public Double latitude = Double.valueOf(0d);
	public Double longitude = Double.valueOf(0d);
	public Float direction = Float.valueOf(0f);
	
	Intent intent;
	
	public GpsService() {
		super();
	}

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
    


	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	public void onLocationChanged(Location arg0) {
        location = arg0;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        direction = location.getBearing();
        
        System.out.println("direction: "+direction+"°"+ " lat: "+latitude+" lon: "+longitude);
        sendMessageToUI();
	}

	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		
		Log.i(TAG, "onCreate");
		
		this.intent = intent;
		
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
		String best = locationManager.getBestProvider(criteria, true);
		locationManager.requestLocationUpdates(best, 0, 0, this);

		
//	    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        
//        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        System.out.println("enabled"+enabled);
//
//        System.out.println();
//        if(enabled) {
//        	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
//        }
//        else {
//        	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
//        }
//
//	    Criteria criteria = new Criteria();
//	    String provider = locationManager.getBestProvider(criteria, false);
	    Location location = locationManager.getLastKnownLocation(best);

	    System.out.println("best: "+best);
	    System.out.println(location);
		if(location != null) {
			onLocationChanged(location);
		}
		
		return START_STICKY;

	}
//	@Override
//	protected void onHandleIntent(Intent intent) {
//		this.intent = intent;
//	
//	    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        
//        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        System.out.println("enabled"+enabled);
//		if (!enabled) {
//		  Intent in = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//		  startActivity(intent);
//    	}
//		
//		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
//
//	    Criteria criteria = new Criteria();
//	    String provider = locationManager.getBestProvider(criteria, false);
//	    Location location = locationManager.getLastKnownLocation(provider);
//		onLocationChanged(location);
//
//	}

	@Override
	public void onCreate() {

        showNotification();
	}
	
    private void showNotification() {
        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        // In this sample, we'll use the same text for the ticker and the expanded notification
//        CharSequence text = getText(R.id.latitude);
//        // Set the icon, scrolling text and timestamp
//        Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
//        // The PendingIntent to launch our activity if the user selects this notification
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
//        // Set the info for the views that show in the notification panel.
//        notification.setLatestEventInfo(this, getText(R.id.latitude), text, contentIntent);
//        // Send the notification.
//        // We use a layout id because it is a unique number.  We use it later to cancel.
//        nm.notify(R.id.latitude, notification);
    }
    
    private void sendMessageToUI() {
        for (int i=mClients.size()-1; i>=0; i--) {
            try {

                //Send values
                Bundle b = new Bundle();
                b.putDoubleArray("location", new double[]{longitude, latitude,direction});
//                b.putDouble("latitude", latitude);
                Message msg = Message.obtain(null, MSG_SET_STRING_VALUE);
                msg.setData(b);
                mClients.get(i).send(msg);

//                b = new Bundle();
//                b.putDouble("longitude", longitude);
//                msg.setData(b);
//                mClients.get(i).send(msg);
//
//                b = new Bundle();
//                b.putFloat("direction", direction);
//                msg.setData(b);
//                mClients.get(i).send(msg);
                
            } catch (RemoteException e) {
            	e.printStackTrace();
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }
    
    class IncomingHandler extends Handler { // Handler of incoming messages from clients. 

        @Override
        public void handleMessage(Message msg) {
            mClients.add(msg.replyTo);
        }
    }
}
