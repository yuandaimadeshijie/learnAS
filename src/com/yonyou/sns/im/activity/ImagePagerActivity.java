package com.yonyou.sns.im.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.fragment.BaseFragment;
import com.yonyou.sns.im.activity.fragment.LazyImageFragment;
import com.yonyou.sns.im.base.BaseActivity;
import com.yonyou.sns.im.entity.message.YMMessage;
import com.yonyou.sns.im.log.YMLogger;

/**
 * 图片浏览控件
 *
 * @author litfb
 * @date 2014年10月29日
 * @version 1.0
 */
public class ImagePagerActivity extends BaseActivity {

	/** extra-图片消息列表 */
	public static final String EXTRA_IMAGE_CHATS = "EXTRA_IMAGE_CHATS";
	/** extra-当前消息id */
	public static final String EXTRA_IMAGE_CURID = "EXTRA_IMAGE_CURID";

	/** ViewPager */
	private ViewPager viewPager;

	private List<YMMessage> imageChats;

	private Integer curId;

	private List<BaseFragment> fragmentList = new ArrayList<>();

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_imagepager);

		viewPager = (ViewPager) findViewById(R.id.image_pager);

		// 取所有图片消息
		imageChats = (List<YMMessage>) getIntent().getSerializableExtra(EXTRA_IMAGE_CHATS);
		// 当前id
		curId = (Integer) getIntent().getIntExtra(EXTRA_IMAGE_CURID, -1);

		/** 当前选中 */
		int curIndex = -1;
		// 遍历图片消息
		for (int i = 0; i < imageChats.size(); i++) {
			// 当前选中
			YMMessage entity = imageChats.get(i);
			if (curIndex < 0) {
				if (curId.equals(entity.get_id())) {
					curIndex = i;
				}
			}
			// 初始化fragments
			LazyImageFragment fragment = null;
			try {
				// new instance
				fragment = LazyImageFragment.class.newInstance();
				// 参数
				Bundle bundle = new Bundle();
				bundle.putSerializable(LazyImageFragment.BUNDLE_IMAGE_CHAT, entity);
				fragment.setArguments(bundle);
			} catch (Exception e) {
				YMLogger.d(e);
			}
			fragmentList.add(fragment);
		}
		// adapter
		viewPager.setAdapter(new ImagePagerAdapter(getSupportFragmentManager()));
		// 设置选中
		viewPager.setCurrentItem(curIndex < 0 ? 0 : curIndex, false);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private class ImagePagerAdapter extends FragmentPagerAdapter {

		public ImagePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int paramInt) {
			return fragmentList.get(paramInt);
		}

		@Override
		public int getCount() {
			return fragmentList.size();
		}

	}

}
