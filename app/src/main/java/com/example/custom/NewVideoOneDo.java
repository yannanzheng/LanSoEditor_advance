package com.example.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.CanvasLayer;
import com.lansosdk.box.CanvasRunnable;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.FileParameter;
import com.lansosdk.box.Layer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.videoeditor.DrawPadVideoExecute;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;

/**
 * 说明1:用来演示DrawPad, 同时处理裁剪,缩放,压缩,剪切,增加文字, 增加logo等信息.
 * 我们的DrawPad是一个容器, 内部可以放任意图层,并调节图层的各种移动等.
 * 这里仅仅是演示 视频图层+图片图层+Canvas图层的组合.
 * 您可以参考我们其他的各种例子,来实现您的具体需求.
 * 
 * 说明2: 如果你有除了我们列举的功能外, 还有做别的, 可以直接拷贝这个类, 然后删除没用的, 增加上你的图层, 来完成您的需求.
 * 
 * 说明3: 如果列举的功能,可以满足您的需求,则调用形式是这样的:
 *      场景1: 只裁剪+logo:
 *      则:
 *      videoOneDo=new VideoOneDo(getApplicationContext(), sourceFilePath);
		
		videoOneDo.setOnVideoOneDoProgressListener(进度监听);
		videoOneDo.setOnVideoOneDoCompletedListener(完成监听, 返回处理后的结果);
			videoOneDo.setCropRect(startX, startY, cropW, cropH);  //裁剪
			videoOneDo.setLogo(bmp, VideoOneDo.LOGO_POSITION_RIGHT_TOP); //加logo
		videoOneDo.start(); //开启另一个线程成功返回true, 失败返回false
		场景2: 增加背景音乐, 剪切时长+logo+文字等
		   则:
		     创建对象 ===>各种set===> 开始执行
		     
 */
public class NewVideoOneDo {

    private static final String TAG="VideoOneDo";
    public final static int LOGO_POSITION_LELF_TOP=0;
    public final static int LOGO_POSITION_LEFT_BOTTOM=1;
    public final static int LOGO_POSITION_RIGHT_TOP=2;
    public final static int LOGO_POSITION_RIGHT_BOTTOM=3;

    private String sourceFilePath;
    private String destFilePath;
    private MediaInfo   srcInfo;
    private String srcAudioPath; //从源视频中分离出的音频临时文件.
    private float tmpvDuration=0.0f;//drawpad处理后的视频时长.

    private String editTmpPath=null;

    private DrawPadVideoExecute mDrawPad=null;
    private boolean isExecuting=false;

    private Layer mainVideoLayer=null;
    private BitmapLayer  logoBmpLayer=null;
    private CanvasLayer  canvasLayer=null;
    
    private  Context context;
    
    //-------------------------------------------------
    private long startTimeUs=0;
    private long cutDurationUs=0;
    private FileParameter fileParamter=null;
    private int startX,startY,cropWidth,cropHeight;
    private GPUImageFilter  videoFilter=null;
    
    private Bitmap logoBitmap=null;
    private int logoPosition=LOGO_POSITION_RIGHT_TOP;
    private int scaleWidth,scaleHeight;
    private float  compressFactor=1.0f;

    private String textAdd=null;

    private String musicAACPath=null;
    private String musicMp3Path=null;
    private MediaInfo  musicInfo;
    private boolean isMixBgMusic; //是否要混合背景音乐.
    private float mixBgMusicVolume=0.8f;  //默认减少一点.
    private String dstAACPath=null; 
    
    public NewVideoOneDo(Context ctx, String sourceFilePath) {
        context=ctx;
        this.sourceFilePath = sourceFilePath;
    }

    public NewVideoOneDo(Context ctx, String sourceFilePath, String destFilePath) {
        this.context = ctx;
        this.sourceFilePath = sourceFilePath;
        this.destFilePath = destFilePath;
    }


    /**
     * 增加背景音乐.
     * 暂时只支持MP3和aac.
     * 如果背景音乐是MP3格式, 我们会转换为AAC格式.
     * 如果背景音乐时间 比视频短,则会循环播放.
     * 如果背景音乐时间 比视频长,则会从开始截取.
     * @param path
     */
    public void setBackGrondMusic(String path)
    {
    	musicInfo=new MediaInfo(path,false);
    	if(musicInfo.prepare() && musicInfo.isHaveAudio()){
    		if(musicInfo.aCodecName.equalsIgnoreCase("mp3")){
    			musicMp3Path=path;
    			musicAACPath=null;
    		}else if(musicInfo.aCodecName.equalsIgnoreCase("aac")){
    			musicAACPath=path;
    			musicMp3Path=null;
    		}else{
    			musicAACPath=null;
    			musicMp3Path=null;
    		}
    	}else{
    		musicMp3Path=null;
    		musicAACPath=null;
    		musicInfo=null;
    	}
    }
    /**
     * 背景音乐是否要和原视频中的声音混合, 即同时保留原音和背景音乐, 背景音乐通常音量略低一些.
     * 
     * @param path
     * @param isMix   是否增加,
     * @param volume 如增加,则背景音乐的音量调节 =1.0f为不变, 小于1.0降低; 大于1.0提高; 最大2.0;
     */
    public void setBackGrondMusic(String path, boolean isMix,float volume)
    {
    	musicInfo=new MediaInfo(path,false);
    	if(musicInfo.prepare() && musicInfo.isHaveAudio())
    	{
    		isMixBgMusic=isMix;
        	mixBgMusicVolume=volume;
        	
    		if(musicInfo.aCodecName.equalsIgnoreCase("mp3")){
    			musicMp3Path=path;
    			musicAACPath=null;
    		}else if(musicInfo.aCodecName.equalsIgnoreCase("aac")){
    			musicAACPath=path;
    			musicMp3Path=null;
    		}else{
    			musicAACPath=null;
    			musicMp3Path=null;
    		}
    	}else{
    		Log.e(TAG,"设置背景音乐出错, 音频文件有误.请查看"+musicInfo.toString());
    		musicMp3Path=null;
    		musicAACPath=null;
    		musicInfo=null;
    	}
    }
    /**
     * 缩放到的目标宽度和高度.
     * @param scaleW
     * @param scaleH
     */
    public void setScaleWidth(int scaleW,int scaleH){
    	if(scaleW>0 && scaleH>0){
    		 scaleWidth=scaleW;
    		 scaleHeight=scaleH;
    	}
    }
    /**
     * 设置压缩比, 此压缩比,在运行时, 会根据缩放后的比例,计算出缩放后的码率
     *  压缩比乘以 缩放后的码率, 等于实际的码率, 如果您缩放后, 建议不要再设置压缩
     * @param percent  压缩比, 值范围0.0f---1.0f;
     */
    public void setCompressPercent(float percent)
    {
    	if(percent>0.0f && percent<1.0f){
    		compressFactor=percent;
    	}
    }

    /**
     * 设置视频的开始位置,等于截取视频中的一段
     *  单位微秒, 如果你打算从2.3秒处开始处理,则这里的应该是2.3*1000*1000;
     *  支持精确截取.
     * @param timeUs
     */
    public  void setStartPostion(long timeUs){
        startTimeUs=timeUs;
    }

    /**
     *设置截取视频中的多长时间.
     * 单位微秒,
     * 支持精确截取.
     * @param timeUs
     */
    public  void setCutDuration(long timeUs)
    {
    	if(timeUs>0){
    		cutDurationUs=timeUs;
    	}
    }
    /**
     * 设置裁剪画面的一部分用来处理,
     *
     *  裁剪后, 如果设置了缩放,则会把cropW和cropH缩放到指定的缩放宽度.
     * @param startX  画面的开始横向坐标,
     * @param startY  画面的结束纵向坐标
     * @param cropW  裁剪多少宽度
     * @param cropH  裁剪多少高度
     */
    public void setCropRect(int startX,int startY,int cropW,int cropH){
        fileParamter=new FileParameter();
        this.startX=startX;
        this.startY=startY;
        cropWidth=cropW;
        cropHeight=cropH;
    }
    /** 
     * 这里仅仅是举例,用一个滤镜.如果你要增加多个滤镜,可以判断处理进度,来不断切换滤镜
     * @param filter
     */
    public void setFilter(GPUImageFilter filter){
        videoFilter=filter;
    }

    /**
     * 设置logo的位置, 这里仅仅是举例,您可以拷贝这个代码, 自行定制各种功能.
     * 原理:  增加一个图片图层到容器DrawPad中, 设置他的位置.
     * 位置这里举例是:
     * {@link #LOGO_POSITION_LEFT_BOTTOM}
     * {@link #LOGO_POSITION_LELF_TOP}
     * {@link #LOGO_POSITION_RIGHT_BOTTOM}
     * {@value #LOGO_POSITION_RIGHT_TOP}
     * 
     * @param bmp  logo图片对象
     * @param position  位置 
     */
    public void setLogo(Bitmap bmp, int position)
    {
        logoBitmap=bmp;
        if(position<=LOGO_POSITION_RIGHT_BOTTOM){
            logoPosition=position;
        }
    }

    /**
     * 增加文字, 这里仅仅是举例,
     * 原理: 增加一个CanvasLayer图层, 把文字绘制到Canvas图层上.
     * 文字的位置, 是Canvas绘制出来的.
     * @param text
     */
    public void setText(String text)
    {
        textAdd=text;
    }
    private OnVideoOneDoProgressListener monVideoOneDoProgressListener;
    public void setOnVideoOneDoProgressListener(OnVideoOneDoProgressListener li)
    {
    	monVideoOneDoProgressListener=li;
    }
    private OnVideoOneDoCompletedListener monVideoOneDOCompletedListener=null;
    public void setOnVideoOneDoCompletedListener(OnVideoOneDoCompletedListener li){
    	monVideoOneDOCompletedListener=li;
    }
    /**
     * 开始执行, 内部会开启一个线程去执行.
     * 开启成功,返回true. 失败返回false;
     * @return
     */
    public boolean start() {
        if (isExecuting) {
            return false;
        }

		srcInfo=new MediaInfo(sourceFilePath,false);
        if(srcInfo.prepare()==false) {
        	return false;
        }
        Log.d("feature_847", "srcInfo = " + srcInfo);

        if(startTimeUs>0 || cutDurationUs>0) {  //有剪切.
        	long du=(long)(srcInfo.vDuration*1000*1000);
        	long aDuration=(long)(srcInfo.aDuration*1000*1000);
        	if(aDuration>0){
        		 du=Math.min(du, aDuration);
        	}
        	if(startTimeUs>du){
       		 	startTimeUs=0;
       		 	Log.w(TAG,"开始时间无效,恢复为0...");
        	}
        	if(du<(startTimeUs+cutDurationUs)){  //如果总时间 小于要截取的时间,则截取时间默认等于总时间.
        		cutDurationUs=0;
        		Log.w(TAG,"剪切时长无效,恢复为0...");
        	}
        }
        
        if(srcInfo.isHaveAudio()){
        	VideoEditor editor=new VideoEditor();
        	srcAudioPath=SDKFileUtils.createAACFileInBox();
            Log.d("feature_847", "sourceFilePath ＝ " + sourceFilePath + ", srcAudioPath = " + srcAudioPath);

			editor.executeDeleteVideo(sourceFilePath, srcAudioPath,(float)startTimeUs/1000000f,(float)cutDurationUs/1000000f);//删除视频，应该就是提取音频了吧
        }else{
        	isMixBgMusic=false;//没有音频则不混合.
        }
        
        isExecuting=true;
        editTmpPath=SDKFileUtils.createMp4FileInBox();
        Log.d("feature_847", "editTmpPath = " + editTmpPath);
        
        tmpvDuration=srcInfo.vDuration;
        if(cutDurationUs>0 && cutDurationUs< (srcInfo.vDuration*1000000)){
        	tmpvDuration=(float)cutDurationUs/1000000f;
        }
        
        /**
         * 开启视频的DrawPad容器处理
         */
        if(startVideoThread()){
        	
        	/**
        	 * 视频开启成功, 开启音频处理
        	 */
        	if(musicMp3Path!=null|| musicAACPath!=null){
            	startAudioThread();
            }
        	return true;
        }else{
        	return false;
        }
    }
    private boolean startVideoThread() {
    	 //先判断有无裁剪画面
        if(cropHeight>0 && cropWidth>0) {
            fileParamter=new FileParameter();
            fileParamter.setDataSoure(sourceFilePath);
            	
            //如果裁剪的视频, 旋转了90度,则宽度高度对调.
        	if(srcInfo.vRotateAngle==90 || srcInfo.vRotateAngle==270){
        		int tmpx=startX;
        		startX=startY;
        		startY=srcInfo.vWidth-cropWidth;
        		
        		tmpx=cropWidth;
        		cropWidth=cropHeight;
        		cropHeight=tmpx;
            }
        	/**
        	 * 设置当前需要显示的区域 ,以左上角为0,0坐标. 
        	 * 
        	 * 这里暂时不做判断是否超出宽度或高度, 在videoLayer内部判断,因为有些宽高要调换.
        	 * 
        	 * @param startX  开始的X坐标, 即从宽度的什么位置开始
        	 * @param startY  开始的Y坐标, 即从高度的什么位置开始
        	 * @param cropW   需要显示的宽度
        	 * @param cropH   需要显示的高度.
        	 */
            fileParamter.setShowRect(startX,startY,cropWidth,cropHeight);
            fileParamter.setStartTimeUs(startTimeUs);
            
            int padWidth=cropWidth;
            int padHeight=cropHeight;
            if(scaleHeight>0 && scaleWidth>0) {
                padWidth=scaleWidth;
                padHeight=scaleHeight;
            }
            float f= (float)(padHeight*padWidth) /(float)(fileParamter.info.vWidth * fileParamter.info.vHeight);
            float bitrate= f *fileParamter.info.vBitRate *compressFactor*2.0f;
            mDrawPad = new DrawPadVideoExecute(context, fileParamter, padWidth, padHeight,(int)bitrate, videoFilter, editTmpPath);
        }else{ //没有裁剪
        	
            int padWidth=srcInfo.vWidth;
            int padHeight=srcInfo.vHeight;
            if(srcInfo.vRotateAngle==90 || srcInfo.vRotateAngle==270){
                padWidth=srcInfo.vHeight;
                padHeight=srcInfo.vWidth;
            }
            float bitrate= srcInfo.vBitRate*compressFactor*2.0f;
            
            if(scaleHeight>0 && scaleWidth>0) {
                padWidth=scaleWidth;
                padHeight=scaleHeight;
                float f= (float)(padHeight*padWidth) /(float)(srcInfo.vWidth * srcInfo.vHeight);
                bitrate *=f;
            }
            Log.d("feature_847", "sourceFilePath = " + sourceFilePath+", startTimeUs = "+startTimeUs+", padWidth = "+padWidth+", bitrate = "+bitrate+", videoFilter = "+videoFilter+", editTmpPath = "+editTmpPath);
            mDrawPad=new DrawPadVideoExecute(context, sourceFilePath,padWidth,padHeight,(int)bitrate,videoFilter,editTmpPath);
        }
        mDrawPad.setUseMainVideoPts(true);
        /**
         * 设置DrawPad处理的进度监听, 回传的currentTimeUs单位是微秒.
         */
        mDrawPad.setDrawPadProgressListener(new onDrawPadProgressListener() {
            @Override
            public void onProgress(DrawPad v, long currentTimeUs) {

            	if(monVideoOneDoProgressListener!=null){
            		float time=(float)currentTimeUs/1000000f;
            		
            		float percent=time/(float)tmpvDuration;
            		
            		float b   =  (float)(Math.round(percent*100))/100;  //保留两位小数.
            		if(b<1.0f && monVideoOneDoProgressListener!=null && isExecuting){
            			monVideoOneDoProgressListener.onProgress(NewVideoOneDo.this, b);
            		}
            	}
                if(cutDurationUs>0 && currentTimeUs>cutDurationUs){  //设置了结束时间, 如果当前时间戳大于结束时间,则停止容器.
                	mDrawPad.stopDrawPad();
                }
            }
        });
        /**
         * 设置DrawPad处理完成后的监听.
         */
        mDrawPad.setDrawPadCompletedListener(new onDrawPadCompletedListener() {

            @Override
            public void onCompleted(DrawPad v) {
                completeDrawPad();
            }
        });

        mDrawPad.pauseRecord();
        Log.d(TAG,"开始执行....startDrawPad");
        if(mDrawPad.startDrawPad()) {
            mainVideoLayer=mDrawPad.getMainVideoLayer();
            
            addBitmapLayer(); //增加图片图层
            
            addCanvasLayer(); //增加文字图层.
            mDrawPad.resumeRecord();  //开始恢复处理.
            
            return true;
        }else{
        	return false;
        }
    }
    /**
     * 处理完成后的动作.
     */
    private void completeDrawPad()
    {
    	 Log.d(TAG,"开始执行....drawPadCompleted");
    	joinAudioThread();
    	
    	if(isExecuting==false){
    		return ;
    	}
    	
		String dstPath=SDKFileUtils.createMp4FileInBox();
    	if(dstAACPath!=null && isExecuting)  //增加背景音乐.
    	{
    		videoMergeAudio(editTmpPath, dstAACPath,dstPath);
    	}else if(srcAudioPath!=null && isExecuting){  //增加原音.
    		videoMergeAudio(editTmpPath, srcAudioPath,dstPath); 
    	}else{
    		dstPath=editTmpPath;
    	}
    	
    	if(monVideoOneDOCompletedListener!=null && isExecuting){
    		monVideoOneDOCompletedListener.onCompleted(NewVideoOneDo.this,dstPath);
    	}
    	isExecuting=false;
    	Log.i(TAG,"最后的视频文件是:"+MediaInfo.checkFile(dstPath));
    }
    public void stop()
    {
    	if(isExecuting){
    		isExecuting=false;
    		  
    		monVideoOneDOCompletedListener=null;
    		monVideoOneDoProgressListener=null;
    		if(mDrawPad!=null){
    			mDrawPad.stopDrawPad();
    		}
    		joinAudioThread();
    		sourceFilePath =null;
    		srcInfo=null;
    		mDrawPad=null;
    	  
    	    logoBitmap=null;
    	    textAdd=null;
    	    dstAACPath=null; 
    	    musicMp3Path=null;
    	    musicInfo=null;
    	}
    }
    public void release()
    {
    	stop();
    }
    /**
     * 增加图片图层
     */
    private void addBitmapLayer()
    {
    	 //如果需要增加图片.
        if(logoBitmap!=null){
        	logoBmpLayer=mDrawPad.addBitmapLayer(logoBitmap);
        	if(logoBmpLayer!=null)
        	{
        		int w=logoBmpLayer.getLayerWidth();
        		int h=logoBmpLayer.getLayerHeight();
        		if(logoPosition==LOGO_POSITION_LELF_TOP){  //左上角.
        			
        			logoBmpLayer.setPosition(w/2, h/2);
            		
        		}else if(logoPosition==LOGO_POSITION_LEFT_BOTTOM){  //左下角
        			
        			logoBmpLayer.setPosition(w/2,logoBmpLayer.getPadHeight()- h/2);
        		}else if(logoPosition==LOGO_POSITION_RIGHT_TOP){  //右上角
        			
        			logoBmpLayer.setPosition(logoBmpLayer.getPadWidth()-w/2,h/2);
        			
        		}else if(logoPosition==LOGO_POSITION_RIGHT_BOTTOM){  //右下角
        			logoBmpLayer.setPosition(logoBmpLayer.getPadWidth()-w/2, logoBmpLayer.getPadHeight() - h/2);
        		}else{
        			Log.w(TAG,"logo默认居中显示");
        		}
        	}
        }
    }
    /**
     * 增加Android的Canvas类图层.
     */
    private void addCanvasLayer()
    {
    	if(textAdd!=null){
        	canvasLayer=mDrawPad.addCanvasLayer();
        	
        	canvasLayer.addCanvasRunnable(new CanvasRunnable() {
				
				@Override
				public void onDrawCanvas(CanvasLayer pen, Canvas canvas, long currentTimeUs) {
					Paint paint = new Paint();
	                paint.setColor(Color.RED);
         			paint.setAntiAlias(true);
         			paint.setTextSize(20);
         			canvas.drawText(textAdd,20,20, paint);
				}
			});
        }
    }
    private Thread audioThread=null;
    /**
     * 音频处理线程.
     */
    private void startAudioThread()
    {
    	if(audioThread==null) {
    		audioThread=new Thread(new Runnable() {
    			@Override
    			public void run() {
    				/**
    				 * 1, 如果mp3,  看是否要mix, 如果要,则长度拼接够, 然后mix;如果不mix,则先转码,再拼接.
    				 * 2, 如果是aac, 是否要mix, 要则拼接 再mix,; 不需要则直接拼接.
    				 */
    				if(musicMp3Path!=null){  //输入的是MP3;
    					  if(isMixBgMusic){  //混合.
    						  dstAACPath=SDKFileUtils.createAACFileInBox();
    						  
    						  String startMp3=getEnoughAudio(musicMp3Path, true);
    						  VideoEditor  editor=new VideoEditor();
    						  
    						  editor.executeAudioVolumeMix(srcAudioPath, startMp3, 1.0f, mixBgMusicVolume,tmpvDuration, dstAACPath);
    					  }else{//直接增加背景.
    						  
    						    VideoEditor editor=new VideoEditor();
    		    				float duration=(float)cutDurationUs/1000000f;
    		    				String tmpAAC=SDKFileUtils.createAACFileInBox();
    		    				editor.executeConvertMp3ToAAC(musicMp3Path, 0,duration, tmpAAC);
    		    				
    		    				dstAACPath=getEnoughAudio(tmpAAC, false);
    					  }
    				}else if(musicAACPath!=null){
    					 if(isMixBgMusic){  //混合.
    						 dstAACPath=SDKFileUtils.createAACFileInBox();
	   						  String startAAC=getEnoughAudio(musicAACPath, false);
	   						  VideoEditor  editor=new VideoEditor();
	   						  editor.executeAudioVolumeMix(srcAudioPath, startAAC, 1.0f, mixBgMusicVolume,tmpvDuration, dstAACPath);
    					 }else{
    						 dstAACPath=getEnoughAudio(musicAACPath, false);
    					 }
    				}
    				audioThread=null;
    			}
    		});
    		audioThread.start();
    	}
    }
    private void joinAudioThread()
    {
    	if(audioThread!=null){
    		try {
				audioThread.join(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				Log.w(TAG,"背景音乐转码失败....使用源音频");
				dstAACPath=null;
			}
    		audioThread=null;
    	}
    }
    /**
     * 得到拼接好的mp3或aac文件. 如果够长,则直接返回;
     * @param input
     * @param isMp3
     * @return
     */
    private String getEnoughAudio(String input, boolean isMp3)
    {
    	String audio=input;
		if(musicInfo.aDuration<tmpvDuration){  //如果小于则自行拼接.
			
			Log.d(TAG,"音频时长不够,开始转换.musicInfo.aDuration:"+musicInfo.aDuration+ " tmpvDuration:"+ tmpvDuration);
			
			 int num= (int)(tmpvDuration/musicInfo.aDuration +1.0f);
			 String[] array=new String[num];  
		     for(int i=0;i<num;i++){  
		    	 array[i]=input;  
		     } 
		     if(isMp3){
		    	 audio=SDKFileUtils.createMP3FileInBox();
		     }else{
		    	 audio=SDKFileUtils.createAACFileInBox();	 
		     }
			 concatAudio(array,audio);  //拼接好.
		}
		return audio;
    }
    /**
     * 拼接aac
     * @param tsArray
     * @param dstFile
     * @return
     */
    private int concatAudio(String[] tsArray,String dstFile)
	   {
		   if(SDKFileUtils.filesExist(tsArray)){
			    String concat="concat:";
			    for(int i=0;i<tsArray.length-1;i++){
			    	concat+=tsArray[i];
			    	concat+="|";
			    }
			    concat+=tsArray[tsArray.length-1];
			    	
				List<String> cmdList=new ArrayList<String>();
				
		    	cmdList.add("-i");
				cmdList.add(concat);

				cmdList.add("-c");
				cmdList.add("copy");
				
				cmdList.add("-y");
				
				cmdList.add(dstFile);
				String[] command=new String[cmdList.size()];  
			     for(int i=0;i<cmdList.size();i++){  
			    	 command[i]=(String)cmdList.get(i);  
			     }  
			     VideoEditor editor=new VideoEditor();
			    return  editor.executeVideoEditor(command);
		  }else{
			  return -1;
		  }
	   }
    /**
     * 之所有从VideoEditor.java中拿过来另外写, 是为了省去两次MediaInfo的时间;
     */
       private void videoMergeAudio(String videoFile,String audioFile,String dstFile)
	  {
		  		VideoEditor editor=new VideoEditor();
				List<String> cmdList=new ArrayList<String>();
				
		    	cmdList.add("-i");
				cmdList.add(videoFile);
				
				cmdList.add("-i");
				cmdList.add(audioFile);

				cmdList.add("-vcodec");
				cmdList.add("copy");
				cmdList.add("-acodec");
				cmdList.add("copy");
				
				
				cmdList.add("-absf");
				cmdList.add("aac_adtstoasc");
				
				cmdList.add("-y");
				cmdList.add(dstFile);
				String[] command=new String[cmdList.size()];  
			     for(int i=0;i<cmdList.size();i++){  
			    	 command[i]=(String)cmdList.get(i);  
			     }  
			    editor.executeVideoEditor(command);
	  }

    public interface OnVideoOneDoCompletedListener {

        void onCompleted(NewVideoOneDo v, String dstVideo);
    }

    public interface OnVideoOneDoProgressListener {

        /**
         * 进度百分比, 最小是0.0,最大是1.0;
         * 如果运行结束, 会回调{@link com.lansosdk.videoeditor.onVideoOneDoCompletedListener}, 只有调用Complete才是正式完成回调.
         * @param v
         * @param percent
         */
        void onProgress(NewVideoOneDo v,float percent);
    }

}
