package com.yonyou.sns.im.entity;

import java.io.Serializable;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.yonyou.sns.im.core.YYIMSessionManager;
import com.yonyou.sns.im.smack.packet.SnsOrgStructPacket.Item;
import com.yonyou.sns.im.util.common.YMDbUtil;

/**
 * 组织结构实体
 * @author wudl
 *
 */
public class YMOrgStruct implements Serializable, BaseColumns, IUser {

	private static final long serialVersionUID = -7673702625781747607L;
	/** MIME TYPE-DIR */
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.yonyou.sns.im.provider.struct";
	/** MIME TYPE-ITEM */
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.yonyou.sns.im.provider.struct";

	/** default order */
	public static final String DEFAULT_SORT_ORDER = "_id ASC";

	public static final String USER_JID = "user_jid";
	/** id */
	public static final String JID = "jid";
	/** 姓名 */
	public static final String NAME = "name";
	/** 是否是用户 */
	public static final String IS_USER = "is_user";
	/** 是否是叶子节点*/
	public static final String IS_LEAF = "is_leaf";
	/** 父亲节点id*/
	public static final String PID = "pid";

	/** 必须字段 */
	public static final String[] REQUIRED_COLUMNS = { USER_JID, NAME, IS_USER, IS_LEAF, JID, PID };
	/** 全部字段 */
	public static final String[] ALL_COLUMNS = { USER_JID, NAME, IS_LEAF, IS_USER, JID, PID };

	/** 根节点pid */
	public static final String PID_ROOT = "1";

	/** userJid */
	String userJid;
	/** id*/
	String jid;
	/** 名称*/
	String name;
	/** 是否是用户*/
	String isUser;
	/** 是否是叶子节点*/
	String isLeaf;
	/** pid*/
	String pid;

	public YMOrgStruct() {

	}

	public YMOrgStruct(Item item, String root) {
		this.userJid = YYIMSessionManager.getInstance().getUserJid();
		this.jid = item.getId();
		this.name = item.getName();
		this.isUser = item.getIsUser();
		this.isLeaf = item.getIsLeaf();
		this.pid = root;
	}

	public YMOrgStruct(Cursor cursor) {
		this.userJid = YMDbUtil.getString(cursor, USER_JID);
		this.jid = YMDbUtil.getString(cursor, JID);
		this.name = YMDbUtil.getString(cursor, NAME);
		this.isUser = YMDbUtil.getString(cursor, IS_USER);
		this.isLeaf = YMDbUtil.getString(cursor, IS_LEAF);
		this.pid = YMDbUtil.getString(cursor, PID);
	}

	/**
	 * ContentValues
	 * 
	 * @return
	 */
	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		values.put(USER_JID, this.userJid);
		values.put(JID, this.jid);
		values.put(NAME, this.name);
		values.put(IS_USER, this.isUser);
		values.put(IS_LEAF, this.isLeaf);
		values.put(PID, this.pid);
		return values;
	}

	/**
	 * @param userJid the userJid to set
	 */
	public void setUserJid(String userJid) {
		this.userJid = userJid;
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
	 * @return the isUser
	 */
	public String getIsUser() {
		return isUser;
	}

	/**
	 * @param isUser the isUser to set
	 */
	public void setIsUser(String isUser) {
		this.isUser = isUser;
	}

	/**
	 * @return the isLeaf
	 */
	public String getIsLeaf() {
		return isLeaf;
	}

	/**
	 * @param isLeaf the isLeaf to set
	 */
	public void setIsLeaf(String isLeaf) {
		this.isLeaf = isLeaf;
	}

	/**
	 * @return the pid
	 */
	public String getPid() {
		return pid;
	}

	/**
	 * @param pid the pid to set
	 */
	public void setPid(String pid) {
		this.pid = pid;
	}

	@Override
	public String getUserName() {
		return getName();
	}

	@Override
	public String getUserJid() {
		return getJid();
	}

	@Override
	public String getUserIcon() {
		return "";
	}

}
