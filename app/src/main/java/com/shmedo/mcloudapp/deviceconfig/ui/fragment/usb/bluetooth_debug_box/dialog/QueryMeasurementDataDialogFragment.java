package com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.bluetooth_debug_box.dialog;

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
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.util.DeviceInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.usb_serial.ATCommandItem;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/19 <br/>
 * 描述：    查询测斜仪测量数据
 */
public class QueryMeasurementDataDialogFragment extends BaseDebugBoxDialogFragment {
    private int USB_SERIAL_DATA_QUERY = 0x10001;//

    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.tv_measurement_data)
    TextView mTvMeasurementData;

    @BindView(R.id.btn_query_data)
    Button mBtnQuery;

    @BindView(R.id.btn_continuous_collection)
    Button mBtnContinuousCollect;

    private boolean isContinuousCollection = false;

    public static QueryMeasurementDataDialogFragment newInstance() {
        return new QueryMeasurementDataDialogFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.query_measurement_data_dialog_fragment;
    }

    @Override
    protected void setWindowStyle(int gravity) {
        super.setWindowStyle(Gravity.CENTER);
        Dialog mDialog = getDialog();
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = (int) (DeviceInfo.getScreenWidth() * 0.9f);
        wlp.height = (int) (DeviceInfo.getScreenHeight() * 0.6f);
        window.setAttributes(wlp);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvTitle.setText("测量数据");
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

        sendCommandFromCmdList(USB_SERIAL_DATA_QUERY, WRITE_TIME_OUT_500_MILLIS);
    }

    /**
     * 查询测量数据
     * 发送 01 03 00 64 00 03 44 14
     */
    private void queryData() {
        atCommandItems.clear();

        String command = "01 03 00 64 00 03 44 14";
        command = command.replace(" ", "").trim();
        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.QUERY_MEASUREMENT_DATA, command);
        atCommandItems.add(atCommandItem);

        sendHexCommandFromCmdList(USB_SERIAL_DATA_QUERY, WRITE_TIME_OUT_1000_MILLIS);
    }

    @OnClick({R.id.iv_close, R.id.btn_query_data, R.id.btn_continuous_collection})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.iv_close) {
            dismiss();

        } else if (id == R.id.btn_query_data) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.usb_config_disconnect_warn));
                return;
            }
            mBtnQuery.setEnabled(false);
            mBtnContinuousCollect.setEnabled(false);
            isContinuousCollection = false;
            stopProgressAll();
            queryData();
        } else if (id == R.id.btn_continuous_collection) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.usb_config_disconnect_warn));
                return;
            }
            mBtnQuery.setEnabled(false);
            mBtnContinuousCollect.setEnabled(false);
            isContinuousCollection = true;
            stopProgressAll();
            queryData();
        }
    }

    @Override
    protected void parseResponseMessage(byte[] data) {
        try {
            resultByteBuf.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        if (atCommandItems.size() == 0)
            return;

        ATCommandItem commandItem = atCommandItems.getFirst();
        atCommandItems.removeFirst();//移除已经发送完的指令
        if (msg.what == USB_SERIAL_DATA_QUERY) {
            switch (commandItem.getCommandType()) {
                case ENTM: {//退出命令模式
                    String cmdStr = resultByteBuf.toString();
                    resultByteBuf.reset();
                    Timber.e("接收串口数据: %s", cmdStr);
                    if ((cmdStr.contains("ENTM:OK") && cmdStr.contains(ATCommand.OK_FLAG)) || TextUtils.isEmpty(cmdStr)) {
                        queryData();
                    }else if (cmdStr.toUpperCase().contains("ERR")) {
                        cmdStr = filterControlCharacter(commandItem.getCommand());
                        Timber.e("%s  出错", cmdStr);
                        ToastUtils.show(cmdStr + "  出错");
                    }
                }
                break;

                case QUERY_MEASUREMENT_DATA: {//
                    mBtnQuery.setEnabled(true);
                    mBtnContinuousCollect.setEnabled(true);
                    String hexData = StringUtil.bytesToHexString(resultByteBuf.toByteArray());
                    resultByteBuf.reset();
                    hexData = hexData.replace(" ", "").toUpperCase().trim();
                    Timber.e("接收16进制串口数据: %s", hexData);

//                    cmdStr = filterControlCharacter(cmdStr);
//                    mTvMeasurementData.setText(cmdStr);
//                    if(isContinuousCollection) {
//                        queryData();
//                    }
//
//                    //CRC检验通过
//                    if (cmdStr.length() > 4 && checkCRCData(cmdStr)) {
//                        String hexData = cmdStr.replace(" ", "").trim();
//                        hexData = hexData.substring(6, hexData.length() - 4);
//
//                        int result = Integer.valueOf(hexData, 16);
////                        String value = df.format((double) result / 1000 ) + "V";
////                        mTvVoltage.setText(value);
//                    }
                }
                break;
            }
        }
    }
}
