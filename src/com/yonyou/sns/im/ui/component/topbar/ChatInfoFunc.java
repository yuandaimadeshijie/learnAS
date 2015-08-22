package com.yonyou.sns.im.ui.component.topbar;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.ChatActivity;
import com.yonyou.sns.im.activity.RoomInfoActivity;
import com.yonyou.sns.im.activity.VCardActivity;

public class ChatInfoFunc extends BaseTopImageBtnFunc {

	public ChatInfoFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.topbar_chat_info;
	}

	@Override
	public int getFuncIcon() {
		if (((ChatActivity) getActivity()).isMultiChat()) {
			return R.drawable.selector_groupinfo_btn;
		} else {
			return R.drawable.selector_userinfo_btn;
		}
	}

	@Override
	public void onclick(View v) {
		if (((ChatActivity) getActivity()).isMultiChat()) {
			openRoomInfoPage();
		} else {
			openChatInfoPage();
		}
	}

	private void openRoomInfoPage() {
		Intent intent = new Intent(getActivity(), RoomInfoActivity.class);
		intent.putExtra(RoomInfoActivity.EXTRA_ROOMJID, ((ChatActivity) getActivity()).getRoomJid());
		getActivity().startActivity(intent);
	}

	private void openChatInfoPage() {
		Intent intent = new Intent(getActivity(), VCardActivity.class);
		intent.putExtra(VCardActivity.EXTRA_JID, ((ChatActivity) getActivity()).getRoomJid());
		getActivity().startActivity(intent);
	}

}
