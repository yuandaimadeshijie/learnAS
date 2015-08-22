package com.yonyou.sns.im.entity;

import java.io.Serializable;
import java.util.List;

import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.search.ReportedData.Row;

import android.text.TextUtils;

import com.yonyou.sns.im.entity.message.YMMessage;
import com.yonyou.sns.im.entity.message.YMMessageContent;
import com.yonyou.sns.im.util.XMPPHelper;

/**
 * 搜索结果实体类
 * 
 * @author wudl
 * 
 */
public class SearchValue implements Serializable, IUser {

	private static final long serialVersionUID = 7480978544229170598L;

	/** 名字 */
	private String name;
	/** 邮箱 */
	private String email;
	/** UserName */
	private String user_name;
	/** jid */
	private String jid;
	/** photo */
	private String photo;
	/** module*/
	private String module;
	/** 是否好友 */
	private boolean isFriend;

	/**
	 * @param roster
	 */
	public SearchValue(YMRoster roster) {
		this.jid = roster.getUser_jid();
		this.name = roster.getUserName();
		this.photo = roster.getUserIcon();
	}

	/**
	 * @param room
	 */
	public SearchValue(YMRoom room) {
		this.jid = room.getRoom_jid();
		this.name = room.getRoom_name();
	}

	/**
	 * @param message
	 */
	public SearchValue(YMMessage message) {
		this.jid = message.getRoom_jid();
		this.name = message.getOpposite_name();
		switch (message.getType()) {
		case YMMessage.CONTENT_TEXT:
			this.email = YMMessageContent.parseMessage(message.getMessage()).getMessage();
			break;
		case YMMessage.CONTENT_FILE:
			this.email = "[文件]" + message.getChatContent().getFileName();
			break;
		case YMMessage.CONTENT_LOCATION:
			this.email = "[位置]" + message.getChatContent().getAddress();
			break;
		default:
			break;
		}
		if (!XMPPHelper.isGroupChat(this.jid)) {
			this.photo = message.getOpposite_photo();
		}
	}

	/**
	 * @param module
	 */
	public SearchValue(String module) {
		this.module = module;
	}

	/**
	 * @param row
	 */
	public SearchValue(Row row) {
		this.jid = getReportedDataRowValue(row, "jid");
		this.user_name = getReportedDataRowValue(row, "Username");
		this.name = getReportedDataRowValue(row, "Name");
		this.email = getReportedDataRowValue(row, "Email");
		this.photo = getReportedDataRowValue(row, "photo");
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	/**
	 * 取搜索结果属性值
	 * 
	 * @param row
	 * @param variable
	 * @return
	 */
	private String getReportedDataRowValue(Row row, String variable) {
		List<String> values = row.getValues(variable);
		if (values != null && values.size() > 0) {
			return values.get(0);
		}
		return "";
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the jid
	 */
	public String getJid() {
		return jid;
	}

	/**
	 * @param jid the jid to set
	 */
	public void setJid(String jid) {
		this.jid = jid;
	}

	/**
	 * @return the photo
	 */
	public String getPhoto() {
		return photo;
	}

	/**
	 * @param photo the photo to set
	 */
	public void setPhoto(String photo) {
		this.photo = photo;
	}

	/**
	 * @return the user_name
	 */
	public String getUser_name() {
		return user_name;
	}

	/**
	 * @param user_name the user_name to set
	 */
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	/**
	 * @return the isFriend
	 */
	public boolean isFriend() {
		return isFriend;
	}

	/**
	 * @param isFriend the isFriend to set
	 */
	public void setFriend(boolean isFriend) {
		this.isFriend = isFriend;
	}

	@Override
	public String getUserJid() {
		return getJid();
	}

	@Override
	public String getUserIcon() {
		return getPhoto();
	}

	@Override
	public String getUserName() {
		String name = getName();
		if (TextUtils.isEmpty(name)) {
			name = getUser_name();
		}
		if (TextUtils.isEmpty(name)) {
			name = StringUtils.parseBareName(getJid());
		}
		return name;
	}

}
