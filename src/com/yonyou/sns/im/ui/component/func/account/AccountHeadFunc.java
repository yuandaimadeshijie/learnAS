package com.yonyou.sns.im.ui.component.func.account;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.UUID;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.AccountActivity;
import com.yonyou.sns.im.activity.AlbumPhotoActivity;
import com.yonyou.sns.im.core.YYIMSessionManager;
import com.yonyou.sns.im.entity.YMVCard;
import com.yonyou.sns.im.entity.album.PhotoItem;
import com.yonyou.sns.im.log.YMLogger;
import com.yonyou.sns.im.ui.component.func.BaseFunc;
import com.yonyou.sns.im.util.XMPPHelper;
import com.yonyou.sns.im.util.common.FileUtils;
import com.yonyou.sns.im.util.common.YMStorageUtil;
import com.yonyou.sns.im.util.message.MessageResUploadTask;

/**
 * 头像
 *
 * @author litfb
 * @date 2014年10月16日
 * @version 1.0
 */
public class AccountHeadFunc extends BaseFunc implements OnClickListener {

	public static final String IMAGE_UNSPECIFIED = "image/*";

	/** 头像Image */
	private ImageView imageHead;
	/** 头像修改菜单 */
	private View viewChoice;
	/** 头像修改dialog */
	private Dialog dlgChoice;

	/** 照片地址 */
	private String photoPath;

	public AccountHeadFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.account_func_head;
	}

	@Override
	public int getFuncIcon() {
		return 0;
	}

	@Override
	public int getFuncName() {
		return R.string.account_head;
	}

	@Override
	public void onclick() {
		getChoiceDialog().show();
	}

	/**
	 * 菜单View
	 * 
	 * @param context
	 * @return
	 */
	private View getChoiceView() {
		if (viewChoice == null) {
			// 初始化选择菜单
			viewChoice = LayoutInflater.from(getActivity()).inflate(R.layout.view_head_choice, null);
			viewChoice.findViewById(R.id.account_head_choice_album).setOnClickListener(this);
			viewChoice.findViewById(R.id.account_head_choice_camera).setOnClickListener(this);
			viewChoice.findViewById(R.id.account_head_choice_cancel).setOnClickListener(this);
		}
		return viewChoice;
	}

	/**
	 * 修改头像对话框
	 * 
	 * @return
	 */
	private Dialog getChoiceDialog() {
		if (dlgChoice == null) {
			dlgChoice = new Dialog(getActivity(), R.style.DialogStyle);
			dlgChoice.setContentView(getChoiceView());
			return dlgChoice;
		}
		return dlgChoice;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.account_head_choice_album:
			// 关闭对话框
			closeChoiceDialog();
			// album
			Intent albumIntent = new Intent(getActivity(), AlbumPhotoActivity.class);
			albumIntent.putExtra(AlbumPhotoActivity.EXTRA_TYPE, AlbumPhotoActivity.TYPE_SINGLE);
			// start
			getActivity().startActivityForResult(albumIntent, REQUEST_CODE_HEAD_ALBUM);
			break;
		case R.id.account_head_choice_camera:
			// 关闭对话框
			closeChoiceDialog();
			// 调用系统相机
			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// 生成photoPath
			File photoFile = new File(YMStorageUtil.getImagePath(getActivity()), UUID.randomUUID().toString() + ".jpg");
			photoPath = photoFile.getPath();
			// uri
			Uri photoUri = Uri.fromFile(new File(photoPath));
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
			cameraIntent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
			// start
			getActivity().startActivityForResult(cameraIntent, REQUEST_CODE_HEAD_CAMERA);
			break;
		case R.id.account_head_choice_cancel:
			// 关闭对话框
			closeChoiceDialog();
			break;
		default:
			break;
		}
	}

	/**
	 * 关闭对话框
	 */
	private void closeChoiceDialog() {
		if (dlgChoice != null && dlgChoice.isShowing()) {
			dlgChoice.cancel();
		}
	}

	@Override
	public boolean acceptRequest(int requestCode) {
		switch (requestCode) {
		case REQUEST_CODE_HEAD_ALBUM:
		case REQUEST_CODE_HEAD_CAMERA:
		case REQUEST_CODE_HEAD_CROP:
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_HEAD_ALBUM:
				PhotoItem photoItem = (PhotoItem) data.getSerializableExtra(AlbumPhotoActivity.BUNDLE_RETURN_PHOTO);
				startCrop(photoItem.getPhotoPath());
				break;
			case REQUEST_CODE_HEAD_CAMERA:
				startCrop(photoPath);
				break;
			case REQUEST_CODE_HEAD_CROP:
				Bundle extras = data.getExtras();
				if (extras != null) {
					Bitmap cropPhoto = extras.getParcelable("data");
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					// (0 - 100)压缩文件
					cropPhoto.compress(Bitmap.CompressFormat.JPEG, 75, stream);

					File cropFile = new File(YMStorageUtil.getImagePath(getActivity()), UUID.randomUUID().toString()
							+ ".jpg");
					FileUtils.compressBmpToFile(cropPhoto, cropFile);
					// 调用上传
					HeadUploadTask task = new HeadUploadTask();
					task.execute(cropFile.getPath());
				}
				break;
			default:
				break;
			}
		}
	}

	// Intent intent = new Intent(getActivity(), AccountHeadActivity.class);
	// intent.putExtra(AccountHeadActivity.EXTRA_IMAGE_PATH, photoItem.getPhotoPath());
	// getActivity().startActivity(intent);

	private void startCrop(String imagePath) {
		Uri uri = Uri.fromFile(new File(imagePath));
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 160);
		intent.putExtra("outputY", 160);
		intent.putExtra("return-data", true);
		getActivity().startActivityForResult(intent, REQUEST_CODE_HEAD_CROP);
	}

	@Override
	public View initFuncView(boolean isSeparator, Object... params) {
		View view = super.initFuncView(isSeparator, params);
		// 内容view
		LinearLayout funcContent = (LinearLayout) view.findViewById(R.id.func_content);
		LayoutParams layoutParams = funcContent.getLayoutParams();
		// 设height
		layoutParams.height = getActivity().getResources().getDimensionPixelSize(R.dimen.account_info_head_height);
		return view;
	}

	@Override
	protected void initCustomView(LinearLayout customView) {
		// head
		imageHead = new ImageView(getActivity());
		// layout params
		int headSize = getActivity().getResources().getDimensionPixelSize(R.dimen.account_info_head_size);
		LayoutParams layoutParams = new LayoutParams(headSize, headSize);
		imageHead.setLayoutParams(layoutParams);
		// 添加到customView
		customView.addView(imageHead);
		customView.setGravity(Gravity.RIGHT);
		customView.setVisibility(View.VISIBLE);
	}

	@Override
	public void bindView() {
		// VCard
		YMVCard vCardEntity = ((AccountActivity) getActivity()).getYMVCard();
		if (vCardEntity == null) {
			return;
		}
		// 设头像
		AccountActivity accountActivity = (AccountActivity) getActivity();
		accountActivity.getBitmapCacheManager().loadFormCache(XMPPHelper.getFullFilePath(vCardEntity.getAvatar()),
				imageHead);
	}

	/**
	 * 更新VCard
	 * 
	 * @param path
	 */
	private void updateVCard(String path) {
		AccountActivity activity = (AccountActivity) getActivity();
		// YMVCard
		YMVCard vCardEntity = activity.getYMVCard();
		if (vCardEntity == null) {
			return;
		}
		// 改变头像
		vCardEntity.setAvatar(path);
		activity.updateVCard(vCardEntity);
	}

	class HeadUploadTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			// 文件上传task
			MessageResUploadTask uploadAvatarTask = new MessageResUploadTask();
			// 文件
			File file = new File(params[0]);
			String userJid = YYIMSessionManager.getInstance().getUserJid();
			// 上传并取得结果
			boolean uploadResult = uploadAvatarTask.upload(file, "", userJid, file.getName(), null);
			// 上传成功更新VCard
			if (uploadResult && !TextUtils.isEmpty(uploadAvatarTask.getBaredPath())) {
				updateVCard(uploadAvatarTask.getBaredPath());
			} else {
				YMLogger.d("没有上传成功");
			}
			return null;
		}

	}

}
