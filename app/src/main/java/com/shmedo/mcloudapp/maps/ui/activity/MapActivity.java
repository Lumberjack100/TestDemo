package com.shmedo.mcloudapp.maps.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

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
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.DistanceItem;
import com.amap.api.services.route.DistanceResult;
import com.amap.api.services.route.DistanceSearch;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.maps.model.MapLayerInfo;
import com.shmedo.mcloudapp.maps.ui.view.DistanceToolbarView;
import com.shmedo.mcloudapp.maps.ui.view.GPSView;
import com.shmedo.mcloudapp.maps.ui.view.LocationTitleView;
import com.shmedo.mcloudapp.maps.ui.view.MapSearchView;
import com.shmedo.mcloudapp.maps.ui.view.NaviMapLayerView;
import com.shmedo.mcloudapp.maps.ui.view.NaviToolView;
import com.shmedo.mcloudapp.maps.ui.view.PoiDetailBottomView;
import com.shmedo.mcloudapp.maps.ui.view.RouteView;
import com.shmedo.mcloudapp.maps.ui.view.SupendPartitionView;
import com.shmedo.mcloudapp.maps.ui.view.ZoomView;
import com.shmedo.mcloudapp.maps.util.AMapLocationUtil;
import com.shmedo.mcloudapp.maps.util.CoordinateFormatUtils;
import com.shmedo.mcloudapp.maps.util.MapErrorUtil;
import com.shmedo.mcloudapp.maps.util.MyAMapUtils;
import com.shmedo.mcloudapp.maps.util.SensorEventHelper;
import com.shmedo.mcloudapp.util.DensityUtil;
import com.shmedo.mcloudapp.util.LocationUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class MapActivity extends CheckMapNeedPermissionsActivity implements AMap.OnMapClickListener,  AMap.OnPOIClickListener,DistanceSearch.OnDistanceSearchListener, AMapGestureListener, AMapLocationListener, LocationSource, DistanceToolbarView.OnDistanceToolbarViewClickListener, MapSearchView.OnMapHeaderViewClickListener, NaviMapLayerView.OnMapLayerItemClickListener, PoiDetailBottomView.OnPoiDetailBottomClickListener, ZoomView.OnZoomViewClickListener {
    @BindView(R.id.map)
    TextureMapView mMapView;

    @BindView(R.id.id_drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.nav_tool_view)
    NaviToolView mNaviToolView;

    @BindView(R.id.nav_map_layer_view)
    NaviMapLayerView mNaviMapLayerView;

    @BindView(R.id.distance_toolbar_view)
    DistanceToolbarView mDistanceToolbarView;

    @BindView(R.id.location_title_view)
    LocationTitleView mLocationTitleView;

    @BindView(R.id.map_search_view)
    MapSearchView mMapSearchView;

    @BindView(R.id.supend_partition_view)
    SupendPartitionView mSupendPartitionView;

    @BindView(R.id.zoom_view)
    ZoomView mZoomView;

    @BindView(R.id.gps_route_container)
    View mGspContainer;

    @BindView(R.id.gps_view)
    GPSView mGpsView;

    @BindView(R.id.route_view)
    RouteView mRouteView;

    @BindView(R.id.poi_detail_bottom_view)
    PoiDetailBottomView mPoiDetailBottomView;

    private AMap aMap; //地图控制器对象
    private MyLocationStyle mLocationStyle;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private AMapLocation mAmapLocation;
    private OnLocationChangedListener mOnLocationChangedListener;
    private int mCurrentGpsState = STATE_UNLOCKED;//当前定位状态
    private static final int STATE_UNLOCKED = 0;//未定位状态，默认状态
    private static final int STATE_LOCKED = 1;//定位状态
    private static final int STATE_ROTATE = 2;//根据地图方向旋转状态
    private int mZoomLevel = 16;//地图缩放级别，最大缩放级别为20
    private LatLng mLatLng;//当前定位经纬度
    private LatLng mClickPoiLatLng;//当前点击的poi经纬度
    private static long mAnimDuartion = 500L;//地图动效时长
    private int mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;//地图状态类型
    private SensorEventHelper mSensorHelper;
    private Marker mLocationMarker;//自定义小蓝点
    private Circle mCircle;

    private boolean isFirstLocation = true;//第一次定位
    private boolean isCanMoveToCenter = true;//是否可以移动地图到定位点
    private boolean onScrolling;//正在滑动地图
    // 当前是否正在处理POI点击
    private boolean isPoiClick;
    private boolean slideDown;//向下滑动
    private float mAccuracy;
    private int moveY;
    private int[] mBottomSheetLoc = new int[2];
    private String mPoiName;
    private String mCity;

    //以下变量是测距所需
    private boolean isDistanceMode = false;//是否打开测距工具
    private int markerHeight;
    private int markerWidth;
    private List<LatLng> latLngList = new ArrayList<>();
    private List<Marker> markerList = new ArrayList<>();
    private List<Polyline> polylineList = new ArrayList<>();


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, MapActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_map;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        initStates();
        initView(savedInstanceState);
        setListener();
    }

    /*//沉浸式状态栏
    private void initStates() {
        if (Build.VERSION.SDK_INT > 19 && getApplicationContext().getApplicationInfo().targetSdkVersion > 19) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }*/

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
        if (null == mSensorHelper) {
            aMap.clear();
            mSensorHelper = new SensorEventHelper(this);
            //重新注册
            mSensorHelper.registerSensorListener();
//            setUpMap();
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
        if (mSensorHelper != null) {
            mSensorHelper.unRegisterSensorListener();
            mSensorHelper.setCurrentMarker(null);
            mSensorHelper = null;
        }
        if (mLocationClient != null) {
            mLocationClient.onDestroy();
        }
        if (mLocationMarker != null) {
            mLocationMarker.destroy();
        }
    }

    private void initView(Bundle savedInstanceState) {
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        mGpsView.setGpsState(mCurrentGpsState);
        initDistanceToolView();
        if (aMap == null) {
            //初始化地图控制器对象
            aMap = mMapView.getMap();
            setUpMap();
        }
    }

    private void initDistanceToolView() {
        markerHeight = DensityUtil.Dp2Px(this, 12);
        markerWidth = DensityUtil.Dp2Px(this, 12);
        mDistanceToolbarView.mIvRemoveMarker.setEnabled(false);
        mDistanceToolbarView.mIvClearMarkers.setEnabled(false);
        mDistanceToolbarView.mTvDistance.setText("0米");
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
        if (null == mLocationStyle) {
            mLocationStyle = new MyLocationStyle();
            mLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
            mLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));//圆圈的颜色,设为透明
        }
        // 将自定义的 myLocationStyle 对象添加到地图上
        aMap.setMyLocationStyle(mLocationStyle.myLocationType(mMapType));
    }

    private void setListener() {
        //地图手势事件
        aMap.setAMapGestureListener(this);
        // 对amap添加单击地图事件监听器
        aMap.setOnMapClickListener(this);
        // 地图poi点击
        aMap.setOnPOIClickListener(this);
        mSensorHelper = new SensorEventHelper(this);
        mSensorHelper.registerSensorListener();
        mDistanceToolbarView.setOnDistanceToolbarViewClickListener(this);
        mMapSearchView.setOnMapHeaderViewClickListener(this);
        mNaviMapLayerView.setOnMapLayerItemClickListener(this);
        mZoomView.setOnZoomViewClickListener(this);
        mPoiDetailBottomView.setOnPoiDetailBottomClickListener(this);
    }


    @Override
    protected void doOnPermissionGranted() {
    }

    @OnClick({R.id.gps_view, R.id.route_view, R.id.mapToolView, R.id.mapLayerView, R.id.testSpeedView, R.id.measureDistanceView})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gps_view://Gps 定位
                processGpsViewClick();
                break;

            case R.id.route_view://路线

                break;

            case R.id.mapToolView://退出/关闭工具箱抽屉
                mNaviToolView.setVisibility(View.VISIBLE);
                mNaviMapLayerView.setVisibility(View.GONE);

                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                break;

            case R.id.mapLayerView://退出/关闭图层抽屉
                mNaviToolView.setVisibility(View.GONE);
                mNaviMapLayerView.setVisibility(View.VISIBLE);

                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                break;

            case R.id.testSpeedView://路线
                SpeedTestActivity.startActivity(this);
                break;

            case R.id.measureDistanceView://测距按钮
                isDistanceMode = true;
                setDistanceToolbarViewVisibility(true);
                mDrawerLayout.closeDrawer(GravityCompat.START);
                break;
        }
    }

    private void processGpsViewClick() {
        if (!LocationUtils.getInstance().isGpsEnabled()) {
            showGPSSettingDialog();
            return;
        }

        CameraUpdate cameraUpdate = null;
        isCanMoveToCenter = true;
        isPoiClick = false;
        //修改定位图标状态
        switch (mCurrentGpsState) {
            case STATE_LOCKED:
                mZoomLevel = 18;
                mAnimDuartion = 500;
                mCurrentGpsState = STATE_ROTATE;
                //连续定位、且将视角移动到地图中心点，地图依照设备方向旋转，定位点会跟随设备移动。
                mMapType = MyLocationStyle.LOCATION_TYPE_MAP_ROTATE;
                cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(mLatLng, mZoomLevel, 30, 0));
                break;

            case STATE_UNLOCKED:
            case STATE_ROTATE:
                mZoomLevel = 16;
                mAnimDuartion = 500;
                mCurrentGpsState = STATE_LOCKED;
                //连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
                mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
                cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(mLatLng, mZoomLevel, 0, 0));
                break;
        }

        //显示底部POI详情
        if (mPoiDetailBottomView.getVisibility() == View.GONE) {
            showPoiDetail("我的位置", String.format("在%s附近", mPoiName));
            moveGspButtonAbove();
        } else {
            mPoiDetailBottomView.tvPoiTitle.setText("我的位置");
            mPoiDetailBottomView.tvPoiDistance.setText(String.format("在%s附近", mPoiName));
        }

        aMap.setMyLocationEnabled(true);
        //改变定位图标状态
        mGpsView.setGpsState(mCurrentGpsState);
        //执行地图动效
        aMap.animateCamera(cameraUpdate, mAnimDuartion, null);
//        setLocationStyle();
        resetLocationMarker();
    }

    @Override
    public void onLocationChanged(final AMapLocation aMapLocation) {
        if (null == mOnLocationChangedListener || null == aMapLocation || aMapLocation.getErrorCode() != 0) {
            if (aMapLocation != null) {
                Timber.d("定位失败：errorCode=" + aMapLocation.getErrorCode() + ",errorMsg=" + aMapLocation.getErrorInfo());
            }
            return;
        }

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

        if (onScrolling) {
            Timber.e("MapView is Scrolling by user,can not operate...");
            return;
        }
        // 当前poiName和上次不相等才更新显示
        if (aMapLocation.getPoiName() != null && !aMapLocation.getPoiName().equals(mPoiName)) {
            if (!isPoiClick) {
                // 点击poi时,定位位置和点击位置不一定一样
                mPoiName = aMapLocation.getPoiName();
                showPoiNameText(String.format("在%s附近", mPoiName));
            }
        }
        Timber.d("定位成功，onLocationChanged： Longitude=" + lng + ",Latitude=" + lat + ",poiName=" + mPoiName + ",getDescription=" + aMapLocation.getDescription() + ", address=" + aMapLocation.getAddress() + ",getLocationDetail" + aMapLocation.getLocationDetail() + ",street=" + aMapLocation.getStreet());

        //参数依次是：视角调整区域的中心点坐标、希望调整到的缩放级别、俯仰角0°~45°（垂直与地图时为0）、偏航角 0~360° (正北方为0)
        mLatLng = new LatLng(lat, lng);
        if (!aMapLocation.getCity().equals(mCity)) {
            mCity = aMapLocation.getCity();
        }

        //首次定位,选择移动到地图中心点并修改级别到15级
        //首次定位成功才修改地图中心点，并移动
        mAccuracy = aMapLocation.getAccuracy();
        Timber.d("accuracy=" + mAccuracy + ",mFirstLocation=" + isFirstLocation);
        if (isFirstLocation) {
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, mZoomLevel), new AMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    mCurrentGpsState = STATE_LOCKED;
                    mGpsView.setGpsState(mCurrentGpsState);
                    mMapType = MyLocationStyle.LOCATION_TYPE_LOCATE;
                    addCircle(mLatLng, mAccuracy);//添加定位精度圆
                    addLockedMarker(mLatLng);//添加定位图标
                    mSensorHelper.setCurrentMarker(mLocationMarker);//定位图标旋转
                    isFirstLocation = false;
                }

                @Override
                public void onCancel() {

                }
            });
        } else {
            //BottomSheet顶上显示,地图缩小显示
            mCircle.setCenter(mLatLng);
            mCircle.setRadius(mAccuracy);
            mLocationMarker.setPosition(mLatLng);
            if (isCanMoveToCenter) {
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, mZoomLevel));
            }
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
            //运行时权限
                /*if (PermissionUtil.checkPermissions(this)) {
                    mLocationClient.startLocation();
                } else {
                    //未授予权限，动态申请
                    PermissionUtil.initPermissions(this, REQ_CODE_INIT);
                }*/
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
     * 当用户点击底图上的poi时回调此方法。
     */
    @Override
    public void onPOIClick(Poi poi) {
        if(poi == null || poi.getCoordinate() == null || TextUtils.isEmpty(poi.getName())){
            return;
        }
        // 当前点击坐标
        mClickPoiLatLng = poi.getCoordinate();
        // 当前正在处理poi点击
        isPoiClick = true;
        addPOIMarderAndShowDetail(poi.getCoordinate(), poi.getName());
    }

    /**
     * 地图手势事件回调：单指双击
     */
    @Override
    public void onDoubleTap(float v, float v1) {
        mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
        mCurrentGpsState = STATE_UNLOCKED;
        mGpsView.setGpsState(mCurrentGpsState);
//        setLocationStyle();
        resetLocationMarker();
        isCanMoveToCenter = false;
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
        if (!onScrolling) {
            onScrolling = true;
            Timber.d("onScroll,x=" + v + ",y=" + v1);
            //旋转不移动到中心点
            mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
            mCurrentGpsState = STATE_UNLOCKED;
            //当前没有正在定位才能修改状态
            if (!isFirstLocation) {
                mGpsView.setGpsState(mCurrentGpsState);
            }
            isCanMoveToCenter = false;
//            setLocationStyle();
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
        onScrolling = false;
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
     * 点击测距返回箭头
     */
    @Override
    public void onCancelDistanceClick() {
        isDistanceMode = false;
        onClearMarkersClick();
        setDistanceToolbarViewVisibility(false);
    }

    /**
     * 点击移除测距点标记
     */
    @Override
    public void onRemoveMarkerClick() {
        if (markerList.size() == 0 || latLngList.size() == 0) {
            return;
        }
        latLngList.remove(latLngList.size() - 1);

        Marker mLastMarker = markerList.get(markerList.size() - 1);
        markerList.remove(mLastMarker);
        mLastMarker.destroy();

        if (polylineList.size() > 0) {
            Polyline polyline = polylineList.get(polylineList.size() - 1);
            polylineList.remove(polyline);
            polyline.remove();
        }

        if (markerList.size() > 0) {
            mLastMarker = markerList.get(markerList.size() - 1);
            BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.measure_point_red);
            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), markerWidth, markerHeight, false);
            mLastMarker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        }

        if (latLngList.size() >= 2) {
            calculateRouteDistance();
        } else {
            mDistanceToolbarView.mTvDistance.setText("0米");
        }
        if (markerList.size() == 0) {
            mDistanceToolbarView.mIvRemoveMarker.setEnabled(false);
            mDistanceToolbarView.mIvClearMarkers.setEnabled(false);
        }
    }

    /**
     * 点击清空所有测距点标记
     */
    @Override
    public void onClearMarkersClick() {
        mDistanceToolbarView.mIvRemoveMarker.setEnabled(false);
        mDistanceToolbarView.mIvClearMarkers.setEnabled(false);
        aMap.clear();
        latLngList.clear();
        markerList.clear();
        polylineList.clear();
        mDistanceToolbarView.mTvDistance.setText("0米");
    }


    /**
     * 地图点击事件
     *
     * @param latLng
     */
    @Override
    public void onMapClick(LatLng latLng) {
        //打开测距模式下
        if (isDistanceMode) {
            addMarkersForDistance(latLng);
            if (latLngList.size() >= 2) {
                addPolylinesForDistance();
                calculateRouteDistance();
            }
        }
    }

    /**
     * 绘制点标记
     *
     * @param latLng
     */
    private void addMarkersForDistance(LatLng latLng) {
        mDistanceToolbarView.mIvRemoveMarker.setEnabled(true);
        mDistanceToolbarView.mIvClearMarkers.setEnabled(true);
        latLngList.add(latLng);

        if (latLngList.size() == 1) {
            MarkerOptions markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.polyline_start))
                    .position(latLng)
                    .draggable(false);
            Marker marker = aMap.addMarker(markerOption);
            markerList.add(marker);
            return;
        }

        if (markerList.size() >= 2) {
            Marker mLastMarker = markerList.get(markerList.size() - 1);
            BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.measure_point);
            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), markerWidth, markerHeight, false);
            mLastMarker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        }

        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.measure_point_red);
        Bitmap smallMarker = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), markerWidth, markerHeight, false);
        MarkerOptions markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                .position(latLng);
        Marker marker = aMap.addMarker(markerOption);
        markerList.add(marker);
    }

    /**
     * 绘制线
     */
    private void addPolylinesForDistance() {
        LatLng latLngStart = latLngList.get(latLngList.size() - 2);
        LatLng latLngEnd = latLngList.get(latLngList.size() - 1);
        PolylineOptions polylineOptions = new PolylineOptions().add(latLngStart, latLngEnd).width(15).color(Color.BLUE);
        Polyline polyline = aMap.addPolyline(polylineOptions);
        polylineList.add(polyline);
    }

    /**
     * 开始搜索路径规划方案
     */
    public void calculateRouteDistance() {
        List<LatLonPoint> latLonPoints = new ArrayList<>();
        for (int i = 0; i < latLngList.size() - 1; i++) {
            LatLng latLng = latLngList.get(i);
            latLonPoints.add(new LatLonPoint(latLng.latitude, latLng.longitude));
        }
        LatLonPoint dest = new LatLonPoint(latLngList.get(latLngList.size() - 1).latitude, latLngList.get(latLngList.size() - 1).longitude);

        DistanceSearch distanceSearch = new DistanceSearch(this);
        distanceSearch.setDistanceSearchListener(this);
        DistanceSearch.DistanceQuery distanceQuery = new DistanceSearch.DistanceQuery();
        distanceQuery.setOrigins(latLonPoints);
        distanceQuery.setDestination(dest);
        distanceQuery.setType(DistanceSearch.TYPE_DISTANCE);

        distanceSearch.calculateRouteDistanceAsyn(distanceQuery);
    }

    @Override
    public void onDistanceSearched(DistanceResult distanceResult, int errorCode) {
        if (errorCode != AMapException.CODE_AMAP_SUCCESS) {
            ToastUtils.show(MapErrorUtil.getErrorMsg(errorCode));
            return;
        }

        float totalDistance = 0;
        List<DistanceItem> distanceItems = distanceResult.getDistanceResults();
        for (DistanceItem item : distanceItems) {
            totalDistance += item.getDistance();
        }
        showDistance(totalDistance);
    }

    private void showDistance(float distance) {
        if (distance > 1000) {
            DecimalFormat decimalFormat = new DecimalFormat(".0");//构造方法的字符格式这里如果小数不足2位,会以0补足.
            String p = decimalFormat.format(distance / 1000);//format 返回的是字符串
            mDistanceToolbarView.mTvDistance.setText(String.format("%s公里", p));
        } else {
            mDistanceToolbarView.mTvDistance.setText(String.format("%s米", distance));
        }
    }

    private void setDistanceToolbarViewVisibility(boolean isOpen) {
        mDistanceToolbarView.setVisibility(isOpen ? View.VISIBLE : View.GONE);
        mLocationTitleView.setVisibility(!isOpen ? View.VISIBLE : View.GONE);
        mMapSearchView.setVisibility(!isOpen ? View.VISIBLE : View.GONE);
        mSupendPartitionView.setVisibility(!isOpen ? View.VISIBLE : View.GONE);
        mRouteView.setVisibility(!isOpen ? View.VISIBLE : View.GONE);
        aMap.setAMapGestureListener(!isOpen ? this : null);
        mDistanceToolbarView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(mMapView.getLayoutParams());
                layoutParams.topMargin = isOpen ? mDistanceToolbarView.getHeight() : 0;
                mMapView.setLayoutParams(layoutParams);
                mDistanceToolbarView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    /**
     * 点击返回箭头
     */
    @Override
    public void onBackClick() {
        finish();
    }

    /**
     * 点击常规搜索点位
     */
    @Override
    public void onSearchNormalClick() {
//        SearchPoiFragment newFragment =  SearchPoiFragment.newInstance(mCity);
//        newFragment.show(getSupportFragmentManager(), "dialog");

        PoiSearchActivity.startActivityForResult(this, 0);
    }

    /**
     * 点击按经纬度搜索点位
     */
    @Override
    public void onSearchLatLongClick() {

    }

    /**
     * 点击放大
     */
    @Override
    public void onZoomInClick() {
        aMap.moveCamera(CameraUpdateFactory.zoomIn());
    }

    /**
     * 点击缩小
     */
    @Override
    public void onZoomOutClick() {
        aMap.moveCamera(CameraUpdateFactory.zoomOut());
    }

    /**
     * 关闭点位详情框
     */
    @Override
    public void onCloseClick() {
        resetGpsButtonPosition();
        hidePoiDetail();
    }

    /**
     * 分享点位信息
     */
    @Override
    public void onShareClick() {

    }

    /**
     * 跳转点位导航
     */
    @Override
    public void onNaviClick() {

    }

    /**
     * 跳转点位路线
     */
    @Override
    public void onRouteClick() {

    }

    /**
     * 添加POImarker
     */
    private void addPOIMarderAndShowDetail(LatLng latLng, String poiName) {
        animMap(latLng);
        mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
        mCurrentGpsState = STATE_UNLOCKED;
        //当前没有正在定位才能修改状态
        if (!isFirstLocation) {
            mGpsView.setGpsState(mCurrentGpsState);
        }
        isCanMoveToCenter = false;
        // 添加marker标记
        addPOIMarker(latLng);
        showClickPoiDetail(latLng, poiName);
    }

    /**
     * 移动地图中心点到指定位置
     * @param latLng
     */
    private void animMap(LatLng latLng){
        if(latLng != null){
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, mZoomLevel));
        }
    }

    private void addPOIMarker(LatLng latLng) {
        aMap.clear();
        MarkerOptions markOptiopns = new MarkerOptions();
        markOptiopns.position(latLng);
        markOptiopns.icon(BitmapDescriptorFactory.fromResource(R.drawable.poi_mark));
        aMap.addMarker(markOptiopns);
    }

    /**
     * 显示poi点击底部BottomSheet
     */
    private void showClickPoiDetail(LatLng latLng, String poiName) {
        mPoiName = poiName;
        String distanceStr = MyAMapUtils.calculateDistanceStr(mLatLng, latLng);
        if (mPoiDetailBottomView.getVisibility() == View.GONE) {
            showPoiDetail(poiName, String.format("距离您%s", distanceStr));
            moveGspButtonAbove();
        }else{
            mPoiDetailBottomView.tvPoiTitle.setText(poiName);
            mPoiDetailBottomView.tvPoiDistance.setText(String.format("距离您%s", distanceStr));
        }
    }

    /**
     * 根据当前地图状态重置定位蓝点
     */
    private void resetLocationMarker() {
        aMap.clear();
        mLocationMarker = null;
        if (mGpsView.getGpsState() == GPSView.STATE_ROTATE) {
            //ROTATE模式不需要方向传感器
            //mSensorHelper.unRegisterSensorListener();
            addRotateMarker(mLatLng);
        } else {
            //mSensorHelper.registerSensorListener();
            addLockedMarker(mLatLng);
            if (null != mLocationMarker) {
                mSensorHelper.setCurrentMarker(mLocationMarker);
            }
        }

        addCircle(mLatLng, mAccuracy);
    }

    private void addCircle(LatLng latlng, double radius) {
        CircleOptions options = new CircleOptions();
        options.strokeWidth(1f);
        options.fillColor(Color.argb(10, 0, 0, 180));
        options.strokeColor(Color.argb(240, 3, 145, 255));
        options.center(latlng);
        options.radius(radius);
        mCircle = aMap.addCircle(options);
    }

    private void addLockedMarker(LatLng latlng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(),
                R.drawable.navi_map_gps_locked)));
        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.position(latlng);
        mLocationMarker = aMap.addMarker(markerOptions);
    }

    private void addRotateMarker(LatLng latlng) {
        MarkerOptions markerOptions = new MarkerOptions();
        //3D效果
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(),
                R.drawable.icon_gps_rotate)));
        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.position(latlng);
        mLocationMarker = aMap.addMarker(markerOptions);
    }



    /**
     * 隐藏底部POI详情
     */
    public void hidePoiDetail() {
        mPoiDetailBottomView.setVisibility(View.GONE);
        //gsp控件回退到原来位置、并显示底部其他控件
        mRouteView.setVisibility(View.VISIBLE);
    }

    /**
     * 显示底部POI详情
     *
     * @param locTitle 定位标题,比如当前所在位置名称
     * @param locInfo  定位信息,比如当前在什么附近/距离当前位置多少米
     */
    public void showPoiDetail(String locTitle, String locInfo) {
        mPoiDetailBottomView.setVisibility(View.VISIBLE);
        mGpsView.setVisibility(View.VISIBLE);
        mRouteView.setVisibility(View.GONE);
        //我的位置
        mPoiDetailBottomView.tvPoiTitle.setText(locTitle);
        mPoiDetailBottomView.tvPoiDistance.setText(locInfo);
    }

    /**
     * 显示当前所在poi点信息
     */
    private void showPoiNameText(String locInfo) {
        mPoiDetailBottomView.tvPoiDistance.setText(locInfo);
    }

    /**
     * 将GpsButton移动到poi detail上面
     */
    private void moveGspButtonAbove() {
        mPoiDetailBottomView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mGpsView.isAbovePoiDetail()) {
                    //已经在上面，不需要重复调用
                    return;
                }
                if (moveY == 0) {
                    //计算Y轴方向移动距离
                    moveY = mGspContainer.getTop() - mPoiDetailBottomView.getTop() + mGspContainer.getMeasuredHeight() + getResources().getDimensionPixelSize(R.dimen.dimen_size_10);
                    mPoiDetailBottomView.getLocationInWindow(mBottomSheetLoc);
                }
                if (moveY > 0) {
                    mZoomView.setTranslationY(-moveY);
                    mGspContainer.setTranslationY(-moveY);
                    mGpsView.setAbovePoiDetail(true);
                }
            }
        });
    }

    /**
     * 将GpsButton移动到原来位置
     */
    private void resetGpsButtonPosition() {
        mPoiDetailBottomView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!mGpsView.isAbovePoiDetail()) {
                    //已经在下面，不需要重复调用
                    return;
                }
                //回到原来位置
                mZoomView.setTranslationY(0);
                mGspContainer.setTranslationY(0);
                mGpsView.setAbovePoiDetail(false);
            }
        });
    }
}
