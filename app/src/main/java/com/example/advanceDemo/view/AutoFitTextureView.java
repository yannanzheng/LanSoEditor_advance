package com.example.advanceDemo.view;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;


import com.lansosdk.box.AudioLine;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.CameraLayer;
import com.lansosdk.box.CanvasLayer;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.DrawPadViewRender;
import com.lansosdk.box.Layer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadThreadProgressListener;
import com.lansosdk.videoeditor.DrawPadView;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;


public class AutoFitTextureView extends TextureView {

    private static final String TAG = "AutoFitTextureView";


    private SurfaceTexture mSurfaceTexture = null;


    private int encWidth, encHeight, encBitRate, encFrameRate;
    private Context mContext;
    /**
     * 经过宽度对齐到手机的边缘后, 缩放后的宽高,作为olayergl的宽高.
     */
    private int viewWidth, viewHeight;

    /**
	 * 是否也录制mic的声音.
	 */
	private boolean isRecordMic=false;

	 private boolean  isRecordExtPcm=false;  //是否使用外面的pcm输入.
	 private int pcmSampleRate=44100;
	 private int pcmBitRate=64000;
	 private int pcmChannels=2; //音频格式. 音频默认是双通道.
	 
	 
    public AutoFitTextureView(Context context) {
        super(context);
        mContext = context;
        initVideoView(context);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initVideoView(context);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initVideoView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AutoFitTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initVideoView(context);
    }

    private class SurfaceCallback implements SurfaceTextureListener {

        /**
         * Invoked when a {@link TextureView}'s SurfaceTexture is ready for use.
         * 当画面呈现出来的时候, 会调用这个回调.
         * <p>
         * 当Activity跳入到别的界面后,这时会调用{@link #onSurfaceTextureDestroyed(SurfaceTexture)} 销毁这个Texture,
         * 如果您想在再次返回到当前Activity时,再次显示预览画面, 可以在这个方法里重新设置一遍DrawPad,并再次startDrawPad
         */

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

            mSurfaceTexture = surface;
            viewHeight = height;
            viewWidth = width;
        }

        /**
         * Invoked when the {@link SurfaceTexture}'s buffers size changed.
         * 当创建的TextureView的大小改变后, 会调用回调.
         * <p>
         * 当您本来设置的大小是480x480,而DrawPad会自动的缩放到父view的宽度时,会调用这个回调,提示大小已经改变, 这时您可以开始startDrawPad
         * 如果你设置的大小更好等于当前Texture的大小,则不会调用这个, 详细的注释见 {@link DrawPadView#startDrawPad(onDrawPadProgressListener, onDrawPadCompletedListener)}
         */
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            mSurfaceTexture = surface;
            viewHeight = height;
            viewWidth = width;
        }

        /**
         * Invoked when the specified {@link SurfaceTexture} is about to be destroyed.
         * <p>
         * 当您跳入到别的Activity的时候, 会调用这个,销毁当前Texture;
         */
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mSurfaceTexture = null;
            viewHeight = 0;
            viewWidth = 0;
            return false;
        }

        /**
         * Invoked when the specified {@link SurfaceTexture} is updated through
         * {@link SurfaceTexture#updateTexImage()}.
         * <p>
         * 每帧视频如果更新了, 则会调用这个!!!!
         */
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    }

    private void initVideoView(Context context) {
        setSurfaceTextureListener(new SurfaceCallback());
    }

    protected int mRatioWidth = 0;
    protected int mRatioHeight = 0;

    /**
	 * 是否在录制的同时, 录制mic的声音.
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
	 * 故您在投递音频的时候, 需要严格按照音频播放的速度投递. 当前仅支持44100采样率的音频, 编码为44100 64000的码率.
	 * 
	 * 如采用外面的pcm数据,则视频在录制过程中,会参考音频时间戳,来计算得出视频的时间戳,
	 * 如外界音频播放完毕,无数据push,应及时stopDrawPad 
	 * 
	 * 可以通过 AudioLine 的getFrameSize()方法获取到每次应该投递多少个字节,此大小为固定大小, 每次投递必须push这么大小字节的数据.
	 * 
	 * @param isrecord  是否录制
	 * @param channels  声音通道个数,       如是mp3或aac文件,可根据MediaInfo获得
	 * @param samplerate 采样率                            如是mp3或aac文件,可根据MediaInfo获得
	 * @param bitrate  码率                                      如是mp3或aac文件,可根据MediaInfo获得
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
	
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int width = getScreenWidth(mContext);

        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
//            if (width < height * mRatioWidth / mRatioHeight) {
//                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
//            } else {
            setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
//            }
        }

    }
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        // 获取屏幕密度（方法1）
        return wm.getDefaultDisplay().getHeight(); // 屏幕高（像素，如：800p）
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        // 获取屏幕密度（方法1）
        return wm.getDefaultDisplay().getWidth(); // 屏幕宽度（像素，如：800p）
    }
    
    private DrawPadUpdateMode mUpdateMode = DrawPadUpdateMode.ALL_VIDEO_READY;
    private int mAutoFlushFps = 0;

    /**
     * 设置DrawPad的刷新模式,默认 {@link DrawPad.UpdateMode#ALL_VIDEO_READY};
     *
     * @param mode
     * @param autofps //自动刷新的参数,每秒钟刷新几次(即视频帧率).当自动刷新的时候有用, 不是自动,则不起作用.
     */
    public void setUpdateMode(DrawPadUpdateMode mode, int autofps) {
        mAutoFlushFps = autofps;

        mUpdateMode = mode;

        if (renderer != null) {
            renderer.setUpdateMode(mUpdateMode, mAutoFlushFps);
        }
    }

    /**
     * 获取当前View的 宽度
     *
     * @return
     */
    public int getViewWidth() {
        return viewWidth;
    }

    /**
     * 获得当前View的高度.
     *
     * @return
     */
    public int getViewHeight() {
        return viewHeight;
    }

    //------------------------------------------------------------------------------------------------------------------------
    private String encodeOutput = null; //编码输出路径
    private DrawPadViewRender renderer;

    public void setRealEncodeEnable(int encW, int encH, int encBr, int encFr, String outPath) {
        if (outPath != null && encW > 0 && encH > 0 && encBr > 0 && encFr > 0) {
            encWidth = encW;
            encHeight = encH;
            encBitRate = encBr;
            encFrameRate = encFr;
            encodeOutput = outPath;
        } else {
            Log.w(TAG, "enable real encode is error,may be outpath is null");
            encodeOutput = null;
        }
    }

    /**
     * DrawPad每执行完一帧画面,会调用这个Listener,返回的timeUs是当前画面的时间戳(微妙),
     * 可以利用这个时间戳来做一些变化,比如在几秒处缩放, 在几秒处平移等等.从而实现一些动画效果.
     *
     * @param currentTimeUs  当前DrawPad处理画面的时间戳.,单位微秒.
     */
    private onDrawPadProgressListener drawpadProgressListener = null;

    public void setDrawPadProgressListener(onDrawPadProgressListener listener) {
        drawpadProgressListener = listener;
    }

    /**
     * 方法与   onDrawPadProgressListener不同的地方在于:
     * 此回调是在DrawPad渲染完一帧后,立即执行这个回调中的代码,不通过Handler传递出去,你可以精确的执行一些下一帧的如何操作.
     * 故不能在回调 内增加各种UI相关的代码.
     */
    private onDrawPadThreadProgressListener drawPadThreadProgressListener = null;

    public void setDrawPadThreadProgressListener(onDrawPadThreadProgressListener listener) {
        drawPadThreadProgressListener = listener;
    }

    private onDrawPadCompletedListener drawpadCompletedListener = null;

    public void setDrawPadCompletedListener(onDrawPadCompletedListener listener) {
        drawpadCompletedListener = listener;
    }


    public void startDrawPad(onDrawPadProgressListener progressListener, onDrawPadCompletedListener completedListener) {
        drawpadProgressListener = progressListener;
        drawpadCompletedListener = completedListener;

        startDrawPad(mPauseRecordDrawPad);
    }

    public void startDrawPad() {
        startDrawPad(mPauseRecordDrawPad);
    }

    public void startDrawPad(boolean pauseRecord) {
        if (mSurfaceTexture != null) {
            renderer = new DrawPadViewRender(getContext(), viewWidth, viewHeight);  //<----从这里去建立DrawPad线程.
            if (renderer != null) {

                renderer.setUseMainVideoPts(isUseMainPts);
                //因为要预览,这里设置显示的Surface,当然如果您有特殊情况需求,也可以不用设置,但displayersurface和EncoderEnable要设置一个,DrawPadRender才可以工作.
                renderer.setDisplaySurface(new Surface(mSurfaceTexture));

                renderer.setEncoderEnable(encWidth, encHeight, encBitRate, encFrameRate, encodeOutput);

                renderer.setUpdateMode(mUpdateMode, mAutoFlushFps);

                //设置DrawPad处理的进度监听, 回传的currentTimeUs单位是微秒.
                renderer.setDrawPadProgressListener(drawpadProgressListener);
                renderer.setDrawPadCompletedListener(drawpadCompletedListener);
                renderer.setDrawPadThreadProgressListener(drawPadThreadProgressListener);

                if(isRecordMic){
 					renderer.setRecordMic(isRecordMic);	
 				}else if(isRecordExtPcm){
 					renderer.setRecordExtraPcm(isRecordExtPcm,pcmChannels,pcmSampleRate,pcmBitRate);
 				}
                
                if (pauseRecord) {
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
    public void pauseDrawPad() {
        if (renderer != null) {
            renderer.pauseRefreshDrawPad();
        }
    }

    /**
     * 恢复之前暂停的DrawPad,让其继续画面更新. 与{@link #pauseDrawPad()}配对使用.
     */
    public void resumeDrawPad() {
        if (renderer != null) {
            renderer.resumeRefreshDrawPad();
        }
    }

    private boolean mPauseRecordDrawPad = false;

    public void pauseDrawPadRecord() {
        if (renderer != null) {
            renderer.pauseRecordDrawPad();
        } else {
            mPauseRecordDrawPad = true;
        }
    }

    public void resumeDrawPadRecord() {
        if (renderer != null) {
            renderer.resumeRecordDrawPad();
        } else {
            mPauseRecordDrawPad = false;
        }
    }

    /**
     * 设置是否使用主视频的时间戳为录制视频的时间戳, 默认第一次获取到的VideoLayer或FilterLayer为主视频.
     * 如果您传递过来的是一个完整的视频, 只是需要在此视频上做一些操作, 操作完成后,时长等于源视频的时长, 则建议使用主视频的时间戳, 如果视频是从中间截取一般开始的
     * 则不建议使用, 默认是这里为false;
     * <p>
     * 注意:需要在DrawPad开始前使用.
     */
    private boolean isUseMainPts = false;

    public void setUseMainVideoPts(boolean use) {
        isUseMainPts = use;
    }

    /**
     * 当前DrawPad是否在工作.
     *
     * @return
     */
    public boolean isRunning() {
        if (renderer != null) {
            return renderer.isRunning();
        } else {
            return false;
        }
    }

    public boolean isRecording() {
        if (renderer != null) {
            return renderer.isRecording();
        } else {
            return false;
        }
    }

    /**
     * 停止DrawPad的渲染线程
     * 因为视频的来源是外接驱动的, DrawPadViewRender不会自动停止, 故需要外部在视频播放外的时候, 调用此方法来停止DrawPad渲染线程.
     */
    public void stopDrawPad() {
        if (renderer != null) {
            renderer.release();
            renderer = null;
        }
    }
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
    
//    public CameraLayer addCameraMaskLayer() {
//    	CameraLayer ret = null;
//        if (renderer != null) {
//            ret = new CameraLayer(getContext(), viewWidth, viewHeight, mUpdateMode);
//            renderer.addCustemLayer(ret);
//        } else {
//            Log.e(TAG, "CameraMaskLayer error render is not avalid");
//        }
//        return ret;
//    }

    /**
     * 获取一个BitmapLayer
     * 注意:此方法一定在 startDrawPad之后,在stopDrawPad之前调用.
     *
     * @param bmp 图片的bitmap对象,可以来自png或jpg等类型,这里是通过BitmapFactory.decodeXXX的方法转换后的bitmap对象.
     * @return 一个BitmapLayer对象
     */
    public BitmapLayer addBitmapLayer(Bitmap bmp) {
        if (bmp != null) {
            Log.i(TAG, "imgBitmapLayer:" + bmp.getWidth() + " height:" + bmp.getHeight());

            if (renderer != null) {
                return renderer.addBitmapLayer(bmp,null);
            } else {
                Log.e(TAG, "addBitmapLayer error render is not avalid");
                return null;
            }
        } else {
            Log.e(TAG, "addBitmapLayer error, bitmap is null");
            return null;
        }
    }

    /**
     * 获得一个 ViewLayer,您可以在获取后,仿照我们的例子,来为视频增加各种UI空间.
     * 注意:此方法一定在 startDrawPad之后,在stopDrawPad之前调用.
     *
     * @return 返回ViewLayer对象.
     */
    public ViewLayer addViewLayer() {
        if (renderer != null) {
            return renderer.addViewLayer();
        } else {
            Log.e(TAG, "addViewLayer error render is not avalid");
            return null;
        }
    }

    /**
     * 获得一个 CanvasLayer
     * 注意:此方法一定在 startDrawPad之后,在stopDrawPad之前调用.
     *
     * @return
     */
    public CanvasLayer addCanvasLayer() {
        if (renderer != null) {
            return renderer.addCanvasLayer();
        } else {
            Log.e(TAG, "addCanvasLayer error render is not avalid");
            return null;
        }
    }

    /**
     * 从渲染线程列表中移除并销毁这个Layer;
     * 注意:此方法一定在 startDrawPad之后,在stopDrawPad之前调用.
     *
     * @param Layer
     */
    public void removeLayer(Layer layer) {
        if (layer != null) {
            if (renderer != null) {
                renderer.removeLayer(layer);
            } else {
                Log.w(TAG, "removeLayer error render is not avalid");
            }
        }
    }

    /**
     * 为已经创建好的FilterLayer对象切换滤镜效果
     *
     * @param Layer    已经创建好的FilterLayer对象
     * @param filter 要切换到的滤镜对象.
     * @return 切换成功, 返回true; 失败返回false
     */
    public boolean switchFilterTo(Layer layer, GPUImageFilter filter) {
        if (renderer != null) {
            return renderer.switchFilter(layer, filter);
        }
        return false;
    }
}
