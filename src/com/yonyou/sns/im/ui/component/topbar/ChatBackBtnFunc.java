package com.yonyou.sns.im.ui.component.topbar;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.MainActivity;

public class ChatBackBtnFunc extends BaseTopImageBtnFunc {

	public ChatBackBtnFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.topbar_back;
	}

	@Override
	public int getFuncIcon() {
		return R.drawable.selector_back_btn;
	}

	@Override
	public void onclick(View v) {
		Intent intent = new Intent(getActivity(), MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		getActivity().startActivity(intent);
	}
}
