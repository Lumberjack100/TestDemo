package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dms;

import android.graphics.Paint;
import android.os.Bundle;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.DebouncingUtils;
import com.blankj.utilcode.util.StringUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.interfaces.BaseDialog;
import com.kongzue.dialogx.interfaces.OnBackPressedListener;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.ConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceBaseInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DiscoveredBluetoothDevice;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.CommonSensorHomeActivity;
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
 * 创建时间:  2023/1/3 <br/>
 * 描述：      DMS 蓝牙配置主页面
 */
public class BleDmsHomeFragment extends BaseUSRBleIotCommunicateFragment {
    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;//设备名称

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;//设备SN号

    @BindView(R.id.tv_product_name)
    TextView mTvProductName;//所属产品

    @BindView(R.id.tv_firmware_version)
    TextView mTvFirmwareVersion;//固件版本

    @BindView(R.id.tv_extended_field3)
    TextView mTvExtendedField;//

    @BindView(R.id.tv_platform_communication_state)
    TextView mTvPlatformCommunicationState;//与平台通信状态(文字标识)

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

    public static BleDmsHomeFragment newInstance(DiscoveredBluetoothDevice device) {
        BleDmsHomeFragment fragment = new BleDmsHomeFragment();
        Bundle args = new Bundle();
        args.putParcelable(AppContants.Extras.BLE_DEVICE, device);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            device = getArguments().getParcelable(AppContants.Extras.BLE_DEVICE);
            sn = device.getName().replaceFirst("(MD)(-?)", "");
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.universal_config_home_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initAdapter();
        updateHeadInfo();
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
                if(!DebouncingUtils.isValid(view, 1000)) {
                    return;
                }
                if (!isConnected()) {
                    ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
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
            case "传感器配置":
                CommonSensorHomeActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);
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
                        bleViewModel.deviceRequest.queryDeviceApiKeyBySn(sn);
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
                    public boolean onBackPressed(BaseDialog dialog) {
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

        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_sensor_config, "传感器配置", "传感器参数配置");
        configModuleList.add(configModule);
    }

    @OnClick({R.id.tv_device_connect_operate})
    public void onClick(View view) {
        if(!DebouncingUtils.isValid(view, 1000)) {
            return;
        }
        if (view.getId() == R.id.tv_device_connect_operate) {//断开/重新连接
            if (!isConnected()) {
                connectDevice(device.getDevice());
            } else {//断开连接处理
                isExitMode = false;
                showDisconnectDialog(StringUtils.getString(R.string.disconnect_device));
            }
        }
    }

    /**
     * 更新头部信息
     */
    private void updateHeadInfo() {
        mTvExtendedField.setVisibility(View.GONE);
        mTvDeviceConnectOperate.setVisibility(View.VISIBLE);
        mTvDeviceConnectOperate.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        if (deviceInfo == null) {
            mTvDeviceName.setText("M20");
            mTvDeviceSn.setText(String.format("设备SN号：%s", sn));
            mTvProductName.setText(String.format("所属产品：%s", "--"));
            mTvFirmwareVersion.setText(String.format("固件版本：%s", "--"));
            mTvPlatformCommunicationState.setText("米度平台连接状态：--");
            return;
        }
        mTvDeviceName.setText(TextUtils.isEmpty(deviceInfo.getDeviceName()) ? "M20" : deviceInfo.getDeviceName());
        mTvDeviceSn.setText(String.format("设备SN号：%s", TextUtils.isEmpty(deviceInfo.getDeviceToken()) ? sn : deviceInfo.getDeviceToken()));
        mTvProductName.setText(String.format("所属产品：%s", TextUtils.isEmpty(deviceInfo.getProductName()) ? "--" : deviceInfo.getProductName()));
        mTvFirmwareVersion.setText(String.format("固件版本：%s", TextUtils.isEmpty(deviceInfo.getFirmwareVersion()) ? "--" : deviceInfo.getFirmwareVersion()));
        if (deviceInfo.isOnlineStatus()) {
            mTvPlatformCommunicationState.setText(getPlatformStateMessage("在线"));
        } else {
            mTvPlatformCommunicationState.setText(getPlatformStateMessage("离线"));
        }
    }

    private CharSequence getPlatformStateMessage(String state) {
        SpannableStringBuilder builder = new SpannableStringBuilder(state);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(state.contains("在线") ? com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_3AD094) : com.blankj.utilcode.util.ColorUtils.getColor(R.color.red));
        builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.insert(0, "米度平台连接状态：");

        return builder;
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
    public void onPause() {
        super.onPause();
        stopDefaultProgress(AppContants.MsgWhat.CONNECT_DEVICE);
    }


    @Override
    public boolean onBackPressed() {
        if (isConnected()) {
            isExitMode = true;
            showDisconnectDialog(StringUtils.getString(R.string.finish_activity_disconnect_bluetooth_device));
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
