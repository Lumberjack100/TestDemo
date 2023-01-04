package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.entity.vms.SetVmsTerminalSensorParamsEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.MonitoringType;
import com.shmedo.configlibrary.iot.enums.VmsSensorCalculation;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalSensorInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.vms.LinearParamView;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.vms.MagnificationView;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.vms.ModulusView;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.vms.PolynomialParamView;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.VmsViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/11/21 <br/>
 * 描述：     Vms终端4g 模式扩展传感器配置页面
 */
public class NetVmsTerminalExternalSensorParamFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.contentLayout)
    ViewGroup contentLayout;

    @BindView(R.id.sensorEnableSBtn)
    SwitchButton mSbSensorEnable;

    @BindView(R.id.tv_sensor_calculation)
    TextView mTvSensorCalculation;//计算方式

    @BindView(R.id.tv_monitor_type)
    TextView mTvMonitorType;//监测类型

    @BindView(R.id.sensorSerialNumber)
    EditText mEtSensorSerialNumber;//传感器序号

    @BindView(R.id.linearParamView)
    LinearParamView linearParamView;//直线式参数

    @BindView(R.id.polynomialParamView)
    PolynomialParamView polynomialParamView;//多项式参数

    @BindView(R.id.modulusView)
    ModulusView modulusView;//模数解算参数

    @BindView(R.id.magnificationView)
    MagnificationView magnificationView;//倍率解算参数

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    private VmsTerminalSensorInfo sensorInfo;

    private List<String> calculationList = Arrays.asList("直线式", "多项式", "MEMS", "模数", "倍率");
    private List<String> monitorTypeList = new ArrayList<>();
    //直线式和多项式计算方式下支持的传感器
    private List<String> vibratingWireMonitorTypeList = Arrays.asList(MonitoringType.CRACK_METER.getDescription(), MonitoringType.AXIAL_FORCE_METER.getDescription(), MonitoringType.WATER_PRESSURE_METER.getDescription(), MonitoringType.WATER_LEVEL_METER.getDescription(), MonitoringType.OSMOMETER.getDescription());
    //MEMS计算方式下支持的传感器
    private List<String> digitalMonitorTypeList = Arrays.asList(MonitoringType.ACCELEROMETER.getDescription(), MonitoringType.INCLINOMETER_METER.getDescription(), MonitoringType.AVALANCHE_METER.getDescription());
    //模数计算方式下支持的传感器
    private List<String> modulusMonitorTypeList = Arrays.asList(MonitoringType.AXIAL_FORCE_METER.getDescription());
    //倍率计算方式下支持的传感器
    private List<String> magnificationMonitorTypeList = Arrays.asList(MonitoringType.RAIN_METER.getDescription(), MonitoringType.BOREHOLE_INCLINOMETER.getDescription(), MonitoringType.CRACK_METER.getDescription(), MonitoringType.MUD_LEVEL_METER.getDescription(), MonitoringType.SOIL_MOISTURE_METER.getDescription(), MonitoringType.WATER_LEVEL_METER.getDescription());

    private VmsSensorCalculation sensorCalculation;
    private String sensorSerialNumber;//传感器序号

    private boolean isSaveParamOperation = false;//判断当前是保存参数操作，还是关闭数据中心操作
    private boolean isResultOK = false;//返回的结果是否是 Activity.RESULT_OK

    private VmsViewModel vmsViewModel;

    public static NetVmsTerminalExternalSensorParamFragment newInstance(DeviceInfo deviceInfo, VmsTerminalSensorInfo sensorInfo) {
        NetVmsTerminalExternalSensorParamFragment fragment = new NetVmsTerminalExternalSensorParamFragment();
        Bundle args = new Bundle();
        args.putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo);
        args.putParcelable(AppContants.Extras.SENSOR_PARAM, sensorInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sensorInfo = getArguments().getParcelable(AppContants.Extras.SENSOR_PARAM);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.vms_terminal_external_sensor_param_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vmsViewModel = getApplicationScopeViewModel(VmsViewModel.class);
        setView();
        initData();
        setSwitchViewListener();
    }

    private void setView() {
        mEtSensorSerialNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
    }

    private void initData() {
        if (sensorInfo == null) {
            sensorInfo = new VmsTerminalSensorInfo();
            return;
        }
        //根据传感器计算方式展示不同视图
        sensorCalculation = VmsSensorCalculation.value(sensorInfo.getType());
        if (sensorCalculation == VmsSensorCalculation.LINEAR) {//直线式(振弦式传感器一种)
            mTvSensorCalculation.setText(calculationList.get(0));
            linearParamView.setVisibility(View.VISIBLE);
            polynomialParamView.setVisibility(View.GONE);
            modulusView.setVisibility(View.GONE);
            magnificationView.setVisibility(View.GONE);
            linearParamView.initData(sensorInfo);

            monitorTypeList.clear();
            monitorTypeList.addAll(vibratingWireMonitorTypeList);
        } else if (sensorCalculation == VmsSensorCalculation.POLYNOMIAL) {//多项式(振弦式传感器一种)
            mTvSensorCalculation.setText(calculationList.get(1));
            linearParamView.setVisibility(View.GONE);
            polynomialParamView.setVisibility(View.VISIBLE);
            modulusView.setVisibility(View.GONE);
            magnificationView.setVisibility(View.GONE);
            polynomialParamView.initData(sensorInfo);

            monitorTypeList.clear();
            monitorTypeList.addAll(vibratingWireMonitorTypeList);
        } else if (sensorCalculation == VmsSensorCalculation.MEMS) {//MEMS
            mTvSensorCalculation.setText(calculationList.get(2));
            linearParamView.setVisibility(View.GONE);
            polynomialParamView.setVisibility(View.GONE);
            modulusView.setVisibility(View.GONE);
            magnificationView.setVisibility(View.GONE);

            monitorTypeList.clear();
            monitorTypeList.addAll(digitalMonitorTypeList);
        } else if (sensorCalculation == VmsSensorCalculation.MODULUS) {//模数
            mTvSensorCalculation.setText(calculationList.get(3));
            linearParamView.setVisibility(View.GONE);
            polynomialParamView.setVisibility(View.GONE);
            modulusView.setVisibility(View.VISIBLE);
            magnificationView.setVisibility(View.GONE);
            modulusView.initData(sensorInfo);

            monitorTypeList.clear();
            monitorTypeList.addAll(modulusMonitorTypeList);
        } else if (sensorCalculation == VmsSensorCalculation.MAGNIFICATION) {//倍率
            mTvSensorCalculation.setText(calculationList.get(4));
            linearParamView.setVisibility(View.GONE);
            polynomialParamView.setVisibility(View.GONE);
            modulusView.setVisibility(View.GONE);
            magnificationView.setVisibility(View.VISIBLE);
            magnificationView.initData(sensorInfo, MonitoringType.valueByCode(sensorInfo.getName()));

            monitorTypeList.clear();
            monitorTypeList.addAll(magnificationMonitorTypeList);
        }
        //解析出传感器名称
        String monitorType = MonitoringType.valueByCode(sensorInfo.getName()).getDescription();
        mTvMonitorType.setText(monitorType);

        //解析出传感器序号
        if (sensorInfo.getName().contains("_")) {
            String[] strs = sensorInfo.getName().split("_");
            sensorSerialNumber = strs.length > 1 ? strs[1] : "";
            mEtSensorSerialNumber.setText(sensorSerialNumber);
        }

        //为0表示未接入传感器
        if (sensorInfo.getInsert().trim().equals("0")) {
            mSbSensorEnable.setCheckedImmediatelyNoEvent(false);
            contentLayout.setVisibility(View.GONE);
        } else {
            mSbSensorEnable.setCheckedImmediatelyNoEvent(true);
            contentLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setSwitchViewListener() {
        mSbSensorEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定不接入此通道传感器吗？");
                } else {
                    contentLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * 关闭SwitchButton
     */
    private void showCloseSwitchButtonDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title("温馨提示：")
                .content(content)
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        disableSensor();
                        contentLayout.setVisibility(View.GONE);
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        mSbSensorEnable.setCheckedImmediatelyNoEvent(true);
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    /**
     * 不接入传感器</br>
     */
    private void disableSensor() {
        SetVmsTerminalSensorParamsEntity entity = new SetVmsTerminalSensorParamsEntity();
        entity.setSn(sensorInfo.getSn());
        entity.setChannel(sensorInfo.getChannel());
        entity.setInsert("0");

        isSaveParamOperation = false;
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_SET_TERMINAL_CHL, entity);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    @OnClick({R.id.calculationLayout, R.id.sensorNameLayout, R.id.btn_confirm})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.calculationLayout) {
            showcCalculationChooseDialog();
        } else if (id == R.id.sensorNameLayout) {
            showcSensorNameChooseDialog();
        } else if (id == R.id.btn_confirm) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!checkValueIsValid()) {
                Timber.w("传感器参数存在错误!");
                return;
            }
            processSave();
        }
    }

    /**
     * 选择计算方式
     */
    private void showcCalculationChooseDialog() {
        int pos = calculationList.indexOf(String.valueOf(mTvSensorCalculation.getText()));
        pos = pos == -1 ? 0 : pos;
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", calculationList.toArray(new String[0]),
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvSensorCalculation.setText(text);
                                if (text.contains("直线式")) {
                                    sensorCalculation = VmsSensorCalculation.LINEAR;
                                    linearParamView.setVisibility(View.VISIBLE);
                                    polynomialParamView.setVisibility(View.GONE);
                                    modulusView.setVisibility(View.GONE);
                                    magnificationView.setVisibility(View.GONE);
                                    monitorTypeList.clear();
                                    monitorTypeList.addAll(vibratingWireMonitorTypeList);
                                } else if (text.contains("多项式")) {
                                    sensorCalculation = VmsSensorCalculation.POLYNOMIAL;
                                    linearParamView.setVisibility(View.GONE);
                                    polynomialParamView.setVisibility(View.VISIBLE);
                                    modulusView.setVisibility(View.GONE);
                                    magnificationView.setVisibility(View.GONE);
                                    monitorTypeList.clear();
                                    monitorTypeList.addAll(vibratingWireMonitorTypeList);
                                } else if (text.contains("MEMS")) {
                                    sensorCalculation = VmsSensorCalculation.MEMS;
                                    linearParamView.setVisibility(View.GONE);
                                    polynomialParamView.setVisibility(View.GONE);
                                    modulusView.setVisibility(View.GONE);
                                    magnificationView.setVisibility(View.GONE);
                                    monitorTypeList.clear();
                                    monitorTypeList.addAll(digitalMonitorTypeList);
                                } else if (text.contains("模数")) {
                                    sensorCalculation = VmsSensorCalculation.MODULUS;
                                    linearParamView.setVisibility(View.GONE);
                                    polynomialParamView.setVisibility(View.GONE);
                                    modulusView.setVisibility(View.VISIBLE);
                                    magnificationView.setVisibility(View.GONE);
                                    monitorTypeList.clear();
                                    monitorTypeList.addAll(modulusMonitorTypeList);
                                } else if (text.contains("倍率")) {
                                    sensorCalculation = VmsSensorCalculation.MAGNIFICATION;
                                    linearParamView.setVisibility(View.GONE);
                                    polynomialParamView.setVisibility(View.GONE);
                                    modulusView.setVisibility(View.GONE);
                                    magnificationView.setVisibility(View.VISIBLE);
                                    monitorTypeList.clear();
                                    monitorTypeList.addAll(magnificationMonitorTypeList);
                                }
                                //如果传感器类型不支持选中的计算方式，则重置等待重新选择
                                if (!monitorTypeList.contains(mTvMonitorType.getText().toString())) {
                                    mTvMonitorType.setText(monitorTypeList.get(0));
                                }
                                if (mTvSensorCalculation.getText().toString().contains("倍率")) {
                                    magnificationView.initData(sensorInfo, MonitoringType.valueByCode(sensorInfo.getName()));
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    /**
     * 选择传感器类型
     */
    private void showcSensorNameChooseDialog() {
        int pos = monitorTypeList.indexOf(String.valueOf(mTvMonitorType.getText()));
        pos = pos == -1 ? 0 : pos;
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", monitorTypeList.toArray(new String[0]),
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvMonitorType.setText(text);
                                if (mTvSensorCalculation.getText().toString().contains("倍率")) {
                                    magnificationView.initData(sensorInfo, MonitoringType.valueByDesc(text));
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    private boolean checkValueIsValid() {
        sensorSerialNumber = mEtSensorSerialNumber.getText().toString().trim();
        if (TextUtils.isEmpty(mTvMonitorType.getText())) {
            ToastUtils.show("请选择监测类型!");
            return false;
        }
        if (TextUtils.isEmpty(sensorSerialNumber)) {
            ToastUtils.show("请输入传感器序号!");
            mEtSensorSerialNumber.requestFocus();
            return false;
        }
        if (!ValidateUtil.isInteger(sensorSerialNumber) || Integer.parseInt(sensorSerialNumber) <= 0) {
            ToastUtils.show("请输入正确的传感器序号!");
            mEtSensorSerialNumber.requestFocus();
            return false;
        }
        return true;
    }

    private void processSave() {
        SetVmsTerminalSensorParamsEntity entity = new SetVmsTerminalSensorParamsEntity();
        entity.setSn(sensorInfo.getSn());
        entity.setChannel(sensorInfo.getChannel());
        entity.setInsert("1");
        entity.setType(sensorCalculation.toString());
        String sensorNameNo = MonitoringType.valueByDesc(mTvMonitorType.getText().toString()).getCode() + "_" + sensorSerialNumber;
        entity.setName(sensorNameNo);

        boolean updateDataSuccess = true;
        switch (sensorCalculation) {
            case LINEAR:
                updateDataSuccess = linearParamView.updateSensorData(entity);
                break;

            case POLYNOMIAL:
                updateDataSuccess = polynomialParamView.updateSensorData(entity);
                break;

            case MODULUS:
                updateDataSuccess = modulusView.updateSensorData(entity);
                break;

            case MAGNIFICATION:
                updateDataSuccess = magnificationView.updateSensorData(entity);
                break;
        }
        if (!updateDataSuccess) {
            Timber.w("传感器参数存在错误!");
            return;
        }
        isSaveParamOperation = true;
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_SET_TERMINAL_CHL, entity);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            dismissWaitDialog();
            showDispatchFailedDialog(cmdStr);
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
     * 指令下发失败弹框
     */
    private void showDispatchFailedDialog(String cmdStr) {
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
            case VMS_MD_SET_TERMINAL_CHL: {//设置Vms终端某个通道下传感器参数
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置参数失败!", cmdResult.getReason());
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
        if (isSaveParamOperation) {
            ToastUtils.show("保存成功");
        }
        isResultOK = true;
        vmsViewModel.setVmsRefreshTerminal(true);
    }

    private void setResult() {
        Intent intent = new Intent();
        mActivity.setResult(isResultOK ? Activity.RESULT_OK : Activity.RESULT_CANCELED, intent);
    }

    @Override
    public boolean onBackPressed() {
        setResult();
//        if (checkValueIsChange()) {
//            warnNotYetSettingBeforeLeavePage();
//            return true;
//        } else {
//            return false;
//        }
        return false;
    }

//    private boolean checkValueIsChange() {
//        if (!mSbSensorEnable.isChecked()) {
//            return false;
//        }
//        if (enableButtonOriginalState != mSbSensorEnable.isChecked()) {
//            return true;
//        }
//        if (calculationPosOld != calculationPos) {
//            return true;
//        }
//        if (sensorNamePosOld != sensorNamePos) {
//            return true;
//        }
//        if (sensorSerialNumber != null && !sensorSerialNumber.equals(mEtSensorSerialNumber.getText().toString().trim())) {
//            return true;
//        }
////        if (threshold != null && !threshold.equals(mEtThreshold.getText().toString().trim())) {
////            return true;
////        }
//
//        if (sensorCalculation == VmsSensorCalculation.LINEAR) {
//            return linearParamView.checkValueIsChange();
//        } else if (sensorCalculation == VmsSensorCalculation.POLYNOMIAL) {
//            return polynomialParamView.checkValueIsChange();
//        } else if (sensorCalculation == VmsSensorCalculation.MODULUS) {
//            return modulusView.checkValueIsChange();
//        } else if (sensorCalculation == VmsSensorCalculation.MAGNIFICATION) {
//            return magnificationView.checkValueIsChange();
//        }
//        return false;
//    }
}
