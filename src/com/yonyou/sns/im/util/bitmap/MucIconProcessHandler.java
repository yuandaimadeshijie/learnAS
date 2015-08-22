package com.yonyou.sns.im.util.bitmap;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.cache.bitmap.ProcessBitmapHandler;
import com.yonyou.sns.im.cache.bitmap.ResizerBitmapHandler;
import com.yonyou.sns.im.core.YYIMChatManager;
import com.yonyou.sns.im.entity.YMRoomMember;
import com.yonyou.sns.im.util.XMPPHelper;
import com.yonyou.sns.im.util.common.BitmapUtils;

/**
 * 房间头像处理类
 * 
 * @author wudl
 * 
 */
public class MucIconProcessHandler extends ProcessBitmapHandler {

	/** 成员默认头像 */
	public final int defaultIcon = R.drawable.icon_default_user;
	/** 目标图片宽度 */
	protected int mImageWidth;
	/** 目标图片高度 */
	protected int mImageHeight;
	/** 上下文 */
	protected Context context;

	private BitmapCacheManager avatarCacheManager;

	/**
	 * 初始化一个目标提供图像的宽度和高度的来处理图像
	 * 
	 * @param context
	 * @param imageWidth
	 * @param imageHeight
	 */
	public MucIconProcessHandler(Context context, int imageWidth, int imageHeight) {
		this.context = context;
		this.mImageWidth = imageWidth;
		this.mImageHeight = imageHeight;
		avatarCacheManager = new BitmapCacheManager(context, true, BitmapCacheManager.CIRCLE_BITMAP,
				BitmapCacheManager.BITMAP_DPSIZE_40);
		avatarCacheManager.generateBitmapCacheWork();
	}

	@Override
	public void initDiskCacheInternal() {

	}

	@Override
	public void clearCacheInternal() {

	}

	@Override
	public void flushCacheInternal() {

	}

	@Override
	public void closeCacheInternal() {

	}

	@Override
	protected Bitmap processBitmap(Object data) {
		// 房间头像
		Bitmap mucIcon = null;
		if (data instanceof String) {
			// 房间jid
			String jid = (String) data;
			// 查询房间成员
			List<YMRoomMember> membres = YYIMChatManager.getInstance().getRoomMemberByJid(jid);
			// 成员头像列表
			List<Bitmap> membersIcon = new ArrayList<Bitmap>();
			// 取房间成员的前三个用于生成群图片
			for (int i = 0; i < membres.size(); i++) {
				if (i == 3) {
					if (membres.size() > 4) {
						membersIcon.add(createLastIcon(String.valueOf(membres.size()), mImageWidth, mImageHeight));
						break;
					}
				} else if (i > 3) {
					break;
				}
				YMRoomMember member = membres.get(i);
				// 获取成员头像路径
				String icon = member.getUserIcon();
				// 获取头像资源
				byte[] buffers = avatarCacheManager.syncLoadFormCache(XMPPHelper.getFullFilePath(icon));
				Bitmap bitmap;
				if (buffers == null || buffers.length <= 0) {
					bitmap = ResizerBitmapHandler.decodeSampledBitmapFromResource(context.getResources(), defaultIcon,
							mImageWidth, mImageHeight);
				} else {
					bitmap = BitmapFactory.decodeByteArray(buffers, 0, buffers.length);
				}

				// 创建指定大小的图片
				bitmap = Bitmap.createScaledBitmap(bitmap, mImageWidth, mImageHeight, false);
				bitmap = BitmapUtils.toRoundBitmap(bitmap);
				// 不为空就加入到成员头像集合
				membersIcon.add(bitmap);
			}

			// 合成群头像
			if (membersIcon.size() > 0) {
				// 绘制群图标
				mucIcon = combineBitmaps(2, membersIcon);
			}
		}
		return mucIcon;
	}

	/**
	 * 绘制群组最后一个圆图形
	 * 
	 * @param num
	 *            群组的成员数
	 * @return
	 */
	private Bitmap createLastIcon(String num, int width, int height) {
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.parseColor("#00000000"));
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setARGB(255, 226, 226, 226);
		RectF rectF = new RectF(0, 0, width, height);
		canvas.drawRoundRect(rectF, width, height, paint);
		paint.setColor(Color.WHITE);
		paint.setTextSize(width / 2);
		FontMetricsInt fontMetrics = paint.getFontMetricsInt();
		int baseline = (int) (rectF.top + (rectF.bottom - rectF.top - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top);
		paint.setTextAlign(Paint.Align.CENTER);
		canvas.drawText(num, width / 2, baseline, paint);
		return bitmap;
	}

	/**
	 * 将多个Bitmap合并成一个图片。
	 * 
	 * @param columns
	 *            将多个图合成多少列
	 * @param bitmaps
	 *            要合成的图片
	 * @return
	 */
	public static Bitmap combineBitmaps(int columns, List<Bitmap> bitmaps) {
		if (columns <= 0 || bitmaps == null || bitmaps.size() == 0) {
			throw new IllegalArgumentException("Wrong parameters: columns must > 0 and bitmaps.length must > 0.");
		}
		int maxWidthPerImage = 0;
		int maxHeightPerImage = 0;
		for (Bitmap b : bitmaps) {
			maxWidthPerImage = maxWidthPerImage > b.getWidth() ? maxWidthPerImage : b.getWidth();
			maxHeightPerImage = maxHeightPerImage > b.getHeight() ? maxHeightPerImage : b.getHeight();
		}
		int rows = 0;
		// 列数不能超过总大小
		if (columns >= bitmaps.size()) {
			rows = 1;
			columns = bitmaps.size();
		}
		Bitmap newBitmap = Bitmap.createBitmap(maxWidthPerImage * columns, maxHeightPerImage * columns, Config.ALPHA_8);
		if (bitmaps.size() == 2) {
			newBitmap = mixtureBitmap(newBitmap, bitmaps.get(0), new PointF(20, 20));
			newBitmap = mixtureBitmap(newBitmap, bitmaps.get(1), new PointF(maxWidthPerImage - 20,
					maxHeightPerImage - 20));
		} else {
			// 判断是否需要居中显示
			boolean isModify = bitmaps.size() == 3;
			// 居中绘制多余的头像
			if (isModify) {
				newBitmap = mixtureBitmap(newBitmap, bitmaps.get(0), new PointF(maxWidthPerImage / 2, 0));
				bitmaps.remove(0);
			}
			rows = bitmaps.size() / columns;
			// 均匀绘制剩余图标
			for (int x = 0; x < rows; x++) {
				for (int y = 0; y < columns; y++) {
					int index = x * columns + y;
					if (index >= bitmaps.size())
						break;
					if (isModify) {
						// 绘制了居中显示就从第二行开始绘制
						newBitmap = mixtureBitmap(newBitmap, bitmaps.get(index), new PointF(y * maxWidthPerImage,
								(x + 1) * maxHeightPerImage - 12));
					} else {
						// 从第一行开始绘制
						newBitmap = mixtureBitmap(newBitmap, bitmaps.get(index), new PointF(y * maxWidthPerImage, x
								* maxHeightPerImage));
					}

				}
			}
		}
		return newBitmap;
	}

	/**
	 * 将位图变成透明
	 * 
	 * @param sourceImg
	 * @param number
	 *            number的范围是0-100，0表示完全透明即完全看不到
	 * @return
	 */
	public static Bitmap getTransparentBitmap(Bitmap sourceImg, int number) {
		int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];

		sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0, sourceImg

		.getWidth(), sourceImg.getHeight());// 获得图片的ARGB值

		number = number * 255 / 100;

		for (int i = 0; i < argb.length; i++) {

			argb[i] = (number << 24) | (argb[i] & 0x00FFFFFF);

		}

		sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(), sourceImg

		.getHeight(), Config.ARGB_8888);

		return sourceImg;
	}

	/**
	 * Mix two Bitmap as one.
	 * 
	 * @param bitmapOne
	 * @param bitmapTwo
	 * @param point
	 *            where the second bitmap is painted.
	 * @return
	 */
	public static Bitmap mixtureBitmap(Bitmap first, Bitmap second, PointF fromPoint) {
		if (first == null || second == null || fromPoint == null) {
			return null;
		}
		Bitmap newBitmap = Bitmap.createBitmap(first.getWidth(), first.getHeight(), Config.ARGB_8888);
		Canvas cv = new Canvas(newBitmap);
		cv.drawColor(Color.parseColor("#ffffffff"));
		Paint paint = new Paint();
		// 设置相交模式
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_OVER));
		Rect rect = new Rect(0, 0, first.getWidth(), first.getHeight());
		RectF dst = new RectF(fromPoint.x, fromPoint.y, fromPoint.x + second.getWidth(), fromPoint.y
				+ second.getHeight());
		Rect rect2 = new Rect(0, 0, second.getWidth(), second.getHeight());
		cv.drawBitmap(first, rect, rect, paint);
		cv.drawBitmap(second, rect2, dst, paint);
		return newBitmap;
	}
}
