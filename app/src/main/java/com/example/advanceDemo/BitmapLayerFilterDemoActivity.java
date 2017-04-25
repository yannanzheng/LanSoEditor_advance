package com.example.advanceDemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;

import com.example.advanceDemo.GPUImageFilterTools.FilterAdjuster;
import com.example.advanceDemo.GPUImageFilterTools.OnGpuImageFilterChosenListener;
import com.example.advanceDemo.view.DrawPadView;
import com.example.advanceDemo.view.ShowHeart;
import com.example.advanceDemo.view.DrawPadView.onViewAvailable;
import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.CanvasLayer;
import com.lansosdk.box.CanvasRunnable;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.MVLayer;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.Layer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.videoeditor.CopyDefaultVideoAsyncTask;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class BitmapLayerFilterDemoActivity extends Activity{
    private static final String TAG = "BitmapLayerFilterDemoActivity";

    private DrawPadView mDrawPadView;
    
    private String dstPath=null;
    
    private Context mContext=null;
    private BitmapLayer  bmpLayer=null;
    private SeekBar AdjusterFilter;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bitmaplayer_filter_layout);
        initView();
        
        mDrawPadView = (DrawPadView) findViewById(R.id.DrawPad_view);

        //在手机的默认路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
        dstPath=SDKFileUtils.newMp4PathInBox();
        mContext=getApplicationContext();
	 	new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					initDrawPad();
				}
			}, 500);
	 	findViewById(R.id.id_bitmapfilter_demo_selectbtn).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				selectFilter();
			}
		});
	 		AdjusterFilter=(SeekBar)findViewById(R.id.id_bitmapfilter_demo_seek1);
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
    /**
     * Step1: 初始化DrawPad
     */
    private void initDrawPad()
    {
		//设置为自动刷新模式, 帧率为25
    	mDrawPadView.setUpdateMode(DrawPadUpdateMode.AUTO_FLUSH,30);
    	//使能实时录制,并设置录制后视频的宽度和高度, 码率, 帧率,保存路径.
    	mDrawPadView.setRealEncodeEnable(480,480,1000000,(int)30,dstPath);
    	
    	mDrawPadView.setOnDrawPadCompletedListener(new DrawPadCompleted());
		mDrawPadView.setOnDrawPadProgressListener(new DrawPadProgressListener());
    	//设置DrawPad的宽高, 这里设置为480x480,如果您已经在xml中固定大小,则不需要再次设置,
    	//可以直接调用startDrawPad来开始录制.
    	mDrawPadView.setDrawPadSize(480,480,new onDrawPadSizeChangedListener() {
			
			@Override
			public void onSizeChanged(int viewWidth, int viewHeight) {
				// TODO Auto-generated method stub
					startDrawPad();
			}
		});
    }
    /**
     * Step2: 开始运行 Drawpad线程. (停止是在进度监听中, 根据时间来停止的.)
     */
    private void startDrawPad()
    {
    		mDrawPadView.startDrawPad();
    		
    		mDrawPadView.pauseDrawPad();
			   
    		DisplayMetrics dm = new DisplayMetrics();// 获取屏幕密度（方法2）
		    dm = getResources().getDisplayMetrics();
		     
		      
		   int screenWidth  = dm.widthPixels;	
		   String picPath=null;   
		   if(screenWidth>=1080){
			   picPath=CopyFileFromAssets.copyAssets(mContext, "pic1080x1080u2.jpg");
		   }else{
			   picPath=CopyFileFromAssets.copyAssets(mContext, "pic720x720.jpg");
		   }
		   //先 增加第一张Bitmap的Layer, 因为是第一张,放在DrawPad中维护的数组的最下面, 认为是背景图片.
		   bmpLayer=  mDrawPadView.addBitmapLayer(BitmapFactory.decodeFile(picPath));
		   
		   mDrawPadView.resumeDrawPad();
    }
    private FilterAdjuster mFilterAdjuster;
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
    	         	   if(mDrawPadView.switchFilterTo(bmpLayer,filter)){
    	         		   mFilterAdjuster = new FilterAdjuster(filter);
    	         		   //如果这个滤镜 可调, 显示可调节进度条.
    	         		   findViewById(R.id.id_bitmapfilter_demo_seek1).setVisibility(
    	         		            mFilterAdjuster.canAdjust() ? View.VISIBLE : View.GONE);
    	         	   }
                }
            });
    	}
    }
   //DrawPad完成时的回调.
    private class DrawPadCompleted implements onDrawPadCompletedListener
    {

		@Override
		public void onCompleted(DrawPad v) {
			// TODO Auto-generated method stub
			
			if(isDestorying==false){
				if(SDKFileUtils.fileExist(dstPath)){
			    	findViewById(R.id.id_DrawPad_saveplay).setVisibility(View.VISIBLE);
				}
				toastStop();
			}
		}
    }
    //DrawPad进度回调.
    private class DrawPadProgressListener implements onDrawPadProgressListener
    {
		@Override
		public void onProgress(DrawPad v, long currentTimeUs) {  //单位是微妙
			// TODO Auto-generated method stub
			
		//	Log.i(TAG,"当前时间戳是:"+currentTimeUs);
			  
			  if(currentTimeUs>=100*1000*1000)  //26秒.多出一秒,让图片走完.
			  {
				  mDrawPadView.stopDrawPad();
			  }
		}
    }
    private void initView()
    {

        findViewById(R.id.id_DrawPad_saveplay).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(SDKFileUtils.fileExist(dstPath)){
		   			 	Intent intent=new Intent(BitmapLayerFilterDemoActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
		   		 }else{
		   			 Toast.makeText(BitmapLayerFilterDemoActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
		   		 }
			}
		});
        findViewById(R.id.id_DrawPad_saveplay).setVisibility(View.GONE);
    }
    private void toastStop()
    {
    	Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
    }
    boolean isDestorying=false;  //是否正在销毁, 因为销毁会停止DrawPad
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	isDestorying=true;
    	if(mDrawPadView!=null){
    		mDrawPadView.stopDrawPad();
    		mDrawPadView=null;        		   
    	}
    	
    	if(SDKFileUtils.fileExist(dstPath)){
    		SDKFileUtils.deleteFile(dstPath);
        }
    }
}
