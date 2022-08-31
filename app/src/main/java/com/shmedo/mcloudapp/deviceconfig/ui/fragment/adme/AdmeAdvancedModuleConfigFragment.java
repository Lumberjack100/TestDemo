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
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.AdmeAdvancedConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeExecutiveAgencyActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeGuideGrooveCalibrationActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeInclinometerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeMeterWheelActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeStepperMotorActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeVoltageConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeLockedRotorDetectionActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.IntelligentControlActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/19 <br/>
 * 描述：     ADME 系列高级模块功能配置
 */
public class AdmeAdvancedModuleConfigFragment extends BaseFragment {
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private AdmeAdvancedConfigModuleAdapter moduleAdapter;
    private List<ConfigModule> configModuleList = new ArrayList<>();
    private ConfigModule selectedConfigModule;

    private int connectWay = AppContants.CommunicationWay.NET_PLATFORM_CONNECT;
    private ProductType productType = ProductType.ADME;
    public DeviceInfo deviceInfo;


    public static AdmeAdvancedModuleConfigFragment newInstance(int connectWay, ProductType productType, DeviceInfo deviceInfo) {
        AdmeAdvancedModuleConfigFragment fragment = new AdmeAdvancedModuleConfigFragment();
        Bundle args = new Bundle();
        args.putInt(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        args.putSerializable(AppContants.Extras.PRODUCT_TYPE, productType);
        args.putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null)
            return;
        if (getArguments().containsKey(AppContants.Extras.COMMUNICATION_WAY)) {
            connectWay = getArguments().getInt(AppContants.Extras.COMMUNICATION_WAY);
        }
        if (getArguments().containsKey(AppContants.Extras.PRODUCT_TYPE)) {
            productType = (ProductType) getArguments().getSerializable(AppContants.Extras.PRODUCT_TYPE);
        }
        if (getArguments().containsKey(AppContants.Extras.DEVICE_INFO)) {
            deviceInfo = getArguments().getParcelable(AppContants.Extras.DEVICE_INFO);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adme_advanced_config_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
                selectedConfigModule = configModuleList.get(position);
                processItemClick();
            }
        });
        mRecyclerView.setAdapter(moduleAdapter);
        mRecyclerView.setHasFixedSize(true);
    }

    private void processItemClick() {
        switch (selectedConfigModule.getName()) {
            case "计米轮":
                AdmeMeterWheelActivity.startActivity(mActivity, connectWay, deviceInfo);
                break;

            case "测斜仪":
                AdmeInclinometerActivity.startActivity(mActivity, connectWay, productType, deviceInfo);
                break;

            case "执行机构":
                AdmeExecutiveAgencyActivity.startActivity(mActivity, connectWay, productType, deviceInfo);
                break;

            case "步进电机":
                AdmeStepperMotorActivity.startActivity(mActivity, connectWay, deviceInfo);
                break;

            case "堵转缓停":
                AdmeLockedRotorDetectionActivity.startActivity(mActivity, connectWay, deviceInfo);
                break;

            case "导槽校准":
                AdmeGuideGrooveCalibrationActivity.startActivity(mActivity, connectWay);
                break;

            case "智能控制":
                IntelligentControlActivity.startActivity(mActivity, connectWay, deviceInfo);
                break;

            case "阈值设置":
                AdmeVoltageConfigActivity.startActivity(mActivity, connectWay, productType, deviceInfo);
                break;
        }
    }

    private void initConfigModuleData() {
        configModuleList.clear();
        if (productType == ProductType.ADME) {
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

            if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
                configModule = new ConfigModule(R.drawable.ic_positive_and_negative_test, "导槽校准", "正反测起点校准");
                configModuleList.add(configModule);
            }

            configModule = new ConfigModule(R.drawable.ic_adme_advanced_config, "智能控制", "参数配置");
            configModuleList.add(configModule);

            configModule = new ConfigModule(R.drawable.ic_adme_advanced_config, "阈值设置", "参数配置");
            configModuleList.add(configModule);

        } else if (productType == ProductType.HAC) {
            ConfigModule configModule = new ConfigModule(R.drawable.ic_adme_advanced_config, "计米轮", "参数配置");
            configModuleList.add(configModule);

            configModule = new ConfigModule(R.drawable.ic_adme_advanced_config, "测斜仪", "参数配置");
            configModuleList.add(configModule);

            configModule = new ConfigModule(R.drawable.ic_adme_advanced_config, "执行机构", "参数配置");
            configModuleList.add(configModule);

            configModule = new ConfigModule(R.drawable.ic_adme_advanced_config, "堵转缓停", "参数配置");
            configModuleList.add(configModule);

            configModule = new ConfigModule(R.drawable.ic_adme_advanced_config, "智能控制", "参数配置");
            configModuleList.add(configModule);

            configModule = new ConfigModule(R.drawable.ic_adme_advanced_config, "阈值设置", "参数配置");
            configModuleList.add(configModule);
        }
    }
}
