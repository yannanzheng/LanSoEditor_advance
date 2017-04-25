package com.example.advanceDemo;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;


import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IF1977Filter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFAmaroFilter;

import com.example.advanceDemo.GPUImageFilterTools.FilterAdjuster;
import com.example.advanceDemo.GPUImageFilterTools.OnGpuImageFilterChosenListener;
import com.example.advanceDemo.view.DrawPadView;
import com.lansoeditor.demo.R;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.DrawPadVideoExecute;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.Layer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.box.onDrawPadSnapShotListener;
import com.lansosdk.videoeditor.CopyFileFromAssets;
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
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * 演示滤镜模块的 VideoLayer的使用, 可以在播放过程中切换滤镜,
 * 在滤镜处理过程中, 增加其他的Layer,比如增加一个BitmapLayer和 ViewLayer等等.
 * 
 * 流程: 从layout中得到DrawPadView,并从DrawPadView中增加多个Layer,
 * 并对Layer进行滤镜, 缩放等操作.
 *
 *
 */
public class FilterDemoRealTimeActivity extends Activity {
    private static final String TAG = "VideoActivity";

    private String mVideoPath;

    private DrawPadView mDrawPadView;
    
    private MediaPlayer mplayer=null;
    
    private VideoLayer  filterLayer=null;
    
    private SeekBar skbarFilterAdjuster;
    /**
     * 在进行视频滤镜的过程中, 实时保存的视频画面文件的临时路径.
     */
    private String editTmpPath=null;
    /**
     * 滤镜处理后, 增加上原来音频文件的最终路径.
     */
    private String dstPath=null;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_layer_demo_layout);
        
        
        mVideoPath = getIntent().getStringExtra("videopath");
        mDrawPadView = (DrawPadView) findViewById(R.id.id_filterLayer_demo_view);
        skbarFilterAdjuster=(SeekBar)findViewById(R.id.id_filterLayer_demo_seek1);
        skbarFilterAdjuster.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				  if (mFilterAdjuster != null) {
			            mFilterAdjuster.adjust(progress);
			        }
			}
		});
        
        
        skbarFilterAdjuster.setMax(100);
        findViewById(R.id.id_filterLayer_demo_selectbtn).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				selectFilter();
				
				mDrawPadView.toggleSnatShot();
				
				/**
				 * 如果您需要演示, 在按下按钮后, 放到后台执行, 则屏蔽上一句,打开这里就可以了. 
				 if(mplayer!=null){
					mplayer.stop();
					mplayer.release();
					mplayer=null;
				}
				mDrawPadView.stopDrawPad();
				testDrawPadExecute();
				 */
				
			}
		});
        
        
        findViewById(R.id.id_filterdemo_saveplay).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(SDKFileUtils.fileExist(dstPath)){
					Intent intent=new Intent(FilterDemoRealTimeActivity.this,VideoPlayerActivity.class);
		    	    intent.putExtra("videopath", dstPath);
		    	    startActivity(intent);
		   		 }else{
		   			 Toast.makeText(FilterDemoRealTimeActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
		   		 }
			}
		});

        findViewById(R.id.id_filterdemo_saveplay).setVisibility(View.GONE);
        
        
        //在手机的默认路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
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
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	if(mplayer!=null){
			mplayer.stop();
			mplayer.release();
			mplayer=null;
		}
    }
    private FilterAdjuster mFilterAdjuster;
    
    /**
     * 选择滤镜效果, 
     */
    private void selectFilter()
    {
    	GPUImageFilterTools.showDialog(this, new OnGpuImageFilterChosenListener() {

            @Override
            public void onGpuImageFilterChosenListener(final GPUImageFilter filter) {
            	
            	//在这里通过DrawPad线程去切换 filterLayer的滤镜
	         	   if(mDrawPadView.switchFilterTo(filterLayer,filter)){  //<-----在这里切换滤镜.
	         		   mFilterAdjuster = new FilterAdjuster(filter);
	
	         		   //如果这个滤镜 可调, 显示可调节进度条.
	         		    findViewById(R.id.id_filterLayer_demo_seek1).setVisibility(
	         		            mFilterAdjuster.canAdjust() ? View.VISIBLE : View.GONE);
	         	   }
            }
        });
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
					initDrawPad();
				}
			});
        	  mplayer.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					// TODO Auto-generated method stub
					if(mDrawPadView!=null && mDrawPadView.isRunning()){
						mDrawPadView.stopDrawPad();
						findViewById(R.id.id_filterdemo_saveplay).setVisibility(View.VISIBLE);
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
    private int pngCnt=0;
    //Step1:开始 DrawPad 画板
    private void initDrawPad()
    {
    	MediaInfo info=new MediaInfo(mVideoPath);
    	if(info.prepare())
    	{
    			mDrawPadView.setUpdateMode(DrawPadUpdateMode.ALL_VIDEO_READY,25);
        	
    			
    			//您可以截图当前DrawPad中的内容. 这里仅仅测试
    			mDrawPadView.setOnDrawPadSnapShotListener(new onDrawPadSnapShotListener() {
					
					@Override
					public void onSnapShot(DrawPad v, Bitmap bmp) {
						// TODO Auto-generated method stub
						
						
//						String str="SnapShot"+ pngCnt +".png";
//				 		 pngCnt++;
//						  File f = new File("/sdcard/", str);
//						  if (f.exists()) {
//							  f.delete();
//						  }
//						  try {
//							   FileOutputStream out = new FileOutputStream(f);
//							   bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
//							   out.flush();
//							   out.close();
//						   Log.i(TAG, "已经保存到"+f.getPath());
//						  } catch (FileNotFoundException e) {
//						   // TODO Auto-generated catch block
//							  e.printStackTrace();
//						  } catch (IOException e) {
//						   // TODO Auto-generated catch block
//							  e.printStackTrace();
//						  }
					}
				});
    			
//        		设置使能 实时保存, 即把正在DrawPad中呈现的画面实时的保存下来,实现所见即所得的模式
        	mDrawPadView.setRealEncodeEnable(480,480,1000000,(int)info.vFrameRate,editTmpPath);
        	
        	mDrawPadView.setOnDrawPadProgressListener(new DrawPadProgressListener());
        	mDrawPadView.setOnDrawPadCompletedListener(new DrawPadCompleted());
        	//设置当前DrawPad的宽度和高度,并把宽度自动缩放到父view的宽度,然后等比例调整高度.
        	mDrawPadView.setDrawPadSize(480,480,new onDrawPadSizeChangedListener() {
    			
    			@Override
    			public void onSizeChanged(int viewWidth, int viewHeight) {
    				// TODO Auto-generated method stub
    				startDrawPad();
    			}
    		});
    	}
    }
    /**
     * Step2: startDrawPad
     */
    private void startDrawPad()
    {
    	mDrawPadView.startDrawPad();
		addVideoLayer();
    }
   
    private void addVideoLayer()
    {
    	/**
		 * 这里增加一个addVideoLayer, 并把设置滤镜效果为GPUImageSepiaFilter滤镜.
		 */
		filterLayer=mDrawPadView.addMainVideoLayer(mplayer.getVideoWidth(),mplayer.getVideoHeight(),
				new IF1977Filter(getBaseContext()));
    			
		if(filterLayer!=null){
			mplayer.setSurface(new Surface(filterLayer.getVideoTexture()));
			mplayer.start();
		}
    }
    /**
     * 您可以增加一个背景图片.
     */
    private void addBackgroundBitmap()
    {
    	  DisplayMetrics dm = new DisplayMetrics();// 获取屏幕密度（方法2）
	       dm = getResources().getDisplayMetrics();
	        
	           
	      int screenWidth  = dm.widthPixels;	
	      String picPath=null;   
	      if(screenWidth>=1080){
	    	  picPath=CopyFileFromAssets.copyAssets(getApplicationContext(), "pic1080x1080u2.jpg");
	      }else{
	    	  picPath=CopyFileFromAssets.copyAssets(getApplicationContext(),"pic720x720.jpg");
	      }
	      //先 增加第一张Bitmap的Layer, 因为是第一张,放在DrawPad中维护的数组的最下面, 认为是背景图片.
	      mDrawPadView.addBitmapLayer(BitmapFactory.decodeFile(picPath));
    }
    /**
     * DrawPad完成后的回调.
     * @author Administrator
     */
    private class DrawPadCompleted implements onDrawPadCompletedListener
    {

		@Override
		public void onCompleted(DrawPad v) {
			// TODO Auto-generated method stub
			if(isDestorying==false)
			{
					toastStop();
					if(SDKFileUtils.fileExist(editTmpPath)){
						boolean ret=VideoEditor.encoderAddAudio(mVideoPath,editTmpPath,SDKDir.TMP_DIR,dstPath);
						if(!ret){
							dstPath=editTmpPath;
						}else{
							SDKFileUtils.deleteFile(editTmpPath);	
						}
					}
			}
		}
    }
    //DrawPad进度回调.每一帧都返回一个回调.
    private class DrawPadProgressListener implements onDrawPadProgressListener
    {

		@Override
		public void onProgress(DrawPad v, long currentTimeUs) {
			// TODO Auto-generated method stub
				
		}
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
    //----------------------------------------------------------放到后台执行
    private boolean isExecuting=false; 
    private DrawPadVideoExecute  vDrawPad=null;
	private void testDrawPadExecute()
	{
		if(isExecuting)
			return ;
		
		isExecuting=true;
		
		vDrawPad=new DrawPadVideoExecute(FilterDemoRealTimeActivity.this,mVideoPath,480,480,1000000,
				new IFAmaroFilter(getApplicationContext()),editTmpPath);
		
		vDrawPad.setDrawPadProgressListener(new onDrawPadProgressListener() {
			
			@Override
			public void onProgress(DrawPad v, long currentTimeUs) {
				// TODO Auto-generated method stub
				Log.i(TAG,"drawpad  progress is:"+currentTimeUs);
			}
		});
		/**
		 * 设置DrawPad完成后的监听.
		 */
		vDrawPad.setDrawPadCompletedListener(new onDrawPadCompletedListener() {
			
			@Override
			public void onCompleted(DrawPad v) {
				// TODO Auto-generated method stub
				Log.i(TAG,"drawpad  Completed:"+ editTmpPath);
				
				if(SDKFileUtils.fileExist(editTmpPath)){
					boolean ret=VideoEditor.encoderAddAudio(mVideoPath,editTmpPath,SDKDir.TMP_DIR,dstPath);
					if(!ret){
						dstPath=editTmpPath;
					}else{
						SDKFileUtils.deleteFile(editTmpPath);	
					}
					findViewById(R.id.id_filterdemo_saveplay).setVisibility(View.VISIBLE);
				}else{
					Toast.makeText(getApplicationContext(), "录制文件不存在", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
		vDrawPad.startDrawPad();
	}

}
