package com.yonyou.sns.im.activity.fragment;

import java.io.File;

import org.apache.http.Header;
import org.jivesoftware.smack.util.StringUtils;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.db.MessageDBTable;
import com.yonyou.sns.im.entity.message.YMMessage;
import com.yonyou.sns.im.entity.message.YMMessageContent;
import com.yonyou.sns.im.http.AsyncHttpClient;
import com.yonyou.sns.im.http.handler.RangeFileHttpResponseHandler;
import com.yonyou.sns.im.log.YMLogger;
import com.yonyou.sns.im.ui.widget.ZoomImageView;
import com.yonyou.sns.im.util.common.LocalBigImageUtil;
import com.yonyou.sns.im.util.common.YMStorageUtil;

public class LazyImageFragment extends BaseFragment {

	public static final String BUNDLE_IMAGE_CHAT = "BUNDLE_IMAGE_CHAT";

	/** 自定义的图片拖动、放大缩小控件 */
	private ZoomImageView zoomImage;
	/** 下载图片的面板 */
	private FrameLayout loadingFrame;
	/** 下载图片的默认图片 */
	private ImageView thumbImage;
	/** 下载图片的进度 */
	private TextView textProcess;

	private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

	private RangeFileHttpResponseHandler fileHttpResponseHandler;

	private YMMessage entity;

	public LazyImageFragment() {
		super();
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_lazyimage;
	}

	@Override
	protected void initView(LayoutInflater inflater, View view) {

		zoomImage = (ZoomImageView) view.findViewById(R.id.zoom_image_view);
		loadingFrame = (FrameLayout) view.findViewById(R.id.loading_image_layout);
		thumbImage = (ImageView) view.findViewById(R.id.loading_image_small);
		textProcess = (TextView) view.findViewById(R.id.loading_image_process);

		// 图片消息
		entity = (YMMessage) getArguments().getSerializable(BUNDLE_IMAGE_CHAT);
	}

	@Override
	public void onResume() {
		super.onResume();
		// 没有原始图片需要下载
		File file = new File(entity.getRes_local() == null ? "" : entity.getRes_local());
		if (!StringUtils.isEmpty(file.getAbsolutePath()) && file.exists()) {
			// 展示原图
			showOriginImage(file.getAbsolutePath());
		} else {
			// 展示缩略图原图
			file = new File(entity.getRes_thumb_local() == null ? "" : entity.getRes_thumb_local());
			if (file.exists()) {
				showOriginImage(file.getAbsolutePath());
			} else {
				// 下载缩略图,展示
				showThumbImage();
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (fileHttpResponseHandler != null) {
			fileHttpResponseHandler.doCancel();
		}
	}

	/**
	 * 展示缩略图
	 */
	private void showThumbImage() {
		zoomImage.setVisibility(View.GONE);
		loadingFrame.setVisibility(View.VISIBLE);
		thumbImage.setImageBitmap(BitmapFactory.decodeFile(entity.getRes_thumb_local()));
		// 启用下载的任务
		downLoadOriginImage(entity);
	}

	/**
	 * 展示原图
	 */
	private void showOriginImage(String path) {
		zoomImage.setVisibility(View.VISIBLE);
		loadingFrame.setVisibility(View.GONE);
		// DisplayMetrics
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		// 取屏幕大小bitmap
		Bitmap imgbitmap = LocalBigImageUtil.getBitmapFromFile(path, displayMetrics.widthPixels,
				displayMetrics.heightPixels);
		zoomImage.setImageBitmap(imgbitmap);
	}

	/**
	 * 下载图片
	 * 
	 * @param entity
	 */
	private void downLoadOriginImage(YMMessage entity) {
		// 消息内容
		YMMessageContent content = entity.getChatContent();
		// 图片url
		String url = content.getFileUrl();
		// 文件大小
		final int fileSize = (int) content.getFileSize();
		// id
		final int id = entity.get_id();

		// 图片路径
		File image = new File(YMStorageUtil.getImagePath(getFragmentActivity()), content.getFileName());

		fileHttpResponseHandler = new RangeFileHttpResponseHandler(image) {

			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				YMLogger.d("bytesWritten:" + bytesWritten + "," + "totalSize:" + fileSize);
				// 下载比例
				long downloadPercent = bytesWritten * 100 / fileSize;
				// 设置显示
				textProcess.setText(downloadPercent + "%");
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, File file) {
				YMLogger.d("下载成功！");
				// 更新数据库的本地图片地址
				String originImagePath = file.getAbsolutePath();
				// uri
				Uri uri = MessageDBTable.getContentURI(id);
				// ContentValues
				ContentValues values = new ContentValues();
				values.put(YMMessage.RES_THUMB_LOCAL, originImagePath);
				// 更新数据库
				getActivity().getContentResolver().update(uri, values, "", null);
				getActivity().getContentResolver().notifyChange(uri, null);
				// 展示原图
				showOriginImage(originImagePath);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
				textProcess.setText("");
				YMLogger.d(throwable);
			}

		};

		// 开始下载
		asyncHttpClient.get(url, fileHttpResponseHandler);
	}
}
