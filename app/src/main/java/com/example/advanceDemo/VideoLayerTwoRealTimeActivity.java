package com.example.advanceDemo;


import java.io.IOException;
import java.util.Locale;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageScreenBlendFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageTwoInputFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBlackMaskBlendFilter;

import com.example.advanceDemo.GPUImageFilterTools.FilterAdjuster;
import com.example.advanceDemo.GPUImageFilterTools.OnGpuImageFilterChosenListener;
import com.example.advanceDemo.view.DrawPadView;
import com.lansoeditor.demo.R;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.Layer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoplayer.VPlayer;
import com.lansosdk.videoplayer.VideoPlayer;
import com.lansosdk.videoplayer.VideoPlayer.OnPlayerPreparedListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
 * 演示: 使用DrawPad来实现 视频和视频的实时叠加.
 * 
 * 流程是: 
 * 先创建一个DrawPad,增加主VideoLayer,在播放过程中,再次增加一个VideoLayer然后可以调节SeekBar来对
 * Layer的每个参数进行调节.
 * 
 * 可以调节的有:平移,旋转,缩放,RGBA值,显示/不显示(闪烁)效果.
 * 实际使用中, 可用这些属性来扩展一些功能.
 * 
 * 比如 调节另一个视频的RGBA中的A值来实现透明叠加效果,类似MV的效果.
 * 
 * 比如 调节另一个视频的平移,缩放,旋转来实现贴纸的效果.
 * 
 */
public class VideoLayerTwoRealTimeActivity extends Activity implements OnSeekBarChangeListener {
    private static final String TAG = "VideoActivity";

    private String mVideoPath;

    private DrawPadView mDrawPadView;
    
    private MediaPlayer mplayer=null;
    private VPlayer  vplayer=null;
    
    private MediaPlayer mplayer2=null;
    
    private VideoLayer subVideoLayer=null;
    private VideoLayer  mVideoLayer=null;
    
    private String editTmpPath=null;
    private String dstPath=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawpad_layout);
        initView();
        
        mVideoPath = getIntent().getStringExtra("videopath");
        mDrawPadView = (DrawPadView) findViewById(R.id.DrawPad_view);

        /**
         * 在手机的默认路径下创建一个文件名,
         * 用来保存生成的视频文件,(在onDestroy中删除)
         */
        editTmpPath=SDKFileUtils.newMp4PathInBox();
        dstPath=SDKFileUtils.newMp4PathInBox();
        
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
    /**
     * Step1:  init Drawpad  初始化DrawPad
     * 
     * @param mp
     */
    private void initDrawPad(MediaPlayer mp)
    {
    	MediaInfo info=new MediaInfo(mVideoPath,false);
    	info.prepare();
		
    	// 设置DrawPad的刷新模式,默认 {@link DrawPad.UpdateMode#ALL_VIDEO_READY};
    	mDrawPadView.setUpdateMode(DrawPadUpdateMode.ALL_VIDEO_READY,25);
    	
    		
    	//设置使能 实时录制, 即把正在DrawPad中呈现的画面实时的保存下来,起到所见即所得的模式
    	mDrawPadView.setRealEncodeEnable(480,480,1000000,(int)info.vFrameRate,editTmpPath);
    	
    	//设置当前DrawPad的宽度和高度,并把宽度自动缩放到父view的宽度,然后等比例调整高度.
    	mDrawPadView.setDrawPadSize(480,480,new onDrawPadSizeChangedListener() {
			
			@Override
			public void onSizeChanged(int viewWidth, int viewHeight) {
				// TODO Auto-generated method stub
				startDrawPad();
			}
		});
    }
    /**
     * Step2:  start DrawPad 开始运行
     */
    private void startDrawPad()
    {
    	// 开始DrawPad的渲染线程. 
		mDrawPadView.startDrawPad();
		//增加一个主视频的 VideoLayer
		mVideoLayer=mDrawPadView.addMainVideoLayer(mplayer.getVideoWidth(),mplayer.getVideoHeight(),null);
		if(mVideoLayer!=null){
			mplayer.setSurface(new Surface(mVideoLayer.getVideoTexture()));
		}
		mplayer.start();
		startPlayer2();
    }
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
			}
		}
    }
    boolean isFirstRemove=false;
    
    private void startPlayer2()
    {
    	mplayer2=new MediaPlayer();
    	try {
    		mplayer2.setDataSource(mVideoPath);
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	mplayer2.setOnPreparedListener(new OnPreparedListener() {
			
			@Override
			public void onPrepared(MediaPlayer mp) {
				// TODO Auto-generated method stub
				//这里有白色, 目标是把白色去掉.
				subVideoLayer=mDrawPadView.addVideoLayer(mp.getVideoWidth(),mp.getVideoHeight(),null);
				if(subVideoLayer!=null){
					mplayer2.setSurface(new Surface(subVideoLayer.getVideoTexture()));	
					subVideoLayer.setScale(0.5f);  //这里先缩小一半
				}
				mplayer2.start();
			}
		});
    	mplayer2.setOnErrorListener(new OnErrorListener() {
			
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				// TODO Auto-generated method stub
				return false;
			}
		});
    	mplayer2.setOnInfoListener( new OnInfoListener() {
			
			@Override
			public boolean onInfo(MediaPlayer mp, int what, int extra) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		mplayer2.prepareAsync();
    }
    
    private void toastStop()
    {
    	Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
    }
    boolean isDestorying=false;  //是否正在销毁, 因为销毁会停止DrawPad
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	
    	
    		isDestorying=true;
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
			
			if(vplayer!=null){
				vplayer.stop();
				vplayer.release();
				vplayer=null;
			}
			
			if(mDrawPadView!=null){
				mDrawPadView.stopDrawPad();
				mDrawPadView=null;        		   
			}
			if(SDKFileUtils.fileExist(dstPath)){
				SDKFileUtils.deleteFile(dstPath);
		    }
		    if(SDKFileUtils.fileExist(editTmpPath)){
		    	SDKFileUtils.deleteFile(editTmpPath);
		    } 
	}
    
    private LinearLayout  playVideo;
    private void initView()
    {
    	 initSeekBar(R.id.id_DrawPad_skbar_rotate,360); //角度是旋转360度.
         initSeekBar(R.id.id_DrawPad_skbar_move,100);   //move的百分比暂时没有用到,举例而已.
         
         initSeekBar(R.id.id_DrawPad_skbar_scale,300);   //这里设置最大可放大8倍
         
         initSeekBar(R.id.id_DrawPad_skbar_red,100);  //red最大为100
         initSeekBar(R.id.id_DrawPad_skbar_green,100);
         initSeekBar(R.id.id_DrawPad_skbar_blue,100);
         initSeekBar(R.id.id_DrawPad_skbar_alpha,100);
         playVideo=(LinearLayout)findViewById(R.id.id_DrawPad_saveplay);
         playVideo.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				 if(SDKFileUtils.fileExist(dstPath)){
 		   			 	Intent intent=new Intent(VideoLayerTwoRealTimeActivity.this,VideoPlayerActivity.class);
 		   			 	intent.putExtra("videopath", dstPath);
 		   			 	startActivity(intent);
 		   		 }else{
 		   			 Toast.makeText(VideoLayerTwoRealTimeActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
 		   		 }
 			}
 		});
         playVideo.setVisibility(View.GONE);
    }
    private void initSeekBar(int resId,int maxvalue)
    {
    	SeekBar   skbar=(SeekBar)findViewById(resId);
           skbar.setOnSeekBarChangeListener(this);
           skbar.setMax(maxvalue);
    }
    /**
     * 提示:实际使用中没有主次之分, 只要是继承自Layer的对象,都可以调节,这里仅仅是举例
     */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		switch (seekBar.getId()) {
			case R.id.id_DrawPad_skbar_rotate:
				if(subVideoLayer!=null){
					subVideoLayer.setRotate(progress);
//					RotateCnt++;  //因当前视频图层旋转任意角度时,会有画面拉伸的效果, 我们提供了4个方法来旋转90/180/270/0来供您使用.
//					if(RotateCnt>3){
//						RotateCnt=0;
//					}
//					if(RotateCnt==0)
//						subVideoLayer.setRotate0();
//					else if(RotateCnt==1)
//						subVideoLayer.setRotate90();
//					else if(RotateCnt==2)
//						subVideoLayer.setRotate180();
//					else if(RotateCnt==3)
//						subVideoLayer.setRotate270();
//					else
//						subVideoLayer.setRotate0();
				}
				break;
			case R.id.id_DrawPad_skbar_move:
					if(subVideoLayer!=null){
						float ypos=subVideoLayer.getPositionY()+10;
						 
						 if(ypos>mDrawPadView.getViewWidth()){
							 ypos=0;
						 }
						 subVideoLayer.setPosition(subVideoLayer.getPositionX(),ypos);
					}
				break;				
			case R.id.id_DrawPad_skbar_scale:
				if(subVideoLayer!=null){
					float scale=(float)progress/100;
					subVideoLayer.setScale(scale);
				}
			break;		
			case R.id.id_DrawPad_skbar_red:
					if(subVideoLayer!=null){
						float value=(float)progress/100;
						subVideoLayer.setRedPercent(value);  //设置每个RGBA的比例,默认是1
					}
				break;

			case R.id.id_DrawPad_skbar_green:
					if(subVideoLayer!=null){
						float value=(float)progress/100;
						subVideoLayer.setGreenPercent(value);
					}
				break;

			case R.id.id_DrawPad_skbar_blue:
					if(subVideoLayer!=null){
						float value=(float)progress/100;
						subVideoLayer.setBluePercent(value);
					}
				break;
			case R.id.id_DrawPad_skbar_alpha:
					if(subVideoLayer!=null){
						float value=(float)progress/100;
						subVideoLayer.setAlphaPercent(value);
					}
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
}
