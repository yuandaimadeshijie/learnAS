package com.yonyou.sns.im.entity;

import com.yonyou.sns.im.activity.fragment.UserFragment;

public interface IUserSelectListener {

	public boolean isUserSelected(String jid);

	public void onUserclick(IUser user);

	public void selectChange(IUser user, boolean isChecked);

	public void changeFragment(UserFragment fragment,String tag);

	public boolean isAlreadyExistsMembers(String userJid);
}
