package com.yonyou.sns.im.ui.emoji.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yonyou.sns.im.log.YMLogger;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.view.View;

/**
 * Emoji工具类
 *
 * @author litfb
 * @date 2014年11月10日
 * @version 1.0
 */
public class EmojiUtil {

	// public static final String STATIC_FACE_PREFIX = "f_static_";
	// public static final String DYNAMIC_FACE_PREFIX = "f";
	/** 表情正则 */
	private static final Pattern EMOTION_URL = Pattern.compile("\\[(\\S+?)\\]");

	public static final String STATIC_FACE_PREFIX = "smile_";
	public static final String DYNAMIC_FACE_PREFIX = "smile_";

	private EmojiUtil() {
		initEmojiMap();
	}

	private static EmojiUtil instance;

	public static EmojiUtil getInstance() {
		if (null == instance)
			instance = new EmojiUtil();
		return instance;
	}

	private Map<String, String> mEmojiMap;

	private void initEmojiMap() {
		mEmojiMap = new LinkedHashMap<String, String>();
		mEmojiMap.put("[:草泥马]", "shenshou_org");
		mEmojiMap.put("[:神马]", "horse2_org");
		mEmojiMap.put("[:浮云]", "fuyun_org");
		mEmojiMap.put("[:给力]", "geili_org");
		mEmojiMap.put("[:围观]", "wg_org");
		mEmojiMap.put("[:威武]", "vw_org");
		mEmojiMap.put("[:熊猫]", "panda_org");
		mEmojiMap.put("[:兔子]", "rabbit_org");
		mEmojiMap.put("[:奥特曼]", "otm_org");
		mEmojiMap.put("[:囧]", "j_org");
		mEmojiMap.put("[:互粉]", "hufen_org");
		mEmojiMap.put("[:礼物]", "liwu_org");
		mEmojiMap.put("[:呵呵]", "smilea_org");
		mEmojiMap.put("[:嘻嘻]", "tootha_org");
		mEmojiMap.put("[:哈哈]", "laugh");
		mEmojiMap.put("[:可爱]", "tza_org");
		mEmojiMap.put("[:可怜]", "kl_org");
		mEmojiMap.put("[:挖鼻屎]", "kbsa_org");
		mEmojiMap.put("[:吃惊]", "cj_org");
		mEmojiMap.put("[:害羞]", "shamea_org");
		mEmojiMap.put("[:挤眼]", "zy_org");
		mEmojiMap.put("[:闭嘴]", "bz_org");
		mEmojiMap.put("[:鄙视]", "bs2_org");
		mEmojiMap.put("[:爱你]", "lovea_org");
		mEmojiMap.put("[:泪]", "sada_org");
		mEmojiMap.put("[:偷笑]", "heia_org");
		mEmojiMap.put("[:亲亲]", "qq_org");
		mEmojiMap.put("[:生病]", "sb_org");
		mEmojiMap.put("[:太开心]", "mb_org");
		mEmojiMap.put("[:懒得理你]", "ldln_org");
		mEmojiMap.put("[:右哼哼]", "yhh_org");
		mEmojiMap.put("[:左哼哼]", "zhh_org");
		mEmojiMap.put("[:嘘]", "x_org");
		mEmojiMap.put("[:衰]", "cry");
		mEmojiMap.put("[:委屈]", "wq_org");
		mEmojiMap.put("[:吐]", "t_org");
		mEmojiMap.put("[:打哈欠]", "k_org");
		mEmojiMap.put("[:抱抱]", "bba_org");
		mEmojiMap.put("[:怒]", "angrya_org");
		mEmojiMap.put("[:疑问]", "yw_org");
		mEmojiMap.put("[:馋嘴]", "cza_org");
		mEmojiMap.put("[:拜拜]", "bye_org");
		mEmojiMap.put("[:思考]", "sk_org");
		mEmojiMap.put("[:汗]", "sweata_org");
		mEmojiMap.put("[:困]", "sleepya_org");
		mEmojiMap.put("[:睡觉]", "sleepa_org");
		mEmojiMap.put("[:钱]", "money_org");
		mEmojiMap.put("[:失望]", "sw_org");
		mEmojiMap.put("[:酷]", "cool_org");
		mEmojiMap.put("[:花心]", "hsa_org");
		mEmojiMap.put("[:哼]", "hatea_org");
		mEmojiMap.put("[:鼓掌]", "gza_org");
		mEmojiMap.put("[:晕]", "dizzya_org");
		mEmojiMap.put("[:悲伤]", "bs_org");
		mEmojiMap.put("[:抓狂]", "crazya_org");
		mEmojiMap.put("[:黑线]", "h_org");
		mEmojiMap.put("[:阴险]", "yx_org");
		mEmojiMap.put("[:怒骂]", "nm_org");
		mEmojiMap.put("[:心]", "hearta_org");
		mEmojiMap.put("[:伤心]", "unheart");
		mEmojiMap.put("[:猪头]", "pig");
		mEmojiMap.put("[:ok]", "ok_org");
		mEmojiMap.put("[:耶]", "ye_org");
		mEmojiMap.put("[:good]", "good_org");
		mEmojiMap.put("[:不要]", "no_org");
		mEmojiMap.put("[:赞]", "z2_org");
		mEmojiMap.put("[:来]", "come_org");
		mEmojiMap.put("[:弱]", "sad_org");
		mEmojiMap.put("[:蜡烛]", "lazu_org");
		mEmojiMap.put("[:钟]", "clock_org");
		mEmojiMap.put("[:话筒]", "m_org");
		mEmojiMap.put("[:蛋糕]", "cake");
	}

	public Map<String, String> getFaceMap() {
		return mEmojiMap;
	}

	public String getFaceId(String faceStr) {
		if (mEmojiMap.containsKey(faceStr)) {
			return mEmojiMap.get(faceStr);
		}
		return "";
	}

	/**
	 * 按尺寸放大缩小图片
	 * @param src
	 * @param scale
	 * @return
	 */
	public static Bitmap resizeBitmap(Bitmap src, float scale) {
		int bmpWidth = src.getWidth();
		int bmpHeight = src.getHeight();
		/* 产生reSize后的Bitmap对象 */
		Matrix matrix = new Matrix();
		// 放大的尺寸
		matrix.postScale(scale, scale);
		Bitmap resizeBmp = Bitmap.createBitmap(src, 0, 0, bmpWidth, bmpHeight, matrix, true);
		return resizeBmp;
	}

	/**
	 * 给图片增加padding
	 * 
	 * @param src
	 *            原始图片
	 * @param padding
	 * @return
	 */
	public static Bitmap addBitmapPadding(Bitmap src, int padding) {
		if (src == null || padding == 0) {
			return null;
		}
		// 图片宽度
		int w = src.getWidth();
		// 图片高度
		int h = src.getHeight();
		// 创建一个指定高宽的图片
		Bitmap newb = Bitmap.createBitmap(w + padding * 2, h + padding * 2, Config.ARGB_8888);
		// 创建画布
		Canvas cv = new Canvas(newb);
		// 绘制一个透明背景的图片
		cv.drawARGB(0, 0, 0, 0);
		// 在 padding,padding坐标开始画入 src
		cv.drawBitmap(src, padding, padding, null);
		// 保存
		cv.save(Canvas.ALL_SAVE_FLAG);
		// 存储
		cv.restore();
		return newb;
	}

	/**
	 * 获得图片span资源
	 * @param content
	 * @return
	 */
	public static ImageSpan getImageSpan(Context context, String content, float f) {
		String idStr = EmojiUtil.getInstance().getFaceId(content);
		Resources resources = context.getResources();
		int id = resources.getIdentifier(EmojiUtil.STATIC_FACE_PREFIX + idStr, "drawable", context.getPackageName());
		if (id > 0) {
			try {
				// InputStream input = getResources().openRawResource(id);
				// // 获取Bitmap对象
				// Bitmap bitmap = BitmapFactory.decodeStream(input);
				// // 放大
				// bitmap = EmojiUtil.resizeBitmap(bitmap, 2);
				// // 添加padding
				// bitmap = EmojiUtil.addBitmapPadding(bitmap, 2);
				// ImageSpan imageSpan = new ImageSpan(getContext(), bitmap,
				// ImageSpan.ALIGN_BOTTOM);
				int size = Math.round(f) + 10;
				Drawable emoji = context.getResources().getDrawable(id);
				// 画出要显示图片的边界
				emoji.setBounds(4, 0, size + 8, size);
				ImageSpan imageSpan = new ImageSpan(emoji, ImageSpan.ALIGN_BOTTOM);
				return imageSpan;
			} catch (Exception e) {
				YMLogger.d(e);
			}
		}
		return null;
	}

	public static DynamicDrawableSpan getDynamicImageSpan(final View view, String tag, float f) {
		String idStr = EmojiUtil.getInstance().getFaceId(tag);
		Resources resources = view.getContext().getResources();
		int id = resources.getIdentifier(EmojiUtil.DYNAMIC_FACE_PREFIX + idStr, "drawable", view.getContext()
				.getPackageName());
		if (id > 0) {
			try {
				AnimatedImageSpan imageSpan = new AnimatedImageSpan(new AnimatedGifDrawable(view.getContext()
						.getResources(), Math.round(f) + 10, view.getContext().getResources().openRawResource(id),
						new AnimatedGifDrawable.UpdateListener() {

							@Override
							public void update() {
								// update the textview
								view.postInvalidate();
							}
						}));
				return imageSpan;
			} catch (Exception e) {
				YMLogger.d(e);
			}
		}
		return null;
	}

	/**
	 * 处理字符串中的表情
	 * 
	 * @param context
	 * @param message
	 * @return
	 */
	public static CharSequence convertNormalStringToSpannableString(Context context, String message) {
		String hackTxt;
		if (message.startsWith("[") && message.endsWith("]")) {
			hackTxt = message + " ";
		} else {
			hackTxt = message;
		}
		// SpannableString
		SpannableString value = SpannableString.valueOf(hackTxt);
		// 正则匹配
		Matcher localMatcher = EMOTION_URL.matcher(value);
		while (localMatcher.find()) {
			// 匹配项
			String emojiKey = localMatcher.group(0);
			int k = localMatcher.start();
			int m = localMatcher.end();
			// 长度判断
			if (m - k >= 8) {
				continue;
			}
			// 是否是表情判断
			String emojiVal = EmojiUtil.getInstance().getFaceMap().get(emojiKey);
			if (emojiVal == null) {
				continue;
			}
			// 取表情资源
			int face = context.getResources().getIdentifier(EmojiUtil.STATIC_FACE_PREFIX + emojiVal, "drawable",
					context.getPackageName());
			// 获得bitmap
			Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), face);
			// 替换文本
			if (bitmap != null) {
				ImageSpan localImageSpan = new ImageSpan(context, bitmap, ImageSpan.ALIGN_BOTTOM);
				value.setSpan(localImageSpan, k, m, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return value;
	}

	// /**
	// * 获得小尺寸表情
	// *
	// * @param bitmap
	// * @return
	// */
	// private static Bitmap genSmallEmojiBitmap(Bitmap bitmap) {
	// if (bitmap == null) {
	// return null;
	// }
	// // 原尺寸
	// int rawHeigh = bitmap.getHeight();
	// int rawWidth = bitmap.getHeight();
	// // 新尺寸
	// int newHeight = 10;
	// int newWidth = 10;
	// // 计算缩放因子
	// float heightScale = ((float) newHeight) / rawHeigh;
	// float widthScale = ((float) newWidth) / rawWidth;
	// // 新建立矩阵
	// Matrix matrix = new Matrix();
	// matrix.postScale(heightScale, widthScale);
	// // 设置图片的旋转角度
	// // matrix.postRotate(-30);
	// // 设置图片的倾斜
	// // matrix.postSkew(0.1f, 0.1f);
	// // 将图片大小压缩,压缩后图片的宽和高以及kB大小均会变化
	// bitmap = Bitmap.createBitmap(bitmap, 0, 0, rawWidth, rawHeigh, matrix, true);
	// return bitmap;
	// }

}
