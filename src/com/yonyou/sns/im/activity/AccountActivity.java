package com.yonyou.sns.im.activity;

import java.lang.ref.WeakReference;
import java.util.Hashtable;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.core.YYIMCallBack;
import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.core.YYIMSessionManager;
import com.yonyou.sns.im.entity.YMVCard;
import com.yonyou.sns.im.exception.YMErrorConsts;
import com.yonyou.sns.im.ui.component.func.BaseFunc;
import com.yonyou.sns.im.ui.component.func.account.AccountDeptFunc;
import com.yonyou.sns.im.ui.component.func.account.AccountEmailFunc;
import com.yonyou.sns.im.ui.component.func.account.AccountHeadFunc;
import com.yonyou.sns.im.ui.component.func.account.AccountJidFunc;
import com.yonyou.sns.im.ui.component.func.account.AccountModifyPassword;
import com.yonyou.sns.im.ui.component.func.account.AccountNameFunc;
import com.yonyou.sns.im.ui.component.func.account.AccountRemarkFunc;
import com.yonyou.sns.im.ui.component.func.account.AccountTelFunc;
import com.yonyou.sns.im.ui.component.func.account.AccountTitleFunc;
import com.yonyou.sns.im.ui.widget.CustomDialog;
import com.yonyou.sns.im.ui.widget.CustomDialog.Builder;
import com.yonyou.sns.im.util.bitmap.BitmapCacheManager;
import com.yonyou.sns.im.util.common.ToastUtil;
import com.yonyou.sns.im.util.inject.InjectView;

/**
 * 个人信息Activity
 * 
 * @author litfb
 * @date 2014年10月11日
 * @version 1.0
 */
public class AccountActivity extends SimpleTopbarActivity {

	/** 查询vcard handler success mark*/
	public static final int QUERY_VCARD_SUCCESS = 0;
	/** 查询出错 mark*/
	public static final int QUERY_VCARD_FALIED = 1;
	/** 主功能列表 */
	private static Class<?> mainFuncArray[] = { AccountHeadFunc.class, AccountNameFunc.class };
	/** 子功能列表 */
	private static Class<?> subFuncArray[] = { AccountDeptFunc.class,AccountTitleFunc.class, AccountEmailFunc.class, AccountTelFunc.class,
			AccountJidFunc.class, AccountRemarkFunc.class, AccountModifyPassword.class };
	/** 功能对象 */
	private Hashtable<Integer, BaseFunc> htFunc = new Hashtable<Integer, BaseFunc>();
	/** 缓存处理器 */
	private BitmapCacheManager bitmapCacheManager;
	/** vCard */
	private YMVCard vCardEntity;
	/** handler*/
	private AccountHandler handler = new AccountHandler(this);
	/** 自定义功能View */
	@InjectView(id = R.id.account_main_view)
	private LinearLayout mainFuncView;
	/** 自定义功能列表 */
	@InjectView(id = R.id.account_main_list)
	private LinearLayout mainFuncList;
	/** 系统功能View */
	@InjectView(id = R.id.account_sub_view)
	private LinearLayout subFuncView;
	/** 系统功能列表 */
	@InjectView(id = R.id.account_sub_list)
	private LinearLayout subFuncList;
	/** 弹出框*/
	private Builder builder;

	@Override
	protected Object getTopbarTitle() {
		return R.string.account_info;
	}

	/**
	 * 主功能列表
	 * 
	 * @return
	 */
	protected Class<?>[] getMainFuncArray() {
		return mainFuncArray;
	}

	/**
	 * 子功能列表
	 * 
	 * @return
	 */
	protected Class<?>[] getSubFuncArray() {
		return subFuncArray;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account);
		// 查询VCard
		vCardEntity = YYIMChatManager.getInstance().queryVCard(YYIMSessionManager.getInstance().getUserJid());
		// 初始化主功能
		initMainFunc();
		// 初始化子功能
		initSubFunc();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 异步查询一次vcard
		reLoad();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		for (BaseFunc func : htFunc.values()) {
			// onActivityResult
			if (func.acceptRequest(requestCode)) {
				func.onActivityResult(requestCode, resultCode, data);
			}
		}
	}

	/**
	 * BitmapCacheManager
	 * 
	 * @return
	 */
	public BitmapCacheManager getBitmapCacheManager() {
		if (bitmapCacheManager == null) {
			// 初始化图片缓存管理器
			bitmapCacheManager = new BitmapCacheManager(this, true, BitmapCacheManager.CIRCLE_BITMAP,
					BitmapCacheManager.BITMAP_DPSIZE_80);
			bitmapCacheManager.generateBitmapCacheWork();
		}
		return bitmapCacheManager;
	}

	/**
	 * 初始化主功能
	 */
	protected void initMainFunc() {
		Class<?>[] mainFuncs = getMainFuncArray();
		// 功能列表为空,隐藏区域
		if (mainFuncs == null || mainFuncs.length == 0) {
			mainFuncView.setVisibility(View.GONE);
			return;
		}
		// 初始化功能
		boolean isSeparator = false;
		for (Class<?> mainFunc : mainFuncs) {
			// view
			View funcView = getFuncView(getLayoutInflater(), mainFunc, isSeparator);
			if (funcView != null) {
				// 点击事件
				funcView.setOnClickListener(this);
				// 加入页面
				mainFuncList.addView(funcView);
				isSeparator = true;
			}
		}
		// 设置列表显示
		mainFuncList.setVisibility(View.VISIBLE);
	}

	/**
	 * 初始化自定义功能
	 */
	protected void initSubFunc() {
		Class<?>[] subFuncs = getSubFuncArray();
		// 功能列表为空,隐藏区域
		if (subFuncs == null || subFuncs.length == 0) {
			subFuncView.setVisibility(View.GONE);
			return;
		}
		// 初始化功能
		boolean isSeparator = false;
		for (Class<?> subFunc : subFuncs) {
			// view
			View funcView = getFuncView(getLayoutInflater(), subFunc, isSeparator);
			if (funcView != null) {
				// 点击事件
				funcView.setOnClickListener(this);
				// 加入页面
				subFuncList.addView(funcView);
				isSeparator = true;
			}
		}
		// 设置列表显示
		subFuncList.setVisibility(View.VISIBLE);
	}

	/**
	 * 获得功能View
	 * 
	 * @param inflater
	 * @param func
	 * @return
	 */
	private View getFuncView(LayoutInflater inflater, Class<?> funcClazz, boolean isSeparator) {
		BaseFunc func = BaseFunc.newInstance(funcClazz, this);
		if (func == null) {
			return null;
		}
		htFunc.put(func.getFuncId(), func);
		// view
		View funcView = func.initFuncView(isSeparator, vCardEntity);
		return funcView;
	}

	/**
	 * 取VCard
	 * 
	 * @return
	 */
	public YMVCard getYMVCard() {
		return vCardEntity;
	}

	/**
	 * 设置VCard
	 * 
	 * @param entity
	 */
	public void setYMVCard(YMVCard entity) {
		vCardEntity = entity;
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		// func
		BaseFunc func = htFunc.get(v.getId());
		// 处理点击事件
		if (func != null) {
			func.onclick();
		}
	}

	/**
	 * 显示编辑弹出框
	 * 
	 * @param text
	 *            输入框的值
	 * @param ok
	 *            确认点击事件
	 * @param cancel
	 */
	public void showEditDialog(String text, DialogInterface.OnClickListener ok, DialogInterface.OnClickListener cancel) {
		LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
		// 重命名弹出框
		builder = new CustomDialog.Builder(this);
		builder.setTitle(R.string.alert_dialog_me_title);
		// 设置内容视图
		builder.setEditView(inflater, text);
		// 确定按钮
		builder.setPositiveButton(android.R.string.ok, ok);
		builder.setNegativeButton(android.R.string.cancel, cancel);
		// 创建弹出框并显示
		builder.create().show();
	}

	/**
	 * 获取弹出框对象
	 * @return
	 */
	public Builder getBuilder() {
		return builder;
	}

	/**
	 * 重新从服务其获取vcard
	 */
	public void reLoad() {
		// 异步查询一次vcard
		YYIMChatManager.getInstance().loadVCard(null, new YYIMCallBack() {

			@Override
			public void onSuccess(Object object) {
				AccountActivity.this.vCardEntity = (YMVCard) object;
				handler.sendEmptyMessage(QUERY_VCARD_SUCCESS);
			}

			@Override
			public void onProgress(int progress, String status) {
			}

			@Override
			public void onError(int errno, String errmsg) {
				switch (errno) {
				case YMErrorConsts.ERROR_AUTHORIZATION:
					handler.obtainMessage(QUERY_VCARD_FALIED, "已断开连接").sendToTarget();
					break;
				case YMErrorConsts.EXCEPTION_NORESPONSE:
					handler.obtainMessage(QUERY_VCARD_FALIED, "服务器未响应").sendToTarget();
					break;
				case YMErrorConsts.EXCEPTION_LOAD_VCARD:
					handler.obtainMessage(QUERY_VCARD_FALIED, "加载vcard异常").sendToTarget();
					break;
				case YMErrorConsts.EXCEPTION_UNKNOWN:
					handler.obtainMessage(QUERY_VCARD_FALIED, "未知错误").sendToTarget();
					break;
				default:
					break;
				}
			}
		});
	}

	/**
	 * 更新VCard
	 * 
	 * @param vCardEntity
	 */
	public void updateVCard(YMVCard vCard) {
		// 发送更新包
		YYIMChatManager.getInstance().updateVCard(vCard, new YYIMCallBack() {

			@Override
			public void onSuccess(Object object) {
				// 重新获取vcard
				reLoad();
			}

			@Override
			public void onProgress(int progress, String status) {

			}

			@Override
			public void onError(int errno, String errmsg) {
				switch (errno) {
				case YMErrorConsts.ERROR_AUTHORIZATION:
					handler.obtainMessage(QUERY_VCARD_FALIED, "已断开连接").sendToTarget();
					break;
				case YMErrorConsts.EXCEPTION_NORESPONSE:
					handler.obtainMessage(QUERY_VCARD_FALIED, "服务器未响应").sendToTarget();
					break;
				case YMErrorConsts.EXCEPTION_LOAD_VCARD:
					handler.obtainMessage(QUERY_VCARD_FALIED, "更新vcard异常").sendToTarget();
					break;
				case YMErrorConsts.EXCEPTION_UNKNOWN:
					handler.obtainMessage(QUERY_VCARD_FALIED, "未知错误").sendToTarget();
					break;
				default:
					break;
				}
			}
		});
	}

	/**
	 * 刷新界面
	 */
	private void bindFuncView() {
		// bindFuncView
		for (BaseFunc func : htFunc.values()) {
			func.bindView();
		}
	}

	/**
	 * handler
	 * @author wudl
	 * @date 2014年11月28日
	 * @version V1.0
	 */
	private static class AccountHandler extends Handler {

		/** 弱引用*/
		private WeakReference<AccountActivity> refActivity;

		AccountHandler(AccountActivity activity) {
			this.refActivity = new WeakReference<AccountActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			AccountActivity activity = refActivity.get();
			switch (msg.what) {
			case QUERY_VCARD_SUCCESS:
				activity.bindFuncView();
				break;
			case QUERY_VCARD_FALIED:
				ToastUtil.showLong(activity, msg.obj.toString());
				break;
			default:
				break;
			}
		}
	}
}
