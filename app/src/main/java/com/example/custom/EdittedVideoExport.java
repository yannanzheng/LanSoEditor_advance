package com.example.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.lansosdk.videoeditor.DrawPadVideoExecute;
import com.lansosdk.videoeditor.MediaInfo;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageLaplacianFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBeautyFilter;

/**
 * Created by jfyang on 11/23/17.
 */

public class EdittedVideoExport {
    private Context mContext;
    private GPUImageFilter mFilter;
    private GPUImageFilter faceBeautyFilter;

    private String sourceFilePath;
    private String destFilePath;
    private Bitmap logoBitmap;

    public EdittedVideoExport(Context context, String sourceFilePath, String destFilePath) {
        this.mContext = context;
        this.sourceFilePath = sourceFilePath;
        this.destFilePath = destFilePath;
    }

    public void setFilter(GPUImageLaplacianFilter filter) {
        this.mFilter = filter;
    }

    public void setFaceBeautyFilter(LanSongBeautyFilter faceBeautyFilter) {
        this.faceBeautyFilter = faceBeautyFilter;
    }

    public void setLogo(Bitmap logoBitmap) {
        this.logoBitmap = logoBitmap;

    }

    public boolean start() {
        NewVideoOneDo videoOneDo=new NewVideoOneDo(mContext, sourceFilePath);
        videoOneDo.setOnVideoOneDoProgressListener(new NewVideoOneDo.onVideoOneDoProgressListener() {

            @Override
            public void onProgress(NewVideoOneDo v, float percent) {
                Log.d("feature_847", "start，progress ＝ " + percent);
            }
        });

        videoOneDo.setOnVideoOneDoCompletedListener(new NewVideoOneDo.onVideoOneDoCompletedListener() {

            @Override
            public void onCompleted(NewVideoOneDo v, String dstVideo) {
                Log.d("feature_847", "start，process completed ");
            }
        });

        videoOneDo.setFilter(mFilter);

        if(videoOneDo.start()){
            Log.d("feature_847", "start，videoInfo 开始执行 " );
        }else{
            Log.d("feature_847", "start，videoInfo 执行失败 " );
        }
        return  true;
    }

    private boolean processVideo(MediaInfo videoInfo) {
        Log.d("feature_847", "processVideo，videoInfo = "+videoInfo);
        DrawPadVideoExecute drawPadVideoExecute = new DrawPadVideoExecute(mContext, sourceFilePath, videoInfo.vWidth, videoInfo.vHeight, mFilter, destFilePath);

        if (drawPadVideoExecute.isRunning()) {
            return false;
        }

        boolean isDrawPadStarted = drawPadVideoExecute.startDrawPad();
        Log.d("feature_847", "processVideo，isDrawPadStarted = "+isDrawPadStarted);

        return true;
    }

}
