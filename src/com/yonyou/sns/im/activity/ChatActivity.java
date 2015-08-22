package com.yonyou.sns.im.activity;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.adapter.ChatAdapter;
import com.yonyou.sns.im.core.YYIMCallBack;
import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.core.YYIMDBNotifier;
import com.yonyou.sns.im.entity.FileItem;
import com.yonyou.sns.im.entity.YMFile;
import com.yonyou.sns.im.entity.album.PhotoItem;
import com.yonyou.sns.im.entity.message.YMMessage;
import com.yonyou.sns.im.entity.message.YMMessageContent;
import com.yonyou.sns.im.exception.YMErrorConsts;
import com.yonyou.sns.im.log.YMLogger;
import com.yonyou.sns.im.ui.component.topbar.ChatInfoFunc;
import com.yonyou.sns.im.ui.emoji.view.EmojiKeyboard;
import com.yonyou.sns.im.ui.emoji.view.EmojiKeyboard.EventListener;
import com.yonyou.sns.im.ui.widget.recoredview.WaveRecoredFrame;
import com.yonyou.sns.im.ui.widget.recoredview.WaveRecoredFrame.RecoredListener;
import com.yonyou.sns.im.ui.widget.xlistview.MsgListView;
import com.yonyou.sns.im.ui.widget.xlistview.MsgListView.IXListViewListener;
import com.yonyou.sns.im.util.RecordManager;
import com.yonyou.sns.im.util.XMPPHelper;
import com.yonyou.sns.im.util.common.FileUtils;
import com.yonyou.sns.im.util.common.LocalBigImageUtil;
import com.yonyou.sns.im.util.common.ToastUtil;
import com.yonyou.sns.im.util.common.YMStorageUtil;

/**
 * 聊天Activity
 * 
 * @author litfb
 * @date 2014年10月28日
 * @version 1.0
 */
public class ChatActivity extends SimpleTopbarActivity implements OnTouchListener, OnClickListener, IXListViewListener,
		ChatAdapter.ChatAdapterOpertionListener {

	/** 房间jid mark*/
	public static final String EXTRA_ROOM_JID = "EXTRA_ROOM_JID";

	/** Topbar功能列表 */
	private static Class<?> TopBarRightFuncArray[] = { ChatInfoFunc.class };
	/** 录音时间太短mark */
	public static final int RECORED_FAILED = 0;
	/** 数据新增mark */
	private static final int MSGWHAT_MESSAGE_ADD = 1;
	/** 数据更新mark*/
	private static final int MSGWHAT_MESSAGE_UPDATE = 2;
	/** 关闭sort panel */
	private static final int MSGWHAT_CLOSESORT = 3;

	/** 图片功能 */
	public static final int REQUEST_IMAGE = 0;
	/** 文档功能 */
	public static final int REQUEST_DOCUMENT = 1;
	/** 附件功能 */
	public static final int REQUEST_LOCAL_FILE = 2;
	/** 位置功能 */
	public static final int REQUEST_LOCATION = 3;

	private InputMethodManager inputMethodManager;
	private WindowManager.LayoutParams windowParams;
	/** receiver*/
	private ChatReceiver receiver = new ChatReceiver();
	/** 聊天jid */
	private String roomJid = null;

	/** 对话ListView */
	private MsgListView messageListView;
	/** Adapter */
	private ChatAdapter adapter;

	private ChatHandler handler;

	/** 发送button */
	private ImageButton mSendBtn;
	/** 表情button */
	private ImageButton mEmojiBtn;
	/** 语音按键和文字按键切换的button */
	private ImageButton mSwitchBtn;
	/** 扩展按钮 */
	private ImageButton mExtendBtn;
	/** 文字和表情输入的区域 */
	private RelativeLayout mInputView;
	/** 消息输入框 */
	private EditText mChatEditText;
	/** 表情面板 */
	private EmojiKeyboard mEmojiView;
	/** 扩展面板 */
	private GridView mExtendView;

	/** 录音的区域 */
	private FrameLayout mRecordView;
	/** 录音button */
	private Button mRecordBtn;
	/** 录音pop */
	private RelativeLayout mRecordProcess;
	/** 录音动态效果 */
	private ImageView mRecordWave;

	/** 分类/过滤/搜索 */
	private View sortView;
	/** 图片过滤查看 */
	private ImageView sortImageView;
	/** 文件过滤查看 */
	private ImageView sortFileView;
	/** 搜索过滤查看 */
	private ImageView sortSearchView;
	/** 滑动时碰触的位置*/
	private PointF scrollMoveTouch;
	/** 滑动时down的位置*/
	private PointF scrollDownTouch;
	/** 过滤面板显示控制线程 */
	private SortCtrlThread sortCtrlThread;
	/** album进入动画 */
	private TranslateAnimation enterAnimation;
	/** album退出动画 */
	private TranslateAnimation exitAnimation;

	/** 用于完成录音 */
	private MediaRecorder mRecorder;
	/** 录音文件路径 */
	private String audioFilePath;
	/** wave recored frame*/
	private WaveRecoredFrame recoredFrame;
	/** 第一次点击的时间 */
	private long firClick = 0;
	/** 第二次点击时间 */
	private long secClick = 0;
	/** 第一次触碰位置 */
	private PointF firstTouch;
	/** 第二次触碰位置 */
	private PointF secondTouch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 获取聊天对象的id
		roomJid = getIntent().getStringExtra(EXTRA_ROOM_JID);
		setContentView(R.layout.activity_chat);
		this.handler = new ChatHandler(this);
		initView();// 初始化view
		setChatWindowAdapter();// 初始化对话数据
	}

	private void initView() {
		// 获取软件键盘对象
		inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		// window layout params
		windowParams = getWindow().getAttributes();

		// 消息
		messageListView = (MsgListView) findViewById(R.id.chat_message_list);
		messageListView.setPullRefreshEnable(false);
		// 触摸ListView隐藏表情和输入法
		messageListView.setOnTouchListener(this);
		messageListView.setPullLoadEnable(false);
		messageListView.setXListViewListener(this);

		// 录音/输入切换按钮
		mSwitchBtn = (ImageButton) findViewById(R.id.chat_switch_btn);
		mSwitchBtn.setOnClickListener(this);
		// 输入
		initInputView();
		// 表情
		initEmojiView();
		// 录音
		initRecordView();
		// 发送
		mSendBtn = (ImageButton) findViewById(R.id.chat_send_btn);
		mSendBtn.setOnClickListener(this);
		// 扩展
		initExtendView();
		// 录音frame
		initWaveRecoredFrame();
		// 过滤
		initSortView();
	}

	/**
	 * 初始化波浪线录音层
	 */
	private void initWaveRecoredFrame() {
		recoredFrame = (WaveRecoredFrame) findViewById(R.id.wave_recored_frame);
		// 设置录音监听
		recoredFrame.setListener(new RecoredListener() {

			@Override
			public void recoredSuccess() {
				// 获取存储路径
				audioFilePath=recoredFrame.getCurrentAudioPath();
				sendAudioMessage();
			}

			@Override
			public void recoredCancel() {
			}
		});
	}

	private void initInputView() {
		// 输入框和表情按钮面板
		mInputView = (RelativeLayout) findViewById(R.id.chat_input_view);

		// 消息输入
		mChatEditText = (EditText) findViewById(R.id.chat_edit_text);
		mChatEditText.setOnTouchListener(this);

		mChatEditText.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					if (windowParams.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
							|| mIsFaceShow) {
						mEmojiView.setVisibility(View.GONE);
						mIsFaceShow = false;
						mEmojiBtn.setImageResource(R.drawable.icon_emoji);
						return true;
					}
				}
				return false;
			}
		});

		mChatEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			// 如果有内容显示发送，如果没有内容显示添加辅助功能的按钮
			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() > 0) {
					mSendBtn.setVisibility(View.VISIBLE);
					mExtendBtn.setVisibility(View.GONE);
				} else {
					mSendBtn.setVisibility(View.GONE);
					mExtendBtn.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	private void initEmojiView() {
		// 表情
		mEmojiBtn = (ImageButton) findViewById(R.id.chat_emoji_btn);
		mEmojiBtn.setOnClickListener(this);
		// 表情面板
		mEmojiView = (EmojiKeyboard) findViewById(R.id.chat_emoji_view);
		mEmojiView.setVisibility(View.GONE);
		mEmojiView.setEventListener(new EventListener() {

			@Override
			public void onEmojiSelected(String res) {
				EmojiKeyboard.input(mChatEditText, res);
			}

			@Override
			public void onBackspace() {
				EmojiKeyboard.backspace(mChatEditText);
			}
		});
	}

	private void initRecordView() {
		mRecordProcess = (RelativeLayout) findViewById(R.id.chat_record_pop);
		mRecordWave = (ImageView) mRecordProcess.findViewById(R.id.chat_record_pop_wave);

		mRecordView = (FrameLayout) findViewById(R.id.chat_record_view);
		mRecordBtn = (Button) findViewById(R.id.chat_record_btn);
		mRecordBtn.setOnTouchListener(this);
	}

	private void initExtendView() {
		// 扩展面板按钮
		mExtendBtn = (ImageButton) findViewById(R.id.chat_extend_btn);
		mExtendBtn.setOnClickListener(this);
		// 辅助应用的网格布局面板
		mExtendView = (GridView) findViewById(R.id.chat_extend_view);
		mExtendView.setVisibility(View.GONE);

		ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
		// 图片
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("chat_extend_icon", R.drawable.chat_application_album);
		map.put("chat_extend_name", getResources().getString(R.string.chat_extend_image));
		lstImageItem.add(map);
		// 文档
		map = new HashMap<String, Object>();
		map.put("chat_extend_icon", R.drawable.chat_application_document);
		map.put("chat_extend_name", getResources().getString(R.string.chat_extend_document));
		lstImageItem.add(map);
		// 附件
		map = new HashMap<String, Object>();
		map.put("chat_extend_icon", R.drawable.chat_application_file);
		map.put("chat_extend_name", getResources().getString(R.string.chat_extend_appendix));
		lstImageItem.add(map);
		// 位置
		map = new HashMap<String, Object>();
		map.put("chat_extend_icon", R.drawable.chat_application_location);
		map.put("chat_extend_name", getResources().getString(R.string.chat_extend_location));
		lstImageItem.add(map);

		SimpleAdapter saImageItems = new SimpleAdapter(ChatActivity.this, lstImageItem, R.layout.view_chat_extend_item,
				new String[] { "chat_extend_icon", "chat_extend_name" }, new int[] { R.id.chat_extend_icon,
						R.id.chat_extend_name });
		mExtendView.setAdapter(saImageItems);
		mExtendView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
				case 0:
					Intent photoIntent = new Intent(ChatActivity.this, AlbumPhotoActivity.class);
					startActivityForResult(photoIntent, REQUEST_IMAGE);
					break;
				case 1:
					// 工作文档
					Intent workDocIntent = new Intent(ChatActivity.this, WorkDocActivity.class);
					startActivityForResult(workDocIntent, REQUEST_DOCUMENT);
					break;
				case 2:
					// 本地文件
					Intent localFileIntent = new Intent(ChatActivity.this, LocalFileActivity.class);
					startActivityForResult(localFileIntent, REQUEST_LOCAL_FILE);
					break;
				case 3:
					// 混合定位
					Intent multyIntent = new Intent(ChatActivity.this, MultyLocationActivity.class);
					startActivityForResult(multyIntent, REQUEST_LOCATION);
					break;
				default:
					break;
				}
			}
		});
	}

	private void initSortView() {
		sortView = findViewById(R.id.chat_sort_view);
		sortImageView = (ImageView) findViewById(R.id.chat_sort_image);
		sortImageView.setOnClickListener(this);
		sortFileView = (ImageView) findViewById(R.id.chat_sort_file);
		sortFileView.setOnClickListener(this);
		sortSearchView = (ImageView) findViewById(R.id.chat_sort_search);
		sortSearchView.setOnClickListener(this);
	}

	private boolean mIsFaceShow = false;// 是否显示表情
	private boolean mIsAppShow = false;
	private boolean mIsRecordAudioShow = false;

	@Override
	protected void onResume() {
		super.onResume();
		updateChat();
		// 注册receiver
		registerReceiver(receiver, new IntentFilter(YYIMDBNotifier.MESSAGE_UPDATE));
		registerReceiver(receiver, new IntentFilter(YYIMDBNotifier.MESSAGE_ADD));
		// 重置title
		updateTitle(roomJid);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 注销receiver
		unregisterReceiver(receiver);
		// 关闭正在播放的录音
		RecordManager.getInstance().stop();
		// 录音层资源回收
		recoredFrame.release();
	}

	/**
	 * 设置聊天的Adapter
	 */
	private void setChatWindowAdapter() {
		adapter = new ChatAdapter(this);
		messageListView.setAdapter(adapter);
		adapter.setChatAdapterOpertionListener(this);
	}

	@Override
	public void onRefresh() {
		//
	}

	@Override
	public void onLoadMore() {
		//
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (v.getId() == R.id.chat_emoji_btn) {
			if (!mIsFaceShow) {
				inputMethodManager.hideSoftInputFromWindow(mChatEditText.getWindowToken(), 0);
				try {
					Thread.sleep(80);// 解决此时会黑一下屏幕的问题
				} catch (InterruptedException e) {
					YMLogger.d(e);
				}
				if (mIsAppShow) {
					mExtendView.setVisibility(View.GONE);
					mIsAppShow = false;
				}
				mEmojiView.setVisibility(View.VISIBLE);
				mIsFaceShow = true;
				mEmojiBtn.setImageResource(R.drawable.im_blue_face_btn);
			} else {
				mEmojiView.setVisibility(View.GONE);
				inputMethodManager.showSoftInput(mChatEditText, 0);
				mIsFaceShow = false;
				mEmojiBtn.setImageResource(R.drawable.icon_emoji);
			}
		} else if (v.getId() == R.id.chat_send_btn) {
			// 发送消息
			sendTextMessage();
		} else if (v.getId() == R.id.chat_switch_btn) {
			if (!mIsRecordAudioShow) {
				inputMethodManager.hideSoftInputFromWindow(mChatEditText.getWindowToken(), 0);

				mRecordView.setVisibility(View.VISIBLE);
				mInputView.setVisibility(View.GONE);
				mIsRecordAudioShow = true;

				mSwitchBtn.setImageResource(R.drawable.im_keyboard_btn);
			} else {
				inputMethodManager.showSoftInput(mChatEditText, 0);

				mRecordView.setVisibility(View.GONE);
				mInputView.setVisibility(View.VISIBLE);
				mIsRecordAudioShow = false;

				mSwitchBtn.setImageResource(R.drawable.icon_record);
			}
		} else if (v.getId() == R.id.chat_extend_btn) {
			if (!mIsAppShow) {
				if (mIsFaceShow) {
					mEmojiView.setVisibility(View.GONE);
					mIsFaceShow = false;
					mEmojiBtn.setImageResource(R.drawable.icon_emoji);
				}
				mExtendView.setVisibility(View.VISIBLE);
				inputMethodManager.hideSoftInputFromWindow(mChatEditText.getWindowToken(), 0);
				mIsAppShow = true;
			} else {
				mExtendView.setVisibility(View.GONE);
				mIsAppShow = false;
			}
		} else if (v.getId() == R.id.chat_sort_image) {
			hideSortView();
			Intent intent = new Intent(this, ChatSortActivity.class);
			intent.putExtra(ChatSortActivity.EXTRA_CHAT_LIST, (Serializable) adapter.getImageChats());
			intent.putExtra(ChatSortActivity.EXTRA_CHAT_TITLE, getResources().getString(R.string.chat_img_title));
			intent.putExtra(ChatSortActivity.EXTRA_CHAT_IS_SEARCH, false);
			startActivity(intent);
		} else if (v.getId() == R.id.chat_sort_file) {
			hideSortView();
			Intent intent = new Intent(this, ChatSortActivity.class);
			intent.putExtra(ChatSortActivity.EXTRA_CHAT_LIST, (Serializable) adapter.getFileChats());
			intent.putExtra(ChatSortActivity.EXTRA_CHAT_TITLE, getResources().getString(R.string.chat_file_tile));
			intent.putExtra(ChatSortActivity.EXTRA_CHAT_IS_SEARCH, false);
			startActivity(intent);
		} else if (v.getId() == R.id.chat_sort_search) {
			hideSortView();
			Intent intent = new Intent(this, ChatSortActivity.class);
			intent.putExtra(ChatSortActivity.EXTRA_CHAT_LIST, (Serializable) adapter.getChatList());
			intent.putExtra(ChatSortActivity.EXTRA_CHAT_TITLE, getResources().getString(R.string.chat_search_title));
			intent.putExtra(ChatSortActivity.EXTRA_CHAT_IS_SEARCH, true);
			startActivity(intent);
		}

	}

	/**
	 * 发送文本消息
	 */
	private void sendTextMessage() {
		// message
		String message = mChatEditText.getText().toString();
		// 空文本判断
		if (TextUtils.isEmpty(message)) {
			return;
		}
		// 发消息
		YYIMChatManager.getInstance().sendTextMessage(roomJid, message, null);
		// 清空输入框
		mChatEditText.setText("");
		// button状态处理
		mSendBtn.setVisibility(View.GONE);
		mExtendBtn.setVisibility(View.VISIBLE);
	}

	/**
	 * 判断是否是群聊
	 * 
	 * @return
	 */
	public boolean isMultiChat() {
		if (XMPPHelper.isGroupChat(roomJid)) {
			return true;
		} else {
			return false;
		}
	}

	public String getRoomJid() {
		return roomJid;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v.getId() == R.id.chat_message_list) {
			if (mIsFaceShow) {
				mEmojiView.setVisibility(View.GONE);
				mIsFaceShow = false;
				mEmojiBtn.setImageResource(R.drawable.icon_emoji);
			}
			if (mIsAppShow) {
				mExtendView.setVisibility(View.GONE);
				mIsAppShow = false;
			}
			inputMethodManager.hideSoftInputFromWindow(mChatEditText.getWindowToken(), 0);
			mEmojiView.setVisibility(View.GONE);
			mIsFaceShow = false;
			mEmojiBtn.setImageResource(R.drawable.icon_emoji);
			// 时间间隔
			float distance = 600;
			// 俩点间距离
			long duration = 300;
			// 录音弹出层
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// 双击事件
				if (firClick == 0) {
					firstTouch = new PointF(event.getX(), event.getY());
					firClick = System.currentTimeMillis();
				} else {
					secondTouch = new PointF(event.getX(), event.getY());
					secClick = System.currentTimeMillis();
					duration = secClick - firClick;
					distance = (float) Math.sqrt((secondTouch.x - firstTouch.x) * (secondTouch.x - firstTouch.x)
							+ (secondTouch.y - firstTouch.y) * (secondTouch.y - firstTouch.y));
					firClick = secClick;
					if (duration < 200 && distance <= 600) {
						// 成功就初始化时间
						firClick = 0;
						secClick = 0;
						// 运行录音层
						recoredFrame.run();
					}
				}
				scrollDownTouch = new PointF(event.getX(), event.getY());
				break;
			case MotionEvent.ACTION_MOVE:
				// 移动时触发
				scrollMoveTouch = new PointF(event.getX(), event.getY());
				if (scrollDownTouch == null) {
					break;
				}

				// 上下滑动事件
				if (scrollMoveTouch.y - scrollDownTouch.y >= 100) {
					// 向下滑
					showSortView();
				} else if (scrollDownTouch.y - scrollMoveTouch.y >= 40) {
					// 向上滑
					hideSortView();
				}
				break;
			case MotionEvent.ACTION_UP:
				break;
			default:
				break;
			}
		} else if (v.getId() == R.id.chat_edit_text) {
			if (mIsFaceShow) {
				mEmojiView.setVisibility(View.GONE);
				mIsFaceShow = false;
				mEmojiBtn.setImageResource(R.drawable.icon_emoji);
			}
			if (mIsAppShow) {
				mExtendView.setVisibility(View.GONE);
				mIsAppShow = false;
			}
			inputMethodManager.showSoftInput(mChatEditText, 0);
			mIsFaceShow = false;
			mEmojiBtn.setImageResource(R.drawable.icon_emoji);
		} else if (v.getId() == R.id.chat_record_btn) {
			// 点击录音按钮
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (mRecorder==null) {
					startVoice();
				}
				break;
			case MotionEvent.ACTION_UP:
				if (mRecorder!=null) {
					stopVoice();
				}
				break;
			default:
				break;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_IMAGE:
			if (data == null || "".equals(data)) {
				return;
			}
			if (CameraEditActivity.REQUEST_CAMERA_OPTION == resultCode) {
				// 照相机图片处理
				boolean isOriginal = data.getBooleanExtra(CameraEditActivity.OPTION_RESULT, false);
				String cameraFilePath = data.getStringExtra(AlbumPhotoActivity.CAMERA_FILE_PATH);
				if (isOriginal) {
					File file = new File(cameraFilePath);
					// 获取图片旋转角度
					int degree = LocalBigImageUtil.readPictureDegree(cameraFilePath);
					if (degree > 0) {
						// 获取图片
						Bitmap curImageBitmap = BitmapFactory.decodeFile(cameraFilePath);
						// 将图片旋转
						curImageBitmap = LocalBigImageUtil.rotateBitmap(curImageBitmap, degree);
						// 保存
						FileUtils.compressBmpToFile(curImageBitmap, file);
					}
					YYIMChatManager.getInstance().sendImageMessage(roomJid, new String[] { cameraFilePath }, null);
				} else {
					DisplayMetrics dm = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(dm);
					int mScreenW = dm.widthPixels; // 得到宽度
					int mScreenH = dm.heightPixels; // 得到高度

					Bitmap curImageBitmap = LocalBigImageUtil.getBitmapFromFile(cameraFilePath, mScreenW, mScreenH);
					File file = new File(cameraFilePath);
					// 获取图片旋转角度
					int degree = LocalBigImageUtil.readPictureDegree(cameraFilePath);
					if (degree > 0) {
						// 将图片旋转
						curImageBitmap = LocalBigImageUtil.rotateBitmap(curImageBitmap, degree);
					}
					FileUtils.compressBmpToFile(curImageBitmap, file);
					YYIMChatManager.getInstance().sendImageMessage(roomJid, new String[] { cameraFilePath }, null);
				}
			} else {
				// 图片处理
				ArrayList<PhotoItem> photoList = (ArrayList<PhotoItem>) data
						.getSerializableExtra(AlbumPhotoActivity.BUNDLE_RETURN_PHOTOS);
				List<String> pathList = new ArrayList<>();
				for (PhotoItem photo : photoList) {
					// 存储图片到图片目录
					int index = photo.getPhotoPath().lastIndexOf(".") + 1;
					String suffix = photo.getPhotoPath().substring(index);

					File file = new File(YMStorageUtil.getImagePath(this), UUID.randomUUID().toString() + "." + suffix);

					FileUtils.copyFile(photo.getPhotoPath(), file.getPath());
					pathList.add(file.getPath());
				}
				YYIMChatManager.getInstance().sendImageMessage(roomJid, pathList.toArray(new String[pathList.size()]),
						null);
			}
			break;
		case REQUEST_DOCUMENT:
			if (data != null) {
				// 只发送文件message不上传
				YMFile fileItem = (YMFile) data.getSerializableExtra(WorkDocActivity.LOCAL_WORK_DOC);
				YMMessageContent content = new YMMessageContent(YMMessage.CONTENT_FILE);
				content.setFileExtension(fileItem.getFileName().substring(fileItem.getFileName().lastIndexOf(".") + 1,
						fileItem.getFileName().length()));
				content.setFileName(fileItem.getFileName());
				content.setFileSize(fileItem.getFileSize());
				content.setFileUrl(fileItem.getFileUrl());
				content.setLocalFilePath(fileItem.getFilePath());
				YYIMChatManager.getInstance().sendMessage(roomJid, content);
			}
			break;
		case REQUEST_LOCAL_FILE:
			if (data != null) {
				// 发送message并上传
				List<FileItem> list = (List<FileItem>) data.getSerializableExtra(LocalFileActivity.LOCAL_FILE_LIST);
				for (FileItem fileItem : list) {
					YYIMChatManager.getInstance().sendFileMessage(roomJid, fileItem.getFilePath());
				}
			}
			break;
		case REQUEST_LOCATION:
			if (data != null) {
				// 图片路径
				String path = data.getStringExtra(MultyLocationActivity.RESULT_FROM_LOCATION_PATH);
				// 地址
				String address = data.getStringExtra(MultyLocationActivity.RESULT_FROM_LOCATION_ADRESS);
				// 经度
				double latitude = data.getDoubleExtra(MultyLocationActivity.RESULT_FROM_LOCATION_LATITUDE, -1.);
				// 纬度
				double longitude = data.getDoubleExtra(MultyLocationActivity.RESULT_FROM_LOCATION_LONGITUDE, -1.);
				YYIMChatManager.getInstance().sendLocationMessage(roomJid, path, address, latitude, longitude);
			}
			break;
		default:
			break;
		}
	}

	/** 开始录音 */
	private void startVoice() {
		// 设置录音保存路径
		audioFilePath = YMStorageUtil.getAudioPath(this) + File.separator + UUID.randomUUID().toString() + ".amr";
		try {
			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
			mRecorder.setOutputFile(audioFilePath);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			mRecorder.setAudioEncodingBitRate(16);
			mRecorder.setAudioSamplingRate(4000);
			mRecorder.prepare();
		} catch (Exception e) {
			YMLogger.e("MediaRecorder run failed!");
		}
		mRecordProcess.setVisibility(View.VISIBLE);
		AnimationDrawable animationDrawable = (AnimationDrawable) mRecordWave.getDrawable();
		animationDrawable.start();
		mRecorder.start();
	}

	/** 停止录音 */
	private void stopVoice() {
		if (mRecorder == null) {
			return;
		}
		// 释放资源
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
		// 停止动画
		mRecordProcess.setVisibility(View.GONE);
		AnimationDrawable animationDrawable = (AnimationDrawable) mRecordWave.getDrawable();
		animationDrawable.stop();
		// 发送录音
		sendAudioMessage();
	}

	/**
	 * 发送录音消息
	 */
	private void sendAudioMessage() {
		YYIMChatManager.getInstance().sendAudioMessage(roomJid, audioFilePath, new YYIMCallBack() {

			@Override
			public void onSuccess(Object object) {
			}

			@Override
			public void onProgress(int progress, String status) {
			}

			@Override
			public void onError(int errno, String errmsg) {
				if (errno == YMErrorConsts.ERROR_AUDIO_TO_SHORT) {
					handler.obtainMessage(RECORED_FAILED, getResources().getString(R.string.chat_audio_too_short))
							.sendToTarget();
				}
			}
		});
	}

	@Override
	public void resendMessage(int id) {
		YYIMChatManager.getInstance().resendMessage(id);
	}

	@Override
	protected Object getTopbarTitle() {
		return 0;
	}

	@Override
	protected Class<?>[] getTopbarRightFuncArray() {
		if (XMPPHelper.isOwnOtherClient(roomJid)) {
			return null;
		} else {
			return TopBarRightFuncArray;
		}
	}

	/**
	 * 展示相册
	 */
	private void showSortView() {
		if (!sortView.isShown()) {
			sortCtrlThread = new SortCtrlThread();
			sortCtrlThread.start();
			sortView.startAnimation(getEnterAnimation());
			sortView.setVisibility(View.VISIBLE);
		} else if (sortCtrlThread != null && !sortCtrlThread.isDone()) {
			sortCtrlThread.delay();
		}
	}

	/**
	 * 隐藏相册
	 */
	private void hideSortView() {
		if (sortCtrlThread != null && !sortCtrlThread.isDone()) {
			sortCtrlThread.setDone(true);
		} else if (sortView.isShown()) {
			sortView.startAnimation(getExitAnimation());
			sortView.setVisibility(View.GONE);
		}
	}

	/**
	 * 进入动画
	 * 
	 * @return
	 */
	private AnimationSet getEnterAnimation() {
		AnimationSet animationSet = new AnimationSet(true);
		if (enterAnimation == null) {
			enterAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
					Animation.RELATIVE_TO_SELF, -1f, Animation.RELATIVE_TO_SELF, 0f);
			enterAnimation.setDuration(400);
		}
		animationSet.addAnimation(enterAnimation);
		return animationSet;
	}

	/**
	 * 退出动画
	 * 
	 * @return
	 */
	private AnimationSet getExitAnimation() {
		AnimationSet animationSet = new AnimationSet(true);
		if (exitAnimation == null) {
			exitAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
					Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -1f);
			exitAnimation.setDuration(400);
		}
		animationSet.addAnimation(exitAnimation);
		return animationSet;
	}

	class SortCtrlThread extends Thread {

		private long timeInMillis = 0;

		private boolean done;

		public SortCtrlThread() {
			timeInMillis = Calendar.getInstance().getTimeInMillis() + 3000;
		}

		@Override
		public void run() {
			while (!done && Calendar.getInstance().getTimeInMillis() < timeInMillis) {
				try {
					synchronized (this) {
						wait(100);
					}
				} catch (InterruptedException e) {
				}
			}
			if (!done) {
				done = true;
				handler.sendEmptyMessage(MSGWHAT_CLOSESORT);
			}
		}

		public void delay() {
			timeInMillis = Calendar.getInstance().getTimeInMillis() + 3000;
		}

		public boolean isDone() {
			return done;
		}

		public void setDone(boolean done) {
			this.done = done;
		}
	}

	private void updateChat() {
		handler.post(new Runnable() {

			@Override
			public void run() {
				List<YMMessage> chatList = YYIMChatManager.getInstance().getMessage(getRoomJid());
				handler.obtainMessage(MSGWHAT_MESSAGE_ADD, chatList).sendToTarget();
			}
		});
	}

	/**
	 * 更新标题
	 * @param message
	 */
	private void updateTitle(String jid) {
		String name = YYIMChatManager.getInstance().getNameByJid(jid);
		if (isMultiChat()) {
			name += "(" + YYIMChatManager.getInstance().getRoomMemberByJid(roomJid).size() + ")";
		}
		resetTopbarTitle(name);
	}

	/**
	 * 聊天接收者
	 * @author wudl
	 * @date 2014年12月16日
	 * @version V1.0
	 */
	private class ChatReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			List<YMMessage> chatList = YYIMChatManager.getInstance().getMessage(getRoomJid());
			if (YYIMDBNotifier.MESSAGE_ADD.equals(intent.getAction())) {
				handler.obtainMessage(MSGWHAT_MESSAGE_ADD, chatList).sendToTarget();
			} else if (YYIMDBNotifier.MESSAGE_UPDATE.equals(intent.getAction())) {
				handler.obtainMessage(MSGWHAT_MESSAGE_UPDATE, chatList).sendToTarget();
			}
		}

	}

	private static class ChatHandler extends Handler {

		WeakReference<ChatActivity> refActivity;

		private ChatHandler(ChatActivity activity) {
			refActivity = new WeakReference<ChatActivity>(activity);
		}

		private ChatActivity getActivity() {
			return refActivity.get();
		}

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RECORED_FAILED:
				ToastUtil.showLong(getActivity(), msg.obj.toString());
				break;
			case MSGWHAT_MESSAGE_ADD:
				// chatList
				List<YMMessage> chatList = (List<YMMessage>) msg.obj;
				getActivity().adapter.setChatList(chatList);
				getActivity().messageListView.setSelection(chatList.size() - 1);
				break;
			case MSGWHAT_MESSAGE_UPDATE:
				// chatList
				List<YMMessage> list = (List<YMMessage>) msg.obj;
				getActivity().adapter.setChatList(list);
				break;
			case MSGWHAT_CLOSESORT:
				getActivity().hideSortView();
				break;
			default:
				break;
			}
		}

	}

}
