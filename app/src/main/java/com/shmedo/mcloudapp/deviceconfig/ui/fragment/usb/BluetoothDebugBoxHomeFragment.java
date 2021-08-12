package com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb;

import android.graphics.Paint;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class BluetoothDebugBoxHomeFragment extends BaseUSBSerialCommunicateFragment {
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


    public static BluetoothDebugBoxHomeFragment newInstance(int deviceId, int port, int baudRate) {
        BluetoothDebugBoxHomeFragment fragment = new BluetoothDebugBoxHomeFragment();
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
            case "名字":
                queryDeviceName();
                break;

            case "版本号":
                queryDeviceVersion();
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
                        setCommandMode();
                        break;

                    case DISCONNECTED://The device disconnected or failed to connect.
                        if (usbConnectionState instanceof USBConnectionState.Disconnected) {
                            final USBConnectionState.Disconnected stateWithReason = (USBConnectionState.Disconnected) usbConnectionState;
                            if (!TextUtils.isEmpty(stateWithReason.getReason())) {
                                Timber.e(stateWithReason.getReason());
                                ToastUtils.show(stateWithReason.getReason());
                            }
                        }
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
        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_state, "配置连接", "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_current_state, "状态", "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_current_time, "电压数据查询", "获取当前设备时间");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_current_time, "测量数据查询", "获取当前设备时间");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_setting, "其他配置", "高级设置");
        configModuleList.add(configModule);
    }

    private void setCommandMode() {
        usbSerialViewModel.sendData("+++a");
    }

    private void queryDeviceName() {
        String command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.NAME.toString() + ATCommand.QUERY_FLAG + ATCommand.NEWLINE_CRLF;
        usbSerialViewModel.sendData(command);
    }

    private void queryDeviceVersion() {
        String command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.CIVER.toString() + ATCommand.QUERY_FLAG + ATCommand.NEWLINE_CRLF;
        usbSerialViewModel.sendData(command);
    }

    @OnClick({R.id.tv_device_connect_operate})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }
        if (v.getId() == R.id.tv_device_connect_operate) {//断开/重新连接
            if (!isConnected()) {
//                startProgress(null, AppContants.MsgWhat.CONNECT_DEVICE, CONNECT_TIME_OUT_MILLIS);
                connectDevice();
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

    private void setResultData(String cmdStr) {
        resultBuilder.append(cmdStr);
        if (resultBuilder.toString().contains("a") && resultBuilder.toString().contains("+ok")) {
            resultBuilder.setLength(0);
            queryDeviceName();
        }
        if (resultBuilder.toString().contains(ATCommand.OK_FLAG)) {
            cmdStr = resultBuilder.toString();
            resultBuilder.setLength(0);
            if (cmdStr.contains(WHBLE102CommandType.NAME.toString()) && cmdStr.contains(ATCommand.OK_FLAG)) {
                cmdStr = cmdStr.replace(ATCommand.OK_FLAG, "")
                        .replace(ATCommand.NEWLINE_CR, "")
                        .replace(ATCommand.NEWLINE_LF, "");
//                    .trim();
                cmdStr = cmdStr.replace(ATCommand.COMMAND_RESULT_HEADER + WHBLE102CommandType.NAME.toString() + ATCommand.DELIMITER_COLON, "");
                mTvDeviceName.setText(cmdStr);
                queryDeviceVersion();
            }

            if (cmdStr.contains("VER") && cmdStr.contains(ATCommand.OK_FLAG)) {
                cmdStr = cmdStr.replace(ATCommand.OK_FLAG, "")
                        .replace(ATCommand.NEWLINE_CR, "")
                        .replace(ATCommand.NEWLINE_LF, "");
//                    .trim();
                cmdStr = cmdStr.replace(ATCommand.COMMAND_RESULT_HEADER + "VER" + ATCommand.DELIMITER_COLON, "");
                mTvDeviceSn.setText(String.format("固件版本：%s", cmdStr));
            }
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

}