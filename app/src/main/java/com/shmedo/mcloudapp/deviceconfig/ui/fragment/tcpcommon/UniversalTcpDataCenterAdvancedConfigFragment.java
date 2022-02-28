package com.shmedo.mcloudapp.deviceconfig.ui.fragment.tcpcommon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;

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
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.configlibrary.iot.enums.ServerNumber;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.DataCenterInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.callback.DataCenterConfigListener;
import com.shmedo.mcloudapp.deviceconfig.view.DataCenterAdvancedConfigView;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/4/21 <br/>
 * 描述：      通用TCP模式数据中心高级参数配置页面
 */
public class UniversalTcpDataCenterAdvancedConfigFragment extends BaseTcpIotCommunicateFragment implements DataCenterConfigListener {
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

    @BindView(R.id.dataCenterAdvancedConfigView)
    DataCenterAdvancedConfigView dataCenterAdvancedConfigView;

    private ProductType productType = ProductType.DAS;
    private ServerNumber serverNumber;
    private String serverStatus;

    public static UniversalTcpDataCenterAdvancedConfigFragment newInstance(ProductType productType, ServerNumber serverNumber, String status) {
        UniversalTcpDataCenterAdvancedConfigFragment fragment = new UniversalTcpDataCenterAdvancedConfigFragment();
        Bundle args = new Bundle();
        args.putSerializable(AppContants.Extras.PRODUCT_TYPE, productType);
        args.putSerializable(AppContants.Extras.DATA_SERVER_NUMBER, serverNumber);
        args.putSerializable(AppContants.Extras.DATA_SERVER_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productType = (ProductType) getArguments().getSerializable(AppContants.Extras.PRODUCT_TYPE);
            serverNumber = (ServerNumber) getArguments().getSerializable(AppContants.Extras.DATA_SERVER_NUMBER);
            serverStatus = getArguments().getString(AppContants.Extras.DATA_SERVER_STATUS);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.universal_data_center_advanced_config_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dataCenterAdvancedConfigView.initData(productType, serverNumber, serverStatus);
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
                startDefaultProgress(null, AppContants.MsgWhat.MSG_SMART_REFRESH, DELAY_5000_MILLIS);
            }
        });
    }

    /**
     * 获取设备的数据中心参数
     */
    private void queryDataCenterInfo() {
        ServerNumberEntity serverNumberEntity = new ServerNumberEntity(serverNumber.toInt());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_GET_DATA_CENTER, serverNumberEntity);
        sendCommand(command);
    }

    @Override
    public void onCloseDataServer(String command) {
        sendCommand(command);
    }

    @Override
    public void onSaveConfig(String command) {
        sendCommand(command);
    }

    @Override
    public boolean onCheckConnect() {
        return tcpViewModel.getConnectStatus();
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case MD_GET_DATA_CENTER: {//获取数据中心参数
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

            case MD_SET_DATA_CENTER: {//
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
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        switch (msg.what) {
            case AppContants.MsgWhat.MSG_SMART_REFRESH:
                if (mRefreshLayout.isRefreshing()) {
                    mRefreshLayout.finishRefresh(false);
                    ToastUtils.show("刷新超时");
                }
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
        if (tcpViewModel.getConnectStatus()) {
            if (dataCenterAdvancedConfigView.checkValueIsChange()) {
                warnNotYetSettingBeforeLeavePage();
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
