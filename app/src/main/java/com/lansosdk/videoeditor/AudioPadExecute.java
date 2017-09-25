package com.lansosdk.videoeditor;

import android.content.Context;
import android.media.MediaPlayer;

import com.lansosdk.box.AudioSource;
import com.lansosdk.box.AudioPad;
import com.lansosdk.box.SampleSave;
/**
 *  音频图层后的后台处理.
 *  可以设置音频时长, 也可以一段音频上增加其他音频效果.
 *  
 *  可以增加任意时长, 比如30秒, 60秒,5分钟等.
 *  
 *  当前处理后的音频编码成aac格式, 采样率是44100, 双通道, 64000码率.
 *  
 *  此类是用来在后台做音频混合处理使用.
 *   
 *  如果您仅仅用来做音频拼接, 可以采用 {@link AudioConcat}来做.
 *  当前的音频格式支持 MP3, AAC(m4a后缀), 采样率为44100, 通道数为2,其他格式暂不支持,请注意
 *  当前的音频格式支持 MP3, AAC(m4a后缀), 采样率为44100, 通道数为2,其他格式暂不支持,请注意
 *  当前的音频格式支持 MP3, AAC(m4a后缀), 采样率为44100, 通道数为2,其他格式暂不支持,请注意
 *  当前的音频格式支持 MP3, AAC(m4a后缀), 采样率为44100, 通道数为2,其他格式暂不支持,请注意
 */
public class AudioPadExecute {
	
	private static final String TAG = "AudioPadExecute";
	AudioPad   sampleMng;
	
	/**
	 * 构造方法,  
	 * @param ctx
	 * @param dstPath 因编码后的为aac格式, 故此路径的文件后缀需m4a或aac; 比如 "/sdcard/testAudioPad.m4a"
	 */
	public AudioPadExecute(Context ctx, String dstPath)
	{
		sampleMng=new AudioPad(ctx,dstPath);
	}
	/**
	 * 给AudioPad 增加一整段音频.
	 * 开始线程前调用.
	 * 
	 * 增加后, AudioPad会以音频的总长度为pad的长度, 其他增加的音频则是和这个音频的某一段混合.
	 * @param mainAudio
	 * @param volume   初始音量, 默认是1.0f, 大于1.0为放大, 小于1.0为缩小.
	 * @return  返回增加好的这个音频的对象, 可以根据这个来实时调节音量.
	 */
	public AudioSource setAudioPadSource(String mainAudio,float volume)
	{
		 if(sampleMng!=null){
			 return sampleMng.addMainAudio(mainAudio,volume);
		 }else{
			 return null;
		 }
	}
	/**
	 * 设置 音频处理的总长度.单位秒.
	 * 开始线程前调用.
	 * 
	 * 如果您只想在 一整段音乐上增加别的音频,可以用{@link #setAudioPadSource(String)}
	 * @return
	 */
	public AudioSource setAudioPadLength(float  duration)
	{
		 if(sampleMng!=null){
			 return sampleMng.addMainAudio(duration);	 
		 }else{
			 return null;
		 }
		  
	}
	 /**
	  * 增加一个其他音频
	  * (可以反复增加多段音频)
	  *  开始线程前调用.
	  * @param srcPath 音频的完成路径地址
	  * @param startTimeMs  从AudioPad的什么时候开始增加, 可以随意增加, 如果中间有空隙,则默认无声.
	  * @param endTimeMs  到AudioPad的哪个时间段停止, 如果是-1, 则一直放入, 直到当前音频处理完.
	  * @param volume
	  * @return
	  */
	 public AudioSource addSubAudio(String srcPath,long startTimeMs,long endTimeMs,float volume) 
	 {
		 if(sampleMng!=null){
			 return sampleMng.addSubAudio(srcPath, startTimeMs, endTimeMs, volume);	 
		 }else{
			 return null;
		 }
	 }
	 public boolean start()
	 {
		 if(sampleMng!=null){
			 return sampleMng.start();
		 }else{
			 return false;
		 }
	 }
	 /**
	  * 等待执行完毕.
	  */
	 public void waitComplete()
	 {
		 if(sampleMng!=null){
			 sampleMng.joinSampleEnd();
		 }
	 }
	 public void stop()
	 {
		 if(sampleMng!=null){
			 sampleMng.stop();
		 }
	 }
	 public void release()
	 {
		 if(sampleMng!=null){
			 sampleMng.release();
			 sampleMng=null;
		 }
	 }
	 /**
	  * 测试代码如下.
	    	AudioPadExecute   audioPad=new AudioPadExecute(getApplicationContext(),"/sdcard/i8.m4a");
	    	
	    	audioPad.setAudioPadLength(60.0f);  //定义生成一段15秒的声音./或者你可以把某一个音频作为一个主音频
	    	
	    	audioPad.addSubAudio("/sdcard/audioPadTest/du15s_44100_2.mp3", 0, 3*1000,1.0f);  //在这15内, 的前3秒增加一个声音
	    	audioPad.addSubAudio("/sdcard/audioPadTest/hongdou10s_44100_2.mp3", 3*1000, 6*1000,1.0f); //中间3秒增加一段
	    	audioPad.addSubAudio("/sdcard/audioPadTest/niu30s_44100_2.m4a", 10*1000, -1,1.0f);  //最后3秒增加一段.
	    	
	    	
	    	audioPad.start();  //开始运行 ,另开一个线程,异步执行.
	    	
	    	audioPad.waitComplete();
	    	
	    	audioPad.release();   //释放(内部会检测是否执行完, 如没有,则等待执行完毕).
	    	
	    	MediaPlayer  player=new MediaPlayer();
	    	try {
				player.setDataSource("/sdcard/i8.m4a");
				player.prepare();
				player.start();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	  */
}

