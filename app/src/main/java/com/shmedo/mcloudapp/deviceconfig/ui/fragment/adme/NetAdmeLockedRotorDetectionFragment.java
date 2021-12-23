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

import com.hjq.toast.ToastUtils;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
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
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/23 <br/>
 * 描述：      ADME 4g模式下电机运动堵转缓停参数配置页面
 */
public class NetAdmeLockedRotorDetectionFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.decentralizedEnableSBtn)
    SwitchButton mSbDecentralizedEnable;

    @BindView(R.id.et_decentralized_pulses_per_unit_time)
    ClearEditText mEtDecentralizedPulsesPerUnitTime;//单位时间脉冲数

    @BindView(R.id.et_decentralized_pulse_detection_time)
    ClearEditText mEtDecentralizedPulseDetectionTime;//检测判断时间

    @BindView(R.id.tv_decentralized_stall_detection_interval)
    TextView mTvDecentralizedStallDetectionInterval;

    @BindView(R.id.seekBar_decentralized_stall_detection_interval)
    RangeSeekBar seekBarDecentralizedStallDetectionInterval;

    @BindView(R.id.et_decentralized_torque_stall_threshold)
    ClearEditText mEtDecentralizedTorqueStallThreshold;

    @BindView(R.id.et_decentralized_torque_detection_time)
    ClearEditText mEtDecentralizedTorqueDetectionTime;

    @BindView(R.id.tv_decentralized_slow_stop_interval)
    TextView mTvDecentralizedSlowStopInterval;

    @BindView(R.id.seekBar_decentralized_slow_stop_interval)
    RangeSeekBar seekBarDecentralizedSlowStopInterval;

    @BindView(R.id.pullUpEnableSBtn)
    SwitchButton mSbPullUpEnable;

    @BindView(R.id.et_pull_up_torque_stall_threshold)
    ClearEditText mEtPullUpTorqueStallThreshold;

    @BindView(R.id.et_pull_up_torque_detection_time)
    ClearEditText mEtPullUpTorqueDetectionTime;

    @BindView(R.id.tv_pull_up_slow_stop_interval)
    TextView mTvPullUpSlowStopInterval;

    @BindView(R.id.seekBar_pull_up_slow_stop_interval)
    RangeSeekBar seekBarPullUpSlowStopInterval;

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

    public static NetAdmeLockedRotorDetectionFragment newInstance(DeviceInfo deviceInfo) {
        NetAdmeLockedRotorDetectionFragment fragment = new NetAdmeLockedRotorDetectionFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.adme_locked_rotor_detection_fragment;
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
        mEtDecentralizedPulsesPerUnitTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtDecentralizedPulsesPerUnitTime.setHint("[1,10000]");

        mEtDecentralizedPulseDetectionTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        mEtDecentralizedPulseDetectionTime.setHint("[0.1,10.0]");

        mEtDecentralizedTorqueStallThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtDecentralizedTorqueStallThreshold.setHint("[0.00,2.00]");

        mEtDecentralizedTorqueDetectionTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        mEtDecentralizedTorqueDetectionTime.setHint("[0.01,5.00]");

        mEtPullUpTorqueStallThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtPullUpTorqueStallThreshold.setHint("[1.00,6.00]");

        mEtPullUpTorqueDetectionTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        mEtPullUpTorqueDetectionTime.setHint("[0.01,5.00]");
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
        seekBarDecentralizedStallDetectionInterval.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                DecimalFormat indicatorTextDecimalFormat = new DecimalFormat("0");
                decentralizedPulseDetectionStart = indicatorTextDecimalFormat.format(leftValue);
                decentralizedPulseDetectionEnd = indicatorTextDecimalFormat.format(rightValue);
                mTvDecentralizedStallDetectionInterval.setText(String.format("%s%%-%s%%", indicatorTextDecimalFormat.format(leftValue), indicatorTextDecimalFormat.format(rightValue)));
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
                //start tracking touch
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
                //stop tracking touch
                float rightValue = view.getRightSeekBar().getProgress();
                float torqueLeftValue = seekBarDecentralizedSlowStopInterval.getLeftSeekBar().getProgress();
                if (rightValue >= torqueLeftValue) {
                    seekBarDecentralizedSlowStopInterval.setProgress(rightValue + 1);
                }
            }
        });

        seekBarDecentralizedSlowStopInterval.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                DecimalFormat indicatorTextDecimalFormat = new DecimalFormat("0");
                decentralizedTorqueDetectionStart = indicatorTextDecimalFormat.format(leftValue);
                decentralizedTorqueDetectionEnd = "100";
                mTvDecentralizedSlowStopInterval.setText(String.format("%s%%-100%%", indicatorTextDecimalFormat.format(leftValue)));
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
                //start tracking touch
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
                //stop tracking touch
                float torqueLeftValue = view.getLeftSeekBar().getProgress();
                float leftValue = seekBarDecentralizedStallDetectionInterval.getLeftSeekBar().getProgress();
                float rightValue = seekBarDecentralizedStallDetectionInterval.getRightSeekBar().getProgress();
                if (torqueLeftValue <= rightValue) {
                    if (torqueLeftValue <= 1) {
                        seekBarDecentralizedStallDetectionInterval.setProgress(0, 0);
                    } else {
                        seekBarDecentralizedStallDetectionInterval.setProgress(0, torqueLeftValue - 1);
                    }
                }
            }
        });

        seekBarPullUpSlowStopInterval.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                DecimalFormat indicatorTextDecimalFormat = new DecimalFormat("0");
                pullUpTorqueDetectionStart = indicatorTextDecimalFormat.format(leftValue);
                pullUpTorqueDetectionEnd = String.valueOf(100);
                mTvPullUpSlowStopInterval.setText(String.format("%s%%-100%%", indicatorTextDecimalFormat.format(leftValue)));
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
                //start tracking touch
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
                //stop tracking touch
            }
        });
    }

    /**
     * 获取参数
     */
    private void queryParamInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_LOCKED_ROTOR_DETECTION);
        showWaitDialog("加载中...");
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getId()));
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.btn_confirm) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
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
                if (value < 1 || value > 10000) {
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
                if (value < 0.1 || value > 10.0) {
                    ToastUtils.show("请输入正确的下放脉冲检测判断时间!");
                    mEtDecentralizedPulseDetectionTime.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的下放脉冲检测判断时间!");
                mEtDecentralizedPulseDetectionTime.requestFocus();
                return false;
            }

            float leftValue = seekBarDecentralizedStallDetectionInterval.getLeftSeekBar().getProgress();
            if (leftValue > 50) {
                ToastUtils.show("下放堵转检测区间起始值不能大于50%!");
                return false;
            }

            float rightValue = seekBarDecentralizedStallDetectionInterval.getRightSeekBar().getProgress();
            if (rightValue <= 50) {
                ToastUtils.show("下放堵转检测区间终值不能小于50%!");
                return false;
            }

            if (TextUtils.isEmpty(decentralizedTorqueStallThreshold)) {
                ToastUtils.show("请输入下放力矩堵转阈值!");
                mEtDecentralizedTorqueStallThreshold.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(decentralizedTorqueStallThreshold);
                if (value < 0.00 || value > 2.00) {
                    ToastUtils.show("请输入正确的下放力矩堵转阈值!");
                    mEtDecentralizedTorqueStallThreshold.requestFocus();
                    return false;
                }
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
                if (value < 0.01 || value > 5.00) {
                    ToastUtils.show("请输入正确的下放力矩检测判断时间!");
                    mEtDecentralizedTorqueDetectionTime.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的下放力矩检测判断时间!");
                mEtDecentralizedTorqueDetectionTime.requestFocus();
                return false;
            }

            float torqueLeftValue = seekBarDecentralizedSlowStopInterval.getLeftSeekBar().getProgress();
            if (torqueLeftValue < rightValue) {
                ToastUtils.show("下放缓停区间起始值不能小于堵转检测区间终值！");
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
                double value = Double.parseDouble(pullUpTorqueStallThreshold);
                if (value < 1.00 || value > 6.00) {
                    ToastUtils.show("请输入正确的上拉力矩堵转阈值!");
                    mEtPullUpTorqueStallThreshold.requestFocus();
                    return false;
                }
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
                if (value < 0.01 || value > 5.00) {
                    ToastUtils.show("请输入正确的上拉力矩检测判断时间!");
                    mEtPullUpTorqueDetectionTime.requestFocus();
                    return false;
                }
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

            mBtnSave.setEnabled(false);
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_LOCKED_ROTOR_DETECTION, entity);
            showWaitDialog("处理中...");
            doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getId()));
        } catch (Exception ex) {
            ex.printStackTrace();
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
//        super.onQueryCmdResponseResultSuccess(queryCmdResult);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(queryCmdResult.getCmdEngName());
        switch (type) {
            case ADME_MD_GET_LOCKED_ROTOR_DETECTION: {
                dismissWaitDialog();
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
                    dismissWaitDialog();
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
                dismissWaitDialog();
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
                break;
        }
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

            decimalFormat.applyPattern("#.##");
            decentralizedTorqueStallThreshold = decimalFormat.format(Double.parseDouble(decentralizedTorqueStallThreshold));
            mEtDecentralizedTorqueStallThreshold.setText(decentralizedTorqueStallThreshold);

            decimalFormat.applyPattern("#.##");
            decentralizedTorqueDetectionTime = decimalFormat.format(Double.parseDouble(decentralizedTorqueDetectionTime));
            mEtDecentralizedTorqueDetectionTime.setText(decentralizedTorqueDetectionTime);

            decimalFormat.applyPattern("#.##");
            pullUpTorqueStallThreshold = decimalFormat.format(Double.parseDouble(pullUpTorqueStallThreshold));
            mEtPullUpTorqueStallThreshold.setText(pullUpTorqueStallThreshold);

            decimalFormat.applyPattern("#.##");
            pullUpTorqueDetectionTime = decimalFormat.format(Double.parseDouble(pullUpTorqueDetectionTime));
            mEtPullUpTorqueDetectionTime.setText(pullUpTorqueDetectionTime);

            seekBarDecentralizedStallDetectionInterval.setProgress(Integer.parseInt(decentralizedPulseDetectionStart), Integer.parseInt(decentralizedPulseDetectionEnd));
            seekBarDecentralizedSlowStopInterval.setProgress(Integer.parseInt(decentralizedTorqueDetectionStart));
            seekBarPullUpSlowStopInterval.setProgress(Integer.parseInt(pullUpTorqueDetectionStart));

        } catch (Exception ex) {
            ex.printStackTrace();
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
