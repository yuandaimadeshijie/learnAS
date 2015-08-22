package com.yonyou.sns.im.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.entity.album.PhotoAlbum;
import com.yonyou.sns.im.log.YMLogger;

public class PhotoAibumAdapter extends BaseAdapter {

	/** 相册列表*/
	private List<PhotoAlbum> aibumList;
	/** context*/
	private Context context;
	/** viewHolder*/
	private ViewHolder holder;

	public PhotoAibumAdapter(List<PhotoAlbum> list, Context context) {
		this.aibumList = list;
		this.context = context;
	}

	@Override
	public int getCount() {
		return aibumList.size();
	}

	@Override
	public Object getItem(int position) {
		return aibumList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// 每次重新创建一个新对象，不沿用前面的历史视图，防止界面闪的情况
		convertView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.view_album_list_item, null);
		holder = new ViewHolder();
		holder.albumImage = (ImageView) convertView.findViewById(R.id.album_listitem_image);
		holder.albumText = (TextView) convertView.findViewById(R.id.album_listitem_name);
		holder.selectedImage = (ImageView) convertView.findViewById(R.id.album_listitem_cur);

		convertView.setTag(holder);

		YMLogger.d("position : " + position);

		/** 通过ID 获取缩略图*/
		if (aibumList.get(position).isTotal()) {
			holder.albumImage.setImageResource(aibumList.get(position).getBitmap());
		} else {
			Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(),
					aibumList.get(position).getBitmap(), Thumbnails.MICRO_KIND, null);

			if (bitmap == null) {
				holder.albumImage.setImageResource(R.drawable.icon_default_user);
			} else {
				holder.albumImage.setImageBitmap(bitmap);
			}
		}

		holder.albumText.setText(aibumList.get(position).getName() + " ( " + aibumList.get(position).count() + " )");
		holder.selectedImage.setVisibility(aibumList.get(position).isCurrentChoice() ? View.VISIBLE : View.GONE);

		return convertView;
	}

	static class ViewHolder {

		ImageView albumImage;
		TextView albumText;
		ImageView selectedImage;
	}

}
