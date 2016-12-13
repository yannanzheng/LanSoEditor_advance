package com.example.commonDemo;


import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.lansoeditor.demo.R;
import com.lansosdk.videoeditor.LanSoEditor;
import com.lansosdk.videoeditor.LoadLanSongSdk;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 
 *注意, 此代码仅仅是sdk的功能的演示, 不属于sdk的一部分.
 *
 */
public class CommonDemoActivity extends Activity{

	private DemoInfo[] mTestCmdArray={ 
			
			 new DemoInfo(R.string.demo_id_mediainfo,R.string.demo_id_mediainfo,true,false),
			 new DemoInfo(R.string.demo_id_avsplit,R.string.demo_more_avsplit,true,true),//是否视频输出, 是否音频输出
			 new DemoInfo(R.string.demo_id_avmerge,R.string.demo_more_avmerge,true,false),
			 new DemoInfo(R.string.demo_id_cutaudio,R.string.demo_more_cutaudio,false,true),
			 new DemoInfo(R.string.demo_id_cutvideo,R.string.demo_more_cutvideo,true,false),
			 new DemoInfo(R.string.demo_id_concatvideo,R.string.demo_more_concatvideo,true,false),
			 new DemoInfo(R.string.demo_id_videocompress,R.string.demo_more_videocompress,true,false),
			 new DemoInfo(R.string.demo_id_videocrop,R.string.demo_more_videocrop,true,false),
			 new DemoInfo(R.string.demo_id_videoscale_soft,R.string.demo_more_videoscale_soft,true,false),
			 new DemoInfo(R.string.demo_id_videoscale_hard,R.string.demo_more_videoscale_hard,false,false),
			 new DemoInfo(R.string.demo_id_videowatermark,R.string.demo_more_videowatermark,true,false),
			 new DemoInfo(R.string.demo_id_videocropwatermark,R.string.demo_more_videocropwatermark,true,false),
			 new DemoInfo(R.string.demo_id_videogetframes,R.string.demo_more_videogetframes,false,false),
			 new DemoInfo(R.string.demo_id_videogetoneframe,R.string.demo_more_videogetoneframe,false,false),
			 new DemoInfo(R.string.demo_id_videozeroangle,R.string.demo_more_videozeroangle,true,false),
			 new DemoInfo(R.string.demo_id_videoclockwise90,R.string.demo_more_videoclockwise90,true,false),
			 new DemoInfo(R.string.demo_id_videocounterClockwise90,R.string.demo_more_videocounterClockwise90,true,false),
			 new DemoInfo(R.string.demo_id_videoaddanglemeta,R.string.demo_more_videoaddanglemeta,true,false),
//			 new DemoInfo(R.string.demo_id_ontpicturevideo,R.string.demo_more_ontpicturevideo,true,false),
			 new DemoInfo(R.string.demo_id_morepicturevideo,R.string.demo_more_morepicturevideo,true,false),
			 new DemoInfo(R.string.demo_id_audiodelaymix,R.string.demo_more_audiodelaymix,false,true),
			 new DemoInfo(R.string.demo_id_audiovolumemix,R.string.demo_more_audiovolumemix,false,true),
			 new DemoInfo(R.string.demo_id_videopad,R.string.demo_more_videopad,true,false),
			 new DemoInfo(R.string.demo_id_videoadjustspeed,R.string.demo_more_videoadjustspeed,true,false),
			 new DemoInfo(R.string.demo_id_videomirrorh,R.string.demo_more_videomirrorh,true,false),
			 new DemoInfo(R.string.demo_id_videomirrorv,R.string.demo_more_videomirrorv,true,false),
			 new DemoInfo(R.string.demo_id_videorotateh,R.string.demo_more_videorotateh,true,false),
			 new DemoInfo(R.string.demo_id_videorotatev,R.string.demo_more_videorotatev,true,false),
			 new DemoInfo(R.string.demo_id_videoreverse,R.string.demo_more_videoreverse,true,false),

			 new DemoInfo(R.string.demo_id_avreverse,R.string.demo_more_avreverse,true,false),
			 
	};
	private ListView  mListView=null;
	private String mVideoPath;
	 private static final String TAG="MainActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		 setContentView(R.layout.demo_layout);
		 
		 mVideoPath = getIntent().getStringExtra("videopath");
		
		 mListView=(ListView)findViewById(R.id.id_demo_list);
		 mListView.setAdapter(new SoftApAdapter(CommonDemoActivity.this));
		 
		 mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(position==0){
						startMediaInfoActivity();
				}else {
						startActivity(position);	
				}
			}
		});
	}
	private void startActivity(int position)
	{
			DemoInfo demo = mTestCmdArray[position];
			
			if(demo.mHintId==R.string.demo_id_videoscale_hard)
			{
				startScaleActivity();
				
			}else{
				Intent intent=new Intent(CommonDemoActivity.this,AVEditorDemoActivity.class);
				
				intent.putExtra("videopath1",mVideoPath);
				intent.putExtra("outvideo", demo.isOutVideo);
				intent.putExtra("outaudio", demo.isOutAudio);
				intent.putExtra("demoID", demo.mHintId);
				intent.putExtra("textID", demo.mTextId);
				
				startActivity(intent);
			}
	}
	private void startMediaInfoActivity()
	{
		Intent intent=new Intent(CommonDemoActivity.this,MediaInfoActivity.class);
		intent.putExtra("videopath", mVideoPath);
		startActivity(intent);
	}

	  //-----------------------
	  private void startScaleActivity()  //开启硬件缩放
	  {
		  Intent intent=new Intent(CommonDemoActivity.this,ScaleExecuteDemoActivity.class);
	    	intent.putExtra("videopath", mVideoPath);
	    	startActivity(intent);
	  }
	//------------------------------------------
		private class SoftApAdapter extends BaseAdapter
		{
		    
		    private Activity mActivity;
		    
		    public SoftApAdapter(Activity activity)
		    {
		        mActivity = activity;
		    }
		    
		    @Override
		    public int getCount()
		    {
		        return mTestCmdArray.length;
		    }
		    
		    @Override
		    public Object getItem(int position)
		    {
		        return mTestCmdArray[position];
		    }
		    
		    @Override
		    public long getItemId(int position)
		    {
		        return 0;
		    }
		    
		    @Override
		    public View getView(int position, View convertView, ViewGroup parent)
		    {
			        if (convertView == null)
			        {
			            LayoutInflater inflater = mActivity.getLayoutInflater();
			            convertView = inflater.inflate(R.layout.test_cmd_item, parent, false);
			        }
			        
			        TextView tvNumber = (TextView)convertView.findViewById(R.id.id_test_cmditem_cnt);
			        
			        TextView tvName = (TextView)convertView.findViewById(R.id.id_test_cmditem_tv);
			        
			        DemoInfo cmdInfo = mTestCmdArray[position];
			        
			        String str="NO.";
					 str+=String.valueOf(position+1);
					 
					 tvNumber.setText(str);
					 
					 tvName.setText(getResources().getString(cmdInfo.mHintId));
					 
			        return convertView;
		    }
		}
}
