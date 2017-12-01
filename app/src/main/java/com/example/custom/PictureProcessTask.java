package com.example.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.lansosdk.box.BitmapGetFilters;
import com.lansosdk.box.onGetFiltersOutFrameListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBeautyFilter;

/**
 * Created by jfyang on 11/30/17.
 */

public class PictureProcessTask implements Runnable {
    private static String TAG = "simulate_task";
    private Context context;
    private int postCount = 0;
    private Bitmap bitmap;
    private static int j = 0;

    public PictureProcessTask(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        final File cacheDir = new File(Environment.getExternalStorageDirectory(),"abcLansonTest");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        Log.e(TAG, "cacheDir = " + cacheDir.getAbsolutePath());

        bitmap = null;
            try {
                Log.d(TAG, "try open test1.jpg");

                InputStream open = context.getAssets().open("test1.jpg");
                bitmap = BitmapFactory.decodeStream(open);
                Log.d(TAG, "try open test1.jpg, bitmap = "+ bitmap);

                ArrayList<GPUImageFilter> filterList = new ArrayList<GPUImageFilter>();
                LanSongBeautyFilter beautyFilter = new LanSongBeautyFilter();
                beautyFilter.setBeautyLevel(0.6f);
                GPUImageSepiaFilter gpuImageSepiaFilter = new GPUImageSepiaFilter();

                filterList.add(beautyFilter);
                filterList.add(gpuImageSepiaFilter);
                final BitmapGetFilters bitmapGetFilters = new BitmapGetFilters(context, bitmap, filterList);
                bitmapGetFilters.setDrawpadOutFrameListener(new onGetFiltersOutFrameListener() {
                    @Override
                    public void onOutFrame(BitmapGetFilters bitmapGetFilters, Object o) {
                        Bitmap bitmap1 = (Bitmap) o;
                        try {
                            Log.d(TAG, "onOutFrame ,输出图片前");
                            File file = new File(cacheDir, "img0_" + postCount + "_" + j + ".jpg");
                            bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
                            j++;
                            onProcessListener.onSucess();
                            bitmap1.recycle();
                            bitmap.recycle();
                            bitmapGetFilters.release();
                            Log.d(TAG, "onOutFrame， 输出图片后" + ", file.exists = " + file.exists());
                        } catch (FileNotFoundException e) {
                            Log.d(TAG, "onOutFrame ,FileNotFoundException, e = "+e.toString());
                            e.printStackTrace();
                        }

                    }
                });
                bitmapGetFilters.start();

                Log.d(TAG, "bitmap, width = " + bitmap.getWidth() + ", height = " + bitmap.getHeight());
            } catch (IOException e) {
                Log.d(TAG, "try open test1.jpg, IOException e = "+e.toString());

                e.printStackTrace();
            }

    }

    private OnProcessListener onProcessListener;

    public void setOnProcessListener(OnProcessListener onProcessListener) {
        this.onProcessListener = onProcessListener;
    }

    public interface OnProcessListener{
        void onSucess();
    }
}
