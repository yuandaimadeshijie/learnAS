package com.yonyou.sns.im.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.adapter.WorkDocAdapter;
import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.entity.YMFile;
import com.yonyou.sns.im.ui.emoji.view.CustomViewPager;
import com.yonyou.sns.im.ui.emoji.view.PagerSlidingTabStrip;
import com.yonyou.sns.im.util.inject.InjectView;

/**
 * 工作文档
 * @author wudl
 * @date 2014年12月2日
 * @version V1.0
 */
public class WorkDocActivity extends SimpleTopbarActivity {

	/** LOCAL_WORK_DOC mark*/
	public static final String LOCAL_WORK_DOC = "LOCAL_WORK_DOC";
	/** page views */
	private ArrayList<View> mViews = new ArrayList<View>();
	/** tabs text*/
	private static int[] WORK_DOC_TAB_TEXT = new int[] { R.string.work_doc_tab_receive, R.string.work_doc_tab_send };
	/** tabs*/
	@InjectView(id = R.id.work_doc_tabwidget)
	private PagerSlidingTabStrip tabs;
	/** viewpage*/
	@InjectView(id = R.id.work_doc_viewpage)
	private CustomViewPager viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_work_doc);
		for (int i = 0; i < WORK_DOC_TAB_TEXT.length; i++) {
			View view = LayoutInflater.from(this).inflate(R.layout.fragment_work_doc, null);
			ListView mineSendList = (ListView) view.findViewById(R.id.work_doc_list);
			View emptyView = view.findViewById(R.id.search_empty_view);
			// 获取数据
			List<YMFile> list = new ArrayList<YMFile>();
			if (i == 0) {
				list = getData(YMFile.FILE_RECEIVE);
			} else {
				list = getData(YMFile.FILE_SEND);
			}
			// 设置适配器
			mineSendList.setEmptyView(emptyView);
			mineSendList.setAdapter(new WorkDocAdapter(this,list));
			((WorkDocAdapter) (mineSendList.getAdapter())).notifyDataSetChanged();
			mViews.add(view);
		}
		// 创建page的adapter，并设置进去
		WorkDocPageAdapter mPagerAdapter = new WorkDocPageAdapter();
		viewPager.setAdapter(mPagerAdapter);

		// 指定tab页对应的page页面，可以获取page的adapter，并设置page的PageListener
		tabs.setViewPager(viewPager);
		tabs.setTabBackground(getResources().getColor(R.color.emoji_background_tab));
		tabs.setDividerColor(getResources().getColor(R.color.emoji_divider_color));
		tabs.setIndicatorPadding(40);
		// 设置初始页
		viewPager.setCurrentItem(0);
	}

	/**
	 * 获取列表数据
	 * @return
	 */
	private List<YMFile> getData(int mark) {
		List<YMFile> list = new ArrayList<YMFile>();
		if (YMFile.FILE_RECEIVE == mark) {
			list = YYIMChatManager.getInstance().getMineReceiveFile();
		} else if (YMFile.FILE_SEND == mark) {
			list = YYIMChatManager.getInstance().getMineSendFile();
		}
		return list;
	}

	@Override
	protected Object getTopbarTitle() {
		return R.string.work_doc_title;

	}

	/**
	 * 发送文件
	 */
	public void sendFile(YMFile fileItem) {
		Intent intent = getIntent();
		Bundle bundle = new Bundle();
		bundle.putSerializable(LOCAL_WORK_DOC, (Serializable) fileItem);
		intent.putExtras(bundle);
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	/**
	 * page adapter
	 * @author wudl
	 * @date 2014年12月2日
	 * @version V1.0
	 */
	private class WorkDocPageAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return WORK_DOC_TAB_TEXT.length;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object paramObject) {
			View localObject = mViews.get(position);
			container.removeView(localObject);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = mViews.get(position);
			container.addView(view);
			return view;
		}

		@Override
		public boolean isViewFromObject(View paramView, Object paramObject) {
			return paramView == paramObject;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return getResources().getString(WORK_DOC_TAB_TEXT[position]);
		}
	}
}
