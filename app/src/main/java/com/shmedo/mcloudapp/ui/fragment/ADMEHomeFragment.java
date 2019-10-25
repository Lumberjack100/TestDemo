package com.shmedo.mcloudapp.ui.fragment;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseFragment;
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
import com.shmedo.mcloudapp.views.editspinner.EditSpinner;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @BindView(R.id.dag_config_layout)
    View dagConfigLayout;

    @BindView(R.id.executive_agency_param_layout)
    View executiveAgencyParamLayout;

    @BindView(R.id.custom_command_test_layout)
    View customCommandTestLayout;

    private TextView tvAutoMonitorState, tvDebugMode;

    private SwitchView svAutoMonitorState, svDebugMode;

    private Unbinder unbinder;
    private String deviceInfo;
    private Handler handler;
    private boolean onRefreshFirst = false;
    private boolean initBluetooth = false;
    private long prelongTim = 0;
    private Context mContext;
    private DaoManager manager = DaoManager.getInstance();
    private List<String> systemDataInfoList = new ArrayList<>();//项目信息列表
    private HashMap<String, SystemDataInfo> systemDataInfoHashMap = new HashMap<>();


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
        setSwitchViewListener();

        return view;
    }

    private void getIntentData() {
        Intent intent = getActivity().getIntent();
        if (intent.getExtras().containsKey(Extras.CUR_DEVICE_NAME)) {
            deviceInfo = intent.getStringExtra(Extras.CUR_DEVICE_NAME);

            String[] scanData = deviceInfo.split(",");
            mTvDeviceName.setText("物联网数据采集器");
            mTvDeviceSn.setText(scanData[1]);//设备编号
            mTvDeviceModel.setText(scanData[2]);//功能型号
            mTvSensorType.setText("拉线位移计");
        }
    }

    private void initView() {
        handler = new Handler();

        ((TextView) autoMonitorLayout.findViewById(R.id.tv_config_name)).setText("自动监测启用");
        tvAutoMonitorState = autoMonitorLayout.findViewById(R.id.tv_device_state);
        svAutoMonitorState = autoMonitorLayout.findViewById(R.id.switchview);

        ((TextView) debugModelLayout.findViewById(R.id.tv_config_name)).setText("测试模式");
        tvDebugMode = debugModelLayout.findViewById(R.id.tv_device_state);
        svDebugMode = debugModelLayout.findViewById(R.id.switchview);

        ((TextView) platformServerConfigLayout.findViewById(R.id.tv_config_name)).setText("云平台服务器设置");
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
            ConfigADMEActivity.sendDeviceStateComd();
        }
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
                    setSwitchViewState(true, svAutoMonitorState, tvAutoMonitorState, "已激活");

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
                            setSwitchViewState(false, svAutoMonitorState, tvAutoMonitorState, "已待机");
                        }
                    });
                    mBuilder.onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            setSwitchViewState(true, svAutoMonitorState, tvAutoMonitorState, "已激活");
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
                    setSwitchViewState(true, svDebugMode, tvDebugMode, "已开启");

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

                            setSwitchViewState(true, svDebugMode, tvDebugMode, "已开启");
                        }
                    });
                }
            }
        });
    }


    @OnClick({R.id.iv_lock, R.id.platform_server_config_layout, R.id.dag_config_layout, R.id.executive_agency_param_layout, R.id.custom_command_test_layout, R.id.firmware_upgrade_layout})
    public void onClick(View v) {
        Intent intent = null;

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

            case R.id.platform_server_config_layout:

                break;

            case R.id.dag_config_layout:
                ADMESensorConfigActivity.startActivity(getActivity());
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


    private void sendCommand(String cmd) {
        Message msg = new Message(UUID.randomUUID().toString(), cmd, true);
        if (ConfigADMEActivity.mdBluetoothManager != null) {
            ConfigADMEActivity.mdBluetoothManager.writeMessage(msg);
        }
    }

    /**
     * 设置显示数据
     */
    private void setResultData() {
        Timber.d("=======从 ConfigADMEActivity 过来的eventbus数据======");


    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (messageEvent.equals("ParameterConfigFragment")) {
            setResultData();
        }
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
