package com.yonyou.sns.im.activity.fragment;

import java.lang.ref.WeakReference;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.ChatActivity;
import com.yonyou.sns.im.activity.RosterActivity;
import com.yonyou.sns.im.activity.SystemInfoActivity;
import com.yonyou.sns.im.adapter.RecentchatAdapter;
import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.core.YYIMDBNotifier;
import com.yonyou.sns.im.entity.message.YMMessage;
import com.yonyou.sns.im.entity.message.YMRecentChat;
import com.yonyou.sns.im.ui.widget.CustomDialog;

/**
 * 消息Fragment
 *
 * @author litfb
 * @date 2014年9月26日
 * @version 1.0
 */
public class RecentchatFragment extends BaseFragment {

	/** 更新list*/
	private static final int UPDATE_LIST = 0;
	/** receiver*/
	private ChatReceiver receiver = new ChatReceiver();
	/** Adapter */
	private RecentchatAdapter recentChatAdapter;
	/** 最近聊天记录列表*/
	private ListView chatListView;
	/** 空页面*/
	private View emptyView;
	/** handler*/
	private RecentChatHandler handler = new RecentChatHandler(this);
	/** mark*/
	private boolean firstUpdate=true;
	/** 列表点击事件 */
	private RecentchatItemClickListener itemClickListener = new RecentchatItemClickListener();
	/** 列表长摁事件 */
	private RecentchatItemLongClickListener itemLongClickListener = new RecentchatItemLongClickListener();

	public RecentchatFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 注册receiver
		getFragmentActivity().registerReceiver(receiver, new IntentFilter(YYIMDBNotifier.MESSAGE_CHANGE));
		getFragmentActivity().registerReceiver(receiver, new IntentFilter(YYIMDBNotifier.VCARD_CHANGE));
		// RecentchatAdapter
		recentChatAdapter = new RecentchatAdapter(getFragmentActivity());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 取消receiver注册
		getFragmentActivity().unregisterReceiver(receiver);
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_recentchat;
	}

	@Override
	protected void initView(LayoutInflater inflater, View view) {
		chatListView = (ListView) view.findViewById(R.id.recent_listview);
		emptyView = view.findViewById(R.id.empty_view);
		View button = view.findViewById(R.id.dialog_button);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View paramView) {
				Intent friendIntent = new Intent(getFragmentActivity(), RosterActivity.class);
				startActivity(friendIntent);
			}
		});
		// empty view
		chatListView.setEmptyView(emptyView);
		// adapter
		chatListView.setAdapter(recentChatAdapter);
		// event listeners
		chatListView.setOnItemClickListener(itemClickListener);
		chatListView.setOnItemLongClickListener(itemLongClickListener);
		// 重新查询
		if(firstUpdate){
			updateChat();
			firstUpdate=false;
		}
	}

	/**
	 * 接收者
	 * @author wudl
	 * @date 2014年12月16日
	 * @version V1.0
	 */
	private class ChatReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			updateChat();
		}

	}

	/**
	 * 异步更新列表
	 */
	private void updateChat() {
		new Thread() {
			@Override
			public void run() {
				List<YMRecentChat> recentChatList = YYIMChatManager.getInstance().getRecentChat();
				handler.obtainMessage(UPDATE_LIST, recentChatList).sendToTarget();
			};
		}.start();
	}

	/**
	 * 点击事件监听
	 *
	 * @author litfb
	 * @date 2014年9月29日
	 * @version 1.0
	 */
	private class RecentchatItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			YMRecentChat chat = (YMRecentChat) recentChatAdapter.getItem(position);
			String roomJid = chat.getRoom_jid();
			if (YMMessage.TYPE_SYSTEM.equals(chat.getChat_type())) {// 系统消息
				Intent intent = new Intent(RecentchatFragment.this.getFragmentActivity(), SystemInfoActivity.class);
				startActivity(intent);
			} else {// 打开聊天界面
				Intent chatIntent = new Intent(RecentchatFragment.this.getFragmentActivity(), ChatActivity.class);
				chatIntent.putExtra(ChatActivity.EXTRA_ROOM_JID, roomJid);
				startActivity(chatIntent);
			}
		}

	}

	/**
	 * 长摁事件监听
	 *
	 * @author litfb
	 * @date 2014年9月29日
	 * @version 1.0
	 */
	private class RecentchatItemLongClickListener implements OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			YMRecentChat chat = (YMRecentChat) recentChatAdapter.getItem(position);
			String roomJid = chat.getRoom_jid();
			if (YMMessage.TYPE_SYSTEM.equals(chat.getChat_type())) {// 群聊

			} else {
				removeChatHistoryDialog(roomJid, chat.getOpposite_name());
			}
			return true;
		}

	}

	/**
	 * 删除历史
	 * 
	 * @param roomJid
	 * @param userName
	 */
	private void removeChatHistoryDialog(final String roomJid, final String userName) {
		new CustomDialog.Builder(getFragmentActivity()).setTitle(R.string.delete)
				.setMessage(getResources().getString(R.string.deleteChatHistory_text, userName))
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						YYIMChatManager.getInstance().deleteChatByJid(roomJid);
					}

				}).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).create().show();
	}

	/**
	 * 主线程
	 * @author wudl
	 * @date 2014年12月19日
	 * @version V1.0
	 */
	private static class RecentChatHandler extends Handler {

		/** 若引用*/
		WeakReference<RecentchatFragment> reference;

		public RecentChatHandler(RecentchatFragment fragment) {
			reference = new WeakReference<RecentchatFragment>(fragment);
		}

		public RecentchatFragment getRecentchatFragment() {
			return reference.get();
		}

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case UPDATE_LIST:
				getRecentchatFragment().recentChatAdapter.setRecentChatList((List<YMRecentChat>) msg.obj);
				getRecentchatFragment().recentChatAdapter.notifyDataSetChanged();
				break;

			default:
				break;
			}
		}
	}
}
