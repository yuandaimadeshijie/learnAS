package com.yonyou.sns.im.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.ui.component.topbar.ChatRoomListAddTopBtnFunc;
import com.yonyou.sns.im.ui.widget.quickaction.ActionItem;
import com.yonyou.sns.im.ui.widget.quickaction.QuickAction;
import com.yonyou.sns.im.ui.widget.quickaction.QuickAction.OnActionItemClickListener;

/**
 * 房间列表显示页面
 * 
 * @author wudl
 * 
 */
public class ChatRoomActivity extends SimpleTopbarActivity {

	/** Topbar功能列表 */
	private static Class<?> TopBarRightFuncArray[] = { ChatRoomListAddTopBtnFunc.class };

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_chatroom);
	}

	/**
	 * 创建群聊/会议
	 * 
	 * @param view
	 */
	public void showCreateMultiChatActionBar(View view) {
		QuickAction quickAction = new QuickAction(ChatRoomActivity.this, QuickAction.VERTICAL);
		quickAction.addActionItem(new ActionItem(0, getString(R.string.main_create_chatgroup), getResources()
				.getDrawable(R.drawable.create_long_multi_chat)));
		quickAction.setOnActionItemClickListener(new OnActionItemClickListener() {

			@Override
			public void onItemClick(QuickAction source, int pos, int actionId) {
				switch (actionId) {
				case 0: {
					Intent intent = new Intent(ChatRoomActivity.this, CreateChatRoomActivity.class);
					// 创建持久房间令牌
					intent.putExtra(CreateChatRoomActivity.EXTRA_TOKEN, CreateChatRoomActivity.EXTRA_TOKEN_CREATEROOM);
					// 打开创建页面
					startActivity(intent);
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

	@Override
	protected Object getTopbarTitle() {
		return R.string.room_list_enter_title;
	}

	@Override
	protected Class<?>[] getTopbarRightFuncArray() {
		return TopBarRightFuncArray;
	}
}
