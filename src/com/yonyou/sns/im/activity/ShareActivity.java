package com.yonyou.sns.im.activity;

import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.fragment.RecentRosterFragment;
import com.yonyou.sns.im.activity.fragment.RosterSearchFragment;
import com.yonyou.sns.im.activity.fragment.UserFragment;
import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.core.YYIMSessionManager;
import com.yonyou.sns.im.entity.IUser;
import com.yonyou.sns.im.entity.IUserSelectListener;
import com.yonyou.sns.im.util.inject.InjectView;

/**
 * 分享到
 *
 * @author litfb
 * @date 2014年12月6日
 * @version 1.0
 */
public class ShareActivity extends SimpleTopbarActivity implements IUserSelectListener {

	private static final String SCHEME_FILE = "file";

	/** frame */
	@InjectView(id = R.id.share_frame)
	private FrameLayout frameFragments;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_share);

		Handler handler = new Handler();
		handler.post(new Runnable() {

			@Override
			public void run() {
				// 用户名
				String account = YYIMSessionManager.getInstance().getAccount();
				// 密码
				String password = YYIMSessionManager.getInstance().getPassword();
				if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password) || password.length() != 32) {
					Intent intent = new Intent(ShareActivity.this, LoginActivity.class);
					intent.putExtra(LoginActivity.LOGIN_FROM, getClass().getName());
					startActivity(intent);
					overridePendingTransition(R.anim.anim_fade_in, R.anim.activity_hold);
					finish();
				}
			}
		});

		// 最近联系人
		showRecentRoster();

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
		fragment.setSelect(false);
		fragment.setCustomClick(true);
		fragment.setUserSelectListener(this);

		// replace fragment
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.share_frame, fragment,tag);
		ft.addToBackStack(null);
		ft.commit();
	}

	@Override
	public void selectChange(IUser user, boolean isChecked) {

	}

	@Override
	public void onUserclick(IUser user) {
		if (user == null || TextUtils.isEmpty(user.getUserJid())) {
			return;
		}

		// intent, action and MIME type
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if (intent.getExtras().containsKey(Intent.EXTRA_STREAM)) {
				handleSendStream(user.getUserJid(), intent);
			} else if (intent.getExtras().containsKey(Intent.EXTRA_TEXT)) {
				handleSendText(user.getUserJid(), intent);
			}
		} else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
			handleSendMultiple(user.getUserJid(), intent);
		}
		startChatActivity(user.getUserJid());
	}

	private void handleSendText(String jid, Intent intent) {
		String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
		if (sharedText != null) {
			YYIMChatManager.getInstance().sendTextMessage(jid, sharedText);
		}
	}

	private void handleSendStream(String jid, Intent intent) {
		Uri shareUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		if (shareUri != null) {
			if (SCHEME_FILE.equalsIgnoreCase(shareUri.getScheme())) {
				YYIMChatManager.getInstance().sendFileMessage(jid, shareUri.getPath());
			}
		}
	}

	private void handleSendMultiple(String jid, Intent intent) {
		List<Uri> shareUriList = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
		if (shareUriList != null) {
			for (Uri shareUri : shareUriList) {
				if (SCHEME_FILE.equalsIgnoreCase(shareUri.getScheme())) {
					YYIMChatManager.getInstance().sendFileMessage(jid, shareUri.getPath());
				}
			}
		}
	}

	@Override
	public boolean isUserSelected(String jid) {
		return false;
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
		return R.string.share_send;
	}

	@Override
	public boolean isAlreadyExistsMembers(String userJid) {
		return false;
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

}
