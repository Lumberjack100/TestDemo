package com.shmedo.mcloudapp.deviceconfig.ui.fragment;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
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
import com.shmedo.core.event.BluetoothConnectStateEvent;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.common.view.editspinner.EditSpinner;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.ADMEMotorControlActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.ADMESensorExecutiveAgencyConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.ConfigADMEActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.CountMeterWheelActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.advanced.InstructionDebugActivity;
import com.shmedo.mcloudapp.entity.SystemDataInfo;
import com.shmedo.mcloudapp.entity.SystemDataInfoDao;
import com.shmedo.mcloudapp.util.DaoManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * A simple
 */
public class ADMEHomeFragment extends BaseFragment {
    private static final int AUTO_MONITOR = 0x0001;

    private static final int DEBUG_MODEL = 0x0002;

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;

    @BindView(R.id.tv_device_model)
    TextView mTvDeviceModel;

    @BindView(R.id.tv_sensor_type)
    TextView mTvSensorType;

    @BindView(R.id.iv_lock)
    ImageView mIvLock;

    @BindView(R.id.tv_lock)
    TextView mTvLock;

    @BindView(R.id.editSpinner1)
    EditSpinner spinnerProjectName;

    @BindView(R.id.tv_projectName)
    TextView mTvProName;

    @BindView(R.id.bluetooth_switch_layout)
    View bluetoothSwitchLayout;

    @BindView(R.id.auto_monitor_layout)
    View autoMonitorLayout;

    @BindView(R.id.debug_model_layout)
    View debugModelLayout;

    @BindView(R.id.debug_item_layout)
    View debugItemLayout;

    @BindView(R.id.platform_server_config_layout)
    View platformServerConfigLayout;

    @BindView(R.id.server_address_layout)
    View serverAddressLayout;

    @BindView(R.id.dag_config_layout)
    View dagConfigLayout;

    @BindView(R.id.custom_command_test_layout)
    View customCommandTestLayout;

    private TextView tvBluetoothState, tvAutoMonitorState, tvDebugMode;

    private SwitchButton sbBluetoothState, sbAutoMonitorState, sbDebugMode;

    private ClearEditText mEtAddress1, mEtAddress2;

    private ImageView mIvExpandAddress, mIvRefreshAddr1, mIvRefreshAddr2;

    private Button mBtnEdit1, mBtnEdit2;

    private Animation mExpandAnimation;

    private Animation mFoldResetAnimation;

    private RotateAnimation mRefreshAnimation;

    private ConfigADMEActivity configADMEActivity;

    private DaoManager manager = DaoManager.getInstance();

    private Handler hander;

    private List<String> systemDataInfoList = new ArrayList<>();//项目信息列表

    private HashMap<String, SystemDataInfo> systemDataInfoHashMap = new HashMap<>();

    private String dagConfigInfo;//DAG 采集器配置指令

    private String executiveAgencyConfigInfo;//执行机构配置指令

    private String motorControl;//控制电机指令

    private String countMeterWheel;//设置计米轮指令

    private String correctionParam;//编码器修正参数


    private boolean isRefreshingAddress = false;


    private Runnable clearAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            ToastUtils.show("刷新地址超时，请稍候再试");
            mIvRefreshAddr1.clearAnimation();
            mIvRefreshAddr2.clearAnimation();
        }
    };

    private Runnable dismssDialogRunnable = new Runnable() {
        @Override
        public void run() {
            dismissProgressDialog();
            ToastUtils.show("发送指令超时,请稍后尝试");
        }
    };


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_admehome;
    }

    @Override
    public void onResume() {
        super.onResume();
        setViewStateByConnectState(MCloudApp.isIsBluetoothDeviceConnected());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getIntentData();
        queryProjectList();
        initAnimation();
        setSwitchViewListener();
        initAdapter();
    }

    @Override
    protected void initView() {
        ((TextView) bluetoothSwitchLayout.findViewById(R.id.tv_config_name)).setText("蓝牙连接");
        tvBluetoothState = bluetoothSwitchLayout.findViewById(R.id.tv_device_state);
        sbBluetoothState = bluetoothSwitchLayout.findViewById(R.id.switchButton);

        ((TextView) autoMonitorLayout.findViewById(R.id.tv_config_name)).setText("自动监测启用");
        tvAutoMonitorState = autoMonitorLayout.findViewById(R.id.tv_device_state);
        sbAutoMonitorState = autoMonitorLayout.findViewById(R.id.switchButton);

        ((TextView) debugModelLayout.findViewById(R.id.tv_config_name)).setText("测试模式");
        tvDebugMode = debugModelLayout.findViewById(R.id.tv_device_state);
        sbDebugMode = debugModelLayout.findViewById(R.id.switchButton);

        ((TextView) platformServerConfigLayout.findViewById(R.id.tv_config_name)).setText("云平台服务器设置");
        mIvExpandAddress = platformServerConfigLayout.findViewById(R.id.iv_arrow);
        mIvExpandAddress.setImageResource(R.drawable.ic_expand_more_black_24dp);

        serverAddressLayout.setVisibility(View.GONE);

        mEtAddress1 = serverAddressLayout.findViewById(R.id.addressET1);
        mIvRefreshAddr1 = serverAddressLayout.findViewById(R.id.refreshIV1);
        mBtnEdit1 = serverAddressLayout.findViewById(R.id.editBtn1);
        mEtAddress1.setEnabled(false);

        mEtAddress2 = serverAddressLayout.findViewById(R.id.addressET2);
        mIvRefreshAddr2 = serverAddressLayout.findViewById(R.id.refreshIV2);
        mBtnEdit2 = serverAddressLayout.findViewById(R.id.editBtn2);
        mEtAddress2.setEnabled(false);

        ((TextView) dagConfigLayout.findViewById(R.id.tv_config_name)).setText("采集器和执行机构参数设置");
        ((TextView) customCommandTestLayout.findViewById(R.id.tv_config_name)).setText("自定义指令输入");

        //TODO 需要查询接口确定设备所属项目
        mTvProName.setText("xxxx 项目");
        if (mTvLock.getText().equals("已锁定")) {
            mIvLock.setImageResource(R.drawable.icon_close_lock);
            mTvProName.setVisibility(View.VISIBLE);
            spinnerProjectName.setVisibility(View.GONE);

        } else if (mTvLock.getText().equals("已解锁")) {
            mIvLock.setImageResource(R.drawable.icon_open_lock);
            mTvProName.setVisibility(View.GONE);
            spinnerProjectName.setVisibility(View.VISIBLE);
        }
    }

    private void getIntentData() {
        Intent intent = getActivity().getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey(AppContants.Extras.CUR_DEVICE_NAME)) {
            String deviceInfo = intent.getStringExtra(AppContants.Extras.CUR_DEVICE_NAME);
            String[] scanData = deviceInfo.split(",");
            mTvDeviceName.setText("自动化深层水平位移监测装置");
            mTvDeviceSn.setText(scanData[1]);//设备编号
            mTvDeviceModel.setText(scanData[2]);//功能型号
            mTvSensorType.setText("S0260");
        }
        configADMEActivity = (ConfigADMEActivity) getActivity();
        hander = new Handler();
    }

    private void initAnimation() {
        mExpandAnimation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mExpandAnimation.setDuration(300);
        mExpandAnimation.setFillAfter(true);

        mFoldResetAnimation = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mFoldResetAnimation.setDuration(300);
        mFoldResetAnimation.setFillAfter(true);

        mRefreshAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        LinearInterpolator interpolator = new LinearInterpolator();
        mRefreshAnimation.setInterpolator(interpolator);
        mRefreshAnimation.setDuration(1200);//设置动画持续周期
        mRefreshAnimation.setRepeatCount(Animation.INFINITE);//设置重复次数,一直重复下去
        mRefreshAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIvRefreshAddr1.setClickable(false);
                mIvRefreshAddr2.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIvRefreshAddr1.setClickable(true);
                mIvRefreshAddr2.setClickable(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }


    /**
     * switch按钮事件
     */
    private void setSwitchViewListener() {
        //连接蓝牙开关
        sbBluetoothState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                sbBluetoothState.setCheckedImmediatelyNoEvent(!isChecked);

                //未连接时，直接打开连接
                if (isChecked) {
                    configADMEActivity.findAndConnectSpecificDevice();

                } else {//断开连接处理
                    if (configADMEActivity.isConfigChange) {
                        configADMEActivity.isExitMode = false;
                        configADMEActivity.showSaveDialog(getResources().getString(R.string.disconnect_bluetooth_device_save_param_warn));

                    } else {
                        configADMEActivity.disconnectDevice();
                        setViewStateByConnectState(false);
                    }
                }
            }
        });
        //自动监测启用开关
        sbAutoMonitorState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    sbAutoMonitorState.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }

                if (isChecked) {
                    if (checkAutoMonitorAndDebugMode(AUTO_MONITOR)) {
                        //打开自动监测模式命令
                        configADMEActivity.sendCommonCommand("##70111\r\n");
                        setSwitchViewState(true, tvAutoMonitorState, "已启用");
                    }
                } else {
                    //关闭自动监测模式命令
                    configADMEActivity.sendCommonCommand("##70112\r\n");
                    setSwitchViewState(false, tvAutoMonitorState, "已关闭");
                }
            }
        });

        //测试模式
        sbDebugMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    sbDebugMode.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }

                if (isChecked) {
                    if (checkAutoMonitorAndDebugMode(DEBUG_MODEL)) {
                        //打开测试模式命令
                        configADMEActivity.sendCommonCommand("##70123\r\n");
                        setSwitchViewState(true, tvDebugMode, "已打开");
                        debugItemLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    //关闭测试模式命令
                    configADMEActivity.sendCommonCommand("##70122\r\n");
                    setSwitchViewState(false, tvDebugMode, "已关闭");
                    debugItemLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initAdapter() {
        spinnerProjectName.setItemData(systemDataInfoList);
        spinnerProjectName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != parent.getAdapter()) {
                    String projectName = spinnerProjectName.getText();
                    mTvProName.setText(projectName);
                }
            }
        });
    }


    @OnClick({R.id.iv_lock, R.id.tv_motor_control, R.id.tv_count_meter_wheel, R.id.platform_server_config_layout, R.id.refreshIV1, R.id.editBtn1, R.id.refreshIV2, R.id.editBtn2, R.id.dag_config_layout, R.id.custom_command_test_layout, R.id.firmware_upgrade_layout})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_lock:
                if (mTvLock.getText().equals("已锁定")) {
                    mTvLock.setText("已解锁");
                    mIvLock.setImageResource(R.drawable.icon_open_lock);
                    mTvProName.setVisibility(View.GONE);
                    spinnerProjectName.setVisibility(View.VISIBLE);

                } else if (mTvLock.getText().equals("已解锁")) {
                    mTvLock.setText("已锁定");
                    mIvLock.setImageResource(R.drawable.icon_close_lock);
                    mTvProName.setVisibility(View.VISIBLE);
                    spinnerProjectName.setVisibility(View.GONE);
                }
                break;

            case R.id.tv_motor_control:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                ADMEMotorControlActivity.startActivity(getActivity(), motorControl);
                break;

            case R.id.tv_count_meter_wheel:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                CountMeterWheelActivity.startActivity(getActivity(), countMeterWheel, correctionParam);
                break;

            case R.id.platform_server_config_layout://展开或折叠服务器地址配置
                doExpandOrFoldServerConfig();
                break;

            case R.id.refreshIV1:
                doRefreshAddress(1);
                break;

            case R.id.editBtn1:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }

                if (mBtnEdit1.getText().toString().contains("编辑")) {
                    mBtnEdit1.setText("确定");
                    mEtAddress1.setEnabled(true);
                    mEtAddress1.requestFocus();
                    mEtAddress1.setSelection(mEtAddress1.getText().length());
                } else {
                    mBtnEdit1.setText("编辑");
                    mEtAddress1.setEnabled(false);
                    mEtAddress1.clearFocus();
                    doServerAddress1Config();
                }
                break;

            case R.id.refreshIV2:
                doRefreshAddress(2);
                break;

            case R.id.editBtn2:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                if (mBtnEdit2.getText().toString().contains("编辑")) {
                    mBtnEdit2.setText("确定");
                    mEtAddress2.setEnabled(true);
                    mEtAddress2.requestFocus();
                    mEtAddress2.setSelection(mEtAddress2.getText().length());
                } else {
                    mBtnEdit2.setText("编辑");
                    mEtAddress2.setEnabled(false);
                    mEtAddress2.clearFocus();
                    doServerAddress2Config();
                }
                break;

            case R.id.dag_config_layout:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                ADMESensorExecutiveAgencyConfigActivity.startActivity(getActivity(), dagConfigInfo, executiveAgencyConfigInfo);
                break;

            case R.id.custom_command_test_layout:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                InstructionDebugActivity.startActivity(getActivity());
                break;

            case R.id.firmware_upgrade_layout:
                //固件升级
                ToastUtils.show("功能开发中...");
                break;
        }
    }

    private void doExpandOrFoldServerConfig() {
        if (serverAddressLayout.getVisibility() == View.GONE) {
            mIvExpandAddress.startAnimation(mExpandAnimation);
            serverAddressLayout.setVisibility(View.VISIBLE);
        } else {
            mIvExpandAddress.startAnimation(mFoldResetAnimation);
            serverAddressLayout.setVisibility(View.GONE);
        }
    }


    private void doRefreshAddress(int index) {
        if (!MCloudApp.isIsBluetoothDeviceConnected()) {
            ToastUtils.show("设备已断开连接，无法刷新");
            return;
        }

        String cmdStr = "";
        if (index == 1) {
            cmdStr = "##2001\r\n";
            mIvRefreshAddr1.startAnimation(mRefreshAnimation);
        } else {
            cmdStr = "##2002\r\n";
            mIvRefreshAddr2.startAnimation(mRefreshAnimation);
        }

        isRefreshingAddress = true;
        hander.postDelayed(clearAnimationRunnable, 6000);
        //查询服务器地址
        configADMEActivity.sendCommonCommand(cmdStr);
    }


    private void doServerAddress1Config() {
        String address = mEtAddress1.getText().toString().trim();
        if (!ValidateUtil.checkServerAddress(address)) {
            ToastUtils.show("地址格式不正确");
            return;
        }

        String cmdStr;
        if (TextUtils.isEmpty(address)) {
            cmdStr = "##2011\r\n";
        } else {
            String addrArray[] = address.split(":");
            cmdStr = "##2011 " + addrArray[0] + " " + addrArray[1] + "\r\n";
        }
        configADMEActivity.sendCommonCommand(cmdStr);
        showProgressDialog("正在发送配置指令...", null, null);
        hander.postDelayed(dismssDialogRunnable, 5000);
    }

    private void doServerAddress2Config() {
        String address = mEtAddress2.getText().toString().trim();
        if (!ValidateUtil.checkServerAddress(address)) {
            ToastUtils.show("地址格式不正确");
            return;
        }

        String cmdStr;
        if (TextUtils.isEmpty(address)) {
            cmdStr = "##2012\r\n";
        } else {
            String addrArray[] = address.split(":");
            cmdStr = "##2012 " + addrArray[0] + " " + addrArray[1] + "\r\n";
        }
        configADMEActivity.sendCommonCommand(cmdStr);
        showProgressDialog("正在发送配置指令...", null, null);
        hander.postDelayed(dismssDialogRunnable, 5000);
    }

    /**
     * 设置显示数据
     */
    private void setResultData(String cmdStr) {
        String[] cmdArray = cmdStr.replace("\r\n", "").split(",");

        //查询工作模式应答
        if (cmdStr.startsWith("$$7010") && cmdStr.endsWith("\r\n")) {
            if (cmdArray.length < 3 || TextUtils.isEmpty(cmdArray[1].trim()) || TextUtils.isEmpty(cmdArray[2].trim())) {
                Timber.d("查询工作模式应答指令错误");
                return;
            }
            sbAutoMonitorState.setCheckedImmediatelyNoEvent(cmdArray[1].trim().equals("1"));
            setSwitchViewState(cmdArray[1].trim().equals("1"), tvAutoMonitorState, cmdArray[1].trim().equals("1") ? "已启用" : "已关闭");

            //TODO 后期当设置为蓝牙模式时，与指令调试界面联动
            sbDebugMode.setCheckedImmediatelyNoEvent(cmdArray[2].trim().equals("3"));
            setSwitchViewState(cmdArray[2].trim().equals("3"), tvDebugMode, cmdArray[2].trim().equals("3") ? "已打开" : "已关闭");
            debugItemLayout.setVisibility(cmdArray[2].trim().equals("3") ? View.VISIBLE : View.GONE);

            //测试模式打开时，查询电机和计米轮相关参数
            if (cmdArray[2].trim().equals("3")) {
                configADMEActivity.sendCommonCommand("##7020\r\n");
                Timber.d("查询控制电机参数指令===" + "##7020");

                configADMEActivity.sendCommonCommand("##7022\r\n");
                Timber.d("查询计米轮参数指令===" + "##7022");
            }
            return;
        }

        //查询服务器地址1应答
        if (cmdStr.startsWith("$$2001") && cmdStr.endsWith("\r\n")) {
            cmdArray = cmdStr.replace("\r\n", "").split(" ");
            if (cmdArray.length < 3 || TextUtils.isEmpty(cmdArray[1].trim()) || TextUtils.isEmpty(cmdArray[2].trim())) {
                Timber.d("查询服务器地址1应答指令错误");
                return;
            }
            if (isRefreshingAddress) {
                isRefreshingAddress = false;
                hander.removeCallbacks(clearAnimationRunnable);
                mIvRefreshAddr1.clearAnimation();
                ToastUtils.show("地址已刷新");
            }
            mEtAddress1.setText(cmdArray[1] + ":" + cmdArray[2]);
            return;
        }

        //查询服务器地址2应答
        if (cmdStr.startsWith("$$2002") && cmdStr.endsWith("\r\n")) {
            cmdArray = cmdStr.replace("\r\n", "").split(" ");
            if (cmdArray.length < 3 || TextUtils.isEmpty(cmdArray[1].trim()) || TextUtils.isEmpty(cmdArray[2].trim())) {
                Timber.d("查询服务器地址2应答指令错误");
                return;
            }
            if (isRefreshingAddress) {
                isRefreshingAddress = false;
                hander.removeCallbacks(clearAnimationRunnable);
                mIvRefreshAddr2.clearAnimation();
                ToastUtils.show("地址已刷新");
            }
            mEtAddress2.setText(cmdArray[1] + ":" + cmdArray[2]);
            return;
        }

        //查询采集器参数应答
        if (cmdStr.startsWith("$$7000") && cmdStr.endsWith("\r\n")) {
            dagConfigInfo = cmdStr;
            return;
        }

        //查询执行机构参数应答
        if (cmdStr.startsWith("$$7002") && cmdStr.endsWith("\r\n")) {
            executiveAgencyConfigInfo = cmdStr;
            return;
        }

        //查询控制电机参数应答
        if (cmdStr.startsWith("$$7020") && cmdStr.endsWith("\r\n")) {
            motorControl = cmdStr;
            return;
        }

        //查询计米轮参数应答
        if (cmdStr.startsWith("$$7022") && cmdStr.endsWith("\r\n")) {
            countMeterWheel = cmdStr;
            return;
        }

        //查询编码器修正参数应答
        if (cmdStr.startsWith("$$7030") && cmdStr.endsWith("\r\n")) {
            correctionParam = cmdStr;
            return;
        }

//        //设置自动测量模式应答
//        if (cmdStr.startsWith("$$7011") && cmdStr.endsWith("\r\n")) {
//            ToastUtils.show("设置自动测量模式完成");
//            return;
//        }
//
        //设置测试模式应答
        if (cmdStr.startsWith("$$70123") && cmdStr.endsWith("\r\n")) {
            Timber.d("测试模式已打开");
            configADMEActivity.sendCommonCommand("##7020\r\n");
            Timber.d("查询控制电机参数指令===" + "##7020");

            configADMEActivity.sendCommonCommand("##7022\r\n");
            Timber.d("查询计米轮参数指令===" + "##7022");

            configADMEActivity.sendCommonCommand("##7030\r\n");
            Timber.d("查询编码器修正参数指令===" + "##7030");
            return;
        }

        //设置服务器地址1应答
        if (cmdStr.startsWith("$$2011") && cmdStr.endsWith("\r\n")) {
            ToastUtils.show("设置MD-NET服务器地址完成");
            dismissProgressDialog();
            hander.removeCallbacks(dismssDialogRunnable);
            return;
        }

        //设置服务器地址2应答
        if (cmdStr.startsWith("$$2012") && cmdStr.endsWith("\r\n")) {
            ToastUtils.show("设置mCloud服务器地址完成");
            dismissProgressDialog();
            hander.removeCallbacks(dismssDialogRunnable);
            return;
        }

        //设置采集器参数应答
        if (cmdStr.startsWith("$$7001") && cmdStr.endsWith("\r\n")) {
            dagConfigInfo = cmdStr;
            return;
        }

        //设置执行机构参数应答
        if (cmdStr.startsWith("$$7003") && cmdStr.endsWith("\r\n")) {
            executiveAgencyConfigInfo = cmdStr;
            return;
        }

        //设置测试控制电机指令应答
        if (cmdStr.startsWith("$$7021") && cmdStr.endsWith("\r\n")) {
            motorControl = cmdStr;
            return;
        }

        //设置计米轮参数应答
        if (cmdStr.startsWith("$$7023") && cmdStr.endsWith("\r\n")) {
            countMeterWheel = cmdStr;
            return;
        }

        //设置编码器修正参数应答
        if (cmdStr.startsWith("$$7031") && cmdStr.endsWith("\r\n")) {
            correctionParam = cmdStr;
            return;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (!TextUtils.isEmpty(messageEvent) && messageEvent.startsWith("$$")) {
            setResultData(messageEvent);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BluetoothConnectStateEvent bluetoothConnectStateEvent) {
        setViewStateByConnectState(bluetoothConnectStateEvent.isConnected);
    }


    private void setSwitchViewState(boolean isOpen, TextView textView, String content) {
        textView.setText(content);
        textView.setTextColor(isOpen ? getResources().getColor(R.color.colorPrimary) : getResources().getColor(R.color.gray_807B7B));
    }

    private void setViewStateByConnectState(boolean isConnected) {
        if (isConnected) {
            sbBluetoothState.setCheckedImmediatelyNoEvent(true);
            setSwitchViewState(true, tvBluetoothState, "已连接");

            mBtnEdit1.setEnabled(true);
            mBtnEdit2.setEnabled(true);
            mIvRefreshAddr1.setEnabled(true);
            mIvRefreshAddr2.setEnabled(true);

        } else {
            sbBluetoothState.setCheckedImmediatelyNoEvent(false);
            setSwitchViewState(false, tvBluetoothState, "已断开");

            sbAutoMonitorState.setCheckedImmediatelyNoEvent(false);
            setSwitchViewState(false, tvAutoMonitorState, "已关闭");

            sbDebugMode.setCheckedImmediatelyNoEvent(false);
            setSwitchViewState(false, tvDebugMode, "已关闭");

            mBtnEdit1.setEnabled(false);
            mBtnEdit2.setEnabled(false);
            mIvRefreshAddr1.setEnabled(false);
            mIvRefreshAddr2.setEnabled(false);
        }
    }


    /**
     * 自动监测模式与测试模式不可以同时开启，此处进行检查
     *
     * @param tag
     * @return
     */
    private boolean checkAutoMonitorAndDebugMode(final int tag) {
        String msg = "";
        switch (tag) {
            case AUTO_MONITOR:
                if (!sbDebugMode.isChecked()) {
                    return true;
                }
                msg = "您已开启测试模式，是否关闭测试模式以打开自动监测模式？";
                break;

            case DEBUG_MODEL:
                if (!sbAutoMonitorState.isChecked()) {
                    return true;
                }
                msg = "您已开启自动监测模式，是否关闭自动监测以打开测试模式？";
                break;
        }
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getActivity())
                .title("温馨提示：")
                .content(msg)
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        switch (tag) {
                            case AUTO_MONITOR:
                                //关闭测试模式命令
                                configADMEActivity.sendCommonCommand("##70122\r\n");
                                sbDebugMode.setCheckedImmediatelyNoEvent(false);
                                setSwitchViewState(false, tvDebugMode, "已关闭");
                                debugItemLayout.setVisibility(View.GONE);

                                //打开自动监测模式命令
                                configADMEActivity.sendCommonCommand("##70111\r\n");
                                setSwitchViewState(true, tvAutoMonitorState, "已启用");
                                break;

                            case DEBUG_MODEL:
                                //关闭自动监测模式命令
                                configADMEActivity.sendCommonCommand("##70112\r\n");
                                sbAutoMonitorState.setCheckedImmediatelyNoEvent(false);
                                setSwitchViewState(false, tvAutoMonitorState, "已关闭");

                                //打开测试模式命令
                                configADMEActivity.sendCommonCommand("##70123\r\n");
                                setSwitchViewState(true, tvDebugMode, "已打开");
                                debugItemLayout.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        switch (tag) {
                            case AUTO_MONITOR:
                                sbAutoMonitorState.setCheckedImmediatelyNoEvent(false);
                                break;

                            case DEBUG_MODEL:
                                sbDebugMode.setCheckedImmediatelyNoEvent(false);
                                break;
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();

        return false;
    }


    /**
     * 查询本地数据库中当前用户的项目信息
     */
    private void queryProjectList() {
        List<SystemDataInfo> infoList = manager.getDaoSession().getSystemDataInfoDao().queryBuilder()
                .where(SystemDataInfoDao.Properties.Account.isNotNull(),
                        SystemDataInfoDao.Properties.Account.eq(MCloudApp.getAccount()))
                .list();

        systemDataInfoList.clear();
        systemDataInfoHashMap.clear();
        if (null != infoList && infoList.size() > 0) {
            for (SystemDataInfo systemDataInfo : infoList) {
                systemDataInfoList.add(systemDataInfo.getProjName());
                systemDataInfoHashMap.put(systemDataInfo.getProjName(), systemDataInfo);
            }
        }
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            //TODO  fragment  显示或隐藏时会触发此事件
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public boolean onBackPressed() {
        return false;
    }

}
