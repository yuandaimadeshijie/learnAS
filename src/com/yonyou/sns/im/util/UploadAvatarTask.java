package com.yonyou.sns.im.util;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.http.RequestParams;
import com.yonyou.sns.im.http.SyncHttpClient;
import com.yonyou.sns.im.http.handler.TextHttpResponseHandler;
import com.yonyou.sns.im.log.YMLogger;

/**
 * 头像上传task
 *
 * @author litfb
 * @date 2014年10月31日
 * @version 1.0
 */
public class UploadAvatarTask {

	/** 上传结果 */
	private boolean isSuccess;
	/** 头像路径 */
	private String avatarPath;

	public boolean uploadSyncSingleFile(File file) {
		// 同步的httpClient
		SyncHttpClient asyncHttpClient = new SyncHttpClient();
		// 文件信息
		RequestParams urlParams = new RequestParams();
		// 文件
		try {
			urlParams.put("__avatar1", file);
		} catch (FileNotFoundException e) {
			YMLogger.d(e);
			return false;
		}
		// 头像上传路径
		String uploadPath = YYIMChatManager.getInstance().getYmSettings().getFileUploadServlet();
		// 上传
		asyncHttpClient.post(null, uploadPath, urlParams, new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String responseString) {
				YMLogger.d(responseString);
				try {
					// 返回值json
					JSONObject jsonObject = new JSONObject(responseString);
					// 是否成功
					isSuccess = jsonObject.getBoolean("success");
					// 头像url
					JSONArray avatarUrls = jsonObject.getJSONArray("avatarUrls");
					if (avatarUrls != null && avatarUrls.length() > 0) {
						avatarPath = avatarUrls.getString(0);
					}
				} catch (JSONException e) {
					YMLogger.d(e);
					isSuccess = false;
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				YMLogger.d(responseString);
				isSuccess = false;
			}
		});

		return isSuccess;
	}

	/**
	 * 取头像路径
	 * 
	 * @return
	 */
	public String getAvatarPath() {
		return avatarPath;
	}

}
