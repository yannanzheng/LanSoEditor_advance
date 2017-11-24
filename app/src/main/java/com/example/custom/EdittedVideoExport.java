package com.example.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageLaplacianFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBeautyFilter;

/**
 * Created by jfyang on 11/23/17.
 */

public class EdittedVideoExport {
    private Context mContext;
    private GPUImageFilter mFilter;
    private GPUImageFilter faceBeautyFilter;

    private String sourceFilePath;
    private String destFilePath;
    private Bitmap logoBitmap;

    public EdittedVideoExport(Context context, String sourceFilePath, String destFilePath) {
        this.mContext = context;
        this.sourceFilePath = sourceFilePath;
        this.destFilePath = destFilePath;
    }

    public void setFilter(GPUImageLaplacianFilter filter) {
        this.mFilter = filter;
    }

    public void setFaceBeautyFilter(LanSongBeautyFilter faceBeautyFilter) {
        this.faceBeautyFilter = faceBeautyFilter;
    }

    public void setLogo(Bitmap logoBitmap) {
        this.logoBitmap = logoBitmap;

    }

    public boolean start() {
        NewVideoOneDo newVideoOneDo=new NewVideoOneDo(mContext, sourceFilePath,sourceFilePath);
        newVideoOneDo.setOnVideoOneDoProgressListener(new NewVideoOneDo.OnVideoOneDoProgressListener() {
            @Override
            public void onProgress(NewVideoOneDo v, float percent) {
                Log.d("feature_847", "start，progress ＝ " + percent);
            }
        });

        newVideoOneDo.setOnVideoOneDoCompletedListener(new NewVideoOneDo.OnVideoOneDoCompletedListener() {

            @Override
            public void onCompleted(NewVideoOneDo v, String dstVideo) {
                Log.d("feature_847", "start，process completed ");
            }
        });

        newVideoOneDo.setFilter(mFilter);

        if(newVideoOneDo.start()){
            Log.d("feature_847", "start，videoInfo 开始执行 " );
        }else{
            Log.d("feature_847", "start，videoInfo 执行失败 " );
        }
        return  true;
    }
}
