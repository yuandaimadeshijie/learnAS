package com.yonyou.sns.im.activity;

import android.os.Bundle;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.ui.component.topbar.FriendGroupAddBtnFun;

public class RosterActivity extends SimpleTopbarActivity {

	private static Class<?>[] funcArray = { FriendGroupAddBtnFun.class };

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_roster);
	}

	@Override
	protected Object getTopbarTitle() {
		return R.string.roster_list;
	}

	@Override
	protected Class<?>[] getTopbarRightFuncArray() {
		return funcArray;
	}

}
