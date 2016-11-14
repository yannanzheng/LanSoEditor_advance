package com.example.lansongeditordemo;

import java.io.IOException;
import java.util.Locale;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;

import com.example.lansong.animview.BitmapCache;
import com.example.lansong.animview.ShowHeart;
import com.example.lansongeditordemo.view.DrawPadView;
import com.example.lansongeditordemo.view.DrawPadView.onViewAvailable;
import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapPen;
import com.lansosdk.box.CanvasRunnable;
import com.lansosdk.box.CanvasPen;
import com.lansosdk.box.Pen;
import com.lansosdk.box.DrawPad;
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

/**
 * 演示: 使用DrawPad来实现 视频和图片的实时叠加. 
 * 
 * 流程是: 
 * 先创建一个DrawPad,然后在视频播放过程中,从DrawPad中获取一个CanvasPen,然后可以调节SeekBar来对Pen的每个
 * 参数进行调节.
 * 
 */

public class CanvasPenDemoActivity extends Activity {
    private static final String TAG = "VideoPictureRealTimeActivity";

    private String mVideoPath;

    private DrawPadView mPlayView;
    
    private MediaPlayer mplayer=null;
    private Pen  mPenMain=null;
    
    private CanvasPen mCanvasPen=null;
    ShowHeart mShowHeart;
    
    private String editTmpPath=null;
    private String dstPath=null;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
		 Thread.setDefaultUncaughtExceptionHandler(new snoCrashHandler());
        setContentView(R.layout.canvaspen_demo_layout);
        
        mVideoPath = getIntent().getStringExtra("videopath");
        mPlayView = (DrawPadView) findViewById(R.id.id_canvaspen_drawpadview);
        
        
        
        findViewById(R.id.id_canvasPen__saveplay).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(SDKFileUtils.fileExist(dstPath)){
		   			 	Intent intent=new Intent(CanvasPenDemoActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
		   		 }else{
		   			 Toast.makeText(CanvasPenDemoActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
		   		 }
			}
		});
        findViewById(R.id.id_canvasPen__saveplay).setVisibility(View.GONE);

        //在手机的/sdcard/lansongBox/路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
        editTmpPath=SDKFileUtils.newMp4PathInBox();
        dstPath=SDKFileUtils.newMp4PathInBox();
        
        //增加提示缩放到480的文字.
        DemoUtils.showScale480HintDialog(CanvasPenDemoActivity.this);
    }
   
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//showHintDialog();
				startPlayVideo();
			}
		}, 100);
    }
    /**
     * VideoPen是外部提供画面来源, 您可以用你们自己的播放器作为画面输入源,也可以用原生的MediaPlayer,只需要视频播放器可以设置surface即可.
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
					startDrawPad();
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
    private void toastStop()
    {
    	Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
    	Log.i(TAG,"录制已停止!!");
    	
    }
    //Step1: 开始运行 DrawPad 画板
    private void startDrawPad()
    {
    	
    		MediaInfo info=new MediaInfo(mVideoPath,false);
        	info.prepare();
        	
       		//设置使能 实时录制, 即把正在DrawPad中呈现的画面实时的保存下来,实现所见即所得的模式
        	mPlayView.setRealEncodeEnable(480,480,1000000,(int)info.vFrameRate,editTmpPath);
        	
        	//设置当前DrawPad的宽度和高度,并把宽度自动缩放到父view的宽度,然后等比例调整高度.
    		mPlayView.setDrawPadSize(480,480,new onDrawPadSizeChangedListener() {
			
			@Override
			public void onSizeChanged(int viewWidth, int viewHeight) {
				// TODO Auto-generated method stub
				// 开始DrawPad的渲染线程. 
				mPlayView.startDrawPad(null,null);
				//获取一个主视频的 VideoPen
				mPenMain=mPlayView.addMainVideoPen(mplayer.getVideoWidth(),mplayer.getVideoHeight());
				if(mPenMain!=null){
					mplayer.setSurface(new Surface(mPenMain.getVideoTexture()));
				}
				mplayer.start();
				
				
				addCanvasPen();
			}
		});
    }
    //Step2:增加一个CanvasPen到画板上
    private void addCanvasPen()
    {
    	if(mPlayView==null)
    		return ;
    	
    	mCanvasPen=mPlayView.addCanvasPen();
		if(mCanvasPen!=null)
		{
				mCanvasPen.setClearCanvas(false);
				
				mShowHeart=new ShowHeart(CanvasPenDemoActivity.this,mCanvasPen.getWidth(),mCanvasPen.getHeight());
				//这里增加两个CanvasRunnable
				mCanvasPen.addCanvasRunnable(new CanvasRunnable() {
					
					@Override
					public void onDrawCanvas(CanvasPen Pen, Canvas canvas,
							long currentTimeUs) {
						// TODO Auto-generated method stub
						
						 Paint paint = new Paint();
		                 paint.setColor(Color.RED);
		         			paint.setAntiAlias(true);
		         			paint.setTextSize(80);
	
		         		canvas.drawText("蓝松短视频演示之<任意绘制>",20,mCanvasPen.getHeight()-200, paint);
					}
				});
				
				//增加另一个
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
    //Step3: 停止画板
    private void stopDrawPad()
    {
    	if(mPlayView!=null && mPlayView.isRunning()){
			
			mPlayView.stopDrawPad();
			
			toastStop();
			
			if(SDKFileUtils.fileExist(editTmpPath)){
				boolean ret=VideoEditor.encoderAddAudio(mVideoPath,editTmpPath,SDKDir.TMP_DIR,dstPath);
				if(!ret){
					dstPath=editTmpPath;
				}else{
					SDKFileUtils.deleteFile(editTmpPath);	
				}
				
				findViewById(R.id.id_canvasPen__saveplay).setVisibility(View.VISIBLE);
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
    	
    	if(mPlayView!=null){
    		mPlayView.stopDrawPad();
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
