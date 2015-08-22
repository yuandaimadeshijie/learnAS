package com.yonyou.sns.im.util;

import com.yonyou.sns.im.core.YYIMChatManager;

/**
 * 配置相关
 *
 * @author litfb
 * @date 2014年11月25日
 * @version 1.0
 */
public class IMConfigUtil {

	/** default app servlet*/
	public static final String getAppVersionServlet() {
		return YYIMChatManager.getInstance().getYmSettings().getAdminServer() + "/plugins/clientcontrol/service";
	}

	/**
	 * 获取下载apk的完整路径
	 * @param path
	 * @return
	 */
	public static String getDownLoadApkFullPath(String path) {
		return YYIMChatManager.getInstance().getYmSettings().getAdminServer() + "/" + path;
	}

}
