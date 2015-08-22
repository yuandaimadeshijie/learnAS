package com.yonyou.sns.im.ui.component.func.localfile;

import android.app.Activity;

import com.yonyou.sns.im.R;

/**
 * 本地文档--手机内存
 * @author wudl
 * @date 2014年12月1日
 * @version V1.0
 */
public class LocalFilePhoneMemery extends LocalFileFunc {

	public LocalFilePhoneMemery(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.local_file_phone_memery;
	}

	@Override
	public int getFuncIcon() {
		return R.drawable.local_file_phone_memery;
	}

	@Override
	public int getFuncName() {
		return R.string.local_file_phone_memery;
	}

	@Override
	public void onclick() {

	}
}
