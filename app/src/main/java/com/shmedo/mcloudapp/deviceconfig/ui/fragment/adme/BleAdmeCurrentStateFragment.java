package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.os.Message;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.StringUtils;
import com.hjq.toast.ToastUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.adme.AdmeCurrentStateInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeMotionState;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.view.adme.AdmeCurrentStateView;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/28<br/>
 * 描述：     ADME  查看当前状态页面
 */
public class BleAdmeCurrentStateFragment extends BaseUSRBleIotCommunicateFragment {
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

    @BindView(R.id.admeCurrentStateView)
    AdmeCurrentStateView admeCurrentStateView;

    private AdmeCurrentStateInfo currentStateInfo;


    public static BleAdmeCurrentStateFragment newInstance() {
        return new BleAdmeCurrentStateFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adme_current_state_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
                if (!isConnected()) {
                    ToastUtils.show(StringUtils.getString(R.string.refresh_failed_while_device_disconnected));
                    mRefreshLayout.finishRefresh(false);
                    return;
                }
                queryEquipmentState();
                startDefaultProgress(null, AppContants.MsgWhat.MSG_SMART_REFRESH, DELAY_15000_MILLIS);
            }
        });
    }

    /**
     * 获取设备的当前状态
     */
    private void queryEquipmentState() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_EQUIPMENT_STATE);
        sendCommand(command);
    }

    /**
     * 获取电机的运行状态
     */
    private void queryMotorState() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_MOTION_STATE);
        sendCommand(command);
    }

    @OnClick({R.id.ll_device_abnormal_diagnosis})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.ll_device_abnormal_diagnosis) {
            admeCurrentStateView.showErrorModulesInfoDialog(currentStateInfo);
        }
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case ADME_MD_GET_EQUIPMENT_STATE: {
//                mRefreshLayout.finishRefresh(true);
                IOTCommandResult<AdmeCurrentStateInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取设备状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                currentStateInfo = commandResult.getResult();
                admeCurrentStateView.initStatusInfo(currentStateInfo);
                queryMotorState();
            }
            break;

            case ADME_MD_GET_MOTION_STATE: {//获取ADME的运行状态
                 mRefreshLayout.finishRefresh(true);
                IOTCommandResult<AdmeMotionState> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取CTR工作状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
//                    ToastUtils.show(errMsg);
                    return;
                }
                AdmeMotionState admeMotionState = commandResult.getResult();
                admeCurrentStateView.updateMotionState(admeMotionState);
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
}