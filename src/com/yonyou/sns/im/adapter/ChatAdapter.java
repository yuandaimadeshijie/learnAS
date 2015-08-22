package com.yonyou.sns.im.adapter;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.util.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.ImagePagerActivity;
import com.yonyou.sns.im.activity.MultyLocationActivity;
import com.yonyou.sns.im.activity.VCardActivity;
import com.yonyou.sns.im.core.YYIMCallBack;
import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.core.YYIMSessionManager;
import com.yonyou.sns.im.entity.FileItem;
import com.yonyou.sns.im.entity.YMVCard;
import com.yonyou.sns.im.entity.message.YMMessage;
import com.yonyou.sns.im.entity.message.YMMessageContent;
import com.yonyou.sns.im.exception.YMErrorConsts;
import com.yonyou.sns.im.ui.emoji.view.EmojiTextView;
import com.yonyou.sns.im.util.RecordManager;
import com.yonyou.sns.im.util.XMPPHelper;
import com.yonyou.sns.im.util.bitmap.BitmapCacheManager;
import com.yonyou.sns.im.util.common.DensityUtils;
import com.yonyou.sns.im.util.common.FileUtils;
import com.yonyou.sns.im.util.common.TimeUtil;
import com.yonyou.sns.im.util.common.ToastUtil;

public class ChatAdapter extends BaseAdapter {

	/** 时间合并阈值-1分钟 */
	private static final int DATE_MERGE_THRESHOLD = 60 * 1000;
	/** vcardEntity 更新界面*/
	private static final int UPDATE_VIEW = 0;
	/** 显示错误信息*/
	private static final int SHOW_FAILED_MSG = 1;

	/** Context */
	protected Context mContext;
	/** LayoutInflater */
	private LayoutInflater layoutInflater;
	/** 消息图片缓存管理 */
	private BitmapCacheManager localBitmapCacheManager;
	/** 头像图片缓存管理 */
	private BitmapCacheManager avatarBitmapCacheManager;
	/** 自己的VCard */
	private YMVCard vCardEntity;
	/** 最后的已读消息 */
	private int lastdelivered;

	/** 消息列表 */
	private List<YMMessage> chatList = new ArrayList<>();
	/** 图片消息列表 */
	private List<YMMessage> imageChats = new ArrayList<>();
	/** 文件消息列表 */
	private List<YMMessage> fileChats = new ArrayList<>();

	private ChatAdapterOpertionListener chatAdapterOpertionListener;

	/** handler*/
	private ChatAdapterHandler handler = new ChatAdapterHandler(this);

	public ChatAdapter(Context context) {
		super();
		this.mContext = context;
		init();
	}

	private void init() {
		// LayoutInflater
		layoutInflater = LayoutInflater.from(mContext);

		// 这个消息内容文件缓存任务
		localBitmapCacheManager = new BitmapCacheManager(mContext, false, BitmapCacheManager.NORMAL_BITMAP,
				BitmapCacheManager.BITMAP_DPSIZE_120);
		localBitmapCacheManager.setDefaultImage(R.drawable.icon_default_image);
		localBitmapCacheManager.generateBitmapCacheWork();
		// 这个是URL头像的缓存任务
		avatarBitmapCacheManager = new BitmapCacheManager(mContext, true, BitmapCacheManager.CIRCLE_BITMAP,
				BitmapCacheManager.BITMAP_DPSIZE_40);
		avatarBitmapCacheManager.generateBitmapCacheWork();
		// 查询自己的VCard
		vCardEntity = YYIMChatManager.getInstance().queryVCard(YYIMSessionManager.getInstance().getUserJid());
		if (vCardEntity == null) {
			YYIMChatManager.getInstance().loadVCard(YYIMSessionManager.getInstance().getUserJid(), new YYIMCallBack() {

				@Override
				public void onSuccess(Object object) {
					vCardEntity = (YMVCard) object;
					handler.sendEmptyMessage(UPDATE_VIEW);
				}

				@Override
				public void onProgress(int progress, String status) {
				}

				@Override
				public void onError(int errno, String errmsg) {
					switch (errno) {
					case YMErrorConsts.ERROR_AUTHORIZATION:
						handler.obtainMessage(SHOW_FAILED_MSG, "已断开连接").sendToTarget();
						break;
					case YMErrorConsts.EXCEPTION_NORESPONSE:
						handler.obtainMessage(SHOW_FAILED_MSG, "服务器未响应").sendToTarget();
						break;
					case YMErrorConsts.EXCEPTION_LOAD_VCARD:
						handler.obtainMessage(SHOW_FAILED_MSG, "加载vcard异常").sendToTarget();
						break;
					case YMErrorConsts.EXCEPTION_UNKNOWN:
						handler.obtainMessage(SHOW_FAILED_MSG, "未知错误").sendToTarget();
						break;
					default:
						break;
					}
				}
			});
		}
	}

	@Override
	public int getCount() {
		return chatList.size();
	}

	@Override
	public Object getItem(int position) {
		return chatList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return chatList.get(position).get_id();
	}

	/**
	 * 设置数据
	 * 
	 * @param chatList
	 */
	public void setChatList(List<YMMessage> chatList) {
		// 改变数据
		this.chatList = chatList;
		// 图片消息
		this.imageChats = null;
		// 文件消息
		this.fileChats = null;
		// 已读位置
		lastdelivered = getLastDelivered();
		// 通知变更
		notifyDataSetChanged();
	}

	/**
	 * 取数据
	 * 
	 * @return
	 */
	public List<YMMessage> getChatList() {
		return chatList;
	}

	/**
	 * 所有图片消息
	 * 
	 * @param cursor
	 */
	public List<YMMessage> getImageChats() {
		if (imageChats == null) {
			// new
			imageChats = new ArrayList<>();
			// 遍历
			for (YMMessage chat : chatList) {
				if (chat.getType() == YMMessage.CONTENT_IMAGE) {
					imageChats.add(chat);
				}
			}
		}
		return imageChats;
	}

	/**
	 * 所有文件信息
	 * 
	 * @param cursor
	 */
	public List<YMMessage> getFileChats() {
		if (fileChats == null) {
			// new
			fileChats = new ArrayList<>();
			// 遍历
			for (YMMessage chat : chatList) {
				if (chat.getType() == YMMessage.CONTENT_FILE) {
					fileChats.add(chat);
				}
			}
		}
		return fileChats;
	}

	private int getLastDelivered() {
		if (chatList == null || chatList.size() <= 0) {
			return -1;
		}

		for (int i = chatList.size() - 1; i > 0; i--) {
			YMMessage message = chatList.get(i);
			if (message.getDirection() == YMMessage.DIRECTION_SEND && message.getStatus() == YMMessage.STATE_DELIVERED) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (chatList == null || chatList.size() <= position) {
			throw new IllegalStateException("couldn't get data for position " + position);
		}

		YMMessage chatEntity = chatList.get(position);
		// direction
		int direction = chatEntity.getDirection();
		View view;
		if (convertView == null || !(direction == (int) convertView.getTag(R.string.chat_direction))) {
			view = newView(chatEntity, parent);
		} else {
			view = convertView;
		}
		// bindView
		bindView(view, position, chatEntity);
		return view;
	}

	public View newView(YMMessage chatEntity, ViewGroup viewGroup) {
		View view = null;
		// direction
		int direction = chatEntity.getDirection();
		// 如果获得状态是发送，用chat_item_right
		if (direction == YMMessage.DIRECTION_SEND) {
			view = layoutInflater.inflate(R.layout.chat_item_right, null);
		} else {// 如果获得状态是接收，用chat_item_left
			view = layoutInflater.inflate(R.layout.chat_item_left, null);
		}
		// 构建view holder
		ViewHolder viewHolder = buildHolder(view);
		// set to tag
		view.setTag(R.string.viewholder, viewHolder);
		view.setTag(R.string.chat_direction, direction);
		return view;
	}

	public void bindView(View view, int position, YMMessage chatEntity) {
		// get from tag
		final ViewHolder viewHolder = (ViewHolder) view.getTag(R.string.viewholder);
		// reset view status
		viewHolder.viewContent.setVisibility(View.VISIBLE);
		for (int i = 0; i < viewHolder.viewFrame.getChildCount(); i++) {
			viewHolder.viewFrame.getChildAt(i).setVisibility(View.GONE);
		}
		viewHolder.textExtraInfo.setVisibility(View.GONE);
		viewHolder.imageUnread.setVisibility(View.GONE);
		// 消息内容
		YMMessageContent chatContent = chatEntity.getChatContent();
		// generate view value and status
		switch (chatEntity.getType()) {
		case YMMessage.CONTENT_TEXT:
			// 文本消息处理
			bindTextViewData(position, chatEntity, viewHolder, chatContent);
			break;
		case YMMessage.CONTENT_AUDIO:
			// 语音消息处理
			bindAudioViewData(position, chatEntity, viewHolder, chatContent);
			break;
		case YMMessage.CONTENT_IMAGE:
			// 图片消息处理
			bindImageViewData(position, chatEntity, viewHolder, chatContent);
			break;
		case YMMessage.CONTENT_LOCATION:
			// 位置消息处理
			bindLocationViewData(position, chatEntity, viewHolder, chatContent);
			break;
		case YMMessage.CONTENT_FILE:
			// 文件消息处理
			bindFileViewData(position, chatEntity, viewHolder, chatContent);
			break;
		case YMMessage.CONTENT_PROMPT:
			bindPromptMsgViewData(position, chatEntity, viewHolder, chatContent);
			break;
		default:
			break;
		}
	}

	/**
	 * 绑定文件视图
	 * @param position
	 * @param entity
	 * @param viewHolder
	 * @param chatContent
	 */
	private void bindFileViewData(int position, final YMMessage entity, ViewHolder viewHolder,
			final YMMessageContent chatContent) {
		// 绑定公共部分
		bindCommonView(position, entity, viewHolder);

		// 防止内存溢出，每次使用图片时都要判断是否要回收之前的图片
		recycleImage((BitmapDrawable) viewHolder.fileIcon.getDrawable());
		viewHolder.fileArea.setVisibility(View.VISIBLE);
		viewHolder.fileArea.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					// 打开文件
					FileUtils.openFile(chatContent.getFileName(), entity.getRes_local(), mContext);
				} catch (Exception e) {
					ToastUtil.showLong(mContext, "没有应用程序可执行此操作。");
				}

			}
		});
		// file icon
		viewHolder.fileIcon.setImageResource(FileItem.getFileIcon(chatContent.getFileName()));
		// file name
		viewHolder.fileName.setText(chatContent.getFileName());
		viewHolder.fileName.setMaxWidth((int) (DensityUtils.getScreenWidth(mContext)*0.45));
		// file size
		if (chatContent.getFileSize() > 0) {
			viewHolder.textExtraInfo.setText(FileUtils.bytes2kb(chatContent.getFileSize()));
			viewHolder.textExtraInfo.setVisibility(View.VISIBLE);
		}
		// 如果是未读，设置成已读
		if (entity.getDirection() == YMMessage.DIRECTION_RECEIVE && entity.getStatus() == YMMessage.STATE_NEW) {
			markChatRead(entity.get_id());
		}
	}

	/**
	 * 绑定地理位置视图
	 * @param position
	 * @param entity
	 * @param viewHolder
	 * @param chatContent
	 */
	private void bindLocationViewData(int position, final YMMessage entity, ViewHolder viewHolder,
			final YMMessageContent chatContent) {
		// 绑定公共部分
		bindCommonView(position, entity, viewHolder);

		// 防止内存溢出，每次使用图片时都要判断是否要回收之前的图片
		recycleImage((BitmapDrawable) viewHolder.locationImage.getDrawable());
		// 有原图加载原图，没有原图使用小图
		viewHolder.locationImage.setVisibility(View.VISIBLE);
		localBitmapCacheManager
				.loadFormCache(
						!StringUtils.isEmpty(entity.getRes_thumb_local()) ? entity.getRes_thumb_local() : entity
								.getRes_local(), viewHolder.locationImage);
		viewHolder.locationImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 打开定位页面
				Intent intent = new Intent(mContext, MultyLocationActivity.class);
				intent.putExtra(MultyLocationActivity.RESULT_FROM_LOCATION_LATITUDE, chatContent.getLatitude());
				intent.putExtra(MultyLocationActivity.RESULT_FROM_LOCATION_LONGITUDE, chatContent.getLongitude());
				intent.putExtra(MultyLocationActivity.RESULT_FROM_LOCATION_ADRESS, chatContent.getAddress());
				mContext.startActivity(intent);
			}

		});
		// 设置地址
		viewHolder.locationAddress.setText(chatContent.getAddress());
		viewHolder.locationAddress.setVisibility(View.VISIBLE);

		// 如果是未读，设置成已读
		if (entity.getDirection() == YMMessage.DIRECTION_RECEIVE && entity.getStatus() == YMMessage.STATE_NEW) {
			markChatRead(entity.get_id());
		}
	}

	/**
	 * 绑定公共部分
	 * 
	 * @param entity
	 * @param viewHolder
	 */
	private void bindCommonView(int position, final YMMessage entity, ViewHolder viewHolder) {
		// 暂时判断如果两次消息时间少于1分钟，进行时间的隐藏合并
		boolean isHideDate = isHideDate(position, entity.getDate());
		// 设时间
		viewHolder.textDate.setText(TimeUtil.parseTimeExplicit(entity.getDate()));
		// 需要时间合并的，不再显示自己的时间
		if (isHideDate) {
			viewHolder.textDate.setVisibility(View.GONE);
		} else {
			viewHolder.textDate.setVisibility(View.VISIBLE);
		}

		// 防止内存溢出，每次使用图片时都要判断是否要回收之前的图片
		recycleImage((BitmapDrawable) viewHolder.imagePhoto.getDrawable());
		// 对方头像
		if (entity.getDirection() == YMMessage.DIRECTION_RECEIVE) {
			if (XMPPHelper.isOwnOtherClient(entity.getOpposite_jid())) {
				// 是我的电脑信息就使用本地资源当头像
				viewHolder.imagePhoto.setImageResource(R.drawable.me_protocal_icon);
			} else if (XMPPHelper.isSystemChat(entity.getOpposite_jid())) {
				// 是系统信息就使用本地资源当头像
				viewHolder.imagePhoto.setImageResource(R.drawable.icon_system_message);
			} else {
				// 否则加载路径头像
				avatarBitmapCacheManager.loadFormCache(XMPPHelper.getFullFilePath(entity.getOpposite_photo()),
						viewHolder.imagePhoto);
				// 发送消息的用户名称
				if (YMMessage.TYPE_GROUPCHAT.equalsIgnoreCase(entity.getChat_type())
						&& YYIMChatManager.getInstance().getYmSettings().isChatroomShowName(entity.getRoom_jid())) {
					String userName = entity.getOpposite_name();
					viewHolder.textUsername.setText(userName);
					viewHolder.textUsername.setVisibility(View.VISIBLE);
				} else {
					viewHolder.textUsername.setVisibility(View.GONE);
				}
			}
		} else {// 如果是自己发出的，使用自己的头像
			if (vCardEntity != null) {
				avatarBitmapCacheManager.loadFormCache(XMPPHelper.getFullFilePath(vCardEntity.getAvatar()),
						viewHolder.imagePhoto);
			} else {
				avatarBitmapCacheManager.loadFormCache("", viewHolder.imagePhoto);
			}
			viewHolder.textUsername.setVisibility(View.GONE);
		}
		if (entity.getDirection() == YMMessage.DIRECTION_RECEIVE && !XMPPHelper.isSystemChat(entity.getOpposite_jid())) {
			// 头像点击事件
			viewHolder.imagePhoto.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// 跳转到VCard页面
					Intent intent = new Intent(mContext, VCardActivity.class);
					intent.putExtra(VCardActivity.EXTRA_JID, entity.getOpposite_jid());
					mContext.startActivity(intent);
				}

			});
		}

		// 如果是自己发送的，并且状态是失败
		if (entity.getDirection() == YMMessage.DIRECTION_SEND && entity.getStatus() == YMMessage.STATE_FAILD) {
			viewHolder.imageResend.setVisibility(View.VISIBLE);
			// 重新发送增加监听
			viewHolder.imageResend.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					v.setVisibility(View.GONE);
					chatAdapterOpertionListener.resendMessage(entity.get_id());
				}

			});
		} else {
			viewHolder.imageResend.setVisibility(View.GONE);
		}

		// loading
		if (entity.getDirection() == YMMessage.DIRECTION_SEND && entity.getStatus() == YMMessage.STATE_NEW) {
			viewHolder.progressLoading.setVisibility(View.VISIBLE);
		} else if (entity.getDirection() == YMMessage.DIRECTION_RECEIVE
				&& entity.getRes_status() == YMMessage.RESSTATE_PROGRESSING) {
			viewHolder.progressLoading.setVisibility(View.VISIBLE);
		} else {
			viewHolder.progressLoading.setVisibility(View.GONE);
		}

		// 已读
		if (entity.getDirection() == YMMessage.DIRECTION_SEND) {
			if (position == lastdelivered) {
				viewHolder.textDelivered.setVisibility(View.VISIBLE);
			} else {
				viewHolder.textDelivered.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * 文本消息
	 * 
	 * @param entity
	 * @param viewHolder
	 * @param chatContent
	 */
	private void bindTextViewData(int position, final YMMessage entity, ViewHolder viewHolder,
			YMMessageContent chatContent) {
		// 绑定公共部分
		bindCommonView(position, entity, viewHolder);
		// set message text
		viewHolder.textMessage.setText(chatContent.getMessage());
		viewHolder.textMessage.setVisibility(View.VISIBLE);

		// loading
		if (entity.getDirection() == YMMessage.DIRECTION_RECEIVE) {
			viewHolder.progressLoading.setVisibility(View.GONE);
		}

		// 如果是未读，设置成已读
		if (entity.getDirection() == YMMessage.DIRECTION_RECEIVE && entity.getStatus() == YMMessage.STATE_NEW) {
			markChatRead(entity.get_id());
		}
	}

	/**
	 * 语音消息
	 * 
	 * @param position
	 * @param entity
	 * @param viewHolder
	 * @param chatContent
	 */
	private void bindAudioViewData(int position, final YMMessage entity, final ViewHolder viewHolder,
			YMMessageContent chatContent) {
		// 绑定公共部分
		bindCommonView(position, entity, viewHolder);

		// 设置是否是未读语音的状态
		if (entity.getSpecific_status() == YMMessage.SPECIFIC_INITIAL
				&& entity.getDirection() == YMMessage.DIRECTION_RECEIVE) {
			viewHolder.imageUnread.setVisibility(View.VISIBLE);
		} else {
			viewHolder.imageUnread.setVisibility(View.GONE);
		}

		// 音频时长
		viewHolder.textExtraInfo.setText(chatContent.getDuration() + "\"");
		viewHolder.textExtraInfo.setVisibility(View.VISIBLE);
		// 根据语音长度设置行宽度
		LayoutParams param = viewHolder.viewAudio.getLayoutParams();
		int audioWidth = (int) mContext.getResources().getDimensionPixelSize(R.dimen.chat_item_audio_width);
		param.width = audioWidth + DensityUtils.dipTopx(mContext, (float) 3 * chatContent.getDuration());
		int MaxWidth = (int) (DensityUtils.getScreenWidth(mContext) * 0.6);
		if (param.width > MaxWidth) {
			param.width = MaxWidth;
		}
		viewHolder.viewAudio.setLayoutParams(param);

		viewHolder.viewAudio.setVisibility(View.VISIBLE);
		viewHolder.viewAudio.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 音频文件不存在
				if (!FileUtils.isFileExist(entity.getRes_local())) {
					ToastUtil.showShort(mContext, "音频文件不存在!");
					return;
				}

				// 播放
				RecordManager.getInstance().play(entity.getRes_local(), viewHolder.imageAudioPlay,
						entity.getDirection());
				// 设置当前录音为已读
				if (entity.getSpecific_status() == YMMessage.SPECIFIC_INITIAL) {
					markAudioRead(entity.get_id());
					// 去掉红点
					viewHolder.imageUnread.setVisibility(View.GONE);
				}
			}
		});
		// 如果是未读，设置成已读
		if (entity.getDirection() == YMMessage.DIRECTION_RECEIVE && entity.getStatus() == YMMessage.STATE_NEW) {
			markChatRead(entity.get_id());
		}
	}

	/**
	 * 图片消息处理
	 * 
	 * @param position
	 * @param entity
	 * @param viewHolder
	 * @param chatContent
	 */
	private void bindImageViewData(int position, final YMMessage entity, ViewHolder viewHolder,
			YMMessageContent chatContent) {
		// 绑定公共部分
		bindCommonView(position, entity, viewHolder);

		// 防止内存溢出，每次使用图片时都要判断是否要回收之前的图片
		recycleImage((BitmapDrawable) viewHolder.imageImage.getDrawable());
		// 有原图加载原图，没有原图使用小图
		localBitmapCacheManager
				.loadFormCache(
						!StringUtils.isEmpty(entity.getRes_thumb_local()) ? entity.getRes_thumb_local() : entity
								.getRes_local(), viewHolder.imageImage);
		viewHolder.imageImage.setVisibility(View.VISIBLE);
		viewHolder.imageImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, ImagePagerActivity.class);
				intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_CHATS, (Serializable) getImageChats());
				intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_CURID, entity.get_id());
				mContext.startActivity(intent);
			}

		});
		// 如果是未读，设置成已读
		if (entity.getDirection() == YMMessage.DIRECTION_RECEIVE && entity.getStatus() == YMMessage.STATE_NEW) {
			markChatRead(entity.get_id());
		}
	}

	/**
	 * 群消息处理
	 * 
	 * @param position
	 * @param entity
	 * @param viewHolder
	 * @param chatContent
	 */
	private void bindPromptMsgViewData(int position, final YMMessage entity, ViewHolder viewHolder,
			YMMessageContent chatContent) {
		viewHolder.viewContent.setVisibility(View.GONE);
		viewHolder.textDate.setText(chatContent.getMessage());
		viewHolder.textDate.setVisibility(View.VISIBLE);
	}

	/**
	 * 回收image
	 * 
	 * @param drawable
	 */
	private void recycleImage(BitmapDrawable drawable) {
		// if (drawable != null && !drawable.getBitmap().isRecycled()) {
		// try {
		// drawable.getBitmap().recycle();
		// } catch (Exception e) {
		// Logger.d(e);
		// }
		// }
	}

	/**
	 * 判断是否需要进行时间合并
	 * 
	 * @param thisdate
	 * @return
	 */
	private boolean isHideDate(int position, long thisdate) {
		// 前移游标
		if (position > 0) {
			// 取上次时间
			long predate = chatList.get(position - 1).getDate();
			// 比较时间
			if (thisdate - predate < DATE_MERGE_THRESHOLD) {
				return true;
			}
		}
		return false;
	}

	private void markChatRead(final int id) {
		YYIMChatManager.getInstance().updateMessageState(id, YMMessage.STATE_SENT_OR_READ);
	}

	private void markAudioRead(final int id) {
		YYIMChatManager.getInstance().updateMessageSpecificState(id, YMMessage.SPECIFIC_AUDIO_READ);
	}

	/**
	 * 获得文字内容子控件的绑定类
	 * 
	 * @param convertView
	 * @return
	 */
	private ViewHolder buildHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.textDate = (TextView) convertView.findViewById(R.id.chat_item_date);
		holder.viewContent = (View) convertView.findViewById(R.id.chat_item_content);
		holder.imagePhoto = (ImageView) convertView.findViewById(R.id.chat_item_photo);
		holder.textUsername = (TextView) convertView.findViewById(R.id.chat_item_username);
		holder.viewFrame = (FrameLayout) convertView.findViewById(R.id.chat_item_frame);
		holder.textMessage = (EmojiTextView) convertView.findViewById(R.id.chat_item_message);
		holder.viewAudio = (View) convertView.findViewById(R.id.chat_item_audio);
		holder.imageAudioPlay = (ImageView) convertView.findViewById(R.id.chat_item_autio_play);
		holder.imageImage = (ImageView) convertView.findViewById(R.id.chat_item_image);
		holder.textExtraInfo = (TextView) convertView.findViewById(R.id.chat_item_extrainfo);
		holder.textDelivered = (TextView) convertView.findViewById(R.id.chat_item_delivered);
		holder.imageUnread = (ImageView) convertView.findViewById(R.id.chat_item_unread);
		holder.progressLoading = (ProgressBar) convertView.findViewById(R.id.chat_item_loading);
		holder.imageResend = (ImageView) convertView.findViewById(R.id.chat_item_resend);
		holder.locationImage = (ImageView) convertView.findViewById(R.id.chat_item_location);
		holder.locationAddress = (TextView) convertView.findViewById(R.id.chat_item_location_text);
		holder.fileIcon = (ImageView) convertView.findViewById(R.id.chat_item_file_icon);
		holder.fileName = (TextView) convertView.findViewById(R.id.chat_item_file_name);
		holder.fileArea = convertView.findViewById(R.id.chat_item_file);
		return holder;
	}

	private static class ViewHolder {

		TextView textDate;
		View viewContent;
		ImageView imagePhoto;
		TextView textUsername;
		FrameLayout viewFrame;
		EmojiTextView textMessage;
		View viewAudio;
		ImageView imageAudioPlay;
		ImageView imageImage;
		TextView textExtraInfo;
		TextView textDelivered;
		ImageView imageUnread;
		ProgressBar progressLoading;
		ImageView imageResend;
		ImageView locationImage;
		TextView locationAddress;
		ImageView fileIcon;
		TextView fileName;
		View fileArea;
	}

	public void setChatAdapterOpertionListener(ChatAdapterOpertionListener chatAdapterOpertionListener) {
		this.chatAdapterOpertionListener = chatAdapterOpertionListener;
	}

	/**
	 * 
	 * 处理chatadapter需要操作的监听器
	 * 
	 * @author yh
	 * 
	 */
	public interface ChatAdapterOpertionListener {

		/**
		 * 重新发送消息
		 * 
		 * @param id
		 */
		public void resendMessage(int id);

	}

	/**
	 * chat adapter handler,主要处理界面更新
	 * @author wudl
	 * @date 2014年12月8日
	 * @version V1.0
	 */
	private static class ChatAdapterHandler extends Handler {

		/** adapter 若引用*/
		private WeakReference<ChatAdapter> reference;

		public ChatAdapterHandler(ChatAdapter adapter) {
			reference = new WeakReference<ChatAdapter>(adapter);
		}

		public ChatAdapter getAdapter() {
			return reference.get();
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case UPDATE_VIEW:
				getAdapter().notifyDataSetChanged();
				break;
			case SHOW_FAILED_MSG:
				ToastUtil.showShort(getAdapter().mContext, msg.obj.toString());
				break;
			default:
				break;
			}
		}
	}

}
