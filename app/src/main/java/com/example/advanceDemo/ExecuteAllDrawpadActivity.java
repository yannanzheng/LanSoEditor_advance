package com.example.advanceDemo;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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

import com.example.advanceDemo.view.ShowHeart;
import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.BoxDecoder;
import com.lansosdk.box.CanvasRunnable;
import com.lansosdk.box.CanvasLayer;
import com.lansosdk.box.DataLayer;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadAllExecute;
import com.lansosdk.box.DrawPadVideoRunnable;
import com.lansosdk.box.FileParameter;
import com.lansosdk.box.GifLayer;
import com.lansosdk.box.Layer;
import com.lansosdk.box.MVLayer;
import com.lansosdk.box.MVLayerENDMode;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadErrorListener;
import com.lansosdk.box.onDrawPadOutFrameListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadThreadProgressListener;
import com.lansosdk.videoeditor.CopyDefaultVideoAsyncTask;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.DrawPadVideoExecute;
import com.lansosdk.videoeditor.LanSoEditor;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.onVideoEditorProgressListener;

/**
 * 后台执行.
 *  
 *
 */
public class ExecuteAllDrawpadActivity extends Activity{
	
		private static final String TAG="ExecuteAllDrawpadActivity";
		private String videoPath=null;
		private TextView tvProgressHint;
		private TextView tvHint;
	    private String editTmpPath=null;  //视频处理的临时文件存放
	    
	    
	    private String videoPath2=null;
	    
	    private DrawPadAllExecute  mDrawPad=null;
	    private VideoLayer firstVideoLayer=null;
	    private VideoLayer secondVideoLayer=null;
	    private BitmapLayer bmpLayer;
	    private boolean isExecuting=false; 
	    private Context mContext=null;
	    SlideEffect  slide;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
		 
		mContext=getApplicationContext();
		 videoPath=getIntent().getStringExtra("videopath");
		 
		 
		 
		 setContentView(R.layout.execute_edit_demo_layout);
		 
		 initView();
		
       //在手机的默认路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
       editTmpPath=SDKFileUtils.newMp4PathInBox();
       new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				videoPath2=CopyFileFromAssets.copyAssets(getApplicationContext(), "ping25s.mp4");
			}
		}).start();
	}
	/**
	 * 从这里开始演示.
	 */
	private void startDrawPad()
	{
		if(isExecuting)
			return ;
		
		 isExecuting=true;
		 /**
		  * 
		  * @param ctx
		  * @param padwidth 容器宽度/生成视频的宽度	
		  * @param padheight 容器高度/生成视频的高度
		  * @param framerate  生成视频的帧率
		  * @param bitrate   生成视频的码率
		  * @param dstPath   生成视频保存的完整路径 .mp4结尾.
		  */
		 mDrawPad=new DrawPadAllExecute(mContext,480,480,25,1000*1000,editTmpPath);
		 /**
		  * 设置错误监听
		  */
		 mDrawPad.setDrawPadErrorListener(new onDrawPadErrorListener() {
			
			@Override
			public void onError(DrawPad d, int what) {
				// TODO Auto-generated method stub
				mDrawPad.stopDrawPad();
				Log.e(TAG,"后台画板线程 运行失败,您请检查下是否码率分辨率设置过大,或者联系我们!...");
			}
		});
		 /**
		  * 设置DrawPad处理的进度监听, 回传的currentTimeUs单位是微秒.
		  */
		mDrawPad.setDrawPadProgressListener(new onDrawPadProgressListener() {
			
			@Override
			public void onProgress(DrawPad v, long currentTimeUs) {
				tvProgressHint.setText(String.valueOf(currentTimeUs));
				
				if(currentTimeUs>30*1000*1000){  //30秒的时候停止.
					mDrawPad.stopDrawPad();
				}
				if(currentTimeUs>15*1000*1000 && bmpLayer==null){  //15秒的时候, 增加图片.
					
					String bmpPath=CopyFileFromAssets.copyAssets(getApplicationContext(), "girl.jpg");
					Bitmap bmp=BitmapFactory.decodeFile(bmpPath);
					bmpLayer=mDrawPad.addBitmapLayer(bmp, null);
					if(firstVideoLayer!=null){
						firstVideoLayer.setVisibility(Layer.INVISIBLE);
					}
				}
				if(currentTimeUs>20*1000*1000 && secondVideoLayer==null){  //20秒的时候增加一个视频.
					if(videoPath2==null){
						videoPath2=CopyFileFromAssets.copyAssets(getApplicationContext(), "ping25s.mp4");
					}
					secondVideoLayer=mDrawPad.addVideoLayer(videoPath2, null);
					slide=new SlideEffect(secondVideoLayer, 25, 20*1000, 20*1000 +6000, true);
				}
				if(slide!=null){  //第二个视频的滑动
					slide.run(currentTimeUs/1000);	
				}
			}
		});
		/**
		 * 设置DrawPad完成后的监听.
		 */
		mDrawPad.setDrawPadCompletedListener(new onDrawPadCompletedListener() {
			
			@Override
			public void onCompleted(DrawPad v) {
				tvProgressHint.setText("DrawPadExecute Completed!!!");
				isExecuting=false;
				findViewById(R.id.id_video_edit_btn2).setEnabled(true);
			}
		});
		/**
		 * 开始执行这个DrawPad
		 */
		if(mDrawPad.startDrawPad()){
			 // 增加第一个视频
			firstVideoLayer=mDrawPad.addVideoLayer(videoPath,null);
		}else{
			Log.e(TAG,"后台画板线程  运行失败,您请检查下是否是路径设置有无, 请用MediaInfo.checkFile执行查看下....");
		}
	}
   @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	
    	if(mDrawPad!=null){
    		mDrawPad.releaseDrawPad();
    		try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		mDrawPad=null;
    	}
           if(SDKFileUtils.fileExist(editTmpPath)){
        	   SDKFileUtils.deleteFile(editTmpPath);
           } 
    }
	 
	   private void initView()
	   {
			   tvHint=(TextView)findViewById(R.id.id_video_editor_hint);
				
			   tvHint.setText(R.string.videolayer_transform_hints);
			   tvProgressHint=(TextView)findViewById(R.id.id_video_edit_progress_hint);
			       
				 findViewById(R.id.id_video_edit_btn).setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							startDrawPad();
						}
				 });
		     findViewById(R.id.id_video_edit_btn2).setEnabled(false);
		     findViewById(R.id.id_video_edit_btn2).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(SDKFileUtils.fileExist(editTmpPath)){
							Intent intent=new Intent(mContext,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", editTmpPath);
			    	    	startActivity(intent);
						}else{
							 Toast.makeText(ExecuteAllDrawpadActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
						}
					}
				});
	   }
}	