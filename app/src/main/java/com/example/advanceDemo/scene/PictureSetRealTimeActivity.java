package com.example.advanceDemo.scene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;

import com.example.advanceDemo.VideoPlayerActivity;
import com.example.advanceDemo.view.ShowHeart;
import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.CanvasLayer;
import com.lansosdk.box.CanvasRunnable;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.MVLayer;
import com.lansosdk.box.MoveAnimation;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.Layer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.box.onDrawPadThreadProgressListener;
import com.lansosdk.videoeditor.CopyDefaultVideoAsyncTask;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.DrawPadView;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.DrawPadView.onViewAvailable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

/**
 *  演示:  图片合成视频的同时保存成文件.
 *  流程: 把DrawPadView设置为自动刷新模式, 然后一次性增加多个BitmapLayer,根据画面走动的时间戳来
 *  操作每个BitmapLayer是否移动,是否显示.
 *  
 *  这里仅仅演示移动的属性, 您实际中可以移动,缩放,旋转,RGBA值调节来混合使用,因为BitmapLayer继承自ILayer,故有这些特性.
 *  
 *  比如你根据时间戳来调节图片的RGBA中的A值(alpha透明度),则实现图片的淡入淡出效果.
 *  
 *  使用移动+缩放+RGBA调节,则实现一些缓慢照片变化的效果,浪漫文艺范的效果.
 *  
 *  视频标记就是一个典型的BitmapLayer的使用场景.
 *  
 */
public class PictureSetRealTimeActivity extends Activity{
    private static final String TAG = "PictureSetRealTimeActivity";

    private DrawPadView drawPadView;
    
    
    private String dstPath=null;
    private BitmapLayer  bgLayer=null;
    private Context mContext=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_set_layout);
        initView();
        
        drawPadView = (DrawPadView) findViewById(R.id.DrawPad_view);

        //在手机的默认路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
        dstPath=SDKFileUtils.newMp4PathInBox();
        mContext=getApplicationContext();
	 	new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					initDrawPad();
				}
			}, 500);
    }
    @Override
    protected void onResume() {
    	super.onResume();
        findViewById(R.id.id_DrawPad_saveplay).setVisibility(View.GONE);
    }
    boolean isSwitched=false;
    /**
     * Step1: 初始化DrawPad
     */
    private void initDrawPad()
    {
		//设置为自动刷新模式, 帧率为25
    	drawPadView.setUpdateMode(DrawPadUpdateMode.AUTO_FLUSH,30);
    	//使能实时录制,并设置录制后视频的宽度和高度, 码率, 帧率,保存路径.
    	drawPadView.setRealEncodeEnable(480,480,1000000,(int)30,dstPath);
    	
    	drawPadView.setOnDrawPadThreadProgressListener(new onDrawPadThreadProgressListener() {
			
			@Override
			public void onThreadProgress(DrawPad arg0, long arg1) {
				 if(arg1>=1000*1000 && isSwitched==false){
					  bgLayer.switchBitmap(BitmapFactory.decodeFile("/sdcard/a2.jpg"));
					  isSwitched=true;
				  }
			}
		});
    	drawPadView.setOnDrawPadCompletedListener(new DrawPadCompleted());
		drawPadView.setOnDrawPadProgressListener(new DrawPadProgressListener());
    	//设置DrawPad的宽高, 这里设置为480x480,如果您已经在xml中固定大小,则不需要再次设置,
    	//可以直接调用startDrawPad来开始录制.
    	drawPadView.setDrawPadSize(480,480,new onDrawPadSizeChangedListener() {
			
			@Override
			public void onSizeChanged(int viewWidth, int viewHeight) {
				// TODO Auto-generated method stub
					startDrawPad();
			}
		});
    	
    	//这里仅仅是举例,当界面再次返回的时候,依旧显示图片更新的动画效果,即重新开始DrawPad, 很多时候是不需要这样的场景, 这里仅仅是举例
    	drawPadView.setOnViewAvailable(new onViewAvailable() {
			
			@Override
			public void viewAvailable(DrawPadView v) {
				startDrawPad();
			}
		});
    }
    /**
     * Step2: 开始运行 Drawpad线程. (停止是在进度监听中, 根据时间来停止的.)
     */
    private void startDrawPad()
    {
    		drawPadView.pauseDrawPad();
    		if(drawPadView.startDrawPad())
    		{
    			DisplayMetrics dm = new DisplayMetrics();// 获取屏幕密度（方法2）
    		    dm = getResources().getDisplayMetrics();
    		     
    		      
    		   int screenWidth  = dm.widthPixels;	
    		   String picPath=null;   
    		   if(screenWidth>=1080){
    			   picPath=CopyFileFromAssets.copyAssets(mContext, "pic1080x1080u2.jpg");
    		   }else{
    			   picPath=CopyFileFromAssets.copyAssets(mContext, "pic720x720.jpg");
    		   }
    		   
    		   //先 增加第一张Bitmap的Layer, 因为是第一张,放在DrawPad中维护的数组的最下面, 认为是背景图片.
    		   bgLayer=drawPadView.addBitmapLayer(BitmapFactory.decodeFile(picPath));
    		   bgLayer.setScaledValue(bgLayer.getPadWidth(), bgLayer.getPadHeight());
    		   
    		   
    		   
    		   //这里同时增加多个,只是不显示出来.
    		   addBitmapLayer(R.drawable.tt,0,5000);  		//1--5秒.
    		   addBitmapLayer(R.drawable.tt3,5000,10000);  //5--10秒.
    		   addBitmapLayer(R.drawable.pic3,10000,15000);	//10---15秒 
    		   addBitmapLayer(R.drawable.pic4,15000,20000);  //15---20秒
    		   addBitmapLayer(R.drawable.pic5,20000,25000);  //20---25秒
    		   
    		 //增加一个MV图层  
//    		   addMVLayer();
    		   
    		   drawPadView.resumeDrawPad();
    		}
    }
    
    private void addMVLayer()
  	{
  		String  colorMVPath=CopyDefaultVideoAsyncTask.copyFile(PictureSetRealTimeActivity.this,"mei.mp4");
  	    String maskMVPath=CopyDefaultVideoAsyncTask.copyFile(PictureSetRealTimeActivity.this,"mei_b.mp4");
  	    
  		MVLayer  layer=drawPadView.addMVLayer(colorMVPath, maskMVPath);  //<-----增加MVLayer
  		layer.setScaledValue(layer.getPadWidth(), layer.getPadHeight());
  		
  		/**
  		 * mv在播放完后, 有3种模式,消失/停留在最后一帧/循环.
  		 * layer.setEndMode(MVLayerENDMode.INVISIBLE); 
  		 */
  	}
    private void addBitmapLayer(int resId,long startMS,long endMS)
    {
    	Layer item=drawPadView.addBitmapLayer(BitmapFactory.decodeResource(getResources(), resId));
    	
    	item.setVisibility(Layer.INVISIBLE);
    	
    	//开始1/3, 中间停1/3,最后划走1/3;
    	long total=endMS-startMS;
		long start2=endMS-total/3;
		
		//第一段运行
    	MoveAnimation move1=new MoveAnimation(startMS*1000, total*1000/3, 
    			0, item.getPadHeight()/2, /*开始位置, 结束位置*/
    			item.getPadWidth()/2,item.getPadHeight()/2);
    	
    	//中间停止在那里.
    	
    	//最后一段运行.
    	MoveAnimation move2=new MoveAnimation(start2*1000, total*1000/3, 
    			item.getPadWidth()/2,item.getPadHeight()/2,
    			
    			item.getPadWidth()+item.getPadWidth()/2,item.getPadHeight()/2);
    	
    	item.addAnimation(move1);
    	item.addAnimation(move2);
    }
    //DrawPad完成时的回调.
    private class DrawPadCompleted implements onDrawPadCompletedListener
    {

		@Override
		public void onCompleted(DrawPad v) {
			if(isDestorying==false){
				if(SDKFileUtils.fileExist(dstPath)){
			    	findViewById(R.id.id_DrawPad_saveplay).setVisibility(View.VISIBLE);
				}
				toastStop();
			}
		}
    }
    //DrawPad进度回调.
    private class DrawPadProgressListener implements onDrawPadProgressListener
    {

		@Override
		public void onProgress(DrawPad v, long currentTimeUs) {  //单位是微妙
			  if(currentTimeUs>=26*1000*1000)  //26秒.多出一秒,让图片走完.
			  {
				  drawPadView.stopDrawPad();
			  }
		}
    }
    private void initView()
    {

        findViewById(R.id.id_DrawPad_saveplay).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(SDKFileUtils.fileExist(dstPath)){
		   			 	Intent intent=new Intent(PictureSetRealTimeActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
		   		 }else{
		   			 Toast.makeText(PictureSetRealTimeActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
		   		 }
			}
		});
        findViewById(R.id.id_DrawPad_saveplay).setVisibility(View.GONE);
    }
    private void toastStop()
    {
    	Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
    }
    boolean isDestorying=false;  //是否正在销毁, 因为销毁会停止DrawPad
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	isDestorying=true;
    	if(drawPadView!=null){
    		drawPadView.stopDrawPad();
    		drawPadView=null;        		   
    	}
    	
    	if(SDKFileUtils.fileExist(dstPath)){
    		SDKFileUtils.deleteFile(dstPath);
        }
    }
}
