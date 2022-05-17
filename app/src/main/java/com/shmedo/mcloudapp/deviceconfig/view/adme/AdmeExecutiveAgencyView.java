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
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeExecutiveAgencyEntity;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.adme.AdmeExecutiveAgencyInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.RecycleViewDivider;
import com.shmedo.mcloudapp.deviceconfig.adapter.AdmeTimeAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.AdmeTimeItem;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/7/12 <br/>
 * 描述：     ADME 执行结构参数配置项视图
 */
public class AdmeExecutiveAgencyView extends LinearLayout {
    @BindView(R.id.tv_measure_method)
    TextView mTvMeasureMethod;//测量方式

    @BindView(R.id.tv_data_settlement_method)
    TextView mTvDataSettlementMethod;

    @BindView(R.id.tv_data_response)
    TextView mTvDataResponse;//数据应答

    @BindView(R.id.tv_measurement_interval_per_round)
    TextView mTvMeasurementIntervalPerRound;//每轮测量间隔

    @BindView(R.id.et_waiting_interval_per_round)
    ClearEditText mEtWaitingIntervalPerRound;//每轮等待时间

    @BindView(R.id.recyclerview_time)
    RecyclerView mRecyclerViewTime;

    @BindView(R.id.et_data_reading_interval)
    ClearEditText mEtDataReadingInterval;//数据读取间隔

    @BindView(R.id.et_measurement_compensation_time)
    ClearEditText mEtMeasurementCompensationTime;

    @BindView(R.id.et_motor_drive_address)
    ClearEditText mEtMotorDriveAddress;//电机驱动器地址

    @BindView(R.id.et_decentralization_speed)
    ClearEditText mEtDecentralizationSpeed;//下放速度(r/min)

    @BindView(R.id.et_inclination_tube_hole_depth)
    ClearEditText mEtInclinometerTubeHoleDepth;//测斜管孔深(m)

    @BindView(R.id.et_decentralization_waiting_time)
    ClearEditText mEtDecentralizationWaitingTime;//下放等待时间(min)

    @BindView(R.id.et_pull_up_speed)
    ClearEditText mEtPullUpSpeed;//电机上拉速度

    @BindView(R.id.et_measuring_distance)
    ClearEditText mEtMeasuringDistance;//测量间距

    @BindView(R.id.et_measurement_interval_time)
    ClearEditText mEtMeasurementIntervalTime;//测量间隔时间

    @BindView(R.id.et_measuring_reference_depth)
    ClearEditText mEtMeasuringReferenceDepth;//测量基准深度

    @BindView(R.id.et_interval_compensation)
    ClearEditText mEtIntervalCompensation;//距离补偿区间h1

    @BindView(R.id.et_interval_fitting)
    ClearEditText mEtIntervalFitting;//数据拟合区间h2

    @BindView(R.id.et_point_offset)
    ClearEditText mEtPointOffset;//测点偏移距离h3

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    @BindView(R.id.ll_measure_method)
    ViewGroup measureMethodLayout;

    @BindView(R.id.ll_data_settlement_method)
    ViewGroup dataSettlementMethodLayout;

    @BindView(R.id.ll_data_response)
    ViewGroup dataResponseLayout;

    @BindView(R.id.ll_measurement_interval_per_round)
    ViewGroup measurementIntervalPerRoundLayout;

    @BindView(R.id.ll_start_time_per_round)
    ViewGroup startTimePerRoundLayout;

    @BindView(R.id.ll_waiting_interval_per_round)
    ViewGroup waitingIntervalPerRoundLayout;

    @BindView(R.id.ll_data_reading_interval)
    ViewGroup dataReadingIntervalLayout;

    @BindView(R.id.ll_measurement_compensation_time)
    ViewGroup measurementCompensationTimeLayout;

    @BindView(R.id.ll_motor_drive_address)
    ViewGroup motorDriveAddressLayout;

    @BindView(R.id.ll_decentralization_speed)
    ViewGroup decentralizationSpeedLayout;

    @BindView(R.id.ll_inclination_tube_hole_depth)
    ViewGroup inclinationTubeHoleDepthLayout;

    @BindView(R.id.ll_decentralization_waiting_time)
    ViewGroup decentralizationWaitingTimeLayout;

    @BindView(R.id.ll_pull_up_speed)
    ViewGroup pullUpSpeedLayout;

    @BindView(R.id.ll_measuring_distance)
    ViewGroup measuringDistanceLayout;

    @BindView(R.id.ll_measurement_interval_time)
    ViewGroup measurementIntervalTimeLayout;

    @BindView(R.id.ll_measuring_reference_depth)
    ViewGroup measuringReferenceDepthLayout;

    @BindView(R.id.ll_interval_compensation)
    ViewGroup intervalCompensationLayout;

    @BindView(R.id.ll_interval_fitting)
    ViewGroup intervalFittingLayout;

    @BindView(R.id.ll_point_offset)
    ViewGroup pointOffsetLayout;


    private String measureMethodOld;//测量方式
    private String measureMethod;//测量方式 （0:实时测量，1:整时整点测量，2:定时定点测量)
    private String dataSettlementMethodOld;//数据结算方式
    private String dataSettlementMethod;// 数据结算方式
    private String dataResponseOld;//数据应答（0:关闭，1:启用）
    private String dataResponse;// 数据应答（0:关闭，1:启用）
    private String measurementIntervalPerRoundOld;// 每轮测量间隔
    private String measurementIntervalPerRound;// 每轮测量间隔
    private String waitingIntervalPerRound;// 每轮等待时间
    private String startTimePerRound;// 每轮测量开始时间
    private String dataReadingInterval;// 数据读取间隔
    private String measurementCompensationTime;// 测量补偿时间
    private String motorDriveAddress;// 电机驱动器地址
    private String decentralizationSpeed;// 下放速度(r/min)
    private String inclinometerTubeHoleDepth;// 测斜管孔深(m)
    private String decentralizationWaitingTime;//下放等待时间(min)
    private String pullUpSpeed;// 电机上拉速度
    private String measuringDistance;// 测量间距
    private String measurementIntervalTime;// 测量间隔时间
    private String measuringReferenceDepth;// 测量基准深度
    private String intervalCompensation;// 距离补偿区间h1
    private String intervalFitting;// 数据拟合区间h2
    private String pointOffset;// 测点偏移距离h3

    private final String[] measureMethods = new String[]{"实时测量", "整时整点测量", "定时定点测量"};
    private final String[] settlementMethods = new String[]{"顶固定法", "底固定法"};
    private final String[] dataResponseTypes = new String[]{"关闭", "启用"};
    private final String[] measIntervalPerRounds = new String[]{"1", "2", "3", "4", "6", "8", "12", "24"};

    private AdmeTimeAdapter admeTimeAdapter;
    private List<AdmeTimeItem> admeTimeItemList = new ArrayList<>();

    private DecimalFormat decimalFormat = new DecimalFormat();
    public AdmeExecutiveAgencyInfo admeExecutiveAgencyInfo;


    public AdmeExecutiveAgencyView(Context context) {
        this(context, null);
    }

    public AdmeExecutiveAgencyView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdmeExecutiveAgencyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.adme_executive_agency_view, this, true);
        ButterKnife.bind(this);
        initView();
        initTimeAdapter(context);
        AdmeTimeItem item = new AdmeTimeItem(null, true);
        admeTimeItemList.add(item);
        admeTimeAdapter.notifyDataSetChanged();
    }

    private void initView() {
        mEtWaitingIntervalPerRound.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtDataReadingInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtMeasurementCompensationTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtMotorDriveAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtDecentralizationSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtDecentralizationSpeed.setHint("1-180");

        mEtInclinometerTubeHoleDepth.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtDecentralizationWaitingTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtDecentralizationWaitingTime.setHint("1-32");

        mEtPullUpSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtPullUpSpeed.setHint("1-180");

        mEtMeasuringDistance.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtMeasurementIntervalTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtMeasuringReferenceDepth.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});

        mTvMeasureMethod.setText("实时测量");
        measureMethodOld = "0";

        mTvDataSettlementMethod.setText("顶固定法");
        dataSettlementMethodOld = "0";

        mTvDataResponse.setText("关闭");
        dataResponseOld = "0";

        mTvMeasurementIntervalPerRound.setText("1");
        measurementIntervalPerRoundOld = "1";
    }

    private void initTimeAdapter(Context context) {
        mRecyclerViewTime.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        mRecyclerViewTime.addItemDecoration(new RecycleViewDivider(LinearLayoutManager.VERTICAL, ConvertUtils.dp2px(0.5f), getResources().getColor(R.color.divider_line_bg_efefef)));
        admeTimeAdapter = new AdmeTimeAdapter(admeTimeItemList);
        admeTimeAdapter.setAnimationEnable(false);
        admeTimeAdapter.setAnimationFirstOnly(false);
        admeTimeAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                processItemClick(context, position);
            }
        });
        admeTimeAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                AdmeTimeItem admeTimeItem = admeTimeItemList.get(position);
                if (admeTimeItem.isAddButton()) {
                    return true;
                }
                warnDeleteSensorItem(context, position);
                return true;
            }
        });
        mRecyclerViewTime.setAdapter(admeTimeAdapter);
    }

    private void processItemClick(Context context, int position) {
        AdmeTimeItem admeTimeItem = admeTimeItemList.get(position);
        if (!admeTimeItem.isAddButton())
            return;

        new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String time = String.format(Locale.getDefault(), "%02d:00:00", hourOfDay);
                for(AdmeTimeItem item :admeTimeItemList) {
                    if (item.getTime().contains(time)) {
                        ToastUtils.show("不能设置重复时间点!");
                        return;
                    }
                }
                admeTimeItemList.remove(admeTimeItemList.size() - 1);
                AdmeTimeItem item = new AdmeTimeItem(time, false);
                admeTimeItemList.add(item);
                if (admeTimeItemList.size() < 8) {
                    item = new AdmeTimeItem(null, true);
                    admeTimeItemList.add(item);
                }
                admeTimeAdapter.notifyDataSetChanged();
            }
        }, 0, 0, true).show();
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
                        admeTimeItemList.remove(position);
                        admeTimeItemList.remove(admeTimeItemList.size() - 1);
                        if (admeTimeItemList.size() < 8) {
                            AdmeTimeItem item = new AdmeTimeItem(null, true);
                            admeTimeItemList.add(item);
                        }
                        admeTimeAdapter.notifyDataSetChanged();
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
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(context)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", measureMethods,
                        null, pos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvMeasureMethod.setText(text);
                                measureMethod = String.valueOf(position);
                                if (position == 0) {
                                    waitingIntervalPerRoundLayout.setVisibility(View.VISIBLE);
                                    measurementIntervalPerRoundLayout.setVisibility(View.GONE);
                                    startTimePerRoundLayout.setVisibility(View.GONE);
                                    mRecyclerViewTime.setVisibility(View.GONE);
                                } else if (position == 1) {
                                    waitingIntervalPerRoundLayout.setVisibility(View.GONE);
                                    measurementIntervalPerRoundLayout.setVisibility(View.VISIBLE);
                                    startTimePerRoundLayout.setVisibility(View.GONE);
                                    mRecyclerViewTime.setVisibility(View.GONE);
                                } else if (position == 2) {
                                    waitingIntervalPerRoundLayout.setVisibility(View.GONE);
                                    measurementIntervalPerRoundLayout.setVisibility(View.GONE);
                                    startTimePerRoundLayout.setVisibility(View.VISIBLE);
                                    mRecyclerViewTime.setVisibility(View.VISIBLE);
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    /**
     * 选择数据结算方式
     */
    public void showDataSettlementMethodDialog(Context context) {
        int pos = Arrays.asList(settlementMethods).indexOf(String.valueOf(mTvDataSettlementMethod.getText()));
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(context)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", settlementMethods,
                        null, pos, true,
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

    /**
     * 选择数据应答方式
     */
    public void showDataResponseDialog(Context context) {
        int pos = Arrays.asList(dataResponseTypes).indexOf(String.valueOf(mTvDataResponse.getText()));
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(context)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", dataResponseTypes,
                        null, pos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvDataResponse.setText(text);
                                if (position == 0) {
                                    dataResponse = "0";
                                } else {
                                    dataResponse = "1";
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
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(context)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", measIntervalPerRounds,
                        null, pos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvMeasurementIntervalPerRound.setText(text);
                                measurementIntervalPerRound = text;
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    public boolean checkValueIsValid() {
        waitingIntervalPerRound = mEtWaitingIntervalPerRound.getText().toString().trim();
        dataReadingInterval = mEtDataReadingInterval.getText().toString().trim();
        measurementCompensationTime = mEtMeasurementCompensationTime.getText().toString().trim();
        motorDriveAddress = mEtMotorDriveAddress.getText().toString().trim();
        decentralizationSpeed = mEtDecentralizationSpeed.getText().toString().trim();
        inclinometerTubeHoleDepth = mEtInclinometerTubeHoleDepth.getText().toString().trim();
        decentralizationWaitingTime = mEtDecentralizationWaitingTime.getText().toString().trim();
        pullUpSpeed = mEtPullUpSpeed.getText().toString().trim();
        measuringDistance = mEtMeasuringDistance.getText().toString().trim();
        measurementIntervalTime = mEtMeasurementIntervalTime.getText().toString().trim();
        measuringReferenceDepth = mEtMeasuringReferenceDepth.getText().toString().trim();
        intervalCompensation = mEtIntervalCompensation.getText().toString().trim();
        intervalFitting = mEtIntervalFitting.getText().toString().trim();
        pointOffset = mEtPointOffset.getText().toString().trim();

        if (!admeExecutiveAgencyInfo.getRoundwaitetime().equals("NullKey")) {
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
        if (!admeExecutiveAgencyInfo.getDatainval().equals("NullKey")) {
            if (TextUtils.isEmpty(dataReadingInterval)) {
                ToastUtils.show("请输入数据读取间隔!");
                mEtDataReadingInterval.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(dataReadingInterval);
                if (value < 1) {
                    ToastUtils.show("请输入正确的数据读取间隔!");
                    mEtDataReadingInterval.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的数据读取间隔!");
                mEtDataReadingInterval.requestFocus();
                return false;
            }
        }
        if (!admeExecutiveAgencyInfo.getCompensatetime().equals("NullKey")) {
            if (TextUtils.isEmpty(measurementCompensationTime)) {
                ToastUtils.show("请输入测量补偿时间!");
                mEtMeasurementCompensationTime.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(measurementCompensationTime);
                if (value < 1) {
                    ToastUtils.show("请输入正确的测量补偿时间!");
                    mEtMeasurementCompensationTime.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的测量补偿时间!");
                mEtMeasurementCompensationTime.requestFocus();
                return false;
            }
        }
        if (!admeExecutiveAgencyInfo.getDriveaddress().equals("NullKey")) {
            if (TextUtils.isEmpty(motorDriveAddress)) {
                ToastUtils.show("请输入电机驱动器地址!");
                mEtMotorDriveAddress.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(motorDriveAddress);
                if (value < 0 || value > 99) {
                    ToastUtils.show("请输入正确的电机驱动器地址!");
                    mEtMotorDriveAddress.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的电机驱动器地址!");
                mEtMotorDriveAddress.requestFocus();
                return false;
            }
        }
        if (!admeExecutiveAgencyInfo.getDownspeed().equals("NullKey")) {
            if (TextUtils.isEmpty(decentralizationSpeed)) {
                ToastUtils.show("请输入电机下放速度!");
                mEtDecentralizationSpeed.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(decentralizationSpeed);
                if (value < 1 || value > 180) {
                    ToastUtils.show("请输入正确的电机下放速度!");
                    mEtDecentralizationSpeed.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的电机下放速度!");
                mEtDecentralizationSpeed.requestFocus();
                return false;
            }
        }
        if (!admeExecutiveAgencyInfo.getInterdeep().equals("NullKey")) {
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
        if (!admeExecutiveAgencyInfo.getDownwaitetime().equals("NullKey")) {
            if (TextUtils.isEmpty(decentralizationWaitingTime)) {
                ToastUtils.show("请输入下放等待时间!");
                mEtDecentralizationWaitingTime.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(decentralizationWaitingTime);
                if (value < 1 || value > 32) {
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
        if (!admeExecutiveAgencyInfo.getUpspeed().equals("NullKey")) {
            if (TextUtils.isEmpty(pullUpSpeed)) {
                ToastUtils.show("请输入电机上拉速度!");
                mEtPullUpSpeed.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(pullUpSpeed);
                if (value < 1 || value > 180) {
                    ToastUtils.show("请输入正确的电机上拉速度!");
                    mEtPullUpSpeed.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的电机上拉速度!");
                mEtPullUpSpeed.requestFocus();
                return false;
            }
        }
        if (!admeExecutiveAgencyInfo.getMeaspacing().equals("NullKey")) {
            if (TextUtils.isEmpty(measuringDistance)) {
                ToastUtils.show("请输入测量间距!");
                mEtMeasuringDistance.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(measuringDistance);
                if (value < 1) {
                    ToastUtils.show("请输入正确的测量间距!");
                    mEtMeasuringDistance.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的测量间距!");
                mEtMeasuringDistance.requestFocus();
                return false;
            }
        }
        if (!admeExecutiveAgencyInfo.getMeaintertime().equals("NullKey")) {
            if (TextUtils.isEmpty(measurementIntervalTime)) {
                ToastUtils.show("请输入测量间隔时间!");
                mEtMeasurementIntervalTime.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(measurementIntervalTime);
                if (value < 1) {
                    ToastUtils.show("请输入正确的测量间隔时间!");
                    mEtMeasurementIntervalTime.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的测量间隔时间!");
                mEtMeasurementIntervalTime.requestFocus();
                return false;
            }
        }
        if (!admeExecutiveAgencyInfo.getMeabaseth().equals("NullKey")) {
            if (TextUtils.isEmpty(measuringReferenceDepth)) {
                ToastUtils.show("请输入测量基准深度!");
                mEtMeasuringReferenceDepth.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(measuringReferenceDepth);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的测量基准深度!");
                mEtMeasuringReferenceDepth.requestFocus();
                return false;
            }
        }
        if (!admeExecutiveAgencyInfo.getInterval_compensation().equals("NullKey")) {
            if (TextUtils.isEmpty(intervalCompensation)) {
                ToastUtils.show("请输入距离补偿区间h1!");
                mEtIntervalCompensation.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(intervalCompensation);
                if (value <= -10 || value >= 10) {
                    ToastUtils.show("请输入正确的距离补偿区间h1!");
                    mEtIntervalCompensation.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的距离补偿区间h1!");
                mEtIntervalCompensation.requestFocus();
                return false;
            }
        }
        if (!admeExecutiveAgencyInfo.getInterval_fitting().equals("NullKey")) {
            if (TextUtils.isEmpty(intervalFitting)) {
                ToastUtils.show("请输入数据拟合区间h2!");
                mEtIntervalFitting.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(intervalFitting);
                if (value < 0 || value >= 10) {
                    ToastUtils.show("请输入正确的数据拟合区间h2!");
                    mEtIntervalFitting.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的数据拟合区间h2!");
                mEtIntervalFitting.requestFocus();
                return false;
            }
        }
        if (!admeExecutiveAgencyInfo.getPoint_offset().equals("NullKey")) {
            if (TextUtils.isEmpty(pointOffset)) {
                ToastUtils.show("请输入测点偏移距离h3!");
                mEtPointOffset.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(pointOffset);
                if (value < 0 || value >= 0.5) {
                    ToastUtils.show("请输入正确的测点偏移距离h3!");
                    mEtPointOffset.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的测点偏移距离h3!");
                mEtPointOffset.requestFocus();
                return false;
            }
        }

        return true;
    }

    public String getSetCommand() {
        String command = "";
        try {
            AdmeExecutiveAgencyEntity entity = new AdmeExecutiveAgencyEntity();
            entity.setMeastype(admeExecutiveAgencyInfo.getMeastype().equals("NullKey") ? "NullKey" : measureMethod);
            entity.setDatatype(admeExecutiveAgencyInfo.getDatatype().equals("NullKey") ? "NullKey" : dataSettlementMethod);
            entity.setDatareply(admeExecutiveAgencyInfo.getDatareply().equals("NullKey") ? "NullKey" : dataResponse);
            entity.setRoundwaitetime(admeExecutiveAgencyInfo.getRoundwaitetime().equals("NullKey") ? "NullKey" : waitingIntervalPerRound);
            entity.setRoundmeasinval(admeExecutiveAgencyInfo.getRoundmeasinval().equals("NullKey") ? "NullKey" : measurementIntervalPerRound);
            //定时测量方式
            if (!admeExecutiveAgencyInfo.getMeastype().equals("NullKey") && admeExecutiveAgencyInfo.getMeastype().equals("2")) {
                StringBuffer timeBuffer = new StringBuffer();
                for (AdmeTimeItem admeTimeItem : admeTimeItemList) {
                    String time = admeTimeItem.getTime();
                    if (!TextUtils.isEmpty(time)) {
                        timeBuffer.append(Integer.parseInt(time.substring(0, time.indexOf(":"))));
                        timeBuffer.append("|");
                    }
                }
                timeBuffer.delete(timeBuffer.length() - 1, timeBuffer.length());
                entity.setRoundmeasstart(timeBuffer.toString());

            } else {
                entity.setRoundmeasstart(admeExecutiveAgencyInfo.getRoundmeasstart().equals("NullKey") ? "NullKey" : admeExecutiveAgencyInfo.getRoundmeasstart());
            }
            entity.setDatainval(admeExecutiveAgencyInfo.getDatainval().equals("NullKey") ? "NullKey" : dataReadingInterval);
            entity.setCompensatetime(admeExecutiveAgencyInfo.getCompensatetime().equals("NullKey") ? "NullKey" : measurementCompensationTime);
            entity.setDriveaddress(admeExecutiveAgencyInfo.getDriveaddress().equals("NullKey") ? "NullKey" : motorDriveAddress);
            entity.setDownspeed(admeExecutiveAgencyInfo.getDownspeed().equals("NullKey") ? "NullKey" : decentralizationSpeed);
            decimalFormat.applyPattern("#.##");
            entity.setInterdeep(admeExecutiveAgencyInfo.getInterdeep().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(inclinometerTubeHoleDepth)));
            entity.setDownwaitetime(admeExecutiveAgencyInfo.getDownwaitetime().equals("NullKey") ? "NullKey" : decentralizationWaitingTime);
            entity.setUpspeed(admeExecutiveAgencyInfo.getUpspeed().equals("NullKey") ? "NullKey" : pullUpSpeed);
            entity.setMeaspacing(admeExecutiveAgencyInfo.getMeaspacing().equals("NullKey") ? "NullKey" : measuringDistance);
            entity.setMeaintertime(admeExecutiveAgencyInfo.getMeaintertime().equals("NullKey") ? "NullKey" : measurementIntervalTime);
            decimalFormat.applyPattern("#.##");
            entity.setMeabaseth(admeExecutiveAgencyInfo.getMeabaseth().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(measuringReferenceDepth)));
            decimalFormat.applyPattern("#.###");
            entity.setInterval_compensation(admeExecutiveAgencyInfo.getInterval_compensation().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(intervalCompensation)));
            decimalFormat.applyPattern("#.#");
            entity.setInterval_fitting(admeExecutiveAgencyInfo.getInterval_fitting().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(intervalFitting)));
            decimalFormat.applyPattern("#.###");
            entity.setPoint_offset(admeExecutiveAgencyInfo.getPoint_offset().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(pointOffset)));

            command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_EXECUTIVE_AGENCY, entity);
        } catch (Exception ex) {
            command = "";
            ex.printStackTrace();
        }
        return command;
    }

    public void initParamConfigInfo() {
        if (admeExecutiveAgencyInfo == null) {
            Timber.e("AdmeExecutiveAgencyInfo is Null!");
            admeExecutiveAgencyInfo = new AdmeExecutiveAgencyInfo();
            return;
        }
        measureMethodOld = admeExecutiveAgencyInfo.getMeastype().trim();
        measureMethod = measureMethodOld;
        dataSettlementMethodOld = admeExecutiveAgencyInfo.getDatatype().trim();
        dataSettlementMethod = dataSettlementMethodOld;
        dataResponseOld = admeExecutiveAgencyInfo.getDatareply().trim();
        dataResponse = dataResponseOld;
        measurementIntervalPerRoundOld = admeExecutiveAgencyInfo.getRoundmeasinval().trim();
        measurementIntervalPerRound = measurementIntervalPerRoundOld;
        startTimePerRound = admeExecutiveAgencyInfo.getRoundmeasstart().trim();

        waitingIntervalPerRound = admeExecutiveAgencyInfo.getRoundwaitetime().trim();
        dataReadingInterval = admeExecutiveAgencyInfo.getDatainval().trim();
        measurementCompensationTime = admeExecutiveAgencyInfo.getCompensatetime().trim();
        motorDriveAddress = admeExecutiveAgencyInfo.getDriveaddress().trim();
        decentralizationSpeed = admeExecutiveAgencyInfo.getDownspeed().trim();
        inclinometerTubeHoleDepth = admeExecutiveAgencyInfo.getInterdeep().trim();
        decentralizationWaitingTime = admeExecutiveAgencyInfo.getDownwaitetime().trim();
        pullUpSpeed = admeExecutiveAgencyInfo.getUpspeed().trim();
        measuringDistance = admeExecutiveAgencyInfo.getMeaspacing().trim();
        measurementIntervalTime = admeExecutiveAgencyInfo.getMeaintertime().trim();
        measuringReferenceDepth = admeExecutiveAgencyInfo.getMeabaseth().trim();
        intervalCompensation = admeExecutiveAgencyInfo.getInterval_compensation().trim();
        intervalFitting = admeExecutiveAgencyInfo.getInterval_fitting().trim();
        pointOffset = admeExecutiveAgencyInfo.getPoint_offset().trim();

        if (measureMethodOld.equals("NullKey")) {
            measureMethodLayout.setVisibility(View.GONE);
            measurementIntervalPerRoundLayout.setVisibility(View.GONE);
            startTimePerRoundLayout.setVisibility(View.GONE);
            mRecyclerViewTime.setVisibility(View.GONE);
        } else {
            switch (measureMethodOld) {
                case "0":
                    mTvMeasureMethod.setText(measureMethods[0]);
                    waitingIntervalPerRoundLayout.setVisibility(View.VISIBLE);
                    measurementIntervalPerRoundLayout.setVisibility(View.GONE);
                    startTimePerRoundLayout.setVisibility(View.GONE);
                    mRecyclerViewTime.setVisibility(View.GONE);
                    break;
                case "1":
                    mTvMeasureMethod.setText(measureMethods[1]);
                    waitingIntervalPerRoundLayout.setVisibility(View.GONE);
                    measurementIntervalPerRoundLayout.setVisibility(View.VISIBLE);
                    startTimePerRoundLayout.setVisibility(View.GONE);
                    mRecyclerViewTime.setVisibility(View.GONE);
                    break;
                case "2":
                    mTvMeasureMethod.setText(measureMethods[2]);
                    waitingIntervalPerRoundLayout.setVisibility(View.GONE);
                    measurementIntervalPerRoundLayout.setVisibility(View.GONE);
                    startTimePerRoundLayout.setVisibility(View.VISIBLE);
                    mRecyclerViewTime.setVisibility(View.VISIBLE);
                    break;
            }
        }

        if (dataSettlementMethodOld.equals("NullKey")) {
            dataSettlementMethodLayout.setVisibility(View.GONE);
        } else {
            if (dataSettlementMethodOld.equals("0")) {
                mTvDataSettlementMethod.setText(settlementMethods[0]);
            } else {
                mTvDataSettlementMethod.setText(settlementMethods[1]);
            }
        }

        if (dataResponseOld.equals("NullKey")) {
            dataResponseLayout.setVisibility(View.GONE);
        } else {
            if (dataResponseOld.equals("0")) {
                mTvDataResponse.setText(dataResponseTypes[0]);
            } else {
                mTvDataResponse.setText(dataResponseTypes[1]);
            }
        }

        try {
            mTvMeasurementIntervalPerRound.setText(measurementIntervalPerRoundOld);

            if (!startTimePerRound.equals("NullKey")) {
                AdmeTimeItem item;
                String[] times = startTimePerRound.split("\\|");
                admeTimeItemList.clear();
                for (String time : times) {
                    if (!TextUtils.isEmpty(time)) {
                        time = String.format(Locale.getDefault(), "%02d:00:00", Integer.parseInt(time));
                        item = new AdmeTimeItem(time, false);
                        admeTimeItemList.add(item);
                    }
                }
                if (admeTimeItemList.size() < 8) {
                    item = new AdmeTimeItem(null, true);
                    admeTimeItemList.add(item);
                }
                admeTimeAdapter.notifyDataSetChanged();
            }
            if (waitingIntervalPerRound.equals("NullKey")) {
                waitingIntervalPerRoundLayout.setVisibility(View.GONE);
            } else {
                mEtWaitingIntervalPerRound.setText(waitingIntervalPerRound);
            }
            if (dataReadingInterval.equals("NullKey")) {
                dataReadingIntervalLayout.setVisibility(View.GONE);
            } else {
                mEtDataReadingInterval.setText(dataReadingInterval);
            }
            if (measurementCompensationTime.equals("NullKey")) {
                measurementCompensationTimeLayout.setVisibility(View.GONE);
            } else {
                mEtMeasurementCompensationTime.setText(measurementCompensationTime);
            }
            if (motorDriveAddress.equals("NullKey")) {
                motorDriveAddressLayout.setVisibility(View.GONE);
            } else {
                mEtMotorDriveAddress.setText(motorDriveAddress);
            }
            decimalFormat.applyPattern("#.##");
            if (decentralizationSpeed.equals("NullKey")) {
                decentralizationSpeedLayout.setVisibility(View.GONE);
            } else {
                mEtDecentralizationSpeed.setText(decentralizationSpeed);
            }
            if (inclinometerTubeHoleDepth.equals("NullKey")) {
                inclinationTubeHoleDepthLayout.setVisibility(View.GONE);
            } else {
                inclinometerTubeHoleDepth = decimalFormat.format(Double.parseDouble(inclinometerTubeHoleDepth));
                mEtInclinometerTubeHoleDepth.setText(inclinometerTubeHoleDepth);
            }
            if (decentralizationWaitingTime.equals("NullKey")) {
                decentralizationWaitingTimeLayout.setVisibility(View.GONE);
            } else {
                mEtDecentralizationWaitingTime.setText(decentralizationWaitingTime);
            }
            if (pullUpSpeed.equals("NullKey")) {
                pullUpSpeedLayout.setVisibility(View.GONE);
            } else {
                mEtPullUpSpeed.setText(pullUpSpeed);
            }
            if (measuringDistance.equals("NullKey")) {
                measuringDistanceLayout.setVisibility(View.GONE);
            } else {
                measuringDistance = decimalFormat.format(Double.parseDouble(measuringDistance));
                mEtMeasuringDistance.setText(measuringDistance);
            }
            if (measurementIntervalTime.equals("NullKey")) {
                measurementIntervalTimeLayout.setVisibility(View.GONE);
            } else {
                mEtMeasurementIntervalTime.setText(measurementIntervalTime);
            }
            decimalFormat.applyPattern("#.##");
            if (measuringReferenceDepth.equals("NullKey")) {
                measuringReferenceDepthLayout.setVisibility(View.GONE);
            } else {
                measuringReferenceDepth = decimalFormat.format(Double.parseDouble(measuringReferenceDepth));
                mEtMeasuringReferenceDepth.setText(measuringReferenceDepth);
            }
            decimalFormat.applyPattern("#.###");
            if (intervalCompensation.equals("NullKey")) {
                intervalCompensationLayout.setVisibility(View.GONE);
            } else {
                intervalCompensation = decimalFormat.format(Double.parseDouble(intervalCompensation));
                mEtIntervalCompensation.setText(intervalCompensation);
            }
            decimalFormat.applyPattern("#.#");
            if (intervalFitting.equals("NullKey")) {
                intervalFittingLayout.setVisibility(View.GONE);
            } else {
                intervalFitting = decimalFormat.format(Double.parseDouble(intervalFitting));
                mEtIntervalFitting.setText(intervalFitting);
            }
            decimalFormat.applyPattern("#.###");
            if (pointOffset.equals("NullKey")) {
                pointOffsetLayout.setVisibility(View.GONE);
            } else {
                pointOffset = decimalFormat.format(Double.parseDouble(pointOffset));
                mEtPointOffset.setText(pointOffset);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void doAfterSetting() {
//        if (admeExecutiveAgencyInfo != null) {
//            admeExecutiveAgencyInfo.setDatatype(dataSettlementMethod);
//            admeExecutiveAgencyInfo.setDatareply(dataResponse);
//            admeExecutiveAgencyInfo.setRoundwaitetime(waitingIntervalPerRound);
//            admeExecutiveAgencyInfo.setDatainval(dataReadingInterval);
//            admeExecutiveAgencyInfo.setCompensatetime(measurementCompensationTime);
//            admeExecutiveAgencyInfo.setDriveaddress(motorDriveAddress);
//            admeExecutiveAgencyInfo.setDownspeed(decentralizationSpeed);
//            admeExecutiveAgencyInfo.setInterdeep(inclinometerTubeHoleDepth);
//            admeExecutiveAgencyInfo.setDownwaitetime(decentralizationWaitingTime);
//            admeExecutiveAgencyInfo.setUpspeed(pullUpSpeed);
//            admeExecutiveAgencyInfo.setMeaspacing(measuringDistance);
//            admeExecutiveAgencyInfo.setMeaintertime(measurementIntervalTime);
//            admeExecutiveAgencyInfo.setMeabaseth(measuringReferenceDepth);
//            admeExecutiveAgencyInfo.setInterval_compensation(intervalCompensation);
//            admeExecutiveAgencyInfo.setInterval_fitting(intervalFitting);
//            admeExecutiveAgencyInfo.setPoint_offset(pointOffset);
//        }
        //TODO  打开注释，设置为浏览模式
//        configPageViewModel.configPageEditableChanged.setValue(false);
        dataSettlementMethodOld = dataSettlementMethod;
        dataResponseOld = dataResponse;
    }

    public boolean checkValueIsChange(boolean configPageEditableChanged) {
        if (!configPageEditableChanged)
            return false;

        if (measureMethodOld != null && !measureMethodOld.equals("NullKey") && measureMethod != null && !measureMethodOld.equals(measureMethod)) {
            return true;
        }
        if (dataSettlementMethodOld != null && !dataSettlementMethodOld.equals("NullKey") && dataSettlementMethod != null && !dataSettlementMethodOld.equals(dataSettlementMethod)) {
            return true;
        }
        if (dataResponseOld != null && !dataResponseOld.equals("NullKey") && dataResponse != null && !dataResponseOld.equals(dataResponse)) {
            return true;
        }
        if (measurementIntervalPerRoundOld != null && !measurementIntervalPerRoundOld.equals("NullKey") && measurementIntervalPerRound != null && !measurementIntervalPerRoundOld.equals(measurementIntervalPerRound)) {
            return true;
        }
        if (waitingIntervalPerRound != null && !waitingIntervalPerRound.equals("NullKey") && !waitingIntervalPerRound.equals(mEtWaitingIntervalPerRound.getText().toString().trim())) {
            return true;
        }
        if (dataReadingInterval != null && !dataReadingInterval.equals("NullKey") && !dataReadingInterval.equals(mEtDataReadingInterval.getText().toString().trim())) {
            return true;
        }
        if (measurementCompensationTime != null && !measurementCompensationTime.equals("NullKey") && !measurementCompensationTime.equals(mEtMeasurementCompensationTime.getText().toString().trim())) {
            return true;
        }
        if (motorDriveAddress != null && !motorDriveAddress.equals("NullKey") && !motorDriveAddress.equals(mEtMotorDriveAddress.getText().toString().trim())) {
            return true;
        }
        if (decentralizationSpeed != null && !decentralizationSpeed.equals("NullKey") && !decentralizationSpeed.equals(mEtDecentralizationSpeed.getText().toString().trim())) {
            return true;
        }
        if (inclinometerTubeHoleDepth != null && !inclinometerTubeHoleDepth.equals("NullKey") && !inclinometerTubeHoleDepth.equals(mEtInclinometerTubeHoleDepth.getText().toString().trim())) {
            return true;
        }
        if (decentralizationWaitingTime != null && !decentralizationWaitingTime.equals("NullKey") && !decentralizationWaitingTime.equals(mEtDecentralizationWaitingTime.getText().toString().trim())) {
            return true;
        }
        if (pullUpSpeed != null && !pullUpSpeed.equals("NullKey") && !pullUpSpeed.equals(mEtPullUpSpeed.getText().toString().trim())) {
            return true;
        }
        if (measuringDistance != null && !measuringDistance.equals("NullKey") && !measuringDistance.equals(mEtMeasuringDistance.getText().toString().trim())) {
            return true;
        }
        if (measurementIntervalTime != null && !measurementIntervalTime.equals("NullKey") && !measurementIntervalTime.equals(mEtMeasurementIntervalTime.getText().toString().trim())) {
            return true;
        }
        if (measuringReferenceDepth != null && !measuringReferenceDepth.equals("NullKey") && !measuringReferenceDepth.equals(mEtMeasuringReferenceDepth.getText().toString().trim())) {
            return true;
        }
        if (intervalCompensation != null && !intervalCompensation.equals("NullKey") && !intervalCompensation.equals(mEtIntervalCompensation.getText().toString().trim())) {
            return true;
        }
        if (intervalFitting != null && !intervalFitting.equals("NullKey") && !intervalFitting.equals(mEtIntervalFitting.getText().toString().trim())) {
            return true;
        }
        if (pointOffset != null && !pointOffset.equals("NullKey") && !pointOffset.equals(mEtPointOffset.getText().toString().trim())) {
            return true;
        }

        return false;
    }

    public void onEditableChanged(boolean isEditable) {
        measureMethodLayout.setEnabled(isEditable);
        dataSettlementMethodLayout.setEnabled(isEditable);
        dataResponseLayout.setEnabled(isEditable);
        measurementIntervalPerRoundLayout.setEnabled(isEditable);
        mEtWaitingIntervalPerRound.setEnabled(isEditable);
        mEtDataReadingInterval.setEnabled(isEditable);
        mEtMeasurementCompensationTime.setEnabled(isEditable);
        mEtMotorDriveAddress.setEnabled(isEditable);
        mEtDecentralizationSpeed.setEnabled(isEditable);
        mEtInclinometerTubeHoleDepth.setEnabled(isEditable);
        mEtDecentralizationWaitingTime.setEnabled(isEditable);
        mEtPullUpSpeed.setEnabled(isEditable);
        mEtMeasuringDistance.setEnabled(isEditable);
        mEtMeasurementIntervalTime.setEnabled(isEditable);
        mEtMeasuringReferenceDepth.setEnabled(isEditable);
        mEtIntervalCompensation.setEnabled(isEditable);
        mEtIntervalFitting.setEnabled(isEditable);
        mEtPointOffset.setEnabled(isEditable);

        if (isEditable) {
            mTvMeasureMethod.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_arrow_right, 0);
            mTvDataSettlementMethod.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_arrow_right, 0);
            mTvDataResponse.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_arrow_right, 0);
            mTvMeasurementIntervalPerRound.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_arrow_right, 0);

            mEtWaitingIntervalPerRound.setHint("请输入");
            mEtDataReadingInterval.setHint("请输入");
            mEtMeasurementCompensationTime.setHint("请输入");
            mEtMotorDriveAddress.setHint("请输入");
            mEtDecentralizationSpeed.setHint("1-180");
            mEtInclinometerTubeHoleDepth.setHint("请输入");
            mEtDecentralizationWaitingTime.setHint("1-32");
            mEtPullUpSpeed.setHint("1-180");
            mEtMeasuringDistance.setHint("请输入");
            mEtMeasurementIntervalTime.setHint("请输入");
            mEtMeasuringReferenceDepth.setHint("请输入");
            mEtIntervalCompensation.setHint("请输入");
            mEtIntervalFitting.setHint("请输入");
            mEtPointOffset.setHint("请输入");
        } else {
            mTvMeasureMethod.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mTvDataSettlementMethod.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mTvDataResponse.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mTvMeasurementIntervalPerRound.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

            mEtWaitingIntervalPerRound.setHint("");
            mEtDataReadingInterval.setHint("");
            mEtMeasurementCompensationTime.setHint("");
            mEtMotorDriveAddress.setHint("");
            mEtDecentralizationSpeed.setHint("");
            mEtInclinometerTubeHoleDepth.setHint("");
            mEtDecentralizationWaitingTime.setHint("");
            mEtPullUpSpeed.setHint("");
            mEtMeasuringDistance.setHint("");
            mEtMeasurementIntervalTime.setHint("");
            mEtMeasuringReferenceDepth.setHint("");
            mEtIntervalCompensation.setHint("");
            mEtIntervalFitting.setHint("");
            mEtPointOffset.setHint("");
//            initParamConfigInfo();
        }
        mBtnSave.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }
}
