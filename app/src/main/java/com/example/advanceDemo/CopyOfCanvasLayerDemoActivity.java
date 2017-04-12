package com.example.advanceDemo;

import java.io.IOException;
import java.util.Locale;


import com.example.advanceDemo.view.BitmapCache;
import com.example.advanceDemo.view.DrawPadView;
import com.example.advanceDemo.view.ShowHeart;
import com.example.advanceDemo.view.DrawPadView.onViewAvailable;
import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.CanvasRunnable;
import com.lansosdk.box.CanvasLayer;
import com.lansosdk.box.Layer;
import com.lansosdk.box.DrawPad;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * 演示: 使用DrawPad来实现 视频和图片的实时叠加. 
 * 
 * 流程是: 
 * 先创建一个DrawPad,然后在视频播放过程中,从DrawPad中增加一个CanvasLayer,然后可以调节SeekBar来对Layer的每个
 * 参数进行调节.
 * 
 */

public class CopyOfCanvasLayerDemoActivity extends Activity {
    private static final String TAG = "CopyOfCanvasLayerDemoActivity";

    private String mVideoPath;

    private DrawPadView mDrawPadView;
    
    private MediaPlayer mplayer=null;
    private VideoLayer  mLayerMain=null;
    
    private CanvasLayer mCanvasLayer=null;
    ShowHeart mShowHeart;
    
    MediaInfo info;
    private String editTmpPath=null;
    private String dstPath=null;
    private LinearLayout  playVideo;
    
    private EditText etX,etY,etStartTime,etEndTime,etText;
    
    private int posX,posY;
    private long startTime,endTime;  //单位毫秒
    private String showText;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.canvaslayer_demo_layout2);
        
        mVideoPath = getIntent().getStringExtra("videopath");
        mDrawPadView = (DrawPadView) findViewById(R.id.id_canvaslayer_drawpadview);
        
        info=new MediaInfo(mVideoPath,false);
    	
		info.prepare();
			
        playVideo=(LinearLayout)findViewById(R.id.id_canvasLayer_saveplay);
        playVideo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(SDKFileUtils.fileExist(dstPath)){
		   			 	Intent intent=new Intent(CopyOfCanvasLayerDemoActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
		   		 }else{
		   			 Toast.makeText(CopyOfCanvasLayerDemoActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
		   		 }
			}
		});
        playVideo.setVisibility(View.GONE);
        
        
        etX=(EditText)findViewById(R.id.id_canvasdemo_et_x);
        etY=(EditText)findViewById(R.id.id_canvasdemo_et_y);
        etStartTime=(EditText)findViewById(R.id.id_canvasdemo_et_starttime);
        etEndTime=(EditText)findViewById(R.id.id_canvasdemo_et_endtime);
        etText=(EditText)findViewById(R.id.id_canvasdemo_et_text);
        
        findViewById(R.id.id_canvasdemo_start).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				
				posX=Integer.valueOf(etX.getText().toString());
				posY=Integer.valueOf(etY.getText().toString());
				startTime=Integer.valueOf(etStartTime.getText().toString());
				endTime=Integer.valueOf(etEndTime.getText().toString());
				showText=etText.getText().toString();
				
				if(startTime>=0 && startTime<=info.vDuration*1000 && endTime>=0 && endTime<=info.vDuration*1000 && endTime>startTime)
				{
					startPlayVideo();
				}else{
					Log.i(TAG,"Iinfo"+info.toString());
				}
			}
		});
        

        //在手机的默认路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
        editTmpPath=SDKFileUtils.newMp4PathInBox();
        dstPath=SDKFileUtils.newMp4PathInBox();
        
//		new Handler().postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				//showHintDialog();
//			
//			}
//		}, 500);
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
    private void toastStop()
    {
    	Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
    	Log.i(TAG,"录制已停止!!");
    }
    /**
     * Step1: 初始化 DrawPad 画板
     */
    private void initDrawPad()
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
    /**
     * Step2: 开始运行画板
     */
    private void startDrawPad()
    {
    	mDrawPadView.startDrawPad(true);
		/**
		 * 增加一个主视频的 VideoLayer
		 */
		mLayerMain=mDrawPadView.addMainVideoLayer(mplayer.getVideoWidth(),mplayer.getVideoHeight(),null);
		if(mLayerMain!=null){
			mplayer.setSurface(new Surface(mLayerMain.getVideoTexture()));
		}
		
		mplayer.start();
		addCanvasLayer();  //增加一个CanvasLayer
    }
    /**
     * Step3: 停止画板,停止后,为新的视频文件增加上音频部分.
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
				playVideo.setVisibility(View.VISIBLE);
			}else{
				Log.e(TAG," player completion, but file:"+editTmpPath+" is not exist!!!");
			}
		}
    }
    private void addCanvasLayer()
    {
    	if(mDrawPadView==null)
    		return ;
    	
    	mCanvasLayer=mDrawPadView.addCanvasLayer();
		if(mCanvasLayer!=null)
		{
				/**
				 * 在绘制一帧的时候, 是否清除上一帧绘制的 内容.
				 */
				mCanvasLayer.setClearCanvas(false);
				
				mShowHeart=new ShowHeart(CopyOfCanvasLayerDemoActivity.this,mCanvasLayer.getPadWidth(),mCanvasLayer.getPadHeight());
				/**
				 * 这里增加两个 CanvasRunnable
				 * CanvasRunnable是把当前的一段代码放到 DrawPad线程中运行的一个类. 类似GLSurfaceView的queueEvent
				 */
				mCanvasLayer.addCanvasRunnable(new CanvasRunnable() {
					
					@Override
					public void onDrawCanvas(CanvasLayer layer, Canvas canvas,
							long currentTimeUs) {
						// TODO Auto-generated method stub
							Paint paint = new Paint();
			                paint.setColor(Color.RED);
		         			paint.setAntiAlias(true);
		         			paint.setTextSize(80);
		         			
		         			if(currentTimeUs>endTime*1000)
		         			{
		         				mCanvasLayer.setVisibility(Layer.INVISIBLE);
		         			}else if(currentTimeUs> startTime*1000){
		         			  	canvas.drawText(showText,posX,posY, paint);
		         			}
		         	  
		         		//canvas.drawText("蓝松短视频演示之<任意绘制>",20,mCanvasLayer.getPadHeight()-200, paint);
					}
				});
				mDrawPadView.resumeDrawPadRecord();
				
				/**
				 * 增加另一个CanvasRunnable
				 */
				mCanvasLayer.addCanvasRunnable(new CanvasRunnable() {
				
				@Override
				public void onDrawCanvas(CanvasLayer layer, Canvas canvas,
						long currentTimeUs) {
					// TODO Auto-generated method stub
					mShowHeart.drawTrack(canvas);
				}
				});
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
