//package com.yonyou.sns.im.base;
//
//import java.util.HashSet;
//import java.util.List;
//
//import android.app.ActivityManager;
//import android.app.ActivityManager.RunningTaskInfo;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.media.MediaPlayer;
//import android.os.Binder;
//import android.os.Handler;
//import android.os.IBinder;
//import android.text.TextUtils;
//
//import com.yonyou.sns.base.log.Logger;
//import com.yonyou.sns.im.R;
//import com.yonyou.sns.im.config.YMConfigConstants;
//import com.yonyou.sns.im.smack.SmackService;
//
///**
// * 全局的公共服务
// *
// * @author litfb
// * @date 2014年10月22日
// * @version 1.0
// */
//public class ImBaseService extends BaseService {
//
//	public static final int NEW_MESSAGE_REMIND = 0;
//
//	private IBinder mBinder = new ImBaseBinder();
//
//	private Handler mMainHandler = new Handler();
//	private ActivityManager activityManager;
//
//	private HashSet<String> mIsBoundTo = new HashSet<String>();// 用来保存当前正在聊天对象的数组
//
//	private long lastBellPlay = System.currentTimeMillis();
//
//	private SmackInfoReceiver smackInfoReceiver = new SmackInfoReceiver();
//
//	@Override
//	public IBinder onBind(Intent intent) {
//		YMLogger.i("[SERVICE] onBind");
//		String chatPartner = intent.getDataString();
//		if ((chatPartner != null)) {
//			mIsBoundTo.add(chatPartner);
//		}
//		return mBinder;
//	}
//
//	@Override
//	public void onRebind(Intent intent) {
//		super.onRebind(intent);
//		String chatPartner = intent.getDataString();
//		if ((chatPartner != null)) {
//			mIsBoundTo.add(chatPartner);
//		}
//	}
//
//	@Override
//	public boolean onUnbind(Intent intent) {
//		String chatPartner = intent.getDataString();
//		if ((chatPartner != null)) {
//			mIsBoundTo.remove(chatPartner);
//		}
//		return true;
//	}
//
//	public class ImBaseBinder extends Binder {
//
//		public ImBaseService getService() {
//			return ImBaseService.this;
//		}
//	}
//
//	@Override
//	public void onCreate() {
//		super.onCreate();
//		// 初始化activity管理器
//		activityManager = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
//		// 注册smack提醒监听
//		registerReceiver(smackInfoReceiver, new IntentFilter(SmackService.REMIND_IMBASESERVICE_FILTER));
//	}
//
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//		unregisterReceiver(smackInfoReceiver);// 注销smack提醒监听
//	}
//
//	// 清除通知栏
//	public void clearNotifications(String Jid) {
//		clearNotification(Jid);
//	}
//
//	/**
//	 * 新消息通知
//	 * 
//	 * @param from
//	 * @param name
//	 * @param message
//	 */
//	public void newMessage(final String from, final String name, final String message) {
//		mMainHandler.post(new Runnable() {
//
//			public void run() {
//				if (!BaseApplication.instance().getPreference().getBoolean(YMConfigConstants.NOTIFY_MUTEMODE, false)
//						&& ((System.currentTimeMillis() - lastBellPlay) > 5 * 1000)) {
//					// 新消息的时间间隔大于5秒则重新调用音频
//					lastBellPlay = System.currentTimeMillis();
//					MediaPlayer.create(ImBaseService.this, R.raw.office).start();
//				}
//				if (!isAppOnForeground())
//					notifyClient(from, name, message, !mIsBoundTo.contains(from));
//			}
//
//		});
//	}
//
//	/**
//	 * 判断是否程序为当前界面上运行
//	 * 
//	 * @return
//	 */
//	public boolean isAppOnForeground() {
//		// 在activity栈中取第一个判断包名
//		List<RunningTaskInfo> taskInfos = activityManager.getRunningTasks(1);
//		if (taskInfos.size() > 0 && TextUtils.equals(getPackageName(), taskInfos.get(0).topActivity.getPackageName())) {
//			return true;
//		}
//		return false;
//	}
//
//	private class SmackInfoReceiver extends BroadcastReceiver {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			int token = intent.getIntExtra(SmackService.OPERATION_TOKEN, -1);
//			switch (token) {
//			case NEW_MESSAGE_REMIND:
//				String fromJid = intent.getStringExtra("FROM_JID");
//				String message = intent.getStringExtra("CHAT_MESSAGE");
//				String name = intent.getStringExtra("NAME_OF_JID");
//				newMessage(fromJid, name, message);
//				break;
//			default:
//				break;
//			}
//		}
//
//	}
//
// }
