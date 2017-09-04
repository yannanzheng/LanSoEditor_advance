package com.example.advanceDemo;

import java.nio.IntBuffer;

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
import com.lansosdk.box.DrawPadAllExecute;
import com.lansosdk.box.DrawPadVideoRunnable;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadThreadProgressListener;
import com.lansosdk.videoeditor.LanSoEditor;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.onVideoEditorProgressListener;

/**
先放入图片, 然后视频, 然后再图片.
 * 
 *
 */
public class ExecuteRandomDemoActivity extends Activity{

		String videoPath=null;
		ProgressDialog  mProgressDialog;
		int videoDuration;
		boolean isRuned=false;
		MediaInfo   mInfo;
		TextView tvProgressHint;
		 TextView tvHint;
	    private String dstPath=null;
	    private VideoLayer  videoLayer=null;
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
	    private DrawPadAllExecute  vDrawPad=null;
	    
	    /**
	     * 用来显示一个心形.
	     */
	    private ShowHeart mShowHeart;
	 	
	    private boolean isExecuting=false; 
	    
	private static final String TAG="BitmapLayer2ExecuteActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
		 
		 videoPath=getIntent().getStringExtra("videopath");
		 mInfo=new MediaInfo(videoPath);
		 mInfo.prepare();
		 
		 Log.i(TAG,"minfo"+mInfo.toString());
		 
		 setContentView(R.layout.video_edit_demo_layout);
		 
		 initUI();
		
       //在手机的默认路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
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
		
		 /**
		  * 创建在后台调用DrawPad来处理视频的构造方法.
		  * 
		  * (类似photoshop的工作区)
		  * 
		  * @param ctx 语境,android的Context
		  * @param srcPath 主视频的路径
		  * @param padwidth DrawPad的的宽度
		  * @param padheight DrawPad的的高度
		  * @param bitrate   编码视频所希望的码率,比特率.
		  * @param filter   为视频增加一个滤镜
		  * @param dstPath  编码视频保存的路径.
		  */
		vDrawPad=new DrawPadAllExecute(getApplicationContext(), 480, 480, 25, 1000000,dstPath);
		 /**
		  * 设置DrawPad处理的进度监听, 回传的currentTimeUs单位是微秒.
		  */
		vDrawPad.setDrawPadProgressListener(new onDrawPadProgressListener() {
			
			@Override
			public void onProgress(DrawPad v, long currentTimeUs) {
				// TODO Auto-generated method stub
				tvProgressHint.setText(String.valueOf(currentTimeUs));
			
				if(currentTimeUs>6000000)  {   //在6秒的时候停止.
					vDrawPad.stopDrawPad();
				}
				if(currentTimeUs>3000000 && videoLayer==null){
				
					videoLayer=vDrawPad.addVideoLayer(videoPath, null);
				}
			}
		});
		/**
		 * 设置DrawPad完成后的监听.
		 */
		vDrawPad.setDrawPadCompletedListener(new onDrawPadCompletedListener() {
			
			@Override
			public void onCompleted(DrawPad v) {
				// TODO Auto-generated method stub
				tvProgressHint.setText("DrawPadExecute Completed!!!");
				
				isExecuting=false;
				findViewById(R.id.id_video_edit_btn2).setEnabled(true);
			}
		});
		
		  Log.i(TAG,"bofore  startDrawPad===>"+MediaInfo.checkFile(videoPath));
		  
		/**
		 * 开始执行这个DrawPad, 是否暂停,为true表示暂停.
		 */
		vDrawPad.startDrawPad();
		
		
		
		
		/**
		 * 一下是在处理过程中, 
		 * 增加的几个Layer, 来实现视频在播放过程中叠加别的一些媒体, 像图片, 文字等.
		 */
		bitmapLayer=vDrawPad.addBitmapLayer(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher),null);
		bitmapLayer.setPosition(300, 200);
		
		//增加一个笑脸, add a bitmap
		vDrawPad.addBitmapLayer(BitmapFactory.decodeResource(getResources(), R.drawable.xiaolian),null);	

		//增加一个CanvasLayer
//		addCanvasLayer();
	//	addDataLayer();
	}
   @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	
    	if(vDrawPad!=null){
    		vDrawPad.releaseDrawPad();
    		try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		vDrawPad=null;
    	}
    	   if(SDKFileUtils.fileExist(dstPath)){
    		   SDKFileUtils.deleteFile(dstPath);
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
							Intent intent=new Intent(ExecuteRandomDemoActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
						}else{
							 Toast.makeText(ExecuteRandomDemoActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
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
		 * 因为Android的View机制是无法在非UI线程中使用View的. 但可以使用Canvas这个类工作在其他线程.
		 * 
		 * 因此我们设计了CanvasLayer,从而可以用Canvas来做各种Draw文字, 线条,图案等.
		 */
		private void addCanvasLayer()
		{
					mCanvasLayer=vDrawPad.addCanvasLayer();
					
					if(mCanvasLayer!=null){
						
						mCanvasLayer.setClearCanvas(false);
						mShowHeart=new ShowHeart(ExecuteRandomDemoActivity.this,mCanvasLayer.getPadWidth(),mCanvasLayer.getPadHeight());
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
	
	
//		private MediaInfo gifInfo;
//		private long decoderHandler;
//	   private IntBuffer  mGLRgbBuffer;
//	   private int gifInterval=0;
//	   private int frameCount=0;
//	   private DataLayer dataLayer;
//	   /**
//	    * 用来计算, 在视频走动过程中, 几秒钟插入一个gif图片
//	    * @return
//	    */
//	   private boolean canDrawNext()
//	   {
//		   if(frameCount%gifInterval==0){  //能被整除则说明间隔到了.
//			   frameCount++;
//			   return true;
//		   }else{
//			   frameCount++;
//			   return false;
//		   }
//	   }
//	   /**
//	    * 增加一个DataLayer, 数据图层. 数据图层是可以把外界的数据图片RGBA, 作为一个图层, 传到到DrawPad中. 
//	    * 
//	    * 流程是: 把gif作为一个视频文件, 一帧一帧的解码,把解码得到的数据通过DataLayer传递到画板中.
//	    */
//	   private void addDataLayer()
//		{
//			   gifInfo=new MediaInfo("/sdcard/a.gif");
//		       if(gifInfo.prepare())
//		       {
//		    	   decoderHandler=BoxDecoder.decoderInit("/sdcard/a.gif");
//		    	   mGLRgbBuffer = IntBuffer.allocate(gifInfo.vWidth * gifInfo.vHeight);
//		    	  
//		    	   gifInterval=(int)(mInfo.vFrameRate/gifInfo.vFrameRate);
//		    	   dataLayer=vDrawPad.addDataLayer(gifInfo.vWidth,gifInfo.vHeight);
//		    	   
//		    	   /**
//		    	    * 画板中的onDrawPadThreadProgressListener监听,与 onDrawPadProgressListener不同的地方在于:
//		    	    * 此回调是在DrawPad渲染完一帧后,立即执行这个回调中的代码,不通过Handler传递出去.
//		    	    */
//		    		vDrawPad.setDrawPadThreadProgressListener(new onDrawPadThreadProgressListener() {
//		    			
//		    			@Override
//		    			public void onThreadProgress(DrawPad v, long currentTimeUs) {
//		    				// TODO Auto-generated method stub
//		    				if(dataLayer!=null){
//		    					if(canDrawNext())
//		    					{
//			    						int seekZero=-1;
//			    						if(decoderHandler!=0 && BoxDecoder.decoderIsEnd(decoderHandler))
//					    				{
//			    							seekZero=0;
//					    				}
//				    					BoxDecoder.decoderFrame(decoderHandler, seekZero, mGLRgbBuffer.array());
//				    					
//			    						dataLayer.pushFrameToTexture( mGLRgbBuffer);
//			    						mGLRgbBuffer.position(0);	
//		    					}
//		    				}
//		    			}
//		    		});
//		       }
//		}
//	   private void removeGif()
//	   {
//		   if(vDrawPad!=null){
//			   vDrawPad.removeLayer(dataLayer);
//				dataLayer=null;
//				BoxDecoder.decoderRelease(decoderHandler);
//				decoderHandler=0; 
//		   }
//	   }
	
}	