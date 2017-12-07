package com.example.custom;

import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.lansoeditor.demo.R;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.onVideoEditorProgressListener;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM;
import static android.media.MediaFormat.KEY_MIME;


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
//                mergeAudioVideo();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mergeVideo(tempVideoPath,tempAudioPath,destVideoPath);
                    }
                }).start();

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

    public int getTrack(MediaExtractor extractor,String mime)
    {
        for(int i = 0 ; i < extractor.getTrackCount();i++)
        {
            MediaFormat format = extractor.getTrackFormat(i);
            String key_mime = format.getString(KEY_MIME);
            if(key_mime.startsWith(mime))
            {
                return i;
            }
        }
        return -1;
    }

    public int mergeVideo(String videoFile,String audioFile,String outMp4File){
        MediaExtractor extractor = new MediaExtractor();
        MediaExtractor audioExtractor = new MediaExtractor();
        try {
            extractor.setDataSource(videoFile);
            audioExtractor.setDataSource(audioFile);
        }catch (IOException ex)
        {
            return -1;
        }

        int videoTrack = getTrack(extractor,"video/");
        int audioTrack = getTrack(audioExtractor,"audio/");
        if(videoTrack == -1)
        {
            return -1;
        }
        if(audioTrack == -1)
        {
            return -1;
        }

        extractor.selectTrack(videoTrack);
        audioExtractor.selectTrack(audioTrack);
        MediaFormat videoFormat = extractor.getTrackFormat(videoTrack);
        MediaFormat audioFormat = audioExtractor.getTrackFormat(audioTrack);

        MediaMuxer muxer = null;
        try {
            muxer = new MediaMuxer(outMp4File, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        }catch(IOException ex)
        {
            return -1;
        }
        int videoIndex = muxer.addTrack(videoFormat);
        int audioIndex = muxer.addTrack(audioFormat);
        ByteBuffer videoBuffer = ByteBuffer.allocate(1024*1024*5);
        ByteBuffer audioBuffer = ByteBuffer.allocate(1024*1024*5);
        muxer.start();

        boolean videoEnd = false;
        boolean audioEnd = false;
        while(true)
        {
            int vRet = extractor.readSampleData(videoBuffer,0);
            if(vRet == -1)
            {
                videoEnd = true;
            }
            long vTime = extractor.getSampleTime();
            int vFlags = extractor.getSampleFlags();

            int aRet = audioExtractor.readSampleData(audioBuffer,0);
            if(aRet == -1)
            {
                audioEnd = true;
            }
            long aTime = audioExtractor.getSampleTime();
            int aFlags = audioExtractor.getSampleFlags();

            if(videoEnd && audioEnd)
            {
                MediaCodec.BufferInfo bi = new MediaCodec.BufferInfo();
                bi.presentationTimeUs = 0;
                bi.offset = 0;
                bi.size = 0;
                bi.flags = BUFFER_FLAG_END_OF_STREAM;
                muxer.writeSampleData(videoIndex,videoBuffer,bi);
                muxer.writeSampleData(audioIndex,audioBuffer,bi);
                break;
            }
            if(videoEnd)
            {
                //Log.d(TAG,"time: videoEnd:" + aTime + " audio Size:" + aRet);
                MediaCodec.BufferInfo bi = new MediaCodec.BufferInfo();
                bi.presentationTimeUs = aTime;
                bi.offset = 0;
                bi.size = aRet;
                bi.flags = aFlags;
                muxer.writeSampleData(audioIndex,audioBuffer,bi);
                audioExtractor.advance();
            }
            else if(audioEnd)
            {
                //Log.d(TAG,"time: audioEnd:" + vTime + " video Size:" + vRet);
                MediaCodec.BufferInfo bi = new MediaCodec.BufferInfo();
                bi.presentationTimeUs = vTime;
                bi.offset = 0;
                bi.size = vRet;
                bi.flags = vFlags;
                muxer.writeSampleData(videoIndex,videoBuffer,bi);
                extractor.advance();
            }
            else if(vTime <= aTime)
            {
                //Log.d(TAG,"time:V-A" + vTime + " " + aTime + " video Size:" + vRet);
                MediaCodec.BufferInfo bi = new MediaCodec.BufferInfo();
                bi.presentationTimeUs = vTime;
                bi.offset = 0;
                bi.size = vRet;
                bi.flags = vFlags;
                muxer.writeSampleData(videoIndex,videoBuffer,bi);
                extractor.advance();
            }else
            {
                //Log.d(TAG,"time:V-A :" + vTime + " " + aTime + " audio Size:" + aRet);
                MediaCodec.BufferInfo bi = new MediaCodec.BufferInfo();
                bi.presentationTimeUs = aTime;
                bi.offset = 0;
                bi.size = aRet;
                bi.flags = aFlags;
                muxer.writeSampleData(audioIndex,audioBuffer,bi);
                audioExtractor.advance();
            }
        }
        while(true)
        {
            try {
                muxer.stop();
            }catch(IllegalStateException ex)
            {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            break;
        }
        muxer.release();
        //Log.d(TAG,"over.");
        return 0;
    }

}
