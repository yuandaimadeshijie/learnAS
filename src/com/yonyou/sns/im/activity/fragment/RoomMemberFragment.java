package com.yonyou.sns.im.activity.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.adapter.RoomMemberAdapter;
import com.yonyou.sns.im.util.inject.InjectView;

public class RoomMemberFragment extends UserFragment {

	public static final String TAG = "RoomMemberFragment";

	/** adapter */
	private RoomMemberAdapter memberAdapter;

	/** room jid */
	private String room_jid;

	/** 群成员列表 */
	@InjectView(id = R.id.roommember_list)
	private ListView roomMemberListView;

	public void setRoom_jid(String room_jid) {
		this.room_jid = room_jid;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_roommember;
	}

	@Override
	protected void initView(LayoutInflater inflater, View view) {
		// 适配器
		memberAdapter = new RoomMemberAdapter(getFragmentActivity(), room_jid, isSelect());
		memberAdapter.setUserSelectListener(getUserSelectListener());
		// 添加适配器
		roomMemberListView.setAdapter(memberAdapter);
	}

	@Override
	public void onResume() {
		super.onResume();
		memberAdapter.requery();
	}

	@Override
	public void dataChanged() {
		memberAdapter.notifyDataSetChanged();
	}

}
