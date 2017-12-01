package com.example.custom;

import android.content.Context;

import java.io.File;

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

/**
 * 对Video file进行处理并导出到指定文件
 * Created by jfyang on 11/30/17.
 */

public class VideoProcessExportRunnable implements Runnable {
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
        void onFail();
    }
}
