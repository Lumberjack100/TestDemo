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
import com.shmedo.core.util.SharedUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.usb_serial.ATCommandItem;
import com.shmedo.mcloudapp.util.TextUtil;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigInteger;
import java.text.DecimalFormat;

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
    private int USB_SERIAL_OPEN_COMMUNICATION = 0x10002;//

    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.tv_modulus)
    TextView mTvModulus;

    @BindView(R.id.tv_coefficientA)
    TextView mTvCoefficientA;

    @BindView(R.id.tv_coefficientB)
    TextView mTvCoefficientB;

    @BindView(R.id.tv_coefficientC)
    TextView mTvCoefficientC;

    @BindView(R.id.tv_coefficientD)
    TextView mTvCoefficientD;

    @BindView(R.id.tv_measurement_data)
    TextView mTvMeasurementData;

    @BindView(R.id.btn_query_data)
    Button mBtnQuery;

    @BindView(R.id.btn_continuous_collection)
    Button mBtnContinuousCollect;

    private boolean isContinuousCollection = false;

    private int measuringSpacing = 500;
    private float A, B, C, D;

    DecimalFormat df = new DecimalFormat("0.000000");//格式化小数


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
        if (SharedUtil.contains(AppContants.Extras.INCLINOMETER_MEASURINGSPACING)) {
            measuringSpacing = SharedUtil.read(AppContants.Extras.INCLINOMETER_MEASURINGSPACING, 500);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isConnected()) {
            mBtnQuery.setEnabled(false);
            mBtnContinuousCollect.setEnabled(false);
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

        sendHexCommandFromCmdList(USB_SERIAL_OPEN_COMMUNICATION, WRITE_TIME_OUT_500_MILLIS);
    }

    /**
     * 查询计算测斜仪测量数值的方程式系数
     */
    private void queryEquationCoefficient() {
        atCommandItems.clear();

        String command = "01 03 00 69 00 0D 54 13";
        command = command.replace(" ", "").trim();
        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.QUERY_EQUATION_COEFFICIENT, command);
        atCommandItems.add(atCommandItem);

        sendHexCommandFromCmdList(USB_SERIAL_DATA_QUERY, WRITE_TIME_OUT_500_MILLIS);
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

        sendHexCommandFromCmdList(USB_SERIAL_DATA_QUERY, WRITE_TIME_OUT_500_MILLIS);
    }

    @OnClick({R.id.iv_close, R.id.btn_query_data, R.id.btn_continuous_collection})
    public void onClick(View view) {
//        if (isDoubleClick(view)) {
//            return;
//        }
        int id = view.getId();
        if (id == R.id.iv_close) {
            dismiss();

        } else if (id == R.id.btn_query_data) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.usb_config_disconnect_warn));
                return;
            }
            stopProgressAll();
            mBtnQuery.setEnabled(false);
            mBtnContinuousCollect.setEnabled(false);
            isContinuousCollection = false;
            queryData();
        } else if (id == R.id.btn_continuous_collection) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.usb_config_disconnect_warn));
                return;
            }
            stopProgressAll();
            mBtnQuery.setEnabled(false);
            mBtnContinuousCollect.setEnabled(false);
            isContinuousCollection = true;
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
                    if (cmdStr.toUpperCase().contains("ERR")) {
                        cmdStr = filterControlCharacter(commandItem.getCommand());
                        Timber.e("%s  出错", cmdStr);
                        ToastUtils.show(cmdStr + "  出错");
                    } else {
                        openCommunication();
                    }
                }
                break;

                case QUERY_EQUATION_COEFFICIENT: {//查询系数
                    String hexData = TextUtil.toHexString(resultByteBuf.toByteArray());
                    resultByteBuf.reset();
                    if (TextUtils.isEmpty(hexData)) {
                        return;
                    }
                    Timber.e("接收16进制串口数据(系数): %s", hexData);
                    hexData = hexData.replace(" ", "").toUpperCase().trim();
                    if (hexData.length() > 54) {
                        A = Float.intBitsToFloat(new BigInteger(hexData.substring(22, 30), 16).intValue());
                        B = Float.intBitsToFloat(new BigInteger(hexData.substring(30, 38), 16).intValue());
                        C = Float.intBitsToFloat(new BigInteger(hexData.substring(38, 46), 16).intValue());
                        D = Float.intBitsToFloat(new BigInteger(hexData.substring(46, 54), 16).intValue());

                        Timber.e("系数 A,B,C,D: %s,%s,%s,%s", A, B, C, D);
                        mTvCoefficientA.setText(String.valueOf(A));
                        mTvCoefficientB.setText(String.valueOf(B));
                        mTvCoefficientC.setText(String.valueOf(C));
                        mTvCoefficientD.setText(String.valueOf(D));

                        queryData();
                    }
                }
                break;

                case QUERY_MEASUREMENT_DATA: {//查询模数
                    mBtnQuery.setEnabled(true);
                    mBtnContinuousCollect.setEnabled(true);
                    String hexData = TextUtil.toHexString(resultByteBuf.toByteArray());
                    resultByteBuf.reset();
                    if (TextUtils.isEmpty(hexData)) {
                        return;
                    }
                    Timber.e("接收16进制串口数据(模数): %s", hexData);
                    hexData = hexData.replace(" ", "").toUpperCase().trim();
                    if (hexData.length() >= 22) {
                        String modulusHex = hexData.substring(10, 14);
                        String temperature = hexData.substring(14, 18);
                        //int modulus = new BigInteger(modulusHex, 16).intValue();
                        short modulus = (short) Integer.parseInt(modulusHex,16);
                        Timber.e("模数 F: %s", modulus);
                        mTvModulus.setText(String.valueOf(modulus));
                        if (A != 0 && B != 0 && C != 0 && D != 0) {
                            try {
                                //得到角度
                                double angle = A + B * modulus + C * modulus * modulus + D * modulus * modulus * modulus;
                                Timber.e("角度: %s", angle);

                                //角度转为弧度
                                double radian = Math.toRadians(angle);
                                Timber.e("弧度: %s", radian);

                                double result = measuringSpacing * Math.sin(radian);
                                String value = df.format(result) + "mm";
                                mTvMeasurementData.setText(value);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        if (isContinuousCollection) {
                            queryData();
                        }
                    }
                }
                break;
            }
        } else if (msg.what == USB_SERIAL_OPEN_COMMUNICATION) {
            switch (commandItem.getCommandType()) {
                case ALLOW_CONNECT: {//允许连接指令
                    resultByteBuf.reset();
                    sendHexCommandFromCmdList(USB_SERIAL_OPEN_COMMUNICATION, WRITE_TIME_OUT_500_MILLIS);
                }
                break;

                case ALLOW_BLUETOOTH_COMMUNICATION: {//允许蓝牙通讯
                    resultByteBuf.reset();
                    queryEquationCoefficient();
                }
                break;
            }
        }
    }
}
