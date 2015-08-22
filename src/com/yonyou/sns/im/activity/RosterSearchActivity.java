package com.yonyou.sns.im.activity;

import android.os.Bundle;

import com.yonyou.sns.im.R;

public class RosterSearchActivity extends SimpleTopbarActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_roster_search);
	}

	@Override
	protected Object getTopbarTitle() {
		return R.string.account_add;
	}
}
