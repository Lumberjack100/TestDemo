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
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeEquipModelEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeBasicInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.ConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.model.DiscoveredBluetoothDevice;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeAdvancedConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeBasicParamConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeDataCenterHomeActivity;

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

    @BindView(R.id.tv_config_model)
    TextView mTvConfigModel;//设备模式

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private ConfigModuleAdapter moduleAdapter;
    private List<ConfigModule> configModuleList = new ArrayList<>();
    private ConfigModule selectedConfigModule;

    private DiscoveredBluetoothDevice device;
    private AdmeBasicInfo admeBasicInfo;

    private int equipModellPos;
    private String equipModel;//设备模式


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
        mTvDeviceConnectOperate.setVisibility(View.VISIBLE);
        mTvDeviceConnectOperate.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        initAdapter();
        initConfigModuleData();
        //观察连接状态变化
        observerConnectionState();
        //观察获取 ApiKey
        observerApiKey();
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
        int spacing = DensityUtil.Dp2Px(mActivity, 15);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
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
            case "状态":
//                DeviceCurrentStateActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);
                break;

            case "基础配置":
                AdmeBasicParamConfigActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);
                break;

            case "测孔深":

                break;

            case "正反测":

                break;

            case "数据中心":
                AdmeDataCenterHomeActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, AppContants.DataCenterConfigMethod.BASIC_CONFIG);
                break;

            case "指令下发":

                break;

            case "高级配置":
                AdmeAdvancedConfigActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);
                break;

            case "设置":

                break;
        }
    }

    private void observerConnectionState() {
        usrBleViewModel.getConnectionState().observe(getViewLifecycleOwner(), new Observer<ConnectionState>() {
            @Override
            public void onChanged(ConnectionState connectionState) {
                switch (connectionState.getState()) {
                    case CONNECTING://A connection to the device was initiated.
                        showProgressBar();
                        mTvConnectState.setText(R.string.ble_state_connecting);
                        break;

                    case INITIALIZING://The device has connected and begun service discovery and initialization.
//                        mTvConnectState.setText(R.string.ble_state_initializing);
                        break;

                    case READY://The initialization is complete, and the device is ready to use.
                        onConnectionStateChanged(true);
                        mTvConnectState.setText("初始化中...");
                        usrBleViewModel.queryDeviceApiKeyBySn(device.getDevice().getName().substring(3));
//                        getEquipmentBaseInfo();
                        break;

                    case DISCONNECTED://The device disconnected or failed to connect.
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
                    case DISCONNECTING://The disconnection was initiated.
                        stopHeartRunnable();
                        break;
                }
            }
        });
    }

    private void observerApiKey() {
        usrBleViewModel.getDeviceApiKey().observeInFragment(this, new Observer<String>() {
            @Override
            public void onChanged(String apiKey) {
                dismissProgressDialog();
                getEquipmentBaseInfo();
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

    private void initConfigModuleData() {
        configModuleList.clear();

        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_state, GlobalUtil.getString(R.string.device_config_module_current_state), "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_basic_config, "基础配置", "设备基础参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_measuring_hole_depth, "测孔深", "测量测斜管深度");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_positive_and_negative_test, "正反测", "正反测起点校准");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_data_center, "数据中心", "基础参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_instruction_send, "指令下发", "自定义指令下发");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_advanced_setting, "高级配置", "设备高级参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_setting, "设置", "高级设置");
        configModuleList.add(configModule);
    }

    @OnClick({R.id.tv_device_connect_operate, R.id.ll_switch_config_model})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }
        switch (v.getId()) {
            case R.id.tv_device_connect_operate://断开/重新连接
                if (!isConnected()) {
                    connectDevice(device.getDevice());
                } else {//断开连接处理
                    isExitMode = false;
                    showDisconnectDialog(getResources().getString(R.string.disconnect_device));
                }
                break;

            case R.id.ll_switch_config_model://切换设备模式
                showSwitchConfigModelDialog();
                break;
        }
    }

    private void showSwitchConfigModelDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"设备配置模式", "自动监测模式"},
                        null, equipModellPos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                equipModellPos = position;
                                mTvConfigModel.setText(text);
                                if (text.equals("设备配置模式")) {
                                    equipModel = "0";
                                } else {
                                    equipModel = "1";
                                }
                                setEquipModel();
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    /**
     * 设置设备模式
     */
    private void setEquipModel() {
        AdmeEquipModelEntity entity = new AdmeEquipModelEntity(equipModel);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_EQUIPMENT_MODEL, entity);
        sendCommand(command);
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
            case ADME_MD_GET_EQUIPMENT_BASIS: {//获取设备的基本信息
                hideProgressBar();
                IOTCommandResult<AdmeBasicInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    hideProgressBar();
                    String errMsg = String.format("%s %s", "获取设备的基本信息出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                admeBasicInfo = commandResult.getResult();
                updateHeadInfo();
                //获取设备的运行状态
//                getGatewayAisleInfo(VmsAisleNumber.NUMBER_ONE);
            }
            break;

            case ADME_MD_SET_EQUIPMENT_MODEL: {//设置设备模式
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置设备模式出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                updateConfigModuleData();
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
    private void updateHeadInfo() {
        if (admeBasicInfo != null) {
            mTvDeviceName.setText("水平自动监测设备");
            mTvDeviceSn.setText(String.format("设备编号：%s", admeBasicInfo.getSn()));
            mTvProductModel.setText(String.format("产品型号：%s", admeBasicInfo.getProductid()));
            mTvSubModel.setText("运行状态：--");
            mTvPlatformCommunicationState.setText("平台连接状态：--");

            if (!TextUtils.isEmpty(admeBasicInfo.getEquimodel())) {
                equipModel = admeBasicInfo.getEquimodel();
                if (equipModel.equals("0")) {
                    equipModellPos = 0;
                    mTvConfigModel.setText("设备配置模式");
                } else {
                    equipModellPos = 1;
                    mTvConfigModel.setText("自动监测模式");
                }
                updateConfigModuleData();
            }
        } else {
            mTvDeviceName.setText("水平自动监测设备");
            mTvDeviceSn.setText("设备编号：--");
            mTvProductModel.setText("产品型号：--");
            mTvSubModel.setText("运行状态：--");
            mTvPlatformCommunicationState.setText("平台连接状态：--");
        }
    }

    private void updateConfigModuleData() {
        if (TextUtils.isEmpty(equipModel))
            return;

        configModuleList.clear();
        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_state, GlobalUtil.getString(R.string.device_config_module_current_state), "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_basic_config, "基础配置", "设备基础参数配置");
        configModuleList.add(configModule);

        if (equipModel.equals("0")) {//0：设备配置模式，1：自动监测模式
            configModule = new ConfigModule(R.drawable.ic_measuring_hole_depth, "测孔深", "测量测斜管深度");
            configModuleList.add(configModule);

            configModule = new ConfigModule(R.drawable.ic_positive_and_negative_test, "正反测", "正反测起点校准");
            configModuleList.add(configModule);

            configModule = new ConfigModule(R.drawable.ic_device_data_center, "数据中心", "基础参数配置");
            configModuleList.add(configModule);

            configModule = new ConfigModule(R.drawable.ic_device_instruction_send, "指令下发", "自定义指令下发");
            configModuleList.add(configModule);

            configModule = new ConfigModule(R.drawable.ic_device_advanced_setting, "高级配置", "设备高级参数配置");
            configModuleList.add(configModule);

            configModule = new ConfigModule(R.drawable.ic_device_setting, "设置", "高级设置");
            configModuleList.add(configModule);
        } else {
            configModule = new ConfigModule(R.drawable.ic_device_advanced_setting, "高级配置", "设备高级参数配置");
            configModuleList.add(configModule);
        }

        moduleAdapter.notifyDataSetChanged();
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
    }
}