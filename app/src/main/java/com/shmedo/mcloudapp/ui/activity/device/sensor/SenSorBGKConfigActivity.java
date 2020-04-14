package com.shmedo.mcloudapp.ui.activity.device.sensor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.core.enums.CollectorModel;
import com.shmedo.core.model.SensorInclinometerInfo;
import com.shmedo.core.model.SensorInfrasoundInfo;
import com.shmedo.core.model.SensorRadarLevelInfo;
import com.shmedo.core.model.SensorSoilMoistureInfo;
import com.shmedo.core.model.SensorWireShiftInfo;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;
import com.shmedo.mcloudapp.inter.Extras;
import com.shmedo.mcloudapp.inter.MyOnClickListener;
import com.shmedo.mcloudapp.ui.activity.device.BaseDeviceConnectActivity;
import com.shmedo.mcloudapp.ui.activity.device.sensor.dialog.DialogFactory;
import com.shmedo.mcloudapp.util.KeyBordUtils;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.bleutil.BlueResultParserUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device.sensor
 * 文件名:   SenSorMHConfigActivity
 * 创建者:   dpc
 * 创建时间:  2019/4/24 14:44
 * 描述：    基康渗压计 参数配置 50
 */
public class SenSorBGKConfigActivity extends BaseDeviceConnectActivity {

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.iv_stay1)
    ImageView imageView1;

    @BindView(R.id.sw_stay1)
    SwitchButton switchButton1;

    @BindView(R.id.tv_stay1)
    TextView textView1;

    @BindView(R.id.iv_stay2)
    ImageView imageView2;

    @BindView(R.id.sw_stay2)
    SwitchButton switchButton2;

    @BindView(R.id.tv_stay2)
    TextView textView2;

    @BindView(R.id.iv_stay3)
    ImageView imageView3;

    @BindView(R.id.sw_stay3)
    SwitchButton switchButton3;

    @BindView(R.id.tv_stay3)
    TextView textView3;

    @BindView(R.id.iv_stay4)
    ImageView imageView4;

    @BindView(R.id.sw_stay4)
    SwitchButton switchButton4;

    @BindView(R.id.tv_stay4)
    TextView textView4;

    @BindView(R.id.iv_stay5)
    ImageView imageView5;

    @BindView(R.id.sw_stay5)
    SwitchButton switchButton5;

    @BindView(R.id.tv_stay5)
    TextView textView5;

    @BindView(R.id.iv_stay6)
    ImageView imageView6;

    @BindView(R.id.sw_stay6)
    SwitchButton switchButton6;

    @BindView(R.id.tv_stay6)
    TextView textView6;

    @BindView(R.id.iv_stay7)
    ImageView imageView7;

    @BindView(R.id.sw_stay7)
    SwitchButton switchButton7;

    @BindView(R.id.tv_stay7)
    TextView textView7;

    @BindView(R.id.iv_stay8)
    ImageView imageView8;

    @BindView(R.id.sw_stay8)
    SwitchButton switchButton8;

    @BindView(R.id.tv_stay8)
    TextView textView8;

    private MaterialDialog.Builder mBuilder;
    private MaterialDialog mMaterialDialog;

    private List<CollectorSensorParamsInfoSub> collectorSensorParamsInfoSubs = new ArrayList<>();

    //以传感器的通道号为 Key,CollectorSensorParamsInfoSub 对象为 Value
    private HashMap<String, CollectorSensorParamsInfoSub> collectorSensorHashMap = new HashMap<>();

    private CollectorSensorParamsInfoSub defaultCollectorSensorParamsInfoSub = new CollectorSensorParamsInfoSub();

    private DialogFactory factory = new DialogFactory();

    private String collectorName = "";

    private int cmdNum = 0;


    public static void startActivity(Context context, String collectorSensorConfig) {
        Intent intent = new Intent(context, SenSorBGKConfigActivity.class);
        intent.putExtra(Extras.SENSOR_PARAMS, collectorSensorConfig);
        context.startActivity(intent);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_sensor_bgk_config;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initTitle();
    }


    private void initData() {
        String collectorSensorConfig = getIntent().getStringExtra(Extras.SENSOR_PARAMS);
        if (TextUtils.isEmpty(collectorSensorConfig)) {
            return;
        }

        String[] sensorConfigs = collectorSensorConfig.split("&&");
        for (String sensorConfig : sensorConfigs) {
            CollectorSensorParamsInfoSub mCollectorParamsInfoSub = BlueResultParserUtil.setCollectorParams(sensorConfig);
            Timber.d("--------XX采集器YY通道的传感器参数-------" + mCollectorParamsInfoSub.toString());

            collectorSensorParamsInfoSubs.add(mCollectorParamsInfoSub);
            collectorSensorHashMap.put(mCollectorParamsInfoSub.getChannelNumber(), mCollectorParamsInfoSub);
            initSwitchButton(mCollectorParamsInfoSub);
        }

        if (!collectorSensorParamsInfoSubs.isEmpty()) {
            defaultCollectorSensorParamsInfoSub = collectorSensorParamsInfoSubs.get(0);
            String sensorType = defaultCollectorSensorParamsInfoSub.getSensorType();
            setSensorIconByType(sensorType);
        }
    }

    /**
     * VW08("00"),
     * DS08("02"),
     * HD08("03"),
     * CX08("04"),
     * UDS08("06"),
     * RD08("07"),
     * SMC08("08"),
     * TH08("12"),
     * DVWP("15"),
     * QJY08("16"),
     * CS08("17"),
     * VW01("20");
     */
    private void initTitle() {
        if (defaultCollectorSensorParamsInfoSub == null)
            return;

        CollectorModel collectorModel = CollectorModel.value(defaultCollectorSensorParamsInfoSub.getCollectorModel());
        collectorName = BlueResultParserUtil.getCollectorName(collectorModel);
        mToolbarTitle.setText(collectorName);
    }

    private String channelNumber0 = "-1";
    private String channelNumber1 = "-1";
    private String channelNumber2 = "-1";
    private String channelNumber3 = "-1";
    private String channelNumber4 = "-1";
    private String channelNumber5 = "-1";
    private String channelNumber6 = "-1";
    private String channelNumber7 = "-1";

    /**
     * 初始化传感器
     *
     * @param mCollectorParamsInfoSub
     */
    private void initSwitchButton(CollectorSensorParamsInfoSub mCollectorParamsInfoSub) {
        String sensorType = mCollectorParamsInfoSub.getSensorType();
        int number = StringUtil.formatNumber(mCollectorParamsInfoSub.getChannelNumber());
        switch (number) {
            case 0:
                channelNumber0 = "00";
                switchButton1.setCheckedImmediatelyNoEvent(true);
                setSwitchViewState(true, textView1);
                break;

            case 1:
                channelNumber1 = "01";
                switchButton2.setCheckedImmediatelyNoEvent(true);
                setSwitchViewState(true, textView2);
                break;

            case 2:
                channelNumber2 = "02";
                switchButton3.setCheckedImmediatelyNoEvent(true);
                setSwitchViewState(true, textView3);
                break;

            case 3:
                channelNumber3 = "03";
                switchButton4.setCheckedImmediatelyNoEvent(true);
                setSwitchViewState(true, textView4);
                break;

            case 4:
                channelNumber4 = "04";
                switchButton5.setCheckedImmediatelyNoEvent(true);
                setSwitchViewState(true, textView5);
                break;

            case 5:
                channelNumber5 = "05";
                switchButton6.setCheckedImmediatelyNoEvent(true);
                setSwitchViewState(true, textView6);
                break;

            case 6:
                channelNumber6 = "06";
                switchButton7.setCheckedImmediatelyNoEvent(true);
                setSwitchViewState(true, textView7);
                break;

            case 7:
                channelNumber7 = "07";
                switchButton8.setCheckedImmediatelyNoEvent(true);
                setSwitchViewState(true, textView8);
                break;
        }
    }

    /**
     * 根据type的值设置不同类型传感器的图标
     */
    private void setSensorIconByType(String sensorType){
        int resId=-1;
        switch (sensorType) {
            case "02"://裂缝计 MPS-M-2000
                resId=R.drawable.ic_sensor;
                break;

            case "03"://土壤含水率 TR-3000
                resId=R.drawable.ic_sensor_soilmoisture;
                break;

            case "04"://测斜仪 I-P-I
                resId=R.drawable.ic_sensor;
                break;

            case "06"://超声波物位计 HBRD908
                resId=R.drawable.ic_sensor;
                break;

            case "07"://雷达物位计 MH-A15R
                resId=R.drawable.ic_sensor;
                break;

            case "08"://墒情计 EP100G
                resId=R.drawable.ic_sensor;
                break;

            case "12"://温湿度计 CSW18
                resId=R.drawable.ic_sensor;
                break;

            case "15"://扬压力计 VWP-G
                resId=R.drawable.ic_sensor;
                break;

            case "16"://陆岩倾角仪 LY215
                resId=R.drawable.ic_sensor;
                break;

            case "21"://次声传感器
                resId=R.drawable.ic_sensor;
                break;

            case "50"://基康渗压计 BGK-4500
                resId=R.drawable.ic_sensor;
                break;

            case "51"://葛南渗压计 VWP-03
                resId=R.drawable.ic_sensor;
                break;

            case "52"://葛南土压力盒 VWE-0.6
                resId=R.drawable.ic_sensor;
                break;

            case "53"://葛南应力计 VWS-15
                resId=R.drawable.ic_sensor;
                break;

            case "54"://葛南无应力计 VWS-15M
                resId=R.drawable.ic_sensor;
                break;

            case "55"://葛南位移计 VWD-100
                resId=R.drawable.ic_sensor;
                break;

            default:
                break;
        }

        imageView1.setImageResource(resId);
        imageView2.setImageResource(resId);
        imageView3.setImageResource(resId);
        imageView4.setImageResource(resId);
        imageView5.setImageResource(resId);
        imageView6.setImageResource(resId);
        imageView7.setImageResource(resId);
        imageView8.setImageResource(resId);
    }


    @OnCheckedChanged({R.id.sw_stay1, R.id.sw_stay2, R.id.sw_stay3, R.id.sw_stay4, R.id.sw_stay5, R.id.sw_stay6, R.id.sw_stay7, R.id.sw_stay8})
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sw_stay1:
                toggleSwitchView(textView1, switchButton1, "00");
                break;

            case R.id.sw_stay2:
                toggleSwitchView(textView2, switchButton2, "01");
                break;

            case R.id.sw_stay3:
                toggleSwitchView(textView3, switchButton3, "02");
                break;

            case R.id.sw_stay4:
                toggleSwitchView(textView4, switchButton4, "03");
                break;

            case R.id.sw_stay5:
                toggleSwitchView(textView5, switchButton5, "04");
                break;

            case R.id.sw_stay6:
                toggleSwitchView(textView6, switchButton6, "05");
                break;

            case R.id.sw_stay7:
                toggleSwitchView(textView7, switchButton7, "06");
                break;

            case R.id.sw_stay8:
                toggleSwitchView(textView8, switchButton8, "07");
                break;
        }
    }


    /**
     * 点击imageview弹框
     * 点击switch 进行关闭、打开状态
     *
     * @param view
     */
    @OnClick({R.id.back, R.id.iv_stay1, R.id.iv_stay2, R.id.iv_stay3, R.id.iv_stay4, R.id.iv_stay5, R.id.iv_stay6, R.id.iv_stay7, R.id.iv_stay8, R.id.btn_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                break;

            case R.id.iv_stay1://弹框
                if (channelNumber0.equals("-1")) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialogForModify(channelNumber0);
                break;

            case R.id.iv_stay2:
                if (channelNumber1.equals("-1")) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialogForModify(channelNumber1);
                break;

            case R.id.iv_stay3:
                if (channelNumber2.equals("-1")) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialogForModify(channelNumber2);
                break;

            case R.id.iv_stay4:
                if (channelNumber3.equals("-1")) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialogForModify(channelNumber3);
                break;

            case R.id.iv_stay5:
                if (channelNumber4.equals("-1")) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialogForModify(channelNumber4);
                break;

            case R.id.iv_stay6:
                if (channelNumber5.equals("-1")) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialogForModify(channelNumber5);
                break;

            case R.id.iv_stay7:
                if (channelNumber6.equals("-1")) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialogForModify(channelNumber6);
                break;

            case R.id.iv_stay8:
                if (channelNumber7.equals("-1")) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialogForModify(channelNumber7);
                break;

            case R.id.btn_confirm://确定发送指令
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    finish();
                    return;
                }
                sendInstruction();
                break;
        }
    }


    private void setSwitchViewState(boolean isOpen, TextView mTvStay) {
        mTvStay.setText(isOpen ? "已启用" : "已停用");
        mTvStay.setBackgroundColor(getResources().getColor(isOpen ? R.color.colorPrimaryDark : R.color.secondary_text));
    }

    private void toggleSwitchView(TextView textView, SwitchButton switchButton, String channelNumber) {
        if (switchButton.isChecked()) {
            setSwitchViewState(true, textView);
            setOpenSwitchDialog(channelNumber);
        } else {
            switchButton.setCheckedImmediatelyNoEvent(!switchButton.isChecked());
            closeSwitchColorbg(textView, switchButton, channelNumber);
        }
    }


    /**
     * 关闭采集器设备  确定后停用6226560118056462
     */
    private void closeSwitchColorbg(final TextView textView, final SwitchButton switchButton, final String channelNumber) {
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
                switchButton.setCheckedImmediatelyNoEvent(true);
                setSwitchViewState(true, textView);
            }
        });
        mBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                switchButton.setCheckedImmediatelyNoEvent(false);
                setSwitchViewState(false, textView);

                //停用后停止
                switch (channelNumber) {
                    case "00":
                        channelNumber0 = "-1";
                        collectorSensorHashMap.remove(channelNumber);
                        break;

                    case "01":
                        channelNumber1 = "-1";
                        collectorSensorHashMap.remove(channelNumber);
                        break;

                    case "02":
                        channelNumber2 = "-1";
                        collectorSensorHashMap.remove(channelNumber);
                        break;

                    case "03":
                        channelNumber3 = "-1";
                        collectorSensorHashMap.remove(channelNumber);
                        break;

                    case "04":
                        channelNumber4 = "-1";
                        collectorSensorHashMap.remove(channelNumber);
                        break;

                    case "05":
                        channelNumber5 = "-1";
                        collectorSensorHashMap.remove(channelNumber);
                        break;

                    case "06":
                        channelNumber6 = "-1";
                        collectorSensorHashMap.remove(channelNumber);
                        break;

                    case "07":
                        channelNumber7 = "-1";
                        collectorSensorHashMap.remove(channelNumber);
                        break;
                }
            }
        });
    }


    private void setOpenSwitchDialog(String channelNumber) {
        switch (channelNumber) {
            case "00":
                channelNumber0 = channelNumber;
                if (!collectorSensorHashMap.containsKey(channelNumber)) {
                    CollectorSensorParamsInfoSub collectorSensorParamsInfoSub = new CollectorSensorParamsInfoSub();
                    collectorSensorParamsInfoSub.setCollectorModel(defaultCollectorSensorParamsInfoSub.getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber(channelNumber);
                    collectorSensorParamsInfoSub.setSensorType(defaultCollectorSensorParamsInfoSub.getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress(defaultCollectorSensorParamsInfoSub.getSensorAddress());
                    collectorSensorParamsInfoSub.setSensorData(defaultCollectorSensorParamsInfoSub.getSensorData());
                    collectorSensorHashMap.put(channelNumber, collectorSensorParamsInfoSub);
                }
                showBottomDialogForAdd(channelNumber0);
                break;

            case "01":
                channelNumber1 = channelNumber;
                if (!collectorSensorHashMap.containsKey(channelNumber)) {
                    CollectorSensorParamsInfoSub collectorSensorParamsInfoSub = new CollectorSensorParamsInfoSub();
                    collectorSensorParamsInfoSub.setCollectorModel(defaultCollectorSensorParamsInfoSub.getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber(channelNumber);
                    collectorSensorParamsInfoSub.setSensorType(defaultCollectorSensorParamsInfoSub.getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress(channelNumber);
                    collectorSensorParamsInfoSub.setSensorData(defaultCollectorSensorParamsInfoSub.getSensorData());
                    collectorSensorHashMap.put(channelNumber, collectorSensorParamsInfoSub);
                }
                showBottomDialogForAdd(channelNumber1);
                break;

            case "02":
                channelNumber2 = channelNumber;
                if (!collectorSensorHashMap.containsKey(channelNumber)) {
                    CollectorSensorParamsInfoSub collectorSensorParamsInfoSub = new CollectorSensorParamsInfoSub();
                    collectorSensorParamsInfoSub.setCollectorModel(defaultCollectorSensorParamsInfoSub.getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber(channelNumber);
                    collectorSensorParamsInfoSub.setSensorType(defaultCollectorSensorParamsInfoSub.getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress(channelNumber);
                    collectorSensorParamsInfoSub.setSensorData(defaultCollectorSensorParamsInfoSub.getSensorData());
                    collectorSensorHashMap.put(channelNumber, collectorSensorParamsInfoSub);
                }
                showBottomDialogForAdd(channelNumber2);
                break;

            case "03":
                channelNumber3 = channelNumber;
                if (!collectorSensorHashMap.containsKey(channelNumber)) {
                    CollectorSensorParamsInfoSub collectorSensorParamsInfoSub = new CollectorSensorParamsInfoSub();
                    collectorSensorParamsInfoSub.setCollectorModel(defaultCollectorSensorParamsInfoSub.getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber(channelNumber);
                    collectorSensorParamsInfoSub.setSensorType(defaultCollectorSensorParamsInfoSub.getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress(channelNumber);
                    collectorSensorParamsInfoSub.setSensorData(defaultCollectorSensorParamsInfoSub.getSensorData());
                    collectorSensorHashMap.put(channelNumber, collectorSensorParamsInfoSub);
                }
                showBottomDialogForAdd(channelNumber3);
                break;

            case "04":
                channelNumber4 = channelNumber;
                if (!collectorSensorHashMap.containsKey(channelNumber)) {
                    CollectorSensorParamsInfoSub collectorSensorParamsInfoSub = new CollectorSensorParamsInfoSub();
                    collectorSensorParamsInfoSub.setCollectorModel(defaultCollectorSensorParamsInfoSub.getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber(channelNumber);
                    collectorSensorParamsInfoSub.setSensorType(defaultCollectorSensorParamsInfoSub.getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress(channelNumber);
                    collectorSensorParamsInfoSub.setSensorData(defaultCollectorSensorParamsInfoSub.getSensorData());
                    collectorSensorHashMap.put(channelNumber, collectorSensorParamsInfoSub);
                }
                showBottomDialogForAdd(channelNumber4);
                break;

            case "05":
                channelNumber5 = channelNumber;
                if (!collectorSensorHashMap.containsKey(channelNumber)) {
                    CollectorSensorParamsInfoSub collectorSensorParamsInfoSub = new CollectorSensorParamsInfoSub();
                    collectorSensorParamsInfoSub.setCollectorModel(defaultCollectorSensorParamsInfoSub.getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber(channelNumber);
                    collectorSensorParamsInfoSub.setSensorType(defaultCollectorSensorParamsInfoSub.getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress(channelNumber);
                    collectorSensorParamsInfoSub.setSensorData(defaultCollectorSensorParamsInfoSub.getSensorData());
                    collectorSensorHashMap.put(channelNumber, collectorSensorParamsInfoSub);
                }
                showBottomDialogForAdd(channelNumber5);
                break;

            case "06":
                channelNumber6 = channelNumber;
                if (!collectorSensorHashMap.containsKey(channelNumber)) {
                    CollectorSensorParamsInfoSub collectorSensorParamsInfoSub = new CollectorSensorParamsInfoSub();
                    collectorSensorParamsInfoSub.setCollectorModel(defaultCollectorSensorParamsInfoSub.getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber(channelNumber);
                    collectorSensorParamsInfoSub.setSensorType(defaultCollectorSensorParamsInfoSub.getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress(channelNumber);
                    collectorSensorParamsInfoSub.setSensorData(defaultCollectorSensorParamsInfoSub.getSensorData());
                    collectorSensorHashMap.put(channelNumber, collectorSensorParamsInfoSub);
                }
                showBottomDialogForAdd(channelNumber6);
                break;

            case "07":
                channelNumber7 = channelNumber;
                if (!collectorSensorHashMap.containsKey(channelNumber)) {
                    CollectorSensorParamsInfoSub collectorSensorParamsInfoSub = new CollectorSensorParamsInfoSub();
                    collectorSensorParamsInfoSub.setCollectorModel(defaultCollectorSensorParamsInfoSub.getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber(channelNumber);
                    collectorSensorParamsInfoSub.setSensorType(defaultCollectorSensorParamsInfoSub.getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress(channelNumber);
                    collectorSensorParamsInfoSub.setSensorData(defaultCollectorSensorParamsInfoSub.getSensorData());
                    collectorSensorHashMap.put(channelNumber, collectorSensorParamsInfoSub);
                }
                showBottomDialogForAdd(channelNumber7);
                break;

            default:
        }
    }

    /**
     * 弹框
     *
     * @param
     */
    private void showBottomDialogForAdd(String channelNumber) {
        factory.createDialog(this, collectorSensorHashMap.get(channelNumber).getSensorType(), collectorSensorHashMap.get(channelNumber), channelNumber, addClickListener);
    }

    private void showBottomDialogForModify(String channelNumber) {
        factory.createDialog(this, collectorSensorHashMap.get(channelNumber).getSensorType(), collectorSensorHashMap.get(channelNumber), channelNumber, modifyClickListener);
    }

    private MyOnClickListener addClickListener = new MyOnClickListener() {
        @Override
        public boolean onSureClick(View view) {
            KeyBordUtils.hideSoftKeyboard(view);

            List<CollectorSensorParamsInfoSub> paramsInfoSubList = new ArrayList<>();
            paramsInfoSubList.addAll(collectorSensorHashMap.values());
            if (paramsInfoSubList.isEmpty()) {
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
        public void onCancelClick(View view, String channelNumber) {
            KeyBordUtils.hideSoftKeyboard(view);

            switch (channelNumber) {
                case "00":
                    switchButton1.setCheckedImmediatelyNoEvent(false);
                    setSwitchViewState(false, textView1);
                    channelNumber0 = "-1";
                    collectorSensorHashMap.remove(channelNumber);
                    break;

                case "01":
                    switchButton2.setCheckedImmediatelyNoEvent(false);
                    setSwitchViewState(false, textView2);
                    channelNumber1 = "-1";
                    collectorSensorHashMap.remove(channelNumber);
                    break;

                case "02":
                    switchButton3.setCheckedImmediatelyNoEvent(false);
                    setSwitchViewState(false, textView3);
                    channelNumber2 = "-1";
                    collectorSensorHashMap.remove(channelNumber);
                    break;

                case "03":
                    switchButton4.setCheckedImmediatelyNoEvent(false);
                    setSwitchViewState(false, textView4);
                    channelNumber3 = "-1";
                    collectorSensorHashMap.remove(channelNumber);
                    break;

                case "04":
                    switchButton5.setCheckedImmediatelyNoEvent(false);
                    setSwitchViewState(false, textView5);
                    channelNumber4 = "-1";
                    collectorSensorHashMap.remove(channelNumber);
                    break;

                case "05":
                    switchButton6.setCheckedImmediatelyNoEvent(false);
                    setSwitchViewState(false, textView6);
                    channelNumber5 = "-1";
                    collectorSensorHashMap.remove(channelNumber);
                    break;

                case "06":
                    switchButton7.setCheckedImmediatelyNoEvent(false);
                    setSwitchViewState(false, textView7);
                    channelNumber6 = "-1";
                    collectorSensorHashMap.remove(channelNumber);
                    break;

                case "07":
                    switchButton8.setCheckedImmediatelyNoEvent(false);
                    setSwitchViewState(false, textView8);
                    channelNumber7 = "-1";
                    collectorSensorHashMap.remove(channelNumber);
                    break;

                default:
            }
        }
    };


    private MyOnClickListener modifyClickListener = new MyOnClickListener() {
        @Override
        public boolean onSureClick(View view) {
            KeyBordUtils.hideSoftKeyboard(view);

            List<CollectorSensorParamsInfoSub> paramsInfoSubList = new ArrayList<>();
            paramsInfoSubList.addAll(collectorSensorHashMap.values());
            if (paramsInfoSubList.isEmpty()) {
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
        public void onCancelClick(View view, String channelNumber) {
            KeyBordUtils.hideSoftKeyboard(view);
        }
    };
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

        //##150zzxxXXXX\r\n：设置采集器接入的传感器
        StringBuilder builderFirst = new StringBuilder();
        builderFirst.append("##150");
        builderFirst.append(defaultCollectorSensorParamsInfoSub.getCollectorModel() + StringUtil.formatStringTwo(String.valueOf(collectorSensorParamsInfoSubs.size())));
        for (CollectorSensorParamsInfoSub paramsInfoSub : collectorSensorParamsInfoSubs) {
            builderFirst.append(StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) + StringUtil.formatStringTwo(paramsInfoSub.getSensorType()));
        }
        builderFirst.append("\r\n");

        String result = String.valueOf(builderFirst);

        startProgressRunnable("正在发送配置指令...", 10000);
        sendCommonCommand(result);
        Timber.d("设置采集器接入的传感器指令===" + result);

        cmdNum = 0;
        for (int i = 0; i < collectorSensorParamsInfoSubs.size(); i++) {
            setSensorValue(collectorSensorParamsInfoSubs.get(i));
        }
    }


    private String cmdTriggerThreshold = "";
    private String cmdCorrectionValue = "";

    private void setSensorValue(CollectorSensorParamsInfoSub paramsInfoSub) {
        CollectorModel collectorModel = CollectorModel.value(paramsInfoSub.getCollectorModel());
        switch (collectorModel) {
            case DS08://裂缝计采集器
                SensorWireShiftInfo sensorWireShiftInfo = (SensorWireShiftInfo) paramsInfoSub.getSensorData();
                cmdTriggerThreshold = "##168" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorWireShiftInfo.getTriggerThreshold() + "\r\n";

                cmdCorrectionValue = "##165" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorWireShiftInfo.getCorrectionValue() + "\r\n";
                break;

            case CS08://次声采集器
                SensorInfrasoundInfo sensorInfrasoundInfo = (SensorInfrasoundInfo) paramsInfoSub.getSensorData();
                cmdTriggerThreshold = "##168" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorInfrasoundInfo.getTriggerThreshold() + "\r\n";

                cmdCorrectionValue = "##165" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorInfrasoundInfo.getRevised() + "\r\n";
                break;

            case HD08://土壤湿度采集器
                SensorSoilMoistureInfo sensorSoilMoistureInfo = (SensorSoilMoistureInfo) paramsInfoSub.getSensorData();
                cmdTriggerThreshold = "##168" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorSoilMoistureInfo.getTriggerThreshold() + "\r\n";

                cmdCorrectionValue = "##165" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorSoilMoistureInfo.getRevised() + "\r\n";
                break;

            case RD08://雷达采集器
                SensorRadarLevelInfo sensorRadarLevelInfo = (SensorRadarLevelInfo) paramsInfoSub.getSensorData();
                cmdTriggerThreshold = "##168" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorRadarLevelInfo.getTriggerThreshold() + "\r\n";

                cmdCorrectionValue = "##165" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorRadarLevelInfo.getRevised() + "\r\n";
                break;

            case CX08://测斜仪采集器
                SensorInclinometerInfo sensorInclinometerInfo = (SensorInclinometerInfo) paramsInfoSub.getSensorData();
                cmdTriggerThreshold = "##168" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorInclinometerInfo.getTriggerThreshold() + "\r\n";

                cmdCorrectionValue = "##165" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorInclinometerInfo.getCorrectionValue() + "\r\n";
                break;
        }

        sendCommonCommand(cmdTriggerThreshold);
        Timber.d("设置" + collectorName + "的传感器触发阈值指令===" + cmdTriggerThreshold);
    }


    /**
     * 设置显示数据
     */
    private void setResultData(String cmdStr) {
        if (cmdStr.startsWith("$$168") && cmdStr.endsWith("\r\n")) {
            if (cmdStr.startsWith("$$168e") || cmdStr.startsWith("$$168ce")) {
                ToastUtils.show(collectorName + "的传感器触发阈值配置错误!");
                stopProgressRunnable();
                return;
            }
            sendCommonCommandImmediately(cmdCorrectionValue);
            Timber.d("设置" + collectorName + "的传感器修正值指令===" + cmdCorrectionValue);
            return;
        }

        //最后一个传感器参数设置指令
        if (cmdStr.startsWith("$$165") && cmdStr.endsWith("\r\n")) {
            if (cmdStr.startsWith("$$165e") || cmdStr.startsWith("$$165ce")) {
                ToastUtils.show(collectorName + "的传感器修正值配置错误!");
                stopProgressRunnable();
                return;
            }
            cmdNum++;

            if (cmdNum == collectorSensorParamsInfoSubs.size()) {
                stopProgressRunnable();
                ToastUtils.show("设置完成");
                hander.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 2000);
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (!TextUtils.isEmpty(messageEvent) && messageEvent.startsWith("$$")) {
            setResultData(messageEvent);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
