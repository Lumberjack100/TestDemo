package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsTerminalExternalSensorConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.projects.model.VmsTerminalSensorItem;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.CommonViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/11/21 <br/>
 * 描述：      Vms终端4g 模式扩展传感器主页面
 */
public class NetVmsTerminalExternalSensorHomeFragment extends BaseNetIotCommunicateFragment {

    @BindView(R.id.recyclerview_sensor)
    RecyclerView mRecyclerViewSensor;

    private CommonAdapter sensorAdapter;

    private List<VmsTerminalSensorItem> sensorItemList = new ArrayList<>();
    //以传感器的通道号为 Key,TerminalSensorInfo 对象为 Value
    protected HashMap<String, VmsTerminalSensorInfo> sensorHashMap = new HashMap<>();

    private String sn;
    private final int accessSum = 4;  //接入扩展传感器总数
    private int sensorIndex = 0;//接入的传感器索引号

    private int curSensorIndex = -1;
    private ActivityResultLauncher<Intent> resultLauncher;

    public static NetVmsTerminalExternalSensorHomeFragment newInstance(ProjectDeviceInfo projectDeviceInfo, String sn) {
        NetVmsTerminalExternalSensorHomeFragment fragment = new NetVmsTerminalExternalSensorHomeFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
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
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            if (curSensorIndex != -1) {
                                sensorIndex = curSensorIndex;
                                showProgressDialog("处理中...");
                                queryTerminalAisleParamInfo();
                            }
                        }
                    }
                });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.vms_terminal_sensor_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initSensorAdapter();
        sensorIndex = 0;
        sensorHashMap.clear();
        sensorItemList.clear();
        showProgressDialog("处理中...");
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
                holder.setImageResource(R.id.iv_vms_terminal_sensor, sensorItem.isInsert() ? R.drawable.ic_sensor_holder_bright : R.drawable.ic_sensor_holder_gray);
            }
        };
        sensorAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                if (isDoubleClick(view)) {
                    return;
                }
                curSensorIndex = position;
                VmsTerminalSensorItem sensorItem = sensorItemList.get(position);
                VmsTerminalSensorInfo sensorInfo = sensorHashMap.get(sensorItem.getChannel());
                VmsTerminalExternalSensorConfigActivity.startActivity(mActivity, resultLauncher, projectDeviceInfo, sensorInfo);
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
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            dismissProgressDialog();
            showDispatchFailedDialog(cmdStr);
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponseRunnable(2000);
        }
    }

    /**
     * 指令下发失败弹框
     */
    private void showDispatchFailedDialog(String cmdStr) {
        ToastUtils.show("下发指令失败");
    }

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
//        super.onQueryCmdResponseResultSuccess(queryCmdResult);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(queryCmdResult.getCmdEngName());
        switch (type) {
            case VMS_MD_GET_TERMINAL_CHL: {//获取终端传感器的参数
                IOTCommandResult<VmsTerminalSensorInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    dismissProgressDialog();
                    String errMsg = String.format("%s %s", "查询获取终端传感器参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                VmsTerminalSensorInfo sensorInfo = commandResult.getResult();
                //处理此通道的传感器配置参数
                processSensorParamsInfo(sensorInfo);

                if (curSensorIndex == -1) {
                    sensorIndex++;
                    //还有待查询通道的传感器
                    if (sensorIndex < accessSum) {
                        queryTerminalAisleParamInfo();
                    } else {//所有通道的传感器参数都查询了
                        dismissProgressDialog();
                        sensorAdapter.notifyDataSetChanged();
                    }
                } else {//只刷新单个通道的传感器数据
                    dismissProgressDialog();
                    sensorAdapter.notifyDataSetChanged();
                }
            }
            default:
                break;
        }
    }

    private void processSensorParamsInfo(VmsTerminalSensorInfo sensorInfo) {
        if (sensorInfo == null)
            return;

        if (curSensorIndex == -1) {
            sensorHashMap.put(sensorInfo.getChannel(), sensorInfo);

            VmsTerminalSensorItem sensorItem = new VmsTerminalSensorItem();
            sensorItem.setChannel(sensorInfo.getChannel());
            sensorItem.setInsert(sensorInfo.getInsert().trim().equals("1"));
            sensorItemList.add(sensorItem);
        } else {
            sensorHashMap.remove(sensorInfo.getChannel());
            sensorHashMap.put(sensorInfo.getChannel(), sensorInfo);

            VmsTerminalSensorItem sensorItem = sensorItemList.get(curSensorIndex);
            sensorItem.setChannel(sensorInfo.getChannel());
            sensorItem.setInsert(sensorInfo.getInsert().trim().equals("1"));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        curSensorIndex = -1;
    }
}
