package com.yonyou.sns.im.activity;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Hashtable;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.log.YMLogger;
import com.yonyou.sns.im.ui.component.func.BaseFunc;
import com.yonyou.sns.im.ui.component.func.about.AboutVersionFunc;
import com.yonyou.sns.im.ui.widget.CustomDialog;
import com.yonyou.sns.im.ui.widget.CustomDialog.Builder;
import com.yonyou.sns.im.util.DialogUtil;
import com.yonyou.sns.im.util.IMConfigUtil;
import com.yonyou.sns.im.util.YMCheckAppTask;
import com.yonyou.sns.im.util.common.ToastUtil;
import com.yonyou.sns.im.util.common.YMStorageUtil;
import com.yonyou.sns.im.util.inject.InjectView;
import com.yonyou.sns.im.util.message.MessageResDownloadTask;

/**
 * 关于
 * 
 * @author litfb
 * @modifier wudl
 * @date 2014年10月10日
 * @version 1.0
 */
public class AboutActivity extends SimpleTopbarActivity implements OnClickListener {

	/** 下载类型 */
	public static final int APP_DOWNLOAD = 512;
	/** 显示已是最新版本 */
	public static final int IS_ALREADY_NEW = 0;
	/** 下载文件失败 */
	public static final int FAILED_DOWNLOAD = 1;
	/** 显示安装apk dialog */
	public static final int SHOW_INIT_DIALOG = 2;
	/** 系统功能列表 */
	private static Class<?> aboutFuncArray[] = { AboutVersionFunc.class };

	/** 功能对象 */
	private Hashtable<Integer, BaseFunc> htFunc = new Hashtable<Integer, BaseFunc>();

	/** 版本号 */
	@InjectView(id = R.id.about_version)
	private TextView aboutVersion;
	/** 功能View */
	@InjectView(id = R.id.about_func_view)
	private LinearLayout aboutFuncView;
	/** 功能列表 */
	@InjectView(id = R.id.about_func_list)
	private LinearLayout aboutFuncList;
	/** 下载中dialog*/
	private Dialog downloadDialog;
	/** handler */
	private final Handler handler = new AboutHandler(this);

	@Override
	protected Object getTopbarTitle() {
		return R.string.setting_about;
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_about);
		// 版本号
		aboutVersion.setText(getVersionName());
		// 初始化功能
		initAboutFunc();
		// 初始化下载dialog
		downloadDialog = DialogUtil.getProgressDialog(AboutActivity.this, R.string.about_download_dialog_text);
	}

	/**
	 * 检查app版本
	 */
	public void checkAppVersion() {
		PackageManager pm = getPackageManager();
		try {
			// 获取版本号
			PackageInfo p = pm.getPackageInfo(getPackageName(), 0);
			int version = p.versionCode;
			// 启动异步任务
			UpdateAppTask task = new UpdateAppTask();
			task.execute(version);
		} catch (NameNotFoundException e) {
			YMLogger.d(e);
		}
	}

	/**
	 * 获得版本号
	 * 
	 * @return
	 */
	private String getVersionName() {
		try {
			PackageManager packageManager = getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
			return packageInfo.versionName;
		} catch (NameNotFoundException e) {
			YMLogger.d(e);
		}
		return null;
	}

	/**
	 * 获得func列表
	 * 
	 * @return
	 */
	protected Class<?>[] getAboutFuncArray() {
		return aboutFuncArray;
	}

	/**
	 * 初始化功能
	 * 
	 * @param view
	 */
	protected void initAboutFunc() {
		Class<?>[] customFuncs = getAboutFuncArray();
		// 功能列表为空,隐藏区域
		if (customFuncs == null || customFuncs.length == 0) {
			aboutFuncView.setVisibility(View.GONE);
			return;
		}
		// 初始化功能
		boolean isSeparator = false;
		for (Class<?> customFunc : customFuncs) {
			// view
			View funcView = getFuncView(getLayoutInflater(), customFunc, isSeparator);
			if (funcView != null) {
				// 点击事件
				funcView.setOnClickListener(this);
				// 加入页面
				aboutFuncList.addView(funcView);
				isSeparator = true;
			}
		}
		// 设置列表显示
		aboutFuncList.setVisibility(View.VISIBLE);
	}

	/**
	 * 获得功能View
	 * 
	 * @param inflater
	 * @param func
	 * @return
	 */
	private View getFuncView(LayoutInflater inflater, Class<?> funcClazz, boolean isSeparator) {
		BaseFunc func = BaseFunc.newInstance(funcClazz, this);
		if (func == null) {
			return null;
		}
		htFunc.put(func.getFuncId(), func);
		// view
		View funcView = func.initFuncView(isSeparator);
		return funcView;
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		// func
		BaseFunc func = htFunc.get(v.getId());

		// 处理点击事件
		if (func != null) {
			func.onclick();
		}
	}

	/**
	 * 显示确认dialog
	 * 
	 * @param message
	 */
	private void showConfirmDialog(final ApkEntity entity) {
		Builder builder = new CustomDialog.Builder(this);
		builder.setTitle(R.string.about_dialog_title);
		if (entity.isRequire_update()) {
			builder.setMessage(R.string.about_dialog_must_update_message);
		} else {
			builder.setMessage(R.string.about_dialog_must_update_message);
		}
		builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 启动apk异步下载
				DownApkTask apkTask = new DownApkTask();
				apkTask.execute(entity.getPath());
				// 启动progress bar
				if (downloadDialog != null && !downloadDialog.isShowing()) {
					downloadDialog.show();
				}
			}
		});
		builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		builder.create().show();
	}

	/**
	 * 下载安装apk任务
	 * @author wudl
	 *
	 */
	class DownApkTask extends AsyncTask<Object, Integer, Void> {

		@Override
		protected void onProgressUpdate(Integer... values) {
		}

		@Override
		protected Void doInBackground(Object... paramVarArgs) {
			if (paramVarArgs[0] instanceof String) {
				String path = (String) paramVarArgs[0];
				// 下载apk
				String url = IMConfigUtil.getDownLoadApkFullPath(path);
				MessageResDownloadTask fileTask = new MessageResDownloadTask();
				if (fileTask.syncDownLoad(AboutActivity.this, url, "apk", YMStorageUtil.STORAGE_TYPE_APP)) {
					// 下载成功，安装apk并自动打开
					installApk(fileTask.getFilePath());
				} else {
					// 下载文件失败
					handler.sendEmptyMessage(FAILED_DOWNLOAD);
				}
				if (downloadDialog != null && downloadDialog.isShowing()) {
					downloadDialog.dismiss();
				}
			}
			return null;
		}
	}

	/**
	 * app更新
	 * 
	 * @author wudl
	 * 
	 */
	class UpdateAppTask extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			int version = (int) params[0];
			// 下载文件
			String path = null;
			// 最新版本
			int newVersion;
			// 是否必须更新
			boolean require_update;
			try {
				YMCheckAppTask task = new YMCheckAppTask();
				// 下载
				task.CheckAppSync();
				// 获取文件本地路径
				path = task.getPath();
				newVersion = task.getVersion();
				require_update = task.getRequire_update();
			} catch (Exception e) {
				YMLogger.e("资源下载失败!");
				return null;
			}
			if (newVersion > version) {
				// 有新版本就提示更新
				ApkEntity entity = new ApkEntity(require_update, path);
				handler.obtainMessage(SHOW_INIT_DIALOG, entity).sendToTarget();
			} else {
				// 已是最新app
				handler.sendEmptyMessage(IS_ALREADY_NEW);
			}
			return null;
		}
	}

	/**
	 * 安装apk
	 * 
	 * @param url
	 */
	private void installApk(String path) {
		File apkfile = new File(path);
		if (!apkfile.exists()) {
			return;
		}
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
		AboutActivity.this.startActivity(i);
	}

	private class ApkEntity {

		private boolean require_update;
		private String path;

		public ApkEntity(boolean require_update, String path) {
			super();
			this.require_update = require_update;
			this.path = path;
		}

		public boolean isRequire_update() {
			return require_update;
		}

		public String getPath() {
			return path;
		}
	}

	private static class AboutHandler extends Handler {

		private WeakReference<AboutActivity> refActivity;

		public AboutHandler(AboutActivity activity) {
			refActivity = new WeakReference<AboutActivity>(activity);
		}

		private AboutActivity getActivity() {
			return refActivity.get();
		}

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case IS_ALREADY_NEW:
				ToastUtil.showLong(getActivity(), "已经是最新版本");
				break;
			case FAILED_DOWNLOAD:
				ToastUtil.showLong(getActivity(), "apk下载失败");
				break;
			case SHOW_INIT_DIALOG:
				if (msg.obj instanceof ApkEntity) {
					ApkEntity entity = (ApkEntity) msg.obj;
					getActivity().showConfirmDialog(entity);
				}
				break;
			}
		};

	}

}
