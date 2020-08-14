package com.shmedo.mcloudapp.maps.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.AMapGestureListener;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.maps.helper.GaoDeCaculateDistanceHelper;
import com.shmedo.mcloudapp.maps.helper.GaoDePoiProcessHelper;
import com.shmedo.mcloudapp.maps.helper.SensorEventHelper;
import com.shmedo.mcloudapp.maps.model.DrawerLayoutType;
import com.shmedo.mcloudapp.maps.model.MapLayerInfo;
import com.shmedo.mcloudapp.maps.model.MapMode;
import com.shmedo.mcloudapp.maps.model.PoiSearchType;
import com.shmedo.mcloudapp.maps.util.AMapLocationUtil;
import com.shmedo.mcloudapp.maps.util.CoordinateFormatUtils;
import com.shmedo.mcloudapp.maps.view.DistanceToolbarView;
import com.shmedo.mcloudapp.maps.view.GPSView;
import com.shmedo.mcloudapp.maps.view.LocationTitleView;
import com.shmedo.mcloudapp.maps.view.MapLayerDrawerView;
import com.shmedo.mcloudapp.maps.view.MapSearchView;
import com.shmedo.mcloudapp.maps.view.PoiDetailBottomView;
import com.shmedo.mcloudapp.maps.view.RouteView;
import com.shmedo.mcloudapp.maps.view.SupendPartitionView;
import com.shmedo.mcloudapp.maps.view.ToolBoxDrawerView;
import com.shmedo.mcloudapp.maps.view.ZoomView;
import com.shmedo.mcloudapp.util.permission.PermissionHelper;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class MapActivity extends CheckMapNeedPermissionsActivity implements AMapGestureListener, AMapLocationListener, LocationSource, MapSearchView.OnMapHeadSearchViewClickListener, MapLayerDrawerView.OnMapLayerItemClickListener, ToolBoxDrawerView.OnMapToolItemClickListener, ZoomView.OnZoomViewClickListener {
    private static final int REQUEST_CODE_POI_SEARCH = 0x1000;
    private static final int REQUEST_CODE_LATLNG_SEARCH = 0x1001;

    @BindView(R.id.map)
    public TextureMapView mMapView;

    @BindView(R.id.id_drawer_layout)
    public DrawerLayout mDrawerLayout;

    @BindView(R.id.toolbox_drawer_view)
    public ToolBoxDrawerView mToolBoxDrawerView;

    @BindView(R.id.map_layer_drawer_view)
    public MapLayerDrawerView mMapLayerDrawerView;

    @BindView(R.id.distance_toolbar_view)
    public DistanceToolbarView mDistanceToolbarView;

    @BindView(R.id.location_title_view)
    public LocationTitleView mLocationTitleView;

    @BindView(R.id.map_search_view)
    public MapSearchView mMapSearchView;

    @BindView(R.id.supend_partition_view)
    public SupendPartitionView mSupendPartitionView;

    @BindView(R.id.zoom_view)
    public ZoomView mZoomView;

    @BindView(R.id.gps_route_container)
    public View mGspContainer;

    @BindView(R.id.gps_view)
    public GPSView mGpsView;

    @BindView(R.id.route_view)
    public RouteView mRouteView;

    @BindView(R.id.poi_detail_bottom_view)
    public PoiDetailBottomView mPoiDetailBottomView;

    private AMap aMap; //地图控制器对象
    private MyLocationStyle mLocationStyle;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private AMapLocation mAmapLocation;
    private OnLocationChangedListener mOnLocationChangedListener;

    public static final int STATE_UNLOCKED = 0;//未定位状态，默认状态
    public static final int STATE_LOCKED = 1;//定位状态
    public static final int STATE_ROTATE = 2;//根据地图方向旋转状态
    public int mCurrentGpsState = STATE_UNLOCKED;//当前定位状态
    public float mZoomLevel = 16;//地图的缩放级别一共分为 17 级，从 3 到 19。数字越大，展示的图面信息越精细。
    private long mAnimDuartion = 500L;//地图动效时长
    public int mMapType = MyLocationStyle.LOCATION_TYPE_LOCATE;//地图状态类型
    public SensorEventHelper mSensorHelper;
    private Marker mLocationMarker;//自定义小蓝点
    private Circle mCircle;//定位蓝点精度圆圈
    public MapMode mMapMode = MapMode.NORMAL;
    public boolean isFirstLocation = true;//是否是第一次定位
    private boolean isOnScrolling;//正在滑动地图
    public boolean isPoiClick; // 当前是否正在处理POI点击
    private float mAccuracy;//定位精度
    private String mCityName;//定位所在城市
    public String mPoiName;//POI的名称
    public LatLng myLatLng;//当前定位经纬度
    public LatLng poiLatLng;//当前点击的poi经纬度
    public PoiItem sharePoi;//分享点位信息


    private GaoDeCaculateDistanceHelper gaoDeCaculateDistanceHelper;
    private GaoDePoiProcessHelper gaoDePoiProcessHelper;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, MapActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_map;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(savedInstanceState);
        setListener();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
        if (Build.VERSION.SDK_INT >= 23) {
            if (isNeedCheck) {
                Timber.d("checkPermissionForGPS call");
                checkPermissionForGPS(PermissionHelper.REQUEST_CODE_LOCATION);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
        deactivate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        isFirstLocation = true;
        if (mLocationClient != null) {
            mLocationClient.onDestroy();
        }
        if (mLocationMarker != null) {
            mLocationMarker.destroy();
            mLocationMarker = null;
        }
        if (mSensorHelper != null) {
            mSensorHelper.unRegisterSensorListener();
            mSensorHelper.setCurrentMarker(null);
            mSensorHelper = null;
        }
    }

    private void initView(Bundle savedInstanceState) {
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        mGpsView.setGpsState(mCurrentGpsState);
        if (aMap == null) {
            //初始化地图控制器对象
            aMap = mMapView.getMap();
            setUpMap();
        }
    }

    private void setUpMap() {
        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);//卫星地图模式
        aMap.getUiSettings().setZoomControlsEnabled(false); //隐藏缩放控件
        aMap.getUiSettings().setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_RIGHT);//设置logo位置
        setLocationStyle();
        // 设置定位监听
        aMap.setLocationSource(this);
        // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.setMyLocationEnabled(true);
    }

    /**
     * 设置 定位小蓝点（当前位置）的绘制样式
     */
    private void setLocationStyle() {
        // 自定义系统定位蓝点
        if (mLocationStyle == null) {
            mLocationStyle = new MyLocationStyle();
        }
        // 将自定义的 myLocationStyle 对象添加到地图上
        aMap.setMyLocationStyle(mLocationStyle.myLocationType(mMapType));
    }

    private void setListener() {
        //地图手势事件
        aMap.setAMapGestureListener(this);
        mSensorHelper = new SensorEventHelper(this);
        mSensorHelper.registerSensorListener();
        mMapSearchView.setOnMapHeaderViewClickListener(this);
        mMapLayerDrawerView.setOnMapLayerItemClickListener(this);
        mToolBoxDrawerView.setOnMapToolItemClickListener(this);
        mZoomView.setOnZoomViewClickListener(this);

        gaoDeCaculateDistanceHelper = new GaoDeCaculateDistanceHelper(this);
        gaoDePoiProcessHelper = new GaoDePoiProcessHelper(this);
    }

    @Override
    protected void doOnPermissionGranted(int requestCode) {
        if (requestCode == PermissionHelper.REQUEST_CODE_LOCATION) {
            aMap.setMyLocationEnabled(true);

        } else if (requestCode == PermissionHelper.REQUEST_CODE_GPS_LOCATION) {
            processGpsViewClick();

        } else if (requestCode == PermissionHelper.REQUEST_CODE_NAVI || requestCode == PermissionHelper.REQUEST_CODE_ROUTE) {
            gaoDePoiProcessHelper.doOnPermissionGranted(requestCode);
        }
    }

    private void processGpsViewClick() {
        CameraUpdate cameraUpdate = null;
        isPoiClick = false;
        //修改定位图标状态
        switch (mCurrentGpsState) {
            case STATE_LOCKED:
                mZoomLevel = 18;
                mCurrentGpsState = STATE_ROTATE;
                //连续定位、且将视角移动到地图中心点，地图依照设备方向旋转，定位点会跟随设备移动。
                mMapType = MyLocationStyle.LOCATION_TYPE_MAP_ROTATE_NO_CENTER;
                cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(myLatLng, mZoomLevel, 30, 0));
                break;

            case STATE_UNLOCKED:
            case STATE_ROTATE:
                mZoomLevel = 16;
                mCurrentGpsState = STATE_LOCKED;
                //连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
                mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
                cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(myLatLng, mZoomLevel, 0, 0));
                break;
        }

        if (mMapMode != MapMode.CACULATE_DISTANCE) {
            gaoDePoiProcessHelper.destroyPoiMarker();
            //显示底部POI详情
            gaoDePoiProcessHelper.showPoiDetailBottomView("我的位置", String.format("在%s附近", mPoiName));
            sharePoi = new PoiItem(null, new LatLonPoint(myLatLng.latitude, myLatLng.longitude), mPoiName, mPoiName);
        }

        aMap.setMyLocationEnabled(true);
        //改变定位图标状态
        mGpsView.setGpsState(mCurrentGpsState);
        //执行地图动效
        aMap.animateCamera(cameraUpdate, mAnimDuartion, null);
        setLocationStyle();
        resetLocationMarker();
    }

    @OnClick({R.id.gps_view, R.id.route_view, R.id.mapLayerView, R.id.mapToolView})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gps_view://Gps 定位
                checkPermissionForGPS(PermissionHelper.REQUEST_CODE_GPS_LOCATION);
                break;

            case R.id.route_view://路线
                poiLatLng = null;
                mPoiName = "";
                checkPermissionForGPS(PermissionHelper.REQUEST_CODE_ROUTE);
                break;

            case R.id.mapLayerView://退出/关闭图层抽屉
                switchDrawerLayoutView(DrawerLayoutType.DRAWER_MAP_LAYER);
                break;

            case R.id.mapToolView://退出/关闭工具箱抽屉
                switchDrawerLayoutView(DrawerLayoutType.DRAWER_TOOL_BOX);
                break;
        }
    }

    /**
     * 切换展开抽屉时显示的视图
     *
     * @param drawerLayoutType
     */
    private void switchDrawerLayoutView(DrawerLayoutType drawerLayoutType) {
        if (drawerLayoutType == DrawerLayoutType.DRAWER_MAP_LAYER) {
            mToolBoxDrawerView.setVisibility(View.GONE);
            mMapLayerDrawerView.setVisibility(View.VISIBLE);
        } else {
            mToolBoxDrawerView.setVisibility(View.VISIBLE);
            mMapLayerDrawerView.setVisibility(View.GONE);
        }

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onLocationChanged(final AMapLocation aMapLocation) {
        if (null == mOnLocationChangedListener || null == aMapLocation || aMapLocation.getErrorCode() != 0) {
            if (aMapLocation != null) {
                Timber.d("定位失败：errorCode=" + aMapLocation.getErrorCode() + ",errorMsg=" + aMapLocation.getErrorInfo());
            }
            return;
        }
        Timber.d("定位成功，onLocationChanged： Longitude=" + aMapLocation.getLongitude() + ",Latitude=" + aMapLocation.getLatitude() + ",poiName=" + aMapLocation.getPoiName());

        mAmapLocation = aMapLocation;
        //获取经纬度
        double lng = aMapLocation.getLongitude();
        double lat = aMapLocation.getLatitude();
        String coordinate = CoordinateFormatUtils.DDtoDMS(lng) + "E," + CoordinateFormatUtils.DDtoDMS(lat) + "N";
        mLocationTitleView.post(new Runnable() {
            @Override
            public void run() {
                mLocationTitleView.updataView(coordinate, aMapLocation.getAltitude(), aMapLocation.getAccuracy(), aMapLocation.getGpsAccuracyStatus());
            }
        });

        if (isOnScrolling) {
            Timber.e("MapView is Scrolling by user,can not operate...");
            return;
        }
        // 当前poiName和上次不相等才更新显示
        if (aMapLocation.getPoiName() != null && !aMapLocation.getPoiName().equals(mPoiName)) {
            if (!isPoiClick) {
                // 点击poi时,定位位置和点击位置不一定一样
                mPoiName = aMapLocation.getPoiName();
                mPoiDetailBottomView.tvPoiDistance.setText(String.format("在%s附近", mPoiName));
            }
        }

        //参数依次是：视角调整区域的中心点坐标、希望调整到的缩放级别、俯仰角0°~45°（垂直与地图时为0）、偏航角 0~360° (正北方为0)
        myLatLng = new LatLng(lat, lng);
        if (!aMapLocation.getCity().equals(mCityName)) {
            mCityName = aMapLocation.getCity();
        }

        //首次定位,选择移动到地图中心点并修改级别到15级
        //首次定位成功才修改地图中心点，并移动
        mAccuracy = aMapLocation.getAccuracy();
        Timber.d("accuracy=" + mAccuracy + ",isFirstLocation=" + isFirstLocation);
        if (isFirstLocation) {
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, mZoomLevel), new AMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    mCurrentGpsState = STATE_LOCKED;
                    mGpsView.setGpsState(mCurrentGpsState);
                    mMapType = MyLocationStyle.LOCATION_TYPE_LOCATE;
                    addLocationLockedMarker(myLatLng);//添加定位图标
                    addCircle();//添加定位精度圆
                    if (null != mLocationMarker) {
                        mSensorHelper.setCurrentMarker(mLocationMarker);//定位图标旋转
                    }
                    isFirstLocation = false;
                }

                @Override
                public void onCancel() {

                }
            });
        } else {
            mCircle.setCenter(myLatLng);
            mCircle.setRadius(mAccuracy);
            mLocationMarker.setPosition(myLatLng);
//            if (isCanMoveToCenter) {
//                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, mZoomLevel));
//            }
        }
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mOnLocationChangedListener = listener;
        //设置定位回调监听
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            mLocationOption = AMapLocationUtil.getInstance().getAMapLocationClientOption();
            mLocationClient.setLocationOption(mLocationOption);
            //设置定位监听
            mLocationClient.setLocationListener(this);
            mLocationClient.startLocation();//启动定位
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mOnLocationChangedListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }


    /**
     * 地图手势事件回调：单指双击
     */
    @Override
    public void onDoubleTap(float v, float v1) {
        mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
        mCurrentGpsState = STATE_UNLOCKED;
        mGpsView.setGpsState(mCurrentGpsState);
        setLocationStyle();
        resetLocationMarker();
    }

    @Override
    public void onSingleTap(float v, float v1) {

    }

    @Override
    public void onFling(float v, float v1) {

    }

    /**
     * 地图手势事件回调：单指滑动
     */
    @Override
    public void onScroll(float v, float v1) {
        //避免重复调用闪屏，当手指up才重置为false
        if (!isOnScrolling) {
            Timber.d("onScroll,x=" + v + ",y=" + v1);
            isOnScrolling = true;
            //旋转不移动到中心点
            mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
            mCurrentGpsState = STATE_UNLOCKED;
            //当前没有正在定位才能修改状态
            if (!isFirstLocation) {
                mGpsView.setGpsState(mCurrentGpsState);
            }
            setLocationStyle();
            resetLocationMarker();
        }
    }

    /**
     * 长按
     */
    @Override
    public void onLongPress(float v, float v1) {

    }

    /**
     * 地图手势事件回调：单指按下
     */
    @Override
    public void onDown(float v, float v1) {

    }

    /**
     * 地图手势事件回调：单指抬起
     */
    @Override
    public void onUp(float v, float v1) {
        isOnScrolling = false;
    }

    /**
     * 地图手势事件回调：地图稳定下来会回到此接口
     */
    @Override
    public void onMapStable() {

    }

    /**
     * 图层列表点击事件
     */
    @Override
    public void onMapLayerItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, MapLayerInfo mapLayerInfo) {
        if (mapLayerInfo == null) {
            Timber.e("切换图层错误: MapLayerInfo is Null");
            return;
        }

        switch (mapLayerInfo.getMapType()) {
            case GAODE_NORMAL:
                aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 设置白昼地图（即普通地图)，aMap是地图控制器对象。
                break;

            case GAODE_SATELLITE:
                aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// 设置卫星地图模式，aMap是地图控制器对象。
                break;

            default:
                break;
        }
    }

    /**
     * 网络测速按钮点击事件
     */
    @Override
    public void onTestSpeedClick() {
        SpeedTestActivity.startActivity(this);
    }

    /**
     * 测量距离按钮点击事件
     */
    @Override
    public void onMeasureDistanceClick() {
        mMapMode = MapMode.CACULATE_DISTANCE;
        mDrawerLayout.closeDrawer(GravityCompat.START);
        setDistanceToolbarViewVisibility(true);
    }

    /**
     * 测量面积按钮点击事件
     */
    @Override
    public void onCalculateAreaClick() {

    }

    /**
     * 指南针按钮点击事件
     */
    @Override
    public void onCompassClick() {

    }

    public void setDistanceToolbarViewVisibility(boolean isShow) {
        mDistanceToolbarView.setVisibility(isShow ? View.VISIBLE : View.GONE);
        mLocationTitleView.setVisibility(!isShow ? View.VISIBLE : View.GONE);
        mMapSearchView.setVisibility(!isShow ? View.VISIBLE : View.GONE);
        mSupendPartitionView.setVisibility(!isShow ? View.VISIBLE : View.GONE);
        mRouteView.setVisibility(!isShow ? View.VISIBLE : View.GONE);
        mPoiDetailBottomView.setVisibility(View.GONE);
        if (isShow) {
            gaoDePoiProcessHelper.destroyPoiMarker();
            gaoDePoiProcessHelper.resetGpsButtonPosition();
        }

        aMap.setAMapGestureListener(!isShow ? this : null);
        gaoDePoiProcessHelper.setOnPOIClickListener(!isShow);
    }

    /**
     * 点击退出地图页面
     */
    @Override
    public void onLeaveMapClick() {
        finish();
    }

    /**
     * 点击常规搜索点位
     */
    @Override
    public void onSearchNormalClick() {
        mMapSearchView.setSearchType(PoiSearchType.NORMAL_SEARCH);
        SearchPoiActivity.startActivityForResult(this, PoiSearchType.NORMAL_SEARCH, mCityName, "", REQUEST_CODE_POI_SEARCH);
    }

    /**
     * 点击按经纬度搜索点位
     */
    @Override
    public void onSearchLatLongClick() {
        mMapSearchView.setSearchType(PoiSearchType.LATLNG_SEARCH);
        SearchPoiActivity.startActivityForResult(this, PoiSearchType.LATLNG_SEARCH, mCityName, "", REQUEST_CODE_LATLNG_SEARCH);
    }

    /**
     * 点击返回 poi 搜索列表页面
     */
    @Override
    public void onGoBackSearchListClick() {
        String poiTitle = mMapSearchView.getPoiInputText();
        SearchPoiActivity.startActivityForResult(this, PoiSearchType.NORMAL_SEARCH, mCityName, poiTitle, REQUEST_CODE_POI_SEARCH);
    }


    /**
     * 点击 恢复到显示正常搜索框
     */
    @Override
    public void onCancelPoiSearchClick() {
        mMapSearchView.setSearchMode(MapSearchView.SEARCH_WITH_EMPTY);
        gaoDePoiProcessHelper.onPoiCloseClick();
    }

    /**
     * 点击放大
     */
    @Override
    public void onZoomInClick() {
        mZoomLevel = aMap.getCameraPosition().zoom + 1;
        if (mZoomLevel > 19) {
            ToastUtils.show("已放大至最高级别");
            return;
        }
        aMap.moveCamera(CameraUpdateFactory.zoomTo(mZoomLevel));
    }

    /**
     * 点击缩小
     */
    @Override
    public void onZoomOutClick() {
        mZoomLevel = aMap.getCameraPosition().zoom - 1;
        if (mZoomLevel < 3) {
            ToastUtils.show("已缩小至最低级别");
            return;
        }
        aMap.moveCamera(CameraUpdateFactory.zoomTo(mZoomLevel));
    }

    /**
     * 根据当前地图状态重置定位蓝点
     */
    private void resetLocationMarker() {
        if (mGpsView.getGpsState() == GPSView.STATE_ROTATE) {
            //ROTATE模式不需要方向传感器
            //mSensorHelper.unRegisterSensorListener();
            addLocationRotateMarker(myLatLng);
        } else {
            //mSensorHelper.registerSensorListener();
            addLocationLockedMarker(myLatLng);
            if (null != mLocationMarker) {
                mSensorHelper.setCurrentMarker(mLocationMarker);
            }
        }

        addCircle();
    }


    /**
     * 添加锁定定位小蓝点
     *
     * @param latlng
     */
    private void addLocationLockedMarker(LatLng latlng) {
        if (mLocationMarker != null) {
            mLocationMarker.destroy();
            mLocationMarker = null;
        }
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(),
                R.drawable.icon_map_gps_locked));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(bitmapDescriptor);
        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.position(latlng);
        mLocationMarker = aMap.addMarker(markerOptions);
        mLocationMarker.setClickable(false);
    }

    /**
     * 添加旋转定位小蓝点
     *
     * @param latlng
     */
    private void addLocationRotateMarker(LatLng latlng) {
        if (mLocationMarker != null) {
            mLocationMarker.destroy();
            mLocationMarker = null;
        }
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(),
                R.drawable.icon_gps_rotate));
        MarkerOptions markerOptions = new MarkerOptions();
        //3D效果
        markerOptions.icon(bitmapDescriptor);
        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.position(latlng);
        mLocationMarker = aMap.addMarker(markerOptions);
    }

    /**
     * 添加定位蓝点精度圆圈
     */
    private void addCircle() {
        if (mCircle != null) {
            mCircle.remove();
            mCircle = null;
        }
        CircleOptions options = new CircleOptions();
        options.strokeWidth(1f);
        options.strokeColor(Color.argb(180, 3, 145, 255));
        options.fillColor(Color.argb(10, 0, 0, 180));
        mCircle = aMap.addCircle(options);
        mCircle.setCenter(myLatLng);
        mCircle.setRadius(mAccuracy);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case REQUEST_CODE_POI_SEARCH:
                if (resultCode != Activity.RESULT_OK) {
                    onCancelPoiSearchClick();
                    return;
                }

                if (intent != null) {
                    PoiItem poiItem = intent.getParcelableExtra(AppContants.Extras.POIITEM_INFO);
                    if (poiItem == null) {
                        return;
                    }


                    isPoiClick = true;
                    LatLonPoint point = poiItem.getLatLonPoint();
                    LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
                    gaoDePoiProcessHelper.addPOIMarderAndShowDetail(latLng, poiItem.getTitle());

                    mMapSearchView.setSearchMode(MapSearchView.SEARCH_WITH_INPUT_TEXT);
                    mMapSearchView.setPoiInputText(poiItem.getTitle());
                    sharePoi = poiItem;
                }
                break;

            case REQUEST_CODE_LATLNG_SEARCH:
                if (resultCode != Activity.RESULT_OK) {
                    onCancelPoiSearchClick();
                    return;
                }
                if (intent != null) {
                    LatLng latLng = intent.getParcelableExtra(AppContants.Extras.POI_LATLNG);
                    String title = intent.getStringExtra(AppContants.Extras.POI_TITLE);
                    if (latLng == null) {
                        return;
                    }

                    isPoiClick = true;
                    gaoDePoiProcessHelper.addPOIMarderAndShowDetail(latLng, title);

                    mMapSearchView.setSearchMode(MapSearchView.SEARCH_WITH_INPUT_TEXT);
                    mMapSearchView.setPoiInputText(title);
                    sharePoi = new PoiItem(null, new LatLonPoint(latLng.latitude, latLng.longitude), title, title);
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 处理返回键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            switch (mMapMode) {
                case NORMAL:
                    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    } else {
                        return super.onKeyDown(keyCode, event);
                    }

                case SHOW_POIDETAIL:
                    mMapMode = MapMode.NORMAL;
                    gaoDePoiProcessHelper.onPoiCloseClick();

                    if (mMapSearchView.getSearchType() == PoiSearchType.LATLNG_SEARCH) {
                        onCancelPoiSearchClick();
                    } else {
                        if (mMapSearchView.getSearchMode() == MapSearchView.SEARCH_WITH_INPUT_TEXT) {
                            onSearchNormalClick();
                        }
                    }
                    return true;

                case CACULATE_DISTANCE:
                    gaoDeCaculateDistanceHelper.onCancelDistanceClick();
                    return true;

            }
        }

        return super.onKeyDown(keyCode, event);
    }

}
