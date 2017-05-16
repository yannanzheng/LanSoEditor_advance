package com.lansosdk.videoeditor;

import com.lansosdk.box.LanSoEditorBox;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class LanSongUtil {

	/**
	 * 检查是否有 摄像头和麦克风的权限.
	 * @param ctx
	 * @return
	 */
	 public static boolean checkRecordPermission(Context ctx)
	 {
		   boolean ret1=LanSoEditorBox.checkCameraPermission(ctx);
		   boolean ret2=LanSoEditorBox.checkMicPermission(ctx);
		   
	       return ret1 && ret2;
	 }
	  /**
     * 隐藏虚拟按键，并且全屏
     * 
     * 如果不全屏, 用这样的不行:
     * int width=mDrawPadView.getDrawPadWidth();
    	 int height=mDrawPadView.getDrawPadHeight();
    	 int padWidth= (padHeight*width)/height;
    	 
    	 padWidth=(int)LanSongUtil.make4Bei((long)padWidth);
    	
    	 Log.i(TAG,"wwwwwidth:"+width+"height"+height+" pad:"+padHeight+" "+padWidth);
     */
    public static void hideBottomUIMenu(Activity  act) {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
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
