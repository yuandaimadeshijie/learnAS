package com.yonyou.sns.im.entity;

import com.yonyou.sns.im.activity.fragment.FileFragment;

/**
 * 文件选择监听
 * @author wudl
 * @date 2014年12月2日
 * @version V1.0
 */
public interface IFileSelectListener {

	public void selectChange(FileItem fileItem, boolean isChecked);

	public void changeFragment(FileFragment fragment,String tag);

}
