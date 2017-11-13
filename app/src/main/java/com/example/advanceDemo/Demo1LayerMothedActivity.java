package com.example.advanceDemo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import su.levenetc.android.textsurface.animations.Alpha;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongTestCentorFilter;

import com.lansoeditor.demo.R;
import com.lansosdk.box.AlphaAnimation;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.FileParameter;
import com.lansosdk.box.Layer;
import com.lansosdk.box.MoveAnimation;
import com.lansosdk.box.RotateAnimation;
import com.lansosdk.box.SubLayer;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.YUVLayer;
import com.lansosdk.box.onDrawPadOutFrameListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.box.onDrawPadThreadProgressListener;
import com.lansosdk.videoeditor.DrawPadView;
import com.lansosdk.videoeditor.DrawPadView.onViewAvailable;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    private String videoPath;

    private DrawPadView drawPadView;
    
    private MediaPlayer mplayer=null;
    private VideoLayer  mainVideoLayer=null;
    private BitmapLayer bitmapLayer=null;
    
    private String editTmpPath=null;
    private String dstPath=null;
    private LinearLayout  playVideo;
    private MediaInfo mInfo=null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawpad_layout);
        
        videoPath = getIntent().getStringExtra("videopath");
        mInfo=new MediaInfo(videoPath,false);
    	if(mInfo.prepare()==false){
    		 Toast.makeText(this, "传递过来的视频文件错误", Toast.LENGTH_SHORT).show();
    		 this.finish();
    	}
    	
        drawPadView = (DrawPadView) findViewById(R.id.DrawPad_view);
        initView();
        //在手机的默认路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
        editTmpPath=SDKFileUtils.newMp4PathInBox();
        dstPath=SDKFileUtils.newMp4PathInBox();
        
    }
    @Override
    protected void onResume() {
    	super.onResume();
    	new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				startPlayVideo();
			}
		}, 100);
    	playVideo.setVisibility(View.INVISIBLE);
    }
    private void startPlayVideo()
    {
    	 if (videoPath != null)
    	 {
		 		mplayer=new MediaPlayer();
		 		try {
					mplayer.setDataSource(videoPath);
					mplayer.setOnPreparedListener(new OnPreparedListener() {
						
						@Override
						public void onPrepared(MediaPlayer mp) {
							initDrawPad();
						}
					});
			 		mplayer.setOnCompletionListener(new OnCompletionListener() {
						
						@Override
						public void onCompletion(MediaPlayer mp) {
							stopDrawPad();
						}
					});
			 		mplayer.prepareAsync();
				}  catch (Exception e) {
					e.printStackTrace();
				}
         }else {
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
        	/**
        	 * 设置当前DrawPad的宽度和高度,并把宽度自动缩放到父view的宽度,然后等比例调整高度.
        	 */
    		drawPadView.setDrawPadSize(padWidth,padHeight,new onDrawPadSizeChangedListener() {
				@Override
				public void onSizeChanged(int viewWidth, int viewHeight) {
					startDrawPad();
				}
    		});
    		drawPadView.setRealEncodeEnable(padWidth,padHeight,1000000,(int)mInfo.vFrameRate,editTmpPath);
    		
        	drawPadView.setOnDrawPadProgressListener(new onDrawPadProgressListener() {
				
				@Override
				public void onProgress(DrawPad v, long currentTimeUs) {
					
				}
			});
    }
    /**
     * Step2: 开始运行 Drawpad
     */
    private void startDrawPad() 
    {
    	drawPadView.pauseDrawPad();
    	
		if(drawPadView.isRunning()==false && drawPadView.startDrawPad())
		{
			//给容器增加一个背景
			BitmapLayer layer=drawPadView.addBitmapLayer(BitmapFactory.decodeResource(getResources(), R.drawable.videobg));
			layer.setScaledValue(layer.getPadWidth(), layer.getPadHeight());  //填充整个容器
			
			mainVideoLayer=drawPadView.addMainVideoLayer(mplayer.getVideoWidth(),mplayer.getVideoHeight(),null);
			if(mainVideoLayer!=null)
			{
				mplayer.setSurface(new Surface(mainVideoLayer.getVideoTexture()));
			//	mainVideoLayer.setScale(0.8f);  //把视频缩小一些, 因为外面有背景.
			}
			mplayer.start();
			addBitmapLayer();
			drawPadView.resumeDrawPad();
		}
    }
    /**
     * Step3: stop DrawPad
     */
    private void stopDrawPad()
    {
    	if(drawPadView!=null && drawPadView.isRunning()){
			
			drawPadView.stopDrawPad();
			DemoUtil.showToast(getApplicationContext(), "录制已停止!!");
			
			if(SDKFileUtils.fileExist(editTmpPath)){
				boolean ret=VideoEditor.encoderAddAudio(videoPath,editTmpPath,SDKDir.TMP_DIR,dstPath);
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
    	if(drawPadView!=null && drawPadView.isRunning())
    	{
    		drawPadView.addGifLayer(R.drawable.g07);  
    	}
    }
    /**
     * 从DrawPad中得到一个BitmapLayer,填入要显示的图片,您实际可以是资源图片,也可以是png或jpg,或网络上的图片等,最后解码转换为统一的
     * Bitmap格式即可.
     */
    private void addBitmapLayer()
    {
    	if(bitmapLayer==null){
    		Bitmap bmp=BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        	bitmapLayer=drawPadView.addBitmapLayer(bmp);
    	}
    }
    private YUVLayer mYuvLayer=null;
    private YUVLayerDemoData mData;
    private int count=0;
    
    /**
     * 增加YUV图层.
     */
    private void addYUVLayer()
    {
    	mYuvLayer=drawPadView.addYUVLayer(960, 720);
    	mData = readDataFromAssets("data.log");
    	drawPadView.setOnDrawPadThreadProgressListener(new onDrawPadThreadProgressListener() {
			
			@Override
			public void onThreadProgress(DrawPad v, long currentTimeUs) {
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
						//这里仅仅是演示把yuv push到容器里, 实际使用中, 你拿到的byte[]的yuv数据,可以直接push
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
    	super.onPause();
    	if(mplayer!=null){
    		mplayer.stop();
    		mplayer.release();
    		mplayer=null;
    	}
    	if(drawPadView!=null){
    		drawPadView.stopDrawPad();
    	}
    }
   @Override
	protected void onDestroy() {
		super.onDestroy();
		SDKFileUtils.deleteFile(dstPath);
		SDKFileUtils.deleteFile(editTmpPath);
	}
    
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
    private float xpos=0,ypos=0;
    /**
     * 提示:实际使用中没有主次之分, 只要是继承自Layer的对象,都可以调节,这里仅仅是举例
     * 可以调节的有:平移,旋转,缩放,RGBA值,显示/不显示(闪烁)效果.
     */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		switch (seekBar.getId()) {
			case R.id.id_DrawPad_skbar_rotate:
				if(bitmapLayer!=null){
					bitmapLayer.setRotate(progress);
				}
				break;
			case R.id.id_DrawPad_skbar_moveX:
					if(bitmapLayer!=null){
						 xpos+=10;
						 if(xpos>drawPadView.getDrawPadWidth())
							 xpos=0;
						 bitmapLayer.setPosition(xpos, bitmapLayer.getPositionY());
					}
				break;	
			case R.id.id_DrawPad_skbar_moveY:
				if(bitmapLayer!=null){
					 ypos+=10;
					 if(ypos>drawPadView.getDrawPadHeight())
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
}
