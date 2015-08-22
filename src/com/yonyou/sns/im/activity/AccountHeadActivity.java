package com.yonyou.sns.im.activity;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.ui.component.topbar.AccountHeadConfirmFunc;
import com.yonyou.sns.im.ui.widget.ZoomImageView;
import com.yonyou.sns.im.util.inject.InjectView;

public class AccountHeadActivity extends SimpleTopbarActivity {

	public static final String EXTRA_IMAGE_PATH = "EXTRA_IMAGE_PATH";

	/** 方框 */
	@InjectView(id = R.id.account_head_square)
	private View viewSquare;
	/** 图像 */
	@InjectView(id = R.id.account_head_zoom)
	private ZoomImageView viewZoom;

	/** 图片路径 */
	private String imagePath;

	@Override
	protected Class<?>[] getTopbarRightFuncArray() {
		return new Class[] { AccountHeadConfirmFunc.class };
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_accounthead);

		// 图片路径
		imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
		// 方框大小
		initSquareView();
		// 设图片
		viewZoom.setImageBitmap(BitmapFactory.decodeFile(imagePath));
	}

	/**
	 * 剪切图片
	 */
	public void cutImage() {

	}

	/**
	 * 方框View
	 * 
	 * @return
	 */
	private void initSquareView() {
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		// 屏幕宽度（像素）
		int size = (int) (metric.widthPixels * 0.85);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
		viewSquare.setLayoutParams(layoutParams);
	}

}
