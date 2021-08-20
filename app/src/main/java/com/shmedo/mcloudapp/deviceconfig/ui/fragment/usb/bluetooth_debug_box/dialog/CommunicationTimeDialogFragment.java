package com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.bluetooth_debug_box.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.at.ATCommand;
import com.shmedo.configlibrary.at.WHBLE102CommandType;
import com.shmedo.configlibrary.ble.utils.CRC16;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.core.util.DeviceInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.usb_serial.ATCommandItem;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/19 <br/>
 * 描述：     设置蓝牙通讯时间
 */
public class CommunicationTimeDialogFragment extends BaseDebugBoxDialogFragment {
    private int USB_SERIAL_COMMUNICATION_TIME = 0x10001;//

    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.tv_sleep_time)
    TextView mTvSleepTime;

    @BindView(R.id.tv_waiting_link_time)
    TextView mTvWaitingLinkTime;

    private String sleepTime;
    private String waitingLinkTime;


    public static CommunicationTimeDialogFragment newInstance() {
        return new CommunicationTimeDialogFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.communication_time_dialog_fragment;
    }

    @Override
    protected void setWindowStyle(int gravity) {
        super.setWindowStyle(Gravity.CENTER);
        Dialog mDialog = getDialog();
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = (int) (DeviceInfo.getScreenWidth() * 0.9f);
        wlp.height = (int) (DeviceInfo.getScreenHeight() * 0.7f);
        window.setAttributes(wlp);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvTitle.setText("待机时间配置");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isConnected()) {
            exitCommand();
        }
    }

    /**
     * 退出命令行模式
     */
    private void exitCommand() {
        atCommandItems.clear();

        String command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.ENTM.toString() + ATCommand.NEWLINE_CRLF;
        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.ENTM, command);
        atCommandItems.add(atCommandItem);

        sendCommandFromCmdList(USB_SERIAL_COMMUNICATION_TIME, WRITE_TIME_OUT_500_MILLIS);
    }

    /**
     * 查询时间
     * 1.发送 01 03 04 15 00 01 94 FE  查询蓝牙无通讯休眠时间
     * 2.发送 01 03 04 16 00 01 64 FE  查询蓝牙开机等待连接时间
     */
    private void queryTime() {
        atCommandItems.clear();

        String command = "01 03 04 15 00 01 94 FE";
        command = command.replace(" ", "").trim();
        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.QUERY_SLEEP_TIME, command);
        atCommandItems.add(atCommandItem);

        command = "01 03 04 16 00 01 64 FE";
        command = command.replace(" ", "").trim();
        atCommandItem = new ATCommandItem(WHBLE102CommandType.QUERY_WAITING_LINK_TIME, command);
        atCommandItems.add(atCommandItem);

        sendHexCommandFromCmdList(USB_SERIAL_COMMUNICATION_TIME, WRITE_TIME_OUT_1000_MILLIS);
    }

    /**
     * 组装蓝牙无通讯休眠时间指令
     * 前缀通用指令 01 10 08 26 00 01 02
     */
    private void assembleSleepTimeCmd(String value) {
        value = value.replace("s", "").trim();
        if (TextUtils.isEmpty(value) || !ValidateUtil.isInteger(value)) {
            return;
        }
        StringBuffer sbCmd = new StringBuffer();
        sbCmd.append("01 10 08 26 00 01 02");
        try {
            String hexValue = Integer.toHexString(Integer.parseInt(value)).toUpperCase();
            if (hexValue.length() != 4) {
                StringBuffer sb = new StringBuffer("0000");
                hexValue = sb.replace(4 - hexValue.length(), 4, hexValue).toString();
            }
            sbCmd.append(hexValue);
            sbCmd.append(CRC16.getCRC(sbCmd.toString()));

            ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.SET_SLEEP_TIME, sbCmd.toString().replace(" ", "").trim());
            atCommandItems.add(atCommandItem);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 组装蓝牙开机等待连接时间
     * 前缀通用指令 01 10 08 27 00 01 02
     */
    private void assembleWaitingLinkTimeCmd(String value) {
        value = value.replace("s", "").trim();
        if (TextUtils.isEmpty(value) || !ValidateUtil.isInteger(value)) {
            return;
        }
        StringBuffer sbCmd = new StringBuffer();
        sbCmd.append("01 10 08 27 00 01 02");
        try {
            String hexValue = Integer.toHexString(Integer.parseInt(value)).toUpperCase();
            if (hexValue.length() != 4) {
                StringBuffer sb = new StringBuffer("0000");
                hexValue = sb.replace(4 - hexValue.length(), 4, hexValue).toString();
            }
            sbCmd.append(hexValue);
            sbCmd.append(CRC16.getCRC(sbCmd.toString()));

            ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.SET_WAITING_LINK_TIME, sbCmd.toString().replace(" ", "").trim());
            atCommandItems.add(atCommandItem);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @OnClick({R.id.iv_close, R.id.ll_sleep_time, R.id.ll_waiting_link_time, R.id.btn_query_data, R.id.btn_save})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.iv_close) {
            dismiss();

        } else if (id == R.id.ll_sleep_time) {
            showSleepTimeDialog();

        } else if (id == R.id.ll_waiting_link_time) {
            showWaitingLinkTimeDialog();

        } else if (id == R.id.btn_query_data) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.usb_config_disconnect_warn));
                return;
            }
            stopProgressAll();
            queryTime();
        } else if (id == R.id.btn_save) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.usb_config_disconnect_warn));
                return;
            }
            stopProgressAll();
            sendHexCommandFromCmdList(USB_SERIAL_COMMUNICATION_TIME, WRITE_TIME_OUT_1000_MILLIS);
        }
    }

    /**
     * 选择休眠时间
     */
    private void showSleepTimeDialog() {
        final String[] times = new String[]{"100s", "200s"};
        int pos = Arrays.asList(times).indexOf(String.valueOf(sleepTime));
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title("选择休眠时间")
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .items(times)
                .itemsCallbackSingleChoice(pos, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        sleepTime = times[which];
                        assembleSleepTimeCmd(sleepTime);
                        return true;
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    /**
     * 选择 等待连接时间
     */
    private void showWaitingLinkTimeDialog() {
        final String[] times = new String[]{"5s", "10s", "30s", "60s", "120s"};
        int pos = Arrays.asList(times).indexOf(String.valueOf(waitingLinkTime));
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title("选择等待连接时间")
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .items(times)
                .itemsCallbackSingleChoice(pos, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        waitingLinkTime = times[which];
                        assembleWaitingLinkTimeCmd(waitingLinkTime);
                        return true;
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    @Override
    protected void parseResponseMessage(String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(String cmdStr) {
        resultBuilder.append(cmdStr);
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        String cmdStr = resultBuilder.toString();
        Timber.e("接收串口数据: %s", cmdStr);

        resultBuilder.setLength(0);
        if (atCommandItems.size() == 0)
            return;

        ATCommandItem commandItem = atCommandItems.getFirst();
        atCommandItems.removeFirst();//移除已经发送完的指令
        if (msg.what == USB_SERIAL_COMMUNICATION_TIME) {
            switch (commandItem.getCommandType()) {
                case ENTM: {//退出命令模式
                    if ((cmdStr.contains("ENTM:OK") && cmdStr.contains(ATCommand.OK_FLAG)) || TextUtils.isEmpty(cmdStr)) {
                        queryTime();
                    }
                }
                break;

                case QUERY_SLEEP_TIME: {//
                    //CRC检验通过
                    if (cmdStr.length() > 4 && checkCRCData(cmdStr)) {
                        String hexData = cmdStr.replace(" ", "").trim();
                        hexData = hexData.substring(6, hexData.length() - 4);

                        int result = Integer.valueOf(hexData, 16);
                        sleepTime = result + "s";
                        mTvSleepTime.setText(sleepTime);
                    }
                    sendHexCommandFromCmdList(USB_SERIAL_COMMUNICATION_TIME, WRITE_TIME_OUT_1000_MILLIS);
                }
                break;

                case QUERY_WAITING_LINK_TIME: {//
                    //CRC检验通过
                    if (cmdStr.length() > 4 && checkCRCData(cmdStr)) {
                        String hexData = cmdStr.replace(" ", "").trim();
                        hexData = hexData.substring(6, hexData.length() - 4);

                        int result = Integer.valueOf(hexData, 16);
                        waitingLinkTime = result + "s";
                        mTvWaitingLinkTime.setText(waitingLinkTime);
                    }
                }
                break;

                case SET_SLEEP_TIME: {//
                    String hexData = cmdStr.replace(" ", "").trim();
                    if (hexData.equals("011008260001E262")) {
                        sendHexCommandFromCmdList(USB_SERIAL_COMMUNICATION_TIME, WRITE_TIME_OUT_1000_MILLIS);
                    }
                }
                break;

                case SET_WAITING_LINK_TIME: {//
                    String hexData = cmdStr.replace(" ", "").trim();
                    if (hexData.equals("011008270001B3A2")) {
                        ToastUtils.show("保存成功");
                    }
                }
                break;
            }
        }
    }

}
