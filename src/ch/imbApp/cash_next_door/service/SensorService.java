package ch.imbApp.cash_next_door.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import ch.imbApp.cash_next_door.alert.Alerts;

public class SensorService  extends Service implements SensorEventListener {
	  private SensorManager sensorManager;
	  private long lastUpdate;

	final Context context = this;

	  
	/** Called when the activity is first created. */

	public int onStartCommand(final Intent intent, final int flags, final int startId) {

	    Alerts.init(context);

	    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    lastUpdate = System.currentTimeMillis();
	    
		return START_STICKY;
	  }

//	  @Override
	  public void onSensorChanged(SensorEvent event) {
	    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
	      getAccelerometer(event);
	    }

	  }

	  private void getAccelerometer(SensorEvent event) {
	    float[] values = event.values;
	    // Movement
//	    float x = values[0];
	    float y = values[1];
//	    float z = values[2];
	    

		boolean isHorizontal = y>6 && y<14;
		if(!isHorizontal) {
			Alerts.showDialog(Alerts.ALERT_HORIZONTALE);
		} else{
			Alerts.hideDialog();
		}
		
	  }

//	  @Override
	  public void onAccuracyChanged(Sensor sensor, int accuracy) {

	  }


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
