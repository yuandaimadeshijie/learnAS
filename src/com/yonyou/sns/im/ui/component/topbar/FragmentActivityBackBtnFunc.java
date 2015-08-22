package com.yonyou.sns.im.ui.component.topbar;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.view.View;

public class FragmentActivityBackBtnFunc extends CommonBackTopBtnFunc {

	public FragmentActivityBackBtnFunc(Activity activity) {
		super(activity);
	}

	@Override
	public void onclick(View v) {
		FragmentActivity activity = (FragmentActivity) getActivity();
		if (activity.getSupportFragmentManager().getBackStackEntryCount() > 1) {
			activity.getSupportFragmentManager().popBackStack();
		} else {
			super.onclick(v);
		}
	}

}
