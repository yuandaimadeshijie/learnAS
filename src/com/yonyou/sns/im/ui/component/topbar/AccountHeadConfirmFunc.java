package com.yonyou.sns.im.ui.component.topbar;

import android.app.Activity;
import android.view.View;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.util.common.ToastUtil;

public class AccountHeadConfirmFunc extends BaseTopTextViewFunc {

	public AccountHeadConfirmFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.account_headcut_confirm;
	}

	@Override
	protected int getFuncTextRes() {
		return R.string.confirm;
	}

	@Override
	public void onclick(View v) {
		ToastUtil.showLong(getActivity(), "hahahahah");
	}

}
