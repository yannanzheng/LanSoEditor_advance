package com.example.advanceDemo;

import com.lansoeditor.demo.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class ListFaceDetectedDemoActivity extends Activity implements OnClickListener{
	 private String videoPath;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.list_facedetected_demo_layout);
		 videoPath = getIntent().getStringExtra("videopath");
	}
	@Override
	public void onClick(View v) {
		
	}
	
	private void startDemoActivity(Class<?> cls)
   	{
    	Intent intent=new Intent(ListFaceDetectedDemoActivity.this,cls);
    	intent.putExtra("videopath", videoPath);
    	startActivity(intent);
   	}

}
