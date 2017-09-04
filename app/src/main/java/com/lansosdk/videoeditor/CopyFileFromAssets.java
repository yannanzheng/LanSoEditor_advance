package com.lansosdk.videoeditor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.lansosdk.box.LanSoEditorBox;

import android.content.Context;

public class CopyFileFromAssets {
	/**
	 * 拷贝资源文件夹中的文件到默认地址.
	 * @param mContext
	 * @param assetsName
	 * @return  返回 拷贝文件的目标路径
	 */
	public static String copyAssets(Context mContext,String assetsName) {
		String filePath =  SDKDir.TMP_DIR  + "/" + assetsName;

		File dir = new File(SDKDir.TMP_DIR);
		// 如果目录不中存在，创建这个目录
		if (!dir.exists())
			dir.mkdirs();
		try {
			if (!(new File(filePath)).exists()) {
				InputStream is = mContext.getResources().getAssets()
						.open(assetsName);
				FileOutputStream fos = new FileOutputStream(filePath);
				byte[] buffer = new byte[7168];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
			}
			return filePath;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	public static String copyResId(Context mContext, int resId) {
	      String str2=  mContext.getResources().getString(resId);
	      String str3=str2.substring(str2.lastIndexOf("/")+1);
	      
		String filePath = LanSoEditorBox.getTempFileDir() + "/" + str3;

		try {
			if (!(new File(filePath)).exists()) {
				InputStream is = mContext.getResources().openRawResource(resId);
				FileOutputStream fos = new FileOutputStream(filePath);
				byte[] buffer = new byte[7168];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
			}
			return filePath;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
