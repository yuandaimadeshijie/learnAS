package com.yonyou.sns.im.activity;

import java.util.Hashtable;
import java.util.Map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.base.BaseActivity;
import com.yonyou.sns.im.ui.component.topbar.BaseTopFunc;
import com.yonyou.sns.im.ui.component.topbar.CommonBackTopBtnFunc;

/**
 * topbar
 * 目标：
 * 1、可以自定义的增加按钮或者文本按钮（支持右侧最多添加两个图片按钮或者一个文本按钮，左侧只能是一个按钮，返回或者目录按钮）
 * 2、自定义按钮自己初始化自己的view并响应事件
 * 3、支持title名称的改变
 * 4、按钮支持文本按钮和imagebutton
 *
 * @author litfb
 * @date 2014年10月10日
 * @version 1.0
 */
public abstract class SimpleTopbarActivity extends BaseActivity implements OnClickListener {

	/** 右侧功能对象的MAP，可以通过id获得指定的功能对象 */
	protected Hashtable<Integer, BaseTopFunc> funcMap = new Hashtable<Integer, BaseTopFunc>();

	/** title */
	private TextView viewTitle;

	/** 左侧功能区，用来放置功能按钮 */
	private LinearLayout leftFuncZone;
	/** 右侧功能区，用来放置功能按钮 */
	private LinearLayout rightFuncZone;
	/** 中间功能区，用来放置功能按钮或者默认的文本域 */
	private LinearLayout middlerFuncZone;

	/**
	 * 获得title文本
	 * 
	 * @return
	 */
	protected Object getTopbarTitle() {
		return "";
	}

	/**
	 * 获得左侧的功能控件
	 * 只能是imagebutton，并且只有一个
	 * 
	 * @return
	 */
	protected Class<?> getTopbarLeftFunc() {
		// 默认使用back
		return CommonBackTopBtnFunc.class;
	}

	/**
	 * 获得右侧的功能控件集合
	 * 如果有文本，那么就不能有imagebutton
	 * 如果是imagebutton，那么可以是一个或者两个
	 * 
	 * @return
	 */
	protected Class<?>[] getTopbarRightFuncArray() {
		return null;
	}

	protected Class<?> getTopbarMiddleFunc() {
		return null;
	}

	@Override
	protected void afterSetContentView() {
		super.afterSetContentView();
		viewTitle = (TextView) findViewById(R.id.topbar_title);
		leftFuncZone = (LinearLayout) findViewById(R.id.left_func);
		rightFuncZone = (LinearLayout) findViewById(R.id.right_func);
		middlerFuncZone = (LinearLayout) findViewById(R.id.topbar_middle);

		// 判断使用默认的文字title还是使用自定义的title
		if (getTopbarMiddleFunc() != null) {
			viewTitle.setVisibility(View.GONE);

			addCustomViewToMiddleFunctionZone();
		} else {
			viewTitle.setVisibility(View.VISIBLE);

			// 设置title
			if (getTopbarTitle() instanceof Integer) {
				int title = (Integer) getTopbarTitle();

				if (title != 0) {
					viewTitle.setText(title);
				}
			} else if (getTopbarTitle() instanceof String) {
				String title = (String) getTopbarTitle();
				viewTitle.setText(title);
			}
		}

		// 添加左侧控件（默认是返回按钮，但是支持自定义替换此按钮）
		addViewToLeftFunctionZone();
		// 添加右侧控件
		addViewToRightFunctionZone();
	}

	/**
	 * 重新设置title
	 * 
	 * @param resId
	 */
	protected void resetTopbarTitle(int resId) {
		// 设置title
		viewTitle.setText(resId);
	}

	/**
	 * 重新设置title
	 * 
	 * @param text
	 */
	public void resetTopbarTitle(String text) {
		// 设置title
		viewTitle.setText(text);
	}

	@Override
	public void onClick(View v) {
		BaseTopFunc topFunc = funcMap.get(v.getId());

		if (topFunc != null) {
			topFunc.onclick(v);
		}
	}

	/**
	 * 在中间放置自定义的控件
	 * 
	 */
	private void addCustomViewToMiddleFunctionZone() {
		Class<?> customFunc = getTopbarMiddleFunc();
		View funcView = getFuncView(getLayoutInflater(), customFunc);

		if (funcView != null) {
			// 点击事件
			funcView.setOnClickListener(this);
			// 加入页面
			middlerFuncZone.addView(funcView);

			// 设置列表显示
			middlerFuncZone.setVisibility(View.VISIBLE);
		} else {
			middlerFuncZone.setVisibility(View.GONE);
		}
	}

	/**
	 * 
	 * 将功能控件添加到左侧功能区域
	 * 
	 */
	private void addViewToLeftFunctionZone() {
		Class<?> customFunc = (Class<?>) getTopbarLeftFunc();
		if (customFunc == null) {
			return;
		}

		View funcView = getFuncView(getLayoutInflater(), customFunc);

		if (funcView != null) {
			// 点击事件
			funcView.setOnClickListener(this);
			// 加入页面
			leftFuncZone.addView(funcView);

			// 设置列表显示
			leftFuncZone.setVisibility(View.VISIBLE);
		} else {
			leftFuncZone.setVisibility(View.GONE);
		}
	}

	/**
	 * 
	 * 将功能控件添加到右侧功能区域
	 * 
	 * @param views
	 */
	private void addViewToRightFunctionZone() {
		Class<?>[] customFuncs = getTopbarRightFuncArray();

		// 功能列表为空,隐藏区域
		if (customFuncs == null || customFuncs.length == 0) {
			rightFuncZone.setVisibility(View.GONE);
			return;
		}
		// 初始化功能
		for (Class<?> customFunc : customFuncs) {
			// view
			View funcView = getFuncView(getLayoutInflater(), customFunc);
			if (funcView != null) {
				// 点击事件
				funcView.setOnClickListener(this);
				// 加入页面
				rightFuncZone.addView(funcView);
			}
		}
		// 设置列表显示
		rightFuncZone.setVisibility(View.VISIBLE);
	}

	/**
	 * 获得功能View
	 * 
	 * @param inflater
	 * @param func
	 * @return
	 */
	private View getFuncView(LayoutInflater inflater, Class<?> funcClazz) {
		BaseTopFunc func = BaseTopFunc.newInstance(funcClazz, this);

		if (func == null) {
			return null;
		}

		funcMap.put(func.getFuncId(), func);

		// view
		View funcView = func.initFuncView(inflater);
		return funcView;
	}

	/**
	 * 
	 * 更新topbar控件，暂时全部更新，以后考虑指定更新
	 * 
	 */
	public void refreshFuncView() {
		for (Map.Entry<Integer, BaseTopFunc> entity : funcMap.entrySet()) {
			BaseTopFunc topFunc = entity.getValue();
			topFunc.reView();
		}
	}

}
