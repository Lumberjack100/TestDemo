package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shmedo.configlibrary.iot.model.SensorErrnoBean;
import com.shmedo.configlibrary.iot.model.TerminalBean;
import com.shmedo.configlibrary.iot.utils.IOTSensorUtil;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.CommonViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20 <br/>
 * 描述：     Vms 网关终端运行状态详情页面
 */
public class TcpVmsTerminalCurrentStateFragment extends BaseFragment {
    private static final String DEVICE_INFO = "device_info";
    /**
     * 基本信息
     */
    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;

    @BindView(R.id.tv_net_id)
    TextView mTvNetId;

    @BindView(R.id.tv_address)
    TextView mTvAddress;

    @BindView(R.id.tv_channel)
    TextView mTvChannel;

    @BindView(R.id.tv_register_time)
    TextView mTvRegisterTime;

    @BindView(R.id.tv_update_time)
    TextView mTvUpdateTime;

    /**
     * 通讯状态
     */
    @BindView(R.id.tv_uplink_signal_strength)
    TextView mTvUplinkSignalStrength;

    @BindView(R.id.tv_downlink_signal_strength)
    TextView mTvDownlinkSignalStrength;

    @BindView(R.id.tv_send_data)
    TextView mTvSendData;

    @BindView(R.id.tv_receive_data)
    TextView mTvReceiveData;

    @BindView(R.id.tv_power_volt)
    TextView mTvPowerVolt;

    @BindView(R.id.sensor_recyclerView)
    RecyclerView sensorRecyclerView;

    private CommonAdapter sensorAdapter;
    private List<SensorErrnoBean> sensorList = new ArrayList<>();

    private TerminalBean terminalBean;

    public static TcpVmsTerminalCurrentStateFragment newInstance(TerminalBean terminalBean) {
        TcpVmsTerminalCurrentStateFragment fragment = new TcpVmsTerminalCurrentStateFragment();
        Bundle args = new Bundle();
        args.putParcelable(DEVICE_INFO, terminalBean);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            terminalBean = getArguments().getParcelable(DEVICE_INFO);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.tcp_vms_terminal_current_state_fragment;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initAdapter();
        initData();
    }

    private void initAdapter() {
        int spanCount = 1;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(mActivity, 16);//每一个矩形的间距
        sensorRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, spanCount));
        //设置每个item间距
        sensorRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        sensorAdapter = new CommonAdapter<SensorErrnoBean>(getActivity(), R.layout.item_vms_terminal_sensor_state, sensorList) {
            @Override
            protected void convert(CommonViewHolder holder, SensorErrnoBean errnoBean, int position) {
                holder.setText(R.id.tv_number, "地址 " + errnoBean.getId());
                holder.setText(R.id.tv_sensor_name, IOTSensorUtil.getInstance().getSensorNameByTypeNo(errnoBean.getName()));
                holder.setText(R.id.tv_sensor_value, String.valueOf(errnoBean.getVal()));
                holder.setText(R.id.tv_sensor_state, IOTSensorUtil.getInstance().getErrorMessageByNo(String.valueOf(errnoBean.getErrno())));

                if (errnoBean.getErrno() == 0) {
                    holder.setTextColorRes(R.id.tv_sensor_state, R.color.text_color_3AD094);
                } else {
                    holder.setTextColorRes(R.id.tv_sensor_state, R.color.red);
                }
            }
        };
        sensorRecyclerView.setAdapter(sensorAdapter);
    }

    private void initData() {
        if (terminalBean == null)
            return;

        mTvDeviceSn.setText(terminalBean.getSn());
        mTvNetId.setText(String.valueOf(terminalBean.getNetid()));
        mTvAddress.setText(String.valueOf(terminalBean.getAddr()));
        mTvChannel.setText(String.valueOf(terminalBean.getChl()));
        mTvRegisterTime.setText(String.valueOf(terminalBean.getLogintime()));
        mTvUpdateTime.setText(String.valueOf(terminalBean.getLastpackagetime()));

        mTvUplinkSignalStrength.setText(String.valueOf(terminalBean.getUprssi()));
        mTvDownlinkSignalStrength.setText(String.valueOf(terminalBean.getDownrssi()));
        mTvSendData.setText(String.valueOf(terminalBean.getTx()));
        mTvReceiveData.setText(String.valueOf(terminalBean.getRx()));
        mTvPowerVolt.setText(terminalBean.getVolt() + "V");

        if (terminalBean.getSensor() != null) {
            sensorList.clear();
            sensorList.addAll(terminalBean.getSensor());
            sensorAdapter.notifyDataSetChanged();
        }
    }

}