package com.example.advanceDemo;

import com.example.advanceDemo.camera.CameraLayerFullLandscapeActivity;
import com.example.advanceDemo.camera.CameraLayerFullPortWithMp3Activity;
import com.example.advanceDemo.camera.CameraLayerFullPortActivity;
import com.example.advanceDemo.camera.CameraLayerFullSegmentActivity;
import com.example.advanceDemo.camera.CameraLayerRectActivity;
import com.example.advanceDemo.camera.CameraSubLayerDemo1Activity;
import com.example.advanceDemo.camera.CameraSubLayerDemo2Activity;
import com.lansoeditor.demo.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class CameraListDemoActivity extends Activity implements OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.camera_demo_list_layout);
		
		findViewById(R.id.id_cameralist_cameralayer).setOnClickListener(this);
		findViewById(R.id.id_cameralist_camerafulllayer).setOnClickListener(this);
		findViewById(R.id.id_cameralist_camerafulllayer2).setOnClickListener(this);
		findViewById(R.id.id_cameralist_cameralayer_segment).setOnClickListener(this);
		
		findViewById(R.id.id_cameralist_mp3record).setOnClickListener(this);
		findViewById(R.id.id_cameralist_sublayer1).setOnClickListener(this);
		findViewById(R.id.id_cameralist_sublayer2).setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.id_cameralist_cameralayer:
				startDemoActivity(CameraLayerRectActivity.class);
				break;
			case R.id.id_cameralist_camerafulllayer:
				startDemoActivity(CameraLayerFullPortActivity.class);
				break;
			case R.id.id_cameralist_camerafulllayer2:
				startDemoActivity(CameraLayerFullLandscapeActivity.class);
				break;
			case R.id.id_cameralist_cameralayer_segment:
				startDemoActivity(CameraLayerFullSegmentActivity.class);
				break;
			case R.id.id_cameralist_mp3record:
				startDemoActivity(CameraLayerFullPortWithMp3Activity.class);
				break;
			case R.id.id_cameralist_sublayer1:
				startDemoActivity(CameraSubLayerDemo1Activity.class);
				break;
			case R.id.id_cameralist_sublayer2:
				startDemoActivity(CameraSubLayerDemo2Activity.class);
				break;
			default:
				break;
		}
	}
	private void startDemoActivity(Class<?> cls)
   	{
    	Intent intent=new Intent(CameraListDemoActivity.this,cls);
    	startActivity(intent);
   	}
	
}
