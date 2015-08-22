package com.yonyou.sns.im.ui.component.topbar;

import android.app.Activity;
import android.view.View;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.util.common.ToastUtil;

public class MainMenuTopBtnFunc extends BaseTopImageBtnFunc {

	public MainMenuTopBtnFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.main_topbar_menu;
	}

	@Override
	public int getFuncIcon() {
		return R.drawable.selector_menu_btn;
	}

	@Override
	public void onclick(View v) {
		ToastUtil.showShort(getActivity(), "OpenMenu");
	}

}
