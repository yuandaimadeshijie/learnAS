package com.yonyou.sns.im.activity;

import java.util.List;

import org.jivesoftware.smack.util.StringUtils;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.adapter.ChatAdapter;
import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.entity.message.YMMessage;
import com.yonyou.sns.im.util.XMPPHelper;
import com.yonyou.sns.im.util.inject.InjectView;

/**
 * 聊天Activity
 *
 * @author litfb
 * @date 2014年10月28日
 * @version 1.0
 */
public class ChatSortActivity extends SimpleTopbarActivity implements TextWatcher {

	/** 数据源*/
	public static final String EXTRA_CHAT_LIST = "EXTRA_CHAT_LIST";
	/** 标题*/
	public static final String EXTRA_CHAT_TITLE = "EXTRA_CHAT_TITLE";
	/** 是否是搜索*/
	public static final String EXTRA_CHAT_IS_SEARCH = "EXTRA_CHAT_IS_SEARCH";
	/** 消息列表 */
	@InjectView(id = R.id.chatsort_msg_list)
	private ListView chatListView;
	/** 搜索*/
	@InjectView(id = R.id.chatsort_search_view)
	private View searchView;
	/** editView*/
	@InjectView(id = R.id.chatsort_search_edit)
	private EditText editView;
	/** editHineView*/
	@InjectView(id = R.id.chatsort_search_hint)
	private TextView editHineView;
	/** edit delete*/
	@InjectView(id = R.id.chatsort_search_delete)
	private View editDelete;
	/** 数据源*/
	private ChatAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chatsort);
		if (isSearch()) {
			initSearchView();
		} else {
			// 隐藏view
			searchView.setVisibility(View.GONE);
		}
		// 初始化对话数据
		setChatWindowAdapter();
	}

	/**
	 * 初始化搜索view
	 */
	private void initSearchView() {
		// 显示view
		searchView.setVisibility(View.VISIBLE);
		// editView添加事件
		editView.addTextChangedListener(this);
		// edit delete
		editDelete.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				editView.setText("");
			}
		});
	}

	/**
	 * 设置聊天的Adapter
	 */
	private void setChatWindowAdapter() {
		adapter = new ChatAdapter(ChatSortActivity.this);
		adapter.setChatList(getCharList());
		chatListView.setAdapter(adapter);
		chatListView.setSelection(adapter.getCount() - 1);
	}

	@SuppressWarnings("unchecked")
	private List<YMMessage> getCharList() {
		return (List<YMMessage>) getIntent().getSerializableExtra(EXTRA_CHAT_LIST);
	}

	@Override
	protected Object getTopbarTitle() {
		return getIntent().getStringExtra(EXTRA_CHAT_TITLE);
	}

	/***
	 * 是否是搜索
	 * @return
	 */
	public boolean isSearch() {
		return getIntent().getBooleanExtra(EXTRA_CHAT_IS_SEARCH, false);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	/**
	 * 更新
	 * @param key
	 */
	private void update(String key) {
		if (StringUtils.isEmpty(key)) {
			adapter.setChatList(getCharList());
		} else {
			adapter.setChatList(YYIMChatManager.getInstance().getMessagesByKey(key));
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (s.length() <= 0) {
			editDelete.setVisibility(View.GONE);
			editHineView.setVisibility(View.VISIBLE);
			// 更新
			update("");
		} else {
			editDelete.setVisibility(View.VISIBLE);
			editHineView.setVisibility(View.GONE);
			String key = editView.getText().toString();
			// 判断是否是中文
			key = XMPPHelper.toLowerCaseNotChinese(key);
			// 更新
			update(key);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {

	}
}
