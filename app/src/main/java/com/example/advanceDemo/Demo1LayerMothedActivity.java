package com.example.advanceDemo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongTestCentorFilter;

import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapLayer;
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
 * 演示: 使用DrawPad来实现 视频和图片的实时叠加. 
 * 
 * 流程是: 
 * 先创建一个DrawPad,然后在视频播放过程中,从DrawPad中增加一个BitmapLayer,然后可以调节SeekBar来对Layer的每个
 * 参数进行调节.
 * 
 * 可以调节的有:平移,旋转,缩放,RGBA值,显示/不显示(闪烁)效果.
 * 实际使用中, 可用这些属性来做些动画,比如平移+RGBA调节,呈现舒缓移除的效果. 缓慢缩放呈现照片播放效果;旋转呈现欢快的炫酷效果等等.
 */

public class Demo1LayerMothedActivity extends Activity implements OnSeekBarChangeListener {
    private static final String TAG = "Demo1LayerMothedActivity";

    private String mVideoPath;

    private DrawPadView mDrawPadView;
    
    private VPlayer mplayer=null;
    private VideoLayer  mainVideoLayer=null;
    private BitmapLayer bitmapLayer=null;
    
    
    private String editTmpPath=null;
    private String dstPath=null;
    private LinearLayout  playVideo;
    private MediaInfo mInfo=null;
    private Button  btnTest;
    int RotateCnt=0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawpad_layout);
        
        mVideoPath = getIntent().getStringExtra("videopath");
        mInfo=new MediaInfo(mVideoPath,false);
    	if(mInfo.prepare()==false){
    		 Toast.makeText(this, "传递过来的视频文件错误", Toast.LENGTH_SHORT).show();
    		 this.finish();
    	}
    	
        mDrawPadView = (DrawPadView) findViewById(R.id.DrawPad_view);
        
        
        initView();
        
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
     * VideoLayer是外部提供画面来源, 
     * 您可以用你们自己的播放器作为画面输入源,
     * 也可以用原生的MediaPlayer,只需要视频播放器可以设置surface即可.
     * 
     * 一下举例是采用MediaPlayer作为视频输入源.
     */
    private void startPlayVideo()
    {
    	 if (mVideoPath != null){
    		 		mplayer=new VPlayer(Demo1LayerMothedActivity.this);
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
     * 第一步:  init DrawPad 初始化
     * @param mp
     */
    private void initDrawPad()
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
        	//设置当前DrawPad的宽度和高度,并把宽度自动缩放到父view的宽度,然后等比例调整高度.
    		mDrawPadView.setDrawPadSize(padWidth,padHeight,new onDrawPadSizeChangedListener() {
				@Override
				public void onSizeChanged(int viewWidth, int viewHeight) {
					// TODO Auto-generated method stub
					startDrawPad();
				}
    		});
    		
//    		/**
//        	 * 是否要实时获取图片
//        	 */
//    		mDrawPadView.setOutFrameInDrawPad(true);
//    		mDrawPadView.setOnDrawPadOutFrameListener(480, 480, 1, new onDrawPadOutFrameListener() {
//				
//				@Override
//				public void onDrawPadOutFrame(DrawPad v, Object obj, int type, long ptsUs) {
//					// TODO Auto-generated method stub
//					Bitmap  bmp=(Bitmap)obj;
//					Log.i(TAG,"out frame listener  bmp is:"+bmp.getWidth()+" x "+bmp.getHeight()+ "ptsUs:"+ptsUs);
//					testFrames.pushBitmap(bmp);
//				}
//			});
    }
    /**
     * Step2: 开始运行 Drawpad
     */
    private void startDrawPad() 
    {
    	// 开始DrawPad的渲染线程. 
		if(mDrawPadView.startDrawPad())
		{
			//如果视频太单调了, 可以给视频增加一个背景图片
			mDrawPadView.addBitmapLayer(BitmapFactory.decodeResource(getResources(), R.drawable.videobg));
			
			//增加一个主视频的 VideoLayer
			mainVideoLayer=mDrawPadView.addMainVideoLayer(mplayer.getVideoWidth(),mplayer.getVideoHeight(),null);
			
//			或者只显示画面的一部分, 如下配置.
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
//				param.setShowRect(0, 0, 300, 200);
//				//param.setStartTimeUs(5*1000*1000); //从5秒处开始播放, 当前仅在后台处理时有效.
//				operationLayer=videoMainLayer=mDrawPadView.addMainVideoLayer(param,new GPUImageSepiaFilter());
//			}
			
			if(mainVideoLayer!=null)
			{
				mplayer.setSurface(new Surface(mainVideoLayer.getVideoTexture()));
				mainVideoLayer.setScale(0.8f);  //把视频缩小一些, 因为外面有背景.
			}
		
			mplayer.start();
		
			addBitmapLayer();
//			addYUVLayer();
//			addGifLayer();
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
    		mDrawPadView.addGifLayer(R.drawable.g07);  
    	}
    }
    /**
     * 从DrawPad中得到一个BitmapLayer,填入要显示的图片,您实际可以是资源图片,也可以是png或jpg,或网络上的图片等,最后解码转换为统一的
     * Bitmap格式即可.
     */
    private void addBitmapLayer()
    {
    	Bitmap bmp=BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
    	bitmapLayer=mDrawPadView.addBitmapLayer(bmp);
    }
    private YUVLayer mYuvLayer=null;
    private YUVLayerDemoData mData;
    private int count=0;
    private void addYUVLayer()
    {
    	mYuvLayer=mDrawPadView.addYUVLayer(960, 720);
    	mData = readDataFromAssets("data.log");
    	mDrawPadView.setOnDrawPadThreadProgressListener(new onDrawPadThreadProgressListener() {
			
			@Override
			public void onThreadProgress(DrawPad v, long currentTimeUs) {
				// TODO Auto-generated method stub
				
				if(mYuvLayer!=null){
					/**
					 * 把外面的数据作为一个图层投递DrawPad中
					 * @param data  nv21格式的数据. 
					 * @param rotate  数据渲染到DrawPad中时,是否要旋转角度, 可旋转0/90/180/270
					 * @param flipHorizontal  数据是否要横向翻转, 把左边的放 右边,把右边的放左边.
					 * @param flipVertical  数据是否要竖向翻转, 把上面的放下面, 把下面的放上边.
					 */
					  count++;
					  if(count>200){
						//这里仅仅是演示把yuv push到画板里, 实际使用中, 你拿到的byte[]的yuv数据,可以直接push
						  mYuvLayer.pushNV21DataToTexture(mData.yuv,270,false,false);
					  }else if(count>150){
						  mYuvLayer.pushNV21DataToTexture(mData.yuv,180,false,false);
					  }else if(count>100){
						  mYuvLayer.pushNV21DataToTexture(mData.yuv,90,false,false);
					  }else{
						  mYuvLayer.pushNV21DataToTexture(mData.yuv,0,false,false);
					  }
				}
				
				
				
				
			}
		});
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
    private void initView()
    {
    
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
  		   			 	Intent intent=new Intent(Demo1LayerMothedActivity.this,VideoPlayerActivity.class);
  			    	    	intent.putExtra("videopath", dstPath);
  			    	    	startActivity(intent);
  		   		 }else{
  		   			 Toast.makeText(Demo1LayerMothedActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
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
     * 可以调节的有:平移,旋转,缩放,RGBA值,显示/不显示(闪烁)效果.
     */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		switch (seekBar.getId()) {
			case R.id.id_DrawPad_skbar_rotate:
				if(bitmapLayer!=null){
					bitmapLayer.setRotate(progress);
				}
				break;
			case R.id.id_DrawPad_skbar_moveX:
					if(bitmapLayer!=null){
						 xpos+=10;
						 if(xpos>mDrawPadView.getDrawPadWidth())
							 xpos=0;
						 bitmapLayer.setPosition(xpos, bitmapLayer.getPositionY());
					}
				break;	
			case R.id.id_DrawPad_skbar_moveY:
				if(bitmapLayer!=null){
					 ypos+=10;
					 if(ypos>mDrawPadView.getDrawPadHeight())
						 ypos=0;
					 
					 bitmapLayer.setPosition(bitmapLayer.getPositionX(), ypos);
				}
			break;				
			case R.id.id_DrawPad_skbar_scale:
				if(bitmapLayer!=null){
					float scale=(float)progress/100;
					int width=(int)(bitmapLayer.getLayerWidth() * scale);
					bitmapLayer.setScaledValue(bitmapLayer.getLayerWidth(), width);
				}
			break;		
			case R.id.id_DrawPad_skbar_brightness:
					if(bitmapLayer!=null){
						float value=(float)progress/100;
						//同时调节RGB的比例, 让他慢慢亮起来,或暗下去.
						bitmapLayer.setRedPercent(value);  
						bitmapLayer.setGreenPercent(value); 
						bitmapLayer.setBluePercent(value);  
						bitmapLayer.setAlphaPercent(value);
					}
				break;
			case R.id.id_DrawPad_skbar_alpha:
				if(bitmapLayer!=null){
					float value=(float)progress/100;
					bitmapLayer.setAlphaPercent(value);
				}
				break;
			case R.id.id_DrawPad_skbar_background:
				if(mainVideoLayer!=null){
					float value=(float)progress/100;
					mainVideoLayer.setBackgroundBlurFactor(value);
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

	public YUVLayerDemoData readDataFromAssets(String fileName) {
		int w = 960;
		int h = 720;
		byte[] data = new byte[w * h * 3 / 2];
		try {
			InputStream is = getAssets().open(fileName);
			is.read(data);
			is.close();

			return new YUVLayerDemoData(w, h, data);
		} catch (IOException e) {
			System.out.println("IoException:" + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	 private void toastStop()
	    {
	    	Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
	    	Log.i(TAG,"录制已停止!!");
	    }
}
