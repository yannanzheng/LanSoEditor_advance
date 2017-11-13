package com.example.advanceDemo.camera;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;

import com.example.advanceDemo.VideoPlayerActivity;
import com.example.advanceDemo.view.VideoFocusView;
import com.lansoeditor.demo.R;
import com.lansosdk.box.CameraLayer;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.videoeditor.DrawPadCameraView;
import com.lansosdk.videoeditor.DrawPadCameraView.onViewAvailable;
import com.lansosdk.videoeditor.FilterLibrary.FilterAdjuster;
import com.lansosdk.videoeditor.FilterLibrary.OnGpuImageFilterChosenListener;
import com.lansosdk.videoeditor.FilterLibrary;
import com.lansosdk.videoeditor.LanSongUtil;
import com.lansosdk.videoeditor.SDKFileUtils;


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
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class CameraLayerRectActivity extends Activity implements Handler.Callback,OnClickListener{
   
	private static final long RECORD_CAMERA_TIME=20*1000*1000; //定义录制的时间为20s
	
	private static final String TAG = "CameraLayerDemoActivity";

    private DrawPadCameraView mDrawPad;
    
    private CameraLayer  mCameraLayer=null;
    private String dstPath=null;
	private PowerManager.WakeLock mWakeLock;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cameralayer_demo_layout);
        
        if(LanSongUtil.checkRecordPermission(getBaseContext())==false){
     	   Toast.makeText(getApplicationContext(), "请打开权限后,重试!!!", Toast.LENGTH_LONG).show();
     	   finish();
        }
        
        mDrawPad = (DrawPadCameraView) findViewById(R.id.id_cameralayer_padview);
        
        initView();
        
        dstPath=SDKFileUtils.newMp4PathInBox();
        
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				initDrawPad();  //开始录制.
			}
		}, 500);
    }
    @Override
    protected void onResume() {
    	super.onResume();
    	if (mWakeLock == null) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
			mWakeLock.acquire();
		}
    	tvTime.setVisibility(View.INVISIBLE);
		playVideo.setVisibility(View.INVISIBLE);
    }
    
    /**
     * Step1: 开始运行 DrawPad 容器
     */
    private void initDrawPad()
    {
    	/**
    	 * 当前CameraLayer 支持全屏和 正方形的宽高比,
    	 */
    	 int padWidth=480;
    	 int padHeight=480;
    	 
   	 
    	mDrawPad.setRecordMic(true);
    	mDrawPad.setCameraParam(true, null, true);
    	mDrawPad.setRealEncodeEnable(padWidth,padHeight,1000000,(int)25,dstPath);
    	
    	mDrawPad.setOnDrawPadProgressListener(drawPadProgressListener);

    	//设置当前DrawPad的宽度和高度,并把宽度自动缩放到父view的宽度,然后等比例调整高度.
    	mDrawPad.setDrawPadSize(padWidth,padHeight,new onDrawPadSizeChangedListener() {
    			@Override
    			public void onSizeChanged(int viewWidth, int viewHeight) {
    				startDrawPad();
    			}
	    });	
    }
    
    /**
     * Step2: 开始录制
     */
    private void startDrawPad()
    {
    	if(mDrawPad.setupDrawpad())
    	{
    		mCameraLayer=mDrawPad.getCameraLayer();
    		
    		/**
    		 * 可以在这里增加别的图层.
    		 */
    		mDrawPad.startPreview();
    		mDrawPad.startRecord();
    	}
    }
    private onDrawPadProgressListener drawPadProgressListener=new onDrawPadProgressListener() {
		
		@Override
		public void onProgress(DrawPad v, long currentTimeUs) {
			// TODO Auto-generated method stub
			
			if(currentTimeUs>=RECORD_CAMERA_TIME){  
				stopDrawPad();
			}
			if(tvTime!=null){
				tvTime.setVisibility(View.VISIBLE);
				long left=RECORD_CAMERA_TIME-currentTimeUs;
				float leftF=((float)left/1000000);
				 float b   =  (float)(Math.round(leftF*10))/10;  //保留一位小数.
						 
				tvTime.setText(String.valueOf(b));
			}
		}
	};
    /**
     * Step3: 停止容器 停止后,为新的视频文件增加上音频部分.
     */
    private void stopDrawPad()
    {
    	if(mDrawPad!=null && mDrawPad.isRunning())
    	{
    			mDrawPad.stopDrawPad();
				mCameraLayer=null;
				playVideo.setVisibility(View.VISIBLE);
		}
    }
    
    private FilterAdjuster mFilterAdjuster;
    /**
     * 选择滤镜效果, 
     */
    private void selectFilter()
    {
    	if(mDrawPad!=null && mDrawPad.isRunning()){
    		FilterLibrary.showDialog(this, new OnGpuImageFilterChosenListener() {

                @Override
                public void onGpuImageFilterChosenListener(final GPUImageFilter filter,String name) {
    	         	   mFilterAdjuster = new FilterAdjuster(filter);
    	         	   if(mCameraLayer!=null){
    	         		   mCameraLayer.switchFilterTo(filter);
    	         		  findViewById(R.id.id_cameralayer_demo_seek1).setVisibility(
  	         		            mFilterAdjuster.canAdjust() ? View.VISIBLE : View.GONE);
    	         	   }
                }
            });
    	}
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	if(mDrawPad!=null){
    		mDrawPad.stopDrawPad();
    	}
    	 if (mWakeLock != null) {
    			mWakeLock.release();
    			mWakeLock = null;
    		}
    }


   @Override
	protected void onDestroy() {
			super.onDestroy();
			SDKFileUtils.deleteFile(dstPath);
			dstPath=null;
	}
   //-------------------------------------------一下是UI界面和控制部分.---------------------------------------------------
   private SeekBar AdjusterFilter;
   private TextView tvTime;
   private LinearLayout  playVideo;
   private void initView()
   {
	   tvTime=(TextView)findViewById(R.id.id_cameralayer_timetv);
	   playVideo=(LinearLayout)findViewById(R.id.id_cameralayer_saveplay);
	   
	   playVideo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 if(SDKFileUtils.fileExist(dstPath)){
		   			 	Intent intent=new Intent(CameraLayerRectActivity.this,VideoPlayerActivity.class);
		   			 	intent.putExtra("videopath", dstPath);
		   			 	startActivity(intent);
		   		 }else{
		   			 Toast.makeText(CameraLayerRectActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
		   		 }
			}
		});
	   playVideo.setVisibility(View.GONE);
   		findViewById(R.id.id_cameralayer_flashlight).setOnClickListener(this);
		findViewById(R.id.id_cameralayer_frontcamera).setOnClickListener(this);
		findViewById(R.id.id_camerape_demo_selectbtn).setOnClickListener(this);
		
		handler = new Handler(this);
		
		
	   AdjusterFilter=(SeekBar)findViewById(R.id.id_cameralayer_demo_seek1);
       AdjusterFilter.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				  if (mFilterAdjuster != null) {
			            mFilterAdjuster.adjust(progress);
			        }
			}
		});
   }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.id_cameralayer_frontcamera:
				if (mCameraLayer!=null && CameraLayer.isSupportFrontCamera()) {
					handler.sendEmptyMessage(MSG_CHANGE_CAMERA);
				}
				break;
			case R.id.id_cameralayer_flashlight:
				if (mCameraLayer!=null) {
					mCameraLayer.changeFlash();
				}
				break;
			case R.id.id_camerape_demo_selectbtn:
				selectFilter();
				break;
		default:
			break;
		}
	}
	//-----------------------------------------------------------------------
	private Handler handler;
	private boolean mAllowTouchFocus = true;
	private static final int MSG_CHANGE_FLASH = 66;
	private static final int MSG_CHANGE_CAMERA = 8;

	public static final int REQUEST_VIDEOPROCESS = 5;
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case MSG_CHANGE_CAMERA:
					mCameraLayer.changeCamera();	
				break;
			case MSG_CHANGE_FLASH:
				mCameraLayer.changeFlash();
				break;
		}
		return false;
	}
	
	

	   //-----------------------------
//  private MediaPlayer mplayer=null;
//  private VideoLayer videolayer=null;
  /**
   * 增加一个视频图层, 仅仅测试使用.
   */
//  private void addVideoLayer()
//  {
//	   if(mDrawPadView!=null && mDrawPadView.isRunning() && videolayer==null)
//	   {
//		   videolayer=mDrawPadView.addVideoLayer(480, 480, null);
//		   if(videolayer!=null){
//				videolayer.setScale(0.5f);
//				
//				mplayer=new MediaPlayer();
//	   			try {
//					mplayer.setDataSource("/sdcard/480x480.mp4");  //注意, 我们用了默认路径
//				}  catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//	   			
//	        	  try {
//					mplayer.prepare();
//					//设置到播放器中.
//					mplayer.setSurface(new Surface(videolayer.getVideoTexture()));
//					
//				} catch (IllegalStateException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//	        	  
//	        	  
//	        	  mplayer.setOnCompletionListener(new OnCompletionListener() {
//					
//					@Override
//					public void onCompletion(MediaPlayer mp) {
//						// TODO Auto-generated method stub
//						//播放完成后, 删除.
//						if(videolayer!=null && mDrawPadView!=null){
//							mDrawPadView.removeLayer(videolayer);
//						}
//					}
//				});
//	        	  mplayer.start();
//			}
//	   }
//  }
}
