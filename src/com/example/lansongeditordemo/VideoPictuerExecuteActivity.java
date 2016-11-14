package com.example.lansongeditordemo;

import org.insta.IFRiseFilter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lansong.animview.ShowHeart;
import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapPen;
import com.lansosdk.box.CanvasRunnable;
import com.lansosdk.box.CanvasPen;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadVideoExecute;
import com.lansosdk.box.VideoPen;
import com.lansosdk.box.ViewPen;
import com.lansosdk.box.FilterExecute;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.videoeditor.LanSoEditor;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.onVideoEditorProgressListener;

/**
 * 演示: 使用DrawPad在后台执行视频和视频的叠加处理.
 * 
 * 适用在 一些UI界面需要用户手动操作UI界面,比如旋转叠加的视频等,增加图片后旋转图片等,这些UI交互完成后, 
 * 记录下用户的操作信息,但需要统一处理时,通过此类来在后台执行.
 * 
 * 流程:通过DrawPadVideoExecute来实现视频的编辑处理,
 * 效果:建立一个DrawPad后,获取VideoPen让其播放,在播放过程中,向里面增加两个图片和一个UI,
 * 其中给一个图片移动位置,并在3秒处放大一倍,在6秒处消失,处理中实时的形成视频等
 * 
 *
 */
public class VideoPictuerExecuteActivity extends Activity{

	String videoPath=null;
	ProgressDialog  mProgressDialog;
	int videoDuration;
	boolean isRuned=false;
	MediaInfo   mMediaInfo;
	TextView tvProgressHint;
	 TextView tvHint;
	    private String editTmpPath=null;  //视频处理的临时文件存放
	    private String dstPath=null;
	    
	    
	private static final String TAG="VideoPictuerExecuteActivity";
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
		 
		 tvHint.setText(R.string.drawpadexecute_demo_hint);
   
		 tvProgressHint=(TextView)findViewById(R.id.id_video_edit_progress_hint);
		 
	       findViewById(R.id.id_video_edit_btn).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					if(mMediaInfo.vDuration>=60*1000){//大于60秒
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
					Intent intent=new Intent(VideoPictuerExecuteActivity.this,VideoPlayerActivity.class);
	    	    	intent.putExtra("videopath", dstPath);
	    	    	startActivity(intent);
				}else{
					 Toast.makeText(VideoPictuerExecuteActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
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
	   
   
	
   private BitmapPen bitmapPen=null;
	
   
   private CanvasPen mCanvasPen=null;
   private ShowHeart mShowHeart;
	
   private DrawPadVideoExecute  vDrawPad=null;
	
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
		//创建在后台调用DrawPad来处理视频的构造方法.
		 vDrawPad=new DrawPadVideoExecute(VideoPictuerExecuteActivity.this,videoPath,480,480,25,1000000,editTmpPath);
		
		 
		 vDrawPad.setUseMainVideoPts(true); //使用主视频的pts作为目标视频的pts
		 
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
		//设置DrawPad完成后的监听.
		vDrawPad.setDrawPadCompletedListener(new onDrawPadCompletedListener() {
			
			@Override
			public void onCompleted(DrawPad v) {
				// TODO Auto-generated method stub
				tvProgressHint.setText("DrawPadExecute Completed!!!");
				
				isExecuting=false;
				
				if(SDKFileUtils.fileExist(editTmpPath)){
					boolean ret=VideoEditor.encoderAddAudio(videoPath, editTmpPath,SDKDir.TMP_DIR,dstPath);
					if(ret==false){
						dstPath=editTmpPath; //没有声音的时候,临时文件为目标文件.
					}
				}
				findViewById(R.id.id_video_edit_btn2).setEnabled(true);
			}
		});
		//开始执行这个DrawPad
		vDrawPad.startDrawPad();
		
		//一下是在处理过程中, 增加的几个Pen, 来实现视频在播放过程中叠加别的一些媒体, 像图片, 文字等.
		
		
		//向其中增加一个图片
		bitmapPen=vDrawPad.addBitmapPen(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
		bitmapPen.setPosition(300, 200);
		
		
		//增加一个笑脸, add a bitmap
		vDrawPad.addBitmapPen(BitmapFactory.decodeResource(getResources(), R.drawable.xiaolian));	

		//增加一个CanvasPen
		addCanvasPen();
		
	}
	private void addCanvasPen()
	{
			
			
				mCanvasPen=vDrawPad.addCanvasPen();
				
				if(mCanvasPen!=null){
					
					mCanvasPen.setClearCanvas(false);
					mShowHeart=new ShowHeart(VideoPictuerExecuteActivity.this,mCanvasPen.getWidth(),mCanvasPen.getHeight());
					mCanvasPen.addCanvasRunnable(new CanvasRunnable() {
						
						@Override
						public void onDrawCanvas(CanvasPen Pen, Canvas canvas,
								long currentTimeUs) {
							// TODO Auto-generated method stub
							mShowHeart.drawTrack(canvas);
						}
					});
				}
	}
	
}	