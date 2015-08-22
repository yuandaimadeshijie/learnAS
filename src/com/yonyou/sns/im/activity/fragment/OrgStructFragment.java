package com.yonyou.sns.im.activity.fragment;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.ChatActivity;
import com.yonyou.sns.im.activity.OrgStructActivity;
import com.yonyou.sns.im.adapter.OrgStructAdapter;
import com.yonyou.sns.im.entity.YMOrgStruct;
import com.yonyou.sns.im.smack.IMSmackManager;
import com.yonyou.sns.im.util.XMPPHelper;
import com.yonyou.sns.im.util.inject.InjectView;
/**
 * 组织结构
 * @author wudl
 * @date 2014年12月15日
 * @version V1.0
 */
public class OrgStructFragment extends UserFragment implements TextWatcher {

	public static final String TAG = "OrgStructFragment";

	/** 上级id */
	private String pid;
	/** adapter */
	private OrgStructAdapter orgStructAdapter;

	/** search view */
	@InjectView(id = R.id.org_struct_search_view)
	private View searchView;
	/** 搜索editView */
	@InjectView(id = R.id.org_struct_search_edit)
	private EditText searchEdit;
	/** 搜索hine */
	@InjectView(id = R.id.org_struct_search_hint)
	private View searchHint;
	/** 搜索删除*/
	@InjectView(id = R.id.org_struct_search_delete)
	private View searchDel;
	/** 组织结构列表 */
	@InjectView(id = R.id.org_struct_listview)
	private ListView orgStructList;
	/** 组织结构列表 */
	@InjectView(id = R.id.search_empty_view)
	private View emptyView;

	/**
	 * 设置上级id
	 * 
	 * @param pid
	 */
	public void setPid(String pid) {
		this.pid = pid;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_orgstruct;
	}

	@Override
	protected void initView(LayoutInflater inflater, View view) {
		// 适配器
		orgStructAdapter = new OrgStructAdapter(getFragmentActivity(), pid, isSelect());
		orgStructAdapter.setCustomClick(isCustomClick());
		orgStructAdapter.setUserSelectListener(getUserSelectListener());
		// 查询一次数据库
		orgStructAdapter.setOrgStructList(IMSmackManager.getInstance().queryStruct(pid));
		// 添加适配器
		orgStructList.setAdapter(orgStructAdapter);
		orgStructAdapter.notifyDataSetChanged();
		// 设置空页面
		orgStructList.setEmptyView(emptyView);

		orgStructList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				YMOrgStruct orgStruct = orgStructAdapter.getItem(position);
				// 是否是叶子节点
				boolean isLeaf = Boolean.valueOf(orgStruct.getIsLeaf());
				// 是否user
				boolean isUser = Boolean.valueOf(orgStruct.getIsUser());

				if (isSelect()) {
					if (isUser || isLeaf) {
						return;
					}
					String ftag = TAG + "_" + orgStruct.getJid();

					OrgStructFragment orgStructFragment = (OrgStructFragment) getFragmentActivity()
							.getSupportFragmentManager().findFragmentByTag(ftag);
					if (orgStructFragment == null) {
						orgStructFragment = new OrgStructFragment();
						orgStructFragment.setPid(orgStruct.getJid());
					}
					getUserSelectListener().changeFragment(orgStructFragment,ftag);
				} else if (isCustomClick()) {
					getUserSelectListener().onUserclick(orgStruct);
				} else {
					if (isUser) {
						// chat
						Intent intent = new Intent(getFragmentActivity(), ChatActivity.class);
						// jid
						intent.putExtra(ChatActivity.EXTRA_ROOM_JID, orgStruct.getJid());
						startActivity(intent);
						getFragmentActivity().finish();
					} else if (!isLeaf) {
						String ftag = TAG + "_" + orgStruct.getJid();

						OrgStructFragment orgStructFragment = (OrgStructFragment) getFragmentActivity()
								.getSupportFragmentManager().findFragmentByTag(ftag);
						if (orgStructFragment == null) {
							orgStructFragment = new OrgStructFragment();
							// 设置根节点
							orgStructFragment.setPid(orgStruct.getJid());
						}
						((OrgStructActivity)getFragmentActivity()).changeFragment(orgStructFragment,ftag);
						// 重设title
						((OrgStructActivity)getFragmentActivity()).resetTopbarTitle(orgStruct.getName());
					}
				}
			}
		});
		// 搜索监听
		searchEdit.addTextChangedListener(this);
		// 删除监听
		searchDel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				searchEdit.setText("");
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		orgStructAdapter.setPid(this.pid);
		orgStructAdapter.requery();
	}
	
	@Override
	public void dataChanged() {
		orgStructAdapter.notifyDataSetChanged();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (s.length() > 0) {
			String key = searchEdit.getText().toString();
			// 判断是否是中文
			key = XMPPHelper.toLowerCaseNotChinese(key);
			// 更新
			searchHint.setVisibility(View.GONE);
			searchDel.setVisibility(View.VISIBLE);
			// 按关键字查询
			orgStructAdapter.requery(key);
		} else {
			// 更新
			searchHint.setVisibility(View.VISIBLE);
			searchDel.setVisibility(View.GONE);
			// 要是为空则查询原先的root节点数据
			orgStructAdapter.requery("");
		}
	}

	@Override
	public void afterTextChanged(Editable s) {

	}

}
