package com.example.advanceDemo.scene;


import com.example.advanceDemo.VideoPlayerActivity;
import com.lansoeditor.demo.R;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.videoeditor.DrawPadView;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoplayer.VPlayer;
import com.lansosdk.videoplayer.VideoPlayer;
import com.lansosdk.videoplayer.VideoPlayer.OnPlayerCompletionListener;
import com.lansosdk.videoplayer.VideoPlayer.OnPlayerPreparedListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class OutBodyDemoActivity extends Activity {
    private static final String TAG = "OutBodyDemoActivity";

    private String mVideoPath;

    private DrawPadView mDrawPadView;
    
    private VPlayer mplayer=null;
    private VideoLayer  mainVideoLayer=null;
    
    
    private String editTmpPath=null;
    private String dstPath=null;
    private LinearLayout  playVideo;
    private MediaInfo mInfo=null;
    private Button  btnTest;
    private int padWidth;
	private int padHeight;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.outbody_demo_layout);
        
        mVideoPath = getIntent().getStringExtra("videopath");
        mInfo=new MediaInfo(mVideoPath,false);
    	if(mInfo.prepare()==false){
    		 Toast.makeText(this, "传递过来的视频文件错误", Toast.LENGTH_SHORT).show();
    		 this.finish();
    	}
    	
        mDrawPadView = (DrawPadView) findViewById(R.id.id_outbody_drawpadview);
        btnTest=(Button)findViewById(R.id.id_outbody_testbutton);
        btnTest.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mainVideoLayer!=null){
					mainVideoLayer.setSubImageEnable(false);
				}
			}
		});
     
        playVideo=(LinearLayout)findViewById(R.id.id_outbody_saveplay);
        playVideo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 if(SDKFileUtils.fileExist(dstPath)){
		   			 	Intent intent=new Intent(OutBodyDemoActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
		   		 }else{
		   			 Toast.makeText(OutBodyDemoActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
		   		 }
			}
		});
        playVideo.setVisibility(View.GONE);

        editTmpPath=SDKFileUtils.newMp4PathInBox();
        dstPath=SDKFileUtils.newMp4PathInBox();
        
        new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//showHintDialog();
				startPlayVideo();
			}
		}, 300);
    }
    /**
     * VideoLayer是外部提供画面来源, 
     * 您可以用你们自己的播放器作为画面输入源,
     * 也可以用原生的MediaPlayer,只需要视频播放器可以设置surface即可.
     * 
     * 一下举例是采用MediaPlayer作为视频输入源.
     */
    private void startPlayVideo()
    {
    	 if (mVideoPath != null){
    		 
    		 		mplayer=new VPlayer(OutBodyDemoActivity.this);
						
    		 		mplayer.setVideoPath(mVideoPath);
    		 		
    		 		mplayer.setOnPreparedListener(new OnPlayerPreparedListener() {
						
						@Override
						public void onPrepared(VideoPlayer mp) {
							// TODO Auto-generated method stub
							initDrawPad();
						}
					});
    		 		mplayer.setOnCompletionListener(new OnPlayerCompletionListener() {
						
						@Override
						public void onCompletion(VideoPlayer mp) {
							// TODO Auto-generated method stub
							stopDrawPad();
						}
					});
		       	  mplayer.prepareAsync();
         }
         else {
             finish();
             return;
         }
    }
    /**
     * Step1:  init DrawPad 初始化
     */
    private void initDrawPad()
    {
    		padWidth=mInfo.vWidth;
    		padHeight=mInfo.vHeight;
    	
    		if(mInfo.vRotateAngle==90 || mInfo.vRotateAngle==270){
    			padWidth=mInfo.vHeight;
    			padHeight=mInfo.vWidth;
    		}
        	/**
        	 * 设置使能 实时录制, 即把正在DrawPad中呈现的画面实时的保存下来,实现所见即所得的模式
        	 */
        	mDrawPadView.setRealEncodeEnable(padWidth,padHeight,1000000,(int)mInfo.vFrameRate,editTmpPath);
        	/**
        	 * 设置当前DrawPad的宽度和高度,并把宽度自动缩放到父view的宽度,然后等比例调整高度.
        	 */
    		mDrawPadView.setDrawPadSize(padWidth,padHeight,new onDrawPadSizeChangedListener() {
				@Override
				public void onSizeChanged(int viewWidth, int viewHeight) {
					startDrawPad();
				}
    		});
    }
    /**
     * Step2: 开始运行 Drawpad
     */
    private void startDrawPad() 
    {
		if(mDrawPadView.startDrawPad())
		{
			mainVideoLayer=mDrawPadView.addMainVideoLayer(padWidth,padHeight,null);
			if(mainVideoLayer!=null)
			{
				mplayer.setSurface(new Surface(mainVideoLayer.getVideoTexture()));
				mplayer.start();
			}
		}
    }
    /**
     * Step3: stop DrawPad
     */
    private void stopDrawPad()
    {
    	if(mDrawPadView!=null && mDrawPadView.isRunning()){
			
			mDrawPadView.stopDrawPad();
			Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
			
			if(SDKFileUtils.fileExist(editTmpPath)){
				boolean ret=VideoEditor.encoderAddAudio(mVideoPath,editTmpPath,SDKDir.TMP_DIR,dstPath);
				if(!ret){
					dstPath=editTmpPath;
				}else{
					SDKFileUtils.deleteFile(editTmpPath);	
				}
				playVideo.setVisibility(View.VISIBLE);
			}else{
				Log.e(TAG," player completion, but file:"+editTmpPath+" is not exist!!!");
			}
		}
    }
    @Override
    protected void onPause() {
    	super.onPause();
    	if(mplayer!=null){
    		mplayer.stop();
    		mplayer.release();
    		mplayer=null;
    	}
    	if(mDrawPadView!=null){
    		mDrawPadView.stopDrawPad();
    	}
    }
   @Override
	protected void onDestroy() {
		super.onDestroy();
	    	SDKFileUtils.deleteFile(dstPath);
	    	SDKFileUtils.deleteFile(editTmpPath);
	}
}