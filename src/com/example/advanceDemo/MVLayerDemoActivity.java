package com.example.advanceDemo;

import java.io.IOException;
import java.util.Locale;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;

import com.example.advanceDemo.view.BitmapCache;
import com.example.advanceDemo.view.DrawPadView;
import com.example.advanceDemo.view.ShowHeart;
import com.example.advanceDemo.view.DrawPadView.onViewAvailable;
import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.CanvasRunnable;
import com.lansosdk.box.CanvasLayer;
import com.lansosdk.box.MVLayerENDMode;
import com.lansosdk.box.Layer;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.MVLayer;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;

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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MVLayerDemoActivity extends Activity {
    private static final String TAG = "MVLayerDemoActivity";

    private String mVideoPath;

    private DrawPadView mDrawPadView;
    
    private MediaPlayer mplayer=null;
    private MediaPlayer mplayer2=null;
    private VideoLayer  mLayerMain=null;
    private MVLayer mMVLayer=null;
    
    private String editTmpPath=null;
    private String dstPath=null;
    
    private String colorMVPath=null;
    private String maskMVPath=null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mvlayer_demo_layout);
        initView();
        
        mVideoPath = getIntent().getStringExtra("videopath");
        mDrawPadView = (DrawPadView) findViewById(R.id.id_mvlayer_padview);
        

        //在手机的/sdcard/lansongBox/路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
        editTmpPath=SDKFileUtils.newMp4PathInBox();
        dstPath=SDKFileUtils.newMp4PathInBox();
        
        /**
         * 拿到两个文件,来还原出mv的视频.
         */
        colorMVPath=com.lansosdk.videoeditor.CopyDefaultVideoAsyncTask.copyFile(MVLayerDemoActivity.this,"mei.ts");
        maskMVPath=com.lansosdk.videoeditor.CopyDefaultVideoAsyncTask.copyFile(MVLayerDemoActivity.this,"mei_b.ts");
    	
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
     * Step1: 开始运行 DrawPad 画板
     */
    private void initDrawPad()
    {
    	if(colorMVPath!=null && maskMVPath!=null)
    	{
    		MediaInfo info=new MediaInfo(mVideoPath,false);
        	if(info.prepare())
        	{

        		//设置使能 实时录制, 即把正在DrawPad中呈现的画面实时的保存下来,实现所见即所得的模式
        		mDrawPadView.setRealEncodeEnable(480,480,1000000,(int)info.vFrameRate,editTmpPath);
	            
        		//设置当前DrawPad的宽度和高度,并把宽度自动缩放到父view的宽度,然后等比例调整高度.
        		mDrawPadView.setDrawPadSize(480,480,new onDrawPadSizeChangedListener() {
	    			
	    			@Override
	    			public void onSizeChanged(int viewWidth, int viewHeight) {
	    				// TODO Auto-generated method stub
	    				// 开始DrawPad的渲染线程. 
	    				startDrawPad();
	    				}
	        		});	
        	}
    	}
    }
    /**
     * Step2: 开始运行 Drawpad线程.
     */
    private void startDrawPad()
    {
    	mDrawPadView.startDrawPad();
		
		//增加一个主视频的 VideoLayer
		mLayerMain=mDrawPadView.addMainVideoLayer(mplayer.getVideoWidth(),mplayer.getVideoHeight(),null);
		if(mLayerMain!=null){
			mplayer.setSurface(new Surface(mLayerMain.getVideoTexture()));
		}
		mplayer.start();
		
		mMVLayer=mDrawPadView.addMVLayer(colorMVPath, maskMVPath);  //<-----增加MVLayer
//		if(mMVLayer!=null){  //可以增加mv在播放结束后的动作, 有停止到最后一帧, 从头循环, 消失.
//			mMVLayer.setEndMode(MVLayerENDMode.LOOP);
//		}
    }
    /**
     * Step3: 停止画板
     */
    private void stopDrawPad()
    {
    	if(mDrawPadView!=null && mDrawPadView.isRunning()){
			
			mDrawPadView.stopDrawPad();
			
			toastStop();
			
			if(SDKFileUtils.fileExist(editTmpPath)){
				boolean ret=VideoEditor.encoderAddAudio(mVideoPath,editTmpPath,SDKDir.TMP_DIR,dstPath);
				if(!ret){
					dstPath=editTmpPath;
				}else{
					SDKFileUtils.deleteFile(editTmpPath);	
				}
				
				findViewById(R.id.id_mvlayer_saveplay).setVisibility(View.VISIBLE);
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
   private void initView()
   {
	   findViewById(R.id.id_mvlayer_saveplay).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(SDKFileUtils.fileExist(dstPath)){
		   			 	Intent intent=new Intent(MVLayerDemoActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
		   		 }else{
		   			 Toast.makeText(MVLayerDemoActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
		   		 }
			}
		});
       
       findViewById(R.id.id_mvlayer_saveplay).setVisibility(View.GONE);
   }
   private void toastStop()
   {
   		Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
   		Log.i(TAG,"录制已停止!!");
   }
}
