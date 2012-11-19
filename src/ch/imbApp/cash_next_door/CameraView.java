package ch.imbApp.cash_next_door;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.view.View;

public class CameraView extends View {

	private Camera camera;
	
	public CameraView(Context context) {
		super(context);
		System.out.println("ok");
		init();
	}

	@TargetApi(9)
	public void init() {
System.out.println("init");
		for(int i = 0; i < Camera.getNumberOfCameras(); i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			
			System.out.println("gfunde");
			if(info.facing  == CameraInfo.CAMERA_FACING_BACK) {
				camera = Camera.open(info.facing);
			}
		}
	}
	

//	  @Override
//	  protected void onDraw(Canvas canvas) {
//		  
//	  }
}
