package com.yonyou.sns.im.ui.component.topbar;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.AlbumPhotoActivity;

public class AlbumPhotoConfirmFunc extends BaseTopTextViewFunc {

	public AlbumPhotoConfirmFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.topbar_album_confirm;
	}

	@Override
	public void onclick(View v) {
		((AlbumPhotoActivity) getActivity()).onConfirm();
	}

	@Override
	public View initFuncView(LayoutInflater inflater) {
		View view = super.initFuncView(inflater);
		getTextView().setEnabled(false);
		return view;
	}

	@Override
	public void reView() {
		int size = ((AlbumPhotoActivity) getActivity()).getSelectPhotoCount();
		if (size > 0) {
			getTextView().setText(getActivity().getResources().getString(R.string.send) + "(" + size + "/9)");
			getFuncView().setEnabled(true);
			getTextView().setEnabled(true);
		} else {
			getTextView().setText(getActivity().getResources().getString(R.string.send));
			getFuncView().setEnabled(false);
			getTextView().setEnabled(false);
		}
	}

}
