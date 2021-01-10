package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

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
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeWorkModeEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeWorkModeInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeDataCenterHomeActivity;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/29<br/>
 * 描述：     ADME 设置页面
 */
public class BleAdmeAdvancedSettingFragment extends BaseBleIotCommunicateFragment {
    private static final int REBOOT = 0x1000;
    private static final int RESET = 0x1001;

    @BindView(R.id.tv_work_mode)
    TextView mTvWorkMode;

    private int workModePos;

    private String workMode;// 工作模式(0:常规测量模式，1:特定点位模式，2:静态测量模式，3:设备停用模式)
    private AdmeWorkModeInfo workModeInfo;

    public static BleAdmeAdvancedSettingFragment newInstance() {
        return new BleAdmeAdvancedSettingFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_advanced_setting_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getWorkMode();
    }

    @OnClick({R.id.dataCenterConfigLayout, R.id.rebootLayout, R.id.resetLayout, R.id.firmwareUpgradeLayout, R.id.workModeLayout})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }
        if (!isConnected()) {
            ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
            return;
        }

        int id = v.getId();
        if (id == R.id.dataCenterConfigLayout) {
            AdmeDataCenterHomeActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, AppContants.DataCenterConfigMethod.ADVANCED_CONFIG);

        } else if (id == R.id.rebootLayout) {//重启
            showWarnDialog("确定重启设备吗？", REBOOT);

        } else if (id == R.id.resetLayout) {//恢复出厂设置
            showWarnDialog("确定恢复出厂设置吗？", RESET);

        } else if (id == R.id.firmwareUpgradeLayout) {//固件升级
            ToastUtils.show("正在研发中,敬请期待...");

        } else if (id == R.id.workModeLayout) {//工作模式
            showWorkModeDialog();
        }
    }

    /**
     * 获取设备工作模式
     */
    private void getWorkMode() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_WORK_MODE);
        sendCommand(command);
    }

    /**
     * 设置设备工作模式
     */
    private void setWorkMode() {
        AdmeWorkModeEntity entity = new AdmeWorkModeEntity();
        entity.setWorkmode(workMode);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_WORK_MODE, entity);
        sendCommand(command);
    }

    /**
     * 重启指令
     */
    private void rebootDevice() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.REBOOT);
        sendCommand(command);
    }

    /**
     * 恢复出厂设置指令
     */
    private void resetDevice() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.RESET);
        sendCommand(command);
    }

    /**
     * 选择工作模式
     */
    private void showWorkModeDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"常规测量模式", "特定点位模式", "静态测量模式", "设备停用模式"},
                        null, workModePos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                workModePos = position;
                                workMode = String.valueOf(position);
                                mTvWorkMode.setText(text);
                                setWorkMode();
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

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case ADME_MD_GET_WORK_MODE: {//获取设备的工作模式
                stopProgressRunnable();
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
                    String errMsg = String.format("%s %s", "设置A工作模式出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show("已设置");
            }
            break;

            case REBOOT: {//重启
                stopProgressRunnable();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "发送重启指令失败!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show("发送指令成功,设备稍后将重启,请等待后重新连接");
                MCloudApp.getMainHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        disconnectDevice();
                    }
                }, 3000);
            }
            break;

            case RESET: {//恢复出厂设置
                stopProgressRunnable();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "发送恢复出厂设置指令失败!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show("发送指令成功,设备稍后将重启,请等待后重新连接");
                MCloudApp.getMainHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        disconnectDevice();
                    }
                }, 3000);
            }
            break;

            default:
                super.parseResponseMessage(cmdStr);
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
}