package com.example.lansongeditordemo;

import com.lansoeditor.demo.R;
import com.lansosdk.videoeditor.MediaInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ScrollView;
import android.widget.TextView;

public class SdkHintActivity extends Activity{
	
	String activityName;
	String videoPath=null;
	TextView  tvCount;
	private int count=COUNT_NUM;
	private static final int COUNT_NUM=10;
	private ScrollView mScrollView;
	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.sdk_hint_layout);
		
		mScrollView=(ScrollView)findViewById(R.id.id_sdk_hint_scrollview);
		tvCount=(TextView)findViewById(R.id.id_playbox_tv_count);
		
		tvCount.setText(String.valueOf(count));
		
		activityName=getIntent().getStringExtra("Activity_name");
		videoPath = getIntent().getStringExtra("videopath");
		
		Log.i("tag","shoActivity "+activityName);
		
		findViewById(R.id.id_playbox_hint_btn).setEnabled(false);
		
		findViewById(R.id.id_playbox_hint_btn).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showActivity();
			}
		});
		 mRunnable=new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(count-->0){
						tvCount.setText(String.valueOf(count));
						mHandler.postDelayed(mRunnable, 1000);
					}else{
						findViewById(R.id.id_playbox_hint_btn).setEnabled(true);
						mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
					}
				}
			};
		findViewById(R.id.id_playbox_hint_btn).setEnabled(false);
		count=COUNT_NUM;
		mHandler.postDelayed(mRunnable, 1000);
	}
	Handler mHandler=new Handler();
	Runnable mRunnable=null;
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mHandler.removeCallbacks(mRunnable);
	}
	private void showActivity()
	{
		Log.i("tag","shoActivity "+activityName);
		Intent intent=new Intent(SdkHintActivity.this,MainActivity.class);
		startActivity(intent);
	}
}
