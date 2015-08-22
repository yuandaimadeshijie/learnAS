package com.yonyou.sns.im.ui.widget;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.fragment.BaseFragment;
import com.yonyou.sns.im.util.inject.InjectView;

/**
 * 引导页
 *
 * @author litfb
 * @date 2014年9月16日
 * @version 1.0
 */
public class GuideFragment extends BaseFragment {

	public static final String TAG = "GuideFragment";

	@InjectView(id = R.id.guide_pager)
	private ViewPager viewPager;
	@InjectView(id = R.id.guide_dots)
	private ViewGroup viewGroup;

	/** 点击事件处理 */
	private OnClickListener onClickListener;

	/** button 立即体验 */
	private View textExperience;
	/** 视图列表 */
	private List<View> listViews;
	/** 导航点 */
	private ImageView[] imageViews;

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_guide;
	}

	@Override
	protected void initView(LayoutInflater inflater, View view) {
		// 动态加载向导页
		listViews = new ArrayList<View>();
		listViews.add(inflater.inflate(R.layout.layout_guide_1, null));
		listViews.add(inflater.inflate(R.layout.layout_guide_2, null));
		listViews.add(inflater.inflate(R.layout.layout_guide_3, null));
		viewPager.setAdapter(new GuidePagerAdapter());
		viewPager.setCurrentItem(0);

		// dots
		imageViews = new ImageView[listViews.size()];
		int margin = inflater.getContext().getResources().getDimensionPixelSize(R.dimen.guide_dot_margin);
		for (int i = 0; i < listViews.size(); i++) {
			ImageView imageView = new ImageView(inflater.getContext());

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			params.setMargins(margin, 0, margin, 0);

			imageView.setLayoutParams(params);

			imageViews[i] = imageView;

			if (i == 0) {
				// 默认选中第一张图片
				imageViews[i].setBackgroundResource(R.drawable.shape_guide_dot_hl);
			} else {
				imageViews[i].setBackgroundResource(R.drawable.shape_guide_dot);
			}
			viewGroup.addView(imageViews[i]);
			imageViews[i].setVisibility(View.VISIBLE);
		}
		viewPager.setOnPageChangeListener(new GuidePageChangeListener());

		View lastView = listViews.get(listViews.size() - 1);
		// 立即体验
		textExperience = lastView.findViewById(R.id.btn_experience);
		textExperience.setOnClickListener(this.onClickListener);
	}

	public void setOnClickListener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}

	class GuidePagerAdapter extends PagerAdapter {

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(listViews.get(arg1));
		}

		@Override
		public void finishUpdate(View arg0) {
		}

		@Override
		public int getCount() {
			return listViews.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(listViews.get(arg1), 0);
			return listViews.get(arg1);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}
	}

	class GuidePageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int arg0) {
			for (int i = 0; i < imageViews.length; i++) {
				if (arg0 == i) {
					imageViews[i].setBackgroundResource(R.drawable.shape_guide_dot_hl);
				} else {
					imageViews[i].setBackgroundResource(R.drawable.shape_guide_dot);
				}
			}
		}

	}

}
