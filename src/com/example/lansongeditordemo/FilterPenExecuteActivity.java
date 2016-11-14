package com.example.lansongeditordemo;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageToneCurveFilter;

import org.insta.IFRiseFilter;

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
import com.lansosdk.box.BitmapPen;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadVideoFilterExecute;
import com.lansosdk.box.DrawPadVideoExecute;
import com.lansosdk.box.VideoPen;
import com.lansosdk.box.ViewPen;
import com.lansosdk.box.FilterExecute;
import com.lansosdk.box.ScaleExecute;
import com.lansosdk.box.onFilterExecuteCompletedListener;
import com.lansosdk.box.onFilterExecuteProssListener;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onScaleCompletedListener;
import com.lansosdk.box.onScaleProgressListener;
import com.lansosdk.videoeditor.LanSoEditor;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.onVideoEditorProgressListener;

/**
 * 演示FilterPen在后台工作的场合, 
 * 
 * 比如你想设计的:在滤镜的过程中,让用户手动拖动增加一些图片,文字等, 增加完成后,记录下用户的操作信息,
 * 但需要统一处理时,通过此类来在后台执行.
 * 
 *  流程是:使用DrawPadVideoFilterExecute, 创建一个DrawPad,从中获取FilterPen,然后设置到视频播放器中,在画面播放过程中,可以随时增加另外的一些
 *  Pen,比如BitmapPen,ViewPen等等.
 *  
 *
 */
public class FilterPenExecuteActivity extends Activity{

	String videoPath=null;
	ProgressDialog  mProgressDialog;
	int videoDuration;
	boolean isRuned=false;
	MediaInfo   mMediaInfo;
	TextView tvProgressHint;
	 TextView tvHint;
	 
	    private String editTmpPath=null;
	    private String dstPath=null;
	    
	    
	private static final String TAG="FilterPenExecuteActivity";
   	private static final boolean VERBOSE = false; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new snoCrashHandler());
		 
		 videoPath=getIntent().getStringExtra("videopath");
		 mMediaInfo=new MediaInfo(videoPath);
		 mMediaInfo.prepare();
		 
		 setContentView(R.layout.video_edit_demo_layout);
		 tvHint=(TextView)findViewById(R.id.id_video_editor_hint);
		 
		 tvHint.setText(R.string.filterPen_execute_hint);
   
		 tvProgressHint=(TextView)findViewById(R.id.id_video_edit_progress_hint);
		 
       findViewById(R.id.id_video_edit_btn).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(mMediaInfo.vDuration>60*1000){//大于60秒
					showHintDialog();
				}else{
					testDrawPadExecute();
				}
			}
		});
       
       findViewById(R.id.id_video_edit_btn2).setEnabled(false);
       findViewById(R.id.id_video_edit_btn2).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(SDKFileUtils.fileExist(dstPath)){
					Intent intent=new Intent(FilterPenExecuteActivity.this,VideoPlayerActivity.class);
	    	    	intent.putExtra("videopath", dstPath);
	    	    	startActivity(intent);
				}else{
					 Toast.makeText(FilterPenExecuteActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
				}
			}
		});
       
       //在手机的/sdcard/lansongBox/路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
       editTmpPath=SDKFileUtils.newMp4PathInBox();
       dstPath=SDKFileUtils.newMp4PathInBox();
	}
   @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	
    	if(vDrawPad!=null){
    		vDrawPad.release();
    		vDrawPad=null;
    	}
    	   if(SDKFileUtils.fileExist(dstPath)){
    		   SDKFileUtils.deleteFile(dstPath);
           }
           if(SDKFileUtils.fileExist(editTmpPath)){
        	   SDKFileUtils.deleteFile(editTmpPath);
           } 
    }
	   
	VideoEditor mVideoEditer;
	BitmapPen bitmapPen=null;
	DrawPadVideoFilterExecute  vDrawPad=null;
	private boolean isExecuting=false;
	private void showHintDialog()
	{
		new AlertDialog.Builder(this)
		.setTitle("提示")
		.setMessage("视频过大,可能会需要一段时间,您确定要处理吗?")
        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				testDrawPadExecute();
			}
		})
		.setNegativeButton("取消", null)
        .show();
	}
	
	private void testDrawPadExecute()
	{
		if(isExecuting)
			return ;
		
		isExecuting=true;
		 /**
		  * 
		  * @param ctx   语境,android的Context
		  * @param srcPath   主视频的路径
		  * @param mpoolW   DrawPad的宽度.
		  * @param mpoolH  DrawPad的高度.
		  * @param bitrate  编码视频所希望的码率,比特率.
		  * @param dstPath  编码视频保存的路径.
		  * @param filter   需要的滤镜效果对象
		  */
		 vDrawPad=new DrawPadVideoFilterExecute(FilterPenExecuteActivity.this,videoPath,480,480,1000000,editTmpPath,new GPUImageSepiaFilter());
		
		 //设置DrawPad处理的进度监听, 回传的currentTimeUs单位是微秒.
		vDrawPad.setDrawPadProgressListener(new onDrawPadProgressListener() {
			
			@Override
			public void onProgress(DrawPad v, long currentTimeUs) {
				// TODO Auto-generated method stub
				tvProgressHint.setText(String.valueOf(currentTimeUs));
			
				//6秒后消失
				if(currentTimeUs>6000000 && bitmapPen!=null)  
					v.removePen(bitmapPen);
				
				//3秒的时候,放大一倍.
				if(currentTimeUs>3000000 && bitmapPen!=null)  
					bitmapPen.setScale(2.0f);
			}
		});
		//设置DrawPad处理完成后的监听.
		vDrawPad.setDrawPadCompletedListener(new onDrawPadCompletedListener() {
			
			@Override
			public void onCompleted(DrawPad v) {
				// TODO Auto-generated method stub
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
		});
		vDrawPad.startDrawPad();
		
		bitmapPen=vDrawPad.addBitmapPen(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
		
		bitmapPen.setPosition(300, 200);
		vDrawPad.addBitmapPen(BitmapFactory.decodeResource(getResources(), R.drawable.xiaolian));	
		
	}
}	