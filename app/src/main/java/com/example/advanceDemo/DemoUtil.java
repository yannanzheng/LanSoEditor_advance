package com.example.advanceDemo;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.lansosdk.videoeditor.SDKFileUtils;

public class DemoUtil {

	public static String savePng(Bitmap bmp)
	 {
		 if(bmp!=null)
		 {
			  try {
					  BufferedOutputStream  bos;
					  String name=SDKFileUtils.createFileInBox("png");
					  Log.i("savePng","name:"+name);
					  bos = new BufferedOutputStream(new FileOutputStream(name));
					  bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
					  bos.close();
					  return name;
				} catch (IOException e) {
					e.printStackTrace();
				}
		 }
		 Log.e("savePng","error  bmp  is null");
		 return null;
	 }
	public static void showToast(Context ctx,String str){
		Toast.makeText(ctx, str, Toast.LENGTH_SHORT).show();
    	Log.i("x",str);
	}
}
