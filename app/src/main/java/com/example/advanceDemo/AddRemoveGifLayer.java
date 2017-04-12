package com.example.advanceDemo;

import android.util.Log;

import com.example.advanceDemo.view.DrawPadView;
import com.lansosdk.box.GifLayer;

public class AddRemoveGifLayer {

	private boolean isAdd=false;
	private GifLayer layer;
	private DrawPadView  mDrawPad;
	public AddRemoveGifLayer(DrawPadView pad,String filePath){
		
		if(isAdd==false)
		{
			mDrawPad=pad;
			 layer= mDrawPad.addGifLayer(filePath);
			 int layerHeight = layer.getLayerHeight();
	            int padHeight = layer.getPadHeight();
	            int layerWidth = layer.getLayerWidth();
	            int padWidth = layer.getPadWidth();
	            Log.i("BBB", "replaceTheGIFLayer: layerWidth="+layerWidth+" padWidth="+padWidth);
	            Log.i("BBB", "replaceTheGIFLayer: layerHeight="+layerHeight+" +padHeight="+padHeight);
	            if(layerHeight!=padHeight){
	                float v = padHeight/ (float) layerHeight;
	                layer.setScale(v);
	            }
	            
			 isAdd=true;
		}
		 
	}
	public AddRemoveGifLayer(DrawPadView pad,int resId)
	{
		if(isAdd==false)
		{
			mDrawPad=pad;
			 layer= mDrawPad.addGifLayer(resId);
			 isAdd=true;
		}
	}
	public void removeGifLayer()
	{
		if(isAdd && mDrawPad!=null && mDrawPad.isRunning())
		{
			mDrawPad.removeLayer(layer);
			 isAdd=false;
		}
	}
}
