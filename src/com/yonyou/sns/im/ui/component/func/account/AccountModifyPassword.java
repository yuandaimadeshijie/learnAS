package com.yonyou.sns.im.ui.component.func.account;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.core.YYIMSessionManager;
import com.yonyou.sns.im.exception.YMException;
import com.yonyou.sns.im.log.YMLogger;
import com.yonyou.sns.im.ui.component.func.BaseFunc;
import com.yonyou.sns.im.ui.widget.CustomDialog;
import com.yonyou.sns.im.ui.widget.CustomDialog.Builder;
import com.yonyou.sns.im.util.common.ToastUtil;

/**
 * 修改密码
 * @author wudl
 *
 */
public class AccountModifyPassword extends BaseFunc {

	/** 旧密码输入框*/
	private EditText oldPassWord;
	/** 新密码输入框*/
	private EditText newPassWord;
	/** 确认密码输入框*/
	private EditText rePassWord;

	public AccountModifyPassword(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.account_modify_password;
	}

	@Override
	public int getFuncIcon() {
		return 0;
	}

	@Override
	public int getFuncName() {
		return R.string.account_modify_password;
	}

	@Override
	public void onclick() {
		LayoutInflater inflater = (LayoutInflater) getActivity().getLayoutInflater();
		View contenView = inflater.inflate(R.layout.common_alert_dialog_content_password, null);
		oldPassWord = (EditText) contenView.findViewById(R.id.oldPassWord);
		newPassWord = (EditText) contenView.findViewById(R.id.newPassWord);
		rePassWord = (EditText) contenView.findViewById(R.id.rePassWord);
		// 重命名弹出框
		Builder builder = new CustomDialog.Builder(getActivity());
		builder.setTitle(R.string.alert_dialog_me_title);
		// 设置内容视图
		builder.setView(contenView);
		// 确定按钮
		builder.setPositiveButton(android.R.string.ok, ok);
		builder.setNegativeButton(android.R.string.cancel, cancel);
		// 创建弹出框并显示
		builder.create().show();
	}

	/**
	 * 点击ok事件
	 * 
	 * @param activity
	 */
	DialogInterface.OnClickListener ok = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// 当前用户账号密码
			String password = YYIMSessionManager.getInstance().getPassword();
			// 输入框密码
			String oldPassword = AccountModifyPassword.this.oldPassWord.getText().toString();
			String newPasswrd = AccountModifyPassword.this.newPassWord.getText().toString();
			String rePassword = AccountModifyPassword.this.rePassWord.getText().toString();

			// 判断密码是否正确
			if (!oldPassword.equals(password)) {
				ToastUtil.showShort(getActivity(), R.string.account_modify_password_error);
			}

			// 判断输入密码书否一致
			if (!newPasswrd.equals(rePassword)) {
				ToastUtil.showShort(getActivity(), R.string.account_modify_password_confirm_error);
			}

			try {
				YYIMSessionManager.getInstance().changePasswordXMPP(newPasswrd);
				// 提示修改成功
				ToastUtil.showShort(getActivity(), R.string.account_modify_password_success);
			} catch (YMException e) {
				YMLogger.d(e);
				ToastUtil.showShort(getActivity(), R.string.account_modify_password_faild);
			}
		}
	};
	/**
	 * 点击取消事件
	 * 
	 * @param activity
	 */
	DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface paramDialogInterface, int paramInt) {

		}
	};

}
