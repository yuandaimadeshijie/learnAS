package com.yonyou.sns.im.adapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.VCardActivity;
import com.yonyou.sns.im.core.YYIMCallBack;
import com.yonyou.sns.im.core.YYIMRosterManager;
import com.yonyou.sns.im.entity.SearchValue;
import com.yonyou.sns.im.exception.YMErrorConsts;
import com.yonyou.sns.im.smack.IMErrorConsts;
import com.yonyou.sns.im.smack.IMSmackManager;
import com.yonyou.sns.im.util.DialogUtil;
import com.yonyou.sns.im.util.XMPPHelper;
import com.yonyou.sns.im.util.bitmap.BitmapCacheManager;
import com.yonyou.sns.im.util.common.ToastUtil;
import com.yonyou.sns.im.util.common.YMStringUtils;

public class RosterSearchAdapter extends UserAdapter {

	/** 发送添加好友成功*/
	private static final int SEND_ADD_ROSTER_SUCCESS = 0;
	/** 发送添加好友失败*/
	private static final int SEND_ADD_ROSTER_FALIED = 1;
	/** 查询成功*/
	public static final int MSG_SEARCH_SUCCESS = 2;
	/** 查询失败*/
	public static final int MSG_SEARCH_FAILD = 3;
	/** Context */
	private Context context;
	/** LayoutInflater */
	private LayoutInflater inflater;
	/** 存放当前adapter中分组的集合 */
	private List<SearchValue> searchValueList = new ArrayList<SearchValue>();
	/** 关键字 */
	private String key;
	/** 图片缓存管理器 */
	private BitmapCacheManager avatarBitmapCacheManager;
	/** dialog */
	private Dialog progressDialog;
	/** handler*/
	private RosterSearchHandler handler = new RosterSearchHandler(this);

	public RosterSearchAdapter(Context context) {
		this(context, false);
	}

	public RosterSearchAdapter(Context context, boolean isSelect) {
		super(isSelect);
		this.context = context;

		this.inflater = LayoutInflater.from(context);

		// 这个是URL头像的缓存任务
		avatarBitmapCacheManager = new BitmapCacheManager(context, true, BitmapCacheManager.CIRCLE_BITMAP,
				BitmapCacheManager.BITMAP_DPSIZE_40);
		avatarBitmapCacheManager.generateBitmapCacheWork();
	}

	/**
	 * 重新搜索
	 */
	public void requery(String key) {
		getProgressDialog().show();
		this.key = key;
		// 搜索
		IMSmackManager.getInstance().searchRosters(key, new YYIMCallBack() {

			@SuppressWarnings("unchecked")
			@Override
			public void onSuccess(Object object) {
				searchValueList = (List<SearchValue>) object;
				handler.sendEmptyMessage(MSG_SEARCH_SUCCESS);
			}

			@Override
			public void onProgress(int progress, String status) {
			}

			@Override
			public void onError(int errno, String errmsg) {
				// 清空查询
				searchValueList.clear();
				switch (errno) {
				case YMErrorConsts.ERROR_AUTHORIZATION:
					handler.obtainMessage(MSG_SEARCH_FAILD, "连接已断开").sendToTarget();
					break;
				case YMErrorConsts.EXCEPTION_NORESPONSE:
					handler.obtainMessage(MSG_SEARCH_FAILD, "服务器未响应").sendToTarget();
					break;
				case IMErrorConsts.EXCEPTION_SEARCH_ROSTER:
					handler.obtainMessage(MSG_SEARCH_FAILD, "搜索联系人失败").sendToTarget();
					break;
				case IMErrorConsts.EXCEPTION_SEARCH_EMPTY:
					handler.obtainMessage(MSG_SEARCH_FAILD, "未查到数据").sendToTarget();
					break;
				case YMErrorConsts.EXCEPTION_UNKNOWN:
					handler.obtainMessage(MSG_SEARCH_FAILD, "未知异常").sendToTarget();
					break;
				default:
					break;
				}
				
			}
		});
	}

	/**
	 * 获取progress dialog
	 * @return
	 */
	private Dialog getProgressDialog() {
		if (progressDialog == null) {
			progressDialog = DialogUtil.getProgressDialog((Activity) context, R.string.searching);
		}
		return progressDialog;
	}

	static class ViewHolder {

		ImageView iconView;
		TextView nameText;
		TextView emailText;
		TextView resultText;
		Button acceptBtn;
		CheckBox checkBox;

	}

	@Override
	public int getCount() {
		return searchValueList.size();
	}

	@Override
	public SearchValue getItem(int position) {
		return searchValueList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// 当前数据
		final SearchValue result = (SearchValue) searchValueList.get(position);

		ViewHolder viewHolder = null;
		if (convertView == null) {
			// 创建新的holder
			viewHolder = new ViewHolder();
			// inflate view
			convertView = inflater.inflate(R.layout.roster_search_list_item, parent, false);
			// init holder
			viewHolder.iconView = (ImageView) convertView.findViewById(R.id.roster_search_icon);
			viewHolder.nameText = (TextView) convertView.findViewById(R.id.roster_search_name);
			viewHolder.emailText = (TextView) convertView.findViewById(R.id.roster_search_email);
			viewHolder.resultText = (TextView) convertView.findViewById(R.id.roster_search_result);
			viewHolder.acceptBtn = (Button) convertView.findViewById(R.id.roster_search_accept);
			viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.roster_search_checkbox);
			// set tag
			convertView.setTag(R.string.HOLDER_TAG_VIEW_HOLDER, viewHolder);
		} else {
			// get from tag
			viewHolder = (ViewHolder) convertView.getTag(R.string.HOLDER_TAG_VIEW_HOLDER);
		}

		// 名称
		SpannableStringBuilder name = YMStringUtils.initStyle(result.getUserName(), key, this.context.getResources()
				.getColor(R.color.search_result_key));
		viewHolder.nameText.setText(name);
		// email
		viewHolder.emailText.setText(result.getEmail());
		// 头像
		avatarBitmapCacheManager.loadFormCache(XMPPHelper.getFullFilePath(result.getPhoto()), viewHolder.iconView);

		if (!XMPPHelper.isSystemChat(result.getJid())) {
			// 头像点击事件
			viewHolder.iconView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// 跳转到VCard页面
					Intent intent = new Intent(context, VCardActivity.class);
					intent.putExtra(VCardActivity.EXTRA_JID, result.getJid());
					context.startActivity(intent);
				}
			});
		}

		if (isSelect()) {
			if (isAlreadyExistsMembers(result.getJid())) {
				viewHolder.checkBox.setEnabled(false);
				viewHolder.checkBox.setChecked(true);
			} else {
				// checkbox
				viewHolder.checkBox.setChecked(isUserSelected(result.getJid()));
				// 绑定事件
				viewHolder.checkBox.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						boolean isChecked = ((CheckBox) v).isChecked();
						// 设置选中状态
						selectChange(result, isChecked);
					}

				});
			}
			viewHolder.checkBox.setVisibility(View.VISIBLE);
			viewHolder.resultText.setVisibility(View.GONE);
			viewHolder.acceptBtn.setVisibility(View.GONE);
		} else if (isCustomClick()) {
			viewHolder.checkBox.setVisibility(View.GONE);
			viewHolder.resultText.setVisibility(View.GONE);
			viewHolder.acceptBtn.setVisibility(View.GONE);
		} else {
			viewHolder.checkBox.setVisibility(View.GONE);
			if (result.isFriend()) {
				viewHolder.resultText.setVisibility(View.VISIBLE);
				viewHolder.acceptBtn.setVisibility(View.GONE);
			} else {
				viewHolder.resultText.setVisibility(View.GONE);
				viewHolder.acceptBtn.setVisibility(View.VISIBLE);
				viewHolder.acceptBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						YYIMRosterManager.getInstance().addRoster(result.getJid(), new YYIMCallBack() {

							@Override
							public void onSuccess(Object object) {
								handler.sendEmptyMessage(SEND_ADD_ROSTER_SUCCESS);
							}

							@Override
							public void onProgress(int progress, String status) {
							}

							@Override
							public void onError(int errno, String errmsg) {
								switch (errno) {
								case YMErrorConsts.ERROR_AUTHORIZATION:
									handler.obtainMessage(SEND_ADD_ROSTER_FALIED, "连接已断开").sendToTarget();
									break;
								case YMErrorConsts.EXCEPTION_UNKNOWN:
									handler.obtainMessage(SEND_ADD_ROSTER_FALIED, "未知异常").sendToTarget();
									break;
								default:
									break;
								}
							}
						});
					}
				});
			}
		}
		return convertView;
	}

	/**
	 * 好友搜索handler
	 * @author wudl
	 * @date 2014年12月9日
	 * @version V1.0
	 */
	private static class RosterSearchHandler extends Handler {

		private WeakReference<RosterSearchAdapter> refFragemnt;

		RosterSearchHandler(RosterSearchAdapter adapter) {
			refFragemnt = new WeakReference<RosterSearchAdapter>(adapter);
		}

		private RosterSearchAdapter getAdapter() {
			return refFragemnt.get();
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SEND_ADD_ROSTER_SUCCESS:
				ToastUtil.showLong(getAdapter().context, R.string.roster_invite_sended);
				break;
			case SEND_ADD_ROSTER_FALIED:
				ToastUtil.showLong(getAdapter().context, msg.obj.toString());
				break;
			case MSG_SEARCH_SUCCESS:
				getAdapter().notifyDataSetChanged();
				getAdapter().getProgressDialog().dismiss();
				break;
			case MSG_SEARCH_FAILD:
				getAdapter().notifyDataSetChanged();
				getAdapter().getProgressDialog().dismiss();
				ToastUtil.showLong(getAdapter().context, msg.obj.toString());
				break;
			default:
				break;
			}

		}
	}
}
