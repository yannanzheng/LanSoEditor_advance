package com.example.advanceDemo;

import java.nio.IntBuffer;
import java.util.ArrayList;

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
import com.lansosdk.videoeditor.CopyDefaultVideoAsyncTask;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.DrawPadPictureExecute;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;

/**
 * 后台执行 照片影集的功能. 
 * 使用DrawPad的扩展类:DrawPadPictureExecute来操作.
 * 
 */
public class ExecuteBitmapLayerActivity extends Activity{

	private static final String TAG="ExecuteBitmapLayerActivity";
		int videoDuration;
		boolean isRuned=false;
		TextView tvProgressHint;
		TextView tvHint;
	 
	    private String dstPath=null;
	    
	    private String picBackGround=null;
	    private ArrayList<SlideEffect>  slideEffectArray;
	    
	    VideoEditor mVideoEditer;
		/**
		 * 图片类的Layer
		 */
		BitmapLayer bitmapLayer=null;
		/**
		 * 使用DrawPad中的Picture执行类来做.
		 */
		DrawPadPictureExecute  mDrawPad=null;
		private int padWidth,padHeight;
		/**
		 * 当前是否已经在执行, 以免造成多次执行.
		 */
		private boolean isExecuting=false;
		
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
						testDrawPadExecute();
				}
			});
       
       findViewById(R.id.id_video_edit_btn2).setEnabled(false);
       findViewById(R.id.id_video_edit_btn2).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(SDKFileUtils.fileExist(dstPath)){
					Intent intent=new Intent(ExecuteBitmapLayerActivity.this,VideoPlayerActivity.class);
	    	    	intent.putExtra("videopath", dstPath);
	    	    	startActivity(intent);
				}else{
					 Toast.makeText(ExecuteBitmapLayerActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
				}
			}
		});
       DisplayMetrics dm = new DisplayMetrics();// 获取屏幕密度（方法2）
       dm = getResources().getDisplayMetrics();
        
      /**
       * 这里增加一个图层, 即作为最底部的一张图片.
       */
       
      int screenWidth  = dm.widthPixels;	
      if(screenWidth>=1080){
    	  picBackGround=CopyFileFromAssets.copyAssets(getApplicationContext(),"pic1080x1080u2.jpg");
      }else{
    	  picBackGround=CopyFileFromAssets.copyAssets(getApplicationContext(), "pic720x720.jpg");
      }

      //在手机的默认路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
       dstPath=SDKFileUtils.newMp4PathInBox();
	}
   @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	
    	if(mDrawPad!=null){
    		mDrawPad.release();
    		mDrawPad=null;
    	}
    	 if(SDKFileUtils.fileExist(dstPath)){
    		 SDKFileUtils.deleteFile(dstPath);
         }
    }
	private void testDrawPadExecute()
	{
		if(isExecuting)
			return ;
		
		padWidth=480;
		padHeight=480;
		isExecuting=true;
		
		
		
		//注意:这里的是直接把DrawPad设置为480x480,execute是没有自动缩放到屏幕的宽度的,如果加载图片,则最大的图片为480x480,如果超过则只显示480x480的部分.
		 mDrawPad=new DrawPadPictureExecute(getApplicationContext(), 480, 480, 26*1000, 25, 1000000, dstPath);
		
		 /**
		  * 设置DrawPad的处理进度监听, 您可以在每一帧的过程中对ILayer做各种变化,
		  * 比如平移,缩放,旋转,颜色变化,增删一个Layer等,来实现各种动画画面.
		  */
		mDrawPad.setDrawPadProgressListener(new onDrawPadProgressListener() {
			
			//currentTimeUs是当前时间戳,单位是微妙,可以根据时间戳/(MediaInfo.vDuration*1000000)来得到当前进度百分比.
			@Override
			public void onProgress(DrawPad v, long currentTimeUs) {
				// TODO Auto-generated method stub
				tvProgressHint.setText(String.valueOf(currentTimeUs));
				 if(slideEffectArray!=null && slideEffectArray.size()>0)
				 {
					  for(SlideEffect item: slideEffectArray){
						  item.run(currentTimeUs/1000);
					  }
				  }
			}
		});
		mDrawPad.setDisableEncode(true);
		
		/**
		 * 处理完毕后的监听
		 */
		mDrawPad.setDrawPadCompletedListener(new onDrawPadCompletedListener() {
			
			@Override
			public void onCompleted(DrawPad v) {
				// TODO Auto-generated method stub
				tvProgressHint.setText("DrawPadExecute Completed!!!");
				isExecuting=false;
				//清空效果数组.
				if(slideEffectArray!=null){
			   		 for(SlideEffect item: slideEffectArray){
			   			mDrawPad.removeLayer(item.getLayer());
			   		 }
			   		 slideEffectArray.clear();
			   		 slideEffectArray=null;
		    	}

				if(SDKFileUtils.fileExist(dstPath)){
					findViewById(R.id.id_video_edit_btn2).setEnabled(true);
				}
			}
		});
			/**
			 * 开始前先设置暂停标记.暂停画面的走动.比如想一次性增加多个Layer对象后,
			  * 在让DrawPad执行,这样比在画面走动中获取更精确一些.
			 */
			mDrawPad.pauseRecord();
			/**
			 *开始处理. 
			 */
			 mDrawPad.startDrawPad();
			//你可以设置一个背景,
			mDrawPad.addBitmapLayer(BitmapFactory.decodeFile(picBackGround),null);
	      
	       slideEffectArray=new ArrayList<SlideEffect>();
	      
			//这里同时增加多个,只是不显示出来.
	      addLayerToArray(R.drawable.pic1,0,5000);  		//1--5秒.
	      addLayerToArray(R.drawable.pic2,5000,10000);  //5--10秒.
	      addLayerToArray(R.drawable.pic3,10000,15000);	//10---15秒 
	      addLayerToArray(R.drawable.pic4,15000,20000);  //15---20秒
	      addLayerToArray(R.drawable.pic5,20000,25000);  //20---25秒
	      
//	      mDrawPad.addGifLayer(R.drawable.g06);
	      //增加完Layer后,再次恢复DrawPad,让其工作.
	      mDrawPad.resumeRecord();
	}
	  private void addLayerToArray(int resId,long startMS,long endMS)
	    {
	    	BitmapLayer item=mDrawPad.addBitmapLayer(BitmapFactory.decodeResource(getResources(), resId),null);
			SlideEffect  slide=new SlideEffect(item, 25, startMS, endMS, true);
			slideEffectArray.add(slide);
			
//			item.switchBitmap(bmp);
	    }
	  
}	