package com.example.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jfyang on 11/30/17.
 */

public class ProcessTask implements Runnable {
    private static String TAG = "simulate_task";
    private Context context;
    private int postCount = 0;

    public ProcessTask(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        int j = 1;
        File cacheDir = context.getCacheDir();
        Log.d(TAG, "cacheDir = " + cacheDir.getAbsolutePath());

        boolean flag = true;
        while (flag) {
            Bitmap bitmap = null;
            Bitmap bitmap2 = null;
            Bitmap bitmap3 = null;
            Bitmap bitmap4 = null;
            Bitmap bitmap5 = null;
//                    Bitmap bitmap6 = null;
//                    Bitmap bitmap7 = null;
            try {
                InputStream open = context.getAssets().open("test1.jpg");
                InputStream open2 = context.getAssets().open("test2.jpg");
                InputStream open3 = context.getAssets().open("test1.jpg");
                InputStream open4 = context.getAssets().open("test2.jpg");
                InputStream open5 = context.getAssets().open("test1.jpg");
//                        InputStream open6 = getAssets().open("test2.jpg");
//                        InputStream open7 = getAssets().open("test1.jpg");
                bitmap = BitmapFactory.decodeStream(open);
                bitmap2 = BitmapFactory.decodeStream(open2);
                bitmap3 = BitmapFactory.decodeStream(open3);
                bitmap4 = BitmapFactory.decodeStream(open4);
                bitmap5 = BitmapFactory.decodeStream(open5);
//                        bitmap5 = BitmapFactory.decodeStream(open6);
//                        bitmap5 = BitmapFactory.decodeStream(open7);

                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(new File(cacheDir,"img0_"+postCount+"_"+j)));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(new File(cacheDir,"img1_"+postCount+"_"+j)));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(new File(cacheDir,"img2_"+postCount+"_"+j)));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(new File(cacheDir,"img3_"+postCount+"_"+j)));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(new File(cacheDir,"img4_"+postCount+"_"+j)));

                Log.d(TAG, "bitmap, width = " + bitmap.getWidth() + ", height = " + bitmap.getHeight());
            } catch (IOException e) {
                e.printStackTrace();
            }

            flag = false;
            if (null != bitmap) {
                bitmap.recycle();
                bitmap = null;
            }

            if (null != bitmap2) {
                bitmap2.recycle();
                bitmap2 = null;
            }

            if (null != bitmap3) {
                bitmap3.recycle();
                bitmap3 = null;
            }

            if (null != bitmap4) {
                bitmap4.recycle();
                bitmap4 = null;
            }

            if (null != bitmap5) {
                bitmap5.recycle();
                bitmap5 = null;
            }
        }
        Log.d(TAG, "break");
        if (null != onProcessListener) {
            onProcessListener.onSucess();
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
