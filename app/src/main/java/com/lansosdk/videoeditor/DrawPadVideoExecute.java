package com.lansosdk.videoeditor;

import java.util.ArrayList;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import android.content.Context;
import android.graphics.Bitmap;

import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.CanvasLayer;
import com.lansosdk.box.DataLayer;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.DrawPadVideoRunnable;
import com.lansosdk.box.FileParameter;
import com.lansosdk.box.GifLayer;
import com.lansosdk.box.Layer;
import com.lansosdk.box.MVLayer;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadErrorListener;
import com.lansosdk.box.onDrawPadOutFrameListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadThreadProgressListener;

/**
 * 此类是对老版本的 DrawPadVideoExecute的一个封装, 并为每个方法增加了详细的使用说明, 以方便您调用具体的方法.
 * 
 * 
 * 2017年5月4日12:35:51:
 * 把DrawPadVideoExecute更改名字为DrawPadVideoRunnable
 *  DrawPadVideoRunnable 我们封装成一个异步的线程. 用来处理视频.
 *  实际使用中, 不建议你另外再开线程处理,不建议同时创建多个DrawPadVideoExecute来处理,
 *  因为手机硬件编码器,当前大部分同一时刻只支持一路编码.即使您开多个,处理速度也不会很快.
 *
 */
public class DrawPadVideoExecute {
	
	private DrawPadVideoRunnable  renderer=null;
	private int padWidth,padHeight;
	/**
	 * 
	 * @param ctx 语境,android的Context
	 * @param srcPath  主视频的路径
	 * @param padwidth DrawPad的的宽度
	 * @param padheight  DrawPad的的高度
	 * @param bitrate 编码视频所希望的码率,比特率.
	 * @param filter  为视频图层增加一个滤镜 如果您要增加多个滤镜,则用{@link #switchFilterList(Layer, ArrayList)}
	 * @param dstPath  视频处理后保存的路径.
	 */
   public DrawPadVideoExecute(Context ctx,String srcPath,int padwidth,int padheight,int bitrate,GPUImageFilter filter,String dstPath) 
   {
	   if(renderer==null){
		   renderer=new DrawPadVideoRunnable(ctx, srcPath, padwidth, padheight, bitrate, filter, dstPath);
	   }  
	   this.padWidth=padwidth;
	   this.padHeight=padheight;
   }
   /**
    * 只比上面少了一个码率的设置.
    * @param ctx
    * @param srcPath
    * @param padwidth
    * @param padheight
    * @param filter
    * @param dstPath
    */
   public DrawPadVideoExecute(Context ctx,String srcPath,int padwidth,int padheight,GPUImageFilter filter,String dstPath) 
   {
	   if(renderer==null){
		   renderer=new DrawPadVideoRunnable(ctx, srcPath, padwidth, padheight, 0, filter, dstPath);   
	   }  
	   this.padWidth=padwidth;
	   this.padHeight=padheight;
   }
   /**
    *  Drawpad后台执行, 可以指定开始时间.
    *  因视频编码原理, 会定位到 [指定时间]前面最近的一个IDR刷新帧, 然后解码到[指定时间],容器才开始渲染视频,中间或许有一些延迟.
    * @param ctx
    * @param srcPath  主视频的完整路径.
    * @param startTimeMs  开始时间. 单位毫秒
    * @param padwidth  容器宽度.
    * @param padheight 容器高度.
    * @param bitrate 容器编码的码率
    * @param filter  为这视频设置一个滤镜, 如果您要增加多个滤镜,则用{@link #switchFilterList(Layer, ArrayList)}
    * @param dstPath  处理后保存的目标文件.
    */
   public DrawPadVideoExecute(Context ctx,String srcPath,long startTimeMs,int padwidth,int padheight,int bitrate,GPUImageFilter filter,String dstPath) 
   {
	   if(renderer==null){
		   renderer=new DrawPadVideoRunnable(ctx, srcPath,startTimeMs, padwidth, padheight, bitrate, filter, dstPath);
	   }
	   this.padWidth=padwidth;
	   this.padHeight=padheight;
   }
   /**
    * 相对于上面,只是少了码率.
    * @param ctx
    * @param srcPath
    * @param startTimeMs
    * @param padwidth
    * @param padheight
    * @param filter
    * @param dstPath
    */
   public DrawPadVideoExecute(Context ctx,String srcPath,long startTimeMs,int padwidth,int padheight,GPUImageFilter filter,String dstPath) 
   {
	   if(renderer==null){
		   renderer=new DrawPadVideoRunnable(ctx, srcPath,startTimeMs, padwidth, padheight,0, filter, dstPath);
	   }
	   this.padWidth=padwidth;
	   this.padHeight=padheight;
   }
   /**
    * 增加了FileParameter类, 
    * 其中FileParameter的配置是:
	 * 
		FileParameter  param=new FileParameter();
		if(param.setDataSoure(mVideoPath)){
			
			 * 设置当前需要显示的区域 ,以左上角为0,0坐标. 
			 * 
			 * @param startX  开始的X坐标, 即从宽度的什么位置开始
			 * @param startY  开始的Y坐标, 即从高度的什么位置开始
			 * @param cropW   需要显示的宽度
			 * @param cropH   需要显示的高度.
			param.setShowRect(0, 0, 300, 200);
			param.setStartTimeUs(5*1000*1000); //从5秒处开始处理, 当前仅在后台处理时有效.
			videoMainLayer=mDrawPadView.addMainVideoLayer(param,new GPUImageSepiaFilter());
		}
		
    * @param ctx
    * @param filebox
    * @param padwidth
    * @param padheight
    * @param filter
    * @param dstPath
    */
   public DrawPadVideoExecute(Context ctx,FileParameter fileParam,int padwidth,int padheight,GPUImageFilter filter,String dstPath) 
   {
	   if(renderer==null){
		   renderer=new DrawPadVideoRunnable(ctx, fileParam, padwidth, padheight,0, filter, dstPath);
	   }
	   this.padWidth=padwidth;
	   this.padHeight=padheight;
   }
   public void setFastVideoMode(boolean is)
   {
	   if(renderer!=null){
		   renderer.setFastVideoMode(is);
	   }
   }
  /**
   * 启动DrawPad,开始执行.
   * 
   * 开启成功,返回true, 失败返回false
   */
   public boolean startDrawPad() {
	   if(renderer!=null && renderer.isRunning()==false){
		   return renderer.startDrawPad();
	   }
	   return false;
   }
   
   public void stopDrawPad() {
   	// TODO Auto-generated method stub
	   if(renderer!=null && renderer.isRunning()){
		   renderer.stopDrawPad();
	   }
   }
   /**
	 * 调节视频的速度
	 * 支持在任意时刻来变速; 你可以前3秒用一个速度, 中间3秒正常, 最后几秒用一个速度.
	 * 
	 *  当前暂时不支持音频, 只是视频的加减速, 请注意!!!
	 *  
	 *  建议5个等级: 0.25f,0.5f,1.0f,1.5f,2.0f; 
	 *  其中 0.25是放慢4倍;  0.5是放慢2倍; 1.0是采用和预览同样的速度; 1.5是加快一半, 2.0是加快2倍.
	 * @param speed  速度系数,
	 * 
	 * 
	 * 测试代码是:
	 * 	if(currentTimeUs> 15*1000000){
					mDrawPad.adjustEncodeSpeed(1.0f);
				}else if(currentTimeUs>6000000 && bitmapLayer!=null){
					mDrawPad.adjustEncodeSpeed(0.25f);
					v.removeLayer(bitmapLayer);
				}else if(currentTimeUs>3000000 && bitmapLayer!=null)  {
					bitmapLayer.setScale(2.0f);
					mDrawPad.adjustEncodeSpeed(2.0f);
				}
	 */
   public void adjustEncodeSpeed(float speed)
	{
		if(renderer!=null){
			renderer.adjustEncodeSpeed(speed);
		}
	}
  /**
   * 
   *设置是否使用主视频的时间戳为录制视频的时间戳;
   *
   *设置是否使用主视频的时间戳为录制视频的时间戳, 如果您传递过来的是一个完整的视频, 只是需要在此视频上做一些操作,
   *操作完成后,时长等于源视频的时长, 则建议使用主视频的时间戳, 如果视频是从中间截取一般开始的则不建议使用,
   *
   *默认是使用主视频时间.
   *
   *此方法,在DrawPad开始前调用.
   */
   public void setUseMainVideoPts(boolean use)
   {
	   if(renderer!=null && renderer.isRunning()==false){
		   renderer.setUseMainVideoPts(use);
	   }
   }
   /**
    * 设置画面刷新模式, 当前有两种模式, 视频刷新/自动刷新. 
    * 
    * 如果你处理一个完整的视频,只是处理视频的滤镜/缩放/增加其他图层等等, 建议用 视频刷新模式.
    * 默认不设置,则为视频刷新模式;
    * 
    * 视频刷新 是指: 当这一帧视频解码完成后,才刷新DrawPad;
    * 自动刷新 是指: DrawPad自动根据您设置的帧率来刷新, 不判断视频有没有解码完成;
    * 
    * @param mode
    * @param fps  一秒钟的帧率, 在自动刷新时有效.
    */
   public void setUpdateMode(DrawPadUpdateMode mode, int fps) {
	   if(renderer!=null && renderer.isRunning()==false){
		   renderer.setUpdateMode(mode, fps);
	   }
   }
   /**
    * 在您配置了 OutFrame, 要输出每一帧的时候, 是否要禁止编码器.
    * 当你只想要处理后的 数据, 而暂时不需要编码成最终的目标文件时, 把这里设置为true.
    * 默认是false;
    * @param dis
    */
   public void setDisableEncode(boolean dis)
   {
	   if(renderer!=null && renderer.isRunning()==false){
		   renderer.setDisableEncode(dis);
	   }
   }
   /**
	 * DrawPad每执行完一帧画面,会调用这个Listener,返回的timeUs是当前画面的时间戳(微妙),
	 *  可以利用这个时间戳来做一些变化,比如在几秒处缩放, 在几秒处平移等等.从而实现一些动画效果.
	 * @param currentTimeUs  当前DrawPad处理画面的时间戳.,单位微秒.
	 */
	public void setDrawPadProgressListener(onDrawPadProgressListener listener){
		if(renderer!=null){
			renderer.setDrawPadProgressListener(listener);
		}
	}
	/**
	 * 方法与   onDrawPadProgressListener不同的地方在于:
	 * 此回调是在DrawPad渲染完一帧后,立即执行这个回调中的代码,不通过Handler传递出去,你可以精确的执行一些下一帧的如何操作.
	 * 故不能在回调 内增加各种UI相关的代码.
	 */
	public void setDrawPadThreadProgressListener(onDrawPadThreadProgressListener listener)
	{
		if(renderer!=null){
			renderer.setDrawPadThreadProgressListener(listener);
		}
	}
	
	/**
	 * DrawPad执行完成后的回调.
	 * @param listener
	 */
	public void setDrawPadCompletedListener(onDrawPadCompletedListener listener){
		if(renderer!=null){
			renderer.setDrawPadCompletedListener(listener);
		}
	}
	/**
	 * 设置当前DrawPad运行错误的回调监听.
	 * @param listener
	 */
	public void setDrawPadErrorListener(onDrawPadErrorListener listener)
	{
		if(renderer!=null){
			renderer.setDrawPadErrorListener(listener);
		}
	}
	/**
	 * 设置每处理一帧的数据监听, 等于把当前处理的这一帧的画面拉出来,
	 * 您可以根据这个画面来自行的编码保存, 或网络传输.
	 * 
	 * 建议在这里拿到数据后, 放到queue中, 然后在其他线程中来异步读取queue中的数据, 请注意queue中数据的总大小, 要及时处理和释放, 以免内存过大,造成OOM问题
	 * 
	 * @param listener 监听对象
	 */
	public void setDrawPadOutFrameListener(onDrawPadOutFrameListener listener)
	{
		if(renderer!=null){
			renderer.setDrawpadOutFrameListener(padWidth, padHeight, 1,listener);
		}
	}
	public void setDrawPadOutFrameListener(int width,int height,onDrawPadOutFrameListener listener)
	{
		if(renderer!=null){
			renderer.setDrawpadOutFrameListener(width, height, 1,listener);
		}
	}
	/**
	 * 设置setOnDrawPadOutFrameListener后, 你可以设置这个方法来让listener是否运行在Drawpad线程中.
	 * 如果你要直接使用里面的数据, 则不用设置, 如果你要开启另一个线程, 把listener传递过来的数据送过去,则建议设置为true;
	 * @param en
	 */
	public void setOutFrameInDrawPad(boolean en){
		if(renderer!=null){
			renderer.setOutFrameInDrawPad(en);
		}
	}
  /**
   * 把当前图层放到DrawPad的最底部.
   * DrawPad运行后,有效.
   * @param pen
   */
   public void bringToBack(Layer layer)
   {
	   if(renderer!=null && renderer.isRunning()){
		   renderer.bringToBack(layer);
	   }
   }
   /**
    * 把当前图层放到最顶层
    * @param layer
    */
   public void bringToFront(Layer layer)
   {
	   if(renderer!=null && renderer.isRunning()){
		   renderer.bringToFront(layer);
	   }
   }
   /**
    *  改变指定图层的位置. 
    * @param layer
    * @param position
    */
   public void changeLayerPosition(Layer layer,int position)
   {
	   if(renderer!=null && renderer.isRunning()){
		   renderer.changeLayerPosition(layer,position);
	   }
   }
   /**
    * 交换两个图层的位置.
    * @param first
    * @param second
    */
   public void swapTwoLayerPosition(Layer first,Layer second)
   {
	   if(renderer!=null && renderer.isRunning()){
		   renderer.swapTwoLayerPosition(first,second);
	   }
   }
   
   /**
    * 获取当前容器中有多少个图层.
    * 
    * @return
    */
   public int getLayerSize()
   {
	   	if(renderer!=null){
	   		return renderer.getLayerSize();
	   	}else{
	   		return 0;
	   	}
   }
   /**
    * 得到当前DrawPadVideoRunnable中设置的视频图层对象.
    * @return
    */
   public VideoLayer getMainVideoLayer()
   {
	   if(renderer!=null && renderer.isRunning()){
		  return  renderer.getMainVideoLayer();
	   }else{
		   return null;
	   }
   }
   /**
    * 在处理中插入一段其他音频,比如笑声,雷声, 各种搞怪声音等. 
    * 注意,这里插入的声音是和视频原有的声音混合后, 形成新的音频,而不是替换原来的声音, 如果您要替换原理的声音,则建议用{@link VideoEditor}中的相关方法来做.
    * 如果原视频没有音频部分,则默认创建一段无声的音频和别的音频混合.
    * 
    * 如果增加了其他声音, 则会在内部合成声音, 合成后, 您设置的目标文件中自然就有了声音, 外界无需另外addAudio的操作, 这是和别的Drawpad操作不同之处.
    * 
    * 成功增加后, 会在DrawPad开始运行时,开启一个线程去编解码音频文件, 如果您音频过大,则可能需要一定的时间来处理完成, 处理完毕后.
    * 此方法在DrawPad开始前调用.
    * 
    * 此方法可以被多次调用, 从而增加多段其他的音频.
    * 
    * @param srcPath  音频的完整路径,当前支持mp3格式和aac格式.
    * 
    * @param startTimeMs 设置从主音频的哪个时间点开始插入.单位毫秒.
    * @param durationMs   把这段声音多长插入进去.
    * 
    * @param volume  插入时,当前音频音量多大  默认是1.0f, 大于1,0则是放大, 小于则是降低
    * @return  插入成功, 返回true, 失败返回false
    */
   	 public boolean addSubAudio(String srcPath,long startTimeMs,long durationMs,float volume) 
	 {
		 if(renderer!=null && renderer.isRunning()==false){
			return renderer.addSubAudio(srcPath, startTimeMs, durationMs,volume);
		 }else{
			 return false;
		 }
	 }
   	@Deprecated
 	 public boolean addSubAudio(String srcPath,long startTimeMs,long durationMs,float mainvolume,float volume) 
 	 {
 		 if(renderer!=null && renderer.isRunning()==false){
 			return renderer.addSubAudio(srcPath, startTimeMs, durationMs,volume);
 		 }else{
 			 return false;
 		 }
 	 }
	 /**
	  * 增加图片图层.
	  * @param bmp
	  * @return
	  */
   public BitmapLayer  addBitmapLayer(Bitmap bmp)
   {
	   if(renderer!=null && renderer.isRunning()){
		   return renderer.addBitmapLayer(bmp,null);
	   }else{
		   return null;
	   }
   }
   /**
    * 增加数据图层,  DataLayer有一个{@link DataLayer#pushFrameToTexture(java.nio.IntBuffer)}可以把数据或图片传递到DrawPad中. 
    * 
    * @param dataWidth
    * @param dataHeight
    * @return
    */
   public DataLayer  addDataLayer(int dataWidth,int dataHeight)
   {
	   if(renderer!=null && renderer.isRunning()){
		 return  renderer.addDataLayer(dataWidth, dataHeight);
	   }else{
		   return null;
	   }
   }
   
   /**
    *  获取一个VideoLayer并返回, 在DrawPadVideoExecute中获取,则等于是在主视频中另外叠加上一个视频,并可以设置其滤镜效果.
    * @param videoPath  叠加视频的完整路径
    * @param vWidth 叠加视频的完整路径
    * @param vHeight 该视频的高度.
    * @param filter 对即将获取到的VideoLayer设置的滤镜对象.
    * @return  返回获取到的VideoLayer对象.
    */
   public VideoLayer addVideoLayer(String videoPath,int vWidth,int vHeight,GPUImageFilter filter)
   {
	   if(renderer!=null && renderer.isRunning()){
		  return renderer.addVideoLayer(videoPath, vWidth, vHeight, filter);
	   }else{
		   return null;
	   }
   }
   /**
    * 当mv在解码的时候, 是否异步执行; 
    * 如果异步执行,则MV解码可能没有那么快,从而MV画面会有慢动作的现象.
    * 如果同步执行,则视频处理会等待MV解码完成, 从而处理速度会慢一些,但MV在播放时,是正常的. 
    *  
    * @param srcPath  MV的彩色视频
    * @param maskPath  MV的黑白视频.
    * @param isAsync   是否异步执行.
    * @return
    */
   public MVLayer addMVLayer(String srcPath,String maskPath,boolean isAsync)
   {
	   if(renderer!=null && renderer.isRunning()){
		   return renderer.addMVLayer(srcPath, maskPath);
	   }else{
		   return null;
	   }
   }
   /**
    * 增加一个MV图层.
    * @param srcPath
    * @param maskPath
    * @return
    */
   public MVLayer addMVLayer(String srcPath,String maskPath)
   {
	   if(renderer!=null && renderer.isRunning()){
		   return renderer.addMVLayer(srcPath, maskPath);
	   }else{
		   return null;
	   }
   }
   /**
    * 增加gif图层
    * @param gifPath
    * @return
    */
   public GifLayer addGifLayer(String gifPath)
   {
	   if(renderer!=null && renderer.isRunning()){
		   return renderer.addGifLayer(gifPath);
	   }else{
		   return null;
	   }
   }
   /**
    * 增加gif图层
    * resId 来自apk中drawable文件夹下的各种资源文件, 我们会在GifLayer中拷贝这个资源到默认文件夹下面, 然后作为一个普通的gif文件来做处理,使用完后, 会在Giflayer
	 * 图层释放的时候, 删除.
    * @param resId
    * @return
    */
   public GifLayer addGifLayer(int resId)
   {
	   if(renderer!=null && renderer.isRunning()){
		   return renderer.addGifLayer(resId);
	   }else{
		   return null;
	   }
   }
   /**
    * 增加一个Canvas图层, 可以用Android系统的Canvas来绘制一些文字线条,颜色等. 可参考我们的的 "花心形"的举例
    * 因为Android的View机制是无法在非UI线程中使用View的. 但可以使用Canvas这个类工作在其他线程.
    * 因此我们设计了CanvasLayer,从而可以用Canvas来做各种Draw文字, 线条,图案等.
    * @return
    */
   public CanvasLayer addCanvasLayer() 
	{
	   if(renderer!=null && renderer.isRunning()){
		   return renderer.addCanvasLayer();
	   }else{
		   return null;
	   }
	}
   /**
    * 删除一个图层.
    * @param layer
    */
   public void removeLayer(Layer layer)
   {
	   if(renderer!=null && renderer.isRunning()){
		   renderer.removeLayer(layer);
	   }
   }
   /**
    * 已废弃.请用pauseRecord();
    */
   @Deprecated
   public void pauseRecordDrawPad()
   {
	   pauseRecord();
   }
   /**
    * 已废弃,请用resumeRecord();
    */
   @Deprecated
   public void resumeRecordDrawPad()
   {
	   resumeRecord();
   }
   private boolean mPauseRecord=false;
   protected boolean isCheckBitRate=true;
   
   protected boolean isCheckPadSize=true;
   /**
    * 暂停录制,
    * 使用在 : 开始DrawPad后, 需要暂停录制, 来增加一些图层, 然后恢复录制的场合.
    *  此方法使用在DrawPad线程中的 暂停和恢复的作用, 不能用在一个Activity的onPause和onResume中.
    *  
    */
   public void pauseRecord()
   {
	   if(renderer!=null && renderer.isRunning()){
		   renderer.pauseRecordDrawPad();
	   }else{
		   mPauseRecord=true;
	   }
   }
   /**
    * 恢复录制.
    * 此方法使用在DrawPad线程中的 暂停和恢复的作用, 不能用在一个Activity的onPause和onResume中.
    */
   public void resumeRecord(){
	   if(renderer!=null && renderer.isRunning()){
		   renderer.resumeRecordDrawPad();
	   }else{
		   mPauseRecord=false;
	   }
   }
   /**
    * 是否在录制.
    * @return
    */
   public boolean isRecording(){
	   if(renderer!=null && renderer.isRunning()){
		   return renderer.isRecording();
	   }else{
		   return false;
	   }
   }
     /**
      * DrawPad是否在运行
      * @return
      */
	 public boolean isRunning() 
	 {
		 if(renderer!=null){
			 return renderer.isRunning();
		 }else{
			 return false;
		 }
	 }
	  public void switchFilterTo(Layer layer,GPUImageFilter filter) {
	    	 if(renderer!=null && renderer.isRunning()){
		    		renderer.switchFilterTo(layer, filter);
		    	}
	     }
	 /**
	  * 切换滤镜
	  * 为一个图层切换多个滤镜. 即一个滤镜处理完后的输出, 作为下一个滤镜的输入.
	  * 
	  * filter的列表, 是先add进去,最新渲染, 把第一个渲染的结果传递给第二个,第二个传递给第三个,以此类推.
	  * 
	  * 注意: 这里内部会在切换的时候, 会销毁 之前的列表中的所有滤镜对象, 然后重新增加, 故您不可以把同一个滤镜对象再次放到进来,
	  * 您如果还想使用之前的滤镜,则应该重新创建一个对象.
	  * 
	  * @param layer
	  * @param filters
	  */
	 public void switchFilterList(Layer layer, ArrayList<GPUImageFilter> filters)
	 {
		 if(renderer!=null && renderer.isRunning()){
			 renderer.switchFilterList(layer, filters);
		 }
	 }
	 /**
	  * 释放DrawPad,方法等同于 {@link #stopDrawPad()}
	  * 只是为了代码标准化而做.
	  */
	 public void releaseDrawPad() {
		   	// TODO Auto-generated method stub
		 if(renderer!=null && renderer.isRunning()){
			 renderer.releaseDrawPad();
		 }
		 mPauseRecord=false;
		 renderer=null;
	 }
	   /**
	    * 停止DrawPad, 并释放资源.如果想再次开始,需要重新new, 然后start.
	    * 
	    * 注意:这里阻塞执行, 只有等待opengl线程执行退出完成后,方返回.
	    * 方法等同于 {@link #stopDrawPad()}
	  * 只是为了代码标准化而做.
	    */
	   public void release()
	   {
		  releaseDrawPad();
	   }
	   
	   /**
	    * 是否在开始运行DrawPad的时候,检查您设置的码率和分辨率是否正常.
	    * 
	    * 默认是检查, 如果您清楚码率大小的设置,请调用此方法,不再检查.
	    *  
	    */
	   public void setNotCheckBitRate()
	   {
		   if(renderer!=null && renderer.isRunning()==false){
			   renderer.setNotCheckBitRate();
		   }else{
			 isCheckBitRate=false;
		   }
	   }
	   /**
	    * 是否在开始运行DrawPad的时候, 检查您设置的DrawPad宽高是否是16的倍数.
	    * 默认是检查.
	    */
	   public void setNotCheckDrawPadSize()
	   {
		   if(renderer!=null && renderer.isRunning()==false){
			   renderer.setNotCheckDrawPadSize();
		   }else{
			   isCheckPadSize=false;
		   }
	   }
	   public void setCheckDrawPadSize(boolean check)
	   {
		   if(renderer!=null && renderer.isRunning()==false){
			   renderer.setCheckDrawPadSize(check);
		   }else{
			   isCheckPadSize=check;
		   }
	   }
}
