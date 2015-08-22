package com.yonyou.sns.im.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
import android.widget.TabWidget;

public class SnsTabWidget extends TabWidget {

	private ITabSelectionListener tabSelectionListener;

	private int selectedTab = -1;

	public SnsTabWidget(Context context) {
		super(context);
	}

	public SnsTabWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SnsTabWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setTabSelectionListener(ITabSelectionListener listener) {
		this.tabSelectionListener = listener;
	}

	public void addView(View child) {
		if (child.getLayoutParams() == null) {
			final LinearLayout.LayoutParams lp = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
			lp.setMargins(0, 0, 0, 0);
			child.setLayoutParams(lp);
		}

		child.setFocusable(true);
		child.setClickable(true);

		super.addView(child);

		child.setOnClickListener(new TabClickListener(getTabCount() - 1));
		child.setOnFocusChangeListener(this);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (v == this && hasFocus && getTabCount() > 0) {
			getChildTabViewAt(selectedTab).requestFocus();
			return;
		}

		if (hasFocus) {
			int i = 0;
			int numTabs = getTabCount();
			while (i < numTabs) {
				if (getChildTabViewAt(i) == v) {
					setCurrentTab(i);
					handleTabSelection(i);
					if (isShown()) {
						sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
					}
					break;
				}
				i++;
			}
		}
	}

	private void handleTabSelection(int index) {
		if (tabSelectionListener != null) {
			tabSelectionListener.onTabSelectionChanged(index);
		}
	}

	@Override
	public void setCurrentTab(int index) {
		super.setCurrentTab(index);
		this.selectedTab = index;
	}

	/**
	 * 取index
	 * 
	 * @return
	 */
	public int getCurIndex() {
		return this.selectedTab;
	}

	@Override
	public void removeAllViews() {
		super.removeAllViews();
		this.selectedTab = -1;
	}

	public class TabClickListener implements OnClickListener {

		private final int mTabIndex;

		private TabClickListener(int tabIndex) {
			mTabIndex = tabIndex;
		}

		public void onClick(View v) {
			handleTabSelection(mTabIndex);
		}
	}

	public interface ITabSelectionListener {

		void onTabSelectionChanged(int tabIndex);
	}

}
