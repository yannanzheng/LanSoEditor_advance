package com.example.advanceDemo;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;


import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.example.commonDemo.CommonDemoActivity;
import com.lansoeditor.demo.R;
import com.lansosdk.box.LanSoEditorBox;
import com.lansosdk.box.MicLine;
import com.lansosdk.videoeditor.CopyFileFromAssets;
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
import android.media.MediaRecorder.AudioSource;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore.Video;
import android.support.v4.content.PermissionChecker;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
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
        
		Thread.setDefaultUncaughtExceptionHandler(new snoCrashHandler());
        setContentView(R.layout.activity_main);
        
        LoadLanSongSdk.loadLibraries();  //拿出来单独加载库文件.
        LanSoEditor.initSo(getApplicationContext(),null);
        
           
        //这个仅仅用来修改box里面的临时路径,如要修改VideoEidtor产生的路径,则可以在SDKDir中直接修改,不调用, 则默认是/sdcard/lansongBox/文件夹下.
       // LanSoEditorBox.setTempFileDir("/sdcard/testTmp/");
        
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
        
        //以下固定视频仅测试使用.
//        tvVideoPath.setText("/sdcard/VIDEO_36minute.mp4");
//        tvVideoPath.setText("/sdcard/VIDEO_90du.mp4");
//        tvVideoPath.setText("/sdcard/VIDEO_270du.mp4");
        
        
        findViewById(R.id.id_main_segmentrecorder).setOnClickListener(this);
        findViewById(R.id.id_main_camerapen).setOnClickListener(this);
        
        
        findViewById(R.id.id_main_viewpendemo1).setOnClickListener(this);
        findViewById(R.id.id_main_viewremark).setOnClickListener(this);
        findViewById(R.id.id_main_viewpendemo2).setOnClickListener(this);
        findViewById(R.id.id_main_canvaspendemo).setOnClickListener(this);
        findViewById(R.id.id_main_videofilterdemo).setOnClickListener(this);  //画笔滤镜. 用videoPen来演示.
        
        findViewById(R.id.id_main_mvpendemo).setOnClickListener(this);  //画笔滤镜. 用videoPen来演示.
        
        findViewById(R.id.id_main_pictures).setOnClickListener(this);
        
        findViewById(R.id.id_main_commonversion).setOnClickListener(this);
        
        findViewById(R.id.id_main_twovideooverlay).setOnClickListener(this);
        findViewById(R.id.id_main_videobitmapoverlay).setOnClickListener(this);
        
        findViewById(R.id.id_main_drawpadpictureexecute).setOnClickListener(this);
        findViewById(R.id.id_main_drawpadexecute_filter).setOnClickListener(this);
        findViewById(R.id.id_main_testvideoplay).setOnClickListener(this);
        
        
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
				new com.lansosdk.videoeditor.CopyDefaultVideoAsyncTask(MainActivity.this, tvVideoPath, "ping20s.mp4").execute();
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
   		timeHint=String.format(timeHint,VideoEditor.getSDKVersion(), lyear,lmonth);
   		
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
				case R.id.id_main_segmentrecorder:
					startDemoActivity(SegmentRecorderActivity.class);
					break;
				case R.id.id_main_camerapen:
					startDemoActivity(CameraPenDemoActivity.class);
					break;
				case R.id.id_main_viewpendemo1:
					startDemoActivity(VideoViewPenDemoActivity.class);
//					startDemoActivity(VideoPenAutoUpdateDemoActivity.class);  //
					break;
				case R.id.id_main_viewremark:
					startDemoActivity(VideoRemarkActivity.class);
					break;
				case R.id.id_main_viewpendemo2:
					startDemoActivity(ViewPenOnlyRealTimeActivity.class);
					break;
				case R.id.id_main_canvaspendemo:
					startDemoActivity(CanvasPenDemoActivity.class);
					break; 
				case R.id.id_main_videofilterdemo:
					startDemoActivity(FilterDemoRealTimeActivity.class);
					break;
				case R.id.id_main_mvpendemo:
					startDemoActivity(MVPenDemoActivity.class);
					break;
				case R.id.id_main_pictures:
					startDemoActivity(PictureSetRealTimeActivity.class);
					break;
				case R.id.id_main_twovideooverlay:
					startDemoActivity(VideoVideoRealTimeActivity.class);
					break;
				case R.id.id_main_videobitmapoverlay:
					startDemoActivity(VideoPictureRealTimeActivity.class);
					break;
				case R.id.id_main_drawpadexecute_filter:
					startDemoActivity(FilterDemoExecuteActivity.class);
					break;
				case R.id.id_main_drawpadpictureexecute:
					startDemoActivity(VideoPictuerExecuteActivity.class);
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
