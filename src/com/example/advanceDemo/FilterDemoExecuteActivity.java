package com.example.advanceDemo;

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
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadVideoExecute;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
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
public class FilterDemoExecuteActivity extends Activity{

	String videoPath=null;
	ProgressDialog  mProgressDialog;
	int videoDuration;
	boolean isRuned=false;
	MediaInfo   mMediaInfo;
	TextView tvProgressHint;
	 TextView tvHint;
	 
	private String editTmpPath=null;
	private String dstPath=null;
	
	private BitmapLayer bitmapLayer=null;
	private DrawPadVideoExecute  vDrawPad=null;
	private boolean isExecuting=false;
		
	private static final String TAG="FilterDemoExecuteActivity";
	
   	private static final boolean VERBOSE = false; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
		 
		 videoPath=getIntent().getStringExtra("videopath");
		 mMediaInfo=new MediaInfo(videoPath);
		 mMediaInfo.prepare();
		 
		 setContentView(R.layout.video_edit_demo_layout);
		 initView();
		 
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
	/**
	 * 
	 */
	private void testDrawPadExecute()
	{
		if(isExecuting)
			return ;
		
		isExecuting=true;
		 /**
		  * 创建在后台调用DrawPad来处理视频的构造方法.
		  * 
		  * (类似photoshop的工作区)
		  * 
		  * @param ctx 语境,android的Context
		  * @param srcPath 主视频的路径
		  * @param padwidth DrawPad的的宽度
		  * @param padheight DrawPad的的高度
		  * @param bitrate   编码视频所希望的码率,比特率.
		  * @param filter   为视频增加一个滤镜
		  * @param dstPath  编码视频保存的路径.
		  */
		 vDrawPad=new DrawPadVideoExecute(FilterDemoExecuteActivity.this,videoPath,480,480,1000000,new GPUImageSepiaFilter(),editTmpPath);
		
		 //设置DrawPad处理的进度监听, 回传的currentTimeUs单位是微秒.
		vDrawPad.setDrawPadProgressListener(new onDrawPadProgressListener() {
			
			@Override
			public void onProgress(DrawPad v, long currentTimeUs) {
				// TODO Auto-generated method stub
				tvProgressHint.setText(String.valueOf(currentTimeUs));
			
				//6秒后消失
				if(currentTimeUs>6000000 && bitmapLayer!=null)  
					v.removeLayer(bitmapLayer);
				
				//3秒的时候,放大一倍.
				if(currentTimeUs>3000000 && bitmapLayer!=null)  
					bitmapLayer.setScale(2.0f);
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
		
		bitmapLayer=vDrawPad.addBitmapLayer(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
		
		bitmapLayer.setPosition(300, 200);
		vDrawPad.addBitmapLayer(BitmapFactory.decodeResource(getResources(), R.drawable.xiaolian));	
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
				testDrawPadExecute();
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
						Intent intent=new Intent(FilterDemoExecuteActivity.this,VideoPlayerActivity.class);
		    	    	intent.putExtra("videopath", dstPath);
		    	    	startActivity(intent);
					}else{
						 Toast.makeText(FilterDemoExecuteActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
					}
				}
			});
	}
}	