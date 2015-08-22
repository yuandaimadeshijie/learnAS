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


public class AccountTitleFunc extends BaseFunc {

	/** TextView */
	private TextView textView;
	/** 初始存的值*/
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

	public AccountTitleFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.account_func_title;
	}

	@Override
	public int getFuncIcon() {
		return 0;
	}

	@Override
	public int getFuncName() {
		return R.string.account_title;
	}

	@Override
	public void onclick() {
		((AccountActivity) getActivity()).showEditDialog(value, ok, cancel);
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
				AccountActivity activity = (AccountActivity) getActivity();
				// YMVCard
				YMVCard vCardEntity = activity.getYMVCard();
				if (vCardEntity == null) {
					return;
				}
				String text = activity.getBuilder().getInput();
				if (!text.equals(value)) {
					// 改变部门属性
					vCardEntity.setTitle(text);
					activity.updateVCard(vCardEntity);
					textView.setText(text);
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
		value = vCardEntity.getTitle()==null?"":vCardEntity.getTitle();
		// 设部门
		textView.setText(value);
	}

}
