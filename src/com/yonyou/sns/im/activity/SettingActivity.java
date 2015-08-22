package com.yonyou.sns.im.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.base.BaseApplication;
import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.core.YYIMSessionManager;
import com.yonyou.sns.im.util.inject.InjectView;

/**
 * 设置页
 *
 * @author litfb
 * @date 2014年10月8日
 * @version 1.0
 */
public class SettingActivity extends SimpleTopbarActivity implements OnClickListener, OnCheckedChangeListener {

	/** 静音模式 */
	@InjectView(id = R.id.setting_mutemode)
	private Switch switchMutemode;
	/** 振动 */
	@InjectView(id = R.id.setting_vibration)
	private Switch switchVibration;
	/** 新消息提示 */
	@InjectView(id = R.id.setting_remind)
	private Switch switchRemind;
	/** 新消息预览 */
	@InjectView(id = R.id.setting_preview)
	private Switch switchPreview;
	/** 关于 */
	@InjectView(id = R.id.setting_about)
	private View viewAbout;
	/** 退出 */
	@InjectView(id = R.id.setting_exit)
	private Button btnExit;

	/** 退出dialog */
	private Dialog dlgExit;
	/** 退出菜单 */
	private View viewExit;
	/** 关闭 */
	private View viewClose;
	/** 退出 */
	private View viewSignout;

	@Override
	protected Object getTopbarTitle() {
		return R.string.me_func_setting;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		// 设置switch状态
		setSwitchState();
		// 添加switch监听
		switchMutemode.setOnCheckedChangeListener(this);
		switchVibration.setOnCheckedChangeListener(this);
		switchRemind.setOnCheckedChangeListener(this);
		switchPreview.setOnCheckedChangeListener(this);
		// 初始化退出菜单
		viewExit = LayoutInflater.from(this).inflate(R.layout.view_setting_exit, null);
		viewClose = viewExit.findViewById(R.id.setting_exit_close);
		viewSignout = viewExit.findViewById(R.id.setting_exit_signout);
		// 添加按钮监听
		// viewFeedback.setOnClickListener(this);
		viewAbout.setOnClickListener(this);
		btnExit.setOnClickListener(this);
		viewClose.setOnClickListener(this);
		viewSignout.setOnClickListener(this);
	}

	/**
	 * 退出对话框
	 * 
	 * @return
	 */
	protected Dialog getExitDialog() {
		if (dlgExit == null) {
			dlgExit = new Dialog(this, R.style.DialogStyle);
			dlgExit.setContentView(viewExit);
		}
		return dlgExit;
	}

	/**
	 * 关闭对话框
	 */
	private void closeExitDialog() {
		if (dlgExit != null && dlgExit.isShowing()) {
			dlgExit.cancel();
		}
	}

	/**
	 * 设置switch状态
	 */
	private void setSwitchState() {
		switchMutemode.setChecked(YYIMChatManager.getInstance().getYmSettings().isNewmsgMutemode());
		switchVibration.setChecked(YYIMChatManager.getInstance().getYmSettings().isNewmsgVibration());
		switchRemind.setChecked(YYIMChatManager.getInstance().getYmSettings().isNewmsgRemind());
		switchPreview.setChecked(YYIMChatManager.getInstance().getYmSettings().isNewmsgPreview());
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.setting_about:
			startActivity(new Intent(this, AboutActivity.class));
			break;
		case R.id.setting_exit:
			getExitDialog().show();
			break;
		case R.id.setting_exit_close:
			// 关闭对话框
			closeExitDialog();
			// 停止服务
			YYIMChatManager.getInstance().logout();
			// 退出
			BaseApplication.instance().exitApp();
			break;
		case R.id.setting_exit_signout:
			// 关闭对话框
			closeExitDialog();
			// 清空账号
			YYIMSessionManager.getInstance().clearSession();
			// 停止服务
			YYIMChatManager.getInstance().logout();
			// 重启动
			BaseApplication.instance().restartApp();
			break;
		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.setting_mutemode:
			YYIMChatManager.getInstance().getYmSettings().setNewmsgMutemode(isChecked);
			break;
		case R.id.setting_vibration:
			YYIMChatManager.getInstance().getYmSettings().setNewmsgVibration(isChecked);
			break;
		case R.id.setting_remind:
			YYIMChatManager.getInstance().getYmSettings().setNewmsgRemind(isChecked);
			break;
		case R.id.setting_preview:
			YYIMChatManager.getInstance().getYmSettings().setNewmsgPreview(isChecked);
			break;
		default:
			break;
		}
	}

}
