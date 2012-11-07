package ch.imbApp.cash_next_door;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import ch.imbApp.cash_next_door.service.GpsService;
import ch.imbApp.cash_next_door.service.SensorService;

public class MainActivity extends Activity {

	private static String TAG = "cash-next-door";

	Button gogoButton;
	TextView locationText;
	private Context context;
	private TextView lat;
	private TextView lon;
	private TextView distance;
	

    Messenger mService = null;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		context = this;
		setContentView(R.layout.activity_main);
		lat = (TextView) findViewById(R.id.latitude);
		lon = (TextView) findViewById(R.id.longitude);
		distance = (TextView) findViewById(R.id.distance);
		addListenerOnGogoButton();

        doBindService();
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

				Intent gps = new Intent(context, GpsService.class);

//				System.out.println("loc:"+obs.getLoc());
//				gps.putExtra("locObservable", obs);
								
				startService(new Intent(context, SensorService.class));
				startService(gps);
				
//				newReciver = new NewerLocationReceiver();
////				registerReceiver(newReciver, new IntentFilter(GpsService.NEW_LOCATION));
//				 registerReceiver(newReciver, new IntentFilter(GpsService.NEW_LOCATION), null, mHandler);

				System.out.println("alles durch");
			}

		});
	}

    void doBindService() {
        bindService(new Intent(this, GpsService.class), mConnection, Context.BIND_AUTO_CREATE);
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
//            textStatus.setText("Attached.");
            try {
                Message msg = Message.obtain(null, GpsService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
		}
    };

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case GpsService.MSG_SET_STRING_VALUE:

            	if(msg.getData().getDouble("latitude") != 0d) {
            		lat.setText(String.valueOf(msg.getData().getDouble("latitude")));
            	}
            	else if(msg.getData().getDouble("longitude") != 0d) {
            		lon.setText(String.valueOf(msg.getData().getDouble("longitude")));
            	}
            	else if(msg.getData().getDouble("distance") != 0d) {
            		distance.setText(msg.getData().getDouble("distance")+"m (oder so)");
            	}
                
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }
}
