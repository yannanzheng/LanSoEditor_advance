package com.example.advanceDemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;



import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;

import com.example.advanceDemo.view.PaintConstants;
import com.lansoeditor.demo.R;
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
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
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
 * 采用自动刷新模式 
 *  
 *  先播放一个视频, 然后在10秒后,插入另一个视频.并增加进入动画.
 */
public class VideoLayerTransformActivity extends Activity{
    private static final String TAG = "VideoLayerTransformActivity";

    private String mVideoPath;
    private String videoPath2;

    private DrawPadView mDrawPad;
    
    private MediaPlayer mplayer=null;
    
    private MediaPlayer mplayer2=null;
    
    private static final int VIDEO2_START_TIME=10*1000*1000;  //第二个视频开始时间.
    
    
    private VideoLayer  videoLayer1=null;
    private VideoLayer  videoLayer2=null;
    private MediaInfo  mInfo=null;
    private LinearLayout  playVideo;
    private String dstPath=null;
    private MoveCentor mVideoMove=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videolay_transform_layout);
        
        initView();
        
        mVideoPath = getIntent().getStringExtra("videopath");
        mDrawPad = (DrawPadView) findViewById(R.id.id_videolayer_drawpad);
        
        //在手机的默认路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
        dstPath=SDKFileUtils.newMp4PathInBox();
	    
        new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				 startPlayVideo();
			}
		}, 500);
        
        new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				videoPath2=CopyFileFromAssets.copyAssets(getApplicationContext(), "ping25s.mp4");
			}
		}).start();
    }
    private void startPlayVideo()
    {
    		
    	mInfo=new MediaInfo(mVideoPath, false);
          if (mInfo.prepare())
          {
        	  mplayer=new MediaPlayer();
        	  try {
				mplayer.setDataSource(mVideoPath);
			}  catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	  mplayer.setOnPreparedListener(new OnPreparedListener() {
				
				@Override
				public void onPrepared(MediaPlayer mp) {
					// TODO Auto-generated method stub
					initDrawPad();
				}
			});
        	  mplayer.prepareAsync();
          }
          else {
              Log.e(TAG, "Null Data Source\n");
              finish();
              return;
          }
    }
    //Step1: 设置DrawPad 画板的尺寸.并设置是否实时录制画板上的内容.
    private void initDrawPad()
    {
    		mDrawPad.setRealEncodeEnable(480,480,1000000,(int)25,dstPath);
    		
    		mDrawPad.setUpdateMode(DrawPadUpdateMode.AUTO_FLUSH, 25);//25是帧率.
    		
        	mDrawPad.setDrawPadSize(480,480,new onDrawPadSizeChangedListener() {
    			
    			@Override
    			public void onSizeChanged(int viewWidth, int viewHeight) {
    				startDrawPad();
    			}
    		});
        	mDrawPad.setOnDrawPadProgressListener(new onDrawPadProgressListener() {
				
				@Override
				public void onProgress(DrawPad v, long currentTimeUs) {
					
					if(currentTimeUs>20*1000*1000){
						stopDrawPad();
					}
					
					if(currentTimeUs>VIDEO2_START_TIME && videoLayer2==null)  //如果大于10秒,则当前视频退出,增加另一个视频.
					{
						startVideo2();
					}
					
					if(mVideoMove!=null){
						mVideoMove.run(currentTimeUs);
					}
				}
			});
    }
    /**
     * Step2: Drawpad设置好后, 开始画板线程运行,并增加一个ViewLayer图层
     */
    private void startDrawPad()
    {
    	mDrawPad.startDrawPad();
		
    	videoLayer1=mDrawPad.addMainVideoLayer(mplayer.getVideoWidth(),mplayer.getVideoHeight(),null);
		if(videoLayer1!=null)
		{
			mplayer.setSurface(new Surface(videoLayer1.getVideoTexture()));
		}
		mplayer.start();
    }
    /**
     * Step3: 做好后, 停止画板, 因为画板里没有声音, 这里增加上原来的声音.
     */
    private void stopDrawPad()
    {
    	if(mDrawPad!=null && mDrawPad.isRunning()){
			mDrawPad.stopDrawPad();
			toastStop();
			
			if(SDKFileUtils.fileExist(dstPath)){
				playVideo.setVisibility(View.VISIBLE);
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
    private void startVideo2()
    {
	    	   if(videoPath2==null){
	    		   videoPath2=CopyFileFromAssets.copyAssets(getApplicationContext(), "ping25s.mp4");
	    	   }
        	  mplayer2=new MediaPlayer();
        	  try {
				mplayer2.setDataSource(videoPath2);
        	  }  catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
        	  }
        	  mplayer2.setOnPreparedListener(new OnPreparedListener() {
				
				@Override
				public void onPrepared(MediaPlayer mp) {
					// TODO Auto-generated method stub
					if(mDrawPad!=null && mDrawPad.isRunning()){
						videoLayer2=mDrawPad.addVideoLayer(mplayer2.getVideoWidth(), mplayer2.getVideoHeight(),null);
						if(videoLayer2!=null)
						{
							mplayer2.setSurface(new Surface(videoLayer2.getVideoTexture()));
							
							mVideoMove=new MoveCentor(videoLayer2, VIDEO2_START_TIME+1000*1000, 1*1000*1000, mInfo.vFrameRate);
							videoLayer2.setVisibility(Layer.INVISIBLE);
						}
						mplayer2.start();
					}
				}
        	  });
        	mplayer2.prepareAsync();
        	
        	new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					if(mplayer!=null){
			    		mplayer.stop();
			    		mplayer.release();
			    		mplayer=null;
			    	}
				}
			}, 1500);  //关闭第一个视频.
    }
    private void initView()
    {
    	  playVideo=(LinearLayout)findViewById(R.id.id_videoLayer_saveplay);
    	  playVideo.setOnClickListener(new OnClickListener() {
  			
  			@Override
  			public void onClick(View v) {
  				 if(SDKFileUtils.fileExist(dstPath)){
  		   			 	Intent intent=new Intent(VideoLayerTransformActivity.this,VideoPlayerActivity.class);
  		   			 	intent.putExtra("videopath", dstPath);
  		   			 	startActivity(intent);
  		   		 }else{
  		   			 Toast.makeText(VideoLayerTransformActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
  		   		 }
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
    	  if(SDKFileUtils.fileExist(dstPath)){
    		  SDKFileUtils.deleteFile(dstPath);
          }
    }
}
