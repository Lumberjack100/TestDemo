package com.shmedo.mcloudapp.ui.activity.device.sensor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.das.common.SensorGudanDisplacementInfo;
import com.shmedo.das.common.SensorGudanNotStressInfo;
import com.shmedo.das.common.SensorGudanPercolateInfo;
import com.shmedo.das.common.SensorGudanSoilPressureInfo;
import com.shmedo.das.common.SensorGudanStressInfo;
import com.shmedo.das.common.SensorInclinometerInfo;
import com.shmedo.das.common.SensorKangPercolateInfo;
import com.shmedo.das.common.SensorMoistureMeterInfo;
import com.shmedo.das.common.SensorRadarLevelInfo;
import com.shmedo.das.common.SensorSoilMoistureInfo;
import com.shmedo.das.common.SensorTemperHumidityInfo;
import com.shmedo.das.common.SensorUltrasonicLevelInfo;
import com.shmedo.das.common.SensorUpliftPressureInfo;
import com.shmedo.das.common.SensorWireShiftInfo;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.bluetooth.Message;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;
import com.shmedo.mcloudapp.inter.MyOnClickListener;
import com.shmedo.mcloudapp.ui.activity.device.sensor.dialog.DialogFactory;
import com.shmedo.mcloudapp.ui.fragment.DeviceFragment;
import com.shmedo.mcloudapp.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import ch.ielse.view.SwitchView;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device.sensor
 * 文件名:   SenSorMHConfigActivity
 * 创建者:   dpc
 * 创建时间:  2019/4/24 14:44
 * 描述：    基康渗压计 参数配置 50
 */
public class SenSorBGKConfigActivity extends BaseActivity {

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.iv_stay1)
    ImageView imageView1;

    @BindView(R.id.sw_stay1)
    SwitchView switchView1;

    @BindView(R.id.tv_stay1)
    TextView textView1;

    @BindView(R.id.iv_stay2)
    ImageView imageView2;

    @BindView(R.id.sw_stay2)
    SwitchView switchView2;

    @BindView(R.id.tv_stay2)
    TextView textView2;

    @BindView(R.id.iv_stay3)
    ImageView imageView3;

    @BindView(R.id.sw_stay3)
    SwitchView switchView3;

    @BindView(R.id.tv_stay3)
    TextView textView3;

    @BindView(R.id.iv_stay4)
    ImageView imageView4;

    @BindView(R.id.sw_stay4)
    SwitchView switchView4;

    @BindView(R.id.tv_stay4)
    TextView textView4;

    @BindView(R.id.iv_stay5)
    ImageView imageView5;

    @BindView(R.id.sw_stay5)
    SwitchView switchView5;

    @BindView(R.id.tv_stay5)
    TextView textView5;

    @BindView(R.id.iv_stay6)
    ImageView imageView6;

    @BindView(R.id.sw_stay6)
    SwitchView switchView6;

    @BindView(R.id.tv_stay6)
    TextView textView6;

    @BindView(R.id.iv_stay7)
    ImageView imageView7;

    @BindView(R.id.sw_stay7)
    SwitchView switchView7;

    @BindView(R.id.tv_stay7)
    TextView textView7;

    @BindView(R.id.iv_stay8)
    ImageView imageView8;

    @BindView(R.id.sw_stay8)
    SwitchView switchView8;

    @BindView(R.id.tv_stay8)
    TextView textView8;

    @BindView(R.id.tv_prompt)
    TextView mTvPrompt;

    @BindView(R.id.btn_confirm)
    Button mBtnConfirm;

    private MaterialDialog.Builder mBuilder;
    private MaterialDialog mMaterialDialog;

    private CollectorSensorParamsInfoSub<SensorWireShiftInfo> sensorWireShiftInfo;
    private CollectorSensorParamsInfoSub<SensorSoilMoistureInfo> sensorSoilMoistureInfo;
    private CollectorSensorParamsInfoSub<SensorInclinometerInfo> sensorInclinometerInfo;
    private CollectorSensorParamsInfoSub<SensorUltrasonicLevelInfo> sensorUltrasonicLevelInfo;
    private CollectorSensorParamsInfoSub<SensorRadarLevelInfo> sensorRadarLevelInfo;
    private CollectorSensorParamsInfoSub<SensorMoistureMeterInfo> sensorMoistureMeterInfo;
    private CollectorSensorParamsInfoSub<SensorTemperHumidityInfo> sensorTemperHumidityInfo;
    private CollectorSensorParamsInfoSub<SensorUpliftPressureInfo> sensorUpliftPressureInfo;
    private CollectorSensorParamsInfoSub<SensorKangPercolateInfo> sensorKangPercolateInfo;
    private CollectorSensorParamsInfoSub<SensorGudanPercolateInfo> sensorGudanPercolateInfo;
    private CollectorSensorParamsInfoSub<SensorGudanSoilPressureInfo> sensorGudanSoilPressureInfo;
    private CollectorSensorParamsInfoSub<SensorGudanStressInfo> sensorGudanStressInfo;
    private CollectorSensorParamsInfoSub<SensorGudanNotStressInfo> sensorGudanNotStressInfo;
    private CollectorSensorParamsInfoSub<SensorGudanDisplacementInfo> sensorGudanDisplacementInfo;

    private List<CollectorSensorParamsInfoSub> collectorSensorParamsInfoSubs = new ArrayList<>();
    //以传感器的通道号为 Key,CollectorSensorParamsInfoSub 对象为 Value
    private HashMap<String, CollectorSensorParamsInfoSub> collectorSensorHashMap = new HashMap<>();
    private int openCount = 0;//采集器接入的传感器打开的数量
    private DialogFactory factory = new DialogFactory();


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, SenSorBGKConfigActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_sensor_bgk_config;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        initView();
        initData();
    }

    private void initView() {
        mToolbarTitle.setText("拉线位移计配置");
    }


    private void initData() {
        collectorSensorParamsInfoSubs = DeviceFragment.mCollectorParamsInfoSubList;
        openCount = collectorSensorParamsInfoSubs.size();
        if (openCount == 0) {
            return;
        }

        //根据list的大小设置需要打开几个传感器
        for (CollectorSensorParamsInfoSub mCollectorParamsInfoSub : collectorSensorParamsInfoSubs) {
            collectorSensorHashMap.put(mCollectorParamsInfoSub.getChannelNumber(), mCollectorParamsInfoSub);
            openSwitch(mCollectorParamsInfoSub);
        }


//        //根据list的大小设置需要打开几个传感器
//        for (int i = 0; i < collectorSensorParamsInfoSubs.size(); i++) {
//            CollectorSensorParamsInfoSub mCollectorParamsInfoSub = collectorSensorParamsInfoSubs.get(i);
//            openSwitch(mCollectorParamsInfoSub);
//        }
    }

    private int channelNumber0 = -1;
    private int channelNumber1 = -1;
    private int channelNumber2 = -1;
    private int channelNumber3 = -1;
    private int channelNumber4 = -1;
    private int channelNumber5 = -1;
    private int channelNumber6 = -1;
    private int channelNumber7 = -1;

    //根据返回数据的设备数量打开switch开关
    private void openSwitch(CollectorSensorParamsInfoSub mCollectorParamsInfoSub) {
        int number = StringUtil.formatNumber(mCollectorParamsInfoSub.getChannelNumber());
        String sensorType = mCollectorParamsInfoSub.getSensorType();

        switch (number) {
            case 0:
                channelNumber0 = 0;
                setSwitchViewState(true, textView1, switchView1);
                setSensorIconByType(sensorType, 0);
                break;

            case 1:
                channelNumber1 = 1;
                setSwitchViewState(true, textView2, switchView2);
                setSensorIconByType(sensorType, 1);
                break;

            case 2:
                channelNumber2 = 2;
                setSwitchViewState(true, textView3, switchView3);
                setSensorIconByType(sensorType, 2);
                break;

            case 3:
                channelNumber3 = 3;
                setSwitchViewState(true, textView4, switchView4);
                setSensorIconByType(sensorType, 3);
                break;

            case 4:
                channelNumber4 = 4;
                setSwitchViewState(true, textView5, switchView5);
                setSensorIconByType(sensorType, 4);
                break;

            case 5:
                channelNumber5 = 5;
                setSwitchViewState(true, textView6, switchView6);
                setSensorIconByType(sensorType, 5);
                break;

            case 6:
                channelNumber6 = 6;
                setSwitchViewState(true, textView7, switchView7);
                setSensorIconByType(sensorType, 6);
                break;

            case 7:
                channelNumber7 = 7;
                setSwitchViewState(true, textView8, switchView8);
                setSensorIconByType(sensorType, 7);
                break;
        }
    }


    /**
     * 根据type的值设置不同类型传感器的图标
     */
    private void setSensorIconByType(String sensorType, int number) {
        switch (sensorType) {
            case "02"://拉线位移计 MPS-M-2000
                setImageIcon(R.drawable.icon_one, number);
                break;

            case "03"://土壤含水率 TR-3000
                setImageIcon(R.drawable.icon_two, number);
                break;

            case "04"://测斜仪 I-P-I
                setImageIcon(R.drawable.icon_three, number);
                break;

            case "06"://超声波物位计 HBRD908
                setImageIcon(R.drawable.icon_four, number);
                break;

            case "07"://雷达物位计 MH-A15R
                setImageIcon(R.drawable.icon_five, number);
                break;

            case "08"://墒情计 EP100G
                setImageIcon(R.drawable.icon_six, number);
                break;

            case "12"://温湿度计 CSW18
                setImageIcon(R.drawable.icon_seven, number);
                break;

            case "15"://扬压力计 VWP-G
                setImageIcon(R.drawable.icon_eight, number);
                break;

            case "16"://陆岩倾角仪 LY215
                setImageIcon(R.drawable.icon_eight, number);
                break;

            case "50"://基康渗压计 BGK-4500
                setImageIcon(R.drawable.icon_eight, number);
                break;

            case "51"://葛南渗压计 VWP-03
                setImageIcon(R.drawable.icon_eight, number);
                break;

            case "52"://葛南土压力盒 VWE-0.6
                setImageIcon(R.drawable.icon_eight, number);
                break;

            case "53"://葛南应力计 VWS-15
                setImageIcon(R.drawable.icon_eight, number);
                break;

            case "54"://葛南无应力计 VWS-15M
                setImageIcon(R.drawable.icon_eight, number);
                break;

            case "55"://葛南位移计 VWD-100
                setImageIcon(R.drawable.icon_eight, number);
                break;

            default:
                break;
        }
    }


    /**
     * 设置传感器图标
     *
     * @param resourceId
     * @param number
     */
    private void setImageIcon(int resourceId, int number) {
        switch (number) {
            case 0:
                imageView1.setImageDrawable(ContextCompat.getDrawable(this, resourceId));
                break;

            case 1:
                imageView2.setImageDrawable(ContextCompat.getDrawable(this, resourceId));
                break;

            case 2:
                imageView3.setImageDrawable(ContextCompat.getDrawable(this, resourceId));
                break;

            case 3:
                imageView4.setImageDrawable(ContextCompat.getDrawable(this, resourceId));
                break;

            case 4:
                imageView5.setImageDrawable(ContextCompat.getDrawable(this, resourceId));
                break;

            case 5:
                imageView6.setImageDrawable(ContextCompat.getDrawable(this, resourceId));
                break;

            case 6:
                imageView7.setImageDrawable(ContextCompat.getDrawable(this, resourceId));
                break;

            case 7:
                imageView8.setImageDrawable(ContextCompat.getDrawable(this, resourceId));
                break;
        }
    }


    /**
     * 点击imageview弹框
     * 点击switch 进行关闭、打开状态
     *
     * @param view
     */
    @OnClick({R.id.sw_stay1, R.id.sw_stay2, R.id.sw_stay3, R.id.sw_stay4, R.id.sw_stay5,
            R.id.sw_stay6, R.id.sw_stay7, R.id.sw_stay8, R.id.iv_stay1, R.id.iv_stay2, R.id.iv_stay3,
            R.id.iv_stay4, R.id.iv_stay5, R.id.iv_stay6, R.id.iv_stay7, R.id.iv_stay8, R.id.btn_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.sw_stay1:
                toggleSwitchView(textView1, switchView1, "0");
                break;

            case R.id.sw_stay2:
                toggleSwitchView(textView2, switchView2, "1");
                break;

            case R.id.sw_stay3:
                toggleSwitchView(textView3, switchView3, "2");
                break;

            case R.id.sw_stay4:
                toggleSwitchView(textView4, switchView4, "3");
                break;

            case R.id.sw_stay5:
                toggleSwitchView(textView5, switchView5, "4");
                break;

            case R.id.sw_stay6:
                toggleSwitchView(textView6, switchView6, "5");
                break;

            case R.id.sw_stay7:
                toggleSwitchView(textView7, switchView7, "6");
                break;

            case R.id.sw_stay8:
                toggleSwitchView(textView8, switchView8, "7");
                break;

            case R.id.iv_stay1://弹框
                if (channelNumber0 == -1) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialog(channelNumber0);
                break;

            case R.id.iv_stay2:
                if (channelNumber1 == -1) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialog(channelNumber1);
                break;

            case R.id.iv_stay3:
                if (channelNumber2 == -1) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialog(channelNumber2);
                break;

            case R.id.iv_stay4:
                if (channelNumber3 == -1) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialog(channelNumber3);
                break;

            case R.id.iv_stay5:
                if (channelNumber4 == -1) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialog(channelNumber4);
                break;

            case R.id.iv_stay6:
                if (channelNumber5 == -1) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialog(channelNumber5);
                break;

            case R.id.iv_stay7:
                if (channelNumber6 == -1) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialog(channelNumber6);
                break;

            case R.id.iv_stay8:
                if (channelNumber7 == -1) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialog(channelNumber7);
                break;

            case R.id.btn_confirm://确定发送指令
                sendInstruction();
                break;
        }
    }


    private void setSwitchViewState(boolean isOpen, TextView mTvStay, SwitchView mSwStay) {
        mSwStay.setOpened(isOpen);
        mTvStay.setText(isOpen ? "已启用" : "已停用");
        mTvStay.setBackgroundColor(getResources().getColor(isOpen ? R.color.colorPrimaryDark : R.color.secondary_text));
    }

    private void toggleSwitchView(TextView textView, SwitchView switchView, String tag) {
        if (switchView.isOpened()) {
            setSwitchViewState(true, textView, switchView);
            setOpenSwitchDialog(tag);

        } else {
            closeSwitchColorbg(textView, switchView, tag);
        }
    }


    /**
     * 关闭采集器设备  确定后停用6226560118056462
     */
    private void closeSwitchColorbg(final TextView textView, final SwitchView switchView, final String tag) {
        mBuilder = new MaterialDialog.Builder(this);
        mBuilder.title("温馨提示：")
                .content("确认要停用该传感器吗？")
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消");
        mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
        mBuilder.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                setSwitchViewState(true, textView, switchView);
            }
        });
        mBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                setSwitchViewState(false, textView, switchView);

                //停用后停止
                switch (tag) {
                    case "0":
                        channelNumber0 = -1;
                        collectorSensorHashMap.remove("00");
                        break;

                    case "1":
                        channelNumber1 = -1;
                        collectorSensorHashMap.remove("01");
                        break;

                    case "2":
                        channelNumber2 = -1;
                        collectorSensorHashMap.remove("02");
                        break;

                    case "3":
                        channelNumber3 = -1;
                        collectorSensorHashMap.remove("03");
                        break;

                    case "4":
                        channelNumber4 = -1;
                        collectorSensorHashMap.remove("04");
                        break;

                    case "5":
                        channelNumber5 = -1;
                        collectorSensorHashMap.remove("05");
                        break;

                    case "6":
                        channelNumber6 = -1;
                        collectorSensorHashMap.remove("06");
                        break;

                    case "7":
                        channelNumber7 = -1;
                        collectorSensorHashMap.remove("07");
                        break;
                }
            }
        });
    }


    private void setOpenSwitchDialog(String tag) {
        if (collectorSensorHashMap.isEmpty()) {
            return;
        }

        switch (tag) {
            case "0":
                channelNumber0 = 0;
                if (!collectorSensorHashMap.containsKey("00")) {
                    CollectorSensorParamsInfoSub collectorSensorParamsInfoSub = new CollectorSensorParamsInfoSub();
                    collectorSensorParamsInfoSub.setCollectorModel(collectorSensorParamsInfoSubs.get(0).getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber("00");
                    collectorSensorParamsInfoSub.setSensorType(collectorSensorParamsInfoSubs.get(0).getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress(collectorSensorParamsInfoSubs.get(0).getSensorAddress());
                    collectorSensorParamsInfoSub.setSensorData(collectorSensorParamsInfoSubs.get(0).getSensorData());
                    collectorSensorHashMap.put("00", collectorSensorParamsInfoSub);
                }
                showBottomDialog(channelNumber0);
                break;

            case "1":
                channelNumber1 = 1;
                if (!collectorSensorHashMap.containsKey("01")) {
                    CollectorSensorParamsInfoSub collectorSensorParamsInfoSub = new CollectorSensorParamsInfoSub();
                    collectorSensorParamsInfoSub.setCollectorModel(collectorSensorParamsInfoSubs.get(0).getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber("01");
                    collectorSensorParamsInfoSub.setSensorType(collectorSensorParamsInfoSubs.get(0).getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress("1");
                    collectorSensorParamsInfoSub.setSensorData(collectorSensorParamsInfoSubs.get(0).getSensorData());
                    collectorSensorHashMap.put("01", collectorSensorParamsInfoSub);
                }
                showBottomDialog(channelNumber1);
                break;

            case "2":
                channelNumber2 = 2;
                if (!collectorSensorHashMap.containsKey("02")) {
                    CollectorSensorParamsInfoSub collectorSensorParamsInfoSub = new CollectorSensorParamsInfoSub();
                    collectorSensorParamsInfoSub.setCollectorModel(collectorSensorParamsInfoSubs.get(0).getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber("02");
                    collectorSensorParamsInfoSub.setSensorType(collectorSensorParamsInfoSubs.get(0).getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress("2");
                    collectorSensorParamsInfoSub.setSensorData(collectorSensorParamsInfoSubs.get(0).getSensorData());
                    collectorSensorHashMap.put("02", collectorSensorParamsInfoSub);
                }
                showBottomDialog(channelNumber2);
                break;

            case "3":
                channelNumber3 = 3;
                if (!collectorSensorHashMap.containsKey("03")) {
                    CollectorSensorParamsInfoSub collectorSensorParamsInfoSub = new CollectorSensorParamsInfoSub();
                    collectorSensorParamsInfoSub.setCollectorModel(collectorSensorParamsInfoSubs.get(0).getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber("03");
                    collectorSensorParamsInfoSub.setSensorType(collectorSensorParamsInfoSubs.get(0).getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress("3");
                    collectorSensorParamsInfoSub.setSensorData(collectorSensorParamsInfoSubs.get(0).getSensorData());
                    collectorSensorHashMap.put("03", collectorSensorParamsInfoSub);
                }
                showBottomDialog(channelNumber3);
                break;

            case "4":
                channelNumber4 = 4;
                if (!collectorSensorHashMap.containsKey("04")) {
                    CollectorSensorParamsInfoSub collectorSensorParamsInfoSub = new CollectorSensorParamsInfoSub();
                    collectorSensorParamsInfoSub.setCollectorModel(collectorSensorParamsInfoSubs.get(0).getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber("04");
                    collectorSensorParamsInfoSub.setSensorType(collectorSensorParamsInfoSubs.get(0).getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress("4");
                    collectorSensorParamsInfoSub.setSensorData(collectorSensorParamsInfoSubs.get(0).getSensorData());
                    collectorSensorHashMap.put("04", collectorSensorParamsInfoSub);
                }
                showBottomDialog(channelNumber4);
                break;

            case "5":
                channelNumber5 = 5;
                if (!collectorSensorHashMap.containsKey("05")) {
                    CollectorSensorParamsInfoSub collectorSensorParamsInfoSub = new CollectorSensorParamsInfoSub();
                    collectorSensorParamsInfoSub.setCollectorModel(collectorSensorParamsInfoSubs.get(0).getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber("05");
                    collectorSensorParamsInfoSub.setSensorType(collectorSensorParamsInfoSubs.get(0).getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress("5");
                    collectorSensorParamsInfoSub.setSensorData(collectorSensorParamsInfoSubs.get(0).getSensorData());
                    collectorSensorHashMap.put("05", collectorSensorParamsInfoSub);
                }
                showBottomDialog(channelNumber5);
                break;

            case "6":
                channelNumber6 = 6;
                if (!collectorSensorHashMap.containsKey("06")) {
                    CollectorSensorParamsInfoSub collectorSensorParamsInfoSub = new CollectorSensorParamsInfoSub();
                    collectorSensorParamsInfoSub.setCollectorModel(collectorSensorParamsInfoSubs.get(0).getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber("06");
                    collectorSensorParamsInfoSub.setSensorType(collectorSensorParamsInfoSubs.get(0).getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress("6");
                    collectorSensorParamsInfoSub.setSensorData(collectorSensorParamsInfoSubs.get(0).getSensorData());
                    collectorSensorHashMap.put("06", collectorSensorParamsInfoSub);
                }
                showBottomDialog(channelNumber6);
                break;

            case "7":
                channelNumber7 = 7;
                if (!collectorSensorHashMap.containsKey("07")) {
                    CollectorSensorParamsInfoSub collectorSensorParamsInfoSub = new CollectorSensorParamsInfoSub();
                    collectorSensorParamsInfoSub.setCollectorModel(collectorSensorParamsInfoSubs.get(0).getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber("07");
                    collectorSensorParamsInfoSub.setSensorType(collectorSensorParamsInfoSubs.get(0).getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress("7");
                    collectorSensorParamsInfoSub.setSensorData(collectorSensorParamsInfoSubs.get(0).getSensorData());
                    collectorSensorHashMap.put("07", collectorSensorParamsInfoSub);
                }
                showBottomDialog(channelNumber7);
                break;

            default:
        }
    }

    /**
     * 弹框
     *
     * @param
     */
    private void showBottomDialog(int channelNumber) {
        switch (channelNumber) {
            case 0:
                factory.createDialog(this, collectorSensorHashMap.get("00").getSensorType(), collectorSensorHashMap.get("00"), channelNumber, onSureClickListener);
                break;

            case 1:
                factory.createDialog(this, collectorSensorHashMap.get("01").getSensorType(), collectorSensorHashMap.get("01"), channelNumber, onSureClickListener);
                break;

            case 2:
                factory.createDialog(this, collectorSensorHashMap.get("02").getSensorType(), collectorSensorHashMap.get("02"), channelNumber, onSureClickListener);
                break;

            case 3:
                factory.createDialog(this, collectorSensorHashMap.get("03").getSensorType(), collectorSensorHashMap.get("03"), channelNumber, onSureClickListener);
                break;

            case 4:
                factory.createDialog(this, collectorSensorHashMap.get("04").getSensorType(), collectorSensorHashMap.get("04"), channelNumber, onSureClickListener);
                break;

            case 5:
                factory.createDialog(this, collectorSensorHashMap.get("05").getSensorType(), collectorSensorHashMap.get("05"), channelNumber, onSureClickListener);
                break;

            case 6:
                factory.createDialog(this, collectorSensorHashMap.get("06").getSensorType(), collectorSensorHashMap.get("06"), channelNumber, onSureClickListener);
                break;

            case 7:
                factory.createDialog(this, collectorSensorHashMap.get("07").getSensorType(), collectorSensorHashMap.get("07"), channelNumber, onSureClickListener);
                break;

            default:
        }
    }

    private MyOnClickListener onSureClickListener = new MyOnClickListener() {
        @Override
        public boolean onSureClick(View v) {
            List<CollectorSensorParamsInfoSub> paramsInfoSubList = new ArrayList<>();
            paramsInfoSubList.addAll(collectorSensorHashMap.values());
            if (paramsInfoSubList == null || paramsInfoSubList.isEmpty()) {
                return false;
            }

            for (int k = 0; k < paramsInfoSubList.size() - 1; k++) {
                for (int j = k + 1; j < paramsInfoSubList.size(); j++) {
                    if (paramsInfoSubList.get(k).getSensorAddress().equals(paramsInfoSubList.get(j).getSensorAddress())) {
                        ToastUtils.show("Modbus地址不能重复");
                        return false;
                    }
                }
            }

            return true;
        }

        @Override
        public void onCancelClick(View view, int channelNumber) {
            switch (channelNumber) {
                case 0:
                    setSwitchViewState(false, textView1, switchView1);
                    channelNumber0 = -1;
                    collectorSensorHashMap.remove("00");
                    break;

                case 1:
                    setSwitchViewState(false, textView2, switchView2);
                    channelNumber1 = -1;
                    collectorSensorHashMap.remove("01");
                    break;

                case 2:
                    setSwitchViewState(false, textView3, switchView3);
                    channelNumber2 = -1;
                    collectorSensorHashMap.remove("02");
                    break;

                case 3:
                    setSwitchViewState(false, textView4, switchView4);
                    channelNumber3 = -1;
                    collectorSensorHashMap.remove("03");
                    break;

                case 4:
                    setSwitchViewState(false, textView5, switchView5);
                    channelNumber4 = -1;
                    collectorSensorHashMap.remove("04");
                    break;

                case 5:
                    setSwitchViewState(false, textView6, switchView6);
                    channelNumber5 = -1;
                    collectorSensorHashMap.remove("05");
                    break;

                case 6:
                    setSwitchViewState(false, textView7, switchView7);
                    channelNumber6 = -1;
                    collectorSensorHashMap.remove("06");
                    break;

                case 7:
                    setSwitchViewState(false, textView8, switchView8);
                    channelNumber7 = -1;
                    collectorSensorHashMap.remove("07");
                    break;

                default:
            }
        }
    };


    List<String> cmdList = new ArrayList<>();

    /**
     * ##150zzxxXXXX\r\n：设置采集器接入的传感器
     * zz 采集器型号
     * xx的取值范围为：01~08，表示接入传感器的个数
     * 1）当传感器个数为01时XXXX（4个字节）的含义：前两位表示地址或者通道号，后两位表示接入传感器类型
     * 2）当传感器个数为02时XXXXXXXX（8个字节）的含义：前四位表示第一个地址和对应的传感器类型，后四位表示第二个地址和对应的传感器类型
     * ……以此类推。
     * 该指令不定长，根据接入传感器的个数而定，地址为01~99,通道为00~07
     */
    private void sendInstruction() {
        collectorSensorParamsInfoSubs.clear();
        collectorSensorParamsInfoSubs.addAll(collectorSensorHashMap.values());
        if (collectorSensorParamsInfoSubs.isEmpty()) {
            return;
        }


        StringBuilder builderFirst = new StringBuilder();
        builderFirst.append("##150");
        builderFirst.append(collectorSensorParamsInfoSubs.get(0).getCollectorModel() + StringUtil.formatStringTwo(String.valueOf(collectorSensorParamsInfoSubs.size())));

        for (CollectorSensorParamsInfoSub paramsInfoSub : collectorSensorParamsInfoSubs) {
            builderFirst.append(StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) + StringUtil.formatStringTwo(paramsInfoSub.getSensorType()));
        }
        builderFirst.append("\r\n");

        for (int j = 0; j < collectorSensorParamsInfoSubs.size(); j++) {
            cmdList.addAll(serData(collectorSensorParamsInfoSubs.get(j), j));
        }

        for (int i = 0; i < cmdList.size(); i++) {
            builderFirst.append(cmdList.get(i).toString());
        }

        String result = String.valueOf(builderFirst);
        if (!MCloudApp.isIsBluetoothDeviceConnected()) {
            ToastUtils.show("蓝牙未连接");
            finish();
            return;
        }

        Message msg = new Message(UUID.randomUUID().toString(), result, true);
        DeviceFragment.mdBluetoothManager.writeMessage(msg);
        Timber.d("发送result指令===" + result);
        finish();
    }

    private List<String> serData(CollectorSensorParamsInfoSub paramsInfoSub, int i) {
        List<String> list = new ArrayList<>();
        StringBuilder result1 = new StringBuilder();
        StringBuilder result2 = new StringBuilder();
        switch (paramsInfoSub.getCollectorModel()) {
            case "02":
                //裂缝计采集器
                SensorWireShiftInfo info = (SensorWireShiftInfo) paramsInfoSub.getSensorData();
                result1.append("##168" + StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        info.getTriggerThreshold() + "\r\n");
                result2.append("##165" + StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        info.getCorrectionValue() + "\r\n");
                list.add(String.valueOf(result1));
                list.add(String.valueOf(result2));
                break;

            case "03":
                //土壤湿度采集器
                SensorSoilMoistureInfo moistureInfo = (SensorSoilMoistureInfo) paramsInfoSub.getSensorData();
                result1.append("##168" + StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(String.valueOf(i + 1)) +
                        moistureInfo.getTriggerThreshold() + "\r\n");
                result2.append("##165" + StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(String.valueOf(i + 1)) +
                        moistureInfo.getRevised() + "\r\n");
                list.add(String.valueOf(result1));
                list.add(String.valueOf(result2));
                break;

            case "04":
                //测斜仪采集器
                SensorInclinometerInfo inclinometerInfo = (SensorInclinometerInfo) paramsInfoSub.getSensorData();
                result1.append("##168" + StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(String.valueOf(i + 1)) +
                        inclinometerInfo.getTriggerThreshold() + "\r\n");
                result2.append("##165" + StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(String.valueOf(i + 1)) +
                        inclinometerInfo.getCorrectionValue() + "\r\n");
                list.add(String.valueOf(result1));
                list.add(String.valueOf(result2));
                break;

            case "07":
                //雷达采集器
                SensorRadarLevelInfo levelInfo = (SensorRadarLevelInfo) paramsInfoSub.getSensorData();
                result1.append("##168" + StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(String.valueOf(i + 1)) +
                        levelInfo.getTriggerThreshold() + "\r\n");
                result2.append("##165" + StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(String.valueOf(i + 1)) +
                        levelInfo.getRevised() + "\r\n");
                list.add(String.valueOf(result1));
                list.add(String.valueOf(result2));
                break;

            default:
                break;
        }

        return list;
    }

}
