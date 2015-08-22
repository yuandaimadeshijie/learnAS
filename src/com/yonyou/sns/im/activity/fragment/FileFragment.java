package com.yonyou.sns.im.activity.fragment;

import com.yonyou.sns.im.entity.IFileSelectListener;

/**
 * 文件选择基类
 * @author wudl
 * @date 2014年12月2日
 * @version V1.0
 */
public abstract class FileFragment extends BaseFragment {

	/** 是否选择 */
	private boolean isSelect;
	/** 是sd卡还是内存卡*/
	private boolean isExternal = true;

	/** 监听 */
	private IFileSelectListener fileSelectListener;

	/**
	 * 数据变化
	 */
	public abstract void dataChanged();

	/**
	 * @return the isSelect
	 */
	public boolean isSelect() {
		return isSelect;
	}

	/**
	 * @param isSelect the isSelect to set
	 */
	public void setSelect(boolean isSelect) {
		this.isSelect = isSelect;
	}

	/**
	 * @param isExternal the isSelect to set
	 */
	public boolean isExternal() {
		return isExternal;
	}

	/**
	 * @return the isSelect
	 */
	public void setExternal(boolean isExternal) {
		this.isExternal = isExternal;
	}

	/**
	 * @return the userSelectListener
	 */
	public IFileSelectListener getFileSelectListener() {
		return fileSelectListener;
	}

	/**
	 * @param userSelectListener the userSelectListener to set
	 */
	public void setFileSelectListener(IFileSelectListener fileSelectListener) {
		this.fileSelectListener = fileSelectListener;
	}
}
