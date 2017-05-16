package com.example.advanceDemo;

import java.io.IOException;
import java.util.Locale;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;

import com.example.advanceDemo.view.BitmapCache;
import com.example.advanceDemo.view.ShowHeart;
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
 * 演示: 使用DrawPad来实现 视频和图片的实时叠加. 
 * 
 * 流程是: 
 * 先创建一个DrawPad,然后在视频播放过程中,从DrawPad中增加一个BitmapLayer,然后可以调节SeekBar来对Layer的每个
 * 参数进行调节.
 * 
 * 可以调节的有:平移,旋转,缩放,RGBA值,显示/不显示(闪烁)效果.
 * 实际使用中, 可用这些属性来做些动画,比如平移+RGBA调节,呈现舒缓移除的效果. 缓慢缩放呈现照片播放效果;旋转呈现欢快的炫酷效果等等.
 */

public class TwoVideoLayerRealTimeActivity extends Activity implements OnSeekBarChangeListener {
    private static final String TAG = "VideoLayerRealTimeActivity";

    private String mVideoPath;

    private DrawPadView mDrawPadView;
    
    private MediaPlayer mplayer=null;
    private MediaPlayer mplayer2=null;
    private TwoVideoLayer  videoMainLayer=null;
    private VideoLayer operationLayer=null;
    
    
    private String editTmpPath=null;
    private String dstPath=null;
    private LinearLayout  playVideo;
    private  MediaInfo mInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawpad_layout);
        
        mVideoPath = getIntent().getStringExtra("videopath");
        mDrawPadView = (DrawPadView) findViewById(R.id.DrawPad_view);
        mInfo=new MediaInfo(mVideoPath,false);
        if(mInfo.prepare()==false)
        {
        	 Toast.makeText(TwoVideoLayerRealTimeActivity.this, "视频源文件错误!", Toast.LENGTH_SHORT).show();
        	 this.finish();
        }
        
        initSeekBar(R.id.id_DrawPad_skbar_rotate,360); //角度是旋转360度,如果值大于360,则取360度内剩余的角度值.
        initSeekBar(R.id.id_DrawPad_skbar_moveX,100);
        initSeekBar(R.id.id_DrawPad_skbar_moveY,100);
        
        initSeekBar(R.id.id_DrawPad_skbar_scale,800);   //这里设置最大可放大8倍
        
        initSeekBar(R.id.id_DrawPad_skbar_brightness,100);  //red最大为100
        initSeekBar(R.id.id_DrawPad_skbar_alpha,100);
        initSeekBar(R.id.id_DrawPad_skbar_background,800);
        
        
    	
    	
        playVideo=(LinearLayout)findViewById(R.id.id_DrawPad_saveplay);
        playVideo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(SDKFileUtils.fileExist(dstPath)){
		   			 	Intent intent=new Intent(TwoVideoLayerRealTimeActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
		   		 }else{
		   			 Toast.makeText(TwoVideoLayerRealTimeActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
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
		}, 500);
    }
    private void initSeekBar(int resId,int maxvalue)
    {
    	   SeekBar skbar=(SeekBar)findViewById(resId);
           skbar.setOnSeekBarChangeListener(this);
           skbar.setMax(maxvalue);
    }
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	
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
    	
        	
        	int padWidth=480;
        	int padHeight=480;
        	
        	//设置使能 实时录制, 即把正在DrawPad中呈现的画面实时的保存下来,实现所见即所得的模式
        	mDrawPadView.setRealEncodeEnable(padWidth,padHeight,1000000,(int)mInfo.vFrameRate,editTmpPath);
        	mDrawPadView.setOnDrawPadProgressListener(new onDrawPadProgressListener() {
				
				@Override
				public void onProgress(DrawPad v, long currentTimeUs) {
					// TODO Auto-generated method stub
				}
			});
        	
        	mDrawPadView.setUpdateMode(DrawPadUpdateMode.AUTO_FLUSH, (int)mInfo.vFrameRate);
        	
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
		
		//如果视频太单调了, 可以给视频增加一个背景图片, 显得艺术一些.^^
//		
//		mDrawPadView.addBitmapLayer(BitmapLoader.loadBitmap(getApplicationContext(), var3, 0, 0));
		mDrawPadView.addBitmapLayer(BitmapFactory.decodeResource(getResources(), R.drawable.videobg));
		
		//增加一个主视频的 VideoLayer
		videoMainLayer=mDrawPadView.addTwoVideoLayer(mplayer.getVideoWidth(),mplayer.getVideoHeight());
		
		if(videoMainLayer!=null)
		{
			mplayer.setSurface(new Surface(videoMainLayer.getVideoTexture()));
		}
	
		
		mplayer.start();
	
		  mplayer2=new MediaPlayer();
    	  try {
			mplayer2.setDataSource("/sdcard/mask.mp4");
			mplayer2.prepare();
			
			mplayer2.setSurface(new Surface(videoMainLayer.getVideoTexture2()));
			mplayer2.start();
		}  catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		addBitmapLayer();
		mDrawPadView.resumeDrawPad();
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
    private void addGifLayer()
    {
    	if(mDrawPadView!=null && mDrawPadView.isRunning())
    	{
//    		operationLayer=mDrawPadView.addGifLayer(R.drawable.g06);
    		mDrawPadView.addGifLayer(R.drawable.g07);  //增加另一个.
    	}
    }
    /**
     * 从DrawPad中得到一个BitmapLayer,填入要显示的图片,您实际可以是资源图片,也可以是png或jpg,或网络上的图片等,最后解码转换为统一的
     * Bitmap格式即可.
     */
    private void addBitmapLayer()
    {
    	mDrawPadView.addBitmapLayer(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
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
//					operationLayer.setScale(scale);
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
					operationLayer.setAlphaPercent(value);
				}
			break;
			
			case R.id.id_DrawPad_skbar_background:
				if(operationLayer!=null){
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
