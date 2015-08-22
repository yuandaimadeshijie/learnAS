package com.yonyou.sns.im.adapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.core.YYIMCallBack;
import com.yonyou.sns.im.entity.YMOrgStruct;
import com.yonyou.sns.im.exception.YMErrorConsts;
import com.yonyou.sns.im.smack.IMSmackManager;
import com.yonyou.sns.im.util.bitmap.BitmapCacheManager;
import com.yonyou.sns.im.util.common.ToastUtil;
/**
 * 组织结构适配器，可通过requery()或者requery(key)重新设置数据源
 * @author wudl
 * @date 2014年12月12日
 * @version V1.0
 */
public class OrgStructAdapter extends UserAdapter {

	/** 更新list*/
	private static final int UPDATE_LIST = 0;
	/** none connection**/
	private static final int QUERY_ORG_STRUCT_FAILED=1;
	/** Context */
	private Context context;
	/** LayoutInflater */
	private LayoutInflater inflater;
	/** 组织结构数据 */
	private List<YMOrgStruct> orgStructList = new ArrayList<YMOrgStruct>();
	/** 图片缓存管理器 */
	private BitmapCacheManager avatarBitmapCacheManager;
	/** 上级id */
	private String pid;
	/** 关键字 */
	private String key;
	/** handler*/
	private OrgStructHandler handler = new OrgStructHandler(this);

	public OrgStructAdapter(Context context, String pid) {
		this(context, pid, false);
	}

	public OrgStructAdapter(Context context, String pid, boolean isSelect) {
		super(isSelect);
		this.context = context;
		this.pid = pid;

		this.inflater = LayoutInflater.from(this.context);

		// 这个是URL头像的缓存任务
		avatarBitmapCacheManager = new BitmapCacheManager(this.context, true, BitmapCacheManager.CIRCLE_BITMAP,
				BitmapCacheManager.BITMAP_DPSIZE_40);
		avatarBitmapCacheManager.generateBitmapCacheWork();
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	/**
	 * 根据根节点获取组织
	 */
	public void requery() {
		if (!TextUtils.isEmpty(this.key)) {
			requery(this.key);
			return ;
		}
		IMSmackManager.getInstance().getOrgStruct(this.pid, new YYIMCallBack() {

			@SuppressWarnings("unchecked")
			@Override
			public void onSuccess(Object object) {
				// 更改数据源
				orgStructList = (List<YMOrgStruct>) object;
				// 通知更新界面
				handler.sendEmptyMessage(UPDATE_LIST);
			}

			@Override
			public void onProgress(int progress, String status) {
			}

			@Override
			public void onError(int errno, String errmsg) {
				// 显示错误信息
				switch (errno) {
				case YMErrorConsts.ERROR_AUTHORIZATION:
					handler.obtainMessage(QUERY_ORG_STRUCT_FAILED, "连接已断开").sendToTarget();
					break;
				case YMErrorConsts.EXCEPTION_UNKNOWN:
					handler.obtainMessage(QUERY_ORG_STRUCT_FAILED, "未知异常").sendToTarget();
					break;
				default:
					break;
				}
				
			}
		});
	}
	
	/**
	 * 设置数据源
	 * @param orgStructList
	 */
	public void setOrgStructList(List<YMOrgStruct> orgStructList) {
		this.orgStructList = orgStructList;
	}
	/**
	 * 通过key查询组织
	 * @param key
	 */
	public void requery(String key) {
		this.key = key;
		if (TextUtils.isEmpty(key)) {
			requery();
			return ;
		}
		orgStructList = IMSmackManager.getInstance().queryStructByKey(key);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return orgStructList.size();
	}

	@Override
	public YMOrgStruct getItem(int position) {
		return orgStructList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	static class ViewHolder {

		ImageView iconView;
		TextView nameText;
		ImageView arrowView;
		CheckBox checkBox;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final YMOrgStruct orgStruct = orgStructList.get(position);

		ViewHolder viewHolder = null;
		if (convertView == null) {
			// 创建新的holder
			viewHolder = new ViewHolder();
			// inflate view
			convertView = inflater.inflate(R.layout.org_struct_list_item, parent, false);
			// init holder
			viewHolder.iconView = (ImageView) convertView.findViewById(R.id.struct_item_icon);
			viewHolder.nameText = (TextView) convertView.findViewById(R.id.struct_item_text);
			viewHolder.arrowView = (ImageView) convertView.findViewById(R.id.struct_item_arrow);
			viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.struct_item_checkbox);
			// set tag
			convertView.setTag(R.string.HOLDER_TAG_VIEW_HOLDER, viewHolder);
		} else {
			// get from tag
			viewHolder = (ViewHolder) convertView.getTag(R.string.HOLDER_TAG_VIEW_HOLDER);
		}

		// 是否是用户
		boolean isUser = Boolean.valueOf(orgStruct.getIsUser());
		// 是否是叶子节点
		boolean isLeaf = Boolean.valueOf(orgStruct.getIsLeaf());
		if (isUser) {
			// TODO
			avatarBitmapCacheManager.loadFormCache("", viewHolder.iconView);
		} else {
			viewHolder.iconView.setImageResource(R.drawable.org_struct_icon);
		}
		// 名字
		viewHolder.nameText.setText(orgStruct.getName());

		if (isSelect()) {
			if (isUser) {
				if (isAlreadyExistsMembers(orgStruct.getJid())) {
					viewHolder.checkBox.setEnabled(false);
					viewHolder.checkBox.setChecked(true);
				} else {
					viewHolder.checkBox.setChecked(isUserSelected(orgStruct.getJid()));
					viewHolder.checkBox.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							boolean isChecked = ((CheckBox) v).isChecked();
							// 设置选中状态
							selectChange(orgStruct, isChecked);
						}
					});
				}
				viewHolder.arrowView.setVisibility(View.GONE);
				viewHolder.checkBox.setVisibility(View.VISIBLE);
			} else if (isLeaf) {
				viewHolder.arrowView.setVisibility(View.GONE);
				viewHolder.checkBox.setVisibility(View.GONE);
			} else {
				viewHolder.arrowView.setVisibility(View.VISIBLE);
				viewHolder.checkBox.setVisibility(View.GONE);
			}
		} else {
			viewHolder.checkBox.setVisibility(View.GONE);
			if (isLeaf) {
				viewHolder.arrowView.setVisibility(View.GONE);
			} else {
				viewHolder.arrowView.setVisibility(View.VISIBLE);
			}
		}

		return convertView;
	}

	/**
	 * handler 处理界面更新使用
	 * @author wudl
	 * @date 2014年12月8日
	 * @version V1.0
	 */
	private static class OrgStructHandler extends Handler {

		WeakReference<OrgStructAdapter> conReference;

		public OrgStructHandler(OrgStructAdapter adapter) {
			conReference = new WeakReference<OrgStructAdapter>(adapter);
		}

		public OrgStructAdapter getAdapter() {
			return conReference.get();
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case UPDATE_LIST:
				getAdapter().notifyDataSetChanged();
				break;
			case QUERY_ORG_STRUCT_FAILED:
				ToastUtil.showShort(getAdapter().context, msg.obj.toString());
				break;
			default:
				break;
			}
		}
	}

}
