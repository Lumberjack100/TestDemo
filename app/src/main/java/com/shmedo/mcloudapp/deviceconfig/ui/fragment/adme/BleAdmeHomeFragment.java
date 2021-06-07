package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
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
import com.shmedo.configlibrary.iot.model.adme.AdmeBaseInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeMotionState;
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
import com.shmedo.mcloudapp.deviceconfig.ui.activity.AdvancedSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.CustomCommandLogPrintActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceCurrentStateActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeAdvancedConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeBasicParamActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeGuideGrooveCalibrationActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeMeasuringHoleDepthActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import no.nordicsemi.android.ble.livedata.state.ConnectionState;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/18/21 <br/>
 * 描述：      ADME 蓝牙配置主页面
 */
public class BleAdmeHomeFragment extends BaseUSRBleIotCommunicateFragment {
    public static final String EXTRA_DEVICE = "com.shmedo.mcloudapp.EXTRA_DEVICE";

    @BindView(R.id.progress_overlay)
    View progressOverlay;

    @BindView(R.id.tv_progress_text)
    TextView mTvProgressText;

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;//设备名称

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;//设备SN号

    @BindView(R.id.tv_product_model)
    TextView mTvProductModel;//版本信息

    @BindView(R.id.tv_time_or_sub_model)
    TextView mTvMotionState;//运行状态

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
    private AdmeBaseInfo admeBaseInfo;

    private int equipModellPos;
    private String equipModel;//设备模式

    private Handler motionStateHander = new Handler();
    private QueryMotorStateRunnable queryMotorStateRunnable;

    private boolean isFirstCreate = false;


    /**
     * 查询设备运行状态
     */
    private class QueryMotorStateRunnable implements Runnable {
        @Override
        public void run() {
            if (!isActive)
                return;
            queryMotorState();
        }
    }

    private void startQueryMotorStateRunnable() {
        if (queryMotorStateRunnable != null && isActive) {
            motionStateHander.postDelayed(queryMotorStateRunnable, 20000);
        }
    }

    private void stopQueryMotorStateRunnable() {
        motionStateHander.removeCallbacksAndMessages(null);
        queryMotorStateRunnable = null;
    }

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
        isFirstCreate = true;
        queryMotorStateRunnable = new QueryMotorStateRunnable();
        setHeadInfo();
        initAdapter();
        initConfigModuleData();
        observerApiKey();
        observerConnectionState();

        progressOverlay.postDelayed(connectTimeOut, 15000);
        //建立蓝牙连接
        connectDevice(device.getDevice());
    }

    private Runnable connectTimeOut = new Runnable() {
        @Override
        public void run() {
            if (!isConnected()) {
                hideProgressBar();
                disconnectDevice();
                ToastUtils.show("连接超时");
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        onConnectionStateChanged(isConnected());
        if (!isFirstCreate) {
            queryMotorStateRunnable = new QueryMotorStateRunnable();
            startQueryMotorStateRunnable();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        isFirstCreate = false;
        progressOverlay.removeCallbacks(connectTimeOut);
        stopQueryMotorStateRunnable();
    }

    private void setHeadInfo() {
        mTvDeviceName.setText("水平自动监测设备");
        mTvDeviceSn.setText(String.format("设备编号：%s", device.getName().substring(3)));
        mTvProductModel.setText("产品型号：--");
        mTvMotionState.setText("运行状态：--");
        mTvPlatformCommunicationState.setText("米度平台连接状态：--");
        mTvDeviceConnectOperate.setVisibility(View.VISIBLE);
        mTvDeviceConnectOperate.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

        equipModellPos = 0;
        mTvConfigModel.setText("设备配置模式");
    }

    private void initAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(mActivity, 15);//每一个矩形的间距
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
                DeviceCurrentStateActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, AppContants.DeviceType.ADME);
                break;

            case "基础配置":
                AdmeBasicParamActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);
                break;

            case "测量孔深":
                AdmeMeasuringHoleDepthActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);
                break;

            case "导槽校准":
                AdmeGuideGrooveCalibrationActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);
                break;

            case "数据中心":
                DataCenterHomeActivity.startActivity(mActivity, AppContants.DeviceType.ADME, AppContants.CommunicationWay.BLE_CONNECT, AppContants.DataCenterConfigMethod.BASIC_CONFIG);
                break;

            case "指令下发":
                CustomCommandLogPrintActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, AppContants.DeviceType.ADME);
                break;

            case "高级配置":
                AdmeAdvancedConfigActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);
                break;

            case "设置":
                AdvancedSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, AppContants.DeviceType.ADME);
                break;
        }
    }

    /**
     * 观察连接状态变化
     */
    private void observerConnectionState() {
        usrBleViewModel.getConnectionState().observe(getViewLifecycleOwner(), new Observer<ConnectionState>() {
            @Override
            public void onChanged(ConnectionState connectionState) {
                switch (connectionState.getState()) {
                    case CONNECTING://A connection to the device was initiated.
                        showProgressBar();
                        mTvProgressText.setText(R.string.ble_state_connecting);
                        break;

                    case INITIALIZING://The device has connected and begun service discovery and initialization.
//                        mTvConnectState.setText(R.string.ble_state_initializing);
                        break;

                    case READY://The initialization is complete, and the device is ready to use.
                        onConnectionStateChanged(true);
                        mTvProgressText.setText("初始化中...");
                        usrBleViewModel.queryDeviceApiKeyBySn(device.getDevice().getName().substring(3));
                        break;

                    case DISCONNECTED://The device disconnected or failed to connect.
                        if (connectionState instanceof ConnectionState.Disconnected) {
                            final ConnectionState.Disconnected stateWithReason = (ConnectionState.Disconnected) connectionState;
                            if (stateWithReason.isNotSupported()) {
                                Timber.e("DISCONNECTED: 不支持的设备");
                                ToastUtils.show("不支持的设备");
                            } else if (stateWithReason.isTimeout()) {
                                Timber.e("DISCONNECTED: 连接超时");
//                                ToastUtils.show("连接超时");
                            }
                        }
                        clearDevice();
                        hideProgressBar();
                        onConnectionStateChanged(false);
                        break;

                    // fallthrough
                    case DISCONNECTING://The disconnection was initiated.
                        break;
                }
            }
        });
    }

    /**
     * 观察获取 ApiKey
     */
    private void observerApiKey() {
        usrBleViewModel.getDeviceApiKey().observeInFragment(this, new Observer<String>() {
            @Override
            public void onChanged(String apiKey) {
                hideProgressBar();
                queryEquipmentBaseInfo();
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
        progressOverlay.removeCallbacks(connectTimeOut);
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

        configModule = new ConfigModule(R.drawable.ic_measuring_hole_depth, "测量孔深", "测量测斜管深度");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_positive_and_negative_test, "导槽校准", "正反测起点校准");
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

    /**
     * 获取设备的基本信息
     */
    private void queryEquipmentBaseInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_EQUIPMENT_BASIS);
        sendCommand(command);
    }

    /**
     * 获取电机的运行状态
     */
    private void queryMotorState() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_MOTION_STATE);
        sendCommand(command);
    }

    @OnClick({R.id.tv_device_connect_operate, R.id.ll_switch_config_model})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }
        int id = v.getId();
        if (id == R.id.tv_device_connect_operate) {//断开/重新连接
            if (!isConnected()) {
                progressOverlay.postDelayed(connectTimeOut, 15000);
                connectDevice(device.getDevice());
            } else {//断开连接处理
                isExitMode = false;
                showDisconnectDialog(getResources().getString(R.string.disconnect_device));
            }
        } else if (id == R.id.ll_switch_config_model) {//切换设备模式
            showSwitchConfigModelDialog();
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
                                    admeViewModel.deviceMode = 0;
                                } else {
                                    equipModel = "1";
                                    admeViewModel.deviceMode = 1;
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
                IOTCommandResult<AdmeBaseInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取设备的基本信息出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                admeBaseInfo = commandResult.getResult();
                updateHeadInfo();
                //获取设备的运行状态
                queryMotorState();
            }
            break;

            case ADME_MD_GET_MOTION_STATE: {//获取ADME的运行状态
                IOTCommandResult<AdmeMotionState> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取设备的运行状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                AdmeMotionState admeMotionState = commandResult.getResult();
                updateMotionState(admeMotionState);
                startQueryMotorStateRunnable();
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
                saveConfigInfo();
            }
            break;

            case MD_SAVE_CONFIG_PARAM: {
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "保存指令出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
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
        if (admeBaseInfo != null) {
            mTvDeviceName.setText("水平自动监测设备");
            mTvDeviceSn.setText(String.format("设备编号：%s", !TextUtils.isEmpty(admeBaseInfo.getSn()) ? admeBaseInfo.getSn() : device.getName().substring(3)));
            mTvProductModel.setText(String.format("产品型号：%s", !TextUtils.isEmpty(admeBaseInfo.getProductid()) ? admeBaseInfo.getProductid() : "ADME"));
            mTvMotionState.setText("运行状态：--");
            if (!TextUtils.isEmpty(admeBaseInfo.getOnline()) && !admeBaseInfo.getOnline().equals("0")) {
                mTvPlatformCommunicationState.setText("米度平台连接状态：在线");

            } else if (admeBaseInfo.getOnline().equals("0")) {
                mTvPlatformCommunicationState.setText(getPlatformAbnormalMessage("离线"));
            }

            if (!TextUtils.isEmpty(admeBaseInfo.getEquimodel())) {
                equipModel = admeBaseInfo.getEquimodel();
                if (equipModel.equals("0")) {
                    admeViewModel.deviceMode = 0;
                    equipModellPos = 0;
                    mTvConfigModel.setText("设备配置模式");
                } else {
                    admeViewModel.deviceMode = 1;
                    equipModellPos = 1;
                    mTvConfigModel.setText("自动监测模式");
                }
                updateConfigModuleData();
            }
        } else {
            mTvDeviceName.setText("水平自动监测设备");
            mTvDeviceSn.setText(String.format("设备编号：%s", device.getName().substring(3)));
            mTvProductModel.setText(String.format("产品型号：：%s", "ADME"));
            mTvMotionState.setText("运行状态：--");
            mTvPlatformCommunicationState.setText("米度平台连接状态：--");

            admeViewModel.deviceMode = 0;
            equipModellPos = 0;
            mTvConfigModel.setText("设备配置模式");
        }
    }

    private CharSequence getPlatformAbnormalMessage(String state) {
        SpannableStringBuilder builder = new SpannableStringBuilder(state);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.red));
        builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.insert(0, "米度平台连接状态：");

        return builder;
    }

    /**
     * 刷新电机运动状态
     */
    private void updateMotionState(AdmeMotionState admeMotionState) {
        if (admeMotionState == null) {
            Timber.e("AdmeMotionState is Null!");
            return;
        }
        switch (admeMotionState.getMotionstate()) {
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

    private void updateConfigModuleData() {
        if (TextUtils.isEmpty(equipModel))
            return;

        configModuleList.clear();
        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_state, GlobalUtil.getString(R.string.device_config_module_current_state), "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_basic_config, "基础配置", "设备基础参数配置");
        configModuleList.add(configModule);

        if (equipModel.equals("0")) {//0：设备配置模式，1：自动监测模式
            configModule = new ConfigModule(R.drawable.ic_measuring_hole_depth, "测量孔深", "测量测斜管深度");
            configModuleList.add(configModule);

            configModule = new ConfigModule(R.drawable.ic_positive_and_negative_test, "导槽校准", "正反测起点校准");
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
    public void onDestroy() {
        admeViewModel.deviceMode = -1;
        MCloudApp.setCurDeviceToken(null);
        super.onDestroy();
    }
}