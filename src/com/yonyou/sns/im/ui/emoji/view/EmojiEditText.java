package com.yonyou.sns.im.ui.emoji.view;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.widget.EditText;

import com.yonyou.sns.im.ui.emoji.util.EmojiUtil;

public class EmojiEditText extends EditText {

	private static final String START_CHAR = "[";
	private static final String END_CHAR = "]";

	public EmojiEditText(Context context) {
		super(context);
		init();
	}

	public EmojiEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public EmojiEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		SpannableString content = new SpannableString(text);
		emotifySpannable(content);
		super.setText(content, BufferType.SPANNABLE);
	}

	private void init() {
		this.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				emotifySpannable(s);

			}
		});
	}

	/**
	 * Work through the contents of the string, and replace any occurrences of
	 * [icon] with the imageSpan
	 * 
	 * 将[icon]的文字替换成图片
	 * 
	 * @param spannable
	 */
	private void emotifySpannable(Spannable spannable) {
		int length = spannable.length();
		int position = 0;
		int tagStartPosition = 0;
		int tagLength = 0;
		StringBuilder buffer = new StringBuilder();
		boolean inTag = false;

		if (length <= 0)
			return;

		do {
			String c = spannable.subSequence(position, position + 1).toString();

			// 没有在tag中并且等于【
			if (!inTag && c.equals(START_CHAR)) {
				buffer = new StringBuilder();
				tagStartPosition = position;
				// Log.d(TAG, "   Entering tag at " + tagStartPosition);

				inTag = true;
				tagLength = 0;
			}

			// 在tag中
			if (inTag) {
				buffer.append(c);
				tagLength++;

				// 是否到了】
				if (c.equals(END_CHAR)) {
					inTag = false;

					String tag = buffer.toString();
					int tagEnd = tagStartPosition + tagLength;

					int lastIndex = tag.lastIndexOf(START_CHAR);
					if (lastIndex > 0) {
						tagStartPosition = tagStartPosition + lastIndex;
						tag = tag.substring(lastIndex, tag.length());
					}
					ImageSpan imageSpan = EmojiUtil.getImageSpan(getContext(), tag, getTextSize());
					if (imageSpan != null)
						spannable.setSpan(imageSpan, tagStartPosition, tagEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}

			position++;
		} while (position < length);
	}
}
