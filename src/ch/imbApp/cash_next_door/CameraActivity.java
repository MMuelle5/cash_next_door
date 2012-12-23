package ch.imbApp.cash_next_door;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class CameraActivity extends Activity{


	 private CameraPreview camPreview; 
	 private FrameLayout mainLayout;
	      
//	 private Handler mHandler = new Handler(Looper.getMainLooper());
	 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		setContentView(R.layout.activity_camera);
//
////		Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
////		Intent intent = new Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA);
//		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//		startActivity(intent);
		
//		  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  
//		  WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		  //Set this APK no title
//		  requestWindowFeature(Window.FEATURE_NO_TITLE);
		  setContentView(R.layout.activity_camera);         
		  SurfaceView camView = new SurfaceView(this);
		  SurfaceHolder camHolder = camView.getHolder();
		  camPreview = new CameraPreview();         
		  camHolder.addCallback(camPreview);
		  mainLayout = (FrameLayout) findViewById(R.id.camera_preview);
		  mainLayout.addView(camView, new LayoutParams());
		  
		  RelativeLayout relativeLayoutSensorsData = (RelativeLayout) findViewById(R.id.view_layout);
	        relativeLayoutSensorsData.bringToFront();
	}
}
