package com.shmedo.mcloudapp.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseFragment;
import com.shmedo.mcloudapp.bluetooth.Message;
import com.shmedo.mcloudapp.entity.SystemDataInfo;
import com.shmedo.mcloudapp.entity.SystemDataInfoDao;
import com.shmedo.mcloudapp.model.Extras;
import com.shmedo.mcloudapp.ui.activity.device.GeneralSettingActivity;
import com.shmedo.mcloudapp.ui.activity.device.OsmometerConfigActivity;
import com.shmedo.mcloudapp.ui.activity.device.RainConfigActivity;
import com.shmedo.mcloudapp.ui.activity.device.sensor.SenSorBGKConfigActivity;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.views.VerticalSwipeRefreshLayout;
import com.shmedo.mcloudapp.views.editspinner.EditSpinner;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ch.ielse.view.SwitchView;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.fragment
 * 文件名:   RunStatusFragment
 * 创建者:   dpc
 * 创建时间:  2019/3/12 16:28
 * 描述：    参数配置页面
 */
public class ParameterConfigFragment extends BaseFragment
        implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;

    @BindView(R.id.tv_device_model)
    TextView mTvDeviceModel;

    @BindView(R.id.tv_sensor_type)
    TextView mTvSensorType;

    @BindView(R.id.iv_pro)
    ImageView mIvPro;

    @BindView(R.id.iv_lock)
    ImageView mIvLock;

    @BindView(R.id.tv_lock)
    TextView mTvLock;

    @BindView(R.id.editSpinner1)
    EditSpinner spinnerProjectName;

    @BindView(R.id.tv_projectName)
    TextView mTvProName;

    @BindView(R.id.sw_device_state)
    SwitchView mSwDeviceState;

    @BindView(R.id.tv_device_state)
    TextView mTvDeviceState;

    @BindView(R.id.sw_device_luck_state)
    SwitchView mSwDeviceLuckState;

    @BindView(R.id.tv_device_luck_state)
    TextView mTvDeviceLuckState;

    @BindView(R.id.sw_debug)
    SwitchView mSwDebug;

    @BindView(R.id.sp_debug)
    Spinner mSpDebug;

    @BindView(R.id.tv_sim_1)
    TextView mTvSim1;

    @BindView(R.id.sw_sim_A)
    SwitchView mSwSimA;

    @BindView(R.id.tv_sim_A)
    TextView mTvSimA;

    @BindView(R.id.tv_sim_2)
    TextView mTvSim2;

    @BindView(R.id.sw_sim_B)
    SwitchView mSwSimB;

    @BindView(R.id.tv_sim_B)
    TextView mTvSimB;

    @BindView(R.id.sw_rain)
    SwitchView mSwRain;

    @BindView(R.id.tv_rain_config)
    TextView mTvRainConfig;

    @BindView(R.id.sw_osmometer)
    SwitchView mSwOsmometer;

    @BindView(R.id.tv_osmometer_config)
    TextView mTvOsmometerConfig;

    @BindView(R.id.refresh)
    VerticalSwipeRefreshLayout mRefreshLayout;

    @BindView(R.id.scrollView)
    ScrollView mScrollView;

    private Unbinder unbinder;
    private String deviceInfo;

    private MaterialDialog.Builder mBuilder;
    private MaterialDialog mMaterialDialog;
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
        return R.layout.fragment_parameter_config;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        mContext = getActivity();

        getIntentData();
        queryProjectList();
        initView();

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
        mRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light,
                android.R.color.holo_green_light);
        mRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        mRefreshLayout.setScrollUpChild(mScrollView);
        mRefreshLayout.setOnRefreshListener(this);
        handler = new Handler();

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

        //调试模式
        String[] debugData = getResources().getStringArray(R.array.bluetooth_debug);
        ArrayAdapter<String> debugDataAdapter = new ArrayAdapter<>(mContext, R.layout.spinner_item, debugData);
        mSpDebug.setAdapter(debugDataAdapter);
        mSpDebug.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                //ToastUtils.show("" + adapterView.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (MCloudApp.isIsBluetoothDeviceConnected()) {
            DeviceFragment.sendDeviceStateComd();
        }

//        TODO SIM卡A功能
//        mSwSimA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (b) {
//                    mTvSimA.setText("已开启");
//                } else {
//                    mTvSimA.setText("已关闭");
//                }
//            }
//        });
//         TODO SIM卡B功能
//        mSwSimB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (b) {
//                    mTvSimB.setText("已开启");
//                } else {
//                    mTvSimB.setText("已关闭");
//                }
//            }
//        });
//        TODO 写在查询调试结果后 解析数据后根据结果来
//        if (mSwRain.isChecked()) {
//            mTvRainConfig.setText("配置");
//            mTvRainConfig.setClickable(true);
//            mTvRainConfig.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
//        } else {
//            mTvRainConfig.setBackgroundColor(getResources().getColor(R.color.secondary_text));
//            mTvRainConfig.setText("已停用");
//            mTvRainConfig.setClickable(false);
//        }

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
        }
    }


    /**
     * switch按钮事件
     */
    private void initSwitchData() {
        //设备启用状态
        mSwDeviceState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSwDeviceState.isOpened()) {
                    //发送激活指令
                    Message msg = new Message("chat", "##0182\r\n", true);
                    if (DeviceFragment.mdBluetoothManager != null) {
                        DeviceFragment.mdBluetoothManager.writeMessage(msg);
                    }

                    mSwDeviceState.setOpened(true);
                    mTvDeviceState.setText("已激活");
                    mTvDeviceState.setTextColor(getResources().getColor(R.color.colorPrimary));

                } else {
                    mBuilder = new MaterialDialog.Builder(mContext);
                    mBuilder.title("温馨提示：")
                            .content("关闭系统激活状态，将导致设备自动关机进入休眠状态。请确认是否关闭【激活状态】")
                            .contentColor(Color.parseColor("#000000"))
                            .canceledOnTouchOutside(false)
                            .positiveText("确定")
                            .negativeText("取消")
                            .negativeColor(Color.parseColor("#807B7B"));
                    mMaterialDialog = mBuilder.build();
                    mMaterialDialog.show();
                    mBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(
                                @NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            //发送待机指令
                            Message msg = new Message("chat", "##0181\r\n", true);
                            if (DeviceFragment.mdBluetoothManager != null) {
                                DeviceFragment.mdBluetoothManager.writeMessage(msg);
                            }

                            mSwDeviceState.setOpened(false);
                            mTvDeviceState.setText("已待机");
                            mTvDeviceState.setTextColor(getResources().getColor(R.color.gray_807B7B));
                        }
                    });
                    mBuilder.onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(
                                @NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            mSwDeviceState.setOpened(true);
                            mTvDeviceState.setText("已激活");
                            mTvDeviceState.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                    });
                }
            }
        });


        //设备锁定状态
        mSwDeviceLuckState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSwDeviceLuckState.isOpened()) {
                    //发送激活指令
                    Message msg = new Message("chat", "##2250\r\n", true);
                    if (DeviceFragment.mdBluetoothManager != null) {
                        DeviceFragment.mdBluetoothManager.writeMessage(msg);
                    }

                    mSwDeviceLuckState.setOpened(true);
                    mTvDeviceLuckState.setText("未锁定");
                    mTvDeviceLuckState.setTextColor(getResources().getColor(R.color.colorPrimary));

                } else {
                    //发送激活指令
                    Message msg = new Message("chat", "##2251\r\n", true);
                    if (DeviceFragment.mdBluetoothManager != null) {
                        DeviceFragment.mdBluetoothManager.writeMessage(msg);
                    }

                    mSwDeviceLuckState.setOpened(false);
                    mTvDeviceLuckState.setText("锁定");
                    mTvDeviceLuckState.setTextColor(getResources().getColor(R.color.gray_807B7B));
                }
            }
        });

        //雨量计功能。
        mSwRain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSwRain.isOpened()) {
                    setSwitchViewState(true, mSwRain, mTvRainConfig);
                    rainSelect("1");//打开雨量站

                } else {
                    mBuilder = new MaterialDialog.Builder(getActivity());
                    mBuilder.title("温馨提示：")
                            .content("确认要关闭雨量站？")
                            .contentColor(Color.parseColor("#000000"))
                            .canceledOnTouchOutside(false)
                            .positiveText("确定")
                            .negativeText("取消")
                            .negativeColor(Color.parseColor("#807B7B"));
                    mMaterialDialog = mBuilder.build();
                    mMaterialDialog.show();
                    mBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(
                                @NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            setSwitchViewState(false, mSwRain, mTvRainConfig);
                            rainSelect("2");//关闭雨量站
                        }
                    });
                    mBuilder.onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(
                                @NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            setSwitchViewState(true, mSwRain, mTvRainConfig);
                        }
                    });
                }
            }
        });

        //渗压计功能
        mSwOsmometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSwOsmometer.isOpened()) {
                    setSwitchViewState(true, mSwOsmometer, mTvOsmometerConfig);
                    osmometerSelect("1"); //打开渗压计

                } else {
                    mBuilder = new MaterialDialog.Builder(getActivity());
                    mBuilder.title("温馨提示：")
                            .content("关闭系统激活状态，将导致设备自动关机进入休眠状态。请确认是否关闭【激活状态】")
                            .contentColor(Color.parseColor("#000000"))
                            .canceledOnTouchOutside(false)
                            .positiveText("确定")
                            .negativeText("取消")
                            .negativeColor(Color.parseColor("#807B7B"));
                    mMaterialDialog = mBuilder.build();
                    mMaterialDialog.show();
                    mBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(
                                @NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            setSwitchViewState(false, mSwOsmometer, mTvOsmometerConfig);
                            osmometerSelect("2");//关闭渗压计
                        }
                    });
                    mBuilder.onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(
                                @NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            setSwitchViewState(true, mSwOsmometer, mTvOsmometerConfig);
                        }
                    });
                }
            }
        });
    }

    /**
     * 雨量计开关
     */
    private void rainSelect(String parameter) {
        if (MCloudApp.isIsBluetoothDeviceConnected()) {
            //雨量站开关
            Message msg = new Message("##005",
                    "##005" + parameter + "\r\n", true);
            if (DeviceFragment.mdBluetoothManager != null) {
                DeviceFragment.mdBluetoothManager.writeMessage(msg);
            }
        } else {
            ToastUtils.show("蓝牙未连接");
        }
    }


    /**
     * 渗压计开关
     */
    private void osmometerSelect(String parameter) {
        if (MCloudApp.isIsBluetoothDeviceConnected()) {
            //渗压计开关
            Message msg = new Message("##401",
                    "##401" + parameter + "\r\n", true);
            if (DeviceFragment.mdBluetoothManager != null) {
                DeviceFragment.mdBluetoothManager.writeMessage(msg);
            }
        } else {
            ToastUtils.show("蓝牙未连接");
        }
    }

    private void setSwitchViewState(boolean isOpen, SwitchView switchView, TextView textView) {
        if (isOpen) {

            switchView.setOpened(true);
            textView.setClickable(true);
            textView.setText("配置");
            textView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            textView.setTextColor(getResources().getColor(R.color.white));

        } else {

            switchView.setOpened(false);
            textView.setClickable(false);
            textView.setText("已停用");
            textView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            textView.setTextColor(getResources().getColor(R.color.gray_807B7B));
        }
    }


    /**
     * 设置显示数据
     */
    private void setResultData() {
        Timber.d("=======从devicefragment过来的eventbus数据======");

        if (DeviceFragment.baseConfigInfoSub != null) {
            //  设备启用状态
            if (DeviceFragment.baseConfigInfoSub.getEquipmentStatus().equals("待机")) {
                mSwDeviceState.setOpened(false);
                mTvDeviceState.setText("已待机");
                mTvDeviceState.setTextColor(getResources().getColor(R.color.gray_807B7B));

            } else if (DeviceFragment.baseConfigInfoSub.getEquipmentStatus().equals("激活")) {
                mSwDeviceState.setOpened(true);
                mTvDeviceState.setText("已激活");
                mTvDeviceState.setTextColor(getResources().getColor(R.color.colorPrimary));
            }

            //设备锁定状态
            if (DeviceFragment.lockStatus.equals("unlock")) {
                mSwDeviceLuckState.setOpened(true);
                mTvDeviceLuckState.setText("未锁定");
                mTvDeviceLuckState.setTextColor(getResources().getColor(R.color.colorPrimary));

            } else if (DeviceFragment.lockStatus.equals("lock")) {
                mSwDeviceLuckState.setOpened(false);
                mTvDeviceLuckState.setText("锁定");
                mTvDeviceLuckState.setTextColor(getResources().getColor(R.color.gray_807B7B));
            }

            //设备调试模式
            if (DeviceFragment.baseConfigInfoSub.getDebugModel().equals("DEBUG")) {
                mSpDebug.setSelection(0);
                mSwDebug.setOpened(true);
            } else if (DeviceFragment.baseConfigInfoSub.getDebugModel().equals("INFO")) {
                mSpDebug.setSelection(1);
                mSwDebug.setOpened(true);
            } else if (DeviceFragment.baseConfigInfoSub.getDebugModel().equals("初始化")) {
                mSwDebug.setOpened(true);
            } else if (DeviceFragment.baseConfigInfoSub.getDebugModel().equals("关闭")) {
                mSwDebug.setOpened(false);
            }

            //选择SIM卡功能
            if (DeviceFragment.baseConfigInfoSub.getSimChoose().equals("选择sim卡1")) {
                mSwSimA.setOpened(true);
                mSwSimB.setOpened(false);
            } else if (DeviceFragment.baseConfigInfoSub.getSimChoose().equals("选择sim卡2")) {
                mSwSimA.setOpened(false);
                mSwSimB.setOpened(true);
            }
            initBluetooth = true;
        }

        //雨量计开关
        if (DeviceFragment.setSelectRainParameter != null) {
            if (DeviceFragment.setSelectRainParameter.getRainSelect().equals("1")) {
                setSwitchViewState(true, mSwRain, mTvRainConfig);
            } else if (DeviceFragment.setSelectRainParameter.getRainSelect().equals("2")){
                setSwitchViewState(false, mSwRain, mTvRainConfig);

            }else if (DeviceFragment.setSelectRainParameter.getRainSelect().equals("3")){
                Log.i("adu","getRainSelect  333");
            }
            initBluetooth = true;
        }

        //渗压计开关
        if (DeviceFragment.queryOsmometerParameterSubInfo != null) {
            if (DeviceFragment.queryOsmometerParameterSubInfo.getOsmometerStatus().equals("开启")) {
                setSwitchViewState(true, mSwOsmometer, mTvOsmometerConfig);
            } else if (DeviceFragment.queryOsmometerParameterSubInfo.getOsmometerStatus().equals("关闭")) {
                setSwitchViewState(false, mSwOsmometer, mTvOsmometerConfig);
            }

            initBluetooth = true;
        }

        if (initBluetooth) {
            initSwitchData();
        }
    }


    @OnClick({R.id.iv_lock, R.id.rl_sensor_setting, R.id.rl_general_setting,
            R.id.tv_rain_config, R.id.tv_osmometer_config})
    public void onViewClicked(View view) {
        Intent intent = null;
        switch (view.getId()) {
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

            case R.id.rl_sensor_setting:
                //采集器的传感器参数配置   根据传感器的类型来进行
                if (MCloudApp.isIsBluetoothDeviceConnected()) {
                    //intent = new Intent(getActivity(), SenSorMPSConfigActivity.class);
                    intent = new Intent(getActivity(), SenSorBGKConfigActivity.class);
                    startActivity(intent);
                } else {
                    ToastUtils.show("蓝牙未连接");
                }
                break;

            case R.id.rl_general_setting:
                //通用设置--采集器
                intent = new Intent(getActivity(), GeneralSettingActivity.class);
                startActivity(intent);
                break;

            case R.id.tv_rain_config:
                //雨量计配置
                intent = new Intent(getActivity(), RainConfigActivity.class);
                startActivity(intent);
                break;

            case R.id.tv_osmometer_config:
                //渗压计配置
                intent = new Intent(getActivity(), OsmometerConfigActivity.class);
                startActivity(intent);
                break;
        }
    }


    /**
     * 下拉刷新
     */
    @Override
    public void onRefresh() {
        if (!onRefreshFirst) {
            DeviceFragment.sendDeviceStateComd();
            //loadWebView(runState);
            onRefreshFirst = true;
        }
        if (prelongTim == 0) {
            prelongTim = (new Date()).getTime();
            mRefreshLayout.setRefreshing(false);
        } else {
            long curTime = (new Date()).getTime();
            long tenTime = curTime - prelongTim;

            //如果下拉刷新超过10s再次发送指令
            if (tenTime >= 10000) {
                DeviceFragment.sendDeviceStateComd();
                ToastUtils.show("已重新发送指令");
                //loadWebView(runState);
                prelongTim = 0;
            } else {
                ToastUtils.show("发送指令间隔需超过10s");
                mRefreshLayout.setRefreshing(false);
            }
        }

        if (!MCloudApp.isIsBluetoothDeviceConnected()) {
            mRefreshLayout.setRefreshing(false);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (messageEvent.equals("ParameterConfigFragment")) {
            setResultData();
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
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }



    @Override
    public boolean onBackPressed() {
        return false;
    }
}
