package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
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
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.StringUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.interfaces.OnBackPressedListener;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.cmd.entity.InstallLocationEntity;
import com.shmedo.configlibrary.ble.enums.CollectorModel;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.model.BaseConfigInfo;
import com.shmedo.configlibrary.ble.model.LoaclTimeInfo;
import com.shmedo.configlibrary.ble.utils.ResultParserUtil;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.util.JZLocationConverter;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.ConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.model.DiscoveredBluetoothDevice;
import com.shmedo.mcloudapp.deviceconfig.model.SyncPositionInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.AdvancedSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceCurrentStateActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.DasCollectorSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.DasSensorConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.BaseDispatchCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.QueryTerminalTimeDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.TelemetryDialog;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.LocationViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import no.nordicsemi.android.ble.livedata.state.ConnectionState;
import no.nordicsemi.android.ble.observer.ConnectionObserver;
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
    private BaseConfigInfo baseConfigInfo;
    private boolean isInitialSensorOpera = false;//是否初始化传感器操作

    private LocationViewModel locationViewModel;

    private ProductType type;


    public static BleDasHomeFragment newInstance(DiscoveredBluetoothDevice device) {
        BleDasHomeFragment fragment = new BleDasHomeFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_DEVICE, device);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            device = getArguments().getParcelable(EXTRA_DEVICE);
            type = ProductType.valueBySuffix(device.getDevice().getName());
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_das_home_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        mTvPlatformCommunicationState.setVisibility(View.GONE);
        mTvDeviceConnectOperate.setVisibility(View.VISIBLE);
        mTvDeviceConnectOperate.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mTvDeviceName.setText(type == ProductType.DAS ? "智能采集器" : "BHY");
        mTvDeviceSn.setText(String.format("设备编号：%s", MCloudApp.getCurDeviceToken()));
        mTvProductModel.setText(String.format("产品型号：%s", type == ProductType.DAS ? "DAS" : "BHY"));
        mTvSubModel.setText("采集器型号：--");
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
        int spacing = ConvertUtils.dp2px(15);//每一个矩形的间距
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
                DeviceCurrentStateActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, ProductType.DAS);
                break;

            case "传感器初始化":
                isInitialSensorOpera = true;
                startDefaultProgress("指令下发中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_20000_MILLIS);
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

            case "采集器配置":
                DasCollectorSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, collectorModel);
                break;

            case "传感器配置":
                DasSensorConfigActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);
                break;

            case "数据中心":
                DataCenterHomeActivity.startActivity(mActivity, ProductType.DAS, AppContants.CommunicationWay.BLE_CONNECT);
                break;

            case "设置":
                AdvancedSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, ProductType.DAS);
                break;
        }
    }

    private void doQueryTimeCmd() {
        startDefaultProgress("指令下发中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_20000_MILLIS);

        String command = CommandManager.getInstance().getCommand(CommandType.LOCAL_TIME, null);
        sendCommand(command);
        Timber.d("获取设备时间信息指令===%s", command);
    }

    private void doTelemetryCmd() {
        startDefaultProgress("指令下发中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_20000_MILLIS);

        String command = CommandManager.getInstance().getCommand(CommandType.INSTANT_COLLEACTOR, null);
        sendCommand(command);
        Timber.d("遥测设备指令===%s", command);
    }

    /**
     * 观察连接状态变化
     */
    private void observerConnectionState() {
        bleViewModel.getConnectionState().observe(getViewLifecycleOwner(), new Observer<ConnectionState>() {
            @Override
            public void onChanged(ConnectionState connectionState) {
                switch (connectionState.getState()) {
                    case CONNECTING:
                        startDefaultProgress(null, AppContants.MsgWhat.CONNECT_DEVICE, DELAY_15000_MILLIS);
                        showProgressBar();
                        break;

                    case INITIALIZING:
                        break;

                    case READY:
                        onConnectionStateChanged(true);
                        setAuthenticateWay();
                        break;

                    case DISCONNECTED:
                        if (connectionState instanceof ConnectionState.Disconnected) {
                            final ConnectionState.Disconnected stateWithReason = (ConnectionState.Disconnected) connectionState;
                            if (stateWithReason.getReason() == ConnectionObserver.REASON_NOT_SUPPORTED) {
                                Timber.e("DISCONNECTED: 不支持的设备");
                                ToastUtils.show("不支持的设备");
                            } else if (stateWithReason.getReason() == ConnectionObserver.REASON_TIMEOUT) {
                                Timber.e("DISCONNECTED: 连接超时");
                            }
                        }
                        onConnectionStateChanged(false);
                        break;

                    // fallthrough
                    case DISCONNECTING:
                        hideProgressBar();
                        stopHeart();
                        break;
                }
            }
        });
    }

    /**
     * 观察获取定位信息
     */
    private void observerLocation() {
        locationViewModel = getFragmentScopeViewModel(LocationViewModel.class);
        locationViewModel.locationUtils.getSyncPositionBean().observe(getViewLifecycleOwner(), new Observer<SyncPositionInfo>() {
            @Override
            public void onChanged(SyncPositionInfo syncPositionInfo) {
                try {
                    //将高德坐标(即GCJ-02火星坐标)转换为WGS-84世界标准地理坐标
                    JZLocationConverter.LatLng latLng = new JZLocationConverter.LatLng(syncPositionInfo.getLatitude(), syncPositionInfo.getLongitude());
                    latLng = JZLocationConverter.gcj02ToWgs84(latLng);

                    String position = String.format(Locale.getDefault(), "%.8f", latLng.longitude) + "," + String.format(Locale.getDefault(), "%.8f", latLng.latitude);
                    if (isConnected()) {
                        InstallLocationEntity installLocationEntity = new InstallLocationEntity(1);
                        String command = CommandManager.getInstance().getCommand(CommandType.INSTALL_LOCATION, installLocationEntity);
                        command = command.replace("\r\n", position + "\r\n");
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
        WaitDialog.show(getString(R.string.ble_state_connecting))
                .setOnBackPressedListener(new OnBackPressedListener() {//返回按键监听
                    @Override
                    public boolean onBackPressed() {
                        disconnectDevice();
                        WaitDialog.dismiss();
                        return false;
                    }
                });
    }

    private void hideProgressBar() {
        stopDefaultProgress(AppContants.MsgWhat.CONNECT_DEVICE);
        WaitDialog.dismiss();
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
        hideProgressBar();
        if (isSuccess) {
            queryDASConfigInfoCmd();
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
                    Timber.e("查询基础配置信息指令出错!");
                    return;
                }
                baseConfigInfo = ResultParserUtil.getEntityObject(cmdStr);
                initBaseConfigInfo();
                locationViewModel.locationUtils.getPositionPermission(mActivity);
                startHeart();
                break;

            case LOW_ENERGY:
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
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
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
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
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
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
                locationViewModel.locationUtils.stopLocalService();
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("同步安装位置出错!");
                    ToastUtils.show("同步安装位置出错!");
                    return;
                }
                saveConfigInfoNoReboot();
            }
            break;

            case SAVE_CONFIG_INFO:
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("保存参数指令错误!");
                    return;
                }
                if (tempStr.contains("0191")) {
                    ToastUtils.show("设备即将重启!");
                }
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

        collectorModel = baseConfigInfo.getCollectorModel().getCode();

        //获取采集器类型
        if (!TextUtils.isEmpty(collectorModel)) {
            CollectorModel model = CollectorModel.value(collectorModel);
            String collectorName = model.getDescription();
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

    private void initConfigModuleData() {
        configModuleList.clear();
        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_state, StringUtils.getString(R.string.device_config_module_current_state), "获取当前设备状态");
        configModuleList.add(configModule);

//        configModule = new ConfigModule(R.drawable.ic_device_reboot, "传感器初始化", "传感器初始化");
//        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_current_time, "时间", "获取当前设备时间");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_telemetry, "遥测", "远距离测量");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_reboot, "重启", "重新启动当前设备");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_collector_config, "采集器配置", "采集器参数配置");
        configModuleList.add(configModule);

        //DAS具有采集器配置项
        if (type == ProductType.DAS) {
            configModule = new ConfigModule(R.drawable.ic_device_sensor_config, "传感器配置", "传感器参数配置");
            configModuleList.add(configModule);
        }

        configModule = new ConfigModule(R.drawable.ic_device_data_center, "数据中心", "MQTT协议配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_setting, "设置", "高级设置");
        configModuleList.add(configModule);
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        switch (msg.what) {
            case AppContants.MsgWhat.MSG_DEFAULT:
                ToastUtils.show("响应超时,请稍后尝试");
                break;

            case AppContants.MsgWhat.CONNECT_DEVICE: {
                hideProgressBar();
                disconnectDevice();
                ToastUtils.show("连接超时");
            }
            break;
        }
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
                                saveConfigInfo();
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

    @Override
    public void onPause() {
        super.onPause();
        stopDefaultProgress(AppContants.MsgWhat.CONNECT_DEVICE);
        locationViewModel.locationUtils.stopLocalService();
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
    public void onDestroy() {
        MCloudApp.setCurDeviceToken(null);
        MCloudApp.setProductID(-1);
        clearDevice();
        super.onDestroy();
    }
}