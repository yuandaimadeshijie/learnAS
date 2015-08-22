package com.yonyou.sns.im.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.adapter.PhotoAibumAdapter;
import com.yonyou.sns.im.entity.album.PhotoAlbum;
import com.yonyou.sns.im.entity.album.PhotoItem;
import com.yonyou.sns.im.ui.component.topbar.AlbumChoiceFunc;
import com.yonyou.sns.im.ui.component.topbar.AlbumPhotoConfirmFunc;
import com.yonyou.sns.im.ui.widget.HorizontalListView;
import com.yonyou.sns.im.util.bitmap.BitmapCacheManager;
import com.yonyou.sns.im.util.common.ToastUtil;
import com.yonyou.sns.im.util.common.YMStorageUtil;

/**
 * 相册Activity
 * 
 * @author yh
 * @modified by litfb
 */
public class AlbumPhotoActivity extends SimpleTopbarActivity implements OnClickListener {

	/** 多选图片时的确定按钮的Topbar功能列表 */
	private static Class<?> TopBarRightFuncArray[] = { AlbumPhotoConfirmFunc.class };

	/** 返回值key-多选 */
	public static final String BUNDLE_RETURN_PHOTOS = "ALBUM_PHOTOS";
	/** 返回值key-单选 */
	public static final String BUNDLE_RETURN_PHOTO = "ALBUM_PHOTO";
	/** camera file path mark*/
	public static final String CAMERA_FILE_PATH = "CAMERA_FILE_PATH";
	/** isOriginal mark*/
	public static final String IS_ORIGINAL = "IS_ORIGINAL";
	/** 拍照*/
	private static final int REQUEST_CAMERA = 0;

	/** 参数 */
	public static final String EXTRA_TYPE = "EXTRA_TYPE";
	/** single selection */
	public static final String TYPE_SINGLE = "TYPE_SINGLE";

	/** 展示相片的网格列表  */
	private GridView gridPhoto;
	/** 相册背景 */
	private View viewBackground;
	/** 相册列表 */
	private ListView listAlbum;
	/** 缩略图列表  */
	private HorizontalListView hlistThumb;
	/** bottom */
	private View viewBottom;

	/** album进入动画 */
	private TranslateAnimation enterAnimation;
	/** album退出动画 */
	private TranslateAnimation exitAnimation;

	/** 相册总列表  */
	private List<PhotoAlbum> aibumList;
	/** 当前类别的相册列表  */
	private PhotoAlbum aibum;
	/** 相片显示的适配器  */
	private PhotoAdappter photoAapter;
	/** 相册显示的适配器  */
	private PhotoAibumAdapter albumAdapter;
	/** 缩略图显示的适配器 */
	private ThumbAdapter thumbAdapter;

	/** 缓存 */
	private BitmapCacheManager bitmapCacheManager;

	/** 已选图片 */
	private ArrayList<PhotoItem> selPhotoList = new ArrayList<PhotoItem>();

	/** 查询字段 */
	private static final String[] IMAGE_PROJECTION = { MediaStore.Images.ImageColumns._ID, // 图片id
			MediaStore.Images.ImageColumns.DISPLAY_NAME, // 图片显示名称
			MediaStore.Images.ImageColumns.DATA, // 图片path
			MediaStore.Images.ImageColumns.ORIENTATION, // 图片旋转
			MediaStore.Images.ImageColumns.BUCKET_ID, // dir id 目录
			MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, // 目录显示名称
			MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC, // 缩略图id
			MediaStore.Images.ImageColumns.SIZE };

	/** where条件 */
	private static final String IMAGE_SELECTION = ImageColumns.SIZE + ">?";
	/** 图片大小限制 */
	private static final int IMAGE_SIZE_LIMIT = 10240;

	/** 拍照的路径*/
	private String cameraFilePath = "";

	/**
	 * 是否单选
	 * 
	 * @return
	 */
	private boolean isSingleSelection() {
		return TYPE_SINGLE.equals(getIntent().getStringExtra(EXTRA_TYPE));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_albumphoto);

		gridPhoto = (GridView) findViewById(R.id.album_photo_gridview);
		viewBackground = findViewById(R.id.album_list_background);
		listAlbum = (ListView) findViewById(R.id.album_list_view);
		hlistThumb = (HorizontalListView) findViewById(R.id.album_thumb_list);
		viewBottom = findViewById(R.id.album_photo_bottom_view);

		// 按钮点击监听
		viewBackground.setOnClickListener(this);

		// 先查询所有的图片，并分类
		aibumList = getPhotoAlbumList();
		// 默认设置的是所有图片
		aibum = aibumList.get(0);
		aibum.setCurrentChoice(true);
		// 设标题
		refreshFuncView();
		// 图片Adapter
		photoAapter = new PhotoAdappter(this, aibum.getPhotoList());
		gridPhoto.setAdapter(photoAapter);

		// 相册adapter
		albumAdapter = new PhotoAibumAdapter(aibumList, this);
		listAlbum.setAdapter(albumAdapter);
		// 相册选择监听
		listAlbum.setOnItemClickListener(new AlbumOnItemClickListener());

		// 单选
		if (isSingleSelection()) {
			// 不需要bottom
			viewBottom.setVisibility(View.GONE);
		} else {
			// thumb list
			thumbAdapter = new ThumbAdapter(this);
			hlistThumb.setAdapter(thumbAdapter);
			hlistThumb.setOnItemClickListener(new ThumbOnItemClickListener());
		}

		// 缓存管理器
		bitmapCacheManager = new BitmapCacheManager(this, false, BitmapCacheManager.ROUND_CORNER_BITMAP,
				BitmapCacheManager.BITMAP_DPSIZE_80);
		bitmapCacheManager.setDefaultImage(R.drawable.album_photo_default);
		bitmapCacheManager.generateBitmapCacheWork();
	}

	/**
	 * 获取相册列表
	 * 
	 * @return
	 */
	private List<PhotoAlbum> getPhotoAlbumList() {
		// 查询sd卡中所有的图片
		Cursor cursor = MediaStore.Images.Media.query(getContentResolver(),
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, IMAGE_SELECTION,
				new String[] { String.valueOf(IMAGE_SIZE_LIMIT) }, MediaStore.Images.Media.DATE_TAKEN + " DESC");

		// 所有图片
		PhotoAlbum total = new PhotoAlbum();
		// 名称
		total.setName(getString(R.string.album_all_photo));
		// 封面
		total.setBitmap(R.drawable.album_photo_default);
		// 所有图片标记
		total.setTotal(true);

		// 所有相册
		Map<String, PhotoAlbum> albumMap = new LinkedHashMap<String, PhotoAlbum>();
		// 遍历图片
		while (cursor.moveToNext()) {
			// id
			String photoId = cursor.getString(0);
			// 路径
			String photoPath = cursor.getString(2);
			// 目录id
			String dirId = cursor.getString(4);
			// 目录名
			String dirName = cursor.getString(5);

			// 生成PhotoItem
			PhotoItem item = new PhotoItem(Integer.valueOf(photoId), photoPath);
			// 添加到total
			total.addPhotoItem(item);

			// 取相册对象
			PhotoAlbum photoAlbum = albumMap.get(dirId);
			if (photoAlbum == null) {
				photoAlbum = new PhotoAlbum(dirName, Integer.parseInt(photoId));
				albumMap.put(dirId, photoAlbum);
			}
			// 相册里添加照片
			photoAlbum.addPhotoItem(item);
		}
		// 关闭cursor
		cursor.close();

		// 相册列表
		List<PhotoAlbum> aibumList = new ArrayList<PhotoAlbum>();
		aibumList.add(total);
		aibumList.addAll(albumMap.values());
		return aibumList;
	}

	/**
	 * 照片Adapter
	 *
	 * @author litfb
	 * @date 2014年10月13日
	 * @version 1.0
	 */
	class PhotoAdappter extends BaseAdapter {

		/** context */
		private Context context;
		/** 照片列表 */
		private List<PhotoItem> photoList;

		public PhotoAdappter(Context context, List<PhotoItem> photoList) {
			this.context = context;
			this.photoList = photoList;
		}

		@Override
		public int getCount() {
			return photoList.size();
		}

		@Override
		public PhotoItem getItem(int position) {
			return photoList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// view holder
			final PhotoViewHolder holder;
			int itemWidth;
			if (convertView == null) {
				// inflate view
				convertView = new RelativeLayout(context);
				// 计算width
				itemWidth = (int) (getResources().getDisplayMetrics().widthPixels - 4 * getResources()
						.getDimensionPixelSize(R.dimen.album_photo_spacing)) / 3;
				// width = height
				AbsListView.LayoutParams param = new AbsListView.LayoutParams(itemWidth, itemWidth);
				convertView.setLayoutParams(param);
				// inflate
				LayoutInflater.from(context).inflate(R.layout.view_album_grid_item, (ViewGroup) convertView);
				// new holder
				holder = new PhotoViewHolder();
				holder.imageView = (ImageView) convertView.findViewById(R.id.album_griditem_photo);
				holder.bgView = (View) convertView.findViewById(R.id.album_griditem_background);
				holder.select = convertView.findViewById(R.id.album_griditem_select);
				holder.check = (CheckBox) convertView.findViewById(R.id.album_griditem_checkbox);
				// set holder to tag
				convertView.setTag(R.string.viewholder, holder);
			} else {
				// get holder from tag
				holder = (PhotoViewHolder) convertView.getTag(R.string.viewholder);
				itemWidth = convertView.getMeasuredHeight();
			}
			// 存position到holder
			holder.position = position;
			// 单选
			if (isSingleSelection()) {
				initSingleSelectedView(holder, position);
			} else {
				initMultiSelectedView(holder, position);
			}
			return convertView;
		}

		/**
		 * 单选是初始化View
		 * @param holder
		 */
		private void initSingleSelectedView(final PhotoViewHolder holder, int position) {
			// load图片
			bitmapCacheManager.loadFormCache(photoList.get(position).getPhotoPath(), holder.imageView);
			holder.select.setVisibility(View.GONE);
			holder.imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = getIntent();
					// set return value
					PhotoItem item = photoList.get(holder.position);
					intent.putExtra(BUNDLE_RETURN_PHOTO, item);
					// set return code
					setResult(Activity.RESULT_OK, intent);
					finish();
				}
			});
		}

		/**
		 * 初始化多选视图
		 * @param holder
		 * @param position
		 */
		private void initMultiSelectedView(final PhotoViewHolder holder, final int position) {
			if (holder.position == 0) {
				// 如果是第一个放置拍照icon
				holder.imageView.setImageResource(R.drawable.photo_camera);
				holder.imageView.setBackgroundResource(R.drawable.chat_camera_bg);
				holder.imageView.setScaleType(ScaleType.CENTER_INSIDE);
				holder.imageView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View paramView) {
						// 调用系统相机
						Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						File file = new File(YMStorageUtil.getImagePath(AlbumPhotoActivity.this), UUID.randomUUID()
								.toString() + ".jpg");
						cameraFilePath = file.getPath();
						Uri mOutPutFileUri = Uri.fromFile(file);
						cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mOutPutFileUri);
						cameraIntent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
						startActivityForResult(cameraIntent, REQUEST_CAMERA);
					}
				});
				holder.select.setVisibility(View.INVISIBLE);
			} else {
				holder.imageView.setScaleType(ScaleType.CENTER_CROP);
				holder.imageView.setBackgroundColor(getResources().getColor(R.color.transparent));
				// load图片
				bitmapCacheManager.loadFormCache(photoList.get(position).getPhotoPath(), holder.imageView);
				holder.select.setVisibility(View.VISIBLE);
				holder.imageView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View paramView) {
						// 路径
						String path = aibum.getPhotoItem(position).getPhotoPath();
						// 图片查看
						Intent intent = new Intent(AlbumPhotoActivity.this, ZoomImageActivity.class);
						intent.putExtra("path", path);
						startActivity(intent);
					}
				});
				// 监听
				holder.select.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View paramView) {
						boolean isChecked = !photoList.get(position).isSelect();
						// cur position
						int curPosition = holder.position;
						PhotoItem photoItem = photoList.get(curPosition);
						// 选中
						if (isChecked) {
							if (selPhotoList.size() >= 9) {
								// 提示
								ToastUtil.showLong(context, context.getString(R.string.album_max_toast, 9));
								holder.check.setChecked(photoItem.isSelect());
								return;
							} else {
								photoItem.setSelect(isChecked);
								if (!selPhotoList.contains(photoItem)) {
									selPhotoList.add(photoItem);
								}
							}
						} else {
							photoItem.setSelect(isChecked);
							selPhotoList.remove(photoItem);
						}
						if (photoItem.isSelect()) {
							holder.bgView.setVisibility(View.VISIBLE);
						} else {
							holder.bgView.setVisibility(View.INVISIBLE);
						}
						// 重设确认按钮文本
						refreshFuncView();
						// 通知grid变更
						photoAapter.notifyDataSetChanged();
						thumbAdapter.notifyDataSetChanged();
					}
				});
				holder.check.setChecked(photoList.get(position).isSelect());
			}
		}

		class PhotoViewHolder {

			/** position */
			int position;
			/** 图片 */
			ImageView imageView;
			/** 半透明背景 */
			View bgView;
			/** 复选框 */
			View select;
			/** checkbox*/
			CheckBox check;

		}
	}

	class ThumbAdapter extends BaseAdapter {

		/** context */
		private Context context;

		public ThumbAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return selPhotoList.size();
		}

		@Override
		public PhotoItem getItem(int position) {
			return selPhotoList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				// inflate view
				convertView = new RelativeLayout(context);
				// width = height
				AbsListView.LayoutParams param = new AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.MATCH_PARENT);
				// inflate
				LayoutInflater.from(context).inflate(R.layout.view_album_thumb_item, (ViewGroup) convertView);
				convertView.setLayoutParams(param);
				// new holder
				holder = new ViewHolder();
				holder.imageView = (ImageView) convertView.findViewById(R.id.album_thumbview_image);
				// set holder to tag
				convertView.setTag(R.string.viewholder, holder);
			} else {
				// get holder from tag
				holder = (ViewHolder) convertView.getTag(R.string.viewholder);
			}

			// set photo in holder
			holder.photoItem = selPhotoList.get(position);
			// load图片
			bitmapCacheManager.loadFormCache(selPhotoList.get(position).getPhotoPath(), holder.imageView);
			return convertView;
		}

		class ViewHolder {

			PhotoItem photoItem;

			ImageView imageView;

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

	/**
	 * 展示相册
	 */
	private void showAlbumList() {
		gridPhoto.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		listAlbum.startAnimation(getEnterAnimation());
		viewBackground.setVisibility(View.VISIBLE);
		listAlbum.setVisibility(View.VISIBLE);
	}

	/**
	 * 隐藏相册
	 */
	private void hideAlbumList() {
		gridPhoto.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
		listAlbum.startAnimation(getExitAnimation());
		viewBackground.setVisibility(View.GONE);
		listAlbum.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);

		if (v.getId() == R.id.album_list_background) {
			// 隐藏事件
			hideAlbumList();
		}
	}

	public void changeAlbum() {
		// 选择相册
		if (listAlbum.getVisibility() == View.VISIBLE) {
			hideAlbumList();
		} else {
			showAlbumList();
		}
	}

	public void onConfirm() {
		Intent intent = getIntent();
		Bundle bundle = new Bundle();
		// set return value
		bundle.putSerializable(BUNDLE_RETURN_PHOTOS, selPhotoList);
		Log.d("info", selPhotoList.toString());
		intent.putExtras(bundle);
		// set return code
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	/**
	 * 相册点击监听
	 *
	 * @author litfb
	 * @date 2014年10月13日
	 * @version 1.0
	 */
	private class AlbumOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// 隐藏列表
			hideAlbumList();
			// 旧相册取消选中
			aibum.setCurrentChoice(false);
			// 设置新的选中相册
			aibum = aibumList.get(position);
			// 设置选中
			aibum.setCurrentChoice(true);
			// 设标题
			refreshFuncView();
			// 重设Adapter
			photoAapter = new PhotoAdappter(AlbumPhotoActivity.this, aibum.getPhotoList());
			gridPhoto.setAdapter(photoAapter);
			// 通知变更
			albumAdapter.notifyDataSetChanged();
		}

	};

	/**
	 * 缩略图点击监听
	 *
	 * @author litfb
	 * @date 2014年10月15日
	 * @version 1.0
	 */
	private class ThumbOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// 取消选中
			selPhotoList.get(position).setSelect(false);
			selPhotoList.remove(position);
			// 重设确认按钮文本
			refreshFuncView();
			// 通知变更
			photoAapter.notifyDataSetChanged();
			thumbAdapter.notifyDataSetChanged();
		}

	}

	public PhotoAlbum getAlbum() {
		return aibum;
	}

	public int getSelectPhotoCount() {
		return selPhotoList.size();
	}

	@Override
	protected Class<?>[] getTopbarRightFuncArray() {
		// 如果是单选，右上角不需要功能按钮，如果是多选需要发送按钮
		return isSingleSelection() ? null : TopBarRightFuncArray;
	}

	@Override
	protected Class<?> getTopbarMiddleFunc() {
		return AlbumChoiceFunc.class;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_CAMERA:
			if (resultCode == RESULT_OK) {
				Intent cameraIntent = new Intent(AlbumPhotoActivity.this, CameraEditActivity.class);
				cameraIntent.putExtra(CameraEditActivity.EXTRA_FILE_PATH, cameraFilePath);
				startActivityForResult(cameraIntent, CameraEditActivity.REQUEST_CAMERA_OPTION);
			}
			break;
		case CameraEditActivity.REQUEST_CAMERA_OPTION:
			if (data != null) {
				boolean isOriginal = data.getBooleanExtra(CameraEditActivity.OPTION_RESULT, false);
				Intent intent = getIntent();
				intent.putExtra(CAMERA_FILE_PATH, cameraFilePath);
				intent.putExtra(IS_ORIGINAL, isOriginal);
				// set return code
				setResult(CameraEditActivity.REQUEST_CAMERA_OPTION, intent);
				finish();
			}
			break;
		default:
			break;
		}
	}

}
