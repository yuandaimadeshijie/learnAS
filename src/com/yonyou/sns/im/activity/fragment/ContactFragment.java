package com.yonyou.sns.im.activity.fragment;

import java.lang.ref.WeakReference;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.ChatActivity;
import com.yonyou.sns.im.activity.ChatRoomActivity;
import com.yonyou.sns.im.activity.OrgStructActivity;
import com.yonyou.sns.im.activity.RosterActivity;
import com.yonyou.sns.im.adapter.RecentRosterAdapter;
import com.yonyou.sns.im.core.YYIMCallBack;
import com.yonyou.sns.im.core.YYIMDBNotifier;
import com.yonyou.sns.im.entity.YMOrgStruct;
import com.yonyou.sns.im.entity.YMRoster;
import com.yonyou.sns.im.smack.IMSmackManager;
import com.yonyou.sns.im.util.common.ToastUtil;
import com.yonyou.sns.im.util.inject.InjectView;

/**
 * 联系人界面
 *
 * @author litfb
 * @date 2014年10月23日
 * @version 1.0
 */
public class ContactFragment extends BaseFragment implements OnClickListener {

	/** 打开orgStruct 页面*/
	private static final int OPEN_ORGSTRUCT_PAGE = 0;
	/** 显示错误信息*/
	private static final int SHOW_FAILED_MSG = 1;
	/** handler */
	private ContectFragHandler handler = new ContectFragHandler(this);
	/** 列表的adapter  */
	private RecentRosterAdapter recentRosterAdapter;
	/** receiver*/
	private RosterReceiver receiver=new RosterReceiver();
	/** 联系人列表 */
	@InjectView(id = R.id.contact_list_view)
	private ListView listView;
	/** 上方界面 */
	@InjectView(id = R.id.contact_header)
	private View headerView;
	/** 组织架构 */
	@InjectView(id = R.id.contact_organization)
	private View organizationView;
	/** 群组列表 */
	@InjectView(id = R.id.contact_groupchat)
	private View groupchatView;
	/** 好友列表 */
	@InjectView(id = R.id.contact_friendlist)
	private View friendlistView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 注册联系人表的监听
		getFragmentActivity().registerReceiver(receiver, new IntentFilter(YYIMDBNotifier.MESSAGE_CHANGE));
		getFragmentActivity().registerReceiver(receiver, new IntentFilter(YYIMDBNotifier.VCARD_CHANGE));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 注销联系人表的监听
		getFragmentActivity().unregisterReceiver(receiver);
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_contact;
	}

	@Override
	protected void initView(LayoutInflater inflater, View view) {
		// adapter init
		recentRosterAdapter = new RecentRosterAdapter(getFragmentActivity());
		// list点击事件
		listView.setOnItemClickListener(new RecentOnItemClickListener());
		// adapter
		listView.setAdapter(recentRosterAdapter);
		// doRequery
		recentRosterAdapter.requery();
		// head点击事件
		organizationView.setOnClickListener(this);
		groupchatView.setOnClickListener(this);
		friendlistView.setOnClickListener(this);
	}

	/**
	 * 打开聊天会话页面
	 * 
	 * @param userJid
	 */
	private void startChatActivity(String userJid) {
		Intent chatIntent = new Intent(getFragmentActivity(), ChatActivity.class);
		chatIntent.putExtra(ChatActivity.EXTRA_ROOM_JID, userJid);
		startActivity(chatIntent);
	}
	/**
	 * 接收者
	 * @author wudl
	 * @date 2014年12月16日
	 * @version V1.0
	 */
	private class RosterReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			recentRosterAdapter.requery();
		}
		
	}

	private class RecentOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// 取联系人jid
			String userJid = ((YMRoster) recentRosterAdapter.getItem(position)).getJid();
			// 打开聊天页面
			startChatActivity(userJid);
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.contact_organization:
			IMSmackManager.getInstance().getRoot(new YYIMCallBack() {

				@SuppressWarnings("unchecked")
				@Override
				public void onSuccess(Object object) {

					List<YMOrgStruct> orgStructs = (List<YMOrgStruct>) object;
					if (orgStructs != null && orgStructs.size() > 0) {
						YMOrgStruct root = orgStructs.get(0);
						if (root != null) {
							handler.obtainMessage(OPEN_ORGSTRUCT_PAGE, root).sendToTarget();
						}
					}

				}

				@Override
				public void onProgress(int progress, String status) {

				}

				@Override
				public void onError(int errno, String errmsg) {
					handler.obtainMessage(SHOW_FAILED_MSG, errmsg).sendToTarget();
				}
			});

			break;
		case R.id.contact_groupchat:
			Intent groupChatIntent = new Intent(getFragmentActivity(), ChatRoomActivity.class);
			startActivity(groupChatIntent);
			break;
		case R.id.contact_friendlist:
			Intent friendIntent = new Intent(getFragmentActivity(), RosterActivity.class);
			startActivity(friendIntent);
			break;
		default:
			break;
		}
	}

	/**
	 * 联系人 handler,处理界面更新
	 * @author wudl
	 * @date 2014年12月8日
	 * @version V1.0
	 */
	private static class ContectFragHandler extends Handler {

		/** ContactFragment 若引用*/
		private WeakReference<ContactFragment> reference;

		public ContectFragHandler(ContactFragment fragment) {
			super();
			reference = new WeakReference<ContactFragment>(fragment);
		}

		public ContactFragment getFragment() {
			return reference.get();
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case OPEN_ORGSTRUCT_PAGE:
				YMOrgStruct root = (YMOrgStruct) msg.obj;
				Intent intent = new Intent(getFragment().getFragmentActivity(), OrgStructActivity.class);
				// 传过去根节点id
				intent.putExtra(OrgStructActivity.EXTRA_PID, root.getJid());
				// 传过去title
				intent.putExtra(OrgStructActivity.EXTRA_PNAME, root.getName());
				getFragment().getFragmentActivity().startActivity(intent);
				break;
			case SHOW_FAILED_MSG:
				// 显示错误信息
				ToastUtil.showLong(getFragment().getFragmentActivity(), msg.obj.toString());
				break;
			default:
				break;
			}
		}
	}

}
