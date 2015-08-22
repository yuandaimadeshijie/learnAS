package com.yonyou.sns.im.ui.component.func.me;

import android.app.Activity;
import android.content.Intent;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.SettingActivity;
import com.yonyou.sns.im.ui.component.func.BaseFunc;

/**
 * 设置
 *
 * @author litfb
 * @date 2014年10月8日
 * @version 1.0
 */
public class MeSettingFunc extends BaseFunc {

	public MeSettingFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.me_func_setting;
	}

	@Override
	public int getFuncIcon() {
		return R.drawable.me_func_setting;
	}

	@Override
	public int getFuncName() {
		return R.string.me_func_setting;
	}

	@Override
	public void onclick() {
		getActivity().startActivity(new Intent(getActivity(), SettingActivity.class));
	}

}
