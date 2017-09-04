package com.example.advanceDemo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageGammaFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImagePixelationFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSwirlFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBulgeDistortionFilter;


import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.example.commonDemo.CommonDemoActivity;
import com.lansoeditor.demo.R;
import com.lansosdk.box.AudioMix;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.BitmapGetFilters;
import com.lansosdk.box.LanSoEditorBox;
import com.lansosdk.box.AudioPad;
import com.lansosdk.box.SampleSave;
import com.lansosdk.box.onDrawPadOutFrameListener;
import com.lansosdk.box.onGetFiltersOutFrameListener;
import com.lansosdk.videoeditor.AVDecoder;
import com.lansosdk.videoeditor.AudioPadExecute;
import com.lansosdk.videoeditor.CopyDefaultVideoAsyncTask;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.FileWriteUtils;
import com.lansosdk.videoeditor.LanSoEditor;
import com.lansosdk.videoeditor.LoadLanSongSdk;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements OnClickListener{


	 private static final String TAG="MainActivity";
	 private static final boolean VERBOSE = false;   
	 
	private TextView tvVideoPath;
	private boolean isPermissionOk=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		Thread.setDefaultUncaughtExceptionHandler(new LanSoSdkCrashHandler());
        setContentView(R.layout.activity_main);
        
        LoadLanSongSdk.loadLibraries();  //拿出来单独加载库文件.
        LanSoEditor.initSo(getApplicationContext(),null);
        
        
//        Log.i(TAG,"getCPUInfo"+ GPUUtils.isSlowGPU_UI());
        
        /**
         * 修改box里面的临时路径如不调用, 则默认是/sdcard/lansongBox/文件夹下.
         * 要修改VideoEidtor产生的路径,则在SDKDir.java中直接修改;
         */
//        LanSoEditorBox.setTempFileDir("/sdcard/testTmp/");
        
        
        
    	//因为从android6.0系统有各种权限的限制,这里先检查是否有读写的权限,PermissionsManager采用github上开源库,不属于我们sdk的一部分.
		 //下载地址是:https://github.com/anthonycr/Grant,您也可以使用别的方式来检查app所需权限.
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {
            	isPermissionOk=true;
                Toast.makeText(MainActivity.this, R.string.message_granted, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDenied(String permission) {
            	isPermissionOk=false;
                String message = String.format(Locale.getDefault(), getString(R.string.message_denied), permission);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
        
        tvVideoPath=(TextView)findViewById(R.id.id_main_tvvideo);
        
        
        findViewById(R.id.id_main_outbody).setOnClickListener(this); //灵魂出窍
        findViewById(R.id.id_main_cameralayer).setOnClickListener(this);
        findViewById(R.id.id_main_camerafulllayer).setOnClickListener(this);
        findViewById(R.id.id_main_camerafulllayer2).setOnClickListener(this);
        
        findViewById(R.id.id_main_viewlayerdemo1).setOnClickListener(this);
        findViewById(R.id.id_main_viewremark).setOnClickListener(this);
        findViewById(R.id.id_main_viewlayerdemo2).setOnClickListener(this);
        findViewById(R.id.id_main_canvaslayerdemo).setOnClickListener(this);
        findViewById(R.id.id_main_videofilterdemo).setOnClickListener(this);  //图层滤镜. 用videoLayer来演示.
        
        findViewById(R.id.id_main_mvlayerdemo).setOnClickListener(this);  //图层滤镜. 用videoLayer来演示.
        
        findViewById(R.id.id_main_pictures).setOnClickListener(this);
        
        findViewById(R.id.id_main_commonversion).setOnClickListener(this);
        
        findViewById(R.id.id_main_twovideooverlay).setOnClickListener(this);
        findViewById(R.id.id_main_videobitmapoverlay).setOnClickListener(this);
        
        findViewById(R.id.id_main_drawpadpictureexecute).setOnClickListener(this);
        findViewById(R.id.id_main_drawpadexecute_filter).setOnClickListener(this);
        findViewById(R.id.id_main_testvideoplay).setOnClickListener(this);
        
        findViewById(R.id.id_main_cameralayer_segment).setOnClickListener(this);
        findViewById(R.id.id_main_extract_frame).setOnClickListener(this);
        
        findViewById(R.id.id_main_select_video).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startSelectVideoActivity();
			}
		});
        
        findViewById(R.id.id_main_use_default_videobtn).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new CopyDefaultVideoAsyncTask(MainActivity.this, tvVideoPath, "ping20s.mp4").execute();
			}
		});
        showHintDialog();
    }
    
    private void showHintDialog()
   	{
    	Calendar c = Calendar.getInstance();
   		int year=c.get(Calendar.YEAR);
   		int month=c.get(Calendar.MONTH)+1;
   		
   		int lyear=VideoEditor.getLimitYear();
   		int lmonth=VideoEditor.getLimitMonth();
   		Log.i(TAG,"current year is:"+year+" month is:"+month +" limit year:"+lyear+" limit month:"+lmonth);
   		String timeHint=getResources().getString(R.string.sdk_limit);
   		String version=VideoEditor.getSDKVersion()+ ";\n BOX:"+LanSoEditorBox.VERSION_BOX;
   		
   		timeHint=String.format(timeHint,version, lyear,lmonth);
   		
   		new AlertDialog.Builder(this)
   		.setTitle("提示")
   		.setMessage(timeHint)
           .setPositiveButton("确定", new DialogInterface.OnClickListener() {
   			
   			@Override
   			public void onClick(DialogInterface dialog, int which) {
   				// TODO Auto-generated method stub
   			}
   		})
        .show();
   	}
    private void showHintDialog(int stringId)
   	{
      new AlertDialog.Builder(this)
   		.setTitle("提示")
   		.setMessage(stringId)
           .setPositiveButton("确定", new DialogInterface.OnClickListener() {
   			
   			@Override
   			public void onClick(DialogInterface dialog, int which) {
   				// TODO Auto-generated method stub
   			}
   		})
           .show();
   	}
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	LanSoEditor.unInitSo();
    	SDKFileUtils.deleteDir(new File(SDKDir.TMP_DIR));
    }
    
    private boolean checkPath(){
    	if(tvVideoPath.getText()!=null && tvVideoPath.getText().toString().isEmpty()){
    		Toast.makeText(MainActivity.this, "请输入视频地址", Toast.LENGTH_SHORT).show();
    		return false;
    	}	
    	else{
    		String path=tvVideoPath.getText().toString();
    		if((new File(path)).exists()==false){
    			Toast.makeText(MainActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
    			return false;
    		}else{
    			MediaInfo info=new MediaInfo(path,false);
    			boolean ret=info.prepare();
    	        Log.i(TAG,"info:"+info.toString());
    			return ret;
    		}
    	}
    }
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(isPermissionOk)
		{
			if(checkPath()==false)
				return;
			switch (v.getId()) {
				case R.id.id_main_outbody:
					//以下这些都是测试具体的一些图层或功能,您可以打开测试或直接使用.
//					startDemoActivity(ExecuteTwoVideoLayerDemoActivity.class);
//					startDemoActivity(TwoVideoLayerRealTimeActivity.class);
//					startDemoActivity(VideoLayerRealTimeActivity.class);
					startDemoActivity(OutBodyDemoActivity.class);
//					startDemoActivity(ExecuteTwoBitmapActivity.class);
					break;
				case R.id.id_main_cameralayer:
					startDemoActivity(CameraLayerRectActivity.class);
//					startDemoActivity(CameraLayerEXTPcmActivity.class);
					break;
				case R.id.id_main_camerafulllayer:
					startDemoActivity(CameraLayerFullRecordActivity.class);
//					startDemoActivity(CameraTestListActivity.class);
					break;
				case R.id.id_main_camerafulllayer2:
					startDemoActivity(CameraLayerFullLandscapeActivity.class);
					break;
				case R.id.id_main_cameralayer_segment:
					startDemoActivity(CameraLayerFullSegmentActivity.class);
					break;
				case R.id.id_main_extract_frame:
					startDemoActivity(ExtractFrameTypeListActivity.class);
					break;
				case R.id.id_main_viewlayerdemo1:
					startDemoActivity(ViewLayerDemoActivity.class);
					break;
				case R.id.id_main_viewremark:
					startDemoActivity(BitmapLayerMarkActivity.class);
					break;
				case R.id.id_main_viewlayerdemo2:
					startDemoActivity(ViewLayerOnlyActivity.class);
					break;
				case R.id.id_main_canvaslayerdemo:  //绘制一个心形.
					startDemoActivity(CanvasLayerDemoActivity.class);
					break; 
				case R.id.id_main_videofilterdemo:
					startDemoActivity(FilterDemoRealTimeActivity.class);
					break;
				case R.id.id_main_mvlayerdemo:
					startDemoActivity(MVLayerDemoActivity.class);
					break;
				case R.id.id_main_pictures:
					startDemoActivity(PictureSetRealTimeActivity.class);
					break;
				case R.id.id_main_twovideooverlay:
					startDemoActivity(Demo2LayerMothedActivity.class);
					break;
				case R.id.id_main_videobitmapoverlay:
					startDemoActivity(Demo1LayerMothedActivity.class);
					break;
				case R.id.id_main_drawpadexecute_filter:
					startDemoActivity(ExecuteFilterDemoActivity.class);
					break;
				case R.id.id_main_drawpadpictureexecute:
					startDemoActivity(ExecuteVideoLayerActivity.class);
					break;
				case R.id.id_main_commonversion:
					startDemoActivity(CommonDemoActivity.class);
					break;
				case R.id.id_main_testvideoplay:
					startDemoActivity(VideoPlayerActivity.class);
					break;
				default:
					break;
			}
		}else{
			showHintDialog(R.string.permission_hint);
		}
	}
	//-----------------------------
	 private final static int SELECT_FILE_REQUEST_CODE=10;
	  	private void startSelectVideoActivity()
	    {
	    	Intent i = new Intent(this, FileExplorerActivity.class);
		    startActivityForResult(i,SELECT_FILE_REQUEST_CODE);
	    }
	    @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    	// TODO Auto-generated method stub
	    	super.onActivityResult(requestCode, resultCode, data);
	    	switch (resultCode) {
			case RESULT_OK:
					if(requestCode==SELECT_FILE_REQUEST_CODE){
						Bundle b = data.getExtras();   
			    		String string = b.getString("SELECT_VIDEO");   
						Log.i("sno","SELECT_VIDEO is:"+string);
						if(tvVideoPath!=null)
							tvVideoPath.setText(string);
					}
				break;
			default:
				break;
			}
	    }
	   	private void startDemoActivity(Class<?> cls)
	   	{
	   		String path=tvVideoPath.getText().toString();
	    	Intent intent=new Intent(MainActivity.this,cls);
	    	intent.putExtra("videopath", path);
	    	startActivity(intent);
	   	}
	   	//------------------------------------------------------------
	   @SuppressLint("NewApi") 
		  public static boolean selfPermissionGranted(Context context,String permission) {
		        // For Android < Android M, self permissions are always granted.
		        boolean result = true;
		        int targetSdkVersion = 0;
		        try {
		            final PackageInfo info = context.getPackageManager().getPackageInfo(
		                    context.getPackageName(), 0);
		            targetSdkVersion = info.applicationInfo.targetSdkVersion;
		        } catch (PackageManager.NameNotFoundException e) {
		            e.printStackTrace();
		        }

		        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
		            if (targetSdkVersion >= Build.VERSION_CODES.M) {
		                // targetSdkVersion >= Android M, we can
		                // use Context#checkSelfPermission
		                result = context.checkSelfPermission(permission)
		                        == PackageManager.PERMISSION_GRANTED;
		            } else {
		                // targetSdkVersion < Android M, we have to use PermissionChecker
		                result = PermissionChecker.checkSelfPermission(context, permission)
		                        == PermissionChecker.PERMISSION_GRANTED;
		            }
		        }
		        return result;
		 }
}
