package com.yonyou.sns.im.ui.component.topbar;

import android.app.Activity;
import android.view.View;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.ChatRoomActivity;

public class ChatRoomListAddTopBtnFunc extends BaseTopImageBtnFunc {

	public ChatRoomListAddTopBtnFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.chatroom_list_topbar_add;
	}

	@Override
	public int getFuncIcon() {
		return R.drawable.selector_add_btn;
	}

	@Override
	public void onclick(View v) {
		((ChatRoomActivity) getActivity()).showCreateMultiChatActionBar(v);
	}

}
