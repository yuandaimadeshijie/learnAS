package com.yonyou.sns.im.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.yonyou.sns.im.R;

/**
 * 
 * 自定义字母定位控件
 * 想使用此控件，adapter必须实现SectionIndexer接口的getPositionForSection方法
 * 
 * @author yh
 *
 */
public class LetterLocationBar extends View {

	/** SectionIndexer置顶 */
	public static final char SECTION_TOP = 0x2191;

	private static final char[] SECTIONS = new char[] { SECTION_TOP, 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
			'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '#' };

	/** list */
	private ListView listView;
	/** text */
	private TextView textView;
	/** indexer */
	private SectionIndexer sectionIndexter;
	/** paint */
	private Paint paint = new Paint();

	public LetterLocationBar(Context context) {
		super(context);
		init();
	}

	public LetterLocationBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LetterLocationBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		paint.setColor(getResources().getColor(R.color.gray_57));
		paint.setStyle(Style.FILL);
		paint.setTextAlign(Paint.Align.CENTER);
	}

	public void setListView(ListView listView) {
		this.listView = listView;
		this.sectionIndexter = (SectionIndexer) listView.getAdapter();
	}

	public void setTextView(TextView mDialogText) {
		this.textView = mDialogText;
	}

	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);

		int i = (int) event.getY();

		// 计算当前点击的是第几位元素
		int idx = i / (getMeasuredHeight() / SECTIONS.length);

		if (idx >= SECTIONS.length) {
			idx = SECTIONS.length - 1;
		} else if (idx < 0) {
			idx = 0;
		}

		if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
			// 设置按住或者拖动时候更换背景图片
			setBackgroundResource(R.color.transparent_dark_66);

			textView.setVisibility(View.VISIBLE);
			textView.setText(String.valueOf(SECTIONS[idx]));
			textView.setTextSize(34);

			// adapter中实现了此字母在列表中对应的位置
			int position = 0;
			if (idx != 0) {
				position = sectionIndexter.getPositionForSection(SECTIONS[idx]);
			}

			if (position == -1) {
				return true;
			}

			// 设置指定位置为定位行
			listView.setSelection(position);
		} else {
			textView.setVisibility(View.INVISIBLE);
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			setBackgroundResource(R.color.transparent);
		}
		return true;
	}

	/**
	 * 
	 * 绘图
	 * 
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		float widthCenter = getMeasuredWidth() / 2;

		if (SECTIONS.length > 0) {
			float height = getMeasuredHeight() / SECTIONS.length;

			for (int i = 0; i < SECTIONS.length; i++) {
				paint.setTextSize((float) (height * 0.8));
				canvas.drawText(String.valueOf(SECTIONS[i]), widthCenter, (i + 1) * height, paint);
			}
		}

		this.invalidate();
		super.onDraw(canvas);
	}

}
