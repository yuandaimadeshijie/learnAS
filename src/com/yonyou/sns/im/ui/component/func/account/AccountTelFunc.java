package com.yonyou.sns.im.ui.component.func.account;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.AccountActivity;
import com.yonyou.sns.im.entity.YMVCard;
import com.yonyou.sns.im.ui.component.func.BaseFunc;

public class AccountTelFunc extends BaseFunc {

	/** TextView */
	private TextView textView;

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

	public AccountTelFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.account_func_tel;
	}

	@Override
	public int getFuncIcon() {
		return 0;
	}

	@Override
	public int getFuncName() {
		return R.string.account_tel;
	}

	@Override
	public void onclick() {
		// 获取文本字段
		String text = textView.getText().toString();
		((AccountActivity) getActivity()).showEditDialog(text, ok, cancel);
	}

	@Override
	protected void initCustomView(LinearLayout customView) {
		// TextView
		textView = (TextView) getActivity().getLayoutInflater().inflate(R.layout.textview_account_func, null);
		// 添加到customView
		customView.addView(textView);
		customView.setGravity(Gravity.RIGHT);
		customView.setVisibility(View.VISIBLE);
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
		// 设Name
		// textView.setText(vCardEntity.getFn());
	}

}
