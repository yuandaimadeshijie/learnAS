package com.yonyou.sns.im.activity.fragment;

import com.yonyou.sns.im.entity.IUserSelectListener;

public abstract class UserFragment extends BaseFragment {

	/** 是否选择 */
	private boolean isSelect;
	/** 自定义点击事件 */
	private boolean isCustomClick;

	/** 监听 */
	private IUserSelectListener userSelectListener;

	/**
	 * 数据变化
	 */
	public abstract void dataChanged();

	/**
	 * @return the isSelect
	 */
	public boolean isSelect() {
		return isSelect;
	}

	/**
	 * @param isSelect the isSelect to set
	 */
	public void setSelect(boolean isSelect) {
		this.isSelect = isSelect;
	}

	/**
	 * @return the isCustomClick
	 */
	public boolean isCustomClick() {
		return isCustomClick;
	}

	/**
	 * @param isCustomClick the isCustomClick to set
	 */
	public void setCustomClick(boolean isCustomClick) {
		this.isCustomClick = isCustomClick;
	}

	/**
	 * @return the userSelectListener
	 */
	public IUserSelectListener getUserSelectListener() {
		return userSelectListener;
	}

	/**
	 * @param userSelectListener the userSelectListener to set
	 */
	public void setUserSelectListener(IUserSelectListener userSelectListener) {
		this.userSelectListener = userSelectListener;
	}

}
