package ch.imbApp.cash_next_door;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import ch.imbApp.cash_next_door.alert.Alerts;
import ch.imbApp.cash_next_door.bean.BankOmat;
import ch.imbApp.cash_next_door.calc.AutomatenLoader;
import ch.imbApp.cash_next_door.calc.CalcAngle;
import ch.imbApp.cash_next_door.service.GpsService;
import ch.imbApp.cash_next_door.service.SensorService;


public class ViewActivity extends Activity {

	private static String TAG = "cash-next-door";

	TextView locationText;
	private Context context;
	private TextView lat;
	private TextView lon;
	private TextView distance;
	private TextView angle;
	private TextView myDirectionText;

	private Location myLoc;
	private Location lastPosLoaded;
	private List<BankOmat> cashMachines = new ArrayList<BankOmat>();
	

    Messenger mService = null;
    Messenger mSensorService = null;
    final Messenger mGpsMessenger = new Messenger(new IncomingGpsHandler());
    final Messenger mSensorMessenger = new Messenger(new IncomingSensorHandler());
    private double myDirection;

	 private CameraPreview camPreview; 
	 private LinearLayout mainLayout;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		context = this;

		  setContentView(R.layout.activity_view);  
		
//		CameraView cv = new CameraView(this);
//		setContentView(cv);
		
       
//		  SurfaceView camView = new SurfaceView(this);
//		  SurfaceHolder camHolder = camView.getHolder();
//		  camPreview = new CameraPreview();         
//		  camHolder.addCallback(camPreview);
//		  mainLayout = (LinearLayout) findViewById(R.id.frameLayout2);
//
//		  mainLayout.addView(camView, new LayoutParams());
		  
		  
		  
//		  mainLayout.addView(camView, mainLayout.getWidth(), mainLayout.getHeight());
		
		
		
//		lat = (TextView) findViewById(R.id.latitude);
//		lon = (TextView) findViewById(R.id.longitude);
//		distance = (TextView) findViewById(R.id.distance);
		angle = (TextView) findViewById(R.id.angle);
		myDirectionText = (TextView) findViewById(R.id.myDirection);
//		myDirectionText.setBackgroundColor(android.R.color.transparent);

	    Alerts.init(context);

	    System.out.println("sensor start");
		startService(new Intent(context, SensorService.class));
		startService(new Intent(context, GpsService.class));
		
        doBindService();        

	}
	
    void doBindService() {
    	bindService(new Intent(this, GpsService.class), mGpsConnection, Context.BIND_AUTO_CREATE);
        
        bindService(new Intent(this, SensorService.class), mSensorConnection, Context.BIND_AUTO_CREATE);
    }
    
    private ServiceConnection mGpsConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, GpsService.MSG_REGISTER_CLIENT);
                msg.replyTo = mGpsMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}
    };
    
    private ServiceConnection mSensorConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
        	mSensorService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, SensorService.MSG_REGISTER_CLIENT);
                msg.replyTo = mSensorMessenger;
                mSensorService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}
    };

    class IncomingGpsHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            if(myLoc == null) {
            	myLoc = new Location(LocationManager.GPS_PROVIDER);
            }
        	
            switch (msg.what) {
            case GpsService.MSG_SET_STRING_VALUE:

            	if(msg.getData().getDouble("latitude") != 0d) {
            		myLoc.setLatitude(msg.getData().getDouble("latitude"));
            		lat.setText(String.valueOf(msg.getData().getDouble("latitude")));
            	}
            	else if(msg.getData().getDouble("longitude") != 0d) {
            		myLoc.setLongitude(msg.getData().getDouble("longitude"));
            		lon.setText(String.valueOf(msg.getData().getDouble("longitude")));
            	}
                
                break;
            default:
                super.handleMessage(msg);
            }
            
            if(lastPosLoaded == null || lastPosLoaded.distanceTo(myLoc) > 0) {
            	cashMachines = AutomatenLoader.getBankomaten(myLoc, 42);
            }
            for(BankOmat machine: cashMachines) {
            	distance.setText(myLoc.distanceTo(machine.getLocation())+" Meter");
            	double myAngle = CalcAngle.calcAngle(myDirection, myLoc, machine.getLocation());
            	System.out.println(myAngle);
                angle.setText(myAngle + "°");
            }
            
        }
    }

    class IncomingSensorHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	
            switch (msg.what) {
            	case SensorService.MSG_SET_STRING_VALUE:
            	
            	if(msg.getData().getDouble("x") != 0d) {
            		double x = msg.getData().getDouble("x");
            		myDirectionText.setText(myDirection+"°");
            		myDirection = x;
            	}
                break;
                case SensorService.MSG_SET_BOOLEAN_VALUE:
                	if(msg.getData().getBoolean(SensorService.POPUP_FIELD)) {
	        			Alerts.showDialog(Alerts.ALERT_HORIZONTALE);
                	}
                	else {
	        			Alerts.hideDialog();
                	}
                	
	            default:
	                super.handleMessage(msg);
	            }
        }
    }
}
