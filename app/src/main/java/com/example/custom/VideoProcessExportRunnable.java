package com.example.custom;

import android.content.Context;
import android.util.Log;

import com.example.custom.dependency.MediaEditType;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.videoeditor.DrawPadVideoExecute;
import com.lansosdk.videoeditor.MediaInfo;

import java.io.File;
import java.util.ArrayList;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IF1977Filter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFAmaroFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFBrannanFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFHudsonFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFInkwellFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFLomofiFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFNashvilleFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFRiseFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFSierraFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFToasterFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFWaldenFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBeautyFilter;

/**
 * 对Video file进行处理并导出到指定文件
 * Created by jfyang on 11/30/17.
 */

public class VideoProcessExportRunnable implements Runnable {
    private static final int MAX_BIT_RATE = 3 * 1024 * 1024;
    private static String TAG = "simulate_task";
    private Context context;
    private static volatile int j = 0;
    private String sourceFilePath = null;
    private int imageFilterType;
    private float facebeautyLevel;

    private String destFilePath;

    private OnProcessListener onProcessListener;

    public VideoProcessExportRunnable(Context context, String sourceFilePath, String destFilePath) {
        this.context = context;
        this.sourceFilePath = sourceFilePath;
        this.destFilePath = destFilePath;
    }

    /**
     * @param context 上下文
     * @param sourceFilePath  待处理的bitmap
     * @param facebeautyLevel 美颜滤镜类型
     * @param imageFilterType 滤镜类型
     * @param destFilePath 处理后输出位置
     */
    public VideoProcessExportRunnable(Context context, String sourceFilePath, String destFilePath, float facebeautyLevel, int imageFilterType) {
        this.context = context;
        this.sourceFilePath = sourceFilePath;
        this.facebeautyLevel = facebeautyLevel;
        this.imageFilterType = imageFilterType;
        this.destFilePath = destFilePath;
    }

    @Override
    public void run() {
        File destFile = new File(destFilePath);
        if (destFile.exists()) {
            destFile.delete();
        }

        final MediaInfo mediaInfo = new MediaInfo(sourceFilePath);
        boolean prepare = mediaInfo.prepare();
        if (!prepare) {
            fail();
            return;
        }

        int padWidth = 1440;
        int padHeight = 720;
        if(mediaInfo.vRotateAngle==90 || mediaInfo.vRotateAngle==270){
            padWidth = 720;
            padHeight = 1440;
        }

        String simpleFileName = mediaInfo.fileName;
        Log.d("compress", "simpleFileName = " + simpleFileName);


        DrawPadVideoExecute mDrawPad = new DrawPadVideoExecute(
                  context
                , sourceFilePath
                , padWidth
                , padHeight
                , MAX_BIT_RATE
                , null
                , destFilePath);
        mDrawPad.setUseMainVideoPts(true);

        mDrawPad.setDrawPadProgressListener(new onDrawPadProgressListener() {
            @Override
            public void onProgress(DrawPad v, long currentTimeUs) {
                Log.d("compress", "on Progress currentTimeUs = "+currentTimeUs);
                double progress = ((currentTimeUs/1000)/(mediaInfo.vDuration * 1000) )/2;
                Log.d("compress", "progress = " + progress);
                progress(progress);
            }
        });

        mDrawPad.setDrawPadCompletedListener(new onDrawPadCompletedListener() {
            @Override
            public void onCompleted(DrawPad drawPad) {
                success();
            }
        });

        mDrawPad.pauseRecord();

        if (mDrawPad.startDrawPad()) {
            if (MediaEditType.FaceBeauty.LEVEL_0 != facebeautyLevel || MediaEditType.Filter.Filter_NULL != imageFilterType) {
                Log.d("feature_847", "没编辑过的文件不应该走这里");
                ArrayList<GPUImageFilter> gpuImageFilters = new ArrayList<>();

                LanSongBeautyFilter beautyFilter = new LanSongBeautyFilter();
                beautyFilter.setBeautyLevel(facebeautyLevel);
                GPUImageFilter videoFilter = getFilter(imageFilterType);

                if (null != beautyFilter) {
                    gpuImageFilters.add(beautyFilter);
                }
                if (null != videoFilter) {
                    gpuImageFilters.add(videoFilter);
                }
                mDrawPad.getMainVideoLayer().switchFilterList(gpuImageFilters);
            }

            mDrawPad.resumeRecord();
        }



    }

    private void success() {
        if (null != onProcessListener) {
            onProcessListener.onSucess(destFilePath);
        }
    }


    private void progress(double progress){
        if (null != onProcessListener) {
            onProcessListener.onProgress(progress);
        }
    }

    private void fail() {
        if (null != onProcessListener) {
            onProcessListener.onFail();
        }
    }


    public GPUImageFilter getFilter(int filterType) {
        GPUImageFilter gpuImageFilter = null;
        switch (filterType) {
            case MediaEditType.Filter.Filter_NULL://0
                gpuImageFilter =null;
                break;
            case MediaEditType.Filter.Filter_IFHudson://1
                gpuImageFilter =new IFHudsonFilter(context);
                break;
            case MediaEditType.Filter.Filter_IFLomofi://2
                gpuImageFilter =new IFLomofiFilter(context);
                break;

            case MediaEditType.Filter.Filter_IFSierra://3
                gpuImageFilter =new IFSierraFilter(context);
                break;

            case MediaEditType.Filter.Filter_IFRise://4
                gpuImageFilter =new IFRiseFilter(context);
                break;

            case MediaEditType.Filter.Filter_IFAmaro://5
                gpuImageFilter =new IFAmaroFilter(context);
                break;

            case MediaEditType.Filter.Filter_IFWalden://6
                gpuImageFilter =new IFWaldenFilter(context);
                break;

            case MediaEditType.Filter.Filter_IFNashville://7
                gpuImageFilter =new IFNashvilleFilter(context);
                break;

            case MediaEditType.Filter.Filter_IFBrannan://8
                gpuImageFilter =new IFBrannanFilter(context);
                break;

            case MediaEditType.Filter.Filter_IFInkwell://9
                gpuImageFilter =new IFInkwellFilter(context);
                break;

            case MediaEditType.Filter.Filter_IFToaster://10
                gpuImageFilter =new IFToasterFilter(context);
                break;

            case MediaEditType.Filter.Filter_IF1977://11
                gpuImageFilter =new IF1977Filter(context);
                break;

            case MediaEditType.Filter.Filter_LanSongSepia://12
                gpuImageFilter =new IFLomofiFilter(context);
                break;

        }

        return gpuImageFilter;
    }

    /**
     * 设置美颜滤镜
     * @param facebeautyLevel 美颜等级
     */
    public void setBeautyFilter(int facebeautyLevel) {
        this.facebeautyLevel = facebeautyLevel;
    }

    /**
     * 设置普通滤镜
     * @param imageFilterType 滤镜类型
     */
    public void setImageFilter(int imageFilterType) {
        this.imageFilterType = imageFilterType;
    }

    public void setOnProcessListener(OnProcessListener onProcessListener) {
        this.onProcessListener = onProcessListener;
    }

    public interface OnProcessListener {
        void onSucess(String exportedFilePath);
        void onProgress(double progress);
        void onFail();
    }

}
