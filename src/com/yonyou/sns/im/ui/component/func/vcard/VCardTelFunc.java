package com.yonyou.sns.im.ui.component.func.vcard;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.VCardActivity;
import com.yonyou.sns.im.entity.YMVCard;
import com.yonyou.sns.im.ui.component.func.BaseFunc;

public class VCardTelFunc extends BaseFunc {

	/** TextView */
	private TextView textView;

	public VCardTelFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.vcard_func_tel;
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
		// textView.setText(vCardEntity.getFn());
	}

	@Override
	public boolean hasArrowRight() {
		return false;
	}

}
