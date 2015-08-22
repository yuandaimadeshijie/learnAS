package com.yonyou.sns.im.activity.fragment;

import java.util.Hashtable;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.AccountActivity;
import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.core.YYIMSessionManager;
import com.yonyou.sns.im.entity.YMVCard;
import com.yonyou.sns.im.ui.component.func.BaseFunc;
import com.yonyou.sns.im.ui.component.func.me.MeProtocolFunc;
import com.yonyou.sns.im.ui.component.func.me.MeSettingFunc;
import com.yonyou.sns.im.util.XMPPHelper;
import com.yonyou.sns.im.util.bitmap.BitmapCacheManager;
import com.yonyou.sns.im.util.inject.InjectView;

/**
 * 我
 *
 * @author litfb
 * @date 2014年10月8日
 * @version 1.0
 */
public class MeFragment extends BaseFragment implements OnClickListener {

	/** 系统功能列表 */
	private static Class<?> systemFuncArray[] = { MeProtocolFunc.class, MeSettingFunc.class };

	/** 功能对象 */
	private Hashtable<Integer, BaseFunc> htFunc = new Hashtable<Integer, BaseFunc>();
	/** 缓存处理器 */
	private BitmapCacheManager bitmapCacheManager;
	/** vcard entity*/
	private YMVCard vCardEntity;
	/** 头像 */
	@InjectView(id = R.id.me_account_head)
	private ImageView imageHead;
	/** 名称 */
	@InjectView(id = R.id.me_account_name)
	private TextView textName;
	/** 个人信息设置 */
	@InjectView(id = R.id.me_account_setting)
	private View viewAccountSetting;
	/** 自定义功能View */
	@InjectView(id = R.id.me_custom_func_view)
	private LinearLayout customFuncView;
	/** 自定义功能列表 */
	@InjectView(id = R.id.me_custom_func_list)
	private LinearLayout customFuncList;
	/** 系统功能View */
	@InjectView(id = R.id.me_system_func_view)
	private LinearLayout systemFuncView;
	/** 系统功能列表 */
	@InjectView(id = R.id.me_system_func_list)
	private LinearLayout systemFuncList;

	/**
	 * 获得自定义功能列表
	 * 
	 * @return
	 */
	protected Class<?>[] getCustomFuncArray() {
		return null;
	}

	/**
	 * 获得系统功能列表
	 * 
	 * @return
	 */
	protected Class<?>[] getSystemFuncArray() {
		return systemFuncArray;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_me;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 初始化图片缓存管理器
		initBitmapCacheManager();
	}

	@Override
	protected void initView(LayoutInflater inflater, View view) {
		// 初始化自定义功能
		initCustomFunc(inflater, view);
		// 初始化系统功能
		initSystemFunc(inflater, view);
		// 监听
		viewAccountSetting.setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		// 查询一次本地数据库VCard
		String userJid = YYIMSessionManager.getInstance().getUserJid();
		vCardEntity = YYIMChatManager.getInstance().queryVCard(userJid);
		bindView();
	}

	/**
	 * 绑定视图数据
	 */
	private void bindView() {
		// 头像
		bitmapCacheManager.loadFormCache(vCardEntity != null ? XMPPHelper.getFullFilePath(vCardEntity.getAvatar())
				: null, imageHead);
		// 显示名称
		String nameShow = vCardEntity != null ? vCardEntity.getNameShow() : null;
		// 用户名
		textName.setText(TextUtils.isEmpty(nameShow) ? YYIMSessionManager.getInstance().getAccount() : nameShow);
	}

	/**
	 * 初始化图片缓存管理器
	 */
	private void initBitmapCacheManager() {
		bitmapCacheManager = new BitmapCacheManager(getFragmentActivity(), true, BitmapCacheManager.CIRCLE_BITMAP,
				BitmapCacheManager.BITMAP_DPSIZE_80);
		bitmapCacheManager.generateBitmapCacheWork();
	}

	/**
	 * 初始化系统功能
	 * 
	 * @param view
	 */
	protected void initSystemFunc(LayoutInflater inflater, View view) {
		Class<?>[] systemFuncs = getSystemFuncArray();
		// 功能列表为空,隐藏区域
		if (systemFuncs == null || systemFuncs.length == 0) {
			systemFuncView.setVisibility(View.GONE);
			return;
		}
		// 初始化功能
		boolean isSeparator = false;
		for (Class<?> systemFunc : systemFuncs) {
			// view
			View funcView = getFuncView(inflater, systemFunc, isSeparator);
			if (funcView != null) {
				// 点击事件
				funcView.setOnClickListener(this);
				// 加入页面
				systemFuncList.addView(funcView);
				isSeparator = true;
			}
		}
		// 设置列表显示
		systemFuncList.setVisibility(View.VISIBLE);
	}

	/**
	 * 初始化自定义功能
	 * 
	 * @param view
	 */
	protected void initCustomFunc(LayoutInflater inflater, View view) {
		Class<?>[] customFuncs = getCustomFuncArray();
		// 功能列表为空,隐藏区域
		if (customFuncs == null || customFuncs.length == 0) {
			customFuncView.setVisibility(View.GONE);
			return;
		}
		// 初始化功能
		boolean isSeparator = false;
		for (Class<?> customFunc : customFuncs) {
			// view
			View funcView = getFuncView(inflater, customFunc, isSeparator);
			if (funcView != null) {
				// 点击事件
				funcView.setOnClickListener(this);
				// 加入页面
				customFuncList.addView(funcView);
				isSeparator = true;
			}
		}
		// 设置列表显示
		customFuncList.setVisibility(View.VISIBLE);
	}

	/**
	 * 获得功能View
	 * 
	 * @param inflater
	 * @param func
	 * @return
	 */
	private View getFuncView(LayoutInflater inflater, Class<?> funcClazz, boolean isSeparator) {
		BaseFunc func = BaseFunc.newInstance(funcClazz, getFragmentActivity());
		if (func == null) {
			return null;
		}
		htFunc.put(func.getFuncId(), func);
		// view
		View funcView = func.initFuncView(isSeparator);
		return funcView;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.me_account_setting:
			startActivity(new Intent(getFragmentActivity(), AccountActivity.class));
			break;
		default:
			// func
			BaseFunc func = htFunc.get(v.getId());
			// 处理点击事件
			if (func != null) {
				func.onclick();
			}
			break;
		}
	}

}
