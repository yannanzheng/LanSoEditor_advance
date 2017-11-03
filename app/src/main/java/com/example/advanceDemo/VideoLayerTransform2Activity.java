package com.example.advanceDemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;



import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSwirlFilter;

import com.example.advanceDemo.view.PaintConstants;
import com.example.advanceDemo.view.ShowHeart;
import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.CanvasLayer;
import com.lansosdk.box.CanvasRunnable;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.Layer;
import com.lansosdk.box.ViewLayerRelativeLayout;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.DrawPadView;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 */
public class VideoLayerTransform2Activity extends Activity{
    private static final String TAG = "VideoLayerTransform2Activity";

    private String mVideoPath;
    private String videoPath2;

    private DrawPadView mDrawPad;
    
    private MediaPlayer mplayer=null;
    private MediaPlayer mplayer2=null;
    private MediaPlayer audioPlay=null;
    
    private BitmapLayer  bmpLayer=null;
    private CanvasLayer  canvasLayer=null;
    private VideoLayer  videoLayer1=null;
    private MediaInfo  mInfo=null;
    private LinearLayout  playVideo;
    private String dstPath=null;
    private MoveCentor moveCentor=null;
    private ScaleAnimation scaleAnim;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videolay_transform_layout);
        
        mContext=getApplicationContext();
        
        initView();
        
        mVideoPath = getIntent().getStringExtra("videopath");
        mDrawPad = (DrawPadView) findViewById(R.id.id_videolayer_drawpad);
        
        dstPath=SDKFileUtils.newMp4PathInBox();
	    
        mInfo=new MediaInfo(mVideoPath,false);
        if(mInfo.prepare()==false){
        	finish();
        }
        
        new Thread(new Runnable() {
			
			@Override
			public void run() {
				videoPath2=CopyFileFromAssets.copyAssets(getApplicationContext(), "ping25s.mp4");
			}
		}).start();
        
        new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				setupDrawPad();
			}
		}, 500);
    }
    int videoWidth,videoHeight;
    /**
     * Step1: 设置DrawPad 容器的尺寸.并设置是否实时录制容器上的内容.
     */
    private void setupDrawPad()
    {
    		videoWidth=mInfo.vWidth;
    		videoHeight=mInfo.vHeight;
    		if(mInfo.vRotateAngle==90 || mInfo.vRotateAngle==270){
    			videoWidth=mInfo.vHeight;
    			videoHeight=mInfo.vWidth;
    		}
    		
    		mDrawPad.setRealEncodeEnable(videoWidth,videoHeight,1500000,(int)25,dstPath);
    		mDrawPad.setUpdateMode(DrawPadUpdateMode.AUTO_FLUSH, 25);//25是帧率.
        	mDrawPad.setDrawPadSize(videoWidth,videoHeight,new onDrawPadSizeChangedListener() {
    			@Override
    			public void onSizeChanged(int viewWidth, int viewHeight) {
    				mDrawPad.pauseDrawPad();
    				startDrawPad();
    			}
    		});
        	mDrawPad.setOnDrawPadProgressListener(new onDrawPadProgressListener() {
				
				@Override
				public void onProgress(DrawPad v, long currentTimeUs) {
				}
			});
    }
    private void startDrawPad()
    {
    	if(mDrawPad.startDrawPad())
    	{
    		//增加一个视频图层, 并开始播放视频1.
        	videoLayer1=mDrawPad.addMainVideoLayer(videoWidth,videoHeight,null);
        	playVideo1();
        	mDrawPad.resumeDrawPad();
        }
    }
    private void playVideo1()
    {
        	  mplayer=new MediaPlayer();
        	  try {
        		  mplayer.setDataSource(mVideoPath);
        		  mplayer.setOnPreparedListener(new OnPreparedListener() {
					
					@Override
					public void onPrepared(MediaPlayer mp) {
						  mplayer.setSurface(new Surface(videoLayer1.getVideoTexture()));  //视频
		        		  mplayer.start();
					}
				});
        		  mplayer.setOnCompletionListener(new OnCompletionListener() {
					
					@Override
					public void onCompletion(MediaPlayer mp) {
						mplayer.setSurface(null);
						playVideo2();  //视频1播放完毕后. 播放视频2
					}
				});
        		  mplayer.prepareAsync();
        	  }  catch (IOException e) {
				e.printStackTrace();
        	  }
    }
    private void playVideo2()
    {
	    	  if(videoPath2==null){
	    		   videoPath2=CopyFileFromAssets.copyAssets(getApplicationContext(), "ping25s.mp4");
	    	  }
        	  mplayer2=new MediaPlayer();
        	  try {
        		  mplayer2.setDataSource(videoPath2);
        		  mplayer2.setOnPreparedListener(new OnPreparedListener() {
					
					@Override
					public void onPrepared(MediaPlayer mp) {
						  mplayer2.setSurface(new Surface(videoLayer1.getVideoTexture()));  //视频
		        		  mplayer2.start();
					}
				});
        		  mplayer2.setOnCompletionListener(new OnCompletionListener() {
					
					@Override
					public void onCompletion(MediaPlayer mp) {
						stopDrawPad();
					}
				 });
        		  mplayer2.prepareAsync();
        	  }  catch (IOException e) {
				e.printStackTrace();
        	  }
    }
    /**
     * Step3: 做好后, 停止容器, 因为容器里没有声音, 这里增加上原来的声音.
     */
    private void stopDrawPad()
    {
    	if(mDrawPad!=null && mDrawPad.isRunning()){
			mDrawPad.stopDrawPad();
			toastStop();
			
			if(SDKFileUtils.fileExist(dstPath)){
				playVideo.setVisibility(View.VISIBLE);
			}
			if(audioPlay!=null){
				audioPlay.stop();
				audioPlay.release();
				audioPlay=null;
			}
			if(mplayer!=null){
	    		mplayer.stop();
	    		mplayer.release();
	    		mplayer=null;
	    	}
	    	if(mplayer2!=null){
	    		mplayer2.stop();
	    		mplayer2.release();
	    		mplayer2=null;
	    	}
		}
    }
    private void initView()
    {
    	  playVideo=(LinearLayout)findViewById(R.id.id_videoLayer_saveplay);
    	  playVideo.setOnClickListener(new OnClickListener() {
  			
  			@Override
  			public void onClick(View v) {
  				Intent intent=new Intent(VideoLayerTransform2Activity.this,VideoPlayerActivity.class);
	   			 	intent.putExtra("videopath", dstPath);
	   			 	startActivity(intent);
  			}
  		});
    	  playVideo.setVisibility(View.GONE);
    }
    private void toastStop()
    {
    	Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if(mplayer!=null){
    		mplayer.stop();
    		mplayer.release();
    		mplayer=null;
    	}
    	if(mplayer2!=null){
    		mplayer2.stop();
    		mplayer2.release();
    		mplayer2=null;
    	}
    	if(mDrawPad!=null){
    		mDrawPad.stopDrawPad();
    		mDrawPad=null;        		   
    	}
    	if(audioPlay!=null){
			audioPlay.stop();
			audioPlay.release();
			audioPlay=null;
		}
    	SDKFileUtils.deleteFile(dstPath);
    }
}
