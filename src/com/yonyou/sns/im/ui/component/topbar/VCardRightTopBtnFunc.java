package com.yonyou.sns.im.ui.component.topbar;

import org.jivesoftware.smack.util.StringUtils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.VCardActivity;
import com.yonyou.sns.im.core.YYIMCallBack;
import com.yonyou.sns.im.core.YYIMRosterManager;
import com.yonyou.sns.im.entity.YMRoster;
import com.yonyou.sns.im.ui.widget.CustomDialog;
import com.yonyou.sns.im.ui.widget.CustomDialog.Builder;
import com.yonyou.sns.im.ui.widget.quickaction.ActionItem;
import com.yonyou.sns.im.ui.widget.quickaction.QuickAction;
import com.yonyou.sns.im.ui.widget.quickaction.QuickAction.OnActionItemClickListener;

public class VCardRightTopBtnFunc extends BaseTopImageBtnFunc {

	public VCardRightTopBtnFunc(Activity activity) {
		super(activity);
	}

	@Override
	public int getFuncId() {
		return R.id.vcard_func_delete;
	}

	@Override
	public int getFuncIcon() {
		return R.drawable.select_topbar_right_point;
	}

	@Override
	public void onclick(View v) {
		showVCardActionBar(v);
	}

	/**
	 * 创建群聊/会议的响应事件
	 * 
	 * @param view
	 */
	public void showVCardActionBar(View view) {
		QuickAction quickAction = new QuickAction(getActivity(), QuickAction.VERTICAL);
		final String jid=((VCardActivity) getActivity()).getJid();
		final YMRoster roster=YYIMRosterManager.getInstance().getRosterByJid(jid);
		if(StringUtils.isEmpty(roster.getJid())){
			quickAction.addActionItem(new ActionItem(0, getActivity().getString(R.string.addFriend_Title), getActivity()
					.getResources().getDrawable(R.drawable.add_friend_icon)));
		}else{
			quickAction.addActionItem(new ActionItem(0, getActivity().getString(R.string.account_delete), getActivity()
					.getResources().getDrawable(R.drawable.vcard_delete_icon_white)));
		}
		quickAction.setOnActionItemClickListener(new OnActionItemClickListener() {

			@Override
			public void onItemClick(QuickAction source, int pos, int actionId) {
				switch (actionId) {
				case 0: {
					if(StringUtils.isEmpty(roster.getJid())){
						// 陌生人显示添加好友
						YYIMRosterManager.getInstance().addRoster(jid, new YYIMCallBack() {
							
							@Override
							public void onSuccess(Object object) {
							}
							
							@Override
							public void onProgress(int progress, String status) {
							}
							
							@Override
							public void onError(int errno, String errmsg) {
							}
						});
					}else{
						// 删除好友弹出框
						Builder builder = new CustomDialog.Builder(getActivity());
						builder.setTitle(R.string.vcard_delete_title);
						// 设置内容
						builder.setMessage(R.string.vcard_delete_message);
						// 确定按钮
						builder.setPositiveButton(android.R.string.ok, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								((VCardActivity) getActivity()).removeRoster();
							}
						});
						builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {

							}
						});
						// 创建弹出框并显示
						builder.create().show();
					}
					break;
				}
				default:
					break;
				}
			}
		});
		quickAction.show(view);
		quickAction.setAnimStyle(QuickAction.ANIM_AUTO);
	}

}
