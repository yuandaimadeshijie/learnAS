package com.yonyou.sns.im.ui.component.topbar;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.CreateChatRoomActivity;
import com.yonyou.sns.im.activity.RosterSearchActivity;
import com.yonyou.sns.im.ui.widget.quickaction.ActionItem;
import com.yonyou.sns.im.ui.widget.quickaction.QuickAction;
import com.yonyou.sns.im.ui.widget.quickaction.QuickAction.OnActionItemClickListener;

public class MainAddTopBtnFunc extends BaseTopImageBtnFunc {

	public MainAddTopBtnFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.main_topbar_add;
	}

	@Override
	public int getFuncIcon() {
		return R.drawable.selector_add_btn;
	}

	@Override
	public void onclick(View v) {
		showCreateMultiChatActionBar(v);
	}

	/**
	 * 创建群聊/会议的响应事件
	 * 
	 * @param view
	 */
	public void showCreateMultiChatActionBar(View view) {
		QuickAction quickAction = new QuickAction(getActivity(), QuickAction.VERTICAL);
		quickAction.addActionItem(new ActionItem(0, getActivity().getString(R.string.main_create_chatgroup),
				getActivity().getResources().getDrawable(R.drawable.create_long_multi_chat)));
		quickAction.addActionItem(new ActionItem(1, getActivity().getString(R.string.addFriend_Title), getActivity()
				.getResources().getDrawable(R.drawable.add_friend_icon)));
		quickAction.setOnActionItemClickListener(new OnActionItemClickListener() {

			@Override
			public void onItemClick(QuickAction source, int pos, int actionId) {
				switch (actionId) {
				case 0: {
					Intent intent = new Intent(getActivity(), CreateChatRoomActivity.class);
					// 创建房间
					intent.putExtra(CreateChatRoomActivity.EXTRA_TOKEN, CreateChatRoomActivity.EXTRA_TOKEN_CREATEROOM);
					// 打开创建页面
					getActivity().startActivity(intent);
					break;
				}
				case 1: {
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
