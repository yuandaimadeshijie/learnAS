package com.yonyou.sns.im.ui.component.topbar;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.RosterSearchActivity;
import com.yonyou.sns.im.ui.widget.quickaction.ActionItem;
import com.yonyou.sns.im.ui.widget.quickaction.QuickAction;
import com.yonyou.sns.im.ui.widget.quickaction.QuickAction.OnActionItemClickListener;

/**
 * 好友通讯录 topbar 加好友按钮
 * @author wudl
 *
 */
public class FriendGroupAddBtnFun extends BaseTopImageBtnFunc {

	public FriendGroupAddBtnFun(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.friend_topbar_add;
	}

	@Override
	public int getFuncIcon() {
		return R.drawable.selector_add_btn;
	}

	@Override
	public void onclick(View v) {
		showAddRosterActionBar(v);
	}

	/**
	 * 添加好友
	 * 
	 * @param view
	 */
	public void showAddRosterActionBar(View view) {
		QuickAction quickAction = new QuickAction(getActivity(), QuickAction.VERTICAL);
		quickAction.addActionItem(new ActionItem(0, getActivity().getString(R.string.addFriend_Title), getActivity()
				.getResources().getDrawable(R.drawable.add_friend_icon)));
		quickAction.setOnActionItemClickListener(new OnActionItemClickListener() {

			@Override
			public void onItemClick(QuickAction source, int pos, int actionId) {
				switch (actionId) {
				case 0: {
					Intent intent = new Intent(getActivity(), RosterSearchActivity.class);
					getActivity().startActivity(intent);
					break;
				}
				default:
					break;
				}
			}
		});
		quickAction.show(view);
		quickAction.setAnimStyle(QuickAction.ANIM_AUTO);
	}
}
