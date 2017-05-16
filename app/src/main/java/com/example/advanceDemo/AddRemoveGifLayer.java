package com.example.advanceDemo;

import android.util.Log;

import com.lansosdk.box.GifLayer;
import com.lansosdk.videoeditor.DrawPadCameraView;
import com.lansosdk.videoeditor.DrawPadView;

public class AddRemoveGifLayer {

	private boolean isAdd=false;
	private GifLayer layer;
	private DrawPadView  mDrawPad=null;
	private DrawPadCameraView  mCamDrawPad=null;
	public AddRemoveGifLayer(DrawPadView pad,String filePath){
		
		if(isAdd==false)
		{
			mDrawPad=pad;
			 layer= mDrawPad.addGifLayer(filePath);
			 int layerHeight = layer.getLayerHeight();
	            int padHeight = layer.getPadHeight();
	            if(layerHeight!=padHeight){
	                float v = padHeight/ (float) layerHeight;
	                layer.setScale(v);
	            }
			 isAdd=true;
		 }
	}
	public AddRemoveGifLayer(DrawPadCameraView pad,String filePath){
		
		if(isAdd==false)
		{
			mCamDrawPad=pad;
			 layer= mCamDrawPad.addGifLayer(filePath);
			 int layerHeight = layer.getLayerHeight();
	            int padHeight = layer.getPadHeight();
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
	public AddRemoveGifLayer(DrawPadCameraView pad,int resId)
	{
		if(isAdd==false)
		{
			mCamDrawPad=pad;
			 layer= mCamDrawPad.addGifLayer(resId);
			 isAdd=true;
		}
	}
	public void removeGifLayer()
	{
		if(isAdd)
		{
			if(mDrawPad!=null && mDrawPad.isRunning()){
				mDrawPad.removeLayer(layer);
				 isAdd=false;
			}else if(mCamDrawPad!=null && mCamDrawPad.isRunning()){
				mCamDrawPad.removeLayer(layer);
				 isAdd=false;
			}
		}
	}
}
