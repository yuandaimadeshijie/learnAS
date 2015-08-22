package com.yonyou.sns.im.base;

import android.view.LayoutInflater;
import android.view.View;

import com.yonyou.sns.im.activity.fragment.BaseFragment;
import com.yonyou.sns.im.util.inject.Injector;

public abstract class IMbaseFragment extends BaseFragment {

	@Override
	protected void initView(LayoutInflater inflater, View view) {
		Injector.getInstance().injectView(this, view);
	}

}
