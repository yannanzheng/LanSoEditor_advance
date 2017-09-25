package com.lansosdk.videoeditor;

import com.lansosdk.box.LanSoEditorBox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.support.v4.content.PermissionChecker;
import android.util.Log;


public class LanSoEditor {

	  public static void initSDK(Context context,String str)
	  {
		  LoadLanSongSdk.loadLibraries();  //拿出来单独加载库文件.
		  LanSoEditor.initSo(context,str);
	  }
	  /**
	   * 为了统一, 这里请不要调用, 直接调用initSDK即可.
	   * @param context
	   * @param str
	   */
	  @Deprecated
	  public static void initSo(Context context,String str)
	  {
	    	    nativeInit(context,context.getAssets(),str);
	    	    LanSoEditorBox.init(); 
	  }
	    
	  public static void unInitSo()
	  {
	    		nativeUninit();
	  
	  }
	  /**
	   * 设置默认产生文件的文件夹, 默认是:/sdcard/lansongBox/
	   * 如果您要设置, 则需要改文件夹存在.
	   * 比如可以是:
	   * @param tmpDir
	   */
	  public static void setTempFileDir(String tmpDir)
	  {
		  LanSoEditorBox.setTempFileDir(tmpDir);
		  SDKDir.TMP_DIR=tmpDir;
	  }
	  
	  
	  
	  
	    public static native void nativeInit(Context ctx,AssetManager ass,String filename);
	    public static native void nativeUninit();
	    
}
