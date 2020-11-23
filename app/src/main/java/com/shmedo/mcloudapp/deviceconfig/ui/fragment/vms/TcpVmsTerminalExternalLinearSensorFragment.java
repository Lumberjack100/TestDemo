package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
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
 * 描述：      Vms终端直线式扩展传感器配置页面
 */
public class TcpVmsTerminalExternalLinearSensorFragment extends BaseTcpConnectFragment {
    @BindView(R.id.temperatureCoefficient)
    EditText mEtTemperatureCoefficient;//温度修正系数B

    @BindView(R.id.sensitivityCoefficient)
    EditText mEtSensitivityCoefficient;//灵敏度K

    @BindView(R.id.initialTemperature)
    EditText mEtInitialTemperature;//初始温度T

    @BindView(R.id.initialModulus)
    EditText mEtInitModulus;//初始模数F

    @BindView(R.id.correctValue)
    EditText mEtCorrectValue;//修正值

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    private String temperatureCoefficient;
    private String sensitivityCoefficient;
    private String initialTemperature;
    private String initialModulus;
    private String correctValue;


    private TerminalSensorInfo sensorInfo;


    public static TcpVmsTerminalExternalLinearSensorFragment newInstance(TerminalSensorInfo sensorInfo) {
        TcpVmsTerminalExternalLinearSensorFragment fragment = new TcpVmsTerminalExternalLinearSensorFragment();
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
        return R.layout.tcp_vms_terminal_external_linear_sensor_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel

        setView();
        initViewData();
    }

    private void setView() {
        mEtTemperatureCoefficient.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        mEtSensitivityCoefficient.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        mEtInitialTemperature.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        mEtInitModulus.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
    }

    private void initViewData() {
        if (sensorInfo == null) {
            sensorInfo = new TerminalSensorInfo();
            return;
        }
        temperatureCoefficient = sensorInfo.getParamb();
        sensitivityCoefficient = sensorInfo.getParamk();
        initialTemperature = sensorInfo.getParamt();
        initialModulus = sensorInfo.getParamf();
        correctValue = sensorInfo.getCorral();

        mEtTemperatureCoefficient.setText(temperatureCoefficient);
        mEtSensitivityCoefficient.setText(sensitivityCoefficient);
        mEtInitialTemperature.setText(initialTemperature);
        mEtInitModulus.setText(initialModulus);
        mEtCorrectValue.setText(correctValue);
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
        entity.setParamb(temperatureCoefficient);
        entity.setParamk(sensitivityCoefficient);
        entity.setParamt(initialTemperature);
        entity.setParamf(initialModulus);
        entity.setCorral(correctValue);

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
        if (temperatureCoefficient != null && !temperatureCoefficient.equals(mEtTemperatureCoefficient.getText().toString().trim())) {
            return true;
        }
        if (sensitivityCoefficient != null && !sensitivityCoefficient.equals(mEtSensitivityCoefficient.getText().toString().trim())) {
            return true;
        }
        if (initialTemperature != null && !initialTemperature.equals(mEtInitialTemperature.getText().toString().trim())) {
            return true;
        }
        if (initialModulus != null && !initialModulus.equals(mEtInitModulus.getText().toString().trim())) {
            return true;
        }
        if (correctValue != null && !correctValue.equals(mEtCorrectValue.getText().toString().trim())) {
            return true;
        }

        return false;
    }

}