package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.cmd.parser.ParseManager;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.ServerNumber;
import com.shmedo.configlibrary.ble.model.BaseConfigInfo;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.event.CmdResponseMessage;
import com.shmedo.core.event.MessageEvent;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterServerConfigActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 蓝牙配置数据中心
 */
public class BleDataCenterFragment extends BaseBleConnectFragment {
    @BindView(R.id.tv_communication_method)
    TextView mTvCommunicationMethod;

    @BindView(R.id.reportingIntervalET)
    EditText mEtReportingInterval;

    @BindView(R.id.bdCardNumberET)
    EditText mEtBdCardNumber;

    @BindView(R.id.ll_bd_card_number)
    View bdCardNumberLayout;

    private int pos = 1;
    private String dataCommunicationModeOld;
    private String dataCommunicationMode;
    private String reportingInterval;
    private String bdCardNumber;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_data_center;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        queryData();
    }

    private void queryData() {
        errMsg = "查询数据超时,请稍后尝试";
        startProgressRunnable("正在获取参数...", 25000);

        String baseInfoCommand = CommandManager.getInstance().getCommand(CommandType.BASE_CONFIG);
        sendCommonCommand(baseInfoCommand);
        Timber.d("查询基础配置信息指令===%s", baseInfoCommand);
    }


    @OnClick({R.id.communicationMethodLayout, R.id.centerOneLayout, R.id.centerTwoLayout, R.id.centerThreeLayout, R.id.btn_confirm})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.communicationMethodLayout:
                XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
                new XPopup.Builder(mActivity)
                        .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                        .asBottomList("", new String[]{"4G", "SMS", "BD", "BD+4G"},
                                null, pos, true,
                                new OnSelectListener() {
                                    @Override
                                    public void onSelect(int position, String text) {
                                        pos = position;
                                        mTvCommunicationMethod.setText(text);

                                        switch (text) {
                                            case "4G":
                                                dataCommunicationMode = "1";
                                                bdCardNumberLayout.setVisibility(View.GONE);
                                                break;

                                            case "SMS":
                                                dataCommunicationMode = "2";
                                                bdCardNumberLayout.setVisibility(View.GONE);
                                                break;

                                            case "BD":
                                                dataCommunicationMode = "3";
                                                bdCardNumberLayout.setVisibility(View.VISIBLE);
                                                break;

                                            case "BD+4G":
                                                dataCommunicationMode = "4";
                                                bdCardNumberLayout.setVisibility(View.VISIBLE);
                                                break;
                                        }
                                    }
                                }, 0, R.layout.custom_xpopup_adapter_text_match)
                        .show();
                break;

            case R.id.centerOneLayout:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                DataCenterServerConfigActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, ServerNumber.NUMBER_ONE);
                break;

            case R.id.centerTwoLayout:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                DataCenterServerConfigActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, ServerNumber.NUMBER_TWO);
                break;

            case R.id.centerThreeLayout:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                DataCenterServerConfigActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, ServerNumber.NUMBER_THREE);
                break;

            case R.id.btn_confirm:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                processSave();
                break;
            default:
                break;
        }
    }

    private void processSave() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent messageEvent) {
        super.onMessageEvent(messageEvent);

        if (messageEvent instanceof CmdResponseMessage) {
            setResultData((CmdResponseMessage) messageEvent);
        }
    }

    private void setResultData(CmdResponseMessage responseMessage) {
        String cmdStr = responseMessage.getResult();
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case BASE_CONFIG://获取基础配置信息
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    stopProgressRunnable();
                    Timber.e("查询基础配置信息指令出错!");
                    ToastUtils.show("查询基础配置信息指令出错!");
                    return;
                }
                CommandResult<BaseConfigInfo> bean = ParseManager.getInstance().parse(cmdStr);
                if (!bean.isSuccess()) {
                    stopProgressRunnable();
                    return;
                }
                BaseConfigInfo baseConfigInfo = bean.getResult();
                int communicateMode = baseConfigInfo.getDataCommunicateMode().toInt();
                pos = communicateMode - 1;
                switch (communicateMode) {
                    case 1:
                        mTvCommunicationMethod.setText("4G");
                        dataCommunicationModeOld = "1";
                        dataCommunicationMode = "1";
                        bdCardNumberLayout.setVisibility(View.GONE);
                        break;
                    case 2:
                        mTvCommunicationMethod.setText("SMS");
                        dataCommunicationModeOld = "2";
                        dataCommunicationMode = "2";
                        bdCardNumberLayout.setVisibility(View.GONE);
                        break;
                    case 3:
                        mTvCommunicationMethod.setText("BD");
                        dataCommunicationModeOld = "3";
                        dataCommunicationMode = "3";
                        bdCardNumberLayout.setVisibility(View.VISIBLE);
                        break;
                    case 4:
                        mTvCommunicationMethod.setText("BD+4G");
                        dataCommunicationModeOld = "4";
                        dataCommunicationMode = "4";
                        bdCardNumberLayout.setVisibility(View.VISIBLE);
                        break;
                }
                int reportInterval = baseConfigInfo.getDataReportInterval();
                reportingInterval = String.valueOf(reportInterval);
                bdCardNumber = baseConfigInfo.getTargetBGNum();
                mEtReportingInterval.setText(reportingInterval);
                mEtBdCardNumber.setText(bdCardNumber);

                stopProgressRunnable();
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        if (MCloudApp.isIsBluetoothDeviceConnected()) {
            if (TextUtils.isEmpty(dataCommunicationModeOld)) {
                mActivity.finish();
                return true;
            }

            if (TextUtils.isEmpty(reportingInterval)) {
                mActivity.finish();
                return true;
            }

            if (TextUtils.isEmpty(bdCardNumber)) {
                mActivity.finish();
                return true;
            }

            if (!dataCommunicationModeOld.equals(dataCommunicationMode) || !reportingInterval.equals(mEtReportingInterval.getText()) || !bdCardNumber.equals(mEtBdCardNumber.getText())) {
                warnNotYetSave();
            }
        } else {
            mActivity.finish();
        }

        return true;
    }

    private void warnNotYetSave() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(Objects.requireNonNull(getContext()))
                .title("温馨提示：")
                .content("您已经修改了参数，还未保存，是否确定离开页面？")
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .negativeColor(Color.parseColor("#807B7B"))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        mActivity.finish();
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }
}
