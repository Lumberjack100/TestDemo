package com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20;

import static autodispose2.AutoDispose.autoDisposable;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.util.GsonFactory;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.FirmWareInfo;
import com.shmedo.mcloudapp.deviceconfig.model.params.FirmwareUpgrade;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.FirmWareSelectDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.BaseDispatchCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.CommonCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.DispatchCmdFailedDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrCode;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.util.ResponseHandler;

import java.util.Arrays;
import java.util.List;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/20/21 <br/>
 * 描述：    M20 网络模式高级设置页面
 */
public class NetM20AdvancedSettingFragment extends BaseNetIotCommunicateFragment {
    private static final int LEVEL_INITIAL = 0x1001;
    private static final int REBOOT = 0x1002;
    private static final int RESET = 0x1003;

    public static NetM20AdvancedSettingFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetM20AdvancedSettingFragment fragment = new NetM20AdvancedSettingFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.net_m20_advanced_setting_fragment;
    }


    @OnClick({R.id.dataCenterConfigLayout, R.id.firmwareUpgradeLayout, R.id.horizontalInitializationLayout, R.id.rebootLayout, R.id.resetLayout})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }

        int id = v.getId();
        if (id == R.id.dataCenterConfigLayout) {
            DataCenterHomeActivity.startActivity(mActivity, AppContants.DeviceType.M20, projectDeviceInfo, AppContants.DataCenterConfigMethod.ADVANCED_CONFIG);

        } else if (id == R.id.firmwareUpgradeLayout) {//固件升级
            FirmWareSelectDialog newFragment = new FirmWareSelectDialog(MCloudApp.getCompanyID(), projectDeviceInfo.getDeviceTypeID());
            newFragment.setDialogFragmentClickListener(firmWareSelectListener);
            newFragment.show(getChildFragmentManager(), "dialog");

        } else if (id == R.id.horizontalInitializationLayout) {
            showWarnDialog("确定进行水平初始化吗？", LEVEL_INITIAL);

        } else if (id == R.id.rebootLayout) {//重启
            showWarnDialog("确定重启设备吗？", REBOOT);

        } else if (id == R.id.resetLayout) {//恢复出厂设置
            showWarnDialog("确定恢复出厂设置吗？", RESET);
        }
    }

    private BaseDialogFragment.DialogFragmentClickListener firmWareSelectListener = new BaseDialogFragment.DialogFragmentClickListener<FirmWareInfo>() {
        @Override
        public boolean onPositiveClick(View view, FirmWareInfo firmWareInfo) {
//            showWaitDialog("指令下发中...");
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
                            case LEVEL_INITIAL: {
                                String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.M20_MD_LEVEL_INITIAL);
                                showWaitDialog("处理中...");
                                doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
                            }
                            break;

                            case REBOOT: {
                                String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.REBOOT);
                                showWaitDialog("处理中...");
                                doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
                            }
                            break;

                            case RESET: {
                                String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.RESET);
                                showWaitDialog("处理中...");
                                doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
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
        dismissWaitDialog();
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
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

            case M20_MD_LEVEL_INITIAL:
                title = "水平初始化";
                break;

            case REBOOT:
                title = "重新启动";
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
                newFragment = new CommonCmdDialog("固件升级", "固件升级中...", "此过程耗时较长,请耐心等待", msgIDList);
                break;

            case M20_MD_LEVEL_INITIAL:
                newFragment = new CommonCmdDialog("水平初始化", "水平初始化完成", msgIDList);
                break;

            case REBOOT:
                newFragment = new CommonCmdDialog("重新启动", "正在重启中...", "预计耗时三分钟,请耐心等待", msgIDList);
                break;

            case RESET:
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
        FirmwareUpgrade parameter = new FirmwareUpgrade(MCloudApp.getCompanyID(), projectDeviceInfo.getId(), firmwareID);
        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .FirmwareUpgrade(MCloudApp.getAccessToken(), body)
                .doOnDispose(() -> Timber.i("Disposing subscription"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())))
                .subscribe(new BaseObserver<String>() {
                    @Override
                    protected void onResponse(String msgId, ErrCode errCode) {
//                        dismissWaitDialog();
                        if (!ResponseHandler.getInstance().handleResponse(errCode)) {
                            if (errCode.getCode() == 0) {
                                msgIDList.clear();
                                msgIDList.add(msgId);
                                doDispatchSuccess("$cmd=md_upgrade");
                            } else {
                                doDispatchFailed("$cmd=md_upgrade");
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
//                        dismissWaitDialog();
                        doDispatchFailed("$cmd=md_upgrade");
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }
}