package com.yonyou.sns.im.ui.component.topbar;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.AlbumPhotoActivity;

public class AlbumChoiceFunc extends BaseTopFunc {

	private TextView textView;

	public AlbumChoiceFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.topbar_album_choice;
	}

	@Override
	public void onclick(View v) {
		((AlbumPhotoActivity) getActivity()).changeAlbum();
	}

	@Override
	public View initFuncView(LayoutInflater inflater) {
		View funcView = null;
		// 获得layout
		funcView = inflater.inflate(R.layout.view_album_choice, null);

		// 获得func id
		funcView.setId(getFuncId());

		// 获得func text
		textView = (TextView) funcView.findViewById(R.id.album_topbar_title);

		return funcView;
	}

	@Override
	public void reView() {
		textView.setText(((AlbumPhotoActivity) getActivity()).getAlbum().getName());
	}
}
