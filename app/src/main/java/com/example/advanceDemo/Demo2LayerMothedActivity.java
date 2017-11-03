package com.example.advanceDemo;


import java.io.IOException;
import java.util.Locale;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageTransformFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBlackFilter;

import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.FileParameter;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.Layer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadOutFrameListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.videoeditor.DrawPadView;
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
import android.graphics.PointF;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
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
 * 
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
public class Demo2LayerMothedActivity extends Activity implements OnSeekBarChangeListener {
    private static final String TAG = "Demo2LayerMothedActivity";

    private String mVideoPath;

    private DrawPadView drawPadView;
    
    private MediaPlayer mplayer=null;
    
    private VideoLayer  videoLayer=null;
    
    private String editTmpPath=null;
    private String dstPath=null;
    private LinearLayout  playVideo;
    private MediaInfo  mInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo2_layer_layout);
        initView();
        
        mVideoPath = getIntent().getStringExtra("videopath");
        drawPadView = (DrawPadView) findViewById(R.id.id_mothed2_drawpadview);
        

        /**
         * 在手机的默认路径下创建一个文件名,
         * 用来保存生成的视频文件,(在onDestroy中删除)
         */
        editTmpPath=SDKFileUtils.newMp4PathInBox();
        dstPath=SDKFileUtils.newMp4PathInBox();
        
        new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
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
				e.printStackTrace();
			}
        	  mplayer.setOnPreparedListener(new OnPreparedListener() {
				
				@Override
				public void onPrepared(MediaPlayer mp) {
					initDrawPad(mp);
				}
			});
        	  mplayer.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
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
     */
    private void initDrawPad(MediaPlayer mp)
    {
    	mInfo=new MediaInfo(mVideoPath,false);
    	if(mInfo.prepare())
    	{
    		customDrawpadEncoder();
    		/**
    		 * 设置DrawPad的刷新模式,默认 {@link DrawPad.UpdateMode#ALL_VIDEO_READY};
    		 */
        	drawPadView.setUpdateMode(DrawPadUpdateMode.ALL_VIDEO_READY,25);
        		
        	/**
        	 * 设置使能 实时录制, 即把正在DrawPad中呈现的画面实时的保存下来,起到所见即所得的模式
        	 */
        	drawPadView.setRealEncodeEnable(480,480,1000000,(int)mInfo.vFrameRate,editTmpPath);
        	
        	/**
        	 * 设置当前DrawPad的宽度和高度,并把宽度自动缩放到父view的宽度,然后等比例调整高度.
        	 */
        	drawPadView.setDrawPadSize(480,480,new onDrawPadSizeChangedListener() {
    			
    			@Override
    			public void onSizeChanged(int viewWidth, int viewHeight) {
    				// TODO Auto-generated method stub
    				startDrawPad();
    			}
    		});
        	

        	drawPadView.setOnDrawPadProgressListener(new onDrawPadProgressListener() {
				
				@Override
				public void onProgress(DrawPad v, long currentTimeUs) {
					
				}
			});
    	}
    }
    private BitmapLayer bitmapLayer;
    /**
     * Step2:  start DrawPad 开始运行这个容器.
     */
    private void startDrawPad()
    {
    	/**
    	 *  开始DrawPad的渲染线程. 
    	 */
		if(drawPadView.startDrawPad())
		{
			/**
			 * 增加一个背景, 用来说明裁剪掉的一部分是透明的
			 */
			BitmapLayer layer=drawPadView.addBitmapLayer(BitmapFactory.decodeResource(getResources(), R.drawable.videobg));
			layer.setScaledValue(layer.getPadWidth(), layer.getPadHeight());  //填充整个屏幕.
			
//			/**
//			 * 增加一个主视频的 VideoLayer
//			 */
//			FileParameter  param=new FileParameter();
//			if(param.setDataSoure(mVideoPath)){
//				/**
//				 * 设置当前需要显示的区域 ,以左上角为0,0坐标. 
//				 * 
//				 * @param startX  开始的X坐标, 即从宽度的什么位置开始
//				 * @param startY  开始的Y坐标, 即从高度的什么位置开始
//				 * @param cropW   需要显示的宽度
//				 * @param cropH   需要显示的高度.
//				 */
//				param.setShowRect(0, 0, 1080, 800);
//				mVideoLayer=mDrawPadView.addMainVideoLayer(param,null);
//				mVideoLayer.setImageRenderLast(false);
//			}
			videoLayer=drawPadView.addMainVideoLayer(mplayer.getVideoWidth(),mplayer.getVideoHeight(),null);
			
			if(videoLayer!=null){
				mplayer.setSurface(new Surface(videoLayer.getVideoTexture()));
			}
			mplayer.start();
		}
    }
    /**
     * Step3 第三步: 停止运行DrawPad
     */
    private void stopDrawPad()
    {
    	if(drawPadView!=null && drawPadView.isRunning()){
			drawPadView.stopDrawPad();
			Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
			
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
    
    boolean isDestorying=false;  //是否正在销毁, 因为销毁会停止DrawPad
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    		isDestorying=true;
			if(mplayer!=null){
				mplayer.stop();
				mplayer.release();
				mplayer=null;
			}
			
			if(drawPadView!=null){
				drawPadView.stopDrawPad();
				drawPadView=null;        		   
			}
			if(SDKFileUtils.fileExist(dstPath)){
				SDKFileUtils.deleteFile(dstPath);
		    }
		    if(SDKFileUtils.fileExist(editTmpPath)){
		    	SDKFileUtils.deleteFile(editTmpPath);
		    }
		    
	}
    
 
    private void initView()
    {
    	 initSeekBar(R.id.id_mothed2_skbar_rectleft,100);
         initSeekBar(R.id.id_mothed2_skbar_rectround,100);
         initSeekBar(R.id.id_mothed2_skbar_rectxy,100); 
         initSeekBar(R.id.id_mothed2_skbar_circle,100);
         initSeekBar(R.id.id_mothed2_skbar_circle_center,100);
         playVideo=(LinearLayout)findViewById(R.id.id_mothed2_saveplay);
         playVideo.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				 if(SDKFileUtils.fileExist(dstPath)){
 		   			 	Intent intent=new Intent(Demo2LayerMothedActivity.this,VideoPlayerActivity.class);
 		   			 	intent.putExtra("videopath", dstPath);
 		   			 	startActivity(intent);
 		   		 }else{
 		   			 Toast.makeText(Demo2LayerMothedActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
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
    int RotateCnt=0;
    
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
    	
    	Layer  optionLayer=(Layer)videoLayer;  //为了测试不同图层对象,特意转换一下. 
    	
		switch (seekBar.getId()) {
         
			case R.id.id_mothed2_skbar_rectleft: //演示从左侧裁剪
				if(optionLayer!=null){
					float startX=(float)progress/100f;
					optionLayer.setVisibleRect(startX, 1.0f, 0.0f, 1.0f);
				}
				break;
			case R.id.id_mothed2_skbar_rectround:  
					if(optionLayer!=null){
						float endX=(float)progress/100f;
						float  half=endX/2.0f;
						optionLayer.setVisibleRect(0.5f-half, 0.5f+half, 0.0f, 1.0f);
					}
				break;	
			case R.id.id_mothed2_skbar_rectxy:  //演示宽度和高度同时缩放
				if(videoLayer!=null){
					float start=(float)progress/100f;
					
					float  end=(float)start+0.5f;
					/**
					 * 设置可见区域, 区域为四方形, 其他不可见为透明
					 * 四方形表示为: 从左到右是0.0f---1.0f;
					 * 从上到下 是: 0.0f---1.0f
					 * 
					 * @param startX   
					 * @param endX
					 * @param startY 
					 * @param endY
					 */
					optionLayer.setVisibleRect(start, end,start,end);
					/**
					 * 设置可见矩形四周的边框的宽度和颜色
					 * @param width  边框的宽度,最大是1.0, 最小是0.0, 推荐是0.01f
					 * @param r  RGBA分量中的Red  范围是0.0f---1.0f
					 * @param g  
					 * @param b
					 * @param a
					 */
					optionLayer.setVisibleRectBorder(0.0f, 1.0f, 0.0f, 0.0f, 1.0f);
				}
			break;
			case R.id.id_mothed2_skbar_circle:
				if(optionLayer!=null){
					float radius=(float)progress/100f;
					/**
					 * 画面以圆形裁剪 只显示画面圆形的某一个部分.
					 * 
					 * @param radius 圆的半径, 范围0--1.0f 
					 * @param center 圆的中心点位置, 范围0--1.0f;, 最上角为0,0,右下角为1,1;, 居中则是new PointF(0.5f,0.5f);
					 */
					optionLayer.setVisibleCircle(radius, new PointF(0.5f,0.5f));
					optionLayer.setVisibleCircleeBorder(0.01f, 1.0f, 0.0f, 0.0f, 1.0f);
				}
			break;		
			case R.id.id_mothed2_skbar_circle_center:
				if(optionLayer!=null){
					float xy=(float)progress/100f;
					xy/=2.0f;  
					//因为是半径, 如果显示画面的一半圆形, 则整个画面是1.0f,则半径0.5;如果显示一半则是0.25;
					optionLayer.setVisibleCircle(0.25f, new PointF(xy,xy));
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
	/**
	   * 定制LanSoSdk的DrawPad的编码参数, 
	   */
	  private  void customDrawpadEncoder()
	  {
		  /**
		   * 可以修改
		   * "video/avc":  是采用H264编码.
		   * mwidht, mHeight:  为视频的宽度和高度, 请在使用时, 建议采用设置到我们sdk中编码的宽度和高度. 
		   */
		  MediaFormat encodeFormat = MediaFormat.createVideoFormat("video/avc", 480, 480);
	        
		  /**
		   * 不能修改
		   * 我们当前sdk是采用surface格式为渲染窗口, 请务必这里要设置COLOR_FORMAT为FormatSurface;
		   */
		  encodeFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface); 
	       
		  /**
		   * 码率,您可以任意修改
		   */
		  encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE,1000*1000);
	        
	      /**
	       * 帧率  , 可以修改
	       */
		  encodeFormat.setInteger(MediaFormat.KEY_FRAME_RATE,25);
	      /**
	       * 几秒钟一个IDR帧. 可以修改  
	       */
		  encodeFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1);//这里表示相隔几秒钟产生一个I帧;设置为1表示1秒钟产生一个I帧.
	        
		  /**
		   * ................这里您可以增加别的参数, 比如profile. bitrate_mode等等.
		   *     encodeFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, EncoderCapabilities.BITRATE_MODE_CQ);
		   * ...................
		   */
		  
		  /**
		   *设置到cEc中.注意, 因为这是静态变量, 设置后, 如果您后期没用更正, 则drawpad一直会采用这个方法里的参数作为编码的参数.
		   *
		   * 如果您想使用我们drawpad的默认参数,请把DrawPad.cEc设置为null;
		   */
	       DrawPad.cEc=encodeFormat;  
	  }
}
