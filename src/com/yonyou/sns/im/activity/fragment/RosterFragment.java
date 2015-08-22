package com.yonyou.sns.im.activity.fragment;

import java.lang.ref.WeakReference;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.ChatActivity;
import com.yonyou.sns.im.adapter.RosterAdapter;
import com.yonyou.sns.im.core.YYIMCallBack;
import com.yonyou.sns.im.core.YYIMDBNotifier;
import com.yonyou.sns.im.core.YYIMRosterManager;
import com.yonyou.sns.im.entity.YMRoster;
import com.yonyou.sns.im.exception.YMErrorConsts;
import com.yonyou.sns.im.ui.widget.CustomDialog;
import com.yonyou.sns.im.ui.widget.CustomDialog.Builder;
import com.yonyou.sns.im.ui.widget.LetterLocationBar;
import com.yonyou.sns.im.ui.widget.quickaction.ActionItem;
import com.yonyou.sns.im.ui.widget.quickaction.QuickAction;
import com.yonyou.sns.im.ui.widget.quickaction.QuickAction.OnActionItemClickListener;
import com.yonyou.sns.im.util.XMPPHelper;
import com.yonyou.sns.im.util.common.ToastUtil;
import com.yonyou.sns.im.util.inject.InjectView;

public class RosterFragment extends UserFragment implements TextWatcher {

	public static final String TAG = "RosterFragment";
	/** 重名名 dialog*/
	private static final int USER_ACTION_RENAME = 1;
	/** 取消 dialog*/
	private static final int USER_ACTION_CNACEL = 2;
	/** 删除 dialog*/
	private static final int USER_ACTION_DELETE = 3;
	/** 成功删除好友*/
	private static final int DELETE_ROSTER_SUCCESS = 0;
	/** 成功重命名好友*/
	private static final int RENAME_ROSTER_SUCCESS = 1;
	/** 删除好友失败*/
	private static final int DELETE_ROSTER_FAILED = 2;
	/** 重命名失败*/
	private static final int RENAME_ROSTER_FAILED =3; 

	/** adapter */
	private RosterAdapter rosterAdapter;

	/** 搜索edit */
	@InjectView(id = R.id.friend_search_edit)
	private EditText searchEdit;
	/** 搜索hint */
	@InjectView(id = R.id.friend_search_hint)
	private View searchHint;
	/** 搜索删除*/
	@InjectView(id = R.id.friend_search_delete)
	private View searchDel;
	/** 好友列表 */
	@InjectView(id = R.id.friend_list)
	private ListView rosterListView;
	/** empty */
	@InjectView(id = R.id.empty_view)
	private View emptyView;
	/** 字母索引条 */
	@InjectView(id = R.id.friend_sidebar)
	private LetterLocationBar letterLocationBar;
	/** 显示当前选中的字母  */
	@InjectView(id = R.id.friend_sidebar_text)
	private TextView sidebarText;
	/** handler*/
	private RosterHandler handler =new RosterHandler(this);
	/** receiver*/
	private RosterReceiver receiver=new RosterReceiver();
	@Override
	protected int getLayoutId() {
		return R.layout.fragment_roster;
	}

	@Override
	protected void initView(LayoutInflater inflater, View view) {
		// adapter
		rosterAdapter = new RosterAdapter(getFragmentActivity(), isSelect());
		rosterAdapter.setUserSelectListener(getUserSelectListener());
		rosterListView.setAdapter(rosterAdapter);
		TextView emptyMsg=(TextView) emptyView.findViewById(R.id.dialog_msg);
		emptyMsg.setText(R.string.dialog_guide_content_empty_roster);
		emptyView.findViewById(R.id.dialog_button).setVisibility(View.GONE);
		rosterListView.setEmptyView(emptyView);
		// 搜索框
		searchEdit.addTextChangedListener(this);
		// 删除
		searchDel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				searchEdit.setText("");
			}
		});
		if (!isSelect() && !isCustomClick()) {
			rosterListView.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					YMRoster roster = rosterAdapter.getItem(position).getRoster();

					showChildQuickActionBar(view.findViewById(R.id.friend_icon), roster);

					return true;
				}
			});
		}
		if (!isSelect()) {
			rosterListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					YMRoster roster = rosterAdapter.getItem(position).getRoster();
					startChatActivity(roster.getJid());
				}

			});
		}

		if (!isSelect()) {
			letterLocationBar.setTextView(sidebarText);
			letterLocationBar.setListView(rosterListView);
		} else {
			letterLocationBar.setVisibility(View.GONE);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		updateRoster(null);
		// 注册观察者
		getFragmentActivity().registerReceiver(receiver, new IntentFilter(YYIMDBNotifier.ROSTER_CHANGE));
	}
	@Override
	public void onPause() {
		super.onPause();
		// 注销联系人观察者
		getFragmentActivity().unregisterReceiver(receiver);
	}

	/**
	 * 
	 * 打开聊天会话页面
	 * 
	 * @param userJid
	 */
	private void startChatActivity(String userJid) {
		Intent chatIntent = new Intent(getFragmentActivity(), ChatActivity.class);
		chatIntent.putExtra(ChatActivity.EXTRA_ROOM_JID, userJid);
		startActivity(chatIntent);
	}

	@Override
	public void dataChanged() {
		rosterAdapter.notifyDataSetChanged();
	}

	/**
	 * key 为空则查询所有，不为空则查询关键字
	 * @param key
	 */
	private void updateRoster(String key) {
		rosterAdapter.requery(key);
		letterLocationBar.invalidate();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (s.length() > 0) {
			String key = searchEdit.getText().toString();
			// 判断是否是中文
			key = XMPPHelper.toLowerCaseNotChinese(key);
			// 更新
			updateRoster(key);
			searchHint.setVisibility(View.GONE);
			searchDel.setVisibility(View.VISIBLE);
		} else {
			// 更新
			updateRoster("");
			searchHint.setVisibility(View.VISIBLE);
			searchDel.setVisibility(View.GONE);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {

	}

	/**
	 * 长按联系人出来的弹出框
	 * 
	 * @param view
	 * @param roster
	 */
	private void showChildQuickActionBar(View view, final YMRoster roster) {
		final QuickAction quickAction = new QuickAction(getFragmentActivity(), QuickAction.HORIZONTAL);
		quickAction.addActionItem(new ActionItem(USER_ACTION_RENAME, getString(R.string.rename), getResources()
				.getDrawable(R.drawable.friend_rename)));
		quickAction.addActionItem(new ActionItem(USER_ACTION_DELETE, getString(R.string.delete), getResources()
				.getDrawable(R.drawable.friend_delete)));
		quickAction.addActionItem(new ActionItem(USER_ACTION_CNACEL, getString(R.string.cancel), getResources()
				.getDrawable(R.drawable.friend_cancel)));
		quickAction.setOnActionItemClickListener(new OnActionItemClickListener() {

			@Override
			public void onItemClick(QuickAction source, int pos, int actionId) {
				String userJid = roster.getJid();
				String userName = roster.getAlias();
				switch (actionId) {
				case USER_ACTION_RENAME:
					renameRosterItemDialog(userJid, userName);
					break;
				case USER_ACTION_DELETE:
					removeRosterItemDialog(userJid, userName);
					break;
				case USER_ACTION_CNACEL:
					quickAction.onDismiss();
					break;
				default:
					break;
				}
			}
		});

		quickAction.show(view);
	}

	/**
	 * 
	 * 重命名的对话框
	 * 
	 * @param rosterJid
	 * @param userName
	 */
	private void renameRosterItemDialog(final String rosterJid, final String userName) {
		final Builder builder = new CustomDialog.Builder(getFragmentActivity());
		builder.setTitle(R.string.RenameEntry_title);
		builder.setEditView((LayoutInflater) getFragmentActivity().getLayoutInflater(), userName);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				String newName = builder.getInput();
				if (newName.length() != 0) {
					YYIMRosterManager.getInstance().renameRoster(rosterJid, newName,new YYIMCallBack() {
						
						@Override
						public void onSuccess(Object object) {
							handler.sendEmptyMessage(RENAME_ROSTER_SUCCESS);
						}
						
						@Override
						public void onProgress(int progress, String status) {
						}
						
						@Override
						public void onError(int errno, String errmsg) {
							switch (errno) {
							case YMErrorConsts.ERROR_AUTHORIZATION:
								handler.obtainMessage(RENAME_ROSTER_FAILED, "连接已断开").sendToTarget();
								break;
							case YMErrorConsts.EXCEPTION_EMPTY_INPUT:
								handler.obtainMessage(RENAME_ROSTER_FAILED, "输入不能为空").sendToTarget();
								break;
							case YMErrorConsts.EXCEPTION_UNKNOWN:
								handler.obtainMessage(RENAME_ROSTER_FAILED, "未知异常").sendToTarget();
								break;
							default:
								break;
							}
						}
					});
				}
				((RosterAdapter) (rosterListView.getAdapter())).notifyDataSetChanged();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		builder.create().show();
	}

	/**
	 * 
	 * 删除好友
	 * 
	 * @param rosterJid
	 * @param userName
	 */
	private void removeRosterItemDialog(final String rosterJid, final String userName) {
		final Builder builder = new CustomDialog.Builder(getFragmentActivity());
		builder.setTitle(R.string.deleteRosterItem_title);
		builder.setMessage(getString(R.string.deleteRosterItem_text, userName));
		builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				YYIMRosterManager.getInstance().removeRoster(rosterJid,new YYIMCallBack() {
					
					@Override
					public void onSuccess(Object object) {
						handler.sendEmptyMessage(DELETE_ROSTER_SUCCESS);
					}
					
					@Override
					public void onProgress(int progress, String status) {
					}
					
					@Override
					public void onError(int errno, String errmsg) {
						switch (errno) {
						case YMErrorConsts.ERROR_AUTHORIZATION:
							handler.obtainMessage(DELETE_ROSTER_FAILED,"连接已断开").sendToTarget();
							break;
						case YMErrorConsts.EXCEPTION_NORESPONSE:
							handler.obtainMessage(DELETE_ROSTER_FAILED,"服务器未响应").sendToTarget();
							break;
						case YMErrorConsts.EXCEPTION_REMOVE_ROSTER:
							handler.obtainMessage(DELETE_ROSTER_FAILED,"删除失败").sendToTarget();
							break;
						case YMErrorConsts.EXCEPTION_UNKNOWN:
							handler.obtainMessage(DELETE_ROSTER_FAILED,"未知异常").sendToTarget();
							break;
						default:
							break;
						}
					}
				});
			}
		});
		builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface paramDialogInterface, int paramInt) {

			}
		});
		builder.create().show();
	}
	/**
	 * 联系人处理器，处理界面更新
	 * @author wudl
	 * @date 2014年12月9日
	 * @version V1.0
	 */
	private static class RosterHandler extends Handler{
		/**若引用*/
		private WeakReference<RosterFragment> reference;
		
		public RosterHandler(RosterFragment fragment) {
			reference=new WeakReference<RosterFragment>(fragment);
		}
		/**
		 * 获取fragment
		 * @return
		 */
		public RosterFragment getFragment() {
			return reference.get();
		}
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case DELETE_ROSTER_SUCCESS:
				getFragment().updateRoster("");
				ToastUtil.showLong(getFragment().getFragmentActivity(), "删除成功");
				break;
			case DELETE_ROSTER_FAILED:
				ToastUtil.showLong(getFragment().getFragmentActivity(), msg.obj.toString());
				break;
			case RENAME_ROSTER_SUCCESS:
				getFragment().updateRoster("");
				ToastUtil.showLong(getFragment().getFragmentActivity(), "重命名成功");
				break;
			case RENAME_ROSTER_FAILED:
				ToastUtil.showLong(getFragment().getFragmentActivity(), msg.obj.toString());
				break;
			default:
				break;
			}
		}
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
			rosterAdapter.requery();
			letterLocationBar.invalidate();
		}
		
	}
}
