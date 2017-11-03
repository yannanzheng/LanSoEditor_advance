package com.example.advanceDemo.camera;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



import com.example.advanceDemo.VideoPlayerActivity;
import com.example.advanceDemo.view.ShowHeart;
import com.example.advanceDemo.view.VideoFocusView;
import com.example.advanceDemo.view.VideoProgressView;
import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.CameraLayer;
import com.lansosdk.box.CanvasRunnable;
import com.lansosdk.box.CanvasLayer;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.LanSoEditorBox;
import com.lansosdk.box.Layer;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.MVLayer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.DrawPadCameraView;
import com.lansosdk.videoeditor.DrawPadView;
import com.lansosdk.videoeditor.LanSongUtil;
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
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
/**
 * 
 */
public class CameraLayerEXTPcmActivity extends Activity implements OnClickListener{
   
	private static final String TAG = "CameraLayerEXTPcmActivity";

    private DrawPadCameraView mDrawPadCamera=null;
    private CameraLayer  mCameraLayer=null;
    private String videoPath=null;
    
    
	VideoFocusView focusView;
	private PowerManager.WakeLock mWakeLock;
	private Context mContext=null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cameralayer_demo_layout);
        
        if(LanSongUtil.checkRecordPermission(getBaseContext())==false){
        	   Toast.makeText(getApplicationContext(), "当前无权限,请打开权限后,重试!!!", Toast.LENGTH_LONG).show();
        	   finish();
          }
        
        mDrawPadCamera = (DrawPadCameraView) findViewById(R.id.id_cameralayer_padview);
        
        mContext=getApplicationContext();
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
		}, 200);
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
    /**
     * Step1: 开始运行 DrawPad 容器
     */
    private void initDrawPad()
    {
    	 int padWidth=480;
    	 int padHeight=480;
    	 
    	 /**
    	  * 设置使能 实时录制, 即把正在DrawPad中呈现的画面实时的保存下来,实现所见即所得的模式
    	  */
    	 mDrawPadCamera.setRealEncodeEnable(padWidth,padHeight,1000000,(int)25,videoPath);
    	/**
    	 * 设置进度监听
    	 */
    	mDrawPadCamera.setOnDrawPadProgressListener(new onDrawPadProgressListener() {
			
			@Override
			public void onProgress(DrawPad v, long currentTimeUs) {
				if(tvTime!=null){
					float leftF=((float)currentTimeUs/1000000);
					 float b   =  (float)(Math.round(leftF*10))/10;  //保留一位小数.
					tvTime.setText(String.valueOf(b));
				}
			}
		});
    	
    	mDrawPadCamera.setOnDrawPadCompletedListener(new onDrawPadCompletedListener() {
			
			@Override
			public void onCompleted(DrawPad v) {
				stopDrawPad();
			}
		});
    	/**
    	 * 设置当前DrawPad的宽度和高度,并把宽度自动缩放到父view的宽度,然后等比例调整高度.
    	 * 如果您已经在布局中固定了宽高, 则可以直接 startDrawPad
    	 */
    	mDrawPadCamera.setDrawPadSize(padWidth,padHeight,new onDrawPadSizeChangedListener() {
	    			
	    			@Override
	    			public void onSizeChanged(int viewWidth, int viewHeight) {
	    				startDrawPad();
	    			}
	    });	
    }
    /**
     * step2:  start drawpad
     */
    private void startDrawPad()
    {
		if(mDrawPadCamera.setupDrawpad())  //建立容器线程
		{
			mCameraLayer=	mDrawPadCamera.getCameraLayer();
			mDrawPadCamera.startPreview();  //开始预览
		}
    }
  /**
   * Step3: 停止容器 ,停止后,为新的视频文件增加上音频部分.
   */
    private void stopDrawPad()
    {
    	if(mCameraLayer!=null)
    	{
	    		/**
	    		 * 注意,当前是拿到音频文件后, 再和视频合成的, 发布版本这里不再这样!!!
	    		 */
    			mDrawPadCamera.stopDrawPad2(); 
				mCameraLayer=null;
				playVideo.setVisibility(View.VISIBLE);
		}
    }
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	if(mDrawPadCamera!=null){
    		mDrawPadCamera.stopDrawPad();
    	}
    	 if (mWakeLock != null) {
    			mWakeLock.release();
    			mWakeLock = null;
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
   private LinearLayout playVideo;
   private void initView()
   {
	   tvTime=(TextView)findViewById(R.id.id_cameralayer_timetv);
	   playVideo=(LinearLayout)findViewById(R.id.id_cameralayer_saveplay);
	   playVideo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(SDKFileUtils.fileExist(videoPath))
				 {
					  if(mDrawPadCamera!=null&& videoPath!=null){
							Intent intent=new Intent(CameraLayerEXTPcmActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", videoPath);
			    	    	startActivity(intent);
					  }
		   		 }else{
		   			 Toast.makeText(CameraLayerEXTPcmActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
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
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.id_cameralayer_frontcamera:
				if(CameraLayer.isSupportFrontCamera()){
					mCameraLayer.changeCamera();	
				}
				break;
			case R.id.id_camerape_demo_selectbtn:
				if(mDrawPadCamera!=null){
						
						if(mDrawPadCamera.isRecording()){
							stopDrawPad();
							btnRecord.setText("录制已结束");
						}else{
							btnRecord.setText("停止录制");
							
							String music=CopyFileFromAssets.copyAssets(getApplicationContext(), "wenbie_5m_2s.mp3");
							mDrawPadCamera.setRecordExtraMp3(music, true);
							
							mDrawPadCamera.startRecord();
						}
				}
				break;
		default:
			break;
		}
	}
	
}
