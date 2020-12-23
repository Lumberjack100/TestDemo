package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.vms.GetVmsTerminalSensorParamsEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalSensorInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsTerminalExternalSensorConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.BaseTcpConnectFragment;
import com.shmedo.mcloudapp.projects.model.VmsTerminalSensorItem;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.CommonViewHolder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/23 <br/>
 * 描述：      Vms终端扩展传感器主页面
 */
public class TcpVmsTerminalExternalSensorHomeFragment extends BaseTcpConnectFragment {

    @BindView(R.id.recyclerview_sensor)
    RecyclerView mRecyclerViewSensor;

    private CommonAdapter sensorAdapter;

    private List<VmsTerminalSensorItem> sensorItemList = new ArrayList<>();
    //以传感器的通道号为 Key,TerminalSensorInfo 对象为 Value
    protected HashMap<String, VmsTerminalSensorInfo> sensorHashMap = new HashMap<>();

    private String sn;
    private int accessSum = 4;              //接入扩展传感器总数
    private int sensorIndex = 0;//接入的传感器索引号


    public static TcpVmsTerminalExternalSensorHomeFragment newInstance(String sn) {
        TcpVmsTerminalExternalSensorHomeFragment fragment = new TcpVmsTerminalExternalSensorHomeFragment();
        Bundle args = new Bundle();
        args.putString(AppContants.Extras.CUR_DEVICE_SN, sn);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sn = getArguments().getString(AppContants.Extras.CUR_DEVICE_SN);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.tcp_vms_terminal_sensor_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initSensorAdapter();
    }

    @Override
    public void onStart() {
        super.onStart();
        sensorIndex = 0;
        sensorHashMap.clear();
        sensorItemList.clear();
//        startProgressRunnable("刷新数据...", QUERY_CMD_DELAY_MILLIS);
        queryTerminalAisleParamInfo();
    }

    private void initSensorAdapter() {
        int spanCount = 4;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(getActivity(), 15);//每一个矩形的间距
        mRecyclerViewSensor.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
        //设置每个item间距
        mRecyclerViewSensor.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));
        sensorAdapter = new CommonAdapter<VmsTerminalSensorItem>(mActivity, R.layout.item_vms_terminal_sensor, sensorItemList) {
            @Override
            protected void convert(CommonViewHolder holder, VmsTerminalSensorItem sensorItem, int position) {
                if (sensorItem.isInsert()) {
                    holder.setImageResource(R.id.iv_vms_terminal_sensor, R.drawable.ic_sensor_holder_bright);

                } else {
                    holder.setImageResource(R.id.iv_vms_terminal_sensor, R.drawable.ic_sensor_holder_gray);
                }
            }
        };
        sensorAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                if (isDoubleClick(view)) {
                    return;
                }
                if (!tcpShareViewModel.getConnectStatus()) {
                    ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
                    return;
                }
                VmsTerminalSensorItem sensorItem = sensorItemList.get(position);
                VmsTerminalSensorInfo sensorInfo = sensorHashMap.get(sensorItem.getChannel());
                VmsTerminalExternalSensorConfigActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, sensorInfo);
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
        mRecyclerViewSensor.setAdapter(sensorAdapter);
    }

    /**
     * 获取Vms终端某个通道下传感器参数
     */
    private void queryTerminalAisleParamInfo() {
        GetVmsTerminalSensorParamsEntity entity = new GetVmsTerminalSensorParamsEntity(sn, sensorIndex);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_GET_TERMINAL_CHL, entity);
        sendCommand(command);
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case VMS_MD_GET_TERMINAL_CHL: {//获取终端传感器的参数
                IOTCommandResult<VmsTerminalSensorInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询获取终端传感器参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                VmsTerminalSensorInfo sensorInfo = commandResult.getResult();
                //处理此通道的传感器配置参数
                processSensorParamsInfo(sensorInfo);
                sensorIndex++;
                //还有待查询通道的传感器
                if (sensorIndex < accessSum) {
                    queryTerminalAisleParamInfo();
                } else {//所有通道的传感器参数都查询了
                    stopProgressRunnable();
                    sensorAdapter.notifyDataSetChanged();
                }
            }

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    private void processSensorParamsInfo(VmsTerminalSensorInfo sensorInfo) {
        if (sensorInfo == null)
            return;

        sensorHashMap.put(sensorInfo.getChannel(), sensorInfo);
        addSensorItem(sensorInfo);
    }

    private void addSensorItem(VmsTerminalSensorInfo sensorInfo) {
        VmsTerminalSensorItem sensorItem = new VmsTerminalSensorItem();
        sensorItem.setChannel(sensorInfo.getChannel());
        sensorItem.setInsert(sensorInfo.getInsert().trim().equals("1"));
        sensorItem.setResId(R.drawable.ic_sensor_holder_bright);
        sensorItemList.add(sensorItem);
    }
}