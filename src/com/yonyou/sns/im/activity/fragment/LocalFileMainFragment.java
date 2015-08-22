package com.yonyou.sns.im.activity.fragment;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.entity.FileItem;
import com.yonyou.sns.im.entity.IFileSelectListener;
import com.yonyou.sns.im.ui.component.func.BaseFunc;
import com.yonyou.sns.im.ui.component.func.localfile.LocalFileDocFunc;
import com.yonyou.sns.im.ui.component.func.localfile.LocalFileFunc;
import com.yonyou.sns.im.ui.component.func.localfile.LocalFileImgFunc;
import com.yonyou.sns.im.ui.component.func.localfile.LocalFileMediaFunc;
import com.yonyou.sns.im.ui.component.func.localfile.LocalFileMusicFunc;
import com.yonyou.sns.im.ui.component.func.localfile.LocalFilePhoneMemery;
import com.yonyou.sns.im.ui.component.func.localfile.LocalFileSDMemery;
import com.yonyou.sns.im.util.inject.InjectView;

/**
 * 本地文件的主界面
 * @author wudl
 * @date 2014年12月2日
 * @version V1.0
 */
public class LocalFileMainFragment extends FileFragment implements OnClickListener {

	public static final String TAG = "LocalFileMainFragment";
	/** media func array*/
	private static final Class<?> mediaFuncArray[] = { LocalFileDocFunc.class, LocalFileImgFunc.class,
			LocalFileMediaFunc.class, LocalFileMusicFunc.class };
	/** phone func array*/
	private static final Class<?> phoneFuncArray[] = { LocalFilePhoneMemery.class, LocalFileSDMemery.class };
	/** media view*/
	@InjectView(id = R.id.local_file_media_view)
	private View mediaView;
	/** media view list*/
	@InjectView(id = R.id.local_file_media_func_list)
	private LinearLayout mediaViewList;
	/** phone view */
	@InjectView(id = R.id.local_file_phone_view)
	private View phoneView;
	/** phone view list*/
	@InjectView(id = R.id.local_file_phone_func_list)
	private LinearLayout phoneViewList;
	/** 文档list*/
	private List<FileItem> docList;
	/** 图片list*/
	private List<FileItem> imgList;
	/** 音频list*/
	private List<FileItem> musicList;
	/** 媒体文件*/
	private List<FileItem> mediaList;
	/** 手机内存文件*/
	private List<FileItem> PhoneMemList;
	/** SD内存文件*/
	private List<FileItem> SDMemList;
	/** 选择列*/
	private String[] columns = new String[] { MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns._ID,
			MediaStore.Files.FileColumns.SIZE, MediaStore.Files.FileColumns.PARENT, MediaStore.Files.FileColumns.TITLE,
			MediaStore.Files.FileColumns.MIME_TYPE };
	/** 外部存储器uri*/
	private Uri externalUri = MediaStore.Files.getContentUri("external");
	/** 内部存储器uri*/
	private Uri internalUri = MediaStore.Files.getContentUri("internal");
	/** doc selection*/
	private String docSelection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
			+ MediaStore.Files.FileColumns.MEDIA_TYPE_NONE + " and " + MediaStore.Files.FileColumns.TITLE
			+ " not like '.%'" + " and (" + MediaStore.Files.FileColumns.MIME_TYPE + "='text/plain'" + " or "
			+ MediaStore.Files.FileColumns.MIME_TYPE + "='application/msword'" + " or "
			+ MediaStore.Files.FileColumns.MIME_TYPE + "='application/pdf'" + " or "
			+ MediaStore.Files.FileColumns.MIME_TYPE
			+ "='application/vnd.openxmlformats-officedocument.wordprocessingml.document'" + " or "
			+ MediaStore.Files.FileColumns.MIME_TYPE + "='application/vnd.ms-excel'" + " or "
			+ MediaStore.Files.FileColumns.MIME_TYPE
			+ "='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'" + " or "
			+ MediaStore.Files.FileColumns.MIME_TYPE
			+ "='application/vnd.openxmlformats-officedocument.presentationml.presentation'" + " or "
			+ MediaStore.Files.FileColumns.MIME_TYPE + "='application/vnd.ms-powerpoint'" + " or "
			+ MediaStore.Files.FileColumns.MIME_TYPE + "='application/vnd.ms-works' )";
	/** image selection*/
	private String imgSelection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
			+ MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
	/** music selection*/
	private String musicSelection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
			+ MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO;
	/** media selection*/
	private String mediaSelection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
			+ MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_local_file_main;
	}

	@Override
	protected void initView(LayoutInflater inflater, View view) {
		if (getFragmentActivity() instanceof IFileSelectListener) {
			setFileSelectListener((IFileSelectListener) getFragmentActivity());

			// 获取数据
			iniData();
			// 初始化media view
			initMediaView();
			// 初始化phone view
			initPhoneView();
		}
	}

	@Override
	public void dataChanged() {

	}

	/**
	 * 获取本地文件数据
	 */
	private void iniData() {
		docList = getDocFileList();
		imgList = getImgFileList();
		musicList = getMusicFileList();
		mediaList = getMediaFileList();
		PhoneMemList = getPhoneMemFileList();
		SDMemList = getSDMemFileList();
	}

	/**
	 * 获取文档
	 * @return
	 */
	private List<FileItem> getDocFileList() {
		Cursor cursor = getFragmentActivity().getContentResolver().query(externalUri, columns, docSelection, null,
				MediaStore.Files.FileColumns.TITLE);
		List<FileItem> list = new ArrayList<FileItem>();
		while (cursor.moveToNext()) {
			FileItem fileItem = new FileItem(cursor);
			if (fileItem.getSize() > 0) {
				list.add(fileItem);
			}
		}
		cursor.close();
		return list;
	}

	/**
	 * 获取图片
	 * @return
	 */
	private List<FileItem> getImgFileList() {
		Cursor cursor = getFragmentActivity().getContentResolver().query(externalUri, columns, imgSelection, null,
				MediaStore.Files.FileColumns.TITLE);
		List<FileItem> list = new ArrayList<FileItem>();
		while (cursor.moveToNext()) {
			FileItem fileItem = new FileItem(cursor);
			if (fileItem.getSize() > 0) {
				list.add(fileItem);
			}
		}
		cursor.close();
		return list;
	}

	/**
	 * 获取音频文件
	 * @return
	 */
	private List<FileItem> getMusicFileList() {
		Cursor cursor = getFragmentActivity().getContentResolver().query(externalUri, columns, musicSelection, null,
				MediaStore.Files.FileColumns.TITLE);
		List<FileItem> list = new ArrayList<FileItem>();
		while (cursor.moveToNext()) {
			FileItem fileItem = new FileItem(cursor);
			if (fileItem.getSize() > 0) {
				list.add(fileItem);
			}
		}
		cursor.close();
		return list;
	}

	/**
	 * 获取媒体文件
	 * @return
	 */
	private List<FileItem> getMediaFileList() {
		Cursor cursor = getFragmentActivity().getContentResolver().query(externalUri, columns, mediaSelection, null,
				MediaStore.Files.FileColumns.SIZE + "," + MediaStore.Files.FileColumns.TITLE);
		List<FileItem> list = new ArrayList<FileItem>();
		while (cursor.moveToNext()) {
			FileItem fileItem = new FileItem(cursor);
			if (fileItem.getSize() > 0) {
				list.add(fileItem);
			}
		}
		cursor.close();
		return list;
	}

	/**
	 * 获取手机SD根目录文件
	 * @return
	 */
	private List<FileItem> getSDMemFileList() {
		String selection = MediaStore.Files.FileColumns.PARENT + "=0" + " and " + MediaStore.Files.FileColumns.TITLE
				+ " not like '.%'";
		Cursor cursor = getFragmentActivity().getContentResolver().query(externalUri, columns, selection, null,
				MediaStore.Files.FileColumns.TITLE);
		List<FileItem> list = new ArrayList<FileItem>();
		while (cursor.moveToNext()) {
			FileItem fileItem = new FileItem(cursor);
			list.add(fileItem);
		}
		cursor.close();
		return list;
	}

	/**
	 * 获取手机内存根目录文件
	 * @return
	 */
	private List<FileItem> getPhoneMemFileList() {
		String selection = MediaStore.Files.FileColumns.PARENT + "=0" + " and " + MediaStore.Files.FileColumns.TITLE
				+ " not like '.%'";
		Cursor cursor = getFragmentActivity().getContentResolver().query(internalUri, columns, selection, null,
				MediaStore.Files.FileColumns.TITLE);
		List<FileItem> list = new ArrayList<FileItem>();
		while (cursor.moveToNext()) {
			FileItem fileItem = new FileItem(cursor);
			list.add(fileItem);
		}
		cursor.close();
		return list;
	}

	/**
	 * 获取媒体list
	 * @return
	 */
	public static Class<?>[] getMediafuncarray() {
		return mediaFuncArray;
	}

	/**
	 * 获取phone list
	 * @return
	 */
	public static Class<?>[] getPhonefuncarray() {
		return phoneFuncArray;
	}

	/**
	 * 获取docList
	 * @return
	 */
	public List<FileItem> getDocList() {
		return docList;
	}

	/**
	 * 获取musicList
	 * @return
	 */
	public List<FileItem> getMusicList() {
		return musicList;
	}

	/**
	 * 获取mediaList
	 * @return
	 */
	public List<FileItem> getMediaList() {
		return mediaList;
	}

	/**
	 * 获取imgList
	 * @return
	 */
	public List<FileItem> getImgList() {
		return imgList;
	}

	/**
	 * 初始化mediaView
	 */
	private void initMediaView() {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		Class<?>[] mediaFuncs = getMediafuncarray();
		// 功能列表为空,隐藏区域
		if (mediaFuncs == null || mediaFuncs.length == 0) {
			mediaView.setVisibility(View.GONE);
			return;
		}
		// 初始化功能
		boolean isSeparator = true;
		for (Class<?> systemFunc : mediaFuncs) {
			// view
			View funcView = getFuncView(inflater, systemFunc, isSeparator);
			if (funcView != null) {
				// 点击事件
				funcView.setOnClickListener(this);
				// 加入页面
				mediaViewList.addView(funcView);
			}
		}
		// 设置列表显示
		mediaView.setVisibility(View.VISIBLE);
	}

	/**
	 * 初始化phone view
	 */
	private void initPhoneView() {
		LayoutInflater inflater = LayoutInflater.from(getFragmentActivity());
		Class<?>[] phoneFuncs = getPhonefuncarray();
		// 功能列表为空,隐藏区域
		if (phoneFuncs == null || phoneFuncs.length == 0) {
			phoneView.setVisibility(View.GONE);
			return;
		}
		// 初始化功能
		boolean isSeparator = true;
		for (Class<?> phoneFunc : phoneFuncs) {
			// view
			View funcView = getFuncView(inflater, phoneFunc, isSeparator);
			if (funcView != null) {
				// 点击事件
				funcView.setOnClickListener(this);
				// 加入页面
				phoneViewList.addView(funcView);
			}
		}
		// 设置列表显示
		phoneView.setVisibility(View.VISIBLE);
	}

	/**
	 * 获得功能View
	 * 
	 * @param inflater
	 * @param func
	 * @return
	 */
	private View getFuncView(LayoutInflater inflater, Class<?> funcClazz, boolean isSeparator) {
		BaseFunc func = BaseFunc.newInstance(funcClazz, getFragmentActivity());
		if (func == null) {
			return null;
		}
		// 设置ref
		((LocalFileFunc) func).setReference(LocalFileMainFragment.this);
		// view
		View funcView = func.initFuncView(isSeparator);
		return funcView;
	}

	/**
	 * 变换fragment
	 */
	private void changeFragment(List<FileItem> list, boolean isExternal,String tag) {
		LocalFileListFragment localFileFragment = (LocalFileListFragment) getFragmentActivity()
				.getSupportFragmentManager().findFragmentByTag(tag);
		if(localFileFragment==null){
			localFileFragment = new LocalFileListFragment();
			localFileFragment.setList(list);
			localFileFragment.setSelect(true);
			localFileFragment.setExternal(isExternal);
		}
		getFileSelectListener().changeFragment(localFileFragment,tag);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.local_file_document:
			changeFragment(docList, true,LocalFileListFragment.TAG+"_DOC");
			break;
		case R.id.local_file_image:
			changeFragment(imgList, true,LocalFileListFragment.TAG+"_IMG");
			break;
		case R.id.local_file_media:
			changeFragment(mediaList, true,LocalFileListFragment.TAG+"_FILE");
			break;
		case R.id.local_file_music:
			changeFragment(musicList, true,LocalFileListFragment.TAG+"_MUSIC");
			break;
		case R.id.local_file_phone_memery:
			changeFragment(PhoneMemList, false,LocalFileListFragment.TAG+"_PHONE");
			break;
		case R.id.local_file_SD_memery:
			changeFragment(SDMemList, true,LocalFileListFragment.TAG+"_SD");
			break;
		default:
			break;
		}
	}
}
