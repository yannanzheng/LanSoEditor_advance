package com.example.advanceDemo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;

import com.lansoeditor.demo.R;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.onDrawPadOutFrameListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.videoeditor.DrawPadNOView;
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

public class DrawPadNOViewDemoActivity extends Activity implements OnSeekBarChangeListener {
    private static final String TAG = "DrawPadNOViewDemoActivity";

    private String mVideoPath;

    private DrawPadNOView mDrawPadView;
    
    private VPlayer mplayer=null;
    private VideoLayer  videoMainLayer=null;
    private VideoLayer operationLayer=null;
    
    
    private String editTmpPath=null;
    private String dstPath=null;
    private LinearLayout  playVideo;
    private MediaInfo mInfo=null;
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
    	
        mDrawPadView = new DrawPadNOView(getApplicationContext());
   
        playVideo=(LinearLayout)findViewById(R.id.id_DrawPad_saveplay);
        playVideo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(SDKFileUtils.fileExist(dstPath)){
		   			 	Intent intent=new Intent(DrawPadNOViewDemoActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
		   		 }else{
		   			 Toast.makeText(DrawPadNOViewDemoActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
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
    		 		mplayer=new VPlayer(DrawPadNOViewDemoActivity.this);
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
    private void toastStop()
    {
    	Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
    	Log.i(TAG,"录制已停止!!");
    }
    /**
     * Step1:  init DrawPad 初始化
     * @param mp
     */
    private void initDrawPad()
    {
        	int padWidth=480;
        	int padHeight=480;
        	
        	//设置使能 实时录制, 即把正在DrawPad中呈现的画面实时的保存下来,实现所见即所得的模式
        	mDrawPadView.setRealEncodeEnable(padWidth,padHeight,1000000,(int)mInfo.vFrameRate,editTmpPath);
            mDrawPadView.setDrawpadSizeDirect(padWidth, padHeight);
            
        	mDrawPadView.setOnDrawPadProgressListener(new onDrawPadProgressListener() {
				
				@Override
				public void onProgress(DrawPad v, long currentTimeUs) {
					// TODO Auto-generated method stub
					
				}
			});
        	
//        	mDrawPadView.setOutFrameInDrawPad(true);
//        	mDrawPadView.setOnDrawPadOutFrameListener(480, 480, 1, new onDrawPadOutFrameListener() {
//				
//				@Override
//				public void onDrawPadOutFrame(DrawPad v, Object obj, int type, long ptsUs) {
//					// TODO Auto-generated method stub
//					Bitmap  bmp=(Bitmap)obj;
//					Log.i(TAG,"out NOOOOOOOOOOOOOOOOOOOframe listener  bmp is:"+bmp.getWidth()+" x "+bmp.getHeight()+ "ptsUs:"+ptsUs);
//				}
//			});
        	
        	
        	
        	startDrawPad();
    }
    /**
     * Step2: 开始运行 Drawpad
     */
    private void startDrawPad()
    {
    	mDrawPadView.setUpdateMode(DrawPadUpdateMode.AUTO_FLUSH, 25);
    	// 开始DrawPad的渲染线程. 
		mDrawPadView.startDrawPad();
		
		
		//如果视频太单调了, 可以给视频增加一个背景图片
		mDrawPadView.addBitmapLayer(BitmapFactory.decodeResource(getResources(), R.drawable.videobg));
		
		//增加一个主视频的 VideoLayer
		operationLayer=videoMainLayer=mDrawPadView.addMainVideoLayer(mplayer.getVideoWidth(),mplayer.getVideoHeight(),new GPUImageSepiaFilter());
		
		if(videoMainLayer!=null)
		{
			mplayer.setSurface(new Surface(videoMainLayer.getVideoTexture()));
			videoMainLayer.setScale(0.8f);  //把视频缩小一些, 因为外面有背景.
//			videoMainLayer.setBackgroundBlurFactor(1.5f);
		}
	
		mplayer.start();
	
		addBitmapLayer();
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
    	mDrawPadView.addBitmapLayer(bmp);
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
						 if(xpos>mDrawPadView.getDrawPadWidth())
							 xpos=0;
						 operationLayer.setPosition(xpos, operationLayer.getPositionY());
					}
				break;	
			case R.id.id_DrawPad_skbar_moveY:
				if(operationLayer!=null){
					 ypos+=10;
					 if(ypos>mDrawPadView.getDrawPadHeight())
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
					float value=(float)progress/100;
					operationLayer.setBackgroundBlurFactor(value);
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
