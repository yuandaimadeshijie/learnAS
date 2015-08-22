package com.yonyou.sns.im.activity.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.adapter.RecentRosterAdapter;
import com.yonyou.sns.im.core.YYIMCallBack;
import com.yonyou.sns.im.core.YYIMDBNotifier;
import com.yonyou.sns.im.entity.YMOrgStruct;
import com.yonyou.sns.im.entity.YMRoster;
import com.yonyou.sns.im.smack.IMSmackManager;
import com.yonyou.sns.im.util.common.ToastUtil;
import com.yonyou.sns.im.util.inject.InjectView;

public class RecentRosterFragment extends UserFragment implements OnClickListener {

	public static final String TAG = "RecentRosterFragment";
	/** 更换fragment*/
	private static final int CHANGER_ORG_FRAGMENT = 0;
	/** 显示错误信息*/
	private static final int SHOW_FAILED_MSG = 1;
	/** receiver*/
	private ChatReceiver receiver=new ChatReceiver();
	/** 搜索和分组 */
	@InjectView(id = R.id.create_chatroom_frametop)
	private View frameTopView;
	/** 搜索 */
	@InjectView(id = R.id.create_chatroom_search)
	private View searchView;
	/** 分组grid */
	@InjectView(id = R.id.create_chatroom_grid)
	private GridView gridGroup;

	/** 最近联系人列表 */
	@InjectView(id = R.id.recent_roster_list)
	private ListView listRecentRoster;

	/** adapter */
	private RecentRosterAdapter recentRosterAdapter;

	/** handler*/
	private RecentRostFragHandler handler = new RecentRostFragHandler(this);

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_recentroster;
	}

	@Override
	protected void initView(LayoutInflater inflater, View view) {
		// 初始化最近联系人列表
		recentRosterAdapter = new RecentRosterAdapter(getFragmentActivity(), isSelect());
		recentRosterAdapter.setCustomClick(isCustomClick());
		recentRosterAdapter.setUserSelectListener(getUserSelectListener());
		listRecentRoster.setAdapter(recentRosterAdapter);

		if (isSelect() || isCustomClick()) {
			frameTopView.setVisibility(View.VISIBLE);
			// 搜索
			searchView.setOnClickListener(this);
			// 初始化分组
			initGridGroup();
		}

		if (isCustomClick()) {
			listRecentRoster.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					YMRoster roster = recentRosterAdapter.getItem(position);
					getUserSelectListener().onUserclick(roster);
				}

			});
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 注册receiver
		getFragmentActivity().registerReceiver(receiver, new IntentFilter(YYIMDBNotifier.ROSTER_CHANGE));
		getFragmentActivity().registerReceiver(receiver, new IntentFilter(YYIMDBNotifier.MESSAGE_CHANGE));
		getFragmentActivity().registerReceiver(receiver, new IntentFilter(YYIMDBNotifier.VCARD_CHANGE));
	}
	@Override
	public void onResume() {
		super.onResume();
		recentRosterAdapter.requery();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		// 注销receiver
		getFragmentActivity().unregisterReceiver(receiver);
	}
	/**
	 * 初始化选择分组列表
	 */
	private void initGridGroup() {
		// 初始化选择模块数据
		List<Map<String, Object>> list = initGridGroupData();
		// 简单适配器
		SimpleAdapter gridAdapter = new SimpleAdapter(getFragmentActivity(), list,
				R.layout.create_chat_room_select_group_list_item, new String[] { "title", "image" }, new int[] {
						R.id.create_chatroom_group_name, R.id.create_chatroom_group_icon });
		// 添加适配器
		gridGroup.setAdapter(gridAdapter);
		gridGroup.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
				case 0:
					IMSmackManager.getInstance().getRoot(new YYIMCallBack() {

						@SuppressWarnings("unchecked")
						@Override
						public void onSuccess(Object object) {
							YMOrgStruct root = ((List<YMOrgStruct>) object).get(0);
							if (root != null) {
								handler.obtainMessage(CHANGER_ORG_FRAGMENT, root).sendToTarget();
							}
						}

						@Override
						public void onProgress(int progress, String status) {
						}

						@Override
						public void onError(int errno, String errmsg) {
							handler.obtainMessage(SHOW_FAILED_MSG, errno).sendToTarget();
						}
					});
					break;
				case 1:
					ChatRoomFragment chatRoomFragment = (ChatRoomFragment) getFragmentActivity()
							.getSupportFragmentManager().findFragmentByTag(ChatRoomFragment.TAG);
					if (chatRoomFragment == null) {
						chatRoomFragment = new ChatRoomFragment();
					}
					getUserSelectListener().changeFragment(chatRoomFragment,ChatRoomFragment.TAG);
					break;
				case 2:
					RosterFragment rosterFragment = (RosterFragment) getFragmentActivity().getSupportFragmentManager()
							.findFragmentByTag(RosterFragment.TAG);
					if (rosterFragment == null) {
						rosterFragment = new RosterFragment();
					}
					getUserSelectListener().changeFragment(rosterFragment,RosterFragment.TAG);
					break;
				default:
					break;
				}
			}
		});
	}

	/**
	 * 初始化选择模块数据
	 * 
	 * @return
	 */
	private List<Map<String, Object>> initGridGroupData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("title", getString(R.string.structure_of_organization));
		map.put("image", R.drawable.structure_of_organization);
		list.add(map);
		map = new HashMap<String, Object>();
		map.put("title", getString(R.string.multi_group_chat));
		map.put("image", R.drawable.multi_group_chat);
		list.add(map);
		map = new HashMap<String, Object>();
		map.put("title", getString(R.string.roster_list));
		map.put("image", R.drawable.roster_list);
		list.add(map);
		return list;
	}

	@Override
	public void dataChanged() {
		recentRosterAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.create_chatroom_search) {
			RosterSearchFragment rosterSearchFragment = (RosterSearchFragment) getFragmentActivity()
					.getSupportFragmentManager().findFragmentByTag(RosterSearchFragment.TAG);
			if (rosterSearchFragment == null) {
				rosterSearchFragment = new RosterSearchFragment();
			}
			getUserSelectListener().changeFragment(rosterSearchFragment,RosterSearchFragment.TAG);
		}
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
			recentRosterAdapter.requery();
		}
		
	}

	/**
	 * 联系人 handler,处理界面更新
	 * @author wudl
	 * @date 2014年12月8日
	 * @version V1.0
	 */
	private static class RecentRostFragHandler extends Handler {

		/** RecentRosterFragment 若引用*/
		private WeakReference<RecentRosterFragment> reference;

		public RecentRostFragHandler(RecentRosterFragment fragment) {
			super();
			reference = new WeakReference<RecentRosterFragment>(fragment);
		}

		public RecentRosterFragment getFragment() {
			return reference.get();
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case CHANGER_ORG_FRAGMENT:
				YMOrgStruct root = (YMOrgStruct) msg.obj;
				String tag=OrgStructFragment.TAG + "_" + root.getJid();
				// 获取子页面
				OrgStructFragment orgStructFragment = (OrgStructFragment) getFragment().getFragmentActivity()
						.getSupportFragmentManager().findFragmentByTag(tag);
				if (orgStructFragment == null) {
					orgStructFragment = new OrgStructFragment();
					orgStructFragment.setPid(root.getJid());
				}
				getFragment().getUserSelectListener().changeFragment(orgStructFragment,tag);
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
