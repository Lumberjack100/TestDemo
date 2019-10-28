package com.shmedo.mcloudapp.ui.fragment;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseFragment;
import com.shmedo.mcloudapp.bluetooth.MdBluetoothManager;
import com.shmedo.mcloudapp.bluetooth.Message;
import com.shmedo.mcloudapp.entity.SystemDataInfo;
import com.shmedo.mcloudapp.entity.SystemDataInfoDao;
import com.shmedo.mcloudapp.model.Extras;
import com.shmedo.mcloudapp.ui.activity.ConfigADMEActivity;
import com.shmedo.mcloudapp.ui.activity.device.ADMEExecutiveAgencyConfigActivity;
import com.shmedo.mcloudapp.ui.activity.device.senior.InstructionDebugActivity;
import com.shmedo.mcloudapp.ui.activity.device.sensor.ADMESensorConfigActivity;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.views.ClearEditText;
import com.shmedo.mcloudapp.views.editspinner.EditSpinner;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ch.ielse.view.SwitchView;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class ADMEHomeFragment extends BaseFragment {

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

    @BindView(R.id.auto_monitor_layout)
    View autoMonitorLayout;

    @BindView(R.id.debug_model_layout)
    View debugModelLayout;

    @BindView(R.id.platform_server_config_layout)
    View platformServerConfigLayout;

    @BindView(R.id.server_address_layout)
    View serverAddressLayout;

    @BindView(R.id.dag_config_layout)
    View dagConfigLayout;

    @BindView(R.id.executive_agency_param_layout)
    View executiveAgencyParamLayout;

    @BindView(R.id.custom_command_test_layout)
    View customCommandTestLayout;

    private TextView tvAutoMonitorState, tvDebugMode;

    private SwitchView svAutoMonitorState, svDebugMode;

    private ClearEditText mEtAddress1, mEtAddress2;

    private ImageView mIvExpandAddress, mIvRefreshAddr1, mIvRefreshAddr2;

    private Button mBtnEdit1, mBtnEdit2;

    private Animation mExpandAnimation;

    private Animation mFoldResetAnimation;

    private RotateAnimation mRefreshAnimation;

    private Unbinder unbinder;

    private Context mContext;

    private ConfigADMEActivity configADMEActivity;

    private MdBluetoothManager mdBluetoothManager;

    private List<String> systemDataInfoList = new ArrayList<>();//项目信息列表

    private HashMap<String, SystemDataInfo> systemDataInfoHashMap = new HashMap<>();

    private String deviceInfo;

    private String dagConfigInfo;//DAG 采集器配置指令

    private String executiveAgencyConfigInfo;//执行机构配置指令

    private DaoManager manager = DaoManager.getInstance();


    @Override
    protected int initContentView() {
        return R.layout.fragment_admehome;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mContext = getActivity();

        getIntentData();
        queryProjectList();
        initView();
        initAnimation();
        setSwitchViewListener();

        return view;
    }

    private void getIntentData() {
        Intent intent = getActivity().getIntent();
        if (intent.getExtras().containsKey(Extras.CUR_DEVICE_NAME)) {
            deviceInfo = intent.getStringExtra(Extras.CUR_DEVICE_NAME);

            String[] scanData = deviceInfo.split(",");
            mTvDeviceName.setText("自动化深层水平位移监测装置");
            mTvDeviceSn.setText(scanData[1]);//设备编号
            mTvDeviceModel.setText(scanData[2]);//功能型号
            mTvSensorType.setText("S0260");
        }

        if (getActivity() instanceof ConfigADMEActivity) {
            configADMEActivity = (ConfigADMEActivity) getActivity();
        }

        mdBluetoothManager = MdBluetoothManager.getInstance();
    }

    private void initView() {

        ((TextView) autoMonitorLayout.findViewById(R.id.tv_config_name)).setText("自动监测启用");
        tvAutoMonitorState = autoMonitorLayout.findViewById(R.id.tv_device_state);
        svAutoMonitorState = autoMonitorLayout.findViewById(R.id.switchview);

        ((TextView) debugModelLayout.findViewById(R.id.tv_config_name)).setText("测试模式");
        tvDebugMode = debugModelLayout.findViewById(R.id.tv_device_state);
        svDebugMode = debugModelLayout.findViewById(R.id.switchview);

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

        ((TextView) dagConfigLayout.findViewById(R.id.tv_config_name)).setText("DAG设置");
        ((TextView) executiveAgencyParamLayout.findViewById(R.id.tv_config_name)).setText("执行机构参数配置");
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

        //发送查询设备状态命令
        if (MCloudApp.isIsBluetoothDeviceConnected()) {
            Objects.requireNonNull(configADMEActivity).sendDeviceStateComd();
        }
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
        //自动监测启用开关
        svAutoMonitorState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtil.showShortToast("设备已断开连接，暂无法进行设置");
                    return;
                }

                if (svAutoMonitorState.isOpened()) {
                    //发送打开自动测量模式命令
                    sendCommand("##70111\r\n");
                    setSwitchViewState(true, svAutoMonitorState, tvAutoMonitorState, "已启用");

                } else {
                    MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mContext);
                    mBuilder.title("温馨提示：")
                            .content("关闭自动监测，将导致设备自动关机进入休眠状态。请确认是否关闭")
                            .contentColor(Color.parseColor("#000000"))
                            .canceledOnTouchOutside(false)
                            .positiveText("确定")
                            .negativeText("取消")
                            .negativeColor(Color.parseColor("#807B7B"));
                    MaterialDialog mMaterialDialog = mBuilder.build();
                    mMaterialDialog.show();
                    mBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(
                                @NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            //发送关闭自动测量模式命令
                            sendCommand("##70112\r\n");
                            setSwitchViewState(false, svAutoMonitorState, tvAutoMonitorState, "已关闭");
                        }
                    });
                    mBuilder.onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            setSwitchViewState(true, svAutoMonitorState, tvAutoMonitorState, "已启用");
                        }
                    });
                }
            }
        });


        //测试模式
        svDebugMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtil.showShortToast("设备已断开连接，暂无法进行设置");
                    return;
                }

                if (svDebugMode.isOpened()) {
                    //发送打开测试模式命令
                    sendCommand("##70121\r\n");
                    setSwitchViewState(true, svDebugMode, tvDebugMode, "已打开");

                } else {
                    MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mContext);
                    mBuilder.title("温馨提示：")
                            .content("关闭自动监测，将导致设备自动关机进入休眠状态。请确认是否关闭")
                            .contentColor(Color.parseColor("#000000"))
                            .canceledOnTouchOutside(false)
                            .positiveText("确定")
                            .negativeText("取消")
                            .negativeColor(Color.parseColor("#807B7B"));
                    MaterialDialog mMaterialDialog = mBuilder.build();
                    mMaterialDialog.show();
                    mBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(
                                @NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            //发送关闭测试模式命令
                            sendCommand("##70122\r\n");
                            setSwitchViewState(false, svDebugMode, tvDebugMode, "已关闭");
                        }
                    });
                    mBuilder.onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            setSwitchViewState(true, svDebugMode, tvDebugMode, "已打开");
                        }
                    });
                }
            }
        });
    }


    @OnClick({R.id.iv_lock, R.id.platform_server_config_layout, R.id.refreshIV1, R.id.editBtn1, R.id.refreshIV2, R.id.editBtn2, R.id.dag_config_layout, R.id.executive_agency_param_layout, R.id.custom_command_test_layout, R.id.firmware_upgrade_layout})
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

            case R.id.platform_server_config_layout://展开或折叠服务器地址配置

                doExpandOrFoldServerConfig();
                break;

            case R.id.refreshIV1:
                mIvRefreshAddr1.startAnimation(mRefreshAnimation);
                break;

            case R.id.editBtn1:
                if (mBtnEdit1.getText().toString().contains("编辑")) {
                    mBtnEdit1.setText("确定");
                    mEtAddress1.setEnabled(true);
                    mEtAddress1.requestFocus();
                    mEtAddress1.setSelection(mEtAddress1.getText().length());
                } else {
                    mBtnEdit1.setText("编辑");
                    mEtAddress1.clearFocus();

                    doServerAddress1Config();
                }
                break;

            case R.id.refreshIV2:
                mIvRefreshAddr2.startAnimation(mRefreshAnimation);
                break;

            case R.id.editBtn2:
                if (mBtnEdit2.getText().toString().contains("编辑")) {
                    mBtnEdit2.setText("确定");
                    mEtAddress2.setEnabled(true);
                    mEtAddress2.requestFocus();
                    mEtAddress2.setSelection(mEtAddress2.getText().length());
                } else {
                    mBtnEdit2.setText("编辑");
                    mEtAddress2.clearFocus();

                    doServerAddress2Config();
                }
                break;

            case R.id.dag_config_layout:
                ADMESensorConfigActivity.startActivity(getActivity(), dagConfigInfo);
                break;

            case R.id.executive_agency_param_layout:
                ADMEExecutiveAgencyConfigActivity.startActivity(getActivity());
                break;

            case R.id.custom_command_test_layout:
                InstructionDebugActivity.startActivity(getActivity());
                break;

            case R.id.firmware_upgrade_layout:
                //固件升级
                ToastUtil.showShortToast("功能开发中...");
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


    private void doServerAddress1Config() {
        String address = mEtAddress1.getText().toString().trim();
        if (TextUtils.isEmpty(address)) {
            ToastUtil.showShortToast("地址不能为空");
            return;
        }

        if (!address.contains(".") || !address.contains(":")) {
            ToastUtil.showShortToast("地址格式不正确");
            return;
        }

        String addrArray[] = address.split(":");
        String cmdStr = "##2011 " + addrArray[0] + " " + addrArray[1] + "\r\n";
        sendCommand(cmdStr);
    }

    private void doServerAddress2Config() {
        String address = mEtAddress2.getText().toString().trim();
        if (TextUtils.isEmpty(address)) {
            ToastUtil.showShortToast("地址不能为空");
            return;
        }

        if (!address.contains(".") || !address.contains(":")) {
            ToastUtil.showShortToast("地址格式不正确");
            return;
        }

        String addrArray[] = address.split(":");
        String cmdStr = "##2012 " + addrArray[0] + " " + addrArray[1] + "\r\n";
        sendCommand(cmdStr);
    }

    /**
     * 设置显示数据
     */
    private void setResultData(String cmdStr) {
        Timber.d("=======从 ConfigADMEActivity 过来的eventbus数据======" + cmdStr);

        String cmdArray[] = cmdStr.replace("\r\n", "").split(",");

        //查询工作模式应答
        if (cmdStr.startsWith("$$7010") && cmdStr.endsWith("\r\n")) {
            if (cmdArray.length < 3) {
                Timber.d("查询工作模式应答指令错误");
                return;
            }

            if (TextUtils.isEmpty(cmdArray[1].trim())) {
                Timber.d("查询工作模式应答指令错误");
                return;
            }

            if (TextUtils.isEmpty(cmdArray[2].trim())) {
                Timber.d("查询工作模式应答指令错误");
                return;
            }

            setSwitchViewState(cmdArray[1].trim().equals("1"), svAutoMonitorState, tvAutoMonitorState, cmdArray[1].trim().equals("1") ? "已启用" : "已关闭");
            setSwitchViewState(cmdArray[2].trim().equals("1"), svDebugMode, tvDebugMode, cmdArray[2].trim().equals("1") ? "已打开" : "已关闭");
            return;
        }

        //查询服务器地址1应答
        if (cmdStr.startsWith("$$2001") && cmdStr.endsWith("\r\n")) {
            cmdArray = cmdStr.replace("\r\n", "").split(" ");
            if (cmdArray.length < 3) {
                Timber.d("查询服务器地址1应答指令错误");
                return;
            }

            if (TextUtils.isEmpty(cmdArray[1].trim())) {
                Timber.d("查询服务器地址1应答指令错误");
                return;
            }

            if (TextUtils.isEmpty(cmdArray[2].trim())) {
                Timber.d("查询服务器地址1应答指令错误");
                return;
            }

            mEtAddress1.setText(cmdArray[1] + ":" + cmdArray[2]);
            return;
        }

        //查询服务器地址2应答
        if (cmdStr.startsWith("$$2002") && cmdStr.endsWith("\r\n")) {
            cmdArray = cmdStr.replace("\r\n", "").split(" ");
            if (cmdArray.length < 3) {
                Timber.d("查询服务器地址2应答指令错误");
                return;
            }

            if (TextUtils.isEmpty(cmdArray[1].trim())) {
                Timber.d("查询服务器地址2应答指令错误");
                return;
            }

            if (TextUtils.isEmpty(cmdArray[2].trim())) {
                Timber.d("查询服务器地址2应答指令错误");
                return;
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


        //设置自动测量模式应答
        if (cmdStr.startsWith("$$7011") && cmdStr.endsWith("\r\n")) {
            showToastOnUiThread("设置自动测量模式完成");
            return;
        }

        //设置测试模式应答
        if (cmdStr.startsWith("$$7012") && cmdStr.endsWith("\r\n")) {
            showToastOnUiThread("设置测试模式完成");
            return;
        }

        //设置服务器地址1应答
        if (cmdStr.startsWith("$$2011") && cmdStr.endsWith("\r\n")) {
            showToastOnUiThread("设置MD-NET服务器地址完成");
            return;
        }

        //设置服务器地址2应答
        if (cmdStr.startsWith("$$2012") && cmdStr.endsWith("\r\n")) {
            showToastOnUiThread("设置mCloud服务器地址完成");
            return;
        }

        //设置采集器参数应答
        if (cmdStr.startsWith("$$7001") && cmdStr.endsWith("\r\n")) {
            showToastOnUiThread("设置采集器参数完成");
            dagConfigInfo = cmdStr;
            return;
        }

        //设置执行机构参数应答
        if (cmdStr.startsWith("$$7003") && cmdStr.endsWith("\r\n")) {
            showToastOnUiThread("设置执行机构参数完成");
            executiveAgencyConfigInfo = cmdStr;
            return;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (!TextUtils.isEmpty(messageEvent) && messageEvent.startsWith("$$")) {
            setResultData(messageEvent);
        }
    }


    private void sendCommand(String cmd) {
        Message msg = new Message(UUID.randomUUID().toString(), cmd, true);
        if (mdBluetoothManager != null) {
            mdBluetoothManager.writeMessage(msg);
        }
    }


    private void showToastOnUiThread(final String msg) {
        MCloudApp.getHandler().post(new Runnable() {
            @Override
            public void run() {
                ToastUtil.showShortToast(msg);
            }
        });
    }


    private void setSwitchViewState(boolean isOpen, SwitchView switchView, TextView textView, String content) {
        switchView.setOpened(isOpen);
        textView.setText(content);
        textView.setTextColor(isOpen ? getResources().getColor(R.color.colorPrimary) : getResources().getColor(R.color.gray_807B7B));
    }


    /**
     * 查询本地数据库中项目信息
     */
    private void queryProjectList() {
        List<SystemDataInfo> infoList = manager.getDaoSession().getSystemDataInfoDao().queryBuilder()
                .where(SystemDataInfoDao.Properties.Account.isNotNull(), SystemDataInfoDao.Properties.Account.eq(MCloudApp.getAccount()))
                .list();

        systemDataInfoList.clear();
        systemDataInfoHashMap.clear();
        if (null != infoList && infoList.size() > 0) {
            for (SystemDataInfo systemDataInfo : infoList) {
                systemDataInfoList.add(systemDataInfo.getProjName());
                systemDataInfoHashMap.put(systemDataInfo.getProjName(), systemDataInfo);
            }

            for (int i = 1; i < 35; i++) {
                systemDataInfoList.add(i + " 测试项目");
            }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

}
