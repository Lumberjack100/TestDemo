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

import com.blankj.utilcode.util.DebouncingUtils;
import com.blankj.utilcode.util.StringUtils;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.das.DasDigitalPiezometerEntity;
import com.shmedo.configlibrary.iot.cmd.entity.das.DasIOSensorEntity;
import com.shmedo.configlibrary.iot.cmd.entity.das.DasMcuAddressEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.das.DasDigitalPiezometerInfo;
import com.shmedo.configlibrary.iot.model.das.DasIOSensorInfo;
import com.shmedo.configlibrary.iot.model.das.McuAddressInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.DasExternalSensorListNewActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;

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
    static final int RAIN_GAUGE = 1;//是否设置雨量计
    static final int DIGITAL_OSMOMETER = 1 << 1;//是否打开水位计
    static final int MCU = 1 << 2;//是否配置 MCU 地址

    private int mExistingUpdateTypes = 0;


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

    @BindView(R.id.dumpMinTimeEt)
    EditText mEtDumpMinTime;//翻斗翻转最小间隔

    @BindView(R.id.ll_dumpMinTime)
    ViewGroup dumpMinTimeLayout;//翻斗翻转最小间隔

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
    ViewGroup digitalOsmometerChildsLayout;//水位计配置项

    @BindView(R.id.osmometerAddressEt)
    EditText mEtOsmometerAddress;//水位计地址

    @BindView(R.id.waterAlarmValueEt)
    EditText mEtWaterAlarmValue;//深度触发值-水位报警值

    @BindView(R.id.waterRevisedEt)
    EditText mEtWaterRevised;//深度修正值

    @BindView(R.id.osmometerCordEt)
    EditText mEtOsmometerCord;//水位计绳长

    @BindView(R.id.nozzelHeightEt)
    EditText mEtNozzelHeight;//安装高程

    @BindView(R.id.mcuAddrLayout)
    ViewGroup mcuAddrLayout;//MCU 地址

    @BindView(R.id.et_mcu_addr)
    EditText mEtMcuAddress;//MCU 地址

    @BindView(R.id.btn_confirm)
    Button btnConfirm;

    private DecimalFormat decimalFormat = new DecimalFormat();
    private DasIOSensorInfo ioSensorInfo = new DasIOSensorInfo();
    private String rainPrecision;

    private DasDigitalPiezometerInfo digitalPiezometerInfo = new DasDigitalPiezometerInfo();
    private String osmometerAddress;
    private String depthTriggerValue;
    private String depthCorrection;
    private String osmometerLength;
    private String nozzelHeight;

    private McuAddressInfo mcuAddressInfo = new McuAddressInfo();
    private String mcuAddress;


    public static NetDasSensorConfigFragment newInstance(DeviceInfo deviceInfo) {
        NetDasSensorConfigFragment fragment = new NetDasSensorConfigFragment();
        Bundle args = new Bundle();
        args.putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_das_sensor;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        mEtDumpMinTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        mEtOsmometerAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtWaterAlarmValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtWaterRevised.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtOsmometerCord.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});
        mEtNozzelHeight.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});

        rainPrecision = "0.1";
        mTvPrecision.setText(rainPrecision);

        if (deviceInfo.getProductName().contains("MR701")) {
            mcuAddrLayout.setVisibility(View.VISIBLE);
            btnConfirm.setVisibility(View.VISIBLE);
            mExistingUpdateTypes |= MCU;
        } else {
            mcuAddrLayout.setVisibility(View.GONE);
        }
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
                    rbCloseSwitchSensor.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_343434));
                    rbRainGauge.setChecked(false);
                    rbBreakAlarm.setChecked(false);
                    mExistingUpdateTypes &= ~RAIN_GAUGE;

                    //发送关闭传感器指令
                    DasIOSensorEntity entity = new DasIOSensorEntity();
                    entity.setType("0");
                    setSwitchSensorInfo(entity);
                } else {
                    rbCloseSwitchSensor.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_cccccc));
                }
            } else if (id == R.id.radio_rain_gauge) {//雨量计单选按钮
                if (isChecked) {
                    rbCloseSwitchSensor.setChecked(false);
                    rbRainGauge.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_343434));
                    rainPrecisionLayout.setVisibility(View.VISIBLE);
                    if (!ioSensorInfo.getMin_time().equals("NullKey")) {
                        dumpMinTimeLayout.setVisibility(View.VISIBLE);
                    }
                    rbBreakAlarm.setChecked(false);
                    mExistingUpdateTypes |= RAIN_GAUGE;
                } else {
                    rbRainGauge.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_cccccc));
                    rainPrecisionLayout.setVisibility(View.GONE);
                    dumpMinTimeLayout.setVisibility(View.GONE);
                }
            } else if (id == R.id.radio_break_alarm) {  //断线报警器单选按钮
                if (isChecked) {
                    rbCloseSwitchSensor.setChecked(false);
                    rbRainGauge.setChecked(false);
                    rbBreakAlarm.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_343434));
                    rgBreakAlarmItems.setVisibility(View.VISIBLE);
                    mExistingUpdateTypes &= ~RAIN_GAUGE;

                    //发送断线报警器常开指令
                    DasIOSensorEntity entity = new DasIOSensorEntity();
                    entity.setType("2");
                    entity.setValue("0");
                    setSwitchSensorInfo(entity);
                } else {
                    rbBreakAlarm.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_cccccc));
                    rgBreakAlarmItems.setVisibility(View.GONE);
                }
            }
            btnConfirm.setVisibility((mExistingUpdateTypes & (RAIN_GAUGE | DIGITAL_OSMOMETER | MCU)) != 0 ? View.VISIBLE : View.GONE);
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
     * 数字水位计启用开关事件
     */
    private void setSwitchViewListener() {
        //数字水位计启用开关事件
        mSbDigitalOsmometerEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    digitalOsmometerChildsLayout.setVisibility(View.VISIBLE);
                    mExistingUpdateTypes |= DIGITAL_OSMOMETER;
                } else {
                    digitalOsmometerChildsLayout.setVisibility(View.GONE);
                    mExistingUpdateTypes &= ~DIGITAL_OSMOMETER;
                    disableDigitalPiezometer();
                }
                btnConfirm.setVisibility((mExistingUpdateTypes & (RAIN_GAUGE | DIGITAL_OSMOMETER | MCU)) != 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                queryParam();
            }
        });
    }

    private void queryParam() {
        commandItems.clear();

        //查询开关量传感器信息
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_IO_SENSOR_INFO);
        commandItems.add(command);

        //查询数字水位计信息
        command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_DIGITAL_PIEZOMETER_INFO);
        commandItems.add(command);

        //查询 MCU 地址
        if ((mExistingUpdateTypes & MCU) != 0) {
            command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_MCU_ADDRESS);
            commandItems.add(command);
        }
        sendCommandFromCmdList();
    }

    /**
     * 设置开关量传感器信息
     */
    private void setSwitchSensorInfo(DasIOSensorEntity entity) {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_IO_SENSOR_INFO, entity);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 关闭水位计
     */
    private void disableDigitalPiezometer() {
        DasDigitalPiezometerEntity entity = new DasDigitalPiezometerEntity();
        entity.setSw("0");

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_DIGITAL_PIEZOMETER_INFO, entity);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    @OnClick({R.id.tvPrecision, R.id.extendSensorLayout, R.id.btn_confirm})
    public void onClick(View view) {
        if (!DebouncingUtils.isValid(view, 1000)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.tvPrecision) {
            String[] values = StringUtils.getStringArray(R.array.rain_value);
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
            DasExternalSensorListNewActivity.startActivity(mActivity, deviceInfo);

        } else if (id == R.id.btn_confirm) {
            try {
                com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
                if (!checkDigitalOsmometerParam() || !checkMcuAddressParam()) {
                    Timber.w("参数存在错误!");
                    return;
                }
                processSave();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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
            ToastUtils.show("请输入水位计地址");
            mEtOsmometerAddress.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(osmometerAddress);
            if (value < 0 || value > 255) {
                ToastUtils.show("请输入正确的水位计地址!");
                mEtOsmometerAddress.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的水位计地址!");
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
            depthTriggerValue = null;
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
            depthCorrection = null;
        }

        if (!decimalFormat.format(Double.parseDouble(digitalPiezometerInfo.getRopelen())).equals(osmometerLength)) {
            if (TextUtils.isEmpty(osmometerLength)) {
                ToastUtils.show("请输入水位计绳长");
                return false;
            }
            try {
                double value = Double.parseDouble(osmometerLength);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的水位计绳长!");
                mEtOsmometerCord.requestFocus();
                return false;
            }
        } else {
            osmometerLength = null;
        }

        if (!decimalFormat.format(Double.parseDouble(digitalPiezometerInfo.getTubealti())).equals(nozzelHeight)) {
            if (TextUtils.isEmpty(nozzelHeight)) {
                ToastUtils.show("请输入安装高程值");
                return false;
            }
            try {
                double value = Double.parseDouble(nozzelHeight);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的安装高程值!");
                mEtNozzelHeight.requestFocus();
                return false;
            }
        } else {
            nozzelHeight = null;
        }
        return true;
    }

    private boolean checkMcuAddressParam() {
        if ((mExistingUpdateTypes & MCU) == 0) {
            return true;
        }

        mcuAddress = mEtMcuAddress.getText().toString().trim();
        if (TextUtils.isEmpty(mcuAddress)) {
            ToastUtils.show("请输入MCU地址");
            mEtMcuAddress.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(mcuAddress);
            if (value < 0 || value > 255) {
                ToastUtils.show("请输入正确的MCU地址!");
                mEtMcuAddress.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的MCU地址!");
            mEtMcuAddress.requestFocus();
            return false;
        }
        return true;
    }


    private void processSave() {
        commandItems.clear();
        //设置开关量传感器信息
        if (rbRainGauge.isChecked()) {
            DasIOSensorEntity entity = new DasIOSensorEntity();
            entity.setType("1");
            entity.setValue(rainPrecision);
            entity.setMin_time(ioSensorInfo.getMin_time().equals("NullKey") || TextUtils.isEmpty(mEtDumpMinTime.getText()) ? null : mEtDumpMinTime.getText().toString());
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_IO_SENSOR_INFO, entity);
            commandItems.add(command);
        }
        //设置水位计参数
        if (mSbDigitalOsmometerEnable.isChecked()) {
            DasDigitalPiezometerEntity entity = new DasDigitalPiezometerEntity();
            entity.setSw("1");
            entity.setAddr(osmometerAddress);
            entity.setThreshold(depthTriggerValue);
            entity.setCorrval(depthCorrection);
            entity.setRopelen(osmometerLength);
            entity.setTubealti(nozzelHeight);

            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_DIGITAL_PIEZOMETER_INFO, entity);
            commandItems.add(command);
        }
        //设置 MCU 地址
        if ((mExistingUpdateTypes & MCU) != 0) {
            DasMcuAddressEntity entity = new DasMcuAddressEntity();
            entity.setMcuaddr(mcuAddress);
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_MCU_ADDRESS, entity);
            commandItems.add(command);
        }
        showWaitDialog("处理中...");
        sendCommandFromCmdList();
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String
            cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            if (mRefreshLayout.isRefreshing()) {
                mRefreshLayout.finishRefresh(false);
            }
            dismissWaitDialog();
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
                sendCommandFromCmdList(() -> {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(true);
                    }
                });
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

            case DAS_MD_GET_DIGITAL_PIEZOMETER_INFO: {//查询数字水位计参数
                sendCommandFromCmdList(() -> {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(true);
                    }
                });
                IOTCommandResult<DasDigitalPiezometerInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询数字水位计参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                digitalPiezometerInfo = commandResult.getResult();
                initDigitalPiezometerInfo();
            }
            break;

            case DAS_MD_GET_MCU_ADDRESS: {//查询 MCU 地址
                sendCommandFromCmdList(() -> {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(true);
                    }
                });
                IOTCommandResult<McuAddressInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询 MCU 地址出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                mcuAddressInfo = commandResult.getResult();
                initMcuAddress();
            }
            break;

            case DAS_MD_SET_IO_SENSOR_INFO: {//设置开关量传感器
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "设置开关量传感器参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                sendCommandFromCmdList(() -> ToastUtils.show("保存成功"));
            }
            break;

            case DAS_MD_SET_DIGITAL_PIEZOMETER_INFO: {//设置数字水位计
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "设置数字水位计参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                sendCommandFromCmdList(() -> ToastUtils.show("保存成功"));
            }
            break;

            case DAS_MD_SET_MCU_ADDRESS: {//设置 MCU 地址
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "设置 MCU 地址出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                sendCommandFromCmdList(() -> ToastUtils.show("保存成功"));
            }

            default:
                break;
        }
    }

    /**
     * 初始化开关量参数
     */
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
                rbRainGauge.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_cccccc));
                rainPrecisionLayout.setVisibility(View.GONE);
                dumpMinTimeLayout.setVisibility(View.GONE);
                rbBreakAlarm.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_cccccc));
                rgBreakAlarmItems.setVisibility(View.GONE);
                rbCloseSwitchSensor.setOnCheckedChangeListener(onCheckedChangeListener);
                break;

            case "1": {//雨量站开启
                //初始化时不需要触发 OnCheckedChangeListener 事件
                rbRainGauge.setOnCheckedChangeListener(null);
                rbRainGauge.setChecked(true);
                rainPrecisionLayout.setVisibility(View.VISIBLE);
                rbCloseSwitchSensor.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_cccccc));
                rbBreakAlarm.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_cccccc));
                rgBreakAlarmItems.setVisibility(View.GONE);
                rbRainGauge.setOnCheckedChangeListener(onCheckedChangeListener);
                mExistingUpdateTypes |= RAIN_GAUGE;
                btnConfirm.setVisibility(View.VISIBLE);
                try {
                    decimalFormat.applyPattern("#.#");
                    rainPrecision = decimalFormat.format(Double.parseDouble(ioSensorInfo.getValue()));
                    mTvPrecision.setText(rainPrecision);

                    //TODO #gh# 翻斗翻转最小间隔参数为指令新增加项，兼容旧版指令处理
                    if (ioSensorInfo.getMin_time().equals("NullKey")) {
                        dumpMinTimeLayout.setVisibility(View.GONE);
                    } else {
                        dumpMinTimeLayout.setVisibility(View.VISIBLE);
                        mEtDumpMinTime.setText(ioSensorInfo.getMin_time());
                    }
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
                rbCloseSwitchSensor.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_cccccc));
                rbRainGauge.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_cccccc));
                rainPrecisionLayout.setVisibility(View.GONE);
                dumpMinTimeLayout.setVisibility(View.GONE);
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

    /**
     * 初始化数字式渗压计参数
     */
    private void initDigitalPiezometerInfo() {
        if (digitalPiezometerInfo == null) {
            Timber.e("DasDigitalPiezometerInfo 为空!");
            digitalPiezometerInfo = new DasDigitalPiezometerInfo();
            mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(false);
            digitalOsmometerChildsLayout.setVisibility(View.GONE);
            return;
        }
        if (digitalPiezometerInfo.getSw().equals("0")) {
            mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(false);
            digitalOsmometerChildsLayout.setVisibility(View.GONE);
        } else {
            mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(true);
            digitalOsmometerChildsLayout.setVisibility(View.VISIBLE);
            mExistingUpdateTypes |= DIGITAL_OSMOMETER;
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

    private void initMcuAddress() {
        if (mcuAddressInfo == null) {
            Timber.e("McuAddressInfo 为空!");
            mcuAddressInfo = new McuAddressInfo();
            return;
        }
        mcuAddress = mcuAddressInfo.getMcuaddr();
        mEtMcuAddress.setText(mcuAddress);
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
//        if (!mSbDigitalOsmometerEnable.isChecked()) {
//            return false;
//        }
//        if (osmometerAddress != null && !osmometerAddress.equals(mEtOsmometerAddress.getText().toString().trim())) {
//            return true;
//        }
//        if (depthTriggerValue != null && !depthTriggerValue.equals(mEtWaterAlarmValue.getText().toString().trim())) {
//            return true;
//        }
//        if (depthCorrection != null && !depthCorrection.equals(mEtWaterRevised.getText().toString().trim())) {
//            return true;
//        }
//        if (osmometerLength != null && !osmometerLength.equals(mEtOsmometerCord.getText().toString().trim())) {
//            return true;
//        }
//        if (nozzelHeight != null && !nozzelHeight.equals(mEtNozzelHeight.getText().toString().trim())) {
//            return true;
//        }
        return false;
    }
}
