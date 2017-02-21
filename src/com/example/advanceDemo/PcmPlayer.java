package com.example.advanceDemo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.lansosdk.box.AudioLine;
import com.lansosdk.box.onAudioLineStartedListener;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class PcmPlayer{

	private final static String TAG = "PcmPlayer";
	
	private PlayPCMThread playThread;
	
	private boolean isRunning;
	private String playFile;
	private AudioLine audioLine;
	private int sampleRate=44100;
	private int channels=2; 
	/**
	 * 
	 * @param pcmPath  pcm的路径
	 * @param channels  通道数, 单声道为1, 双声道为2, 如是mp3或aac文件,可根据MediaInfo获得
	 * @param samplerate 采样率  如是mp3或aac文件,可根据MediaInfo获得
	 * @param line  是否需要投递到AudioLine中
	 */
	public PcmPlayer(String pcmPath,int channels,int samplerate,AudioLine line)
	{
		playFile=pcmPath;
		audioLine=line;
		this.channels=channels;
		sampleRate=samplerate;
	}
	public void prepare()
	{
		if (playThread == null)
		{
			playThread = new PlayPCMThread();
			isRunning=true;
		}
		if(audioLine!=null){
			//音频开始后, 接着这里开始播放音频.
			audioLine.setOnAudioLineStartedListener(new onAudioLineStartedListener() {
				
				@Override
				public void onStarted(AudioLine a) {
					// TODO Auto-generated method stub
					playThread.start();
					Log.i(TAG,"开始播放.");
				}
			});
		}
	}
	
	
	public void stop()
	{
		if (playThread != null && isRunning)
		{
			isRunning=false;
			try {
				playThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			playThread = null;
		}
	}
	public void release()
	{
		stop();
		playFile=null;
	}

	private class PlayPCMThread extends Thread
	{
		AudioTrack mAudioTrack;
		
		public PlayPCMThread()
		{
			int formatChnl= channels==2 ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO;
			
			int minBufSize=AudioTrack.getMinBufferSize(sampleRate, 
					formatChnl,
					 AudioFormat.ENCODING_PCM_16BIT);
			
			
			mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
										sampleRate, 
										formatChnl, 
										 AudioFormat.ENCODING_PCM_16BIT, 
										 minBufSize,
										AudioTrack.MODE_STREAM);
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
				openFile();
				mAudioTrack.play();	
				while(isRunning)
				{											
					try {
						int FrameSize=1024*4;
						byte[] readData=new byte[FrameSize];
						
						int readSize=readFile(readData);
						if(readSize==0){
							mAudioTrack.stop();
							break;
						}
						/**
						 * 请及时的投递声音数据,不要延迟过长,不然会导致视频丢帧,建议和AudioTrack一起写入数据,
						 * 如果您处理的数据时间过长,则建议另起一个线程,来提前处理数据,以保证这里的数据是及时的.
						 * 如果你需要write到AudioTrack,则建议先push到AudioLine,然后执行AudioTrack的write方法.
						 */
						if(audioLine!=null){
							audioLine.pushAudioData(readData);	
						}
						
						//开始播放
						mAudioTrack.write(readData, 0, readSize);
					
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						break;
					}
				}
				if (mAudioTrack != null){
					mAudioTrack.stop();				       	
					mAudioTrack.release();
					mAudioTrack = null;
				}
				closeFile();
				Log.d(TAG, "PlayAudioThread complete...");				
		}
	}
	private FileInputStream inStream;
	private void openFile()
	{
		File file = new File(playFile);
    	if (file == null){
    		Log.i("TAG","nef file error.");
    	}
		try {
			inStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	private int readFile(byte[] data)
	{
		int read=0;
		if(inStream!=null && data!=null && data.length>0){
			try {
				read=inStream.read(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return read;
	}
	private void closeFile()
	{
		if(inStream!=null){
			try {
				inStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
