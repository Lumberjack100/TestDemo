package com.shmedo.mcloudapp.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.OnClick;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationQualityReport;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.github.clans.fab.FloatingActionButton;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.InfoWinAdapter;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.bluetooth.BluetoothDeviceFindEventData;
import com.shmedo.mcloudapp.bluetooth.BluetoothEvent;
import com.shmedo.mcloudapp.bluetooth.BluetoothEventHandler;
import com.shmedo.mcloudapp.bluetooth.MdBluetoothManager;
import com.shmedo.mcloudapp.bluetooth.Message;
import com.shmedo.mcloudapp.entity.ble.MDevice;
import com.shmedo.mcloudapp.util.StartActivityUtil;
import com.shmedo.mcloudapp.util.TimeUtil;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.util.XPermissionUtils;
import com.shmedo.mcloudapp.util.bleutil.ByteManagerUtil;
import com.shmedo.mcloudapp.util.bleutil.LogTag;
import com.shmedo.mcloudapp.views.LoadingDialog;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.shmedo.mcloudapp.util.bleutil.Constants.*;

public class MainActivity extends BaseActivity implements AMap.OnMapClickListener, AMap.OnMarkerClickListener, AMapLocationListener,
    LocationSource {

    @BindView(R.id.img_user) ImageView mImgUser;
    @BindView(R.id.main_titile) TextView mMainTitile;
    @BindView(R.id.img_equipment) ImageView mImgEquipment;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.map) MapView mMapView;
    @BindView(R.id.RL_scan) RelativeLayout mRLScan;
    @BindView(R.id.fab_add) FloatingActionButton mFabAdd;
    @BindView(R.id.fab_config) FloatingActionButton mFabConfig;
    @BindView(R.id.fab_location) FloatingActionButton mFabLocation;
    @BindView(R.id.fab_refresh) FloatingActionButton mFabRefresh;

    //初始化地图控制器对象
    private AMap aMap;
    private MyLocationStyle myLocationStyle;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;
    private UiSettings mUiSettings;//定义一个UiSettings对象
    private boolean followMove = true;
    private  LatLng myLatLng;
    private InfoWinAdapter adapter;
    private Marker oldMarker;
    private OnLocationChangedListener mListener;

    private LoadingDialog mLoadingDialog;
    private MaterialDialog mMaterialDialog;
    private MaterialDialog.Builder mBuilder;

    private BluetoothAdapter mBluetoothAdapter;
    private MdBluetoothManager mdBluetoothManager;
    private List<MDevice> list = new ArrayList<>();
    private boolean isShowingDialog = false;
    private Handler hander;
    private String currentMessageId ="";
    private String SN = "";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    //当前模式是否是蓝牙模式
    private boolean isBluModle = true;
    //蓝牙是否已连接
    private boolean isConneted = false;
    private boolean autoOpenBt;

    private Runnable dismssDialogRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();

                initBlueAdapter();
            }
            // disconnectDevice();
        }
    };
    private Runnable dismssConDialogRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
        }
    };

    @Override protected int initContentView() {
        return R.layout.activity_main;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        XPermissionUtils.requestPermissionsResult(this, 200, new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION },
            new XPermissionUtils.OnPermissionListener() {
                @Override
                public void onPermissionGranted() {
                    initMap();
                }
                @Override
                public void onPermissionDenied() {
                    LoadingDialog.showRefusePermissionDialog(MainActivity.this,
                        "在设置-应用管理-米易通-权限中开启相机权限");
                }
            });
            initData();
    }


    private void initData() {
        mLoadingDialog = new LoadingDialog(this);
        hander = new Handler();
    }


    //初始化地图信息
    private void initMap() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            mUiSettings = aMap.getUiSettings();//实例化UiSettings类对象
        }
        adapter = new InfoWinAdapter();
        aMap.setInfoWindowAdapter(adapter);
        mUiSettings.setZoomControlsEnabled(false); //隐藏缩放控件
        mUiSettings.setMyLocationButtonEnabled(false);//设置默认定位按钮是否显示，非必需设置。
        mUiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_RIGHT);//设置logo位置

        //设置地图的放缩级别
        //aMap.moveCamera(CameraUpdateFactory.zoomTo(13));
        myLocationStyle = new MyLocationStyle();
        //myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        //aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        //myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.mylocation));// 设置小蓝点的图标
        myLocationStyle.strokeColor(getResources().getColor(R.color.app_color_blue_2));// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(100, 29, 161, 242));// 设置圆形的填充颜色
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);//定位一次，且将视角移动到地图中心点。
        myLocationStyle.showMyLocation(true);//设置是否显示定位小蓝点，用于满足只想使用定位，不想使用定位小蓝点的场景，设置false以后图面上不再有定位蓝点的概念，但是会持续回调位置信息。
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(this);// 设置定位资源。如果不设置此定位资源则定位按钮不可点击。并且实现activate激活定位,停止定位的回调方法
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false

        //myLocationStyle.anchor(0.0F,1.0F);
        aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override public void onMyLocationChange(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                myLatLng =new LatLng(latitude,longitude);
                if(followMove){
                  aMap.animateCamera(CameraUpdateFactory.newLatLng(myLatLng));
                }
            }
        });
        aMap.setOnMapTouchListener(new AMap.OnMapTouchListener() {
            @Override public void onTouch(MotionEvent motionEvent) {
                followMove = false;
            }
        });
        initMarker();
    }




    // 根据控件的选择，重新设置定位参数
    private void resetOption() {
            // 设置是否需要显示地址信息
            mLocationOption.setNeedAddress(true);
            /**
             * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
             * 注意：只有在高精度模式下的单次定位有效，其他方式无效
             */
            mLocationOption.setGpsFirst(true);
            // 设置是否开启缓存
            mLocationOption.setLocationCacheEnable(false);
            // 设置是否单次定位
            mLocationOption.setOnceLocation(true);
            //设置是否等待设备wifi刷新，如果设置为true,会自动变为单次定位，持续定位时不要使用
            mLocationOption.setOnceLocationLatest(true);
            //设置是否使用传感器
            mLocationOption.setSensorEnable(false);
            //设置是否开启wifi扫描，如果设置为false时同时会停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差

            // 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
            mLocationOption.setInterval(10000);
            // 设置网络请求超时时间
            mLocationOption.setHttpTimeOut(1000);
    }

    private void initMarker(){
        LatLng latLng = new LatLng(31.0964540159,121.591186523);
        LatLng latLng2 = new LatLng(31.1718317,121.65444374);
        addMarkerToMap(latLng,"das","DAS");
        addMarkerToMap(latLng2,"上海","上海市浦东新区");

        // 绑定 Marker 被点击事件
        //aMap.setOnMarkerClickListener(this);
        aMap.setOnMapClickListener(this);
    }


    @Override public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if(mListener != null){
                //                aMap.clear();  清除之前的marker
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点-我的位置
            }

            if (aMapLocation.getErrorCode() == 0) {
                myLatLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 14));
                String city = aMapLocation.getCity();
                String address = aMapLocation.getAddress();
                //                addMarkerToMap(latLng,city,address);
            } else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:" + aMapLocation.getErrorCode() + ", errInfo:"
                    + aMapLocation.getErrorInfo());
            }
        }
    }

    //地图的点击事件
    @Override public void onMapClick(LatLng latLng) {
        Log.i("adu","地图的点击事件");
        //点击地图上没marker 的地方，隐藏inforwindow
        //if (oldMarker != null) {
            oldMarker.hideInfoWindow();
            //oldMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_normal));
        //}
    }

    // 定义 Marker 点击事件监听
    @Override public boolean onMarkerClick(Marker marker) {
        Log.i("adu","--Marker 点击事件监听--"+marker.getPosition().equals(myLatLng));
        if (!marker.getPosition().equals(myLatLng)){ //点击的marker不是自己位置的那个marker
            if (oldMarker != null) {
                //oldMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_normal));
                oldMarker.showInfoWindow();
            }
            //oldMarker = marker;
            //marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_selected));
        }else {
            if (oldMarker != null) {
                oldMarker.hideInfoWindow();
            }
        }

        return false; //返回 “false”，除定义的操作之外，默认操作也将会被执行
    }

    //激活定位
    //记得注册定位
    //<service android:name="com.amap.api.location.APSService"/>
    @Override public void activate(OnLocationChangedListener onLocationChangedListener) {
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

    //停止定位
    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    //添加marker
    private void addMarkerToMap(LatLng latLng, String title, String snippet) {
        aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
            .position(latLng)
            .title(title)
            .snippet(snippet)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marker))
        );
    }

    @OnClick({ R.id.img_user, R.id.img_equipment, R.id.RL_scan,
                 R.id.fab_add, R.id.fab_config, R.id.fab_location, R.id.fab_refresh})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_user:
                StartActivityUtil.comeOnBaby(this, UserInfoActivity.class);
                break;
            case R.id.img_equipment:
                ToastUtil.showSToast("设备");
                break;
            case R.id.RL_scan:
                StartActivityUtil.comeOnBaby(this,ScanAddDeviceActivity.class);
                break;
            case R.id.fab_add:
                 chooseModel();
                break;
            case R.id.fab_config:
                showResultDialog("米易通App远程配置功能开发中...");
                break;
            case R.id.fab_location:
                Log.i("adu","==="+myLatLng.latitude+"-"+myLatLng.longitude);
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(myLatLng));
                break;
            case R.id.fab_refresh:
                ToastUtil.showSToast("===fab_refresh");
                break;
        }
    }

    //搜索蓝牙或者搜索wifi
    private void chooseModel() {
        View view =  getLayoutInflater().inflate(R.layout.choose_menu, null);
        LinearLayout remove = view.findViewById(R.id.search_bluetooth);
        LinearLayout author = view.findViewById(R.id.search_wifi);
            final MaterialDialog mDialog = new MaterialDialog.Builder(this)
                .cancelable(false)
                .title("请选择")
                .customView(view,true)
                .show();
            remove.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    mDialog.dismiss();
                    if (isBluModle && !isConneted) {
                        disconnectDevice();
                        initBluetooth();
                        if (null != mBluetoothAdapter && mBluetoothAdapter.isEnabled()) {

                            if (null != list && list.size() > 0) {
                                list.clear();
                            }
                            if (null == mLoadingDialog.getDialog() ||
                                !mLoadingDialog.getDialog().isShowing()) {
                                mLoadingDialog.showCancelDialog("正在获取附近的蓝牙设备...");
                                hander.postDelayed(dismssDialogRunnable, 10000);
                            }

                        }
                    } else if (isBluModle && isConneted) {
                        //showChangeModle( getResources().getString(R.string.blue_model));
                    } else {
                        if (null != list && list.size() > 0) {
                            list.clear();
                        }
                        initBluetooth();
                    }
                }
            });
            author.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    mDialog.dismiss();
                    ToastUtil.showSToast("搜索wifi");
                }
            });

    }


    /**
     * 初始化蓝牙
     */
    private void initBluetooth() {

        final BluetoothManager bluetoothManager =
            (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        MdBluetoothManager.init(mBluetoothAdapter, bluetoothManager);
        mdBluetoothManager = MdBluetoothManager.getInstance();
        mdBluetoothManager.setEventHandler(new MdBluetoothEventHandler());
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
        mdBluetoothManager.scanDevice(20, this);

    }

    private void initBlueAdapter() {
        if (!autoOpenBt) {
            Intent serverIntent = new Intent(MainActivity.this, BlueToothListActivity.class);
            serverIntent.putExtra("devlist", (Serializable) list);
            startActivityForResult(serverIntent, REQUEST_ENABLE_BT);
        }else {
            //自动连接
            int temp=0;
            for (int i = 0; i < list.size(); i++) {
                if ((list.get(i).getDevice().getName()).contains(SN)){
                    mdBluetoothManager.connectDevice(list.get(i).getDevice(), MainActivity.this);
                    if (null != mLoadingDialog) {
                        mLoadingDialog.showNoCancelDialog("正在连接...");
                    }
                    hander.postDelayed(dismssConDialogRunnable, 20000);
                    break;

                }else {
                    temp++;
                }
            }
            if (temp==list.size()){
                Intent serverIntent = new Intent(MainActivity.this, BlueToothListActivity.class);
                serverIntent.putExtra("devlist", (Serializable) list);
                startActivityForResult(serverIntent, REQUEST_ENABLE_BT);
            }

        }
        mdBluetoothManager.stopScan();

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
                    Log.e(LogTag.ERROR_TAG, "MTU请求设置失败");
                    mHandler.sendEmptyMessage(BT_REQUEST_MTU_FAIL);
                    break;
                }
                case SERVICE_FIND_FAIL: {
                    Log.e(LogTag.ERROR_TAG, "蓝牙服务发现失败");
                    mHandler.sendEmptyMessage(BT_SERVICE_FIND_FAIL);
                    break;
                }
                case CHARACTERISTICS_FIND_FAIL: {
                    Log.e(LogTag.ERROR_TAG, "特征读取失败");
                    mHandler.sendEmptyMessage(BT_CHARACTERISTICS_FIND_FAIL);
                    break;
                }
                case ENABLE_READ_SUCCESS: {
                    Log.i(LogTag.INFO_TAG, "设置读取Descriptor成功");
                    mHandler.sendEmptyMessage(BT_ENABLE_READ_SUCCESS);
                    break;
                }
                case ENABLE_READ_FAIL: {
                    Log.e(LogTag.ERROR_TAG, "设置读取Descriptor失败");
                    mHandler.sendEmptyMessage(BT_ENABLE_READ_FAIL);
                    break;
                }
                case WRITE_TIME_OUT: {
                    Log.e(LogTag.ERROR_TAG, "写入等待超时");
                    disconnectDevice();
                    mHandler.sendEmptyMessage(BT_WRITE_TIME_OUT);
                    break;
                }
                case MESSAGE_WRITE_SUCCESS: {
                    Log.i(LogTag.INFO_TAG, "消息写入成功");
                    mHandler.sendEmptyMessage(BT_MESSAGE_WRITE_SUCCESS);
                    try {
                        currentMessageId=((Message) event.getEventData()).getMessageID();
                        Log.i(LogTag.INFO_TAG, "消息id===" + ((Message) event.getEventData()).getMessageID());
                        String msg=((Message) event.getEventData()).getResponseMessage();
                        byte[] data = (byte[]) msg.getBytes();

                        if (data != null && data.length > 0) {
                            //ByteManagerUtil.getInstance().writeByte(data);
                        }

                    } catch (Exception ex) {
                        Log.e(LogTag.ERROR_TAG, ex.getMessage(), ex);
                    }

                    break;
                }
                case MESSAGE_RESPONSE_TIME_OUT:
                    Log.e(LogTag.ERROR_TAG, "消息等待响应超时");
                    mHandler.sendEmptyMessage(MESSAGE_RESPONSE_TIME_OUT);
                    disconnectDevice();
                    break;
                case MESSAGE_WRITE_FAIL: {
                    Log.e(LogTag.ERROR_TAG, "消息写入失败");
                    mHandler.sendEmptyMessage(BT_MESSAGE_WRITE_FAIL);
                    break;
                }
                case  RESPONSE_WITH_NO_MESSAGE: {
                    try {
                        byte[] data = (byte[]) event.getEventData();
                        if (data != null && data.length > 0) {
                            //ByteManagerUtil.getInstance().writeByte(data);
                        }

                    } catch (Exception ex) {
                        Log.e(LogTag.ERROR_TAG, ex.getMessage(), ex);
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case BT_CONNECT:
                    //mMenu.findItem(R.id.current_blu)
                    //    .setIcon(R.drawable.bar_item_blu_connect_yellow);
                    Log.i(LogTag.INFO_TAG, "蓝牙连接成功");
                    if (mLoadingDialog != null) {
                        mLoadingDialog.dismiss();
                    }
                    //isConneted = true;
                    ToastUtil.showSToast("蓝牙连接成功");
                    //startBluAuthenticate();//蓝牙连接成功开始进行验证
                    hander.removeCallbacks(dismssDialogRunnable);
                    hander.removeCallbacks(dismssConDialogRunnable);

                    break;
                case BT_DISCONNECTED:
                    //if (isConneted) {
                    //    isConneted = false;
                    //    if (!isBluModle) {
                    //        mMenu.findItem(R.id.current_blu)
                    //            .setIcon(R.drawable.bar_item_offline);
                    //    } else {
                    //        mMenu.findItem(R.id.current_blu)
                    //            .setIcon(R.drawable.bar_item_bt);
                    //    }
                    //    Log.i(LogTag.INFO_TAG, "蓝牙连接已断开");
                    //    disconnectDevice();//非手动断开，清除蓝牙数据
                    //    Toast.makeText(MainActivity.this,"蓝牙连接已断开!",Toast.LENGTH_SHORT).show();
                    //}
                    break;
                case BT_MESSAGE_WRITE_SUCCESS:
                    //  ToastUtils.showShort(MainActivity.this, "蓝牙发送指令成功");
                    break;
                case BT_MESSAGE_WRITE_FAIL:
                    ToastUtil.showSToast("蓝牙发送指令失败");
                    break;
                case BT_WRITE_TIME_OUT:
                    ToastUtil.showSToast("蓝牙发送指令超时");
                    break;
                case VERIFY_RESULT:
                    if (msg.obj.equals("1")){
                        Toast.makeText(MainActivity.this,"蓝牙认证通过!",Toast.LENGTH_SHORT).show();
                        //sendDeviceStateComd();
                    }else {
                        Toast.makeText(MainActivity.this,"蓝牙认证失败!",Toast.LENGTH_SHORT).show();
                        try {
                            Thread.sleep(1000);
                            disconnectDevice();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                    break;
                case MESSAGE_RESPONSE_TIME_OUT:
                    ToastUtil.showSToast("消息等待响应超时！");
                    break;
                case REFRESH_RUN_STATE:
                    //loadWebView(runState);
                    break;
                case MESSAGE_RESPONSE_SAVE_SETTINGS_SUCCESS:
                    ToastUtil.showSToast("保存设置信息成功！");
                    //mWebView.loadUrl("javascript:restart()");
                    break;
                case BT_REQUEST_MTU_FAIL:
                    ToastUtil.showSToast("MTU请求设置失败！");
                    break;
                case BT_SERVICE_FIND_FAIL:
                    ToastUtil.showSToast("蓝牙服务发现失败！");
                    break;
                case BT_CHARACTERISTICS_FIND_FAIL:
                    ToastUtil.showSToast("蓝牙特征读取失败！");
                    break;
                case BT_ENABLE_READ_FAIL:
                    ToastUtil.showSToast("设置读取Descriptor失败！");
                    break;
                case BT_RECOVERY_SUCCESS:
                    Toast.makeText(MainActivity.this,"恢复出厂设置成功！",Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_RESPONSE_REBOOT_DEVICE:
                    Toast.makeText(MainActivity.this,"重启系统成功！",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }

            return false;
        }
    });

    /**
     * 发送蓝牙请求设备信息指令
     */
    /*private void sendDeviceStateComd() {
        if (isConneted) {
            //获取所有配置
            final String allInfoCommand = CommandManager.getInstance()
                .getCommand(GET_ALL_SENSOR_CONFIG, null);
            //运行状态
            String runstateCommand = CommandManager.getInstance()
                .getCommand(SYSTEM_RUN_STATE, null);
            //渗压计开关
            String shenyajiCommand = CommandManager.getInstance()
                .getCommand(QUERY_OSMOMETER_PARAMETER, null);
            //版本信息
            String versionCommand = CommandManager.getInstance()
                .getCommand(CommandType.VERSION_MESSAGE, null);


            String serverCommand = CommandManager.getInstance()
                .getCommand(CommandType.SERVER_ADDRESS, null);

            mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), allInfoCommand, true));
            Log.i(LogTag.INFO_TAG, "发送指令===" + allInfoCommand);
            mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), runstateCommand, true));
            Log.i(LogTag.INFO_TAG, "发送指令===" + runstateCommand);
            mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), shenyajiCommand, true));
            Log.i(LogTag.INFO_TAG, "发送指令===" + shenyajiCommand);
            mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), versionCommand, true));
            Log.i(LogTag.INFO_TAG, "发送指令===" + versionCommand);
        } else {
            if (!isConneted) {
                ToastUtil.showSToast("蓝牙未连接");
            }
        }

    }*/

    private void handleDeviceFind(BluetoothDeviceFindEventData eventData) {

        if (list.contains(eventData.getNewDevice()) ||
            eventData.getNewDevice().getDevice().getName() == null) {
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
        isShowingDialog = false;
        if (null != MdBluetoothManager.getInstance()) {
            MdBluetoothManager.getInstance().disconnect();
        }
    }


    private void showResultDialog(String content){
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

        if (null != mLocationClient) {
            mLocationClient.onDestroy();
        }
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

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        isShowingDialog = true;
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // 当DeviceListActivity返回与设备连接的消息
                if (resultCode == Activity.RESULT_OK) {
                    // 得到链接设备的MAC
                    BluetoothDevice dev = (BluetoothDevice) data.getParcelableExtra("device");
                    mdBluetoothManager.connectDevice(dev, MainActivity.this);
                    if (null != mLoadingDialog) {
                        mLoadingDialog.showNoCancelDialog("正在连接...");
                    }
                    hander.postDelayed(dismssConDialogRunnable, 20000);
                }
                break;
            case REQUEST_CONNECT_DEVICE:

                // 判断蓝牙是否启用
                if (resultCode == Activity.RESULT_OK) {
                    // 开启蓝牙
                    initBluetooth();
                } else {
                    ToastUtil.showSToast("蓝牙未启用");
                }
                break;
            default:
                break;
        }
    }


}
