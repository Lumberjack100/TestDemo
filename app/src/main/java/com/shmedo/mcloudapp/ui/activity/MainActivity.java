package com.shmedo.mcloudapp.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MyLocationStyle;
import com.github.clans.fab.FloatingActionButton;
import com.google.gson.reflect.TypeToken;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.InfoWinAdapter;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.bluetooth.BluetoothDeviceFindEventData;
import com.shmedo.mcloudapp.bluetooth.BluetoothEvent;
import com.shmedo.mcloudapp.bluetooth.BluetoothEventHandler;
import com.shmedo.mcloudapp.bluetooth.MdBluetoothManager;
import com.shmedo.mcloudapp.bluetooth.Message;
import com.shmedo.mcloudapp.entity.DeviceBasicInfoResult;
import com.shmedo.mcloudapp.entity.DeviceBasicInfoResultDao;
import com.shmedo.mcloudapp.entity.StatusInfoResult;
import com.shmedo.mcloudapp.entity.ble.MDevice;
import com.shmedo.mcloudapp.entity.cluster.ClusterAnotherClickListener;
import com.shmedo.mcloudapp.entity.cluster.ClusterAnotherRender;
import com.shmedo.mcloudapp.entity.cluster.ClusterItem;
import com.shmedo.mcloudapp.entity.cluster.ClusterItemImp;
import com.shmedo.mcloudapp.entity.cluster.ClusterOverlayMerchant;
import com.shmedo.mcloudapp.entity.event.MapDeviceEvent;
import com.shmedo.mcloudapp.entity.event.WifiEvent;
import com.shmedo.mcloudapp.entity.parameter.LocationResult;
import com.shmedo.mcloudapp.model.BaseObserver;
import com.shmedo.mcloudapp.model.MDRetrofit;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.DensityUtil;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.StartActivityUtil;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.util.XPermissionUtils;
import com.shmedo.mcloudapp.util.bleutil.ByteManagerUtil;
import com.shmedo.mcloudapp.util.common.MapManagerUtil;
import com.shmedo.mcloudapp.views.HintDialog;
import com.shmedo.mcloudapp.views.LoadingDialog;

import org.greenrobot.eventbus.EventBus;
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

import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_CHARACTERISTICS_FIND_FAIL;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_CONNECT;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_DISCONNECTED;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_ENABLE_READ_FAIL;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_ENABLE_READ_SUCCESS;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_MESSAGE_WRITE_FAIL;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_MESSAGE_WRITE_SUCCESS;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_RECOVERY_SUCCESS;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_REQUEST_MTU_FAIL;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_SERVICE_FIND_FAIL;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_WRITE_TIME_OUT;
import static com.shmedo.mcloudapp.util.bleutil.Constants.MESSAGE_RESPONSE_REBOOT_DEVICE;
import static com.shmedo.mcloudapp.util.bleutil.Constants.MESSAGE_RESPONSE_SAVE_SETTINGS_SUCCESS;
import static com.shmedo.mcloudapp.util.bleutil.Constants.MESSAGE_RESPONSE_TIME_OUT;
import static com.shmedo.mcloudapp.util.bleutil.Constants.REFRESH_RUN_STATE;
import static com.shmedo.mcloudapp.util.bleutil.Constants.VERIFY_RESULT;

public class MainActivity extends BaseActivity implements
        LocationSource, AMapLocationListener {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.img_user)
    ImageView mImgUser;

    @BindView(R.id.main_titile)
    TextView mMainTitile;

    @BindView(R.id.img_equipment)
    ImageView mImgEquipment;

    @BindView(R.id.map)
    MapView mMapView;

    @BindView(R.id.fab_add)
    FloatingActionButton mFabAdd;

    @BindView(R.id.fab_config)
    FloatingActionButton mFabConfig;

    @BindView(R.id.fab_location)
    FloatingActionButton mFabLocation;

    @BindView(R.id.fab_refresh)
    FloatingActionButton mFabRefresh;

    @BindView(R.id.tv_connection)
    TextView mTvConnection;

    @BindView(R.id.ll_connection)
    LinearLayout mLlConnection;

    @BindView(R.id.RL_scan)
    RelativeLayout mRLScan;

    @BindView(R.id.img_scan)
    ImageView mImgScan;

    private AMap aMap; //初始化地图控制器对象
    private UiSettings mUiSettings;//定义一个UiSettings对象
    private MyLocationStyle myLocationStyle;
    private InfoWinAdapter adapter;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private LatLng myLatLng;
    private boolean followMove = true;

    private LoadingDialog mLoadingDialog;
    private MaterialDialog mMaterialDialog;
    private MaterialDialog.Builder mBuilder;
    private DaoManager manager = DaoManager.getInstance();

    private BluetoothAdapter mBluetoothAdapter;
    private MdBluetoothManager mdBluetoothManager;

    private List<MDevice> list = new ArrayList<>();
    private Handler hander;
    private String currentMessageId = "";

    private boolean isBlueModle = true;//当前模式是否是蓝牙模式
    private static boolean isConneted = false;//蓝牙是否已连接
    private static boolean autoOpenBt = false;
    private List<ClusterItem> clusterItemsMerchant = new ArrayList<>();
    private ClusterOverlayMerchant clusterOverlayMerchant;
    private Map<Integer, Drawable> mBackDrawAblesMerchant = new HashMap<Integer, Drawable>();
    private int clusterRadius = 48;

    private static final int REQUEST_ENABLE_BT = 2;



    private Runnable dismssDialogRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();

                mdBluetoothManager.stopScan();
                if (list.isEmpty()) {
                    ToastUtil.showShortToast("未发现设备，请尝试重新扫描");
                    return;
                }

                if (!autoOpenBt) {
                    BlueToothListActivity.startActivity(MainActivity.this, list);
                }
            }
        }
    };


    @Override
    protected int initContentView() {
        return R.layout.activity_main;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        initData();
        hidingConnectionView();
        initBluetooth();

        //获取地图要加载的数据
        getDeviceBasicInfoList("1");

        XPermissionUtils.requestPermissionsResult(this, 200, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        initMap();
                    }

                    @Override
                    public void onPermissionDenied() {
                        XPermissionUtils.showRefusePermissionDialog(MainActivity.this,
                                getResources().getString(R.string.permission_request_location));
                    }
                });
    }


    private void initData() {
        mLoadingDialog = new LoadingDialog(this);
        manager.init(this);
        hander = new Handler();
    }


    //初始化地图信息
    private void initMap() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            mUiSettings = aMap.getUiSettings();//实例化UiSettings类对象
        }
        mUiSettings.setZoomControlsEnabled(false); //隐藏缩放控件
        mUiSettings.setMyLocationButtonEnabled(false);//设置默认定位按钮是否显示，非必需设置。
        mUiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_RIGHT);//设置logo位置

        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
        //myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.strokeColor(getResources().getColor(R.color.app_color_blue_2));// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(100, 29, 161, 242));// 设置圆形的填充颜色
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);//定位一次，且将视角移动到地图中心点。
        // myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.showMyLocation(true);

        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        //aMap.setLocationSource(this);// 设置定位资源。如果不设置此定位资源则定位按钮不可点击。并且实现activate激活定位,停止定位的回调方法
        aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                myLatLng = new LatLng(latitude, longitude);
                if (followMove) {
                    aMap.animateCamera(CameraUpdateFactory.newLatLng(myLatLng));
                }
            }
        });

        aMap.setOnMapTouchListener(new AMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                followMove = false;
            }
        });

        addMerchantClustersToMap(queryDeviceInfoList(""));
    }


    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (null == mLocationClient) {
            //初始化定位
            mLocationClient = new AMapLocationClient(this);
            //初始化AMapLocationClientOption对象
            mLocationOption = new AMapLocationClientOption();
            //设置定位回调监听
            mLocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setOnceLocation(true);
            //设置是否返回地址信息（默认返回地址信息）
            mLocationOption.setNeedAddress(true);
            //resetOption();
            mLocationClient.setLocationOption(mLocationOption);
            //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
            //mLocationClient.stopLocation();
            mLocationClient.startLocation();
        }

    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (mListener != null) {
                // aMap.clear();  清除之前的marker
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点-我的位置
            }

            if (aMapLocation.getErrorCode() == 0) {
                myLatLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 8));
                String city = aMapLocation.getCity();
                String address = aMapLocation.getAddress();
                //addMarkerToMap(latLng,city,address);
                Timber.d("city=" + city + ",address=" + address);
            } else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Timber.e("location Error, ErrCode:" + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }

    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }


    /**
     * 获取设备信息列表
     *
     * @param currentCompanyID
     */
    private void getDeviceBasicInfoList(String currentCompanyID) {
        RequestBody body = RequestBody.create(CommonVariable.JSON_TYPE, currentCompanyID);
        MDRetrofit.getInstance()
                .createService()
                .QueryDeviceBasicInfoList(CommonVariable.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<DeviceBasicInfoResult>>() {

                    @Override
                    public void Success(List<DeviceBasicInfoResult> infoList, String message) {
                        mLoadingDialog.dismiss();

                        if (null != infoList && infoList.size() != 0) {
                            for (DeviceBasicInfoResult deviceBasicInfoResult : infoList) {
                                deviceBasicInfoResult.setAccount(CommonVariable.getAccount());
                            }
                            manager.getDaoSession().getDeviceBasicInfoResultDao().insertOrReplaceInTx(infoList);
                        }
                    }

                    @Override
                    public void Failure(String message) {
                        mLoadingDialog.dismiss();
                        Timber.w("服务器连接失败--" + message);
                    }
                });
    }


    /**
     * 查询位置信息不为空的设备
     *
     * @param gpsLocation
     * @return
     */
    private List<DeviceBasicInfoResult> queryDeviceInfoList(String gpsLocation) {
        return manager.getDaoSession()
                .getDeviceBasicInfoResultDao()
                .queryBuilder()
                .where(DeviceBasicInfoResultDao.Properties.GpsLocation.notEq(gpsLocation), DeviceBasicInfoResultDao.Properties.Account.eq(CommonVariable.getAccount()))
                .list();
    }

    //添加设备的 marker 点
    private void addMerchantClustersToMap(final List<DeviceBasicInfoResult> deviceList) {
        LatLng latLng = null;
        for (int i = 0; i < deviceList.size(); i++) {
            LocationResult location = GsonFactory.getGson()
                    .fromJson(deviceList.get(i).getInstallLocation(), new TypeToken<LocationResult>() {
                    }.getType());
            latLng = new LatLng(location.getLat(), location.getLng());
            ClusterItemImp clusterImp = new ClusterItemImp(latLng, deviceList.get(i).getDeviceName());
            clusterItemsMerchant.add(clusterImp);
        }

        if (clusterOverlayMerchant == null) {
            clusterOverlayMerchant = new ClusterOverlayMerchant(aMap, clusterItemsMerchant,
                    DensityUtil.Dp2Px(getApplicationContext(), clusterRadius), getApplicationContext());
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

    @OnClick({R.id.img_user, R.id.img_equipment, R.id.RL_scan,
            R.id.fab_add, R.id.fab_config, R.id.fab_location, R.id.fab_refresh})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_user://用户信息
                StartActivityUtil.comeOnBaby(this, UserInfoActivity.class);
                break;

            case R.id.img_equipment://设备管理
                StartActivityUtil.comeOnBaby(this, DeviceManageActivity.class);
                break;

            case R.id.RL_scan: //扫一扫
                ScanAddDeviceActivity.startActivity(this, myLatLng);
                break;

            case R.id.fab_add://添加
                chooseModel();
                break;

            case R.id.fab_config://配置
                showResultDialog("米易通App远程配置功能开发中...");
                break;

            case R.id.fab_location://定位

                if (myLatLng != null) {
                    Timber.d("latitude=" + myLatLng.latitude + ",longitude=" + myLatLng.longitude);
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(myLatLng));
                }
                break;

            case R.id.fab_refresh:
                ToastUtil.showShortToast("===fab_refresh");
                break;
        }
    }


    //搜索蓝牙或者搜索wifi
    private void chooseModel() {
        View view = getLayoutInflater().inflate(R.layout.choose_menu, null);
        LinearLayout bluetooth = view.findViewById(R.id.search_bluetooth);
        LinearLayout wifi = view.findViewById(R.id.search_wifi);
        LinearLayout cloud = view.findViewById(R.id.search_cloud);
        final MaterialDialog mDialog = new MaterialDialog.Builder(this)
                .cancelable(true)
                .title("请选择")
                .customView(view, true)
                .show();
        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                if (isBlueModle && !isConneted) {
                    disconnectDevice();
                    startDiscoveryDevice();

                    if (null != mBluetoothAdapter && mBluetoothAdapter.isEnabled()) {

                        if (null == mLoadingDialog.getDialog() || !mLoadingDialog.getDialog().isShowing()) {
                            mLoadingDialog.showCancelDialog("正在获取附近的蓝牙设备...");
                            hander.postDelayed(dismssDialogRunnable, 10000);
                        }
                    }
                } else if (isBlueModle && isConneted) {
                    showChangeModle(getResources().getString(R.string.disconnect_bluetooth_device));
                }
            }
        });
        wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                XPermissionUtils.requestPermissionsResult(MainActivity.this, 200, new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        },
                        new XPermissionUtils.OnPermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                Intent intent = new Intent(MainActivity.this, WifiConnectionActivity.class);
                                startActivity(intent);
                            }

                            @Override
                            public void onPermissionDenied() {
                                XPermissionUtils.showRefusePermissionDialog(MainActivity.this,
                                        getResources().getString(R.string.permission_request_location));
                            }
                        });

            }
        });
        cloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtil.showShortToast("云端数据");
            }
        });

    }

    /**
     * 初始化蓝牙
     */
    private void initBluetooth() {

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        MdBluetoothManager.init(mBluetoothAdapter, bluetoothManager);
        mdBluetoothManager = MdBluetoothManager.getInstance();
        mdBluetoothManager.setEventHandler(new MdBluetoothEventHandler());
    }


    /**
     * 扫描蓝牙设备，主要用来判断要连接的设备是否能被搜索到
     */
    private void startDiscoveryDevice() {
        //未打开蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        if (null != list && list.size() > 0) {
            list.clear();
        }
        mdBluetoothManager.scanDevice(20, this);
    }


    private class MdBluetoothEventHandler implements BluetoothEventHandler {
        @Override
        public void handle(final BluetoothEvent event) {
            switch (event.getEventType()) {
                case DEVICE_FIND:
                    handleDeviceFind((BluetoothDeviceFindEventData) event.getEventData());
                    break;
                case CONNECTED: {
                    //ByteManagerUtil.init(new MyOnBytePackage());
                    mHandler.sendEmptyMessage(BT_CONNECT);
                    break;
                }
                case DISCONNECTED:
                    mHandler.sendEmptyMessage(BT_DISCONNECTED);

                    break;
                case REQUEST_MTU_FAIL: {
                    Timber.d("MTU请求设置失败");
                    mHandler.sendEmptyMessage(BT_REQUEST_MTU_FAIL);
                    break;
                }
                case SERVICE_FIND_FAIL: {
                    Timber.d("蓝牙服务发现失败");
                    mHandler.sendEmptyMessage(BT_SERVICE_FIND_FAIL);
                    break;
                }
                case CHARACTERISTICS_FIND_FAIL: {
                    Timber.d("特征读取失败");
                    mHandler.sendEmptyMessage(BT_CHARACTERISTICS_FIND_FAIL);
                    break;
                }
                case ENABLE_READ_SUCCESS: {
                    Timber.d("设置读取Descriptor成功");
                    mHandler.sendEmptyMessage(BT_ENABLE_READ_SUCCESS);
                    break;
                }
                case ENABLE_READ_FAIL: {
                    Timber.d("设置读取Descriptor失败");
                    mHandler.sendEmptyMessage(BT_ENABLE_READ_FAIL);
                    break;
                }
                case WRITE_TIME_OUT: {
                    Timber.d("写入等待超时");
                    disconnectDevice();
                    mHandler.sendEmptyMessage(BT_WRITE_TIME_OUT);
                    break;
                }
                case MESSAGE_WRITE_SUCCESS: {
                    Timber.d("消息写入成功");
                    mHandler.sendEmptyMessage(BT_MESSAGE_WRITE_SUCCESS);
                    try {
                        currentMessageId = ((Message) event.getEventData()).getMessageID();
                        Timber.d("消息id===" + currentMessageId);
                        String msg = ((Message) event.getEventData()).getResponseMessage();
                        byte[] data = (byte[]) msg.getBytes();

                        if (data != null && data.length > 0) {
                            ByteManagerUtil.getInstance().writeByte(data);
                        }

                    } catch (Exception ex) {
                        Timber.e(ex);
                    }

                    break;
                }
                case MESSAGE_RESPONSE_TIME_OUT:
                    Timber.w("消息等待响应超时");
                    mHandler.sendEmptyMessage(MESSAGE_RESPONSE_TIME_OUT);
                    disconnectDevice();
                    break;
                case MESSAGE_WRITE_FAIL: {
                    Timber.w("消息写入失败");
                    mHandler.sendEmptyMessage(BT_MESSAGE_WRITE_FAIL);
                    break;
                }
                case RESPONSE_WITH_NO_MESSAGE: {
                    try {
                        byte[] data = (byte[]) event.getEventData();
                        if (data != null && data.length > 0) {
                            ByteManagerUtil.getInstance().writeByte(data);
                        }

                    } catch (Exception ex) {
                        Timber.e(ex);
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }


    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case BT_CONNECT:
                    Timber.d("蓝牙连接成功");
                    ToastUtil.showShortToast("蓝牙已连接");

                    if (mLoadingDialog != null) {
                        mLoadingDialog.dismiss();
                    }
                    isConneted = true;
                    //startBluAuthenticate();//蓝牙连接成功开始进行验证
                    hander.removeCallbacks(dismssDialogRunnable);
                    break;

                case BT_DISCONNECTED:
                    if (isConneted) {
                        isConneted = false;
                        if (!isBlueModle) {
                            //mMenu.findItem(R.id.current_blu)
                            //    .setIcon(R.drawable.bar_item_offline);
                        } else {
                            //mMenu.findItem(R.id.current_blu)
                            //    .setIcon(R.drawable.bar_item_bt);
                        }
                        Timber.d("蓝牙连接已断开");
                        ToastUtil.showShortToast("蓝牙连接已断开!");

                        disconnectDevice();//非手动断开，清除蓝牙数据
                    }
                    break;

                case BT_MESSAGE_WRITE_SUCCESS:
                    ToastUtil.showShortToast("已发送指令");
                    break;

                case BT_MESSAGE_WRITE_FAIL:
                    ToastUtil.showShortToast("发送指令失败");
                    break;

                case BT_WRITE_TIME_OUT:
                    ToastUtil.showShortToast("发送指令超时");
                    break;

                case VERIFY_RESULT:
                    if (msg.obj.equals("1")) {
                        ToastUtil.showShortToast("蓝牙认证通过!");

                        //sendDeviceStateComd();
                    } else {
                        ToastUtil.showShortToast("蓝牙认证失败!");

                        try {
                            Thread.sleep(1000);
                            disconnectDevice();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                    break;

                case MESSAGE_RESPONSE_TIME_OUT:
                    ToastUtil.showShortToast("消息等待响应超时！");
                    break;

                case REFRESH_RUN_STATE:
                    //loadWebView(runState);
                    break;

                case MESSAGE_RESPONSE_SAVE_SETTINGS_SUCCESS:
                    ToastUtil.showShortToast("设置信息已保存！");
                    //mWebView.loadUrl("javascript:restart()");
                    break;

                case BT_REQUEST_MTU_FAIL:
                    ToastUtil.showShortToast("MTU请求设置失败！");
                    break;

                case BT_SERVICE_FIND_FAIL:
                    ToastUtil.showShortToast("蓝牙服务发现失败！");
                    break;

                case BT_CHARACTERISTICS_FIND_FAIL:
                    ToastUtil.showShortToast("蓝牙特征读取失败！");
                    break;

                case BT_ENABLE_READ_FAIL:
                    ToastUtil.showShortToast("设置读取Descriptor失败！");
                    break;

                case BT_RECOVERY_SUCCESS:
                    ToastUtil.showShortToast("已恢复出厂设置！");
                    break;

                case MESSAGE_RESPONSE_REBOOT_DEVICE:
                    ToastUtil.showShortToast("已重启系统！");
                    break;

                default:
                    break;
            }

            return false;
        }
    });


    private void handleDeviceFind(BluetoothDeviceFindEventData eventData) {

        if (list.contains(eventData.getNewDevice()) || eventData.getNewDevice().getDevice().getName() == null) {
            return;
        }

        if (null != list && list.size() > 0) {
            for (MDevice mDevice : list) {
                if (eventData.getNewDevice()
                        .getDevice()
                        .getName()
                        .equals(mDevice.getDevice().getName())) {
                    return;
                }
            }
        }
        list.add(eventData.getNewDevice());
    }

    /**
     * ble 取消连接
     */
    private void disconnectDevice() {
        if (null != MdBluetoothManager.getInstance()) {
            MdBluetoothManager.getInstance().disconnect();
        }
    }


    private void showResultDialog(String content) {
        mBuilder = new MaterialDialog.Builder(this);
        mBuilder.title("温馨提示：")
                .content(content)
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
                .positiveText("确定");
        //.negativeText("取消");
        mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();

        EventBus.getDefault().unregister(this);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // 判断蓝牙是否启用
                if (resultCode == Activity.RESULT_OK) {

                    startDiscoveryDevice();

                } else {
                    ToastUtil.showShortToast("蓝牙未启用");
                }
                break;

            default:
                break;
        }
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

    //添加设备名称
    private void addDeviceOnMap(String[] device) {

        //返回的数据有这个值18A095L
        if (null != queryDeviceInList(device[1])) {
            ToastUtil.showShortToast("此设备已存在！");
            return;
        }

        LatLng latLng = myLatLng;
        String installLocation = GsonFactory.getGson().toJson(latLng);
        MapManagerUtil.addMarkerToMap(aMap, latLng, device[2], device[1]);
        DeviceBasicInfoResult result = new DeviceBasicInfoResult();
        Long proId = System.currentTimeMillis();
        result.setId(proId);
        result.setDeviceName(device[1]);
        result.setDeviceToken(device[1]);
        result.setDeviceTypeID(0);
        result.setDeviceTypeName(device[2]);
        result.setGpsLocation(null);
        result.setInstallLocation(installLocation);
        result.setSecurityNO(null);
        result.setAccount(CommonVariable.getAccount());
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
        infoResult.setAccount(CommonVariable.getAccount());
        infoResult.setLocal(true);

        manager.getDaoSession().getDeviceBasicInfoResultDao().insertOrReplaceInTx(result);
        manager.getDaoSession().getStatusInfoResultDao().insertOrReplaceInTx(infoResult);
    }

    //查询设备列表中是否有这个设备
    private DeviceBasicInfoResult queryDeviceInList(String deviceName) {
        return manager.getDaoSession().getDeviceBasicInfoResultDao()
                .queryBuilder()
                .where(DeviceBasicInfoResultDao.Properties.DeviceName.eq(deviceName))
                .unique();
    }

    public void showConnectionView(String str) {
        mLlConnection.setVisibility(View.VISIBLE);
        mTvConnection.setText(str);
    }

    public void hidingConnectionView() {
        mLlConnection.setVisibility(View.GONE);
    }

    /**
     * 是否切换连接模式
     */
    private void showChangeModle(String content) {
        mBuilder = new MaterialDialog.Builder(this);
        mBuilder.title("温馨提示：")
                .content(content)
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消");
        mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
        mBuilder.onAny(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                if (which == DialogAction.POSITIVE) {
                    isConneted = false;
                    disconnectDevice();
                    mMaterialDialog.dismiss();

                } else if (which == DialogAction.NEGATIVE) {
                    mMaterialDialog.dismiss();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        new HintDialog.Builder(this)
                .setTitle("提示")
                .setMessage("你确定要退出吗？")
                .setConfirmBtnListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                        System.exit(0);
                        //Process.killProcess(Process.myPid());
                    }
                }).onCreate().show();
    }
}
