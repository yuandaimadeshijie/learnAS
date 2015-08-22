package com.yonyou.sns.im.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jivesoftware.smack.util.StringUtils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapScreenShotListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.poisearch.PoiSearch.Query;
import com.amap.api.services.poisearch.PoiSearch.SearchBound;
import com.yonyou.sns.im.R;
import com.yonyou.sns.im.ui.component.topbar.MultyLocationSendTopBtnFunc;
import com.yonyou.sns.im.util.common.FileUtils;
import com.yonyou.sns.im.util.common.ToastUtil;
import com.yonyou.sns.im.util.common.YMStorageUtil;

/**
 * 位置地图
 * @author wudl
 * @date 2014年11月20日
 * @version V1.0
 */
public class MultyLocationActivity extends SimpleTopbarActivity implements LocationSource, AMapLocationListener,
		OnMapScreenShotListener, OnPoiSearchListener {

	/** 地图存储位置*/
	public static final String RESULT_FROM_LOCATION_PATH = "RESULT_FROM_LOCATION_PATH";
	/** 地图存储地址*/
	public static final String RESULT_FROM_LOCATION_ADRESS = "RESULT_FROM_LOCATION_ADRESS";
	/** 地图存储经度*/
	public static final String RESULT_FROM_LOCATION_LATITUDE = "RESULT_FROM_LOCATION_LATITUDE";
	/** 地图存储纬度*/
	public static final String RESULT_FROM_LOCATION_LONGITUDE = "RESULT_FROM_LOCATION_LONGITUDE";
	/** 地图视图*/
	private MapView mapView;
	/** 周边兴趣点*/
	private ListView mInterestPlace;
	/** marker 定位位置*/
	private Marker mGPSMarker;
	/** 目标 marker*/
	private Marker mTarMarker;
	/** 地图*/
	private AMap aMap;
	/** 定位监听*/
	private OnLocationChangedListener mListener;
	/** 代理*/
	private LocationManagerProxy mAMapLocationManager;
	/** 纬度*/
	private double latitude;
	/** 经度*/
	private double longitude;
	/** 地理信息*/
	private String address;
	/** 目标经度*/
	private double tarLatitude;
	/** 目标纬度*/
	private double tarLongitude;
	/** 目标地理*/
	private String tarAddress;
	/** 图片存储的位置*/
	private String dirpath;
	/** 图片名字*/
	private String imageName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multy_location);
		init(savedInstanceState);
		// 图片存储路径
		dirpath = YMStorageUtil.getLocationPath(this).getPath();
		imageName = UUID.randomUUID() + ".jpg";
	}

	/**
	 * 初始化
	 */
	private void init(Bundle savedInstanceState) {
		if (aMap == null) {
			// 获取资源
			mapView = (MapView) findViewById(R.id.map);
			mInterestPlace = (ListView) findViewById(R.id.map_search);
			// 此方法必须重写
			mapView.onCreate(savedInstanceState);
			// 获取地图操作对象
			aMap = mapView.getMap();
			// 配置地图
			setUpMap();
			if (!getExtra()) {
				// 显示定位列表
				mInterestPlace.setVisibility(View.VISIBLE);
				// 设置适配器
				InterestPlaceListAdapter adapter = new InterestPlaceListAdapter();
				mInterestPlace.setAdapter(adapter);
			}
		}
	}

	/**
	 * 设置一些amap的属性
	 */
	private void setUpMap() {
		aMap.setLocationSource(this);// 设置定位监听
		aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
		aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
		deactivate();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	/**
	 * 此方法已经废弃
	 */
	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	/**
	 * 定位成功后回调函数
	 */
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (mListener != null && amapLocation != null) {

			if (amapLocation != null && amapLocation.getAMapException().getErrorCode() == 0) {
				// 实现点击回原定位位置
				mListener.onLocationChanged(amapLocation);
				// 清除系统小蓝点
				aMap.clear();
				// 改变地图的缩放级别，高德地图的缩放级别是在4-20 之间。
				aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
				initMarker();
				// 纬度
				latitude = amapLocation.getLatitude();
				// 经度
				longitude = amapLocation.getLongitude();
				// 设置标记点
				if (mGPSMarker != null) {
					// 设置定位点标记
					mGPSMarker.setPosition(new LatLng(latitude, longitude));
					// 设置地图中心点
					if (!getExtra()) {
						aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(latitude, longitude)));
						poiSearch(amapLocation);
					}
				}
				// 判断是网络连接还是gps
				if (amapLocation.getProvider().equals("lbs")) {
					address = amapLocation.getAddress();
				} else if (amapLocation.getProvider().equals("gps")) {

				}
			}
		}
	}

	/**
	 * 初始化地图标记
	 */
	private void initMarker() {
		MarkerOptions markOptions = new MarkerOptions();
		markOptions.title(getResources().getString(R.string.gps_my_location));
		mGPSMarker = aMap.addMarker(markOptions);
		// 设置目标位置
		if (getExtra()) {
			markOptions = new MarkerOptions();
			markOptions.title(tarAddress);
			mTarMarker = aMap.addMarker(markOptions);
			mTarMarker.setPosition(new LatLng(tarLatitude, tarLongitude));
			// 显示infowindow
			mTarMarker.showInfoWindow();
			aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(tarLatitude, tarLongitude)));
		}
	}

	/**
	 * 搜索周边信息
	 * 
	 * @param amapLocation
	 */
	private void poiSearch(AMapLocation amapLocation) {
		// 设置当前显示页
		int currentPage = 0;
		// 第一个参数表示搜索字符串，第二个参数表示POI搜索类型,第三个参数表示POI搜索区域（空字符串代表全国），前俩个参数为空代表查询所有
		Query query = new PoiSearch.Query("", "", amapLocation.getCityCode());
		// 设置每页最多返回多少条poiitem
		query.setPageSize(50);
		// 设置查第一页
		query.setPageNum(currentPage);
		PoiSearch poiSearch = new PoiSearch(this, query);
		// 设置搜索中心点和搜索半径
		poiSearch.setBound(new SearchBound(new LatLonPoint(mGPSMarker.getPosition().latitude,
				mGPSMarker.getPosition().longitude), 1000));
		poiSearch.setOnPoiSearchListener(this);
		// 开启异步搜索
		poiSearch.searchPOIAsyn();
	}

	/**
	 * 激活定位
	 */
	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
			// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
			// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用removeUpdates()方法来取消定位请求
			// 在定位结束后，在合适的生命周期调用destroy()方法
			// 其中如果间隔时间为-1，则定位只定一次
			// 在单次定位情况下，定位无论成功与否，都无需调用removeUpdates()方法移除请求，定位sdk内部会移除
			mAMapLocationManager.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 10, this);
			// 关闭gps定位
			mAMapLocationManager.setGpsEnable(true);
		}
	}

	/**
	 * 停止定位
	 */
	@Override
	public void deactivate() {
		mListener = null;
		if (mAMapLocationManager != null) {
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destroy();
		}
		mAMapLocationManager = null;
	}

	@Override
	protected Class<?>[] getTopbarRightFuncArray() {
		if (getExtra()) {
			return null;
		} else {
			Class<?>[] btnFunc = { MultyLocationSendTopBtnFunc.class };
			return btnFunc;
		}
	}

	/**
	 * 发送地理位置
	 */
	public void sendLocation() {
		if (latitude != 0 && longitude != 0) {
			// 定位信息
			aMap.getMapScreenShot(this);
		}
	}

	/**
	 * 获取数据
	 */
	private boolean getExtra() {
		tarAddress = getIntent().getStringExtra(RESULT_FROM_LOCATION_ADRESS);
		tarLatitude = getIntent().getDoubleExtra(RESULT_FROM_LOCATION_LATITUDE, -1);
		tarLongitude = getIntent().getDoubleExtra(RESULT_FROM_LOCATION_LONGITUDE, -1);
		return (tarLatitude != -1 && tarLongitude != -1);
	}

	@Override
	public void onMapScreenShot(Bitmap bitmap) {
		// 存储到sd卡中
		File file = new File(dirpath, imageName);
		FileUtils.compressBmpToFile(bitmap, file);
		// 返回chat页面
		Intent intent = getIntent();
		intent.putExtra(RESULT_FROM_LOCATION_PATH, file.getPath());
		intent.putExtra(RESULT_FROM_LOCATION_ADRESS, address);
		intent.putExtra(RESULT_FROM_LOCATION_LATITUDE, latitude);
		intent.putExtra(RESULT_FROM_LOCATION_LONGITUDE, longitude);
		// set return code
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	@Override
	public void onPoiSearched(PoiResult result, int rCode) {
		if (rCode == 0) {
			List<PoiItem> items = new ArrayList<PoiItem>();
			// 添加第一个位置信息
			String name = getResources().getString(R.string.gps_my_location);
			PoiItem item = new PoiItem("", new LatLonPoint(latitude, longitude), name, address);
			items.add(item);
			// 搜索POI的结果
			if (result != null && result.getQuery() != null) {
				PoiResult poiResult = result;
				// 取得第一页的poiitem数据，页数从数字0开始
				List<PoiItem> poiItems = poiResult.getPois();
				if (poiItems != null && poiItems.size() > 0) {
					// 添加搜索内容
					items.addAll(poiItems);
				} else {
					ToastUtil.showLong(MultyLocationActivity.this, R.string.gps_no_result);
				}
			} else {
				ToastUtil.showLong(MultyLocationActivity.this, R.string.gps_no_result);
			}
			// 设置数据源
			InterestPlaceListAdapter adapter = (InterestPlaceListAdapter) (mInterestPlace.getAdapter());
			adapter.setList(items);
			adapter.notifyDataSetChanged();
		} else {
			ToastUtil.showLong(MultyLocationActivity.this, R.string.gps_service_busizy);
		}
	}

	@Override
	public void onPoiItemDetailSearched(PoiItemDetail result, int rCode) {

	}

	@Override
	protected Object getTopbarTitle() {
		return R.string.gps_title;
	}

	/***
	 * 兴趣列表适配器
	 * 
	 * @author wudl
	 * 
	 */
	public class InterestPlaceListAdapter extends BaseAdapter {

		/** 数据源 */
		List<PoiItem> list;
		/** 记录当前选中位置 */
		int currentPosition;
		/** 记录是否被选中 */
		List<Boolean> isCheckedList;

		public InterestPlaceListAdapter() {
			this.list = new ArrayList<PoiItem>();
			this.isCheckedList = new ArrayList<Boolean>();
			// 默认选中第一个
			currentPosition = 0;
		}

		public List<PoiItem> getList() {
			return list;
		}

		public void setList(List<PoiItem> list) {
			this.list = list;
			currentPosition = 0;
			// 添加选中状态
			for (int i = 0; i < list.size(); i++) {
				if (currentPosition == i) {
					isCheckedList.add(true);
				} else {
					isCheckedList.add(false);
				}
			}
		}

		public void setCurrentPosition(int currentPosition) {
			this.currentPosition = currentPosition;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(MultyLocationActivity.this).inflate(
						R.layout.location_interest_list_item, null);
			}
			// 位置名称
			TextView mLocationName = (TextView) convertView.findViewById(R.id.location_list_title);
			mLocationName.setText(list.get(position).getTitle());
			// 位置信息
			TextView mLocationInfo = (TextView) convertView.findViewById(R.id.location_list_info);
			mLocationInfo.setText(list.get(position).getSnippet());
			// checkbox
			CheckBox mCheckBox = (CheckBox) convertView.findViewById(R.id.location_list_checkbox);
			mCheckBox.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View paramView) {
					// 点击其他地方就更改数据，否则不变
					if (currentPosition != position) {
						// 更改数据
						LatLonPoint latLonPoint = list.get(position).getLatLonPoint();
						// 改变经纬度和地址
						latitude = latLonPoint.getLatitude();
						longitude = latLonPoint.getLongitude();
						address = list.get(position).getTitle();
						String name = getResources().getString(R.string.gps_my_location);
						if (StringUtils.isEmpty(address) || address.equals(name)) {
							address = list.get(position).getSnippet();
						}
						// 移动标记
						mGPSMarker.setPosition(new LatLng(latitude, longitude));
						aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(latitude, longitude)));
						// 保证只有一个现实选中
						isCheckedList.set(currentPosition, false);
						isCheckedList.set(position, true);
						currentPosition = position;
					}
					((InterestPlaceListAdapter) (mInterestPlace.getAdapter())).notifyDataSetChanged();
				}
			});
			mCheckBox.setChecked(isCheckedList.get(position));
			return convertView;
		}

	}
}
