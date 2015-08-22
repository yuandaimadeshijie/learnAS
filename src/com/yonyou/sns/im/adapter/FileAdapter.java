package com.yonyou.sns.im.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.LocalFileActivity;
import com.yonyou.sns.im.activity.fragment.LocalFileListFragment;
import com.yonyou.sns.im.entity.FileItem;
import com.yonyou.sns.im.util.common.FileUtils;
import com.yonyou.sns.im.util.common.ToastUtil;

/**
 * 文件列表适配器,查询MediaStore系统数据库
 * @author wudl
 * @date 2014年12月3日
 * @version V1.0
 */
public class FileAdapter extends BaseAdapter {

	/** 列表数据源*/
	private List<FileItem> list;
	/** context*/
	private Context context;
	/** isSelected*/
	private boolean isSelect = true;
	/** 是sd卡还是内存卡*/
	private boolean isExternal = true;

	public FileAdapter(List<FileItem> list, Context context, boolean isSelect) {
		this.list = list;
		this.context = context;
		this.isSelect = isSelect;
	}

	/**
	 * 设置是sd卡还是内存卡
	 * @param isExternal
	 */
	public void setExternal(boolean isExternal) {
		this.isExternal = isExternal;
	}

	/**
	 * 是否是选择
	 * @return
	 */
	public boolean isSelect() {
		return isSelect;
	}

	/**
	 * 变换fragment
	 */
	private void changeFragment(List<FileItem> list, String tag) {
		LocalFileListFragment localFileFragment = (LocalFileListFragment) ((LocalFileActivity) context)
				.getSupportFragmentManager().findFragmentByTag(tag);
		if (localFileFragment == null) {
			localFileFragment = new LocalFileListFragment();
			localFileFragment.setList(list);
			localFileFragment.setSelect(true);
			localFileFragment.setExternal(isExternal);
		}
		((LocalFileActivity) context).changeFragment(localFileFragment, tag);
	}

	/**
	 * 获取手机SD根目录文件
	 * @return
	 */
	private List<FileItem> getFileList(int id) {
		/** 选择列*/
		String[] columns = new String[] { MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns._ID,
				MediaStore.Files.FileColumns.SIZE, MediaStore.Files.FileColumns.PARENT };
		Uri uri;
		if (isExternal) {
			// 外部存储器uri
			uri = MediaStore.Files.getContentUri("external");
		} else {
			// 内部存储器uri
			uri = MediaStore.Files.getContentUri("internal");
		}
		String selection = MediaStore.Files.FileColumns.PARENT + "=" + id;
		Cursor cursor = context.getContentResolver().query(uri, columns, selection, null,
				MediaStore.Files.FileColumns.TITLE);
		List<FileItem> list = new ArrayList<FileItem>();
		while (cursor.moveToNext()) {
			FileItem fileItem = new FileItem(cursor);
			list.add(fileItem);
		}
		cursor.close();
		return list;
	}

	static class ViewHolder {

		ImageView iconView;
		TextView nameText;
		TextView fileSize;
		CheckBox checkBox;
		ImageView arrowView;
		LinearLayout listItem;
		View sendButton;
		View listClick;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		final FileItem fileItem = list.get(position);
		if (convertView == null) {
			// 创建新的holder
			viewHolder = new ViewHolder();
			// inflate view
			convertView = LayoutInflater.from(context).inflate(R.layout.local_file_list_item, parent, false);
			// init holder
			viewHolder.iconView = (ImageView) convertView.findViewById(R.id.local_file_item_icon);
			viewHolder.nameText = (TextView) convertView.findViewById(R.id.local_file_item_name);
			viewHolder.fileSize = (TextView) convertView.findViewById(R.id.local_file_item_size);
			viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.local_file_item_checkbox);
			viewHolder.arrowView = (ImageView) convertView.findViewById(R.id.local_file_item_arrow);
			viewHolder.listItem = (LinearLayout) convertView.findViewById(R.id.local_file_item);
			viewHolder.sendButton = convertView.findViewById(R.id.work_doc_item_send);
			viewHolder.listClick = convertView.findViewById(R.id.local_file_click);
			// set tag
			convertView.setTag(R.string.HOLDER_TAG_VIEW_HOLDER, viewHolder);
		} else {
			// get from tag
			viewHolder = (ViewHolder) convertView.getTag(R.string.HOLDER_TAG_VIEW_HOLDER);
		}
		// 设置icon
		viewHolder.iconView.setImageResource(fileItem.getFileIcon());
		// name
		viewHolder.nameText.setText(fileItem.getFileName());
		// fileSize
		viewHolder.fileSize.setText(FileUtils.bytes2kb(fileItem.getSize()));
		// checkBox and arrow and button
		bindLocalFileView(viewHolder, fileItem, convertView);
		return convertView;
	}

	/**
	 * 绑定本地文件view
	 * @param viewHolder
	 * @param fileItem
	 */
	private void bindLocalFileView(ViewHolder viewHolder, final FileItem fileItem, View convertView) {
		final File file = new File(fileItem.getFilePath());
		if (file.isDirectory()) {
			// 表示文件夹
			viewHolder.checkBox.setVisibility(View.GONE);
			viewHolder.arrowView.setVisibility(View.VISIBLE);
			viewHolder.fileSize.setVisibility(View.GONE);
			viewHolder.iconView.setImageResource(R.drawable.local_file_root_file);
			viewHolder.listItem.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View paramView) {
					// 更换子页面
					changeFragment(getFileList(fileItem.getFileId()),
							LocalFileListFragment.TAG + "_" + fileItem.getFileId());
				}
			});
			// 阻塞冒泡
			viewHolder.listItem.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		} else if (isSelect()) {
			// 表示文件
			viewHolder.checkBox.setVisibility(View.VISIBLE);
			viewHolder.arrowView.setVisibility(View.GONE);
			viewHolder.fileSize.setVisibility(View.VISIBLE);
			viewHolder.listItem.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					FileUtils.openFile(fileItem.getFileName(), fileItem.getFilePath(), context);
				}
			});
			// 还原冒泡
			viewHolder.listItem.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
			viewHolder.listClick.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					boolean isChecked = !fileItem.isChecked();
					int totalFileNum = ((LocalFileActivity) context).getTotalFileNum();
					if (isChecked && (totalFileNum + 1) > LocalFileActivity.MAX_FILE_SIZE) {
						// 超出最大文件数
						fileItem.setChecked(false);
						ToastUtil.showLong(context, R.string.local_file_over_max_size);
					} else {
						// 更改选中项
						((LocalFileActivity) context).selectChange(fileItem, isChecked);
						fileItem.setChecked(isChecked);
					}
					FileAdapter.this.notifyDataSetChanged();
				}
			});
			viewHolder.checkBox.setChecked(fileItem.isChecked());
		}
	}

}
