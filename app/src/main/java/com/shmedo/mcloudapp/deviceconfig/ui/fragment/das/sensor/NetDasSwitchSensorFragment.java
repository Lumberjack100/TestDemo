package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.das.DasIOSensorEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.das.DasIOSensorInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/19 <br/>
 * 描述：     开关量传感器配置页面
 */
public class NetDasSwitchSensorFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.radio_close_switch_sensor)
    RadioButton rbCloseSwitchSensor;//关闭开关量传感器

    @BindView(R.id.radio_rain_gauge)
    RadioButton rbRainGauge;

    @BindView(R.id.rainPrecisionEt)
    EditText mEtRainPrecision;//雨量计精度

    @BindView(R.id.radio_break_alarm)
    RadioButton rbBreakAlarm;

    @BindView(R.id.rg_break_alarm_items)
    RadioGroup rgBreakAlarmItems;//断线报警器状态单选按钮组

    @BindView(R.id.radio_break_alarm_open)
    RadioButton rbBreakAlarmOpen;//断线报警器常开

    @BindView(R.id.radio_break_alarm_close)
    RadioButton rbBreakAlarmClose;//断线报警器常闭

    @BindView(R.id.btn_confirm)
    Button btnConfirm;

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private DasIOSensorInfo ioSensorInfo;

    private String rainPrecision;


    public static NetDasSwitchSensorFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetDasSwitchSensorFragment fragment = new NetDasSwitchSensorFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.das_switch_sensor_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setFilter();
        setRadioButtonListener();
        querySwitchSensorInfo();
    }

    private void setFilter() {
        mEtRainPrecision.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
    }

    /**
     * 单选框事件
     */
    private void setRadioButtonListener() {
        //关闭单选按钮
        rbCloseSwitchSensor.setOnCheckedChangeListener(onCheckedChangeListener);
        //雨量计单选按钮
        rbRainGauge.setOnCheckedChangeListener(onCheckedChangeListener);
        //断线报警器单选按钮
        rbBreakAlarm.setOnCheckedChangeListener(onCheckedChangeListener);
        rgBreakAlarmItems.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                DasIOSensorEntity entity = new DasIOSensorEntity();
                entity.setType("2");
                //断线报警器常开
                if (checkedId == R.id.radio_break_alarm_open) {
                    entity.setValue("0");

                } else if (checkedId == R.id.radio_break_alarm_close) {
                    entity.setValue("1");
                }
                setSwitchSensorInfo(entity);
            }
        });
    }

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int id = buttonView.getId();
            if (id == R.id.radio_close_switch_sensor) {//关闭开关传感器单选按钮
                if (isChecked) {
                    rbCloseSwitchSensor.setTextColor(GlobalUtil.getColor(R.color.text_color_343434));
                    rbRainGauge.setChecked(false);
                    rbBreakAlarm.setChecked(false);
                    btnConfirm.setVisibility(View.GONE);

                    //发送关闭传感器指令
                    DasIOSensorEntity entity = new DasIOSensorEntity();
                    entity.setType("0");
                    setSwitchSensorInfo(entity);
                } else {
                    rbCloseSwitchSensor.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                }
            } else if (id == R.id.radio_rain_gauge) {//雨量计单选按钮
                if (isChecked) {
                    rbCloseSwitchSensor.setChecked(false);
                    rbRainGauge.setTextColor(GlobalUtil.getColor(R.color.text_color_343434));
                    mEtRainPrecision.setEnabled(true);
                    rbBreakAlarm.setChecked(false);
                    btnConfirm.setVisibility(View.VISIBLE);
                } else {
                    rbRainGauge.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                    mEtRainPrecision.setEnabled(false);
                }
            } else if (id == R.id.radio_break_alarm) {  //断线报警器单选按钮
                if (isChecked) {
                    rbCloseSwitchSensor.setChecked(false);
                    rbRainGauge.setChecked(false);
                    rbBreakAlarm.setTextColor(GlobalUtil.getColor(R.color.text_color_343434));
                    rgBreakAlarmItems.setVisibility(View.VISIBLE);
                    btnConfirm.setVisibility(View.GONE);
                } else {
                    rbBreakAlarm.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                    rgBreakAlarmItems.setVisibility(View.GONE);
                }
            }
        }
    };

    /**
     * 查询开关量传感器信息
     */
    private void querySwitchSensorInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_IO_SENSOR_INFO);
        showProgressDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 设置开关量传感器信息
     */
    private void setSwitchSensorInfo(DasIOSensorEntity entity) {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_IO_SENSOR_INFO, entity);
        showProgressDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_confirm) {
            KeyBordUtils.hideSoftKeyboard(view);
            if (!checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            processSave();
        }
    }

    private boolean checkValueIsValid() {
        rainPrecision = mEtRainPrecision.getText().toString().trim();

        if (TextUtils.isEmpty(rainPrecision)) {
            ToastUtils.show("请输入雨量计精度!");
            mEtRainPrecision.requestFocus();
            return false;
        }
        try {
            double value = Double.parseDouble(rainPrecision);
            if (value >= 1) {
                ToastUtils.show("请输入正确的雨量计精度!");
                mEtRainPrecision.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的雨量计精度!");
            mEtRainPrecision.requestFocus();
            return false;
        }
        return true;
    }

    private void processSave() {
        DasIOSensorEntity entity = new DasIOSensorEntity();
        entity.setType("1");
        int precision = (int) (Double.parseDouble(rainPrecision) * 100);
        entity.setValue(precision + "");

        setSwitchSensorInfo(entity);
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            dismissProgressDialog();
            showDispatchFailedDialog();
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponseRunnable(0);
        }
    }

    /**
     * 指令下发失败弹框
     */
    private void showDispatchFailedDialog() {
        ToastUtils.show("下发指令失败");
    }

    /**
     * 查询指令响应结果出错
     *
     * @param errMsg
     */
    @Override
    protected void onQueryCmdResponseResultError(String errMsg) {
        super.onQueryCmdResponseResultError(errMsg);
        ToastUtils.show("指令响应错误");
    }

    /**
     * 查询指令响应结果超时
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultTimeOut(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultTimeOut(queryCmdResult);
        ToastUtils.show("指令响应超时");
    }

    /**
     * 查询指令响应结果成功
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultSuccess(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultSuccess(queryCmdResult);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(queryCmdResult.getCmdEngName());
        switch (type) {
            case DAS_MD_GET_IO_SENSOR_INFO: {//
                IOTCommandResult<DasIOSensorInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询开关量传感器参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ioSensorInfo = commandResult.getResult();
                initSwitchSensor();
            }
            break;

            case DAS_MD_SET_IO_SENSOR_INFO: {//
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置开关量传感器参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                doAfterSetting();
            }
            break;

            default:
                break;
        }
    }

    private void initSwitchSensor() {
        if (ioSensorInfo == null) {
            Timber.e("DasIOSensorInfo 为空!");
            ioSensorInfo = new DasIOSensorInfo();
            return;
        }
        switch (ioSensorInfo.getType()) {
            case "0":
                //初始化时不需要触发 OnCheckedChangeListener 事件
                rbCloseSwitchSensor.setOnCheckedChangeListener(null);
                rbCloseSwitchSensor.setChecked(true);
                rbRainGauge.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                mEtRainPrecision.setEnabled(false);
                rbBreakAlarm.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                rgBreakAlarmItems.setVisibility(View.GONE);
                btnConfirm.setVisibility(View.GONE);
                rbCloseSwitchSensor.setOnCheckedChangeListener(onCheckedChangeListener);
                break;

            case "1": {//雨量站开启
                //初始化时不需要触发 OnCheckedChangeListener 事件
                rbRainGauge.setOnCheckedChangeListener(null);
                rbRainGauge.setChecked(true);
                rbCloseSwitchSensor.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                rbBreakAlarm.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                rgBreakAlarmItems.setVisibility(View.GONE);
                btnConfirm.setVisibility(View.VISIBLE);
                rbRainGauge.setOnCheckedChangeListener(onCheckedChangeListener);

                try {
                    rainPrecision = decimalFormat.format(Double.parseDouble(ioSensorInfo.getValue()) / 10000);
                    mEtRainPrecision.setText(rainPrecision);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            break;

            case "2": {//断线报警器开启
                //初始化时不需要触发 OnCheckedChangeListener 事件
                rbBreakAlarm.setOnCheckedChangeListener(null);
                rbBreakAlarm.setChecked(true);
                rgBreakAlarmItems.setVisibility(View.VISIBLE);
                rbCloseSwitchSensor.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                rbRainGauge.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                mEtRainPrecision.setEnabled(false);
                if (ioSensorInfo.getValue().equals("0")) {
                    rbBreakAlarmOpen.setChecked(true);
                } else if (ioSensorInfo.getValue().equals("1")) {
                    rbBreakAlarmClose.setChecked(true);
                }
                btnConfirm.setVisibility(View.GONE);
                rbBreakAlarm.setOnCheckedChangeListener(onCheckedChangeListener);
            }
            break;
        }
    }

    private void doAfterSetting() {
        ToastUtils.show("保存成功");
    }

    @Override
    public boolean onBackPressed() {
        if (checkValueIsChange()) {
            warnNotYetSettingBeforeLeavePage();
            return true;
        } else {
            return false;
        }
    }

    private boolean checkValueIsChange() {
        if (rbRainGauge.isChecked() && rainPrecision != null && !rainPrecision.equals(mEtRainPrecision.getText().toString().trim())) {
            return true;
        }
        return false;
    }
}