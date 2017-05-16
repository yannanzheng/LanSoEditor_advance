package com.example.advanceDemo;

import java.nio.IntBuffer;

import jp.co.cyberagent.lansongsdk.gpuimage.IFAmaroFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFRiseFilter;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.advanceDemo.view.ShowHeart;
import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.BoxDecoder;
import com.lansosdk.box.CanvasRunnable;
import com.lansosdk.box.CanvasLayer;
import com.lansosdk.box.DataLayer;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadVideoRunnable;
import com.lansosdk.box.GifLayer;
import com.lansosdk.box.MVLayer;
import com.lansosdk.box.MVLayerENDMode;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadThreadProgressListener;
import com.lansosdk.videoeditor.CopyDefaultVideoAsyncTask;
import com.lansosdk.videoeditor.DrawPadVideoExecute;
import com.lansosdk.videoeditor.LanSoEditor;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.onVideoEditorProgressListener;

/**
 * 演示: 使用DrawPad在后台执行视频和视频的叠加处理.
 * 
 * 适用在 一些UI界面需要用户手动操作UI界面,比如旋转叠加的视频等,增加图片后旋转图片等,这些UI交互完成后, 
 * 记录下用户的操作信息,但需要统一处理时,通过此类来在后台执行.
 * 
 * 流程:通过DrawPadVideoExecute来实现视频的编辑处理,
 * 效果:建立一个DrawPad后,增加VideoLayer让其播放,在播放过程中,向里面增加两个图片和一个UI,
 * 其中给一个图片移动位置,并在3秒处放大一倍,在6秒处消失,处理中实时的形成视频等
 * 
 *
 */
public class ExecuteVideoLayerDemoActivity extends Activity{

		String videoPath=null;
		ProgressDialog  mProgressDialog;
		int videoDuration;
		boolean isRuned=false;
		MediaInfo   mInfo;
		TextView tvProgressHint;
		 TextView tvHint;
	    private String editTmpPath=null;  //视频处理的临时文件存放
	    private String dstPath=null;
	    /**
	     * 图片图层
	     */
	    private BitmapLayer bitmapLayer=null;
	    /**
	     * Canvas 图层.
	     */
	    private CanvasLayer mCanvasLayer=null;
	    /**
	     * DrawPad, 用来执行图像处理的对象.
	     */
	    private DrawPadVideoExecute  mDrawPad=null;
	    
	    /**
	     * 用来显示一个心形.
	     */
	    private ShowHeart mShowHeart;
	 	
	    private boolean isExecuting=false; 
	    
	private static final String TAG="ExecuteVideoLayerDemoActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
		 
		 videoPath=getIntent().getStringExtra("videopath");
		 
		 mInfo=new MediaInfo(videoPath);
		 mInfo.prepare();
		 
		 setContentView(R.layout.video_edit_demo_layout);
		 
		 initUI();
		
       //在手机的默认路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
       editTmpPath=SDKFileUtils.newMp4PathInBox();
		 if(SDKFileUtils.fileExist(editTmpPath)){
			 SDKFileUtils.deleteFile(editTmpPath);
		 }
       dstPath=SDKFileUtils.newMp4PathInBox();
	}
	/**
	 * 从这里开始演示.
	 */
	private void testDrawPadExecute()
	{
		if(isExecuting)
			return ;
		
		isExecuting=true;
		 mDrawPad=new DrawPadVideoExecute(ExecuteVideoLayerDemoActivity.this,videoPath,480,480,1000000,null,editTmpPath);
		 
//		mDrawPad=new DrawPadVideoExecute(ExecuteVideoLayerDemoActivity.this,videoPath,1080,1920,3000000,null,editTmpPath);
		
		 mDrawPad.setUseMainVideoPts(true);
		 /**
		  * 设置DrawPad处理的进度监听, 回传的currentTimeUs单位是微秒.
		  */
		mDrawPad.setDrawPadProgressListener(new onDrawPadProgressListener() {
			
			@Override
			public void onProgress(DrawPad v, long currentTimeUs) {
				// TODO Auto-generated method stub
				
				tvProgressHint.setText(String.valueOf(currentTimeUs));
				//6秒后消失
				if(currentTimeUs>6000000 && bitmapLayer!=null)  
					v.removeLayer(bitmapLayer);
				
				//3秒的时候,放大一倍.
				if(currentTimeUs>3000000 && bitmapLayer!=null)  
					bitmapLayer.setScale(2.0f);
			}
		});
		/**
		 * 设置DrawPad完成后的监听.
		 */
		mDrawPad.setDrawPadCompletedListener(new onDrawPadCompletedListener() {
			
			@Override
			public void onCompleted(DrawPad v) {
				// TODO Auto-generated method stub
				tvProgressHint.setText("DrawPadExecute Completed!!!");
				isExecuting=false;
				
				if(isInsertAudio){  //
					dstPath=editTmpPath; 
				}else{
					if(SDKFileUtils.fileExist(editTmpPath)){
						boolean ret=VideoEditor.encoderAddAudio(videoPath, editTmpPath,SDKDir.TMP_DIR,dstPath);
						if(ret==false){
							dstPath=editTmpPath;
						}
					}
				}
				findViewById(R.id.id_video_edit_btn2).setEnabled(true);
			}
		});
//		vDrawPad.setUseMainVideoPts(true);
		
		addOtherAudio();
		
		//在开启前,先设置为暂停录制,因为要增加一些图层.
		mDrawPad.pauseRecord();
		/**
		 * 开始执行这个DrawPad
		 */
		if(mDrawPad.startDrawPad()){
			 // 增加一些图层.
			addLayers();
		}else{
			Log.e(TAG,"后台画板线程  运行失败,您请检查下是否是路径设置有无, 请用MediaInfo.checkFile执行查看下....");
		}
	}
	private void addLayers()
	{
		if(mDrawPad.isRunning())
		{
			//给视频增加一个虚化背景.
//			VideoLayer mainLayer=mDrawPad.getMainVideoLayer();
//			if(mainLayer!=null){
//				mainLayer.setBackgroundBlurFactor(3.5f);
//			}
			/**
			 * 一下是在处理过程中, 
			 * 增加的几个Layer, 来实现视频在播放过程中叠加别的一些媒体, 像图片, 文字等.
			 */
			bitmapLayer=mDrawPad.addBitmapLayer(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
			bitmapLayer.setPosition(300, 200);
			
			//增加一个笑脸, add a bitmap
			mDrawPad.addBitmapLayer(BitmapFactory.decodeResource(getResources(), R.drawable.xiaolian));	

			//增加一个CanvasLayer
//			addCanvasLayer();
//			addDataLayer();
//			 addMVLayer();
			 
			//addGifLayer();
			
			//增加完图层, 恢复运行.
			 mDrawPad.resumeRecord();
		}
	}
	/**
	 * 可以插入一段声音.
	 * 注意, 需要在drawpad开始前调用.
	 */
	private boolean isInsertAudio=false;
	private void addOtherAudio()
	{
		/**
		 *  插入一段声音, 这里拷贝Assets中的资源来做.
		 */
		//String audio=CopyDefaultVideoAsyncTask.copyFile(getApplicationContext(), "hongdou10s.mp3");
		
		//String audio="/sdcard/effect_harouha.aac";
		
		String audio="/sdcard/chongjibo_a_music.mp3";
		
		//以下是多种测试.
//		isInsertAudio=mDrawPad.addSubAudio(audio,0,-1,3.0f,1.0f);
	//	isInsertAudio=mDrawPad.addSubAudio(audio,300,-1,3.0f,1.0f);
		
		//isInsertAudio=mDrawPad.addSubAudio(audio,800,-1,3.0f,1.0f);
//		isInsertAudio=mDrawPad.addSubAudio(audio,2000,-1,3.0f,1.0f);
//		isInsertAudio=mDrawPad.addSubAudio(audio,1500,-1,1.0f,1.0f);
		
		isInsertAudio=mDrawPad.addSubAudio(audio,300,-1,1.0f,1.0f);
		
		
		Log.i(TAG,"isInsertAudio is:"+isInsertAudio);
		
		//vDrawPad.addSubAudio(audio,5000,-1,3.0f,2.0f);
		//vDrawPad.addSubAudio(audio,1000,8000,3.0f,2.0f);
	}
   @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	
    	 removeGif();
    	if(mDrawPad!=null){
    		mDrawPad.releaseDrawPad();
    		try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		mDrawPad=null;
    	}
    	   if(SDKFileUtils.fileExist(dstPath)){
    		   SDKFileUtils.deleteFile(dstPath);
           }
           if(SDKFileUtils.fileExist(editTmpPath)){
        	   SDKFileUtils.deleteFile(editTmpPath);
           } 
    }
	 
	   private void initUI()
	   {
			   tvHint=(TextView)findViewById(R.id.id_video_editor_hint);
				
			   tvHint.setText(R.string.drawpadexecute_demo_hint);
			   tvProgressHint=(TextView)findViewById(R.id.id_video_edit_progress_hint);
			       
				 findViewById(R.id.id_video_edit_btn).setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							
							if(mInfo.vDuration>=60*1000){//大于60秒
								showHintDialog();
							}else{
								testDrawPadExecute();
							}
						}
				 });
		     findViewById(R.id.id_video_edit_btn2).setEnabled(false);
		     findViewById(R.id.id_video_edit_btn2).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(SDKFileUtils.fileExist(dstPath)){
							Intent intent=new Intent(ExecuteVideoLayerDemoActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
						}else{
							 Toast.makeText(ExecuteVideoLayerDemoActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
						}
					}
				});
	
	   }
	   private void showHintDialog()
		{
			new AlertDialog.Builder(this)
			.setTitle("提示")
			.setMessage("视频过大,可能会需要一段时间,您确定要处理吗?")
	        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					testDrawPadExecute();
				}
			})
			.setNegativeButton("取消", null)
	        .show();
		}
	
		/**
		 * 增加一个CanvasLayer,
		 */
		private void addCanvasLayer()
		{
					mCanvasLayer=mDrawPad.addCanvasLayer();
					if(mCanvasLayer!=null){
						
						mCanvasLayer.setClearCanvas(false);
						mShowHeart=new ShowHeart(ExecuteVideoLayerDemoActivity.this,mCanvasLayer.getPadWidth(),mCanvasLayer.getPadHeight());
						mCanvasLayer.addCanvasRunnable(new CanvasRunnable() {
							
							@Override
							public void onDrawCanvas(CanvasLayer pen, Canvas canvas,
									long currentTimeUs) {
								// TODO Auto-generated method stub
								mShowHeart.drawTrack(canvas);
							}
						});
					}
		}
		private void addMVLayer()
		{
		   	Log.i(TAG,"增加一个MV");
			String  colorMVPath=CopyDefaultVideoAsyncTask.copyFile(ExecuteVideoLayerDemoActivity.this,"mei.mp4");
		    String maskMVPath=CopyDefaultVideoAsyncTask.copyFile(ExecuteVideoLayerDemoActivity.this,"mei_b.mp4");
		    /**
		     * 当mv在解码的时候, 是否异步执行; 
		     * 如果异步执行,则MV解码可能没有那么快,从而MV画面会有慢动作的现象.
		     * 如果同步执行,则视频处理会等待MV解码完成, 从而处理速度会慢一些,但MV在播放时,是正常的. 
		     *  
		     * @param srcPath  MV的彩色视频
		     * @param maskPath  MV的黑白视频.
		     * @param isAsync   是否异步执行.
		     * @return
		     */
		    MVLayer  layer=mDrawPad.addMVLayer(colorMVPath, maskMVPath,true); 
			 // mv在播放完后, 有3种模式,消失/停留在最后一帧/循环.默认是循环.
//			  layer.setEndMode(MVLayerENDMode.INVISIBLE); 
			 
		}
		GifLayer gifLayer;
		private void addGifLayer()
		{
			gifLayer=mDrawPad.addGifLayer(R.drawable.g06);
			
//			new Handler().postDelayed(new Runnable() {
//				
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					gifLayer.setScale(0.5f);
//					gifLayer.setRotate(60);
//					gifLayer.setPosition(gifLayer.getPadWidth()-gifLayer.getLayerWidth()/4,giflayer.getPositionY()/4);
//				}
//			}, 1000);  //系统时间1秒后,旋转到右上角.
		}
		private MediaInfo gifInfo;
		private long decoderHandler;
	   private IntBuffer  mGLRgbBuffer;
	   private int gifInterval=0;
	   private int frameCount=0;
	   private DataLayer dataLayer;
	   /**
	    * 用来计算, 在视频走动过程中, 几秒钟插入一个gif图片
	    * @return
	    */
	   private boolean canDrawNext()
	   {
		   if(frameCount%gifInterval==0){  //能被整除则说明间隔到了.
			   frameCount++;
			   return true;
		   }else{
			   frameCount++;
			   return false;
		   }
	   }
	   
	   /**
	    * 增加一个DataLayer, 数据图层. 数据图层是可以把外界的数据图片RGBA, 作为一个图层, 传到到DrawPad中. 
	    * 
	    * 流程是: 把gif作为一个视频文件, 一帧一帧的解码,把解码得到的数据通过DataLayer传递到画板中.
	    */
	 
	   private void addDataLayer()
		{
		   String gifPath=CopyDefaultVideoAsyncTask.copyFile(getApplicationContext(),"a.gif");
			   gifInfo=new MediaInfo(gifPath);
		       if(gifInfo.prepare())
		       {
		    	   decoderHandler=BoxDecoder.decoderInit(gifPath);
		    	   mGLRgbBuffer = IntBuffer.allocate(gifInfo.vWidth * gifInfo.vHeight);
		    	  
		    	   gifInterval=(int)(mInfo.vFrameRate/gifInfo.vFrameRate);
		    	   dataLayer=mDrawPad.addDataLayer(gifInfo.vWidth,gifInfo.vHeight);
		    	   
		    	   /**
		    	    * 画板中的onDrawPadThreadProgressListener监听,与 onDrawPadProgressListener不同的地方在于:
		    	    * 此回调是在DrawPad渲染完一帧后,立即执行这个回调中的代码,不通过Handler传递出去.
		    	    */
		    		mDrawPad.setDrawPadThreadProgressListener(new onDrawPadThreadProgressListener() {
		    			
		    			@Override
		    			public void onThreadProgress(DrawPad v, long currentTimeUs) {
		    				// TODO Auto-generated method stub
		    				if(dataLayer!=null){
		    					if(canDrawNext())
		    					{
		    						int seekZero=-1;
		    						if(decoderHandler!=0 && BoxDecoder.decoderIsEnd(decoderHandler))
				    				{
		    							seekZero=0;
				    				}
				    					BoxDecoder.decoderFrame(decoderHandler, seekZero, mGLRgbBuffer.array());
				    					
			    						dataLayer.pushFrameToTexture( mGLRgbBuffer);
			    						mGLRgbBuffer.position(0);	
		    					}
		    				}
		    			}
		    		});
		       }
		}
	   private void removeGif()
	   {
		   if(mDrawPad!=null && dataLayer!=null){
			   mDrawPad.removeLayer(dataLayer);
				dataLayer=null;
				BoxDecoder.decoderRelease(decoderHandler);
				decoderHandler=0; 
		   }
	   }
	
}	