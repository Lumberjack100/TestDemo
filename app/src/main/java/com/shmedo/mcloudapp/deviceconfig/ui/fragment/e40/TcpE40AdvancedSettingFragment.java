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
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.e40.E40BoardSolutionActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.e40.E40CORSServiceActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.e40.E40EthernetActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.tcpcommon.BaseTcpIotCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/4/21 <br/>
 * 描述：     E40 TCP 模式设置页面
 */
public class TcpE40AdvancedSettingFragment extends BaseTcpIotCommunicateFragment {
    private static final int REBOOT = 0x1002;
    private static final int RESET = 0x1003;

    @BindView(R.id.tv_rtk_mode)
    TextView mTvRTKMode;

    private int rtkModePos;
    private String rtkMode;//0表示基站，1表示移动站
    private E40RTKModeInfo rtkModeInfo;


    public static TcpE40AdvancedSettingFragment newInstance() {
        TcpE40AdvancedSettingFragment fragment = new TcpE40AdvancedSettingFragment();
        return fragment;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.tcp_e40_advanced_setting_fragment;
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
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_GET_RTK);
        sendCommand(command);
    }

    /**
     * 设置RTK模式
     */
    private void setRTKMode() {
        E40RTKModeEntity entity = new E40RTKModeEntity();
        entity.setMode(rtkMode);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_SET_RTK, entity);
        sendCommand(command);
    }

    @OnClick({R.id.dataCenterConfigLayout, R.id.rebootLayout, R.id.resetLayout, R.id.rtkModeLayout, R.id.corsServiceLayout, R.id.boardSolveLayout, R.id.sensorSettingLayout, R.id.wiredNetworkSettingLayout, R.id.fileDownloadLayout})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }
        if (!tcpViewModel.getConnectStatus()) {
            ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
            return;
        }

        int id = v.getId();
        if (id == R.id.dataCenterConfigLayout) {
            DataCenterHomeActivity.startActivity(mActivity, AppContants.DeviceType.E40, AppContants.CommunicationWay.TCP_CONNECT, AppContants.DataCenterConfigMethod.ADVANCED_CONFIG);

        } else if (id == R.id.rebootLayout) {//重启
            showWarnDialog("确定重启设备吗？", REBOOT);

        } else if (id == R.id.resetLayout) {//恢复出厂设置
            showWarnDialog("确定恢复出厂设置吗？", RESET);

        } else if (id == R.id.rtkModeLayout) {//RTK模式
            showRTKModeDialog();

        } else if (id == R.id.corsServiceLayout) {//CORS 服务设置
            E40CORSServiceActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT);

        } else if (id == R.id.boardSolveLayout) {//板卡解算设置
            E40BoardSolutionActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT);

        } else if (id == R.id.sensorSettingLayout) {//传感器设置

        } else if (id == R.id.wiredNetworkSettingLayout) {//有线网络设置
            E40EthernetActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT);

        } else if (id == R.id.fileDownloadLayout) {//下载文件

        }
    }

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
                                sendCommand(command);
                            }
                            break;

                            case RESET: {
                                String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.RESET);
                                sendCommand(command);
                            }
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
            case E40_MD_GET_RTK: {//获取RTK
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

            case E40_MD_SET_RTK: {
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

            case REBOOT: {//重启
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "发送重启指令失败!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show("发送指令成功,设备稍后将重启,请稍候重新连接");
                MCloudApp.getMainHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tcpViewModel.disconnect();
                    }
                }, 3000);
            }
            break;

            case RESET: {//恢复出厂设置
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "发送恢复出厂设置指令失败!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show("发送指令成功,设备稍后将重启,请稍候重新连接");
                MCloudApp.getMainHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tcpViewModel.disconnect();
                    }
                }, 3000);
            }
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
}
