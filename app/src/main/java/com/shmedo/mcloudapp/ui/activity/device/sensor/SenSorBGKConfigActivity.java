package com.shmedo.mcloudapp.ui.activity.device.sensor;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.bluetooth.Message;
import com.shmedo.mcloudapp.entity.ble.CollectorInfoSub;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;
import com.shmedo.mcloudapp.ui.activity.device.sensor.dialog.DialogFactory;
import com.shmedo.mcloudapp.ui.fragment.DeviceFragment;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.util.bleutil.LogTag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import ch.ielse.view.SwitchView;

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
    ImageView mIvStay1;
    @BindView(R.id.sw_stay1)
    SwitchView mSwStay1;
    @BindView(R.id.tv_stay1)
    TextView mTvStay1;
    @BindView(R.id.iv_stay2)
    ImageView mIvStay2;
    @BindView(R.id.sw_stay2)
    SwitchView mSwStay2;
    @BindView(R.id.tv_stay2)
    TextView mTvStay2;
    @BindView(R.id.iv_stay3)
    ImageView mIvStay3;
    @BindView(R.id.sw_stay3)
    SwitchView mSwStay3;
    @BindView(R.id.tv_stay3)
    TextView mTvStay3;
    @BindView(R.id.iv_stay4)
    ImageView mIvStay4;
    @BindView(R.id.sw_stay4)
    SwitchView mSwStay4;
    @BindView(R.id.tv_stay4)
    TextView mTvStay4;
    @BindView(R.id.iv_stay5)
    ImageView mIvStay5;
    @BindView(R.id.sw_stay5)
    SwitchView mSwStay5;
    @BindView(R.id.tv_stay5)
    TextView mTvStay5;
    @BindView(R.id.iv_stay6)
    ImageView mIvStay6;
    @BindView(R.id.sw_stay6)
    SwitchView mSwStay6;
    @BindView(R.id.tv_stay6)
    TextView mTvStay6;
    @BindView(R.id.iv_stay7)
    ImageView mIvStay7;
    @BindView(R.id.sw_stay7)
    SwitchView mSwStay7;
    @BindView(R.id.tv_stay7)
    TextView mTvStay7;
    @BindView(R.id.iv_stay8)
    ImageView mIvStay8;
    @BindView(R.id.sw_stay8)
    SwitchView mSwStay8;
    @BindView(R.id.tv_stay8)
    TextView mTvStay8;
    @BindView(R.id.tv_prompt)
    TextView mTvPrompt;
    @BindView(R.id.btn_confirm)
    Button mBtnConfirm;

    private MaterialDialog.Builder mBuilder;
    private MaterialDialog mMaterialDialog;
    private int count = 0;

    private CollectorInfoSub mCollectorInfoSub;
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
    private List<CollectorSensorParamsInfoSub> mCollectorParamsInfoSubList = new ArrayList<>();

    private int openCount = 0;//传感器打开的数量
    private boolean[] switchs = new boolean[8];
    private DialogFactory factory = new DialogFactory();

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

        mCollectorParamsInfoSubList = DeviceFragment.mCollectorParamsInfoSubList;
        openCount = mCollectorParamsInfoSubList.size();
        if (openCount == 0) {
            return;
        } else {
            //根据list的大小设置需要打开几个传感器
            for (int i = 0; i < mCollectorParamsInfoSubList.size(); i++) {
                CollectorSensorParamsInfoSub mCollectorParamsInfoSub = mCollectorParamsInfoSubList.get(i);
                switchs[i] = true;
                if (switchs[i]) {
                    openSwitch(i, mCollectorParamsInfoSub);
                }
            }
        }
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
    private void openSwitch(int number, CollectorSensorParamsInfoSub mCollectorParamsInfoSub) {
        switch (number) {
            case 0:
                mSwStay1.setOpened(true);
                mTvStay1.setText("已启用");
                mTvStay1.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                channelNumber0 = StringUtil.formatNumber(mCollectorParamsInfoSub.getChannelNumber());
                getTypeSetSensor(mCollectorParamsInfoSub, 0);
                break;
            case 1:
                mSwStay2.setOpened(true);
                mTvStay2.setText("已启用");
                mTvStay2.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                channelNumber1 = StringUtil.formatNumber(mCollectorParamsInfoSub.getChannelNumber());
                getTypeSetSensor(mCollectorParamsInfoSub, 1);
                break;
            case 2:
                mSwStay3.setOpened(true);
                mTvStay3.setText("已启用");
                mTvStay3.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                channelNumber2 = StringUtil.formatNumber(mCollectorParamsInfoSub.getChannelNumber());
                getTypeSetSensor(mCollectorParamsInfoSub, 2);
                break;
            case 3:
                mSwStay4.setOpened(true);
                mTvStay4.setText("已启用");
                mTvStay4.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                channelNumber3 = StringUtil.formatNumber(mCollectorParamsInfoSub.getChannelNumber());
                getTypeSetSensor(mCollectorParamsInfoSub, 3);
                break;
            case 4:
                mSwStay5.setOpened(true);
                mTvStay5.setText("已启用");
                mTvStay5.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                channelNumber4 = StringUtil.formatNumber(mCollectorParamsInfoSub.getChannelNumber());
                getTypeSetSensor(mCollectorParamsInfoSub, 4);
                break;
            case 5:
                mSwStay6.setOpened(true);
                mTvStay6.setText("已启用");
                mTvStay6.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                channelNumber5 = StringUtil.formatNumber(mCollectorParamsInfoSub.getChannelNumber());
                getTypeSetSensor(mCollectorParamsInfoSub, 5);
                break;
            case 6:
                mSwStay7.setOpened(true);
                mTvStay7.setText("已启用");
                mTvStay7.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                channelNumber6 = StringUtil.formatNumber(mCollectorParamsInfoSub.getChannelNumber());
                getTypeSetSensor(mCollectorParamsInfoSub, 6);
                break;
            case 7:
                mSwStay8.setOpened(true);
                mTvStay8.setText("已启用");
                mTvStay8.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                channelNumber7 = StringUtil.formatNumber(mCollectorParamsInfoSub.getChannelNumber());
                getTypeSetSensor(mCollectorParamsInfoSub, 7);
                break;

        }

    }

    //根据type的值设置图标
    private void getTypeSetSensor(CollectorSensorParamsInfoSub mCollectorParamsInfoSub, int number) {
        switch (mCollectorParamsInfoSub.getSensorType()) {
            case "02":
                setImageIcon(R.drawable.icon_one, number);
                break;
            case "03":
                setImageIcon(R.drawable.icon_two, number);
                break;
            case "04":
                setImageIcon(R.drawable.icon_three, number);
                break;
            case "06":
                setImageIcon(R.drawable.icon_four, number);
                break;
            case "07":
                setImageIcon(R.drawable.icon_five, number);
                break;
            case "08":
                setImageIcon(R.drawable.icon_six, number);
                break;
            case "12":
                setImageIcon(R.drawable.icon_seven, number);
                break;
            case "15":
                setImageIcon(R.drawable.icon_eight, number);
                break;
            case "50":
                setImageIcon(R.drawable.icon_eight, number);
                break;
            case "51":
                setImageIcon(R.drawable.icon_eight, number);
                break;
            case "52":
                setImageIcon(R.drawable.icon_eight, number);
                break;
            case "53":
                setImageIcon(R.drawable.icon_eight, number);
                break;
            case "54":
                setImageIcon(R.drawable.icon_eight, number);
                break;
            case "55":
                setImageIcon(R.drawable.icon_eight, number);
                break;
            default:
                break;
        }


    }


    /**
     * 设置传感器图标
     *
     * @param id
     * @param number
     */
    private void setImageIcon(int id, int number) {
        switch (number) {
            case 0:
                mIvStay1.setImageDrawable(ContextCompat.getDrawable(this, id));
                break;
            case 1:
                mIvStay2.setImageDrawable(ContextCompat.getDrawable(this, id));
                break;
            case 2:
                mIvStay3.setImageDrawable(ContextCompat.getDrawable(this, id));
                break;
            case 3:
                mIvStay4.setImageDrawable(ContextCompat.getDrawable(this, id));
                break;
            case 4:
                mIvStay5.setImageDrawable(ContextCompat.getDrawable(this, id));
                break;
            case 5:
                mIvStay6.setImageDrawable(ContextCompat.getDrawable(this, id));
                break;
            case 6:
                mIvStay7.setImageDrawable(ContextCompat.getDrawable(this, id));
                break;
            case 7:
                mIvStay8.setImageDrawable(ContextCompat.getDrawable(this, id));
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
                checkSwitchColor(mTvStay1, mSwStay1, "0");
                break;
            case R.id.sw_stay2:
                checkSwitchColor(mTvStay2, mSwStay2, "1");
                break;
            case R.id.sw_stay3:
                checkSwitchColor(mTvStay3, mSwStay3, "2");
                break;
            case R.id.sw_stay4:
                checkSwitchColor(mTvStay4, mSwStay4, "3");
                break;
            case R.id.sw_stay5:
                checkSwitchColor(mTvStay5, mSwStay5, "4");
                break;
            case R.id.sw_stay6:
                checkSwitchColor(mTvStay6, mSwStay6, "5");
                break;
            case R.id.sw_stay7:
                checkSwitchColor(mTvStay7, mSwStay7, "6");
                break;
            case R.id.sw_stay8:
                checkSwitchColor(mTvStay8, mSwStay8, "7");
                break;
            case R.id.iv_stay1://弹框
                if (channelNumber0 != -1) {
                    showDialog("0", channelNumber0);
                } else {
                    ToastUtil.showSToast("此传感器为空");
                    return;
                }

                break;
            case R.id.iv_stay2:
                if (channelNumber1 != -1) {
                    showDialog("1", channelNumber1);
                } else {
                    ToastUtil.showSToast("此传感器为空");
                    return;
                }
                break;
            case R.id.iv_stay3:
                if (channelNumber2 != -1) {
                    showDialog("2", channelNumber2);
                } else {
                    ToastUtil.showSToast("此传感器为空");
                    return;
                }
                break;
            case R.id.iv_stay4:
                if (channelNumber3 != -1) {
                    showDialog("3", channelNumber3);
                } else {
                    ToastUtil.showSToast("此传感器为空");
                    return;
                }
                break;
            case R.id.iv_stay5:
                if (channelNumber4 != -1) {
                    showDialog("4", channelNumber4);
                } else {
                    ToastUtil.showSToast("此传感器为空");
                    return;
                }
                break;
            case R.id.iv_stay6:
                if (channelNumber5 != -1) {
                    showDialog("5", channelNumber5);
                } else {
                    ToastUtil.showSToast("此传感器为空");
                    return;
                }
                break;
            case R.id.iv_stay7:
                if (channelNumber6 != -1) {
                    showDialog("6", channelNumber6);
                } else {
                    ToastUtil.showSToast("此传感器为空");
                    return;
                }
                break;
            case R.id.iv_stay8:
                if (channelNumber7 != -1) {
                    showDialog("7", channelNumber7);
                } else {
                    ToastUtil.showSToast("此传感器为空");
                    return;
                }
                break;
            case R.id.btn_confirm://确定发送指令
                sendInstruction();
                break;
        }
    }


    private void checkSwitchColor(TextView mTvStay, SwitchView mSwStay, String tag) {
        if (mSwStay.isOpened()) {
            //
            openSwitchColorbg(mTvStay, mSwStay, tag);
        } else {
            //
            closeSwitchColorbg(mTvStay, mSwStay, tag);

        }
    }


    /**
     * 关闭采集器设备  确定后停用6226560118056462
     *
     * @param mTvStay
     * @param mSwStay
     */
    private void closeSwitchColorbg(final TextView mTvStay, final SwitchView mSwStay, final String tag) {
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
                mSwStay.setOpened(true);
                mTvStay.setText("已启用");
                mTvStay.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            }
        });
        mBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                mSwStay.setOpened(false);
                mTvStay.setText("已停用");
                mTvStay.setBackgroundColor(getResources().getColor(R.color.secondary_text));
                //停用后停止
                switch (tag) {
                    case "0":
                        channelNumber0 = -1;
                        mCollectorParamsInfoSubList.remove(0);
                        break;
                    case "1":
                        channelNumber1 = -1;
                        mCollectorParamsInfoSubList.remove(1);
                        break;
                    case "2":
                        channelNumber2 = -1;
                        mCollectorParamsInfoSubList.remove(2);
                        break;
                }
            }
        });

    }


    /**
     * 打开选择按钮
     *
     * @param mTvStay
     * @param mSwStay
     */
    private void openSwitchColorbg(final TextView mTvStay, final SwitchView mSwStay, String tag) {
        mSwStay.setOpened(true);
        mTvStay.setText("已启用");
        mTvStay.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        setOpenSwitchDialog(tag);
    }

    private void setOpenSwitchDialog(String tag) {
        if (mCollectorParamsInfoSubList.size() == 0) {
            Log.i("adu", "集合等于空");
            return;
        }
        switch (tag) {
            case "0":
                channelNumber0 = 0;
                //String count0 = StringUtil.formatTwo(getSwitchCount());
                //Log.i("adu","--setOpenSwitchDialog--channel0="+count0);
                CollectorSensorParamsInfoSub mCollectorParamsInfoSub0 = new CollectorSensorParamsInfoSub();
                mCollectorParamsInfoSub0.setCollectorModel(mCollectorParamsInfoSubList.get(0).getCollectorModel());
                mCollectorParamsInfoSub0.setChannelNumber("00");
                mCollectorParamsInfoSub0.setSensorType(mCollectorParamsInfoSubList.get(0).getSensorType());
                mCollectorParamsInfoSub0.setSensorAddress(mCollectorParamsInfoSubList.get(0).getSensorAddress());
                mCollectorParamsInfoSub0.setSensorData(mCollectorParamsInfoSubList.get(0).getSensorData());
                mCollectorParamsInfoSubList.add(mCollectorParamsInfoSub0);
                showDialog("0", channelNumber0);
                break;
            case "1":
                channelNumber1 = 1;
                //String count1 = StringUtil.formatTwo(getSwitchCount());
                //Log.i("adu","--setOpenSwitchDialog--channel1="+count1);
                CollectorSensorParamsInfoSub mCollectorParamsInfoSub1 = new CollectorSensorParamsInfoSub();
                mCollectorParamsInfoSub1.setCollectorModel(mCollectorParamsInfoSubList.get(0).getCollectorModel());
                mCollectorParamsInfoSub1.setChannelNumber("01");
                mCollectorParamsInfoSub1.setSensorType(mCollectorParamsInfoSubList.get(0).getSensorType());
                mCollectorParamsInfoSub1.setSensorAddress(mCollectorParamsInfoSubList.get(0).getSensorAddress());
                mCollectorParamsInfoSub1.setSensorData(mCollectorParamsInfoSubList.get(0).getSensorData());
                mCollectorParamsInfoSubList.add(mCollectorParamsInfoSub1);
                showDialog("1", channelNumber1);
                break;
            case "2":
                channelNumber2 = 2;
                //String count2 = StringUtil.formatTwo(getSwitchCount());
                //Log.i("adu","--setOpenSwitchDialog--channel2="+count2);
                CollectorSensorParamsInfoSub mCollectorParamsInfoSub2 = new CollectorSensorParamsInfoSub();
                mCollectorParamsInfoSub2.setCollectorModel(mCollectorParamsInfoSubList.get(0).getCollectorModel());
                mCollectorParamsInfoSub2.setChannelNumber("02");
                mCollectorParamsInfoSub2.setSensorType(mCollectorParamsInfoSubList.get(0).getSensorType());
                mCollectorParamsInfoSub2.setSensorAddress(mCollectorParamsInfoSubList.get(0).getSensorAddress());
                mCollectorParamsInfoSub2.setSensorData(mCollectorParamsInfoSubList.get(0).getSensorData());
                mCollectorParamsInfoSubList.add(mCollectorParamsInfoSub2);
                showDialog("2", channelNumber2);
                break;
            case "3":
                channelNumber3 = 3;
                //String count3 = StringUtil.formatTwo(getSwitchCount());
                //Log.i("adu","--setOpenSwitchDialog--channel3="+count3);
                CollectorSensorParamsInfoSub mCollectorParamsInfoSub3 = new CollectorSensorParamsInfoSub();
                mCollectorParamsInfoSub3.setCollectorModel(mCollectorParamsInfoSubList.get(0).getCollectorModel());
                mCollectorParamsInfoSub3.setChannelNumber("03");
                mCollectorParamsInfoSub3.setSensorType(mCollectorParamsInfoSubList.get(0).getSensorType());
                mCollectorParamsInfoSub3.setSensorAddress(mCollectorParamsInfoSubList.get(0).getSensorAddress());
                mCollectorParamsInfoSub3.setSensorData(mCollectorParamsInfoSubList.get(0).getSensorData());
                mCollectorParamsInfoSubList.add(mCollectorParamsInfoSub3);
                showDialog("3", channelNumber3);
                break;
            case "4":
                channelNumber4 = 4;
                //String count4 = StringUtil.formatTwo(getSwitchCount());
                //Log.i("adu","--setOpenSwitchDialog--channel4="+count4);
                CollectorSensorParamsInfoSub mCollectorParamsInfoSub4 = new CollectorSensorParamsInfoSub();
                mCollectorParamsInfoSub4.setCollectorModel(mCollectorParamsInfoSubList.get(0).getCollectorModel());
                mCollectorParamsInfoSub4.setChannelNumber("04");
                mCollectorParamsInfoSub4.setSensorType(mCollectorParamsInfoSubList.get(0).getSensorType());
                mCollectorParamsInfoSub4.setSensorAddress(mCollectorParamsInfoSubList.get(0).getSensorAddress());
                mCollectorParamsInfoSub4.setSensorData(mCollectorParamsInfoSubList.get(0).getSensorData());
                mCollectorParamsInfoSubList.add(mCollectorParamsInfoSub4);
                showDialog("4", channelNumber4);
                break;
            case "5":
                channelNumber5 = 5;
                //String count5 = StringUtil.formatTwo(getSwitchCount());
                //Log.i("adu","--setOpenSwitchDialog--channel5="+count5);
                CollectorSensorParamsInfoSub mCollectorParamsInfoSub5 = new CollectorSensorParamsInfoSub();
                mCollectorParamsInfoSub5.setCollectorModel(mCollectorParamsInfoSubList.get(0).getCollectorModel());
                mCollectorParamsInfoSub5.setChannelNumber("05");
                mCollectorParamsInfoSub5.setSensorType(mCollectorParamsInfoSubList.get(0).getSensorType());
                mCollectorParamsInfoSub5.setSensorAddress(mCollectorParamsInfoSubList.get(0).getSensorAddress());
                mCollectorParamsInfoSub5.setSensorData(mCollectorParamsInfoSubList.get(0).getSensorData());
                mCollectorParamsInfoSubList.add(mCollectorParamsInfoSub5);
                showDialog("5", channelNumber5);
                break;
            case "6":
                channelNumber6 = 6;
                //String count6 = StringUtil.formatTwo(getSwitchCount());
                //Log.i("adu","--setOpenSwitchDialog--channel6="+count6);
                CollectorSensorParamsInfoSub mCollectorParamsInfoSub6 = new CollectorSensorParamsInfoSub();
                mCollectorParamsInfoSub6.setCollectorModel(mCollectorParamsInfoSubList.get(0).getCollectorModel());
                mCollectorParamsInfoSub6.setChannelNumber("06");
                mCollectorParamsInfoSub6.setSensorType(mCollectorParamsInfoSubList.get(0).getSensorType());
                mCollectorParamsInfoSub6.setSensorAddress(mCollectorParamsInfoSubList.get(0).getSensorAddress());
                mCollectorParamsInfoSub6.setSensorData(mCollectorParamsInfoSubList.get(0).getSensorData());
                mCollectorParamsInfoSubList.add(mCollectorParamsInfoSub6);
                showDialog("6", channelNumber6);
                break;
            case "7":
                channelNumber7 = 7;
                //String count7 = StringUtil.formatTwo(getSwitchCount());
                //Log.i("adu","--setOpenSwitchDialog--channel7="+count7);
                CollectorSensorParamsInfoSub mCollectorParamsInfoSub7 = new CollectorSensorParamsInfoSub();
                mCollectorParamsInfoSub7.setCollectorModel(mCollectorParamsInfoSubList.get(0).getCollectorModel());
                mCollectorParamsInfoSub7.setChannelNumber("07");
                mCollectorParamsInfoSub7.setSensorType(mCollectorParamsInfoSubList.get(0).getSensorType());
                mCollectorParamsInfoSub7.setSensorAddress(mCollectorParamsInfoSubList.get(0).getSensorAddress());
                mCollectorParamsInfoSub7.setSensorData(mCollectorParamsInfoSubList.get(0).getSensorData());
                mCollectorParamsInfoSubList.add(mCollectorParamsInfoSub7);
                showDialog("7", channelNumber7);
                break;
            default:
        }
    }

    /**
     * 弹框
     *
     * @param
     */
    private void showDialog(String type, int channelNumber) {
        switch (type) {
            case "0":
                factory.createDialog(this, mCollectorParamsInfoSubList.get(0).getSensorType(), mCollectorParamsInfoSubList.get(0), channelNumber);
                break;
            case "1":
                factory.createDialog(this, mCollectorParamsInfoSubList.get(1).getSensorType(), mCollectorParamsInfoSubList.get(1), channelNumber);
                break;
            case "2":
                factory.createDialog(this, mCollectorParamsInfoSubList.get(2).getSensorType(), mCollectorParamsInfoSubList.get(2), channelNumber);
                break;
            case "3":
                factory.createDialog(this, mCollectorParamsInfoSubList.get(3).getSensorType(), mCollectorParamsInfoSubList.get(3), channelNumber);
                break;
            case "4":
                factory.createDialog(this, mCollectorParamsInfoSubList.get(4).getSensorType(), mCollectorParamsInfoSubList.get(4), channelNumber);
                break;
            case "5":
                factory.createDialog(this, mCollectorParamsInfoSubList.get(5).getSensorType(), mCollectorParamsInfoSubList.get(5), channelNumber);
                break;
            case "6":
                factory.createDialog(this, mCollectorParamsInfoSubList.get(6).getSensorType(), mCollectorParamsInfoSubList.get(6), channelNumber);
                break;
            case "7":
                factory.createDialog(this, mCollectorParamsInfoSubList.get(7).getSensorType(), mCollectorParamsInfoSubList.get(7), channelNumber);
                break;
            default:
        }
    }


    List<String> cmdList = new ArrayList<>();

    //生成指令
    private void sendInstruction() {

        List<CollectorSensorParamsInfoSub> list = mCollectorParamsInfoSubList;

        for (int k = 0; k < list.size() - 1; k++) {
            for (int j = k + 1; j < list.size(); j++) {
                if (list.get(k).getSensorAddress().equals(list.get(j).getSensorAddress())) {
                    Log.i("adu", list.size() + "--" + list.get(k).getSensorAddress() + "====" + list.get(j).getSensorAddress());
                    ToastUtil.showSToast("Modbus地址不能重复");
                    return;
                }
            }
        }
        StringBuilder builderFirst = new StringBuilder();
        if (list.size() != 0) {
            builderFirst.append("##150");

            for (int i = 0; i < list.size(); i++) {
                builderFirst.append(list.get(i).getCollectorModel() + StringUtil.formatStringTwo(String.valueOf(i + 1)));

                builderFirst.append(StringUtil.formatStringTwo(list.get(i).getSensorAddress()) +
                        StringUtil.formatStringTwo(list.get(i).getSensorType()));
            }
            builderFirst.append("\r\n");

            for (int j = 0; j < list.size(); j++) {
                Log.i("adu", "==list size=" + j);
                cmdList.addAll(serData(list.get(j), j));
            }
            for (int i = 0; i < cmdList.size(); i++) {
                builderFirst.append(cmdList.get(i).toString());
            }
            String result = String.valueOf(builderFirst);
            Log.i("adu", "==result==" + result);
            if (DeviceFragment.isConnected) {
                Message msg = new Message(UUID.randomUUID().toString(), result, true);
                DeviceFragment.mdBluetoothManager.writeMessage(msg);
                Log.i(LogTag.INFO_TAG, "发送result指令===" + result);
                finish();
            } else {
                ToastUtil.showSToast("蓝牙未连接");
                finish();
            }

        }


    }

    private List<String> serData(CollectorSensorParamsInfoSub paramsInfoSub, int i) {
        List<String> list = new ArrayList<>();
        StringBuilder result1 = new StringBuilder();
        StringBuilder result2 = new StringBuilder();
        switch (paramsInfoSub.getSensorType()) {
            case "02":
                //裂缝计
                SensorWireShiftInfo info = (SensorWireShiftInfo) paramsInfoSub.getSensorData();
                result1.append("##168" + StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(String.valueOf(i + 1)) +
                        info.getTriggerThreshold() + "\r\n");
                result2.append("##165" + StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(String.valueOf(i + 1)) +
                        info.getCorrectionValue() + "\r\n");
                list.add(String.valueOf(result1));
                list.add(String.valueOf(result2));
                break;
            case "03":
                //土壤含水率
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
                //测斜仪
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
                //雷达物位计
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
