package com.yonyou.sns.im.ui.component.func;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.log.YMLogger;

/**
 * "我"页面中的系统与用户功能列表抽象类
 *
 * @author litfb
 * @date 2014年10月8日
 * @version 1.0
 */
public abstract class BaseFunc implements IFuncRequestCode {

	/** funcView */
	private View funcView;

	/** 功能id */
	public abstract int getFuncId();

	/** 功能图标 */
	public abstract int getFuncIcon();

	/** 功能名称 */
	public abstract int getFuncName();

	/** 弱引用 */
	private WeakReference<Activity> refActivity;

	public BaseFunc(Activity activity) {
		this.refActivity = new WeakReference<Activity>(activity);
	}

	/**
	 * 实例化一个BaseFunc
	 * 
	 * @param clazz
	 * @param activity
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static BaseFunc newInstance(Class<?> clazz, Activity activity) {
		if (!BaseFunc.class.isAssignableFrom(clazz)) {
			return null;
		}
		BaseFunc func = null;
		try {
			Constructor<BaseFunc> constructor = (Constructor<BaseFunc>) clazz.getConstructor(Activity.class);
			func = (BaseFunc) constructor.newInstance(activity);
		} catch (Exception e) {
			YMLogger.d(e);
		}
		return func;
	}

	/**
	 * 取Activity
	 * 
	 * @return
	 */
	protected Activity getActivity() {
		return refActivity.get();
	}

	/**
	 * 功能点击事件
	 * 
	 * @param activity
	 */
	public abstract void onclick();

	/**
	 * 是否处理该request
	 * 
	 * @param requestCode
	 * @return
	 */
	public boolean acceptRequest(int requestCode) {
		return false;
	}

	/**
	 * onActivityResult
	 * 
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

	}

	/**
	 * 是否有右箭头
	 * 
	 * @return
	 */
	public boolean hasArrowRight() {
		return true;
	}

	/**
	 * 获得功能View
	 * 
	 * @param isSeparator
	 * @param params
	 * @return
	 */
	public View initFuncView(boolean isSeparator, Object... params) {
		// view
		funcView = getActivity().getLayoutInflater().inflate(R.layout.view_func, null);
		// func id
		funcView.setId(getFuncId());
		// func icon
		ImageView iconView = (ImageView) funcView.findViewById(R.id.func_icon);
		if (getFuncIcon() > 0) {
			iconView.setImageResource(getFuncIcon());
		} else {
			iconView.setVisibility(View.GONE);
		}
		// func name
		TextView nameView = (TextView) funcView.findViewById(R.id.func_name);
		nameView.setText(getFuncName());
		// custom view
		LinearLayout customView = (LinearLayout) funcView.findViewById(R.id.func_custom);
		initCustomView(customView);
		// arrow right
		if (!hasArrowRight()) {
			ImageView rightView = (ImageView) funcView.findViewById(R.id.func_right);
			rightView.setVisibility(View.INVISIBLE);
		}
		// separator
		if (!isSeparator) {
			View separator = funcView.findViewById(R.id.func_separator);
			separator.setVisibility(View.GONE);
		}
		// bindView
		bindView();
		return funcView;
	}

	/**
	 * bindView
	 * 
	 * @param params
	 */
	public void bindView() {

	}

	/**
	 * 自定义部分
	 * 
	 * @param customView
	 * @param params
	 */
	protected void initCustomView(LinearLayout customView) {

	}

}
