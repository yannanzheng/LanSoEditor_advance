package com.example.advanceDemo.view;

import java.util.ArrayList;
import java.util.List;


import com.lansoeditor.demo.R;
import com.lansosdk.videoeditor.FilterList;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FilterDemoAdapter extends BaseAdapter{

	FilterList bmpList;
	public FilterDemoAdapter(Context con, FilterList bmpList){
//		mInflater=LayoutInflater.from(con);
		this.bmpList=bmpList;
		mInflater=(LayoutInflater)con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);//LayoutInflater.from(mContext);
	}
	@Override
	public int getCount() {
		if(bmpList!=null){
			return bmpList.getBitmapSize();
		}else{
			return 0;
		}
	}
	private LayoutInflater mInflater;
	@Override
	public Object getItem(int position) {
		return position;
	}
	private static class ViewHolder {
		private TextView tvName ;
		private ImageView ivImage;
	}
	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if(convertView==null){
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.filter_item, null);
			viewHolder.ivImage=(ImageView)convertView.findViewById(R.id.id_filter_item_iv);
			viewHolder.tvName=(TextView)convertView.findViewById(R.id.id_filter_item_tv);
			convertView.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder)convertView.getTag();
		}
		
		if(position<bmpList.getBitmapSize())
		{
			Bitmap bmp=bmpList.getBitmap(position);
			if(bmp!=null){
				viewHolder.ivImage.setImageBitmap(bmp);
			}
		}
		viewHolder.tvName.setText(bmpList.getName(position));
		return convertView;
	}
}