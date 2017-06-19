package com.example.advanceDemo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBeautyFilter;

import com.example.advanceDemo.GPUImageFilterTools.OnGpuImageFilterChosenListener;
import com.example.advanceDemo.view.VideoFocusView;
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
import com.lansosdk.videoeditor.LanSongUtil;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CameraLayerFullSegmentActivity extends Activity implements OnClickListener{
   
	private static final long RECORD_CAMERA_TIME=20*1000*1000; //定义录制的时间为20s
	
	private static final String TAG = "CameraLayerFullScreenActivity";

    private DrawPadCameraView mDrawPadCamera;
    
    private CameraLayer  mCameraLayer=null;
    
    private String dstPath=null;
    private String editTmpPath=null;  //用来存储录制的视频部分,
    
	VideoFocusView focusView;
	private PowerManager.WakeLock mWakeLock;
	private ViewLayer mViewLayer=null;
    private ViewLayerRelativeLayout mLayerRelativeLayout;
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

        dstPath=SDKFileUtils.newMp4PathInBox();
        editTmpPath=SDKFileUtils.newMp4PathInBox();
        
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				initDrawPad();  //开始录制.
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
     * Step1: 开始运行 DrawPad 画板
     */
    private void initDrawPad()
    {
    	//设置使能 实时录制, 即把正在DrawPad中呈现的画面实时的保存下来,实现所见即所得的模式
    	 DisplayMetrics dm = new DisplayMetrics();
    	 dm = getResources().getDisplayMetrics();
    	 
    	 //因手机屏幕是16:9;全屏模式,建议分辨率设置为960x544;
    	 int padWidth=544;  
    	 int padHeight=960;
    	 
    	mDrawPadCamera.setRealEncodeEnable(padWidth,padHeight,3000000,(int)25,editTmpPath);
    	//设置处理进度监听.
    	mDrawPadCamera.setOnDrawPadProgressListener(drawPadProgressListener);

    	//全屏不需要缩放, 直接开始播放.
    	startDrawPad();
    }
    /**
     * Step2: 开始运行 Drawpad线程.
     */
      private void startDrawPad()
      {
    	  	mDrawPadCamera.setRecordMic(true);
    		/**
        	 * 这里设置先不开始录制.
        	 */
        	mDrawPadCamera.pauseDrawPadRecord();
    	    if(mDrawPadCamera.startDrawPad())
    	    {
    	    	mCameraLayer=	mDrawPadCamera.getCameraLayer();  //使用前置相机,暂时不使用滤镜.
        		if(mCameraLayer!=null){
        			mCameraLayer.startPreview();
        		}
//        		addViewLayer();
//        		addBitmapLayer();	
    	    }
      }
  
      
      /**
       * Step3: 停止画板, 停止后,为新的视频文件增加上音频部分.
       */
      private void stopDrawPad()
      {
    	  	if(mDrawPadCamera!=null && mDrawPadCamera.isRunning())
	      	{
	    		  	if(mDrawPadCamera.isRecording())
					{
						String segmentPath=mDrawPadCamera.segmentStop();
						segmentArray.add(segmentPath); 
					}
	    		  
	  				mDrawPadCamera.stopDrawPad();
	  				mCameraLayer=null;
	  				
	  				 //停止后, 得到多段视频, 这里拼接,
	  				if(segmentArray.size()>0)
	  				{
	  					VideoEditor editor=new VideoEditor();
	  					String[] segments=new String[segmentArray.size()];  
	  				     for(int i=0;i<segmentArray.size();i++){  
	  				    	 segments[i]=(String)segmentArray.get(i);  
	  				     }  
	  					editor.executeConcatMP4(segments, dstPath);
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
	 private void addMVLayer()
	  	{
	  		String  colorMVPath=CopyDefaultVideoAsyncTask.copyFile(CameraLayerFullSegmentActivity.this,"mei.mp4");
	  	    String maskMVPath=CopyDefaultVideoAsyncTask.copyFile(CameraLayerFullSegmentActivity.this,"mei_b.mp4");
	  		
	  	    MVLayer  layer=mDrawPadCamera.addMVLayer(colorMVPath, maskMVPath);  //<-----增加MVLayer
	  		/**
	  		 * mv在播放完后, 有3种模式,消失/停留在最后一帧/循环.默认是循环.
	  		 * layer.setEndMode(MVLayerENDMode.INVISIBLE); 
	  		 */
	  	}
    /**
     * 选择滤镜效果, 
     */
    private void selectFilter()
    {
    	if(mDrawPadCamera!=null && mDrawPadCamera.isRunning()){
    		GPUImageFilterTools.showDialog(this, new OnGpuImageFilterChosenListener() {

                @Override
                public void onGpuImageFilterChosenListener(final GPUImageFilter filter) {
                	/**
                	 * 通过DrawPad线程去切换 filterLayer的滤镜
                	 * 有些Filter是可以调节的,这里为了代码简洁,暂时没有演示, 可以在CameraeLayerDemoActivity中查看.
                	 */
                	if(mDrawPadCamera!=null){
                		mDrawPadCamera.switchFilterTo(mCameraLayer,filter);
                	}
                }
            });
    	}
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
		    if(SDKFileUtils.fileExist(editTmpPath)){
		    	SDKFileUtils.deleteFile(editTmpPath);
		    	editTmpPath=null;
		    }
	}
   /**
    * 增加一个UI图层: ViewLayer 
    */
   private TextView tvWord; 
   private TextView tvWord2; 
   private TextView tvWord3; 
   private void addViewLayer()
   {
        mLayerRelativeLayout=(ViewLayerRelativeLayout)findViewById(R.id.id_vview_realtime_gllayout);
	   	if(mDrawPadCamera!=null && mDrawPadCamera.isRunning())
	   	{
	   			mViewLayer=mDrawPadCamera.addViewLayer();
	           
	   		//把这个图层绑定到LayerRelativeLayout中.从而LayerRelativeLayout中的各种UI界面会被绘制到Drawpad上.
	   			mLayerRelativeLayout.bindViewLayer(mViewLayer);
	   		
	           mLayerRelativeLayout.invalidate();//刷新一下.
	           
	           ViewGroup.LayoutParams  params=mLayerRelativeLayout.getLayoutParams();
	           params.height=mViewLayer.getPadHeight();  //因为布局时, 宽度一致, 这里调整高度,让他们一致.
	           mLayerRelativeLayout.setLayoutParams(params);
	   	}
	    tvWord=(TextView)findViewById(R.id.id_vview_tvtest);
	    tvWord2=(TextView)findViewById(R.id.id_vview_tvtest2);
	    tvWord3=(TextView)findViewById(R.id.id_vview_tvtest3);
   }
   /**
    * 在增加一个UI图层.
    */
   private BitmapLayer  bmpLayer;
   private void addBitmapLayer()
   {
	   	if(mDrawPadCamera!=null && mDrawPadCamera.isRunning())
		{
			String bitmapPath=CopyFileFromAssets.copyAssets(getApplicationContext(), "small.png");
			bmpLayer=mDrawPadCamera.addBitmapLayer(BitmapFactory.decodeFile(bitmapPath));
			
			//把位置放到中间的右侧, 因为获取的高级是中心点的高度.
			bmpLayer.setPosition(bmpLayer.getPadWidth()-bmpLayer.getLayerWidth()/2,bmpLayer.getPositionY());
		}
   }
   private void showWord()
   {
   	 		if(tvWord!=null&& tvWord.getVisibility()!=View.VISIBLE){
				 tvWord.startAnimation(AnimationUtils.loadAnimation(CameraLayerFullSegmentActivity.this, R.anim.slide_right_in));
				 tvWord.setVisibility(View.VISIBLE); 
				 new Handler().postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						 tvWord2.startAnimation(AnimationUtils.loadAnimation(CameraLayerFullSegmentActivity.this, R.anim.slide_right_in));
						 tvWord2.setVisibility(View.VISIBLE); 
					}
				}, 500);
				 
				 //1秒后再显示这个.
				 new Handler().postDelayed(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							 tvWord3.startAnimation(AnimationUtils.loadAnimation(CameraLayerFullSegmentActivity.this, R.anim.slide_right_in));
							 tvWord3.setVisibility(View.VISIBLE); 
						}
					}, 1000);
			 }
   }
   private void hideWord()
   {
   	 	if(tvWord!=null&& tvWord.getVisibility()==View.VISIBLE){
				 tvWord.startAnimation(AnimationUtils.loadAnimation(CameraLayerFullSegmentActivity.this, R.anim.push_up_out));
				 tvWord.setVisibility(View.INVISIBLE); 
				 new Handler().postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						 tvWord2.startAnimation(AnimationUtils.loadAnimation(CameraLayerFullSegmentActivity.this, R.anim.push_up_out));
						 tvWord2.setVisibility(View.INVISIBLE); 
					}
				}, 500);
				 
				 new Handler().postDelayed(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							 tvWord3.startAnimation(AnimationUtils.loadAnimation(CameraLayerFullSegmentActivity.this, R.anim.push_up_out));
							 tvWord3.setVisibility(View.INVISIBLE); 
						}
					}, 1000);
		 }
   }
   //-------------------------------------------一下是UI界面和控制部分.---------------------------------------------------
   private LinearLayout  playVideo;
   private TextView tvTime;
   
   private void initView()
   {
	   tvTime=(TextView)findViewById(R.id.id_fullscreen_timetv);
	   
	   playVideo=(LinearLayout)findViewById(R.id.id_fullscreen_saveplay);
	   playVideo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(SDKFileUtils.fileExist(dstPath)){
		   			 	Intent intent=new Intent(CameraLayerFullSegmentActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
		   		 }else{
		   			 Toast.makeText(CameraLayerFullSegmentActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
		   		 }
			}
		});
	   playVideo.setVisibility(View.GONE);
	   
   		findViewById(R.id.id_fullscreen_flashlight).setOnClickListener(this);
		findViewById(R.id.id_fullscreen_frontcamera).setOnClickListener(this);
		findViewById(R.id.id_fullscreen_filter).setOnClickListener(this);

		findViewById(R.id.id_camerape_demo_recordbtn).setOnClickListener(this);
		findViewById(R.id.id_camerape_demo_completebtn).setOnClickListener(this);
   }
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.id_fullscreen_frontcamera:
				if(mCameraLayer!=null){
					if(mDrawPadCamera.isRunning())  
					{
						//先把DrawPad暂停运行.
						mDrawPadCamera.pauseDrawPad();
						mCameraLayer.changeCamera();	
						mDrawPadCamera.resumeDrawPad(); //再次开启.
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
			case R.id.id_camerape_demo_completebtn:
				stopDrawPad();
				break;
			case R.id.id_camerape_demo_recordbtn:
				if(mDrawPadCamera!=null)
				{
					if(mDrawPadCamera.isRecording())
					{
						String segmentPath=mDrawPadCamera.segmentStop();
						/**
						 * 把一段录制好的,增加到数组里.
						 */
						segmentArray.add(segmentPath); 
						((Button )findViewById(R.id.id_camerape_demo_recordbtn)).setText("开始");
					}else{
						mDrawPadCamera.segmentStart();
						((Button )findViewById(R.id.id_camerape_demo_recordbtn)).setText("暂停");
					}
				}
		default:
			break;
		}
	}
	  private void toastStop()
	    {
	    	Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
	    	Log.i(TAG,"录制已停止!!");
	    }
}

