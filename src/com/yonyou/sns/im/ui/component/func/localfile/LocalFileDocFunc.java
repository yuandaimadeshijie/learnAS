package com.yonyou.sns.im.ui.component.func.localfile;

import android.app.Activity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.util.common.YMStringUtils;

/**
 * 本地文档func
 * @author wudl
 * @date 2014年12月1日
 * @version V1.0
 */
public class LocalFileDocFunc extends LocalFileFunc {

	public LocalFileDocFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.local_file_document;
	}

	@Override
	public int getFuncIcon() {
		return R.drawable.local_file_doc;
	}

	@Override
	public int getFuncName() {
		return R.string.local_file_doc;
	}

	@Override
	public void onclick() {
	}

	@Override
	public View initFuncView(boolean isSeparator, Object... params) {
		View funcView = super.initFuncView(isSeparator, params);
		TextView nameTextView = (TextView) funcView.findViewById(R.id.func_name);
		String res = getActivity().getResources().getString(R.string.local_file_doc);
		String key = "(" + getFragment().getDocList().size() + ")";
		SpannableStringBuilder style = YMStringUtils.initStyle(res + " " + key, key, R.color.gray_f);
		style.setSpan(new AbsoluteSizeSpan(15, true), res.length(), (res + " " + key).length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		nameTextView.setText(style);
		return funcView;
	}
}
