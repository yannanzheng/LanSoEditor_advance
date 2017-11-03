package com.example.advanceDemo;

import com.lansoeditor.demo.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class ListSceneDemoActivity extends Activity implements OnClickListener{

	 private String videoPath;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.list_scene_demo_layout);

		 videoPath = getIntent().getStringExtra("videopath");
		 
		findViewById(R.id.id_layer_pictures).setOnClickListener(this);
		findViewById(R.id.id_layer_outbody).setOnClickListener(this);
		findViewById(R.id.id_layer_videotransform).setOnClickListener(this);
		findViewById(R.id.id_layer_videotransform2).setOnClickListener(this);
		findViewById(R.id.id_layer_videobiansu).setOnClickListener(this);
		findViewById(R.id.id_layer_videoreverse).setOnClickListener(this);
		
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
				case R.id.id_layer_pictures:  //图片影集
					startDemoActivity(PictureSetRealTimeActivity.class);
				break;
				case R.id.id_layer_outbody:
					startDemoActivity(OutBodyDemoActivity.class);
				break;
				case R.id.id_layer_videotransform:
					startDemoActivity(VideoLayerTransformActivity.class);
				break;
				case R.id.id_layer_videotransform2:
					startDemoActivity(ExecuteAllDrawpadActivity.class);
				break;
				case R.id.id_layer_videobiansu:
					startDemoActivity(TestLayerPlayerActivity.class);
				break;
				case R.id.id_layer_videoreverse:
					showHintDialog("此功能演示在合作后提供");
					break;
				default:
					break;
		}
	}
	private void showHintDialog(String hint)
   	{
      new AlertDialog.Builder(this)
   		.setTitle("提示")
   		.setMessage(hint)
           .setPositiveButton("确定", new DialogInterface.OnClickListener() {
   			
   			@Override
   			public void onClick(DialogInterface dialog, int which) {
   			}
   		})
           .show();
   	}
	private void startDemoActivity(Class<?> cls)
   	{
    	Intent intent=new Intent(ListSceneDemoActivity.this,cls);
    	intent.putExtra("videopath", videoPath);
    	startActivity(intent);
   	}
	
}
