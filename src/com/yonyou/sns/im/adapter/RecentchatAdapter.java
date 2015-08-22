package com.yonyou.sns.im.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.VCardActivity;
import com.yonyou.sns.im.entity.message.YMMessage;
import com.yonyou.sns.im.entity.message.YMRecentChat;
import com.yonyou.sns.im.util.IMMessageUtil;
import com.yonyou.sns.im.util.XMPPHelper;
import com.yonyou.sns.im.util.bitmap.BitmapCacheManager;
import com.yonyou.sns.im.util.bitmap.MucIconCacheManager;
import com.yonyou.sns.im.util.common.TimeUtil;

/**
 * 最近消息的Adapter
 *
 * @author litfb
 * @date 2014年9月29日
 * @version 1.0
 */
public class RecentchatAdapter extends BaseAdapter {

	/** Context */
	private Context context;
	/** LayoutInflater */
	private LayoutInflater layoutInflater;
	/** 图片缓存 */
	private BitmapCacheManager bitmapCacheManager;
	/** 群组头像 */
	private MucIconCacheManager mucIconCacheManager;
	/** 最近消息list */
	private List<YMRecentChat> recentChatList;

	public RecentchatAdapter(Context context) {
		this.context = context;
		init();
	}

	private void init() {
		// layoutInflater
		this.layoutInflater = LayoutInflater.from(context);
		// 这个是URL头像的缓存任务
		bitmapCacheManager = new BitmapCacheManager(context, true, BitmapCacheManager.CIRCLE_BITMAP,
				BitmapCacheManager.BITMAP_DPSIZE_40);
		bitmapCacheManager.generateBitmapCacheWork();
		mucIconCacheManager = new MucIconCacheManager(context);
	}

	@Override
	public int getCount() {
		return recentChatList == null ? 0 : recentChatList.size();
	}

	@Override
	public Object getItem(int position) {
		return recentChatList != null && recentChatList.size() > position ? recentChatList.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = newView(parent);
		}
		bindView(convertView, position);
		return convertView;
	}

	public View newView(ViewGroup parent) {
		// 创建View
		View view = layoutInflater.inflate(R.layout.recent_listview_item, null);
		// 构建ViewHolder
		ViewHolder viewHolder = buildHolder(view);
		// set holder in tag
		view.setTag(R.string.viewholder, viewHolder);
		return view;
	}

	public void bindView(View paramView, int position) {
		// 获得ChatEntity
		final YMRecentChat chat = (YMRecentChat) getItem(position);
		if (chat == null) {
			return;
		}
		// check view holder
		ViewHolder viewHolder = getViewHolder(paramView);
		// 设图标
		if (YMMessage.TYPE_GROUPCHAT.equals(chat.getChat_type())) {// 群聊
			// 拼装图标
			mucIconCacheManager.loadFormCache(chat.getRoom_jid(), viewHolder.icon);
			viewHolder.msgView.setText(IMMessageUtil.genChatContent(this.context, chat.getChatContent()));
			viewHolder.timeView.setVisibility(View.VISIBLE);
		} else if (YMMessage.TYPE_SYSTEM.equals(chat.getChat_type())) {// 系统消息
			// 系统消息图标
			viewHolder.icon.setImageResource(R.drawable.icon_system_message);
			viewHolder.timeView.setVisibility(View.GONE);
			viewHolder.msgView.setText(chat.getMessage());
		} else {
			viewHolder.timeView.setVisibility(View.VISIBLE);
			// URL头像
			bitmapCacheManager.loadFormCache(XMPPHelper.getFullFilePath(chat.getOpposite_photo()), viewHolder.icon);
		}
		viewHolder.msgView.setText(IMMessageUtil.genChatContent(this.context, chat.getChatContent()));
		// 名称
		viewHolder.jidView.setText(chat.getOpposite_name());
		// 时间
		viewHolder.timeView.setText(TimeUtil.parseTimeSketchy(chat.getDate()));
		// 设置未读的数量
		viewHolder.unReadView.setText(String.valueOf(chat.getNewmsg_count()));
		// 当数量大于0就显示未读View
		viewHolder.unReadView.setVisibility(chat.getNewmsg_count() > 0 ? View.VISIBLE : View.GONE);
		// Change the view's z order in the tree, so it's on top of other sibling views
		viewHolder.unReadView.bringToFront();
		// 图标点击事件
		viewHolder.iconArea.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (YMMessage.TYPE_GROUPCHAT.equals(chat.getChat_type())) {// 群聊
					// 预留
				} else if (YMMessage.TYPE_SYSTEM.equals(chat.getChat_type())) {// 系统消息
					// 预留
				} else {
					// 跳转到VCard页面
					Intent intent = new Intent(context, VCardActivity.class);
					intent.putExtra(VCardActivity.EXTRA_JID, chat.getRoom_jid());
					context.startActivity(intent);
				}
			}
		});
	}

	/**
	 * 获得ViewHolder
	 * 
	 * @param view
	 * @return
	 */
	private ViewHolder getViewHolder(View view) {
		// get view holder from tag
		ViewHolder viewHolder = (ViewHolder) view.getTag(R.string.viewholder);
		if (viewHolder == null) {
			// build holder
			viewHolder = buildHolder(view);
			// set holder in tag
			view.setTag(R.string.viewholder, viewHolder);
		}
		return viewHolder;
	}

	/**
	 * 生成ViewHolder
	 * 
	 * @param convertView
	 * @return
	 */
	private ViewHolder buildHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.icon = (ImageView) convertView.findViewById(R.id.icon);
		holder.jidView = (TextView) convertView.findViewById(R.id.recent_list_item_name);
		holder.timeView = (TextView) convertView.findViewById(R.id.recent_list_item_time);
		holder.msgView = (TextView) convertView.findViewById(R.id.recent_list_item_msg);
		holder.unReadView = (TextView) convertView.findViewById(R.id.unreadmsg);
		holder.iconArea = convertView.findViewById(R.id.icon_area);
		return holder;
	}

	/**
	 * ViewHolder
	 *
	 * @author litfb
	 * @date 2014年9月29日
	 * @version 1.0
	 */
	private static class ViewHolder {

		/** 头像 */
		ImageView icon;
		/** 对方名称 */
		TextView jidView;
		/** 时间 */
		TextView timeView;
		/** 消息 */
		TextView msgView;
		/** 未读数量 */
		TextView unReadView;
		/** 响应头像点击的区域*/
		View iconArea;

	}

	/**
	 * @return the recentChatList
	 */
	public List<YMRecentChat> getRecentChatList() {
		return recentChatList;
	}

	/**
	 * @param recentChatList the recentChatList to set
	 */
	public void setRecentChatList(List<YMRecentChat> recentChatList) {
		this.recentChatList = recentChatList;
	}

}
