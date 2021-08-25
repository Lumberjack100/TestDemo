package com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.bluetooth_debug_box;

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

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.at.ATCommand;
import com.shmedo.configlibrary.at.WHBLE102CommandType;
import com.shmedo.core.AppContants;
import com.shmedo.core.usbserial.livedata.state.USBConnectionState;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.ConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.model.usb_serial.ATCommandItem;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DebugCommandLoggerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.usb.CollectionConfigurationActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.BaseUSBSerialCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.bluetooth_debug_box.dialog.CommunicationTimeDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.bluetooth_debug_box.dialog.ConfigBluetoothMacDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.bluetooth_debug_box.dialog.ConfigWorkModeDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.bluetooth_debug_box.dialog.QueryBluetoothLinkStatusDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.bluetooth_debug_box.dialog.QueryMeasurementDataDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.bluetooth_debug_box.dialog.QueryVoltageDataDialogFragment;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  8/13/21 <br/>
 * 描述：    蓝牙测斜仪调试盒子配置主页面
 */
public class InclinometerDebugBoxHomeFragment extends BaseUSBSerialCommunicateFragment {
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

    private int deviceId, portNum, baudRate;
    private boolean initialStart = true;


    public static InclinometerDebugBoxHomeFragment newInstance(int deviceId, int port, int baudRate) {
        InclinometerDebugBoxHomeFragment fragment = new InclinometerDebugBoxHomeFragment();
        Bundle args = new Bundle();
        args.putInt(AppContants.Extras.DEVICE_ID, deviceId);
        args.putInt(AppContants.Extras.USB_PORT_NUM, port);
        args.putInt(AppContants.Extras.USB_BAUD_RATE, baudRate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deviceId = getArguments().getInt(AppContants.Extras.DEVICE_ID);
            portNum = getArguments().getInt(AppContants.Extras.USB_PORT_NUM);
            baudRate = getArguments().getInt(AppContants.Extras.USB_BAUD_RATE);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.bluetooth_debug_box_home_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHeadInfo();
        initAdapter();
        initConfigModuleData();
        observerConnectionState();

        usbSerialViewModel.initUsb(deviceId, portNum, baudRate);
        //建立USB连接
        connectDevice();
    }

    private void setHeadInfo() {
        mTvDeviceName.setText("--");
        mTvDeviceSn.setText(String.format("固件版本：%s", "--"));
        mTvProductModel.setVisibility(View.GONE);
        mTvFirmwareVersion.setVisibility(View.GONE);
        mTvPlatformCommunicationState.setVisibility(View.GONE);
        mTvDeviceConnectOperate.setVisibility(View.VISIBLE);
        mTvDeviceConnectOperate.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
    }

    private void initAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(mActivity, 15);//每一个矩形的间距
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
                    ToastUtils.show(getString(R.string.usb_config_disconnect_warn));
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
            case "连接配置":
                ConfigBluetoothMacDialogFragment configLinkMacDialogFragment = ConfigBluetoothMacDialogFragment.newInstance();
                configLinkMacDialogFragment.show(getChildFragmentManager(), "dialog");
                break;

            case "状态":
                QueryBluetoothLinkStatusDialogFragment queryBluetoothLinkStatusDialogFragment = QueryBluetoothLinkStatusDialogFragment.newInstance();
                queryBluetoothLinkStatusDialogFragment.show(getChildFragmentManager(), "dialog");
                break;

            case "电压数据读取":
                QueryVoltageDataDialogFragment queryVoltageDataDialogFragment = QueryVoltageDataDialogFragment.newInstance();
                queryVoltageDataDialogFragment.show(getChildFragmentManager(), "dialog");
                break;

            case "测量数据读取":
                QueryMeasurementDataDialogFragment queryMeasurementDataDialogFragment = QueryMeasurementDataDialogFragment.newInstance();
                queryMeasurementDataDialogFragment.show(getChildFragmentManager(), "dialog");
                break;

            case "模式配置":
                ConfigWorkModeDialogFragment configWorkModeDialogFragment = ConfigWorkModeDialogFragment.newInstance();
                configWorkModeDialogFragment.show(getChildFragmentManager(), "dialog");
                break;

            case "待机时间配置":
                CommunicationTimeDialogFragment communicationTimeDialogFragment = CommunicationTimeDialogFragment.newInstance();
                communicationTimeDialogFragment.show(getChildFragmentManager(), "dialog");
                break;

            case "其他参数配置":
                CollectionConfigurationActivity.startActivity(mActivity);
                break;

            case "指令调试":
                DebugCommandLoggerActivity.startActivity(mActivity, AppContants.CommunicationWay.USB_SERIAL, AppContants.DeviceType.INCLINOMETER_DEBUG_BOX);
                break;
        }
    }

    /**
     * 观察连接状态变化
     */
    private void observerConnectionState() {
        usbSerialViewModel.getConnectionState().observe(getViewLifecycleOwner(), new Observer<USBConnectionState>() {
            @Override
            public void onChanged(USBConnectionState usbConnectionState) {
                switch (usbConnectionState.getState()) {
                    case CONNECTING://A connection to the device was initiated.
                        break;

                    case READY://The initialization is complete, and the device is ready to use.
                        onConnectionStateChanged(true);
                        connectInitialCommands();
                        sendCommandFromCmdList(AppContants.MsgWhat.USB_SERIAL_DEVICE_INITIAL, WRITE_TIME_OUT_500_MILLIS);
                        break;

                    case DISCONNECTED://The device disconnected or failed to connect.
                        if (usbConnectionState instanceof USBConnectionState.Disconnected) {
                            final USBConnectionState.Disconnected stateWithReason = (USBConnectionState.Disconnected) usbConnectionState;
                            if (!TextUtils.isEmpty(stateWithReason.getReason())) {
                                Timber.e(stateWithReason.getReason());
                                ToastUtils.show(stateWithReason.getReason());
                            }
                        }
                        ToastUtils.show("USB连接断开");
                        onConnectionStateChanged(false);
                        break;
                }
            }
        });
    }

    /**
     * USB连接/断开回调，更新页面头部信息
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

    private void initConfigModuleData() {
        configModuleList.clear();
        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_sensor_config, "连接配置", "测斜仪连接配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_current_state, "状态", "查询测斜仪连接状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_telemetry, "电压数据读取", "获取电压数据");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_telemetry, "测量数据读取", "获取测量数据");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_sensor_config, "模式配置", "配置测斜仪工作模式");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_sensor_config, "待机时间配置", "配置测斜仪待机时间");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_setting, "其他参数配置", "其他参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_setting, "指令调试", "指令调试日志");
        configModuleList.add(configModule);
    }

    /**
     * 连接后初始化指令
     */
    private void connectInitialCommands() {
        atCommandItems.clear();

        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.ENTER_COMMAND, WHBLE102CommandType.ENTER_COMMAND.toString());
        atCommandItems.add(atCommandItem);//进入命令模式

        if (initialStart) {
            String command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.MODE.toString() + "=M" + ATCommand.NEWLINE_CRLF;
            atCommandItem = new ATCommandItem(WHBLE102CommandType.MODE, command);
            atCommandItems.add(atCommandItem);//设置主设备模式

            atCommandItem = new ATCommandItem(WHBLE102CommandType.ENTER_COMMAND, WHBLE102CommandType.ENTER_COMMAND.toString());
            atCommandItems.add(atCommandItem);//重新进入命令模式
        }
    }

    /**
     * 查询 USB设备名称和版本号
     */
    private void queryDeviceNameAndVerison() {
        atCommandItems.clear();

        String command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.NAME.toString() + ATCommand.QUERY_FLAG + ATCommand.NEWLINE_CRLF;
        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.NAME, command);
        atCommandItems.add(atCommandItem);

        command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.CIVER.toString() + ATCommand.QUERY_FLAG + ATCommand.NEWLINE_CRLF;
        atCommandItem = new ATCommandItem(WHBLE102CommandType.CIVER, command);
        atCommandItems.add(atCommandItem);
    }

    @OnClick({R.id.tv_device_connect_operate})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }
        if (v.getId() == R.id.tv_device_connect_operate) {//断开/重新连接
            if (!isConnected()) {
                connectDevice();
            } else {//断开连接处理
                isExitMode = false;
                showDisconnectDialog(getResources().getString(R.string.disconnect_device));
            }
        }
    }

    @Override
    protected void parseResponseMessage(byte[] data) {
        if (!isActive) {
            return;
        }
        try {
            resultByteBuf.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        if (msg.what == AppContants.MsgWhat.USB_SERIAL_DEVICE_INITIAL) {
            String cmdStr = resultByteBuf.toString();
            resultByteBuf.reset();
            Timber.e("接收串口数据: %s", cmdStr);
            if (atCommandItems.size() == 0)
                return;

            ATCommandItem commandItem = atCommandItems.getFirst();
            atCommandItems.removeFirst();//移除已经发送完的指令
            switch (commandItem.getCommandType()) {
                case ENTER_COMMAND: {
//                    if (cmdStr.contains("a+ok") || TextUtils.isEmpty(cmdStr)) {
//                        if (atCommandItems.size() == 0 && initialStart) {
//                            initialStart = false;
//                            queryDeviceNameAndVerison();
//                        }
//                        sendCommandFromCmdList(AppContants.MsgWhat.USB_SERIAL_DEVICE_INITIAL, WRITE_TIME_OUT_MILLIS);
//                    } else
                    if (cmdStr.toUpperCase().contains("ERR")) {
                        cmdStr = filterControlCharacter(commandItem.getCommand());
                        Timber.e("%s  出错", cmdStr);
                        ToastUtils.show(cmdStr + "  出错");
                    } else {
                        if (atCommandItems.size() == 0 && initialStart) {
                            initialStart = false;
                            queryDeviceNameAndVerison();
                        }
                        sendCommandFromCmdList(AppContants.MsgWhat.USB_SERIAL_DEVICE_INITIAL, WRITE_TIME_OUT_500_MILLIS);
                    }
                }
                break;

                case MODE: {
//                    if (cmdStr.contains(WHBLE102CommandType.MODE.toString()) && cmdStr.contains(ATCommand.OK_FLAG)) {
//                        sendCommandFromCmdList(AppContants.MsgWhat.USB_SERIAL_DEVICE_INITIAL, WRITE_TIME_OUT_MILLIS);
//                    } else
                    if (cmdStr.toUpperCase().contains("ERR")) {
                        cmdStr = filterControlCharacter(commandItem.getCommand());
                        Timber.e("%s  出错", cmdStr);
                        ToastUtils.show(cmdStr + "  出错");
                    } else {
                        sendCommandFromCmdList(AppContants.MsgWhat.USB_SERIAL_DEVICE_INITIAL, WRITE_TIME_OUT_500_MILLIS);
                    }
                }
                break;

                case NAME: {
//                    if (cmdStr.contains(WHBLE102CommandType.NAME.toString()) && cmdStr.contains(ATCommand.OK_FLAG)) {
//                        cmdStr = filterControlCharacter(cmdStr);
//                        cmdStr = cmdStr.replace(ATCommand.COMMAND_RESULT_HEADER + WHBLE102CommandType.NAME.toString() + ATCommand.DELIMITER_COLON, "");
//                        mTvDeviceName.setText(cmdStr);
//
//                        sendCommandFromCmdList(AppContants.MsgWhat.USB_SERIAL_DEVICE_INITIAL, WRITE_TIME_OUT_500_MILLIS);
//                    } else
                    if (cmdStr.toUpperCase().contains("ERR")) {
                        cmdStr = filterControlCharacter(commandItem.getCommand());
                        Timber.e("%s  出错", cmdStr);
                        ToastUtils.show(cmdStr + "  出错");
                    } else {
                        cmdStr = filterControlCharacter(cmdStr);
                        cmdStr = cmdStr.replace(ATCommand.COMMAND_RESULT_HEADER + WHBLE102CommandType.NAME.toString() + ATCommand.DELIMITER_COLON, "");
                        mTvDeviceName.setText(cmdStr);

                        sendCommandFromCmdList(AppContants.MsgWhat.USB_SERIAL_DEVICE_INITIAL, WRITE_TIME_OUT_500_MILLIS);
                    }
                }
                break;

                case CIVER: {
//                    if (cmdStr.contains("VER") && cmdStr.contains(ATCommand.OK_FLAG)) {
//                        cmdStr = filterControlCharacter(cmdStr);
//                        cmdStr = cmdStr.replace(ATCommand.COMMAND_RESULT_HEADER + "VER" + ATCommand.DELIMITER_COLON, "");
//                        mTvDeviceSn.setText(String.format("固件版本：%s", cmdStr));
//
//                    } else
                    if (cmdStr.toUpperCase().contains("ERR")) {
                        cmdStr = filterControlCharacter(commandItem.getCommand());
                        Timber.e("%s  出错", cmdStr);
                        ToastUtils.show(cmdStr + "  出错");
                    } else {
                        cmdStr = filterControlCharacter(cmdStr);
                        cmdStr = cmdStr.replace(ATCommand.COMMAND_RESULT_HEADER + "VER" + ATCommand.DELIMITER_COLON, "");
                        mTvDeviceSn.setText(String.format("固件版本：%s", cmdStr));
                    }
                }
                break;
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        if (isConnected()) {
            isExitMode = true;
            showDisconnectDialog(getResources().getString(R.string.finish_activity_disconnect_usb_device));
            return true;
        }
        return false;
    }

}