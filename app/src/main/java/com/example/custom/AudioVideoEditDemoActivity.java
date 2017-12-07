package com.example.custom;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.lansoeditor.demo.R;

import java.io.File;


public class AudioVideoEditDemoActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "AudioVideoEditDemoActivity";
    private String testDirPath;
    private Button deleteAudioButton;
    private Button deleteVideoButton;
    private Button mergeAVButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_merge_demo);
        testDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/av_test_dir";
        File testDir = new File(testDirPath);
        if (!testDir.exists()) {
            testDir.mkdirs();
        }

        initView();






    }

    private void initView() {
        deleteAudioButton = (Button) findViewById(R.id.delete_audio_bt);
        deleteAudioButton.setOnClickListener(this);

        deleteVideoButton = (Button) findViewById(R.id.delete_video_bt);
        deleteVideoButton.setOnClickListener(this);

        mergeAVButton = (Button) findViewById(R.id.merge_video_audio_bt);
        mergeAVButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delete_audio_bt:
                deleteAudio();
                break;
            case R.id.delete_video_bt:
                deleteVideo();
                break;
            case R.id.merge_video_audio_bt:

                break;
        }
    }

    /**
     * 删除mp4中的视频,输出mp4
     */
    public void deleteAudio() {
        Log.d(TAG, "点击删除音频，输出mp4视频");






    }

    /**
     * 删除视频，输出aac
     */
    public void deleteVideo(){
        Log.d(TAG, "点击删除视频，输出aac音频");



    }


}
