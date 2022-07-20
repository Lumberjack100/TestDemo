package com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20;

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
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.m20.M20BaseInfo;
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
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceCurrentStateActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import no.nordicsemi.android.ble.livedata.state.ConnectionState;
import no.nordicsemi.android.ble.observer.ConnectionObserver;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/18/21 <br/>
 * 描述：    M20 蓝牙配置主页面
 */
public class BleM20HomeFragment extends BaseUSRBleIotCommunicateFragment {
    public static final String EXTRA_DEVICE = "com.shmedo.mcloudapp.EXTRA_DEVICE";

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;//设备名称

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;//设备SN号

    @BindView(R.id.tv_product_model)
    TextView mTvProductModel;//产品型号

    @BindView(R.id.tv_time_or_sub_model)
    TextView mTvFirmwareVersion;//固件版本

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
    private String sn;

    private BleM20SetupWizardDialogFragment setupWizardDialogFragment;

    public static BleM20HomeFragment newInstance(DiscoveredBluetoothDevice device) {
        BleM20HomeFragment fragment = new BleM20HomeFragment();
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
            sn = device.getName().substring(3);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.universal_config_home_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        updateHeadInfo(null);
        initAdapter();
        initConfigModuleData();
        observerApiKey();
        observerConnectionState();

        //建立蓝牙连接
        connectDevice(device.getDevice());
    }

    @Override
    public void onResume() {
        super.onResume();
        onConnectionStateChanged(isConnected());
    }

    private void initAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = ConvertUtils.dp2px(15);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));
        moduleAdapter = new ConfigModuleAdapter(configModuleList);
        moduleAdapter.setAnimationEnable(true);
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
                selectedConfigModule = (ConfigModule) configModuleList.get(position);
                processItemClick();
            }
        });
        mRecyclerView.setAdapter(moduleAdapter);
    }

    private void processItemClick() {
        switch (selectedConfigModule.getName()) {
            case "设置向导":
                setupWizardDialogFragment = BleM20SetupWizardDialogFragment.newInstance();
                setupWizardDialogFragment.show(getChildFragmentManager(), "dialog");
                break;

            case "状态":
                DeviceCurrentStateActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, ProductType.M20);
                break;

            case "数据中心":
                DataCenterHomeActivity.startActivity(mActivity, ProductType.M20, AppContants.CommunicationWay.BLE_CONNECT);
                break;

            case "设置":
                AdvancedSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, ProductType.M20);
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
                        break;

                    case READY://The initialization is complete, and the device is ready to use.
                        onConnectionStateChanged(true);
                        //查询设备 ApiKey
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
                updateHeadInfo(null);
                queryBaseInfo();
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

    private void initConfigModuleData() {
        configModuleList.clear();

        ConfigModule configModule = new ConfigModule(R.drawable.ic_setup_wizard, "设置向导", "一键配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_current_state, "状态", "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_data_center, "数据中心", "基础参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_setting, "设置", "高级设置");
        configModuleList.add(configModule);
    }

    /**
     * 获取设备的基本信息
     */
    private void queryBaseInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.M20_MD_GET_BASE_INFO);
        sendCommand(command);
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
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case M20_MD_GET_BASE_INFO: {//获取设备的基本信息
                IOTCommandResult<M20BaseInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取设备的基本信息出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                updateHeadInfo(commandResult.getResult());
            }
            break;

            case M20_MD_LEVEL_INITIAL://水平初始化
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "水平初始化出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    if (setupWizardDialogFragment != null && setupWizardDialogFragment.isVisible()) {
                        setupWizardDialogFragment.updateState(false);
                    }
                    return;
                }
                if (setupWizardDialogFragment != null && setupWizardDialogFragment.isVisible()) {
                    setupWizardDialogFragment.updateState(true);
                }
                break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    /**
     * 更新头部信息
     */
    private void updateHeadInfo(M20BaseInfo m20BaseInfo) {
        if (deviceInfo == null)
            deviceInfo = new DeviceBaseInfo();

        try {
            mTvDeviceName.setText(TextUtils.isEmpty(deviceInfo.getProductName()) ? "普适型GNSS一体机" : deviceInfo.getProductName());
            mTvDeviceSn.setText(String.format("设备编号：%s", TextUtils.isEmpty(deviceInfo.getDeviceToken()) ? sn : deviceInfo.getDeviceToken()));
            mTvFirmwareVersion.setText(String.format("固件版本：%s", TextUtils.isEmpty(deviceInfo.getFirmwareVersion()) ? "--" : deviceInfo.getFirmwareVersion()));

            if (m20BaseInfo != null) {
                mTvProductModel.setText(String.format("产品型号：%s", !TextUtils.isEmpty(m20BaseInfo.getProductid()) ? m20BaseInfo.getProductid() : "M20"));
                mTvFirmwareVersion.setText(String.format("固件版本：%s", m20BaseInfo.getFirversion()));
            } else {
                mTvProductModel.setText(String.format("产品型号：：%s", "M20"));
            }

            mTvPlatformCommunicationState.setVisibility(View.GONE);
            mTvDeviceConnectOperate.setVisibility(View.VISIBLE);
            mTvDeviceConnectOperate.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        } catch (Exception ex) {
            ex.printStackTrace();
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
        stopDefaultProgress(AppContants.MsgWhat.CONNECT_DEVICE);
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