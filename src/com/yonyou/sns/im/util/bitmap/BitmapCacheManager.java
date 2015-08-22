package com.yonyou.sns.im.util.bitmap;

import android.content.Context;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.cache.ICacheCallBackHandler;
import com.yonyou.sns.im.cache.ICacheDataProcessHandler;
import com.yonyou.sns.im.cache.bitmap.BitmapCacheWork;
import com.yonyou.sns.im.cache.bitmap.CircleBitmapCallBackHanlder;
import com.yonyou.sns.im.cache.bitmap.DownloadBitmapHandler;
import com.yonyou.sns.im.cache.bitmap.NormalBitmapCallBackHanlder;
import com.yonyou.sns.im.cache.bitmap.ResizerBitmapHandler;
import com.yonyou.sns.im.cache.bitmap.RoundCornerBitmapCallBackHanlder;
import com.yonyou.sns.im.core.YYIMChat;
import com.yonyou.sns.im.util.common.DensityUtils;
import com.yonyou.sns.im.util.common.LocalBigImageUtil;

/**
 * Bitmap缓存管理器
 *
 * @author litfb
 * @date 2014年10月11日
 * @version 1.0
 */
public class BitmapCacheManager {

	/** 图片尺寸40dp-默认 */
	public static final int BITMAP_DPSIZE_40 = 40;
	/** 图片尺寸80dp */
	public static final int BITMAP_DPSIZE_80 = 80;
	/** 图片尺寸80dp */
	public static final int BITMAP_DPSIZE_120 = 120;

	/** 普通矩形图像 */
	public static final int NORMAL_BITMAP = 0;
	/** 圆形图像 */
	public static final int CIRCLE_BITMAP = 1;
	/** 圆角图像 */
	public static final int ROUND_CORNER_BITMAP = 2;

	/** context */
	private Context context;
	/** BitmapCacheWork */
	private BitmapCacheWork bitmapCacheWork;
	/** 是否url图片 */
	private boolean isUrl;
	/** 是否圆角 */
	private int imageType;
	/** 图片宽度px*/
	private int imageWidth;
	/** 图片高度px */
	private int imageHeight;
	/** 默认图片 */
	private int defaultBitmapResId = R.drawable.icon_default_user;

	/**
	 * 初始化url图片生成器（URl图片、不做圆形处理）
	 * 
	 * @param context
	 * @param application
	 */
	public BitmapCacheManager(Context context) {
		this(context, true, NORMAL_BITMAP);
	}

	/**
	 * 初始化本地图片生成器（不做圆形处理）
	 * 
	 * @param context
	 * @param isUrl 是否是URL图片
	 */
	public BitmapCacheManager(Context context, boolean isUrl) {
		this(context, isUrl, NORMAL_BITMAP);
	}

	/**
	 * 初始化生成器
	 * 
	 * @param context
	 * @param isUrl 是否是URL图片
	 * @param imageType 是否需要处理成圆形展示
	 */
	public BitmapCacheManager(Context context, boolean isUrl, int imageType) {
		this.context = context;
		this.isUrl = isUrl;
		this.imageType = imageType;
	}

	/**
	 * 初始化生成器
	 * 
	 * @param context
	 * @param isUrl 是否是URL图片
	 * @param imageType 是否需要处理成圆形展示
	 * @param imageDpSize
	 */
	public BitmapCacheManager(Context context, boolean isUrl, int imageType, int imageDpSize) {
		this(context, isUrl, imageType);
		setImageWidthDp(imageDpSize);
		setImageHeightDp(imageDpSize);
	}

	/**
	 * 设置宽dp
	 * 
	 * @param width
	 */
	public void setImageWidthDp(float width) {
		this.imageWidth = DensityUtils.dipTopx(context, width);
	}

	/**
	 * 设置宽px
	 * 
	 * @param width
	 */
	public void setImageWidthPx(int width) {
		this.imageWidth = width;
	}

	/**
	 * width px value
	 * 
	 * @return
	 */
	protected int getImageWidth() {
		if (imageWidth <= 0) {
			setImageWidthDp(BITMAP_DPSIZE_40);
		}
		return imageWidth;
	}

	/**
	 * 设置高dp
	 * 
	 * @param width
	 */
	public void setImageHeightDp(float height) {
		this.imageHeight = DensityUtils.dipTopx(context, height);
	}

	/**
	 * 设置高px
	 * 
	 * @param width
	 */
	public void setImageHeightPx(int height) {
		this.imageHeight = height;
	}

	/**
	 * height px value
	 * 
	 * @return
	 */
	public int getImageHeight() {
		if (imageHeight <= 0) {
			setImageHeightDp(BITMAP_DPSIZE_40);
		}
		return imageHeight;
	}

	/**
	 * 设置默认的图片
	 * 
	 * @param bitmap
	 */
	public void setDefaultImage(int resId) {
		this.defaultBitmapResId = resId;
	}

	/**
	 * 
	 * 获取图片缓存
	 * 
	 * @param data
	 * @param responseObject
	 */
	public void loadFormCache(Object data, Object responseObject) {
		bitmapCacheWork.loadFormCache(data, responseObject);
	}

	/**
	 * 同步获得图片缓存
	 * 
	 * @param data
	 * @return
	 */
	public byte[] syncLoadFormCache(Object data) {
		return bitmapCacheWork.syncLoadFormCache(data);
	}

	/**
	 * 
	 * 生成需求的图片缓存机制处理
	 * 
	 * @return
	 */
	public BitmapCacheWork generateBitmapCacheWork() {
		// 数据处理器
		ICacheDataProcessHandler cacheDataProcessHandler;
		if (isUrl) {
			cacheDataProcessHandler = new DownloadBitmapHandler(context, getImageWidth(), getImageHeight());
		} else {
			cacheDataProcessHandler = new ResizerBitmapHandler(context, getImageWidth(), getImageHeight());
		}
		// 回调处理
		ICacheCallBackHandler cacheCallBackHandler;
		switch (imageType) {
		case NORMAL_BITMAP:
			cacheCallBackHandler = new NormalBitmapCallBackHanlder();
			break;
		case CIRCLE_BITMAP:
			cacheCallBackHandler = new CircleBitmapCallBackHanlder();
			break;
		case ROUND_CORNER_BITMAP:
			cacheCallBackHandler = new RoundCornerBitmapCallBackHanlder();
			break;
		default:
			cacheCallBackHandler = new NormalBitmapCallBackHanlder();
			break;
		}
		// 默认图片设置
		cacheCallBackHandler.setLoadingImage(LocalBigImageUtil.getBitmapFromResource(context.getResources(),
				this.defaultBitmapResId, getImageWidth(), getImageHeight()));

		// BitmapCacheWork
		bitmapCacheWork = new BitmapCacheWork(context);
		bitmapCacheWork.setProcessDataHandler(cacheDataProcessHandler);
		bitmapCacheWork.setCallBackHandler(cacheCallBackHandler);
		// 使用全局缓存
		bitmapCacheWork.setFileCache(YYIMChat.getInstance().getFileCache());
		// 设置缓存前缀
		bitmapCacheWork.setPrefix(genCachePrefix());
		return bitmapCacheWork;
	}

	/**
	 * 获取缓存前缀
	 * 
	 * @return
	 */
	private String genCachePrefix() {
		return getImageWidth() + "_" + getImageHeight() + "|";
	}

}
