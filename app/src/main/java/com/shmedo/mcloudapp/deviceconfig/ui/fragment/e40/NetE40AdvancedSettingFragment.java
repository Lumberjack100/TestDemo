package com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.e40.E40RTKModeEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.e40.E40RTKModeInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.util.GsonFactory;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.FirmWareInfo;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.model.params.DispatchRawCmdParam;
import com.shmedo.mcloudapp.deviceconfig.model.params.FirmwareUpgrade;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.FirmWareSelectDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.BaseDispatchCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.CommonCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.DispatchCmdFailedDialog;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrCode;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.util.ResponseHandler;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2/25/21 <br/>
 * 描述：    E40网络模式设置页面
 */
public class NetE40AdvancedSettingFragment extends BaseNetIotCommunicateFragment {
    public static final String PRO_DEVICE_INFO = "com.shmedo.mcloudapp.PRO_DEVICE_INFO";

    private static final int REBOOT = 0x1002;
    private static final int RESET = 0x1003;

    @BindView(R.id.tv_rtk_mode)
    TextView mTvRTKMode;

    private int rtkModePos;
    private String rtkMode;//0表示基站，1表示移动站
    private E40RTKModeInfo rtkModeInfo;

    private ProjectDeviceInfo projectDeviceInfo;

    public static NetE40AdvancedSettingFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetE40AdvancedSettingFragment fragment = new NetE40AdvancedSettingFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectDeviceInfo = getArguments().getParcelable(PRO_DEVICE_INFO);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.net_e40_advanced_setting_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getRTKMode();
    }

    /**
     * 获取RTK模式
     */
    private void getRTKMode() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.GNSS_MD_GET_RTK);
        doCommonDispatchRawCmd(command);
    }

    /**
     * 设置RTK模式
     */
    private void setRTKMode() {
        E40RTKModeEntity entity = new E40RTKModeEntity();
        entity.setMode(rtkMode);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.GNSS_MD_SET_RTK, entity);
        doCommonDispatchRawCmd(command);
    }

    @OnClick({R.id.dataCenterConfigLayout, R.id.firmwareUpgradeLayout, R.id.rebootLayout, R.id.resetLayout, R.id.rtkModeLayout, R.id.corsServiceLayout, R.id.boardSolveLayout, R.id.sensorSettingLayout, R.id.wiredNetworkSettingLayout, R.id.fileDownloadLayout})
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

        } else if (id == R.id.rebootLayout) {//重启
            showWarnDialog("确定重启设备吗？", REBOOT);

        } else if (id == R.id.resetLayout) {//恢复出厂设置
            showWarnDialog("确定恢复出厂设置吗？", RESET);

        } else if (id == R.id.rtkModeLayout) {//RTK模式
            showRTKModeDialog();

        } else if (id == R.id.corsServiceLayout) {//CORS 服务设置

        } else if (id == R.id.boardSolveLayout) {//板卡解算设置

        } else if (id == R.id.sensorSettingLayout) {//传感器设置

        } else if (id == R.id.wiredNetworkSettingLayout) {//有线网络设置

        } else if (id == R.id.fileDownloadLayout) {//下载文件

        }
    }

    private BaseDialogFragment.DialogFragmentClickListener firmWareSelectListener = new BaseDialogFragment.DialogFragmentClickListener<FirmWareInfo>() {
        @Override
        public boolean onPositiveClick(View view, FirmWareInfo firmWareInfo) {
//            showProgressDialog("指令下发中...");
            doFirmwareUpgrade(firmWareInfo.getId());
            return true;
        }

        @Override
        public void onNegativeClick(View view) {

        }
    };

    /**
     * 选择RTK模式
     */
    private void showRTKModeDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true)
                .asBottomList("", new String[]{"基站", "移动站"},
                        null, rtkModePos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                rtkModePos = position;
                                rtkMode = String.valueOf(position);
                                mTvRTKMode.setText(text);
                                setRTKMode();
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
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
                            case REBOOT: {
                                String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.REBOOT);
                                doCommonDispatchRawCmd(command);
                            }
                            break;

                            case RESET: {
                                String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.RESET);
                                doCommonDispatchRawCmd(command);
                            }
                            break;
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    /**
     * 调用指令透传接口
     *
     * @param content
     */
    private void doCommonDispatchRawCmd(String content) {
        DispatchRawCmdParam rawCmdParam = new DispatchRawCmdParam();
        rawCmdParam.setContent(content);
        rawCmdParam.setCompanyID(MCloudApp.getCompanyID());
        rawCmdParam.setDeviceIDList(Arrays.asList(projectDeviceInfo.getId()));

        showProgressDialog("处理中...");
        processDispatchRawCmd(rawCmdParam);
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
//        dismissProgressDialog();
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            dismissProgressDialog();
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

            case GNSS_MD_GET_RTK:
                title = "获取RTK模式";
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
                dismissProgressDialog();
                newFragment = new CommonCmdDialog("固件升级", "固件升级中...", "此过程耗时较长,请耐心等待", msgIDList);
                break;

            case REBOOT:
                dismissProgressDialog();
                newFragment = new CommonCmdDialog("重新启动", "正在重启中...", "预计耗时三分钟,请耐心等待", msgIDList);
                break;

            case RESET:
                dismissProgressDialog();
                newFragment = new CommonCmdDialog("恢复出厂设置", "设备开始恢复出厂设置...", "此过程耗时较长,请耐心等待", msgIDList);
                break;

            case GNSS_MD_GET_RTK:
            case GNSS_MD_SET_RTK:
                if (msgIDList != null && msgIDList.size() > 0) {
                    startQueryCmdResponseRunnable(2000);
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
        super.onQueryCmdResponseResultSuccess(queryCmdResult);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case GNSS_MD_GET_RTK: {//获取RTK
                IOTCommandResult<E40RTKModeInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取RTK模式出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                rtkModeInfo = commandResult.getResult();
                initRTKMode();
            }
            break;

            case GNSS_MD_SET_RTK: {
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置RTK模式出错!", cmdResult.getReason());
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

    private void initRTKMode() {
        if (rtkModeInfo == null) {
            Timber.e("E40RTKModeInfo is Null!");
            rtkModeInfo = new E40RTKModeInfo();
            return;
        }

        rtkMode = rtkModeInfo.getMode().trim();
        if (rtkMode.equals("0")) {
            mTvRTKMode.setText("基站");
            rtkModePos = 0;
        } else if (rtkMode.equals("1")) {
            mTvRTKMode.setText("移动站");
            rtkModePos = 1;
        }
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    protected void onResponse(String msgId, ErrCode errCode) {
                        dismissProgressDialog();
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
                        dismissProgressDialog();
                        doDispatchFailed("$cmd=md_upgrade");
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }

}