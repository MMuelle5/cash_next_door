package ch.imbApp.cash_next_door;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import ch.imbApp.cash_next_door.service.GpsService;

public class MainActivity extends Activity {

	private static String TAG = "cash-next-door";

	Button gogoButton;
	Button cameraView;
	TextView locationText;
	private Context context;
	

    Messenger mService = null;
   
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		context = this;
		setContentView(R.layout.activity_main);

		addListenerOnGogoButton();

	}

	private void addListenerOnGogoButton() {

		gogoButton = (Button) findViewById(R.id.gogoButton);

		gogoButton.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				
				Intent viewActivity = new Intent(context, ViewActivity.class);
				startActivity(viewActivity);

			}

		});

		cameraView = (Button) findViewById(R.id.cameraButton);
		
		cameraView.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {

				Intent cameraActivity = new Intent(context, CameraActivity.class);
				startActivity(cameraActivity);
			}
		});
	}
}
