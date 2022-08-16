package com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
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

    @BindView(R.id.tv_product_name)
    protected TextView mTvProductName;//所属产品

    @BindView(R.id.tv_firmware_version)
    protected TextView mTvFirmwareVersion;//固件版本

    @BindView(R.id.tv_extended_field3)
    protected TextView mTvExtendedField3;//

    @BindView(R.id.tv_platform_communication_state)
    protected TextView mTvPlatformCommunicationState;//与平台通信状态(文字标识),隐藏

    @BindView(R.id.tv_device_state_flag)
    protected TextView mTvDeviceState;//与平台通信状态(在线、离线图标)

    @BindView(R.id.tv_device_connect_operate)
    protected TextView mTvDeviceConnectOperate;//蓝牙连接操作(断开连接、重新连接)

    @BindView(R.id.recyclerview)
    protected RecyclerView mRecyclerView;

    private ConfigModuleAdapter moduleAdapter;
    protected List<ConfigModule> configModuleList = new ArrayList<>();
    protected ConfigModule selectedConfigModule;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey(EXTRA_DEVICE)) {
                deviceInfo = getArguments().getParcelable(EXTRA_DEVICE);
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.universal_config_home_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initHeadInfo();
        initAdapter();
        initConfigModuleData();
    }

    protected void initHeadInfo() {
        try {
            mTvDeviceName.setText(TextUtils.isEmpty(deviceInfo.getDeviceName()) ? deviceInfo.getDeviceToken() : deviceInfo.getDeviceName());
            mTvDeviceSn.setText(String.format("设备SN号：%s", TextUtils.isEmpty(deviceInfo.getDeviceToken()) ? "" : deviceInfo.getDeviceToken()));
            mTvProductName.setText(String.format("所属产品：%s", TextUtils.isEmpty(deviceInfo.getProductName()) ? "--" : deviceInfo.getProductName()));
            mTvFirmwareVersion.setText(String.format("固件版本：%s", TextUtils.isEmpty(deviceInfo.getFirmwareVersion()) ? "--" : deviceInfo.getFirmwareVersion()));
            setDeviceState(mTvDeviceState, deviceInfo.isOnlineStatus());
            //TODO #gh# 暂时不展示运行状态处理，后期考虑优化
            mTvExtendedField3.setVisibility(View.GONE);
            mTvPlatformCommunicationState.setVisibility(View.GONE);
            mTvDeviceConnectOperate.setVisibility(View.GONE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    protected void setResultData(QueryCmdResult queryCmdResult) {
    }

}