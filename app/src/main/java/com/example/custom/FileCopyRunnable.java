package com.example.custom;

import android.content.Context;
import android.util.Log;

import com.example.custom.util.ActivityUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by jfyang on 8/29/17.
 */

public class FileCopyRunnable implements Runnable{
    private static final String TAG = "FileCopierWithProgress";

    private volatile boolean cancel = false;
    private Context mContext;
    private String sourceFilePath;
    private String destFilePath;

    public FileCopyRunnable(Context context, String sourceFilePath, String destFilePath) {
        this.mContext = context;
        this.sourceFilePath = sourceFilePath;
        this.destFilePath = destFilePath;
    }

    private void copyFile(File srcFile, File destFile) {

        if (!destFile.getParentFile().exists()) {
            destFile.mkdirs();
        }
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        long fileSize = srcFile.length();
        long copiedSize = 0;
        double copiedPercent = 0.0;
        try {
            bis = new BufferedInputStream(new FileInputStream(srcFile));
            //抛异常了
            bos = new BufferedOutputStream(new FileOutputStream(destFile));
            byte[] buffer = new byte[(int) fileSize / 1000];
            int length = -1;
            while ((length = bis.read(buffer)) != -1) {
                if (cancel) {
                    destFile.delete();
                    if (onProcessListener != null) {
                        onProcessListener.onFail();
                    }
                    break;
                }
                bos.write(buffer, 0, length);
                bos.flush();
                copiedSize += length;
                if (fileSize != 0) {
                    copiedPercent = ((double) copiedSize) / fileSize;
                    Log.d(TAG, "copiedPercent = " + copiedPercent);
                    if (onProcessListener != null) {
                        onProcessListener.onProgress(copiedPercent);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != bis) {
                    bis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (null != bos) {
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (onProcessListener != null) {
                ActivityUtils.notifyMediaScannerScanFile(mContext, destFile);
                onProcessListener.onSuccess();
            }
        }
    }

    private OnProcessListener onProcessListener;

    public void setOnProcessListener(OnProcessListener onProcessListener) {
        this.onProcessListener = onProcessListener;
    }


    public void cancel() {
        cancel = true;
    }

    @Override
    public void run() {
        copyFile(new File(sourceFilePath),new File(destFilePath));
    }

    public interface OnProcessListener {
        void onProgress(double progress);

        void onSuccess();

        void onFail();
    }
}
