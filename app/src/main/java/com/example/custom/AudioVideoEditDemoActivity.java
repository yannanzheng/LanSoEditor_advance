package com.example.custom;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.lansoeditor.demo.R;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.onVideoEditorProgressListener;

import java.io.File;


public class AudioVideoEditDemoActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "AudioVideoEditDemoActivity";
    private String testDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/av_test_dir";
    private String testOriginVideoPath = testDirPath + "/origin.mp4";
    private String tempAudioPath = testDirPath + "/testAudio.aac";
    private String tempVideoPath = testDirPath + "/testVideo.mp4";
    private String destVideoPath = testDirPath + "/destVideo.mp4";

    private Button deleteAudioButton;
    private Button deleteVideoButton;
    private Button mergeAVButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_merge_demo);
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
                mergeAudioVideo();
                break;
        }
    }

    /**
     * 合并音频和视频测试成功
     */
    private void mergeAudioVideo() {
        VideoEditor editor = new VideoEditor();
        editor.setOnProgessListener(new onVideoEditorProgressListener() {
            @Override
            public void onProgress(VideoEditor v, int percent) {
                Log.d(TAG,"mergeAudioVideo, progress = "+percent );
            }
        });
        editor.executeVideoMergeAudio(tempVideoPath, tempAudioPath, destVideoPath);

    }

    /**
     * 删除mp4中的视频,输出mp4，测试没问题
     */
    public void deleteAudio() {
        Log.d(TAG, "点击删除音频，输出mp4视频");
        VideoEditor editor=new VideoEditor();
        editor.setOnProgessListener(new onVideoEditorProgressListener() {
            @Override
            public void onProgress(VideoEditor v, int percent) {
                Log.d(TAG,"deleteAudio, progress = "+percent );
            }
        });
        editor.executeDeleteAudio(testOriginVideoPath, tempVideoPath);

    }

    /**
     * 删除视频，输出aac，测试没问题
     */
    public void deleteVideo(){
        Log.d(TAG, "点击删除视频，输出aac音频");
        VideoEditor editor=new VideoEditor();
        editor.setOnProgessListener(new onVideoEditorProgressListener() {
            @Override
            public void onProgress(VideoEditor v, int percent) {
                Log.d(TAG,"deleteVideo, progress = "+percent );
            }
        });
        editor.executeDeleteVideo(testOriginVideoPath, tempAudioPath);

    }


}
