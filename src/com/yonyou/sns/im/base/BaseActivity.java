package com.yonyou.sns.im.base;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.util.inject.Injector;

/**
 * Activity基类
 *
 * @author litfb
 * @date 2014年9月10日
 * @version 1.0
 */
public abstract class BaseActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		AppManager.getInstance().addActivity(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		YYIMChatManager.getInstance().activityResumed();
	}

	@Override
	public void finish() {
		super.finish();
		AppManager.getInstance().removeActivity(this);
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		// view必须在视图记载之后添加注入
		Injector.getInstance().injectView(this);
		afterSetContentView();
	}

	@Override
	public void setContentView(View view, LayoutParams params) {
		super.setContentView(view, params);
		// view必须在视图记载之后添加注入
		Injector.getInstance().injectView(this);
		afterSetContentView();
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(view);
		// view必须在视图记载之后添加注入
		Injector.getInstance().injectView(this);
		afterSetContentView();
	}

	protected void afterSetContentView() {

	}

	/**
	 * 根据resId获得一个Bitmap
	 * 
	 * @param resId
	 * @return
	 */
	protected Bitmap getDrawableBitmap(int resId) {
		Drawable drawable = getResources().getDrawable(resId);
		if (drawable instanceof BitmapDrawable) {
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			return bitmapDrawable.getBitmap();
		} else {
			return null;
		}
	}

}
