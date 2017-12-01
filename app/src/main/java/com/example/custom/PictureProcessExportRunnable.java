package com.example.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.custom.util.ActivityUtils;
import com.lansosdk.videoeditor.BitmapPadExecute;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFHudsonFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFLomofiFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBeautyFilter;

/**
 * 对Bitmap进行处理并导出到指定文件
 * Created by jfyang on 11/30/17.
 */

public class PictureProcessExportRunnable implements Runnable {
    private static String TAG = "simulate_task";
    private Context context;
    private static volatile int j = 0;
    private String sourceFilePath = null;
    private int imageFilterType;
    private float facebeautyLevel;

//    private LanSongBeautyFilter beautyFilter;
//    private GPUImageFilter imageFilter;
    private String destFilePath;

    private OnProcessListener onProcessListener;

    public PictureProcessExportRunnable(Context context, String sourceFilePath, String destFilePath) {
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
    public PictureProcessExportRunnable(Context context, String sourceFilePath, String destFilePath, float facebeautyLevel, int imageFilterType) {
        this.context = context;
        this.sourceFilePath = sourceFilePath;
        this.facebeautyLevel = facebeautyLevel;
        this.imageFilterType = imageFilterType;
        this.destFilePath = destFilePath;
    }

    @Override
    public void run() {
        Bitmap filterBitmap = processPicture(sourceFilePath);
        try {
            exportPicture(filterBitmap);
            if (null != onProcessListener) {
                onProcessListener.onSucess(destFilePath);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (null != onProcessListener) {
                onProcessListener.onFail();
            }

        }
    }

    private void exportPicture(Bitmap filterBitmap) throws FileNotFoundException {
        filterBitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(destFilePath));
        ActivityUtils.notifyMediaScannerScanFile(context, new File(destFilePath));
    }

    private Bitmap processPicture(String sourceFilePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(sourceFilePath);
        BitmapPadExecute bitmapPadExecute = new BitmapPadExecute(context);
        boolean isExecuteInitSuccess = bitmapPadExecute.init(bitmap.getWidth(), bitmap.getHeight());
        if (!isExecuteInitSuccess) {
            return null;
        }
        LanSongBeautyFilter beautyFilter = new LanSongBeautyFilter();
        beautyFilter.setBeautyLevel(facebeautyLevel);

        GPUImageFilter imageFilter = getFilter(imageFilterType);

        Bitmap bmp = bitmapPadExecute.getFilterBitmap(bitmap, beautyFilter);
        Bitmap filterBitmap = bitmapPadExecute.getFilterBitmap(bmp, imageFilter);
        bitmapPadExecute.release();
        return filterBitmap;
    }

    public GPUImageFilter getFilter(int filterType) {
        GPUImageFilter gpuImageFilter = null;
        switch (filterType) {
            case MediaEditType.Filter.Filter_NULL://0
                gpuImageFilter =null;
                break;
            case MediaEditType.Filter.Filter_IFHudson://1
                gpuImageFilter =new IFLomofiFilter(context);
                break;
            case MediaEditType.Filter.Filter_IFLomofi://2
                gpuImageFilter =new IFHudsonFilter(context);
                break;

            case MediaEditType.Filter.Filter_IFSierra://3
                gpuImageFilter =new IFHudsonFilter(context);
                break;

            case MediaEditType.Filter.Filter_IFRise://4
                gpuImageFilter =new IFHudsonFilter(context);
                break;

            case MediaEditType.Filter.Filter_IFAmaro://5
                gpuImageFilter =new IFHudsonFilter(context);
                break;

            case MediaEditType.Filter.Filter_IFWalden://6
                gpuImageFilter =new IFHudsonFilter(context);
                break;

            case MediaEditType.Filter.Filter_IFNashville://7
                gpuImageFilter =new IFHudsonFilter(context);
                break;

            case MediaEditType.Filter.Filter_IFBrannan://8
                gpuImageFilter =new IFHudsonFilter(context);
                break;

            case MediaEditType.Filter.Filter_IFInkwell://9
                gpuImageFilter =new IFHudsonFilter(context);
                break;

            case MediaEditType.Filter.Filter_IFToaster://10
                gpuImageFilter =new IFHudsonFilter(context);
                break;

            case MediaEditType.Filter.Filter_IF1977://11
                gpuImageFilter =new IFHudsonFilter(context);
                break;

            case MediaEditType.Filter.Filter_LanSongSepia://12
                gpuImageFilter =new IFHudsonFilter(context);
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
