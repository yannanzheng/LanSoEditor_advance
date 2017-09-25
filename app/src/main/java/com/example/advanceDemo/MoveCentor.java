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
	private  int stepX,stepY;  //x,y每次递增多少.
	private int currentX=0,currentY=0;
	
	
	public MoveCentor(Layer layer,long startUs, long durationUs,float fps)
	{
		if(durationUs>0 && layer!=null){
			
			mLayer=layer;
			mStartUS=startUs;
			mEndUS=mStartUS + durationUs;
			
			float time= (float)durationUs/1000000f;  //总时间,转换为秒
			
			int frameSize=(int)(time *fps);  //得到这个时间段内有多少帧;

			stepX= layer.getPadWidth()/(frameSize*2);  //移动到 layer.getPadWidth()/2的位置 ,在这些帧内, 故每一帧移动多少, 要除以frameSize;
			
			stepY= layer.getPadHeight()/(frameSize*2);  
			 
			currentX=0;
			currentY=0;
//			Log.i("mov","frameSize:"+frameSize+ "layer.getPadWidth():"+layer.getPadWidth()+ "stepX:"+stepX);
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
			currentX+=stepX;
			currentY+=stepY;
			if(currentX>mLayer.getPadWidth()/2){  //以为浮点计算后取整, 可能有偏差,这里如果大于,则等于;
				currentX=mLayer.getPadWidth()/2;
			}
			if(currentY>mLayer.getPadHeight()/2){
				currentY=mLayer.getPadHeight()/2;
			}
			mLayer.setPosition(currentX, currentY);
//			Log.i("move","currentX:"+currentX + " currentY:"+currentY);
		}
	}
}
