package com.example.advanceDemo;

import android.util.Log;

import com.lansosdk.box.Layer;

/**
 *  此代码仅仅是用来演示多张图片的移动效果的, 实际你可以根据自己的需求而定.  
 *  
 *  此代码不属于sdk的一部分, 仅仅用作演示demo使用.
 *  
 *  您可以在仿照这个来做各种图片的动作.
 */
public class SlideEffect {

	private Layer mLayer;
	private final long mStartMS,mEndMS;
	private final int viewWidth,viewHeight;
	
	private final int stepPerFrame;
	//Layer的中心坐标.
	private int currentX=0,currentY=0;
	private final boolean needReleaseLayer;
	////第一秒滑入到中间, 第2,3,4秒显示,第5秒消失.
	/**
	 *  注意:这里没有检查endMS是否大于startMS,但实际一定要大于.
	 * @param Layer
	 * @param fps
	 * @param startMS
	 * @param endMS
	 * @param glwidth
	 * @param glheight
	 */
	public SlideEffect(Layer pen,int fps,long startMS,long endMS,boolean needRelease)
	{
		mLayer=pen;
		mStartMS=startMS;
		mEndMS=endMS;
		viewWidth=mLayer.getPadWidth();
		viewHeight=mLayer.getPadHeight();
		needReleaseLayer=needRelease;
		
		//一秒钟划入,则需要走 viewWidth/2的距离. 则一帧step,需要走 viewWidth/(2*fps);
		
		//宽度是多少, 需要多少秒走完,一秒钟多少帧.则 每走一步是
		float needS=(endMS-startMS)/1000;
		
		//这里是让中心点移动.
		stepPerFrame=(int)(viewWidth/(2*fps));  //只运动两秒钟,故这里运动的step就等于   总共2*fps这些帧内走完.
		
		
		currentY=viewHeight/2;
		mLayer.setVisibility(Layer.INVISIBLE);  //刚开始是不显示的.
	}
	
	public void run(long currentTimeMS)
	{
		long firstSecond=mStartMS+1000;
		long endSecond=mEndMS-1000;
		
		if(currentTimeMS>(mEndMS+1000) || currentTimeMS<mStartMS){  //不在这个范围,则不显示,mEndMS+1000多出一秒是让右侧滑动的走完.
//		if(currentX>viewHeight || currentTimeMS<mStartMS){
			mLayer.setVisibility(Layer.INVISIBLE);
			return ;
		}
		mLayer.setVisibility(Layer.VISIBLE);

		if(currentTimeMS<(firstSecond)){
			currentX+=stepPerFrame;
		}
		if(currentTimeMS >=endSecond){
			currentX+=stepPerFrame;
		}
//		Log.i("sno","current:"+currentX+" y "+currentY);
		
		mLayer.setPosition(currentX, currentY);
		
	}
	public Layer getLayer()
	{
		return mLayer;
	}
//	public void release()
//	{
//		if(needReleaseLayer){
//			mLayer.release();
//			mLayer=null;
//		}
//	}
	
}
