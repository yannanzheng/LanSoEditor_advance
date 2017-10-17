package com.example.advanceDemo;

import android.util.Log;

import com.lansosdk.box.Layer;

/**
 * 移动到中间 
 *
 */
public class MoveCentor {

	private Layer mLayer;
	private  long mStartUS,mEndUS;
	private int currentX=0,currentY=0;
	private float durationS;
	
	/**
	 * @param layer
	 * @param startUs  开始时间
	 * @param durationUs  动画运行时间
	 */
	public MoveCentor(Layer layer,long startUs, long durationUs)
	{
		if(durationUs>0 && layer!=null){
			mLayer=layer;
			mStartUS=startUs;
			mEndUS=mStartUS + durationUs;
			durationS= (float)durationUs/1000000f;
			
			currentX=0;
			currentY=0;
		}
	}
	
	public void run(long currentTimeUs)
	{
		if(currentTimeUs<mStartUS || currentTimeUs>mEndUS){  //不在这个范围,则不显示,mEndMS+1000多出一秒是让右侧滑动的走完.
			return ;
		}
		
		if(mLayer!=null)
		{
			mLayer.setVisibility(Layer.VISIBLE);
			
			float timeDelta=(float)( (currentTimeUs-mStartUS)/1000000f);  //时间差.
			
			float factor=timeDelta/durationS;  //当前时间段应该缩放的值是, 与开始时间差,除以总时间,等于一个百分比.
			
			
			//缩放视频图层
			mLayer.setScale(factor);
			
			float width2=(float)mLayer.getPadWidth() *0.5f;
			float height2=(float)mLayer.getPadHeight() *0.5f;
			
			currentX= (int)(factor* width2);
			currentY= (int)(factor* height2);
			
			
			if(currentX>mLayer.getPadWidth()/2){  //以为浮点计算后取整, 可能有偏差,这里如果大于,则等于;
				currentX=mLayer.getPadWidth()/2;
			}
			if(currentY>mLayer.getPadHeight()/2){
				currentY=mLayer.getPadHeight()/2;
			}
			
			//移动视频图层
			mLayer.setPosition(currentX, currentY);
			
//			Log.i("move","currentX:"+currentX + " currentY:"+currentY);
		}
	}
}
