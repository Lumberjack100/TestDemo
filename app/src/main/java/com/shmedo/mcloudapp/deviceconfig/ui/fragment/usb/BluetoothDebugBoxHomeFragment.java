package com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.ConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

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
        BluetoothDebugBoxHomeFragment fragment=new BluetoothDebugBoxHomeFragment();
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
        if (getArguments() != null ) {
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
        super.onViewCreated(view,savedInstanceState);
        setHeadInfo();
        initAdapter();
        initConfigModuleData();

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
//                if (!isConnected()) {
//                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
//                    return;
//                }
//                selectedConfigModule = (ConfigModule) configModuleList.get(position);
//                processItemClick();
            }
        });
        mRecyclerView.setAdapter(moduleAdapter);
    }

    private void initConfigModuleData() {
        configModuleList.clear();
        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_state, "状态", "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_current_time, "时间", "获取当前设备时间");
        configModuleList.add(configModule);

    }

}