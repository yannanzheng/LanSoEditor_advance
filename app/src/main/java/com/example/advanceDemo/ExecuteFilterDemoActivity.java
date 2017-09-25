package com.example.advanceDemo;

import java.lang.reflect.Array;
import java.util.ArrayList;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSwirlFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageToneCurveFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IF1977Filter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFNashvilleFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFRiseFilter;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadVideoRunnable;
import com.lansosdk.box.Layer;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onScaleCompletedListener;
import com.lansosdk.box.onScaleProgressListener;
import com.lansosdk.videoeditor.DrawPadVideoExecute;
import com.lansosdk.videoeditor.LanSoEditor;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.onVideoEditorProgressListener;

/**
 * 演示Layer中的滤镜属性在后台工作的场合, 
 * 
 * 
 * 比如你想设计的:在滤镜的过程中,让用户手动拖动增加一些图片,文字等, 增加完成后,记录下用户的操作信息,
 * 但需要统一处理时,通过此类来在后台执行.
 * 
 *  流程是:使用DrawPadVideoExecute, 创建一个DrawPad,从中增加VideoLayer,向内部增加各种滤镜,然后设置到视频播放器中,在画面播放过程中,可以随时增加另外的一些
 *  Layer,比如BitmapLayer,CanvasLayer等等.
 *  
 *
 */
public class ExecuteFilterDemoActivity extends Activity{

	private static final String TAG="FilterDemoExecuteActivity";
	private String videoPath=null;
	MediaInfo   mInfo;
	TextView tvProgressHint;
	 TextView tvHint;
	 
	private String editTmpPath=null;
	private String dstPath=null;
	
	private DrawPadVideoExecute  mDrawPad=null;
	private boolean isExecuting=false;
		
	private Layer  mainVideoLayer=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
		 
		 videoPath=getIntent().getStringExtra("videopath");
		 
		 
		 mInfo=new MediaInfo(videoPath);
		 mInfo.prepare();
		 
		 setContentView(R.layout.execute_edit_demo_layout);
		 initView();
		 
       //在手机的默认路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
       editTmpPath=SDKFileUtils.newMp4PathInBox();
       dstPath=SDKFileUtils.newMp4PathInBox();
	}
   @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	
    	if(mDrawPad!=null){
    		mDrawPad.release();
    		mDrawPad=null;
    	}
    	   if(SDKFileUtils.fileExist(dstPath)){
    		   SDKFileUtils.deleteFile(dstPath);
           }
           if(SDKFileUtils.fileExist(editTmpPath)){
        	   SDKFileUtils.deleteFile(editTmpPath);
           } 
    }
	long  beforeDraw=0;
	private boolean isSwirlFilter=false;
	private boolean isNashvilleFilter=false;
	private void startDrawPadExecute()
	{
		if(isExecuting)
			return ;
		
		  beforeDraw=System.currentTimeMillis();
	    
		isExecuting=true;
		//设置pad的宽度和高度.
		int padWidth=mInfo.vWidth;
		int padHeight=mInfo.vHeight;
		if(mInfo.vRotateAngle==90 || mInfo.vRotateAngle==270){
			padWidth=mInfo.vHeight;
			padHeight=mInfo.vWidth;
		}
		
		 mDrawPad=new DrawPadVideoExecute(ExecuteFilterDemoActivity.this,videoPath,padWidth,padHeight,
				 (int)(mInfo.vBitRate*1.5f)
				 ,new IF1977Filter(getApplicationContext()),editTmpPath);
		 
		 mDrawPad.setUseMainVideoPts(true);
		
		 /**
		  * 设置DrawPad处理的进度监听, 回传的currentTimeUs单位是微秒.
		  */
		mDrawPad.setDrawPadProgressListener(new onDrawPadProgressListener() {
			
			@Override
			public void onProgress(DrawPad v, long currentTimeUs) {
				// TODO Auto-generated method stub
				drawPadProgress(v, currentTimeUs);
			}
		});
		/**
		 * 设置DrawPad处理完成后的监听.
		 */
		mDrawPad.setDrawPadCompletedListener(new onDrawPadCompletedListener() {
			
			@Override
			public void onCompleted(DrawPad v) {
				// TODO Auto-generated method stub
				drawPadCompleted();
			}
		});

		mDrawPad.pauseRecord();
		
		if(mDrawPad.startDrawPad())
		{
			mainVideoLayer=mDrawPad.getMainVideoLayer();
			mDrawPad.resumeRecord();  //开始恢复处理.
		}else{
			new AlertDialog.Builder(this)
			.setTitle("提示")
			.setMessage("DrawPad开启错误.或许视频分辨率过高导致..")
	        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}
			})
	        .show();
		}
	}
	
	/**
	 * DrawPad容器的进度监听, 走到什么位置后,设置对应的内容.
	 * @param v
	 * @param currentTimeUs
	 */
	private void drawPadProgress(DrawPad v,long currentTimeUs)
	{
		tvProgressHint.setText(String.valueOf(currentTimeUs));
		/**
		 * 演示在中间切换滤镜
		 * 在3秒的时候, 切换一个滤镜.
		 * 在6秒的时候, 再次切换一个滤镜
		 */
		if(currentTimeUs>6*1000*1000)
		{
			if(mainVideoLayer!=null && isNashvilleFilter==false){
				mainVideoLayer.switchFilterTo(new IFNashvilleFilter(getApplicationContext()));
				isNashvilleFilter=true;
			}
		}
		else if(currentTimeUs>3*1000*1000)
		{
			if(mainVideoLayer!=null && isSwirlFilter==false){
				mainVideoLayer.switchFilterTo(new GPUImageSwirlFilter());
				isSwirlFilter=true;
			}
		}
	}
	/**
	 * 完成后, 去播放
	 */
	private void drawPadCompleted()
	{
		tvProgressHint.setText("DrawPadExecute Completed!!!");
		isExecuting=false;
		if(SDKFileUtils.fileExist(editTmpPath)){
			//合并音频文件.
			boolean ret=VideoEditor.encoderAddAudio(videoPath, editTmpPath,SDKDir.TMP_DIR,dstPath);
			if(!ret){
				dstPath=editTmpPath;
			}
		}
		  findViewById(R.id.id_video_edit_btn2).setEnabled(true);
	}
	private void showHintDialog()
	{
		new AlertDialog.Builder(this)
		.setTitle("提示")
		.setMessage("视频过大,可能会需要一段时间,您确定要处理吗?")
        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				startDrawPadExecute();
			}
		})
		.setNegativeButton("取消", null)
        .show();
	}
	private void initView()
	{
		 tvHint=(TextView)findViewById(R.id.id_video_editor_hint);
		 tvHint.setText(R.string.filterLayer_execute_hint);
		 
		 
		 tvProgressHint=(TextView)findViewById(R.id.id_video_edit_progress_hint);
	      findViewById(R.id.id_video_edit_btn).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					if(mInfo.vDuration>60*1000){//大于60秒
						showHintDialog();
					}else{
						startDrawPadExecute();
					}
				}
			});
      
	      findViewById(R.id.id_video_edit_btn2).setEnabled(false);
	      findViewById(R.id.id_video_edit_btn2).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(SDKFileUtils.fileExist(dstPath)){
						Intent intent=new Intent(ExecuteFilterDemoActivity.this,VideoPlayerActivity.class);
		    	    	intent.putExtra("videopath", dstPath);
		    	    	startActivity(intent);
					}else{
						 Toast.makeText(ExecuteFilterDemoActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
					}
				}
			});
	}
}	