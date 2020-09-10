package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.ConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceHistoryDataAnalysisActivity;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class BleConfigDeviceFragment extends BaseBleConnectFragment {
    private static final String DEVICE_INFO = "device_info";

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;

    @BindView(R.id.tv_device_model)
    TextView mTvDeviceModel;

    @BindView(R.id.tv_time)
    TextView mTvTime;

    @BindView(R.id.tv_device_communication_state_flag)
    TextView mTvDeviceCommunicationState;//通信状态(在线、离线、已连接、已断开)

    @BindView(R.id.tv_device_connect_state)
    TextView mTvDeviceConnectState;//蓝牙连接状态(断开连接、重新连接)

    @BindView(R.id.tv_device_communication_way)
    TextView mTvDeviceCommunicationWay;//通信方式(网络、蓝牙)

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private ConfigModuleAdapter moduleAdapter;

    private List<ConfigModule> configModuleList = new ArrayList<>();

    private String bleInfo;
    private ProjectDeviceInfo projectDeviceInfo;
    private int companyID;

    private ConfigModule selectedConfigModule;
    private List<String> msgIDList = new ArrayList<>();


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
            bleInfo = getArguments().getString(DEVICE_INFO);
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
        initAdapter();
        initConfigModuleData();
//        queryCmdState();
        findAndConnectSpecificDevice();
    }

    private void setHeadInfo() {
        if (projectDeviceInfo != null) {
            mTvDeviceName.setText("物联网数据采集器");
            mTvDeviceSn.setText(String.format("设备编号：%s", TextUtils.isEmpty(projectDeviceInfo.getToken()) ? "" : projectDeviceInfo.getToken()));
            mTvDeviceModel.setText(String.format("产品型号：%s", TextUtils.isEmpty(projectDeviceInfo.getDeviceTypeName()) ? "" : projectDeviceInfo.getDeviceTypeName()));
            mTvTime.setText(String.format("更新时间：%s", TextUtils.isEmpty(projectDeviceInfo.getLastActiveTime()) ? "" : projectDeviceInfo.getLastActiveTime()));
            if (projectDeviceInfo.isOnline()) {
                mTvDeviceCommunicationState.setText("在线");
                mTvDeviceCommunicationState.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_50E9B9));
                mTvDeviceCommunicationState.setBackgroundResource(R.drawable.bg_device_online_state_flag);
            } else {
                mTvDeviceCommunicationState.setText("离线");
                mTvDeviceCommunicationState.setTextColor(ContextCompat.getColor(mActivity, R.color.sub_title_text_color));
                mTvDeviceCommunicationState.setBackgroundResource(R.drawable.bg_device_offline_state_flag);
            }
        }
        mTvDeviceConnectState.setVisibility(View.INVISIBLE);
        mTvDeviceCommunicationWay.setText("网络");
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
                selectedConfigModule = (ConfigModule) configModuleList.get(position);
                processItemClick();
            }
        });
        mRecyclerView.setAdapter(moduleAdapter);
    }

    private void processItemClick() {
        switch (selectedConfigModule.getName()) {
            case "状态":
            case "时间":
            case "遥测":
            case "重启":

                break;
        }
    }

    @OnClick({R.id.tv_device_communication_way, R.id.rl_run_state_analysis})
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.tv_device_communication_way://切换连接方式
                break;

            case R.id.rl_run_state_analysis:
                DeviceHistoryDataAnalysisActivity.startActivity(mActivity, projectDeviceInfo);
                break;
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

        // TODO(设备暂不支持网络配置)
        //DAS具有采集器配置项
//        if (projectDeviceInfo.getDeviceTypeID() == 4) {
//            configModule = new ConfigModule(R.drawable.ic_device_collector_config, "采集器配置", "采集器参数配置");
//            configModuleList.add(configModule);
//        }

        // TODO(下次版本迭代再开发)
//        configModule = new ConfigModule(R.drawable.ic_device_instruction_send, "指令下发", "服务端代码指令下发");
//        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_advanced_setting, "设置", "高级设置");
        configModuleList.add(configModule);
    }
}
