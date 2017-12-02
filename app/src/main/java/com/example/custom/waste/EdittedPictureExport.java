package com.example.custom.waste;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.example.custom.dependency.MediaEditType;
import com.lansosdk.box.BitmapGetFilters;
import com.lansosdk.box.onGetFiltersOutFrameListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IF1977Filter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFAmaroFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFBrannanFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFHudsonFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFInkwellFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFLomofiFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFNashvilleFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFRiseFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFSierraFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFToasterFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IFWaldenFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBeautyFilter;

/**
 * Created by jfyang on 11/28/17.
 */

public class EdittedPictureExport {
    public static String TAG = "EdittedPictureExport";

    private Context mContext;
    private String sourceFilePath;
    private String destFilePath;
    private volatile boolean isExecuting = false;

    private LanSongBeautyFilter beautyFilter;
    private GPUImageFilter filter = null;
    private Bitmap logoBitmap;

    public EdittedPictureExport(Context mContext, String sourceFilePath, String destFilePath) {
        this.mContext = mContext;
        this.sourceFilePath = sourceFilePath;
        this.destFilePath = destFilePath;
    }

    public EdittedPictureExport(Context mContext, String sourceFilePath) {
        this.mContext = mContext;
        this.sourceFilePath = sourceFilePath;
        this.destFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + "Camera" + File.separator + new File(sourceFilePath).getName();
    }

    /**
     * 设置滤镜
     *
     * @param filter
     */
    public void setFilter(GPUImageFilter filter) {
        this.filter = filter;
    }

    /**
     * @param filterType 滤镜类型
     */
    public void setFilter(int filterType) {
        switch (filterType) {
            case MediaEditType.Filter.Filter_NULL://0
                this.filter = null;
                break;
            case MediaEditType.Filter.Filter_IFHudson://1
                this.filter = new IFHudsonFilter(mContext);
                break;

            case MediaEditType.Filter.Filter_IFLomofi://2
                this.filter = new IFLomofiFilter(mContext);
                break;
            case MediaEditType.Filter.Filter_IFSierra://3
                this.filter = new IFSierraFilter(mContext);
                break;
            case MediaEditType.Filter.Filter_IFRise://4
                this.filter = new IFRiseFilter(mContext);
                break;
            case MediaEditType.Filter.Filter_IFAmaro://5
                this.filter = new IFAmaroFilter(mContext);
                break;
            case MediaEditType.Filter.Filter_IFWalden://6
                this.filter = new IFWaldenFilter(mContext);
                break;
            case MediaEditType.Filter.Filter_IFNashville://7
                this.filter = new IFNashvilleFilter(mContext);
                break;
            case MediaEditType.Filter.Filter_IFBrannan://8
                this.filter = new IFBrannanFilter(mContext);
                break;
            case MediaEditType.Filter.Filter_IFInkwell://9
                this.filter = new IFInkwellFilter(mContext);
                break;
            case MediaEditType.Filter.Filter_IFToaster://10
                this.filter = new IFToasterFilter(mContext);
                break;
            case MediaEditType.Filter.Filter_IF1977://11
                this.filter = new IF1977Filter(mContext);
                break;
            case MediaEditType.Filter.Filter_LanSongSepia://12
                this.filter = new GPUImageSepiaFilter();
                break;
        }
    }

    /**
     * 设置美颜滤镜
     *
     * @param beautyFilter 美颜滤镜
     */
    public void setFaceBeautyFilter(LanSongBeautyFilter beautyFilter) {
        this.beautyFilter = beautyFilter;
    }

    /**
     * @param beautyLevel 美颜等级
     */
    public void setFaceBeautyFilter(float beautyLevel) {
        this.beautyFilter = new LanSongBeautyFilter();
        beautyFilter.setBeautyLevel(beautyLevel);
    }

    public void setLogo(Bitmap bmp) {
        logoBitmap = bmp;
    }

    public void setLogo(int logoNumber) {
        logoBitmap = BitmapFactory.decodeResource(mContext.getResources()
                , MediaEditType.Logo.getResourceIdFromLogoNumber(logoNumber));
    }

    public void start() {
        Log.d("inner_share", "调用 EdittedPictureExport.start");
        if (isExecuting) {
            return;
        }
        isExecuting = true;
        if (null == filter && null == beautyFilter) {
            Log.e(TAG, "没有滤镜美颜，没必要使用这个，请直接copy");
            Log.d("inner_share", "没有滤镜美颜，没必要使用这个，导出失败 ");
            return;
        }

        ArrayList<GPUImageFilter> filterList = new ArrayList<GPUImageFilter>();
        if (null != beautyFilter) {
            filterList.add(beautyFilter);
        }

        if (null != filter) {
            filterList.add(filter);
        }

        Log.d("inner_share", "导出文件, sourceFilePath =  " + sourceFilePath);

        final Bitmap bitmap = BitmapFactory.decodeFile(sourceFilePath);
        final BitmapGetFilters bitmapGetFilters = new BitmapGetFilters(mContext, bitmap, filterList);

        bitmapGetFilters.setDrawpadOutFrameListener(new onGetFiltersOutFrameListener() {
            @Override
            public void onOutFrame(BitmapGetFilters v, Object obj) {
                Log.d("inner_share", "onOutFrame,  " );

                FileOutputStream fileOutputStream = null;
                try {
                    Bitmap bmp = (Bitmap) obj;
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(destFilePath));
                    bmp.recycle();
                    System.gc();
                    Log.d("inner_share", "图片压缩写入存储完成");
                    if (null != onExportListener) {
                        onExportListener.onSuccess();
                        release();
                        bitmapGetFilters.release();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    if (null != onExportListener) {
                        onExportListener.onFail();
                        release();
                        bitmapGetFilters.release();
                    }
                } finally {
                    if (null != fileOutputStream) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (null != bitmap) {
                        bitmap.recycle();
                        System.gc();
                    }
                }
            }
        });
        bitmapGetFilters.start();
    }

    private OnExportListener onExportListener;

    public void setOnExportListener(OnExportListener onExportListener) {
        this.onExportListener = onExportListener;
    }

    public void release() {
        stop();
    }

    public void stop() {
        mContext = null;
        sourceFilePath = null;
        destFilePath = null;
        isExecuting = false;
        beautyFilter = null;
        filter = null;
        if (null != logoBitmap) {
            logoBitmap.recycle();
            logoBitmap = null;
        }

    }

    public interface OnExportListener {
        void onSuccess();

        void onFail();
    }
}
