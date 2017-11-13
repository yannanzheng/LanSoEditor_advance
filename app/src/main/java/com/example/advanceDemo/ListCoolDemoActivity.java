package com.example.advanceDemo;

import com.example.advanceDemo.camera.CameraLayerFullLandscapeActivity;
import com.example.advanceDemo.camera.CameraLayerFullPortWithMp3Activity;
import com.example.advanceDemo.camera.CameraLayerFullPortActivity;
import com.example.advanceDemo.camera.CameraLayerFullSegmentActivity;
import com.example.advanceDemo.camera.CameraLayerRectActivity;
import com.example.advanceDemo.camera.CameraSubLayerDemo1Activity;
import com.example.advanceDemo.camera.CameraSubLayerDemo2Activity;
import com.example.advanceDemo.cool.ParticleDemoActivity;
import com.example.advanceDemo.cool.VViewImage3DDemoActivity;
import com.lansoeditor.demo.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class ListCoolDemoActivity extends Activity implements OnClickListener{

	 private String videoPath;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.list_cool_demo_layout);
		 videoPath = getIntent().getStringExtra("videopath");
		 
		
		findViewById(R.id.id_cool_image3d).setOnClickListener(this);
		findViewById(R.id.id_cool_particle).setOnClickListener(this);
		
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.id_cool_image3d:
				startDemoActivity(VViewImage3DDemoActivity.class);
				break;
			case R.id.id_cool_particle:
				startDemoActivity(ParticleDemoActivity.class);
				break;
			default:
				break;
		}
	}
	private void startDemoActivity(Class<?> cls)
   	{
    	Intent intent=new Intent(ListCoolDemoActivity.this,cls);
    	intent.putExtra("videopath", videoPath);
    	startActivity(intent);
   	}

	
}
