package com.yonyou.sns.im.ui.component.func.account;

import android.app.Activity;
import android.content.DialogInterface;
import android.widget.LinearLayout;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.AccountActivity;
import com.yonyou.sns.im.entity.YMVCard;
import com.yonyou.sns.im.ui.component.func.BaseFunc;

/**
 * 备注
 * 
 * @author litfb
 * @date 2014年10月16日
 * @version 1.0
 */
public class AccountRemarkFunc extends BaseFunc {

	/** 原始值 */
	private String value;

	/**
	 * 点击ok事件
	 * 
	 * @param activity
	 */
	DialogInterface.OnClickListener ok;

	/**
	 * 点击取消事件
	 * 
	 * @param activity
	 */
	DialogInterface.OnClickListener cancel;

	public AccountRemarkFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.account_func_remark;
	}

	@Override
	public int getFuncIcon() {
		return 0;
	}

	@Override
	public int getFuncName() {
		return R.string.account_remark;
	}

	@Override
	public void onclick() {
		// 获取文本字段
		((AccountActivity) getActivity()).showEditDialog(value, ok, cancel);
	}

	@Override
	protected void initCustomView(LinearLayout customView) {
		// 初始化确定取消事件
		initOk();
		initCancel();
	}

	/**
	 * 初始化ok事件
	 */
	private void initOk() {
		ok = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				AccountActivity activity = (AccountActivity) getActivity();
				// YMVCard
				YMVCard vCardEntity = activity.getYMVCard();
				if (vCardEntity == null) {
					return;
				}
				String text = activity.getBuilder().getInput();
				if (!text.equals(value)) {
					// 改变备注
					vCardEntity.setDesc(text);
					activity.updateVCard(vCardEntity);
				}
			}
		};
	}

	/**
	 * 初始化cancel事件
	 */
	private void initCancel() {
		cancel = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		};
	}

	@Override
	public void bindView() {
		// VCard
		YMVCard vCardEntity = ((AccountActivity) getActivity()).getYMVCard();
		if (vCardEntity == null) {
			return;
		}
		value = vCardEntity.getDesc()==null?"":vCardEntity.getDesc();
	}

}
