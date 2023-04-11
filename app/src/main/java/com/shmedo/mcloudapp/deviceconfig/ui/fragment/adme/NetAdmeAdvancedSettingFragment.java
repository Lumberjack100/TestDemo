package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import static autodispose2.AutoDispose.autoDisposable;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.DebouncingUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeWorkModeEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeWorkModeInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.FirmWareInfo;
import com.shmedo.mcloudapp.deviceconfig.model.FirmwareCmdInfo;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.model.params.FirmwareCmdParam;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.FirmWareSelectDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.BaseDispatchCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.CommonCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.DispatchCmdFailedDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrorInfo;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.RequestHeader;
import com.shmedo.mcloudapp.network.ServiceAddressType;
import com.shmedo.mcloudapp.util.ResponseHandler;

import java.util.Arrays;
import java.util.List;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/23 <br/>
 * 描述：     TODO
 */
public class NetAdmeAdvancedSettingFragment extends BaseNetIotCommunicateFragment {
    private static final int REBOOT = 0x1000;
    private static final int RESET = 0x1001;

    @BindView(R.id.tv_work_mode)
    TextView mTvWorkMode;

    private int workModePos;
    private String workMode;// 工作模式(0:常规测量模式，1:特定点位模式，2:静态测量模式，3:设备停用模式)
    private AdmeWorkModeInfo workModeInfo;

    public static NetAdmeAdvancedSettingFragment newInstance(DeviceInfo deviceInfo) {
        NetAdmeAdvancedSettingFragment fragment = new NetAdmeAdvancedSettingFragment();
        Bundle args = new Bundle();
        args.putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adme_advanced_setting_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getWorkMode();
    }

    /**
     * 获取设备工作模式
     */
    private void getWorkMode() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_WORK_MODE);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 设置设备工作模式
     */
    private void setWorkMode() {
        AdmeWorkModeEntity entity = new AdmeWorkModeEntity();
        entity.setWorkmode(workMode);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_WORK_MODE, entity);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 重启指令
     */
    private void rebootDevice() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.REBOOT);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 恢复出厂设置指令
     */
    private void resetDevice() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.RESET);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    @OnClick({R.id.dataCenterConfigLayout, R.id.rebootLayout, R.id.resetLayout, R.id.firmwareUpgradeLayout, R.id.workModeLayout})
    public void onClick(View v) {
        if (!DebouncingUtils.isValid(v, 1000)) {
            return;
        }
        int id = v.getId();
        if (id == R.id.dataCenterConfigLayout) {
            DataCenterHomeActivity.startActivity(mActivity, ProductType.ADME, deviceInfo);

        } else if (id == R.id.rebootLayout) {//重启
            showWarnDialog("确定重启设备吗？", REBOOT);

        } else if (id == R.id.resetLayout) {//恢复出厂设置
            showWarnDialog("确定恢复出厂设置吗？", RESET);

        } else if (id == R.id.firmwareUpgradeLayout) {//固件升级
            FirmWareSelectDialog newFragment = new FirmWareSelectDialog(deviceInfo.getProductID());
            newFragment.setDialogFragmentClickListener(firmWareSelectListener);
            newFragment.show(getChildFragmentManager(), "dialog");

        } else if (id == R.id.workModeLayout) {//工作模式
            showWorkModeDialog();
        }
    }

    private BaseDialogFragment.DialogFragmentClickListener firmWareSelectListener = new BaseDialogFragment.DialogFragmentClickListener<FirmWareInfo>() {
        @Override
        public boolean onPositiveClick(View view, FirmWareInfo firmWareInfo) {
            doFirmwareUpgrade(firmWareInfo.getId());
            return true;
        }

        @Override
        public void onNegativeClick(View view) {

        }
    };

    /**
     * 选择工作模式
     */
    private void showWorkModeDialog() {
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"常规测量模式", "特定点位模式", "静态测量模式", "设备停用模式"},
                        null, workModePos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                workModePos = position;
                                workMode = String.valueOf(position);
                                mTvWorkMode.setText(text);
                                setWorkMode();
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    /**
     * 危险操作前弹框提醒
     */
    private void showWarnDialog(String content, int operateType) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title("温馨提示")
                .content(content)
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        switch (operateType) {
                            case REBOOT:
                                rebootDevice();
                                break;

                            case RESET:
                                resetDevice();
                                break;
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
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
            doDispatchFailed(cmdStr);
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        doDispatchSuccess(cmdStr);
    }


    /**
     * 指令下发失败弹框
     */
    private void doDispatchFailed(String cmdStr) {
        String title = "";
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case MD_UPGRADE:
                title = "固件升级";
                break;

            case REBOOT:
                title = "重新启动";
                break;

            case RESET:
                title = "恢复出厂设置";
                break;

            case E40_MD_GET_RTK:
                title = "工作模式";
                break;

            default:
                break;
        }
        BaseDispatchCmdDialog newFragment = new DispatchCmdFailedDialog(title);
        newFragment.show(getChildFragmentManager(), "dialog");
    }

    /**
     * 指令下发成功弹框
     */
    private void doDispatchSuccess(String cmdStr) {
        BaseDispatchCmdDialog newFragment = null;
        //下发指令成功，弹出对话框开始轮询查询指令响应
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case MD_UPGRADE:
                dismissWaitDialog();
                newFragment = new CommonCmdDialog("固件升级", "固件升级中...", "此过程耗时较长,请耐心等待", msgIDList);
                break;

            case REBOOT:
                dismissWaitDialog();
                newFragment = new CommonCmdDialog("重新启动", "正在重启中...", "预计耗时三分钟,请耐心等待", msgIDList);
                break;

            case RESET:
                dismissWaitDialog();
                newFragment = new CommonCmdDialog("恢复出厂设置", "设备开始恢复出厂设置...", "此过程耗时较长,请耐心等待", msgIDList);
                break;

            case ADME_MD_GET_WORK_MODE:
            case ADME_MD_SET_WORK_MODE:
                if (msgIDList != null && msgIDList.size() > 0) {
                    startQueryCmdResponse();
                }
                break;

            default:
                break;
        }
        if (newFragment != null)
            newFragment.show(getChildFragmentManager(), "dialog");
    }

    /**
     * 查询指令响应结果出错
     *
     * @param errMsg
     */
    @Override
    protected void onQueryCmdResponseResultError(String errMsg) {
        super.onQueryCmdResponseResultError(errMsg);
        ToastUtils.show("查询设备响应错误");
    }

    /**
     * 查询指令响应结果超时
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultTimeOut(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultTimeOut(queryCmdResult);
        ToastUtils.show("查询设备响应超时");
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
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case ADME_MD_GET_WORK_MODE: {//获取设备的工作模式
                dismissWaitDialog();
                IOTCommandResult<AdmeWorkModeInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取设备的工作模式出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                workModeInfo = commandResult.getResult();
                initWorkMode();
            }
            break;

            case ADME_MD_SET_WORK_MODE: {//设置ADME的工作模式
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "设置A工作模式出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
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
            ToastUtils.show("已设置");
            break;

            default:
                break;
        }
    }

    private void initWorkMode() {
        if (workModeInfo == null) {
            Timber.e("AdmeWorkModeInfo is Null!");
            workModeInfo = new AdmeWorkModeInfo();
            return;
        }

        workMode = workModeInfo.getWorkmode().trim();
        if (workMode.equals("0")) {
            mTvWorkMode.setText("常规测量模式");
            workModePos = 0;
        } else if (workMode.equals("1")) {
            mTvWorkMode.setText("特定点位模式");
            workModePos = 1;

        } else if (workMode.equals("2")) {
            mTvWorkMode.setText("静态测量模式");
            workModePos = 2;

        } else if (workMode.equals("3")) {
            mTvWorkMode.setText("设备停用模式");
            workModePos = 3;
        }
    }

    /**
     * 固件升级
     */
    private void doFirmwareUpgrade(int firmwareID) {
        FirmwareCmdParam parameter = new FirmwareCmdParam();
        parameter.setDeviceTokenList(Arrays.asList(deviceInfo.getDeviceToken()));
        parameter.setFirmwareID(firmwareID);
        String json = GsonUtils.toJson(parameter);
        RequestBody body = RequestBody.create(json, RequestHeader.JSON_TYPE);

        MDRetrofit.getInstance()
                .createService(ServiceAddressType.IOT_INTERACTIVE_SERVICE_ADDRESS)
                .batchFirmwareUpgrade(MCloudApp.getAccessToken(), body)
                .doOnDispose(() -> Timber.i("Disposing subscription"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())))
                .subscribe(new BaseObserver<List<FirmwareCmdInfo>>() {
                    @Override
                    protected void onResponse(List<FirmwareCmdInfo> firmwareCmdInfoList, ErrorInfo errorInfo) {
                        dismissWaitDialog();
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                msgIDList.clear();
                                msgIDList.add(firmwareCmdInfoList.get(0).getMsgID());
                                doDispatchSuccess("$cmd=md_upgrade");
                            } else {
                                doDispatchFailed("$cmd=md_upgrade");
                                if (!TextUtils.isEmpty(errorInfo.getMsg())) {
                                    ToastUtils.show(errorInfo.getMsg());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissWaitDialog();
                        doDispatchFailed("$cmd=md_upgrade");
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }
}
