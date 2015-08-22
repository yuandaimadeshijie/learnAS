package com.yonyou.sns.im.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.util.StringUtils;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.fragment.RecentRosterFragment;
import com.yonyou.sns.im.activity.fragment.RosterSearchFragment;
import com.yonyou.sns.im.activity.fragment.UserFragment;
import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.core.YYIMSessionManager;
import com.yonyou.sns.im.entity.IUser;
import com.yonyou.sns.im.entity.IUserSelectListener;
import com.yonyou.sns.im.entity.YMRoomMember;
import com.yonyou.sns.im.entity.YMVCard;
import com.yonyou.sns.im.ui.component.topbar.CreateChatRoomConfirmTopBtnFunc;
import com.yonyou.sns.im.ui.component.topbar.FragmentActivityBackBtnFunc;
import com.yonyou.sns.im.ui.widget.HorizontalListView;
import com.yonyou.sns.im.util.DialogUtil;
import com.yonyou.sns.im.util.XMPPHelper;
import com.yonyou.sns.im.util.bitmap.BitmapCacheManager;
import com.yonyou.sns.im.util.inject.InjectView;

/**
 * 创建群组和会议的页面
 * @author wudl
 *
 */
public class CreateChatRoomActivity extends SimpleTopbarActivity implements IUserSelectListener {

	/** 
	 * 判断是创建还是邀请
	 * {@link #EXTRA_TOKEN_CREATEROOM} 
	 * {@link #EXTRA_TOKEN_INVITE}
	 */
	public static final String EXTRA_TOKEN = "EXTRA_TOKEN";
	/** 如果是邀请，还需传入房间jid*/
	public static final String EXTRA_ROOM_JID = "EXTRA_ROOM_JID";
	/** 创建mark */
	public static final String EXTRA_TOKEN_CREATEROOM = "EXTRA_TOKEN_CREATEROOM";
	/** 邀请mark */
	public static final String EXTRA_TOKEN_INVITE = "EXTRA_TOKEN_INVITE";
	/** 创建房间handler mark*/
	public static final int CONFIRM_CREATE_ROOM = 0;

	/** 已选数据 */
	private List<IUser> selectedUsers = new ArrayList<>();
	/** 已经在房间内的成员数据*/
	private List<YMRoomMember> alreadyExistsMembers = new ArrayList<YMRoomMember>();
	/** 已选列表适配器 */
	private SelectedListAdapter selectedListAdapter;
	/** handler*/
	private CreateRoomHandler handler = new CreateRoomHandler(this);
	/** dialog*/
	private Dialog dialog;

	/** frame */
	@InjectView(id = R.id.create_chatroom_frame)
	private FrameLayout frameFragments;
	/** 已选列表 */
	@InjectView(id = R.id.crate_chatroom_list)
	private HorizontalListView listSelected;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_create_chatroom);
		// 邀请中dialog
		dialog = DialogUtil.getProgressDialog(CreateChatRoomActivity.this, R.string.create_chat_room_progress_bar);
		// 初始化已选列表
		selectedUsers.clear();
		// 初始化房间内的成员数据
		if (!isCreateRoom() && !StringUtils.isEmpty(getRoomJid())) {
			alreadyExistsMembers = YYIMChatManager.getInstance().getRoomMemberByJid(getRoomJid());
		}
		// 已选列表适配器
		selectedListAdapter = new SelectedListAdapter();
		listSelected.setAdapter(selectedListAdapter);
		listSelected.setOnItemClickListener(new SelectedListClickListener());
		refreshSelectedList();
		// 最近联系人
		showRecentRoster();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 返回按钮监听
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
				getSupportFragmentManager().popBackStack();
			} else {
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void showRecentRoster() {
		RecentRosterFragment recentRosterFragment = (RecentRosterFragment) getSupportFragmentManager()
				.findFragmentByTag(RecentRosterFragment.TAG);
		if (recentRosterFragment == null) {
			recentRosterFragment = new RecentRosterFragment();
		}
		changeFragment(recentRosterFragment,RecentRosterFragment.TAG);
	}

	@Override
	public void changeFragment(UserFragment fragment,String tag) {
		// 设fragment属性
		fragment.setActivity(this);
		fragment.setSelect(true);
		fragment.setUserSelectListener(this);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom);
		ft.replace(R.id.create_chatroom_frame, fragment,tag);
		ft.addToBackStack(null);
		ft.commit();
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		switch (view.getId()) {
		case R.id.create_chatroom_search:
			RosterSearchFragment rosFragment = (RosterSearchFragment) getSupportFragmentManager().findFragmentByTag(
					RosterSearchFragment.TAG);
			if (rosFragment != null) {
				rosFragment = new RosterSearchFragment();
			}
			changeFragment(rosFragment,RosterSearchFragment.TAG);
			break;
		default:
			break;
		}
	}

	@Override
	protected Object getTopbarTitle() {
		return isCreateRoom() ? R.string.create_chat_room_title : R.string.create_chatroom_invite;
	}

	@Override
	protected Class<?>[] getTopbarRightFuncArray() {
		return new Class<?>[] { CreateChatRoomConfirmTopBtnFunc.class };
	}

	@Override
	public void selectChange(IUser user, boolean isChecked) {
		// 存入滚动图片列表数据源
		if (isChecked) {
			if (!isUserSelected(user.getUserJid())) {
				selectedUsers.add(user);
			}
		} else {
			removeUser(user.getUserJid());
		}
		// 更改topbar上的确认数字
		refreshFuncView();
		// 更新已选列表
		refreshSelectedList();
	}

	@Override
	public void onUserclick(IUser user) {

	}

	public void refreshSelectedList() {
		// 通知界面更新
		selectedListAdapter.notifyDataSetChanged();
		if (selectedUsers.size() > 0) {
			listSelected.setVisibility(View.VISIBLE);
		} else {
			listSelected.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean isUserSelected(String jid) {
		if (TextUtils.isEmpty(jid)) {
			return false;
		}

		for (IUser user : selectedUsers) {
			if (jid.equals(user.getUserJid())) {
				return true;
			}
		}
		return false;
	}

	private void removeUser(String jid) {
		if (TextUtils.isEmpty(jid)) {
			return;
		}

		IUser sUser = null;
		for (IUser user : selectedUsers) {
			if (jid.equals(user.getUserJid())) {
				sUser = user;
				break;
			}
		}
		selectedUsers.remove(sUser);
		refreshSelectedList();
	}

	private boolean isCreateRoom() {
		return EXTRA_TOKEN_CREATEROOM.equals(getIntent().getStringExtra(EXTRA_TOKEN));
	}

	/**
	 * 获取传过来的RoomJid
	 * @return
	 */
	private String getRoomJid() {
		return getIntent().getStringExtra(EXTRA_ROOM_JID);
	}

	/**
	 * 点击确认按钮事件
	 */
	public void onConfirm() {
		// 必须要是有选中的人才能创建房间
		if (selectedUsers.size() > 0) {
			// progress bar
			if (!dialog.isShowing()) {
				dialog.show();
			}
			// 异步邀请
			new CreateRoomTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	/**
	 * 打开聊天室页面
	 * @param roomJid
	 * @param name
	 */
	private void startChatActivity(String roomJid) {
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(ChatActivity.EXTRA_ROOM_JID, roomJid);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	/**
	 * 生成房间名字
	 * 
	 * @return
	 */
	private String produceRoomName() {
		// 自己的VCard
		YMVCard vCard = YYIMChatManager.getInstance().queryVCard(YYIMSessionManager.getInstance().getUserJid());
		String name = null;
		if (vCard != null) {
			name = vCard.getNameShow();
		}
		if (StringUtils.isEmpty(name)) {
			name = YYIMSessionManager.getInstance().getAccount();
		}
		// 给房间命名
		for (int i = 0; i < selectedUsers.size(); i++) {
			name += "、" + selectedUsers.get(i).getUserName();
			if (name.length() > 50) {
				name = name.substring(0, 50);
				break;
			}
		}
		return name;
	}

	public int getSelectedSize() {
		return selectedUsers.size();
	}

	private class SelectedListClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// 移除item
			selectedUsers.remove(position);
			// 更改topbar上的确认数字
			refreshFuncView();
			// 更新已选列表
			refreshSelectedList();
			// 刷新fragment显示
			refreshFragmentView();
		}

	}

	/**
	 * 刷新fragment
	 */
	private void refreshFragmentView() {
		List<Fragment> fragments = getSupportFragmentManager().getFragments();
		for (Fragment fragment : fragments) {
			if (fragment instanceof UserFragment && fragment.getView().isShown()) {
				((UserFragment) fragment).dataChanged();
			}
		}
	}

	/**
	 * 滚动图片的适配器
	 * @author wudl
	 *
	 */
	private class SelectedListAdapter extends BaseAdapter {

		private BitmapCacheManager bitmapCacheManager;

		public SelectedListAdapter() {
			bitmapCacheManager = new BitmapCacheManager(CreateChatRoomActivity.this, true,
					BitmapCacheManager.CIRCLE_BITMAP, BitmapCacheManager.BITMAP_DPSIZE_40);
			bitmapCacheManager.generateBitmapCacheWork();
		}

		@Override
		public int getCount() {
			return selectedUsers.size();
		}

		@Override
		public Object getItem(int paramInt) {
			return selectedUsers.get(paramInt);
		}

		@Override
		public long getItemId(int paramInt) {
			return paramInt;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if (convertView == null) {
				int width = (int) getResources().getDimension(R.dimen.create_chatroom_selected_icon_size);
				// 没有视图，初始化视图
				imageView = new ImageView(CreateChatRoomActivity.this);
				// 设置宽高
				imageView.setLayoutParams(new ListView.LayoutParams(width, width));
				// 设置图片显示样式
				imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			} else {
				imageView = (ImageView) convertView;
			}
			// 设置图片
			bitmapCacheManager.loadFormCache(XMPPHelper.getFullFilePath(selectedUsers.get(position).getUserIcon()),
					imageView);
			return imageView;
		}
	}

	@Override
	protected Class<?> getTopbarLeftFunc() {
		return FragmentActivityBackBtnFunc.class;
	}

	@Override
	public boolean isAlreadyExistsMembers(String userJid) {
		for (YMRoomMember member : alreadyExistsMembers) {
			if (userJid.equals(member.getUserJid())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * handler
	 * @author wudl
	 * @date 2014年11月28日
	 * @version V1.0
	 */
	private static class CreateRoomHandler extends Handler {

		/** 弱引用*/
		private WeakReference<CreateChatRoomActivity> refActivity;

		CreateRoomHandler(CreateChatRoomActivity activity) {
			this.refActivity = new WeakReference<CreateChatRoomActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			CreateChatRoomActivity activity = refActivity.get();
			switch (msg.what) {
			case CONFIRM_CREATE_ROOM:
				if (msg.obj instanceof String) {
					// 关闭dialog
					if (activity.dialog.isShowing()) {
						activity.dialog.dismiss();
					}
					// 打开聊天页面
					activity.startChatActivity((String) msg.obj);
				}
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 
	 * @author wudl
	 * @date 2014年11月28日
	 * @version V1.0
	 */
	private class CreateRoomTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			String roomJid = null;
			if (isCreateRoom()) {
				// 房间名字
				String name = produceRoomName();
				// 建立chatRoom
				roomJid = YYIMChatManager.getInstance().createMultiChatRoom(name);
			} else {
				roomJid = getRoomJid();
			}
			if (!TextUtils.isEmpty(roomJid)) {
				// 邀请
				List<String> users = new ArrayList<String>();
				for (IUser user : selectedUsers) {
					users.add(user.getUserJid());
				}
				YYIMChatManager.getInstance().invitationUser(roomJid, users, "");
				handler.obtainMessage(CreateChatRoomActivity.CONFIRM_CREATE_ROOM, roomJid).sendToTarget();
			}
			return null;
		}
	}

}
