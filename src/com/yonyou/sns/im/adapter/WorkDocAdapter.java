package com.yonyou.sns.im.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.WorkDocActivity;
import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.entity.FileItem;
import com.yonyou.sns.im.entity.YMFile;
import com.yonyou.sns.im.ui.widget.quickaction.ActionItem;
import com.yonyou.sns.im.ui.widget.quickaction.QuickAction;
import com.yonyou.sns.im.ui.widget.quickaction.QuickAction.OnActionItemClickListener;
import com.yonyou.sns.im.util.common.FileUtils;
import com.yonyou.sns.im.util.common.TimeUtil;

/**
 * 工作文档adapter,查询File数据库
 * @author wudl
 * @date 2014年12月15日
 * @version V1.0
 */
public class WorkDocAdapter extends BaseAdapter {

	/** 删除action*/
	private static final int FILE_DELETE = 0;
	/** 取消action*/
	private static final int FILE_CANCEL = 1;
	/** 列表数据源*/
	private List<YMFile> list;
	/** context*/
	private Context context;

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

	public WorkDocAdapter(Context context, List<YMFile> list) {
		this.context = context;
		this.list = list;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int paramInt) {
		return list.get(paramInt);
	}

	@Override
	public long getItemId(int paramInt) {
		return paramInt;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		final YMFile file = list.get(position);
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
		viewHolder.iconView.setImageResource(FileItem.getFileIcon(file.getFileName()));
		// name
		viewHolder.nameText.setText(file.getFileName());
		// fileSize
		viewHolder.fileSize.setText(FileUtils.bytes2kb(file.getFileSize()));
		// 重设info
		String name = YYIMChatManager.getInstance().getNameByJid(file.getOppoJid());
		String time = TimeUtil.parseTimeExplicit(file.getDate());
		String info = FileUtils.bytes2kb(file.getFileSize()) + "/" + name + "/" + time;
		viewHolder.fileSize.setText(info);
		// 绑定事件
		bindWorkDocView(convertView, viewHolder, file);
		return convertView;
	}

	private void bindWorkDocView(final View convertView, ViewHolder viewHolder, final YMFile file) {
		viewHolder.sendButton.setVisibility(View.VISIBLE);
		viewHolder.listClick.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				((WorkDocActivity) context).sendFile(file);
			}
		});
		viewHolder.listItem.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				FileUtils.openFile(file.getFileName(), file.getFilePath(), context);
			}
		});
		viewHolder.listItem.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				showChildQuickActionBar(convertView, file);
				return false;
			}
		});
	}

	/**
	 * 获取资源字符串
	 * @param id
	 * @return
	 */
	private String getString(int id) {
		return context.getResources().getString(id);
	}

	/**
	 * 获取资源图片
	 * @param id
	 * @return
	 */
	private Drawable getDrawable(int id) {
		return context.getResources().getDrawable(id);
	}

	/**
	 * 长按联系人出来的弹出框
	 * 
	 * @param view
	 * @param roster
	 */
	private void showChildQuickActionBar(View view, final YMFile fileItem) {
		final QuickAction quickAction = new QuickAction(context, QuickAction.HORIZONTAL);
		quickAction.addActionItem(new ActionItem(FILE_DELETE, getString(R.string.delete),
				getDrawable(R.drawable.friend_delete)));
		quickAction.addActionItem(new ActionItem(FILE_CANCEL, getString(R.string.cancel),
				getDrawable(R.drawable.friend_cancel)));
		quickAction.setOnActionItemClickListener(new OnActionItemClickListener() {

			@Override
			public void onItemClick(QuickAction source, int pos, int actionId) {
				switch (actionId) {
				case FILE_DELETE:
					if (YYIMChatManager.getInstance().deleteFileById(fileItem.getId()) >= 0) {
						list.remove(fileItem);
						WorkDocAdapter.this.notifyDataSetChanged();
					}
					break;
				case FILE_CANCEL:
					quickAction.onDismiss();
					break;
				default:
					break;
				}
			}
		});
		quickAction.show(view);
	}
}
