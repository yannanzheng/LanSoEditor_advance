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
import com.lansosdk.box.Layer;
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
 * 先创建一个DrawPad,然后在视频播放过程中,从DrawPad中增加一个BitmapLayer,然后可以调节SeekBar来对Layer的每个
 * 参数进行调节.
 * 
 * 可以调节的有:平移,旋转,缩放,RGBA值,显示/不显示(闪烁)效果.
 * 实际使用中, 可用这些属性来做些动画,比如平移+RGBA调节,呈现舒缓移除的效果. 缓慢缩放呈现照片播放效果;旋转呈现欢快的炫酷效果等等.
 */

public class VideoLayerRealTimeActivity extends Activity implements OnSeekBarChangeListener {
    private static final String TAG = "VideoPictureRealTimeActivity";

    private String mVideoPath;

    private DrawPadView mDrawPadView;
    
    private MediaPlayer mplayer=null;
    private MediaPlayer mplayer2=null;
    private Layer  mLayerMain=null;
    private BitmapLayer mBitmapLayer=null;
    
    
    private String editTmpPath=null;
    private String dstPath=null;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawpad_layout);
        
        mVideoPath = getIntent().getStringExtra("videopath");
        mDrawPadView = (DrawPadView) findViewById(R.id.DrawPad_view);
        
        initSeekBar(R.id.id_DrawPad_skbar_rotate,360); //角度是旋转360度,如果值大于360,则取360度内剩余的角度值.
        initSeekBar(R.id.id_DrawPad_skbar_move,100);   
        initSeekBar(R.id.id_DrawPad_skbar_scale,800);   //这里设置最大可放大8倍
        
        initSeekBar(R.id.id_DrawPad_skbar_red,100);  //red最大为100
        initSeekBar(R.id.id_DrawPad_skbar_green,100);
        initSeekBar(R.id.id_DrawPad_skbar_blue,100);
        initSeekBar(R.id.id_DrawPad_skbar_alpha,100);
        
        findViewById(R.id.id_DrawPad_saveplay).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(SDKFileUtils.fileExist(dstPath)){
		   			 	Intent intent=new Intent(VideoLayerRealTimeActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
		   		 }else{
		   			 Toast.makeText(VideoLayerRealTimeActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
		   		 }
			}
		});
        findViewById(R.id.id_DrawPad_saveplay).setVisibility(View.GONE);

        //在手机的/sdcard/lansongBox/路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
        editTmpPath=SDKFileUtils.newMp4PathInBox();
        dstPath=SDKFileUtils.newMp4PathInBox();
        
        //增加提示缩放到480的文字.
        DemoUtils.showScale480HintDialog(VideoLayerRealTimeActivity.this);
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
    	SeekBar   skbar=(SeekBar)findViewById(resId);
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
					start(mp);
				}
			});
        	  mplayer.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					// TODO Auto-generated method stub
					
					//completion不确定会在什么时候停，故需要判断是否为null
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
							
							findViewById(R.id.id_DrawPad_saveplay).setVisibility(View.VISIBLE);
						}else{
							Log.e(TAG," player completion, but file:"+editTmpPath+" is not exist!!!");
						}
					}
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
    private void start(MediaPlayer mp)
    {
    	
    		MediaInfo info=new MediaInfo(mVideoPath,false);
        	info.prepare();
        	
        		//设置使能 实时录制, 即把正在DrawPad中呈现的画面实时的保存下来,实现所见即所得的模式
        		mDrawPadView.setRealEncodeEnable(480,480,1000000,(int)info.vFrameRate,editTmpPath);

        		//设置当前DrawPad的宽度和高度,并把宽度自动缩放到父view的宽度,然后等比例调整高度.
    		mDrawPadView.setDrawPadSize(480,480,new onDrawPadSizeChangedListener() {
			
			@Override
			public void onSizeChanged(int viewWidth, int viewHeight) {
				// TODO Auto-generated method stub
				// 开始DrawPad的渲染线程. 
					mDrawPadView.startDrawPad(new onDrawPadProgressListener() {
					
					@Override
					public void onProgress(DrawPad v, long currentTimeUs) {
						// TODO Auto-generated method stub
						//用来测试把一个图层放到底层或外层.
						if(currentTimeUs>5*1000*1000 && mBitmapLayer!=null){
							mDrawPadView.bringLayerToFront(mBitmapLayer);
						
						}else if(currentTimeUs>3*1000*1000){
							mDrawPadView.bringLayerToBack(mBitmapLayer);
						}
					}
				},null);
					
					//如果视频太单调了, 可以给视频增加一个背景图片, 显得艺术一些.^^
					mDrawPadView.addBitmapLayer(BitmapFactory.decodeResource(getResources(), R.drawable.videobg));
					
				//增加一个主视频的 VideoLayer
				mLayerMain=mDrawPadView.addMainVideoLayer(mplayer.getVideoWidth(),mplayer.getVideoHeight(),null);
				if(mLayerMain!=null){
					mplayer.setSurface(new Surface(mLayerMain.getVideoTexture()));
					mLayerMain.setScale(0.8f);  //把视频缩小一些, 因为外面有背景.
				}
				
				mplayer.start();
				
				addBitmapLayer();
			}
		});
    }
    /**
     * 从DrawPad中得到一个BitmapLayer,填入要显示的图片,您实际可以是资源图片,也可以是png或jpg,或网络上的图片等,最后解码转换为统一的
     * Bitmap格式即可.
     */
    private void addBitmapLayer()
    {
    	mBitmapLayer=mDrawPadView.addBitmapLayer(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));

//    	mBitmapLayer=mDrawPadView.addBitmapLayer(BitmapFactory.decodeResource(getResources(), R.drawable.tt3));
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
				if(mBitmapLayer!=null){
					mBitmapLayer.setRotate(progress);
				}
				break;
			case R.id.id_DrawPad_skbar_move:
					if(mBitmapLayer!=null){
						 xpos+=10;
						 ypos+=10;
						 
						 if(xpos>mDrawPadView.getViewWidth())
							 xpos=0;
						 if(ypos>mDrawPadView.getViewWidth())
							 ypos=0;
						 mBitmapLayer.setPosition(xpos, ypos);
					}
				break;				
			case R.id.id_DrawPad_skbar_scale:
				if(mBitmapLayer!=null){
					float scale=(float)progress/100;
					mBitmapLayer.setScale(scale);
				}
			break;		
			case R.id.id_DrawPad_skbar_red:
					if(mBitmapLayer!=null){
						float value=(float)progress/100;
						mBitmapLayer.setRedPercent(value);  //设置每个RGBA的比例,默认是1
					}
				break;

			case R.id.id_DrawPad_skbar_green:
					if(mBitmapLayer!=null){
						float value=(float)progress/100;
						mBitmapLayer.setGreenPercent(value);
					}
				break;

			case R.id.id_DrawPad_skbar_blue:
					if(mBitmapLayer!=null){
						float value=(float)progress/100;
						mBitmapLayer.setBluePercent(value);
					}
				break;

			case R.id.id_DrawPad_skbar_alpha:
					if(mBitmapLayer!=null){
						float value=(float)progress/100;
						mBitmapLayer.setAlphaPercent(value);
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
