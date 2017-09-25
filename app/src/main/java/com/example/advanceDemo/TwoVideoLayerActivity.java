package com.example.advanceDemo;

import java.io.IOException;
import java.util.Locale;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;

import com.example.advanceDemo.view.BitmapCache;
import com.example.advanceDemo.view.ShowHeart;
import com.example.advanceDemo.view.SlidingLayer;
import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.BitmapLoader;
import com.lansosdk.box.BoxDecoder;
import com.lansosdk.box.CanvasRunnable;
import com.lansosdk.box.CanvasLayer;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.Layer;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.TwoVideoLayer;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.YUVLayer;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.box.onDrawPadThreadProgressListener;
import com.lansosdk.videoeditor.DrawPadView;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.DrawPadView.onViewAvailable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
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
 * 演示<双视频图层>的功能.
 */

public class TwoVideoLayerActivity extends Activity {
    private static final String TAG = "TwoVideoLayerActivity";

    private String mVideoPath;

    private DrawPadView mDrawPadView;
    
    private MediaPlayer mplayer=null;
    private MediaPlayer mplayer2=null;
    private TwoVideoLayer  twoVideoLayer=null;
    
    
    private String editTmpPath=null;
    private String dstPath=null;
    private LinearLayout  playVideo;
    private  MediaInfo mInfo;
    private boolean isDisplayed;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twovideolayer_demo_layout);
        
        mVideoPath = getIntent().getStringExtra("videopath");
        
        mDrawPadView = (DrawPadView) findViewById(R.id.DrawPad_view);
        mInfo=new MediaInfo(mVideoPath,false);
        if(mInfo.prepare()==false)
        {
        	 Toast.makeText(TwoVideoLayerActivity.this, "视频源文件错误!", Toast.LENGTH_SHORT).show();
        	 this.finish();
        }
        
        playVideo=(LinearLayout)findViewById(R.id.id_DrawPad_saveplay);
        playVideo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(SDKFileUtils.fileExist(dstPath)){
		   			 	Intent intent=new Intent(TwoVideoLayerActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
		   		 }else{
		   			 Toast.makeText(TwoVideoLayerActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
		   		 }
			}
		});
        playVideo.setVisibility(View.GONE);
        findViewById(R.id.id_drawpad_testbutton).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(twoVideoLayer!=null){
					isDisplayed=!isDisplayed;
					twoVideoLayer.setDisplayTexture2(isDisplayed);
				}
			}
		});
        //在手机的默认路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
        editTmpPath=SDKFileUtils.newMp4PathInBox();
        dstPath=SDKFileUtils.newMp4PathInBox();
       
        new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//showHintDialog();
				startPlayVideo();
			}
		}, 500);
    }
    /**
     * VideoLayer是外部提供画面来源, 您可以用你们自己的播放器作为画面输入源,也可以用原生的MediaPlayer,只需要视频播放器可以设置surface即可.
     * 一下举例是采用MediaPlayer作为视频输入源.
     */
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
					initDrawPad(mp);
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
              finish();
              return;
          }
    }
    private void toastStop()
    {
    	Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
    	Log.i(TAG,"录制已停止!!");
    }
    /**
     * Step1:  init DrawPad 初始化
     * @param mp
     */
    private void initDrawPad(MediaPlayer mp)
    {
        	int padWidth=544;
        	int padHeight=960;
        	
        	//设置使能 实时录制, 即把正在DrawPad中呈现的画面实时的保存下来,实现所见即所得的模式
        	mDrawPadView.setRealEncodeEnable(padWidth,padHeight,3000000,(int)mInfo.vFrameRate,editTmpPath);
        	mDrawPadView.setOnDrawPadProgressListener(new onDrawPadProgressListener() {
				
				@Override
				public void onProgress(DrawPad v, long currentTimeUs) {
					// TODO Auto-generated method stub
				}
			});
        	
//        	mDrawPadView.setUpdateMode(DrawPadUpdateMode.AUTO_FLUSH, (int)mInfo.vFrameRate);
        	
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
     * Step2: 开始运行 Drawpad
     */
    private void startDrawPad()
    {
    	// 开始DrawPad的渲染线程. 
    	mDrawPadView.pauseDrawPad();
		mDrawPadView.startDrawPad();
		
		//增加一个主视频的 VideoLayer
		twoVideoLayer=mDrawPadView.addTwoVideoLayer(mplayer.getVideoWidth(),mplayer.getVideoHeight());
		if(twoVideoLayer!=null)
		{
			mplayer.setSurface(new Surface(twoVideoLayer.getVideoTexture()));
		}
		mplayer.start();
	
		mplayer2=new MediaPlayer();
  	  try {
				mplayer2.setDataSource("/sdcard/taohua.mp4");
				mplayer2.prepare();
				mplayer2.setSurface(new Surface(twoVideoLayer.getVideoTexture2()));
				mplayer2.start();
		  }  catch (IOException e) {
			// TODO Auto-generated catch block
			  e.printStackTrace();
		 }
		mDrawPadView.resumeDrawPad();
    }
    private void changeMp2()
    {
    	if(mplayer2!=null){
    		mplayer2.stop();
    		mplayer2.release();
    		mplayer2=null;
    	}
    	mplayer2=new MediaPlayer();
  	  try {
				mplayer2.setDataSource("/sdcard/mask.mp4");
				mplayer2.prepare();
				
				mplayer2.setSurface(new Surface(twoVideoLayer.getVideoTexture2()));
				mplayer2.start();
		  }  catch (IOException e) {
			// TODO Auto-generated catch block
			  e.printStackTrace();
		 }
    }
    
    /**
     * Step3: stop DrawPad
     */
    private void stopDrawPad()
    {
    	if(mDrawPadView!=null && mDrawPadView.isRunning()){
			
			mDrawPadView.stopDrawPad();
			toastStop();
			
			//增加音频
			if(SDKFileUtils.fileExist(editTmpPath)){
				boolean ret=VideoEditor.encoderAddAudio(mVideoPath,editTmpPath,SDKDir.TMP_DIR,dstPath);
				if(!ret){
					dstPath=editTmpPath;
				}else{
					SDKFileUtils.deleteFile(editTmpPath);	
				}
				playVideo.setVisibility(View.VISIBLE);
			}else{
				Log.e(TAG," player completion, but file:"+editTmpPath+" is not exist!!!");
			}
		}
    }
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	if(mplayer!=null){
    		mplayer.stop();
    		mplayer.release();
    		mplayer=null;
    	}
    	
    	if(mplayer2!=null){
    		mplayer2.stop();
    		mplayer2.release();
    		mplayer2=null;
    	}
    	if(mDrawPadView!=null){
    		mDrawPadView.stopDrawPad();
    	}
    }
    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
	    if(SDKFileUtils.fileExist(dstPath)){
	    	SDKFileUtils.deleteFile(dstPath);
	    }
	    if(SDKFileUtils.fileExist(editTmpPath)){
	    	SDKFileUtils.deleteFile(editTmpPath);
	    } 
	}
}
