package com.lansosdk.videoeditor;

import android.util.Log;

public class LoadLanSongSdk {
	private static boolean isLoaded=false;
	  
	public static synchronized void loadLibraries() {
        if (isLoaded)
            return;
        Log.d("lansoeditor","load libraries.....LanSongffmpeg.");
        
        //2017年10月15日, 名字更改为LanSongxxxx
    	System.loadLibrary("LanSongffmpeg");
    	System.loadLibrary("LanSongdisplay");
    	System.loadLibrary("LanSongplayer");
    	
	    isLoaded=true;
	}
}
