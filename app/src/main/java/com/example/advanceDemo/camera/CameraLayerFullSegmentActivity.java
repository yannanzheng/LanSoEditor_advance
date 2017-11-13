package com.example.advanceDemo.camera;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBeautyAdvanceFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBeautyFilter;

import com.example.advanceDemo.VideoPlayerActivity;
import com.example.advanceDemo.view.VideoFocusView;
import com.example.advanceDemo.view.VideoProgressView;
import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.CameraLayer;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.GifLayer;
import com.lansosdk.box.LanSoEditorBox;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.Layer;
import com.lansosdk.box.MVLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.ViewLayerRelativeLayout;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.videoeditor.CopyDefaultVideoAsyncTask;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.DrawPadCameraView;
import com.lansosdk.videoeditor.DrawPadView;
import com.lansosdk.videoeditor.FilterLibrary;
import com.lansosdk.videoeditor.LanSongUtil;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.DrawPadCameraView.onViewAvailable;
import com.lansosdk.videoeditor.FilterLibrary.OnGpuImageFilterChosenListener;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CameraLayerFullSegmentActivity extends Activity implements OnClickListener{
   
	public static final long MAX_RECORD_TIME =15 * 1000 *1000;  //设置录制的最大时间.  15秒.
	
	public static final long MIN_RECORD_TIME = 2 * 1000 *1000;   //录制的最小时间
	
	private static final String TAG = "CameraLayerFullSegmentActivity";

    private DrawPadCameraView mDrawPadCamera;
    
    private CameraLayer  mCameraLayer=null;
    
    private String dstPath=null;
    
	VideoFocusView focusView;
	private PowerManager.WakeLock mWakeLock;

    private VideoProgressView progressView;
    private Button cancelBtn;
    private Button okBtn;
    private Button recorderVideoBtn;
	
	private long currentSegDuration;   //当前正在录制段的时间. 单位US
	private long beforeSegDuration;   //正在录制的这一段前的总时间. 单位US
    private ArrayList<String>  segmentArray=new ArrayList<String>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        LanSongUtil.hideBottomUIMenu(this);
        
        setContentView(R.layout.cameralayer_fullsegment_layout);
        
        if(LanSongUtil.checkRecordPermission(getBaseContext())==false){
      	   Toast.makeText(getApplicationContext(), "请打开权限后,重试!!!", Toast.LENGTH_LONG).show();
      	   finish();
         }
        
        mDrawPadCamera = (DrawPadCameraView) findViewById(R.id.id_fullscreen_padview);
        initView();
		
		recorderVideoBtn.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:  
						segmentStart();
						break;
					case MotionEvent.ACTION_UP:  //录制结束.
						segmentStop();
						break;
					}
				return true;
			}
		});
        
        dstPath=SDKFileUtils.newMp4PathInBox();
        initDrawPad();  //开始录制.
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	if (mWakeLock == null) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
			mWakeLock.acquire();
		}
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				startDrawPad();
			}
		},100);
    }
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	if(mDrawPadCamera!=null){
    		mDrawPadCamera.stopDrawPad();
    	}
    	if (mWakeLock != null) {
			mWakeLock.release();
			mWakeLock = null;
    	}
    }
   @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
			super.onDestroy();
		    if(SDKFileUtils.fileExist(dstPath)){
		    	SDKFileUtils.deleteFile(dstPath);
		    	dstPath=null;
		    }
	}
    
    /**
     *初始化 DrawPad 容器
     */
    private void initDrawPad()
    {
    	//设置使能 实时录制, 即把正在DrawPad中呈现的画面实时的保存下来,实现所见即所得的模式
    	 DisplayMetrics dm = new DisplayMetrics();
    	 dm = getResources().getDisplayMetrics();
    	 
    	 /**
    	  * 因手机屏幕是16:9;全屏模式,建议分辨率设置为960x544;
    	  */
    	 int padWidth=544;  
    	 int padHeight=960;
    		
    	 mDrawPadCamera.setRecordMic(true);
    	mDrawPadCamera.setRealEncodeEnable(padWidth,padHeight,3000000,(int)25,SDKFileUtils.newMp4PathInBox());
    	
    	mDrawPadCamera.setCameraParam(false,null , false);
    	
    	//设置处理进度监听.
    	mDrawPadCamera.setOnDrawPadProgressListener(drawPadProgressListener);

    	mDrawPadCamera.setOnViewAvailable(new onViewAvailable() {
			
			@Override
			public void viewAvailable(DrawPadCameraView v) {
				// TODO Auto-generated method stub
				startDrawPad();
			}
		});
    }
    /**
     * 开始运行 Drawpad线程.
     */
      private void startDrawPad()
      {
    	    if(mDrawPadCamera.setupDrawpad())
    	    {
    	    	mCameraLayer=mDrawPadCamera.getCameraLayer();
    	    	
    	    	mDrawPadCamera.startPreview();
    	    }
      }
      /**
       * 录制录制, 并开始另一个Activity 去预览录制好的画面.
       */
      private void stopDrawPad()
      {
    	  	if(mDrawPadCamera!=null && mDrawPadCamera.isRunning())
	      	{		
	    	  		/**
	    	  		 * 如果正在录制,则把最后一段增加进来.
	    	  		 */
	    		  	if(mDrawPadCamera.isRecording())
					{
						String segmentPath=mDrawPadCamera.segmentStop();
						segmentArray.add(segmentPath); 
					}
	    		  	/**
	    		  	 * 停止 容器.
	    		  	 */
	  				mDrawPadCamera.stopDrawPad();
	  				mCameraLayer=null;
	  				
	  				 /**
	  				  * 停止后, 得到多段视频, 开始拼接视频.
	  				  */
	  				if(segmentArray.size()>0)
	  				{
	  					VideoEditor editor=new VideoEditor();
	  					String[] segments=new String[segmentArray.size()];  
	  				     for(int i=0;i<segmentArray.size();i++){  
	  				    	 segments[i]=(String)segmentArray.get(i);  
	  				     }  
	  					editor.executeConcatMP4(segments, dstPath);
	  				}
	  				/**
	  				 * 开始播放.
	  				 */
	  				 if(SDKFileUtils.fileExist(dstPath)){
			   			 	Intent intent=new Intent(CameraLayerFullSegmentActivity.this,VideoPlayerActivity.class);
				    	    	intent.putExtra("videopath", dstPath);
				    	    	startActivity(intent);
			   		 }else{
			   			 Toast.makeText(CameraLayerFullSegmentActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
			   		 }
	  		}
      }
      /**
       * 视频每处理一帧,则会执行这里的回调, 返回当前处理后的时间戳,单位微秒. 
       */
    private onDrawPadProgressListener drawPadProgressListener=new onDrawPadProgressListener() {
		
		@Override
		public void onProgress(DrawPad v, long currentTimeUs) {
			// TODO Auto-generated method stub
			
			currentSegDuration=currentTimeUs;
			
			long totalTime=beforeSegDuration+ currentTimeUs;
			
			if (totalTime < MIN_RECORD_TIME) 
			{
				okBtn.setVisibility(View.INVISIBLE);
			} else if (totalTime >= MIN_RECORD_TIME && totalTime < MAX_RECORD_TIME) {
				okBtn.setVisibility(View.VISIBLE);
			} else if (totalTime >= MAX_RECORD_TIME) {
				stopDrawPad();
			}
			
			if(progressView!=null){
				progressView.setProgressTime(currentTimeUs/1000);
			}
		}
	};
	/**
	 * 开始录制一段视频.
	 */
    private void segmentStart()
    {
    	if(mDrawPadCamera.isRecording()==false){
			mDrawPadCamera.segmentStart();	
			progressView.setCurrentState(VideoProgressView.State.START);
		}
    }
    /**
     * 停止一段的录制
     */
    private void segmentStop()
    {
    	if(mDrawPadCamera.isRecording())
		{
			String segmentPath=mDrawPadCamera.segmentStop();
			progressView.setCurrentState(VideoProgressView.State.PAUSE);
		
			int timeMS=(int)(currentSegDuration/1000);  //转换为毫秒.
			progressView.putTimeList(timeMS);
			
			beforeSegDuration+=currentSegDuration;
			
			segmentArray.add(segmentPath);  // 把一段录制好的,增加到数组里.
			
			cancelBtn.setVisibility(View.VISIBLE);
		}
    }
   //-------------------------------------------一下是UI界面和控制部分.---------------------------------------------------
   private void initView()
   {
   		findViewById(R.id.id_fullscreen_flashlight).setOnClickListener(this);
		findViewById(R.id.id_fullscreen_frontcamera).setOnClickListener(this);
		findViewById(R.id.id_fullscreen_filter).setOnClickListener(this);

        progressView=(VideoProgressView)findViewById(R.id.id_fullsegment_progress);
        progressView.setMinRecordTime(MIN_RECORD_TIME/1000f);
        progressView.setMaxRecordTime(MAX_RECORD_TIME/1000f);
        
        
        cancelBtn=(Button)findViewById(R.id.id_fullsegment_cancel);
		okBtn=(Button)findViewById(R.id.id_fullsegment_next);
		recorderVideoBtn=(Button)findViewById(R.id.id_fullsegment_video);
		
		cancelBtn.setOnClickListener(this);
		okBtn.setOnClickListener(this);
   }
	private volatile boolean isDeleteState = false;
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.id_fullsegment_cancel:
			
			if(segmentArray.size()==0){
				isDeleteState = false;
				progressView.setCurrentState(VideoProgressView.State.DELETE);
				cancelBtn.setBackgroundResource(R.drawable.video_record_backspace);
				break;
			}
			if (isDeleteState) { //在按一下, 删除.
				isDeleteState = false;
				deleteSegment();
				
			
				
				progressView.setCurrentState(VideoProgressView.State.DELETE);
				cancelBtn.setBackgroundResource(R.drawable.video_record_backspace);
			} else { 
				isDeleteState = true; 
				progressView.setCurrentState(VideoProgressView.State.BACKSPACE);
				cancelBtn.setBackgroundResource(R.drawable.video_record_delete);
			}
			break;
		case R.id.id_fullsegment_next:
			stopDrawPad();
			break;
			case R.id.id_fullscreen_frontcamera:
				if(mCameraLayer!=null){
					if(mDrawPadCamera.isRunning() && CameraLayer.isSupportFrontCamera())  
					{
						//先把DrawPad暂停运行.
						mDrawPadCamera.pausePreview();
						mCameraLayer.changeCamera();	
						mDrawPadCamera.resumePreview(); //再次开启.
					}
				}
				break;
			case R.id.id_fullscreen_flashlight:
				if(mCameraLayer!=null){
					mCameraLayer.changeFlash();
				}
				break;
			case R.id.id_fullscreen_filter:
					selectFilter();
				break;
		default:
			break;
		}
	}
	/**
	 * 从数组里删除最后一段视频
	 */
	private void deleteSegment()
	{
		if(segmentArray.size()>0){
			
			String filePath=segmentArray.get(segmentArray.size()-1); //拿到最后一个.
			
			SDKFileUtils.deleteFile(filePath);
			segmentArray.remove(segmentArray.size()-1);
			
			beforeSegDuration-=progressView.getLastTime()*1000;
			if(beforeSegDuration<=0){
				beforeSegDuration=0;
			}
		}
	}

    /**
     * 选择滤镜效果, 
     */
    private void selectFilter()
    {
    	if(mDrawPadCamera!=null && mDrawPadCamera.isRunning()){
    		FilterLibrary.showDialog(this, new OnGpuImageFilterChosenListener() {

                @Override
                public void onGpuImageFilterChosenListener(final GPUImageFilter filter,String name) {
                	/**
                	 * 通过DrawPad线程去切换 filterLayer的滤镜
                	 * 有些Filter是可以调节的,这里为了代码简洁,暂时没有演示, 可以在CameraeLayerDemoActivity中查看.
                	 */
                	if(mCameraLayer!=null){
                		mCameraLayer.switchFilterTo(filter);
                	}
                }
            });
    	}
    }
}

