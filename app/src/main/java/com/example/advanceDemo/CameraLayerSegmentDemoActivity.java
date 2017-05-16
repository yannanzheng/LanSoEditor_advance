package com.example.advanceDemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImagePixelationFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;

import com.example.advanceDemo.GPUImageFilterTools.FilterAdjuster;
import com.example.advanceDemo.GPUImageFilterTools.OnGpuImageFilterChosenListener;
import com.example.advanceDemo.view.BitmapCache;
import com.example.advanceDemo.view.ShowHeart;
import com.example.advanceDemo.view.VideoFocusView;
import com.example.advanceDemo.view.VideoProgressView;
import com.lansoeditor.demo.R;
import com.lansosdk.box.CameraLayer;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.LanSoEditorBox;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.videoeditor.DrawPadCameraView;
import com.lansosdk.videoeditor.DrawPadView;
import com.lansosdk.videoeditor.LanSongUtil;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.DrawPadView.onViewAvailable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class CameraLayerSegmentDemoActivity extends Activity implements OnClickListener{
   
	
	private static final String TAG = "CameraLayerSegmentDemoActivity";

    private DrawPadCameraView mDrawPadView;
    
    /**
     * 用来存放当前分段录制的多段视频文件, 
     * 如果您要回删,或增加一个别的用DrawPad生成的视频文件, 则可以在这个数组里增删,插入或排序.
     */
    private ArrayList<String>  segmentArray=new ArrayList<String>();
    
    private CameraLayer  mCameraLayer=null;
	VideoFocusView focusView;
	private PowerManager.WakeLock mWakeLock;
	
	private String dstPath=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cameralayer_segment_layout);
        
        if(LanSongUtil.checkRecordPermission(getBaseContext())==false){
      	   Toast.makeText(getApplicationContext(), "请打开权限后,重试!!!", Toast.LENGTH_LONG).show();
      	   finish();
         }
        
        mDrawPadView = (DrawPadCameraView) findViewById(R.id.id_cameralayer_padview);
        initView();

        dstPath=SDKFileUtils.createMp4FileInBox();
		new Handler().postDelayed(new Runnable() {
					
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//showHintDialog();
				createDrawPad();
			}
		}, 500);
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
    }
    /**
     * Step1: 创建画板.
     */
    private void createDrawPad()
    {
    	 int padWidth=480;
    	 int padHeight=480;
    	 
    	mDrawPadView.setRecordMic(true);
    	mDrawPadView.setRealEncodeEnable(padWidth,padHeight,1000000,(int)25,null);

    	//设置进度监听
    	mDrawPadView.setOnDrawPadProgressListener(drawPadProgressListener);
    	//设置当前DrawPad的宽度和高度,并把宽度自动缩放到父view的宽度,然后等比例调整高度.
    	mDrawPadView.setDrawPadSize(padWidth,padHeight,new onDrawPadSizeChangedListener() {
	    			
	    			@Override
	    			public void onSizeChanged(int viewWidth, int viewHeight) {
	    				// TODO Auto-generated method stub
	    				startDrawPad();
	    			}
	    });	
    }
    /**
     * step2: 开始画板
     */
    private void startDrawPad()
    {
    	/**
    	 * 这里设置先不开始录制.
    	 */
    	mDrawPadView.pauseDrawPadRecord();
		if(mDrawPadView.startDrawPad())
		{
			//增加一个CameraLayer
			mCameraLayer=	mDrawPadView.getCameraLayer();
			if(mCameraLayer!=null){
				mCameraLayer.startPreview();
				doAutoFocus(); //摄像头打开后,开始自动聚焦.
			}
		}
    }
    /**
     * Step3: 停止画板 停止后,为新的视频文件增加上音频部分.
     */
    private void stopDrawPad()
    {
    	if(mDrawPadView!=null && mDrawPadView.isRunning())
    	{
				mDrawPadView.stopDrawPad();
				mCameraLayer=null;
				/**
				 * 停止后, 得到多段视频, 这里拼接,
				 */
				if(segmentArray.size()>0)
				{
					
					long  beforeDraw=System.currentTimeMillis();
					   
					 
					VideoEditor editor=new VideoEditor();
					String[] segments=new String[segmentArray.size()];  
				     for(int i=0;i<segmentArray.size();i++){  
				    	 segments[i]=(String)segmentArray.get(i);  
				     }  
					editor.executeConcatMP4(segments, dstPath);
					
					  Log.i(TAG,"消耗的时间是 :"+ (System.currentTimeMillis() - beforeDraw));
				}
				playVideo.setVisibility(View.VISIBLE);
		}
    }
    private onDrawPadProgressListener drawPadProgressListener=new onDrawPadProgressListener() {
		
		@Override
		public void onProgress(DrawPad v, long currentTimeUs) {
			// TODO Auto-generated method stub
			
			if(tvTime!=null){
				long left=currentTimeUs;
				
				float leftF=((float)left/1000000);
				 float b   =  (float)(Math.round(leftF*10))/10;  //保留一位小数.
						 
				tvTime.setText(String.valueOf(b));
			}
		}
	};
   
    
    /**
     * 选择滤镜效果, 
     */
    private void selectFilter()
    {
    	if(mDrawPadView!=null && mDrawPadView.isRunning()){
    		GPUImageFilterTools.showDialog(this, new OnGpuImageFilterChosenListener() {

                @Override
                public void onGpuImageFilterChosenListener(final GPUImageFilter filter) {
                	
                	//在这里通过DrawPad线程去切换 filterLayer的滤镜
    	         	   if(mDrawPadView.switchFilterTo(mCameraLayer,filter)){
    	         		   //如果这个滤镜 可调, 显示可调节进度条.
    	         	   }
                }
            });
    	}
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	if(mDrawPadView!=null){
    		mDrawPadView.stopDrawPad();
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
		    }
	}
   
   //-------------------------------------------一下是UI界面和控制部分.---------------------------------------------------
   private TextView tvTime;
   private LinearLayout  playVideo;
   private void initView()
   {
	   tvTime=(TextView)findViewById(R.id.id_cameralayer_timetv);
	   playVideo=(LinearLayout)findViewById(R.id.id_cameralayer_saveplay);
	   
	   playVideo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(SDKFileUtils.fileExist(dstPath)){
		   			 	Intent intent=new Intent(CameraLayerSegmentDemoActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
		   		 }else{
		   			 Toast.makeText(CameraLayerSegmentDemoActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
		   		 }
			}
		});
	   playVideo.setVisibility(View.GONE);

   	focusView=(VideoFocusView)findViewById(R.id.id_cameralayer_focus_view);
   	focusView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return onSquareFocusViewTouch(v,event);
			}
		});
   	
   	
   		findViewById(R.id.id_cameralayer_flashlight).setOnClickListener(this);
		findViewById(R.id.id_cameralayer_frontcamera).setOnClickListener(this);
		findViewById(R.id.id_camerape_demo_selectbtn).setOnClickListener(this);
		findViewById(R.id.id_camerape_demo_recordbtn).setOnClickListener(this);
		findViewById(R.id.id_camerape_demo_completebtn).setOnClickListener(this);
   }
   int count=100;
	private boolean mAllowTouchFocus = true;
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.id_cameralayer_frontcamera:
				mCameraLayer.changeCamera();
				//doAutoFocus(); //摄像头打开后
				break;
			case R.id.id_cameralayer_flashlight:
				mCameraLayer.changeFlash();
				break;
			case R.id.id_camerape_demo_selectbtn:
				selectFilter();
				break;
			case R.id.id_camerape_demo_completebtn:
				stopDrawPad();
				break;
			case R.id.id_camerape_demo_recordbtn:
				if(mDrawPadView!=null)
				{
					if(mDrawPadView.isRecording())
					{
						String segmentPath=mDrawPadView.segmentStop();
						/**
						 * 把一段录制好的,增加到数组里.
						 */
						segmentArray.add(segmentPath); 
						((Button )findViewById(R.id.id_camerape_demo_recordbtn)).setText("开始");
					}else{
						mDrawPadView.segmentStart();
						
						((Button )findViewById(R.id.id_camerape_demo_recordbtn)).setText("暂停");
					}
				}
				break;
		default:
			break;
		}
	}
	
	private boolean onSquareFocusViewTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if(mCameraLayer!=null)
				{
					focusView.setDownY(event.getY());
					boolean con = mCameraLayer.supportFocus() && mCameraLayer.isPreviewing();
					if (con) {// 对焦
						if (mAllowTouchFocus) {
							mAllowTouchFocus = false;
							Rect rect = doTouchFocus(event.getX(), event.getY());
							if (rect != null) {
								focusView.setHaveTouch(true, rect);
							}
							focusFinishTime(1000);  //1秒后关闭聚焦动画.
						}
					}
				}	
				break;
			case MotionEvent.ACTION_UP:
				float upY = event.getY();
				float dis = upY - focusView.getDownY();
//				if (Math.abs(dis) >= 100) {
//					if (mCameraLayer.cameraChangeEnable()) {
//						handler.sendEmptyMessage(MSG_CHANGE_CAMERA);
//					}
//				}
				break;
		}
		return true;
	}
	private void doAutoFocus() {
		boolean con = mCameraLayer.supportFocus() && mCameraLayer.isPreviewing();
		if (con) {
			if (mAllowTouchFocus && focusView != null && focusView.getWidth() > 0) {
				mAllowTouchFocus = false;
				int w = focusView.getWidth();
				Rect rect = doTouchFocus(w / 2, w / 2);
				if (rect != null) {
					focusView.setHaveTouch(true, rect);
					focusFinishTime(1000);  //1秒后关闭聚焦动画
				}
			}
		}
	}
	private Rect doTouchFocus(float x, float y) {
		int w = mDrawPadView.getWidth();
		int h = mDrawPadView.getHeight();
		int left = 0;
		int top = 0;
		if (x - VideoFocusView.FOCUS_IMG_WH / 2 <= 0) {
			left = 0;
		} else if (x + VideoFocusView.FOCUS_IMG_WH / 2 >= w) {
			left = w - VideoFocusView.FOCUS_IMG_WH;
		} else {
			left = (int) (x - VideoFocusView.FOCUS_IMG_WH / 2);
		}
		if (y - VideoFocusView.FOCUS_IMG_WH / 2 <= 0) {
			top = 0;
		} else if (y + VideoFocusView.FOCUS_IMG_WH / 2 >= w) {
			top = w - VideoFocusView.FOCUS_IMG_WH;
		} else {
			top = (int) (y - VideoFocusView.FOCUS_IMG_WH / 2);
		}
		Rect rect = new Rect(left, top, left + VideoFocusView.FOCUS_IMG_WH, top + VideoFocusView.FOCUS_IMG_WH);
		
		Rect targetFocusRect = new Rect(rect.left * 2000 / w - 1000, rect.top * 2000 / h - 1000, rect.right * 2000 / w - 1000, rect.bottom * 2000 / h - 1000);
		try {
			List<Camera.Area> focusList = new ArrayList<Camera.Area>();
			Area focusA = new Area(targetFocusRect, 1000);
			focusList.add(focusA);
			mCameraLayer.doFocus(focusList);
			return rect;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private void focusFinishTime(int delayMS)
	{
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (focusView != null) {
					focusView.setHaveTouch(false, new Rect(0, 0, 0, 0));
					mAllowTouchFocus = true;
				}
			}
		}, delayMS);
		
	}
}
