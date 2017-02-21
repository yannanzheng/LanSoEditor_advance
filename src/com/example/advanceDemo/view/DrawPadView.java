package com.example.advanceDemo.view;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.TableLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;

import com.lansosdk.box.AudioLine;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.CameraLayer;
import com.lansosdk.box.CameraLayer;
import com.lansosdk.box.CanvasLayer;
import com.lansosdk.box.DataLayer;
import com.lansosdk.box.MVLayer;
import com.lansosdk.box.Layer;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.DrawPadViewRender;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.box.onDrawPadThreadProgressListener;
import com.lansosdk.videoeditor.MediaInfo;



/**
 *  视频处理预览和实时保存的View, 继承自FrameLayout.
 *  
 *   适用在增加到UI界面中, 一边预览,一边实时保存的场合.
 *
 */
public class DrawPadView extends FrameLayout {
	
	private static final String TAG="DrawPadView";
	private static final boolean VERBOSE = false;  
  
    private int mVideoRotationDegree;

    private TextureRenderView mTextureRenderView;
 	
    private DrawPadViewRender  renderer;
 	
 	private SurfaceTexture mSurfaceTexture=null;
 	
	private boolean isUseMainPts=false;
 	
 	private int encWidth,encHeight,encBitRate,encFrameRate;
 	
 	/**
 	 *  经过宽度对齐到手机的边缘后, 缩放后的宽高,作为opengl的宽高. 
 	 */
 	private int viewWidth,viewHeight;  
 	
 	  
    public DrawPadView(Context context) {
        super(context);
        initVideoView(context);
    }

    public DrawPadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVideoView(context);
    }

    public DrawPadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DrawPadView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initVideoView(context);
    }


    private void initVideoView(Context context) {
        setTextureView();

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }
    /**
     * 视频画面显示模式：不裁剪直接和父view匹配， 这样如果画面超出父view的尺寸，将会只显示视频画面的
     * 一部分，您可以使用这个来平铺视频画面，通过手动拖拽的形式来截取画面的一部分。类似视频画面区域裁剪的功能。
     */
    static final int AR_ASPECT_FIT_PARENT = 0; // without clip
    /**
     * 视频画面显示模式:裁剪和父view匹配, 当视频画面超过父view大小时,不会缩放,会只显示视频画面的一部分.
     * 超出部分不予显示.
     */
    static final int AR_ASPECT_FILL_PARENT = 1; // may clip
    /**
     * 视频画面显示模式: 自适应大小.当小于画面尺寸时,自动显示.当大于尺寸时,缩放显示.
     */
    static final int AR_ASPECT_WRAP_CONTENT = 2;
    /**
     * 视频画面显示模式:和父view的尺寸对其.完全填充满父view的尺寸
     */
    static final int AR_MATCH_PARENT = 3;
    /**
     * 把画面的宽度等于父view的宽度, 高度按照16:9的形式显示. 大部分的网络视频推荐用这种方式显示.
     */
    static final int AR_16_9_FIT_PARENT = 4;
    /**
     * 把画面的宽度等于父view的宽度, 高度按照4:3的形式显示.
     */
    static final int AR_4_3_FIT_PARENT = 5;

    
    private void setTextureView() {
    	mTextureRenderView = new TextureRenderView(getContext());
    	mTextureRenderView.setSurfaceTextureListener(new SurfaceCallback());
    	
       /**
        * 注意： 此
        */
    	mTextureRenderView.setDispalyRatio(AR_ASPECT_FIT_PARENT);
        
    	View renderUIView = mTextureRenderView.getView();
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        renderUIView.setLayoutParams(lp);
        addView(renderUIView);
        mTextureRenderView.setVideoRotation(mVideoRotationDegree);
    }
    private String encodeOutput=null; //编码输出路径
    
    private DrawPadUpdateMode mUpdateMode=DrawPadUpdateMode.ALL_VIDEO_READY;
    private int mAutoFlushFps=0;
    
    /**
     * 设置DrawPad的刷新模式,默认 {@link DrawPad.UpdateMode#ALL_VIDEO_READY};
     * 
     * @param mode
     * @param autofps  //自动刷新的参数,每秒钟刷新几次(即视频帧率).当自动刷新的时候有用, 不是自动,则不起作用.
     */
    public void setUpdateMode(DrawPadUpdateMode mode,int autofps)
    {
    	mAutoFlushFps=autofps;
    	
    	mUpdateMode=mode;
    	
    	if(renderer!=null)
    	{
    		 renderer.setUpdateMode(mUpdateMode,mAutoFlushFps);
    	}
    }
    /**
     * 获取当前View的 宽度
     * @return
     */
    public int getViewWidth(){
    	return viewWidth;
    }
    /**
     * 获得当前View的高度.
     * @return
     */
    public int getViewHeight(){
    	return viewHeight;
    }
    
  
    public interface onViewAvailable {	    
        void viewAvailable(DrawPadView v);
    }
	private onViewAvailable mViewAvailable=null;
	  /**
     * 此回调仅仅是作为演示: 当跳入到别的Activity后的返回时,会再次预览当前画面的功能.
     * 你完全可以重新按照你的界面需求来修改这个DrawPadView类.
     *
     */
	public void setOnViewAvailable(onViewAvailable listener)
	{
		mViewAvailable=listener;
	}
	
	
	private class SurfaceCallback implements SurfaceTextureListener {
    			
				/**
				 *  Invoked when a {@link TextureView}'s SurfaceTexture is ready for use.
				 *   当画面呈现出来的时候, 会调用这个回调.
				 *   
				 *  当Activity跳入到别的界面后,这时会调用{@link #onSurfaceTextureDestroyed(SurfaceTexture)} 销毁这个Texture,
				 *  如果您想在再次返回到当前Activity时,再次显示预览画面, 可以在这个方法里重新设置一遍DrawPad,并再次startDrawPad 
				 */
				
    	        @Override
    	        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

    	            mSurfaceTexture = surface;
    	            viewHeight=height;
    	            viewWidth=width;
    	            if(mViewAvailable!=null){
    	            	mViewAvailable.viewAvailable(null);
    	            }	
    	        }
    	        
    	        /**
    	         * Invoked when the {@link SurfaceTexture}'s buffers size changed.
    	         * 当创建的TextureView的大小改变后, 会调用回调.
    	         * 
    	         * 当您本来设置的大小是480x480,而DrawPad会自动的缩放到父view的宽度时,会调用这个回调,提示大小已经改变, 这时您可以开始startDrawPad
    	         * 如果你设置的大小更好等于当前Texture的大小,则不会调用这个, 详细的注释见 {@link DrawPadView#startDrawPad(onDrawPadProgressListener, onDrawPadCompletedListener)}
    	         */
    	        @Override
    	        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    	            mSurfaceTexture = surface;
    	            viewHeight=height;
    	            viewWidth=width;
	        			if(mSizeChangedCB!=null)
	        				mSizeChangedCB.onSizeChanged(width, height);
    	        }
    	
    	        /**
    	         *  Invoked when the specified {@link SurfaceTexture} is about to be destroyed.
    	         *  
    	         *  当您跳入到别的Activity的时候, 会调用这个,销毁当前Texture;
    	         *  
    	         */
    	        @Override
    	        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
    	            mSurfaceTexture = null;
    	            viewHeight=0;
    	            viewWidth=0;
    	            return false;
    	        }
    	
    	        /**
    	         * 
    	         * Invoked when the specified {@link SurfaceTexture} is updated through
    	         * {@link SurfaceTexture#updateTexImage()}.
    	         * 
    	         *每帧视频如果更新了, 则会调用这个!!!! 
    	         */
    	        @Override
    	        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    	        }
    }
		/**
		 * 
		 * 设置使能 实时保存, 即把正在DrawPad中呈现的画面实时的保存下来,实现所见即所得的模式
		 * 
		 *  如果实时保存的宽高和原视频的宽高不成比例,则会先等比例缩放原视频,然后在多出的部分出增加黑边的形式呈现,比如原视频是16:9,设置的宽高是480x480,则会先把原视频按照宽度进行16:9的比例缩放.
		 *  在缩放后,在视频的上下增加黑边的形式来实现480x480, 从而不会让视频变形.
		 *  
		 *  如果视频在拍摄时有角度, 比如手机相机拍照,会有90度或270, 则会自动的识别拍摄的角度,并在缩放时,自动判断应该左右加黑边还是上下加黑边.
		 *  
		 *  因有缩放的特性, 您可以直接向DrawPad中投入一个视频,然后把宽高比设置一致,这样可实现一个视频压缩的功能;您也可以把宽高比设置一下,这样可实现视频加黑边的压缩功能.
		 *  或者您完全不进行缩放, 仅仅想把视频码率减低一下, 也可以把其他参数设置为和源视频一致, 仅仅调试encBr这个参数,来实现视频压缩的功能.
		 *  
		 * @param encW  录制视频的宽度
		 * @param encH  录制视频的高度
		 * @param encBr 录制视频的bitrate,
		 * @param encFr 录制视频的 帧率
		 * @param outPath  录制视频的保存路径.
		 */
	   public void setRealEncodeEnable(int encW,int encH,int encBr,int encFr,String outPath)
	    {
	    	if(encW>0 && encH>0 && encBr>0 && encFr>0){
	    			encWidth=encW;
			        encHeight=encH;
			        encBitRate=encBr;
			        encFrameRate=encFr;
			        encodeOutput=outPath;
	    	}else{
	    		Log.w(TAG,"enable real encode is error");
	    	}
	    }
	
	   private onDrawPadSizeChangedListener mSizeChangedCB=null; 
	   /**
	    * 设置当前DrawPad的宽度和高度,并把宽度自动缩放到父view的宽度,然后等比例调整高度.
	    * 
	    * 如果在父view中已经预设好了希望的宽高,则可以不调用这个方法,直接 {@link #startDrawPad(onDrawPadProgressListener, onDrawPadCompletedListener)}
	    * 可以通过 {@link #getViewHeight()} 和 {@link #getViewWidth()}来得到当前view的宽度和高度.
	    * 
	    * 
	    * 注意: 这里的宽度和高度,会根据手机屏幕的宽度来做调整,默认是宽度对齐到手机的左右两边, 然后调整高度, 把调整后的高度作为DrawPad渲染线程的真正宽度和高度.
	    * 注意: 此方法需要在 {@link #startDrawPad(onDrawPadProgressListener, onDrawPadCompletedListener)} 前调用.
	    * 比如设置的宽度和高度是480,480, 而父view的宽度是等于手机分辨率是1080x1920,则DrawPad默认对齐到手机宽度1080,然后把高度也按照比例缩放到1080.
	    * 
	    * @param width  DrawPad宽度
	    * @param height DrawPad高度 
	    * @param cb   设置好后的回调, 注意:如果预设值的宽度和高度经过调整后 已经和父view的宽度和高度一致,则不会触发此回调(当然如果已经是希望的宽高,您也不需要调用此方法).
	    */
	public void setDrawPadSize(int width,int height,onDrawPadSizeChangedListener cb){
		
		if (width != 0 && height != 0 && cb!=null) {
			float setAcpect=(float)width/(float)height;
			float setViewacpect=(float)viewWidth/(float)viewHeight;
			
		    if(setAcpect==setViewacpect){  //如果比例已经相等,不需要再调整,则直接显示.
		    	
		    	if(mSizeChangedCB!=null)
    				mSizeChangedCB.onSizeChanged(width, height);
		    	
		    }else if (mTextureRenderView != null) {
            	
                mTextureRenderView.setVideoSize(width, height);
                mTextureRenderView.setVideoSampleAspectRatio(1,1);
                mSizeChangedCB=cb;
            }
            requestLayout();
        }
	}
	/**
	 * DrawPad每执行完一帧画面,会调用这个Listener,返回的timeUs是当前画面的时间戳(微妙),
	 *  可以利用这个时间戳来做一些变化,比如在几秒处缩放, 在几秒处平移等等.从而实现一些动画效果.
	 * @param currentTimeUs  当前DrawPad处理画面的时间戳.,单位微秒.
	 */
	private onDrawPadProgressListener drawpadProgressListener=null;
	
	public void setDrawPadProgressListener(onDrawPadProgressListener listener){
		drawpadProgressListener=listener;
	}
	/**
	 * 方法与   onDrawPadProgressListener不同的地方在于:
	 * 此回调是在DrawPad渲染完一帧后,立即执行这个回调中的代码,不通过Handler传递出去,你可以精确的执行一些下一帧的如何操作.
	 * 故不能在回调 内增加各种UI相关的代码.
	 */
	private onDrawPadThreadProgressListener drawPadThreadProgressListener=null;
	
	public void setDrawPadThreadProgressListener(onDrawPadThreadProgressListener listener){
		drawPadThreadProgressListener=listener;
	}
	
	private onDrawPadCompletedListener drawpadCompletedListener=null;
	public void setDrawPadCompletedListener(onDrawPadCompletedListener listener){
		drawpadCompletedListener=listener;
	}

	/**
	 * 开始DrawPad的渲染线程, 阻塞执行, 直到DrawPad真正开始执行后才退出当前方法.
	 * 
	 * 此方法可以在 {@link onDrawPadSizeChangedListener} 完成后调用.
	 * 可以先通过 {@link #setDrawPadSize(int, int, onDrawPadSizeChangedListener)}来设置宽高,然后在回调中执行此方法.
	 * 如果您已经在xml中固定了view的宽高,则可以直接调用这里, 无需再设置DrawPadSize
	 * @param progresslistener
	 * @param completedListener
	 */
	public void startDrawPad(onDrawPadProgressListener progressListener,onDrawPadCompletedListener completedListener)
	{
		drawpadProgressListener=progressListener;
		drawpadCompletedListener=completedListener;
		
		startDrawPad(pauseRecord);
	}
	
	/**
	 * 此方法仅仅使用在录制视频的同时,您也设置了录制音频
	 * 
	 * @return  在录制结束后, 返回录制mic的音频文件路径, 
	 */
	public String getMicPath()
	{
		if(renderer!=null){
			return renderer.getAudioRecordPath();
		}else{
			return null;
		}
	}
	
	public void startDrawPad()
	{
		startDrawPad(pauseRecord);
	}
	public void startDrawPad(boolean pauseRecord)
	{
		 if( mSurfaceTexture!=null)
         {
			 renderer=new DrawPadViewRender(getContext(), viewWidth, viewHeight);  //<----从这里去建立DrawPad线程.
 			if(renderer!=null){
 				
 				renderer.setUseMainVideoPts(isUseMainPts);
 				//因为要预览,这里设置显示的Surface,当然如果您有特殊情况需求,也可以不用设置,但displayersurface和EncoderEnable要设置一个,DrawPadRender才可以工作.
 				renderer.setDisplaySurface(new Surface(mSurfaceTexture));
 				
 				renderer.setEncoderEnable(encWidth,encHeight,encBitRate,encFrameRate,encodeOutput);
 				
 				renderer.setUpdateMode(mUpdateMode,mAutoFlushFps);
 				
 				 //设置DrawPad处理的进度监听, 回传的currentTimeUs单位是微秒.
 				renderer.setDrawPadProgressListener(drawpadProgressListener);
 				renderer.setDrawPadCompletedListener(drawpadCompletedListener);
 				renderer.setDrawPadThreadProgressListener(drawPadThreadProgressListener);
 				
 				if(isRecordMic){
 					renderer.setRecordMic(isRecordMic);	
 				}else if(isRecordExtPcm){
 					renderer.setRecordExtraPcm(isRecordExtPcm, pcmChannels,pcmSampleRate,pcmBitRate);
 				}
 				
 				if(pauseRecord){
 					renderer.pauseRecordDrawPad();	
 				}
 				renderer.startDrawPad();
 			}
         }
	}
	/**
	 * 暂停DrawPad的画面更新.
	 * 在一些场景里,您需要开启DrawPad后,暂停下, 然后obtain各种Layer后,安排好各种事宜后,再让其画面更新,
	 * 则用到这个方法.
	 */
	public void pauseDrawPad()
	{
		if(renderer!=null){
			renderer.pauseRefreshDrawPad();
		}
	}
	/**
	 * 恢复之前暂停的DrawPad,让其继续画面更新. 与{@link #pauseDrawPad()}配对使用.
	 */
	public void resumeDrawPad()
	{
		if(renderer!=null){
			renderer.resumeRefreshDrawPad();
		}
	}
	private boolean pauseRecord=false;
	/**
	 * 是否也录制mic的声音.
	 */
	private boolean isRecordMic=false;

	 private boolean  isRecordExtPcm=false;  //是否使用外面的pcm输入.
	 private int pcmSampleRate=44100;
	 private int pcmBitRate=64000;
	 private int pcmChannels=2; //音频格式. 音频默认是双通道.
	 
	/**
	 * 暂停drawpad的录制,这个方法使用在暂停录制后, 在当前画面做其他的一些操作,
	 * 不可用来暂停后跳入到别的Activity中.
	 */
	public void pauseDrawPadRecord()
	{
		if(renderer!=null){
			renderer.pauseRecordDrawPad();
		}else{
			pauseRecord=true;
		}
	}
	
	public void resumeDrawPadRecord()
	{
		if(renderer!=null){
			renderer.resumeRecordDrawPad();
		}else{
			pauseRecord=false;
		}
	}
	/**
	 * 是否在CameraLayer录制的同时, 录制mic的声音. 
	 * 此方法仅仅使用在录像的时候, 同时录制Mic的场合.录制的采样默认是44100, 码率64000, 编码为aac格式.
	 * 录制的同时, 视频编码以音频的时间戳为参考.
	 * 可通过 {@link #stopDrawPad2()}停止,停止后返回mic录制的音频文件 m4a格式的文件, 
	 * @param record
	 */
	public void setRecordMic(boolean record)
	{
		if(renderer!=null){
			renderer.setRecordMic(record);
		}else{
			isRecordMic=record;
		}
	}
	/**
	 * 是否在录制画面的同时,录制外面的push进去的音频数据.
	 * 注意:当设置了录制外部的pcm数据后, 当前画板上录制的视频帧,就以音频的帧率为参考时间戳,从而保证音视频同步进行. 
	 * 故您在投递音频的时候, 需要严格按照音频播放的速度投递. 
	 * 
	 * 如采用外面的pcm数据,则视频在录制过程中,会参考音频时间戳,来计算得出视频的时间戳,
	 * 如外界音频播放完毕,无数据push,应及时stopDrawPad2
	 * 
	 * 可以通过 AudioLine 的getFrameSize()方法获取到每次应该投递多少个字节,此大小为固定大小, 每次投递必须push这么大小字节的数据, 后期会
	 * 
	 * 可通过 {@link #stopDrawPad2()}停止,停止后返回mic录制的音频文件 m4a格式的文件,
	 * 
	 * @param isrecord  是否录制
	 * @param channels  声音通道个数, 如是mp3或aac文件,可根据MediaInfo获得
	 * @param samplerate 采样率 如是mp3或aac文件,可根据MediaInfo获得
	 * @param bitrate  码率 如是mp3或aac文件,可根据MediaInfo获得
	 */
	public void setRecordExtraPcm(boolean isrecord,int channels,int samplerate,int bitrate)
	{
		if(renderer!=null){
			renderer.setRecordExtraPcm(isrecord, channels,samplerate, bitrate);
		}else{
			isRecordExtPcm=isrecord;
			pcmSampleRate=samplerate;
			pcmBitRate=bitrate;
			pcmChannels=channels;
		}
	}
	/**
	 * 获取一个音频输入对象, 向内部投递数据, 
	 * 只有当开启画板录制,并设置了录制外面数据的情况下,才有效.
	 * @return
	 */
	public AudioLine getAudioLine()
	{
		if(renderer!=null){
			return renderer.getAudioLine();
		}else{
			return null;
		}
	}
	/**
	 * 此代码只是用在分段录制的Camera的过程中, 其他地方不建议使用.
	 */
	public void segmentStart(String videoPath)
	{
		if(renderer!=null){
			renderer.segmentStart(videoPath);
		}else{
			pauseRecord=false;
		}
	}
	/**
	 * 此代码只是用在分段录制的Camera的过程中, 其他地方不建议使用.
	 */
	public void segmentStop()
	{
		if(renderer!=null){
			renderer.segmentStop();
		}else{
			pauseRecord=false;
		}
	}
	
	
	public boolean isRecording()
	{
		if(renderer!=null)
			return renderer.isRecording();
		else
			return false;
	}
	/**
    * 设置是否使用主视频的时间戳为录制视频的时间戳, 
    * 如果您传递过来的是一个完整的视频, 只是需要在此视频上做一些操作, 操作完成后,时长等于源视频的时长, 则建议使用主视频的时间戳, 如果视频是从中间截取一般开始的
    * 则不建议使用, 默认是这里为false;
    * 
    * 注意:需要在DrawPad开始前使用.
    */
    public void setUseMainVideoPts(boolean use)
    {
    	isUseMainPts=use;
    }
	/**
	 * 当前DrawPad是否在工作.
	 * @return
	 */
	public boolean isRunning()
	{
		if(renderer!=null)
			return renderer.isRunning();
		else
			return false;
	}
	/**
	 * 停止DrawPad的渲染线程
	 */
	public void stopDrawPad()
	{	
			if (renderer != null){
	        	renderer.release();
	        	renderer=null;
	        }
			
	}
	/**
	 * 停止DrawPad的渲染线程 
	 * 如果设置了在录制的时候,设置了录制mic或extPcm, 则返回录制音频的文件路径. 
	 * @return
	 */
	public String stopDrawPad2()
	{	
			String ret=null;
			if (renderer != null){
	        	renderer.release();
	        	ret=renderer.getAudioRecordPath();
	        	renderer=null;
	        }
			return ret;
	}
	/**
	 * 作用同 {@link #setDrawPadSize(int, int, onDrawPadSizeChangedListener)}, 只是有采样宽高比, 如用我们的VideoPlayer则使用此方法,
	 * 建议用 {@link #setDrawPadSize(int, int, onDrawPadSizeChangedListener)}
	 * @param width
	 * @param height
	 * @param sarnum  如mediaplayer设置后,可以为1,
	 * @param sarden  如mediaplayer设置后,可以为1,
	 * @param cb
	 */
	public void setDrawPadSize(int width,int height,int sarnum,int sarden,onDrawPadSizeChangedListener cb)
	{
		if (width != 0 && height != 0) {
            if (mTextureRenderView != null) {
                mTextureRenderView.setVideoSize(width, height);
                mTextureRenderView.setVideoSampleAspectRatio(sarnum,sarden);
            }
            mSizeChangedCB=cb;
            requestLayout();
        }
	}
	 /**
     * 把当前图层放到最里层, 里面有 一个handler-loop机制, 将会在下一次刷新后执行.
     * @param layer
     */
	public void bringLayerToBack(Layer layer)
	{
			if(renderer!=null){
				renderer.bringLayerToBack(layer);
			}
	}
	  /**
     * 把当前图层放到最外层, 里面有 一个handler-loop机制, 将会在下一次刷新后执行.
     * @param layer
     */
	public void bringLayerToFront(Layer layer)
	{
			if(renderer!=null){
				renderer.bringLayerToFront(layer);
			}
	}
	public void changeLayerLayPosition(Layer layer,int position)
    {
		if(renderer!=null){
			renderer.changeLayerLayPosition(layer, position);
		}
    }
    public void swapTwoLayerPosition(Layer first,Layer second)
    {
    	if(renderer!=null){
			renderer.swapTwoLayerPosition(first, second);
		}
    }
	/**
	 * 获取一个主视频的 VideoLayer
	 * @param width 主视频的画面宽度  建议用 {@link MediaInfo#vWidth}来赋值
	 * @param height  主视频的画面高度 
	 * @return
	 */
	public VideoLayer addMainVideoLayer(int width, int height,GPUImageFilter filter)
    {
		VideoLayer ret=null;
	    
		if(renderer!=null)
			ret=renderer.addMainVideoLayer(width, height,filter);
		else{
			Log.e(TAG,"setMainVideoLayer error render is not avalid");
		}
		return ret;
    }
	/**
	 * 获取一个VideoLayer,从中获取surface {@link VideoLayer#getSurface()}来设置到视频播放器中,
	 * 用视频播放器提供的画面,来作为DrawPad的画面输入源.
	 * 
	 * 注意:此方法一定在 startDrawPad之后,在stopDrawPad之前调用.
	 * 
	 * @param width  视频的宽度
	 * @param height 视频的高度
	 * @return  VideoLayer对象
	 */
	public VideoLayer addVideoLayer(int width, int height,GPUImageFilter filter)
	{
		if(renderer!=null)
			return renderer.addVideoLayer(width,  height,filter);
		else{
			Log.e(TAG,"obtainSubVideoLayer error render is not avalid");
			return null;
		}
	}
	/**
	 * 
	 * @param degree  当前窗口Activity的角度, 可用我们的BoxUtils中的方法获取.
	 * @param filter  当前使用到的滤镜 ,如果不用, 则可以设置为null
	 * @return
	 */
	public CameraLayer addCameraLayer(int degree,GPUImageFilter filter)
	{
			CameraLayer ret=null;
		    
			if(renderer!=null)
				ret=renderer.addCameraLayer(degree,filter);
			else{
				Log.e(TAG,"setMainVideoLayer error render is not avalid");
			}
			return ret;
	}
	/**
	 * 增加 摄像头图层. 
	 * 
	 * 注意: 在增加前,需先检查当前是否有Camera权限.
	 * @return
	 */
	public CameraLayer addCameraLayer()
	{
			CameraLayer ret=null;
			if(renderer!=null){
				ret =new CameraLayer(getContext(),viewWidth,viewHeight,null,mUpdateMode);
				renderer.addCustemLayer(ret);
			}else{
				Log.e(TAG,"CameraMaskLayer error render is not avalid");
			}
			return ret;
	}
	
	/**
	 * 获取一个BitmapLayer
	 * 注意:此方法一定在 startDrawPad之后,在stopDrawPad之前调用.
	 * @param bmp  图片的bitmap对象,可以来自png或jpg等类型,这里是通过BitmapFactory.decodeXXX的方法转换后的bitmap对象.
	 * @return 一个BitmapLayer对象
	 */
	public BitmapLayer addBitmapLayer(Bitmap bmp)
	{
		if(bmp!=null)
		{
			Log.i(TAG,"imgBitmapLayer:"+bmp.getWidth()+" height:"+bmp.getHeight());
	    	
			if(renderer!=null)
				return renderer.addBitmapLayer(bmp);
			else{
				Log.e(TAG,"obtainBitmapLayer error render is not avalid");
				return null;
			}
		}else{
			Log.e(TAG,"obtainBitmapLayer error, bitmap is null");
			return null;
		}
	}
	/**
	 * 获取一个DataLayer的图层, 
	 * 数据图层, 是一个RGBA格式的数据, 内部是一个RGBA格式的图像.
	 * 
	 * @param dataWidth 图像的宽度
	 * @param dataHeight 图像的高度.
	 * @return
	 */
	public DataLayer  addDataLayer(int dataWidth,int dataHeight)
	{
		if(dataWidth>0 && dataHeight>0)
		{
			if(renderer!=null)
				return renderer.addDataLayer(dataWidth, dataHeight);
			else{
				Log.e(TAG,"addDataLayer error render is not avalid");
				return null;
			}
		}else{
			Log.e(TAG,"addDataLayer error, data size is error");
			return null;
		}
	}
	/**
	 * 增加一个mv图层, mv图层分为两个视频文件, 一个是彩色的视频, 一个黑白视频
	 * @param srcPath
	 * @param maskPath
	 * @return
	 */
	public MVLayer addMVLayer(String srcPath,String maskPath)
	{
			if(renderer!=null)
				return renderer.addMVLayer(srcPath,maskPath);
			else{
				Log.e(TAG,"obtainBitmapLayer error render is not avalid");
				return null;
			}
	}
	/**
	 * 获得一个 ViewLayer,您可以在获取后,仿照我们的例子,来为视频增加各种UI空间.
	 * 注意:此方法一定在 startDrawPad之后,在stopDrawPad之前调用.
	 * @return 返回ViewLayer对象.
	 */
	 public ViewLayer addViewLayer()
	 {
			if(renderer!=null)
				return renderer.addViewLayer();
			else{
				Log.e(TAG,"obtainViewLayer error render is not avalid");
				return null;
			}
	 }
	 /**
	  *  获得一个 CanvasLayer
	  * 注意:此方法一定在 startDrawPad之后,在stopDrawPad之前调用.
	  * @return
	  */
	 public CanvasLayer addCanvasLayer()
	 {
			if(renderer!=null)
				return renderer.addCanvasLayer();
			else{
				Log.e(TAG,"obtainViewLayer error render is not avalid");
				return null;
			}
	 }
	 /**
	  * 从渲染线程列表中移除并销毁这个Layer;
	  * 注意:此方法一定在 startDrawPad之后,在stopDrawPad之前调用.
	  * @param Layer
	  */
	public void removeLayer(Layer layer)
	{
		if(layer!=null)
		{
			if(renderer!=null)
				renderer.removeLayer(layer);
			else{
				Log.w(TAG,"removeLayer error render is not avalid");
			}
		}
	}
	/**
	 * 为已经创建好的图层对象切换滤镜效果
	 * @param Layer  已经创建好的Layer对象
	 * @param filter  要切换到的滤镜对象.
	 * @return 切换成功,返回true; 失败返回false
	 */
	   public boolean  switchFilterTo(Layer layer, GPUImageFilter filter) {
	    	if(renderer!=null){
	    		return renderer.switchFilter(layer, filter);
	    	}
    		return false;
	    }
}
