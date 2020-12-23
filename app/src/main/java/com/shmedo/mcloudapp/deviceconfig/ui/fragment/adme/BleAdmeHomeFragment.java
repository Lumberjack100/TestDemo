package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
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
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.ConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.model.DiscoveredBluetoothDevice;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceCurrentStateActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.BaseDispatchCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.QueryTerminalTimeDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.TelemetryDialog;
import com.shmedo.mcloudapp.util.bleutil.BlueResultParserUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import no.nordicsemi.android.ble.livedata.state.ConnectionState;
import timber.log.Timber;

public class BleAdmeHomeFragment extends BaseBleIotCommunicateFragment {
    public static final String EXTRA_DEVICE = "com.shmedo.mcloudapp.EXTRA_DEVICE";

    @BindView(R.id.device_container)
    View content;

    @BindView(R.id.progress_overlay)
    View progressOverlay;

    @BindView(R.id.connection_state)
    TextView mTvConnectState;

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;//设备名称

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;//设备SN号

    @BindView(R.id.tv_product_model)
    TextView mTvProductModel;//版本信息

    @BindView(R.id.tv_time_or_sub_model)
    TextView mTvSubModel;//网关电压

    @BindView(R.id.tv_platform_communication_state)
    TextView mTvPlatformCommunicationState;// 与平台通信状态

    @BindView(R.id.tv_device_state_flag)
    TextView mTvDeviceState;//蓝牙连接状态(已连接、已断开)

    @BindView(R.id.tv_device_connect_operate)
    TextView mTvDeviceConnectOperate;//蓝牙连接操作(断开连接、重新连接)

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private ConfigModuleAdapter moduleAdapter;
    private List<ConfigModule> configModuleList = new ArrayList<>();
    private ConfigModule selectedConfigModule;

    private DiscoveredBluetoothDevice device;
    private BaseConfigInfo baseConfigInfo;
    private String collectorModel = "";//采集器类型
    private String deviceTypeName;

    public static BleAdmeHomeFragment newInstance(DiscoveredBluetoothDevice device) {
        BleAdmeHomeFragment fragment = new BleAdmeHomeFragment();
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
        return R.layout.ble_adme_home_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHeadInfo();
        initAdapter();
        initConfigModuleData();
        observerConnectionState();
        connectDevice(device.getDevice());
    }

    @Override
    public void onResume() {
        super.onResume();
        onConnectionStateChanged(isConnected());
    }

    private void setHeadInfo() {
        if (device != null) {
            mTvDeviceSn.setText(String.format("设备编号：%s", device.getName().substring(3)));
            mTvProductModel.setText(String.format("产品型号：%s", "DAS"));
            mTvDeviceName.setText("物联网数据采集器");
            deviceTypeName = "DAS";
        }
        mTvDeviceConnectOperate.setVisibility(View.VISIBLE);
        mTvDeviceConnectOperate.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
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
                DeviceCurrentStateActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);
                break;

            case "遥测":
                doTelemetryCmd();
                break;
        }
    }

    private void doTelemetryCmd() {
        showProgressDialog("指令下发中...");
        String command = CommandManager.getInstance().getCommand(CommandType.INSTANT_COLLEACTOR, null);
        usrBleViewModel.sendIOTProtocolCommand(command);
        Timber.d("遥测设备指令===%s", command);
    }

    private void initConfigModuleData() {
        configModuleList.clear();
        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_state, 5, GlobalUtil.getString(R.string.device_config_module_current_state), "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_current_time, 1, "时间", "获取当前设备时间");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_telemetry, 6, "遥测", "远距离测量");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_reboot, 7, "重启", "重新启动当前设备");
        configModuleList.add(configModule);

//        TODO 后期换成物联网协议再开放
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

    private void observerConnectionState() {
        usrBleViewModel.getConnectionState().observe(getViewLifecycleOwner(), new Observer<ConnectionState>() {
            @Override
            public void onChanged(ConnectionState connectionState) {
                switch (connectionState.getState()) {
                    case CONNECTING:
                        showProgressBar();
                        mTvConnectState.setText(R.string.ble_state_connecting);
                        break;

                    case INITIALIZING:
//                        mTvConnectState.setText(R.string.ble_state_initializing);
                        break;

                    case READY:
//                        content.setVisibility(View.VISIBLE);
                        onConnectionStateChanged(true);
//                        mTvConnectState.setText("设备认证中...");
                        setAuthenticateWay();
                        hideProgressBar();
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
//                        disconnectDevice();
                        clearDevice();
                        onConnectionStateChanged(false);
                        hideProgressBar();
                        break;

                    // fallthrough
                    case DISCONNECTING:
                        stopHeartRunnable();
                        onConnectionStateChanged(false);
                        break;
                }
            }
        });
    }

    private void showProgressBar() {
        progressOverlay.setVisibility(View.VISIBLE);
        //TODO android:clickable="true" 和 android:focusable="true" 已经实现了禁止触摸遮罩层下面的 View,
        // 防止点击未遮住的ToolBar，添加下面代码禁用窗体触摸
        mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideProgressBar() {
        progressOverlay.setVisibility(View.GONE);
        //get user interaction back
        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    //    @Override
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
            mTvConnectState.setText("初始化中…");
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
        switch (v.getId()) {
            case R.id.tv_device_connect_operate://断开/重新连接
                if (!isConnected()) {
//                    findAndConnectSpecificDevice();
                    connectDevice(device.getDevice());
                } else {//断开连接处理
                    isExitMode = false;
                    showDisconnectDialog(getResources().getString(R.string.disconnect_device));
                }
                break;
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
                break;

            case LOW_ENERGY:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("激活/待机指令出错!");
                    return;
                }
                break;

            case LOG_OUTPUT_STATUS:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("设置日志输出状态指令出错!");
                    return;
                }
                break;

            case LOCAL_TIME:
                dismissProgressDialog();
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
                dismissProgressDialog();
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("遥测指令出错!");
                    ToastUtils.show("遥测指令出错!");
                    return;
                }
                BaseDispatchCmdDialog newFragment = new TelemetryDialog("遥测", tempStr);
                newFragment.show(getChildFragmentManager(), "dialog");
            }
            break;

            case REBOOT_DEVICE: {
                dismissProgressDialog();
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("重启指令出错!");
                    ToastUtils.show("重启指令出错!");
                    return;
                }
            }
            break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MCloudApp.setCurDeviceToken(null);
//        MCloudApp.setCurDeviceMacAddr(null);
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
}