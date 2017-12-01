package com.example.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
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
    private static int j = 0;

    public ProcessTask(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        final File cacheDir = new File(Environment.getExternalStorageDirectory(),"abcLansonTest");
        Log.d(TAG, "cacheDir = " + cacheDir.getAbsolutePath());
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = null;
                try {
                    InputStream open = context.getAssets().open("test1.jpg");
                    bitmap = BitmapFactory.decodeStream(open);

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(new File(cacheDir,"img0_"+postCount+"_"+j)));

                    Log.d(TAG, "bitmap, width = " + bitmap.getWidth() + ", height = " + bitmap.getHeight());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

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
