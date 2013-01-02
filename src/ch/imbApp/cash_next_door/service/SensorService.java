package ch.imbApp.cash_next_door.service;

import ch.imbApp.cash_next_door.helper.Timer;
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
	private float[] accels;
	private float[] mags;

	private float[] gravity = new float[3];
	private float[] geomag = new float[3];
	private float[] rotationMatrix = new float[16];
	
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

		System.out.println("Listeners registred!");
		return START_STICKY;
	}

	public void onSensorChanged(SensorEvent event) {

		// onSensorChanged gets called for each sensor so we have to remember
		// the values
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			//with lowPass Filter: 
			//mAccelerometer = lowPass(event.values, mAccelerometer);
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
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				// at this point, orientation contains the azimuth(direction),
				// pitch and roll values.
				azimuth = 180 * orientation[0] / Math.PI;
				pitch = 180 * orientation[1] / Math.PI;
				roll = 180 * orientation[2] / Math.PI;
				sendMessageToUI();
			}
		}

	}

	
	/**
	 * @see http://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
	 * @see http://developer.android.com/reference/android/hardware/Sensor.html#TYPE_ACCELEROMETER
	 */
	protected float[] lowPass(float[] input, float[] output) {
		if (output == null) return input;

	    for (int i = 0; i < input.length; i++) {
	        output[i] = output[i] + ALPHA * (input[i] - output[i]);
	    }
	    return output;
	}
	
	
	private void getAccelerometer(SensorEvent event) {
		float[] values = event.values;
		// Movement
		float x = values[0];
		float y = values[1];
		float z = values[2];

		isHorizontal = y > -115 && y < -50;

		sendMessageToUI();
	}

	protected void onPause() {

		sensorManager.unregisterListener(this, accelerometer);
		sensorManager.unregisterListener(this, magnetometer);
	}

	private void sendMessageToUI() {

		if (mClient != null && timer.isTimeReached()) {
			Bundle b = new Bundle();
			isHorizontal = pitch > -115 && pitch < -50;
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
}
