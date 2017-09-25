package com.example.advanceDemo;

import com.lansoeditor.demo.R;
import com.lansosdk.videoeditor.AudioPadExecute;
import com.lansosdk.videoeditor.SDKFileUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class AudioPadDemoActivity  extends Activity  implements OnClickListener{

	TextView tvProgressHint;
	TextView tvHint;
	ProgressDialog  mProgressDialog;
	String  dstPath;
	boolean isExecuting;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.execute_edit_demo_layout);
		initView();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		SDKFileUtils.deleteFile(dstPath);
	}
	/**
	 * 开始处理
	 */
	private void startExecute()
	{
		if(isExecuting){
			return ;
		}
		isExecuting=true;
		dstPath=SDKFileUtils.createM4AFileInBox();
		AudioPadExecute   audioPad=new AudioPadExecute(getApplicationContext(),dstPath);
    	
		/**
		 * 设置处理后生成声音的总长度.
		 */
    	audioPad.setAudioPadLength(15.0f);  //定义生成一段15秒的声音./或者你可以把某一个音频作为一个主音频
    	
    	/**
    	 * 在这15内, 前3秒增加一个声音
    	 */
    	audioPad.addSubAudio("/sdcard/audioPadTest/du15s_44100_2.mp3", 0, 3*1000,1.0f);
    	/**
    	 * 中间3秒增加一段
    	 */
    	audioPad.addSubAudio("/sdcard/audioPadTest/hongdou10s_44100_2.mp3", 3*1000, 6*1000,1.0f); //
    	/**
    	 * 从10秒开始增加全部.
    	 */
    	audioPad.addSubAudio("/sdcard/audioPadTest/niu30s_44100_2.m4a", 10*1000, -1,1.0f);  //
    	
    	
    	 mProgressDialog = new ProgressDialog(getApplicationContext());
         mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
         mProgressDialog.setMessage("正在处理...");
         mProgressDialog.setCancelable(false);
         mProgressDialog.show();
         
//    	开启执行.
    	audioPad.start();  //开始运行 ,另开一个线程,异步执行.
    	
    	
    	audioPad.waitComplete();  //等待完成.
    	
    	audioPad.release();   //释放(内部会检测是否执行完, 如没有,则等待执行完毕).
    	
    	if( mProgressDialog!=null){
    		 mProgressDialog.cancel();
    		 mProgressDialog=null;
    	}
	}
	private void resultPreview()
	{
		if(dstPath!=null)
		{
			MediaPlayer  player=new MediaPlayer();
	    	try {
				player.setDataSource(dstPath);
				player.prepare();
				player.start();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
	}
	private void initView()
	{
		 tvHint=(TextView)findViewById(R.id.id_video_editor_hint);
		 tvHint.setText(R.string.audiopad_execute_hint);
		 
		 tvProgressHint=(TextView)findViewById(R.id.id_video_edit_progress_hint);
		 
		 
		 findViewById(R.id.id_video_edit_btn).setOnClickListener(this);
		 findViewById(R.id.id_video_edit_btn2).setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.id_video_edit_btn:
				startExecute();
				break;
			case R.id.id_video_edit_btn2:
				resultPreview();
				break;
			default:
				break;
		}
	}
}
