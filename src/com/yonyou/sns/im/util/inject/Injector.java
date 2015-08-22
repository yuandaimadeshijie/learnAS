package com.yonyou.sns.im.util.inject;

import java.lang.reflect.Field;

import com.yonyou.sns.im.log.YMLogger;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

public class Injector {

	private static Injector instance = new Injector();

	private Injector() {

	}

	public synchronized static Injector getInstance() {
		return instance;
	}

	private void injectView(Activity activity, Field field) {
		if (field.isAnnotationPresent(InjectView.class)) {
			InjectView viewInject = field.getAnnotation(InjectView.class);
			int viewId = viewInject.id();
			try {
				field.setAccessible(true);
				field.set(activity, activity.findViewById(viewId));
			} catch (Exception e) {
				YMLogger.d(e);
			}
		}
	}

	private void injectView(Fragment fragment, View view, Field field) {
		if (field.isAnnotationPresent(InjectView.class)) {
			InjectView viewInject = field.getAnnotation(InjectView.class);
			int viewId = viewInject.id();
			try {
				field.setAccessible(true);
				field.set(fragment, view.findViewById(viewId));
			} catch (Exception e) {
				YMLogger.d(e);
			}
		}
	}

	public void injectView(Activity activity) {
		Field[] fields = activity.getClass().getDeclaredFields();
		if (fields != null && fields.length > 0) {
			for (Field field : fields) {
				if (field.isAnnotationPresent(InjectView.class)) {
					injectView(activity, field);
				}
			}
		}
	}

	public <T extends Activity> void injectView(T activity, Class<T> clazz) {
		Field[] fields = clazz.getDeclaredFields();
		if (fields != null && fields.length > 0) {
			for (Field field : fields) {
				if (field.isAnnotationPresent(InjectView.class)) {
					injectView(activity, field);
				}
			}
		}
	}

	public void injectView(Fragment fragment, View view) {
		Field[] fields = fragment.getClass().getDeclaredFields();
		if (fields != null && fields.length > 0) {
			for (Field field : fields) {
				if (field.isAnnotationPresent(InjectView.class)) {
					injectView(fragment, view, field);
				}
			}
		}
	}

}
