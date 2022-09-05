package com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.Inclinometer_debug_box.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.StringUtils;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.at.ATCommand;
import com.shmedo.configlibrary.at.WHBLE102CommandType;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.model.usb_serial.ATCommandItem;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/17 <br/>
 * 描述：     TODO
 */
public class ConfigBluetoothMacDialogFragment extends BaseDebugBoxDialogFragment {

    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.et_mac_address)
    ClearEditText mEtMacAddr;

    @BindView(R.id.btn_link)
    Button mBtnLink;

    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    private String defaultMac;

    private int queryCont = 0;


    public static ConfigBluetoothMacDialogFragment newInstance() {
        return new ConfigBluetoothMacDialogFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.config_bluetooth_mac_dialog_fragment;
    }

    @Override
    protected void setWindowStyle(int gravity) {
        super.setWindowStyle(Gravity.CENTER);
        Dialog mDialog = getDialog();
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = (int) (ScreenUtils.getScreenWidth() * 0.9f);
        wlp.height = (int) (ScreenUtils.getScreenHeight() * 0.5f);
        window.setAttributes(wlp);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvTitle.setText("配置连接");
        setView();
    }

    private void setView() {
        mEtMacAddr.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isConnected()) {
            queryConfigBluetoothMac();
        }
    }

    /**
     * 查询配置的默认连接蓝牙 MAC 地址
     */
    private void queryConfigBluetoothMac() {
        atCommandItems.clear();

        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.ENTER_COMMAND, WHBLE102CommandType.ENTER_COMMAND.toString());
        atCommandItems.add(atCommandItem);//进入命令模式

        String command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.CONNADD.toString() + ATCommand.QUERY_FLAG + ATCommand.NEWLINE_CRLF;
        atCommandItem = new ATCommandItem(WHBLE102CommandType.CONNADD, command);
        atCommandItems.add(atCommandItem);

        sendCommandFromCmdList(AppContants.UsbSerialMsgWhat.USB_SERIAL_AT_CONNECT.getCode(), WRITE_TIME_OUT_500_MILLIS);
    }

    /**
     * 配置默认连接 MAC 地址
     * 1.发送 AT+CONNADD=mac地址 设置默认连接的mac地址
     * 2.发送 AT+AUTOCONN=on 使能自动重连
     */
    private void configDefaultMac() {
        atCommandItems.clear();

        String command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.CONNADD.toString() + "=" + mEtMacAddr.getText().toString() + ATCommand.NEWLINE_CRLF;
        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.CONNADD, command);
        atCommandItems.add(atCommandItem);

        command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.AUTOCONN.toString() + "=ON" + ATCommand.NEWLINE_CRLF;
        atCommandItem = new ATCommandItem(WHBLE102CommandType.AUTOCONN, command);
        atCommandItems.add(atCommandItem);

        sendCommandFromCmdList(AppContants.UsbSerialMsgWhat.USB_SERIAL_AT_CONNECT.getCode(), WRITE_TIME_OUT_500_MILLIS);
    }

    /**
     * 查询蓝牙测斜仪设备自动连接状态
     * 1.发送 AT+LINK?查询连接状态
     * 2.发送 AT+Z 控制模块重启
     * 3.发送 +++a 进入命令行模式
     */
    private void queryBluetoothLinkStatus() {
        if (queryCont >= 110) {
            updateFailureStatus("连接查询超时！");
            return;
        }
        queryCont++;
        atCommandItems.clear();
        Timber.e("第 %s 次查询", queryCont);

        String command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.Z.toString() + ATCommand.NEWLINE_CRLF;
        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.Z, command);
        atCommandItems.add(atCommandItem);

        atCommandItem = new ATCommandItem(WHBLE102CommandType.ENTER_COMMAND, WHBLE102CommandType.ENTER_COMMAND.toString());
        atCommandItems.add(atCommandItem);//进入命令模式

        command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.LINK.toString() + ATCommand.QUERY_FLAG + ATCommand.NEWLINE_CRLF;
        atCommandItem = new ATCommandItem(WHBLE102CommandType.LINK, command);
        atCommandItems.add(atCommandItem);

        //发送 AT+Z 指令后,延迟 1000 ms 发送下一条指令
        sendCommandFromCmdList(AppContants.UsbSerialMsgWhat.USB_SERIAL_LINK_QUERY.getCode(), 3000);
    }

    /**
     * 退出命令行模式
     */
    private void exitCommand() {
        atCommandItems.clear();

        String command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.ENTM.toString() + ATCommand.NEWLINE_CRLF;
        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.ENTM, command);
        atCommandItems.add(atCommandItem);

        sendCommandFromCmdList(AppContants.UsbSerialMsgWhat.USB_SERIAL_LINK_QUERY.getCode(), WRITE_TIME_OUT_500_MILLIS);
    }

    /**
     * 打开测斜仪通讯
     * 1.发送 01 10 07 DD 00 01 02 55 AA 7D 32 允许连接指令
     * 2.发送 01 10 08 17 00 01 02 5A 5A 96 2C 允许蓝牙通讯
     */
    private void openCommunication() {
        atCommandItems.clear();

        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.ALLOW_CONNECT, "011007DD00010255AA7D32");
        atCommandItems.add(atCommandItem);

        atCommandItem = new ATCommandItem(WHBLE102CommandType.ALLOW_BLUETOOTH_COMMUNICATION, "011008170001025A5A962C");
        atCommandItems.add(atCommandItem);

        sendHexCommandFromCmdList(AppContants.UsbSerialMsgWhat.USB_SERIAL_OPEN_COMMUNICATION.getCode(), WRITE_TIME_OUT_500_MILLIS);
    }

    @OnClick({R.id.iv_close, R.id.btn_link})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.iv_close) {
            dismiss();

        } else if (id == R.id.btn_link) {
            if (!isConnected()) {
                ToastUtils.show(StringUtils.getString(R.string.usb_config_disconnect_warn));
                return;
            }
            if (mEtMacAddr.getText().toString().trim().length() != 12) {
                ToastUtils.show("请输入有效的 MAC 地址！");
                return;
            }
            mBtnLink.setEnabled(false);
            mProgressBar.setVisibility(View.VISIBLE);
            //与原来默认配置的 MAC 地址不同，需要先发送 at+connadd=mac 指令进行设置
            if (TextUtils.isEmpty(defaultMac) ||
                    (!TextUtils.isEmpty(defaultMac) && !defaultMac.equals(mEtMacAddr.getText().toString().trim()))) {
                configDefaultMac();
            } else {
                //直接循环查询自动连接状态
                queryCont = 1;
                queryBluetoothLinkStatus();
            }
        }
    }

    public void updateSuccessStatus() {
        mBtnLink.setEnabled(true);
        mProgressBar.setVisibility(View.GONE);
        ToastUtils.show("蓝牙测斜仪连接成功！");
    }

    public void updateFailureStatus(String content) {
        mBtnLink.setEnabled(true);
        mProgressBar.setVisibility(View.GONE);
        Timber.e("updateFailureStatus: %s", content);
        ToastUtils.show(content);
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        if (atCommandItems.size() == 0)
            return;

        String cmdStr = resultByteBuf.toString();
        resultByteBuf.reset();
        Timber.e("接收串口数据: %s", cmdStr);
        commandItem = atCommandItems.getFirst();
        atCommandItems.removeFirst();//移除已经发送完的指令
        if (msg.what == AppContants.UsbSerialMsgWhat.USB_SERIAL_AT_CONNECT.getCode()) {
            switch (commandItem.getCommandType()) {
                case ENTER_COMMAND: {
                    if (cmdStr.toUpperCase().contains("ERR")) {
                        cmdStr = filterControlCharacter(commandItem.getCommand());
                        Timber.e("%s  出错", cmdStr);
                        ToastUtils.show(cmdStr + "  出错");
                    } else {
                        sendCommandFromCmdList(AppContants.UsbSerialMsgWhat.USB_SERIAL_AT_CONNECT.getCode(), WRITE_TIME_OUT_1000_MILLIS);
                    }
                }
                break;

                case CONNADD: {
                    cmdStr = filterControlCharacter(cmdStr);
                    if (cmdStr.toUpperCase().contains("ERR")) {
                        cmdStr = filterControlCharacter(commandItem.getCommand());
                        Timber.e("%s  出错", cmdStr);
                        ToastUtils.show(cmdStr + "  出错");
                    } else {
                        cmdStr = cmdStr.replace(ATCommand.COMMAND_RESULT_HEADER + WHBLE102CommandType.CONNADD.toString() + ATCommand.DELIMITER_COLON, "");
                        defaultMac = cmdStr;
                        mEtMacAddr.setText(cmdStr);

                        //说明还有 AT+AUTOCONN 指令,表示进行连接处理
                        if (atCommandItems.size() > 0) {
                            sendCommandFromCmdList(AppContants.UsbSerialMsgWhat.USB_SERIAL_AT_CONNECT.getCode(), WRITE_TIME_OUT_500_MILLIS);
                        }
                    }
                }
                break;

                case AUTOCONN: {
                    if (cmdStr.toUpperCase().contains("ERR")) {
                        cmdStr = filterControlCharacter(commandItem.getCommand());
                        updateFailureStatus(cmdStr + "  出错");

                    } else {
                        //循环查询连接状态
                        queryCont = 1;
                        queryBluetoothLinkStatus();
                    }
                }
                break;
            }
        } else if (msg.what == AppContants.UsbSerialMsgWhat.USB_SERIAL_LINK_QUERY.getCode()) {
            switch (commandItem.getCommandType()) {
                case Z:

                case ENTER_COMMAND: {//重启
                    if (cmdStr.toUpperCase().contains("ERR")) {
                        cmdStr = filterControlCharacter(commandItem.getCommand());
                        updateFailureStatus(cmdStr + "  出错");

                    } else {
                        sendCommandFromCmdList(AppContants.UsbSerialMsgWhat.USB_SERIAL_LINK_QUERY.getCode(), WRITE_TIME_OUT_1000_MILLIS);
                    }
                }
                break;

                case LINK: {
                    if (cmdStr.toUpperCase().contains("ERR")) {
                        cmdStr = filterControlCharacter(commandItem.getCommand());
                        updateFailureStatus(cmdStr + "  出错");

                    } else {
                        if (cmdStr.toUpperCase().contains("ONLINE")) {
                            //连接成功
                            updateSuccessStatus();
                            //退出命令模式
                            exitCommand();
                        } else {
                            //循环查询连接状态
                            queryBluetoothLinkStatus();
                        }
                    }
                }
                break;

                case ENTM: {//退出命令模式
                    if (cmdStr.toUpperCase().contains("ERR")) {
                        cmdStr = filterControlCharacter(commandItem.getCommand());
                        updateFailureStatus(cmdStr + "  出错");

                    } else {
                        //打开通讯
                        openCommunication();
                    }
                }
                break;
            }
        } else if (msg.what == AppContants.UsbSerialMsgWhat.USB_SERIAL_OPEN_COMMUNICATION.getCode()) {
            switch (commandItem.getCommandType()) {
                case ALLOW_CONNECT: {//允许连接指令
                    sendHexCommandFromCmdList(AppContants.UsbSerialMsgWhat.USB_SERIAL_OPEN_COMMUNICATION.getCode(), WRITE_TIME_OUT_500_MILLIS);
                }
                break;

                case ALLOW_BLUETOOTH_COMMUNICATION: {//允许蓝牙通讯
                }
                break;
            }
        }
    }
}
