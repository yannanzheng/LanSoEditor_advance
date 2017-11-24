package com.example.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.Layer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.videoeditor.DrawPadVideoExecute;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.VideoEditor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBeautyFilter;

public class NewVideoOneDo {

    private String sourceFilePath;
    private String destFilePath;
    private String extractSourceAudioPath; //从源视频中分离出的音频临时文件.

    private String editTmpPath = null;

    private DrawPadVideoExecute mDrawPad = null;
    private boolean isExecuting = false;

    private BitmapLayer logoBmpLayer = null;

    private Context context;

    private long startTimeUs = 0;
    private LanSongBeautyFilter beautyFilter;
    private GPUImageFilter videoFilter = null;

    private Bitmap logoBitmap = null;

    private OnProgressListener onProgressListener;
    private OnCompletedListener onCompletedListener = null;

    public NewVideoOneDo(Context ctx, String sourceFilePath, String destFilePath) {
        this.context = ctx;
        this.sourceFilePath = sourceFilePath;
        this.destFilePath = destFilePath;
    }

    /**
     * 设置滤镜
     * @param filter
     */
    public void setFilter(GPUImageFilter filter) {
        videoFilter = filter;
    }

    public void setFaceBeautyFilter(LanSongBeautyFilter beautyFilter) {
        this.beautyFilter = beautyFilter;
    }

    public void setLogo(Bitmap bmp) {
        logoBitmap = bmp;
    }

    /**
     * 开始执行, 内部会开启一个线程去执行.
     * 开启成功,返回true. 失败返回false;
     *
     * @return
     */
    public boolean start() {
        if (isExecuting) {
            return false;
        }

        MediaInfo srcInfo = new MediaInfo(sourceFilePath, false);
        if (srcInfo.prepare() == false) {
            return false;
        }
        Log.d("feature_847", "srcInfo = " + srcInfo);

        if (srcInfo.isHaveAudio()) {
            VideoEditor editor = new VideoEditor();
            File destDir = new File(destFilePath).getParentFile();
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            extractSourceAudioPath = destDir.getAbsolutePath() + "/abstract_music.aac";
            Log.d("feature_847", "sourceFilePath ＝ " + sourceFilePath + ", extractSourceAudioPath = " + extractSourceAudioPath);

            editor.executeDeleteVideo(sourceFilePath, extractSourceAudioPath);//删除视频，提取音频
        }

        isExecuting = true;
        return startVideoThread(srcInfo);
    }

    /**
     * 开启视频的DrawPad容器处理
     */
    private boolean startVideoThread(final MediaInfo srcInfo) {
        int padWidth = srcInfo.vWidth;
        int padHeight = srcInfo.vHeight;
        if (srcInfo.vRotateAngle == 90 || srcInfo.vRotateAngle == 270) {
            padWidth = srcInfo.vHeight;
            padHeight = srcInfo.vWidth;
        }

        editTmpPath = new File(destFilePath).getParent() + "/temp_edit_video.mp4";
        Log.d("feature_847", "sourceFilePath = " + sourceFilePath + ", startTimeUs = " + startTimeUs + ", padWidth = " + padWidth + ", videoFilter = " + videoFilter + ", editTmpPath = " + editTmpPath);
        mDrawPad = new DrawPadVideoExecute(context, sourceFilePath, padWidth, padHeight, null, editTmpPath);
        mDrawPad.setUseMainVideoPts(true);
        /**
         * 设置DrawPad处理的进度监听, 回传的currentTimeUs单位是微秒.
         */
        mDrawPad.setDrawPadProgressListener(new onDrawPadProgressListener() {
            @Override
            public void onProgress(DrawPad v, long currentTimeUs) {

                if (onProgressListener != null) {
                    float time = (float) currentTimeUs / 1000000f;

                    float percent = time / (float) srcInfo.vDuration;

                    float b = (float) (Math.round(percent * 100)) / 100;  //保留两位小数.
                    if (b < 1.0f && onProgressListener != null && isExecuting) {
                        onProgressListener.onProgress(NewVideoOneDo.this, b);
                    }
                }
            }
        });
        /**
         * 设置DrawPad处理完成后的监听.
         */
        mDrawPad.setDrawPadCompletedListener(new onDrawPadCompletedListener() {

            @Override
            public void onCompleted(DrawPad v) {
                completeDrawPad();
            }
        });

        mDrawPad.pauseRecord();
        if (mDrawPad.startDrawPad()) {
            Layer mainVideoLayer = mDrawPad.getMainVideoLayer();
            ArrayList<GPUImageFilter> gpuImageFilters = new ArrayList<>();
            if (null != beautyFilter) {
                gpuImageFilters.add(beautyFilter);
            }
            if (null != videoFilter) {
                gpuImageFilters.add(videoFilter);
            }
            mainVideoLayer.switchFilterList(gpuImageFilters);
            addLogo(); //增加图片图层
            mDrawPad.resumeRecord();  //开始恢复处理.
            return true;
        } else {
            return false;
        }
    }

    /**
     * 处理完成后的动作.
     */
    private void completeDrawPad() {
        if (isExecuting == false) {
            return;
        }

        Log.i("feature_847", "destFilePath = " + destFilePath + ",isExecuting = " + isExecuting + ", extractSourceAudioPath = " + extractSourceAudioPath);

        if (extractSourceAudioPath != null && isExecuting) {  //增加原音.
            videoMergeAudio(editTmpPath, extractSourceAudioPath, destFilePath);
        } else {
            new File(editTmpPath).renameTo(new File(destFilePath));
        }

        new File(editTmpPath).delete();
        new File(extractSourceAudioPath).delete();

        if (onCompletedListener != null && isExecuting) {
            onCompletedListener.onCompleted(NewVideoOneDo.this, destFilePath);
        }
        isExecuting = false;
        Log.d("feature_847", "最后的视频文件是:" + MediaInfo.checkFile(destFilePath));
    }

    public void stop() {
        if (isExecuting) {
            isExecuting = false;

            onCompletedListener = null;
            onProgressListener = null;
            if (mDrawPad != null) {
                mDrawPad.stopDrawPad();
            }
            sourceFilePath = null;
            mDrawPad = null;

            logoBitmap = null;
        }
    }

    public void release() {
        stop();
    }

    /**
     * 增加logo
     */
    private void addLogo() {
        if (logoBitmap != null) {
            logoBmpLayer = mDrawPad.addBitmapLayer(logoBitmap);
            if (logoBmpLayer != null) {
                logoBmpLayer.setPosition(logoBmpLayer.getPadWidth() / 2, logoBmpLayer.getPadHeight() / 2);
            }
        }
    }

    /**
     * 之所有从VideoEditor.java中拿过来另外写, 是为了省去两次MediaInfo的时间;
     */
    private void videoMergeAudio(String videoFile, String audioFile, String dstFile) {
        VideoEditor editor = new VideoEditor();
        List<String> cmdList = new ArrayList<String>();

        cmdList.add("-i");
        cmdList.add(videoFile);

        cmdList.add("-i");
        cmdList.add(audioFile);

        cmdList.add("-vcodec");
        cmdList.add("copy");
        cmdList.add("-acodec");
        cmdList.add("copy");

        cmdList.add("-absf");
        cmdList.add("aac_adtstoasc");

        cmdList.add("-y");
        cmdList.add(dstFile);
        String[] command = new String[cmdList.size()];
        for (int i = 0; i < cmdList.size(); i++) {
            command[i] = (String) cmdList.get(i);
        }
        editor.executeVideoEditor(command);
    }

    public void setOnVideoOneDoProgressListener(OnProgressListener listener) {
        onProgressListener = listener;
    }

    public void setOnVideoOneDoCompletedListener(OnCompletedListener listener) {
        onCompletedListener = listener;
    }

    public interface OnCompletedListener {
        void onCompleted(NewVideoOneDo v, String dstVideo);
    }

    public interface OnProgressListener {
        void onProgress(NewVideoOneDo v, float percent);
    }
}
