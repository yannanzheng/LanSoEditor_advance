package com.example.advanceDemo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;

import com.lansoeditor.demo.R;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.FileParameter;
import com.lansosdk.box.Layer;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.YUVLayer;
import com.lansosdk.box.onDrawPadOutFrameListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.box.onDrawPadThreadProgressListener;
import com.lansosdk.videoeditor.DrawPadView;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoplayer.VPlayer;
import com.lansosdk.videoplayer.VideoPlayer;
import com.lansosdk.videoplayer.VideoPlayer.OnPlayerCompletionListener;
import com.lansosdk.videoplayer.VideoPlayer.OnPlayerPreparedListener;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * 用来演示当前比较火的抖音的一些功能.
 * 2017年8月1日11:53:20: 目前有灵魂出窍的功能.
 */

public class OutBodyDemoActivity extends Activity implements OnSeekBarChangeListener {
    private static final String TAG = "VideoLayerRealTimeActivity";

    private String mVideoPath;

    private DrawPadView mDrawPadView;
    
    private VPlayer mplayer=null;
    private VideoLayer  mainVideoLayer=null;
    private Layer operationLayer=null;
    
    
    private String editTmpPath=null;
    private String dstPath=null;
    private LinearLayout  playVideo;
    private MediaInfo mInfo=null;
    private Button  btnTest;
    private int postion=0;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.outbody_demo_layout);
        
        mVideoPath = getIntent().getStringExtra("videopath");
        mInfo=new MediaInfo(mVideoPath,false);
    	if(mInfo.prepare()==false){
    		 Toast.makeText(this, "传递过来的视频文件错误", Toast.LENGTH_SHORT).show();
    		 this.finish();
    	}
    	
        mDrawPadView = (DrawPadView) findViewById(R.id.DrawPad_view);
        btnTest=(Button)findViewById(R.id.id_drawpad_testbutton);
        btnTest.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mainVideoLayer!=null){
					mainVideoLayer.setSubImageEnable(false);
				}
			}
		});
     
        playVideo=(LinearLayout)findViewById(R.id.id_DrawPad_saveplay);
        playVideo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(SDKFileUtils.fileExist(dstPath)){
		   			 	Intent intent=new Intent(OutBodyDemoActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
		   		 }else{
		   			 Toast.makeText(OutBodyDemoActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
		   		 }
			}
		});
        playVideo.setVisibility(View.GONE);

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
		}, 300);
    }
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	
    }
    /**
     * VideoLayer是外部提供画面来源, 
     * 您可以用你们自己的播放器作为画面输入源,
     * 也可以用原生的MediaPlayer,只需要视频播放器可以设置surface即可.
     * 
     * 一下举例是采用MediaPlayer作为视频输入源.
     */
    private void startPlayVideo()
    {
    	 if (mVideoPath != null){
    		 
    		 		mplayer=new VPlayer(OutBodyDemoActivity.this);
						
    		 		mplayer.setVideoPath(mVideoPath);
    		 		
    		 		mplayer.setOnPreparedListener(new OnPlayerPreparedListener() {
						
						@Override
						public void onPrepared(VideoPlayer mp) {
							// TODO Auto-generated method stub
							initDrawPad();
						}
					});
    		 		mplayer.setOnCompletionListener(new OnPlayerCompletionListener() {
						
						@Override
						public void onCompletion(VideoPlayer mp) {
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
    /**
     * Step1:  init DrawPad 初始化
     * @param mp
     */
    private void initDrawPad()
    {
    		int padWidth=mInfo.vCodecWidth;
    		int padHeight=mInfo.vCodecHeight;
    	
    		if(mInfo.vRotateAngle==90 || mInfo.vRotateAngle==270){
    			padWidth=mInfo.vCodecHeight;
    			padHeight=mInfo.vCodecWidth;
    		}
        	//设置使能 实时录制, 即把正在DrawPad中呈现的画面实时的保存下来,实现所见即所得的模式
        	mDrawPadView.setRealEncodeEnable(padWidth,padHeight,1000000,(int)mInfo.vFrameRate,editTmpPath);
        	mDrawPadView.setOnDrawPadProgressListener(new onDrawPadProgressListener() {
				
				@Override
				public void onProgress(DrawPad v, long currentTimeUs) {
					// TODO Auto-generated method stub
				}
			});
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
		mDrawPadView.startDrawPad();
		
		mainVideoLayer=mDrawPadView.addMainVideoLayer(mDrawPadView.getDrawPadWidth(),mDrawPadView.getDrawPadHeight(),null);
		if(mainVideoLayer!=null)
		{
			mplayer.setSurface(new Surface(mainVideoLayer.getVideoTexture()));
		}
	
		mplayer.start();
    }
    
    /**
     * Step3: stop DrawPad
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
    private float xpos=0,ypos=0;
	
    /**
     * 提示:实际使用中没有主次之分, 只要是继承自Layer的对象,都可以调节,这里仅仅是举例
     * 可以调节的有:平移,旋转,缩放,RGBA值,显示/不显示(闪烁)效果.
     */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		switch (seekBar.getId()) {
			case R.id.id_DrawPad_skbar_rotate:
				if(operationLayer!=null){
					operationLayer.setRotate(progress);
				}
				break;
			case R.id.id_DrawPad_skbar_moveX:
					if(operationLayer!=null){
						 xpos+=10;
						 if(xpos>mDrawPadView.getViewWidth())
							 xpos=0;
						 operationLayer.setPosition(xpos, operationLayer.getPositionY());
					}
				break;	
			case R.id.id_DrawPad_skbar_moveY:
				if(operationLayer!=null){
					 ypos+=10;
					 if(ypos>mDrawPadView.getViewWidth())
						 ypos=0;
					 operationLayer.setPosition(operationLayer.getPositionX(), ypos);
				}
			break;				
			case R.id.id_DrawPad_skbar_scale:
				if(operationLayer!=null){
					float scale=(float)progress/100;
					int width=(int)(operationLayer.getLayerWidth() * scale);
					operationLayer.setScaledValue(width, operationLayer.getLayerHeight());
				}
			break;		
			case R.id.id_DrawPad_skbar_brightness:
					if(operationLayer!=null){
						float value=(float)progress/100;
						//同时调节RGB的比例, 让他慢慢亮起来,或暗下去.
						operationLayer.setRedPercent(value);  
						operationLayer.setGreenPercent(value); 
						operationLayer.setBluePercent(value);  
					}
				break;
			case R.id.id_DrawPad_skbar_alpha:
				if(operationLayer!=null){
					float value=(float)progress/100;
					operationLayer.setAlphaPercent(0.01f);
				}
				break;
			case R.id.id_DrawPad_skbar_background:
//				if(operationLayer!=null){
//					float value=(float)progress/100;
//					operationLayer.setBackgroundBlurFactor(value);
//				}
				break;
			default:
				break;
		}
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	 private void toastStop()
	    {
	    	Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
	    	Log.i(TAG,"录制已停止!!");
	    }
}
