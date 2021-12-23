package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeInclinometerInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.view.adme.AdmeInclinometerView;
import com.shmedo.mcloudapp.deviceconfig.model.ProjectDeviceInfo;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/23 <br/>
 * 描述：       ADME 测斜仪参数配置页面
 */
public class NetAdmeInclinometerFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.admeInclinometerView)
    AdmeInclinometerView admeInclinometerView;

    @BindView(R.id.maskLayerLayout)
    ViewGroup maskLayerLayout;


    public static NetAdmeInclinometerFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetAdmeInclinometerFragment fragment = new NetAdmeInclinometerFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adme_inclinometer_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        queryParamConfigInfo();
        //TODO #gh# 设备处于自动监测模式时，不可编辑参数(后期还要考虑点击编辑按钮时的页面状态切换)
        if (admeViewModel.deviceMode == 0) {
            configPageViewModel.configPageEditableChanged.setValue(true);
        } else {
            configPageViewModel.configPageEditableChanged.setValue(false);
        }
    }

    /**
     * 获取ADME的测斜仪参数
     */
    private void queryParamConfigInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_INCLINOMETER);
        showWaitDialog("加载中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    @OnClick({R.id.ll_inclinometer_type, R.id.ll_low_power_mode, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.ll_inclinometer_type) {
            admeInclinometerView.showInclinometerTypeDialog(mActivity);

        } else if (id == R.id.ll_low_power_mode) {
            admeInclinometerView.showLowPowerModeDialog(mActivity);

        } else if (id == R.id.btn_confirm) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!admeInclinometerView.checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            String command = admeInclinometerView.getSetCommand();
            if (!TextUtils.isEmpty(command)) {
                showWaitDialog("处理中...");
                doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
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
            case ADME_MD_GET_INCLINOMETER: {//获取ADME的测斜仪配置参数
                dismissWaitDialog();
                IOTCommandResult<AdmeInclinometerInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取设备的测斜仪参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(commandResult.getMessage().contains("unsupported") ? "设备版本不支持!" : errMsg);
                    maskLayerLayout.setVisibility(commandResult.getMessage().contains("unsupported") ? View.VISIBLE : View.GONE);
                    return;
                }
                admeInclinometerView.admeInclinometerInfo = commandResult.getResult();
                admeInclinometerView.initParamConfigInfo();
            }
            break;

            case ADME_MD_SET_INCLINOMETER: {//设置ADME的测斜仪配置参数
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "保存测斜仪参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                admeInclinometerView.doAfterSetting();
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
        if (admeInclinometerView.checkValueIsChange(configPageViewModel.configPageEditableChanged.getValue())) {
            warnNotYetSettingBeforeLeavePage();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onEditableChanged(boolean isEditable) {
        admeInclinometerView.onEditableChanged(isEditable);
    }
}
