package com.example.advanceDemo;

import com.lansoeditor.demo.R;
import com.lansosdk.videoeditor.SDKFileUtils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class ShowOneBitmapActivity extends Activity{

	private ImageView ivImage;
	private String bmpPath;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.show_one_bitmap_layout);
		
		bmpPath = getIntent().getStringExtra("pngPath");
	     
	     
		ivImage=(ImageView)findViewById(R.id.id_show_onebitmap_iv);
		
		if(SDKFileUtils.fileExist(bmpPath)){
			Bitmap bmp=BitmapFactory.decodeFile(bmpPath);
			ivImage.setImageBitmap(bmp);
		}
	}
	
}
