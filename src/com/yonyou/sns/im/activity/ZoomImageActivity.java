package com.yonyou.sns.im.activity;

import java.io.File;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.ui.widget.ZoomImageView;
import com.yonyou.sns.im.util.common.LocalBigImageUtil;
import com.yonyou.sns.im.util.common.ToastUtil;

public class ZoomImageActivity extends SimpleTopbarActivity {

	/** ZoomImageView*/
	private ZoomImageView zoomImageView;
	/** 图片下载路径*/
	private String path;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_zoomimage);

		zoomImageView = (ZoomImageView) findViewById(R.id.zoom_image_view);
		path = getIntent().getStringExtra("path");

		DisplayMetrics dm = new DisplayMetrics();

		getWindowManager().getDefaultDisplay().getMetrics(dm);

		int mScreenW = dm.widthPixels; // 得到宽度
		int mScreenH = dm.heightPixels; // 得到高度

		File file = new File(path);

		if (file.exists()) {
			Bitmap imgbitmap = LocalBigImageUtil.getBitmapFromFile(path, mScreenW, mScreenH);
			zoomImageView.setImageBitmap(imgbitmap);
		} else {
			ToastUtil.showLong(this, "原图已经不存在");
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			finish();
			break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected Object getTopbarTitle() {
		return R.string.album_view_photo;
	}

}
