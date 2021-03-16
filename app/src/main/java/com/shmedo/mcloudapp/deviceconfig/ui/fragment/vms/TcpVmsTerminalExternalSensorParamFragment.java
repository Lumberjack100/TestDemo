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
import com.shmedo.configlibrary.iot.enums.VmsSensorCalculation;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalSensorInfo;
import com.shmedo.configlibrary.iot.utils.IOTSensorUtil;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.vms.LinearParamView;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.vms.PolynomialParamView;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/27 <br/>
 * 描述：      Vms终端扩展传感器配置页面
 */
public class TcpVmsTerminalExternalSensorParamFragment extends BaseVmsTcpCommunicateFragment {
    @BindView(R.id.contentLayout)
    ViewGroup contentLayout;

    @BindView(R.id.sensorEnableSBtn)
    SwitchButton mSbSensorEnable;

    @BindView(R.id.tv_sensor_calculation)
    TextView mTvSensorCalculation;//计算方式

    @BindView(R.id.tv_sensor_name)
    TextView mTvSensorName;//传感器名称

    @BindView(R.id.sensorSerialNumber)
    EditText mEtSensorSerialNumber;//传感器序号

    @BindView(R.id.linearParamView)
    LinearParamView linearParamView;//直线式参数

    @BindView(R.id.polynomialParamView)
    PolynomialParamView polynomialParamView;//多项式参数

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    private VmsTerminalSensorInfo sensorInfo;

    private List<String> calculationList = Arrays.asList("直线式", "多项式", "MEMS");
    private List<String> sensorNameList = new ArrayList<>();
    private List<String> vibratingWireSensorNameList = Arrays.asList("裂缝计", "轴力计", "水压力计", "水位计", "渗压计");
    private List<String> digitalSensorNameList = Arrays.asList("加速度计");

    private VmsSensorCalculation sensorCalculation;
    private String sensorName;
    private String sensorSerialNumber;//传感器序号
    private int calculationPos;//计算方式索引
    private int sensorNamePos;// 传感器名称索引
    private int calculationPosOld;//计算方式索引
    private int sensorNamePosOld;// 传感器名称索引

    private boolean enableButtonOriginalState;//数据中心开关初始状态，用于判断开关是否有打开后没有设置参数就返回
    private boolean isSaveParamOperation = false;//判断当前是保存参数操作，还是关闭数据中心操作
    private boolean isResultOK = false;//返回的结果是否是 Activity.RESULT_OK


    public static TcpVmsTerminalExternalSensorParamFragment newInstance(VmsTerminalSensorInfo sensorInfo) {
        TcpVmsTerminalExternalSensorParamFragment fragment = new TcpVmsTerminalExternalSensorParamFragment();
        Bundle args = new Bundle();
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
            calculationPosOld = 0;
            calculationPos = 0;
            mTvSensorCalculation.setText(calculationList.get(0));
            linearParamView.setVisibility(View.VISIBLE);
            polynomialParamView.setVisibility(View.GONE);
            linearParamView.initData(sensorInfo);

            sensorNameList.clear();
            sensorNameList.addAll(vibratingWireSensorNameList);
        } else if (sensorCalculation == VmsSensorCalculation.POLYNOMIAL) {//多项式(振弦式传感器一种)
            calculationPosOld = 1;
            calculationPos = 1;
            mTvSensorCalculation.setText(calculationList.get(1));
            linearParamView.setVisibility(View.GONE);
            polynomialParamView.setVisibility(View.VISIBLE);
            polynomialParamView.initData(sensorInfo);

            sensorNameList.clear();
            sensorNameList.addAll(vibratingWireSensorNameList);
        } else if (sensorCalculation == VmsSensorCalculation.MEMS) {//数字式
            calculationPosOld = 2;
            calculationPos = 2;
            mTvSensorCalculation.setText(calculationList.get(2));
            linearParamView.setVisibility(View.GONE);
            polynomialParamView.setVisibility(View.GONE);

            sensorNameList.clear();
            sensorNameList.addAll(digitalSensorNameList);
        }
        //解析出传感器名称
        sensorName = IOTSensorUtil.getInstance().getSensorNameByTypeCode(sensorInfo.getName());
        sensorNamePosOld = sensorNameList.indexOf(sensorName);
        sensorNamePos = sensorNamePosOld;
        mTvSensorName.setText(sensorName);

        //解析出传感器序号
        if (sensorInfo.getName().contains("_")) {
            String[] strs = sensorInfo.getName().split("_");
            sensorSerialNumber = strs.length > 1 ? strs[1] : "";
            mEtSensorSerialNumber.setText(sensorSerialNumber);
        }

        //为0表示未接入传感器
        if (sensorInfo.getInsert().trim().equals("0")) {
            enableButtonOriginalState = false;
            mSbSensorEnable.setCheckedImmediatelyNoEvent(false);
            contentLayout.setVisibility(View.GONE);
        } else {
            enableButtonOriginalState = true;
            mSbSensorEnable.setCheckedImmediatelyNoEvent(true);
            contentLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setSwitchViewListener() {
        mSbSensorEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!tcpViewModel.getConnectStatus()) {
                    ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
                    mSbSensorEnable.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
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
                        enableButtonOriginalState = mSbSensorEnable.isChecked();
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
        sendCommand(command);
    }

    @OnClick({R.id.calculationLayout, R.id.sensorNameLayout, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.calculationLayout) {
            showcCalculationChooseDialog();
        } else if (id == R.id.sensorNameLayout) {
            showcSensorNameChooseDialog();
        } else if (id == R.id.btn_confirm) {
            KeyBordUtils.hideSoftKeyboard(view);
            if (!tcpViewModel.getConnectStatus()) {
                ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
                return;
            }
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
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", calculationList.toArray(new String[calculationList.size()]),
                        null, calculationPos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                calculationPos = position;
                                mTvSensorCalculation.setText(text);
                                if (text.contains("直线式")) {
                                    sensorCalculation = VmsSensorCalculation.LINEAR;
                                    linearParamView.setVisibility(View.VISIBLE);
                                    polynomialParamView.setVisibility(View.GONE);
                                    sensorNameList.clear();
                                    sensorNameList.addAll(vibratingWireSensorNameList);
                                } else if (text.contains("多项式")) {
                                    sensorCalculation = VmsSensorCalculation.POLYNOMIAL;
                                    linearParamView.setVisibility(View.GONE);
                                    polynomialParamView.setVisibility(View.VISIBLE);
                                    sensorNameList.clear();
                                    sensorNameList.addAll(vibratingWireSensorNameList);
                                } else if (text.contains("MEMS")) {
                                    sensorCalculation = VmsSensorCalculation.MEMS;
                                    linearParamView.setVisibility(View.GONE);
                                    polynomialParamView.setVisibility(View.GONE);
                                    sensorNameList.clear();
                                    sensorNameList.addAll(digitalSensorNameList);
                                }

                                //如果传感器类型不支持选中的计算方式，则重置等待重新选择
                                if (!sensorNameList.contains(mTvSensorName.getText().toString())) {
                                    sensorNamePosOld = 0;
                                    sensorNamePos = sensorNamePosOld;
                                    sensorName = sensorNameList.get(0);
                                    mTvSensorName.setText(sensorName);
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    /**
     * 选择传感器类型
     */
    private void showcSensorNameChooseDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", sensorNameList.toArray(new String[sensorNameList.size()]),
                        null, sensorNamePos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                sensorNamePos = position;
                                sensorName = text;
                                mTvSensorName.setText(text);
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    private boolean checkValueIsValid() {
        sensorSerialNumber = mEtSensorSerialNumber.getText().toString().trim();

        if (TextUtils.isEmpty(mTvSensorName.getText())) {
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
        boolean updateDataSuccess = true;
        switch (sensorCalculation) {
            case LINEAR:
                updateDataSuccess = linearParamView.updateSensorData(entity);
                break;

            case POLYNOMIAL:
                updateDataSuccess = polynomialParamView.updateSensorData(entity);
                break;
        }
        if (!updateDataSuccess) {
            Timber.w("传感器参数存在错误!");
            return;
        }
        entity.setSn(sensorInfo.getSn());
        entity.setChannel(sensorInfo.getChannel());
        entity.setInsert("1");
        entity.setType(sensorCalculation.toString());
        String sensorNameNo = IOTSensorUtil.getInstance().getSensorTypeCodeByName(sensorName) + "_" + sensorSerialNumber;
        entity.setName(sensorNameNo);

        enableButtonOriginalState = mSbSensorEnable.isChecked();
        isSaveParamOperation = true;
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_SET_TERMINAL_CHL, entity);
        sendCommand(command);
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
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
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    private void doAfterSetting() {
        if (isSaveParamOperation) {
            ToastUtils.show("设置成功");
        }
        calculationPosOld = calculationPos;
        sensorNamePosOld = sensorNamePos;
        isResultOK = true;
    }

    private void setResult() {
        Intent intent = new Intent();
        mActivity.setResult(isResultOK ? Activity.RESULT_OK : Activity.RESULT_CANCELED, intent);
    }

    @Override
    public boolean onBackPressed() {
        setResult();
        if (tcpViewModel.getConnectStatus()) {
            if (checkValueIsChange()) {
                warnNotYetSettingBeforeLeavePage();
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    private boolean checkValueIsChange() {
        if (!mSbSensorEnable.isChecked()) {
            return false;
        }
        if (enableButtonOriginalState != mSbSensorEnable.isChecked()) {
            return true;
        }
        if (calculationPosOld != calculationPos) {
            return true;
        }
        if (sensorNamePosOld != sensorNamePos) {
            return true;
        }
        if (sensorSerialNumber != null && !sensorSerialNumber.equals(mEtSensorSerialNumber.getText().toString().trim())) {
            return true;
        }

        if (sensorCalculation == VmsSensorCalculation.LINEAR) {
            return linearParamView.checkValueIsChange();
        } else if (sensorCalculation == VmsSensorCalculation.POLYNOMIAL) {
            return polynomialParamView.checkValueIsChange();
        }
        return false;
    }
}