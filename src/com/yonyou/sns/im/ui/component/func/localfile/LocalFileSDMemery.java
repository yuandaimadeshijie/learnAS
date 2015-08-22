package com.yonyou.sns.im.ui.component.func.localfile;

import android.app.Activity;

import com.yonyou.sns.im.R;

/**
 * 本地文档--sd内存
 * @author wudl
 * @date 2014年12月1日
 * @version V1.0
 */
public class LocalFileSDMemery extends LocalFileFunc {

	public LocalFileSDMemery(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.local_file_SD_memery;
	}

	@Override
	public int getFuncIcon() {
		return R.drawable.local_file_sd_membmery;
	}

	@Override
	public int getFuncName() {
		return R.string.local_file_sd_card;
	}

	@Override
	public void onclick() {

	}
}
