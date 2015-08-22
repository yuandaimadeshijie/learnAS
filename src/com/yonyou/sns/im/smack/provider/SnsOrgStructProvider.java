package com.yonyou.sns.im.smack.provider;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import com.yonyou.sns.im.smack.packet.SnsOrgStructPacket;
import com.yonyou.sns.im.smack.packet.SnsOrgStructPacket.Item;

/**
 * 组织架构解析器
 * @author wudl
 *
 */
public class SnsOrgStructProvider implements IQProvider {

	@Override
	public IQ parseIQ(XmlPullParser paramXmlPullParser) throws Exception {
		int i = 0;
		String id = "";
		String name = "";
		String isUser = "";
		String isLeaf = "";
		SnsOrgStructPacket packet = new SnsOrgStructPacket();
		while (i == 0) {
			int j = paramXmlPullParser.next();
			if (j == XmlPullParser.START_TAG && paramXmlPullParser.getName().equals("item")) {
				id = paramXmlPullParser.getAttributeValue("", "id");
				name = paramXmlPullParser.getAttributeValue("", "name");
				isUser = paramXmlPullParser.getAttributeValue("", "isUser");
				isLeaf = paramXmlPullParser.getAttributeValue("", "isLeaf");
			} else if (j == XmlPullParser.END_TAG && paramXmlPullParser.getName().equals("item")) {
				// 添加item
				Item item = new Item();
				item.setId(id);
				item.setName(name);
				item.setIsUser(isUser);
				item.setIsLeaf(isLeaf);
				packet.addItem(item);
			} else if ((j == XmlPullParser.END_TAG && paramXmlPullParser.getName().equals("query"))
					&& ("query".equals(paramXmlPullParser.getName()))) {
				i = 1;
			}
		}
		return packet;
	}

}
