package ch.imbApp.cash_next_door;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import ch.imbApp.cash_next_door.alert.Alerts;
import ch.imbApp.cash_next_door.bean.BankOmat;
import ch.imbApp.cash_next_door.calc.AutomatenLoader;
import ch.imbApp.cash_next_door.calc.CalcAngle;
import ch.imbApp.cash_next_door.helper.Timer;
import ch.imbApp.cash_next_door.service.GpsService;
import ch.imbApp.cash_next_door.service.SensorService;

@TargetApi(13)
public class CameraActivity extends Activity {

	private Context context;
	private CameraPreview camPreview;
	private FrameLayout mainLayout;
	
	private TextView machineListInfos;

	private Location myLoc;
	private Location lastPosLoaded;
	private List<BankOmat> cashMachines = Collections.synchronizedList(new ArrayList<BankOmat>());
	private List<TextView> unusedTextList = Collections.synchronizedList(new ArrayList<TextView>());
	private int xWith;

	private Timer timer = new Timer(100);

	private Messenger mService = null;
	private Messenger mSensorService = null;
	private final Messenger mGpsMessenger = new Messenger(new IncomingGpsHandler());
	private final Messenger mSensorMessenger = new Messenger(new IncomingSensorHandler());
	private double myDirection;
	
	private AutomatenLoader loader = new AutomatenLoader();
	private Thread thread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;

		setContentView(R.layout.activity_camera);
		SurfaceView camView = new SurfaceView(this);
		SurfaceHolder camHolder = camView.getHolder();
		camPreview = new CameraPreview();
		camHolder.addCallback(camPreview);
		mainLayout = (FrameLayout) findViewById(R.id.camera_preview);
		mainLayout.addView(camView, new LayoutParams());
		
		machineListInfos = (TextView) findViewById(R.id.machineListInfos);
	  
		Display disp = this.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		disp.getSize(size);
		xWith = size.x;

		RelativeLayout relativeLayoutSensorsData = (RelativeLayout) findViewById(R.id.view_layout);

		doViewActivities();
		relativeLayoutSensorsData.bringToFront();
		updateMachineList();

	}

	private void doViewActivities() {
		
		initUnusedTextFields();

		Alerts.init(context);

		System.out.println("sensor start");
		startService(new Intent(context, SensorService.class));
		startService(new Intent(context, GpsService.class));
		
		doBindService();
	}

	/**
	 * setzt die Textfelder zurück
	 */
	private void initUnusedTextFields() {
		
		unusedTextList = new ArrayList<TextView>();
		unusedTextList.add((TextView) findViewById(R.id.cashOmat1));
		unusedTextList.add((TextView) findViewById(R.id.cashOmat2));		
		unusedTextList.add((TextView) findViewById(R.id.cashOmat3));		
		unusedTextList.add((TextView) findViewById(R.id.cashOmat4));		
		unusedTextList.add((TextView) findViewById(R.id.cashOmat5));
		
		for(TextView textView : unusedTextList) {
			textView.setText("");
			
		}
		
	}

	/**
	 * holt die Records von google
	 */
	private void updateMachineList() {    
		
		if(myLoc == null) {
            Toast.makeText(CameraActivity.this, R.string.locationWarning, Toast.LENGTH_LONG).show();
            machineListInfos.setVisibility(View.GONE);
			return;
		}

		Toast.makeText(CameraActivity.this, R.string.machinesUpdate, Toast.LENGTH_SHORT).show();
		System.out.println("Load");
		loader.latitude = myLoc.getLatitude();
		loader.longitude = myLoc.getLongitude();
		
		thread = new Thread(loader);
		thread.start();
		
		while(loader.machineList == null || loader.machineList.size() ==0) {
	        try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	
		}
	
		cashMachines = loader.machineList;
		int maxDistanceToPrefered = 0;
		int maxDistanceToOthers = 0;
		for(int i = 0; i<cashMachines.size(); i++) {
			int actDistance = (int) myLoc.distanceTo(cashMachines.get(i).getLocation());
			if(i<loader.preferedListSize) {
				if(maxDistanceToPrefered < actDistance) {
					maxDistanceToPrefered = actDistance;
				}
			}
			else if(maxDistanceToOthers < actDistance) {
				maxDistanceToOthers = actDistance;
			}
		}
		String text;
		if(loader.preferedMachine != null) {
			text = loader.preferedListSize +" "+ getResources().getString(R.string.countPreferedMachines) +" "+ maxDistanceToPrefered+"m\n"
					+loader.othersListSize+" "+getResources().getString(R.string.countOthersMachines)+" "+ maxDistanceToOthers+" m";
		}
		else {
			text = loader.othersListSize +" "+ getResources().getString(R.string.countMachines) +" "+ maxDistanceToOthers+"m";
		}
		machineListInfos.setText(text);
		machineListInfos.setVisibility(View.VISIBLE);
		
		System.out.println(machineListInfos.getText());
		
		initUnusedTextFields();
		
        Toast.makeText(CameraActivity.this, R.string.machinesUpdated, Toast.LENGTH_LONG).show();
		
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

			if (myLoc == null) {
				myLoc = new Location(LocationManager.GPS_PROVIDER);
			}

			switch (msg.what) {
				case GpsService.MSG_SET_STRING_VALUE:
					
					double[] loc = msg.getData().getDoubleArray("location");
					myLoc.setLongitude(loc[0]);
					myLoc.setLatitude(loc[1]);
					break;
				default:
					super.handleMessage(msg);
			}

			if(lastPosLoaded != null) {
			System.out.println(lastPosLoaded.distanceTo(myLoc));
			}
			
			if (lastPosLoaded == null ){
				lastPosLoaded = new Location("LAST");
			}

			if (lastPosLoaded.distanceTo(myLoc) > 30) { //nur alle 30m aktualisieren
				updateMachineList();
				lastPosLoaded.setLatitude(myLoc.getLatitude());
				lastPosLoaded.setLongitude(myLoc.getLongitude());
			}

			moved();

		}
	}

	class IncomingSensorHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
				case SensorService.MSG_SET_STRING_VALUE:

					if (msg.getData().getDouble("azimuth") != 0d) {
						myDirection = msg.getData().getDouble("azimuth");
						moved();
					}
					break;
				case SensorService.MSG_SET_BOOLEAN_VALUE:
					if (msg.getData().getBoolean(SensorService.POPUP_FIELD)) {
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

	private void moved() {

		if (myLoc == null || myLoc.getLatitude() == 0d || myLoc.getLongitude() == 0d) {
			return;
		}

		if (timer.isTimeReached()) {
			
			if (cashMachines != null && cashMachines.size() > 0) {
				for (BankOmat machine : cashMachines) {

					CalcAngle.uptDateBankOmatInfos(myLoc, machine);

					pointText(machine);

				}
			}
		}
	}

	/**
	 * Bei einer verfuegbaren Textview wird diese mit dem Record abgefuellt, sofern der Record in
	 * der gewuenschten Range ist.
	 * 
	 * @param machine
	 */
	private void pointText(BankOmat machine) {
		double totDir = machine.getDirection() - myDirection;
		

		if ((totDir < camPreview.getCameraAngel() / 2 && totDir > camPreview.getCameraAngel() / -2)) {
			displayText(machine, totDir);
		}
		//Problemzone zwischen -180 und 180
		else if((machine.getDirection() > (180-camPreview.getCameraAngel()/2) && myDirection < -180+camPreview.getCameraAngel()/2)
					|| (machine.getDirection() < (-180+camPreview.getCameraAngel()/2) && myDirection > 180-camPreview.getCameraAngel()/2)) {
			displayText(machine, totDir);
			
		}
		else if(machine.getDisplayedView() != null){
			machine.getDisplayedView().setText("");
			machine.getDisplayedView().setBackgroundColor(Color.TRANSPARENT);
			unusedTextList.add(machine.getDisplayedView());
			machine.setDisplayedView(null);
		}
		
	}
	
	public void displayText(BankOmat machine, double totDir) {
		double widthPerDegree = xWith / camPreview.getCameraAngel();

		TextView bankOmat;
		if(machine.getDisplayedView() != null) {
			bankOmat = machine.getDisplayedView();
		}
		else if(unusedTextList.size() == 0) {
			return;
		}
		else {
			bankOmat = unusedTextList.get(0);
			unusedTextList.remove(bankOmat);
			machine.setDisplayedView(bankOmat);
		}
		
		ImageSpan is = new ImageSpan(context, R.drawable.direction);
		SpannableString text = new SpannableString("  " + machine.getBankName() 
				+ "\n" + machine.getBankAddress()
				+ "\n" + (int) machine.getDistance()+"m");
		text.setSpan(is, 0, 1, SpannableString.SPAN_EXCLUSIVE_INCLUSIVE);
		bankOmat.setText(text);
		
		MarginLayoutParams params = (MarginLayoutParams) bankOmat.getLayoutParams();
		params.leftMargin = (int) (widthPerDegree * totDir + xWith / 2);
		bankOmat.setLayoutParams(params);
		bankOmat.setBackgroundResource(R.drawable.bg_shape);
	}


	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.destroyMe:
	            stopAll();
	            return true;
	        case R.id.info:
	        	String text = "INFO\n-----\nVersion: 1.0.0\n\nSeminararbeit <<Hand held>>\n\nWritten by:\n -Ramon Burri\n -Marius Mueller";
	            Toast.makeText(CameraActivity.this, text, Toast.LENGTH_LONG).show();
	        	return true;
	        case R.id.preferedMachine:
	        	getSubmenu(item);
	            return true;
	        default:
	        	String title = (String) item.getTitle();
	        	System.out.println(title);
	        	if(title != null && title.equals(getResources().getString(R.string.all))) {
	        		loader.preferedMachine = null;
	        		updateMachineList();
	        	}
	        	else if(title != null) {
	        		loader.preferedMachine = title;
	        		updateMachineList();
	        	}
	            return super.onOptionsItemSelected(item);
	    }
	}

	private void getSubmenu(MenuItem item) {

		Map<String, String> types = new HashMap<String, String>();
		
		for(BankOmat machine : loader.completeMachineList) {
			if(types.get(machine.getBankName()) == null) {
				types.put(machine.getBankName(), "ok");
			}
		}

		for(String name : types.keySet()) {
	        item.getSubMenu().add(name); 
		}
	}

	private void stopAll() {
		unbindService(mGpsConnection);
		unbindService(mSensorConnection);
		
		stopService(new Intent(context, SensorService.class));
		stopService(new Intent(context, GpsService.class));
		finish();
	}

}
