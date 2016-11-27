package com.example.lansongeditordemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;



import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;

import com.example.lansongeditordemo.view.DrawPadView;
import com.example.lansongeditordemo.view.PaintConstants;
import com.lansoeditor.demo.R;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.ViewPen;
import com.lansosdk.box.Pen;
import com.lansosdk.box.ViewPenRelativeLayout;
import com.lansosdk.box.onDrawPadProgressListener;
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
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 *  演示: 视频涂鸦
 *  
 *
 */
public class VideoViewPenDemoActivity extends Activity{
    private static final String TAG = "VViewDrawImageDemoActivity";

    private String mVideoPath;

    private DrawPadView mDrawPadView;
    
    private MediaPlayer mplayer=null;
    private MediaPlayer mplayer2=null;
    
    private Pen  mPenMain=null;
    private ViewPen mViewPen=null;
    
//    
    private String editTmpPath=null;  //用来保存画板录制的目标文件路径.
    private String dstPath=null;

    private ViewPenRelativeLayout mPenRelativeLayout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
		 Thread.setDefaultUncaughtExceptionHandler(new snoCrashHandler());
        setContentView(R.layout.vview_drawimage_demo_layout);
        
        
        mVideoPath = getIntent().getStringExtra("videopath");
        mDrawPadView = (DrawPadView) findViewById(R.id.id_vview_realtime_drawpadview);
        
      
        mPenRelativeLayout=(ViewPenRelativeLayout)findViewById(R.id.id_vview_realtime_gllayout);
      
    	
        initView();
        
        //在手机的/sdcard/lansongBox/路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
        editTmpPath=SDKFileUtils.newMp4PathInBox();
        dstPath=SDKFileUtils.newMp4PathInBox();
	    
        
	    //演示例子用到的.
		PaintConstants.SELECTOR.COLORING = true;
		PaintConstants.SELECTOR.KEEP_IMAGE = true;
		
		//增加提示缩放到480的文字.
        DemoUtils.showScale480HintDialog(VideoViewPenDemoActivity.this);
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				 startPlayVideo();
			}
		}, 100);
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
        	
    		mDrawPadView.setRealEncodeEnable(480,480,1000000,(int)info.vFrameRate,editTmpPath);
    		
        	mDrawPadView.setDrawPadSize(480,480,new onDrawPadSizeChangedListener() {
    			
    			@Override
    			public void onSizeChanged(int viewWidth, int viewHeight) {
    				// TODO Auto-generated method stub
    				startDrawPad();
    			}
    		});
    	}
    }
    //Step2: Drawpad设置好后, 开始画板线程运行,并增加一个ViewPen画笔
    private void startDrawPad()
    {
    	mDrawPadView.startDrawPad(new DrawPadProgressListener(),null);
		
		mPenMain=mDrawPadView.addMainVideoPen(mplayer.getVideoWidth(),mplayer.getVideoHeight(),null);
		if(mPenMain!=null){
			mplayer.setSurface(new Surface(mPenMain.getVideoTexture()));
		}
		mplayer.start();
		addViewPen();
    }
	  private class DrawPadProgressListener implements onDrawPadProgressListener
	  {
		  	@Override
		  	public void onProgress(DrawPad v, long currentTimeUs) {
		  		// TODO Auto-generated method stub
		  			if(currentTimeUs>7000*1000)  //在第7秒的时候, 不再显示.
		  			{
		  				hideWord();
		  			}else if(currentTimeUs>3*1000*1000)  //在第三秒的时候, 显示tvWord
		  			{
		  				showWord();
		  			}
		  	}
	  }
    
    //Step3: 做好后, 停止画板, 因为画板里没有声音, 这里增加上原来的声音.
    private void stopDrawPad()
    {
    	if(mDrawPadView!=null && mDrawPadView.isRunning()){
			mDrawPadView.stopDrawPad();
			
			toastStop();
			if(SDKFileUtils.fileExist(editTmpPath)){
				boolean ret=VideoEditor.encoderAddAudio(mVideoPath,editTmpPath,SDKDir.TMP_DIR, dstPath);
				if(!ret){
					dstPath=editTmpPath;
				}else
					SDKFileUtils.deleteFile(editTmpPath);
		    	findViewById(R.id.id_vview_realtime_saveplay).setVisibility(View.VISIBLE);
			}
		}
    }
   
    private void addViewPen()
    {
    	if(mDrawPadView!=null && mDrawPadView.isRunning()){
    		mViewPen=mDrawPadView.addViewPen();
            mPenRelativeLayout.bindViewPen(mViewPen);
            mPenRelativeLayout.invalidate();
            
            ViewGroup.LayoutParams  params=mPenRelativeLayout.getLayoutParams();
            params.height=mViewPen.getHeight();  //因为布局时, 宽度一致, 这里调整高度,让他们一致.
            
            mPenRelativeLayout.setLayoutParams(params);
    	}
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
    	
    	if(mplayer2!=null){
    		mplayer2.stop();
    		mplayer2.release();
    		mplayer2=null;
    	}
    	
    	if(mDrawPadView!=null){
    		mDrawPadView.stopDrawPad();
    		mDrawPadView=null;        		   
    	}
    	  if(SDKFileUtils.fileExist(dstPath)){
    		  SDKFileUtils.deleteFile(dstPath);
          }
          if(SDKFileUtils.fileExist(editTmpPath)){
        	  SDKFileUtils.deleteFile(editTmpPath);
          } 
    }
    //--------------------------------------一下为UI界面-----------------------------------------------------------
    private void initView()
    {
    	  tvWord=(TextView)findViewById(R.id.id_vview_tvtest);
          
          findViewById(R.id.id_vview_realtime_saveplay).setOnClickListener(new OnClickListener() {
  			
  			@Override
  			public void onClick(View v) {
  				// TODO Auto-generated method stub
  				 if(SDKFileUtils.fileExist(dstPath)){
  		   			 	Intent intent=new Intent(VideoViewPenDemoActivity.this,VideoPlayerActivity.class);
  			    	    	intent.putExtra("videopath", dstPath);
  			    	    	startActivity(intent);
  		   		 }else{
  		   			 Toast.makeText(VideoViewPenDemoActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
  		   		 }
  			}
  		});
      	findViewById(R.id.id_vview_realtime_saveplay).setVisibility(View.GONE);
    }
    private TextView tvWord; 
    private void showWord()
    {
    	 if(tvWord!=null&& tvWord.getVisibility()!=View.VISIBLE){
				 tvWord.startAnimation(AnimationUtils.loadAnimation(VideoViewPenDemoActivity.this, R.anim.slide_right_in));
				 tvWord.setVisibility(View.VISIBLE); 
			 }
    }
    private void hideWord()
    {
    	 if(tvWord!=null&& tvWord.getVisibility()==View.VISIBLE){
				 tvWord.startAnimation(AnimationUtils.loadAnimation(VideoViewPenDemoActivity.this, R.anim.slide_right_out));
  			 tvWord.setVisibility(View.GONE); 
			 }
    }
    
}
