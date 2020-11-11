package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.VmsAisleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceTypeInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.ui.VmsViewModel;
import com.shmedo.mcloudapp.entity.DeviceTypeInfoDao;
import com.shmedo.mcloudapp.util.DaoManager;

import butterknife.BindView;


public class TcpVmsHomeFragment extends BaseFragment {
    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;

    @BindView(R.id.tv_product_model)
    TextView mTvProductModel;

    @BindView(R.id.tv_time_or_sub_model)
    TextView mTvSubModel;

    @BindView(R.id.tv_device_communication_state_flag)
    TextView mTvDeviceCommunicationState;//通信状态(在线、离线、已连接、已断开)

    @BindView(R.id.tv_device_connect_state)
    TextView mTvDeviceConnectState;//Tcp连接状态(断开连接、重新连接)

    @BindView(R.id.tv_device_communication_way)
    TextView mTvDeviceCommunicationWay;//通信方式(网络、蓝牙)

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private VmsAisleAdapter vmsAisleAdapter;

    private VmsViewModel mViewModel;

    private int deviceTypeID;
    private String deviceTypeName;

    public static TcpVmsHomeFragment newInstance() {
        return new TcpVmsHomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.tcp_vms_home_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(VmsViewModel.class);

        setHeadInfo();
//        initAdapter();
//        initConfigModuleData();
//        //连接设备
//        findAndConnectSpecificDevice();
    }

    private void setHeadInfo() {
//        String[] infos = bleNameInfo.split(",");
//        if (infos.length >= 3) {
//            mTvDeviceSn.setText(String.format("设备编号：%s", TextUtils.isEmpty(infos[1]) ? "" : infos[1]));
//            mTvProductModel.setText(String.format("产品型号：%s", TextUtils.isEmpty(infos[2]) ? "" : infos[2]));
//            searchDeviceTypeInfo(TextUtils.isEmpty(infos[2]) ? "" : infos[2]);
//        }
//        mTvDeviceConnectState.setVisibility(View.VISIBLE);
//        mTvDeviceConnectState.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
//        mTvDeviceCommunicationWay.setText("网络");
    }

    private void searchDeviceTypeInfo(String typeName) {
        DeviceTypeInfo deviceTypeInfo = DaoManager.getInstance().getDaoSession().getDeviceTypeInfoDao().queryBuilder()
                .where(DeviceTypeInfoDao.Properties.DeviceTypeName.like("%" + typeName + "%"))
                .unique();

        mTvDeviceName.setText("VMS网关");
        if (deviceTypeInfo != null) {
            deviceTypeID = deviceTypeInfo.getId();
            deviceTypeName = deviceTypeInfo.getDeviceTypeName();
        } else {
            deviceTypeID = -1;
            deviceTypeName = typeName;
        }
    }

    private void initAdapter() {
        int spanCount = 1;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(mActivity, 15);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
//        vmsAisleAdapter = new VmsAisleAdapter(configModuleList);
        vmsAisleAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if (isDoubleClick(view)) {
                    return;
                }

                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }

            }
        });
        mRecyclerView.setAdapter(vmsAisleAdapter);
    }

}