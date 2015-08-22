package com.yonyou.sns.im.ui.component.func.vcard;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.VCardActivity;
import com.yonyou.sns.im.entity.YMVCard;
import com.yonyou.sns.im.ui.component.func.BaseFunc;
import com.yonyou.sns.im.ui.widget.CustomDialog;
import com.yonyou.sns.im.ui.widget.CustomDialog.Builder;

public class VCardDeleteFunc extends BaseFunc {

	/** TextView */
	private TextView textView;

	public VCardDeleteFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.vcard_func_delete;
	}

	@Override
	public int getFuncIcon() {
		return 0;
	}

	@Override
	public int getFuncName() {
		return R.string.account_delete;
	}

	@Override
	public void onclick() {
		// 重命名弹出框
		Builder builder = new CustomDialog.Builder(getActivity());
		builder.setTitle(R.string.vcard_delete_title);
		// 设置内容
		builder.setMessage(R.string.vcard_delete_message);
		// 确定按钮
		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				((VCardActivity) getActivity()).removeRoster();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		// 创建弹出框并显示
		builder.create().show();

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