package com.example.advanceDemo;



import java.util.ArrayList;
import java.util.List;

import com.lansoeditor.demo.R;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.Layer;
import com.lansosdk.box.VideoLayer2;
import com.lansosdk.box.onCompressCompletedListener;
import com.lansosdk.box.onCompressProgressListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.box.onVideoLayer2ProgressListener;
import com.lansosdk.videoeditor.DrawPadView;
import com.lansosdk.videoeditor.MediaInfo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class TestLayerPlayerActivity extends Activity implements  OnClickListener{
    private static final String TAG = "TestLayerPlayerActivity";
    private DrawPadView drawPadView;
    private VideoLayer2  videoLayer;
    private TextView  tvProgress;
    private String videoPath;
    private  SeekBar  skbProgess;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layplayer_layout);
        
        videoPath = getIntent().getStringExtra("videopath");
        
        
        drawPadView = (DrawPadView) findViewById(R.id.id_test_playerview);
        tvProgress=(TextView)findViewById(R.id.id_test_playerTv);

        findViewById(R.id.id_test_btn_pause).setOnClickListener(this);
        findViewById(R.id.id_test_btn_backplay).setOnClickListener(this);
        findViewById(R.id.id_test_btn_replace).setOnClickListener(this);
        
        
        skbProgess=(SeekBar)findViewById(R.id.id_test_seekbar);
        skbProgess.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(videoLayer!=null && fromUser){
					float precent= (float)progress/100f;
					long seektimeUs=(long)(videoLayer.getVideoDuration() *precent);  //转微秒.
					videoLayer.seekPause(seektimeUs);
				}
			}
		});
        
		  new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				 startPlayVideo();
			}
		}, 500);
    }
    long testStarttime=0;
    private void startPlayVideo()
    {
        	/**
        	 * 设置当前DrawPad的宽度和高度,并把宽度自动缩放到父view的宽度,然后等比例调整高度.
        	 */
        	drawPadView.setDrawPadSize(480,480,new onDrawPadSizeChangedListener() {
    			@Override
    			public void onSizeChanged(int viewWidth, int viewHeight) {
    				startDrawPad();
    			}
    		});
        	drawPadView.setUpdateMode(DrawPadUpdateMode.AUTO_FLUSH, 25);
    }
    private void startDrawPad()
    {
		if(drawPadView.startDrawPad())
		{
			videoLayer=drawPadView.addVideoLayer2(videoPath, null);
			videoLayer.setOnVideoLayer2ProgressListener(new onVideoLayer2ProgressListener() {
				
				@Override
				public void onProgress(Layer layer, long currentPtsUs) {
					float time=(float)currentPtsUs/1000000f;
					float b   =  (float)(Math.round(time*10))/10;  //保留一位小数.
					tvProgress.setText("当前时间戳是:"+b);
					if(skbProgess!=null){
						float progress=(float)currentPtsUs/(float)videoLayer.getVideoDuration();  //转微秒.
						progress*=100;
						skbProgess.setProgress((int)progress);
					}
				}
			});
		}
    }
    @Override
    protected void onDestroy() {
    	super.onDestroy();
			if(drawPadView!=null){
				drawPadView.stopDrawPad();
				drawPadView=null;        		   
			}
	}
    @Override
    public void onClick(View v) {
    	
    	if(drawPadView==null){
    		return ;
    	}
    	switch (v.getId()) {
			case R.id.id_test_btn_pause:	
				if(videoLayer.isPaused()){
					videoLayer.resume(); 
				}else{
					videoLayer.pause();
				}
				videoLayer.setPlaySpeed(1.0f); 
				break;
			case R.id.id_test_btn_backplay:
				videoLayer.setPlaySpeed(0.7f);  //范围0.5---2.0
				break;
			case R.id.id_test_btn_replace:
				videoLayer.setPlaySpeed(2.0f); //范围0.5---2.0
				break;
			default:
				break;
		}
    }
}
