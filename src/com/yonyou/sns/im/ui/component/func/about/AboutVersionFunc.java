package com.yonyou.sns.im.ui.component.func.about;

import android.app.Activity;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.AboutActivity;
import com.yonyou.sns.im.ui.component.func.BaseFunc;

/**
 * 检查新版本
 *
 * @author litfb
 * @date 2014年10月8日
 * @version 1.0
 */
public class AboutVersionFunc extends BaseFunc {

	public AboutVersionFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.about_func_version;
	}

	@Override
	public int getFuncIcon() {
		return 0;
	}

	@Override
	public int getFuncName() {
		return R.string.about_func_version;
	}

	@Override
	public boolean hasArrowRight() {
		return false;
	}

	@Override
	public void onclick() {
		((AboutActivity) getActivity()).checkAppVersion();
	}

}
