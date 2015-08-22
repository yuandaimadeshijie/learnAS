package com.yonyou.sns.im.ui.component.topbar;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.yonyou.sns.im.R;

/**
 * 搜索topbar
 * @author wudl
 *
 */
public class SearchEditMiddleFunc extends BaseTopFunc implements TextWatcher, OnClickListener {

	// 目标视图
	private View viewFunc;
	// 编辑视图
	private EditText editView;
	// 编辑上的删除按钮
	private View editDelete;

	public SearchEditMiddleFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.search_topbar_edit;
	}

	@Override
	public void onclick(View v) {

	}

	@Override
	public View initFuncView(LayoutInflater inflater) {
		viewFunc = inflater.inflate(R.layout.topbar_search_edit, null);
		editView = (EditText) viewFunc.findViewById(R.id.search_result_edit);
		editDelete = viewFunc.findViewById(R.id.search_result_edit_delete);
		// 添加文本监听
		editView.addTextChangedListener(this);
		// 添加删除监听
		editDelete.setOnClickListener(this);
		return viewFunc;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (s.length() > 0) {
			// 输入内容显示删除按钮
			editDelete.setVisibility(View.VISIBLE);
		} else {
			// 没有输入内容隐藏删除按钮
			editDelete.setVisibility(View.GONE);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.search_result_edit_delete:
			// 删除edit内容
			editView.setText("");
			break;

		default:
			break;
		}
	}

	/**
	 * 获取输入框
	 * @return
	 */
	public EditText getEditView() {
		return editView;
	}

	public View getFuncView() {
		return viewFunc;
	}
}
