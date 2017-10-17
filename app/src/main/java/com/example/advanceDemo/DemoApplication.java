package com.example.advanceDemo;



import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.MediaInfo;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

public class DemoApplication  extends Application {
	
	private String srcVideo;
	private MediaInfo mInfo=null;
	private static DemoApplication instance;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		 instance = this;
	}
	
	 public Context getContext()
	 {
	        return getBaseContext();
	 }
	 public Resources getResources()
	 {
	        return getBaseContext().getResources();
	 }
	 public static DemoApplication getInstance()
	 {
	        if (instance == null)
	        {
	            throw new NullPointerException(
	                "DemoApplication instance is null");
	        }
	        return instance;
	 }
	 
	 public void setVideoPath(String video)
	 {
			srcVideo=video;
	 }
	 public String getVideoPath()
	 {
		 if(srcVideo==null){
			 srcVideo=CopyFileFromAssets.copyAssets(getContext(), "ping20s.mp4");
		 }
		 return srcVideo;
	 }
	 public MediaInfo getVideoMediaInfo()
	 {
		 if(mInfo==null){
			 mInfo=new MediaInfo(srcVideo);
			 if(mInfo.prepare()==false){
				 srcVideo=CopyFileFromAssets.copyAssets(getContext(), "ping20s.mp4");
				 mInfo=new MediaInfo(srcVideo);
				 mInfo.prepare();
			 }
		 }
		 return mInfo;
	 }
}
