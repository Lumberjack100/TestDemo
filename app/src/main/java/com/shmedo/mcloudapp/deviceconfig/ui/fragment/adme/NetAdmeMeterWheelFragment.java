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
import com.shmedo.configlibrary.iot.model.adme.AdmeMeterWheelInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.view.adme.AdmeMeterWheelView;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/23 <br/>
 * 描述：      ADME 计米轮参数配置页面
 */
public class NetAdmeMeterWheelFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.admeMeterWheelView)
    AdmeMeterWheelView admeMeterWheelView;

    @BindView(R.id.maskLayerLayout)
    ViewGroup maskLayerLayout;

    public static NetAdmeMeterWheelFragment newInstance(DeviceInfo deviceInfo) {
        NetAdmeMeterWheelFragment fragment = new NetAdmeMeterWheelFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adme_meter_wheel_fragment;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        queryParamConfigInfo();
        //TODO 设备处于自动监测模式时，不可编辑参数(后期还要考虑点击编辑按钮时的页面状态切换)
        if (admeViewModel.deviceMode == 0) {
            configPageViewModel.configPageEditableChanged.setValue(true);
        } else {
            configPageViewModel.configPageEditableChanged.setValue(false);
        }
    }

    /**
     * 获取ADME的计米轮参数
     */
    private void queryParamConfigInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_METER_WHEEL);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
        showWaitDialog("加载中...");
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.btn_confirm) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!admeMeterWheelView.checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            String command = admeMeterWheelView.getSetCommand();
            if (!TextUtils.isEmpty(command)) {
                doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
                showWaitDialog("处理中...");
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
            dismissProgressDialog();
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
            case ADME_MD_GET_METER_WHEEL: {//获取ADME的计米轮配置参数
                dismissWaitDialog();
                IOTCommandResult<AdmeMeterWheelInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取计米轮配置参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(commandResult.getMessage().contains("unsupported") ? "设备版本不支持!" : errMsg);
                    maskLayerLayout.setVisibility(commandResult.getMessage().contains("unsupported") ? View.VISIBLE : View.GONE);
                    return;
                }
                admeMeterWheelView.admeMeterWheelInfo = commandResult.getResult();
                admeMeterWheelView.initParamConfigInfo();
            }
            break;

            case ADME_MD_SET_METER_WHEEL: {//设置ADME的计米轮配置参数
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "保存计米轮配置参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                admeMeterWheelView.doAfterSetting();
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
        if (admeMeterWheelView.checkValueIsChange(configPageViewModel.configPageEditableChanged.getValue())) {
            warnNotYetSettingBeforeLeavePage();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onEditableChanged(boolean isEditable) {
        admeMeterWheelView.onEditableChanged(isEditable);
    }
}
