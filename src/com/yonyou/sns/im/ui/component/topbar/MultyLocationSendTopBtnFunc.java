package com.yonyou.sns.im.ui.component.topbar;

import android.app.Activity;
import android.view.View;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.MultyLocationActivity;

public class MultyLocationSendTopBtnFunc extends BaseTopTextViewFunc {

	public MultyLocationSendTopBtnFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.multy_location_send;
	}

	@Override
	public void onclick(View v) {
		((MultyLocationActivity) getActivity()).sendLocation();
	}

	@Override
	protected int getFuncTextRes() {
		return R.string.gps_send;
	}

}
