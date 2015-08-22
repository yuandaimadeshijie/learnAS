package com.yonyou.sns.im.ui.component.func.localfile;

import java.lang.ref.WeakReference;

import android.app.Activity;

import com.yonyou.sns.im.activity.fragment.LocalFileMainFragment;
import com.yonyou.sns.im.ui.component.func.BaseFunc;

/**
 * 本地文档的基类
 * @author wudl
 * @date 2014年12月3日
 * @version V1.0
 */
public abstract class LocalFileFunc extends BaseFunc {

	/** 弱引用*/
	WeakReference<LocalFileMainFragment> reference;

	public LocalFileFunc(Activity activity) {
		super(activity);
	}

	/**
	 * 设置弱引用
	 * @param reference
	 */
	public void setReference(LocalFileMainFragment fragment) {
		this.reference = new WeakReference<LocalFileMainFragment>(fragment);
	}

	/**
	 * 获取fragment
	 * @return
	 */
	public LocalFileMainFragment getFragment() {
		return reference.get();
	}

}
