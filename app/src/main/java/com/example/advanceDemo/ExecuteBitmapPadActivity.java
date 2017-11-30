package com.example.advanceDemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapGetFilters;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.onGetFiltersOutFrameListener;
import com.lansosdk.videoeditor.BitmapPadExecute;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.SDKFileUtils;

import java.util.ArrayList;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSwirlFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBulgeDistortionFilter;

/**
 * 
 */
public class ExecuteBitmapPadActivity extends Activity{

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

	  private void testGetFilters() {
		ArrayList<GPUImageFilter>  filters=new ArrayList<GPUImageFilter>();
		filters.add(new GPUImageSepiaFilter());
		filters.add(new GPUImageSwirlFilter());
		filters.add(new LanSongBulgeDistortionFilter());

		Bitmap bmp=BitmapFactory.decodeFile(CopyFileFromAssets.copyAssets(getApplicationContext(), "t14.jpg"));

		//------------------------一下是调用流程.
		//创建对象,传递参数 (此类可被多个线程同时开启执行)
		BitmapGetFilters bitmapGetFilters=new BitmapGetFilters(getApplicationContext(), bmp,filters);
		//设置回调, 注意:回调是在
		bitmapGetFilters.setDrawpadOutFrameListener(new onGetFiltersOutFrameListener() {

			@Override
			public void onOutFrame(BitmapGetFilters v, Object obj) {
				// TODO Auto-generated method stub
				Bitmap bmp2=(Bitmap)obj;
			}
		});
		bitmapGetFilters.start();
	}
}