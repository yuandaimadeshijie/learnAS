package com.yonyou.sns.im.ui.component.func.account;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.core.YYIMSessionManager;
import com.yonyou.sns.im.ui.component.func.BaseFunc;

/**
 * 账号
 *
 * @author litfb
 * @date 2014年10月31日
 * @version 1.0
 */
public class AccountJidFunc extends BaseFunc {

	/** TextView */
	private TextView textView;

	public AccountJidFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.account_func_jid;
	}

	@Override
	public int getFuncIcon() {
		return 0;
	}

	@Override
	public int getFuncName() {
		return R.string.account_jid;
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
		// 取账号
		String account = YYIMSessionManager.getInstance().getAccount();
		// 设账号
		textView.setText(account);
	}

}
