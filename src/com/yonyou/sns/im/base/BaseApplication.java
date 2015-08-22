package com.yonyou.sns.im.base;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.yonyou.sns.im.activity.ChatActivity;
import com.yonyou.sns.im.core.YYIMChat;
import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.core.YYIMSettings;
import com.yonyou.sns.im.entity.message.YMMessage;
import com.yonyou.sns.im.entity.message.YMMessageNotifyListener;
import com.yonyou.sns.im.log.YMLogger;
import com.yonyou.sns.im.smack.IMSmackManager;
import com.yonyou.sns.im.util.IMMessageUtil;

/**
 * application基类
 *
 * @author litfb
 * @date 2014年9月10日
 * @version 1.0
 */
public class BaseApplication extends Application {

	private static final String TAG = "Application";

	/** 实例 */
	private static BaseApplication instance;

	public synchronized static BaseApplication instance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		YMLogger.d(TAG, "application on create in!");

		// 注册App异常崩溃处理器
		Thread.setDefaultUncaughtExceptionHandler(new BaseExceptionHandler());
		// application
		instance = this;
		// appName
		String appName = getAppName();
		if (TextUtils.isEmpty(appName)) {
			return;
		}
		YMLogger.d(TAG, "application on create!");

		// 初始化yyim
		YYIMChat.getInstance().init(this);

		YMLogger.setLevel(Log.VERBOSE);

		IMSmackManager.getInstance().init();

		// Settings
		YYIMSettings settings = YYIMChatManager.getInstance().getYmSettings();

		// 设置notification消息点击时，跳转的intent为自定义的intent
		settings.setMessageNotifyListener(new YMMessageNotifyListener() {

			@Override
			public String getNotificationTotal(YMMessage message, int userNum, int msgNum) {
				return null;
			}

			@Override
			public String getNotificationTitle(YMMessage message) {
				return null;
			}

			@Override
			public Intent getNotificationResponse(YMMessage message) {
				// 判断是否开启预览模式
				if (YYIMChatManager.getInstance().getYmSettings().isNewmsgPreview()) {
					Intent intent = new Intent(BaseApplication.this, ChatActivity.class);
					intent.putExtra(ChatActivity.EXTRA_ROOM_JID, message.getRoom_jid());
					return intent;
				} else {
					String packetName = BaseApplication.this.getPackageName();
					return BaseApplication.this.getPackageManager().getLaunchIntentForPackage(packetName);
				}

			}

			@Override
			public String getNotificationMessage(YMMessage message) {
				return (String) IMMessageUtil.genChatContent(BaseApplication.this, message.getChatContent(), false);
			}

		});
	}

	/**
	 * 退出应用程序
	 */
	public void exitApp() {
		try {
			AppManager.getInstance().finishAllActivity();
			ActivityManager activityMgr = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			activityMgr.killBackgroundProcesses(getPackageName());
		} catch (Exception e) {
			YMLogger.d(e);
		} finally {
			System.exit(0);
		}
	}

	/**
	 * 重启应用
	 */
	public void restartApp() {
		AppManager.getInstance().finishAllActivity();
		// restart
		Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);

	}

	/**
	 * 回到桌面
	 */
	public void backHome() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
	}

	/**
	 * 退出登录,清空数据
	 */
	public void logout() {
		YYIMChatManager.getInstance().logout();
	}

	private String getAppName() {
		int pid = android.os.Process.myPid();
		String appName = null;
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> processList = activityManager.getRunningAppProcesses();

		PackageManager packageManager = this.getPackageManager();
		for (RunningAppProcessInfo info : processList) {
			try {
				if (info.pid == pid) {
					appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(
							info.processName, PackageManager.GET_META_DATA));
					break;
				}
			} catch (Exception e) {
				YMLogger.d(TAG, e);
			}
		}
		return appName;
	}

}
