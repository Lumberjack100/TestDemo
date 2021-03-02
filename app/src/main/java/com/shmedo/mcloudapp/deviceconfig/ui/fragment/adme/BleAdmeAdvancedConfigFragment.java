package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.AdmeAdvancedConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeExecutiveAgencyActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeInclinometerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeMeterWheelActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeStepperMotorActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.BleAdmeLockedRotorDetectionActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/28<br/>
 * 描述：     ADME 高级配置页面
 */
public class BleAdmeAdvancedConfigFragment extends BaseBleIotCommunicateFragment {
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private AdmeAdvancedConfigModuleAdapter moduleAdapter;
    private List<ConfigModule> configModuleList = new ArrayList<>();
    private ConfigModule selectedConfigModule;

    public static BleAdmeAdvancedConfigFragment newInstance() {
        return new BleAdmeAdvancedConfigFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_advanced_config_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initAdapter();
        initConfigModuleData();
    }

    private void initAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(mActivity, 15);//每一个矩形的间距
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

                if (!isConnected()) {
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
            case "计米轮":
                AdmeMeterWheelActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);
                break;

            case "测斜仪":
                AdmeInclinometerActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);
                break;

            case "执行机构":
                AdmeExecutiveAgencyActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);
                break;

            case "步进电机":
                AdmeStepperMotorActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);
                break;

            case "堵转缓停":
                BleAdmeLockedRotorDetectionActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);
                break;
        }
    }


    private void initConfigModuleData() {
        configModuleList.clear();

        ConfigModule   configModule = new ConfigModule(R.drawable.ic_adme_advanced_config, "计米轮", "参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_adme_advanced_config, "测斜仪", "参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_adme_advanced_config, "执行机构", "参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_adme_advanced_config, "步进电机", "参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_adme_advanced_config, "堵转缓停", "参数配置");
        configModuleList.add(configModule);
    }

}