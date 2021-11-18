package com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.Inclinometer_debug_box;

import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.SPStaticUtils;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.at.ATCommand;
import com.shmedo.configlibrary.at.WHBLE102CommandType;
import com.shmedo.configlibrary.ble.utils.CRC8Utils;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.model.usb_serial.ATCommandItem;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.BaseUSBSerialCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class CollectionConfigurationFragment extends BaseUSBSerialCommunicateFragment {

    @BindView(R.id.et_execution_interval)
    ClearEditText mEtExecutionInterval;// 执行间隔

    @BindView(R.id.et_dormancy_time)
    ClearEditText mEtDormancyTime;//休眠时间

    @BindView(R.id.et_solution_time)
    ClearEditText mEtSolutionTime;//解算时间

    @BindView(R.id.et_collection_interval)
    ClearEditText mEtCollectionInterval;//采集间隔

    @BindView(R.id.et_sensor_type)
    ClearEditText mEtSensorType;//传感器类型

    @BindView(R.id.et_sensor_addr)
    ClearEditText mEtSensorAddr;//传感器地址

    @BindView(R.id.et_collection_points)
    ClearEditText mEtCollectionPoints;//采集点数

    @BindView(R.id.et_measuring_spacing)
    ClearEditText mEtMeasuringSpacing;//测量间距

    @BindView(R.id.et_collector_standby_interval)
    ClearEditText mEtCollectorStandbyInterval;//采集器待机间隔

    @BindView(R.id.et_motion_acceleration)
    ClearEditText mEtMotionAcceleration;//运动加速度

    @BindView(R.id.tv_collector_debug_mode_switch)
    TextView mTvCollectorDebugModeSwitch;//采集器调试模式开关

    @BindView(R.id.btn_save)
    Button mBtnSave;

    private String executionInterval;
    private String dormancyTime;
    private String solutionTime;
    private String collectionInterval;
    private String sensorType;
    private String sensorAddr;
    private String collectionPoints;
    private String measuringSpacing;
    private String collectorStandbyInterval;
    private String motionAcceleration;

    private int debugMode = 0;

    public static CollectionConfigurationFragment newInstance() {
        return new CollectionConfigurationFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.collection_configuration_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvCollectorDebugModeSwitch.setText("关闭");
    }


    @Override
    public void onResume() {
        super.onResume();
        if (isConnected()) {
            exitCommand();
        }
    }

    @Override
    protected void initView() {
        mEtExecutionInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtDormancyTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtSolutionTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtCollectionInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtSensorType.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtSensorAddr.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtCollectionPoints.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtMeasuringSpacing.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtCollectorStandbyInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtMotionAcceleration.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
    }

    /**
     * 退出命令行模式
     */
    private void exitCommand() {
        atCommandItems.clear();

        String command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.ENTM.toString() + ATCommand.NEWLINE_CRLF;
        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.ENTM, command);
        atCommandItems.add(atCommandItem);

        sendCommandFromCmdList(AppContants.UsbSerialMsgWhat.USB_SERIAL_COLLECTION_CONFIGURATION.getCode(), WRITE_TIME_OUT_500_MILLIS);
    }

    @OnClick({R.id.ll_collector_debug_mode_switch, R.id.btn_save})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }
        int id = v.getId();
        if (id == R.id.ll_collector_debug_mode_switch) {
            showDebugModeDialog();

        } else if (id == R.id.btn_save) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.usb_config_disconnect_warn));
                return;
            }
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(v);
            if (!checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            mBtnSave.setEnabled(false);
            processSave();
        }
    }

    /**
     * 选择调试模式
     */
    private void showDebugModeDialog() {
        final String[] modes = new String[]{"关闭", "开启"};
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title("选择状态")
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .items(modes)
                .itemsCallbackSingleChoice(debugMode, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        debugMode = which;
                        mTvCollectorDebugModeSwitch.setText(modes[which]);
                        return true;
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    private boolean checkValueIsValid() {
        executionInterval = mEtExecutionInterval.getText().toString().trim();
        dormancyTime = mEtDormancyTime.getText().toString().trim();
        solutionTime = mEtSolutionTime.getText().toString().trim();
        collectionInterval = mEtCollectionInterval.getText().toString().trim();
        sensorType = mEtSensorType.getText().toString().trim();
        sensorAddr = mEtSensorAddr.getText().toString().trim();
        collectionPoints = mEtCollectionPoints.getText().toString().trim();
        measuringSpacing = mEtMeasuringSpacing.getText().toString().trim();
        collectorStandbyInterval = mEtCollectorStandbyInterval.getText().toString().trim();
        motionAcceleration = mEtMotionAcceleration.getText().toString().trim();

        if (TextUtils.isEmpty(executionInterval)) {
            ToastUtils.show("请输入执行间隔!");
            mEtExecutionInterval.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(executionInterval);
//            if (value < 0 || value > 255) {
//                ToastUtils.show("请输入正确的执行间隔!");
//                mEtCollectorAddress.requestFocus();
//                return false;
//            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的执行间隔!");
            mEtExecutionInterval.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(dormancyTime)) {
            ToastUtils.show("请输入休眠时间!");
            mEtDormancyTime.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(dormancyTime);
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的休眠时间!");
            mEtDormancyTime.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(solutionTime)) {
            ToastUtils.show("请输入解算时间!");
            mEtSolutionTime.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(solutionTime);

        } catch (Exception ex) {
            ToastUtils.show("请输入正确的解算时间!");
            mEtSolutionTime.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(collectionInterval)) {
            ToastUtils.show("请输入采集间隔!");
            mEtCollectionInterval.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(collectionInterval);

        } catch (Exception ex) {
            ToastUtils.show("请输入正确的采集间隔!");
            mEtCollectionInterval.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(sensorType)) {
            ToastUtils.show("请输入传感器类型!");
            mEtSensorType.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(sensorType);

        } catch (Exception ex) {
            ToastUtils.show("请输入正确的传感器类型!");
            mEtSensorType.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(sensorAddr)) {
            ToastUtils.show("请输入传感器地址!");
            mEtSensorAddr.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(sensorAddr);

        } catch (Exception ex) {
            ToastUtils.show("请输入正确的传感器地址!");
            mEtSensorAddr.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(collectionPoints)) {
            ToastUtils.show("请输入采集点数!");
            mEtCollectionPoints.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(collectionPoints);

        } catch (Exception ex) {
            ToastUtils.show("请输入正确的采集点数!");
            mEtCollectionPoints.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(measuringSpacing)) {
            ToastUtils.show("请输入测量间距!");
            mEtMeasuringSpacing.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(measuringSpacing);

        } catch (Exception ex) {
            ToastUtils.show("请输入正确的测量间距!");
            mEtMeasuringSpacing.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(collectorStandbyInterval)) {
            ToastUtils.show("请输入采集器待机间隔!");
            mEtCollectorStandbyInterval.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(collectorStandbyInterval);

        } catch (Exception ex) {
            ToastUtils.show("请输入正确的采集器待机间隔!");
            mEtCollectorStandbyInterval.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(motionAcceleration)) {
            ToastUtils.show("请输入运动加速度!");
            mEtMotionAcceleration.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(motionAcceleration);
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的运动加速度!");
            mEtMotionAcceleration.requestFocus();
            return false;
        }

        return true;
    }

    private void processSave() {
        StringBuilder sb = new StringBuilder();
        sb.append("##CFG:");
        sb.append(executionInterval + ",");
        sb.append(dormancyTime + ",");
        sb.append(solutionTime + ",");
        sb.append(collectionInterval + ",");
        sb.append(sensorType + ",");
        sb.append(sensorAddr + ",");
        sb.append(collectionPoints + ",");
        sb.append(measuringSpacing + ",");
        sb.append(collectorStandbyInterval + ",");
        sb.append(motionAcceleration);

        int crcSum = CRC8Utils.CRC8_MAXIM(sb.toString().getBytes(), 0, sb.length());
        String result = Integer.toHexString(crcSum).toUpperCase();

        sb.append("*");
        sb.append(result);
        sb.append("\r\n");
        String command = sb.toString();

        atCommandItems.clear();
        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.SET_COLLECTION_CONFIGURATION, command);
        atCommandItems.add(atCommandItem);
        sendCommandFromCmdList(AppContants.UsbSerialMsgWhat.USB_SERIAL_COLLECTION_CONFIGURATION.getCode(), WRITE_TIME_OUT_1000_MILLIS);
    }

    @Override
    protected void parseResponseMessage(byte[] data) {
        if (!isActive) {
            return;
        }
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
        if (msg.what == AppContants.UsbSerialMsgWhat.USB_SERIAL_COLLECTION_CONFIGURATION.getCode()) {
            switch (commandItem.getCommandType()) {
                case ENTM: {//退出命令模式
                    String cmdStr = resultByteBuf.toString();
                    resultByteBuf.reset();
                    Timber.e("接收串口数据: %s", cmdStr);
                    if ((cmdStr.contains("ENTM:OK") && cmdStr.contains(ATCommand.OK_FLAG)) || TextUtils.isEmpty(cmdStr)) {
//                        queryWorkMode();
                    } else if (cmdStr.toUpperCase().contains("ERR")) {
                        cmdStr = filterControlCharacter(commandItem.getCommand());
                        Timber.e("%s  出错", cmdStr);
                        ToastUtils.show(cmdStr + "  出错");
                    }
                }
                break;

                case SET_COLLECTION_CONFIGURATION: {
                    mBtnSave.setEnabled(true);
                    String cmdStr = resultByteBuf.toString();
                    resultByteBuf.reset();
                    Timber.e("接收串口数据: %s", cmdStr);
                    if (cmdStr.contains("$$CFG:OK")) {
                        ToastUtils.show("保存成功");
                        SPStaticUtils.put(AppContants.Extras.INCLINOMETER_MEASURINGSPACING, measuringSpacing);
                    } else {
                        ToastUtils.show("保存失败");
                    }
                }
                break;
            }
        }
    }
}