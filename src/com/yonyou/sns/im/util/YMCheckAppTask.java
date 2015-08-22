package com.yonyou.sns.im.util;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.yonyou.sns.im.http.RequestParams;
import com.yonyou.sns.im.http.SyncHttpClient;
import com.yonyou.sns.im.http.handler.TextHttpResponseHandler;
import com.yonyou.sns.im.log.YMLogger;

public class YMCheckAppTask {

	/** android客户端 */
	private static final Integer CLIENT_TYPE_ANDROID = 0;

	/** 是否成功 */
	private boolean isSuccess;
	/** 下载路径 */
	private String path;
	/** 版本 */
	private int version;
	/** 是否必须更新 */
	private boolean require_update;

	public boolean CheckAppSync() {
		// 同步的Client
		SyncHttpClient asyncHttpClient = new SyncHttpClient();

		// 文件信息
		RequestParams urlParams = new RequestParams();
		// 0表示android客户端
		urlParams.put("clienttype", CLIENT_TYPE_ANDROID);

		asyncHttpClient.post(null, IMConfigUtil.getAppVersionServlet(), urlParams, new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String responseString) {
				YMLogger.d(responseString);
				try {
					JSONObject fileResult = new JSONObject(responseString);
					isSuccess = true;
					YMCheckAppTask.this.path = fileResult.optString("path");
					YMCheckAppTask.this.version = fileResult.optInt("version");
					YMCheckAppTask.this.require_update = fileResult.optBoolean("require_update");
				} catch (JSONException e) {
					YMLogger.e(e.getMessage());
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

	public String getPath() {
		return path;
	}

	public int getVersion() {
		return version;
	}

	public boolean getRequire_update() {
		return require_update;
	}
}
