package com.example.advanceDemo;

import java.lang.reflect.Array;
import java.nio.IntBuffer;
import java.util.ArrayList;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSwirlFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongScreenBlendFilter;
import junit.framework.Test;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.BoxDecoder;
import com.lansosdk.box.DataLayer;
import com.lansosdk.box.Layer;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadBitmapRunnable;
import com.lansosdk.box.DrawPadVideoRunnable;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadOutFrameListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadThreadProgressListener;
import com.lansosdk.videoeditor.BitmapPadExecute;
import com.lansosdk.videoeditor.CopyDefaultVideoAsyncTask;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.DrawPadPictureExecute;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;

/**
 * 
 */
public class ExecuteBitmapPadActivity2 extends Activity{

	private static final String TAG="ExecuteBitmapPadActivity";
		int videoDuration;
		boolean isRuned=false;
		TextView tvProgressHint;
		TextView tvHint;
	 
	    private String dstPath=null;
	    
	    
	
		BitmapLayer bitmapLayer=null;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
		 
		 
		 setContentView(R.layout.execute_edit_demo_layout);
		 tvHint=(TextView)findViewById(R.id.id_video_editor_hint);
		 
		 tvHint.setText(R.string.pictureset_execute_demo_hint);
   
		 tvProgressHint=(TextView)findViewById(R.id.id_video_edit_progress_hint);
		 
	       findViewById(R.id.id_video_edit_btn).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
						final Bitmap bmp1=BitmapFactory.decodeFile("/sdcard/t14.jpg");
						final Bitmap bmp2=BitmapFactory.decodeFile("/sdcard/b2.jpg");
						testDrawPadExecute(bmp1,bmp2);
				}
			});
    	}
   @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	 if(SDKFileUtils.fileExist(dstPath)){
    		 SDKFileUtils.deleteFile(dstPath);
         }
    }
	private void testDrawPadExecute(Bitmap bmp1,Bitmap bmp2)
	{
		BitmapPadExecute  bendBmp;
		bendBmp=new BitmapPadExecute(getApplicationContext());
		
		if(bendBmp.init(bmp1.getWidth(),bmp1.getHeight()))
		{
			Bitmap bmp=bendBmp.getBlendBitmap(bmp1, bmp2);
			
		}
		bendBmp.release();
	}
}	