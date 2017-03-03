package com.example.advanceDemo;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.lansoeditor.demo.R;
import com.lansosdk.box.ExtractVideoFrame;
import com.lansosdk.box.onExtractVideoFrameCompletedListener;
import com.lansosdk.box.onExtractVideoFrameProgressListener;
import com.lansosdk.videoeditor.MediaInfo;

/**
先放入图片, 然后视频, 然后再图片.
 * 
 *
 */
public class ExtractVideoFrameDemoActivity extends Activity{

		String videoPath=null;
		ProgressDialog  mProgressDialog;
		int videoDuration;
		boolean isRuned=false;
		MediaInfo   mInfo;
		TextView tvProgressHint;
		 TextView tvHint;
	  
	    
	    private boolean isExecuting=false; 
	    private ExtractVideoFrame mExtractFrame;
	    private static final String TAG="ExtractVideoFrameDemoActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
		 
		 videoPath=getIntent().getStringExtra("videopath");
		 mInfo=new MediaInfo(videoPath);
		 mInfo.prepare();
		 Log.i(TAG,"minfo"+mInfo.toString());
		 
		 setContentView(R.layout.video_edit_demo_layout);
		 
		 initUI();
		
	}

	/**
	 * 从这里开始演示.
	 */
	private void testExtractVideoFrame()
	{
		if(isExecuting)
			return ;
		
		isExecuting=true;
		
		/**
		 * 初始化.
		 */
		mExtractFrame=new ExtractVideoFrame(ExtractVideoFrameDemoActivity.this,videoPath);
		 /**
	     * 设置在获取图片的时候, 可以指定图片的宽高, 指定后, 视频帧画面会被缩放到指定的宽高.不调用则使用默认大小.
	     * @param width  缩放宽度
	     * @param height 缩放高度
	     */
		mExtractFrame.setBitmapWH(480, 480);
		 /**
	     * 设置处理完成监听.
	     */
		mExtractFrame.setOnExtractCompletedListener(new onExtractVideoFrameCompletedListener() {
			
			@Override
			public void onCompleted(ExtractVideoFrame v) {
				// TODO Auto-generated method stub
				 Log.i("sno","onCompletedListener:");
				 tvProgressHint.setText("Completed");
			}
		});
		/**
		 * 设置处理进度监听.
		 */
		mExtractFrame.setOnExtractProgressListener(new onExtractVideoFrameProgressListener() {
			
			/**
			 * 当前帧的画会掉,  ptsUS:当前帧的时间戳,单位微秒.
			 * 拿到图片后,建议放到ArrayList中, 不要直接在这里处理.
			 */
			@Override
			public void onExtractBitmap(Bitmap bmp, long ptsUS) {
				// TODO Auto-generated method stub
				String hint="Frame pts:"+String.valueOf(ptsUS);
				tvProgressHint.setText(hint);
				 Log.i("sno",hint);
				 
				// savePng(bmp);  //用保持图片的方式来测试.
				 
				if(bmp!=null && bmp.isRecycled()){
					 bmp.recycle();
					 bmp=null;
				}
				
//				if(ptsUS>15*1000*1000){   你可以在指定的时间段停止.
//					mExtractFrame.stop();   //这里演示在15秒的时候停止.
//				}
			}
		});
		/**
		 * 开始执行.  或者你可以从指定地方开始解码.
		 * mExtractFrame.start(10*1000*1000);则从视频的10秒处开始提取.
		 */
		mExtractFrame.start();
	}
   @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    
    }
   private void initUI()
   {
		   tvHint=(TextView)findViewById(R.id.id_video_editor_hint);
			
		   tvHint.setText(R.string.extract_video_frame_hint);
		   
		   tvProgressHint=(TextView)findViewById(R.id.id_video_edit_progress_hint);
		       
			 findViewById(R.id.id_video_edit_btn).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
							testExtractVideoFrame();
					}
			 });
	     findViewById(R.id.id_video_edit_btn2).setVisibility(View.GONE);
   }
   int bmtcnt=0;
   /**
    * 把bmp保存到本地, 因为保存很慢,这里仅仅是演示,不建议保存到本地.
    * @param bmp
    */
	 private void savePng(Bitmap bmp)
	 {
		 File dir=new File("/sdcard/testExtract/");
		 if(dir.exists()==false){
			 dir.mkdir();
		 }
		  try {
				  BufferedOutputStream  bos;
				  String name="/sdcard/testExtract/"+ bmtcnt++ +".png";
				  bos = new BufferedOutputStream(new FileOutputStream(name));
				  bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
				  bos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 }
	
	
	
}	