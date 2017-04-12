package com.example.advanceDemo;

import java.io.IOException;
import java.util.Locale;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;

import com.example.advanceDemo.view.MarkArrowView;
import com.lansoeditor.demo.R;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.MVLayer;
import com.lansosdk.box.MVLayerENDMode;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.Layer;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 *  演示: 使用DrawPad完成 视频的实时标记.
 *  
 *  在视频处理的过程中, 提供 重写DrawPad中的onTouchEvent方法的类MarkArrowView,
 *  当点击这个View时,增加到点击位置, 并增加一个BitmapLayer, 并再次位置显示叠加图片.move时,移动图片.
 *  在手指抬起后, 释放BitmapLayer,从而实时滑动画面实时标记的效果.
 *
 */
public class BitmapLayerMarkActivity extends Activity{
    private static final String TAG = "BitmapLayerDemoActivity";

    private String mVideoPath;

    private MarkArrowView mMarkView;
    
    private MediaPlayer mplayer=null;
    
    private VideoLayer  mLayerMain=null;
    
    private String editTmpPath=null;
    private String dstPath=null;
    private LinearLayout  playVideo=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
		 setContentView(R.layout.drawpad_touch_layout);
        
        
        mVideoPath = getIntent().getStringExtra("videopath");
        mMarkView = (MarkArrowView) findViewById(R.id.markarrow_view);
        
        playVideo=(LinearLayout)findViewById(R.id.id_markarrow_saveplay);
        playVideo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(SDKFileUtils.fileExist(dstPath)){
		   			 	Intent intent=new Intent(BitmapLayerMarkActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
		   		 }else{
		   			 Toast.makeText(BitmapLayerMarkActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
		   		 }
			}
		});
        playVideo.setVisibility(View.GONE);
        
        /**
         * 在手机的默认路径下创建一个文件名,
         * 用来保存生成的视频文件,(在onDestroy中删除)
         */
        editTmpPath=SDKFileUtils.createFile(SDKDir.TMP_DIR, ".mp4");
        dstPath=SDKFileUtils.newFilePath(SDKDir.TMP_DIR, ".mp4");
        
        
        new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				 startPlayVideo();
			}
		}, 500);
    }
    private void startPlayVideo()
    {
          if (mVideoPath != null){
        	  mplayer=new MediaPlayer();
        	  try {
				mplayer.setDataSource(mVideoPath);
				
			}  catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	  mplayer.setOnPreparedListener(new OnPreparedListener() {
				
				@Override
				public void onPrepared(MediaPlayer mp) {
					// TODO Auto-generated method stub
					initDrawPad();
				}
			});
        	  mplayer.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					// TODO Auto-generated method stub
					stopDrawPad();
				}
			});
        	  mplayer.prepareAsync();
          }
          else {
              Log.e(TAG, "Null Data Source\n");
              finish();
              return;
          }
    }
    /**
     * Step1: 初始化 DrawPad 画板
     */
    private void initDrawPad()
    {
    	MediaInfo info=new MediaInfo(mVideoPath);
    	if(info.prepare())
    	{
//    		mMarkView.setUseMainVideoPts(true);
        	mMarkView.setRealEncodeEnable(480,480,1000000,(int)info.vFrameRate,editTmpPath);
        	mMarkView.setDrawPadSize(480,480,new onDrawPadSizeChangedListener() {
    			
    			@Override
    			public void onSizeChanged(int viewWidth, int viewHeight) {
    				// TODO Auto-generated method stub
    				 startDrawPad();
    			}
    		});
    	}
    }
    /**
     * Step2:增加一个BitmapLayer到画板上.已经在MarkArrowView中实现了.
     */
    private void startDrawPad()
    {
    	mMarkView.startDrawPad();
		
		mLayerMain=mMarkView.addMainVideoLayer(mplayer.getVideoWidth(),mplayer.getVideoHeight(),null);
		if(mLayerMain!=null){
			mplayer.setSurface(new Surface(mLayerMain.getVideoTexture()));
		}
		mplayer.start();
    }
    
    /**
     * Step3: 增加完成后, 停止画板DrawPad 停止后,为新的视频文件增加上音频部分.
     */
    private void stopDrawPad()
    {
    	if(mMarkView!=null && mMarkView.isRunning()){
			mMarkView.stopDrawPad();
			
			toastStop();
			
			if(SDKFileUtils.fileExist(editTmpPath)){
				boolean ret=VideoEditor.encoderAddAudio(mVideoPath,editTmpPath,SDKDir.TMP_DIR,dstPath);
				if(!ret){
					dstPath=editTmpPath;
				}else
					SDKFileUtils.deleteFile(editTmpPath);
				playVideo.setVisibility(View.VISIBLE);
			}
		}
    }
    private void toastStop()
    {
    	Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
    }
   @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mplayer!=null){
			mplayer.stop();
			mplayer.release();
			mplayer=null;
		}
		if(mMarkView!=null){
			mMarkView.stopDrawPad();
			mMarkView=null;        		   
		}
		if(SDKFileUtils.fileExist(editTmpPath)){
			SDKFileUtils.deleteFile(editTmpPath);
			editTmpPath=null;
		}
		if(SDKFileUtils.fileExist(dstPath)){
			SDKFileUtils.deleteFile(dstPath);
			dstPath=null;
		}
	}
}
