package com.shmedo.mcloudapp.deviceconfig.ui.activity.sensor;

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
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.configlibrary.ble.enums.CollectorModel;
import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.model.CollectorSensorParamsInfo;
import com.shmedo.configlibrary.ble.model.SensorGudanPercolateInfo;
import com.shmedo.configlibrary.ble.model.SensorInclinometerInfo;
import com.shmedo.configlibrary.ble.model.SensorInfrasoundInfo;
import com.shmedo.configlibrary.ble.model.SensorJunXingZljInfo;
import com.shmedo.configlibrary.ble.model.SensorKangPercolateInfo;
import com.shmedo.configlibrary.ble.model.SensorRadarLevelInfo;
import com.shmedo.configlibrary.ble.model.SensorSoilMoistureInfo;
import com.shmedo.configlibrary.ble.model.SensorWireShiftInfo;
import com.shmedo.configlibrary.ble.utils.ResultParserUtil;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseDeviceConnectActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.CommonSensorConfigDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.Osmometer_AxialForceGaugeDialogFragment;
import com.shmedo.mcloudapp.util.KeyBordUtils;
import com.shmedo.mcloudapp.util.bleutil.BlueResultParserUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/8 <br/>
 * 描述：    传感器基础配置页面
 */
public abstract class BaseSensorConfigActivity extends BaseDeviceConnectActivity implements BaseDialogFragment.DialogFragmentClickListener<CollectorSensorParamsInfo> {

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

    protected List<CollectorSensorParamsInfo> collectorSensorParamsInfoSubs = new ArrayList<>();
    //以传感器的通道号为 Key,CollectorSensorParamsInfo 对象为 Value
    protected HashMap<String, CollectorSensorParamsInfo> collectorSensorHashMap = new HashMap<>();
    protected CollectorSensorParamsInfo defaultCollectorSensorParamsInfo = new CollectorSensorParamsInfo();

    protected String collectorName = "";

    private String channelNumber0 = "-1";
    private String channelNumber1 = "-1";
    private String channelNumber2 = "-1";
    private String channelNumber3 = "-1";
    private String channelNumber4 = "-1";
    private String channelNumber5 = "-1";
    private String channelNumber6 = "-1";
    private String channelNumber7 = "-1";

    private String curChannelNumber = "";
    private boolean isEnableNewSensor = false;
    protected int sensorIndex = 0;//接入的传感器索引号
    protected StringBuilder sbCollectorSensorConfig = new StringBuilder();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_sensor_bgk_config;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initTitle();
    }

    /**
     * 接同种传感器参数信息:
     * $$1010200,3,2,7,1.100000&&$$1010201,5,2,5,0.000000&&$$1010202,2,2,7,1.100000&&
     * <p>
     * 接多种传感器参数信息:
     * $$1010000,00,50,2.000000e+00,4.597945e-08,3.000000e+00,4.000000e+00,5.000000e+00,3.800000e+01,3.300000e+01,2.300000e+01,6.600000e+01&&
     * $$1010001,01,51,2.000000e+00,4.597945e-08,3.000000e+00,4.400000e+01,3.800000e+01,1.300000e+01,5.000000e+00,1.600000e+01&&
     * $$1010002,02,58,2.000000e+00,4.597945e-08,3.000000e+00,5.000000e+00&&
     */
    private void initData() {
        String collectorSensorConfig = getIntent().getStringExtra(AppContants.Extras.SPLICE_SENSOR_PARAMS);
        if (TextUtils.isEmpty(collectorSensorConfig)) {
            Timber.e("传递的传感器参数为空!");
            return;
        }

        String[] sensorConfigs = collectorSensorConfig.split("&&");
        for (String sensorConfig : sensorConfigs) {
            if (TextUtils.isEmpty(sensorConfig))
                continue;

            CollectorSensorParamsInfo mCollectorParamsInfoSub = ResultParserUtil.getEntityObject(sensorConfig);
            Timber.d("--------XX采集器YY通道的传感器参数-------%s", mCollectorParamsInfoSub.toString());
            collectorSensorHashMap.put(mCollectorParamsInfoSub.getChannelNumber(), mCollectorParamsInfoSub);
            initSensorState(mCollectorParamsInfoSub);
        }

        if (!collectorSensorHashMap.values().isEmpty()) {
            defaultCollectorSensorParamsInfo = (CollectorSensorParamsInfo) collectorSensorHashMap.values().toArray()[0];
        }
    }

    /**
     * 采集器名称作为标题
     */
    private void initTitle() {
        if (defaultCollectorSensorParamsInfo == null)
            return;

        CollectorModel collectorModel = defaultCollectorSensorParamsInfo.getCollectorModel();
        collectorName = BlueResultParserUtil.getCollectorName(collectorModel);
        mToolbarTitle.setText(collectorName);
    }

    /**
     * 初始化传感器启用状态
     *
     * @param mCollectorParamsInfoSub
     */
    private void initSensorState(CollectorSensorParamsInfo mCollectorParamsInfoSub) {
        int number = StringUtil.formatNumber(mCollectorParamsInfoSub.getChannelNumber());
        switch (number) {
            case 0:
                channelNumber0 = "00";
                switchButton1.setCheckedImmediatelyNoEvent(true);
                setBgColorAndText(true, textView1);
                break;

            case 1:
                channelNumber1 = "01";
                switchButton2.setCheckedImmediatelyNoEvent(true);
                setBgColorAndText(true, textView2);
                break;

            case 2:
                channelNumber2 = "02";
                switchButton3.setCheckedImmediatelyNoEvent(true);
                setBgColorAndText(true, textView3);
                break;

            case 3:
                channelNumber3 = "03";
                switchButton4.setCheckedImmediatelyNoEvent(true);
                setBgColorAndText(true, textView4);
                break;

            case 4:
                channelNumber4 = "04";
                switchButton5.setCheckedImmediatelyNoEvent(true);
                setBgColorAndText(true, textView5);
                break;

            case 5:
                channelNumber5 = "05";
                switchButton6.setCheckedImmediatelyNoEvent(true);
                setBgColorAndText(true, textView6);
                break;

            case 6:
                channelNumber6 = "06";
                switchButton7.setCheckedImmediatelyNoEvent(true);
                setBgColorAndText(true, textView7);
                break;

            case 7:
                channelNumber7 = "07";
                switchButton8.setCheckedImmediatelyNoEvent(true);
                setBgColorAndText(true, textView8);
                break;
        }
    }

    @OnCheckedChanged({R.id.sw_stay1, R.id.sw_stay2, R.id.sw_stay3, R.id.sw_stay4, R.id.sw_stay5, R.id.sw_stay6, R.id.sw_stay7, R.id.sw_stay8})
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sw_stay1:
                toggleSensor(textView1, switchButton1, "00");
                break;

            case R.id.sw_stay2:
                toggleSensor(textView2, switchButton2, "01");
                break;

            case R.id.sw_stay3:
                toggleSensor(textView3, switchButton3, "02");
                break;

            case R.id.sw_stay4:
                toggleSensor(textView4, switchButton4, "03");
                break;

            case R.id.sw_stay5:
                toggleSensor(textView5, switchButton5, "04");
                break;

            case R.id.sw_stay6:
                toggleSensor(textView6, switchButton6, "05");
                break;

            case R.id.sw_stay7:
                toggleSensor(textView7, switchButton7, "06");
                break;

            case R.id.sw_stay8:
                toggleSensor(textView8, switchButton8, "07");
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
                showBottomDialog(channelNumber0, false);
                break;

            case R.id.iv_stay2:
                if (channelNumber1.equals("-1")) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialog(channelNumber1, false);
                break;

            case R.id.iv_stay3:
                if (channelNumber2.equals("-1")) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialog(channelNumber2, false);
                break;

            case R.id.iv_stay4:
                if (channelNumber3.equals("-1")) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialog(channelNumber3, false);
                break;

            case R.id.iv_stay5:
                if (channelNumber4.equals("-1")) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialog(channelNumber4, false);
                break;

            case R.id.iv_stay6:
                if (channelNumber5.equals("-1")) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialog(channelNumber5, false);
                break;

            case R.id.iv_stay7:
                if (channelNumber6.equals("-1")) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialog(channelNumber6, false);
                break;

            case R.id.iv_stay8:
                if (channelNumber7.equals("-1")) {
                    ToastUtils.show("此传感器为空");
                    return;
                }
                showBottomDialog(channelNumber7, false);
                break;

            case R.id.btn_confirm://确定发送指令
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    finish();
                    return;
                }
                sendInstruction();
                break;
        }
    }

    private void setBgColorAndText(boolean isOpen, TextView mTvStay) {
        mTvStay.setText(isOpen ? "已启用" : "已停用");
        mTvStay.setBackgroundColor(getResources().getColor(isOpen ? R.color.colorPrimaryDark : R.color.secondary_text));
    }

    private void toggleSensor(TextView textView, SwitchButton switchButton, String channelNumber) {
        if (switchButton.isChecked()) {
            setBgColorAndText(true, textView);
            enableSensor(channelNumber);
        } else {
            switchButton.setCheckedImmediatelyNoEvent(!switchButton.isChecked());
            disableSensor(textView, switchButton, channelNumber);
        }
    }

    /**
     * 关闭传感器
     */
    private void disableSensor(final TextView textView, final SwitchButton switchButton, final String channelNumber) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this);
        mBuilder.title("温馨提示：")
                .content("确认要停用该传感器吗？")
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消");
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
        mBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                switchButton.setCheckedImmediatelyNoEvent(false);
                setBgColorAndText(false, textView);
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

    /**
     * 启用传感器
     */
    private void enableSensor(String channelNumber) {
        switch (channelNumber) {
            case "00":
                channelNumber0 = channelNumber;
                if (!collectorSensorHashMap.containsKey(channelNumber)) {
                    CollectorSensorParamsInfo collectorSensorParamsInfoSub = new CollectorSensorParamsInfo();
                    collectorSensorParamsInfoSub.setCollectorModel(defaultCollectorSensorParamsInfo.getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber(channelNumber);
                    collectorSensorParamsInfoSub.setSensorType(defaultCollectorSensorParamsInfo.getSensorType());
                    collectorSensorParamsInfoSub.setChannelNumber(defaultCollectorSensorParamsInfo.getChannelNumber());
                    collectorSensorParamsInfoSub.setSensorData(defaultCollectorSensorParamsInfo.getSensorData());
                    collectorSensorHashMap.put(channelNumber, collectorSensorParamsInfoSub);
                }
                showBottomDialog(channelNumber0, true);
                break;

            case "01":
                channelNumber1 = channelNumber;
                if (!collectorSensorHashMap.containsKey(channelNumber)) {
                    CollectorSensorParamsInfo collectorSensorParamsInfoSub = new CollectorSensorParamsInfo();
                    collectorSensorParamsInfoSub.setCollectorModel(defaultCollectorSensorParamsInfo.getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber(channelNumber);
                    collectorSensorParamsInfoSub.setSensorType(defaultCollectorSensorParamsInfo.getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress(channelNumber);
                    collectorSensorParamsInfoSub.setSensorData(defaultCollectorSensorParamsInfo.getSensorData());
                    collectorSensorHashMap.put(channelNumber, collectorSensorParamsInfoSub);
                }
                showBottomDialog(channelNumber1, true);
                break;

            case "02":
                channelNumber2 = channelNumber;
                if (!collectorSensorHashMap.containsKey(channelNumber)) {
                    CollectorSensorParamsInfo collectorSensorParamsInfoSub = new CollectorSensorParamsInfo();
                    collectorSensorParamsInfoSub.setCollectorModel(defaultCollectorSensorParamsInfo.getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber(channelNumber);
                    collectorSensorParamsInfoSub.setSensorType(defaultCollectorSensorParamsInfo.getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress(channelNumber);
                    collectorSensorParamsInfoSub.setSensorData(defaultCollectorSensorParamsInfo.getSensorData());
                    collectorSensorHashMap.put(channelNumber, collectorSensorParamsInfoSub);
                }
                showBottomDialog(channelNumber2, true);
                break;

            case "03":
                channelNumber3 = channelNumber;
                if (!collectorSensorHashMap.containsKey(channelNumber)) {
                    CollectorSensorParamsInfo collectorSensorParamsInfoSub = new CollectorSensorParamsInfo();
                    collectorSensorParamsInfoSub.setCollectorModel(defaultCollectorSensorParamsInfo.getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber(channelNumber);
                    collectorSensorParamsInfoSub.setSensorType(defaultCollectorSensorParamsInfo.getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress(channelNumber);
                    collectorSensorParamsInfoSub.setSensorData(defaultCollectorSensorParamsInfo.getSensorData());
                    collectorSensorHashMap.put(channelNumber, collectorSensorParamsInfoSub);
                }
                showBottomDialog(channelNumber3, true);
                break;

            case "04":
                channelNumber4 = channelNumber;
                if (!collectorSensorHashMap.containsKey(channelNumber)) {
                    CollectorSensorParamsInfo collectorSensorParamsInfoSub = new CollectorSensorParamsInfo();
                    collectorSensorParamsInfoSub.setCollectorModel(defaultCollectorSensorParamsInfo.getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber(channelNumber);
                    collectorSensorParamsInfoSub.setSensorType(defaultCollectorSensorParamsInfo.getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress(channelNumber);
                    collectorSensorParamsInfoSub.setSensorData(defaultCollectorSensorParamsInfo.getSensorData());
                    collectorSensorHashMap.put(channelNumber, collectorSensorParamsInfoSub);
                }
                showBottomDialog(channelNumber4, true);
                break;

            case "05":
                channelNumber5 = channelNumber;
                if (!collectorSensorHashMap.containsKey(channelNumber)) {
                    CollectorSensorParamsInfo collectorSensorParamsInfoSub = new CollectorSensorParamsInfo();
                    collectorSensorParamsInfoSub.setCollectorModel(defaultCollectorSensorParamsInfo.getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber(channelNumber);
                    collectorSensorParamsInfoSub.setSensorType(defaultCollectorSensorParamsInfo.getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress(channelNumber);
                    collectorSensorParamsInfoSub.setSensorData(defaultCollectorSensorParamsInfo.getSensorData());
                    collectorSensorHashMap.put(channelNumber, collectorSensorParamsInfoSub);
                }
                showBottomDialog(channelNumber5, true);
                break;

            case "06":
                channelNumber6 = channelNumber;
                if (!collectorSensorHashMap.containsKey(channelNumber)) {
                    CollectorSensorParamsInfo collectorSensorParamsInfoSub = new CollectorSensorParamsInfo();
                    collectorSensorParamsInfoSub.setCollectorModel(defaultCollectorSensorParamsInfo.getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber(channelNumber);
                    collectorSensorParamsInfoSub.setSensorType(defaultCollectorSensorParamsInfo.getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress(channelNumber);
                    collectorSensorParamsInfoSub.setSensorData(defaultCollectorSensorParamsInfo.getSensorData());
                    collectorSensorHashMap.put(channelNumber, collectorSensorParamsInfoSub);
                }
                showBottomDialog(channelNumber6, true);
                break;

            case "07":
                channelNumber7 = channelNumber;
                if (!collectorSensorHashMap.containsKey(channelNumber)) {
                    CollectorSensorParamsInfo collectorSensorParamsInfoSub = new CollectorSensorParamsInfo();
                    collectorSensorParamsInfoSub.setCollectorModel(defaultCollectorSensorParamsInfo.getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber(channelNumber);
                    collectorSensorParamsInfoSub.setSensorType(defaultCollectorSensorParamsInfo.getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress(channelNumber);
                    collectorSensorParamsInfoSub.setSensorData(defaultCollectorSensorParamsInfo.getSensorData());
                    collectorSensorHashMap.put(channelNumber, collectorSensorParamsInfoSub);
                }
                showBottomDialog(channelNumber7, true);
                break;
        }
    }

    private void showBottomDialog(String channelNumber, boolean isAddOrModifySensor) {
        BaseDialogFragment newFragment;

        curChannelNumber = channelNumber;
        isEnableNewSensor = isAddOrModifySensor;
        CollectorSensorParamsInfo collectorSensorParamsInfoSub = collectorSensorHashMap.get(channelNumber);
        SensorType sensorType = collectorSensorParamsInfoSub.getSensorType();
        switch (sensorType) {
            case WIRE_SHIFT://拉线位移计
            case SOIL_MOISTURE://土壤含水率
            case INCLINOMETER://测斜仪
            case RADAR_LEVEL_GAUGE://雷达物位计
            case INFRASOUND_SENSOR://次声
                newFragment = new CommonSensorConfigDialogFragment();
                newFragment.show(getSupportFragmentManager(), "dialog");
                break;

            case KANG_PERCOLATE://基康渗压计(BGK-4500)
            case GUDAN_PERCOLATE://葛南渗压计(VWP-03)
            case JUNXING_ZLJ_300T://轴力计(ZLJ-300T)
                newFragment = new Osmometer_AxialForceGaugeDialogFragment();
                newFragment.show(getSupportFragmentManager(), "dialog");
                break;
        }
    }


    @Override
    public void onNegativeClick(View view) {
        KeyBordUtils.hideSoftKeyboard(view);
        if (isEnableNewSensor) {
            switch (curChannelNumber) {
                case "00":
                    switchButton1.setCheckedImmediatelyNoEvent(false);
                    setBgColorAndText(false, textView1);
                    channelNumber0 = "-1";
                    collectorSensorHashMap.remove(curChannelNumber);
                    break;

                case "01":
                    switchButton2.setCheckedImmediatelyNoEvent(false);
                    setBgColorAndText(false, textView2);
                    channelNumber1 = "-1";
                    collectorSensorHashMap.remove(curChannelNumber);
                    break;

                case "02":
                    switchButton3.setCheckedImmediatelyNoEvent(false);
                    setBgColorAndText(false, textView3);
                    channelNumber2 = "-1";
                    collectorSensorHashMap.remove(curChannelNumber);
                    break;

                case "03":
                    switchButton4.setCheckedImmediatelyNoEvent(false);
                    setBgColorAndText(false, textView4);
                    channelNumber3 = "-1";
                    collectorSensorHashMap.remove(curChannelNumber);
                    break;

                case "04":
                    switchButton5.setCheckedImmediatelyNoEvent(false);
                    setBgColorAndText(false, textView5);
                    channelNumber4 = "-1";
                    collectorSensorHashMap.remove(curChannelNumber);
                    break;

                case "05":
                    switchButton6.setCheckedImmediatelyNoEvent(false);
                    setBgColorAndText(false, textView6);
                    channelNumber5 = "-1";
                    collectorSensorHashMap.remove(curChannelNumber);
                    break;

                case "06":
                    switchButton7.setCheckedImmediatelyNoEvent(false);
                    setBgColorAndText(false, textView7);
                    channelNumber6 = "-1";
                    collectorSensorHashMap.remove(curChannelNumber);
                    break;

                case "07":
                    switchButton8.setCheckedImmediatelyNoEvent(false);
                    setBgColorAndText(false, textView8);
                    channelNumber7 = "-1";
                    collectorSensorHashMap.remove(curChannelNumber);
                    break;

                default:
            }
        }
    }

    protected abstract void sendInstruction();

    protected void spliceStringCollectorSensorParams(CollectorSensorParamsInfo collectorSensorParamsInfoSub) {
        sbCollectorSensorConfig.append("$$101" + collectorSensorParamsInfoSub.getCollectorModel() + StringUtil.formatStringTwo(collectorSensorParamsInfoSub.getChannelNumber()));

        SensorType sensorType = collectorSensorParamsInfoSub.getSensorType();
        switch (sensorType) {
            case WIRE_SHIFT: {//拉线位移计 $$1010200,3,2,7,1.100000&&$$1010201,5,2,5,0.000000&&$$1010202,2,2,7,1.100000&&
                sbCollectorSensorConfig.append("," + collectorSensorParamsInfoSub.getSensorAddress());
                sbCollectorSensorConfig.append("," + collectorSensorParamsInfoSub.getSensorType());
                SensorWireShiftInfo sensorInfo = (SensorWireShiftInfo) collectorSensorParamsInfoSub.getSensorData();
                sbCollectorSensorConfig.append("," + sensorInfo.getTriggerThreshold());
                sbCollectorSensorConfig.append("," + sensorInfo.getCorrectionValue());
                sbCollectorSensorConfig.append("&&");
            }
            break;

            case SOIL_MOISTURE: {//土壤含水率
                sbCollectorSensorConfig.append("," + collectorSensorParamsInfoSub.getSensorAddress());
                sbCollectorSensorConfig.append("," + collectorSensorParamsInfoSub.getSensorType());
                SensorSoilMoistureInfo sensorInfo = (SensorSoilMoistureInfo) collectorSensorParamsInfoSub.getSensorData();
                sbCollectorSensorConfig.append("," + sensorInfo.getTriggerThreshold());
                sbCollectorSensorConfig.append("," + sensorInfo.getCorrectionValue());
                sbCollectorSensorConfig.append("&&");
            }
            break;

            case INCLINOMETER: {//测斜仪
                sbCollectorSensorConfig.append("," + collectorSensorParamsInfoSub.getSensorAddress());
                sbCollectorSensorConfig.append("," + collectorSensorParamsInfoSub.getSensorType());
                SensorInclinometerInfo sensorInfo = (SensorInclinometerInfo) collectorSensorParamsInfoSub.getSensorData();
                sbCollectorSensorConfig.append("," + sensorInfo.getTriggerThreshold());
                sbCollectorSensorConfig.append("," + sensorInfo.getCorrectionValue());
                sbCollectorSensorConfig.append("&&");
            }
            break;

            case RADAR_LEVEL_GAUGE: {//雷达物位计
                sbCollectorSensorConfig.append("," + collectorSensorParamsInfoSub.getSensorAddress());
                sbCollectorSensorConfig.append("," + collectorSensorParamsInfoSub.getSensorType());
                SensorRadarLevelInfo sensorInfo = (SensorRadarLevelInfo) collectorSensorParamsInfoSub.getSensorData();
                sbCollectorSensorConfig.append("," + sensorInfo.getTriggerThreshold());
                sbCollectorSensorConfig.append("," + sensorInfo.getCorrectionValue());
                sbCollectorSensorConfig.append("&&");
            }
            break;

            case INFRASOUND_SENSOR: {//次声
                sbCollectorSensorConfig.append("," + collectorSensorParamsInfoSub.getSensorAddress());
                sbCollectorSensorConfig.append("," + collectorSensorParamsInfoSub.getSensorType());
                SensorInfrasoundInfo sensorInfo = (SensorInfrasoundInfo) collectorSensorParamsInfoSub.getSensorData();
                sbCollectorSensorConfig.append("," + sensorInfo.getTriggerThreshold());
                sbCollectorSensorConfig.append("," + sensorInfo.getCorrectionValue());
                sbCollectorSensorConfig.append("&&");
            }
            break;

            case KANG_PERCOLATE: {//基康渗压计(BGK-4500)
                sbCollectorSensorConfig.append("," + collectorSensorParamsInfoSub.getChannelNumber());
                sbCollectorSensorConfig.append("," + collectorSensorParamsInfoSub.getSensorType());
                SensorKangPercolateInfo sensorInfo = (SensorKangPercolateInfo) collectorSensorParamsInfoSub.getSensorData();
                sbCollectorSensorConfig.append("," + sensorInfo.getTriggerThreshold());
                sbCollectorSensorConfig.append("," + sensorInfo.getPolynomialRatioA());
                sbCollectorSensorConfig.append("," + sensorInfo.getPolynomialRatioB());
                sbCollectorSensorConfig.append("," + sensorInfo.getPolynomialRatioC());
                sbCollectorSensorConfig.append("," + sensorInfo.getTemperatureCoefficientK());
                sbCollectorSensorConfig.append("," + sensorInfo.getCreateTemperature());
                sbCollectorSensorConfig.append("," + sensorInfo.getManualCorrection());
                sbCollectorSensorConfig.append("," + sensorInfo.getCordLenght());
                sbCollectorSensorConfig.append("," + sensorInfo.getInstallElevation());
                sbCollectorSensorConfig.append("&&");
            }
            break;

            case GUDAN_PERCOLATE: {//葛南渗压计(VWP-03)
                sbCollectorSensorConfig.append("," + collectorSensorParamsInfoSub.getChannelNumber());
                sbCollectorSensorConfig.append("," + collectorSensorParamsInfoSub.getSensorType());
                SensorGudanPercolateInfo sensorInfo = (SensorGudanPercolateInfo) collectorSensorParamsInfoSub.getSensorData();
                sbCollectorSensorConfig.append("," + sensorInfo.getTriggerThreshold());
                sbCollectorSensorConfig.append("," + sensorInfo.getSensitivityK());
                sbCollectorSensorConfig.append("," + sensorInfo.getTemperatureCoefficientB());
                sbCollectorSensorConfig.append("," + sensorInfo.getReferenceValue());
                sbCollectorSensorConfig.append("," + sensorInfo.getCreateTemperature());
                sbCollectorSensorConfig.append("," + sensorInfo.getManualCorrection());
                sbCollectorSensorConfig.append("," + sensorInfo.getCordLenght());
                sbCollectorSensorConfig.append("," + sensorInfo.getInstallElevation());
                sbCollectorSensorConfig.append("&&");
            }
            break;

            case JUNXING_ZLJ_300T: {//轴力计(ZLJ-300T)
                sbCollectorSensorConfig.append("," + collectorSensorParamsInfoSub.getChannelNumber());
                sbCollectorSensorConfig.append("," + collectorSensorParamsInfoSub.getSensorType());
                SensorJunXingZljInfo sensorInfo = (SensorJunXingZljInfo) collectorSensorParamsInfoSub.getSensorData();
                sbCollectorSensorConfig.append("," + sensorInfo.getTriggerThreshold());
                sbCollectorSensorConfig.append("," + sensorInfo.getPolynomialRatioA());
                sbCollectorSensorConfig.append("," + sensorInfo.getReferenceValue());
                sbCollectorSensorConfig.append("," + sensorInfo.getManualCorrection());
                sbCollectorSensorConfig.append("," + sensorInfo.getTemperatureCoefficientB());
                sbCollectorSensorConfig.append("," + sensorInfo.getCreateTemperature());
                sbCollectorSensorConfig.append("&&");
            }
            break;
        }
    }

    public CollectorSensorParamsInfo getCurrentCollectorSensorParamsInfo() {
        CollectorSensorParamsInfo collectorSensorParamsInfoSub = collectorSensorHashMap.get(curChannelNumber);
        return collectorSensorParamsInfoSub;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
