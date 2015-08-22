package com.yonyou.sns.im.base;

import java.lang.Thread.UncaughtExceptionHandler;

import com.yonyou.sns.im.log.YMLogger;

/**
 * 异常处理器
 *
 * @author litfb
 * @date 2014年9月9日
 * @version 1.0
 */
public class BaseExceptionHandler implements UncaughtExceptionHandler {

	private UncaughtExceptionHandler defaultUEH;

	public BaseExceptionHandler() {
		defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		YMLogger.e(ex);
		if (defaultUEH != null) {
			defaultUEH.uncaughtException(thread, ex);
		}
	}

}