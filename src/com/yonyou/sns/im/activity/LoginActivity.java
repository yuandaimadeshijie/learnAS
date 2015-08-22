package com.yonyou.sns.im.activity;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.base.BaseActivity;
import com.yonyou.sns.im.config.YMPreferenceConfig;
import com.yonyou.sns.im.core.YYIMCallBack;
import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.core.YYIMSessionManager;
import com.yonyou.sns.im.log.YMLogger;
import com.yonyou.sns.im.util.DialogUtil;
import com.yonyou.sns.im.util.XMPPHelper;
import com.yonyou.sns.im.util.common.MD5Util;
import com.yonyou.sns.im.util.common.ToastUtil;
import com.yonyou.sns.im.util.inject.InjectView;

/**
 * 登录Activity
 *
 * @author litfb
 * @date 2014年9月15日
 * @version 1.0
 */
public class LoginActivity extends BaseActivity implements TextWatcher, OnEditorActionListener, OnClickListener {

	public static final String LOGIN_FROM = "LOGIN_FROM";

	/** cache key 最后试图登录的account,just fro LoginActivity, no any business */
	private static final String FRONT_LAST_LOGIN_ACCOUNT = "FRONT_LAST_LOGIN_ACCOUNT";

	/** 登录失败*/
	private static final int LOGIN_FAILED = -1;
	/** 登陆成功*/
	private static final int LOGIN_SUCCEED = 1;
	/** 登录超时*/
	private static final int LOGIN_OUT_TIME = 0;
	/** 登录按钮*/
	@InjectView(id = R.id.login_button)
	private Button loginBtn;
	/** 账号edit*/
	@InjectView(id = R.id.login_account)
	private EditText accountEt;
	/** 密码edit*/
	@InjectView(id = R.id.login_password)
	private EditText passwordEt;

	/** 账号 */
	private String accountVal;
	/** 密码 */
	private String passwordVal;

	/** 登录中 */
	private Dialog loginDialog;
	/** handler */
	private LoginHandler handler = new LoginHandler(this);

	private static class LoginHandler extends Handler {

		/** 弱引用 */
		WeakReference<LoginActivity> refActivity;

		LoginHandler(LoginActivity activity) {
			this.refActivity = new WeakReference<LoginActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			YMLogger.d("handle message!" + Thread.currentThread().getId());
			LoginActivity activity = refActivity.get();
			switch (msg.what) {
			case LOGIN_OUT_TIME:
				activity.dialogDissmiss();
				// 登录超时
				ToastUtil.showShort(activity, R.string.timeout_try_again);
				break;
			case LOGIN_FAILED:
				activity.dialogDissmiss();
				// 登录失败
				ToastUtil.showShort(activity, R.string.failed_try_again);
				break;
			case LOGIN_SUCCEED:
				YMLogger.d("handle login succeed!" + Thread.currentThread().getId());
				if (ShareActivity.class.getName().equals(activity.getIntent().getStringExtra(LOGIN_FROM))) {
					// 跳转分享
					activity.startActivity(new Intent(activity, ShareActivity.class));
				} else {
					YMLogger.d("start main!" + Thread.currentThread().getId());
					// 跳转主页
					activity.startActivity(new Intent(activity, MainActivity.class));
				}
				YMLogger.d("finish self!" + Thread.currentThread().getId());
				// 结束本页面
				activity.finish();
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		initView();
	}

	/**
	 * 初始化
	 */
	private void initView() {
		// 如果保存过用户名密码,自动显示
		String account = YMPreferenceConfig.getInstance().getString("FRONT_LAST_LOGIN_ACCOUNT", "");
		if (!TextUtils.isEmpty(account)) {
			accountEt.setText(account);
		}
		// text change listener
		accountEt.addTextChangedListener(this);
		passwordEt.addTextChangedListener(this);
		passwordEt.setOnEditorActionListener(this);
		loginBtn.setOnClickListener(this);
		// 登录中dialog
		loginDialog = DialogUtil.getProgressDialog(this, R.string.login_prompt);
	}

	/**
	 * 重置登录按钮状态
	 */
	private void resetButtonState() {
		// 取账号/密码值
		String actVal = accountEt.getText().toString();
		String pwdVal = passwordEt.getText().toString();
		if (TextUtils.isEmpty(actVal) || TextUtils.isEmpty(pwdVal)) {
			loginBtn.setEnabled(false);
		} else {
			if (XMPPHelper.isSimpleAccount(actVal) || XMPPHelper.isMultiTenantAccount(actVal)) {
				loginBtn.setEnabled(true);
				accountEt.setTextColor(getResources().getColor(R.color.login_input));
			} else {
				loginBtn.setEnabled(false);
				accountEt.setTextColor(getResources().getColor(R.color.login_input_error));
			}
		}
	}

	@Override
	protected void onPause() {
		dialogDissmiss();
		super.onPause();
	}

	/**
	 * 登录动作
	 */
	public void doLogin() {
		// 取账号/密码值
		accountVal = accountEt.getText().toString();
		passwordVal = passwordEt.getText().toString();
		// 判空
		if (TextUtils.isEmpty(accountVal)) {
			ToastUtil.showShort(this, R.string.login_account_hint);
			return;
		} else {
			YMPreferenceConfig.getInstance().setString(FRONT_LAST_LOGIN_ACCOUNT, accountVal);
			YYIMSessionManager.getInstance().clearSession();
			if (XMPPHelper.isMultiTenantAccount(accountVal)) {
				int atIndex = accountVal.indexOf("@");
				int dotIndex = accountVal.indexOf(".");
				String appkey = accountVal.substring(atIndex + 1, dotIndex);
				String etpkey = accountVal.substring(dotIndex + 1);
				accountVal = accountVal.substring(0, atIndex);
				YYIMChatManager.getInstance().getYmSettings().setCustomAppkey(appkey);
				YYIMChatManager.getInstance().getYmSettings().setCustomEtpkey(etpkey);
			}
		}
		if (TextUtils.isEmpty(passwordVal)) {
			ToastUtil.showShort(this, R.string.login_password_hint);
			return;
		} else {
			passwordVal = MD5Util.encrypt(passwordVal);
		}
		// 登录中Dialog
		if (loginDialog != null && !loginDialog.isShowing()) {
			loginDialog.show();
		}
		// login
		YMLogger.d("login task execute!" + Thread.currentThread().getId());
		new LoginAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 60000L);
	}

	private class LoginAsyncTask extends AsyncTask<Long, Void, Boolean> {

		private Timer timer;

		@Override
		protected Boolean doInBackground(Long... params) {
			YMLogger.d("login task background!");
			timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					// 超时,取消task
					LoginAsyncTask.this.cancel(true);
					// send 超时 message
					handler.sendEmptyMessage(LOGIN_OUT_TIME);
				}

			}, params[0]);

			// 登录
			YYIMChatManager.getInstance().login(accountVal, passwordVal, new YYIMCallBack() {

				@Override
				public void onSuccess(Object object) {
					YMLogger.d("login task onsuccess!" + Thread.currentThread().getId());
					handler.sendEmptyMessage(LOGIN_SUCCEED);
				}

				@Override
				public void onProgress(int errno, String errmsg) {
				}

				@Override
				public void onError(int errno, String errmsg) {
					YMLogger.d("login task onerror!" + Thread.currentThread().getId());
					handler.sendEmptyMessage(LOGIN_FAILED);
				}
			});
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			timer.cancel();
		}

	}

	/**
	 * 关闭登录中dialog
	 */
	protected void dialogDissmiss() {
		if (loginDialog != null && loginDialog.isShowing()) {
			loginDialog.dismiss();
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE) {
			doLogin();
		}
		return true;
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void afterTextChanged(Editable s) {
		resetButtonState();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.login_button) {
			doLogin();
		}
	}

}
