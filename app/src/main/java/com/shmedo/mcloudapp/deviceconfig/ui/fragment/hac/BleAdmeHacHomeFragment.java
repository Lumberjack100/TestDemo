package com.shmedo.mcloudapp.deviceconfig.ui.fragment.hac;

import android.graphics.Paint;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.interfaces.OnBackPressedListener;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.configlibrary.iot.model.hac.HacMotionState;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.ConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceBaseInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DiscoveredBluetoothDevice;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.AdvancedSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.CustomCommandLogPrintActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceCurrentStateActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeAdvancedConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeBasicParamActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeMeasuringHoleDepthActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import no.nordicsemi.android.ble.livedata.state.ConnectionState;
import no.nordicsemi.android.ble.observer.ConnectionObserver;
import timber.log.Timber;

public class BleAdmeHacHomeFragment extends BaseUSRBleIotCommunicateFragment {
    public static final String EXTRA_DEVICE = "com.shmedo.mcloudapp.EXTRA_DEVICE";

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;//设备名称

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;//设备SN号

    @BindView(R.id.tv_product_model)
    TextView mTvFirmwareVersion;//固件版本

    @BindView(R.id.tv_time_or_sub_model)
    TextView mTvMotionState;//运行状态

    @BindView(R.id.tv_platform_communication_state)
    TextView mTvPlatformCommunicationState;//与米度平台连接状态

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
    private DeviceBaseInfo deviceInfo;

    public static BleAdmeHacHomeFragment newInstance(DiscoveredBluetoothDevice device) {
        BleAdmeHacHomeFragment fragment = new BleAdmeHacHomeFragment();
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
        return R.layout.universal_config_home_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        isFirstCreate = true;
        initAdapter();
        updateHeadInfo();
        updateConfigModuleData();
        observerApiKey();
        observerConnectionState();

        //建立蓝牙连接
        connectDevice(device.getDevice());
    }

    @Override
    public void onResume() {
        super.onResume();
        onConnectionStateChanged(isConnected());
//        if (!isFirstCreate) {
//            startQueryMotorStateProgress(0);
//        }
    }

    private void initAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = ConvertUtils.dp2px(15);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));
        moduleAdapter = new ConfigModuleAdapter(configModuleList);
        moduleAdapter.setAnimationEnable(false);
        moduleAdapter.setAnimationFirstOnly(false);
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
                selectedConfigModule = configModuleList.get(position);
                processItemClick();
            }
        });
        mRecyclerView.setAdapter(moduleAdapter);
    }

    private void processItemClick() {
        switch (selectedConfigModule.getName()) {
            case "状态":
                DeviceCurrentStateActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, ProductType.HAC);
                break;

            case "基础配置":
                AdmeBasicParamActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);
                break;

            case "测量孔深":
                AdmeMeasuringHoleDepthActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);
                break;

            case "指令下发":
                CustomCommandLogPrintActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, ProductType.ADME);
                break;

            case "高级配置":
                AdmeAdvancedConfigActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, ProductType.HAC, null);
                break;

            case "设置":
                AdvancedSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, ProductType.ADME);
                break;
        }
    }

    /**
     * 观察连接状态变化
     */
    private void observerConnectionState() {
        bleViewModel.getConnectionState().observe(getViewLifecycleOwner(), new Observer<ConnectionState>() {
            @Override
            public void onChanged(ConnectionState connectionState) {
                switch (connectionState.getState()) {
                    case CONNECTING://A connection to the device was initiated.
                        startDefaultProgress(null, AppContants.MsgWhat.CONNECT_DEVICE, DELAY_15000_MILLIS);
                        showProgressBar();
                        break;

                    case INITIALIZING://The device has connected and begun service discovery and initialization.
//                        mTvConnectState.setText(R.string.ble_state_initializing);
                        break;

                    case READY://The initialization is complete, and the device is ready to use.
                        onConnectionStateChanged(true);
                        bleViewModel.deviceRequest.queryDeviceApiKeyBySn(device.getDevice().getName().substring(3));
                        break;

                    case DISCONNECTED://The device disconnected or failed to connect.
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
//                        clearDevice();
                        break;

                    // fallthrough
                    case DISCONNECTING://The disconnection was initiated.
                        hideProgressBar();
                        break;
                }
            }
        });
    }

    /**
     * 观察获取 ApiKey
     */
    private void observerApiKey() {
        bleViewModel.deviceRequest.getDeviceApiKeyLiveData().observe(getViewLifecycleOwner(), new Observer<DeviceBaseInfo>() {
            @Override
            public void onChanged(DeviceBaseInfo deviceBaseInfo) {
                deviceInfo = deviceBaseInfo;
                hideProgressBar();
                updateHeadInfo();
//                queryEquipmentBaseInfo();
            }
        });
    }

    /**
     * 蓝牙连接/断开回调，更新页面头部信息
     *
     * @param isConnected
     */
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



    @OnClick({R.id.tv_device_connect_operate})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }
        int id = v.getId();
        if (id == R.id.tv_device_connect_operate) {//断开/重新连接
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
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {



            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    /**
     * 更新头部信息
     */
    private void updateHeadInfo() {
        if (deviceInfo == null)
            deviceInfo = new DeviceBaseInfo();

        try {
            mTvDeviceName.setText(TextUtils.isEmpty(deviceInfo.getProductName()) ? "HAC-半自动化测斜机器人" : deviceInfo.getProductName());
            mTvDeviceSn.setText(String.format("设备编号：%s", TextUtils.isEmpty(deviceInfo.getDeviceToken()) ? device.getName().substring(3) : deviceInfo.getDeviceToken()));
            mTvFirmwareVersion.setText(String.format("固件版本：%s", TextUtils.isEmpty(deviceInfo.getFirmwareVersion()) ? "--" : deviceInfo.getFirmwareVersion()));
            mTvMotionState.setText("运行状态：--");

            mTvPlatformCommunicationState.setVisibility(View.GONE);
            mTvDeviceConnectOperate.setVisibility(View.VISIBLE);
            mTvDeviceConnectOperate.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateConfigModuleData() {
        configModuleList.clear();
        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_state, "状态", "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_measuring_hole_depth, "孔深测量", "测量测斜管深度");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_advanced_setting, "高级配置", "设备高级参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_measuring_hole_depth, "开始测斜", "测量位移");
        configModuleList.add(configModule);

         configModule = new ConfigModule(R.drawable.ic_device_current_state, "历史数据", "获取测量历史数据");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_instruction_send, "指令下发", "自定义指令下发");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_setting, "设置", "高级设置");
        configModuleList.add(configModule);

        moduleAdapter.notifyDataSetChanged();
    }

    /**
     * 刷新电机运动状态
     */
    private void updateMotionState(HacMotionState hacMotionState) {
        if (hacMotionState == null) {
            Timber.e("AdmeMotionState is Null!");
            return;
        }
        switch (hacMotionState.getMotorinfo()) {
            case "0":
                mTvMotionState.setText("运行状态：管口停止");
                break;

            case "1":
                mTvMotionState.setText("运行状态：管底停止");
                break;

            case "2":
                mTvMotionState.setText("运行状态：管口测量");
                break;

            case "3":
                mTvMotionState.setText("运行状态：管口测试");
                break;

            case "4":
                mTvMotionState.setText("运行状态：上拉测量");
                break;

            case "5":
                mTvMotionState.setText("运行状态：上拉测试");
                break;

            case "6":
                mTvMotionState.setText("运行状态：下放测量");
                break;

            case "7":
                mTvMotionState.setText("运行状态：下放测试");
                break;

            default:
                break;
        }
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        switch (msg.what) {
            case AppContants.MsgWhat.MSG_DEFAULT:
                ToastUtils.show("发送指令超时,请稍后尝试");
                break;

            case AppContants.MsgWhat.CONNECT_DEVICE: {
                if (!isConnected()) {
                    hideProgressBar();
                    disconnectDevice();
                    ToastUtils.show("连接超时");
                }
            }
            break;
        }
    }

    @Override
    public void onStop() {
//        isFirstCreate = false;
//        stopDefaultProgress(AppContants.MsgWhat.CONNECT_DEVICE);
//        stopQueryMotorStateProgress();
        super.onStop();
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