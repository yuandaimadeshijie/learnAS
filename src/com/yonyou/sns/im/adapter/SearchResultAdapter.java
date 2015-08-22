package com.yonyou.sns.im.adapter;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.util.StringUtils;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.entity.SearchValue;
import com.yonyou.sns.im.util.XMPPHelper;
import com.yonyou.sns.im.util.bitmap.BitmapCacheManager;
import com.yonyou.sns.im.util.bitmap.MucIconCacheManager;
import com.yonyou.sns.im.util.common.YMStringUtils;

/**
 * 搜索结果适配器
 * @author wudl
 * @date 2014年11月25日
 * @version V1.0
 */
public class SearchResultAdapter extends BaseAdapter {

	/** 搜索结果列表数据源*/
	private List<SearchValue> mSearchResultList = new ArrayList<SearchValue>();
	/** 用户头像缓存管理*/
	private BitmapCacheManager avatarBitmapCacheManager;
	/** 群头像缓存管理*/
	private MucIconCacheManager mucIconCacheManager;
	/** context*/
	private Context context;
	/** 关键字*/
	private String key;

	public SearchResultAdapter(Context context) {
		this.context = context;
		this.key = "";
		// 初始化缓存任务
		avatarBitmapCacheManager = new BitmapCacheManager(this.context, true, BitmapCacheManager.CIRCLE_BITMAP);
		avatarBitmapCacheManager.generateBitmapCacheWork();
		// 初始化群头像缓存任务
		mucIconCacheManager = new MucIconCacheManager(this.context, BitmapCacheManager.NORMAL_BITMAP);
	}

	/**
	 * 更新页面
	 * @param key
	 * @param isForwordSearch
	 * @param isAddFriendSearch
	 */
	public void updateUI(List<SearchValue> mSearchResultList, String key) {
		this.key = key;
		this.mSearchResultList = mSearchResultList;
		notifyDataSetChanged();
	}

	/**
	 * @param key
	 */
	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public int getCount() {
		return mSearchResultList.size();
	}

	@Override
	public Object getItem(int position) {
		return mSearchResultList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SearchValue result = mSearchResultList.get(position);
		// 获取当前结果对象的jid
		String jid = result.getUserJid();
		// 头像
		String icon = result.getUserIcon();
		// name
		String name = result.getUserName();
		// module
		String module = result.getModule();
		if (!StringUtils.isEmpty(module)) {
			// 表示是模块
			convertView = LayoutInflater.from(context).inflate(R.layout.search_result_module_name, null);
			TextView mModuleName = (TextView) convertView.findViewById(R.id.search_result_module_text);
			if (result instanceof SearchValue) {
				mModuleName.setText(((SearchValue) result).getModule());
			}
		} else {
			// 表示是数据
			convertView = LayoutInflater.from(context).inflate(R.layout.search_result_item, null);
			// 设置头像
			ImageView mIcon = (ImageView) convertView.findViewById(R.id.search_result_icon);
			if (!XMPPHelper.isGroupChat(jid)) {
				avatarBitmapCacheManager.loadFormCache(XMPPHelper.getFullFilePath(icon), mIcon);
			} else {
				mucIconCacheManager.loadFormCache(jid, mIcon);
			}
			// 设置名字
			TextView mName = (TextView) convertView.findViewById(R.id.search_result_name);
			SpannableStringBuilder nameBuilder = YMStringUtils.initStyle(name, key,
					context.getResources().getColor(R.color.search_result_key));
			mName.setText(nameBuilder);
			// 设置信息
			TextView mInfo = (TextView) convertView.findViewById(R.id.search_result_msg);
			// info
			String info = result.getEmail();
			if (!StringUtils.isEmpty(info)) {
				int index = info.indexOf(key);
				if ((info.length() - index) >= 20) {
					info = info.substring(0, index + 20);
				}
				SpannableStringBuilder infoBuilder = YMStringUtils.initStyle(info, key, context.getResources()
						.getColor(R.color.search_result_key));
				mInfo.setText(infoBuilder);
			} else {
				mInfo.setVisibility(View.GONE);
			}
		}
		return convertView;
	}
}
