package com.example.advanceDemo.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.lansoeditor.demo.R;
import com.lansosdk.box.Pen;

/**
 * 继承自DrawPadView, 用来演示在视频中做标记的功能.
 * 
 *  原理是: 根据onTouch事件,
 *  按下时,从DrawPad中获取一个BitmapPen,
 *  移动时,把获取到BitmapPen实时的移动坐标.
 *  抬起时,从DrawPad中删除BitmapPen
 *
 */
public class MarkArrowView extends DrawPadView{

	  
    public MarkArrowView(Context context) {
        super(context);
    }

    public MarkArrowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarkArrowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MarkArrowView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
  
    Pen bitmap=null;
    
    @Override
  public boolean onTouchEvent(MotionEvent event) {
  	// TODO Auto-generated method stub
  	
  	
  	 switch (event.getAction())   
       {  
	       case MotionEvent.ACTION_DOWN:  
	    	   	if(bitmap!=null){
	    	   		bitmap.release();
	    	   	}
	    	   	//继承自DrawPadView, 在按下时获取一个BitmapPen
	    	   	bitmap=addBitmapPen(BitmapFactory.decodeResource(getResources(), R.drawable.arrow_red));
	    	   	if(bitmap!=null){
	    	   		bitmap.setVisibility(Pen.INVISIBLE);
	    	   	}	
	          return true; 
	       case MotionEvent.ACTION_MOVE:  
	    		if(bitmap!=null){
	    			bitmap.setPosition(event.getX(),event.getY());
	    			bitmap.setVisibility(Pen.VISIBLE);
	    		}
	    		  return true;   
	 
	       case MotionEvent.ACTION_UP:  
//	    		Log.i("test","ACTION_UP:"+event.getX()+" Y:"+event.getY());
	    		//当抬起时, 删除这个Pen
	    		if(bitmap!=null){
	    			bitmap.setVisibility(Pen.INVISIBLE);
	    			removePen(bitmap);
	    		}
	    		break;
	    }  
  	 	return super.onTouchEvent(event);
  }
    
}
