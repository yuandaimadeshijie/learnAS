package com.yonyou.sns.im.activity.fragment;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.ChatActivity;
import com.yonyou.sns.im.adapter.ChatRoomAdapter;
import com.yonyou.sns.im.entity.YMRoom;
import com.yonyou.sns.im.util.inject.InjectView;

public class ChatRoomFragment extends UserFragment {

	public static final String TAG = "ChatRoomFragment";

	/** adapter */
	private ChatRoomAdapter charRoomAdapter;

	/** 房间列表 */
	@InjectView(id = R.id.chatroom_list)
	private ListView charRoomList;
	/** empty view */
	@InjectView(id = R.id.empty_view)
	private View emptyView;

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_chatroom;
	}

	@Override
	protected void initView(LayoutInflater inflater, View view) {
		// adapter
		charRoomAdapter = new ChatRoomAdapter(getFragmentActivity());
		charRoomAdapter.setCustomClick(isCustomClick());
		charRoomAdapter.setUserSelectListener(getUserSelectListener());
		// 设置适配器
		charRoomList.setAdapter(charRoomAdapter);
		// 设置空
		TextView emptyMsg=(TextView) emptyView.findViewById(R.id.dialog_msg);
		emptyMsg.setText(R.string.dialog_guide_content_empty_roomlist);
		emptyView.findViewById(R.id.dialog_button).setVisibility(View.GONE);
		charRoomList.setEmptyView(emptyView);
		// 设置列表每列的点击事件
		charRoomList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				YMRoom room = charRoomAdapter.getItem(position);
				if (isSelect()) {
					String tag=RoomMemberFragment.TAG + "_" + room.getRoom_jid();
					RoomMemberFragment roomMemberFragment = (RoomMemberFragment) getFragmentActivity()
							.getSupportFragmentManager().findFragmentByTag(tag);
					if (roomMemberFragment == null) {
						roomMemberFragment = new RoomMemberFragment();
						roomMemberFragment.setRoom_jid(room.getRoom_jid());
					}
					getUserSelectListener().changeFragment(roomMemberFragment,tag);
				} else if (isCustomClick()) {
					getUserSelectListener().onUserclick(room);
				} else {
					Intent intent = new Intent(getFragmentActivity(), ChatActivity.class);
					intent.putExtra(ChatActivity.EXTRA_ROOM_JID, room.getRoom_jid());
					startActivity(intent);
				}
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		charRoomAdapter.requery();
	}

	@Override
	public void dataChanged() {

	}

}
