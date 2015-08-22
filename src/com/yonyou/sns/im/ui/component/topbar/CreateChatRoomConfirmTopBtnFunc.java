package com.yonyou.sns.im.ui.component.topbar;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.CreateChatRoomActivity;

/**
 * 发起群聊右侧确认按钮
 * @author wudl
 *
 */
public class CreateChatRoomConfirmTopBtnFunc extends BaseTopTextViewFunc {

	public CreateChatRoomConfirmTopBtnFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.chatroom_list_topbar_confirm;
	}

	@Override
	public void onclick(View v) {
		((CreateChatRoomActivity) getActivity()).onConfirm();
	}

	@Override
	public View initFuncView(LayoutInflater inflater) {
		View view = super.initFuncView(inflater);
		getFuncView().setEnabled(false);
		getTextView().setEnabled(false);
		return view;
	}

	@Override
	protected int getFuncTextRes() {
		return R.string.confirm;
	}

	@Override
	public void reView() {
		int size = ((CreateChatRoomActivity) getActivity()).getSelectedSize();
		if (size > 0) {
			getTextView()
					.setText(
							getActivity().getResources().getString(R.string.create_chat_room_confirm_btn,
									String.valueOf(size)));
			getFuncView().setEnabled(true);
			getTextView().setEnabled(true);
		} else {
			getTextView().setText(getFuncTextRes());
			getFuncView().setEnabled(false);
			getTextView().setEnabled(false);
		}
	}

}
