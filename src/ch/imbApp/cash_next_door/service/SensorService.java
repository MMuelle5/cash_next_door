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
	private double x;
	private boolean isHorizontal;
	private boolean isExceptionPopupVisible;

    private static final int matrix_size = 16;
    float[] R = new float[matrix_size];
    float[] outR = new float[matrix_size];
    float[] I = new float[matrix_size];
    float[] values = new float[3];
    private double azimuth;
    private double pitch;
    private float[] accels;
    private float[] mags;
	  
    private float[] gravity = new float[3];
    private float[] geomag = new float[3];
    private float[] rotationMatrix = new float[16];
    
	/** Called when the activity is first created. */

	public int onStartCommand(final Intent intent, final int flags, final int startId) {

		Log.i(TAG, "onCreate");

//	    SensorManager.getRotationMatrix(rMatrix, null, accelerometerValues, magneticValues);
//	    SensorManager.remapCoordinateSystem(rMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
//	    SensorManager.getOrientation(outR, orientationValues);
	    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    lastUpdate = System.currentTimeMillis();
	    System.out.println(sensorManager);


	    sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
	    sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
	  
		return START_STICKY;
	  }

	  public void onSensorChanged(SensorEvent event) {

//	    if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
//	      getAccelerometer(event);
//	    }
	    /**
	     * Start test
	     */
		    int type=event.sensor.getType();
		    
		    //Smoothing the sensor data a bit
		    if (type == Sensor.TYPE_MAGNETIC_FIELD) {
		      geomag[0]=(geomag[0]*1+event.values[0])*0.5f;
		      geomag[1]=(geomag[1]*1+event.values[1])*0.5f;
		      geomag[2]=(geomag[2]*1+event.values[2])*0.5f;
		    } else if (type == Sensor.TYPE_ACCELEROMETER) {
		      gravity[0]=(gravity[0]*2+event.values[0])*0.33334f;
		      gravity[1]=(gravity[1]*2+event.values[1])*0.33334f;
		      gravity[2]=(gravity[2]*2+event.values[2])*0.33334f;
		    }
		    
		    if ((type==Sensor.TYPE_MAGNETIC_FIELD) || (type==Sensor.TYPE_ACCELEROMETER)) {
			    rotationMatrix = new float[16];
			    SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomag);
			    SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, rotationMatrix );
			     
			    SensorManager.getOrientation(rotationMatrix, values);
			     
			//		      System.out.println("values: "+values[0] +" "+ Math.toDegrees(values[0]));
			      
			    x=Math.toDegrees(values[0]);
			     
			    double y = Math.toDegrees(values[1]);
				isHorizontal = y>-115 && y<-50;

				sendMessageToUI();
		    }  
		    
//	    if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
//	    	return;
//	    }
//        switch (event.sensor.getType()) {
//                case Sensor.TYPE_MAGNETIC_FIELD:
//                        mags = event.values.clone();
//                        break;
//        case Sensor.TYPE_ACCELEROMETER:
//                        accels = event.values.clone();
//                        break;
//        }
//
//        if (mags != null && accels != null) {
//
//                SensorManager.getRotationMatrix(R, I, accels, mags);
//
//                // Correct if screen is in Landscape
//	                            SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
//
//	                            float[] val = SensorManager.getOrientation(outR, values);
//
//	                            azimuth = Math.toDegrees(values[0]);
////	                            System.out.println("azimuth:" +azimuth+" "+values[0]);
//	                            System.out.println("val:" +Math.toDegrees(val[0])+" "+val[0]);
//                pitch = Math.toDegrees(values[1]);
////	                            roll = Math.toDegrees(values[2]);
//
//        		sendMessageToUI();
//        }

	    /**
	     * End test
	     */

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
		    	
	            b.putDouble("x",  Math.toDegrees(values[0]));
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
