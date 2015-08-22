package com.yonyou.sns.im.activity.fragment;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.adapter.FileAdapter;
import com.yonyou.sns.im.entity.FileItem;
import com.yonyou.sns.im.entity.IFileSelectListener;
import com.yonyou.sns.im.util.inject.InjectView;

/**
 * 本地文件列表
 * @author wudl
 * @date 2014年12月2日
 * @version V1.0
 */
public class LocalFileListFragment extends FileFragment {

	public static final String TAG = "LocalFileListFragment";
	/** 文件列表*/
	@InjectView(id = R.id.local_file_list)
	private ListView fileListView;
	/** empty view*/
	@InjectView(id = R.id.search_empty_view)
	private View emptyView;
	/** 列表数据源*/
	List<FileItem> list;

	/**
	 * 设置数据源
	 * @param list
	 */
	public void setList(List<FileItem> list) {
		this.list = list;
	}

	@Override
	public void dataChanged() {

	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_local_file_list;
	}

	@Override
	protected void initView(LayoutInflater inflater, View view) {
		// 设置监听器
		if (getFragmentActivity() instanceof IFileSelectListener) {
			setFileSelectListener((IFileSelectListener) getFragmentActivity());
			fileListView.setEmptyView(emptyView);
			FileAdapter adapter = new FileAdapter(list, getFragmentActivity(), isSelect());
			adapter.setExternal(isExternal());
			fileListView.setAdapter(adapter);
			((FileAdapter) (fileListView.getAdapter())).notifyDataSetChanged();
		}
	}
}
