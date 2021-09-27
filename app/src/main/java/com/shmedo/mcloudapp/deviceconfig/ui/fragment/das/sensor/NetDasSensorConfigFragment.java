package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.das.DasDigitalPiezometerEntity;
import com.shmedo.configlibrary.iot.cmd.entity.das.DasIOSensorEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.das.DasDigitalPiezometerInfo;
import com.shmedo.configlibrary.iot.model.das.DasIOSensorInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.DasExternalSensorListNewActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wandersnail.widget.textview.RoundTextView;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/20 <br/>
 * 描述：     TODO
 */
public class NetDasSensorConfigFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

    @BindView(R.id.radio_close_switch_sensor)
    RadioButton rbCloseSwitchSensor;//关闭开关量传感器

    @BindView(R.id.radio_rain_gauge)
    RadioButton rbRainGauge;

    @BindView(R.id.rainPrecisionLayout)
    View rainPrecisionLayout;

    @BindView(R.id.tvPrecision)
    RoundTextView mTvPrecision;

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

    private DecimalFormat decimalFormat = new DecimalFormat();

    private DasIOSensorInfo ioSensorInfo;
    private String rainPrecision;

    private DasDigitalPiezometerInfo digitalPiezometerInfo;
    private String osmometerAddress;
    private String depthTriggerValue;
    private String depthCorrection;
    private String osmometerLength;
    private String nozzelHeight;

    private boolean isDigitalPiezometerChange = false;
    private boolean isSaveParamOperation = false;//判断当前是保存参数操作，还是关闭北斗数传终端操作

    public static NetDasSensorConfigFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetDasSensorConfigFragment fragment = new NetDasSensorConfigFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_das_sensor;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setFilter();
        setRadioButtonListener();
        setSwitchViewListener();
        initRefreshLayout();
        mRefreshLayout.setEnableLoadMore(false);
        //是否在刷新的时候禁止内容的一切手势操作（默认false）
        mRefreshLayout.setDisableContentWhenRefresh(true);
        mRefreshLayout.autoRefresh();
    }

    private void setFilter() {
        mEtOsmometerAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtWaterAlarmValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtWaterRevised.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtOsmometerCord.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});
        mEtNozzelHeight.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});

        rainPrecision = "0.1";
        mTvPrecision.setText(rainPrecision);
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
        rgBreakAlarmItems.setOnCheckedChangeListener(breakAlarmOnCheckedChangeListener);
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
                    if (!mSbDigitalOsmometerEnable.isChecked()) {
                        btnConfirm.setVisibility(View.GONE);
                    }
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
                    rainPrecisionLayout.setVisibility(View.VISIBLE);
                    rbBreakAlarm.setChecked(false);
                    btnConfirm.setVisibility(View.VISIBLE);
                } else {
                    rbRainGauge.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                    rainPrecisionLayout.setVisibility(View.GONE);
                }
            } else if (id == R.id.radio_break_alarm) {  //断线报警器单选按钮
                if (isChecked) {
                    rbCloseSwitchSensor.setChecked(false);
                    rbRainGauge.setChecked(false);
                    rbBreakAlarm.setTextColor(GlobalUtil.getColor(R.color.text_color_343434));
                    rgBreakAlarmItems.setVisibility(View.VISIBLE);
                    if (!mSbDigitalOsmometerEnable.isChecked()) {
                        btnConfirm.setVisibility(View.GONE);
                    }
                    //发送断线报警器常开指令
                    DasIOSensorEntity entity = new DasIOSensorEntity();
                    entity.setType("2");
                    entity.setValue("0");
                    setSwitchSensorInfo(entity);
                } else {
                    rbBreakAlarm.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                    rgBreakAlarmItems.setVisibility(View.GONE);
                }
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener breakAlarmOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
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
    };

    /**
     * 数字式渗压计启用开关事件
     */
    private void setSwitchViewListener() {
        //数字式渗压计启用开关事件
        mSbDigitalOsmometerEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    digitalOsmometerChildsLayout.setVisibility(View.VISIBLE);
                    btnConfirm.setVisibility(View.VISIBLE);
                } else {
                    digitalOsmometerChildsLayout.setVisibility(View.GONE);
                    //判断是否禁用保存按钮
                    if (!rbRainGauge.isChecked()) {
                        btnConfirm.setVisibility(View.GONE);
                    }
                    disableDigitalPiezometer();
                }
            }
        });
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                querySwitchSensorInfo();
            }
        });
    }

    /**
     * 查询开关量传感器信息
     */
    private void querySwitchSensorInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_IO_SENSOR_INFO);
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

    /**
     * 查询渗压计信息
     */
    private void queryDigitalPiezometerInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_DIGITAL_PIEZOMETER_INFO);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 关闭渗压计
     */
    private void disableDigitalPiezometer() {
        DasDigitalPiezometerEntity entity = new DasDigitalPiezometerEntity();
        entity.setSw("0");

        isSaveParamOperation = false;
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_DIGITAL_PIEZOMETER_INFO, entity);
        showProgressDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 设置渗压计参数
     */
    private void setDigitalOsmometerParam() {
        DasDigitalPiezometerEntity entity = new DasDigitalPiezometerEntity();
        entity.setSw("1");
        entity.setAddr(osmometerAddress);
        entity.setThreshold(depthTriggerValue);
        entity.setCorrval(depthCorrection);
        entity.setRopelen(osmometerLength);
        entity.setTubealti(nozzelHeight);

        isSaveParamOperation = true;
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_DIGITAL_PIEZOMETER_INFO, entity);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    @OnClick({R.id.tvPrecision, R.id.extendSensorLayout, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.tvPrecision) {
            String[] values = getResources().getStringArray(R.array.rain_value);
            int pos = Arrays.asList(values).indexOf(rainPrecision);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("雨量精度");
            builder.setSingleChoiceItems(values, pos, (dialog, item1) -> {
                rainPrecision = values[item1];
                mTvPrecision.setText(values[item1]);
                dialog.dismiss();
            });
            builder.create().show();
        } else if (id == R.id.extendSensorLayout) {
            DasExternalSensorListNewActivity.startActivity(mActivity, projectDeviceInfo);

        } else if (id == R.id.btn_confirm) {
            KeyBordUtils.hideSoftKeyboard(view);
            if (!checkDigitalOsmometerParam()) {
                Timber.w("参数存在错误!");
                return;
            }
            processSave();
        }
    }

    private boolean checkDigitalOsmometerParam() {
        if (!mSbDigitalOsmometerEnable.isChecked())
            return true;

        osmometerAddress = mEtOsmometerAddress.getText().toString().trim();
        depthTriggerValue = mEtWaterAlarmValue.getText().toString().trim();
        depthCorrection = mEtWaterRevised.getText().toString().trim();
        osmometerLength = mEtOsmometerCord.getText().toString().trim();
        nozzelHeight = mEtNozzelHeight.getText().toString().trim();

        if (TextUtils.isEmpty(osmometerAddress)) {
            ToastUtils.show("请输入渗压计地址");
            mEtOsmometerAddress.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(osmometerAddress);
            if (value < 0 || value > 255) {
                ToastUtils.show("请输入正确的渗压计地址!");
                mEtOsmometerAddress.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的渗压计地址!");
            mEtOsmometerAddress.requestFocus();
            return false;
        }

        decimalFormat.applyPattern("#");
        if (!decimalFormat.format(Double.parseDouble(digitalPiezometerInfo.getThreshold())).equals(depthTriggerValue)) {
            if (TextUtils.isEmpty(depthTriggerValue)) {
                ToastUtils.show("请输入水位报警值");
                return false;
            }
            try {
                int value = Integer.parseInt(depthTriggerValue);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的水位报警值!");
                mEtWaterAlarmValue.requestFocus();
                return false;
            }
        } else {
            depthTriggerValue = "";
        }

        decimalFormat.applyPattern("#.###");
        if (!decimalFormat.format(Double.parseDouble(digitalPiezometerInfo.getCorrval())).equals(depthCorrection)) {
            if (TextUtils.isEmpty(depthCorrection)) {
                ToastUtils.show("请输入水深修正值");
                return false;
            }
            try {
                double value = Double.parseDouble(depthCorrection);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的水深修正值!");
                mEtWaterRevised.requestFocus();
                return false;
            }
        } else {
            depthCorrection = "";
        }

        if (!decimalFormat.format(Double.parseDouble(digitalPiezometerInfo.getRopelen())).equals(osmometerLength)) {
            if (TextUtils.isEmpty(osmometerLength)) {
                ToastUtils.show("请输入渗压计绳长");
                return false;
            }
            try {
                double value = Double.parseDouble(osmometerLength);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的渗压计绳长!");
                mEtOsmometerCord.requestFocus();
                return false;
            }
        } else {
            osmometerLength = "";
        }

        if (!decimalFormat.format(Double.parseDouble(digitalPiezometerInfo.getTubealti())).equals(nozzelHeight)) {
            if (TextUtils.isEmpty(nozzelHeight)) {
                ToastUtils.show("请输入管口高程值");
                return false;
            }
            try {
                double value = Double.parseDouble(nozzelHeight);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的管口高程值!");
                mEtNozzelHeight.requestFocus();
                return false;
            }
        } else {
            nozzelHeight = "";
        }
        isDigitalPiezometerChange = true;
        return true;
    }

    private void processSave() {
        if (rbRainGauge.isChecked()) {
            DasIOSensorEntity entity = new DasIOSensorEntity();
            entity.setType("1");
            entity.setValue(rainPrecision);
            setSwitchSensorInfo(entity);
        } else if (mSbDigitalOsmometerEnable.isChecked()) {
            setDigitalOsmometerParam();
            showProgressDialog("处理中...");
        }
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            if (mRefreshLayout.isRefreshing()) {
                mRefreshLayout.finishRefresh(false);
            }
            dismissProgressDialog();
            ToastUtils.show("下发指令失败");
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponse();
        }
    }

    /**
     * 查询指令响应结果出错
     *
     * @param errMsg
     */
    @Override
    protected void onQueryCmdResponseResultError(String errMsg) {
        super.onQueryCmdResponseResultError(errMsg);
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh(false);
        }
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
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh(false);
        }
        ToastUtils.show("指令响应超时");
    }

    /**
     * 查询指令响应结果成功
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultSuccess(QueryCmdResult queryCmdResult) {
//        super.onQueryCmdResponseResultSuccess(queryCmdResult);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(queryCmdResult.getCmdEngName());
        switch (type) {
            case DAS_MD_GET_IO_SENSOR_INFO: {//查询开关量传感器参数
                IOTCommandResult<DasIOSensorInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    String errMsg = String.format("%s %s", "查询开关量传感器参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ioSensorInfo = commandResult.getResult();
                initSwitchSensor();
                queryDigitalPiezometerInfo();
            }
            break;

            case DAS_MD_GET_DIGITAL_PIEZOMETER_INFO: {//查询数字渗压计参数
                mRefreshLayout.finishRefresh(true);
                IOTCommandResult<DasDigitalPiezometerInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询数字渗压计参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                digitalPiezometerInfo = commandResult.getResult();
                initDigitalPiezometerInfo();
            }
            break;

            case DAS_MD_SET_IO_SENSOR_INFO: {//设置开关量传感器
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    dismissProgressDialog();
                    String errMsg = String.format("%s %s", "设置开关量传感器参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }

                if (mSbDigitalOsmometerEnable.isChecked() && isDigitalPiezometerChange) {
                    setDigitalOsmometerParam();
                } else {
                    dismissProgressDialog();
                    ToastUtils.show("设置成功");
                }
            }
            break;

            case DAS_MD_SET_DIGITAL_PIEZOMETER_INFO: {//设置数字渗压计
                dismissProgressDialog();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置数字渗压计参数出错!", cmdResult.getReason());
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
                rainPrecisionLayout.setVisibility(View.GONE);
                rbBreakAlarm.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                rgBreakAlarmItems.setVisibility(View.GONE);
                rbCloseSwitchSensor.setOnCheckedChangeListener(onCheckedChangeListener);
                break;

            case "1": {//雨量站开启
                //初始化时不需要触发 OnCheckedChangeListener 事件
                rbRainGauge.setOnCheckedChangeListener(null);
                rbRainGauge.setChecked(true);
                rainPrecisionLayout.setVisibility(View.VISIBLE);
                rbCloseSwitchSensor.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                rbBreakAlarm.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                rgBreakAlarmItems.setVisibility(View.GONE);
                rbRainGauge.setOnCheckedChangeListener(onCheckedChangeListener);
                btnConfirm.setVisibility(View.VISIBLE);
                try {
                    decimalFormat.applyPattern("#.#");
                    rainPrecision = decimalFormat.format(Double.parseDouble(ioSensorInfo.getValue()));
                    mTvPrecision.setText(rainPrecision);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            break;

            case "2": {//断线报警器开启
                //初始化时不需要触发 OnCheckedChangeListener 事件
                rbBreakAlarm.setOnCheckedChangeListener(null);
                rbBreakAlarm.setChecked(true);
                rgBreakAlarmItems.setOnCheckedChangeListener(null);
                rgBreakAlarmItems.setVisibility(View.VISIBLE);
                rbCloseSwitchSensor.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                rbRainGauge.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                rainPrecisionLayout.setVisibility(View.GONE);
                if (ioSensorInfo.getValue().equals("0")) {
                    rbBreakAlarmOpen.setChecked(true);
                } else if (ioSensorInfo.getValue().equals("1")) {
                    rbBreakAlarmClose.setChecked(true);
                }
                rbBreakAlarm.setOnCheckedChangeListener(onCheckedChangeListener);
                rgBreakAlarmItems.setOnCheckedChangeListener(breakAlarmOnCheckedChangeListener);
            }
            break;
        }
    }

    private void initDigitalPiezometerInfo() {
        if (digitalPiezometerInfo == null) {
            Timber.e("DasDigitalPiezometerInfo 为空!");
            digitalPiezometerInfo = new DasDigitalPiezometerInfo();
            mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(false);
            digitalOsmometerChildsLayout.setVisibility(View.GONE);
            if (!rbRainGauge.isChecked()) {
                btnConfirm.setVisibility(View.GONE);
            }
            return;
        }
        if (digitalPiezometerInfo.getSw().equals("0")) {
            mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(false);
            digitalOsmometerChildsLayout.setVisibility(View.GONE);
            if (!rbRainGauge.isChecked()) {
                btnConfirm.setVisibility(View.GONE);
            }
        } else {
            mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(true);
            digitalOsmometerChildsLayout.setVisibility(View.VISIBLE);
            btnConfirm.setVisibility(View.VISIBLE);
        }
        try {
            osmometerAddress = digitalPiezometerInfo.getAddr();
            decimalFormat.applyPattern("#");
            depthTriggerValue = decimalFormat.format(Double.parseDouble(digitalPiezometerInfo.getThreshold()));
            decimalFormat.applyPattern("#.###");
            depthCorrection = decimalFormat.format(Double.parseDouble(digitalPiezometerInfo.getCorrval()));
            osmometerLength = decimalFormat.format(Double.parseDouble(digitalPiezometerInfo.getRopelen()));
            nozzelHeight = decimalFormat.format(Double.parseDouble(digitalPiezometerInfo.getTubealti()));

            mEtOsmometerAddress.setText(osmometerAddress);
            mEtWaterAlarmValue.setText(depthTriggerValue);
            mEtWaterRevised.setText(depthCorrection);
            mEtOsmometerCord.setText(osmometerLength);
            mEtNozzelHeight.setText(nozzelHeight);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void doAfterSetting() {
        if (isSaveParamOperation) {
            ToastUtils.show("保存成功");
            isDigitalPiezometerChange = false;
            osmometerAddress = mEtOsmometerAddress.getText().toString().trim();
            depthTriggerValue = mEtWaterAlarmValue.getText().toString().trim();
            depthCorrection = mEtWaterRevised.getText().toString().trim();
            osmometerLength = mEtOsmometerCord.getText().toString().trim();
            nozzelHeight = mEtNozzelHeight.getText().toString().trim();
        }
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
        if (!mSbDigitalOsmometerEnable.isChecked()) {
            return false;
        }
        if (osmometerAddress != null && !osmometerAddress.equals(mEtOsmometerAddress.getText().toString().trim())) {
            return true;
        }
        if (depthTriggerValue != null && !depthTriggerValue.equals(mEtWaterAlarmValue.getText().toString().trim())) {
            return true;
        }
        if (depthCorrection != null && !depthCorrection.equals(mEtWaterRevised.getText().toString().trim())) {
            return true;
        }
        if (osmometerLength != null && !osmometerLength.equals(mEtOsmometerCord.getText().toString().trim())) {
            return true;
        }
        if (nozzelHeight != null && !nozzelHeight.equals(mEtNozzelHeight.getText().toString().trim())) {
            return true;
        }
        return false;
    }
}
