package com.yonyou.sns.im.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.entity.YMRoom;
import com.yonyou.sns.im.util.bitmap.MucIconCacheManager;

public class ChatRoomAdapter extends UserAdapter {

	/** Context */
	private Context context;
	/** LayoutInflater */
	private LayoutInflater inflater;
	/** 存放当前adapter中分组的集合 */
	private List<YMRoom> roomList = new ArrayList<>();
	/** 图片缓存管理器 */
	private MucIconCacheManager iconCacheManager;

	public ChatRoomAdapter(Context context) {
		super(false);
		this.context = context;

		this.inflater = LayoutInflater.from(this.context);

		iconCacheManager = new MucIconCacheManager(this.context);
		iconCacheManager.generateBitmapCacheWork();
	}

	public void requery() {
		roomList = YYIMChatManager.getInstance().getChatRooms();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return roomList.size();
	}

	@Override
	public YMRoom getItem(int position) {
		return roomList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	static class ViewHolder {

		ImageView iconView;
		TextView nameText;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final YMRoom room = roomList.get(position);
		ViewHolder viewHolder = null;
		if (convertView == null) {
			// 创建新的holder
			viewHolder = new ViewHolder();
			// inflate view
			convertView = inflater.inflate(R.layout.view_chatroom_list_item, parent, false);
			// init holder
			viewHolder.iconView = (ImageView) convertView.findViewById(R.id.chatroom_icon);
			viewHolder.nameText = (TextView) convertView.findViewById(R.id.chatroom_name);
			// set tag
			convertView.setTag(R.string.HOLDER_TAG_VIEW_HOLDER, viewHolder);
		} else {
			// get from tag
			viewHolder = (ViewHolder) convertView.getTag(R.string.HOLDER_TAG_VIEW_HOLDER);
		}

		// 名称
		viewHolder.nameText.setText(room.getRoom_name());
		// 设置房间icon
		iconCacheManager.loadFormCache(room.getRoom_jid(), viewHolder.iconView);
		return convertView;
	}

}
