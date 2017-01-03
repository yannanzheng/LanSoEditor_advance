package com.lansosdk.videoeditor;


import android.util.Log;


public class AVEncoder {

	private static final String TAG ="AVEncoder";
	
	private long  mHandler=0;
	
	private int dataWidth,dataHeight;

	/**
	 * 
	 * @param fileName 
	 * @param encW   用户设置的宽度
	 * @param encH  用户设置的高度
	 * @param vfps
	 * @param bitrate
	 * @param asampleRate
	 * @param abitrate
	 * @return
	 */
	public boolean init(String fileName,int degree,int encW,int encH,int vfps,int bitrate,int asampleRate,int abitrate)  //一定是ts的后缀
	{
		mHandler=encoderInit(fileName,encW,encH, vfps, bitrate, asampleRate, abitrate);
		
		if(degree==90 || degree==270){  //竖屏,用户设置的是宽高对调了,但图像没有对调,故这里还原到图像没有对调的宽高.
			dataWidth=encH;
			dataHeight=encW;
		}else{
			dataWidth=encW;
			dataHeight=encH;
		}
		return mHandler!=0 ? true: false;
	}
	public boolean init(String fileName,int sameleRate,int bitrate)  //音频编解码.
	{
		mHandler=encoderInit(fileName,0, 0, 0, 0, sameleRate, bitrate);
		return mHandler!=0 ? true: false;
	}
	/**
	 * 
	 * 这个方法只能使用与摄像头, 不能作为其他使用.
	 * 
	 * 视频编码, 这里是直接把摄像头Camera的预览画面传递过来， 没有进行裁剪。
	 * @param data
	 * @param previewW  //视频预览的宽度
	 * @param previewH  //视频预览的高度
	 * @param degree
	 * @param ptsMS   当前时间和开始时间的差值.
	 */
	public void pushVideoData(byte[] data,int previewW,int previewH,int degree,long ptsMS)
	{
		if(mHandler!=0){
			
			
			//暂时放在这里:
			byte[]  bb=null;
			if( (previewW!=dataWidth || previewH!=dataHeight) && previewW>=dataWidth && previewH>=dataHeight){
					bb=frameCut(data,previewW,previewH,dataWidth,dataHeight);
			}else{
				bb=data;
			}
			
			
			byte[] byteArray=null;
			
			if(	degree==90)  //不是后置,就是前置.
			{
				byteArray=rotateYUV420Degree90(bb,dataWidth,dataHeight);  //应该在这里实时的检测当前是后置还是前置,后置,应旋转90, 前置270;
			}else{
				byteArray=rotateYUV420Degree270(bb,dataWidth,dataHeight);
			}
			encoderWriteVideoFrame(mHandler, byteArray, ptsMS); 
		}
	}

	
	public void pushAudioData(byte[] data,long ptsMs)
	{
		if(mHandler!=0){
			encoderWriteAudioFrame(mHandler, data, ptsMs); 
		}
	}
	
	public void release()
	{
		if(mHandler!=0){
			encoderRelease(mHandler);	
		}
		mHandler=0;
	}
	/**
	 * 只能对NV12或   NV21来做,可以用在宽度不等于高度的情况. 但这里宽高是 data中数据的真实宽高, 比如及时旋转90的视频, 宽高也是640x480或1280x720等等.
	 * @param data
	 * @param imageWidth
	 * @param imageHeight
	 * @return
	 */
	private byte[] rotateYUV420Degree180(byte[] data, int imageWidth, int imageHeight)
	{
		byte [] yuv = new byte[imageWidth*imageHeight*3/2];
		int i = 0;
		int count = 0;

		for (i = imageWidth * imageHeight - 1; i >= 0; i--) {
			yuv[count] = data[i];
			count++;
		}

		i = imageWidth * imageHeight * 3 / 2 - 1;
		for (i = imageWidth * imageHeight * 3 / 2 - 1; i >= imageWidth
				* imageHeight; i -= 2) {
			yuv[count++] = data[i - 1];
			yuv[count++] = data[i];
		}
		return yuv;
	}
	/**
	 * @param data
	 * @param imageWidth
	 * @param imageHeight
	 * @return
	 */
	private byte[] rotateYUV420Degree270(byte[] data, int imageWidth, int imageHeight)
	{
		final byte [] yuv = new byte[imageWidth*imageWidth*3/2];
		int wh = 0;
		int uvHeight = 0;
		if(imageWidth != 0 || imageHeight != 0)
		{
			wh = imageWidth * imageHeight;
			uvHeight = imageHeight >> 1;//uvHeight = height / 2
		}

		//旋转Y
		int k = 0;
		for(int i = 0; i < imageWidth; i++) {
			int nPos = 0;
			for(int j = 0; j < imageHeight; j++) {
				yuv[k] = data[nPos + i];
				k++;
				nPos += imageWidth;
			}
		}

		for(int i = 0; i < imageWidth; i+=2){
			int nPos = wh;
			for(int j = 0; j < uvHeight; j++) {
				yuv[k] = data[nPos + i];
				yuv[k + 1] = data[nPos + i + 1];
				k += 2;
				nPos += imageWidth;
			}
		}
		return rotateYUV420Degree180(yuv,imageWidth,imageHeight);
	}
	
	private byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) 
	{
	    byte [] yuv = new byte[imageWidth*imageHeight*3/2];
	    // Rotate the Y luma
	    int i = 0;
	    for(int x = 0;x < imageWidth;x++)
	    {
	        for(int y = imageHeight-1;y >= 0;y--)                               
	        {
	            yuv[i] = data[y*imageWidth+x];
	            i++;
	        }
	    }
	    // Rotate the U and V color components 
	    i = imageWidth*imageHeight*3/2-1;
	    for(int x = imageWidth-1;x > 0;x=x-2)
	    {
	        for(int y = 0;y < imageHeight/2;y++)                                
	        {
	            yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+x];
	            i--;
	            yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
	            i--;
	        }
	    }
	    return yuv;
	}
	/**
	 *
	 *  对视频是NV12 或NV21的格式 ,进行裁剪, 
	 *   
	 * @param bytes
	 * @param srcWidth
	 * @param srcHeight
	 * @param dstWidth
	 * @param dstHeight
	 * @return
	 */
	byte[] frameCut(byte[] bytes,int srcWidth,int srcHeight,int dstWidth,int dstHeight)
	{
		byte[] retBytes=new byte[dstWidth*dstHeight*3/2];
		
		int srcPos=0;
		int dstPos=0;
		//拷贝Y;
		for(int x = 0;x < dstHeight;x++)  //高度一致.
		{
			System.arraycopy(bytes, srcPos, retBytes, dstPos, dstWidth);
			srcPos+=srcWidth; // 开始下一行.
			dstPos+=dstWidth;
		}
		//copy UV
		for(int x = 0;x < dstHeight/2;x++)  //高度一致.
		{
			for(int i=0;i<dstWidth/2;i++){
				retBytes[dstPos]=bytes[srcPos];
				retBytes[dstPos+1]=bytes[srcPos+1];
				dstPos+=2;
				srcPos+=2;
			}
			srcPos+=srcWidth-dstWidth;
		}
		return retBytes;
	}
	
	
	protected native long encoderInit(String saveFile,int width,int height,int vfps,int vbitrate,int asamplerate,int abitrate);
	protected  native long encoderRelease(long handle);
	/**
	 * 
	 * @param handle
	 * @param yuv420sp  * NV12格式是:YYYY UV UV UV UV
	 * @param pts
	 * @return
	 */
	protected  native int  encoderWriteVideoFrame(long handle,byte[] yuv420sp,long pts);
	
	/**
	 * 
	 * 这里暂时没有返回长度, 实际注意, audiodata的长度要等于 采样点 *通道数 *2
	 * 
	 * @param handle
	 * @param audiodata  每个采样点占用两个字节,
	 * @param pts
	 * @return
	 */
	protected  native int  encoderWriteAudioFrame(long handle,byte[] audiodata,long pts);
	
}
