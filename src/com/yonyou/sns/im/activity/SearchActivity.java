package com.yonyou.sns.im.activity;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.util.StringUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.adapter.SearchResultAdapter;
import com.yonyou.sns.im.entity.SearchValue;
import com.yonyou.sns.im.smack.IMSmackManager;
import com.yonyou.sns.im.ui.component.topbar.SearchEditMiddleFunc;
import com.yonyou.sns.im.ui.component.topbar.SearchTopBtnFunc;
import com.yonyou.sns.im.util.XMPPHelper;
import com.yonyou.sns.im.util.common.ToastUtil;
import com.yonyou.sns.im.util.inject.InjectView;

/**
 * 前台搜索
 * @author wudl
 * 
 */
public class SearchActivity extends SimpleTopbarActivity implements OnClickListener, OnEditorActionListener {

	/** 搜索按钮*/
	private View mSearchButton;
	/** 搜索edit*/
	private EditText mSearchEdit;
	/** 搜索结果列表*/
	@InjectView(id = R.id.search_result)
	private ListView mSearchListView;
	/** 搜索结果列表数据源*/
	private List<SearchValue> mSearchResultList = new ArrayList<SearchValue>();

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_search);
		// 初始化搜索结果列表
		initSearchResult();
	}

	@Override
	protected void onResume() {
		mSearchEdit = (EditText) findViewById(R.id.search_result_edit);
		mSearchEdit.setOnEditorActionListener(this);
		mSearchButton = findViewById(R.id.search_topbar_btn);
		mSearchButton.setOnClickListener(this);
		super.onResume();
	}

	/**
	 * 初始化搜索结果列表
	 */
	private void initSearchResult() {
		// 列表空页面
		View emptyView = findViewById(R.id.search_empty_view);
		// 设置空页面
		mSearchListView.setEmptyView(emptyView);
		// 搜索结果适配器
		SearchResultAdapter adapter = new SearchResultAdapter(SearchActivity.this);
		// 添加适配器
		mSearchListView.setAdapter(adapter);
		// 添加事件
		mSearchListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// 打开聊天界面
				String jid = mSearchResultList.get(position).getUserJid();
				if (!StringUtils.isEmpty(jid)) {
					openChatPage(jid);
				}
			}
		});
	}

	/**
	 * 打开聊天界面
	 */
	private void openChatPage(String jid) {
		Intent intent = new Intent(SearchActivity.this, ChatActivity.class);
		intent.putExtra(ChatActivity.EXTRA_ROOM_JID, jid);
		startActivity(intent);
	}

	/**
	 * 搜索
	 */
	private void search() {
		// 用户输入的关键字
		String key = mSearchEdit.getText().toString();
		if (key.length() > 0) {
			// 先清除一次数据源
			key = XMPPHelper.toLowerCaseNotChinese(key);
			// 搜索
			mSearchResultList = IMSmackManager.getInstance().searchForWord(key);
			// 通知界面数据更新
			SearchResultAdapter adapter = (SearchResultAdapter) mSearchListView.getAdapter();
			adapter.updateUI(mSearchResultList, key);
		} else {
			ToastUtil.showShort(SearchActivity.this, getResources().getString(R.string.search_content_empty));
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.search_topbar_btn: {
			search();
			break;
		}
		default:
			break;
		}
	}

	protected Class<?> getTopbarMiddleFunc() {
		return SearchEditMiddleFunc.class;
	}

	protected Class<?>[] getTopbarRightFuncArray() {
		Class<?>[] btn = { SearchTopBtnFunc.class };
		return btn;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
			search();
		}
		return true;
	}
}
