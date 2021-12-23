package com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.ServerNumberEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.ServerNumber;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.DataCenterInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.callback.DataCenterConfigListener;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.view.DataCenterAdvancedConfigView;
import com.shmedo.mcloudapp.deviceconfig.model.ProjectDeviceInfo;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/22/21 <br/>
 * 描述：    通用网络模式数据中心高级参数配置页面
 */
public class UniversalNetDataCenterAdvancedConfigFragment extends BaseNetIotCommunicateFragment implements DataCenterConfigListener {
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

    @BindView(R.id.dataCenterAdvancedConfigView)
    DataCenterAdvancedConfigView dataCenterAdvancedConfigView;

    private int deviceType = AppContants.DeviceType.DAS;
    private ServerNumber serverNumber;
    private String serverStatus;


    public static UniversalNetDataCenterAdvancedConfigFragment newInstance(int deviceType, ServerNumber serverNumber, String status, ProjectDeviceInfo projectDeviceInfo) {
        UniversalNetDataCenterAdvancedConfigFragment fragment = new UniversalNetDataCenterAdvancedConfigFragment();
        Bundle args = new Bundle();
        args.putInt(AppContants.Extras.DEVICE_TYPE, deviceType);
        args.putSerializable(AppContants.Extras.DATA_SERVER_NUMBER, serverNumber);
        args.putSerializable(AppContants.Extras.DATA_SERVER_STATUS, status);
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deviceType = getArguments().getInt(AppContants.Extras.DEVICE_TYPE, AppContants.DeviceType.DAS);
            serverNumber = (ServerNumber) getArguments().getSerializable(AppContants.Extras.DATA_SERVER_NUMBER);
            serverStatus = getArguments().getString(AppContants.Extras.DATA_SERVER_STATUS);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.universal_data_center_advanced_config_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dataCenterAdvancedConfigView.initData(deviceType, serverNumber, serverStatus);
        dataCenterAdvancedConfigView.setDataCenterConfigListener(this);
        initRefreshLayout();
        mRefreshLayout.setEnableLoadMore(false);
        //是否在刷新的时候禁止内容的一切手势操作（默认false）
        mRefreshLayout.setDisableContentWhenRefresh(true);
        mRefreshLayout.autoRefresh();
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                queryDataCenterInfo();
            }
        });
    }

    /**
     * 获取设备的数据中心参数
     */
    private void queryDataCenterInfo() {
        ServerNumberEntity serverNumberEntity = new ServerNumberEntity(serverNumber.toInt());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_GET_DATA_CENTER, serverNumberEntity);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    @Override
    public void onCloseDataServer(String command) {
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    @Override
    public void onSaveConfig(String command) {
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    @Override
    public boolean onCheckConnect() {
        return true;
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            if (mRefreshLayout.isRefreshing()) {
                mRefreshLayout.finishRefresh(false);
            }
            dismissWaitDialog();
            ToastUtils.show("下发指令失败");
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponse();
        }
    }

    /**
     * 查询指令响应结果出错
     *
     * @param errMsg
     */
    @Override
    protected void onQueryCmdResponseResultError(String errMsg) {
        super.onQueryCmdResponseResultError(errMsg);
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh(false);
        }
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
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh(false);
        }
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

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(queryCmdResult.getCmdEngName());
        switch (type) {
            case MD_GET_DATA_CENTER: {//获取设备的数据中心参数
                mRefreshLayout.finishRefresh(true);
                IOTCommandResult<DataCenterInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询数据中心参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                dataCenterAdvancedConfigView.dataCenterInfo = commandResult.getResult();
                dataCenterAdvancedConfigView.initDataCenterData();
            }
            break;

            case MD_SET_DATA_CENTER: {//设置设备的数据中心参数
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置数据中心参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                dataCenterAdvancedConfigView.doAfterSetting();
                if (dataCenterAdvancedConfigView.isSaveParamOperation) {
                    ToastUtils.show("保存成功");
                }
            }
            break;

            default:
                break;
        }
    }

    private void setResult() {
        Intent intent = new Intent();
        mActivity.setResult(dataCenterAdvancedConfigView.isResultOK ? Activity.RESULT_OK : Activity.RESULT_CANCELED, intent);
    }

    @Override
    public boolean onBackPressed() {
        setResult();
        if (dataCenterAdvancedConfigView.checkValueIsChange()) {
            warnNotYetSettingBeforeLeavePage();
            return true;
        } else {
            return false;
        }
    }
}