package com.shmedo.mcloudapp.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.dragon.core.MCloudApp;
import com.dragon.core.util.DensityUtil;
import com.dragon.core.util.GlobalUtil;
import com.google.gson.reflect.TypeToken;
import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.InfoWinAdapter;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.DeviceBasicInfoResult;
import com.shmedo.mcloudapp.entity.DeviceBasicInfoResultDao;
import com.shmedo.mcloudapp.entity.DeviceTypeEnum;
import com.shmedo.mcloudapp.entity.StatusInfoResult;
import com.shmedo.mcloudapp.entity.cluster.ClusterAnotherClickListener;
import com.shmedo.mcloudapp.entity.cluster.ClusterAnotherRender;
import com.shmedo.mcloudapp.entity.cluster.ClusterItem;
import com.shmedo.mcloudapp.entity.cluster.ClusterItemImp;
import com.shmedo.mcloudapp.entity.cluster.ClusterOverlayMerchant;
import com.shmedo.mcloudapp.entity.event.MapDeviceEvent;
import com.shmedo.mcloudapp.entity.event.WifiEvent;
import com.shmedo.mcloudapp.entity.parameter.LocationResult;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.ui.SearchDataUI;
import com.shmedo.mcloudapp.user.ui.activity.NewUserInfoActivity;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;
import com.shmedo.mcloudapp.util.permission.UpdataManagerUtil;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

public class MainActivity extends BaseActivity {

    public static final int REQUEST_CODE_SCAN = 0x001;

    public static final int PERMISSION_CODE_GPS = 0x011;


    @BindView(R.id.img_user)
    ImageView mImgUser;

    @BindView(R.id.main_titile)
    TextView mMainTitile;

    @BindView(R.id.img_equipment)
    ImageView mImgEquipment;

    @BindView(R.id.map)
    MapView mMapView;

    @BindView(R.id.tv_connection)
    TextView mTvConnection;

    @BindView(R.id.ll_connection)
    LinearLayout mLlConnection;

    private SearchDataUI searchDataUI;

    private AMap aMap; //初始化地图控制器对象

    private AMapLocationClient mLocationClient;

    private MyLocationStyle myLocationStyle;

    private InfoWinAdapter adapter;

    private LatLng myLatLng;

    private DaoManager manager = DaoManager.getInstance();

    private List<ClusterItem> clusterItemsMerchant = new ArrayList<>();
    private ClusterOverlayMerchant clusterOverlayMerchant;
    private Map<Integer, Drawable> mBackDrawAblesMerchant = new HashMap<Integer, Drawable>();
    private int clusterRadius = 48;

    /**
     * 定位需要进行检测的权限数组
     */
    protected String[] locationNeedPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };


    public static void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_main;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hidingConnectionView();
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        initMap();
        initLocation();
        checkPermissionForGPS();
        searchDataUI = new SearchDataUI(this);
        UpdataManagerUtil.requestPermissionForInstallPackage(this, false);//版本更新
    }

    @Override
    protected void onStart() {
        super.onStart();
        //获取地图要加载的数据
        getDeviceBasicInfoList("1");
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
        if (null != mLocationClient) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
            mLocationClient = null;
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * 初始化地图配置
     */
    private void initMap() {
        if (Build.VERSION.SDK_INT > 28 && getApplicationContext().getApplicationInfo().targetSdkVersion > 28) {
            locationNeedPermissions = new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Permission.ACCESS_BACKGROUND_LOCATION
            };
        }
        if (aMap == null) {
            aMap = mMapView.getMap();
        }

        UiSettings mUiSettings = aMap.getUiSettings();//实例化UiSettings类对象
        mUiSettings.setZoomControlsEnabled(false); //隐藏缩放控件
        mUiSettings.setMyLocationButtonEnabled(false);//设置默认定位按钮是否显示，非必需设置。
        mUiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_RIGHT);//设置logo位置
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        setupLocationStyle();

//        addMerchantClustersToMap(queryLocalDeviceList());
    }


    /**
     * 设置自定义定位蓝点
     */
    private void setupLocationStyle() {
        // 自定义系统定位蓝点
        myLocationStyle = new MyLocationStyle();
//        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.gps_point)); // 自定义定位小蓝点图标
        myLocationStyle.showMyLocation(true);//设置是否显示定位小蓝点，true 显示，false不显示。
        myLocationStyle.strokeColor(getResources().getColor(R.color.app_color_blue_2));  //设置定位小蓝点精度圆圈的边框颜色
        myLocationStyle.strokeWidth(1); //设置定位小蓝点精度圆圈的边框宽度
        myLocationStyle.radiusFillColor(Color.argb(100, 29, 161, 242)); // 设置定位小蓝点精度圆圈的填充颜色
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);//定位一次，且将视角移动到地图中心点。
        // 将自定义的 myLocationStyle 对象添加到地图上
        aMap.setMyLocationStyle(myLocationStyle);
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        //初始化定位
        mLocationClient = new AMapLocationClient(MCloudApp.getContext());
        mLocationClient.setLocationOption(getDefaultOption());
        mLocationClient.setLocationListener(location -> {
            if (null != location) {
                if (location.getErrorCode() == 0) {
                    Timber.i("定位成功===" + location.toString());
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    myLatLng = new LatLng(latitude, longitude);
                    aMap.animateCamera(CameraUpdateFactory.changeLatLng(myLatLng));

                } else {
                    Timber.i("定位失败\n错误码：" + location.getErrorCode()
                            + "\n错误信息:" + location.getErrorInfo()
                            + "\n错误描述:" + location.getLocationDetail());
                }
            } else {
                Timber.i("定位失败，loc is null");
            }
            //停止定位服务
            mLocationClient.stopLocation();
        });

        //获取最后一次定位的位置
        AMapLocation location = mLocationClient.getLastKnownLocation();
        if (location != null && location.getErrorCode() == 0) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            myLatLng = new LatLng(latitude, longitude);
            aMap.animateCamera(CameraUpdateFactory.changeLatLng(myLatLng));
        }
    }


    /**
     * 获取用户在当前公司的设备基础信息列表
     *
     * @param currentCompanyID
     */
    private void getDeviceBasicInfoList(String currentCompanyID) {
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, currentCompanyID);
        MDRetrofit.getInstance()
                .createService()
                .QueryDeviceBasicInfoList(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<DeviceBasicInfoResult>>() {
                    @Override
                    public void Success(List<DeviceBasicInfoResult> infoList, String message) {
                        if (null != infoList && infoList.size() != 0) {
                            for (DeviceBasicInfoResult deviceBasicInfoResult : infoList) {
                                deviceBasicInfoResult.setAccount(MCloudApp.getAccount());
                            }
                            manager.getDaoSession().getDeviceBasicInfoResultDao().insertOrReplaceInTx(infoList);
                        }

                        addMerchantClustersToMap(queryLocalDeviceList());
                    }

                    @Override
                    public void Failure(String message) {
                        Timber.w("服务器连接失败--" + message);
                    }
                });
    }


    /**
     * 添加设备的 marker 点
     *
     * @param deviceList
     */
    private void addMerchantClustersToMap(final List<DeviceBasicInfoResult> deviceList) {
        LatLng latLng = null;
        for (int i = 0; i < deviceList.size(); i++) {
            LocationResult location = GsonFactory.getGson()
                    .fromJson(deviceList.get(i).getInstallLocation(), new TypeToken<LocationResult>() {
                    }.getType());
            if (location != null) {
                latLng = new LatLng(location.getLat(), location.getLng());
                ClusterItemImp clusterImp = new ClusterItemImp(latLng, deviceList.get(i).getDeviceName());
                clusterItemsMerchant.add(clusterImp);
            }
        }

        if (clusterOverlayMerchant == null) {
            clusterOverlayMerchant = new ClusterOverlayMerchant(aMap, clusterItemsMerchant, DensityUtil.Dp2Px(getApplicationContext(), clusterRadius), getApplicationContext());
        } else {
            clusterOverlayMerchant.onDestroy();
            clusterOverlayMerchant = null;
            clusterOverlayMerchant = new ClusterOverlayMerchant(aMap, clusterItemsMerchant, DensityUtil.Dp2Px(getApplicationContext(), clusterRadius), getApplicationContext());
        }

        clusterOverlayMerchant.setClusterAnotherRenderer(new ClusterAnotherRender() {
            @Override
            public Drawable getAnotherDrawAble(int clusterNum) {
                if (clusterNum <= 5) {
                    Drawable bitmapDrawable = mBackDrawAblesMerchant.get(2);
                    if (bitmapDrawable == null) {
                        //bitmapDrawable = getApplication().getResources().getDrawable(checkMarkerIcon(sensorType));
                        bitmapDrawable = getApplication().getResources().getDrawable(R.drawable.icon_marker_das);
                        mBackDrawAblesMerchant.put(2, bitmapDrawable);
                    }
                    return bitmapDrawable;
                } else {
                    Drawable bitmapDrawable = mBackDrawAblesMerchant.get(3);
                    if (bitmapDrawable == null) {
                        bitmapDrawable =
                                getApplication().getResources().getDrawable(R.drawable.icon_marker_das);
                        mBackDrawAblesMerchant.put(3, bitmapDrawable);
                    }
                    return bitmapDrawable;
                }
            }
        });
        clusterOverlayMerchant.setOnClusterAnotherClickListener(new ClusterAnotherClickListener() {
            @Override
            public void onAnotherClick(Marker marker, List<ClusterItem> clusterItems) {
                Toast.makeText(MainActivity.this, ">>>>>>>点击了商家聚合点", Toast.LENGTH_SHORT).show();
                if (aMap.getCameraPosition().zoom <= 18) {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (ClusterItem clusterItem : clusterItems) {
                        builder.include(clusterItem.getPosition());
                    }
                    LatLngBounds latLngBounds = builder.build();
                    aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 10));
                }

            }
        });

    }


    private int checkMarkerIcon(String sensorType) {
        switch (sensorType) {
            case "DAS":
                return R.drawable.icon_marker_das;

            case "DAG":
                return R.drawable.icon_marker_dag;

            case "E60":
                return R.drawable.icon_marker_e60;

            default:
                return R.drawable.icon_marker;
        }
    }


    @OnClick({R.id.img_user, R.id.img_equipment})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_user://用户信息
                NewUserInfoActivity.startActivity(this);
                break;

            case R.id.img_equipment://项目管理
                ProjectListActivity.startActivity(this);
                break;
        }
    }


    /**
     * 配置点击事件
     */
    public void processConfigListener() {
        ToastUtils.show("米易通App远程配置功能开发中...");
    }

    /**
     * 定位点击事件
     */
    public void processLocationListener() {
        checkPermissionForGPS();
    }


    /**
     * 刷新点击事件
     */
    public void processRefreshListener() {
        ToastUtils.show("功能开发中...");
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(WifiEvent events) {
        if (events.getMessage().equals("wifi")) {
            if (events.getWifiBean() != null) {
                Timber.d("wifi--------" + events.getWifiBean().getWifiName());
                showConnectionView("已连接WIFI—" + events.getWifiBean().getWifiName());

            } else {
                hidingConnectionView();
            }
        }
    }


    /**
     * 获取设备信息，显示在地图中
     *
     * @param events
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvents(MapDeviceEvent events) {
        if (events.getType().equals("mapDevice")) {
            if (events.getDeviceName() != null) {
                Timber.d("DeviceName from scan : " + events.getDeviceName());
                String[] device = events.getDeviceName().split(",");
                addDeviceOnMap(device);
            }
        }
    }


    /**
     * 添加新设备到本地数据库，并且在地图上标记设备
     */
    private void addDeviceOnMap(String[] device) {
        if (null != queryExistDevice(device[1])) {
            Timber.d("此设备已存在本地数据库中！");
            return;
        }

        LatLng latLng = myLatLng;
        aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
                .position(latLng)
                .title(device[2])
                .snippet(device[1])
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marker))
        );

        String installLocation = GsonFactory.getGson().toJson(latLng);
        DeviceBasicInfoResult result = new DeviceBasicInfoResult();
        Long proId = System.currentTimeMillis();
        result.setId(proId);
        result.setDeviceName(device[1]);
        result.setDeviceToken(device[1]);
        result.setDeviceTypeID(0);
        result.setDeviceTypeName(device[2]);
        result.setGpsLocation(installLocation);
        result.setInstallLocation(installLocation);
        result.setSecurityNO(null);
        result.setAccount(MCloudApp.getAccount());
        result.setLocal(true);

        StatusInfoResult infoResult = new StatusInfoResult();
        infoResult.setId(proId);
        infoResult.setDeviceName(device[1]);
        infoResult.setDeviceToken(device[1]);
        infoResult.setDeviceTypeID(0);
        infoResult.setDeviceTypeName(device[2]);
        infoResult.setSecurityNO(null);
        infoResult.setSensorInfo(null);
        infoResult.setVoltage(0);
        infoResult.setGprs(0);
        infoResult.setSignal(0);
        infoResult.setAccount(MCloudApp.getAccount());
        infoResult.setLocal(true);

        manager.getDaoSession().getDeviceBasicInfoResultDao().insertOrReplaceInTx(result);
        manager.getDaoSession().getStatusInfoResultDao().insertOrReplaceInTx(infoResult);
    }


    /**
     * 查询本地数据库设备列表中是否有这个设备
     */
    private DeviceBasicInfoResult queryExistDevice(String deviceName) {
        return manager.getDaoSession()
                .getDeviceBasicInfoResultDao()
                .queryBuilder()
                .where(DeviceBasicInfoResultDao.Properties.DeviceName.eq(deviceName))
                .unique();
    }


    /**
     * 查询位置信息不为空的当前用户的设备
     */
    private List<DeviceBasicInfoResult> queryLocalDeviceList() {
        return manager.getDaoSession()
                .getDeviceBasicInfoResultDao()
                .queryBuilder()
                .where(DeviceBasicInfoResultDao.Properties.GpsLocation.notEq(""),
                        DeviceBasicInfoResultDao.Properties.Account.isNotNull(),
                        DeviceBasicInfoResultDao.Properties.Account.eq(MCloudApp.getAccount()))
                .list();
    }


    public void showConnectionView(String str) {
        mLlConnection.setVisibility(View.VISIBLE);
        mTvConnection.setText(str);
    }


    public void hidingConnectionView() {
        mLlConnection.setVisibility(View.GONE);
    }


    /**
     * 检查是否打开系统位置服务，如果开启了，接着检查是否授予 APP 定位权限
     */
    private void checkPermissionForGPS() {
        if (isGPSOPen(this)) {
            checkPermissionForLocation();

        } else {
            showGPSSettingDialog();
        }
    }


    /**
     * 检查是否授予 APP 定位权限
     */
    private void checkPermissionForLocation() {
        AndPermission.with(MainActivity.this)
                .runtime()
                .permission(locationNeedPermissions)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        mLocationClient.startLocation();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {
                        if (AndPermission.hasAlwaysDeniedPermission(MainActivity.this, permissions)) {
                            XPermissionUtils.showRefusePermissionDialog(MainActivity.this, GlobalUtil.getString(R.string.message_permission_location_rationale));
                        }
                    }
                })
                .start();
    }

    private void showGPSSettingDialog() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(MainActivity.this)
                .title("权限申请").content(getResources().getString(R.string.permission_request_location_hardware))
                .negativeText("暂不开启")
                .positiveText("去设置")
                .negativeColor(getResources().getColor(R.color.font_main))
                .positiveColor(getResources().getColor(R.color.colorPrimary))
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, PERMISSION_CODE_GPS);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                });

        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }


    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(true);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        mOption.setGeoLanguage(AMapLocationClientOption.GeoLanguage.DEFAULT);//可选，设置逆地理信息的语言，默认值为默认语言（根据所在地区选择语言）
        return mOption;
    }

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    public static final boolean isGPSOPen(final Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }

        return false;
    }

    private void scanResult(String result) {
        if (TextUtils.isEmpty(result)) {
            showTipDialog("请扫码正确的设备二维码");
            return;
        }

        if (result.contains("=")) {
            String results = result.substring(result.indexOf("=") + 1);
            scan(results);
        } else {
            scan(result);
        }
    }


    /**
     * 处理扫描结果，例如：MEDO,189150L,DAS
     */
    private void scan(String deviceInfo) {
        if (!deviceInfo.startsWith("MEDO")) {
            showTipDialog("请扫码正确的设备二维码");
            return;
        }

        String[] localData = deviceInfo.split(",");
        if (localData.length != 3) {
            showTipDialog("请扫码正确的设备二维码");
            return;
        }

        if (TextUtils.isEmpty(localData[0]) || TextUtils.isEmpty(localData[1]) || TextUtils.isEmpty(localData[2])) {
            showTipDialog("二维码信息不能为空");
            return;
        }

        if (localData[1].length() != 7) {
            showTipDialog("设备标识有误,请扫码正确的设备二维码");
            return;
        }

        if (!DeviceTypeEnum.value(localData[2])) {
            showTipDialog("此设备类型暂时不支持");
            return;
        }

        MCloudApp.setCurDeviceToken(localData[1]);
        MCloudApp.setCurDeviceMacAddr(null);

        if (localData[2].equals("DAS")) {
            ConfigDASActivity.startActivity(MainActivity.this, deviceInfo);

        } else if (localData[2].equals("ADME")) {
            ConfigADMEActivity.startActivity(MainActivity.this, deviceInfo);

        } else if (localData[2].equals("E60")) {
            DeviceBasicInfoResult deviceBasicInfoResult = new DeviceBasicInfoResult();
            deviceBasicInfoResult.setDeviceToken(localData[2]);
            deviceBasicInfoResult.setDeviceTypeName(localData[1]);
            ConfigE60Activity.startActivity(MainActivity.this, deviceBasicInfoResult);
        }

        //如果是新设备，添加到本地数据库并标记在地图上
        String[] device = deviceInfo.split(",");
        addDeviceOnMap(device);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PERMISSION_CODE_GPS:
                if (isGPSOPen(MainActivity.this)) {
//                    mLocationClient.startLocation();
                    aMap.setMyLocationStyle(myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE));
                }
                break;

            case XPermissionUtils.REQUEST_CODE_OPEN_APPLICATION_SETTING:
                if (AndPermission.hasPermissions(this, locationNeedPermissions)) {
                    //刷新定位
//                    mLocationClient.startLocation();
                    aMap.setMyLocationStyle(myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE));
                }
                break;

            case REQUEST_CODE_SCAN:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        String content = data.getStringExtra(ScanActivity.CODED_CONTENT);
                        Timber.d("扫描结果为：" + content);
                        scanResult(content);
                    }
                }
                break;
        }
    }


    //声明一个long类型变量：用于存放上一点击“返回键”的时刻
    private long mExitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //判断用户是否点击了“返回键”
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //与上次点击返回键时刻作差
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                //大于2000ms则认为是误操作，使用Toast进行提示
                Toast toast = Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                //并记录下本次点击“返回键”的时刻，以便下次进行判断
                mExitTime = System.currentTimeMillis();
            } else {
                //小于2000ms则认为是用户确实希望退出程序-调用System.exit()方法进行退出
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
