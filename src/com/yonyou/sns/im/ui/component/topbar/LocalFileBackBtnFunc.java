package com.yonyou.sns.im.ui.component.topbar;

import android.app.Activity;
import android.view.View;

import com.yonyou.sns.im.activity.LocalFileActivity;

/**
 * 本地文件topbar 后退按钮
 * @author wudl
 * @date 2014年12月2日
 * @version V1.0
 */
public class LocalFileBackBtnFunc extends CommonBackTopBtnFunc {

	public LocalFileBackBtnFunc(Activity activity) {
		super(activity);
	}

	@Override
	public void onclick(View v) {
		LocalFileActivity activity = (LocalFileActivity) getActivity();
		if (activity.getSupportFragmentManager().getBackStackEntryCount() > 1) {
			activity.getSupportFragmentManager().popBackStack();
		} else {
			super.onclick(v);
		}
	}

}
