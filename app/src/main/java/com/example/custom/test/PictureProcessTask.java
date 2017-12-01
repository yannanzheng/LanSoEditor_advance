package com.example.custom.test;

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

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBeautyFilter;

/**
 * Created by jfyang on 11/30/17.
 */

public class PictureProcessTask implements Runnable {
    private static String TAG = "simulate_task";
    private Context context;
    private int postCount = 0;
    private static volatile int j = 0;
    private Bitmap bitmap = null;

    public PictureProcessTask(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        final File cacheDir = new File(Environment.getExternalStorageDirectory(), "abcLansonTest");
        Log.d(TAG, "cacheDir = " + cacheDir.getAbsolutePath());
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        bitmap = null;

        InputStream open = null;
        try {
            open = context.getAssets().open("test1.jpg");
            bitmap = BitmapFactory.decodeStream(open);

            BitmapPadExecute bitmapPadExecute;
            bitmapPadExecute = new BitmapPadExecute(context);
            LanSongBeautyFilter lanSongBeautyFilter = new LanSongBeautyFilter();
            lanSongBeautyFilter.setBeautyLevel(0.8f);

            GPUImageSepiaFilter gpuImageSepiaFilter = new GPUImageSepiaFilter();


            if (bitmapPadExecute.init(bitmap.getWidth(), bitmap.getHeight())) {
                Bitmap bmp = bitmapPadExecute.getFilterBitmap(bitmap, lanSongBeautyFilter);
                Bitmap filterBitmap = bitmapPadExecute.getFilterBitmap(bmp, gpuImageSepiaFilter);

                File file = new File(cacheDir, "img" + "_" + j + ".jpg");
                filterBitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
                ActivityUtils.notifyMediaScannerScanFile(context, file);
                j++;
                if (null != onProcessListener) {
                    onProcessListener.onSucess();
                }
            }
            bitmapPadExecute.release();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private OnProcessListener onProcessListener;

    public void setOnProcessListener(OnProcessListener onProcessListener) {
        this.onProcessListener = onProcessListener;
    }

    public interface OnProcessListener {
        void onSucess();
    }
}
