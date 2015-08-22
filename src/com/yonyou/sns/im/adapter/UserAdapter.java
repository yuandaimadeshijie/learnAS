package com.yonyou.sns.im.adapter;

import android.widget.BaseAdapter;

import com.yonyou.sns.im.entity.IUser;
import com.yonyou.sns.im.entity.IUserSelectListener;

public abstract class UserAdapter extends BaseAdapter {

	/** 是否选择 */
	private boolean isSelect;
	/** 是否自定义点击 */
	private boolean isCustomClick;

	/** 监听 */
	private IUserSelectListener userSelectListener;

	protected UserAdapter(boolean isSelect) {
		this.isSelect = isSelect;
	}

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
	 * user是否选中
	 * 
	 * @param jid
	 * @return
	 */
	protected boolean isUserSelected(String jid) {
		if (userSelectListener == null) {
			return false;
		}
		return userSelectListener.isUserSelected(jid);
	}

	/**
	 * 房间邀请的时候，判断成员是否已存在
	 * @param jid
	 * @return
	 */
	protected boolean isAlreadyExistsMembers(String jid) {
		if (userSelectListener == null) {
			return false;
		}
		return userSelectListener.isAlreadyExistsMembers(jid);
	}

	/**
	 * 选择变更
	 * 
	 * @param user
	 * @param isChecked
	 */
	protected void selectChange(IUser user, boolean isChecked) {
		if (userSelectListener == null) {
			return;
		}
		userSelectListener.selectChange(user, isChecked);
	}

	/**
	 * 用户点击
	 * 
	 * @param user
	 */
	protected void onUserclick(IUser user) {
		if (userSelectListener == null) {
			return;
		}
		userSelectListener.onUserclick(user);
	}

	/**
	 * 设选择变化监听
	 * 
	 * @param listener
	 */
	public void setUserSelectListener(IUserSelectListener listener) {
		this.userSelectListener = listener;
	}

}
