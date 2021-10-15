package com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.ConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2/25/21 <br/>
 * 描述：    通用网络模式设备配置主页面
 */
public abstract class UniversalNetConfigHomeFragment extends BaseNetIotCommunicateFragment {
    public static final String EXTRA_DEVICE = "com.shmedo.mcloudapp.EXTRA_DEVICE";

    @BindView(R.id.tv_device_name)
    protected TextView mTvDeviceName;//设备名称

    @BindView(R.id.tv_device_sn)
    protected TextView mTvDeviceSn;//设备SN号

    @BindView(R.id.tv_product_model)
    protected TextView mTvProductModel;//产品型号

    @BindView(R.id.tv_time_or_sub_model)
    protected TextView mTvFirmwareVersion;//固件版本

    @BindView(R.id.tv_platform_communication_state)
    protected TextView mTvPlatformCommunicationState;//与米度平台连接状态

    @BindView(R.id.tv_device_state_flag)
    protected TextView mTvDeviceState;//(在线、离线)

    @BindView(R.id.tv_device_connect_operate)
    protected TextView mTvDeviceConnectOperate;//蓝牙连接操作(断开连接、重新连接)

    @BindView(R.id.recyclerview)
    protected RecyclerView mRecyclerView;

    private ConfigModuleAdapter moduleAdapter;
    protected List<ConfigModule> configModuleList = new ArrayList<>();
    protected ConfigModule selectedConfigModule;

    private int deviceType = AppContants.DeviceType.DAS;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey(EXTRA_DEVICE)) {
                projectDeviceInfo = getArguments().getParcelable(EXTRA_DEVICE);
            }
            if (getArguments().containsKey(AppContants.Extras.DEVICE_TYPE)) {
                deviceType = getArguments().getInt(AppContants.Extras.DEVICE_TYPE);
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.universal_config_home_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHeadInfo();
        initAdapter();
        initConfigModuleData();
    }

    protected void setHeadInfo() {
        switch (deviceType) {
            case AppContants.DeviceType.DAS:
                mTvDeviceName.setText("物联网数据采集器");
                break;

            case AppContants.DeviceType.ADME:
                break;

            case AppContants.DeviceType.M20:
                mTvDeviceName.setText("普适型GNSS一体机");
                break;

            case AppContants.DeviceType.E40:
                mTvDeviceName.setText("测地形GNSS一体机");
                break;
        }
        if (projectDeviceInfo != null) {
            mTvDeviceSn.setText(String.format("设备编号：%s", TextUtils.isEmpty(projectDeviceInfo.getToken()) ? "" : projectDeviceInfo.getToken()));
            mTvProductModel.setText(String.format("产品型号：%s", TextUtils.isEmpty(projectDeviceInfo.getDeviceTypeName()) ? "M20" : projectDeviceInfo.getDeviceTypeName()));
            mTvFirmwareVersion.setText(String.format("固件版本：%s", TextUtils.isEmpty(projectDeviceInfo.getFirmwareVersion()) ? "" : projectDeviceInfo.getFirmwareVersion()));
            if (projectDeviceInfo.isOnline()) {
                mTvDeviceState.setText("在线");
                mTvDeviceState.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_50E9B9));
                mTvDeviceState.setBackgroundResource(R.drawable.bg_device_online_state_flag);
            } else {
                mTvDeviceState.setText("离线");
                mTvDeviceState.setTextColor(ContextCompat.getColor(mActivity, R.color.sub_title_text_color));
                mTvDeviceState.setBackgroundResource(R.drawable.bg_device_offline_state_flag);
            }
        }
        mTvPlatformCommunicationState.setVisibility(View.GONE);
        mTvDeviceConnectOperate.setVisibility(View.GONE);
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
                selectedConfigModule = configModuleList.get(position);
                processItemClick();
            }
        });
        mRecyclerView.setAdapter(moduleAdapter);
    }

    protected abstract void processItemClick();

    protected abstract void initConfigModuleData();

    /**
     * 指令下发失败处理
     */
    protected abstract void doDispatchFailed(String cmdStr);

    /**
     * 指令下发成功处理
     */
    protected abstract void doDispatchSuccess(String cmdStr);

    /**
     * 查询指令响应结果出错
     *
     * @param errMsg
     */
    @Override
    protected void onQueryCmdResponseResultError(String errMsg) {
        super.onQueryCmdResponseResultError(errMsg);
        ToastUtils.show("指令响应错误");
    }

    /**
     * 查询指令响应结果超时
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultTimeOut(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultTimeOut(queryCmdResult);
        ToastUtils.show("指令响应超时");
    }

    /**
     * 查询指令响应结果成功
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultSuccess(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultSuccess(queryCmdResult);
        setResultData(queryCmdResult);
    }

    protected abstract void setResultData(QueryCmdResult queryCmdResult);
}