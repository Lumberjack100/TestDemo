package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.guyj.BidirectionalSeekBar;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeLockedRotorDetectionEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeLockedRotorDetectionInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/2/21 <br/>
 * 描述：    ADME 蓝牙模式下电机运动堵转缓停参数配置页面
 */
public class BleAdmeLockedRotorDetectionFragment extends BaseUSRBleIotCommunicateFragment {
    @BindView(R.id.decentralizedEnableSBtn)
    SwitchButton mSbDecentralizedEnable;

    @BindView(R.id.et_decentralized_pulses_per_unit_time)
    ClearEditText mEtDecentralizedPulsesPerUnitTime;//单位时间脉冲数

    @BindView(R.id.et_decentralized_pulse_detection_time)
    ClearEditText mEtDecentralizedPulseDetectionTime;//检测判断时间

    @BindView(R.id.tv_decentralized_stall_detection_interval)
    TextView mTvDecentralizedStallDetectionInterval;

    @BindView(R.id.bSeekBar_decentralized_stall_detection_interval)
    BidirectionalSeekBar seekBarDecentralizedStallDetectionInterval;

    @BindView(R.id.et_decentralized_torque_stall_threshold)
    ClearEditText mEtDecentralizedTorqueStallThreshold;

    @BindView(R.id.et_decentralized_torque_detection_time)
    ClearEditText mEtDecentralizedTorqueDetectionTime;

    @BindView(R.id.tv_decentralized_slow_stop_interval)
    TextView mTvDecentralizedSlowStopInterval;

    @BindView(R.id.bSeekBar_decentralized_slow_stop_interval)
    BidirectionalSeekBar seekBarDecentralizedSlowStopInterval;

    @BindView(R.id.pullUpEnableSBtn)
    SwitchButton mSbPullUpEnable;

    @BindView(R.id.et_pull_up_torque_stall_threshold)
    ClearEditText mEtPullUpTorqueStallThreshold;

    @BindView(R.id.et_pull_up_torque_detection_time)
    ClearEditText mEtPullUpTorqueDetectionTime;

    @BindView(R.id.tv_pull_up_slow_stop_interval)
    TextView mTvPullUpSlowStopInterval;

    @BindView(R.id.bSeekBar_pull_up_slow_stop_interval)
    BidirectionalSeekBar seekBarPullUpSlowStopInterval;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    @BindView(R.id.decentralizedChildLayout)
    ViewGroup decentralizedChildLayout;

    @BindView(R.id.pullUpChildLayout)
    ViewGroup pullUpChildLayout;

    @BindView(R.id.maskLayerLayout)
    ViewGroup maskLayerLayout;

    private AdmeLockedRotorDetectionInfo lockedRotorDetectionInfo;

    private String decentralizedPulsesPerUnitTime;//下放单位时间脉冲数
    private String decentralizedPulseDetectionTime;//下放脉冲检测判断时间
    private String decentralizedPulseDetectionStart;//堵转检测起点
    private String decentralizedPulseDetectionEnd;//堵转检测终点
    private String decentralizedTorqueStallThreshold;//下放力矩堵转阈值
    private String decentralizedTorqueDetectionTime;//下放力矩检测判断时间
    private String decentralizedTorqueDetectionStart;//下放力矩检测起点
    private String decentralizedTorqueDetectionEnd;//下放力矩检测终点
    private String pullUpTorqueStallThreshold;//上拉力矩堵转阈值
    private String pullUpTorqueDetectionTime;//上拉力矩检测判断时间
    private String pullUpTorqueDetectionStart;//上拉力矩检测起点
    private String pullUpTorqueDetectionEnd;//上拉力矩检测终点

    private DecimalFormat decimalFormat = new DecimalFormat();


    public static BleAdmeLockedRotorDetectionFragment newInstance() {
        return new BleAdmeLockedRotorDetectionFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_locked_rotor_detection_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
        setSwitchViewListener();
        setSeekBarListener();
        queryParamInfo();
        //TODO 设备处于自动监测模式时，不可编辑参数(后期还要考虑点击编辑按钮时的页面状态切换)
        if (admeViewModel.deviceMode == 0) {
            configPageViewModel.configPageEditableChanged.setValue(true);
        } else {
            configPageViewModel.configPageEditableChanged.setValue(false);
        }
    }

    private void setView() {
        mEtDecentralizedPulsesPerUnitTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        mEtDecentralizedPulseDetectionTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        mEtDecentralizedTorqueStallThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtDecentralizedTorqueDetectionTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        mEtPullUpTorqueStallThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtPullUpTorqueDetectionTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
    }

    private void setSwitchViewListener() {
        mSbDecentralizedEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    decentralizedChildLayout.setVisibility(View.GONE);
                } else {
                    decentralizedChildLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        mSbPullUpEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    pullUpChildLayout.setVisibility(View.GONE);
                } else {
                    pullUpChildLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setSeekBarListener() {
        seekBarDecentralizedStallDetectionInterval.setOnSeekBarChangeListener(new BidirectionalSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(int leftProgress, int rightProgress) {
                decentralizedPulseDetectionStart = String.valueOf(leftProgress);
                decentralizedPulseDetectionEnd = String.valueOf(rightProgress);

                mTvDecentralizedStallDetectionInterval.setText(leftProgress + "%-" + rightProgress + "%");
            }
        });

        seekBarDecentralizedSlowStopInterval.setOnSeekBarChangeListener(new BidirectionalSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(int leftProgress, int rightProgress) {
                decentralizedTorqueDetectionStart = String.valueOf(leftProgress);
                decentralizedTorqueDetectionEnd = String.valueOf(rightProgress);

                mTvDecentralizedSlowStopInterval.setText(leftProgress + "%-" + rightProgress + "%");
            }
        });

        seekBarPullUpSlowStopInterval.setOnSeekBarChangeListener(new BidirectionalSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(int leftProgress, int rightProgress) {
                pullUpTorqueDetectionStart = String.valueOf(leftProgress);
                pullUpTorqueDetectionEnd = String.valueOf(rightProgress);

                mTvPullUpSlowStopInterval.setText(leftProgress + "%-" + rightProgress + "%");
            }
        });
    }

    /**
     * 获取参数
     */
    private void queryParamInfo() {
        errMsg = "查询数据超时,请稍后尝试";
        startProgressRunnable("加载中...", WRITE_TIME_OUT_SECOND);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_LOCKED_ROTOR_DETECTION);
        sendCommand(command);
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.btn_confirm) {
            KeyBordUtils.hideSoftKeyboard(view);
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            if (!checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            processSave();
        }
    }

    private boolean checkValueIsValid() {
        decentralizedPulsesPerUnitTime = mEtDecentralizedPulsesPerUnitTime.getText().toString().trim();
        decentralizedPulseDetectionTime = mEtDecentralizedPulseDetectionTime.getText().toString().trim();
        decentralizedTorqueStallThreshold = mEtDecentralizedTorqueStallThreshold.getText().toString().trim();
        decentralizedTorqueDetectionTime = mEtDecentralizedTorqueDetectionTime.getText().toString().trim();
        pullUpTorqueStallThreshold = mEtPullUpTorqueStallThreshold.getText().toString().trim();
        pullUpTorqueDetectionTime = mEtPullUpTorqueDetectionTime.getText().toString().trim();

        if (mSbDecentralizedEnable.isChecked()) {
            if (TextUtils.isEmpty(decentralizedPulsesPerUnitTime)) {
                ToastUtils.show("请输入下放单位时间脉冲数!");
                mEtDecentralizedPulsesPerUnitTime.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(decentralizedPulsesPerUnitTime);
                if (value < 1) {
                    ToastUtils.show("请输入正确的下放单位时间脉冲数!");
                    mEtDecentralizedPulsesPerUnitTime.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的下放单位时间脉冲数!");
                mEtDecentralizedPulsesPerUnitTime.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(decentralizedPulseDetectionTime)) {
                ToastUtils.show("请输入下放脉冲检测判断时间!");
                mEtDecentralizedPulseDetectionTime.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(decentralizedPulseDetectionTime);
                if (value > 1) {
                    ToastUtils.show("请输入正确的下放脉冲检测判断时间!");
                    mEtDecentralizedPulseDetectionTime.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的下放脉冲检测判断时间!");
                mEtDecentralizedPulseDetectionTime.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(decentralizedTorqueStallThreshold)) {
                ToastUtils.show("请输入下放力矩堵转阈值!");
                mEtDecentralizedTorqueStallThreshold.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(decentralizedTorqueStallThreshold);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的下放力矩堵转阈值!");
                mEtDecentralizedTorqueStallThreshold.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(decentralizedTorqueDetectionTime)) {
                ToastUtils.show("请输入下放力矩检测判断时间!");
                mEtDecentralizedTorqueDetectionTime.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(decentralizedTorqueDetectionTime);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的下放力矩检测判断时间!");
                mEtDecentralizedTorqueDetectionTime.requestFocus();
                return false;
            }
        }

        if (mSbPullUpEnable.isChecked()) {
            if (TextUtils.isEmpty(pullUpTorqueStallThreshold)) {
                ToastUtils.show("请输入上拉力矩堵转阈值!");
                mEtPullUpTorqueStallThreshold.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(pullUpTorqueStallThreshold);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的上拉力矩堵转阈值!");
                mEtPullUpTorqueStallThreshold.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(pullUpTorqueDetectionTime)) {
                ToastUtils.show("请输入上拉力矩检测判断时间!");
                mEtPullUpTorqueDetectionTime.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(pullUpTorqueDetectionTime);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的上拉力矩检测判断时间!");
                mEtPullUpTorqueDetectionTime.requestFocus();
                return false;
            }
        }
        return true;
    }

    private void processSave() {
        try {
            AdmeLockedRotorDetectionEntity entity = new AdmeLockedRotorDetectionEntity();
            if (!mSbDecentralizedEnable.isChecked()) {
                entity.setLowtbtss("0");
            } else {
                entity.setLowtbtss("1");
                entity.setNumpput(decentralizedPulsesPerUnitTime);
                entity.setPdajtime(decentralizedPulseDetectionTime);
                entity.setDetintiona(decentralizedPulseDetectionStart);
                entity.setDetintionb(decentralizedPulseDetectionEnd);
                entity.setLowtorblothr(decentralizedTorqueStallThreshold);
                entity.setLowtordetime(decentralizedTorqueDetectionTime);
                entity.setLowsusrana(decentralizedTorqueDetectionStart);
                entity.setLowsusranb(decentralizedTorqueDetectionEnd);
            }

            if (!mSbPullUpEnable.isChecked()) {
                entity.setUptbtss("0");
            } else {
                entity.setUptbtss("1");
                entity.setUptorblothr(pullUpTorqueStallThreshold);
                entity.setUptordetime(pullUpTorqueDetectionTime);
                entity.setUpsusrana(pullUpTorqueDetectionStart);
                entity.setUpsusranb(pullUpTorqueDetectionEnd);
            }

            errMsg = "发送指令超时,请稍后尝试";
            startProgressRunnable("正在发送配置指令...", WRITE_TIME_OUT_SECOND);
            mBtnSave.setEnabled(false);
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_LOCKED_ROTOR_DETECTION, entity);
            sendCommand(command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void doProgressRun() {
        super.doProgressRun();
        mBtnSave.setEnabled(true);
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case ADME_MD_GET_LOCKED_ROTOR_DETECTION: {
                stopProgressRunnable();
                IOTCommandResult<AdmeLockedRotorDetectionInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询堵转参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                lockedRotorDetectionInfo = commandResult.getResult();
                initParamConfigInfo();
            }
            break;

            case ADME_MD_SET_LOCKED_ROTOR_DETECTION: {
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    stopProgressRunnable();
                    String errMsg = String.format("%s %s", "设置堵转参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    mBtnSave.setEnabled(true);
                    return;
                }
                doAfterSetting();
            }
            break;

            case MD_SAVE_CONFIG_PARAM: {
                stopProgressRunnable();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "发送保存指令出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
            }
            ToastUtils.show("保存成功");
            break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    private void doAfterSetting() {
        if (lockedRotorDetectionInfo != null) {
            if (!mSbDecentralizedEnable.isChecked()) {
                lockedRotorDetectionInfo.setLowtbtss("0");
            } else {
                lockedRotorDetectionInfo.setLowtbtss("1");
                lockedRotorDetectionInfo.setNumpput(decentralizedPulsesPerUnitTime);
                lockedRotorDetectionInfo.setPdajtime(decentralizedPulseDetectionTime);
                lockedRotorDetectionInfo.setDetintiona(decentralizedPulseDetectionStart);
                lockedRotorDetectionInfo.setDetintionb(decentralizedPulseDetectionEnd);
                lockedRotorDetectionInfo.setLowtorblothr(decentralizedTorqueStallThreshold);
                lockedRotorDetectionInfo.setLowtordetime(decentralizedTorqueDetectionTime);
                lockedRotorDetectionInfo.setLowsusrana(decentralizedTorqueDetectionStart);
                lockedRotorDetectionInfo.setLowsusranb(decentralizedTorqueDetectionEnd);
            }

            if (!mSbPullUpEnable.isChecked()) {
                lockedRotorDetectionInfo.setUptbtss("0");
            } else {
                lockedRotorDetectionInfo.setUptbtss("1");
                lockedRotorDetectionInfo.setUptorblothr(pullUpTorqueStallThreshold);
                lockedRotorDetectionInfo.setUptordetime(pullUpTorqueDetectionTime);
                lockedRotorDetectionInfo.setUpsusrana(pullUpTorqueDetectionStart);
                lockedRotorDetectionInfo.setUpsusranb(pullUpTorqueDetectionEnd);
            }
        }
        mBtnSave.setEnabled(true);
        //TODO  打开注释，设置为浏览模式
//        configPageViewModel.configPageEditableChanged.setValue(false);

        saveConfigInfo();
    }

    private void initParamConfigInfo() {
        if (lockedRotorDetectionInfo == null) {
            Timber.e("AdmeLockedRotorDetectionInfo is Null!");
            lockedRotorDetectionInfo = new AdmeLockedRotorDetectionInfo();
            return;
        }
        decentralizedPulsesPerUnitTime = lockedRotorDetectionInfo.getNumpput().trim();
        decentralizedPulseDetectionTime = lockedRotorDetectionInfo.getPdajtime().trim();
        decentralizedPulseDetectionStart = lockedRotorDetectionInfo.getDetintiona().trim();
        decentralizedPulseDetectionEnd = lockedRotorDetectionInfo.getDetintionb().trim();
        decentralizedTorqueStallThreshold = lockedRotorDetectionInfo.getLowtorblothr().trim();
        decentralizedTorqueDetectionTime = lockedRotorDetectionInfo.getLowtordetime().trim();
        decentralizedTorqueDetectionStart = lockedRotorDetectionInfo.getLowsusrana().trim();
        decentralizedTorqueDetectionEnd = lockedRotorDetectionInfo.getLowsusranb().trim();
        pullUpTorqueStallThreshold = lockedRotorDetectionInfo.getUptorblothr().trim();
        pullUpTorqueDetectionTime = lockedRotorDetectionInfo.getUptordetime().trim();
        pullUpTorqueDetectionStart = lockedRotorDetectionInfo.getUpsusrana().trim();
        pullUpTorqueDetectionEnd = lockedRotorDetectionInfo.getUpsusranb().trim();

        if (lockedRotorDetectionInfo.getLowtbtss().equals("0")) {
            mSbDecentralizedEnable.setCheckedImmediatelyNoEvent(false);
            decentralizedChildLayout.setVisibility(View.GONE);
        } else {
            mSbDecentralizedEnable.setCheckedImmediatelyNoEvent(true);
            decentralizedChildLayout.setVisibility(View.VISIBLE);
        }

        if (lockedRotorDetectionInfo.getUptbtss().equals("0")) {
            mSbPullUpEnable.setCheckedImmediatelyNoEvent(false);
            pullUpChildLayout.setVisibility(View.GONE);
        } else {
            mSbPullUpEnable.setCheckedImmediatelyNoEvent(true);
            pullUpChildLayout.setVisibility(View.VISIBLE);
        }

        try {
            mEtDecentralizedPulsesPerUnitTime.setText(decentralizedPulsesPerUnitTime);
            decimalFormat.applyPattern("#.#");
            decentralizedPulseDetectionTime = decimalFormat.format(Double.parseDouble(decentralizedPulseDetectionTime));
            mEtDecentralizedPulseDetectionTime.setText(decentralizedPulseDetectionTime);

            decimalFormat.applyPattern("#");
            decentralizedTorqueStallThreshold = decimalFormat.format(Double.parseDouble(decentralizedTorqueStallThreshold));
            mEtDecentralizedTorqueStallThreshold.setText(decentralizedTorqueStallThreshold);
            decimalFormat.applyPattern("#.#");
            decentralizedTorqueDetectionTime = decimalFormat.format(Double.parseDouble(decentralizedTorqueDetectionTime));
            mEtDecentralizedTorqueDetectionTime.setText(decentralizedTorqueDetectionTime);

            decimalFormat.applyPattern("#");
            pullUpTorqueStallThreshold = decimalFormat.format(Double.parseDouble(pullUpTorqueStallThreshold));
            mEtPullUpTorqueStallThreshold.setText(pullUpTorqueStallThreshold);
            decimalFormat.applyPattern("#.#");
            pullUpTorqueDetectionTime = decimalFormat.format(Double.parseDouble(pullUpTorqueDetectionTime));
            mEtPullUpTorqueDetectionTime.setText(pullUpTorqueDetectionTime);

        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onEditableChanged(boolean isEditable) {
        if (isEditable) {
            mEtDecentralizedPulsesPerUnitTime.setHint("请输入");
            mEtDecentralizedPulseDetectionTime.setHint("请输入");
            mEtDecentralizedTorqueStallThreshold.setHint("请输入");
            mEtDecentralizedTorqueDetectionTime.setHint("请输入");
            mEtPullUpTorqueStallThreshold.setHint("请输入");
            mEtPullUpTorqueDetectionTime.setHint("请输入");
        } else {
            mEtDecentralizedPulsesPerUnitTime.setHint("");
            mEtDecentralizedPulseDetectionTime.setHint("");
            mEtDecentralizedTorqueStallThreshold.setHint("");
            mEtDecentralizedTorqueDetectionTime.setHint("");
            mEtPullUpTorqueStallThreshold.setHint("");
            mEtPullUpTorqueDetectionTime.setHint("");

            mEtDecentralizedPulsesPerUnitTime.clearFocus();
            mEtDecentralizedPulseDetectionTime.clearFocus();
            mEtDecentralizedTorqueStallThreshold.clearFocus();
            mEtDecentralizedTorqueDetectionTime.clearFocus();
            mEtPullUpTorqueStallThreshold.clearFocus();
            mEtPullUpTorqueDetectionTime.clearFocus();

            initParamConfigInfo();
        }
        maskLayerLayout.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        mBtnSave.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }
}