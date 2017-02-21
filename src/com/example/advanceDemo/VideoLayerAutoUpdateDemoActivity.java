package com.example.advanceDemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;



import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;

import com.example.advanceDemo.view.DrawPadView;
import com.example.advanceDemo.view.PaintConstants;
import com.lansoeditor.demo.R;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.Layer;
import com.lansosdk.box.ViewLayerRelativeLayout;
import com.lansosdk.box.onDrawPadSizeChangedListener;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * 采用自动刷新模式 增加视频VideoLayer 到DrawPad中. 
 */
public class VideoLayerAutoUpdateDemoActivity extends Activity{
    private static final String TAG = "VideoLayerAutoUpdateDemoActivity";

    private String mVideoPath;

    private DrawPadView mDrawPad;
    
    private MediaPlayer mplayer=null;
    
    private Layer  mLayerMain=null;
    private ViewLayer mViewLayer=null;
    
//    
    private String dstPath=null;

    private ViewLayerRelativeLayout mLayerRelativeLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videopen_autoupdate_demo_layout);
        
        initView();
        
        mVideoPath = getIntent().getStringExtra("videopath");
        mDrawPad = (DrawPadView) findViewById(R.id.id_vauto_demo_drawpad_view);
        
        //在手机的/sdcard/lansongBox/路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
        dstPath=SDKFileUtils.newMp4PathInBox();
	    
        
	    //演示例子用到的.
		PaintConstants.SELECTOR.COLORING = true;
		PaintConstants.SELECTOR.KEEP_IMAGE = true;
		
		   //增加提示缩放到480的文字.
        DemoUtils.showScale480HintDialog(VideoLayerAutoUpdateDemoActivity.this);
        new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				 startPlayVideo();
			}
		}, 500);
    }
    private void startPlayVideo()
    {
          if (mVideoPath != null)
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
        	  mplayer.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					// TODO Auto-generated method stub
					stopDrawPad();
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
    	MediaInfo info=new MediaInfo(mVideoPath,false);
    	if(info.prepare())
    	{
        	
    		mDrawPad.setRealEncodeEnable(480,480,1000000,(int)info.vFrameRate,dstPath);
    		
    		mDrawPad.setUpdateMode(DrawPadUpdateMode.AUTO_FLUSH, 25);//25是帧率.
    		
        	mDrawPad.setDrawPadSize(480,480,new onDrawPadSizeChangedListener() {
    			
    			@Override
    			public void onSizeChanged(int viewWidth, int viewHeight) {
    				// TODO Auto-generated method stub
    				startDrawPad();
    			}
    		});
    	}
    }
    //Step2: Drawpad设置好后, 开始画板线程运行,并增加一个ViewLayer图层
    private void startDrawPad()
    {
    	mDrawPad.startDrawPad(null,null);
		
		mLayerMain=mDrawPad.addMainVideoLayer(mplayer.getVideoWidth(),mplayer.getVideoHeight(),null);
		if(mLayerMain!=null){
			mplayer.setSurface(new Surface(mLayerMain.getVideoTexture()));
		}
		mplayer.start();
		
		addViewLayer();
    }
    
    //Step3: 做好后, 停止画板, 因为画板里没有声音, 这里增加上原来的声音.
    private void stopDrawPad()
    {
    	if(mDrawPad!=null && mDrawPad.isRunning()){
			mDrawPad.stopDrawPad();
			toastStop();
			
			if(SDKFileUtils.fileExist(dstPath)){
		    	findViewById(R.id.id_vauto_demo_saveplay).setVisibility(View.VISIBLE);
			}
		}
    }
   
    private void addViewLayer()
    {
    	if(mDrawPad!=null && mDrawPad.isRunning()){
    		mViewLayer=mDrawPad.addViewLayer();
            mLayerRelativeLayout.bindViewLayer(mViewLayer);
            mLayerRelativeLayout.invalidate();
            
            ViewGroup.LayoutParams  params=mLayerRelativeLayout.getLayoutParams();
            params.height=mViewLayer.getPadHeight();  //因为布局时, 宽度一致, 这里调整高度,让他们一致.
            
            mLayerRelativeLayout.setLayoutParams(params);
    	}
    }
    private void initView()
    {
    	  mLayerRelativeLayout=(ViewLayerRelativeLayout)findViewById(R.id.id_vauto_demo_viewpenayout);
          
          findViewById(R.id.id_vauto_demo_saveplay).setOnClickListener(new OnClickListener() {
  			
  			@Override
  			public void onClick(View v) {
  				// TODO Auto-generated method stub
  				 if(SDKFileUtils.fileExist(dstPath)){
  		   			 	Intent intent=new Intent(VideoLayerAutoUpdateDemoActivity.this,VideoPlayerActivity.class);
  			    	    	intent.putExtra("videopath", dstPath);
  			    	    	startActivity(intent);
  		   		 }else{
  		   			 Toast.makeText(VideoLayerAutoUpdateDemoActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
  		   		 }
  			}
  		});
      	findViewById(R.id.id_vauto_demo_saveplay).setVisibility(View.GONE);
      	
      	
      	findViewById(R.id.id_vauto_demo_pausevideo).setOnClickListener(new OnClickListener() {
  			
  			@Override
  			public void onClick(View v) {
  				// TODO Auto-generated method stub
  					if(mplayer!=null && mplayer.isPlaying()){
  						mplayer.pause();
  					}
  			}	
  		});
      	findViewById(R.id.id_vauto_demo_startvideo).setOnClickListener(new OnClickListener() {
  			
  			@Override
  			public void onClick(View v) {
  				// TODO Auto-generated method stub
  					if(mplayer!=null && mplayer.isPlaying()==false){
  						mplayer.start();
  					}
  			}	
  		});
    }
    private void toastStop()
    {
    	Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	if(mplayer!=null){
    		mplayer.stop();
    		mplayer.release();
    		mplayer=null;
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
