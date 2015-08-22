package com.yonyou.sns.im.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.fragment.BaseFragment;
import com.yonyou.sns.im.activity.fragment.ContactFragment;
import com.yonyou.sns.im.activity.fragment.MeFragment;
import com.yonyou.sns.im.activity.fragment.RecentchatFragment;
import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.core.YYIMDBNotifier;
import com.yonyou.sns.im.log.YMLogger;
import com.yonyou.sns.im.ui.component.topbar.MainAddTopBtnFunc;
import com.yonyou.sns.im.ui.component.topbar.MainSearchTopBtnFunc;
import com.yonyou.sns.im.ui.widget.SnsTabWidget;
import com.yonyou.sns.im.util.common.ToastUtil;
import com.yonyou.sns.im.util.inject.InjectView;

/**
 * 主页面
 *
 * @author litfb
 * @date 2014年9月23日
 * @version 1.0
 */
public class MainActivity extends SimpleTopbarActivity {

	/** fragment classes */
	private static Class<?> fragmentArray[] = { RecentchatFragment.class, ContactFragment.class, MeFragment.class };
	/** tabs text*/
	private static int[] MAIN_TAB_TEXT = new int[] { R.string.main_tab_recentchat, R.string.main_tab_contact,
			R.string.main_tab_me };
	/** tabs image normal*/
	private static int[] MAIN_TAB_IMAGE = new int[] { R.drawable.main_tab_recentchat, R.drawable.main_tab_contact,
			R.drawable.main_tab_me };
	/** tabs image selected*/
	private static int[] MAIN_TAB_IMAGEHL = new int[] { R.drawable.main_tab_recentchat_hl,
			R.drawable.main_tab_contact_hl, R.drawable.main_tab_me_hl };

	/** Topbar功能列表 */
	private static Class<?> rightFuncArray[] = { MainAddTopBtnFunc.class };

	/** fragment列表 */
	private List<BaseFragment> fragmentList = new ArrayList<BaseFragment>();

	@InjectView(id = R.id.main_viewpager)
	private ViewPager viewPager;
	@InjectView(id = R.id.main_tabwidget)
	private SnsTabWidget tabWidget;

	/** receiver*/ 
	private MainReceiver receiver=new MainReceiver();
	/** 第一次返回按钮时间 */
	private long firstTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 初始化fragments
		initFragments();
		// 初始化ViewPager
		initPager();
		// 初始化Tab
		initTabWidget();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 注册receiver
		registerReceiver(receiver, new IntentFilter(YYIMDBNotifier.MESSAGE_CHANGE));
		// 重设title
		resetTitle(tabWidget.getCurIndex());
		// 重设红点
		resetRedPoints();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 注销receiver
		unregisterReceiver(receiver);
	}

	/**
	 * 获得所有的FragmentClass
	 * 
	 * @return
	 */
	protected Class<?>[] getFragmentClassArray() {
		return fragmentArray;
	}

	/**
	 * 初始化fragments
	 */
	protected void initFragments() {
		// 初始化fragments
		for (int i = 0; i < fragmentArray.length; i++) {
			BaseFragment fragment = null;
			try {
				fragment = (BaseFragment) fragmentArray[i].newInstance();
				fragment.setActivity(this);
			} catch (Exception e) {
				YMLogger.d(e);
			}
			fragmentList.add(fragment);
		}
	}

	/**
	 * 初始化ViewPager
	 */
	protected void initPager() {
		// adapter
		viewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
		// 默认选中第一个
		viewPager.setCurrentItem(0);
		// page change监听
		viewPager.setOnPageChangeListener(new MainPageChangeListener());
	}

	/**
	 * 初始化Tab
	 */
	protected void initTabWidget() {
		// LayoutInflater
		LayoutInflater inflater = LayoutInflater.from(this);
		for (int i = 0; i < fragmentArray.length; i++) {
			// 实例化tabitem
			View view = inflater.inflate(R.layout.view_main_tabitem, null);
			// 为每一个Tab按钮设置图标、文字和内容
			setTextViewStyle(view, i, (i == 0));
			tabWidget.addView(view);
		}
		// 选中第一个
		tabWidget.setCurrentTab(0);
		// 添加监听
		tabWidget.setTabSelectionListener(new MainTabSelectionListener());
	}

	/**
	 * 重设TextView的样式
	 * 
	 * @param textView
	 * @param index
	 * @param isCur
	 */
	protected void setTextViewStyle(View view, int index, boolean isCur) {
		// TextView
		TextView textView = (TextView) view.findViewById(R.id.main_tabitem_text);
		// 设置文字
		textView.setText(MAIN_TAB_TEXT[index]);

		// TextView
		TextView textViewHl = (TextView) view.findViewById(R.id.main_tabitem_texthl);
		// 设置文字
		textViewHl.setText(MAIN_TAB_TEXT[index]);

		// ImageView
		ImageView imageView = (ImageView) view.findViewById(R.id.main_tabitem_icon);
		// 非高亮图标
		imageView.setImageResource(MAIN_TAB_IMAGE[index]);

		// ImageView
		ImageView imageViewHl = (ImageView) view.findViewById(R.id.main_tabitem_iconhl);
		// 高亮图标
		imageViewHl.setImageResource(MAIN_TAB_IMAGEHL[index]);

		resetTextViewStyle(view, index, isCur);
	}

	/**
	 * 重设TextView的样式
	 * 
	 * @param textView
	 * @param index
	 * @param isCur
	 */
	protected void resetTextViewStyle(View view, int index, boolean isCur) {
		// 选中页签
		if (isCur) {
			resetTextViewAlpha(view, 1);
		} else {// 未选中页签
			resetTextViewAlpha(view, 0);
		}
	}

	/**
	 * 重设TextView的Alpha值
	 * 
	 * @param view
	 * @param f
	 */
	private void resetTextViewAlpha(View view, float f) {
		if (view == null) {
			return;
		}
		// ViewHl
		View itemViewHl = (View) view.findViewById(R.id.main_tabitem_viewhl);
		itemViewHl.setAlpha(f);
	}

	/**
	 * 重设页面的Alpha
	 * 
	 * @param index
	 * @param f
	 */
	private void resetFragmentAlpha(int index, float f) {
		if (index < 0 || index >= fragmentList.size()) {
			return;
		}
		View view = fragmentList.get(index).getView();
		if (view != null) {
			view.setAlpha(f);
		}
	}

	/**
	 * 连续按两次返回键就退出
	 */
	@Override
	public void onBackPressed() {
		if (System.currentTimeMillis() - firstTime < 3000) {
			backHome();
		} else {
			firstTime = System.currentTimeMillis();
			// 再按一次返回桌面
			ToastUtil.showShort(this, R.string.main_press_again_back);
		}
	}

	private void backHome() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
	}

	/**
	 * tab change监听
	 *
	 * @author litfb
	 * @date 2014年9月23日
	 * @version 1.0
	 */
	private class MainTabSelectionListener implements SnsTabWidget.ITabSelectionListener {

		@Override
		public void onTabSelectionChanged(int tabIndex) {
			// 重设当前页
			viewPager.setCurrentItem(tabIndex, false);
			// 重设title
			resetTitle(tabIndex);
			if (!viewPager.hasFocus()) {
				viewPager.requestFocus();
			}
		}
	}

	/**
	 * pager adapter
	 *
	 * @author litfb
	 * @date 2014年9月23日
	 * @version 1.0
	 */
	private class MainPagerAdapter extends FragmentPagerAdapter {

		public MainPagerAdapter(FragmentManager fm) {
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

	/**
	 * page change监听
	 *
	 * @author litfb
	 * @date 2014年9月23日
	 * @version 1.0
	 */
	private class MainPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrolled(int paramInt1, float paramFloat, int paramInt2) {
			int curIndex = tabWidget.getCurIndex();
			// 向右滑
			if (curIndex == paramInt1) {
				resetTextViewAlpha(tabWidget.getChildAt(curIndex), 1 - paramFloat);
				resetFragmentAlpha(curIndex, 1 - paramFloat);
				resetTextViewAlpha(tabWidget.getChildAt(curIndex + 1), paramFloat);
				resetFragmentAlpha(curIndex + 1, paramFloat);
			} else if (curIndex == paramInt1 + 1) {// 向左划
				resetTextViewAlpha(tabWidget.getChildAt(curIndex), paramFloat);
				resetFragmentAlpha(curIndex, paramFloat);
				resetTextViewAlpha(tabWidget.getChildAt(paramInt1), 1 - paramFloat);
				resetFragmentAlpha(paramInt1, 1 - paramFloat);
			}
		}

		@Override
		public void onPageSelected(int index) {
			// tabWidget焦点策略
			int oldFocusability = tabWidget.getDescendantFocusability();
			// 阻止冒泡
			tabWidget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
			// 切换tab
			tabWidget.setCurrentTab(index);
			// 重设title
			resetTitle(index);
			// 变换tab显示
			for (int i = 0; i < fragmentArray.length; i++) {
				View view = tabWidget.getChildAt(i);
				resetTextViewStyle(view, i, (i == index));
			}
			// 还原焦点策略
			tabWidget.setDescendantFocusability(oldFocusability);
		}

		@Override
		public void onPageScrollStateChanged(int paramInt) {

		}

	}

	@Override
	protected Object getTopbarTitle() {
		return R.string.main_tab_recentchat;
	}

	@Override
	protected Class<?> getTopbarLeftFunc() {
		return MainSearchTopBtnFunc.class;
	}

	@Override
	protected Class<?>[] getTopbarRightFuncArray() {
		return rightFuncArray;
	}

	/**
	 * 重设标题
	 */
	private void resetTitle(int index) {
		switch (index) {
		case 0:
			int newMsgCount = YYIMChatManager.getInstance().getUnreadMsgsCount();
			if (newMsgCount > 0) {
				resetTopbarTitle(getResources().getString(R.string.main_tab_recentchat_num, newMsgCount));
			} else {
				resetTopbarTitle(R.string.main_tab_recentchat);
			}
			break;
		case 1:
			resetTopbarTitle(R.string.main_tab_contact);
			break;
		case 2:
			resetTopbarTitle(R.string.main_tab_me);
			break;
		default:
			break;
		}
	}

	/**
	 * 重设红点
	 * 
	 * @param index
	 * @param number
	 */
	private void resetRedPoint(int index, int number) {
		View view = tabWidget.getChildAt(index);
		// red number
		TextView textRedpoint = (TextView) view.findViewById(R.id.main_tabitem_redpoint);
		if (number > 0) {
			textRedpoint.setText(String.valueOf(number));
			textRedpoint.setVisibility(View.VISIBLE);
		} else {
			textRedpoint.setText("");
			textRedpoint.setVisibility(View.GONE);
		}
	}

	/**
	 * 重设所有红点
	 */
	private void resetRedPoints() {
		// 重新查询一次新消息数量
		int newMsgCount = YYIMChatManager.getInstance().getUnreadMsgsCount();
		resetRedPoint(0, newMsgCount);
		// 重新查询一次我的电脑消息数量
		int newPmsgCount = YYIMChatManager.getInstance().getOwnUnreadMsgsCount();
		resetRedPoint(2, newPmsgCount);
	}
	/**
	 * 接收者
	 * @author wudl
	 * @date 2014年12月16日
	 * @version V1.0
	 */
	private class MainReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			resetRedPoints();
		}
		
	}

}
