package com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.bluetooth_debug_box.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.at.ATCommand;
import com.shmedo.configlibrary.at.WHBLE102CommandType;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.DeviceInfo;
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
    private int USB_SERIAL_LINK_QUERY = 0x10001;//

    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.et_mac_address)
    ClearEditText mEtMacAddr;

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
        wlp.width = (int) (DeviceInfo.getScreenWidth() * 0.9f);
        wlp.height = (int) (DeviceInfo.getScreenHeight() * 0.5f);
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

        String command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.CONNADD.toString() + ATCommand.QUERY_FLAG + ATCommand.NEWLINE_CRLF;
        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.CONNADD, command);
        atCommandItems.add(atCommandItem);

        sendCommandFromCmdList(AppContants.MsgWhat.USB_SERIAL_AT_CONNECT, WRITE_TIME_OUT_MILLIS);
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

        sendCommandFromCmdList(AppContants.MsgWhat.USB_SERIAL_AT_CONNECT, WRITE_TIME_OUT_MILLIS);
    }

    /**
     * 查询蓝牙测斜仪设备连接状态
     * 1.发送 AT+LINK?查询连接状态
     * 2.发送 AT+Z 控制模块重启
     * 3.发送 +++a 进入命令行模式
     */
    private void queryBluetoothLinkStatus() {
        atCommandItems.clear();

        String command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.Z.toString() + ATCommand.NEWLINE_CRLF;
        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.Z, command);
        atCommandItems.add(atCommandItem);

        atCommandItem = new ATCommandItem(WHBLE102CommandType.ENTER_COMMAND, WHBLE102CommandType.ENTER_COMMAND.toString());
        atCommandItems.add(atCommandItem);//进入命令模式

        command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.LINK.toString() + ATCommand.QUERY_FLAG + ATCommand.NEWLINE_CRLF;
        atCommandItem = new ATCommandItem(WHBLE102CommandType.LINK, command);
        atCommandItems.add(atCommandItem);

        sendCommandFromCmdList(USB_SERIAL_LINK_QUERY, WRITE_TIME_OUT_MILLIS);
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
                ToastUtils.show(getString(R.string.usb_config_disconnect_warn));
                return;
            }
            if (mEtMacAddr.getText().toString().trim().length() != 12) {
                ToastUtils.show("请输入有效的 MAC 地址！");
                return;
            }
            mProgressBar.setVisibility(View.VISIBLE);
            //与原来默认配置的 MAC 地址不同，需要先发送 at+connadd=mac 指令进行设置
            if (!defaultMac.equals(mEtMacAddr.getText().toString().trim())) {
                configDefaultMac();
            } else {
                //直接循环查询连接状态
                queryBluetoothLinkStatus();
            }
        }
    }


    public void updateSuccessStatus() {
        mProgressBar.setVisibility(View.GONE);
        ToastUtils.show("蓝牙测斜仪连接成功！");
    }

    public void updateFailureStatus(String content) {
        mProgressBar.setVisibility(View.GONE);
        ToastUtils.show(content);
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
        if (msg.what == AppContants.MsgWhat.USB_SERIAL_AT_CONNECT) {
            String cmdStr = resultBuilder.toString();
            Timber.e("接收串口数据: %s", cmdStr);

            resultBuilder.setLength(0);
            if (atCommandItems.size() == 0)
                return;
            ATCommandItem commandItem = atCommandItems.getFirst();
            atCommandItems.removeFirst();//移除已经发送完的指令
            switch (commandItem.getCommandType()) {
                case CONNADD: {
                    if (cmdStr.contains(WHBLE102CommandType.CONNADD.toString()) && cmdStr.contains(ATCommand.OK_FLAG)) {
                        cmdStr = filterControlCharacter(cmdStr);
                        cmdStr = cmdStr.replace(ATCommand.COMMAND_RESULT_HEADER + WHBLE102CommandType.CONNADD.toString() + ATCommand.DELIMITER_COLON, "");
                        defaultMac = cmdStr;
                        mEtMacAddr.setText(cmdStr);

                        atCommandItems.removeFirst();//移除已经发送完的指令
                        if (atCommandItems.size() > 0) {
                            mProgressBar.setVisibility(View.VISIBLE);
                            sendCommandFromCmdList(AppContants.MsgWhat.USB_SERIAL_AT_CONNECT, WRITE_TIME_OUT_MILLIS);
                        }
                    } else {
                        atCommandItems.removeFirst();//移除已经发送完的指令
                    }
                }
                break;

                case AUTOCONN: {
                    if (cmdStr.contains("AUTOCONN:ON") && cmdStr.contains(ATCommand.OK_FLAG)) {
                        atCommandItems.removeFirst();//移除已经发送完的指令
                        queryBluetoothLinkStatus();
                        sendCommandFromCmdList(USB_SERIAL_LINK_QUERY, WRITE_TIME_OUT_MILLIS);
                    }
                }
                break;
            }
        } else if (msg.what == USB_SERIAL_LINK_QUERY) {
            String cmdStr = resultBuilder.toString();
            Timber.e("接收串口数据: %s", cmdStr);

            resultBuilder.setLength(0);
            if (atCommandItems.size() == 0)
                return;
            ATCommandItem commandItem = atCommandItems.getFirst();
            switch (commandItem.getCommandType()) {
                case Z: {//重启
                    if (cmdStr.contains("RST:OK") && cmdStr.contains(ATCommand.OK_FLAG)) {
                        atCommandItems.removeFirst();//移除已经发送完的指令
                        sendCommandFromCmdList(USB_SERIAL_LINK_QUERY, WRITE_TIME_OUT_MILLIS);
                    }else{
                        updateFailureStatus("蓝牙测斜仪连接失败！");
                    }
                }
                break;

                case ENTER_COMMAND: {
                    cmdStr = cmdStr.replace(ATCommand.NEWLINE_CR, "").replace(ATCommand.NEWLINE_LF, "").trim();
                    if (cmdStr.contains("a+ok")) {
                        atCommandItems.removeFirst();//移除已经发送完的指令
                        sendCommandFromCmdList(USB_SERIAL_LINK_QUERY, WRITE_TIME_OUT_MILLIS);
                    }else{
                        updateFailureStatus("蓝牙测斜仪连接失败！");
                    }
                }
                break;

                case LINK: {
                    if (cmdStr.contains(WHBLE102CommandType.LINK.toString()) && cmdStr.contains(ATCommand.OK_FLAG)) {
                        atCommandItems.removeFirst();//移除已经发送完的指令

                        if (cmdStr.toUpperCase().contains("ONLINE")) {
                            queryCont = 0;
                            //连接成功
                            updateSuccessStatus();
                        } else {
                            //查询连接状态超过10次，判定超时
                            if (queryCont >= 3 || !isConnected()) {
                                stopProgress(USB_SERIAL_LINK_QUERY);
                                updateFailureStatus("蓝牙测斜仪连接超时！");
                                return;
                            }
                            queryCont++;
                            queryBluetoothLinkStatus();
                            sendCommandFromCmdList(USB_SERIAL_LINK_QUERY, 1000);
                        }
                    } else {
                        updateFailureStatus("蓝牙测斜仪连接失败！");
                    }
                }
                break;
            }
        }
    }

    private String filterControlCharacter(String str) {
        str = str.replace(ATCommand.OK_FLAG, "")
                .replace(ATCommand.NEWLINE_CR, "")
                .replace(ATCommand.NEWLINE_LF, "");
        return str;
    }
}
