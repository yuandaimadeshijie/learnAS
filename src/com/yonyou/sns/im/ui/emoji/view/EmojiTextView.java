package com.yonyou.sns.im.ui.emoji.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.ui.emoji.util.EmojiUtil;

/**
 * EmojiTextView
 *
 * @author litfb
 * @date 2014年11月10日
 * @version 1.0
 */
public class EmojiTextView extends TextView {

	private static final String START_CHAR = "[";
	private static final String END_CHAR = "]";
	private boolean mIsDynamic;

	public EmojiTextView(Context context) {
		super(context);
		init(null);
	}

	public EmojiTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public EmojiTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		if (attrs == null) {
			mIsDynamic = false;
		} else {
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Emojicon);
			mIsDynamic = a.getBoolean(R.styleable.Emojicon_isDynamic, false);
			a.recycle();
		}
		setText(getText());
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		SpannableString content = new SpannableString(text);
		emotifySpannable(content);
		super.setText(content, BufferType.SPANNABLE);
	}

	/**
	 * Work through the contents of the string, and replace any occurrences of
	 * [icon] with the imageSpan
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

			if (!inTag && c.equals(START_CHAR)) {
				buffer = new StringBuilder();
				tagStartPosition = position;
				// Log.d(TAG, "   Entering tag at " + tagStartPosition);

				inTag = true;
				tagLength = 0;
			}

			if (inTag) {
				buffer.append(c);
				tagLength++;

				// Have we reached end of the tag?
				if (c.equals(END_CHAR)) {
					inTag = false;

					String tag = buffer.toString();
					int tagEnd = tagStartPosition + tagLength;
					int lastIndex = tag.lastIndexOf(START_CHAR);
					if (lastIndex > 0) {
						tagStartPosition = tagStartPosition + lastIndex;
						tag = tag.substring(lastIndex, tag.length());
					}

					if (mIsDynamic) {
						DynamicDrawableSpan imageSpan = EmojiUtil.getDynamicImageSpan(this, tag, getTextSize());
						if (imageSpan != null)
							spannable.setSpan(imageSpan, tagStartPosition, tagEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					} else {
						ImageSpan imageSpan = EmojiUtil.getImageSpan(getContext(), tag, getTextSize());
						if (imageSpan != null)
							spannable.setSpan(imageSpan, tagStartPosition, tagEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
				}
			}

			position++;
		} while (position < length);
	}

}
