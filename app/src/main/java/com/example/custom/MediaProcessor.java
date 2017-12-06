package com.example.custom;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.custom.dependency.MediaEditType;
import com.example.custom.dependency.Resource;
import com.example.custom.dependency.ResourceType;

import java.io.File;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by jfyang on 12/2/17.
 */

public class MediaProcessor {
    private final static String TAG = "MediaProcessor";
    private Context mContext;
    private Queue<Resource> resourceQueue;

    private Handler processThreadHandler;
    private String destDirPath;

    private OnProcessListener onProcessListener;

    public MediaProcessor(Context context, Resource[] resources, String destDirPath) {
        mContext = context;
        this.destDirPath = destDirPath;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                processThreadHandler = new Handler();
                Looper.loop();
            }
        }).start();

        resourceQueue = new ArrayBlockingQueue<Resource>(resources.length);
        resourceQueue.addAll(Arrays.asList(resources));
    }

    public void start() {
       notifyPost();
    }
    private volatile boolean isProcessing = false;

    private void notifyPost(){
        if (isProcessing) {
            return;
        }
        if (resourceQueue.size() == 0) {
            success();
            return;
        }

        isProcessing = true;
        Resource pollResource = resourceQueue.poll();
        float beautyLevel = pollResource.getBeautyLevel();
        int filterType = pollResource.getFilterType();

        if (MediaEditType.FaceBeauty.LEVEL_0 == beautyLevel && MediaEditType.Filter.Filter_NULL == filterType) {
            copy(pollResource);
        }else {
            //需要编辑，滤镜美颜后导出
            if (ResourceType.Picture==pollResource.getResourceType()) {//图片
                edittedExportPicture(pollResource);
            }else {
                edittedExportVideo(pollResource);
            }
        }
    }

    private void copy(Resource pollResource) {
        //不需要编辑，直接复制导出
        File file = new File(destDirPath, pollResource.getName());
        FileCopyRunnable fileCopyRunnable = new FileCopyRunnable(mContext, pollResource.getDownloadUrl(), file.getAbsolutePath());
        fileCopyRunnable.setOnProcessListener(new FileCopyRunnable.OnProcessListener() {
            @Override
            public void onProgress(double progress) {

            }

            @Override
            public void onSuccess() {
                Log.d(TAG, "copy, onSuccess" );
                notifyPost();
            }

            @Override
            public void onFail() {
                Log.d(TAG, "copy, onFail" );
                fail();
            }
        });

        processThreadHandler.post(fileCopyRunnable);
    }

    private void edittedExportPicture(Resource pollResource) {
        File file = new File(destDirPath, pollResource.getName());

        PictureProcessExportRunnable pictureProcessExportRunnable =
                new PictureProcessExportRunnable(mContext
                        , pollResource.getDownloadUrl()
                        , file.getAbsolutePath()
                        , pollResource.getBeautyLevel()
                        , pollResource.getFilterType());
        pictureProcessExportRunnable.setOnProcessListener(new PictureProcessExportRunnable.OnProcessListener() {
            @Override
            public void onSuccess(String exportedFilePath) {
                Log.d(TAG, "edittedExportPicture, onSuccess, exportedFilePath = " + exportedFilePath);
                isProcessing = false;
                notifyPost();
            }

            @Override
            public void onFail() {
                Log.d(TAG, "edittedExportPicture, onfail" );
                fail();

            }
        });

        processThreadHandler.post(pictureProcessExportRunnable);
    }

    private void edittedExportVideo(Resource pollResource) {//视频
        File destFile = new File(destDirPath, pollResource.getName());
        VideoProcessExportRunnable videoProcessExportRunnable = new VideoProcessExportRunnable(
                mContext
                , pollResource.getDownloadUrl()
                , destFile.getAbsolutePath()
                , pollResource.getBeautyLevel()
                , pollResource.getFilterType());

        videoProcessExportRunnable.setOnProcessListener(new VideoProcessExportRunnable.OnProcessListener() {
            @Override
            public void onSuccess(String exportedFilePath) {
                Log.d("inner_share", "video process, onSuccess");
                isProcessing = false;
                notifyPost();
            }

            @Override
            public void onProgress(double progress) {
            }

            @Override
            public void onFail() {
                fail();
            }
        });

        processThreadHandler.post(videoProcessExportRunnable);
    }

    private void success(){
        if (null != onProcessListener) {
            onProcessListener.onSuccess();
        }
    }

    private void fail(){
        if (null != onProcessListener) {
            onProcessListener.onFail();
        }
    }

    public void setOnProcessListener(OnProcessListener onProcessListener) {
        this.onProcessListener = onProcessListener;
    }

    public interface OnProcessListener{
        void onSuccess();
        void onFail();
    }
}
