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
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
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

	private Context context;
	private TextView myDirectionText;

	private Location myLoc;
	private Location lastPosLoaded;
	private List<BankOmat> cashMachines = Collections.synchronizedList(new ArrayList<BankOmat>());
	private List<BankOmat> visibleCashMachines = Collections.synchronizedList(new ArrayList<BankOmat>());
	private List<TextView> unusedTextList = Collections.synchronizedList(new ArrayList<TextView>());
	private int xWith;

	private Timer timer = new Timer(100);

	Messenger mService = null;
	Messenger mSensorService = null;
	final Messenger mGpsMessenger = new Messenger(new IncomingGpsHandler());
	final Messenger mSensorMessenger = new Messenger(new IncomingSensorHandler());
	private double myDirection;
	private double cameraAngle;
	
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
		cameraAngle = camPreview.getCameraAngel();
		mainLayout = (FrameLayout) findViewById(R.id.camera_preview);
		mainLayout.addView(camView, new LayoutParams());

		//FIXME Kamerawinkel wurde mit parameters.getHorizontalViewAngle()); für mein Phone geholt (54.8°)		  
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

		myDirectionText = (TextView) findViewById(R.id.myDirection);
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
		
		for(TextView tv : unusedTextList) {
			tv.setText("");
		}
		
	}

	/**
	 * holt die Records von google
	 */
	private void updateMachineList() {    
		
		if(myLoc == null) {
			return;
		}
		
		System.out.println("Load");
		loader.latitude = myLoc.getLatitude();
		loader.longitude = myLoc.getLongitude();
		loader.shownMachines = visibleCashMachines;
		
		thread = new Thread(loader);
		thread.start();
		
		ProgressDialog pd = ProgressDialog.show(context, "Bitte warten...", "Daten werden aktualisiert", true, false);
    
		while(loader.machineList == null || loader.machineList.size() ==0) {
	        try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	
		}
	
		cashMachines = loader.machineList;
		initUnusedTextFields();

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

			if (lastPosLoaded.distanceTo(myLoc) > 20) { //nur alle 20 m?
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
						myDirectionText.setText("myDirect: "+ myDirection + "°");
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
		double widthPerDegree = xWith / cameraAngle;

		if (totDir < cameraAngle / 2 && totDir > cameraAngle / -2) {
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
			
			bankOmat.setText(machine.getBankName() + "\n" + (int) machine.getDistance()+"m");
			
			MarginLayoutParams params = (MarginLayoutParams) bankOmat.getLayoutParams();
			params.leftMargin = (int) (widthPerDegree * totDir + xWith / 2);
			
				
			bankOmat.setLayoutParams(params);
			bankOmat.setBackgroundColor(Color.GREEN);
		}
		else if(machine.getDisplayedView() != null){
			machine.getDisplayedView().setText("");
			machine.getDisplayedView().setBackgroundColor(Color.TRANSPARENT);
			unusedTextList.add(machine.getDisplayedView());
			machine.setDisplayedView(null);
		}
	}

}
