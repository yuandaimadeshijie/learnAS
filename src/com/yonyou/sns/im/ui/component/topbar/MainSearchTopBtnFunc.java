package com.yonyou.sns.im.ui.component.topbar;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.SearchActivity;

public class MainSearchTopBtnFunc extends BaseTopImageBtnFunc {

	public MainSearchTopBtnFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.main_topbar_search;
	}

	@Override
	public int getFuncIcon() {
		return R.drawable.selector_search_btn;
	}

	@Override
	public void onclick(View v) {
		Intent intent = new Intent(getActivity(), SearchActivity.class);
		getActivity().startActivity(intent);
	}

}
