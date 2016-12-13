package com.example.commonDemo;
/**
 * 
 *注意, 此代码仅仅是sdk的功能的演示, 不属于sdk的一部分.
 *
 */
public class DemoInfo {

	public final int mHintId;
	public final int mTextId;
	public final boolean isOutVideo;
	public final boolean isOutAudio;
	
	/**
	 * 
	 * @param hintTextId  显示字符串名字的ID, 同时也作为当前demo的ID号.
	 */
	public DemoInfo(int hintId,int textId,boolean outvideo,boolean outaudio)
	{
		mHintId=hintId;
		mTextId=textId;
		isOutVideo=outvideo;
		isOutAudio=outaudio;
	}
}
