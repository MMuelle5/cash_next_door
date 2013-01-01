package ch.imbApp.cash_next_door;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import ch.imbApp.cash_next_door.alert.Alerts;
import ch.imbApp.cash_next_door.bean.BankOmat;
import ch.imbApp.cash_next_door.calc.AutomatenLoader;
import ch.imbApp.cash_next_door.calc.CalcAngle;
import ch.imbApp.cash_next_door.helper.Timer;
import ch.imbApp.cash_next_door.service.GpsService;
import ch.imbApp.cash_next_door.service.SensorService;

@TargetApi(13)
public class CameraActivity extends Activity {

	private CameraPreview camPreview;
	private FrameLayout mainLayout;

	//		TextView locationText;
	private Context context;
	//		private TextView lat;
	//		private TextView lon;
	//		private TextView distance;
	private TextView angle;
	private TextView myDirectionText;
    private ProgressBar mProgress;
	//		private TextView lonText;

	private Location myLoc;
	private Location lastPosLoaded;
	private List<BankOmat> hiddenCashMachines = Collections.synchronizedList(new ArrayList<BankOmat>());
	private List<BankOmat> visibleCashMachines = Collections.synchronizedList(new ArrayList<BankOmat>());
	private List<TextView> unusedTextList = Collections.synchronizedList(new ArrayList<TextView>());
	private int xWith;

	private Timer timer = new Timer(100);

	Messenger mService = null;
	Messenger mSensorService = null;
	final Messenger mGpsMessenger = new Messenger(new IncomingGpsHandler());
	final Messenger mSensorMessenger = new Messenger(new IncomingSensorHandler());
	private double myDirection;
	private double cameraAngle = 54.8;
	
	private AutomatenLoader loader = new AutomatenLoader();
	private Thread thread = new Thread(loader);

	//	 private Handler mHandler = new Handler(Looper.getMainLooper());

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;

		//		setContentView(R.layout.activity_camera);
		//
		////		Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
		////		Intent intent = new Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA);
		//		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		//		startActivity(intent);

		//		  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  
		//		  WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//		  requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_camera);
		SurfaceView camView = new SurfaceView(this);
		SurfaceHolder camHolder = camView.getHolder();
		camPreview = new CameraPreview();
		camHolder.addCallback(camPreview);
		mainLayout = (FrameLayout) findViewById(R.id.camera_preview);
		mainLayout.addView(camView, new LayoutParams());

		//FIXME Kamerawinkel wurde mit parameters.getHorizontalViewAngle()); für mein Phone geholt (54.8%)		  
		Display disp = this.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		disp.getSize(size);
		xWith = size.x;

		RelativeLayout relativeLayoutSensorsData = (RelativeLayout) findViewById(R.id.view_layout);

		doViewActivities();
		relativeLayoutSensorsData.bringToFront();
		updateHiddenList();

	}

	private void doViewActivities() {
		//		lat = (TextView) findViewById(R.id.latitude);
		//		lon = (TextView) findViewById(R.id.longitude);
		//		distance = (TextView) findViewById(R.id.distance);
		angle = (TextView) findViewById(R.id.angle);
		myDirectionText = (TextView) findViewById(R.id.myDirection);
//		mProgress = (ProgressBar) findViewById(R.id.lin_progress_bar);
		//		lonText = (TextView) findViewById(R.id.longitudeText);
		unusedTextList.add((TextView) findViewById(R.id.cashOmat1));
		unusedTextList.add((TextView) findViewById(R.id.cashOmat2));
		unusedTextList.add((TextView) findViewById(R.id.cashOmat3));

		Alerts.init(context);

		System.out.println("sensor start");
		startService(new Intent(context, SensorService.class));
		startService(new Intent(context, GpsService.class));
		
		doBindService();
	}

	private void updateHiddenList() {    
		
		thread.start();   
		
		ProgressDialog pd = ProgressDialog.show(context, "Bitte warten...", "Daten werden aktualisiert", true, false);
    
		while(loader.machineList == null || loader.machineList.size() ==0) {
	        try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	
		}
	
		hiddenCashMachines = loader.machineList;
		pd.dismiss();
		
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
			else if (lastPosLoaded.distanceTo(myLoc) > 20) { //nur alle 20 m?
				updateHiddenList();
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
						double x = msg.getData().getDouble("azimuth");
						myDirectionText.setText(myDirection + "°");
						myDirection = x;
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
			
			if (visibleCashMachines != null && visibleCashMachines.size() > 0) {
				for (int i = visibleCashMachines.size() - 1; i >= 0; i--) {
					//		        for(BankOmat machine: visibleCashMachines) {

					//		        	machine.setDirection(CalcAngle.calcAngle(myDirection, myLoc, machine.getLocation()));
					BankOmat machine = visibleCashMachines.get(i);
					CalcAngle.uptDateBankOmatInfos(myLoc, machine);

					pointTextAlreadyDisplayed(machine);
					angle.setText(machine.getDirection() + "°");

				}
			}
			if (hiddenCashMachines != null && hiddenCashMachines.size() > 0) {
				for (int i = hiddenCashMachines.size() - 1; i >= 0; i--) {
					//		        for(BankOmat machine: hiddenCashMachines) {
					//	        	distance.setText(myLoc.distanceTo(machine.getLocation())+" Meter");
					//		        	machine.setDirection(CalcAngle.calcAngle(myDirection, myLoc, machine.getLocation()));
					BankOmat machine = hiddenCashMachines.get(i);
					CalcAngle.uptDateBankOmatInfos(myLoc, machine);

					pointText(machine);
					angle.setText(machine.getDirection() + "°");

				}
			}
		}
	}

	/**
	 * Position der verfuegbaren View wird angepasst ist der Bankomat nicht mehr sichtbar, wird der
	 * Record zu den unsichtbaren gelegt und die TextView wieder freigegeben.
	 * 
	 * @param machine
	 */
	private void pointTextAlreadyDisplayed(BankOmat machine) {
		double totDir = machine.getDirection() - myDirection;//myLoc.getBearing();
System.out.println(totDir +" "+ cameraAngle / 2);
		if (totDir < cameraAngle / 2 && totDir > cameraAngle / -2) {
			//    		int width = (int) (xWith /54.8);
			//    		TextView bankOmat = machine.getDisplayedView();
			//    		
			//    		MarginLayoutParams params=(MarginLayoutParams )bankOmat.getLayoutParams();
			//    		params.leftMargin = (int) (width*totDir+xWith/2);
			//    		bankOmat.setLayoutParams(params);
			displayText(machine, machine.getDisplayedView(), totDir);
		}
		else {
			machine.getDisplayedView().setText("");
			unusedTextList.add(machine.getDisplayedView());
			machine.setDisplayedView(null);
			hiddenCashMachines.add(machine);
			visibleCashMachines.remove(machine);
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

		if (totDir < cameraAngle / 2 && totDir > cameraAngle / -2 && unusedTextList.size() > 0) {
			TextView bankOmat = unusedTextList.get(0);
			displayText(machine, bankOmat, totDir);
			machine.setDisplayedView(bankOmat);
			unusedTextList.remove(bankOmat);
			visibleCashMachines.add(machine);
			hiddenCashMachines.remove(machine);
		}
	}

	private void displayText(BankOmat machine, TextView bankOmat, double totDir) {
		int widthPerDegree = (int) (xWith / 54.8);
		bankOmat.setText(machine.getBankName() + "\n" + machine.getDistance());
		MarginLayoutParams params = (MarginLayoutParams) bankOmat.getLayoutParams();

		if(totDir < 0) {
			params.leftMargin = (int) (widthPerDegree * totDir * -1);
		}
		else {
			params.leftMargin = (int) (widthPerDegree * totDir + xWith / 2);
		}
		bankOmat.setLayoutParams(params);
	}
}
