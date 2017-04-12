package com.example.advanceDemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



import com.example.advanceDemo.view.DrawPadView;
import com.example.advanceDemo.view.ShowHeart;
import com.example.advanceDemo.view.VideoFocusView;
import com.example.advanceDemo.view.VideoProgressView;
import com.example.advanceDemo.view.DrawPadView.onViewAvailable;
import com.lansoeditor.demo.R;
import com.lansosdk.box.AudioLine;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.CameraLayer;
import com.lansosdk.box.CanvasRunnable;
import com.lansosdk.box.CanvasLayer;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.LanSoEditorBox;
import com.lansosdk.box.Layer;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.MVLayer;
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
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
/**
 * 用来测试录制CameraLayer的同时, 录制声音, 
 * 或者让用户push进声音.
 * @author Administrator
 * 
 * 暂时不列到界面上.
 *
 */
public class CameraRecordDemoActivity extends Activity implements OnClickListener{
   
	private static final String TAG = "CameraRecordDemoActivity";

    private DrawPadView mDrawPadView=null;
    private PcmPlayer  mPcmPlayer=null;
    private CameraLayer  mCameraLayer=null;
    private String videoPath=null;
    private String audioPath=null;
    
    
	VideoFocusView focusView;
	private PowerManager.WakeLock mWakeLock;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cameralayer_demo_layout);
        
        if(LanSoEditorBox.checkCameraPermission(getBaseContext())==false){
     	   Toast.makeText(getApplicationContext(), "请打开权限后,重试!!!", Toast.LENGTH_LONG).show();
     	   finish();
        }
        
        mDrawPadView = (DrawPadView) findViewById(R.id.id_cameralayer_padview);
        
       
        initView();

        //在手机的默认路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
        videoPath=SDKFileUtils.newMp4PathInBox();
        
		new Handler().postDelayed(new Runnable() {
					
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//showHintDialog();
				initDrawPad();
			}
		}, 500);

    }
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	if (mWakeLock == null) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
			mWakeLock.acquire();
		}
    }
    
    //Step1: 开始运行 DrawPad 画板
    private void initDrawPad()
    {
    	//设置使能 实时录制, 即把正在DrawPad中呈现的画面实时的保存下来,实现所见即所得的模式
    	 int padWidth=480;
    	 int padHeight=480;
    	 
    	mDrawPadView.setRealEncodeEnable(padWidth,padHeight,1000000,(int)25,videoPath);
    	
    	mDrawPadView.setUpdateMode(DrawPadUpdateMode.AUTO_FLUSH, 25);

    	/**
    	 * 设置进度监听
    	 */
    	mDrawPadView.setOnDrawPadProgressListener(drawPadProgressListener);
    	//设置当前DrawPad的宽度和高度,并把宽度自动缩放到父view的宽度,然后等比例调整高度.
    	mDrawPadView.setDrawPadSize(padWidth,padHeight,new onDrawPadSizeChangedListener() {
	    			
	    			@Override
	    			public void onSizeChanged(int viewWidth, int viewHeight) {
	    				// TODO Auto-generated method stub
	    				startDrawPad();
	    			}
	    });	
    }
    //step2:  start drawpad
    private void startDrawPad()
    {
    	mDrawPadView.pauseDrawPadRecord();
		/**
		 * 如采用外面的pcm数据,则视频在录制过程中,会参考音频时间戳,来计算得出视频的时间戳,
		 * 如外界音频播放完毕,无数据push,应及时stopDrawPad 
		 */
//		mDrawPadView.setRecordExtraPcm(true,2,44100,64000);
		mDrawPadView.startDrawPad();
		
		//画板开始后, 获取一个AudioLine对象, 向里面投递数据.这里开始播放声音.获取声音的数据,投递进去.
		AudioLine line=mDrawPadView.getAudioLine();
		
		
		mPcmPlayer=new PcmPlayer("/sdcard/niu_44100_2.pcm",2,44100, line);
		mPcmPlayer.prepare();
		
		//增加一个CameraLayer
		mCameraLayer=	mDrawPadView.addCameraLayer(false,null);
		if(mCameraLayer!=null){
			mCameraLayer.startPreview();
			doAutoFocus(); //摄像头打开后,开始自动聚焦.
		}
    }
  //Step3: 停止画板 ,停止后,为新的视频文件增加上音频部分.
    private void stopDrawPad()
    {
    	if(mDrawPadView!=null && mDrawPadView.isRunning())
    	{
    			audioPath=mDrawPadView.stopDrawPad2();
				mCameraLayer=null;
				playVideo.setVisibility(View.VISIBLE);
		}
    }
    
    private onDrawPadProgressListener drawPadProgressListener=new onDrawPadProgressListener() {
		
		@Override
		public void onProgress(DrawPad v, long currentTimeUs) {
			// TODO Auto-generated method stub
			
			if(tvTime!=null){
				float leftF=((float)currentTimeUs/1000000);
				 float b   =  (float)(Math.round(leftF*10))/10;  //保留一位小数.
				tvTime.setText(String.valueOf(b));
			}
		}
	};
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	if(mDrawPadView!=null){
    		mDrawPadView.stopDrawPad();
    	}
    	 if (mWakeLock != null) {
    			mWakeLock.release();
    			mWakeLock = null;
    	}
    	 if(mPcmPlayer!=null){
    		 mPcmPlayer.release();
    		 mPcmPlayer=null;
    	 }
    }
   @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
			super.onDestroy();
			
		    if(SDKFileUtils.fileExist(videoPath)){
		    	SDKFileUtils.deleteFile(videoPath);
		    }
	}
   //-------------------------------------------一下是UI界面和控制部分.---------------------------------------------------
   private TextView tvTime;
   private Button playVideo;
   private void initView()
   {
	   tvTime=(TextView)findViewById(R.id.id_cameralayer_timetv);
	   playVideo=(Button)findViewById(R.id.id_cameralayer_saveplay);
	   playVideo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(SDKFileUtils.fileExist(videoPath))
				 {
					  if(mDrawPadView!=null){
						   
						   VideoEditor editor=new VideoEditor();
						   String testPath=SDKFileUtils.createMp4FileInBox();
						   editor.executeVideoMergeAudio(videoPath, audioPath, testPath);
						   
							Intent intent=new Intent(CameraRecordDemoActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", testPath);
			    	    	startActivity(intent);
					  }
		   		 }else{
		   			 Toast.makeText(CameraRecordDemoActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
		   		 }
			}
		});
	   playVideo.setVisibility(View.GONE);

   		findViewById(R.id.id_cameralayer_flashlight).setOnClickListener(this);
		findViewById(R.id.id_cameralayer_frontcamera).setOnClickListener(this);
		findViewById(R.id.id_camerape_demo_selectbtn).setOnClickListener(this);
		
	    btnRecord=(Button)findViewById(R.id.id_camerape_demo_selectbtn);
	    btnRecord.setText("开始录制");
   }
   Button btnRecord;
	private boolean mAllowTouchFocus = true;
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.id_cameralayer_frontcamera:
			//	mCameraLayer.changeCamera();
				break;
			case R.id.id_camerape_demo_selectbtn:
				if(mDrawPadView!=null){
						
						if(mDrawPadView.isRecording()){
							stopDrawPad();
							if(mPcmPlayer!=null){
								mPcmPlayer.stop();
							}
							btnRecord.setText("录制已结束");
						}else{
							btnRecord.setText("停止录制");
							mDrawPadView.resumeDrawPadRecord();
						}
				}
				mDrawPadView.resumeDrawPadRecord();
				break;
		default:
			break;
		}
	}
	private void doAutoFocus() {
//		boolean con = mCameraLayer.supportFocus() && mCameraLayer.isPreviewing();
//		if (con) {
//			if (mAllowTouchFocus && focusView != null && focusView.getWidth() > 0) {
//				mAllowTouchFocus = false;
//				int w = focusView.getWidth();
//				Rect rect = doTouchFocus(w / 2, w / 2);
//				if (rect != null) {
//					focusView.setHaveTouch(true, rect);
//					focusFinishTime(1000);  //1秒后关闭聚焦动画
//				}
//			}
//		}
	}
	private Rect doTouchFocus(float x, float y) {
		int w = mDrawPadView.getWidth();
		int h = mDrawPadView.getHeight();
		int left = 0;
		int top = 0;
		if (x - VideoFocusView.FOCUS_IMG_WH / 2 <= 0) {
			left = 0;
		} else if (x + VideoFocusView.FOCUS_IMG_WH / 2 >= w) {
			left = w - VideoFocusView.FOCUS_IMG_WH;
		} else {
			left = (int) (x - VideoFocusView.FOCUS_IMG_WH / 2);
		}
		if (y - VideoFocusView.FOCUS_IMG_WH / 2 <= 0) {
			top = 0;
		} else if (y + VideoFocusView.FOCUS_IMG_WH / 2 >= w) {
			top = w - VideoFocusView.FOCUS_IMG_WH;
		} else {
			top = (int) (y - VideoFocusView.FOCUS_IMG_WH / 2);
		}
		Rect rect = new Rect(left, top, left + VideoFocusView.FOCUS_IMG_WH, top + VideoFocusView.FOCUS_IMG_WH);
		
		Rect targetFocusRect = new Rect(rect.left * 2000 / w - 1000, rect.top * 2000 / h - 1000, rect.right * 2000 / w - 1000, rect.bottom * 2000 / h - 1000);
		try {
			List<Camera.Area> focusList = new ArrayList<Camera.Area>();
			Area focusA = new Area(targetFocusRect, 1000);
			focusList.add(focusA);
			//mCameraLayer.doFocus(focusList);
			return rect;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private void focusFinishTime(int delayMS)
	{
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (focusView != null) {
					focusView.setHaveTouch(false, new Rect(0, 0, 0, 0));
					mAllowTouchFocus = true;
				}
			}
		}, delayMS);
		
	}
}
