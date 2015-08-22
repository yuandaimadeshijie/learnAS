package com.yonyou.sns.im.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.core.YYIMCallBack;
import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.entity.YMRoomMember;
import com.yonyou.sns.im.exception.YMErrorConsts;
import com.yonyou.sns.im.ui.widget.CustomDialog;
import com.yonyou.sns.im.ui.widget.CustomDialog.Builder;
import com.yonyou.sns.im.util.XMPPHelper;
import com.yonyou.sns.im.util.bitmap.BitmapCacheManager;
import com.yonyou.sns.im.util.common.ToastUtil;

/**
 * 群信息页面，显示群成员和群配置
 * @author wudl
 * @date 2014年11月28日
 * @version V1.0
 */
public class RoomInfoActivity extends SimpleTopbarActivity implements OnClickListener, OnCheckedChangeListener {

	/** room jid mark*/
	public static final String EXTRA_ROOMJID = "EXTRA_ROOM_JID";
	/** 打开主页面*/
	private static final int OPEN_MAIN_PAGE = 0;
	/** 显示错误信息*/
	private static final int SHOW_FALIED_MEG = 1;
	/** 重命名成功*/
	private static final int RENAME_SECCESS = 2;

	private BitmapCacheManager bitmapCacheManager;
	/** 成员gridView */
	private GridView mMemberGridView;
	/** 群聊名称面板 */
	private View mRoomNameView;
	/** 群聊名称 */
	private TextView mRoomName;
	/** 置顶消息switch */
	private Switch mPushMessageSwitch;
	/** 消息免打扰 */
	private Switch mNoMessageSwitch;
	/** 清空聊天记录 */
	private View mClearMessageButton;
	/** 显示群成员名称 */
	private Switch mShowMemberName;
	/** 退出按钮 */
	private View mExitButton;
	/** 房间jid */
	private String roomJid;
	/** 房间name */
	private String roomName;
	/** 房间gridView数据源 */
	private List<YMRoomMember> mMemberGridViewList = new ArrayList<>();
	/** handler*/
	private RoomInfoHandler handler = new RoomInfoHandler(this);

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_roominfo);

		bitmapCacheManager = new BitmapCacheManager(this, true, BitmapCacheManager.CIRCLE_BITMAP,
				BitmapCacheManager.BITMAP_DPSIZE_40);
		bitmapCacheManager.generateBitmapCacheWork();

		mMemberGridView = (GridView) findViewById(R.id.room_info_gridview);
		mRoomNameView = findViewById(R.id.room_info_list_room_name);
		mRoomName = (TextView) findViewById(R.id.room_info_list_room_name_text);
		mPushMessageSwitch = (Switch) findViewById(R.id.room_info_list_item_push_room_switch);
		mNoMessageSwitch = (Switch) findViewById(R.id.room_info_list_no_message_switch);
		mClearMessageButton = findViewById(R.id.room_info_list_clear_chat_message);
		mShowMemberName = (Switch) findViewById(R.id.room_info_list_show_member_name);
		mExitButton = findViewById(R.id.room_info_exit_button);

		// 获取房间jid
		roomJid = getRoomJid();
		// 获取房间name
		roomName = YYIMChatManager.getInstance().getNameByJid(roomJid);
		// 获取房间成员
		mMemberGridViewList = getMembersInfo();
		// 初始化界面
		init();
	}

	/**
	 * 初始化界面
	 */
	private void init() {
		resetTopbarTitle(getResources().getString(R.string.room_info_title, String.valueOf(mMemberGridViewList.size())));
		// 成员gridView
		initMemberGridView();
		// 房间信息ListView
		initRoomInfoList();
		// 退出按钮
		mExitButton.setOnClickListener(this);
	}

	/**
	 * 初始化房间信息listView
	 */
	private void initRoomInfoList() {
		// 群聊名称arrow
		mRoomNameView.setOnClickListener(this);
		mRoomName.setText(roomName);
		// 置顶消息switch
		mPushMessageSwitch.setOnCheckedChangeListener(this);
		// 消息免打扰
		mNoMessageSwitch.setChecked(!YYIMChatManager.getInstance().getYmSettings().isChatroomMsgRemind(getRoomJid()));
		mNoMessageSwitch.setOnCheckedChangeListener(this);
		// 显示群成员名字
		mShowMemberName.setChecked(YYIMChatManager.getInstance().getYmSettings().isChatroomShowName(getRoomJid()));
		mShowMemberName.setOnCheckedChangeListener(this);
		// 清空聊天记录
		mClearMessageButton.setOnClickListener(this);
	}

	/**
	 * 初始化成员gridView
	 */
	private void initMemberGridView() {
		// 增加一个邀请按钮
		mMemberGridViewList.add(new YMRoomMember());
		// 适配器
		MemberGridViewAdapter adapter = new MemberGridViewAdapter();
		// 添加适配器
		mMemberGridView.setAdapter(adapter);
		// 设置gridview 的space
		int horizontalSpacing = (int) (getResources().getDisplayMetrics().widthPixels - 4 * getResources()
				.getDimensionPixelSize(R.dimen.create_multi_chat_user_image_size)) / 5;
		mMemberGridView.setHorizontalSpacing(horizontalSpacing);
		// 设置padding
		int top = (int) getResources().getDimension(R.dimen.room_info_grid_vertical_spacing);
		mMemberGridView.setPadding(horizontalSpacing, top, horizontalSpacing, 0);
		// 设置一下高度
		setGridViewHeight(mMemberGridView);
		// 通知界面更新
		adapter.notifyDataSetChanged();
	}

	/**
	 * 设置gridView高度
	 * @param gView
	 */
	private void setGridViewHeight(GridView gView) {
		// 行高
		float colheight = getResources().getDimension(R.dimen.room_info_grid_item_height);
		float verticalSpace = getResources().getDimension(R.dimen.room_info_grid_vertical_spacing);
		// 行数
		int num = gView.getAdapter().getCount();
		int colCount = (int) Math.ceil(num / 4.0);
		// 计算当前高度
		float totalHeight = colCount * (colheight + verticalSpace) + verticalSpace;
		// 设置gridView高度
		LayoutParams params = gView.getLayoutParams();
		params.height = (int) totalHeight;
		gView.setLayoutParams(params);
	}

	/**
	 * 成员适配器
	 * 
	 * @author wudl
	 * 
	 */
	private class MemberGridViewAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mMemberGridViewList.size();
		}

		@Override
		public Object getItem(int paramInt) {
			return mMemberGridViewList.get(paramInt);
		}

		@Override
		public long getItemId(int paramInt) {
			return paramInt;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// 判空处理
			if (convertView == null) {
				convertView = LayoutInflater.from(RoomInfoActivity.this).inflate(R.layout.chat_room_info_grid_item,
						null);
			}
			// 设置头像
			ImageView mMemberIcon = (ImageView) convertView.findViewById(R.id.room_info_grid_item_icon);
			// 设置名字
			TextView mMemberName = (TextView) convertView.findViewById(R.id.room_info_grid_item_text);
			// 最后一个对象是邀请好友按钮，特殊区分
			if (position == getCount() - 1) {
				int padding = getResources().getDimensionPixelSize(R.dimen.room_info_addbtn_padding);
				mMemberIcon.setPadding(padding, padding, padding, padding);
				mMemberIcon.setBackgroundResource(R.drawable.shape_dotted);
				mMemberIcon.setImageResource(R.drawable.room_info_add_member);
				mMemberIcon.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View paramView) {
						// 打开邀请好友页面
						openInviteUser();
					}
				});
				mMemberName.setText(getResources().getString(R.string.room_info_grid_add_member));
			} else {
				bitmapCacheManager.loadFormCache(
						XMPPHelper.getFullFilePath(mMemberGridViewList.get(position).getPhoto()), mMemberIcon);

				mMemberName.setText(mMemberGridViewList.get(position).getName());
			}
			return convertView;
		}

	}

	/**
	 * 打开邀请好友页面
	 */
	private void openInviteUser() {
		// 打开邀请页
		Intent intent = new Intent(RoomInfoActivity.this, CreateChatRoomActivity.class);
		intent.putExtra(CreateChatRoomActivity.EXTRA_TOKEN, CreateChatRoomActivity.EXTRA_TOKEN_INVITE);
		intent.putExtra(CreateChatRoomActivity.EXTRA_ROOM_JID, roomJid);
		startActivity(intent);
		// 关闭当前页
		finish();
	}

	/**
	 * 获取成员信息
	 * 
	 * @return
	 */
	private List<YMRoomMember> getMembersInfo() {
		List<YMRoomMember> members = YYIMChatManager.getInstance().getRoomMemberByJid(roomJid);
		return members;
	}

	/**
	 * 获取通讯传过来的roomJid
	 * 
	 * @return
	 */
	private String getRoomJid() {
		return getIntent().getStringExtra(EXTRA_ROOMJID);
	}

	/**
	 * 删除房间历史记录弹出框
	 * 
	 * @param roomJid
	 * @param userName
	 */
	private void removeChatHistoryDialog(final String roomJid, final String userName) {
		new CustomDialog.Builder(this).setTitle(R.string.delete)
				.setMessage(getResources().getString(R.string.deleteChatHistory_text, userName, roomJid))
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						YYIMChatManager.getInstance().deleteChatByJid(roomJid);
					}
				}).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).create().show();
	}

	/**
	 * 编辑文本的弹出框，修改房间名字
	 * @param text
	 */
	private void editTextDialog(String text) {
		LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
		// 重命名弹出框
		final Builder builder = new CustomDialog.Builder(this);
		builder.setTitle(R.string.alert_dialog_title);
		// 设置内容视图
		builder.setEditView(inflater, text);
		// 确定按钮
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

				String name = builder.getInput();
				if (!name.equals(roomName)) {
					reName(name);
				}
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		// 创建弹出框并显示
		builder.create().show();
	}

	/**
	 * 删除房间弹出框
	 */
	private void showDeleteRoomDialog() {
		Builder builder = new CustomDialog.Builder(RoomInfoActivity.this);
		builder.setTitle(R.string.room_info_delete_room_dialog_title);
		builder.setMessage(R.string.room_info_delete_room_dialog_message);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 退出房间
				YYIMChatManager.getInstance().leaveRoom(getRoomJid(), new YYIMCallBack() {

					@Override
					public void onSuccess(Object object) {
						handler.sendEmptyMessage(OPEN_MAIN_PAGE);
					}

					@Override
					public void onProgress(int progress, String status) {
					}

					@Override
					public void onError(int errno, String errmsg) {
						switch (errno) {
						case YMErrorConsts.ERROR_AUTHORIZATION:
							handler.obtainMessage(SHOW_FALIED_MEG, "连接已断开").sendToTarget();
							break;
						case YMErrorConsts.EXCEPTION_UNKNOWN:
							handler.obtainMessage(SHOW_FALIED_MEG, "未知异常").sendToTarget();
							break;
						default:
							break;
						}
					}
				});
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		builder.create().show();
	}

	/**
	 * 点击ok事件,修改房间名字
	 */
	private void reName(final String name) {
		if (name.length() != 0) {
			YYIMChatManager.getInstance().renameChatRoom(roomJid, name, new YYIMCallBack() {

				@Override
				public void onSuccess(Object object) {
					roomName = name;
					handler.sendEmptyMessage(RENAME_SECCESS);
				}

				@Override
				public void onProgress(int progress, String status) {
				}

				@Override
				public void onError(int errno, String errmsg) {
					switch (errno) {
					case YMErrorConsts.ERROR_AUTHORIZATION:
						handler.obtainMessage(SHOW_FALIED_MEG, "连接已断开").sendToTarget();
						break;
					case YMErrorConsts.EXCEPTION_NORESPONSE:
						handler.obtainMessage(SHOW_FALIED_MEG, "服务器未响应").sendToTarget();
						break;
					case YMErrorConsts.EXCEPTION_RENAME_ROOM:
						handler.obtainMessage(SHOW_FALIED_MEG, "你没有权限修改群名称").sendToTarget();
						break;
					case YMErrorConsts.EXCEPTION_UNKNOWN:
						handler.obtainMessage(SHOW_FALIED_MEG, "未知异常").sendToTarget();
						break;
					default:
						break;
					}
				}
			});
		}

	}

	@Override
	public void onClick(View v) {
		super.onClick(v);

		if (v.getId() == R.id.room_info_list_room_name) {
			editTextDialog(roomName);
		} else if (v.getId() == R.id.room_info_list_clear_chat_message) {
			String roomName = YYIMChatManager.getInstance().getNameByJid(roomJid);
			// 删除聊天历史记录
			removeChatHistoryDialog(roomJid, roomName);
		} else if (v.getId() == R.id.room_info_exit_button) {
			// 显示删除提示框
			showDeleteRoomDialog();
		}
	}

	@Override
	protected Object getTopbarTitle() {
		return getResources().getString(R.string.room_info_title, String.valueOf(mMemberGridViewList.size()));
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.room_info_list_show_member_name) {
			YYIMChatManager.getInstance().getYmSettings().setChatroomShowName(getRoomJid(), isChecked);
		} else if (buttonView.getId() == R.id.room_info_list_item_push_room_switch) {
			// TODO
		} else if (buttonView.getId() == R.id.room_info_list_no_message_switch) {
			YYIMChatManager.getInstance().getYmSettings().setChatroomMsgRemind(getRoomJid(), !isChecked);
		}
	}

	/**
	 * 房间信息 handler，处理界面更新
	 * @author wudl
	 * @date 2014年12月8日
	 * @version V1.0
	 */
	private static class RoomInfoHandler extends Handler {

		WeakReference<RoomInfoActivity> reference;

		RoomInfoHandler(RoomInfoActivity activity) {
			reference = new WeakReference<RoomInfoActivity>(activity);
		}

		RoomInfoActivity getActivity() {
			return reference.get();
		}

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case OPEN_MAIN_PAGE:
				Intent intent = new Intent(getActivity(), MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				getActivity().startActivity(intent);
				break;
			case SHOW_FALIED_MEG:
				ToastUtil.showLong(getActivity(), msg.obj.toString());
				break;
			case RENAME_SECCESS:
				getActivity().mRoomName.setText(getActivity().roomName);
				ToastUtil.showShort(getActivity(), "修改名字成功");
				break;
			default:
				break;
			}
		};
	}

}
