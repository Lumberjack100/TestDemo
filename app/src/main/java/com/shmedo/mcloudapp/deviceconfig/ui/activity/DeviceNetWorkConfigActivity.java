package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.ConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class DeviceNetWorkConfigActivity extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;

    @BindView(R.id.tv_device_model)
    TextView mTvDeviceModel;

    @BindView(R.id.tv_collector_mode)
    TextView mTvCollectorType;

    @BindView(R.id.tv_device_state_flag)
    TextView mTvDeviceStateFlag;

    @BindView(R.id.tv_device_connect_mode)
    TextView mTvDeviceConnectMode;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;


    private ConfigModuleAdapter moduleAdapter;

    private List<ConfigModule> configModuleList = new ArrayList<>();

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, DeviceNetWorkConfigActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_device_net_work_config;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent();
        initAdapter();
        initConfigModuleData();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() == null)
            return;

//        if (intent.getExtras().containsKey(PROJECT_ID)) {
//            projectID = intent.getIntExtra(PROJECT_ID, 0);
//        }
//        if (intent.getExtras().containsKey(PROJECT_NAME)) {
//            projectName = intent.getStringExtra(PROJECT_NAME);
//        }
    }

    private void initAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(this, 15);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        moduleAdapter = new ConfigModuleAdapter(configModuleList);
        moduleAdapter.setAnimationEnable(true);
        moduleAdapter.setAnimationFirstOnly(false);
        moduleAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                ConfigModule configModule = (ConfigModule) configModuleList.get(position);
            }
        });
        mRecyclerView.setAdapter(moduleAdapter);
    }


    @OnClick({R.id.right_icon, R.id.rl_run_state_analysis})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_icon:
                break;

            case R.id.rl_run_state_analysis:
                break;
        }

    }

    private void initConfigModuleData() {
        configModuleList.clear();
        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_state, "状态", "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_current_time, "时间", "获取当前设备时间");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_telemetry, "遥测", "远距离测量");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_reboot, "重启", "重新启动当前设备");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_firmware_upgrade, "固件升级", "版本:01.0012");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_collector_config, "采集器配置", "采集器参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_instruction_send, "指令下发", "服务端代码指令下发");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_advanced_setting, "设置", "高级设置");
        configModuleList.add(configModule);
    }

}
