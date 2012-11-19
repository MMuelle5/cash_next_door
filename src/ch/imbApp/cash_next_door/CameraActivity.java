package ch.imbApp.cash_next_door;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;

public class CameraActivity extends Activity{


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_camera);

//		Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
//		Intent intent = new Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA);
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivity(intent);
	}
}
