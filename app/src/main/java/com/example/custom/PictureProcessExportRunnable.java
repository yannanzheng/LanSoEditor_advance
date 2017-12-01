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
    private File sourceFile = null;
    private LanSongBeautyFilter beautyFilter;
    private GPUImageFilter imageFilter;
    private File destFile;

    private OnProcessListener onProcessListener;

    public PictureProcessExportRunnable(Context context, File sourceFile, File destFile) {
        this.context = context;
        this.sourceFile = sourceFile;
        this.destFile = destFile;
    }

    /**
     * @param context 上下文
     * @param sourceFile  待处理的bitmap
     * @param beautyFilter 美颜滤镜
     * @param imageFilter 滤镜
     * @param destFile 处理后输出位置
     */
    public PictureProcessExportRunnable(Context context, File sourceFile, File destFile, LanSongBeautyFilter beautyFilter, GPUImageFilter imageFilter) {
        this.context = context;
        this.sourceFile = sourceFile;
        this.beautyFilter = beautyFilter;
        this.imageFilter = imageFilter;
        this.destFile = destFile;
    }

    @Override
    public void run() {
        Bitmap filterBitmap = processPicture(sourceFile);
        try {
            exportPicture(filterBitmap);
            if (null != onProcessListener) {
                onProcessListener.onSucess();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (null != onProcessListener) {
                onProcessListener.onFail();
            }

        }
    }

    private void exportPicture(Bitmap filterBitmap) throws FileNotFoundException {
        filterBitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(destFile));
        ActivityUtils.notifyMediaScannerScanFile(context, destFile);
    }

    private Bitmap processPicture(File sourceFile) {
        Bitmap bitmap = BitmapFactory.decodeFile(String.valueOf(sourceFile));
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
        void onSucess();
        void onFail();
    }
}
