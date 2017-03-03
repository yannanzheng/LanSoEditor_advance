package com.example.advanceDemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;

import com.example.advanceDemo.GPUImageFilterTools.OnGpuImageFilterChosenListener;
import com.example.advanceDemo.view.DrawPadView;
import com.example.advanceDemo.view.VideoFocusView;
import com.lansoeditor.demo.R;
import com.lansosdk.box.CameraLayer;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.LanSoEditorBox;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.ViewLayerRelativeLayout;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.videoeditor.SDKFileUtils;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CameraLayerFullScreenActivity extends Activity implements OnClickListener{
   
	private static final long RECORD_CAMERA_TIME=20*1000*1000; //定义录制的时间为20s
	
	private static final String TAG = "CameraLayerFullScreenActivity";

    private DrawPadView mDrawPadView;
    
    private CameraLayer  mCameraLayer=null;
    private String dstPath=null;
	VideoFocusView focusView;
	private PowerManager.WakeLock mWakeLock;
	private ViewLayer mViewLayer=null;
    private ViewLayerRelativeLayout mLayerRelativeLayout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cameralayer_fullscreen_demo_layout);
        
        if(LanSoEditorBox.checkCameraPermission(getBaseContext())==false){
     	   Toast.makeText(getApplicationContext(), "请打开权限后,重试!!!", Toast.LENGTH_LONG).show();
     	   finish();
        }
        
        mDrawPadView = (DrawPadView) findViewById(R.id.id_fullscreen_padview);
        
        initView();

        dstPath=SDKFileUtils.newMp4PathInBox();
        
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
    	 
    	 //全屏模式,建议分辨率设置为960x544;
    	 int padWidth=544;  
    	 int padHeight=960;
    	 
    	mDrawPadView.setRealEncodeEnable(padWidth,padHeight,3000000,(int)25,dstPath);
    	
    	mDrawPadView.setUpdateMode(DrawPadUpdateMode.AUTO_FLUSH, 25);

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
     * Step2: 开始运行 Drawpad线程.
     */
      private void startDrawPad()
      {
    	    mDrawPadView.startDrawPad();
    		mCameraLayer=	mDrawPadView.addCameraLayer(false,null);  //使用前置相机,暂时不使用滤镜.
    		addViewLayer();
      }
      /**
       * Step3: 停止画板
       */
      private void stopDrawPad()
      {
      	if(mDrawPadView!=null && mDrawPadView.isRunning())
      	{
  				mDrawPadView.stopDrawPad();
  				mCameraLayer=null;
  		}
      	playVideo.setVisibility(View.VISIBLE);
      }
    private onDrawPadProgressListener drawPadProgressListener=new onDrawPadProgressListener() {
		
		@Override
		public void onProgress(DrawPad v, long currentTimeUs) {
			// TODO Auto-generated method stub
			
			if(currentTimeUs>=RECORD_CAMERA_TIME){  
				stopDrawPad();
			}
			if(tvTime!=null){
				long left=RECORD_CAMERA_TIME-currentTimeUs;
				
				float leftF=((float)left/1000000);
				 float b   =  (float)(Math.round(leftF*10))/10;  //保留一位小数.
						 
				tvTime.setText(String.valueOf(b));
			}
			if(currentTimeUs>7000*1000)  //在第7秒的时候, 不再显示.
  			{
  				hideWord();
  			}else if(currentTimeUs>3*1000*1000)  //在第三秒的时候, 显示tvWord
  			{
  				showWord();
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
                	/**
                	 * 通过DrawPad线程去切换 filterLayer的滤镜
                	 * 有些Filter是可以调节的,这里为了代码简洁,暂时没有演示, 可以在CameraeLayerDemoActivity中查看.
                	 */
                	if(mDrawPadView!=null){
                		mDrawPadView.switchFilterTo(mCameraLayer,filter);
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
   /**
    * 增加一个UI图层: ViewLayer 
    */
   private TextView tvWord; 
   private void addViewLayer()
   {
        mLayerRelativeLayout=(ViewLayerRelativeLayout)findViewById(R.id.id_vview_realtime_gllayout);
	   	if(mDrawPadView!=null && mDrawPadView.isRunning())
	   	{
	   			mViewLayer=mDrawPadView.addViewLayer();
	           
	   		//把这个图层绑定到LayerRelativeLayout中.从而LayerRelativeLayout中的各种UI界面会被绘制到Drawpad上.
	   			mLayerRelativeLayout.bindViewLayer(mViewLayer);
	   		
	           mLayerRelativeLayout.invalidate();//刷新一下.
	           
	           ViewGroup.LayoutParams  params=mLayerRelativeLayout.getLayoutParams();
	           params.height=mViewLayer.getPadHeight();  //因为布局时, 宽度一致, 这里调整高度,让他们一致.
	           mLayerRelativeLayout.setLayoutParams(params);
	   	}
	    tvWord=(TextView)findViewById(R.id.id_vview_tvtest);
   }
   private void showWord()
   {
   	 		if(tvWord!=null&& tvWord.getVisibility()!=View.VISIBLE){
				 tvWord.startAnimation(AnimationUtils.loadAnimation(CameraLayerFullScreenActivity.this, R.anim.slide_right_in));
				 tvWord.setVisibility(View.VISIBLE); 
			 }
   }
   private void hideWord()
   {
   	 	if(tvWord!=null&& tvWord.getVisibility()==View.VISIBLE){
				 tvWord.startAnimation(AnimationUtils.loadAnimation(CameraLayerFullScreenActivity.this, R.anim.slide_right_out));
 			 tvWord.setVisibility(View.GONE); 
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
		   			 	Intent intent=new Intent(CameraLayerFullScreenActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
		   		 }else{
		   			 Toast.makeText(CameraLayerFullScreenActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
		   		 }
			}
		});
	   playVideo.setVisibility(View.GONE);
	   
   		findViewById(R.id.id_fullscreen_flashlight).setOnClickListener(this);
		findViewById(R.id.id_fullscreen_frontcamera).setOnClickListener(this);
		findViewById(R.id.id_fullscreen_filter).setOnClickListener(this);
		
   }
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.id_fullscreen_frontcamera:
				if(mCameraLayer!=null){
					
					if(mDrawPadView.isRunning())  
					{
						//先把DrawPad暂停运行.
						mDrawPadView.pauseDrawPad();
						mCameraLayer.changeCamera();	
						mDrawPadView.resumeDrawPad(); //再次开启.
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
}

