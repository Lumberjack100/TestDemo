package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.AdmeAdvancedConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeExecutiveAgencyActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeInclinometerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeLowEnergyModeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeMeterWheelActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeStepperMotorActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.BleAdmeLockedRotorDetectionActivity;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/23 <br/>
 * 描述：     TODO
 */
public class NetAdmeAdvancedConfigFragment extends BaseFragment {
    protected static final String PRO_DEVICE_INFO = "com.shmedo.mcloudapp.PRO_DEVICE_INFO";

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private AdmeAdvancedConfigModuleAdapter moduleAdapter;
    private List<ConfigModule> configModuleList = new ArrayList<>();
    private ConfigModule selectedConfigModule;

    public ProjectDeviceInfo projectDeviceInfo;


    public static NetAdmeAdvancedConfigFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetAdmeAdvancedConfigFragment fragment = new NetAdmeAdvancedConfigFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(PRO_DEVICE_INFO)) {
            projectDeviceInfo = getArguments().getParcelable(PRO_DEVICE_INFO);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adme_advanced_config_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initAdapter();
        initConfigModuleData();
    }

    private void initAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = ConvertUtils.dp2px(15);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        moduleAdapter = new AdmeAdvancedConfigModuleAdapter(configModuleList);
        moduleAdapter.setAnimationEnable(true);
        moduleAdapter.setAnimationFirstOnly(false);
        moduleAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if (isDoubleClick(view)) {
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
            case "计米轮":
                AdmeMeterWheelActivity.startActivity(mActivity, projectDeviceInfo);
                break;

            case "测斜仪":
                AdmeInclinometerActivity.startActivity(mActivity, projectDeviceInfo);
                break;

            case "执行机构":
                AdmeExecutiveAgencyActivity.startActivity(mActivity, projectDeviceInfo);
                break;

            case "步进电机":
                AdmeStepperMotorActivity.startActivity(mActivity, projectDeviceInfo);
                break;

            case "堵转缓停":
                BleAdmeLockedRotorDetectionActivity.startActivity(mActivity, projectDeviceInfo);
                break;

            case "继电器使能":
                AdmeLowEnergyModeActivity.startActivity(mActivity, projectDeviceInfo);
                break;
        }
    }

    private void initConfigModuleData() {
        configModuleList.clear();

        ConfigModule configModule = new ConfigModule(R.drawable.ic_adme_advanced_config, "计米轮", "参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_adme_advanced_config, "测斜仪", "参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_adme_advanced_config, "执行机构", "参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_adme_advanced_config, "步进电机", "参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_adme_advanced_config, "堵转缓停", "参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_adme_advanced_config, "继电器使能", "参数配置");
        configModuleList.add(configModule);
    }
}
