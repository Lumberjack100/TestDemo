package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.enums.CollectorModel;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.model.BaseConfigInfo;
import com.shmedo.configlibrary.ble.model.LoaclTimeInfo;
import com.shmedo.configlibrary.ble.model.VersionMessageInfo;
import com.shmedo.configlibrary.ble.utils.ResultParserUtil;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.core.util.JZLocationConverter;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.ConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceTypeInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DiscoveredBluetoothDevice;
import com.shmedo.mcloudapp.deviceconfig.model.FirmWareInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.AdvancedSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.DataCenterActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceCurrentStateActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.DasCollectorSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.sensor.DasSensorConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.FirmWareSelectDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.BaseDispatchCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.QueryTerminalTimeDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.TelemetryDialog;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.LocationViewModel;
import com.shmedo.mcloudapp.entity.DeviceTypeInfoDao;
import com.shmedo.mcloudapp.entity.SyncPositionBean;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.LocationUtils;
import com.shmedo.mcloudapp.util.BlueResultParserUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import no.nordicsemi.android.ble.livedata.state.ConnectionState;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/7/20 <br/>
 * 描述：     DAS蓝牙配置主页面
 */
public class BleDasHomeFragment extends BaseBleCommunicateFragment {
    public static final String EXTRA_DEVICE = "com.shmedo.mcloudapp.EXTRA_DEVICE";

    private static final int LOW_ENERGY_MODEL = 0x0001;
    private static final int REBOOT = 0x0002;

    @BindView(R.id.progress_overlay)
    View progressOverlay;

    @BindView(R.id.tv_progress_text)
    TextView mTvProgressText;

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;//设备名称

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;//设备SN号

    @BindView(R.id.tv_product_model)
    TextView mTvProductModel;//产品型号

    @BindView(R.id.tv_time_or_sub_model)
    TextView mTvSubModel;//采集器型号

    @BindView(R.id.tv_platform_communication_state)
    TextView mTvPlatformCommunicationState;// 与平台通信状态

    @BindView(R.id.tv_device_state_flag)
    TextView mTvDeviceState;//蓝牙连接状态(已连接、已断开)

    @BindView(R.id.tv_device_connect_operate)
    TextView mTvDeviceConnectOperate;//蓝牙连接操作(断开连接、重新连接)

    @BindView(R.id.tv_active_state)
    TextView mTvActiveState;

    @BindView(R.id.activeStateSwBtn)
    SwitchButton mSbActiveState;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private ConfigModuleAdapter moduleAdapter;
    private List<ConfigModule> configModuleList = new ArrayList<>();
    private ConfigModule selectedConfigModule;

    private DiscoveredBluetoothDevice device;
    private String collectorModel = "";//采集器类型
    private int deviceTypeID;
    private String deviceTypeName;
    private BaseConfigInfo baseConfigInfo;
    private boolean isInitialSensorOpera = false;//是否初始化传感器操作

    private LocationViewModel locationViewModel;

    public static BleDasHomeFragment newInstance(DiscoveredBluetoothDevice device) {
        BleDasHomeFragment fragment = new BleDasHomeFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_DEVICE, device);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            device = getArguments().getParcelable(EXTRA_DEVICE);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.test_ble_das_home_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHeadInfo();
        initSwitchViewListener();
        initAdapter();
        initConfigModuleData();
        observerConnectionState();
        observerLocation();
        //建立蓝牙连接
        connectDevice(device.getDevice());
    }

    @Override
    public void onResume() {
        super.onResume();
        onConnectionStateChanged(isConnected());
    }

    private void setHeadInfo() {
        if (device != null) {
            searchDeviceTypeInfo("DAS");
            mTvDeviceSn.setText(String.format("设备编号：%s", device.getName().substring(3)));
            mTvProductModel.setText(String.format("产品型号：%s", "DAS"));
        }
        mTvSubModel.setText("采集器型号：--");
        mTvPlatformCommunicationState.setVisibility(View.GONE);
        mTvDeviceConnectOperate.setVisibility(View.VISIBLE);
        mTvDeviceConnectOperate.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
    }

    private void searchDeviceTypeInfo(String typeName) {
        DeviceTypeInfo deviceTypeInfo = DaoManager.getInstance().getDaoSession().getDeviceTypeInfoDao().queryBuilder()
                .where(DeviceTypeInfoDao.Properties.DeviceTypeName.like("%" + typeName + "%"))
                .unique();

        if (deviceTypeInfo != null) {
            mTvDeviceName.setText(TextUtils.isEmpty(deviceTypeInfo.getDesc()) ? "" : deviceTypeInfo.getDesc());
            deviceTypeID = deviceTypeInfo.getId();
            deviceTypeName = deviceTypeInfo.getDeviceTypeName();
        } else {
            mTvDeviceName.setText("物联网数据采集器");
            deviceTypeID = -1;
            deviceTypeName = typeName;
        }
    }

    /**
     * switch按钮事件
     */
    private void initSwitchViewListener() {
        //设备启用状态开关
        mSbActiveState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    mSbActiveState.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }

                if (isChecked) {
                    //发送激活DAS命令
                    setLowEnergyModel(true);
                    mTvActiveState.setText("已激活");
                } else {
                    setLowEnergyModel(false);
                    mTvActiveState.setText("已待机");
                }
            }
        });
    }

    private void initAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(mActivity, 15);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        moduleAdapter = new ConfigModuleAdapter(configModuleList);
//        moduleAdapter.setAnimationEnable(true);
//        moduleAdapter.setAnimationFirstOnly(false);
        moduleAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if (isDoubleClick(view)) {
                    return;
                }

                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }

                selectedConfigModule = (ConfigModule) configModuleList.get(position);
                processItemClick();
            }
        });
        mRecyclerView.setAdapter(moduleAdapter);
    }

    private void processItemClick() {
        switch (selectedConfigModule.getName()) {
            case "状态":
                DeviceCurrentStateActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, AppContants.DeviceType.DAS);
                break;

            case "传感器初始化":
                isInitialSensorOpera = true;
                errMsg = "发送指令超时,请稍后尝试";
                startProgressRunnable("指令下发中...", CONFIG_PARAMS_DELAY_MILLIS);
                //发送激活DAS命令
                setLowEnergyModel(true);
                break;

            case "时间":
                doQueryTimeCmd();
                break;

            case "遥测":
                doTelemetryCmd();
                break;

            case "重启":
                showWarnDialog("温馨提示", "确定重启设备吗？", REBOOT);
                break;

            case "固件升级":
                FirmWareSelectDialog newFragment = new FirmWareSelectDialog(MCloudApp.getCompanyID(), deviceTypeID);
                newFragment.setDialogFragmentClickListener(firmWareSelectListener);
                newFragment.show(getChildFragmentManager(), "dialog");
                break;

            case "采集器配置":
                DasCollectorSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, collectorModel);
                break;

            case "传感器配置":
                DasSensorConfigActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);
                break;

            case "数据中心":
                DataCenterActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);
                break;

            case "设置":
                AdvancedSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, AppContants.DeviceType.DAS);
                break;
        }
    }

    private void doQueryTimeCmd() {
        errMsg = "发送指令超时,请稍后尝试";
        startProgressRunnable("指令下发中...", CONFIG_PARAMS_DELAY_MILLIS);
        String command = CommandManager.getInstance().getCommand(CommandType.LOCAL_TIME, null);
        sendCommand(command);
        Timber.d("获取设备时间信息指令===%s", command);
    }

    private void doTelemetryCmd() {
        errMsg = "发送指令超时,请稍后尝试";
        startProgressRunnable("指令下发中...", CONFIG_PARAMS_DELAY_MILLIS);
        String command = CommandManager.getInstance().getCommand(CommandType.INSTANT_COLLEACTOR, null);
        sendCommand(command);
        Timber.d("遥测设备指令===%s", command);
    }

    private void doRebootCmd() {
        saveConfigInfo();
    }

    private BaseDialogFragment.DialogFragmentClickListener firmWareSelectListener = new BaseDialogFragment.DialogFragmentClickListener<FirmWareInfo>() {
        @Override
        public boolean onPositiveClick(View view, FirmWareInfo firmWareInfo) {

            return true;
        }

        @Override
        public void onNegativeClick(View view) {

        }
    };

    /**
     * 观察连接状态变化
     */
    private void observerConnectionState() {
        usrBleViewModel.getConnectionState().observe(getViewLifecycleOwner(), new Observer<ConnectionState>() {
            @Override
            public void onChanged(ConnectionState connectionState) {
                switch (connectionState.getState()) {
                    case CONNECTING:
                        showProgressBar();
                        mTvProgressText.setText(R.string.ble_state_connecting);
                        break;

                    case INITIALIZING:
//                        mTvConnectState.setText(R.string.ble_state_initializing);
                        break;

                    case READY:
                        onConnectionStateChanged(true);
                        mTvProgressText.setText("初始化中...");
                        setAuthenticateWay();
                        break;

                    case DISCONNECTED:
                        if (connectionState instanceof ConnectionState.Disconnected) {
                            final ConnectionState.Disconnected stateWithReason = (ConnectionState.Disconnected) connectionState;
                            if (stateWithReason.isNotSupported()) {
                                ToastUtils.show("不支持的设备");
                            } else if (stateWithReason.isTimeout()) {
                                ToastUtils.show("连接超时");
                            }
                        }
                        clearDevice();
                        onConnectionStateChanged(false);
                        hideProgressBar();
                        break;

                    // fallthrough
                    case DISCONNECTING:
                        stopHeartRunnable();
                        break;
                }
            }
        });
    }

    /**
     * 观察获取定位信息
     */
    private void observerLocation() {
        locationViewModel = getApplicationScopeViewModel(LocationViewModel.class);
        locationViewModel.getSyncPositionBean().observeInFragment(this, new Observer<SyncPositionBean>() {
            @Override
            public void onChanged(SyncPositionBean syncPositionBean) {
                try {
                    //将高德坐标(即GCJ-02火星坐标)转换为WGS-84世界标准地理坐标
                    JZLocationConverter.LatLng latLng = new JZLocationConverter.LatLng(syncPositionBean.getLatitude(), syncPositionBean.getLongitude());
                    latLng = JZLocationConverter.gcj02ToWgs84(latLng);

                    String position = String.format(Locale.getDefault(), "%.8f", latLng.longitude) + "," + String.format(Locale.getDefault(), "%.8f", latLng.latitude);
                    if (isConnected()) {
                        String command = "##9161" + position + "\r\n";
                        sendCommand(command);
                        Timber.i("同步安装位置指令：%s", command);
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void showProgressBar() {
        progressOverlay.setVisibility(View.VISIBLE);
        //TODO #gh# android:clickable="true" 和 android:focusable="true" 已经实现了禁止触摸遮罩层下面的 View,
        // 防止点击未遮住的ToolBar，添加下面代码禁用窗体触摸
        mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideProgressBar() {
        progressOverlay.setVisibility(View.GONE);
        //get user interaction back
        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void onConnectionStateChanged(boolean isConnected) {
        if (isConnected) {
            mTvDeviceState.setText("已连接");
            mTvDeviceState.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_50E9B9));
            mTvDeviceState.setBackgroundResource(R.drawable.bg_device_online_state_flag);

            mTvDeviceConnectOperate.setText("断开连接");
            mTvDeviceConnectOperate.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_b3b3b3));
        } else {
            mTvDeviceState.setText("已断开");
            mTvDeviceState.setTextColor(ContextCompat.getColor(mActivity, R.color.sub_title_text_color));
            mTvDeviceState.setBackgroundResource(R.drawable.bg_device_offline_state_flag);

            mTvDeviceConnectOperate.setText("重新连接");
            mTvDeviceConnectOperate.setTextColor(ContextCompat.getColor(mActivity, R.color.blue_52B4F8));
            mSbActiveState.setCheckedImmediatelyNoEvent(false);
        }
    }

    /**
     * 设备认证结果回调
     *
     * @param isSuccess
     */
    @Override
    protected void onAuthenticateResult(boolean isSuccess) {
        if (isSuccess) {
            queryDASConfigInfoCmd();

        } else {
            hideProgressBar();
        }
    }

    @OnClick({R.id.tv_device_connect_operate})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }
        if (v.getId() == R.id.tv_device_connect_operate) {//断开/重新连接
            if (!isConnected()) {
                connectDevice(device.getDevice());
            } else {//断开连接处理
                isExitMode = false;
                showDisconnectDialog(getResources().getString(R.string.disconnect_device));
            }
        }
    }

    @Override
    protected void parseResponseMessage(String cmdStr) {
        if (!isActive) {
            return;
        }
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case BASE_CONFIG://基础配置信息 000
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
//                    stopProgressRunnable();
                    hideProgressBar();
                    Timber.e("查询基础配置信息指令出错!");
                    return;
                }
                baseConfigInfo = ResultParserUtil.getEntityObject(cmdStr);
                initBaseConfigInfo();
                queryDeviceVersionInfo();
                break;

            case VERSION_MESSAGE:
                hideProgressBar();
                startHeartRunnable();
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("查询版本信息指令出错!");
                    return;
                }
                VersionMessageInfo versionMessageInfo = ResultParserUtil.getEntityObject(cmdStr);
                initVersionInfo(versionMessageInfo);
                LocationUtils.getInstance().getPositionPermission(mActivity);
                break;

            case LOW_ENERGY:
                stopProgressRunnable();
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    if (isInitialSensorOpera) {
                        isInitialSensorOpera = false;
                        ToastUtils.show("传感器初始化失败!");
                    }
                    Timber.e("激活/待机指令出错!");
                    return;
                }
                if (isInitialSensorOpera) {
                    isInitialSensorOpera = false;
                    ToastUtils.show("传感器已初始化,设备即将重启!");
                }
                break;

            case LOG_OUTPUT_STATUS:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("设置日志输出状态指令出错!");
                    return;
                }
                break;

            case LOCAL_TIME:
                stopProgressRunnable();
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("查询终端时间指令出错!");
                    ToastUtils.show("查询终端时间指令出错!");
                    return;
                }
                LoaclTimeInfo timeInfo = ResultParserUtil.getEntityObject(cmdStr);
                if (timeInfo != null && !TextUtils.isEmpty(timeInfo.getTime())) {
                    String time = timeInfo.getTime();
                    if (time.length() == 12) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("20" + time.substring(0, 2));
                        sb.append("-" + time.substring(2, 4));
                        sb.append("-" + time.substring(4, 6));
                        sb.append(" " + time.substring(6, 8));
                        sb.append(":" + time.substring(8, 10));
                        sb.append(":" + time.substring(10, 12));
                        BaseDispatchCmdDialog newFragment = new QueryTerminalTimeDialog("终端时间", sb.toString());
                        newFragment.show(getChildFragmentManager(), "dialog");
                    }
                }
                break;

            case INSTANT_COLLEACTOR: {
                stopProgressRunnable();
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("遥测指令出错!");
                    ToastUtils.show("遥测指令出错!");
                    return;
                }
                BaseDispatchCmdDialog newFragment = new TelemetryDialog("遥测", tempStr);
                newFragment.show(getChildFragmentManager(), "dialog");
            }
            break;

            case INSTALL_LOCATION: {
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("同步安装位置出错!");
                    ToastUtils.show("同步安装位置出错!");
                    return;
                }
                saveConfigInfoNoReboot();
            }
            break;

            case SAVE_CONFIG_INFO:
                stopProgressRunnable();
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("保存参数指令错误!");
                    return;
                }
                ToastUtils.show("设备即将重启!");
                break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    /**
     * 处理基础配置信息
     */
    private void initBaseConfigInfo() {
        if (baseConfigInfo == null) {
            Timber.e("基础配置信息为空!");
            return;
        }

        collectorModel = baseConfigInfo.getCollectorModel().toString();

        //获取采集器类型
        if (!TextUtils.isEmpty(collectorModel)) {
            CollectorModel model = CollectorModel.value(collectorModel);
            String collectorName = BlueResultParserUtil.getCollectorName(model);
            mTvSubModel.setText(String.format("采集器型号：%s", TextUtils.isEmpty(collectorName) ? "" : collectorName));
        }

        //设备状态
        switch (baseConfigInfo.getEquipmentStatus()) {
            case STANDBY:   //待机
                mSbActiveState.setCheckedImmediatelyNoEvent(false);
                mTvActiveState.setText("已待机");
                break;
            case ACTIVATION:    //激活
                mSbActiveState.setCheckedImmediatelyNoEvent(true);
                mTvActiveState.setText("已激活");
                break;
        }
    }

    private void initVersionInfo(VersionMessageInfo versionInfo) {
        if (versionInfo == null) {
            return;
        }
        String firmwareVersion = TextUtils.isEmpty(versionInfo.getFirmwareVersion()) ? "--" : versionInfo.getFirmwareVersion();
        firmwareVersion = firmwareVersion.replace(deviceTypeName + "-", "").replace(deviceTypeName, "");

        for (ConfigModule configModule : configModuleList) {
            if (configModule.getName().equals("固件升级")) {
                configModule.setDesc("版本:" + firmwareVersion);
                break;
            }
        }
        moduleAdapter.notifyDataSetChanged();
    }

    private void initConfigModuleData() {
        configModuleList.clear();
        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_state, 5, GlobalUtil.getString(R.string.device_config_module_current_state), "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_reboot, "传感器初始化", "传感器初始化");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_current_time, 1, "时间", "获取当前设备时间");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_telemetry, 6, "遥测", "远距离测量");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_reboot, 7, "重启", "重新启动当前设备");
        configModuleList.add(configModule);

//        TODO #gh# 后期换成物联网协议再开放
//        configModule = new ConfigModule(R.drawable.ic_device_firmware_upgrade, 24, "固件升级", "版本:--");
//        configModuleList.add(configModule);

        //DAS具有采集器配置项
        if (mTvProductModel.getText().toString().contains("DAS")) {
            configModule = new ConfigModule(R.drawable.ic_device_collector_config, "采集器配置", "采集器参数配置");
            configModuleList.add(configModule);

            configModule = new ConfigModule(R.drawable.ic_device_sensor_config, "传感器配置", "传感器参数配置");
            configModuleList.add(configModule);

            configModule = new ConfigModule(R.drawable.ic_device_data_center, "数据中心", "MQTT协议配置");
            configModuleList.add(configModule);
        }

        configModule = new ConfigModule(R.drawable.ic_device_setting, "设置", "高级设置");
        configModuleList.add(configModule);
    }

    @Override
    public boolean onBackPressed() {
        if (isConnected()) {
            isExitMode = true;
            showDisconnectDialog(getResources().getString(R.string.finish_activity_disconnect_bluetooth_device));
            return true;
        }
        return false;
    }

    @Override
    public void onStop() {
        super.onStop();
        LocationUtils.getInstance().stopLocalService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MCloudApp.setCurDeviceToken(null);
    }

    /**
     * 危险操作前弹框提醒
     */
    private void showWarnDialog(String title, String content, int operateType) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title(title)
                .content(content)
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        switch (operateType) {
                            case LOW_ENERGY_MODEL:
                                setLowEnergyModel(false);
                                mTvActiveState.setText("已待机");
                                break;

                            case REBOOT:
                                doRebootCmd();
                                break;
                        }
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        switch (operateType) {
                            case LOW_ENERGY_MODEL:
                                mSbActiveState.setCheckedImmediatelyNoEvent(true);
                                break;

                            case REBOOT:
                                break;
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

}