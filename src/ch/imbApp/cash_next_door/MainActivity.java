package ch.imbApp.cash_next_door;

import android.app.Activity;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static String TAG = "cash-next-door";

	Button gogoButton;
	TextView locationText;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		setContentView(R.layout.activity_main);
		// alert = new AlertDialog(R.layout.main);

		addListenerOnGogoButton();
	}
	
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_main, menu);
//        return true;
//    }

	private void addListenerOnGogoButton() {

		gogoButton = (Button) findViewById(R.id.gogoButton);

		gogoButton.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {

				LocationManager mlocManager = null;
				LocationListener mlocListener;
				mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				mlocListener = new GPSListener();
				mlocManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 0, 0, mlocListener);

				if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					// GPS-Signal nicht gefunden?
					 if(GPSListener.latitude>0)
					 {
						String text = "Latitude: " + GPSListener.latitude + "\n";
						text += "Longitude: " + GPSListener.longitude;
	
						Toast.makeText(getApplicationContext(), text,
								Toast.LENGTH_SHORT).show();
					 }
					 else
					 {
					 Toast.makeText(getApplicationContext(),
					 "kein GPS-Signal gefunden.", Toast.LENGTH_SHORT).show();
					 }
				} else {
					Toast.makeText(getApplicationContext(),
							"GPS nicht eingeschaltet", Toast.LENGTH_SHORT)
							.show();
				}

			}

		});
	}
}
