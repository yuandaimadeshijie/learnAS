package com.yonyou.sns.im.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.fragment.FileFragment;
import com.yonyou.sns.im.activity.fragment.LocalFileMainFragment;
import com.yonyou.sns.im.entity.FileItem;
import com.yonyou.sns.im.entity.IFileSelectListener;
import com.yonyou.sns.im.ui.component.topbar.LocalFileBackBtnFunc;
import com.yonyou.sns.im.util.common.FileUtils;
import com.yonyou.sns.im.util.common.YMStringUtils;
import com.yonyou.sns.im.util.inject.InjectView;

/**
 * 本地文档
 * @author wudl
 * @date 2014年12月1日
 * @version V1.0
 */
public class LocalFileActivity extends SimpleTopbarActivity implements OnClickListener, IFileSelectListener {

	/** 允许发送文件的个数*/
	public static final int MAX_FILE_SIZE = 5;
	/** local file list*/
	public static final String LOCAL_FILE_LIST = "LOCAL_FILE_LIST";
	/** 发送按钮*/
	@InjectView(id = R.id.local_file_send)
	private Button sendFile;
	/** 文件大小*/
	@InjectView(id = R.id.local_file_size)
	private TextView fileSize;
	/** 选择列表*/
	private List<FileItem> selectedFile = new ArrayList<FileItem>();
	/** 总文件大小*/
	private long totalFileSize = 0;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_local_file);
		// 加载中间内容页面
		showMainView();
		// 初始化bottom view
		changeBottomView();
		sendFile.setOnClickListener(this);
	}

	/**
	 * 更改界面的文件大小显示
	 */
	private void changeBottomView() {
		String content = getResources().getString(R.string.local_file_text, FileUtils.bytes2kb(totalFileSize));
		fileSize.setText(YMStringUtils.initStyle(content, FileUtils.bytes2kb(totalFileSize),
				getResources().getColor(R.color.camera_edit_confirm_normal)));
		if (totalFileSize <= 0) {
			sendFile.setText(R.string.local_file_send);
			sendFile.setEnabled(false);
		} else {
			sendFile.setText(getResources().getString(R.string.local_file_send_hl, String.valueOf(selectedFile.size())));
			sendFile.setEnabled(true);
		}
	}

	/**
	 * 展示main view
	 */
	private void showMainView() {
		LocalFileMainFragment LocalFileFragment = (LocalFileMainFragment) getSupportFragmentManager()
				.findFragmentByTag(LocalFileMainFragment.TAG);
		if (LocalFileFragment == null) {
			LocalFileFragment = new LocalFileMainFragment();
			LocalFileFragment.setSelect(true);
		}
		changeFragment(LocalFileFragment,LocalFileMainFragment.TAG);
	}

	/**
	 * 获取总的文件大小
	 * @return
	 */
	public long getTotalFileSize() {
		return totalFileSize;
	}

	/**
	 * 获取总的文件个数
	 */
	public int getTotalFileNum() {
		return selectedFile.size();
	}

	@Override
	protected Object getTopbarTitle() {
		return R.string.local_file_title;
	}

	protected Class<?> getTopbarLeftFunc() {
		return LocalFileBackBtnFunc.class;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 返回按钮监听
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
				getSupportFragmentManager().popBackStack();
			} else {
				finish();
			}
            return true;
        }
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.local_file_send:
			Intent intent = getIntent();
			Bundle bundle = new Bundle();
			bundle.putSerializable(LOCAL_FILE_LIST, (Serializable) selectedFile);
			intent.putExtras(bundle);
			setResult(Activity.RESULT_OK, intent);
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	public void selectChange(FileItem fileItem, boolean isChecked) {
		if (isChecked) {
			selectedFile.add(fileItem);
			totalFileSize += fileItem.getSize();
		} else {
			selectedFile.remove(fileItem);
			totalFileSize -= fileItem.getSize();
		}
		changeBottomView();
	}

	@Override
	public void changeFragment(FileFragment fragment,String tag) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom);
		ft.replace(R.id.local_file_frame, fragment,tag);
		ft.addToBackStack(null);
		ft.commit();
	}
}
