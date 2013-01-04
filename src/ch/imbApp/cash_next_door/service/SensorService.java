package ch.imbApp.cash_next_door.service;

import java.util.LinkedList;
import java.util.Queue;

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
import ch.imbApp.cash_next_door.helper.Timer;

public class SensorService extends Service implements SensorEventListener {
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
	private boolean isHorizontal;
	private boolean isExceptionPopupVisible;

	private static final int matrix_size = 16;
	float[] R = new float[matrix_size];
	float[] outR = new float[matrix_size];
	float[] I = new float[matrix_size];
	float[] values = new float[3];
	private double azimuth;
	private double pitch;
	private double lastAzimuthUpd;
	private Queue<Double> directionChange = new LinkedList<Double>();
	
	static final float ALPHA = 0.8f;
	
	public static float swRoll;
	public static float swPitch;
	public static float swAzimuth;

	public static Sensor accelerometer;
	public static Sensor magnetometer;

	public static float[] mAccelerometer = null;
	public static float[] mGeomagnetic = null;

	private Timer timer = new Timer(100);
	private double roll;

	/** Called when the activity is first created. */

	public int onStartCommand(final Intent intent, final int flags,
			final int startId) {

		Log.i(TAG, "onCreate");

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(this, magnetometer,
				SensorManager.SENSOR_DELAY_GAME);
	
		for(int i = 0; i < 10; i++) {
			directionChange.add(0d);
		}
		System.out.println("Listeners registred!");
		return START_STICKY;
	}
	
	public void onSensorChanged(SensorEvent event) {

		// onSensorChanged gets called for each sensor so we have to remember
		// the values
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			mAccelerometer = event.values;
		}

		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			mGeomagnetic = event.values;
		}

		if (mAccelerometer != null && mGeomagnetic != null) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I,
					mAccelerometer, mGeomagnetic);

			if (success) {
				//float[] orientation = new float[3];

				float[] rotationMatrix = new float[9];  
				if(SensorManager.getRotationMatrix(rotationMatrix, mAccelerometer, mAccelerometer, mGeomagnetic)){
					float[] orientMatrix = new float[3];
					float[] remapMatrix = new float[9];
					SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remapMatrix);
					SensorManager.getOrientation(remapMatrix, orientMatrix);
	
					// at this point, orientation contains the azimuth(direction),
					// pitch and roll values.
					azimuth = orientMatrix[0] * 180 / Math.PI;
					pitch = orientMatrix[0] * 180 / Math.PI;
					roll = orientMatrix[0] * 180 / Math.PI;
				}
				
				int grow = 0;
				int sink = 0;
				double lastDif = 0;
				double avgDif = 0;
				for(Double d : directionChange) {
					if(d-1.5 > azimuth) {
						sink++;
					}
					else if(d+1.5<azimuth){
						grow ++;
					}
					lastDif = d - azimuth;
					avgDif = d-azimuth;
				}
				directionChange.add(azimuth);
				directionChange.remove();

				avgDif = avgDif / 10;
				if(sink > 8 || grow > 8) {
					if(lastDif<5 && lastDif > -5) {

						azimuth -= avgDif/2;
						sendMessageToUI();
						azimuth += avgDif/2;
					}
				}
			}
		}

	}

	protected void onPause() {

		sensorManager.unregisterListener(this, accelerometer);
		sensorManager.unregisterListener(this, magnetometer);
	}

	private void sendMessageToUI() {

		if (mClient != null && timer.isTimeReached()) {
			Bundle b = new Bundle();
			isHorizontal = pitch > -115 && pitch < -50;
			isHorizontal = true;
			if (isExceptionPopupVisible && isHorizontal) {
				b.putBoolean(POPUP_FIELD, false);
				isExceptionPopupVisible = false;
				Message msg = Message.obtain(null, MSG_SET_BOOLEAN_VALUE);
				msg.setData(b);
				sendMsg(msg);
			} else if (!isExceptionPopupVisible && !isHorizontal) {
				b.putBoolean(POPUP_FIELD, true);
				isExceptionPopupVisible = true;
				Message msg = Message.obtain(null, MSG_SET_BOOLEAN_VALUE);
				msg.setData(b);
				sendMsg(msg);
			}

			if (lastAzimuthUpd - azimuth > 2 || lastAzimuthUpd - azimuth < -2) {
				b.putDouble("azimuth", azimuth);
				Message msg = Message.obtain(null, MSG_SET_STRING_VALUE);
				msg.setData(b);
				sendMsg(msg);
			}
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
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	class IncomingHandler extends Handler { // Handler of incoming messages from
											// clients.

		@Override
		public void handleMessage(Message msg) {
			mClient = msg.replyTo;
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		sensorManager.unregisterListener(this, accelerometer);
		sensorManager.unregisterListener(this, magnetometer);
	}
	
}
