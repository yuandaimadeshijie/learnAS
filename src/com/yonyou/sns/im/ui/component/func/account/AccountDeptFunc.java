package com.yonyou.sns.im.ui.component.func.account;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.AccountActivity;
import com.yonyou.sns.im.entity.YMVCard;
import com.yonyou.sns.im.ui.component.func.BaseFunc;

/**
 * 部门
 * 
 * @author litfb
 * @date 2014年10月16日
 * @version 1.0
 */
public class AccountDeptFunc extends BaseFunc {

	/** TextView */
	private TextView textView;
	/** 初始存的值*/
	private String value;

	public AccountDeptFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.account_func_dept;
	}

	@Override
	public int getFuncIcon() {
		return 0;
	}

	@Override
	public int getFuncName() {
		return R.string.account_dept;
	}

	@Override
	public void onclick() {
	}
	
	@Override
	public boolean hasArrowRight() {
		return false;
	}
	
	@Override
	protected void initCustomView(LinearLayout customView) {
		// TextView
		textView = (TextView) getActivity().getLayoutInflater().inflate(R.layout.textview_account_func, null);
		// 添加到customView
		customView.addView(textView);
		customView.setGravity(Gravity.RIGHT);
		customView.setVisibility(View.VISIBLE);
	}


	@Override
	public void bindView() {
		// VCard
		YMVCard vCardEntity = ((AccountActivity) getActivity()).getYMVCard();
		if (vCardEntity == null) {
			return;
		}
		value = vCardEntity.getOrg_unit()==null?"":vCardEntity.getOrg_unit();
		// 设部门
		textView.setText(value);
	}

}
