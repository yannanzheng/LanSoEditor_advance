package com.lansosdk.videoeditor;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.View;

public class LanSongUtil {

	  /*
	  * 隐藏虚拟按键，并且全屏
	   * 使用在android的3个按键为虚拟按键 全屏录制的时候,
	    * 比如华为的P9 mate7 等手机上.
	    */
    public static void hideBottomUIMenu(Activity  act) {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = act.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = act.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
    
    public static long make4Bei(long value)
	{
		long  val2= value/4;
		
		if(value%4!=0)
			val2+=1;
		
			val2*=4;
		return val2;
		
		/**
		 * 	for(int i=0;i<100;i++)
		{
			int  val= i/4;
			
			if(i%4!=0)
				val+=1;
			
			val*=4;
			System.out.println("i="+i+" val:"+val);
		}
		 */
	}
}
