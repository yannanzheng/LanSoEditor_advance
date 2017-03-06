package com.lansosdk.videoeditor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;

public class CopyFileFromAssets {

	/**
	 * 
	 * @param mContext
	 * @param ASSETS_NAME
	 * @param savePath
	 * @param saveName
	 * @return  返回拷贝后的文件路径.
	 */
	public static String copy(Context mContext, String ASSETS_NAME,
			String savePath, String saveName) {
		String filePath = savePath + "/" + saveName;

		File dir = new File(savePath);
		// 如果目录不中存在，创建这个目录
		if (!dir.exists())
			dir.mkdir();
		try {
			if (!(new File(filePath)).exists()) {
				InputStream is = mContext.getResources().getAssets()
						.open(ASSETS_NAME);
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
	public void testCopy(Context context) {
		String path=context.getFilesDir().getAbsolutePath();
    	String name="test.txt";
    	CopyFileFromAssets.copy(context, name, path, name);
	}
}
