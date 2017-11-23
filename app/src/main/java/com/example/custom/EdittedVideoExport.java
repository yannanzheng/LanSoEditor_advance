package com.example.custom;

import android.content.Context;
import android.graphics.Bitmap;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageLaplacianFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBeautyFilter;

/**
 * Created by jfyang on 11/23/17.
 */

public class EdittedVideoExport {
    private GPUImageLaplacianFilter filter;
    private LanSongBeautyFilter faceBeautyFilter;

    public EdittedVideoExport(Context applicationContext, String sourceFilePath, String destFilePath) {

    }

    public void setFilter(GPUImageLaplacianFilter filter) {
        this.filter = filter;
    }

    public void setFaceBeautyFilter(LanSongBeautyFilter faceBeautyFilter) {
        this.faceBeautyFilter = faceBeautyFilter;
    }

    public void setLogo(Bitmap bmp, int logoPositionRightTop) {

    }

    public void start() {

    }
}
