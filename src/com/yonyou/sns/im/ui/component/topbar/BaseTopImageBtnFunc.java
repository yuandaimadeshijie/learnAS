package com.yonyou.sns.im.ui.component.topbar;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.yonyou.sns.im.R;

/**
 * topbar图标按钮
 *
 * @author litfb
 * @date 2014年10月20日
 * @version 1.0
 */
public abstract class BaseTopImageBtnFunc extends BaseTopFunc {

	private View funcView;

	private ImageView imageView;

	public BaseTopImageBtnFunc(Activity activity) {
		super(activity);
	}

	/** 功能图标 */
	public int getFuncIcon() {
		return 0;
	}

	@Override
	public View initFuncView(LayoutInflater inflater) {
		funcView = inflater.inflate(R.layout.button_topbar_image, null);
		// 获得func id
		funcView.setId(getFuncId());
		// 获得func icon
		imageView = (ImageView) funcView.findViewById(R.id.topbar_func_icon);

		if (getFuncIcon() > 0) {
			imageView.setImageResource(getFuncIcon());
		} else {
			imageView.setVisibility(View.GONE);
		}
		return funcView;
	}

	protected ImageView getImageView() {
		return imageView;
	}

	protected View getFuncView() {
		return funcView;
	}

}
