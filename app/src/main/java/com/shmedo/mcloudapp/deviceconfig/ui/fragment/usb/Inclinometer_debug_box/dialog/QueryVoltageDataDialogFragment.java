package com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.Inclinometer_debug_box.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.at.ATCommand;
import com.shmedo.configlibrary.at.WHBLE102CommandType;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.DeviceInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.usb_serial.ATCommandItem;
import com.shmedo.mcloudapp.util.TextUtil;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/19 <br/>
 * 描述：    查询测斜仪电池电压数据
 */
public class QueryVoltageDataDialogFragment extends BaseDebugBoxDialogFragment {

    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.tv_battery_voltage)
    TextView mTvVoltage;

    @BindView(R.id.btn_query_data)
    Button mBtnQuery;

    DecimalFormat df = new DecimalFormat("0.000");//格式化小数

    public static QueryVoltageDataDialogFragment newInstance() {
        return new QueryVoltageDataDialogFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.query_voltage_data_dialog_fragment;
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
        mTvTitle.setText("电池电压");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isConnected()) {
            mBtnQuery.setEnabled(false);
            enterCommand();
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

        sendCommandFromCmdList(AppContants.UsbSerialMsgWhat.USB_SERIAL_DATA_QUERY.getCode(), WRITE_TIME_OUT_500_MILLIS);
    }

    /**
     * 查询电压数据
     * 发送 01 03 04 08 00 01 04 F8
     */
    private void queryVoltage() {
        atCommandItems.clear();

        String command = "01 03 04 08 00 01 04 F8";
        command = command.replace(" ", "").trim();
        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.QUERY_BATTERY_VOLTAGE, command);
        atCommandItems.add(atCommandItem);

        sendHexCommandFromCmdList(AppContants.UsbSerialMsgWhat.USB_SERIAL_DATA_QUERY.getCode(), WRITE_TIME_OUT_500_MILLIS);
    }

    @OnClick({R.id.iv_close, R.id.btn_query_data})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.iv_close) {
            dismiss();

        } else if (id == R.id.btn_query_data) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.usb_config_disconnect_warn));
                return;
            }
            if (!isBluetoothConnected) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            stopProgressAll();
            mBtnQuery.setEnabled(false);
            queryVoltage();
        }
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        if (atCommandItems.size() == 0)
            return;
        super.customHandleMessage(msg);
        if (msg.what == AppContants.UsbSerialMsgWhat.USB_SERIAL_LINK_QUERY.getCode()) {
            if (commandItem.getCommandType() == WHBLE102CommandType.LINK) {
                if (isBluetoothConnected) {
                    exitCommand();
                } else {
                    mBtnQuery.setEnabled(false);
                    ToastUtils.show("蓝牙未连接");
                }
            }
        } else if (msg.what == AppContants.UsbSerialMsgWhat.USB_SERIAL_DATA_QUERY.getCode()) {
            commandItem = atCommandItems.getFirst();
            atCommandItems.removeFirst();//移除已经发送完的指令
            switch (commandItem.getCommandType()) {
                case ENTM: {//退出命令模式
                    String cmdStr = resultByteBuf.toString();
                    resultByteBuf.reset();
                    Timber.e("接收串口数据: %s", cmdStr);
                    if ((cmdStr.contains("ENTM:OK") && cmdStr.contains(ATCommand.OK_FLAG)) || TextUtils.isEmpty(cmdStr)) {
                        queryVoltage();
                    } else if (cmdStr.toUpperCase().contains("ERR")) {
                        cmdStr = filterControlCharacter(commandItem.getCommand());
                        Timber.e("%s  出错", cmdStr);
                        ToastUtils.show(cmdStr + "  出错");
                    }
                }
                break;

                case QUERY_BATTERY_VOLTAGE: {//
                    mBtnQuery.setEnabled(true);
                    String hexData = TextUtil.toHexString(resultByteBuf.toByteArray());
                    resultByteBuf.reset();
                    if (TextUtils.isEmpty(hexData)) {
                        return;
                    }
                    Timber.e("接收16进制串口数据(电压): %s", hexData);
                    hexData = hexData.replace(" ", "").toUpperCase().trim();

                    //CRC检验通过
                    if (hexData.length() > 4 && checkCRCData(hexData)) {
                        hexData = hexData.substring(6, hexData.length() - 4);

                        int result = Integer.valueOf(hexData, 16);
                        String value = df.format((double) result / 1000) + "V";
                        mTvVoltage.setText(value);
                    }
                }
                break;
            }
        }
    }
}
