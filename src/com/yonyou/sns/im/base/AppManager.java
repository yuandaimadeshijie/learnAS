package com.yonyou.sns.im.base;

import java.util.Stack;

import android.app.Activity;

import com.yonyou.sns.im.log.YMLogger;

/**
 * 应用管理器
 *
 * @author litfb
 * @date 2014年9月9日
 * @version 1.0
 */
public class AppManager {

	/** activity栈 */
	private static Stack<Activity> activityStack = new Stack<Activity>();
	/** 单实例 */
	private static AppManager instance = new AppManager();

	private AppManager() {

	}

	/**
	 * 单实例
	 */
	public synchronized static AppManager getInstance() {
		return instance;
	}

	/**
	 * 添加Activity到堆栈
	 */
	public void addActivity(Activity activity) {
		activityStack.add(activity);
	}

	/**
	 * 获取当前Activity（堆栈中最后一个压入的）
	 */
	public Activity currentActivity() {
		Activity activity = activityStack.lastElement();
		return activity;
	}

	/**
	 * 结束当前Activity（堆栈中最后一个压入的）
	 */
	public void finishActivity() {
		Activity activity = activityStack.lastElement();
		finishActivity(activity);
	}

	/**
	 * 结束指定的Activity
	 */
	public void finishActivity(Activity activity) {
		if (activity != null) {
			activityStack.remove(activity);
			activity.finish();
			activity = null;
		}
	}

	/**
	 * 移除指定的Activity
	 */
	public void removeActivity(Activity activity) {
		if (activity != null) {
			activityStack.remove(activity);
			activity = null;
		}
	}

	/**
	 * 结束指定类名的Activity
	 */
	public void finishActivity(Class<?> cls) {
		for (Activity activity : activityStack) {
			if (activity.getClass().equals(cls)) {
				finishActivity(activity);
			}
		}
	}

	/**
	 * 结束所有Activity
	 */
	public void finishAllActivity() {
		Activity[] activities = activityStack.toArray(new Activity[activityStack.size()]);
		for (Activity activity : activities) {
			try {
				activity.finish();
			} catch (Exception e) {
				YMLogger.d(e);
			}
		}
		activityStack.clear();
	}

}