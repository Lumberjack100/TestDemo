package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.TypeEntity;
import com.shmedo.configlibrary.iot.cmd.entity.das.AlarmLevelEntity;
import com.shmedo.configlibrary.iot.cmd.entity.das.AudibleAlarmEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.IOTSensorType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.das.AlarmLevel;
import com.shmedo.configlibrary.iot.model.das.AudibleAlarm;
import com.shmedo.configlibrary.iot.model.das.DasCollectorInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/23 <br/>
 * 描述：     声光报警器
 */
public class NetAudibleAlarmFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.et_channel_number)
    EditText mEtChannelNumber;

    @BindView(R.id.et_network_number)
    EditText mEtNetworkNumber;

    @BindView(R.id.et_target_address)
    EditText mEtTargetAddress;

    @BindView(R.id.tv_alarm_type)
    TextView mTvAlarmType;

    /**
     * 雨量计
     */
    @BindView(R.id.et_rain_gauge_level1)
    EditText mEtRainGaugeLevel1;

    @BindView(R.id.et_rain_gauge_level2)
    EditText mEtRainGaugeLevel2;

    @BindView(R.id.et_rain_gauge_level3)
    EditText mEtRainGaugeLevel3;

    @BindView(R.id.et_rain_gauge_level4)
    EditText mEtRainGaugeLevel4;

    @BindView(R.id.et_rain_gauge_level5)
    EditText mEtRainGaugeLevel5;

    /**
     * 倾角计
     */
    @BindView(R.id.et_inclinometer_level1)
    EditText mEtInclinometerLevel1;

    @BindView(R.id.et_inclinometer_level2)
    EditText mEtInclinometerLevel2;

    @BindView(R.id.et_inclinometer_level3)
    EditText mEtInclinometerLevel3;

    @BindView(R.id.et_inclinometer_level4)
    EditText mEtInclinometerLevel4;

    @BindView(R.id.et_inclinometer_level5)
    EditText mEtInclinometerLevel5;

    /**
     * 主传感器
     */
    @BindView(R.id.tv_external_sensor_name)
    TextView mTvExternalSensorName;

    @BindView(R.id.et_external_sensor_level1)
    EditText mEtExternalSensorLevel1;

    @BindView(R.id.et_external_sensor_level2)
    EditText mEtExternalSensorLevel2;

    @BindView(R.id.et_external_sensor_level3)
    EditText mEtExternalSensorLevel3;

    @BindView(R.id.et_external_sensor_level4)
    EditText mEtExternalSensorLevel4;

    @BindView(R.id.et_external_sensor_level5)
    EditText mEtExternalSensorLevel5;

    private String channelNumber;
    private String networkNumber;
    private String targetAddress;
    private String alarmType;

    private String sensorType;
    private String rainGaugeLevel1;
    private String rainGaugeLevel2;
    private String rainGaugeLevel3;
    private String rainGaugeLevel4;
    private String rainGaugeLevel5;

    private String inclinometerLevel1;
    private String inclinometerLevel2;
    private String inclinometerLevel3;
    private String inclinometerLevel4;
    private String inclinometerLevel5;

    private String externalSensorLevel1;
    private String externalSensorLevel2;
    private String externalSensorLevel3;
    private String externalSensorLevel4;
    private String externalSensorLevel5;

    private AudibleAlarm audibleAlarm;
    private AlarmLevel alarmLevel;

    private String[] alarmTypes;

    private LinkedList<String> commandItems = new LinkedList<>();

    public static NetAudibleAlarmFragment newInstance(DeviceInfo deviceInfo) {
        NetAudibleAlarmFragment fragment = new NetAudibleAlarmFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.audible_alarm_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        alarmTypes = getResources().getStringArray(R.array.alarm_type);
        setFilter();

        showWaitDialog("加载中...");
        queryCollectorInfo();
    }

    private void setFilter() {
        mEtChannelNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtNetworkNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtTargetAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        mEtRainGaugeLevel1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtRainGaugeLevel2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtRainGaugeLevel3.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtRainGaugeLevel4.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtRainGaugeLevel5.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        mEtInclinometerLevel1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtInclinometerLevel2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtInclinometerLevel3.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtInclinometerLevel4.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtInclinometerLevel5.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        mEtExternalSensorLevel1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtExternalSensorLevel2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtExternalSensorLevel3.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtExternalSensorLevel4.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtExternalSensorLevel5.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        mTvAlarmType.setText(alarmTypes[0]);
        alarmType = "1";
    }

    /**
     * 查询采集器配置信息
     */
    private void queryCollectorInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 查询声光报警器控制参数
     */
    private void queryAlarmControl() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_AUDIBLE_ALARM);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 查询声光报警器报警级别参数
     */
    private void queryAlarmLevel(String type) {
        TypeEntity typeEntity = new TypeEntity(type);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_AUDIBLE_ALARM_LEVEL, typeEntity);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    @OnClick({R.id.ll_alarm_type, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.ll_alarm_type) {
            showAlarmTypeDialog();

        } else if (id == R.id.btn_confirm) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            processSave();
        }
    }

    private boolean checkValueIsValid() {
        if (!channelNumber.equals(mEtChannelNumber.getText().toString())) {
            if (TextUtils.isEmpty(mEtChannelNumber.getText().toString())) {
                ToastUtils.show("请输入通信信道!");
                mEtChannelNumber.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(mEtChannelNumber.getText().toString());
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的通信信道!");
                mEtChannelNumber.requestFocus();
                return false;
            }
            channelNumber = mEtChannelNumber.getText().toString();
        } else {
            channelNumber = null;
        }

        if (!networkNumber.equals(mEtNetworkNumber.getText().toString())) {
            if (TextUtils.isEmpty(mEtNetworkNumber.getText().toString())) {
                ToastUtils.show("请输入网络编号!");
                mEtNetworkNumber.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(mEtNetworkNumber.getText().toString());
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的网络编号!");
                mEtNetworkNumber.requestFocus();
                return false;
            }
            networkNumber = mEtNetworkNumber.getText().toString();
        } else {
            networkNumber = null;
        }

        if (!targetAddress.equals(mEtTargetAddress.getText().toString())) {
            if (TextUtils.isEmpty(mEtTargetAddress.getText().toString())) {
                ToastUtils.show("请输入目标地址!");
                mEtTargetAddress.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(mEtTargetAddress.getText().toString());
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的目标地址!");
                mEtTargetAddress.requestFocus();
                return false;
            }
            targetAddress = mEtTargetAddress.getText().toString();
        } else {
            targetAddress = null;
        }

        if (!rainGaugeLevel1.equals(mEtRainGaugeLevel1.getText().toString())) {
            if (TextUtils.isEmpty(mEtRainGaugeLevel1.getText().toString())) {
                ToastUtils.show("请输入雨量计无报警值!");
                mEtRainGaugeLevel1.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(mEtRainGaugeLevel1.getText().toString());
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的雨量计无报警值!");
                mEtRainGaugeLevel1.requestFocus();
                return false;
            }
            rainGaugeLevel1 = mEtRainGaugeLevel1.getText().toString();
        } else {
            rainGaugeLevel1 = null;
        }

        if (!rainGaugeLevel2.equals(mEtRainGaugeLevel2.getText().toString())) {
            if (TextUtils.isEmpty(mEtRainGaugeLevel2.getText().toString())) {
                ToastUtils.show("请输入雨量计一级报警值!");
                mEtRainGaugeLevel2.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(mEtRainGaugeLevel2.getText().toString());
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的雨量计一级报警值!");
                mEtRainGaugeLevel2.requestFocus();
                return false;
            }
            rainGaugeLevel2 = mEtRainGaugeLevel2.getText().toString();
        } else {
            rainGaugeLevel2 = null;
        }

        if (!rainGaugeLevel3.equals(mEtRainGaugeLevel3.getText().toString())) {
            if (TextUtils.isEmpty(mEtRainGaugeLevel3.getText().toString())) {
                ToastUtils.show("请输入雨量计二级报警值!");
                mEtRainGaugeLevel3.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(mEtRainGaugeLevel3.getText().toString());
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的雨量计二级报警值!");
                mEtRainGaugeLevel3.requestFocus();
                return false;
            }
            rainGaugeLevel3 = mEtRainGaugeLevel3.getText().toString();
        } else {
            rainGaugeLevel3 = null;
        }

        if (!rainGaugeLevel4.equals(mEtRainGaugeLevel4.getText().toString())) {
            if (TextUtils.isEmpty(mEtRainGaugeLevel4.getText().toString())) {
                ToastUtils.show("请输入雨量计三级报警值!");
                mEtRainGaugeLevel4.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(mEtRainGaugeLevel4.getText().toString());
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的雨量计三级报警值!");
                mEtRainGaugeLevel4.requestFocus();
                return false;
            }
            rainGaugeLevel4 = mEtRainGaugeLevel4.getText().toString();
        } else {
            rainGaugeLevel4 = null;
        }

        if (!rainGaugeLevel5.equals(mEtRainGaugeLevel5.getText().toString())) {
            if (TextUtils.isEmpty(mEtRainGaugeLevel5.getText().toString())) {
                ToastUtils.show("请输入雨量计四级报警值!");
                mEtRainGaugeLevel5.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(mEtRainGaugeLevel5.getText().toString());
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的雨量计四级报警值!");
                mEtRainGaugeLevel5.requestFocus();
                return false;
            }
            rainGaugeLevel5 = mEtRainGaugeLevel5.getText().toString();
        } else {
            rainGaugeLevel5 = null;
        }

        if (!inclinometerLevel1.equals(mEtInclinometerLevel1.getText().toString())) {
            if (TextUtils.isEmpty(mEtInclinometerLevel1.getText().toString())) {
                ToastUtils.show("请输入倾角计无报警值!");
                mEtInclinometerLevel1.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(mEtInclinometerLevel1.getText().toString());
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的倾角计无报警值!");
                mEtInclinometerLevel1.requestFocus();
                return false;
            }
            inclinometerLevel1 = mEtInclinometerLevel1.getText().toString();
        } else {
            inclinometerLevel1 = null;
        }

        if (!inclinometerLevel2.equals(mEtInclinometerLevel2.getText().toString())) {
            if (TextUtils.isEmpty(mEtRainGaugeLevel2.getText().toString())) {
                ToastUtils.show("请输入倾角计一级报警值!");
                mEtInclinometerLevel2.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(mEtInclinometerLevel2.getText().toString());
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的倾角计一级报警值!");
                mEtInclinometerLevel2.requestFocus();
                return false;
            }
            inclinometerLevel2 = mEtInclinometerLevel2.getText().toString();
        } else {
            inclinometerLevel2 = null;
        }

        if (!inclinometerLevel3.equals(mEtInclinometerLevel3.getText().toString())) {
            if (TextUtils.isEmpty(mEtRainGaugeLevel3.getText().toString())) {
                ToastUtils.show("请输入倾角计二级报警值!");
                mEtInclinometerLevel3.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(mEtInclinometerLevel3.getText().toString());
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的倾角计二级报警值!");
                mEtInclinometerLevel3.requestFocus();
                return false;
            }
            inclinometerLevel3 = mEtInclinometerLevel3.getText().toString();
        } else {
            inclinometerLevel3 = null;
        }

        if (!inclinometerLevel4.equals(mEtInclinometerLevel4.getText().toString())) {
            if (TextUtils.isEmpty(mEtRainGaugeLevel4.getText().toString())) {
                ToastUtils.show("请输入倾角计三级报警值!");
                mEtInclinometerLevel4.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(mEtInclinometerLevel4.getText().toString());
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的倾角计三级报警值!");
                mEtInclinometerLevel4.requestFocus();
                return false;
            }
            inclinometerLevel4 = mEtInclinometerLevel4.getText().toString();
        } else {
            inclinometerLevel4 = null;
        }

        if (!inclinometerLevel5.equals(mEtInclinometerLevel5.getText().toString())) {
            if (TextUtils.isEmpty(mEtRainGaugeLevel5.getText().toString())) {
                ToastUtils.show("请输入倾角计四级报警值!");
                mEtInclinometerLevel5.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(mEtInclinometerLevel5.getText().toString());
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的倾角计四级报警值!");
                mEtInclinometerLevel5.requestFocus();
                return false;
            }
            inclinometerLevel5 = mEtInclinometerLevel5.getText().toString();
        } else {
            inclinometerLevel5 = null;
        }

        if (!externalSensorLevel1.equals(mEtExternalSensorLevel1.getText().toString())) {
            if (TextUtils.isEmpty(mEtExternalSensorLevel1.getText().toString())) {
                String msg = String.format("请输入%s的无报警值!", mTvExternalSensorName.getText().toString());
                ToastUtils.show(msg);
                mEtExternalSensorLevel1.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(mEtExternalSensorLevel1.getText().toString());
            } catch (Exception ex) {
                String msg = String.format("请输入正确的%s无报警值!", mTvExternalSensorName.getText().toString());
                ToastUtils.show(msg);
                mEtExternalSensorLevel1.requestFocus();
                return false;
            }
            externalSensorLevel1 = mEtExternalSensorLevel1.getText().toString();
        } else {
            externalSensorLevel1 = null;
        }

        if (!externalSensorLevel2.equals(mEtExternalSensorLevel2.getText().toString())) {
            if (TextUtils.isEmpty(mEtExternalSensorLevel2.getText().toString())) {
                String msg = String.format("请输入%s的一级报警值!", mTvExternalSensorName.getText().toString());
                ToastUtils.show(msg);
                mEtExternalSensorLevel2.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(mEtExternalSensorLevel2.getText().toString());
            } catch (Exception ex) {
                String msg = String.format("请输入正确的%s一级报警值!", mTvExternalSensorName.getText().toString());
                ToastUtils.show(msg);
                mEtExternalSensorLevel2.requestFocus();
                return false;
            }
            externalSensorLevel2 = mEtExternalSensorLevel2.getText().toString();
        } else {
            externalSensorLevel2 = null;
        }

        if (!externalSensorLevel3.equals(mEtExternalSensorLevel3.getText().toString())) {
            if (TextUtils.isEmpty(mEtExternalSensorLevel3.getText().toString())) {
                String msg = String.format("请输入%s的二级报警值!", mTvExternalSensorName.getText().toString());
                ToastUtils.show(msg);
                mEtExternalSensorLevel3.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(mEtExternalSensorLevel3.getText().toString());
            } catch (Exception ex) {
                String msg = String.format("请输入正确的%s二级报警值!", mTvExternalSensorName.getText().toString());
                ToastUtils.show(msg);
                mEtExternalSensorLevel3.requestFocus();
                return false;
            }
            externalSensorLevel3 = mEtExternalSensorLevel3.getText().toString();
        } else {
            externalSensorLevel3 = null;
        }

        if (!externalSensorLevel4.equals(mEtExternalSensorLevel4.getText().toString())) {
            if (TextUtils.isEmpty(mEtExternalSensorLevel4.getText().toString())) {
                String msg = String.format("请输入%s的三级报警值!", mTvExternalSensorName.getText().toString());
                ToastUtils.show(msg);
                mEtExternalSensorLevel4.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(mEtExternalSensorLevel4.getText().toString());
            } catch (Exception ex) {
                String msg = String.format("请输入正确的%s三级报警值!", mTvExternalSensorName.getText().toString());
                ToastUtils.show(msg);
                mEtExternalSensorLevel4.requestFocus();
                return false;
            }
            externalSensorLevel4 = mEtExternalSensorLevel4.getText().toString();
        } else {
            externalSensorLevel4 = null;
        }

        if (!externalSensorLevel5.equals(mEtExternalSensorLevel5.getText().toString())) {
            if (TextUtils.isEmpty(mEtExternalSensorLevel5.getText().toString())) {
                String msg = String.format("请输入%s的三级报警值!", mTvExternalSensorName.getText().toString());
                ToastUtils.show(msg);
                mEtExternalSensorLevel5.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(mEtExternalSensorLevel5.getText().toString());
            } catch (Exception ex) {
                String msg = String.format("请输入正确的%s四级报警值!", mTvExternalSensorName.getText().toString());
                ToastUtils.show(msg);
                mEtExternalSensorLevel5.requestFocus();
                return false;
            }
            externalSensorLevel5 = mEtExternalSensorLevel5.getText().toString();
        } else {
            externalSensorLevel5 = null;
        }
        return true;
    }

    private void processSave() {
        commandItems.clear();

        if (channelNumber != null || networkNumber != null || targetAddress != null || !alarmType.equals(audibleAlarm.getAlarmtype())) {
            AudibleAlarmEntity audibleAlarmEntity = new AudibleAlarmEntity();
            audibleAlarmEntity.setChannell(mEtChannelNumber.getText().toString());
            audibleAlarmEntity.setPanid(mEtNetworkNumber.getText().toString());
            audibleAlarmEntity.setGroupid(mEtTargetAddress.getText().toString());
            audibleAlarmEntity.setAlarmtype(alarmType);
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_AUDIBLE_ALARM, audibleAlarmEntity);
            commandItems.add(command);
        }

        if (rainGaugeLevel1 != null || rainGaugeLevel2 != null || rainGaugeLevel3 != null || rainGaugeLevel4 != null || rainGaugeLevel5 != null) {
            AlarmLevelEntity alarmLevelEntity = new AlarmLevelEntity();
            alarmLevelEntity.setType("1");
            alarmLevelEntity.setLevel1(mEtRainGaugeLevel1.getText().toString());
            alarmLevelEntity.setLevel2(mEtRainGaugeLevel2.getText().toString());
            alarmLevelEntity.setLevel3(mEtRainGaugeLevel3.getText().toString());
            alarmLevelEntity.setLevel4(mEtRainGaugeLevel4.getText().toString());
            alarmLevelEntity.setLevel5(mEtRainGaugeLevel5.getText().toString());
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_AUDIBLE_ALARM_LEVEL, alarmLevelEntity);
            commandItems.add(command);
        }

        if (inclinometerLevel1 != null || inclinometerLevel2 != null || inclinometerLevel3 != null || inclinometerLevel4 != null || inclinometerLevel5 != null) {
            AlarmLevelEntity alarmLevelEntity = new AlarmLevelEntity();
            alarmLevelEntity.setType("2");
            alarmLevelEntity.setLevel1(mEtInclinometerLevel1.getText().toString());
            alarmLevelEntity.setLevel2(mEtInclinometerLevel3.getText().toString());
            alarmLevelEntity.setLevel3(mEtInclinometerLevel3.getText().toString());
            alarmLevelEntity.setLevel4(mEtInclinometerLevel4.getText().toString());
            alarmLevelEntity.setLevel5(mEtInclinometerLevel5.getText().toString());
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_AUDIBLE_ALARM_LEVEL, alarmLevelEntity);
            commandItems.add(command);
        }

        if (externalSensorLevel1 != null || externalSensorLevel2 != null || externalSensorLevel3 != null || externalSensorLevel4 != null || externalSensorLevel5 != null) {
            AlarmLevelEntity alarmLevelEntity = new AlarmLevelEntity();
            alarmLevelEntity.setType("3");
            alarmLevelEntity.setLevel1(mEtExternalSensorLevel1.getText().toString());
            alarmLevelEntity.setLevel2(mEtExternalSensorLevel2.getText().toString());
            alarmLevelEntity.setLevel3(mEtExternalSensorLevel3.getText().toString());
            alarmLevelEntity.setLevel4(mEtExternalSensorLevel4.getText().toString());
            alarmLevelEntity.setLevel5(mEtExternalSensorLevel5.getText().toString());
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_AUDIBLE_ALARM_LEVEL, alarmLevelEntity);
            commandItems.add(command);
        }

        if (commandItems.size() > 0) {
            showWaitDialog("处理中...");
            doCommonDispatchRawCmd(commandItems.getFirst(), Arrays.asList(deviceInfo.getDeviceToken()));
        } else {
            ToastUtils.show("您没有修改信息！");
        }
    }

    /**
     * 选择报警类型
     */
    private void showAlarmTypeDialog() {
        int pos = Arrays.asList(alarmTypes).indexOf(String.valueOf(mTvAlarmType.getText()));
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", alarmTypes,
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvAlarmType.setText(text);
                                alarmType = String.valueOf(position + 1);
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
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
            case DAS_MD_GET_COLLECTOR_CONTROL: {//
                IOTCommandResult<DasCollectorInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "查询采集器参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                initCollectorInfo(commandResult.getResult());
                queryAlarmControl();
            }
            break;

            case DAS_MD_GET_AUDIBLE_ALARM: {
                IOTCommandResult<AudibleAlarm> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "查询报警控制参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                audibleAlarm = commandResult.getResult();
                initAlarmControl();
                queryAlarmLevel("1");
            }
            break;

            case DAS_MD_GET_AUDIBLE_ALARM_LEVEL: {
                IOTCommandResult<AlarmLevel> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "查询报警级别参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                alarmLevel = commandResult.getResult();
                initAlarmLevel();
            }
            break;

            case DAS_MD_SET_AUDIBLE_ALARM: {
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "设置报警控制参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                commandItems.removeFirst();
                if (commandItems.size() > 0) {
                    doCommonDispatchRawCmd(commandItems.getFirst(), Arrays.asList(deviceInfo.getDeviceToken()));
                } else {
                    dismissWaitDialog();
                    ToastUtils.show("保存成功");
                }
            }
            break;

            case DAS_MD_SET_AUDIBLE_ALARM_LEVEL: {
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "设置报警级别参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                commandItems.removeFirst();
                if (commandItems.size() > 0) {
                    doCommonDispatchRawCmd(commandItems.getFirst(), Arrays.asList(deviceInfo.getDeviceToken()));
                } else {
                    dismissWaitDialog();
                    ToastUtils.show("保存成功");
                }
            }
            break;

            default:
                break;
        }
    }

    private void initCollectorInfo(DasCollectorInfo collectorInfo) {
        if (collectorInfo == null)
            return;

        IOTSensorType sensorType = IOTSensorType.getSensorTypeByCollectorCode(collectorInfo.getType());
        mTvExternalSensorName.setText(sensorType.getDescription());
    }

    private void initAlarmControl() {
        if (audibleAlarm == null) {
            Timber.e("AudibleAlarm 为空!");
            audibleAlarm = new AudibleAlarm();
            return;
        }
        channelNumber = audibleAlarm.getChannel();
        networkNumber = audibleAlarm.getPanid();
        targetAddress = audibleAlarm.getGroupid();
        alarmType = audibleAlarm.getAlarmtype();

        mEtChannelNumber.setText(channelNumber);
        mEtNetworkNumber.setText(networkNumber);
        mEtTargetAddress.setText(targetAddress);
        switch (alarmType) {
            case "1":
                mTvAlarmType.setText(alarmTypes[0]);
                break;

            case "2":
                mTvAlarmType.setText(alarmTypes[1]);
                break;

            case "3":
                mTvAlarmType.setText(alarmTypes[2]);
                break;

            case "4":
                mTvAlarmType.setText(alarmTypes[3]);
                break;
        }
    }

    private void initAlarmLevel() {
        if (alarmLevel == null) {
            dismissWaitDialog();
            Timber.e("AlarmLevel 为空!");
            alarmLevel = new AlarmLevel();
            return;
        }
        sensorType = alarmLevel.getType();
        switch (sensorType) {
            case "1": {
                rainGaugeLevel1 = alarmLevel.getLevel1();
                rainGaugeLevel2 = alarmLevel.getLevel2();
                rainGaugeLevel3 = alarmLevel.getLevel3();
                rainGaugeLevel4 = alarmLevel.getLevel4();
                rainGaugeLevel5 = alarmLevel.getLevel5();

                mEtRainGaugeLevel1.setText(rainGaugeLevel1);
                mEtRainGaugeLevel2.setText(rainGaugeLevel2);
                mEtRainGaugeLevel3.setText(rainGaugeLevel3);
                mEtRainGaugeLevel4.setText(rainGaugeLevel4);
                mEtRainGaugeLevel5.setText(rainGaugeLevel5);
                queryAlarmLevel("2");
            }
            break;

            case "2": {
                inclinometerLevel1 = alarmLevel.getLevel1();
                inclinometerLevel2 = alarmLevel.getLevel2();
                inclinometerLevel3 = alarmLevel.getLevel3();
                inclinometerLevel4 = alarmLevel.getLevel4();
                inclinometerLevel5 = alarmLevel.getLevel5();

                mEtInclinometerLevel1.setText(inclinometerLevel1);
                mEtInclinometerLevel2.setText(inclinometerLevel2);
                mEtInclinometerLevel3.setText(inclinometerLevel3);
                mEtInclinometerLevel4.setText(inclinometerLevel4);
                mEtInclinometerLevel5.setText(inclinometerLevel5);
                queryAlarmLevel("3");
            }
            break;

            case "3": {
                dismissWaitDialog();
                externalSensorLevel1 = alarmLevel.getLevel1();
                externalSensorLevel2 = alarmLevel.getLevel2();
                externalSensorLevel3 = alarmLevel.getLevel3();
                externalSensorLevel4 = alarmLevel.getLevel4();
                externalSensorLevel5 = alarmLevel.getLevel5();

                mEtExternalSensorLevel1.setText(externalSensorLevel1);
                mEtExternalSensorLevel2.setText(externalSensorLevel2);
                mEtExternalSensorLevel3.setText(externalSensorLevel3);
                mEtExternalSensorLevel4.setText(externalSensorLevel4);
                mEtExternalSensorLevel5.setText(externalSensorLevel5);
            }
            break;
        }
    }
}