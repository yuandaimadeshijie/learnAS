package com.yonyou.sns.im.util;

import android.content.Context;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.entity.message.YMMessage;
import com.yonyou.sns.im.entity.message.YMMessageContent;
import com.yonyou.sns.im.ui.emoji.util.EmojiUtil;

public class IMMessageUtil {

	/**
	 * 生成列表显示文本
	 * 
	 * @param context
	 * @param messageContent
	 * @return
	 */
	public static CharSequence genChatContent(Context context, YMMessageContent messageContent) {
		return genChatContent(context, messageContent, true);
	}

	/**
	 * 生成列表显示文本
	 * 
	 * @param context
	 * @param messageContent
	 * @param parseEmoji
	 * @return
	 */
	public static CharSequence genChatContent(Context context, YMMessageContent messageContent, boolean parseEmoji) {
		if (messageContent == null) {
			return "";
		}

		// 根据消息类型处理
		switch (messageContent.getType()) {
		case YMMessage.CONTENT_TEXT:
			if (parseEmoji) {
				return EmojiUtil.convertNormalStringToSpannableString(context, messageContent.getMessage());
			} else {
				return messageContent.getMessage();
			}
		case YMMessage.CONTENT_AUDIO:
			return context.getString(R.string.recent_audio);
		case YMMessage.CONTENT_IMAGE:
			return context.getString(R.string.recent_image);
		case YMMessage.CONTENT_FILE:
			return context.getString(R.string.recent_file) + " " + messageContent.getFileName();
		case YMMessage.CONTENT_LOCATION:
			return context.getString(R.string.recent_location);
		default:
			return "";
		}
	}

}
