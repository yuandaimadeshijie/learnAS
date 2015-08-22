package com.yonyou.sns.im.activity;

import java.lang.ref.WeakReference;
import java.util.Hashtable;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.core.YYIMCallBack;
import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.core.YYIMRosterManager;
import com.yonyou.sns.im.entity.YMVCard;
import com.yonyou.sns.im.exception.YMErrorConsts;
import com.yonyou.sns.im.ui.component.func.BaseFunc;
import com.yonyou.sns.im.ui.component.func.vcard.VCardDeptFunc;
import com.yonyou.sns.im.ui.component.func.vcard.VCardEmailFunc;
import com.yonyou.sns.im.ui.component.func.vcard.VCardNameFunc;
import com.yonyou.sns.im.ui.component.func.vcard.VCardTelFunc;
import com.yonyou.sns.im.ui.component.topbar.VCardRightTopBtnFunc;
import com.yonyou.sns.im.util.XMPPHelper;
import com.yonyou.sns.im.util.bitmap.BitmapCacheManager;
import com.yonyou.sns.im.util.common.ToastUtil;

/**
 * 个人信息页
 *
 * @author litfb
 * @date 2014年10月16日
 * @version 1.0
 */
public class VCardActivity extends SimpleTopbarActivity {

	/** 绑定vcard*/
	private static final int MESSAGE_BIND_VCARD = 0;
	/** show failed msg*/
	private static final int SHOW_FAILED_MSG = 1;
	/** 移除联系人成功*/
	private static final int REMOVE_ROSTER_SUCCESS = 2;
	/** 移除联系人失败*/
	private static final int REMOVE_ROSTER_FAILED = 3;
	/** extra jid*/
	public static final String EXTRA_JID = "EXTRA_JID";

	/** VCard展示项列表 */
	private static Class<?> vcardFuncArray[] = { VCardNameFunc.class, VCardDeptFunc.class, VCardEmailFunc.class,
			VCardTelFunc.class };
	/** 功能对象 */
	private Hashtable<Integer, BaseFunc> htFunc = new Hashtable<Integer, BaseFunc>();
	/** 缓存处理器 */
	private BitmapCacheManager bitmapCacheManager;

	/** 头像 */
	private ImageView imageHead;
	/** 名称 */
	private TextView textName;
	/** 功能View */
	private LinearLayout vCardFuncView;
	/** 功能列表 */
	private LinearLayout vCardFuncList;
	/** 发送消息按钮 */
	Button mSendMessage;
	/** vcard */
	private YMVCard vCardEntity;
	/** handler */
	private VCardHandler handler = new VCardHandler(this);

	@Override
	protected Object getTopbarTitle() {
		return R.string.vcard_info;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vcard);

		imageHead = (ImageView) findViewById(R.id.vcard_head);
		textName = (TextView) findViewById(R.id.vcard_name);
		vCardFuncView = (LinearLayout) findViewById(R.id.vcard_func_view);
		vCardFuncList = (LinearLayout) findViewById(R.id.vcard_func_list);
		mSendMessage = (Button) findViewById(R.id.vcard_send_message);

		// 初始化图片缓存管理器
		initBitmapCacheManager();
		// 查询VCard
		vCardEntity = YYIMChatManager.getInstance().queryVCard(getJid());
		// 初始化展示项列表
		initVCardFunc();
		// 发送消息按钮
		mSendMessage.setOnClickListener(this);
	}

	/**
	 * bindView
	 */
	private void bindView() {
		// 头像
		bitmapCacheManager.loadFormCache(vCardEntity != null ? XMPPHelper.getFullFilePath(vCardEntity.getAvatar())
				: null, imageHead);
		// 用户名
		String nameShow = vCardEntity.getNameShow();
		textName.setText(TextUtils.isEmpty(nameShow) ? YYIMChatManager.getInstance().getNameByJid(getJid()) : nameShow);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 异步查询一次vcard
		YYIMChatManager.getInstance().loadVCard(getJid(), new YYIMCallBack() {

			@Override
			public void onSuccess(Object object) {
				VCardActivity.this.vCardEntity = (YMVCard) object;
				handler.sendEmptyMessage(MESSAGE_BIND_VCARD);
			}

			@Override
			public void onProgress(int progress, String status) {
			}

			@Override
			public void onError(int errno, String errmsg) {
				switch (errno) {
				case YMErrorConsts.ERROR_AUTHORIZATION:
					handler.obtainMessage(SHOW_FAILED_MSG, "已断开连接").sendToTarget();
					break;
				case YMErrorConsts.EXCEPTION_NORESPONSE:
					handler.obtainMessage(SHOW_FAILED_MSG, "服务器未响应").sendToTarget();
					break;
				case YMErrorConsts.EXCEPTION_LOAD_VCARD:
					handler.obtainMessage(SHOW_FAILED_MSG, "加载vcard异常").sendToTarget();
					break;
				case YMErrorConsts.EXCEPTION_UNKNOWN:
					handler.obtainMessage(SHOW_FAILED_MSG, "未知错误").sendToTarget();
					break;
				default:
					break;
				}
			}
		});
	}

	/**
	 * 初始化图片缓存管理器
	 */
	private void initBitmapCacheManager() {
		bitmapCacheManager = new BitmapCacheManager(this, true, BitmapCacheManager.CIRCLE_BITMAP,
				BitmapCacheManager.BITMAP_DPSIZE_120);
		bitmapCacheManager.generateBitmapCacheWork();
	}

	/**
	 * 获得Jid
	 * 
	 * @return
	 */
	public String getJid() {
		return getIntent().getStringExtra(EXTRA_JID);
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
	 * 获得展示项列表
	 * 
	 * @return
	 */
	protected Class<?>[] getVCardFuncArray() {
		return vcardFuncArray;
	}

	/**
	 * 初始化展示项
	 * 
	 * @param view
	 */
	protected void initVCardFunc() {
		Class<?>[] vCardFuncs = getVCardFuncArray();
		// 功能列表为空,隐藏区域
		if (vCardFuncs == null || vCardFuncs.length == 0) {
			vCardFuncView.setVisibility(View.GONE);
			return;
		}
		// 初始化功能
		boolean isSeparator = false;
		for (Class<?> vCardFunc : vCardFuncs) {
			// view
			View funcView = getFuncView(getLayoutInflater(), vCardFunc, isSeparator);
			if (funcView != null) {
				// 点击事件
				funcView.setOnClickListener(this);
				// 加入页面
				vCardFuncList.addView(funcView);
				isSeparator = true;
			}
		}
		// 设置列表显示
		vCardFuncList.setVisibility(View.VISIBLE);
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

	@Override
	public void onClick(View v) {
		super.onClick(v);
		// func
		BaseFunc func = htFunc.get(v.getId());
		// 处理点击事件
		if (func != null) {
			func.onclick();
		}

		if (v.getId() == R.id.vcard_send_message) {
			startChatActivity();
		}
	}

	@Override
	protected Class<?>[] getTopbarRightFuncArray() {
		Class<?>[] rightFunc = { VCardRightTopBtnFunc.class };
		return rightFunc;
	}

	/**
	 * 打开聊天界面
	 */
	private void startChatActivity() {
		Intent intent = new Intent(VCardActivity.this, ChatActivity.class);
		intent.putExtra(ChatActivity.EXTRA_ROOM_JID, getJid());
		startActivity(intent);
	}

	/**
	 * 删除好友
	 */
	public void removeRoster() {
		YYIMRosterManager.getInstance().removeRoster(getJid(), new YYIMCallBack() {

			@Override
			public void onSuccess(Object object) {
				handler.sendEmptyMessage(REMOVE_ROSTER_SUCCESS);
			}

			@Override
			public void onProgress(int progress, String status) {
			}

			@Override
			public void onError(int errno, String errmsg) {
				switch (errno) {
				case YMErrorConsts.ERROR_AUTHORIZATION:
					handler.obtainMessage(REMOVE_ROSTER_FAILED, "连接已断开").sendToTarget();
					break;
				case YMErrorConsts.EXCEPTION_NORESPONSE:
					handler.obtainMessage(REMOVE_ROSTER_FAILED, "服务器未响应").sendToTarget();
					break;
				case YMErrorConsts.EXCEPTION_REMOVE_ROSTER:
					handler.obtainMessage(REMOVE_ROSTER_FAILED, "删除失败").sendToTarget();
					break;
				case YMErrorConsts.EXCEPTION_UNKNOWN:
					handler.obtainMessage(REMOVE_ROSTER_FAILED, "未知异常").sendToTarget();
					break;
				default:
					break;
				}
			}
		});
	}

	/**
	 * 发送添加好友请求
	 */
	public void addRoster() {
		// 陌生人显示添加好友
		YYIMRosterManager.getInstance().addRoster(getJid(), new YYIMCallBack() {

			@Override
			public void onSuccess(Object object) {
			}

			@Override
			public void onProgress(int progress, String status) {
			}

			@Override
			public void onError(int errno, String errmsg) {
			}
		});
	}

	/**
	 * 刷新页面
	 */
	private void bindFuncView() {
		for (BaseFunc func : htFunc.values()) {
			func.bindView();
		}
	}

	/**
	 * vcard handler,主要处理界面更新
	 * @author wudl
	 * @date 2014年12月8日
	 * @version V1.0
	 */
	private static class VCardHandler extends Handler {

		/** 弱引用 */
		WeakReference<VCardActivity> refActivity;

		VCardHandler(VCardActivity activity) {
			this.refActivity = new WeakReference<VCardActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			VCardActivity activity = refActivity.get();
			switch (msg.what) {
			case MESSAGE_BIND_VCARD:
				activity.bindView();
				activity.bindFuncView();
				break;
			case SHOW_FAILED_MSG:
				ToastUtil.showLong(activity, msg.obj.toString());
				break;
			case REMOVE_ROSTER_SUCCESS:
				activity.mSendMessage.setVisibility(View.GONE);
				ToastUtil.showLong(activity, "删除成功");
				break;
			case REMOVE_ROSTER_FAILED:
				ToastUtil.showLong(activity, msg.obj.toString());
				break;
			default:
				break;
			}
		}
	}

}
