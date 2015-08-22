package com.yonyou.sns.im.ui.component.func.vcard;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.VCardActivity;
import com.yonyou.sns.im.entity.YMVCard;
import com.yonyou.sns.im.ui.component.func.BaseFunc;

/**
 * 个人信息-姓名
 *
 * @author litfb
 * @date 2014年10月16日
 * @version 1.0
 */
public class VCardNameFunc extends BaseFunc {

	/** TextView */
	private TextView textView;

	public VCardNameFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.vcard_func_name;
	}

	@Override
	public int getFuncIcon() {
		return 0;
	}

	@Override
	public int getFuncName() {
		return R.string.account_name;
	}

	@Override
	public void onclick() {

	}

	@Override
	protected void initCustomView(LinearLayout customView) {
		// TextView
		textView = (TextView) getActivity().getLayoutInflater().inflate(R.layout.textview_vcard_func, null);
		// 添加到customView
		customView.addView(textView);
		customView.setVisibility(View.VISIBLE);
	}

	@Override
	public void bindView() {
		// VCard
		YMVCard vCardEntity = ((VCardActivity) getActivity()).getYMVCard();
		if (vCardEntity == null) {
			return;
		}
		// 设Email
		textView.setText(vCardEntity.getFn());
	}

	@Override
	public boolean hasArrowRight() {
		return false;
	}

}
