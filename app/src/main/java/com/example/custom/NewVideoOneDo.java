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

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;

public class NewVideoOneDo {

    public final static int LOGO_POSITION_LELF_TOP = 0;
    public final static int LOGO_POSITION_LEFT_BOTTOM = 1;
    public final static int LOGO_POSITION_RIGHT_TOP = 2;
    public final static int LOGO_POSITION_RIGHT_BOTTOM = 3;

    private String sourceFilePath;
    private String destFilePath;
    private String srcAudioPath; //从源视频中分离出的音频临时文件.

    private String editTmpPath = null;

    private DrawPadVideoExecute mDrawPad = null;
    private boolean isExecuting = false;

    private BitmapLayer logoBmpLayer = null;

    private Context context;

    private long startTimeUs = 0;
    private long cutDurationUs = 0;
    private GPUImageFilter videoFilter = null;

    private Bitmap logoBitmap = null;
    private int logoPosition = LOGO_POSITION_RIGHT_TOP;

    public NewVideoOneDo(Context ctx, String sourceFilePath, String destFilePath) {
        this.context = ctx;
        this.sourceFilePath = sourceFilePath;
        this.destFilePath = destFilePath;
    }

    /**
     * 这里仅仅是举例,用一个滤镜.如果你要增加多个滤镜,可以判断处理进度,来不断切换滤镜
     *
     * @param filter
     */
    public void setFilter(GPUImageFilter filter) {
        videoFilter = filter;
    }

    /**
     * 设置logo的位置, 这里仅仅是举例,您可以拷贝这个代码, 自行定制各种功能.
     * 原理:  增加一个图片图层到容器DrawPad中, 设置他的位置.
     * 位置这里举例是:
     * {@link #LOGO_POSITION_LEFT_BOTTOM}
     * {@link #LOGO_POSITION_LELF_TOP}
     * {@link #LOGO_POSITION_RIGHT_BOTTOM}
     * {@value #LOGO_POSITION_RIGHT_TOP}
     *
     * @param bmp      logo图片对象
     * @param position 位置
     */
    public void setLogo(Bitmap bmp, int position) {
        logoBitmap = bmp;
        if (position <= LOGO_POSITION_RIGHT_BOTTOM) {
            logoPosition = position;
        }
    }

    private OnProgressListener monProgressListener;

    public void setOnVideoOneDoProgressListener(OnProgressListener li) {
        monProgressListener = li;
    }

    private OnCompletedListener monCompletedListener = null;

    public void setOnVideoOneDoCompletedListener(OnCompletedListener li) {
        monCompletedListener = li;
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
            srcAudioPath = "/sdcard/lansongBox/abstract_music.aac";
            Log.d("feature_847", "sourceFilePath ＝ " + sourceFilePath + ", srcAudioPath = " + srcAudioPath);

            editor.executeDeleteVideo(sourceFilePath, srcAudioPath, (float) startTimeUs / 1000000f, (float) cutDurationUs / 1000000f);//删除视频，应该就是提取音频了吧
        }

        isExecuting = true;
        editTmpPath = "/sdcard/lansongBox/temp_edit_video.mp4";
        Log.d("feature_847", "editTmpPath = " + editTmpPath);

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

        Log.d("feature_847", "sourceFilePath = " + sourceFilePath + ", startTimeUs = " + startTimeUs + ", padWidth = " + padWidth + ", videoFilter = " + videoFilter + ", editTmpPath = " + editTmpPath);
        mDrawPad = new DrawPadVideoExecute(context, sourceFilePath, padWidth, padHeight, videoFilter, editTmpPath);
        mDrawPad.setUseMainVideoPts(true);
        /**
         * 设置DrawPad处理的进度监听, 回传的currentTimeUs单位是微秒.
         */
        mDrawPad.setDrawPadProgressListener(new onDrawPadProgressListener() {
            @Override
            public void onProgress(DrawPad v, long currentTimeUs) {

                if (monProgressListener != null) {
                    float time = (float) currentTimeUs / 1000000f;

                    float percent = time / (float) srcInfo.vDuration;

                    float b = (float) (Math.round(percent * 100)) / 100;  //保留两位小数.
                    if (b < 1.0f && monProgressListener != null && isExecuting) {
                        monProgressListener.onProgress(NewVideoOneDo.this, b);
                    }
                }
                if (cutDurationUs > 0 && currentTimeUs > cutDurationUs) {  //设置了结束时间, 如果当前时间戳大于结束时间,则停止容器.
                    mDrawPad.stopDrawPad();
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
            addBitmapLayer(); //增加图片图层
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

        String dstPath = "/sdcard/lansongBox/destVideo.mp4";
        Log.i("feature_847", "dstPath = " + dstPath + ",isExecuting = " + isExecuting + ", srcAudioPath = " + srcAudioPath);

        if (srcAudioPath != null && isExecuting) {  //增加原音.
            videoMergeAudio(editTmpPath, srcAudioPath, dstPath);
        } else {
            dstPath = editTmpPath;
        }

        if (monCompletedListener != null && isExecuting) {
            monCompletedListener.onCompleted(NewVideoOneDo.this, dstPath);
        }
        isExecuting = false;
        Log.d("feature_847", "最后的视频文件是:" + MediaInfo.checkFile(dstPath));
    }

    public void stop() {
        if (isExecuting) {
            isExecuting = false;

            monCompletedListener = null;
            monProgressListener = null;
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
     * 增加图片图层
     */
    private void addBitmapLayer() {
        if (logoBitmap != null) {
            logoBmpLayer = mDrawPad.addBitmapLayer(logoBitmap);
            if (logoBmpLayer != null) {
                int w = logoBmpLayer.getLayerWidth();
                int h = logoBmpLayer.getLayerHeight();
                if (logoPosition == LOGO_POSITION_LELF_TOP) {  //左上角.

                    logoBmpLayer.setPosition(w / 2, h / 2);

                } else if (logoPosition == LOGO_POSITION_LEFT_BOTTOM) {  //左下角

                    logoBmpLayer.setPosition(w / 2, logoBmpLayer.getPadHeight() - h / 2);
                } else if (logoPosition == LOGO_POSITION_RIGHT_TOP) {  //右上角

                    logoBmpLayer.setPosition(logoBmpLayer.getPadWidth() - w / 2, h / 2);

                } else if (logoPosition == LOGO_POSITION_RIGHT_BOTTOM) {  //右下角
                    logoBmpLayer.setPosition(logoBmpLayer.getPadWidth() - w / 2, logoBmpLayer.getPadHeight() - h / 2);
                }
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

    public interface OnCompletedListener {
        void onCompleted(NewVideoOneDo v, String dstVideo);
    }

    public interface OnProgressListener {
        void onProgress(NewVideoOneDo v, float percent);
    }
}
