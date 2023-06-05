package com.shmedo.mcloudapp.deviceconfig.view.adme;

import android.app.TimePickerDialog;
import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.ConvertUtils;
import com.hjq.toast.ToastUtils;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kyleduo.switchbutton.SwitchButton;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeBasicConfigEntity;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeExecutiveAgencyEntity;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeLockedRotorDetectionEntity;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeStepperMotorEntity;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.adme.AdmeBasicConfigInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeExecutiveAgencyInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeLockedRotorDetectionInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeStepperMotorInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.RecycleViewDivider;
import com.shmedo.mcloudapp.deviceconfig.adapter.AdmeTimeAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.AdmeTimeItem;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/7/13 <br/>
 * 描述：     TODO
 */
public class AdmeBasicParamConfigView extends LinearLayout {
    @BindView(R.id.et_mac_address)
    ClearEditText mEtMacAddress;//Mac 地址

    @BindView(R.id.tv_measure_method)
    TextView mTvMeasureMethod;//测量方式

    @BindView(R.id.et_waiting_interval_per_round)
    ClearEditText mEtWaitingIntervalPerRound;//每轮等待时间

    @BindView(R.id.tv_measurement_interval_per_round)
    TextView mTvMeasurementIntervalPerRound;//每轮测量间隔

    @BindView(R.id.tv_modified_date)
    TextView  mTvModifiedDate;//修改日期

    @BindView(R.id.et_interval_day)
    ClearEditText mEtIntervalDay;//间隔时间

    @BindView(R.id.recyclerview_time)
    RecyclerView mRecyclerViewTime;

    @BindView(R.id.et_inclination_tube_hole_depth)
    ClearEditText mEtInclinometerTubeHoleDepth;//测斜管孔深(m)

    @BindView(R.id.et_decentralization_speed)
    ClearEditText mEtDecentralizationSpeed;//下放速度(r/min)

    @BindView(R.id.et_decentralization_waiting_time)
    ClearEditText mEtDecentralizationWaitingTime;//下放等待时间(min)

    @BindView(R.id.tv_data_settlement_method)
    TextView mTvDataSettlementMethod;

    @BindView(R.id.decentralizedEnableSBtn)
    public SwitchButton mSbDecentralizedEnable;

    @BindView(R.id.positiveAndNegativeEnableSBtn)
    public SwitchButton positiveAndNegativeEnableSBtn;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    @BindView(R.id.ll_mac_address)
    ViewGroup macAddressLayout;

    @BindView(R.id.ll_measure_method)
    ViewGroup measureMethodLayout;

    @BindView(R.id.ll_waiting_interval_per_round)
    ViewGroup waitingIntervalPerRoundLayout;

    @BindView(R.id.ll_measurement_interval_per_round)
    ViewGroup measurementIntervalPerRoundLayout;

    @BindView(R.id.ll_modified_date)
    ViewGroup modifiedDateLayout;

    @BindView(R.id.ll_interval_day)
    ViewGroup intervalDayLayout;

    @BindView(R.id.ll_start_time_per_round)
    ViewGroup startTimePerRoundLayout;

    @BindView(R.id.ll_inclination_tube_hole_depth)
    ViewGroup inclinometerTubeHoleDepthLayout;

    @BindView(R.id.ll_decentralization_speed)
    ViewGroup decentralizationSpeedLayout;

    @BindView(R.id.ll_decentralization_waiting_time)
    ViewGroup decentralizationWaitingTimeLayout;

    @BindView(R.id.ll_data_settlement_method)
    ViewGroup dataSettlementMethodLayout;

    private String measureMethod;//测量方式 （0:实时测量，1:整时整点测量，2:定时定点测量)
    private String waitingIntervalPerRound;//每轮等待时间
    private String measurementIntervalPerRound;//每轮测量间隔
    private String intervalDays;//间隔时间
    private String startTimePerRound;//每轮测量开始时间
    private String address;//采集器地址/Mac 地址
    private String inclinometerTubeHoleDepth;//测斜管孔深(m)
    private String decentralizationSpeed;//下放速度(r/min)
    private String decentralizationWaitingTime;//下放等待时间(min)
    private String dataSettlementMethod;//数据解算方式
    private final String[] measureMethods = new String[]{"实时测量", "整时整点测量", "定时定点测量"};
    private final String[] measIntervalPerRounds = new String[]{"1", "2", "3", "4", "6", "8", "12", "24"};
    private final String[] settlementMethods = new String[]{"顶部固定法", "底部固定法"};

    private DecimalFormat decimalFormat = new DecimalFormat();

    public AdmeBasicConfigInfo basicConfigParam = new AdmeBasicConfigInfo();
    public AdmeExecutiveAgencyInfo admeExecutiveAgencyInfo = new AdmeExecutiveAgencyInfo();
    public AdmeLockedRotorDetectionInfo lockedRotorDetectionInfo = new AdmeLockedRotorDetectionInfo();

    private AdmeTimeAdapter admeTimeAdapter;

    public AdmeBasicParamConfigView(Context context) {
        this(context, null);
    }

    public AdmeBasicParamConfigView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdmeBasicParamConfigView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.adme_basic_param_view, this, true);
        ButterKnife.bind(this);
        initView();
        initTimeAdapter(context);
    }

    private void initView() {
        mEtMacAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        mEtMacAddress.setHint("XXXXXXXXXXXX");

        mEtWaitingIntervalPerRound.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtIntervalDay.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        mEtInclinometerTubeHoleDepth.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

        mEtDecentralizationSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtDecentralizationSpeed.setHint("1-180");

        mEtDecentralizationWaitingTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtDecentralizationWaitingTime.setHint("1-32");

        mTvDataSettlementMethod.setText(settlementMethods[0]);
        dataSettlementMethod = "0";

        mTvMeasureMethod.setText(measureMethods[1]);
        measureMethod = "1";

        mTvMeasurementIntervalPerRound.setText("1");
        measurementIntervalPerRound = "1";
    }

    private void initTimeAdapter(Context context) {
        mRecyclerViewTime.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        mRecyclerViewTime.addItemDecoration(new RecycleViewDivider(LinearLayoutManager.VERTICAL, ConvertUtils.dp2px(0.5f), com.blankj.utilcode.util.ColorUtils.getColor(R.color.divider_line_bg_efefef)));
        admeTimeAdapter = new AdmeTimeAdapter(context, new ArrayList<>());
        admeTimeAdapter.setOnItemClickListener(new AdmeTimeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
            }

            @Override
            public void addItem() {
                new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String time = String.format(Locale.getDefault(), "%02d:00:00", hourOfDay);
                        for (AdmeTimeItem item : admeTimeAdapter.getData()) {
                            if (item.getTime().contains(time)) {
                                ToastUtils.show("不能设置重复时间点!");
                                return;
                            }
                        }
                        admeTimeAdapter.getData().add(new AdmeTimeItem(time));
                        admeTimeAdapter.notifyItemInserted(admeTimeAdapter.getData().size());
                    }
                }, 0, 0, true).show();
            }
        });
        admeTimeAdapter.setItemLongClickListener(new AdmeTimeAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(RecyclerView.ViewHolder holder, int position, View v) {
                warnDeleteSensorItem(context, position);
            }
        });
        mRecyclerViewTime.setAdapter(admeTimeAdapter);
    }

    private void warnDeleteSensorItem(Context context, int position) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(context)
                .title("温馨提示")
                .content("移除?")
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
                        admeTimeAdapter.getData().remove(position);
                        admeTimeAdapter.notifyItemRemoved(position);
                        admeTimeAdapter.notifyItemRangeChanged(position, admeTimeAdapter.getData().size() - position);
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    /**
     * 选择测量方式
     */
    public void showMeasureMethodDialog(Context context) {
        int pos = Arrays.asList(measureMethods).indexOf(String.valueOf(mTvMeasureMethod.getText()));
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(context)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", measureMethods,
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvMeasureMethod.setText(text);
                                measureMethod = String.valueOf(position);
                                if (position == 0) {
                                    if (null != waitingIntervalPerRound && !waitingIntervalPerRound.equals("NullKey"))
                                        waitingIntervalPerRoundLayout.setVisibility(View.VISIBLE);
                                    measurementIntervalPerRoundLayout.setVisibility(View.GONE);
                                    modifiedDateLayout.setVisibility(View.GONE);
                                    intervalDayLayout.setVisibility(View.GONE);
                                    startTimePerRoundLayout.setVisibility(View.GONE);
                                    mRecyclerViewTime.setVisibility(View.GONE);
                                } else if (position == 1) {
                                    waitingIntervalPerRoundLayout.setVisibility(View.GONE);
                                    measurementIntervalPerRoundLayout.setVisibility(View.VISIBLE);
                                    modifiedDateLayout.setVisibility(View.GONE);
                                    intervalDayLayout.setVisibility(View.GONE);
                                    startTimePerRoundLayout.setVisibility(View.GONE);
                                    mRecyclerViewTime.setVisibility(View.GONE);
                                } else if (position == 2) {
                                    waitingIntervalPerRoundLayout.setVisibility(View.GONE);
                                    measurementIntervalPerRoundLayout.setVisibility(View.GONE);
                                    if (null != intervalDays && intervalDays.equals("NullKey")) {
                                        modifiedDateLayout.setVisibility(View.VISIBLE);
                                        intervalDayLayout.setVisibility(View.VISIBLE);
                                    }
                                    startTimePerRoundLayout.setVisibility(View.VISIBLE);
                                    mRecyclerViewTime.setVisibility(View.VISIBLE);
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    /**
     * 选择每轮测量间隔
     */
    public void showMeasIntervalPerRoundsDialog(Context context) {
        int pos = Arrays.asList(measIntervalPerRounds).indexOf(String.valueOf(mTvMeasurementIntervalPerRound.getText()));
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(context)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", measIntervalPerRounds,
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvMeasurementIntervalPerRound.setText(text);
                                measurementIntervalPerRound = text;
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    /**
     * 选择数据解算方式
     */
    public void showDataSettlementMethodDialog(Context context) {
        int pos = Arrays.asList(settlementMethods).indexOf(String.valueOf(mTvDataSettlementMethod.getText()));
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(context)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", settlementMethods,
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvDataSettlementMethod.setText(text);
                                if (position == 0) {
                                    dataSettlementMethod = "0";
                                } else {
                                    dataSettlementMethod = "1";
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    public boolean checkValueIsValid() {
        address = mEtMacAddress.getText().toString().trim();
        if (TextUtils.isEmpty(address)) {
            ToastUtils.show("请输入Mac地址!");
            mEtMacAddress.requestFocus();
            return false;
        }
        if (!ValidateUtil.isValidMacAddressNoColon(address)) {
            ToastUtils.show("请输入正确的Mac地址!");
            mEtMacAddress.requestFocus();
            return false;
        }

        if (!measureMethod.equals("NullKey") && measureMethod.equals("2")) {
            if (admeTimeAdapter.getData().size() <= 1) {
                MessageDialog.show("提示", "请设置测量时间点!", "我已知晓");
                return false;
            }
        }

        if (!waitingIntervalPerRound.equals("NullKey")) {
            waitingIntervalPerRound = mEtWaitingIntervalPerRound.getText().toString().trim();
            if (TextUtils.isEmpty(waitingIntervalPerRound)) {
                ToastUtils.show("请输入每轮等待时间!");
                mEtWaitingIntervalPerRound.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(waitingIntervalPerRound);
                if (value < 1) {
                    ToastUtils.show("请输入正确的每轮等待时间!");
                    mEtWaitingIntervalPerRound.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的每轮等待时间!");
                mEtWaitingIntervalPerRound.requestFocus();
                return false;
            }
        }

        if (!intervalDays.equals("NullKey")) {
            intervalDays = mEtIntervalDay.getText().toString().trim();
            if (TextUtils.isEmpty(waitingIntervalPerRound)) {
                ToastUtils.show("请输入间隔时间!");
                mEtIntervalDay.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(intervalDays);
                if (value < 1) {
                    ToastUtils.show("请输入正确的间隔时间!");
                    mEtIntervalDay.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的间隔时间!");
                mEtIntervalDay.requestFocus();
                return false;
            }
        }

        if (!inclinometerTubeHoleDepth.equals("NullKey")) {
            inclinometerTubeHoleDepth = mEtInclinometerTubeHoleDepth.getText().toString().trim();
            if (TextUtils.isEmpty(inclinometerTubeHoleDepth)) {
                ToastUtils.show("请输入测斜管孔深!");
                mEtInclinometerTubeHoleDepth.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(inclinometerTubeHoleDepth);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的测斜管孔深!");
                mEtInclinometerTubeHoleDepth.requestFocus();
                return false;
            }
        }
        if (!decentralizationSpeed.equals("NullKey")) {
            decentralizationSpeed = mEtDecentralizationSpeed.getText().toString().trim();
            if (TextUtils.isEmpty(decentralizationSpeed)) {
                ToastUtils.show("请输入下放速度!");
                mEtDecentralizationSpeed.requestFocus();
                return false;
            }
            try {
                int port = Integer.parseInt(decentralizationSpeed);
                if (port < 1 || port > 120) {
                    ToastUtils.show("请输入正确的下放速度!");
                    mEtDecentralizationSpeed.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的下放速度!");
                mEtDecentralizationSpeed.requestFocus();
                return false;
            }
        }
        if (!decentralizationWaitingTime.equals("NullKey")) {
            decentralizationWaitingTime = mEtDecentralizationWaitingTime.getText().toString().trim();
            if (TextUtils.isEmpty(decentralizationWaitingTime)) {
                ToastUtils.show("请输入下放等待时间!");
                mEtDecentralizationWaitingTime.requestFocus();
                return false;
            }
            try {
                int port = Integer.parseInt(decentralizationWaitingTime);
                if (port < 1 || port > 32) {
                    ToastUtils.show("请输入正确的下放等待时间!");
                    mEtDecentralizationWaitingTime.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的下放等待时间!");
                mEtDecentralizationWaitingTime.requestFocus();
                return false;
            }
        }
        return true;
    }

    /**
     * 获取设备的基础配置指令
     */
    public String getBasicCommand() {
        String command = "";
        try {
            AdmeBasicConfigEntity entity = new AdmeBasicConfigEntity();
            entity.setInctype(basicConfigParam.getInctype().equals("NullKey") ? "NullKey" : basicConfigParam.getInctype());
            entity.setAddress(basicConfigParam.getAddress().equals("NullKey") ? "NullKey" : address);
            decimalFormat.applyPattern("#.##");
            entity.setInterdeep(basicConfigParam.getInterdeep().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(inclinometerTubeHoleDepth)));
            entity.setDownspeed(basicConfigParam.getDownspeed().equals("NullKey") ? "NullKey" : decentralizationSpeed);
            entity.setDownwaitetime(basicConfigParam.getDownwaitetime().equals("NullKey") ? "NullKey" : decentralizationWaitingTime);
            entity.setDatatype(basicConfigParam.getDatatype().equals("NullKey") ? "NullKey" : dataSettlementMethod);

            command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_BASIC, entity);
        } catch (Exception ex) {
            command = "";
            ex.printStackTrace();
        }
        return command;
    }

    public String getExecutiveAgencyCommand() {
        String command = "";
        try {
            AdmeExecutiveAgencyEntity entity = new AdmeExecutiveAgencyEntity();
            entity.setMeastype(admeExecutiveAgencyInfo.getMeastype().equals("NullKey") ? "NullKey" : measureMethod);
            entity.setDatatype(admeExecutiveAgencyInfo.getDatatype().equals("NullKey") ? "NullKey" : dataSettlementMethod);
            entity.setDatareply(admeExecutiveAgencyInfo.getDatareply());
            entity.setRoundwaitetime(admeExecutiveAgencyInfo.getRoundwaitetime().equals("NullKey") ? "NullKey" : waitingIntervalPerRound);
            entity.setRoundmeasinval(admeExecutiveAgencyInfo.getRoundmeasinval().equals("NullKey") ? "NullKey" : measurementIntervalPerRound);
            entity.setInvalday(admeExecutiveAgencyInfo.getInvalday().equals("NullKey") ? "NullKey" : intervalDays);
            //定时测量方式
            if (!measureMethod.equals("NullKey") && measureMethod.equals("2")) {
                StringBuffer timeBuffer = new StringBuffer();
                for (AdmeTimeItem admeTimeItem : admeTimeAdapter.getData()) {
                    String time = admeTimeItem.getTime();
                    if (!TextUtils.isEmpty(time)) {
                        timeBuffer.append(Integer.parseInt(time.substring(0, time.indexOf(":"))));
                        timeBuffer.append("|");
                    }
                }
                if (timeBuffer.length() > 1)
                    timeBuffer.delete(timeBuffer.length() - 1, timeBuffer.length());
                entity.setRoundmeasstart(timeBuffer.toString());
            } else {
                entity.setRoundmeasstart(admeExecutiveAgencyInfo.getRoundmeasstart());
            }
            entity.setDatainval(admeExecutiveAgencyInfo.getDatainval());
            entity.setCompensatetime(admeExecutiveAgencyInfo.getCompensatetime());
            entity.setDriveaddress(admeExecutiveAgencyInfo.getDriveaddress());
            entity.setDownspeed(admeExecutiveAgencyInfo.getDownspeed().equals("NullKey") ? "NullKey" : decentralizationSpeed);
            decimalFormat.applyPattern("#.##");
            entity.setInterdeep(admeExecutiveAgencyInfo.getInterdeep().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(inclinometerTubeHoleDepth)));
            entity.setDownwaitetime(admeExecutiveAgencyInfo.getDownwaitetime().equals("NullKey") ? "NullKey" : decentralizationWaitingTime);
            entity.setUpspeed(admeExecutiveAgencyInfo.getUpspeed());
            entity.setMeaspacing(admeExecutiveAgencyInfo.getMeaspacing());
            entity.setMeaintertime(admeExecutiveAgencyInfo.getMeaintertime());
            entity.setMeabaseth(admeExecutiveAgencyInfo.getMeabaseth());
            entity.setInterval_compensation(admeExecutiveAgencyInfo.getInterval_compensation());
            entity.setBottom_safe_distance(admeExecutiveAgencyInfo.getBottom_safe_distance());
            entity.setInterval_fitting(admeExecutiveAgencyInfo.getInterval_fitting());
            entity.setPoint_offset(admeExecutiveAgencyInfo.getPoint_offset());

            command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_EXECUTIVE_AGENCY, entity);
        } catch (Exception ex) {
            command = "";
            ex.printStackTrace();
        }
        return command;
    }

    /**
     * 获取堵转检测配置指令
     */
    public String getLockRotorCommand(boolean isChecked) {
        if (lockedRotorDetectionInfo == null) {
            return null;
        }
        AdmeLockedRotorDetectionEntity entity = new AdmeLockedRotorDetectionEntity();
        entity.setLowtbtss(isChecked ? "1" : "0");
        entity.setNumpput(lockedRotorDetectionInfo.getNumpput());
        entity.setPdajtime(lockedRotorDetectionInfo.getPdajtime());
        entity.setDetintiona(lockedRotorDetectionInfo.getDetintiona());
        entity.setDetintionb(lockedRotorDetectionInfo.getDetintionb());
        entity.setLowtorblothr(lockedRotorDetectionInfo.getLowtorblothr());
        entity.setLowtordetime(lockedRotorDetectionInfo.getLowtordetime());
        entity.setLowsusrana(lockedRotorDetectionInfo.getLowsusrana());
        entity.setLowsusranb(lockedRotorDetectionInfo.getLowsusranb());

        entity.setUptbtss(lockedRotorDetectionInfo.getUptbtss());
        entity.setUptorblothr(lockedRotorDetectionInfo.getUptorblothr());
        entity.setUptordetime(lockedRotorDetectionInfo.getUptordetime());
        entity.setUpsusrana(lockedRotorDetectionInfo.getUpsusrana());
        entity.setUpsusranb(lockedRotorDetectionInfo.getUpsusranb());

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_LOCKED_ROTOR_DETECTION, entity);
        return command;
    }

    /**
     * 获取正反测使能指令
     */
    public String getPositiveAndNegativeCommand(boolean isChecked) {
        AdmeStepperMotorEntity entity = new AdmeStepperMotorEntity();
        entity.setPosnegtest(isChecked ? "1" : "0");

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_STEPPER_MOTOR, entity);
        return command;
    }

    public void initBasicConfigInfo(AdmeBasicConfigInfo info) {
        basicConfigParam = info;
        if (basicConfigParam == null) {
            Timber.e("AdmeBasicConfigParam is Null!");
            basicConfigParam = new AdmeBasicConfigInfo();
            return;
        }
        address = basicConfigParam.getAddress().trim();
        inclinometerTubeHoleDepth = basicConfigParam.getInterdeep().trim();
        decentralizationSpeed = basicConfigParam.getDownspeed().trim();
        decentralizationWaitingTime = basicConfigParam.getDownwaitetime().trim();
        dataSettlementMethod = basicConfigParam.getDatatype().trim();

        mEtMacAddress.setText(address);
        macAddressLayout.setVisibility(View.VISIBLE);
        try {
            if (inclinometerTubeHoleDepth.equals("NullKey")) {
                inclinometerTubeHoleDepthLayout.setVisibility(View.GONE);
            } else {
                decimalFormat.applyPattern("#.##");
                inclinometerTubeHoleDepth = decimalFormat.format(Double.parseDouble(inclinometerTubeHoleDepth));
                mEtInclinometerTubeHoleDepth.setText(inclinometerTubeHoleDepth);
            }
            if (decentralizationSpeed.equals("NullKey")) {
                decentralizationSpeedLayout.setVisibility(View.GONE);
            } else {
                mEtDecentralizationSpeed.setText(decentralizationSpeed);
            }
            if (decentralizationWaitingTime.equals("NullKey")) {
                decentralizationWaitingTimeLayout.setVisibility(View.GONE);
            } else {
                mEtDecentralizationWaitingTime.setText(decentralizationWaitingTime);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (dataSettlementMethod.equals("NullKey")) {
            dataSettlementMethodLayout.setVisibility(View.GONE);
        } else {
            if (dataSettlementMethod.equals("0")) {
                mTvDataSettlementMethod.setText(settlementMethods[0]);
            } else {
                mTvDataSettlementMethod.setText(settlementMethods[1]);
            }
        }
    }

    /**
     * 初始化执行结构参数
     */
    public void initExecutiveAgencyInfo(AdmeExecutiveAgencyInfo info) {
        admeExecutiveAgencyInfo = info;
        if (admeExecutiveAgencyInfo == null) {
            Timber.e("AdmeExecutiveAgencyInfo is Null!");
            admeExecutiveAgencyInfo = new AdmeExecutiveAgencyInfo();
            return;
        }
        measureMethod = admeExecutiveAgencyInfo.getMeastype().trim();
        waitingIntervalPerRound = admeExecutiveAgencyInfo.getRoundwaitetime().trim();
        measurementIntervalPerRound = admeExecutiveAgencyInfo.getRoundmeasinval().trim();
        intervalDays = admeExecutiveAgencyInfo.getInvalday();
        startTimePerRound = admeExecutiveAgencyInfo.getRoundmeasstart().trim();
        if (measureMethod.equals("NullKey")) {
            measureMethodLayout.setVisibility(View.GONE);
            waitingIntervalPerRoundLayout.setVisibility(View.GONE);
            measurementIntervalPerRoundLayout.setVisibility(View.GONE);
            modifiedDateLayout.setVisibility(View.GONE);
            intervalDayLayout.setVisibility(View.GONE);
            startTimePerRoundLayout.setVisibility(View.GONE);
            mRecyclerViewTime.setVisibility(View.GONE);
        } else {
            switch (measureMethod) {
                case "0":
                    mTvMeasureMethod.setText(measureMethods[0]);
                    waitingIntervalPerRoundLayout.setVisibility(View.VISIBLE);
                    measurementIntervalPerRoundLayout.setVisibility(View.GONE);
                    modifiedDateLayout.setVisibility(View.GONE);
                    intervalDayLayout.setVisibility(View.GONE);
                    startTimePerRoundLayout.setVisibility(View.GONE);
                    mRecyclerViewTime.setVisibility(View.GONE);
                    break;
                case "1":
                    mTvMeasureMethod.setText(measureMethods[1]);
                    waitingIntervalPerRoundLayout.setVisibility(View.GONE);
                    measurementIntervalPerRoundLayout.setVisibility(View.VISIBLE);
                    modifiedDateLayout.setVisibility(View.GONE);
                    intervalDayLayout.setVisibility(View.GONE);
                    startTimePerRoundLayout.setVisibility(View.GONE);
                    mRecyclerViewTime.setVisibility(View.GONE);
                    break;
                case "2":
                    mTvMeasureMethod.setText(measureMethods[2]);
                    waitingIntervalPerRoundLayout.setVisibility(View.GONE);
                    measurementIntervalPerRoundLayout.setVisibility(View.GONE);
                    modifiedDateLayout.setVisibility(View.VISIBLE);
                    intervalDayLayout.setVisibility(View.VISIBLE);
                    startTimePerRoundLayout.setVisibility(View.VISIBLE);
                    mRecyclerViewTime.setVisibility(View.VISIBLE);
                    break;
            }
        }

        try {
            if (waitingIntervalPerRound.equals("NullKey")) {
                waitingIntervalPerRoundLayout.setVisibility(View.GONE);
            } else {
                mEtWaitingIntervalPerRound.setText(waitingIntervalPerRound);
            }
            mTvMeasurementIntervalPerRound.setText(measurementIntervalPerRound);

            if (admeExecutiveAgencyInfo.getUpdatedate().equals("NullKey")) {
                modifiedDateLayout.setVisibility(View.GONE);
            } else {
                mTvModifiedDate.setText(admeExecutiveAgencyInfo.getUpdatedate());
            }

            if (intervalDays.equals("NullKey")) {
                intervalDayLayout.setVisibility(View.GONE);
            } else {
                mEtIntervalDay.setText(intervalDays);
            }

            if (!startTimePerRound.equals("NullKey")) {
                AdmeTimeItem item;
                String[] times = startTimePerRound.split("\\|");
                admeTimeAdapter.getData().clear();
                for (String time : times) {
                    if (!TextUtils.isEmpty(time)) {
                        time = String.format(Locale.getDefault(), "%02d:00:00", Integer.parseInt(time));
                        item = new AdmeTimeItem(time);
                        admeTimeAdapter.getData().add(item);
                    }
                }
                admeTimeAdapter.notifyDataSetChanged();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 初始化堵转检测参数
     */
    public void initLockedRotorDetectionInfo(AdmeLockedRotorDetectionInfo info) {
        lockedRotorDetectionInfo = info;
        if (lockedRotorDetectionInfo == null) {
            Timber.e("AdmeLockedRotorDetectionInfo is Null!");
            lockedRotorDetectionInfo = new AdmeLockedRotorDetectionInfo();
            return;
        }
        if (lockedRotorDetectionInfo.getLowtbtss().equals("0")) {
            mSbDecentralizedEnable.setCheckedImmediatelyNoEvent(false);
        } else {
            mSbDecentralizedEnable.setCheckedImmediatelyNoEvent(true);
        }
    }

    /**
     * 初始化正反测使能参数
     */
    public void initPositiveAndNegativeInfo(AdmeStepperMotorInfo admeStepperMotorInfo) {
        if (admeStepperMotorInfo == null) {
            Timber.e("AdmeStepperMotorInfo is Null!");
            return;
        }
        if (admeStepperMotorInfo.getPosnegtest().trim().equals("0")) {
            positiveAndNegativeEnableSBtn.setCheckedImmediatelyNoEvent(false);
        } else {
            positiveAndNegativeEnableSBtn.setCheckedImmediatelyNoEvent(true);
        }
    }

    public void doAfterSetting() {
//        inclinometerTypeOld = inclinometerType;
//        dataSettlementMethodOld = dataSettlementMethod;
    }

    public boolean checkValueIsChange(boolean configPageEditableChanged) {
//        if (!configPageEditableChanged)
//            return false;
//
//        if (inclinometerTypeOld != null && !inclinometerTypeOld.equals("NullKey") && inclinometerType != null && !inclinometerTypeOld.equals(inclinometerType)) {
//            return true;
//        }
//        if (address != null) {
//            if (inclinometerTypeOld.equals("0") && !inclinometerTypeOld.equals("NullKey") && !address.equals(mEtCollectorAddress.getText().toString().trim())) {
//                return true;
//            }
//            if (inclinometerTypeOld.equals("1") && !inclinometerTypeOld.equals("NullKey") && !address.equals(mEtMacAddress.getText().toString().trim())) {
//                return true;
//            }
//        }
//        if (inclinometerTubeHoleDepth != null && !inclinometerTubeHoleDepth.equals("NullKey") && !inclinometerTubeHoleDepth.equals(mEtInclinometerTubeHoleDepth.getText().toString().trim())) {
//            return true;
//        }
//        if (decentralizationSpeed != null && !decentralizationSpeed.equals("NullKey") && !decentralizationSpeed.equals(mEtDecentralizationSpeed.getText().toString().trim())) {
//            return true;
//        }
//        if (decentralizationWaitingTime != null && !decentralizationWaitingTime.equals("NullKey") && !decentralizationWaitingTime.equals(mEtDecentralizationWaitingTime.getText().toString().trim())) {
//            return true;
//        }
//        if (dataSettlementMethodOld != null && !dataSettlementMethodOld.equals("NullKey") && dataSettlementMethod != null && !dataSettlementMethodOld.equals(dataSettlementMethod)) {
//            return true;
//        }
        return false;
    }

    public void onEditableChanged(boolean isEditable) {
        mEtMacAddress.setEnabled(isEditable);
        mEtWaitingIntervalPerRound.setEnabled(isEditable);
        mEtIntervalDay.setEnabled(isEditable);
        mEtInclinometerTubeHoleDepth.setEnabled(isEditable);
        mEtDecentralizationSpeed.setEnabled(isEditable);
        mEtDecentralizationWaitingTime.setEnabled(isEditable);
        dataSettlementMethodLayout.setEnabled(isEditable);
        mSbDecentralizedEnable.setEnabled(isEditable);
        positiveAndNegativeEnableSBtn.setEnabled(isEditable);
        if (isEditable) {
            mEtMacAddress.setHint("XXXXXXXXXXXX");
            mTvMeasureMethod.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_arrow_right, 0);
            mEtWaitingIntervalPerRound.setHint("请输入");
            mEtIntervalDay.setHint("请输入");
            mEtInclinometerTubeHoleDepth.setHint("请输入");
            mEtDecentralizationSpeed.setHint("1-180");
            mEtDecentralizationWaitingTime.setHint("1-32");
            mTvDataSettlementMethod.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_arrow_right, 0);
        } else {
            mEtMacAddress.setHint("");
            mTvMeasureMethod.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mEtWaitingIntervalPerRound.setHint("");
            mEtIntervalDay.setHint("");
            mEtInclinometerTubeHoleDepth.setHint("");
            mEtDecentralizationSpeed.setHint("");
            mEtDecentralizationWaitingTime.setHint("");
            mTvDataSettlementMethod.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
//            initParamConfigInfo();
        }
        mBtnSave.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }
}
