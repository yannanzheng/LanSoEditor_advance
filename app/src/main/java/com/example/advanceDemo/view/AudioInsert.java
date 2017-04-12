package com.example.advanceDemo.view;

import android.content.Context;
import android.util.Log;

import com.lansosdk.box.AudioInsertManager;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;

/**
 *  使用在给主音频增加一段别的音频, 可以设置两者在混合时的音量. 
 *  
 *   比如给一个视频中的音频部分增加别的搞笑音乐等.
 *
 */
public class AudioInsert {

	private AudioInsertManager insertMng;
	public AudioInsert(Context ctx)
	{
		insertMng=new AudioInsertManager(ctx);
	}

	/**
	  * 增加主音频文件, 格式是mp3或aac格式.
	  * 如果您想给视频的音频部分,增加音频, 可以先执行如下命令,得到音频部分, 注意这里当前仅仅执行双声道混合.
	  * VideoEditor et=new VideoEditor();
	  * String audioPath=SDKFileUtils.createAACFileInBox();
		et.executeDeleteVideo("/sdcard/2x.mp4", audioPath);
			  
	  * @param audioPath  音频的完整路径
	  * @param volume  在和别的声音混淆的时候的音量, 比如把音量调小一些, 默认是1.0f, 大于1,0则是放大, 小于则是降低
	  * @param needrelease  处理完毕后, 是否要删除当前文件.
	  */
	 public boolean addMainAudio(String audioPath) 
	 {
			return insertMng.addMainAudio(audioPath);
	 }
	 /**
	  * 增加一个 从音频文件, 格式是mp3或aac格式.
	  * @param srcPath   音频的完整路径
	  * @param startTimeMs  设置从主音频的哪个时间点开始插入.单位毫秒.
	  * @param volume  在和别的声音混淆的时候的音量, 比如把音量调小一些, 默认是1.0f, 大于1,0则是放大, 小于则是降低
	  */
	 public boolean addSubAudio(String srcPath,long startTimeMs,long durationMs,float mainVolume,float volume) 
	 {
			return  insertMng.addSubAudio(srcPath, startTimeMs, durationMs, mainVolume,volume);
	 }
	 /**
	  * 增加主音频文件, 格式是pcm格式, 即原始采样点.
	  * 
	  * @param pcmPath  pcm文件的完整路径
	  * @param sampleRate  采样率
	  * @param channels  通道号, 当前仅仅支持双通道
	  * @param duration  pcm的总长度, 可以通过对原音频文件进行MediaInfo得到
	  * @param volume  在和别的声音混淆的时候的音量, 比如把音量调小一些, 默认是1.0f, 大于1,0则是放大, 小于则是降低
	  * @param needrelease  处理完毕后, 是否要释放.
	  */
   public boolean addMainAudioPCM(String pcmPath,int sampleRate,int channels,int duration,float volume,boolean needrelease) 
   {
	   return insertMng.addMainAudioPCM(pcmPath, sampleRate, channels, duration, volume, needrelease);
   }
  
   /**
    * 增加从音频文件, 格式是pcm格式, 即原始采样点. 可以增加多个.
    * @param srcPath   pcm文件的完整路径
    * @param sampleRate  采样率
    * @param channels  通道号, 当前仅仅支持双通道
    * @param startTimeMs    设置从主音频的哪个时间点开始插入.单位毫秒.
    * @param durationMS   pcm的总长度, 可以通过对原音频文件进行MediaInfo得到
    * 
    * @param volume   在和别的声音混淆的时候的音量, 比如把音量调小一些, 默认是1.0f, 大于1,0则是放大, 小于则是降低
    * 
    * @param needrelease
    */
   public boolean addSubAudioPCM(String srcPath,int sampleRate,int channels,long startTimeMs,long durationMS,float mainVolume,float volume,boolean needrelease) 
   {
		 return  insertMng.addSubAudioPCM(srcPath, sampleRate, channels, startTimeMs, durationMS,mainVolume, volume, needrelease);
   }
   /**
    * 开始执行. 这里是阻塞执行, 一直到执行完毕才退出. 
    * 因为代码中有音频的编码和解码, 如果手机支持 音频硬件加速, 则会执行很快. 
    * 建议异步执行.
    * @return
    */
   public String executeAudioMix()
   {
	   if(insertMng!=null){
		 return   insertMng.executeAudioMix();
	   }else{
		   return  null;
	   }
   }
   
	/**
	 * 一下为测试代码.
	 *  VideoEditor et=new VideoEditor();
			  et.executeDeleteVideo("/sdcard/2x.mp4", "/sdcard/2x_audio.aac");
			  
			  
			  AudioInsertManager mixMng=new AudioInsertManager(getApplicationContext());
			  mixMng.addMainAudio("/sdcard/2x_audio.aac",1.0f, true);
			  
			  
			  //从第4秒出开始增加, 增加3秒的时长,音量为6倍.
			  mixMng.addSubAudio("/sdcard/hongdou10s.mp3",4000,3000,6.0f,true);
			  //增加一段pcm的音频文件,从4秒出增加
			  mixMng.addSubAudioPCM("/sdcard/wuya.pcm", 44100, 2, 4000,2500,2.0f,true);
			  
			  //开始执行...执行结束,返回混合后的文件.
			   String dstMix= mixMng.executeAudioMix();
			   Log.i(TAG,"目标音频文件是:"+dstMix);
	 */
}
