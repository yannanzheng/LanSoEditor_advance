package com.example.advanceDemo;



import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IF1977Filter;

import com.example.advanceDemo.GPUImageFilterTools.FilterAdjuster;
import com.example.advanceDemo.GPUImageFilterTools.OnGpuImageFilterChosenListener;
import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.Layer;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.VideoLayer2;
import com.lansosdk.box.onCompressCompletedListener;
import com.lansosdk.box.onCompressProgressListener;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.box.onVideoLayer2ProgressListener;
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
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class TestLayerPlayerActivity extends Activity implements  OnClickListener{
	 private static final String TAG = "TestLayerPlayerActivity";

	    private String mVideoPath;
	    private final static int GET_VIDEO_PROGRESS=101;
	    private final static int UPDATE_PROGRESS_TIME=100;

	    private DrawPadView drawPadView;
	    
	    private VPlayer mplayer=null;
	    
	    private VideoLayer  videoLayer=null;
	    
	    private MediaInfo  mInfo;
	    
	    private SeekBar skProgress;
	    @Override
	    protected void onCreate(Bundle savedInstanceState) 
	    {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.test_layplayer_layout);
	        drawPadView=(DrawPadView)findViewById(R.id.id_test_playerview);
	        
	        
	        findViewById(R.id.id_test_btn_pause).setOnClickListener(this);
	        findViewById(R.id.id_test_btn_speedslow).setOnClickListener(this);
	        findViewById(R.id.id_test_btn_speedfast).setOnClickListener(this);
	        
	        skProgress=(SeekBar)findViewById(R.id.id_test_seekbar);
	        skProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					
					if(fromUser&& mplayer!=null){
						float percent=(float)progress/100f;
						int time=(int)(percent*mplayer.getDuration());
						mplayer.seekTo(time);
					}
				}
			});
	        
	        mVideoPath = getIntent().getStringExtra("videopath");
	        
	        mInfo=new MediaInfo(mVideoPath, false);
	        if(mInfo.prepare()){
	        	 new Handler().postDelayed(new Runnable() {
	     			
	     			@Override
	     			public void run() {
	     				 startPlayVideo();
	     			}
	     		}, 500);
	        }
	    }
	    private void startPlayVideo()
	    {
        	  mplayer=new VPlayer(getApplicationContext());
        	  mplayer.setVideoPath(mVideoPath);
        	  mplayer.setOnPreparedListener(new OnPlayerPreparedListener() {
				
				@Override
				public void onPrepared(VideoPlayer mp) {
					initDrawPad();
				}
			});
        	  mplayer.setOnCompletionListener(new OnPlayerCompletionListener() {
				
				@Override
				public void onCompletion(VideoPlayer mp) {
					if(drawPadView!=null && drawPadView.isRunning()){
						drawPadView.stopDrawPad();
					}
				}
			});
        	  mplayer.prepareAsync();
	    }
	    private void initDrawPad()
	    {
	    	MediaInfo info=new MediaInfo(mVideoPath);
	    	if(info.prepare())
	    	{
	    			drawPadView.setUpdateMode(DrawPadUpdateMode.ALL_VIDEO_READY,25);
	    			drawPadView.setDrawPadSize(480,480,new onDrawPadSizeChangedListener() {
	    			
		    			@Override
		    			public void onSizeChanged(int viewWidth, int viewHeight) {
		    				if(drawPadView.startDrawPad())
		    		    	{
		    		    		addVideoLayer();	  //增加图层,开始播放呢.
		    		    	}
		    			}
	    			});
	    	}
	    }
	    private void addVideoLayer()
	    { 
	    	BitmapLayer layer=drawPadView.addBitmapLayer(BitmapFactory.decodeResource(getResources(), R.drawable.videobg));
	    	layer.setScaledValue(layer.getPadWidth(), layer.getPadHeight());
	    	
	    	Log.i(TAG," wxh:"+mplayer.getVideoWidth()+mplayer.getVideoHeight());
	    	
	    	int videoW=mplayer.getVideoWidth();
	    	int videoH=mplayer.getVideoHeight();
	    	
			videoLayer=drawPadView.addMainVideoLayer(videoW,videoH,null);
			videoLayer.setRotate(360-mInfo.vRotateAngle);
			
			if(videoLayer!=null){
				mplayer.setSurface(new Surface(videoLayer.getVideoTexture()));
				mplayer.start();
				getTime();
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
	    }
	    boolean isDestorying=false;  //是否正在销毁, 因为销毁会停止DrawPad
	    @Override
	    protected void onDestroy() {
	    	super.onDestroy();
	    	isDestorying=true;
			if(drawPadView!=null){
				drawPadView.stopDrawPad();
				drawPadView=null;        		   
			}
			if(mplayer!=null){
				mplayer.stop();
				mplayer.release();
				mplayer=null;
			}
	    }
		@Override
		public void onClick(View v) {
			if(mplayer==null){
				return ;
			}
			switch (v.getId()) {
				case R.id.id_test_btn_pause:
						if(mplayer.isPlaying()){
							mplayer.pause();	
						}else{
							mplayer.start();
							mplayer.setSpeedEnable();
							mplayer.setSpeed(1.0f);
						}
						
					break;
				case R.id.id_test_btn_speedslow:
						mplayer.setSpeedEnable();
						mplayer.setSpeed(0.5f);
					break;
				case R.id.id_test_btn_speedfast:
						mplayer.setSpeedEnable();
						mplayer.setSpeed(1.5f);
					break;

			default:
				break;
			}
		}
		private void getTime()
		{
			mhandler.sendEmptyMessageDelayed(GET_VIDEO_PROGRESS, UPDATE_PROGRESS_TIME);
		}
		private EventHandler mhandler=new EventHandler();
	    protected  class EventHandler extends Handler {

	        @Override
	        public void handleMessage(Message msg) {
	        	switch (msg.what) {
	        		case GET_VIDEO_PROGRESS:
	        			
	        			if(mplayer!=null){
	        				Log.i(TAG,"get video progress------当前进度是:"+mplayer.getCurrentPosition());
	        				
	        				float progress=(float)(mplayer.getCurrentPosition())/(float)mplayer.getDuration();
	        				
	        				skProgress.setProgress((int)(progress*100));
	        			}
	        				
        				if(isDestorying==false){
        					mhandler.sendEmptyMessageDelayed(GET_VIDEO_PROGRESS, UPDATE_PROGRESS_TIME);		
        				}
	        		break;
	        	}
	        }
	    };
}
