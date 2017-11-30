package com.example.advanceDemo.BitmapAudio;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;

import com.example.advanceDemo.view.BitmapUtils;
import com.example.advanceDemo.view.ImageTouchView;
import com.example.advanceDemo.view.StickerView;
import com.example.advanceDemo.view.TextStickerView;
import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.ViewLayerRelativeLayout;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadOutFrameListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.DrawPadView;
import com.lansosdk.videoeditor.FilterLibrary;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.DrawPadView.onViewAvailable;
import com.lansosdk.videoeditor.FilterLibrary.FilterAdjuster;
import com.lansosdk.videoeditor.FilterLibrary.OnGpuImageFilterChosenListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * 图片编辑, 编辑后, 输出一张图片.
 */
public class BitmapEditDemoActivity extends Activity implements OnClickListener{
    private static final String TAG = "BitmapEditDemoActivity";

    private Bitmap  srcBmp=null;
    
    private DrawPadView drawPadView;
    
    private BitmapLayer  bmpLayer=null;
    
    
    private FilterAdjuster mFilterAdjuster;
    private SeekBar AdjusterFilter;
    
    private ViewLayer mViewLayer=null;
    
    private ViewLayerRelativeLayout viewLayerRelativeLayout;
    private ImageTouchView imgeTouchView;
    private StickerView  stickView;
    private TextStickerView textStickView;
    private ImageView ivShowImg;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bitmap2bitmap_layout);
        
        initView();
     
        //原图片.
        String bmpPath=CopyFileFromAssets.copyAssets(getApplicationContext(), "t14.jpg");
        srcBmp=BitmapFactory.decodeFile(bmpPath);
    }
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				initDrawPad();
			}
		}, 100);
    }
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	drawPadView.stopDrawPad();
    }
    /**
     * Step1: 初始化DrawPad容器
     */
    private void initDrawPad()
    {
		//设置为自动刷新模式, 帧率为25
    	drawPadView.setUpdateMode(DrawPadUpdateMode.AUTO_FLUSH,30);
    	drawPadView.setDrawPadSize(srcBmp.getWidth(),srcBmp.getHeight(),new onDrawPadSizeChangedListener() {
			@Override
			public void onSizeChanged(int viewWidth, int viewHeight) {
					startDrawPad();
			}
		});
    }
    /**
     * Step2: 开始运行 Drawpad容器.
     */
    private void startDrawPad()
    {
    	
    	drawPadView.pauseDrawPad();
    	if(drawPadView.startDrawPad())
    	{
    		//增加一个图片图层
    		bmpLayer=  drawPadView.addBitmapLayer(srcBmp);
    		bmpLayer.setScaledValue(bmpLayer.getPadWidth(), bmpLayer.getPadHeight());
    		
    		//再增加一个UI图层, UI图层在图片图层上面.
    		addViewLayer();
    	}
    	drawPadView.resumeDrawPad();
    }
    /**
     * 增加UI图层: ViewLayer 
     */
    private void addViewLayer()
    {
    	if(drawPadView!=null && drawPadView.isRunning())
    	{
    		mViewLayer=drawPadView.addViewLayer();
            
    		//绑定
            ViewGroup.LayoutParams  params=viewLayerRelativeLayout.getLayoutParams();
            params.width=mViewLayer.getPadWidth();  
            params.height=mViewLayer.getPadHeight();  
            viewLayerRelativeLayout.setLayoutParams(params);
            
            setLayout(imgeTouchView,mViewLayer.getPadWidth(),mViewLayer.getPadHeight());
            setLayout(stickView,mViewLayer.getPadWidth(),mViewLayer.getPadHeight());
            setLayout(textStickView,mViewLayer.getPadWidth(),mViewLayer.getPadHeight());
            
        	viewLayerRelativeLayout.bindViewLayer(mViewLayer);
    		viewLayerRelativeLayout.invalidate();
    	}
    }
    private void setLayout(View v,int w,int h)
    {
    	ViewGroup.LayoutParams  params=v.getLayoutParams();
        params.width=w;  
        params.height=h;  
        v.setLayoutParams(params);
    }
    /**
     * 选择滤镜效果, 
     */
    private void selectFilter()
    {
    	if(drawPadView!=null && drawPadView.isRunning()){
    		FilterLibrary.showDialog(this, new OnGpuImageFilterChosenListener() {

                @Override
                public void onGpuImageFilterChosenListener(final GPUImageFilter filter,String name) {
                	
                	  if(bmpLayer!=null){
                		  bmpLayer.switchFilterTo(filter);
                		  mFilterAdjuster = new FilterAdjuster(filter);
                		  //如果这个滤镜 可调, 显示可调节进度条.
                		  findViewById(R.id.id_bmp2bmp_filter_seekbar).setVisibility(
   	         		            mFilterAdjuster.canAdjust() ? View.VISIBLE : View.GONE);
                	  }
                }
            });
    	}
    }
    private void initView()
    {
           drawPadView = (DrawPadView) findViewById(R.id.id_bmp2bmp_drawpadview);
           
   		 
   		 	 viewLayerRelativeLayout=(ViewLayerRelativeLayout)findViewById(R.id.id_bmp2bmp_gllayout);
   		 	
	   	     imgeTouchView=(ImageTouchView)findViewById(R.id.id_bmp2bmp_switcher);
	         imgeTouchView.setActivity(BitmapEditDemoActivity.this);
	         
	         stickView=(StickerView)findViewById(R.id.id_bmp2bmp_stickview);
	         textStickView=(TextStickerView)findViewById(R.id.id_bmp2bmp_textstickview);
   		 	
			 findViewById(R.id.id_bmp2bmp_export_btn).setOnClickListener(this);
	         findViewById(R.id.id_bmp2bmp_btnfilter).setOnClickListener(this);
	         findViewById(R.id.id_bmp2bmp_addstick).setOnClickListener(this);
	         findViewById(R.id.id_bmp2bmp_addtext).setOnClickListener(this);
	   		 	
   		 	//滤镜的设置.
   	 		AdjusterFilter=(SeekBar)findViewById(R.id.id_bmp2bmp_filter_seekbar);
   	        AdjusterFilter.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
   				
   				@Override
   				public void onStopTrackingTouch(SeekBar seekBar) {
   				}
   				@Override
   				public void onStartTrackingTouch(SeekBar seekBar) {
   				}
   				@Override
   				public void onProgressChanged(SeekBar seekBar, int progress,
   						boolean fromUser) {
   					  if (mFilterAdjuster != null) {
   				            mFilterAdjuster.adjust(progress);
   				        }
   				}
   			});
   	        ivShowImg=(ImageView)findViewById(R.id.id_bmp2bmp_showimg_iv);
   	        
   	        findViewById(R.id.id_bmp2bmp_showlayout).setVisibility(View.GONE);
   	        findViewById(R.id.id_bmp2bmp_showimg_btn).setOnClickListener(this);
    }
    boolean isDestorying=false;  //是否正在销毁, 因为销毁会停止DrawPad
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	isDestorying=true;
    	if(drawPadView!=null){
    		drawPadView.stopDrawPad();
    		drawPadView=null;
    	}
    }
    private int stickCnt=2;
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_bmp2bmp_btnfilter:
			selectFilter();
			break;
		case R.id.id_bmp2bmp_addstick:
			   if(stickView!=null){
				   Bitmap bmp=null;
				   if(stickCnt==2){
					   bmp=BitmapFactory.decodeResource(getResources(), R.drawable.stick2); 
				   }else if(stickCnt==3){
					   bmp=BitmapFactory.decodeResource(getResources(), R.drawable.stick3);   
				   }else if(stickCnt==4){
					   bmp=BitmapFactory.decodeResource(getResources(), R.drawable.stick4);   
				   }else{
					   bmp=BitmapFactory.decodeResource(getResources(), R.drawable.stick5);   
				   }
			   		stickCnt++;
			   		stickView.addBitImage(bmp);
			   }
			break;
			case R.id.id_bmp2bmp_addtext:
				showInputDialog();
				break;
			case R.id.id_bmp2bmp_export_btn:

				String path = SDKDir.getPath();
				Log.d("feature_847", "path = " + path);
//				BitmapUtils.saveToSdCard();


//				stickView.disappearIconBorder();
//				textStickView.disappearIconBorder();
//				drawPadView.setOnDrawPadOutFrameListener(srcBmp.getWidth(), srcBmp.getHeight(), 1, new onDrawPadOutFrameListener() {
//
//					@Override
//					public void onDrawPadOutFrame(DrawPad v, Object obj, int type, long ptsUs) {
//							Bitmap bmp=(Bitmap)obj;
//							drawPadView.setOnDrawPadOutFrameListener(0, 0, 0, null);  //禁止再次提取图片.
//							ivShowImg.setImageBitmap(bmp);//显示图片.
//							findViewById(R.id.id_bmp2bmp_showlayout).setVisibility(View.VISIBLE);
//							//startShowOneBitmapActivity(DemoUtil.savePng(bmp));//也可以保存,在另外地方显示,建议异步保存,因为耗时.
//					}
//				});
				break;
			case R.id.id_bmp2bmp_showimg_btn:
				findViewById(R.id.id_bmp2bmp_showlayout).setVisibility(View.GONE);;
				break;
		default:
			break;
		}
	}
	private String strInputText="蓝松文字演示";
	private void showInputDialog()
	{
		final EditText etInput = new EditText(this);  
           
		new AlertDialog.Builder(this).setTitle("请输入文字")  
		.setView(etInput)  
		.setPositiveButton("确定", new AlertDialog.OnClickListener() {  
		    public void onClick(DialogInterface dialog, int which) {  
		    	String input = etInput.getText().toString();  
		    	if(input!=null && input.equals("")==false){
		    		strInputText=input;
		    		textStickView.setText(strInputText);
		    	}
		    }})
		.show(); 
	}
	
//	private void startShowOneBitmapActivity(String pngPath)
//	{
//		if(pngPath!=null){
//			Intent intent=new Intent(BitmapEditDemoActivity.this,ShowOneBitmapActivity.class);
//	    	intent.putExtra("pngPath", pngPath);
//	    	startActivity(intent);
//		}
//	}
}
