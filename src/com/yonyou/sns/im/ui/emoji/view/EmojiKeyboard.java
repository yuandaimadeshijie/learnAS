package com.yonyou.sns.im.ui.emoji.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.ui.emoji.util.EmojiUtil;
import com.yonyou.sns.im.ui.emoji.view.EmojiLinearLayout.OnEmojiClickedListener;

public class EmojiKeyboard extends FrameLayout {

	private final static String EMOJI_PREFERENCE = "emoji_preferences";
	private final static String PREF_KEY_LAST_TAB = "last_tab";
	private final static String PREF_KEY_RECENT_EMOJI = "recent_remoji";
	private final static int[] mIcons = { R.drawable.selector_emoji_recent, R.drawable.selector_emoji_people };
	private ViewPager mPager;
	private ImageButton mBackSpace;
	private PagerSlidingTabStrip mTabs;
	private View mEmptyView;
	private GridView mRecentGridView;
	private SharedPreferences mPreference;
	private ArrayList<View> mViews;
	private EmojiPagerAdapter mPagerAdapter;
	private Handler mHandler = new Handler();
	private boolean mContinueDel;

	private EventListener mListener;

	public void setEventListener(EventListener listener) {
		this.mListener = listener;
	}

	public static interface EventListener {

		public void onBackspace();

		public void onEmojiSelected(String res);
	}

	public EmojiKeyboard(Context context) {
		super(context);
		init();
	}

	public EmojiKeyboard(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		init();
	}

	private void init() {
		mPreference = getContext().getSharedPreferences(EMOJI_PREFERENCE, Context.MODE_PRIVATE);
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View root = inflater.inflate(R.layout.emoji_keyboard, this);

		// 下面的tab和滚动条区域
		mTabs = (PagerSlidingTabStrip) root.findViewById(R.id.tabs);

		// 上面的Flipper区域
		mPager = (ViewPager) root.findViewById(R.id.parent_pager);

		// 删除键
		mBackSpace = (ImageButton) root.findViewById(R.id.back_space);

		mViews = new ArrayList<View>();

		for (int i = 0; i < mIcons.length; i++) {
			if (i == 0) {
				// 初始化最近使用过的表情View
				EmojiGridAdapter emojiGridAdapter = new EmojiGridAdapter(getContext(), new String[] {});
				View mRecentsWrap = inflater.inflate(R.layout.emoji_tab_recent, null);

				mRecentGridView = (GridView) mRecentsWrap.findViewById(R.id.grid);
				mRecentGridView.setAdapter(emojiGridAdapter);
				mRecentGridView.setOnItemClickListener(mRecentItemClickListener);
				mEmptyView = mRecentsWrap.findViewById(R.id.no_recent);
				mViews.add(mRecentsWrap);
			} else {
				// 初始化所有的表情View
				EmojiLinearLayout emojiView = (EmojiLinearLayout) inflater.inflate(R.layout.emoji_tab_qq, null);

				emojiView.setOnEmojiClickedListener(mOnEmojiClickedListener);
				mViews.add(emojiView);
			}
		}

		// 实际加载最近使用过的表情
		loadRecent();

		// 创建page的adapter，并设置进去
		mPagerAdapter = new EmojiPagerAdapter();
		mPager.setAdapter(mPagerAdapter);

		// 指定tab页对应的page页面，可以获取page的adapter，并设置page的PageListener
		mTabs.setOnPageChangeListener(mOnPageChangeListener);
		mTabs.setViewPager(mPager);

		// 从文件中获取上一次打开的是最近还是所有
		mPager.setCurrentItem(mPreference.getInt(PREF_KEY_LAST_TAB, 1));

		// 返回按钮设置监听，都是让注册到此页面的上级页面具体实现
		mBackSpace.setOnClickListener(mBackSpaceClickListener);
		mBackSpace.setOnLongClickListener(mBackSpaceLongClickListener);
		mBackSpace.setOnTouchListener(mBackSpaceTouchListener);
	}

	/**
	 * 设置最近表情的点击事件
	 */
	private OnItemClickListener mRecentItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			String selected = (String) view.getTag(view.getId());
			if (mListener != null)
				mListener.onEmojiSelected(selected);
		}
	};

	/**
	 * 点击执行输入框的删除
	 */
	private OnClickListener mBackSpaceClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mListener != null) {
				mListener.onBackspace();
			}
		}
	};

	/**
	 * 长按执行输入框的删除
	 */
	private OnLongClickListener mBackSpaceLongClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			if (mListener == null) {
				return false;
			}
			mContinueDel = true;
			mHandler.post(mContinueDelRunnable);
			return false;
		}
	};

	/**
	 * 抬起手指停止输入框删除
	 */
	private OnTouchListener mBackSpaceTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action = event.getAction();
			if ((action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) && mContinueDel) {
				mContinueDel = false;
				mHandler.removeCallbacks(mContinueDelRunnable);
			}
			return false;
		}
	};

	/**
	 * 执行删除的线程，其中调用上级监听器。
	 */
	private Runnable mContinueDelRunnable = new Runnable() {

		@Override
		public void run() {
			if (mContinueDel) {
				mListener.onBackspace();
				mHandler.postDelayed(this, 50);
			}
		}
	};

	/**
	 * 设置所有表情的点击事件
	 */
	private OnEmojiClickedListener mOnEmojiClickedListener = new OnEmojiClickedListener() {

		@Override
		public void onEmojiClicked(String emoji) {
			if (TextUtils.isEmpty(emoji)) {
				return;
			}
			if (mListener != null) {
				mListener.onEmojiSelected(emoji);
			}
			addToRecent(emoji);
		}
	};

	/**
	 * 实现page切换的时候的监听器，将当前的页面标示写入文件
	 */
	private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {

		@Override
		public void onPageSelected(int item) {
			mPreference.edit().putInt(PREF_KEY_LAST_TAB, item).commit();
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	};

	/**
	 * 从文件中获取最近使用过的表情
	 */
	private void loadRecent() {
		String recentEmoji = mPreference.getString(PREF_KEY_RECENT_EMOJI, null);
		if (!TextUtils.isEmpty(recentEmoji)) {
			mEmptyView.setVisibility(View.GONE);
			String[] recentEmojis = recentEmoji.split(",");
			Collections.reverse(Arrays.asList(recentEmojis));
			EmojiGridAdapter recentAdapter = (EmojiGridAdapter) mRecentGridView.getAdapter();
			recentAdapter.setEmoji(recentEmojis);
			recentAdapter.notifyDataSetChanged();
		}
	}

	private void addToRecent(String selected) {
		String recentEmoji = mPreference.getString(PREF_KEY_RECENT_EMOJI, null);
		if (TextUtils.isEmpty(recentEmoji)) {
			recentEmoji = selected + ",";
		} else {
			String[] recs = recentEmoji.split(",");
			List<String> list = Arrays.asList(recs);
			List<String> newList = new ArrayList<String>(list);
			for (int i = newList.size() - 1; i >= 0; i--) {
				if (newList.get(i).equals(selected)) {
					newList.remove(i);
					break;
				}
			}
			newList.add(selected);
			if (newList.size() > 21)
				newList.remove(0);// 大于21个表情时，删除最后一个
			StringBuilder builder = new StringBuilder();
			for (String str : newList) {
				builder.append(str).append(",");
			}
			recentEmoji = builder.toString();
		}
		mPreference.edit().putString(PREF_KEY_RECENT_EMOJI, recentEmoji).commit();
		loadRecent();
	}

	/**
	 * 将键入的表情加入到输入框中
	 * @param editText
	 * @param emoji
	 */
	public static void input(EditText editText, String emoji) {
		if (editText == null || emoji == null) {
			return;
		}

		Log.v("test_log", "输入：" + emoji);

		int start = editText.getSelectionStart();
		int end = editText.getSelectionEnd();
		if (start < 0) {
			editText.append(emoji);
		} else {
			editText.getText().replace(Math.min(start, end), Math.max(start, end), emoji, 0, emoji.length());
		}
	}

	/**
	 * 调用文本输入框的默认删除事件
	 * @param editText
	 */
	public static void backspace(EditText editText) {
		KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
		editText.dispatchKeyEvent(event);
	}

	/**
	 * EmojiGridAdapter 表情集合的adapter，通过ViewHolder记录表情的文本
	 * @author yh
	 *
	 */
	class EmojiGridAdapter extends BaseAdapter {

		private String[] mEmojis;
		private LayoutInflater mInflater;

		public EmojiGridAdapter(Context c, String[] emojis) {
			mInflater = LayoutInflater.from(c);
			mEmojis = emojis;
		}

		public void setEmoji(String[] emojis) {
			mEmojis = emojis;
		}

		public int getCount() {
			return mEmojis.length;
		}

		public Object getItem(int position) {
			return mEmojis[position];
		}

		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			if (convertView == null) {
				rowView = mInflater.inflate(R.layout.emoji_cell, parent, false);
				ViewHolder viewHolder = new ViewHolder((ImageView) rowView);
				rowView.setTag(viewHolder);
			}

			ViewHolder viewHolder = (ViewHolder) rowView.getTag();
			int resId = getResources().getIdentifier(
					EmojiUtil.STATIC_FACE_PREFIX + EmojiUtil.getInstance().getFaceId(mEmojis[position]), "drawable",
					getContext().getPackageName());
			viewHolder.imageView.setImageResource(resId);
			viewHolder.imageView.setTag(viewHolder.imageView.getId(), mEmojis[position]);
			return rowView;
		}
	}

	static class ViewHolder {

		public ImageView imageView;

		public ViewHolder(ImageView imageView) {
			this.imageView = imageView;
		};
	}

	/**
	 * EmojiPagerAdapter  pageflipper的adapter
	 * 
	 * @author yh
	 *
	 */
	private class EmojiPagerAdapter extends PagerAdapter implements PagerSlidingTabStrip.IconTabProvider {

		private EmojiPagerAdapter() {
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object paramObject) {
			View localObject = mViews.get(position);
			container.removeView(localObject);
		}

		@Override
		public int getCount() {
			return mViews.size();
		}

		@Override
		public int getPageIconResId(int paramInt) {
			return mIcons[paramInt];
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View gridView = mViews.get(position);
			container.addView(gridView);
			return gridView;
		}

		@Override
		public boolean isViewFromObject(View paramView, Object paramObject) {
			return paramView == paramObject;
		}
	}
}
