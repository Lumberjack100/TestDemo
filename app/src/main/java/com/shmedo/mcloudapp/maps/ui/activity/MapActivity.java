package com.shmedo.mcloudapp.maps.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

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
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.maps.ui.view.GPSView;
import com.shmedo.mcloudapp.maps.util.AMapLocationUtil;
import com.shmedo.mcloudapp.maps.util.SensorEventHelper;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class MapActivity extends CheckMapNeedPermissionsActivity implements AMapGestureListener, AMapLocationListener, LocationSource {
    @BindView(R.id.map)
    TextureMapView mMapView;


    @BindView(R.id.gps_route_container)
    View mGspContainer;

    @BindView(R.id.gps_view)
    GPSView mGpsView;

    private AMap aMap; //地图控制器对象
    private MyLocationStyle myLocationStyle;
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
    private Marker mLocMarker;//自定义小蓝点
    private Circle mCircle;

    private boolean isFirstLocation = true;//第一次定位
    private boolean isCanMoveToCenter = true;//是否可以移动地图到定位点
    private boolean onScrolling;//正在滑动地图
    // 当前是否正在处理POI点击
    private boolean isPoiClick;
    private boolean slideDown;//向下滑动
    private float mAccuracy;
    private String mPoiName;
    private String mCity;


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
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
//        if (null != mLocationClient) {
//            mLocationClient.stopLocation();
//            mLocationClient.onDestroy();
//            mLocationClient = null;
//        }
    }

    private void initView(Bundle savedInstanceState) {
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        mGpsView.setGpsState(mCurrentGpsState);

        setUpMap();
    }

    private void setListener() {
        //地图手势事件
        aMap.setAMapGestureListener(this);
        mSensorHelper = new SensorEventHelper(this);
        mSensorHelper.registerSensorListener();
    }

    private void setUpMap() {
        if (aMap == null) {
            //初始化地图控制器对象
            aMap = mMapView.getMap();
        }
        // 设置定位监听
        aMap.setLocationSource(this);
        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);//卫星地图模式
        // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.setMyLocationEnabled(true);
        aMap.getUiSettings().setZoomControlsEnabled(false); //隐藏缩放控件
        aMap.getUiSettings().setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_RIGHT);//设置logo位置
//        mUiSettings.setLogoBottomMargin(-50);//隐藏logo
        setupLocationStyle();
    }

    /**
     * 设置 定位小蓝点（当前位置）的绘制样式
     */
    private void setupLocationStyle() {
        // 自定义系统定位蓝点
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.strokeColor(getResources().getColor(R.color.app_color_blue_2));  //设置定位小蓝点精度圆圈的边框颜色
        myLocationStyle.strokeWidth(1); //设置定位小蓝点精度圆圈的边框宽度
        myLocationStyle.radiusFillColor(Color.argb(100, 29, 161, 242)); // 设置定位小蓝点精度圆圈的填充颜色
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);//定位一次，且将视角移动到地图中心点。
        // 将自定义的 myLocationStyle 对象添加到地图上
        aMap.setMyLocationStyle(myLocationStyle);
    }


    @Override
    protected void doOnPermissionGranted() {
    }

    @OnClick({R.id.gps_view})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gps_view:
                processGpsViewClick();
                break;
        }
    }

    private void processGpsViewClick() {
        if (!isGPSOPen(this)) {
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
//                setLocationStyle(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE);
                mMapType = MyLocationStyle.LOCATION_TYPE_MAP_ROTATE;
                cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(mLatLng, mZoomLevel, 30, 0));
                break;
            case STATE_UNLOCKED:
            case STATE_ROTATE:
                mZoomLevel = 16;
                mAnimDuartion = 500;
                mCurrentGpsState = STATE_LOCKED;
                mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
                cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(mLatLng, mZoomLevel, 0, 0));
                break;
        }
        //显示底部POI详情
//        if (mBottomSheet.getVisibility() == View.GONE || mBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
//            showPoiDetail("我的位置", String.format("在%s附近", mPoiName));
//            moveGspButtonAbove();
//        } else {
//            mTvLocTitle.setText("我的位置");
//            mTvLocation.setText(String.format("在%s附近", mPoiName));
//        }

        aMap.setMyLocationEnabled(true);
        Timber.d( "onGPSClick:mCurrentGpsState=" + mCurrentGpsState + ",mMapType=" + mMapType);
        //改变定位图标状态
        mGpsView.setGpsState(mCurrentGpsState);
        //执行地图动效
        aMap.animateCamera(cameraUpdate, mAnimDuartion, new AMap.CancelableCallback() {
            @Override
            public void onFinish() {
            }

            @Override
            public void onCancel() {

            }
        });
//        setLocationStyle();
        resetLocationMarker();
    }

    @Override
    public void onLocationChanged(final AMapLocation location) {
        if (null == mOnLocationChangedListener || null == location || location.getErrorCode() != 0) {
            if (location != null) {
                Timber.d("定位失败：errorCode=" + location.getErrorCode() + ",errorMsg=" + location.getErrorInfo());
            }
            return;
        }

        mAmapLocation = location;
        if (onScrolling) {
            Timber.e("MapView is Scrolling by user,can not operate...");
            return;
        }
        //获取经纬度
        double lng = location.getLongitude();
        double lat = location.getLatitude();
        // 当前poiName和上次不相等才更新显示
        if (location.getPoiName() != null && !location.getPoiName().equals(mPoiName)) {
            if (!isPoiClick) {
                // 点击poi时,定位位置和点击位置不一定一样
                mPoiName = location.getPoiName();
//                showPoiNameText(String.format("在%s附近", mPoiName));
            }
        }
        Timber.d("定位成功，onLocationChanged： Longitude=" + lng + ",Latitude=" + lat + ",poiName=" + mPoiName + ",getDescription=" + location.getDescription() + ", address=" + location.getAddress() + ",getLocationDetail" + location.getLocationDetail() + ",street=" + location.getStreet());

        //参数依次是：视角调整区域的中心点坐标、希望调整到的缩放级别、俯仰角0°~45°（垂直与地图时为0）、偏航角 0~360° (正北方为0)
        mLatLng = new LatLng(lat, lng);
        if (!location.getCity().equals(mCity)) {
            mCity = location.getCity();
        }

        //首次定位,选择移动到地图中心点并修改级别到15级
        //首次定位成功才修改地图中心点，并移动
        mAccuracy = location.getAccuracy();
        Timber.d("accuracy=" + mAccuracy + ",mFirstLocation=" + isFirstLocation);
        if (isFirstLocation) {
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, mZoomLevel), new AMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    mCurrentGpsState = STATE_LOCKED;
                    mGpsView.setGpsState(mCurrentGpsState);
                    mMapType = MyLocationStyle.LOCATION_TYPE_LOCATE;
                    addCircle(mLatLng, mAccuracy);//添加定位精度圆
                    addMarker(mLatLng);//添加定位图标
                    mSensorHelper.setCurrentMarker(mLocMarker);//定位图标旋转
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
            mLocMarker.setPosition(mLatLng);
            if (isCanMoveToCenter) {
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, mZoomLevel));
            }
        }
    }

    /**
     * 激活定位
     *
     * @param listener
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

            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
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
        mLocMarker = null;
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
     * 地体手势事件回调：单指滑动
     */
    @Override
    public void onScroll(float v, float v1) {
        //避免重复调用闪屏，当手指up才重置为false
        if (!onScrolling) {
            onScrolling = true;
            Timber.d( "onScroll,x=" + v + ",y=" + v1);
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
     * 地体手势事件回调：单指按下
     */
    @Override
    public void onDown(float v, float v1) {

    }

    /**
     * 地体手势事件回调：单指抬起
     */
    @Override
    public void onUp(float v, float v1) {
        onScrolling = false;
    }

    /**
     * 地体手势事件回调：地图稳定下来会回到此接口
     */
    @Override
    public void onMapStable() {

    }

    /**
     * 根据当前地图状态重置定位蓝点
     */
    private void resetLocationMarker() {
        aMap.clear();
        mLocMarker = null;
        if (mGpsView.getGpsState() == GPSView.STATE_ROTATE) {
            //ROTATE模式不需要方向传感器
            //mSensorHelper.unRegisterSensorListener();
            addRotateMarker(mLatLng);
        } else {
            //mSensorHelper.registerSensorListener();
            addMarker(mLatLng);
            if (null != mLocMarker) {
                mSensorHelper.setCurrentMarker(mLocMarker);
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

    private void addMarker(LatLng latlng) {
        /*if (mLocMarker != null) {
            return;
        }*/
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(),
                R.drawable.navi_map_gps_locked)));
        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.position(latlng);
        mLocMarker = aMap.addMarker(markerOptions);
    }

    private void addRotateMarker(LatLng latlng) {
       /* if (mLocMarker != null) {
            return;
        }*/
        MarkerOptions markerOptions = new MarkerOptions();
        //3D效果
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(),
                R.drawable.navi_map_gps_3d)));
        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.position(latlng);
        mLocMarker = aMap.addMarker(markerOptions);
    }
}
