package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shmedo.configlibrary.iot.model.SensorErrnoInfo;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalInfo;
import com.shmedo.configlibrary.iot.utils.IOTSensorUtil;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.CommonViewHolder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20 <br/>
 * 描述：     Vms 网关终端运行状态详情页面
 */
public class VmsTerminalCurrentStateFragment extends BaseFragment {
    private static final String TERMINAL_INFO = "terminal_info";
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
    private List<SensorErrnoInfo> sensorList = new ArrayList<>();

    private VmsTerminalInfo vmsTerminalInfo;

    public static VmsTerminalCurrentStateFragment newInstance(VmsTerminalInfo vmsTerminalInfo) {
        VmsTerminalCurrentStateFragment fragment = new VmsTerminalCurrentStateFragment();
        Bundle args = new Bundle();
        args.putParcelable(TERMINAL_INFO, vmsTerminalInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vmsTerminalInfo = getArguments().getParcelable(TERMINAL_INFO);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.vms_terminal_current_state_fragment;
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
        sensorAdapter = new CommonAdapter<SensorErrnoInfo>(getActivity(), R.layout.item_vms_terminal_sensor_state, sensorList) {
            @Override
            protected void convert(CommonViewHolder holder, SensorErrnoInfo errnoBean, int position) {
                holder.setText(R.id.tv_number, "地址 " + errnoBean.getId());
                holder.setText(R.id.tv_sensor_name, IOTSensorUtil.getInstance().getSensorNameByTypeCode(errnoBean.getName()));
                DecimalFormat df = new DecimalFormat("#.###");//格式化小数
                String value = df.format(Double.valueOf(errnoBean.getVal()));
                holder.setText(R.id.tv_sensor_value, value);
                holder.setText(R.id.tv_sensor_status, IOTSensorUtil.getInstance().getErrorMessageByNo(String.valueOf(errnoBean.getErrno())));

                if (errnoBean.getErrno() == 0) {
                    holder.setTextColorRes(R.id.tv_sensor_status, R.color.text_color_3AD094);
                } else {
                    holder.setTextColorRes(R.id.tv_sensor_status, R.color.red);
                }
            }
        };
        sensorRecyclerView.setAdapter(sensorAdapter);
    }

    private void initData() {
        if (vmsTerminalInfo == null)
            return;

        mTvDeviceSn.setText(vmsTerminalInfo.getSn());
        mTvNetId.setText(String.valueOf(vmsTerminalInfo.getNetid()));
        mTvAddress.setText(String.valueOf(vmsTerminalInfo.getAddr()));
        mTvChannel.setText(String.valueOf(vmsTerminalInfo.getChl()));
        mTvRegisterTime.setText(String.valueOf(vmsTerminalInfo.getLogintime()));
        mTvUpdateTime.setText(String.valueOf(vmsTerminalInfo.getLastpackagetime()));

        mTvUplinkSignalStrength.setText(String.valueOf(vmsTerminalInfo.getUprssi()));
        mTvDownlinkSignalStrength.setText(String.valueOf(vmsTerminalInfo.getDownrssi()));
        mTvSendData.setText(String.valueOf(vmsTerminalInfo.getTx()));
        mTvReceiveData.setText(String.valueOf(vmsTerminalInfo.getRx()));

        DecimalFormat df = new DecimalFormat("#");//格式化小数
        String rate = df.format(vmsTerminalInfo.getVolt()) + "%";
        mTvPowerVolt.setText(rate);

        if (vmsTerminalInfo.getSensor() != null) {
            sensorList.clear();
            sensorList.addAll(vmsTerminalInfo.getSensor());
            sensorAdapter.notifyDataSetChanged();
        }
    }

}