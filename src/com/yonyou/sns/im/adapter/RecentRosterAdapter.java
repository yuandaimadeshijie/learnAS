package com.yonyou.sns.im.adapter;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.util.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.VCardActivity;
import com.yonyou.sns.im.core.YYIMRosterManager;
import com.yonyou.sns.im.entity.YMRoster;
import com.yonyou.sns.im.util.XMPPHelper;
import com.yonyou.sns.im.util.bitmap.BitmapCacheManager;

/**
 * 最近联系人Adapter
 *
 * @author litfb
 * @date 2014年10月23日
 * @version 1.0
 */
public class RecentRosterAdapter extends UserAdapter {

	/** Context */
	private Context context;
	/** ContentResolver */
	private LayoutInflater layoutInflater;
	/** 最近联系人 */
	private List<YMRoster> recentRosterList = new ArrayList<YMRoster>();
	/** 头像缓存管理 */
	private BitmapCacheManager avatarBitmapCacheManager;

	public RecentRosterAdapter(Context context) {
		this(context, false);
	}

	public RecentRosterAdapter(Context context, boolean isSelect) {
		super(isSelect);
		this.context = context;

		layoutInflater = LayoutInflater.from(context);

		// 这个是URL头像的缓存任务
		avatarBitmapCacheManager = new BitmapCacheManager(context, true, BitmapCacheManager.CIRCLE_BITMAP,
				BitmapCacheManager.BITMAP_DPSIZE_40);
		avatarBitmapCacheManager.generateBitmapCacheWork();
	}

	/**
	 * 更新联系人信息
	 */
	public void requery() {
		recentRosterList = YYIMRosterManager.getInstance().getRecentRosters();
		// 通知变更
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return recentRosterList.size();
	}

	@Override
	public YMRoster getItem(int position) {
		return recentRosterList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// roster
		final YMRoster roster = recentRosterList.get(position);

		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.recent_contact_list_item, parent, false);
			// init view holder
			viewHolder = new ViewHolder();
			viewHolder.IconImage = (ImageView) convertView.findViewById(R.id.recent_contact_list_item_icon);
			viewHolder.nameText = (TextView) convertView.findViewById(R.id.recent_contact_list_item_text);
			viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.recent_contact_list_item_checkbox);
			convertView.setTag(R.string.viewholder, viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag(R.string.viewholder);
		}
		// 姓名
		viewHolder.nameText.setText(TextUtils.isEmpty(roster.getAlias()) ? StringUtils.parseBareName(roster.getJid())
				: roster.getAlias());
		// 头像
		avatarBitmapCacheManager.loadFormCache(XMPPHelper.getFullFilePath(roster.getPhoto()), viewHolder.IconImage);
		// checkbox
		if (isSelect()) {
			if (isAlreadyExistsMembers(roster.getJid())) {
				viewHolder.checkBox.setEnabled(false);
				viewHolder.checkBox.setChecked(true);
			} else {
				viewHolder.checkBox.setChecked(isUserSelected(roster.getJid()));
				// 绑定事件
				viewHolder.checkBox.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						boolean isChecked = ((CheckBox) v).isChecked();
						// 设置选中状态
						selectChange(roster, isChecked);
					}

				});
			}
			viewHolder.checkBox.setVisibility(View.VISIBLE);
		} else {
			viewHolder.checkBox.setVisibility(View.GONE);
		}
		// 头像点击事件
		viewHolder.IconImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 取jid
				String jid = roster.getJid();
				// 跳转到VCard页面
				Intent intent = new Intent(context, VCardActivity.class);
				intent.putExtra(VCardActivity.EXTRA_JID, jid);
				context.startActivity(intent);
			}

		});
		return convertView;
	}

	/**
	 * view holder
	 *
	 * @author litfb
	 * @date 2014年10月23日
	 * @version 1.0
	 */
	private static class ViewHolder {

		ImageView IconImage;
		TextView nameText;
		CheckBox checkBox;
	}

}
