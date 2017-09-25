package com.lansosdk.videoeditor;


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

import com.lansosdk.box.AudioInsertManager;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.CameraLayer;
import com.lansosdk.box.CameraLayer;
import com.lansosdk.box.CanvasLayer;
import com.lansosdk.box.GifLayer;
import com.lansosdk.box.DataLayer;
import com.lansosdk.box.MVLayer;
import com.lansosdk.box.Layer;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.DrawPadViewRender;
import com.lansosdk.box.TwoVideoLayer;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.YUVLayer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadOutFrameListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSnapShotListener;
import com.lansosdk.box.onDrawPadThreadProgressListener;



/**
 *  
 *  用在前台处理,但没有UI界面, 
 *  
 *  适合用在用你们自己的界面或第三方SDK的界面, 然后把结果输入到我们DrawPad中,从而编码的场合. 
 *   
 * 适用在增加到UI界面中, 一边预览,一边实时保存的场合.
 *
 */
public class DrawPadNOView  {
	
	private static final String TAG="DrawPadNOView";
	private static final boolean VERBOSE = false;  
  
    private DrawPadViewRender  renderer;
 	
 	
	private boolean isUseMainPts=false;
 	
 	private int encWidth,encHeight,encFrameRate;
 	private int encBitRate=0;
 	/**
 	 *  经过宽度对齐到手机的边缘后, 缩放后的宽高,作为drawpad(画板)的宽高. 
 	 */
 	private int padWidth,padHeight;  
 	
 	
 	private String encodeOutput=null; //编码输出路径
    
    private DrawPadUpdateMode mUpdateMode=DrawPadUpdateMode.ALL_VIDEO_READY;
    private int mAutoFlushFps=0;
    private Context mContext;
    public DrawPadNOView(Context ctx)
    {
    	mContext=ctx;
    }
    /**
	 * 直接设置画板的宽高, 不让他自动缩放.
	 * 
	 * 要在画板开始前调用.
	 * @param width
	 * @param height
	 */
	public void setDrawpadSizeDirect(int width,int height)
	{
		padWidth=width;
		padHeight=height;
		if(renderer!=null){
			Log.w(TAG,"renderer maybe is running. your setting is not available!!");
		}
	}
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
    public int getDrawPadWidth(){
    	return padWidth;
    }
    /**
     * 获得当前View的高度.
     * @return
     */
    public int getDrawPadHeight(){
    	return padHeight;
    }
  
    /**
	 * 是否也录制mic的声音.
	 */
	private boolean isRecordMic=false;
	
	
	/**
	 *  暂停和恢复有3种方法, 分别是:刷新,预览,录制; 
	 * 
	 * 暂停刷新,则都暂停了,
	 * 暂停录制则只暂停录制,刷新和预览一样在走动.
	 * 暂停预览,则录制还在继续,但只是录制了同一副画面.
	 */
	private boolean isPauseRefreshDrawPad=false;
	private boolean isPausePreviewDrawPad=false;
	private boolean isPauseRecord=false;

	 private boolean  isRecordExtPcm=false;  //是否使用外面的pcm输入.
	 private int pcmSampleRate=44100;
	 private int pcmBitRate=64000;
	 private int pcmChannels=2; //音频格式. 音频默认是双通道.
	/**
	 * 
	 * 设置使能 实时保存, 即把正在DrawPad中呈现的画面实时的保存下来,实现所见即所得的模式, 
	 * 您可以设置哪些图层需要录制, 哪些图层不需要录制 。比如增加一个Logo，您只需要录制成视频有Logo，但录制中的画面没有这个Logo，则可以设置为只在录制中显示。
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
	 * @param outPath  录制视频的保存路径. 注意:这个路径在分段录制功能时,无效.即调用 {@link #segmentStart(String)}时.
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
	public void setRealEncodeEnable(int encW,int encH,int encFr,String outPath)
    {
    	if(encW>0 && encH>0 && encFr>0){
    			encWidth=encW;
		        encHeight=encH;
		        encBitRate=0;
		        encFrameRate=encFr;
		        encodeOutput=outPath;
    	}else{
    		Log.w(TAG,"enable real encode is error");
    	}
    }  
	/**
	 * DrawPad每执行完一帧画面,会调用这个Listener,返回的timeUs是当前画面的时间戳(微妙),
	 *  可以利用这个时间戳来做一些变化,比如在几秒处缩放, 在几秒处平移等等.从而实现一些动画效果.
	 * @param currentTimeUs  当前DrawPad处理画面的时间戳.,单位微秒.
	 */
	private onDrawPadProgressListener drawpadProgressListener=null;
	
	public void setOnDrawPadProgressListener(onDrawPadProgressListener listener){
		if(renderer!=null){
			renderer.setDrawPadProgressListener(listener);
		}
		drawpadProgressListener=listener;
	}
	/**
	 * 方法与   onDrawPadProgressListener不同的地方在于:
	 * 此回调是在DrawPad渲染完一帧后,立即执行这个回调中的代码,不通过Handler传递出去,你可以精确的执行一些下一帧的如何操作.
	 * 故不能在回调 内增加各种UI相关的代码.
	 */
	private onDrawPadThreadProgressListener drawPadThreadProgressListener=null;
	/**
	 * 方法与   onDrawPadProgressListener不同的地方在于:
	 * 此回调是在DrawPad渲染完一帧后,立即执行这个回调中的代码,不通过Handler传递出去,你可以精确的执行一些下一帧的如何操作.
	 * 
	 * listener中的方法是在DrawPad线程中执行的, 请不要在此监听里放置耗时的处理, 以免造成DrawPad线程的卡顿.
	 * 
	 * 不能在回调 内增加各种UI相关的代码.
	 * 
	 * @param listener  此listner工作在opengl线程， 非UI线程。
	 */
	public void setOnDrawPadThreadProgressListener(onDrawPadThreadProgressListener listener)
	{
		if(renderer!=null){
			renderer.setDrawPadThreadProgressListener(listener);
		}
		drawPadThreadProgressListener=listener;
	}
	
	private onDrawPadSnapShotListener drawpadSnapShotListener=null;
	/**
	 * 设置 获取当前DrawPad这一帧的画面的监听, 
	 *  设置截图监听,当截图完成后, 返回当前图片的btimap格式的图片.
	 *  此方法工作在主线程. 请不要在此方法里做图片的处理,以免造成拥堵;  建议获取到bitmap后,放入到一个链表中,在外面或另开一个线程处理. 
	 */
	public void setOnDrawPadSnapShotListener(onDrawPadSnapShotListener listener)
	{
		if(renderer!=null){
			renderer.setDrawpadSnapShotListener(listener);
		}
		drawpadSnapShotListener=listener;
	}
	public void toggleSnatShot()
	{
		if(drawpadSnapShotListener!=null && renderer!=null && renderer.isRunning()){
			renderer.toggleSnapShot(padWidth,padHeight);
		}else{
			Log.e(TAG,"toggle snap shot failed!!!");
		}
	}
	/**
	 * 触发一下截取当前DrawPad中的内容.
	 * 触发后, 会在DrawPad内部设置一个标志位,DrawPad线程会检测到这标志位后, 截取DrawPad, 并通过onDrawPadSnapShotListener监听反馈给您.
	 * 请不要多次或每一帧都截取DrawPad, 以免操作DrawPad处理过慢.
	 * 
	 * 此方法,仅在前台工作时有效.
	 * (注意:截取的仅仅是各种图层的内容, 不会截取DrawPad的黑色背景)
	 */
	public void toggleSnatShot(int width,int height)
	{
		if(drawpadSnapShotListener!=null && renderer!=null && renderer.isRunning()){
			renderer.toggleSnapShot(width,height);
		}else{
			Log.e(TAG,"toggle snap shot failed!!!");
		}
	}
	private onDrawPadCompletedListener drawpadCompletedListener=null;
	public void setOnDrawPadCompletedListener(onDrawPadCompletedListener listener){
		if(renderer!=null){
			renderer.setDrawPadCompletedListener(listener);
		}
		drawpadCompletedListener=listener;
	}
	
	private onDrawPadOutFrameListener drawPadPreviewFrameListener=null;
	private int previewFrameWidth;
	private int previewFrameHeight;
	private int previewFrameType;
	private boolean frameListenerInDrawPad=false;
	/**
	 * 设置每处理一帧的数据预览监听, 等于把当前处理的这一帧的画面拉出来,
	 * 您可以根据这个画面来自行的编码保存, 或网络传输.
	 * 
	 * 建议在这里拿到数据后, 放到queue中, 然后在其他线程中来异步读取queue中的数据, 请注意queue中数据的总大小, 要及时处理和释放, 以免内存过大,造成OOM问题
	 * 
	 * @param width  可以设置要引出这一帧画面的宽度, 如果宽度不等于drawpad的预览宽度,则会缩放.  
	 * @param height  画面缩放到的高度,
	 * @param type  数据的类型, 当前仅支持Bitmap
	 * @param listener 监听对象.
	 */
	public void setOnDrawPadOutFrameListener(int width,int height,int type,onDrawPadOutFrameListener listener)
	{
		if(renderer!=null){
			renderer.setDrawpadOutFrameListener(width, height, type,listener);
		}
		previewFrameWidth=width;
		previewFrameHeight=height;
		previewFrameType=type;
		drawPadPreviewFrameListener=listener;
	}
	/**
	 * 设置setOnDrawPadOutFrameListener后, 你可以设置这个方法来让listener是否运行在Drawpad线程中.
	 * 如果你要直接使用里面的数据, 则不用设置, 如果你要开启另一个线程, 把listener传递过来的数据送过去,则建议设置为true;
	 * [建议设置为true, 在DrawPad内部执行listener, 间隔取图片后,放入到列表中,然后在另外线程中使用.]
	 * @param en
	 */
	public void setOutFrameInDrawPad(boolean en){
		if(renderer!=null){
			renderer.setOutFrameInDrawPad(en);
		}
		frameListenerInDrawPad=en;
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
	/**
	 * 开始DrawPad的渲染线程, 阻塞执行, 直到DrawPad真正开始执行后才退出当前方法.
	 * 
	 * 可以先通过 {@link #setDrawPadSize(int, int, onDrawPadSizeChangedListener)}来设置宽高,然后在回调中执行此方法.
	 * 如果您已经在xml中固定了view的宽高,则可以直接调用这里, 无需再设置DrawPadSize
	 * @return
	 */
	public boolean startDrawPad()
	{
		boolean ret=false;
		 if(renderer==null&& padWidth>0 &&padHeight>0)
         {
			 	renderer=new DrawPadViewRender(mContext, padWidth, padHeight);  //<----从这里去建立DrawPad线程.
	 			if(renderer!=null){
	 				
	 				renderer.setUseMainVideoPts(isUseMainPts);
	 				//因为要预览,这里设置显示的Surface,当然如果您有特殊情况需求,也可以不用设置,但displayersurface和EncoderEnable要设置一个,DrawPadRender才可以工作.
	 				
	 				if(isCheckPadSize){
	 					encWidth=LanSongUtil.make32Multi(encWidth);
	 					encHeight=LanSongUtil.make32Multi(encHeight);
	 				}
	 				
	 				if(isCheckBitRate || encBitRate==0){
	 					encBitRate=LanSongUtil.checkSuggestBitRate(encHeight * encWidth, encBitRate);
	 				}
	 				renderer.setEncoderEnable(encWidth,encHeight,encBitRate,encFrameRate,encodeOutput);
	 				
	 				renderer.setUpdateMode(mUpdateMode,mAutoFlushFps);
	 				
	 				 //设置DrawPad处理的进度监听, 回传的currentTimeUs单位是微秒.
	 				renderer.setDrawpadSnapShotListener(drawpadSnapShotListener);
	 				renderer.setDrawPadProgressListener(drawpadProgressListener);
	 				renderer.setDrawPadCompletedListener(drawpadCompletedListener);
	 				renderer.setDrawPadThreadProgressListener(drawPadThreadProgressListener);
	 				
	 				renderer.setDrawpadOutFrameListener(previewFrameWidth, previewFrameHeight, previewFrameType, drawPadPreviewFrameListener);
	 				renderer.setOutFrameInDrawPad(frameListenerInDrawPad);
	 				
	 				
	 				if(isRecordMic){
	 					renderer.setRecordMic(isRecordMic);	
	 				}else if(isRecordExtPcm){
	 					renderer.setRecordExtraPcm(isRecordExtPcm, pcmChannels,pcmSampleRate,pcmBitRate);
	 				}
	 				
	 				ret=renderer.startDrawPad();
	 				
	 				if(ret==false){  //用中文注释.
	 					Log.e(TAG,"开启 DrawPad 失败, 或许是您之前的DrawPad没有Stop.");
	 				}
	 			}
         }else{
        	 Log.e(TAG,"开启 DrawPad 失败, 您设置的宽度和高度是:"+padWidth+" x "+padHeight+" 如果您是从一个Activity返回到当前Activity,希望再次预览, 可以看下我们setOnViewAvailable, 在PictureSetRealtimeActivity.java代码里有说明.");
         }
		 return ret;
	}
	/**
	 * 暂停drawpad的录制,这个方法使用在暂停录制后, 在当前画面做其他的一些操作,
	 * 不可用来暂停后跳入到别的Activity中.
	 */
	public void pauseDrawPadRecord()
	{
		if(renderer!=null){
			renderer.pauseRecordDrawPad();
		}
		isPauseRecord=true;
	}
	/**
	 * 恢复drawpad的录制.
	 * 如果刷新也是暂停状态, 同时恢复刷新.
	 */
	public void resumeDrawPadRecord()
	{
		if(renderer!=null){
			renderer.resumeRecordDrawPad();
		}
		isPauseRecord=false;
	}
	/**
	 * 是否在CameraLayer录制的同时, 录制mic的声音.  在drawpad开始前调用. 
	 * 
	 * 此方法仅仅使用在录像的时候, 同时录制Mic的场合.录制的采样默认是44100, 码率64000, 编码为aac格式.
	 * 录制的同时, 视频编码以音频的时间戳为参考.
	 * 可通过 {@link #stopDrawPad2()}停止,停止后返回mic录制的音频文件 m4a格式的文件, 
	 * @param record
	 */
	public void setRecordMic(boolean record)
	{
		if(renderer!=null && renderer.isRecording()){
			Log.e(TAG,"DrawPad is running. set Mic Error!");
		}else{
			isRecordMic=record;
		}
	}
	/**
	 * 是否在录制画面的同时,录制外面的push进去的音频数据 .
	 * 
	 * 适用在需要实时录制的视频, 如果您仅仅是对视频增加背景音乐等, 可以使用 {@link VideoEditor#executeVideoMergeAudio(String, String, String)}
	 * 来做处理.
	 * 
	 * 注意:当设置了录制外部的pcm数据后, 当前画板上录制的视频帧,就以音频的帧率为参考时间戳,从而保证音视频同步进行. 
	 * 故您在投递音频的时候, 需要严格按照音频播放的速度投递. 
	 * 
	 * 如采用外面的pcm数据,则视频在录制过程中,会参考音频时间戳,来计算得出视频的时间戳,
	 * 如外界音频播放完毕,无数据push,应及时stopDrawPad2
	 * 
	 * 可以通过 AudioLine 的getFrameSize()方法获取到每次应该投递多少个字节,此大小为固定大小, 每次投递必须push这么大小字节的数据,
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
	public boolean isRecording()
	{
		if(renderer!=null)
			return renderer.isRecording();
		else
			return false;
	}
	/**
    * 设置是否使用主视频的时间戳为录制视频的时间戳, 
    * 如果您传递过来的是一个完整的视频, 只是需要在此视频上做一些操作, 操作完成后,时长等于源视频的时长, 则使用主视频的时间戳, 如果视频是从中间截取一般开始的
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
	/**
	 * 设置当前图层对象layer 在DrawPad中所有图层队列中的位置, 您可以认为内部是一个ArrayList的列表, 先add进去的 的position是0, 后面增加的依次是1,2,3等等
	 * 可以通过Layer的getIndexLayerInDrawPad属性来获取当前图层的位置.
	 * 
	 * @param layer
	 * @param position
	 */
	public void changeLayerLayPosition(Layer layer,int position)
    {
		if(renderer!=null){
			renderer.changeLayerLayPosition(layer, position);
		}
    }
	/**
	 * 交换两个图层的位置.
	 * @param first
	 * @param second
	 */
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
	public TwoVideoLayer addTwoVideoLayer(int width, int height)
    {
		TwoVideoLayer ret=null;
	    
		if(renderer!=null)
			ret=renderer.addTwoVideoLayer(width, height);
		else{
			Log.e(TAG,"addTwoVideoLayer error render is not avalid");
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
			Log.e(TAG,"addVideoLayer error render is not avalid");
			return null;
		}
	}
	/**
	 * 增加一个相机图层. 建议使用 {@link DrawPadCameraView}
	 * @param isFaceFront   是否使用前置镜头
	 * @param filter 当前使用到的滤镜 ,如果不用, 则设置为null
	 * @return
	 */
	public CameraLayer addCameraLayer(boolean isFaceFront,GPUImageFilter filter)
	{
			CameraLayer ret=null;
			if(renderer!=null)
				if(filter!=null){
					ret=renderer.addCameraLayer(isFaceFront,filter);
				}else{
					ret=renderer.addCameraLayer(isFaceFront,null);
				}
			else{
				Log.e(TAG,"addCameraLayer error render is not avalid");
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
			if(renderer!=null)
				return renderer.addBitmapLayer(bmp,null);
			else{
				Log.e(TAG,"addBitmapLayer error render is not avalid");
				return null;
			}
		}else{
			Log.e(TAG,"addBitmapLayer error, bitmap is null");
			return null;
		}
	}
	public BitmapLayer addBitmapLayer(Bitmap bmp,GPUImageFilter filter)
	{
		if(bmp!=null)
		{
			//Log.i(TAG,"imgBitmapLayer:"+bmp.getWidth()+" height:"+bmp.getHeight());
			if(renderer!=null)
				return renderer.addBitmapLayer(bmp,filter);
			else{
				Log.e(TAG,"addBitmapLayer error render is not avalid");
				return null;
			}
		}else{
			Log.e(TAG,"addBitmapLayer error, bitmap is null");
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
	 * 增加一个gif图层.
	 * @param gifPath  gif的绝对地址,
	 * @return
	 */
	public GifLayer  addGifLayer(String gifPath)
	{
			if(renderer!=null)
				return renderer.addGifLayer(gifPath);
			else{
				Log.e(TAG,"addYUVLayer error! render is not avalid");
				return null;
			}
	}
	
	/**
	 * 增加一个gif图层, 
	 * 
	 * resId 来自apk中drawable文件夹下的各种资源文件, 我们会在GifLayer中拷贝这个资源到默认文件夹下面, 然后作为一个普通的gif文件来做处理,使用完后, 会在Giflayer
	 * 图层释放的时候, 删除.
	 * 
	 * @param resId 来自apk中drawable文件夹下的各种资源文件.
	 * @return
	 */
	public GifLayer  addGifLayer(int resId)
	{
			if(renderer!=null)
				return renderer.addGifLayer(resId);
			else{
				Log.e(TAG,"addGifLayer error! render is not avalid");
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
				Log.e(TAG,"addMVLayer error render is not avalid");
				return null;
			}
	}
	 /**
     * 是否在mv好了之后, 直接去显示, 如果不想直接显示, 可以先设置isShow=false,然后在需要显示的使用,
     * 调用 {@link MVLayer #setPlayEnable(boolean)}, 此方法暂时只能被调用一次.
     * 
     * @param srcPath
     * @param maskPath
     * @param isplay  
     * @return
     */
	public MVLayer addMVLayer(String srcPath,String maskPath, boolean isplay)
	{
			if(renderer!=null)
				return renderer.addMVLayer(srcPath,maskPath,isplay);
			else{
				Log.e(TAG,"addMVLayer error render is not avalid");
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
				Log.e(TAG,"addViewLayer error render is not avalid");
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
				Log.e(TAG,"addCanvasLayer error render is not avalid");
				return null;
			}
	 }
	 /**
	  * 增加一个yuv图层, 让您可以把YUV数据输入进来,当前仅支持NV21的格式
	  * 
	  * yuv数据,可以是别家SDK处理后的结果, 或Camera的onPreviewFrame回调的数据, 或您本地的视频数据.也可以是您本地的视频数据.
	  * 
	  * @param width
	  * @param height
	  * @return
	  */
	 public YUVLayer addYUVLayer(int width,int height)
	 {
			if(renderer!=null)
				return renderer.addYUVLayer(width,height);
			else{
				Log.e(TAG,"addCanvasLayer error render is not avalid");
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
	 * 
	 * 注意: 这里内部会在切换的时候, 会销毁 之前的滤镜对象, 然后重新增加, 故您不可以把同一个滤镜对象再次放到进来, 您如果还想使用之前的滤镜,则应该重新创建一个对象.
	 * 
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
	   /**
	    * 为一个图层切换多个滤镜. 即一个滤镜处理完后的输出, 作为下一个滤镜的输入.
	    * 
	    * filter的列表, 是先add进去,最新渲染, 把第一个渲染的结果传递给第二个,第二个传递给第三个,以此类推.
	    * 
	    * 注意: 这里内部会在切换的时候, 会销毁 之前的列表中的所有滤镜对象, 然后重新增加, 故您不可以把同一个滤镜对象再次放到进来,
	    * 您如果还想使用之前的滤镜,则应该重新创建一个对象.
	    * 
	    * @param layer  图层对象,
	    * @param filters  滤镜数组; 如果设置为null,则不增加滤镜.
	    * @return
	    */
	   public boolean  switchFilterList(Layer layer, ArrayList<GPUImageFilter> filters) {
	    	if(renderer!=null){
	    		return renderer.switchFilterList(layer, filters);
	    	}
	    	return false;
	    }
	   
	   //----------------------------------------------
	   private boolean isCheckBitRate=true;
	   
	   private boolean isCheckPadSize=true;
	   /**
	    * 是否在开始运行DrawPad的时候,检查您设置的码率和分辨率是否正常.
	    * 
	    * 默认是检查, 如果您清楚码率大小的设置,请调用此方法,不再检查.
	    *  
	    */
	   public void setNotCheckBitRate()
	   {
		   isCheckBitRate=false;
	   }
	   /**
	    * 是否在开始运行DrawPad的时候, 检查您设置的DrawPad宽高是否是16的倍数.
	    * 默认是检查.
	    */
	   public void setNotCheckDrawPadSize()
	   {
		   isCheckPadSize=false;
	   }
}
