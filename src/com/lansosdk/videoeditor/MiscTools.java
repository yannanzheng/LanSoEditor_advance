package com.lansosdk.videoeditor;

import com.lansosdk.box.AudioMix;

/**
 * 高级版用的一些其他方法.
 *
 */
public class MiscTools {

	
	
	
	 /**
	  * 调整音频采样点的音量, 把音量放大一些或 降低一些.
	  * 
	  * @param channelNUmber 采样点的通道数, 当前只支持单声道和双声道, 即这里要么是1,要么是2, 没有其他的值.
	  * @param sample1   音频1 的采样数组,  
	  * @param volume  要调整的音量,
	  * @param mixResultOut  调整后的输出结果, 由外部创建的数组.
	  * @return 混合成功,返回0, 失败返回-1;
	  */
	public static  int  adjustSampleVolume(int channelNUmber,byte[] sample1,
			float volume,byte[] mixResultOut)
	{
		return AudioMix.adjustSampleVolume(channelNUmber, sample1, volume, mixResultOut);
	}
	
			   
	/**
	 * 音频混合. 两个音频采样点的混合, 是音频的原始数据.
	 * 
	 * 原始音频数据 采样点的混合, 建议音频1,和音频2采用同一个采样率,推荐44100.   
	 * 
	 * @param channelNUmber  采样点的通道数, 当前只支持单声道和双声道, 即这里要么是1,要么是2, 没有其他的值.
	 * 
	 * @param sample1  音频1 的采样数组, 	 !!!因是采样点的混合,两个数组长度一定要相等,并 每个采样点是2个字节数据.!!!!!! 
	 * @param sample2  音频2 的采样数组,   !!!因是采样点的混合,两个数组长度一定要相等,并 每个采样点是2个字节数据.!!!!!! 
	 * 
	 * @param sample1Volume  音频1的在混合的同时,调整它的音量, 为1.0,则音量不变, 小于1,则减小音量, 大于1,则放大音量.
	 * @param sample2Volume  音频2的在混合时,调整它的音量, 为1.0,则音量不变, 小于1,则减小音量, 大于1,则放大音量.
	 * 
	 * @param mixResultOut  混合后, 输出的结果.由外部创建的数组.
	 * 
	 * @return  混合成功,返回0, 失败返回-1;
	 */
	public static  int  audioSampleMix(int channelNUmber,byte[] sample1,byte[] sample2,
			float sample1Volume,float sample2Volume,byte[] mixResultOut)
	{
		return AudioMix.audioSampleMix(channelNUmber, sample1, sample2, sample1Volume, sample2Volume, mixResultOut);
	}
	/*
	 * -----------------------------一下是测试.-------------------------------------------------------------------
	void testAudioMix() {
		
	    byte[] sample1 = new byte[44100*4];  //双通道,
	    byte[] sample2 = new byte[44100*4];  //双通道,
	    byte[] resultMixBuffer = new byte[44100*4];  //双通道,

	    try {
	        InputStream inputStream = new FileInputStream(new File("/sdcard/h5.pcm"));
	        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
	        DataInputStream stream1 = new DataInputStream(bufferedInputStream);

	        
	        
	        InputStream inputStream1 = new FileInputStream(new File("/sdcard/n3.pcm"));
	        BufferedInputStream bufferedInputStream1 = new BufferedInputStream(inputStream1);
	        DataInputStream stream2 = new DataInputStream(bufferedInputStream1);

	        
	        //目标文件.
	        OutputStream outputStream = new FileOutputStream(new File("/sdcard/v33.pcm"));
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);
            
            
	        while (stream1.available() > 0 && stream2.available() > 0) {
	        	
	        	int length=stream1.read(sample1);
	        	stream2.read(sample2);
	        	
	        	//AudioMix.audioSampleMix(2, sample1, sample2, 2.0f, 0.1f, resultMixBuffer);
	        	AudioMix.adjustSampleVolume(2, sample1, 3.0f, resultMixBuffer);
	        	
	        	Log.i(TAG,"开始写入."+length);
	        	dataOutputStream.write(resultMixBuffer);
	        }
	        Log.i("TAG","混合完毕!");
	        stream1.close();
	        stream2.close();
	        dataOutputStream.close();
	          
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } catch (IllegalStateException e) {
	        e.printStackTrace();
	    }
	}
	 * */
}
