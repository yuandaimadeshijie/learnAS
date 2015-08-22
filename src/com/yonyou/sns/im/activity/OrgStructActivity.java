package com.yonyou.sns.im.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.fragment.OrgStructFragment;
import com.yonyou.sns.im.ui.component.topbar.FragmentActivityBackBtnFunc;

/**
 * 勾选组织结构内成员 本页面需要传递一个父节点pid;
 * 
 * @author wudl
 * 
 */
public class OrgStructActivity extends SimpleTopbarActivity {

	/** root jid*/
	public static final String EXTRA_PID = "EXTRA_PID";
	/** root name*/
	public static final String EXTRA_PNAME = "EXTRA_PNAME";

	/** 组织结构fragment */
	private OrgStructFragment orgStructFragment;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_orgstruct);
		// 初始化fragment
		orgStructFragment = new OrgStructFragment();
		orgStructFragment.setPid(getPid());
		changeFragment(orgStructFragment,OrgStructFragment.TAG + "_" + getPid());
	}

	/**
	 * 替换fragment
	 */
	public void changeFragment(Fragment fragment,String tag) {
		// 设fragment属性
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom);
		ft.replace(R.id.orgstruct_fragment, fragment,tag);
		ft.addToBackStack(null);
		ft.commit();
	}
	private String getPid() {
		return getIntent().getStringExtra(EXTRA_PID);
	}

	private String getPname() {
		return getIntent().getStringExtra(EXTRA_PNAME);
	}

	@Override
	protected Object getTopbarTitle() {
		return getPname();
	}
	
	@Override
	protected Class<?> getTopbarLeftFunc() {
		return FragmentActivityBackBtnFunc.class;
	}
}
