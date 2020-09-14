package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.cmd.CommandManager;
import com.shmedo.core.cmd.CommandResult;
import com.shmedo.core.enums.CollectorModel;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.event.BluetoothConnectStateEvent;
import com.shmedo.core.event.CmdResponseMessage;
import com.shmedo.core.event.MessageEvent;
import com.shmedo.core.model.BaseConfigInfo;
import com.shmedo.core.model.BreakAlarmStatusInfo;
import com.shmedo.core.model.CollectorConfigInfo;
import com.shmedo.core.model.LoaclTimeInfo;
import com.shmedo.core.model.QueryOsmometerParameterInfo;
import com.shmedo.core.model.SetRainPrecisionInfo;
import com.shmedo.core.model.VersionMessageInfo;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.core.utils.ResultParserUtil;
import com.shmedo.core.utils.StringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.ConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.AdvancedSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceCurrentStateActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.IOTCollectorSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.BaseDispatchCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.QueryTerminalTimeDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.TelemetryDialog;
import com.shmedo.mcloudapp.util.bleutil.BlueResultParserUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class BleConfigDeviceFragment extends BaseBleConnectFragment {
    private static final String DEVICE_INFO = "device_info";

    private static final int DEVICE_ACTIVE = 0x0002;

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;

    @BindView(R.id.tv_product_model)
    TextView mTvProductModel;

    @BindView(R.id.tv_time_or_sub_model)
    TextView mTvSubModel;

    @BindView(R.id.tv_device_communication_state_flag)
    TextView mTvDeviceCommunicationState;//通信状态(在线、离线、已连接、已断开)

    @BindView(R.id.tv_device_connect_state)
    TextView mTvDeviceConnectState;//蓝牙连接状态(断开连接、重新连接)

    @BindView(R.id.tv_device_communication_way)
    TextView mTvDeviceCommunicationWay;//通信方式(网络、蓝牙)

    @BindView(R.id.tv_active_state)
    TextView mTvActiveState;

    @BindView(R.id.activeStateSwBtn)
    SwitchButton mSbActiveState;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private ConfigModuleAdapter moduleAdapter;

    private List<ConfigModule> configModuleList = new ArrayList<>();

    private String bleNameInfo;

    private ConfigModule selectedConfigModule;

    private String collectorModel = "";//采集器类型
    private SetRainPrecisionInfo setRainPrecisionInfo;
    private CollectorConfigInfo collectorConfigInfo;
    private BaseConfigInfo baseConfigInfo;
    private BreakAlarmStatusInfo breakAlarmStatusInfo;
    private QueryOsmometerParameterInfo queryOsmometerParameterInfo;


    public static BleConfigDeviceFragment newInstance(String deviceInfo) {
        BleConfigDeviceFragment fragment = new BleConfigDeviceFragment();
        Bundle args = new Bundle();
        args.putString(DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bleNameInfo = getArguments().getString(DEVICE_INFO);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_config_device;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHeadInfo();
        initSwitchViewListener();
        initAdapter();
        initConfigModuleData();

        findAndConnectSpecificDevice();
    }


    @Override
    public void onResume() {
        super.onResume();
        updateViewStateByConnectState(MCloudApp.isIsBluetoothDeviceConnected());
    }

    private void setHeadInfo() {
        String[] infos = bleNameInfo.split(",");
        mTvDeviceName.setText("物联网数据采集器");
        if (infos.length >= 3) {
            mTvDeviceSn.setText(String.format("设备编号：%s", TextUtils.isEmpty(infos[1]) ? "" : infos[1]));
            mTvProductModel.setText(String.format("产品型号：%s", TextUtils.isEmpty(infos[2]) ? "" : infos[2]));
        }
        mTvDeviceConnectState.setVisibility(View.VISIBLE);
        mTvDeviceConnectState.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mTvDeviceCommunicationWay.setText("蓝牙");
    }

    /**
     * switch按钮事件
     */
    private void initSwitchViewListener() {
        //设备启用状态开关
        mSbActiveState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    mSbActiveState.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }

                if (isChecked) {
                    //发送激活DAS命令
                    setLowEnergyModel(true);
                    mTvActiveState.setText("已激活");
                } else {
                    showCloseSwitchButtonDialog(getString(R.string.device_enable_state_close_warn), DEVICE_ACTIVE);
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
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
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
                DeviceCurrentStateActivity.startActivity(mActivity, DeviceCurrentStateActivity.BLE_CONNECT);
                break;

            case "时间":
                queryTimeCmd();
                break;

            case "遥测":
                telemetryCmd();
                break;

            case "重启":

                break;

            case "固件升级":

                break;

            case "采集器配置":
                IOTCollectorSettingActivity.startActivity(mActivity, IOTCollectorSettingActivity.BLE_CONNECT, collectorModel);
                break;

            case "传感器配置":

                break;

            case "数据中心":

                break;

            case "设置":
                AdvancedSettingActivity.startActivity(mActivity, null);
                break;
        }
    }

    private void queryTimeCmd() {
        showLoadingDialog("指令下发中...");
        String command = CommandManager.getInstance().getCommand(CommandType.LOCAL_TIME, null);
        sendCommonCommandImmediately(command);
        Timber.d("获取设备时间信息指令===%s", command);
    }

    private void telemetryCmd() {
        showLoadingDialog("指令下发中...");
        String command = CommandManager.getInstance().getCommand(CommandType.INSTANT_COLLEACTOR, null);
        sendCommonCommandImmediately(command);
        Timber.d("遥测设备指令===%s", command);
    }

    @OnClick({R.id.tv_device_connect_state, R.id.tv_device_communication_way})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_device_connect_state://断开/重新连接
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    findAndConnectSpecificDevice();
                } else {//断开连接处理
                    if (isConfigChange) {
                        isExitMode = false;
                        showSaveDialog(getResources().getString(R.string.disconnect_bluetooth_device_save_param_warn));
                    } else {
                        disconnectDevice();
                        updateViewStateByConnectState(false);
                    }
                }
                break;

            case R.id.tv_device_communication_way://切换连接方式
                break;
        }
    }

    /**
     * 关闭SwitchButton
     */
    private void showCloseSwitchButtonDialog(String content, final int index) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getActivity())
                .title("温馨提示：")
                .content(content)
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        if (index == DEVICE_ACTIVE) {//发送关闭DAS命令
                            setLowEnergyModel(false);
                            mTvActiveState.setText("已待机");
                        }
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        if (index == DEVICE_ACTIVE) {
                            mSbActiveState.setCheckedImmediatelyNoEvent(true);
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent messageEvent) {
        super.onMessageEvent(messageEvent);

        if (messageEvent instanceof CmdResponseMessage) {
            setResultData((CmdResponseMessage) messageEvent);
        } else if (messageEvent instanceof BluetoothConnectStateEvent) {
            boolean isConnected = ((BluetoothConnectStateEvent) messageEvent).isConnected;
            updateViewStateByConnectState(isConnected);
        }
    }

    private void setResultData(CmdResponseMessage responseMessage) {
        String cmdStr = responseMessage.getResult();
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
                queryDeviceVersionInfo();
                break;

            case VERSION_MESSAGE:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("查询版本信息指令出错!");
                    return;
                }
                VersionMessageInfo versionMessageInfo = ResultParserUtil.getEntityObject(cmdStr);
                initVersionInfo(versionMessageInfo);
                break;

            case LOCAL_TIME:
                dismissLoadingDialog();
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
                dismissLoadingDialog();
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("遥测指令出错!");
                    ToastUtils.show("遥测指令出错!");
                    return;
                }
                BaseDispatchCmdDialog newFragment = new TelemetryDialog("遥测", tempStr);
                newFragment.show(getChildFragmentManager(), "dialog");
            }
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

        setRainPrecisionInfo = new SetRainPrecisionInfo();
        setRainPrecisionInfo.setPrecision((double) baseConfigInfo.getRainAccuracy() / 100);
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
        for (ConfigModule configModule : configModuleList) {
            if (configModule.getName().equals("固件升级")) {
                configModule.setDesc("版本:" + firmwareVersion);
                break;
            }
        }
        moduleAdapter.notifyDataSetChanged();
    }

    private void updateViewStateByConnectState(boolean isConnected) {
        if (isConnected) {
            mTvDeviceCommunicationState.setText("已连接");
            mTvDeviceCommunicationState.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_50E9B9));
            mTvDeviceCommunicationState.setBackgroundResource(R.drawable.bg_device_online_state_flag);

            mTvDeviceConnectState.setText("断开连接");
            mTvDeviceConnectState.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_b3b3b3));
        } else {
            mTvDeviceCommunicationState.setText("已断开");
            mTvDeviceCommunicationState.setTextColor(ContextCompat.getColor(mActivity, R.color.sub_title_text_color));
            mTvDeviceCommunicationState.setBackgroundResource(R.drawable.bg_device_offline_state_flag);

            mTvDeviceConnectState.setText("重新连接");
            mTvDeviceConnectState.setTextColor(ContextCompat.getColor(mActivity, R.color.blue_52B4F8));

            mSbActiveState.setCheckedImmediatelyNoEvent(false);
        }
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

        configModule = new ConfigModule(R.drawable.ic_device_firmware_upgrade, 24, "固件升级", "版本:--");
        configModuleList.add(configModule);

        //DAS具有采集器配置项
        if (mTvProductModel.getText().toString().contains("DAS")) {
            configModule = new ConfigModule(R.drawable.ic_device_collector_config, "采集器配置", "采集器参数配置");
            configModuleList.add(configModule);

            configModule = new ConfigModule(R.drawable.ic_device_sensor_config, "传感器配置", "传感器参数配置");
            configModuleList.add(configModule);

            configModule = new ConfigModule(R.drawable.ic_device_data_center, "数据中心", "MQTT协议配置");
            configModuleList.add(configModule);
        }

        configModule = new ConfigModule(R.drawable.ic_device_advanced_setting, "设置", "高级设置");
        configModuleList.add(configModule);
    }

    @Override
    public boolean onBackPressed() {
        if (MCloudApp.isIsBluetoothDeviceConnected()) {
            if (isConfigChange) {
                isExitMode = true;
                showSaveDialog(getResources().getString(R.string.disconnect_bluetooth_device_save_param_warn));

            } else {
                showDisconnectDialog(getResources().getString(R.string.finish_activity_disconnect_bluetooth_device));
            }
        } else {
            mActivity.finish();
        }

        return false;
    }
}
