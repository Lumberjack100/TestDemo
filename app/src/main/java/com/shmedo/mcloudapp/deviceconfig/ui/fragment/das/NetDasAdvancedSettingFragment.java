package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import static autodispose2.AutoDispose.autoDisposable;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.DebouncingUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.FirmWareInfo;
import com.shmedo.mcloudapp.deviceconfig.model.FirmwareCmdInfo;
import com.shmedo.mcloudapp.deviceconfig.model.params.FirmwareCmdParam;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.AudibleAlarmActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.VoiceBroadcastActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.FixedPointReportingActivity;
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
 * 网络模式高级设置
 */
public class NetDasAdvancedSettingFragment extends BaseNetIotCommunicateFragment {
    private static final int RESET = 0x1003;

    @BindView(R.id.audibleAlarmLayout)
    ViewGroup audibleAlarmLayout;//声光报警器

    public static NetDasAdvancedSettingFragment newInstance(DeviceInfo deviceInfo) {
        NetDasAdvancedSettingFragment fragment = new NetDasAdvancedSettingFragment();
        Bundle args = new Bundle();
        args.putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_net_das_advanced_setting;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        audibleAlarmLayout.setVisibility(deviceInfo != null && deviceInfo.getProductName().contains("MR701") ? View.VISIBLE : View.GONE);
    }

    @OnClick({R.id.firmwareUpgradeLayout, R.id.resetLayout, R.id.fixedPointReportingLayout, R.id.voiceBroadcastLayout, R.id.audibleAlarmLayout})
    public void onClick(View view) {
        if(!DebouncingUtils.isValid(view, 1000)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.firmwareUpgradeLayout) {//固件升级
            FirmWareSelectDialog newFragment = new FirmWareSelectDialog(deviceInfo.getProductID());
            newFragment.setDialogFragmentClickListener(firmWareSelectListener);
            newFragment.show(getChildFragmentManager(), "dialog");

        } else if (id == R.id.resetLayout) {//恢复出厂设置
            showWarnDialog("确定恢复出厂设置吗？", RESET);

        } else if (id == R.id.fixedPointReportingLayout) {//定时定点上报
            FixedPointReportingActivity.startActivity(mActivity, deviceInfo);

        } else if (id == R.id.voiceBroadcastLayout) {
            VoiceBroadcastActivity.startActivity(mActivity, deviceInfo);

        } else if (id == R.id.audibleAlarmLayout) {
            AudibleAlarmActivity.startActivity(mActivity, deviceInfo);
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
                            case RESET: {
                                String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.RESET);
                                showWaitDialog("处理中...");
                                doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
                            }
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

            case RESET:
                title = "恢复出厂设置";
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

            case RESET:
                dismissWaitDialog();
                newFragment = new CommonCmdDialog("恢复出厂设置", "设备开始恢复出厂设置...", "此过程耗时较长,请耐心等待", msgIDList);
                break;

            default:
                break;
        }
        if (newFragment != null)
            newFragment.show(getChildFragmentManager(), "dialog");
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
