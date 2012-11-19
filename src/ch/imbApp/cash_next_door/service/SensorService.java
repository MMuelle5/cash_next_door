package ch.imbApp.cash_next_door.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;


public class SensorService  extends Service implements SensorEventListener {
	private static final String TAG = "SensorService";

    public static final int MSG_REGISTER_CLIENT = 2;
    public static final int MSG_SET_STRING_VALUE = 4;
    public static final int MSG_SET_BOOLEAN_VALUE = 5;
    
    public static final String POPUP_FIELD = "isPoupupVisible";
    
	private SensorManager sensorManager;
	Messenger mClient;
    private NotificationManager nm;
	private long lastUpdate;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
	private float x;
	private boolean isHorizontal;
	private boolean isExceptionPopupVisible;

	  
	/** Called when the activity is first created. */

	public int onStartCommand(final Intent intent, final int flags, final int startId) {

		Log.i(TAG, "onCreate");
		
	    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    lastUpdate = System.currentTimeMillis();
	    System.out.println(sensorManager);

	    sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
		return START_STICKY;
	  }

	  public void onSensorChanged(SensorEvent event) {

	    if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
	      getAccelerometer(event);
	    }

	  }

	  private void getAccelerometer(SensorEvent event) {
	    float[] values = event.values;
	    // Movement
	    x = values[0];
	    float y = values[1];
	    float z = values[2];
	    
		isHorizontal = y>-115 && y<-50;
		
		sendMessageToUI();
	  }

//	  @Override
	  public void onAccuracyChanged(Sensor sensor, int accuracy) {

	  }

	    
	    private void sendMessageToUI() {
	    	
	    	if(mClient != null) {
		    	Bundle b = new Bundle();
		    	if(isExceptionPopupVisible && isHorizontal) {
		    		b.putBoolean(POPUP_FIELD, false);
		    		isExceptionPopupVisible = false;
		            Message msg = Message.obtain(null, MSG_SET_BOOLEAN_VALUE);
		            msg.setData(b);
		            sendMsg(msg);
		    	}
		    	else if(!isExceptionPopupVisible && !isHorizontal) {
		    		b.putBoolean(POPUP_FIELD, true);
		    		isExceptionPopupVisible = true;
		            Message msg = Message.obtain(null, MSG_SET_BOOLEAN_VALUE);
		            msg.setData(b);
		            sendMsg(msg);
		    	}
		    	
	            b.putDouble("x", x);
	            Message msg = Message.obtain(null, MSG_SET_STRING_VALUE);
	            msg.setData(b);
	            sendMsg(msg);
	    	}
	    }
	   
	private void sendMsg(Message msg) {

        try {
			mClient.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	    
	@Override
	public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
	}


	@Override
	public void onCreate() {

        showNotification();
	}
	
    private void showNotification() {
        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }

    class IncomingHandler extends Handler { // Handler of incoming messages from clients. 

        @Override
        public void handleMessage(Message msg) {
        	mClient = msg.replyTo;
        }
    }
}
