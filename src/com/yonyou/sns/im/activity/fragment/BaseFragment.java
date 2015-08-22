package com.yonyou.sns.im.activity.fragment;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yonyou.sns.im.activity.SimpleTopbarActivity;
import com.yonyou.sns.im.util.inject.Injector;

/**
 * fragment基类
 *
 * @author litfb
 * @date 2014年10月8日
 * @version 1.0
 */
public abstract class BaseFragment extends Fragment {

	/** 自行持有activity */
	private WeakReference<FragmentActivity> refActivity;

	/**
	 * get layout resid
	 * 
	 * @return
	 */
	protected abstract int getLayoutId();

	/**
	 * 初始化页面
	 * 
	 * @param view
	 */
	protected abstract void initView(LayoutInflater inflater, View view);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (getLayoutId() == 0) {
			return null;
		}
		View view = inflater.inflate(getLayoutId(), container, false);
		Injector.getInstance().injectView(this, view);
		initView(inflater, view);
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof FragmentActivity) {
			setActivity((FragmentActivity) activity);
		}
	}

	/**
	 * setActivity
	 * 
	 * @param activity
	 */
	public void setActivity(FragmentActivity activity) {
		this.refActivity = new WeakReference<FragmentActivity>(activity);
	}

	/**
	 * getActivity
	 * 
	 * @return
	 */
	public FragmentActivity getFragmentActivity() {
		return refActivity.get();
	}

	/**
	 * 重设Title
	 * 
	 * @param text
	 */
	public void setActivityTitle(String text) {
		FragmentActivity fragmentActivity = getFragmentActivity();
		if (fragmentActivity instanceof SimpleTopbarActivity) {
			((SimpleTopbarActivity) fragmentActivity).resetTopbarTitle(text);
		}
	}

}