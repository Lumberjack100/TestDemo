package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeExecutiveAgencyInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.view.adme.AdmeExecutiveAgencyView;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/23 <br/>
 * 描述：     ADME 执行机构配置页面
 */
public class NetAdmeExecutiveAgencyFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.admeExecutiveAgencyView)
    AdmeExecutiveAgencyView admeExecutiveAgencyView;

    @BindView(R.id.maskLayerLayout)
    ViewGroup maskLayerLayout;

    public static NetAdmeExecutiveAgencyFragment newInstance(DeviceInfo deviceInfo) {
        NetAdmeExecutiveAgencyFragment fragment = new NetAdmeExecutiveAgencyFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.adme_executive_agency_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        queryParamInfo();
        //TODO 设备处于自动监测模式时，不可编辑参数(后期还要考虑点击编辑按钮时的页面状态切换)
        if (admeViewModel.deviceMode == 0) {
            configPageViewModel.configPageEditableChanged.setValue(true);
        } else {
            configPageViewModel.configPageEditableChanged.setValue(false);
        }

    }

    /**
     * 获取执行机构参数
     */
    private void queryParamInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_EXECUTIVE_AGENCY);
        showWaitDialog("加载中...");
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    @OnClick({R.id.ll_measure_method, R.id.ll_data_settlement_method, R.id.ll_data_response, R.id.ll_measurement_interval_per_round, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.ll_measure_method) {
            admeExecutiveAgencyView.showMeasureMethodDialog(mActivity);

        } else if (id == R.id.ll_data_settlement_method) {
            admeExecutiveAgencyView.showDataSettlementMethodDialog(mActivity);

        } else if (id == R.id.ll_data_response) {
            admeExecutiveAgencyView.showDataResponseDialog(mActivity);

        } else if (id == R.id.ll_measurement_interval_per_round) {
            admeExecutiveAgencyView.showMeasIntervalPerRoundsDialog(mActivity);

        } else if (id == R.id.btn_confirm) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!admeExecutiveAgencyView.checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            String command = admeExecutiveAgencyView.getSetCommand();
            if (!TextUtils.isEmpty(command)) {
                showWaitDialog("处理中...");
                doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
            }
        }
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            dismissWaitDialog();
            showDispatchFailedDialog(cmdStr);
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
            case ADME_MD_GET_EXECUTIVE_AGENCY: {//获取ADME的执行机构配置参数
                dismissWaitDialog();
                IOTCommandResult<AdmeExecutiveAgencyInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询执行机构参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(commandResult.getMessage().contains("unsupported") ? "设备版本不支持!" : errMsg);
                    maskLayerLayout.setVisibility(commandResult.getMessage().contains("unsupported") ? View.VISIBLE : View.GONE);
                    return;
                }
                admeExecutiveAgencyView.admeExecutiveAgencyInfo = commandResult.getResult();
                admeExecutiveAgencyView.initParamConfigInfo();
            }
            break;

            case ADME_MD_SET_EXECUTIVE_AGENCY: {//设置ADME的执行机构配置参数
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "设置执行机构参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                admeExecutiveAgencyView.doAfterSetting();
                saveConfigInfo();
            }
            break;

            case MD_SAVE_CONFIG_PARAM: {
                dismissWaitDialog();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "保存指令出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
            }
            ToastUtils.show("保存成功");
            break;

            default:
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        if (admeExecutiveAgencyView.checkValueIsChange(configPageViewModel.configPageEditableChanged.getValue())) {
            warnNotYetSettingBeforeLeavePage();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onEditableChanged(boolean isEditable) {
        admeExecutiveAgencyView.onEditableChanged(isEditable);
    }
}
