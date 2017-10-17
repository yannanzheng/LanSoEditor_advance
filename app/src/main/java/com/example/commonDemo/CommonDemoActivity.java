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

public class CommonDemoActivity extends Activity{

	private DemoInfo[] mTestCmdArray={ 
			
			 new DemoInfo(R.string.demo_id_mediainfo,R.string.demo_id_mediainfo,true,false),
			 new DemoInfo(R.string.demo_id_avsplit,R.string.demo_more_avsplit,true,true),//是否视频输出, 是否音频输出
			 new DemoInfo(R.string.demo_id_avmerge,R.string.demo_more_avmerge,true,false),
			 new DemoInfo(R.string.demo_id_cutaudio,R.string.demo_more_cutaudio,false,true),
			 new DemoInfo(R.string.demo_id_cutvideo,R.string.demo_more_cutvideo,true,false),
			 new DemoInfo(R.string.demo_id_concatvideo,R.string.demo_more_concatvideo,true,false),
			 
	};
	private ListView  mListView=null;
	private boolean isPermissionOk=false;
	 private static final String TAG="MainActivity";
	 private String mVideoPath;
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
			
				Intent intent=new Intent(CommonDemoActivity.this,AVEditorDemoActivity.class);
				
				intent.putExtra("videopath1",mVideoPath.toString());
				intent.putExtra("outvideo", demo.isOutVideo);
				intent.putExtra("outaudio", demo.isOutAudio);
				intent.putExtra("demoID", demo.mHintId);
				intent.putExtra("textID", demo.mTextId);
				startActivity(intent);
	}
	private void startMediaInfoActivity()
	{
		Intent intent=new Intent(CommonDemoActivity.this,MediaInfoActivity.class);
		intent.putExtra("videopath",mVideoPath.toString());
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
