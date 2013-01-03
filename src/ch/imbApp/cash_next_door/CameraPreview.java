package ch.imbApp.cash_next_door;

import java.io.IOException;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.SurfaceHolder;

public class CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback {
	
	private Camera mCamera;

	private float cameraAngel;
	
	public CameraPreview() {

	}
	
	public void onPreviewFrame(byte[] arg0, Camera arg1) {
		// At preview mode, the frame data will push to here.
		// But we do not want these data.
	}

	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

		  Parameters parameters;
		  parameters = mCamera.getParameters();

		  setCameraAngel(parameters.getHorizontalViewAngle());
		   
		  // Turn on the camera flash. 
		  String NowFlashMode = parameters.getFlashMode();
		  if ( NowFlashMode != null )
		   parameters.setFlashMode(Parameters.FLASH_MODE_ON);
		  // Set the auto-focus. 
		  String NowFocusMode = parameters.getFocusMode ();
		  if ( NowFocusMode != null )
		   parameters.setFocusMode("auto");
		   
		  mCamera.setParameters(parameters);

		  mCamera.setDisplayOrientation(90);
		  mCamera.startPreview();
	}

	public void surfaceCreated(SurfaceHolder arg0) {
		mCamera = Camera.open();
		try {
			// If did not set the SurfaceHolder, the preview area will be black.
			System.out.println(arg0);
			mCamera.setPreviewDisplay(arg0);
			mCamera.setPreviewCallback(this);
			mCamera.setDisplayOrientation(90);
		} catch (IOException e) {
			mCamera.release();
			mCamera = null;
		}
	}

	// Take picture interface
	public void CameraTakePicture(String FileName) {
	}

	// Set auto-focus interface
	public void CameraStartAutoFocus() {
	}

	public void setCameraAngel(float cameraAngel) {
		this.cameraAngel = cameraAngel;
	}

	public float getCameraAngel() {
		return cameraAngel;
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i("CameraPreview", "destroy");
		if(mCamera != null) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null); 
			mCamera.release();
			mCamera = null;
		}
	}
	
}
