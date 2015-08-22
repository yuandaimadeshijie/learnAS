package com.yonyou.sns.im.activity.fragment;

import org.jivesoftware.smack.util.StringUtils;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.activity.ChatActivity;
import com.yonyou.sns.im.adapter.RosterSearchAdapter;
import com.yonyou.sns.im.util.XMPPHelper;
import com.yonyou.sns.im.util.inject.InjectView;

/**
 * 联系人搜索
 * 
 * 
 * @author wudl
 * 
 */
public class RosterSearchFragment extends UserFragment implements OnEditorActionListener, TextWatcher {

	public static final String TAG = "RosterSearchFragment";

	/** adapter */
	private RosterSearchAdapter rosterSearchAdapter;

	/** 搜索edit */
	@InjectView(id = R.id.roster_search_edit)
	private EditText searchEdit;
	/** 搜索hint */
	@InjectView(id = R.id.roster_search_hint)
	private View searchHint;
	/** 搜索结果列表 */
	@InjectView(id = R.id.roster_search_result)
	private ListView searchListView;
	/** empty */
	@InjectView(id = R.id.search_empty_view)
	private View emptyView;
	/** edit delete*/
	@InjectView(id = R.id.search_result_edit_delete)
	private View searchDel;
	/** 搜索*/
	@InjectView(id=R.id.roster_search_btn)
	private View searchBtn;

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_rostersearch;
	}

	@Override
	protected void initView(LayoutInflater inflater, View view) {
		// adapter
		rosterSearchAdapter = new RosterSearchAdapter(getFragmentActivity(), isSelect());
		rosterSearchAdapter.setUserSelectListener(getUserSelectListener());
		searchListView.setAdapter(rosterSearchAdapter);
		searchListView.setEmptyView(emptyView);
		searchEdit.setOnEditorActionListener(this);
		searchEdit.addTextChangedListener(this);
		searchDel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				searchEdit.setText("");
			}
		});
		if (!isSelect()) {
			// 添加事件
			searchListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					// 打开聊天界面
					String jid = rosterSearchAdapter.getItem(position).getUserJid();
					if (!StringUtils.isEmpty(jid)) {
						startChatActivity(jid);
					}
				}
			});
		}
		searchBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View paramView) {
				search();
			}
		});
	}
	/**
	 * 打开聊天界面
	 */
	private void startChatActivity(String jid) {
		Intent intent = new Intent(getFragmentActivity(), ChatActivity.class);
		intent.putExtra(ChatActivity.EXTRA_ROOM_JID, jid);
		startActivity(intent);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
			search();
		}
		return true;
	}

	/**
	 * 搜索
	 */
	public void search() {
		// 用户输入的关键字
		String key = searchEdit.getText().toString();
		if (!TextUtils.isEmpty(key)) {
			key = XMPPHelper.toLowerCaseNotChinese(key);
			// 搜索
			rosterSearchAdapter.requery(key);
		}
	}

	@Override
	public void dataChanged() {
		rosterSearchAdapter.notifyDataSetChanged();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (TextUtils.isEmpty(s)) {
			searchHint.setVisibility(View.VISIBLE);
			searchDel.setVisibility(View.GONE);
		} else {
			searchHint.setVisibility(View.GONE);
			searchDel.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {

	}

}