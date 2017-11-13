package com.example.advanceDemo.cool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;



import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;

import com.example.advanceDemo.VideoPlayerActivity;
import com.example.advanceDemo.view.ImageTouchView;
import com.example.advanceDemo.view.PaintConstants;
import com.lansoeditor.demo.R;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.Layer;
import com.lansosdk.box.ViewLayerRelativeLayout;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.box.onDrawPadSnapShotListener;
import com.lansosdk.videoeditor.DrawPadView;
import com.lansosdk.videoeditor.LanSongUtil;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.plattysoft.leonids.ParticleSystem;
import com.plattysoft.leonids.modifiers.AlphaModifier;
import com.plattysoft.leonids.modifiers.ScaleModifier;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 *  演示:视频增加粒子效果
 *  
 */
public class ParticleDemoActivity extends Activity implements OnClickListener{
    private static final String TAG = "ViewLayerDemoActivity";

    private String mVideoPath;

    private DrawPadView drawPadView;
    
    private MediaPlayer mplayer=null;
    
    private VideoLayer  mainVideoLayer=null;
    private ViewLayer mViewLayer=null;
    
//    
    private String editTmpPath=null;  //用来保存容器录制的目标文件路径.
    private String dstPath=null;

    private ViewLayerRelativeLayout mLayerRelativeLayout;
    private MediaInfo  mInfo=null;
    RelativeLayout particleLayout;
    ParticleSystem ps;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.particel_demo_layout);
        
        
        mVideoPath = getIntent().getStringExtra("videopath");
        mInfo=new MediaInfo(mVideoPath,false);
        if(mInfo.prepare()==false){
            Log.e(TAG, " video path is error.finish\n");
            finish();
        }
        
        drawPadView = (DrawPadView) findViewById(R.id.id_particle_drawpadview);
        
        mLayerRelativeLayout=(ViewLayerRelativeLayout)findViewById(R.id.id_particle_viewlayerlayout);
      
        particleLayout=(RelativeLayout)findViewById(R.id.id_particle_layout);
        
    	
        initView();
        
        //在手机的默认路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
        editTmpPath=SDKFileUtils.newMp4PathInBox();
        dstPath=SDKFileUtils.newMp4PathInBox();
	    
        new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				 startPlayVideo();
			}
		},500);
    }
    private void startPlayVideo()
    {
        	  mplayer=new MediaPlayer();
        	  try {
				mplayer.setDataSource(mVideoPath);
				
			}  catch (IOException e) {
				e.printStackTrace();
			}
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
    }
    
    long lastTimeUs=0;
    /**
     * Step1: 设置DrawPad 容器的尺寸.
     * 并设置是否实时录制容器上的内容.
     */
    private void initDrawPad()
    {
    	MediaInfo info=new MediaInfo(mVideoPath,false);
    	if(info.prepare())
    	{
    		
    		drawPadView.setRealEncodeEnable(480,480,1000000,(int)info.vFrameRate,editTmpPath);
        	drawPadView.setDrawPadSize(480,480,new onDrawPadSizeChangedListener() {
    			
    			@Override
    			public void onSizeChanged(int viewWidth, int viewHeight) {
    				startDrawPad();
    			}
    		});
    	}
    }
    /**
     * Step2: Drawpad设置好后, 开始容器线程运行,
     * 并增加一个视频图层和 view图层.
     */
    private void startDrawPad()
    {
    	if(drawPadView.startDrawPad())
    	{
    		mainVideoLayer=drawPadView.addMainVideoLayer(mplayer.getVideoWidth(),mplayer.getVideoHeight(),null);
    		if(mainVideoLayer!=null){
    			mplayer.setSurface(new Surface(mainVideoLayer.getVideoTexture()));
    		}
    		mplayer.start();
    		addViewLayer();	
    	}
    }
    /**
     * Step3: 做好后, 停止容器, 因为容器里没有声音, 这里增加上原来的声音.
     */
    private void stopDrawPad()
    {
    	if(drawPadView!=null && drawPadView.isRunning()){
			drawPadView.stopDrawPad();
			
			toastStop();
			
			if(SDKFileUtils.fileExist(editTmpPath))
			{
				boolean ret=VideoEditor.encoderAddAudio(mVideoPath,editTmpPath,SDKDir.TMP_DIR, dstPath);
				if(!ret){
					dstPath=editTmpPath;
				}else
					SDKFileUtils.deleteFile(editTmpPath);
		    	findViewById(R.id.id_particle_saveplay).setVisibility(View.VISIBLE);
			}
			
		}
    }
    /**
     * 增加一个UI图层: ViewLayer 
     */
    private void addViewLayer()
    {
    	if(drawPadView!=null && drawPadView.isRunning())
    	{
    		mViewLayer=drawPadView.addViewLayer();
            
    		//把这个图层绑定到LayerRelativeLayout中.从而LayerRelativeLayout中的各种UI界面会被绘制到Drawpad上.
    		mLayerRelativeLayout.bindViewLayer(mViewLayer);
    		
            mLayerRelativeLayout.invalidate();//刷新一下.
            
            ViewGroup.LayoutParams  params=mLayerRelativeLayout.getLayoutParams();
            params.height=mViewLayer.getPadHeight();  //因为布局时, 宽度一致, 这里调整高度,让他们一致.
            mLayerRelativeLayout.setLayoutParams(params);
    	}
    }
	  
    @Override
    public void onClick(View v) {
    	switch (v.getId()) {
			case R.id.id_particle_touch:
				touchShot();
				break;
			case R.id.id_particle_oneshot:
				oneShot();		
				break;
			case R.id.id_particle_baoza:
				baozhaShot();
				break;
			case R.id.id_particle_yunduo:
				yunduoShot();
				break;
			default:
				break;
		}
    }
    private void touchShot()
    {
    	particleLayout.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					
					//开始粒子效果.
					Drawable  drawable=getResources().getDrawable(R.drawable.star_pink);
					ps = new ParticleSystem(particleLayout,100, drawable, 800);
					ps.setScaleRange(0.7f, 1.3f);
					ps.setSpeedRange(0.05f, 0.1f);
					ps.setRotationSpeedRange(90, 180);
					ps.setFadeOut(200, new AccelerateInterpolator());
					ps.emit((int) event.getX(), (int) event.getY()-10, 40);
					
					break;
				case MotionEvent.ACTION_MOVE:
					ps.updateEmitPoint((int) event.getX(), (int) event.getY());
					break;
				case MotionEvent.ACTION_UP:
					ps.stopEmitting();
					break;
				}
				return true;
			}
		});
    }
    /**
     * 单点触发
     */
    private void oneShot()
    {
    	Drawable  drawable=getResources().getDrawable(R.drawable.star_pink);
		ps = new ParticleSystem(particleLayout,100, drawable, 800);
		ps.setSpeedRange(0.1f, 0.25f);
		ps.oneShot(particleLayout, 100);
    }
    private void baozhaShot()
    {
    	Drawable  drawable=getResources().getDrawable(R.drawable.animated_confetti);
    	ps = new ParticleSystem(particleLayout,100, drawable, 5000);
    	ps.setSpeedRange(0.1f, 0.25f);
		ps.setRotationSpeedRange(90, 180);
		ps.setInitialRotationRange(0, 360);
		ps.oneShot(particleLayout, 100);
    }
    private void yunduoShot()
    {
    	Drawable  drawable=getResources().getDrawable(R.drawable.dust);
    	ps = new ParticleSystem(particleLayout, 4,drawable, 3000);
    	ps.setSpeedByComponentsRange(-0.025f, 0.025f, -0.06f, -0.08f);		
		ps.setAcceleration(0.00001f, 30);
		ps.setInitialRotationRange(0, 360);
		ps.addModifier(new AlphaModifier(255, 0, 1000, 3000));
		ps.addModifier(new ScaleModifier(0.5f, 2f, 0, 1000));
		ps.oneShot(particleLayout, 4);
    }
    private void toastStop()
    {
    	Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if(mplayer!=null){
    		mplayer.stop();
    		mplayer.release();
    		mplayer=null;
    	}
    	if(drawPadView!=null){
    		drawPadView.stopDrawPad();
    		drawPadView=null;        		   
    	}
    	SDKFileUtils.deleteFile(dstPath);
    	SDKFileUtils.deleteFile(editTmpPath);
    }
    //--------------------------------------一下为UI界面-----------------------------------------------------------
    private void initView()
    {
    	findViewById(R.id.id_particle_yunduo).setOnClickListener(this);
    	findViewById(R.id.id_particle_touch).setOnClickListener(this);
    	findViewById(R.id.id_particle_oneshot).setOnClickListener(this);
    	findViewById(R.id.id_particle_baoza).setOnClickListener(this);
    	
    	
    	
          findViewById(R.id.id_particle_saveplay).setOnClickListener(new OnClickListener() {
  			
  			@Override
  			public void onClick(View v) {
  				 if(SDKFileUtils.fileExist(dstPath)){
  		   			 	Intent intent=new Intent(ParticleDemoActivity.this,VideoPlayerActivity.class);
  			    	    	intent.putExtra("videopath", dstPath);
  			    	    	startActivity(intent);
  		   		 }else{
  		   			 Toast.makeText(ParticleDemoActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
  		   		 }
  			}
  		});
      	findViewById(R.id.id_particle_saveplay).setVisibility(View.GONE);
    }
}
