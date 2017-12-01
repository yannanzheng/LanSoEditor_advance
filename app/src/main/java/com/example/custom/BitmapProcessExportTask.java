package com.example.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.example.custom.util.ActivityUtils;
import com.lansosdk.videoeditor.BitmapPadExecute;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBeautyFilter;

/**
 * 对Bitmap进行处理并导出到指定文件
 * Created by jfyang on 11/30/17.
 */

public class BitmapProcessExportTask implements Runnable {
    private static String TAG = "simulate_task";
    private Context context;
    private int postCount = 0;
    private static volatile int j = 0;
    private Bitmap sourceBitmap = null;
    private LanSongBeautyFilter beautyFilter;
    private GPUImageFilter imageFilter;
    private File destFile;

    private OnProcessListener onProcessListener;

    public BitmapProcessExportTask(Context context, Bitmap sourceBitmap, File destFile) {
        this.context = context;
        this.sourceBitmap = sourceBitmap;
        this.destFile = destFile;
    }

    public BitmapProcessExportTask(Context context, Bitmap sourceBitmap, LanSongBeautyFilter beautyFilter, GPUImageFilter imageFilter, File destFile) {
        this.context = context;
        this.sourceBitmap = sourceBitmap;
        this.beautyFilter = beautyFilter;
        this.imageFilter = imageFilter;
        this.destFile = destFile;
    }

    @Override
    public void run() {
        final File cacheDir = new File(Environment.getExternalStorageDirectory(), "abcLansonTest");
        Log.d(TAG, "cacheDir = " + cacheDir.getAbsolutePath());
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        sourceBitmap = null;

        try {
            if (null == sourceBitmap) {
                InputStream open = null;
                open = context.getAssets().open("test1.jpg");
                sourceBitmap = BitmapFactory.decodeStream(open);
            }

            BitmapPadExecute bitmapPadExecute;
            bitmapPadExecute = new BitmapPadExecute(context);

            if (beautyFilter == null) {
                beautyFilter = new LanSongBeautyFilter();
                beautyFilter.setBeautyLevel(0.8f);
            }

            if (null == imageFilter) {
                imageFilter = new GPUImageSepiaFilter();
            }

            boolean isExecuteInitSuccess = bitmapPadExecute.init(sourceBitmap.getWidth(), sourceBitmap.getHeight());
            if (!isExecuteInitSuccess) {
                return;
            }
            Bitmap bmp = bitmapPadExecute.getFilterBitmap(sourceBitmap, beautyFilter);
            Bitmap filterBitmap = bitmapPadExecute.getFilterBitmap(bmp, imageFilter);
            bitmapPadExecute.release();

            if (null == destFile) {
                destFile = new File(cacheDir, "img" + "_" + j + ".jpg");
                j++;
            }
            filterBitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(destFile));
            ActivityUtils.notifyMediaScannerScanFile(context, destFile);

            if (null != onProcessListener) {
                onProcessListener.onSucess();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setBeautyFilter(LanSongBeautyFilter beautyFilter) {
        this.beautyFilter = beautyFilter;
    }

    public void setImageFilter(GPUImageFilter imageFilter) {
        this.imageFilter = imageFilter;
    }

    public void setOnProcessListener(OnProcessListener onProcessListener) {
        this.onProcessListener = onProcessListener;
    }

    public interface OnProcessListener {
        void onSucess();
    }
}
