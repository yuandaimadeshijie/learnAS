package com.yonyou.sns.im.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.VCardActivity;
import com.yonyou.sns.im.core.YYIMRosterManager;
import com.yonyou.sns.im.entity.YMLetterRoster;
import com.yonyou.sns.im.entity.YMRoster;
import com.yonyou.sns.im.entity.YMVCard;
import com.yonyou.sns.im.ui.widget.LetterLocationBar;
import com.yonyou.sns.im.util.XMPPHelper;
import com.yonyou.sns.im.util.bitmap.BitmapCacheManager;

/**
 * 联系人adapter
 *
 * @author litfb
 * @date 2014年10月23日
 * @version 1.0
 */
public class RosterAdapter extends UserAdapter implements SectionIndexer {

	/** Context */
	private Context context;
	/** LayoutInflater */
	private LayoutInflater inflater;
	/** 存放当前adapter中分组的集合 */
	private List<YMLetterRoster> rosterList = new ArrayList<YMLetterRoster>();
	/** 图片缓存管理器 */
	private BitmapCacheManager avatarBitmapCacheManager;
	/** key */
	private String key;

	public RosterAdapter(Context context) {
		this(context, false);
	}

	public RosterAdapter(Context context, boolean isSelect) {
		super(isSelect);
		this.context = context;

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
		if (!TextUtils.isEmpty(key)) {
			requery(this.key);
			return;
		}
		List<YMRoster> rosters = YYIMRosterManager.getInstance().getRosters();
		genRosters(rosters);
	}

	/**
	 * 重新查询好友列表数据
	 * 
	 * @param key
	 */
	public void requery(String key) {
		this.key = key;
		if (TextUtils.isEmpty(key)) {
			requery();
			return;
		}
		// 查询联系人
		List<YMRoster> rosters = YYIMRosterManager.getInstance().getRostersByKey(key);
		genRosters(rosters);
	}

	private void genRosters(List<YMRoster> rosters) {
		// 封装数据,处理首字母信息
		List<YMLetterRoster> rosterList = new ArrayList<YMLetterRoster>();
		for (YMRoster roster : rosters) {
			YMLetterRoster letterRoster = new YMLetterRoster(roster);
			rosterList.add(letterRoster);
		}
		// 首字母排序
		Collections.sort(rosterList, new LetterComparator());
		// 设置rosterList
		this.rosterList = rosterList;
		notifyDataSetChanged();
	}

	static class ViewHolder {

		TextView letterView;
		ImageView iconView;
		TextView nameText;
		CheckBox checkBox;
		TextView infoView;
		View phoneCall;
	}

	@Override
	public int getCount() {
		return rosterList.size();
	}

	@Override
	public YMLetterRoster getItem(int position) {
		return rosterList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// 当前数据
		YMLetterRoster letterRosterEntity = rosterList.get(position);
		// 首字母
		Character firstLetter = letterRosterEntity.getFirstLetter();
		// 联系人
		final YMRoster roster = letterRosterEntity.getRoster();
		// vcard
		YMVCard rosterVcard = letterRosterEntity.getRosterVcard();
		ViewHolder viewHolder = null;
		if (convertView == null) {
			// 创建新的holder
			viewHolder = new ViewHolder();
			// inflate view
			convertView = inflater.inflate(R.layout.friend_group_list_item, parent, false);
			// init holder
			viewHolder.letterView = (TextView) convertView.findViewById(R.id.letter_name);
			viewHolder.iconView = (ImageView) convertView.findViewById(R.id.friend_icon);
			viewHolder.nameText = (TextView) convertView.findViewById(R.id.friend_name);
			viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.friend_choose_checkbox);
			viewHolder.infoView = (TextView) convertView.findViewById(R.id.friend_info);
			viewHolder.phoneCall = convertView.findViewById(R.id.friend_call_icon);
			// set tag
			convertView.setTag(R.string.HOLDER_TAG_VIEW_HOLDER, viewHolder);
		} else {
			// get from tag
			viewHolder = (ViewHolder) convertView.getTag(R.string.HOLDER_TAG_VIEW_HOLDER);
		}

		// 如果position是必须显示首字母名称
		if (position == 0) {
			viewHolder.letterView.setVisibility(View.VISIBLE);
			viewHolder.letterView.setText(String.valueOf(firstLetter));
		} else {
			// 如果和上一个是同一个首字符，隐藏首字符，如果不是则显示首字母
			Character lastLetter = rosterList.get(position - 1).getFirstLetter();
			if (firstLetter.equals(lastLetter)) {
				viewHolder.letterView.setVisibility(View.GONE);
			} else {
				viewHolder.letterView.setVisibility(View.VISIBLE);
				viewHolder.letterView.setText(String.valueOf(firstLetter));
			}
		}
		// 名称
		viewHolder.nameText.setText(roster.getAlias());
		// info
		String info = "";
		if (rosterVcard != null && !StringUtils.isEmpty(rosterVcard.getTitle())) {
			info += rosterVcard.getTitle();
		}
		if (rosterVcard != null && !StringUtils.isEmpty(rosterVcard.getOrg_unit())) {
			if(!StringUtils.isEmpty(info)){
				info+="·";
			}
			info += rosterVcard.getOrg_unit();
		}
		if (StringUtils.isEmpty(info)) {
			viewHolder.infoView.setVisibility(View.GONE);
		} else {
			viewHolder.infoView.setVisibility(View.VISIBLE);
			viewHolder.infoView.setText(info);
		}
		// 头像
		avatarBitmapCacheManager.loadFormCache(XMPPHelper.getFullFilePath(roster.getPhoto()), viewHolder.iconView);

		if (!XMPPHelper.isSystemChat(roster.getJid())) {
			// 头像点击事件
			viewHolder.iconView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// 跳转到VCard页面
					Intent intent = new Intent(context, VCardActivity.class);
					intent.putExtra(VCardActivity.EXTRA_JID, roster.getJid());
					context.startActivity(intent);
				}
			});
		}

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
			viewHolder.phoneCall.setVisibility(View.GONE);
		} else {
			viewHolder.checkBox.setVisibility(View.GONE);
			viewHolder.phoneCall.setVisibility(View.VISIBLE);
		}
		return convertView;
	}

	/**
	 * 首字母排序
	 *
	 * @author litfb
	 * @date 2014年10月23日
	 * @version 1.0
	 */
	class LetterComparator implements Comparator<YMLetterRoster> {

		@Override
		public int compare(YMLetterRoster o1, YMLetterRoster o2) {
			if ('#' == o1.getFirstLetter()) {
				return 1;
			} else if ('#' == o2.getFirstLetter()) {
				return -1;
			}
			return o1.getFirstLetter().compareTo(o2.getFirstLetter());
		}

	}

	@Override
	public Object[] getSections() {
		return null;
	}

	@Override
	public int getPositionForSection(int sectionIndex) {
		// 通过选择的字母获得指定的位置
		if (sectionIndex == LetterLocationBar.SECTION_TOP) {// 如果是置顶，直接返回第一行
			return 0;
		} else {
			YMLetterRoster entity = null;
			for (int i = 0; i < getCount(); i++) {
				entity = (YMLetterRoster) rosterList.get(i);
				char firstLetter = entity.getFirstLetter();
				if (firstLetter == sectionIndex) {
					return i;
				}
			}
		}
		return -1;
	}

	@Override
	public int getSectionForPosition(int position) {
		return 0;
	}

}
