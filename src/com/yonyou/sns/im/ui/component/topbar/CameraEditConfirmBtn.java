package com.yonyou.sns.im.ui.component.topbar;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.CameraEditActivity;

/**
 * 照相编辑topbar 右侧确认按钮
 * 
 * @author wudl
 * 
 */
public class CameraEditConfirmBtn extends BaseTopTextViewFunc {

	public CameraEditConfirmBtn(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.topbar_camera_edit_confirm;
	}

	@Override
	public void onclick(View v) {
		((CameraEditActivity) getActivity()).onConfirm();
	}

	@Override
	public View initFuncView(LayoutInflater inflater) {
		// 获得layout
		View funcView = super.initFuncView(inflater);
		getTextView().setBackgroundResource(R.drawable.common_btn_green);
		return funcView;
	}

	@Override
	protected int getFuncTextRes() {
		// 设置文本文字
		return R.string.camera_edit_confrim;
	}

}
