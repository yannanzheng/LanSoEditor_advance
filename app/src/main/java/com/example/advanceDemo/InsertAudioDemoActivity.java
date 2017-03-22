package com.example.advanceDemo;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.advanceDemo.view.AudioInsert;
import com.example.advanceDemo.view.DrawPadView;
import com.lansoeditor.demo.R;
import com.lansosdk.box.MVLayer;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.videoeditor.CopyDefaultVideoAsyncTask;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;

public class InsertAudioDemoActivity extends Activity {
    private static final String TAG = "InsertAudioDemoActivity";

    private String mVideoPath;

    
    private String dstPath=null;
    
    TextView tvProgressHint;
	 TextView tvHint;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_edit_demo_layout);
		 
		 initUI();
        mVideoPath = getIntent().getStringExtra("videopath");
        dstPath=SDKFileUtils.createMp4FileInBox();
    }
    
    private void initUI()
	   {
			   tvHint=(TextView)findViewById(R.id.id_video_editor_hint);
			   tvHint.setText(R.string.insert_audio_demo_hint);
			   
			   tvProgressHint=(TextView)findViewById(R.id.id_video_edit_progress_hint);
			       
				 findViewById(R.id.id_video_edit_btn).setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							
							tvHint.setText("正在处理中...");
							testInsert();
							   findViewById(R.id.id_video_edit_btn2).setEnabled(true);
						}
				 });
		     findViewById(R.id.id_video_edit_btn2).setEnabled(false);
		     findViewById(R.id.id_video_edit_btn2).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(SDKFileUtils.fileExist(dstPath)){
							Intent intent=new Intent(InsertAudioDemoActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
						}else{
							 Toast.makeText(InsertAudioDemoActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
						}
					}
				});
	
	   }
    /**
     * 为了简单演示, 这里没有用异步线程来做, 实际使用中,您可以用异步线程来做. 
     * 如果手机CPU支持音频硬件加速, 则处理是很快的, 基本是5秒一下. 
     * 如果不支持 音频硬件加速, 则使用软解码软编码来做.
     */
    private void testInsert()
    {
	   	  VideoEditor et=new VideoEditor();
	   	  MediaInfo info=new MediaInfo(mVideoPath);
	   	  if(info.prepare() && info.isHaveVideo() && info.isHaveAudio())
	   	  {
	   		  String audioPath=SDKFileUtils.createFileInBox(info.aCodecName);
	   		  et.executeDeleteVideo(mVideoPath, audioPath);
	   		  
	   		  AudioInsert audioInsert=new AudioInsert(getApplicationContext());
			  audioInsert.addMainAudio(audioPath,1.0f, true);
			  
			  String add1=CopyDefaultVideoAsyncTask.copyFile(InsertAudioDemoActivity.this,"hongdou10s.mp3");
			  //从第4秒出开始增加, 增加3秒的时长,音量为3倍.
			  audioInsert.addSubAudio(add1,4000,3000,3.0f,true);
			  
			  String add2=CopyDefaultVideoAsyncTask.copyFile(InsertAudioDemoActivity.this,"wuya.pcm");
			  //增加一段pcm的音频文件,从0秒出增加
			  audioInsert.addSubAudioPCM(add2, 44100, 2, 0,2500,2.0f,true);
			  
			  //开始执行...执行结束,返回混合后的文件.
			   String dstMix= audioInsert.executeAudioMix();
			   Log.i(TAG,"目标音audioInsert频文件是:"+dstMix);
			   
			   if(SDKFileUtils.fileExist(dstMix)){
				   VideoEditor.videoReplaceNewAudio(mVideoPath, dstMix, dstPath);
			   }
	   	  }
    }
}
