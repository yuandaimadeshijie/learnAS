package com.yonyou.sns.im.smack.packet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.packet.IQ;

/**
 * 组织架构包
 * @author wudl
 *
 */
public class SnsOrgStructPacket extends IQ {

	private List<Item> items = new ArrayList<Item>();

	/**
	 * 获取items
	 * @return
	 */
	public List<Item> getItems() {
		synchronized (this.items) {
			// 返回一个不可修改的list
			return Collections.unmodifiableList(new ArrayList<Item>(this.items));
		}
	}

	/**
	 * 添加item
	 * @param paramItem
	 */
	public void addItem(Item paramItem) {
		synchronized (this.items) {
			this.items.add(paramItem);
		}
	}

	/**
	 * 获取query xml
	 */
	@Override
	public String getChildElementXML() {
		StringBuilder localStringBuilder = new StringBuilder();
		localStringBuilder.append("<query xmlns=\"http://jabber.org/protocol/org\">");
		synchronized (this.items) {
			for (int i = 0; i < this.items.size(); i++) {
				Item localItem = this.items.get(i);
				localStringBuilder.append(localItem.toXML());
			}
		}
		localStringBuilder.append(getExtensionsXML());
		localStringBuilder.append("</query>");
		return localStringBuilder.toString();
	}

	/**
	 * item子节点
	 * @author wudl
	 *
	 */
	public static class Item {

		public Item() {
		}

		// id
		String id;
		// 名字
		String name;
		// 判断是否是用户
		String isUser;
		// 判断是否是叶子
		String isLeaf;

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getIsUser() {
			return isUser;
		}

		public String getIsLeaf() {
			return isLeaf;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setIsUser(String isUser) {
			this.isUser = isUser;
		}

		public void setIsLeaf(String isLeaf) {
			this.isLeaf = isLeaf;
		}

		public String toXML() {
			StringBuilder localStringBuilder = new StringBuilder();
			localStringBuilder.append("<item");
			if (getId() != null) {
				localStringBuilder.append(" id=\"").append(getId()).append("\"");
			}
			if (getName() != null) {
				localStringBuilder.append(" name=\"").append(getName()).append("\"");
			}
			if (getIsUser() != null) {
				localStringBuilder.append(" isUser=\"").append(getIsUser()).append("\"");
			}
			if (getIsLeaf() != null) {
				localStringBuilder.append(" isLeaf=\"").append(getIsLeaf()).append("\"");
			}
			localStringBuilder.append("/></item>");
			return localStringBuilder.toString();
		}
	}

}
