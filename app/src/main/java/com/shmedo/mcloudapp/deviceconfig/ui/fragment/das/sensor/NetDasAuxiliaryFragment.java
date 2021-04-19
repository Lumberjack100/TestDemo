package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.configlibrary.ble.enums.BreakAlarmStatus;
import com.shmedo.configlibrary.ble.enums.OsmometerStatus;
import com.shmedo.configlibrary.ble.enums.RainStation;
import com.shmedo.configlibrary.ble.model.BaseConfigInfo;
import com.shmedo.configlibrary.ble.model.BreakAlarmStatusInfo;
import com.shmedo.configlibrary.ble.model.QueryOsmometerParameterInfo;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.das.DasCollectorInfo;
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

public class NetDasAuxiliaryFragment extends BaseNetIotCommunicateFragment {
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

    @BindView(R.id.digitalOsmometerEnableSBtn)
    SwitchButton mSbDigitalOsmometerEnable;

    @BindView(R.id.digitalOsmometerChildsLayout)
    ViewGroup digitalOsmometerChildsLayout;//渗压计配置项

    @BindView(R.id.osmometerAddressEt)
    EditText mEtOsmometerAddress;//渗压计地址

    @BindView(R.id.waterAlarmValueEt)
    EditText mEtWaterAlarmValue;//深度触发值-水位报警值

    @BindView(R.id.waterRevisedEt)
    EditText mEtWaterRevised;//深度修正值

    @BindView(R.id.osmometerCordEt)
    EditText mEtOsmometerCord;//渗压计绳长

    @BindView(R.id.nozzelHeightEt)
    EditText mEtNozzelHeight;//管口高程

    @BindView(R.id.btn_confirm)
    Button btnConfirm;

    private boolean isFirstEnterPage = true;//是否第一次进入页面

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private String collectorModel = "";//采集器类型
    private BaseConfigInfo baseConfigInfo;
    private BreakAlarmStatusInfo breakAlarmStatusInfo;
    private QueryOsmometerParameterInfo queryOsmometerParameterInfo;//数字式渗压计参数

    private String rainPrecision;
    private String osmometerAddress;
    private String depthTriggerValue;
    private String depthCorrection;
    private String osmometerLength;
    private String nozzelHeight;

    private String cmdRainPrecision;// 雨量计精度配置指令
    private String cmdOsmometerAddress;//渗压计地址配置指令
    private String cmdDepthTriggerValue;//水位报警值配置指令
    private String cmdDepthCorrection;//深度修正值配置指令
    private String cmdOsmometerLength;//渗压计绳长配置指令
    private String cmdNozzelHeight;//管口高程配置指令


    public static NetDasAuxiliaryFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetDasAuxiliaryFragment fragment = new NetDasAuxiliaryFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.das_auxiliary_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setFilter();
        setSwitchViewListener();
        setRadioButtonListener();
        querySwitchSensorInfo();
    }

    private void setFilter() {
        mEtRainPrecision.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtOsmometerAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtWaterAlarmValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtWaterRevised.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtOsmometerCord.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtNozzelHeight.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
    }

    /**
     * 开关控件事件
     */
    private void setSwitchViewListener() {
        //数字式渗压计启用开关事件
        mSbDigitalOsmometerEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    enableOrDisableDigitalOsmometerCmd(OsmometerStatus.OSMOMETER_OPEN);
                    digitalOsmometerChildsLayout.setVisibility(View.VISIBLE);
                    btnConfirm.setEnabled(true);
                    isFirstEnterPage = false;
                } else {
                    enableOrDisableDigitalOsmometerCmd(OsmometerStatus.OSMOMETER_CLOSE);
                    digitalOsmometerChildsLayout.setVisibility(View.GONE);

                    //恢复刚进入页面时的渗压计原始数据
                    mEtOsmometerAddress.setText(osmometerAddress);
                    mEtWaterAlarmValue.setText(depthTriggerValue);
                    mEtWaterRevised.setText(depthCorrection);
                    mEtOsmometerCord.setText(osmometerLength);
                    mEtNozzelHeight.setText(nozzelHeight);

                    //判断是否禁用保存按钮
                    if (!rbRainGauge.isChecked()) {
                        btnConfirm.setEnabled(false);
                    }
                }
            }
        });
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
                //断线报警器常开
                if (checkedId == R.id.radio_break_alarm_open) {
                    queryOrSetBreakAlarmCmd(BreakAlarmStatus.OPEN);

                } else if (checkedId == R.id.radio_break_alarm_close) {
                    queryOrSetBreakAlarmCmd(BreakAlarmStatus.CLOSE);
                }
            }
        });
    }

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int id = buttonView.getId();
            if (id == R.id.radio_close_switch_sensor) {//关闭开关传感器单选按钮
                if (isChecked) {
                    setSwitchSensorCmd(RainStation.CLOSE);
                    rbCloseSwitchSensor.setTextColor(GlobalUtil.getColor(R.color.text_color_343434));
                    rbRainGauge.setChecked(false);
                    rbBreakAlarm.setChecked(false);
                    if (!mSbDigitalOsmometerEnable.isChecked()) {
                        btnConfirm.setEnabled(false);
                    }
                } else {
                    rbCloseSwitchSensor.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                }
            } else if (id == R.id.radio_rain_gauge) {//雨量计单选按钮
                if (isChecked) {
                    setSwitchSensorCmd(RainStation.OPEN);
                    rbRainGauge.setTextColor(GlobalUtil.getColor(R.color.text_color_343434));
                    mEtRainPrecision.setEnabled(true);
                    rbCloseSwitchSensor.setChecked(false);
                    rbBreakAlarm.setChecked(false);
                    btnConfirm.setEnabled(true);
                } else {
                    rbRainGauge.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                    mEtRainPrecision.setEnabled(false);
                }
            } else if (id == R.id.radio_break_alarm) {  //断线报警器单选按钮
                if (isChecked) {
                    setSwitchSensorCmd(RainStation.ALARM_OPEN);
                    rbBreakAlarm.setTextColor(GlobalUtil.getColor(R.color.text_color_343434));
                    rgBreakAlarmItems.setVisibility(View.VISIBLE);
                    rbCloseSwitchSensor.setChecked(false);
                    rbRainGauge.setChecked(false);
                    if (!mSbDigitalOsmometerEnable.isChecked()) {
                        btnConfirm.setEnabled(false);
                    }
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

        return true;
    }

    private void processSave() {

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
            case DAS_MD_GET_COLLECTOR_CONTROL: {//
                IOTCommandResult<DasCollectorInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询采集器参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                collectorInfo = commandResult.getResult();
                initCollectorInfo();
            }
            break;

            case DAS_MD_SET_COLLECTOR_CONTROL: {//
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置采集器参数出错!", cmdResult.getReason());
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

    private void doAfterSetting() {
        ToastUtils.show("保存成功");
    }
}