package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.entity.SetTerminalSensorParamsEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.TerminalSensorInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.BaseTcpConnectFragment;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/23 <br/>
 * 描述：      Vms终端多项式扩展传感器配置页面
 */
public class TcpVmsTerminalExternalPolynomialSensorFragment extends BaseTcpConnectFragment {
    @BindView(R.id.page1)
    ViewGroup pageOneLayout;

    @BindView(R.id.page2)
    ViewGroup pageTwoaLyout;

    @BindView(R.id.centerEnableSBtn)
    SwitchButton mSbCenterEnable;

    @BindView(R.id.polynomialRatioA)
    EditText mEtPolynomialRatioA;//多项式系数A

    @BindView(R.id.polynomialRatioB)
    EditText mEtPolynomialRatioB;//多项式系数B

    @BindView(R.id.polynomialRatioC)
    EditText mEtPolynomialRatioC;//多项式系数C

    @BindView(R.id.temperatureCoefficient)
    EditText mEtTemperatureCoefficient;//温度修正系数K

    @BindView(R.id.initialTemperature)
    EditText mEtInitialTemperature;//初始温度T

    @BindView(R.id.correctValue)
    EditText mEtCorrectValue;//修正值M

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    private String polynomialRatioA;
    private String polynomialRatioB;
    private String polynomialRatioC;
    private String temperatureCoefficient;
    private String initialTemperature;
    private String correctValue;

    private TerminalSensorInfo sensorInfo;


    public static TcpVmsTerminalExternalPolynomialSensorFragment newInstance(TerminalSensorInfo sensorInfo) {
        TcpVmsTerminalExternalPolynomialSensorFragment fragment = new TcpVmsTerminalExternalPolynomialSensorFragment();
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
        return R.layout.fragment_tcp_vms_terminal_external_polynomial_sensor;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setSwitchViewListener();
        initViewData();
    }

    @Override
    protected void initView() {
        mEtPolynomialRatioA.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        mEtPolynomialRatioB.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        mEtPolynomialRatioC.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        mEtTemperatureCoefficient.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        mEtInitialTemperature.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
    }

    private void setSwitchViewListener() {
        mSbCenterEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!tcpShareViewModel.getConnectStatus()) {
                    ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
                    mSbCenterEnable.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }

                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定不接入此通道传感器吗？");
                } else {
                    enableSensor();
                    pageTwoaLyout.setVisibility(View.GONE);
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
                        pageTwoaLyout.setVisibility(View.VISIBLE);
                        pageTwoaLyout.setOnClickListener(null);
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        mSbCenterEnable.setCheckedImmediatelyNoEvent(true);
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    private void initViewData() {
        if (sensorInfo == null) {
            sensorInfo = new TerminalSensorInfo();
            return;
        }
        polynomialRatioA = sensorInfo.getParama();
        polynomialRatioB = sensorInfo.getParamb();
        polynomialRatioC = sensorInfo.getParamc();
        temperatureCoefficient = sensorInfo.getParamk();
        initialTemperature = sensorInfo.getParamt();
        correctValue = sensorInfo.getParamm();

        mEtPolynomialRatioA.setText(polynomialRatioA);
        mEtPolynomialRatioB.setText(polynomialRatioB);
        mEtPolynomialRatioC.setText(polynomialRatioC);
        mEtTemperatureCoefficient.setText(temperatureCoefficient);
        mEtInitialTemperature.setText(initialTemperature);
        mEtCorrectValue.setText(correctValue);

        //为0表示未接入传感器
        if (sensorInfo.getInsert().trim().equals("0")) {
            mSbCenterEnable.setCheckedImmediatelyNoEvent(false);
            pageTwoaLyout.setVisibility(View.VISIBLE);
            pageTwoaLyout.setOnClickListener(null);
        } else {
            mSbCenterEnable.setCheckedImmediatelyNoEvent(true);
            pageTwoaLyout.setVisibility(View.GONE);
        }
    }

    private void enableSensor() {
        SetTerminalSensorParamsEntity entity = new SetTerminalSensorParamsEntity();
        entity.setSn(sensorInfo.getSn());
        entity.setChannel(sensorInfo.getChannel());
        entity.setInsert("1");

        mBtnSave.setEnabled(false);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_SET_TERMINAL_CHL, entity);
        sendCommand(command);
    }

    /**
     * 不接入传感器</br>
     */
    private void disableSensor() {
        SetTerminalSensorParamsEntity entity = new SetTerminalSensorParamsEntity();
        entity.setSn(sensorInfo.getSn());
        entity.setChannel(sensorInfo.getChannel());
        entity.setInsert("0");

        mBtnSave.setEnabled(false);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_SET_TERMINAL_CHL, entity);
        sendCommand(command);
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View view) {
        if (view.getId() == R.id.btn_confirm) {
            KeyBordUtils.hideSoftKeyboard(view);

            if (!tcpShareViewModel.getConnectStatus()) {
                ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
                return;
            }

            if (!checkValueIsValid()) {
                Timber.w("通道参数存在错误!");
                return;
            }

            processSave();
        }
    }

    private boolean checkValueIsValid() {

        return true;
    }

    private void processSave() {
        SetTerminalSensorParamsEntity entity = new SetTerminalSensorParamsEntity();
        entity.setSn(sensorInfo.getSn());
        entity.setChannel(sensorInfo.getChannel());
        entity.setParama(polynomialRatioA);
        entity.setParamb(polynomialRatioB);
        entity.setParamc(polynomialRatioC);
        entity.setParamk(temperatureCoefficient);
        entity.setParamt(initialTemperature);
        entity.setParamm(correctValue);

        mBtnSave.setEnabled(false);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_SET_TERMINAL_CHL, entity);
        sendCommand(command);
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case MD_SET_TERMINAL_CHL: {//设置Vms终端某个通道下传感器参数
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = "设置参数失败!";
                    Timber.e("%s%s", errMsg, cmdResult.getReason());
                    ToastUtils.show(errMsg);
                    mBtnSave.setEnabled(true);
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
        ToastUtils.show("设置成功");
        mBtnSave.setEnabled(true);
    }

    @Override
    public boolean onBackPressed() {
        if (tcpShareViewModel.getConnectStatus()) {
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
        if (!mSbCenterEnable.isChecked()) {
            return false;
        }

        if (polynomialRatioA != null && !polynomialRatioA.equals(mEtPolynomialRatioA.getText().toString().trim())) {
            return true;
        }
        if (polynomialRatioB != null && !polynomialRatioB.equals(mEtPolynomialRatioB.getText().toString().trim())) {
            return true;
        }
        if (polynomialRatioC != null && !polynomialRatioC.equals(mEtPolynomialRatioC.getText().toString().trim())) {
            return true;
        }
        if (temperatureCoefficient != null && !temperatureCoefficient.equals(mEtTemperatureCoefficient.getText().toString().trim())) {
            return true;
        }
        if (initialTemperature != null && !initialTemperature.equals(mEtInitialTemperature.getText().toString().trim())) {
            return true;
        }
        if (correctValue != null && !correctValue.equals(mEtCorrectValue.getText().toString().trim())) {
            return true;
        }

        return false;
    }
}