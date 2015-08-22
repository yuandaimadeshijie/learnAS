package com.yonyou.sns.im.ui.component.topbar;

import android.app.Activity;
import android.view.View;

import com.yonyou.sns.im.R;

/**
 * 搜索topbar btn
 * @author wudl
 *
 */
public class SearchTopBtnFunc extends BaseTopImageBtnFunc {

	public SearchTopBtnFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.search_topbar_btn;
	}

	@Override
	public void onclick(View v) {

	}

	@Override
	public int getFuncIcon() {
		return R.drawable.im_search_icon_normal;
	}
}
