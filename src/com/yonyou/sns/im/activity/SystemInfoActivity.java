package com.yonyou.sns.im.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.core.YYIMCallBack;
import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.core.YYIMDBNotifier;
import com.yonyou.sns.im.core.YYIMRosterManager;
import com.yonyou.sns.im.entity.message.YMMessage;
import com.yonyou.sns.im.entity.message.YMMessageContent;
import com.yonyou.sns.im.exception.YMErrorConsts;
import com.yonyou.sns.im.util.CommonConstants;
import com.yonyou.sns.im.util.XMPPHelper;
import com.yonyou.sns.im.util.bitmap.BitmapCacheManager;
import com.yonyou.sns.im.util.common.TimeUtil;
import com.yonyou.sns.im.util.common.ToastUtil;

/**
 * 系统消息页面
 * @author wudl
 *
 */
public class SystemInfoActivity extends SimpleTopbarActivity {

	/** 发送接受邀请成功*/
	private static final int SEND_ACCEPT_SUCCESS = 0;
	/** 发送拒绝邀请成功*/
	private static final int SEND_REFAUSE_SUCCESS = 1;
	/** 发送接受邀请失败*/
	private static final int SEND_ACCEPT__FAILED = 2;
	/** 发送拒接邀请失败*/
	private static final int SEND_REFAUSE_FAILED = 3;
	/** 系统消息列表 */
	private ListView mSystemInfoListView;

	private SystemInfoAdapter adapter;

	private BitmapCacheManager bitmapCacheManager;
	/** receiver*/
	private ChatReceiver receiver = new ChatReceiver();
	/** 系统消息 */
	private List<YMMessage> mSystemInfoList = new ArrayList<>();
	/** handler*/
	private SystemInfoHanler handler = new SystemInfoHanler(this);

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_systeminfo);

		mSystemInfoListView = (ListView) findViewById(R.id.system_info_listview);
		// 列表适配器
		adapter = new SystemInfoAdapter();
		// 设置适配器
		mSystemInfoListView.setAdapter(adapter);
		// 初始化图片缓存管理器
		bitmapCacheManager = new BitmapCacheManager(this, true, BitmapCacheManager.CIRCLE_BITMAP,
				BitmapCacheManager.BITMAP_DPSIZE_40);
		bitmapCacheManager.generateBitmapCacheWork();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 获取数据
		mSystemInfoList = YYIMChatManager.getInstance().getMessage(CommonConstants.JID_SYSTEM_MESSAGE);
		mSystemInfoListView.setSelection(mSystemInfoList.size() - 1);
		// 注册receiver
		registerReceiver(receiver, new IntentFilter(YYIMDBNotifier.MESSAGE_CHANGE));
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 注销receiver
		unregisterReceiver(receiver);
	}

	@Override
	protected Object getTopbarTitle() {
		return R.string.systeminfo_title;
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
	}

	/**
	 * 系统消息适配器
	 * @author wudl
	 *
	 */
	private class SystemInfoAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mSystemInfoList.size();
		}

		@Override
		public Object getItem(int position) {
			return mSystemInfoList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// 判空处理
			if (convertView == null) {
				convertView = LayoutInflater.from(SystemInfoActivity.this).inflate(R.layout.systeminfo_list_item, null);
			}

			final YMMessage message = mSystemInfoList.get(position);
			if (YMMessage.STATE_NEW == message.getStatus()) {
				YYIMChatManager.getInstance().updateMessageState(message.get_id(), YMMessage.STATE_SENT_OR_READ);
			}
			// 头像
			ImageView mIcon = (ImageView) convertView.findViewById(R.id.systeminfo_item_icon);
			if(XMPPHelper.isMultiPushSystemChat(message.getOpposite_jid())){
				mIcon.setImageResource(R.drawable.icon_system_message);
			}else{
				bitmapCacheManager.loadFormCache(XMPPHelper.getFullFilePath(message.getOpposite_photo()), mIcon);
				mIcon.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 打开vcard
						Intent intent = new Intent(SystemInfoActivity.this, VCardActivity.class);
						intent.putExtra(VCardActivity.EXTRA_JID, message.getOpposite_jid());
						startActivity(intent);
					}
				});
			}
			// 消息
			TextView mMessage = (TextView) convertView.findViewById(R.id.systeminfo_item_msg);
			mMessage.setText(YMMessageContent.parseMessage(message.getMessage()).getMessage());
			// 名字
			TextView mName = (TextView) convertView.findViewById(R.id.systeminfo_item_name);
			String name = YYIMChatManager.getInstance().getNameByJid(message.getOpposite_jid());
			mName.setText(name);
			// 结果
			TextView resultText = (TextView) convertView.findViewById(R.id.systeminfo_item_result);
			// 接受按钮
			Button acceptButton = (Button) convertView.findViewById(R.id.systeminfo_item_accept);
			// 拒绝按钮
			Button refuseButton = (Button) convertView.findViewById(R.id.systeminfo_item_refuse);
			// 判断是否已接受邀请
			int status = message.getSpecific_status();
			switch (status) {
			case YMMessage.SPECIFIC_INVITE_ACCEPT:
				resultText.setVisibility(View.VISIBLE);
				acceptButton.setVisibility(View.GONE);
				refuseButton.setVisibility(View.GONE);
				resultText.setText(getResources().getString(R.string.systeminfo_accepted));
				break;
			case YMMessage.SPECIFIC_INVITE_REFUSE:
				resultText.setVisibility(View.VISIBLE);
				acceptButton.setVisibility(View.GONE);
				refuseButton.setVisibility(View.GONE);
				resultText.setText(getResources().getString(R.string.systeminfo_refused));
				break;
			default:
				if(XMPPHelper.isMultiPushSystemChat(message.getOpposite_jid())){
					resultText.setVisibility(View.VISIBLE);
					acceptButton.setVisibility(View.GONE);
					resultText.setText(TimeUtil.parseTimeExplicit(message.getDate()));
				}else{
					resultText.setVisibility(View.GONE);
					acceptButton.setVisibility(View.VISIBLE);
					acceptButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							YYIMRosterManager.getInstance().acceptRosterInvite(message, new YYIMCallBack() {

								@Override
								public void onSuccess(Object object) {
									handler.sendEmptyMessage(SEND_ACCEPT_SUCCESS);
								}

								@Override
								public void onProgress(int progress, String status) {
								}

								@Override
								public void onError(int errno, String errmsg) {
									switch (errno) {
									case YMErrorConsts.ERROR_AUTHORIZATION:
										handler.obtainMessage(SEND_ACCEPT__FAILED, "连接已断开").sendToTarget();
										break;
									case YMErrorConsts.EXCEPTION_UNKNOWN:
										handler.obtainMessage(SEND_ACCEPT__FAILED, "未知异常").sendToTarget();
										break;
									default:
										break;
									}
								}
							});
						}
					});
					refuseButton.setVisibility(View.VISIBLE);
					refuseButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							YYIMRosterManager.getInstance().refuseRosterInvite(message, new YYIMCallBack() {

								@Override
								public void onSuccess(Object object) {
									handler.sendEmptyMessage(SEND_REFAUSE_SUCCESS);
								}

								@Override
								public void onProgress(int progress, String status) {
								}

								@Override
								public void onError(int errno, String errmsg) {
									switch (errno) {
									case YMErrorConsts.ERROR_AUTHORIZATION:
										handler.obtainMessage(SEND_REFAUSE_FAILED, "连接已断开").sendToTarget();
										break;
									case YMErrorConsts.EXCEPTION_UNKNOWN:
										handler.obtainMessage(SEND_REFAUSE_FAILED, "未知异常").sendToTarget();
										break;
									default:
										break;
									}
								}
							});
						}
					});
				}
				
				break;
			}
			return convertView;
		}
	}

	private void updateView() {
		mSystemInfoList = YYIMChatManager.getInstance().getMessage(CommonConstants.JID_SYSTEM_MESSAGE);
		adapter.notifyDataSetChanged();
	}
	/**
	 * 接收者
	 * @author wudl
	 * @date 2014年12月16日
	 * @version V1.0
	 */
	private class ChatReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			updateView();
		}
		
	}

	/**
	 * SystemInfo Hanler,主要处理界面更新
	 * @author wudl
	 * @date 2014年12月9日
	 * @version V1.0
	 */
	private static class SystemInfoHanler extends Handler {

		/** 若引用*/
		private WeakReference<SystemInfoActivity> reference;

		public SystemInfoHanler(SystemInfoActivity activity) {
			reference = new WeakReference<SystemInfoActivity>(activity);
		}

		/**
		 * 获取activity
		 * @return
		 */
		public SystemInfoActivity getActivity() {
			return reference.get();
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SEND_ACCEPT_SUCCESS:
				ToastUtil.showLong(getActivity(), "你俩已成为好友。");
				break;
			case SEND_ACCEPT__FAILED:
				ToastUtil.showLong(getActivity(), msg.obj.toString());
				break;
			case SEND_REFAUSE_SUCCESS:
				ToastUtil.showLong(getActivity(), "拒绝成功，IM君很遗憾你错失了一位好基友。");
				break;
			case SEND_REFAUSE_FAILED:
				ToastUtil.showLong(getActivity(), msg.obj.toString());
				break;
			default:
				break;
			}
		}
	}
}
