package com.example.advanceDemo;

import android.util.Log;

import com.lansosdk.box.Layer;

public class ScaleAnimation {

	private Layer mLayer;
	private  long mStartUS,mEndUS;
	
	//缩放要持续的时间
	private float durationS=1.0f;
	
	//在这个持续时间内, 要缩放的系数.
	private float totalScale=1.0f;
	
	/**
	 * 放大动画
	 * @param layer 对那个图层对象做放大
	 * @param startUs  开始时间, 单位微秒, 从drawpad的进度中的哪个时间段开始, 传递过来的是drawpad的progress的时间
	 * @param totalScale   最终要放大到的因子,比如放大2倍,这里是2.0f;
	 * @param durationUs  在多长时间内放大到lastValue;, 有缓慢进度等.
	 */
	public ScaleAnimation(Layer layer,long startUs, float totalScale, long durationUs)
	{
		if(durationUs>0 && layer!=null){
			mLayer=layer;
			mStartUS=startUs;
			mEndUS=mStartUS + durationUs;
			this.durationS= (float)durationUs/1000000f;
			this.totalScale=totalScale;
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
			factor*=totalScale;     //百分比乘以总的缩放系数,则等于当前要缩放到的值.
//			Log.i("scaleAnim","当前要缩放到:"+factor + "  currentTimeUs:"+currentTimeUs);
			mLayer.setScale(factor);
		}
	}
}
