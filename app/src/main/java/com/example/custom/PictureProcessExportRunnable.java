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
    private LanSongBeautyFilter beautyFilter;
    private GPUImageFilter imageFilter;
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
     * @param beautyFilter 美颜滤镜
     * @param imageFilter 滤镜
     * @param destFilePath 处理后输出位置
     */
    public PictureProcessExportRunnable(Context context, String sourceFilePath, String destFilePath, LanSongBeautyFilter beautyFilter, GPUImageFilter imageFilter) {
        this.context = context;
        this.sourceFilePath = sourceFilePath;
        this.beautyFilter = beautyFilter;
        this.imageFilter = imageFilter;
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
        Bitmap bmp = bitmapPadExecute.getFilterBitmap(bitmap, beautyFilter);
        Bitmap filterBitmap = bitmapPadExecute.getFilterBitmap(bmp, imageFilter);
        bitmapPadExecute.release();
        return filterBitmap;
    }

    /**
     * 设置美颜滤镜
     * @param beautyFilter
     */
    public void setBeautyFilter(LanSongBeautyFilter beautyFilter) {
        this.beautyFilter = beautyFilter;
    }

    /**
     * 设置普通滤镜
     * @param imageFilter
     */
    public void setImageFilter(GPUImageFilter imageFilter) {
        this.imageFilter = imageFilter;
    }

    public void setOnProcessListener(OnProcessListener onProcessListener) {
        this.onProcessListener = onProcessListener;
    }

    public interface OnProcessListener {
        void onSucess(String exportedFilePath);
        void onFail();
    }
}
