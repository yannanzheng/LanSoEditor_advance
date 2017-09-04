package com.example.advanceDemo.view;

import java.util.ArrayList;

import com.lansosdk.box.BitmapLoader;

import android.content.Context;
import android.graphics.Bitmap;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageLookupFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBeautyAdvanceFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongPinkFilter;


/**
 *  此代码仅仅作为美颜的参考使用,您完全可以自定义美白滤镜的组合.
 *  我们这里是举例 把磨皮+美白+粉色 三种滤镜做叠加,从而形成美颜的效果.
 *  
 */
public class BeautylLevel {
    
    /**
     * 可作为参考的美颜等级划分.实际您可任意配置.
     * 
     * 这里仅仅是举例:划分为5级, 第一级只有磨皮; 2--4级有美白, 第五级加上一个粉色;
     * filter的列表, 是先add进去,最新渲染, 把第一个渲染的结果传递给第二个,第二个传递给第三个,以此类推.
     * 
     * @param ctx
     * @param level
     * @return
     */
    public static ArrayList<GPUImageFilter> getFilters(Context ctx,int level)
    {
    	ArrayList<GPUImageFilter>  filters=new ArrayList<GPUImageFilter>();
    	//磨皮		
    		LanSongBeautyAdvanceFilter beautyFilter=new LanSongBeautyAdvanceFilter();
		 	if(level>=1 && level<=5){
	  			beautyFilter.setLevel(level);
	  		}else{
	  			beautyFilter.setLevel(5);
	  		}
		 
		//美白 	
		 	GPUImageLookupFilter  whiteFilter = new GPUImageLookupFilter();
		 	String var3 = "assets://LSResource/a0_whiten.png";
		 	Bitmap bmp=BitmapLoader.load(ctx, var3, 0, 0);
		 	whiteFilter.setBitmap(bmp);
	   
		 	//加一些粉色
		 	LanSongPinkFilter  pinkFilter=new LanSongPinkFilter(ctx);
	   
	   
    	if(level==1)
    	{
    		filters.add(beautyFilter);
    	}
    	else if(level>=2 && level<=4)
    	{
    		filters.add(beautyFilter);
    		filters.add(whiteFilter);
    	}
    	else  //如果是其他,则全部打开.
    	{
    		filters.add(beautyFilter);
    		filters.add(whiteFilter);
    		filters.add(pinkFilter);
    	}
    	return filters;
    }
}
