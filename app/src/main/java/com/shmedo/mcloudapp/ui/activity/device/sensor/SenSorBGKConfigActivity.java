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
import com.shmedo.core.cmd.CommandResult;
import com.shmedo.core.enums.CollectorModel;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.model.SensorInclinometerInfo;
import com.shmedo.core.model.SensorInfrasoundInfo;
import com.shmedo.core.model.SensorRadarLevelInfo;
import com.shmedo.core.model.SensorSoilMoistureInfo;
import com.shmedo.core.model.SensorWireShiftInfo;
import com.shmedo.core.utils.StringUtil;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;
import com.shmedo.mcloudapp.interfaces.Extras;
import com.shmedo.mcloudapp.ui.activity.device.BaseDeviceConnectActivity;
import com.shmedo.mcloudapp.ui.activity.device.sensor.dialog.BaseDialogFragment;
import com.shmedo.mcloudapp.ui.activity.device.sensor.dialog.CommonSensorConfigDialogFragment;
import com.shmedo.mcloudapp.ui.activity.device.sensor.dialog.TestFragment;
import com.shmedo.mcloudapp.util.KeyBordUtils;
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
 * 创建者:   dpc
 * 创建时间:  2019/4/24 14:44
 * 描述：   传感器配置页面
 */
public class SenSorBGKConfigActivity extends BaseDeviceConnectActivity implements BaseDialogFragment.DialogFragmentClickListener {

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

    private List<CollectorSensorParamsInfoSub> collectorSensorParamsInfoSubs = new ArrayList<>();
    //以传感器的通道号为 Key,CollectorSensorParamsInfoSub 对象为 Value
    private HashMap<String, CollectorSensorParamsInfoSub> collectorSensorHashMap = new HashMap<>();
    private CollectorSensorParamsInfoSub defaultCollectorSensorParamsInfoSub = new CollectorSensorParamsInfoSub();

    private String collectorName = "";

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

    private int sensorIndex = 0;//接入的传感器索引号


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
        String collectorSensorConfig = getIntent().getStringExtra(Extras.SENSOR_PARAMS);
        if (TextUtils.isEmpty(collectorSensorConfig)) {
            Timber.e("传递的传感器参数为空!");
            return;
        }

        //测试参数，测试结束删除此行
        collectorSensorConfig = "$$1010000,00,50,2.000000e+00,4.597945e-08,3.000000e+00,4.000000e+00,5.000000e+00,3.800000e+01,3.300000e+01,2.300000e+01,6.600000e+01&&"+
        "$$1010001,01,51,2.000000e+00,4.597945e-08,3.000000e+00,4.400000e+01,3.800000e+01,1.300000e+01,5.000000e+00,1.600000e+01&&"+
        "$$1010002,02,58,2.000000e+00,4.597945e-08,3.000000e+00,5.000000e+00&&";

        String[] sensorConfigs = collectorSensorConfig.split("&&");
        for (String sensorConfig : sensorConfigs) {
            if (TextUtils.isEmpty(sensorConfig))
                continue;

            CollectorSensorParamsInfoSub mCollectorParamsInfoSub = BlueResultParserUtil.setCollectorParams(sensorConfig);
            Timber.d("--------XX采集器YY通道的传感器参数-------%s", mCollectorParamsInfoSub.toString());
            collectorSensorParamsInfoSubs.add(mCollectorParamsInfoSub);
            collectorSensorHashMap.put(mCollectorParamsInfoSub.getChannelNumber(), mCollectorParamsInfoSub);
            initSensorState(mCollectorParamsInfoSub);
        }

        if (!collectorSensorParamsInfoSubs.isEmpty()) {
            defaultCollectorSensorParamsInfoSub = collectorSensorParamsInfoSubs.get(0);
        }
    }

    /**
     * 采集器名称作为标题
     */
    private void initTitle() {
        if (defaultCollectorSensorParamsInfoSub == null)
            return;

        CollectorModel collectorModel = CollectorModel.value(defaultCollectorSensorParamsInfoSub.getCollectorModel());
        collectorName = BlueResultParserUtil.getCollectorName(collectorModel);
        mToolbarTitle.setText(collectorName);
    }

    /**
     * 初始化传感器启用状态
     *
     * @param mCollectorParamsInfoSub
     */
    private void initSensorState(CollectorSensorParamsInfoSub mCollectorParamsInfoSub) {
        String sensorType = mCollectorParamsInfoSub.getSensorType();
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
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
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
                    CollectorSensorParamsInfoSub collectorSensorParamsInfoSub = new CollectorSensorParamsInfoSub();
                    collectorSensorParamsInfoSub.setCollectorModel(defaultCollectorSensorParamsInfoSub.getCollectorModel());
                    collectorSensorParamsInfoSub.setChannelNumber(channelNumber);
                    collectorSensorParamsInfoSub.setSensorType(defaultCollectorSensorParamsInfoSub.getSensorType());
                    collectorSensorParamsInfoSub.setSensorAddress(defaultCollectorSensorParamsInfoSub.getSensorAddress());
                    collectorSensorParamsInfoSub.setSensorData(defaultCollectorSensorParamsInfoSub.getSensorData());
                    collectorSensorHashMap.put(channelNumber, collectorSensorParamsInfoSub);
                }
                showBottomDialog(channelNumber0, true);
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
                showBottomDialog(channelNumber1, true);
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
                showBottomDialog(channelNumber2, true);
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
                showBottomDialog(channelNumber3, true);
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
                showBottomDialog(channelNumber4, true);
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
                showBottomDialog(channelNumber5, true);
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
                showBottomDialog(channelNumber6, true);
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
                showBottomDialog(channelNumber7, true);
                break;
        }
    }

    private void showBottomDialog(String channelNumber, boolean isAddOrModifySensor) {
        BaseDialogFragment newFragment;

        curChannelNumber = channelNumber;
        isEnableNewSensor = isAddOrModifySensor;
        CollectorSensorParamsInfoSub collectorSensorParamsInfoSub = collectorSensorHashMap.get(channelNumber);
        String sensorType = collectorSensorParamsInfoSub.getSensorType();
        switch (sensorType) {
            case "02"://拉线位移计
            case "03"://土壤含水率
            case "04"://测斜仪
            case "07"://雷达物位计
            case "21"://次声
                newFragment = CommonSensorConfigDialogFragment.newInstance(collectorSensorParamsInfoSub);
                newFragment.show(getSupportFragmentManager(), "dialog");
                break;

            case "50"://基康渗压计(BGK-4500)
            case "51"://葛南渗压计(VWP-03)
            case "58"://军星轴力计(ZLJ-300T)
                newFragment = TestFragment.newInstance(collectorSensorParamsInfoSub);
                newFragment.show(getSupportFragmentManager(), "dialog");
                break;
        }
    }

    @Override
    public boolean onPositiveClick(View view) {
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
            Timber.e("%s 采集器接入的传感器信息为空!", collectorName);
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
        String command = String.valueOf(builderFirst);

        startProgressRunnable("正在发送配置指令...", CONFIG_DELAY_MILLIS);
        sendCommonCommandImmediately(command);
        Timber.d("设置 %s 接入的传感器指令===%s", collectorName, command);
    }


    /**
     * 设置采集器接入传感器触发阈值
     */
    private void setTriggerThreshold() {
        if (sensorIndex >= collectorSensorParamsInfoSubs.size()) {
            return;
        }

        String command = "";
        CollectorSensorParamsInfoSub paramsInfoSub = collectorSensorParamsInfoSubs.get(sensorIndex);
        CollectorModel collectorModel = CollectorModel.value(paramsInfoSub.getCollectorModel());
        switch (collectorModel) {
            case DS08://裂缝计采集器
                SensorWireShiftInfo sensorWireShiftInfo = (SensorWireShiftInfo) paramsInfoSub.getSensorData();
                command = "##168" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorWireShiftInfo.getTriggerThreshold() + "\r\n";
                break;

            case CS08://次声采集器
                SensorInfrasoundInfo sensorInfrasoundInfo = (SensorInfrasoundInfo) paramsInfoSub.getSensorData();
                command = "##168" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorInfrasoundInfo.getTriggerThreshold() + "\r\n";
                break;

            case HD08://土壤湿度采集器
                SensorSoilMoistureInfo sensorSoilMoistureInfo = (SensorSoilMoistureInfo) paramsInfoSub.getSensorData();
                command = "##168" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorSoilMoistureInfo.getTriggerThreshold() + "\r\n";
                break;

            case RD08://雷达采集器
                SensorRadarLevelInfo sensorRadarLevelInfo = (SensorRadarLevelInfo) paramsInfoSub.getSensorData();
                command = "##168" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorRadarLevelInfo.getTriggerThreshold() + "\r\n";
                break;

            case CX08://测斜仪采集器
                SensorInclinometerInfo sensorInclinometerInfo = (SensorInclinometerInfo) paramsInfoSub.getSensorData();
                command = "##168" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorInclinometerInfo.getTriggerThreshold() + "\r\n";
                break;
        }

        sendCommonCommand(command);
        Timber.d("设置 %s 采集器 %s 地址的传感器触发阈值参数===%s", collectorModel, StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()), command);
    }

    /**
     * 设置采集器接入传感器修正值（只有墒情计用到3个修正值，其他传感器只用到一个修正值）
     */
    private void setCorrectionValue() {
        if (sensorIndex >= collectorSensorParamsInfoSubs.size()) {
            return;
        }

        String command = "";
        CollectorSensorParamsInfoSub paramsInfoSub = collectorSensorParamsInfoSubs.get(sensorIndex);
        CollectorModel collectorModel = CollectorModel.value(paramsInfoSub.getCollectorModel());
        switch (collectorModel) {
            case DS08://裂缝计采集器
                SensorWireShiftInfo sensorWireShiftInfo = (SensorWireShiftInfo) paramsInfoSub.getSensorData();
                command = "##165" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorWireShiftInfo.getCorrectionValue() + "\r\n";
                break;

            case CS08://次声采集器
                SensorInfrasoundInfo sensorInfrasoundInfo = (SensorInfrasoundInfo) paramsInfoSub.getSensorData();
                command = "##165" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorInfrasoundInfo.getRevised() + "\r\n";
                break;

            case HD08://土壤湿度采集器
                SensorSoilMoistureInfo sensorSoilMoistureInfo = (SensorSoilMoistureInfo) paramsInfoSub.getSensorData();
                command = "##165" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorSoilMoistureInfo.getRevised() + "\r\n";
                break;

            case RD08://雷达采集器
                SensorRadarLevelInfo sensorRadarLevelInfo = (SensorRadarLevelInfo) paramsInfoSub.getSensorData();
                command = "##165" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorRadarLevelInfo.getRevised() + "\r\n";
                break;

            case CX08://测斜仪采集器
                SensorInclinometerInfo sensorInclinometerInfo = (SensorInclinometerInfo) paramsInfoSub.getSensorData();
                command = "##165" +
                        StringUtil.formatStringTwo(paramsInfoSub.getCollectorModel()) +
                        StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()) +
                        sensorInclinometerInfo.getCorrectionValue() + "\r\n";
                break;
        }
        sendCommonCommandImmediately(command);
        Timber.d("设置 %s 采集器 %s 地址的传感器修正值参数===%s", collectorModel, StringUtil.formatStringTwo(paramsInfoSub.getSensorAddress()), command);
    }


    /**
     * 设置显示数据
     */
    private void setResultData(String cmdStr) {
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case SET_COLLECTOR_SENSOR://设置采集器接入的传感器 150
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show(collectorName + "接入传感器配置错误!");
                    stopProgressRunnable();
                    return;
                }
                setTriggerThreshold();
                break;

            case COLLECTOR_SENSOR_THRESHOLD_SOLI://传感器触发阈值 168
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show(collectorName + "的传感器触发阈值配置错误!");
                    stopProgressRunnable();
                    return;
                }
                setCorrectionValue();
                break;

            case COLLECTOR_SENSOR_REVISED: //传感器修正值 165
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show(collectorName + "的传感器修正值配置错误!");
                    stopProgressRunnable();
                    return;
                }
                sensorIndex++;
                setTriggerThreshold();

                if (sensorIndex >= collectorSensorParamsInfoSubs.size()) {
                    stopProgressRunnable();
                    ToastUtils.show("设置完成");
                    hander.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 2000);
                }
                break;
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
