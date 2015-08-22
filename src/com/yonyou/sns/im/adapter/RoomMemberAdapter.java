package com.yonyou.sns.im.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.VCardActivity;
import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.entity.YMRoomMember;
import com.yonyou.sns.im.util.XMPPHelper;
import com.yonyou.sns.im.util.bitmap.BitmapCacheManager;

public class RoomMemberAdapter extends UserAdapter {

	/** Context */
	private Context context;
	/** LayoutInflater */
	private LayoutInflater inflater;
	/** room jid */
	private String room_jid;
	/** 成员列表 */
	private List<YMRoomMember> memberList = new ArrayList<YMRoomMember>();
	/** 图片缓存管理器 */
	private BitmapCacheManager avatarBitmapCacheManager;

	public RoomMemberAdapter(Context context, String room_jid) {
		this(context, room_jid, false);
	}

	public RoomMemberAdapter(Context context, String room_jid, boolean isSelect) {
		super(isSelect);
		this.context = context;

		this.room_jid = room_jid;

		this.inflater = LayoutInflater.from(context);

		// 这个是URL头像的缓存任务
		avatarBitmapCacheManager = new BitmapCacheManager(context, true, BitmapCacheManager.CIRCLE_BITMAP,
				BitmapCacheManager.BITMAP_DPSIZE_40);
		avatarBitmapCacheManager.generateBitmapCacheWork();
	}

	/**
	 * 重新查询好友列表数据
	 */
	public void requery() {
		// 查询成员
		memberList = YYIMChatManager.getInstance().getRoomMemberByJid(room_jid);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return memberList.size();
	}

	@Override
	public YMRoomMember getItem(int position) {
		return memberList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	static class ViewHolder {

		TextView letterView;
		ImageView iconView;
		TextView nameText;
		CheckBox checkBox;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// 当前数据
		final YMRoomMember member = memberList.get(position);

		ViewHolder viewHolder = null;
		if (convertView == null) {
			// 创建新的holder
			viewHolder = new ViewHolder();
			// inflate view
			convertView = inflater.inflate(R.layout.create_chat_room_recent_roster_list_item, parent, false);
			// init holder
			viewHolder.iconView = (ImageView) convertView.findViewById(R.id.roommember_icon);
			viewHolder.nameText = (TextView) convertView.findViewById(R.id.roommember_name);
			viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.roommember_checkbox);
			// set tag
			convertView.setTag(R.string.HOLDER_TAG_VIEW_HOLDER, viewHolder);
		} else {
			// get from tag
			viewHolder = (ViewHolder) convertView.getTag(R.string.HOLDER_TAG_VIEW_HOLDER);
		}

		// 名称
		viewHolder.nameText.setText(member.getName());
		// 头像
		avatarBitmapCacheManager.loadFormCache(XMPPHelper.getFullFilePath(member.getPhoto()), viewHolder.iconView);

		// 头像点击事件
		viewHolder.iconView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 跳转到VCard页面
				Intent intent = new Intent(context, VCardActivity.class);
				intent.putExtra(VCardActivity.EXTRA_JID, member.getUserJid());
				context.startActivity(intent);
			}
		});

		// checkbox
		if (isSelect()) {
			if (isAlreadyExistsMembers(member.getUserJid())) {
				viewHolder.checkBox.setEnabled(false);
				viewHolder.checkBox.setChecked(true);
			} else {
				viewHolder.checkBox.setChecked(isUserSelected(member.getUserJid()));
				// 绑定事件
				viewHolder.checkBox.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						boolean isChecked = ((CheckBox) v).isChecked();
						// 设置选中状态
						selectChange(member, isChecked);
					}

				});
			}
			viewHolder.checkBox.setVisibility(View.VISIBLE);

		} else {
			viewHolder.checkBox.setVisibility(View.GONE);
		}
		return convertView;
	}

}
