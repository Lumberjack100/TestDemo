package com.shmedo.mcloudapp.ui.fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.das.common.GetAllSensorConfigInfo;
import com.shmedo.das.das.cmd.CommandManager;
import com.shmedo.das.das.cmd.CommandType;
import com.shmedo.das.utils.DesUtil;
import com.shmedo.das.utils.OnBytePackage;
import com.shmedo.das.utils.StringUtil;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseFragment;
import com.shmedo.mcloudapp.bluetooth.BluetoothDeviceFindEventData;
import com.shmedo.mcloudapp.bluetooth.BluetoothEvent;
import com.shmedo.mcloudapp.bluetooth.BluetoothEventHandler;
import com.shmedo.mcloudapp.bluetooth.MdBluetoothManager;
import com.shmedo.mcloudapp.bluetooth.Message;
import com.shmedo.mcloudapp.entity.ble.BaseConfigInfoSub;
import com.shmedo.mcloudapp.entity.ble.CollectorInfoSub;
import com.shmedo.mcloudapp.entity.ble.DeviceLockStatusSub;
import com.shmedo.mcloudapp.entity.ble.DigitalOsmometerFunctionSub;
import com.shmedo.mcloudapp.entity.ble.QueryOsmometerParameterSubInfo;
import com.shmedo.mcloudapp.entity.ble.RainStationSub;
import com.shmedo.mcloudapp.entity.ble.RebootDeviceSub;
import com.shmedo.mcloudapp.entity.ble.SettingRainPrecisionSub;
import com.shmedo.mcloudapp.entity.ble.SystemRunStateSub;
import com.shmedo.mcloudapp.entity.ble.VersionMessageSub;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;
import com.shmedo.mcloudapp.model.Extras;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.util.bleutil.BlueResultParserUtil;
import com.shmedo.mcloudapp.util.bleutil.ByteManagerUtil;
import com.shmedo.mcloudapp.util.bleutil.Constants;
import com.shmedo.mcloudapp.util.page.model.SetRainAccuryPage;
import com.shmedo.mcloudapp.util.page.model.SetRainSelectPage;
import com.shmedo.mcloudapp.views.LoadingDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.fragment
 * 文件名:   DeviceFragment
 * 创建者:   dpc
 * 创建时间:  2019/3/13 09:14
 * 描述：    所有的设备类
 */
public class DeviceFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.img_bluetooth)
    ImageView mImgBluetooth;

    @BindView(R.id.sub_content)
    FrameLayout mSubContent;

    @BindView(R.id.tv_parameter)
    TextView mTvParameter;

    @BindView(R.id.tv_query_data)
    TextView mTvQueryData;

    @BindView(R.id.tv_device_details)
    TextView mTvDeviceDetails;

    @BindView(R.id.tv_highsetting)
    TextView mTvHighsetting;

    private ParameterConfigFragment parameterConfigFragment;//参数配置
    private QueryDataFragment queryDataFragment;        //查询数据
    private DeviceDetailsFragment deviceDetailsFragment;//设备详情
    private AdvanceSetFragment advanceSetFragment;      //高级设置

    private Unbinder unbinder;
    private LoadingDialog mLoadingDialog;
    private Handler hander;

    private static boolean isConnected = false;
    private boolean isBlueMode = true;//当前模式是否是蓝牙模式
    private boolean stopBluetooth = false;

    public static MdBluetoothManager mdBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String SN = "";
    private String deviceInfo;
    private String macAddress;

    public static QueryOsmometerParameterSubInfo queryOsmometerParameterSubInfo;
    public static SystemRunStateSub systemRunStateSub;
    public static DigitalOsmometerFunctionSub digitalOsmometerFunctionSub;
    public static RainStationSub rainStationSub;
    public static SettingRainPrecisionSub settingRainPrecisionSub;
    public static RebootDeviceSub rebootDeviceSub;
    public static CollectorInfoSub collectorInfoSub;
    public static GetAllSensorConfigInfo getAllSensorConfigInfo;
    public static BaseConfigInfoSub baseConfigInfoSub;
    public static VersionMessageSub versionMessageSub;
    public static SetRainAccuryPage.SetRianAccuryParameter setRianAccuryParameter = new SetRainAccuryPage.SetRianAccuryParameter();
    public static SetRainSelectPage.SetSelectRainParameter setSelectRainParameter = new SetRainSelectPage.SetSelectRainParameter();
    public static DeviceLockStatusSub deviceLockStatusSub;

    public static List<CollectorSensorParamsInfoSub> mCollectorParamsInfoSubList = new ArrayList<>();
    public static CollectorSensorParamsInfoSub mCollectorParamsInfoSub;

    public static String collectorType = "";//采集器编号
    public static String lockStatus = "";


    private static final int REQUEST_ENABLE_BT = 0x001;


    @Override
    protected int initContentView() {
        return R.layout.fragment_device;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        initView();
        initBluetooth();
        getIntentData();
        return view;
    }


    private void initView() {
        mLoadingDialog = new LoadingDialog(getActivity());
        hander = new Handler();
        mTvParameter.setOnClickListener(this);
        mTvQueryData.setOnClickListener(this);
        mTvDeviceDetails.setOnClickListener(this);
        mTvHighsetting.setOnClickListener(this);
        setDefaultFragment();
    }

    private void getIntentData() {
        Intent intent = getActivity().getIntent();
        if (intent.getExtras().containsKey(Extras.DEVICE_MAC_ADDRESS)) {
            macAddress = intent.getStringExtra(Extras.DEVICE_MAC_ADDRESS);
        }

        if (intent.getExtras().containsKey(Extras.CUR_DEVICE_NAME)) {
            deviceInfo = intent.getStringExtra(Extras.CUR_DEVICE_NAME);
            Timber.d("deviceInfo=" + deviceInfo);
            String[] scanData = deviceInfo.split(",");
            SN = scanData[1];

            connectBluetooth();
        }
    }

    /**
     * set the default Fragment
     */
    private void setDefaultFragment() {
        switchFrgment(0);
        //set the defalut tab state
        setTabState(mTvParameter, R.drawable.szxd, getColor(R.color.colorPrimary));
    }


    @Override
    public void onClick(View v) {
        resetTabState();//reset the tab state
        switch (v.getId()) {
            case R.id.tv_parameter:
                setTabState(mTvParameter, R.drawable.szxd, getColor(R.color.colorPrimary));
                switchFrgment(0);
                break;
            case R.id.tv_query_data:
                setTabState(mTvQueryData, R.drawable.yxzt, getColor(R.color.colorPrimary));
                switchFrgment(1);
                break;
            case R.id.tv_device_details:
                setTabState(mTvDeviceDetails, R.drawable.xtgj, getColor(R.color.colorPrimary));
                switchFrgment(2);
                break;
            case R.id.tv_highsetting:
                setTabState(mTvHighsetting, R.drawable.gjpz, getColor(R.color.colorPrimary));
                switchFrgment(3);
                break;

            default:
        }
    }


    /**
     * switch the fragment accordting to id
     *
     * @param i id
     */
    private void switchFrgment(int i) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        switch (i) {
            case 0:
                parameterConfigFragment = new ParameterConfigFragment();
                transaction.replace(R.id.sub_content, parameterConfigFragment);
                break;
            case 1:
                queryDataFragment = new QueryDataFragment();
                transaction.replace(R.id.sub_content, queryDataFragment);
                break;
            case 2:
                deviceDetailsFragment = new DeviceDetailsFragment();
                transaction.replace(R.id.sub_content, deviceDetailsFragment);
                break;
            case 3:
                advanceSetFragment = new AdvanceSetFragment();
                transaction.replace(R.id.sub_content, advanceSetFragment);
                break;
            default:
        }
        transaction.commit();
    }


    /**
     * set the tab state of bottom navigation bar
     *
     * @param textView the text to be shown
     * @param image    the image
     * @param color    the text color
     */
    private void setTabState(TextView textView, int image, int color) {
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, image, 0, 0);//Call requires API level 17
        textView.setTextColor(color);
    }


    /**
     * revert the image color and text color to black
     */
    private void resetTabState() {
        setTabState(mTvParameter, R.drawable.szxd_wxz, getColor(R.color.font_main));
        setTabState(mTvQueryData, R.drawable.yxzt_wxz, getColor(R.color.font_main));
        setTabState(mTvDeviceDetails, R.drawable.xtgj_wxz, getColor(R.color.font_main));
        setTabState(mTvHighsetting, R.drawable.gjpz_wxz, getColor(R.color.font_main));

    }


    /**
     * @param i the color id
     * @return color
     */
    private int getColor(int i) {
        return ContextCompat.getColor(getActivity(), i);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    private void initBluetooth() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = Objects.requireNonNull(bluetoothManager).getAdapter();
        MdBluetoothManager.init(mBluetoothAdapter, bluetoothManager);
        mdBluetoothManager = MdBluetoothManager.getInstance();
        mdBluetoothManager.setEventHandler(new MdBluetoothEventHandler());
    }

    /**
     * 扫描蓝牙设备，主要用来判断要连接的设备是否能被搜索到
     */
    private void startDiscoveryDevice() {
        //未打开蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        //蓝牙已打开时，开始扫描蓝牙设备
        mdBluetoothManager.scanDevice(20, getActivity());
    }


    @OnClick({R.id.back, R.id.img_bluetooth})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                break;

            case R.id.img_bluetooth:
                if (isConnected) {
                    showChangeModle(getResources().getString(R.string.disconnect_bluetooth_device), "2");
                } else {
                    connectBluetooth();
                }
                break;
        }
    }


    /**
     * 连接蓝牙设备
     */
    private void connectBluetooth() {
        if (isBlueMode && !isConnected) {

            //通过蓝牙设备列表页面跳转过来时，不需要开启蓝牙搜索，直接连击设备
            if (!TextUtils.isEmpty(macAddress)) {
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);
                if (device != null) {
                    mdBluetoothManager.stopScan();
                    mdBluetoothManager.connectDevice(device, getActivity());
                    if (null == mLoadingDialog.getDialog() || !mLoadingDialog.getDialog().isShowing()) {
                        mLoadingDialog.showNoCancelDialog("正在连接设备：" + SN);
                        hander.postDelayed(dismssConDialogRunnable, 10000);
                    }
                }
                return;
            }

            //搜索附近蓝牙设备
            startDiscoveryDevice();
            if (null != mBluetoothAdapter && mBluetoothAdapter.isEnabled()) {
                if (null == mLoadingDialog.getDialog() || !mLoadingDialog.getDialog().isShowing()) {
                    mLoadingDialog.showCancelDialog("正在搜索设备：" + SN);
                    hander.postDelayed(dismssConDialogRunnable, 10000);
                }
            }

        } else if (isBlueMode && isConnected) {
            showChangeModle(getResources().getString(R.string.disconnect_bluetooth_device), "2");
        }
    }


    private Runnable dismssConDialogRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
        }
    };

    /**
     * ble 取消连接
     */
    private void disconnectDevice() {
        //isShowingDialog = false;
        if (null != MdBluetoothManager.getInstance()) {
            MdBluetoothManager.getInstance().disconnect();
        }
    }


    private class MdBluetoothEventHandler implements BluetoothEventHandler {
        @Override
        public void handle(final BluetoothEvent event) {
            switch (event.getEventType()) {
                case DEVICE_FIND:
                    Timber.d("DEVICE_FIND===" + ((BluetoothDeviceFindEventData) event.getEventData()).getNewDevice().getDevice().getName());
                    handleDeviceFind((BluetoothDeviceFindEventData) event.getEventData());
                    break;

                case CONNECTED:
                    ByteManagerUtil.init(new MyOnBytePackage());
                    mHandler.sendEmptyMessage(Constants.BT_CONNECT);
                    break;

                case DISCONNECTED:
                    mHandler.sendEmptyMessage(Constants.BT_DISCONNECTED);
                    break;

                case REQUEST_MTU_FAIL:
                    Timber.d("MTU请求设置失败");
                    //mHandler.sendEmptyMessage(BT_REQUEST_MTU_FAIL);
                    break;

                case SERVICE_FIND_FAIL:
                    Timber.d("蓝牙服务发现失败");
                    mHandler.sendEmptyMessage(Constants.BT_SERVICE_FIND_FAIL);
                    break;

                case CHARACTERISTICS_FIND_FAIL:
                    Timber.d("特征读取失败");
                    mHandler.sendEmptyMessage(Constants.BT_CHARACTERISTICS_FIND_FAIL);
                    break;

                case ENABLE_READ_SUCCESS:
                    Timber.d("设置读取Descriptor成功");
                    mHandler.sendEmptyMessage(Constants.BT_ENABLE_READ_SUCCESS);
                    break;

                case ENABLE_READ_FAIL:
                    Timber.d("设置读取Descriptor失败");
                    mHandler.sendEmptyMessage(Constants.BT_ENABLE_READ_FAIL);
                    break;

                case WRITE_TIME_OUT:
                    Timber.d("写入等待超时");
                    disconnectDevice();
                    mHandler.sendEmptyMessage(Constants.BT_WRITE_TIME_OUT);
                    break;

                case MESSAGE_WRITE_SUCCESS: {
                    Timber.d("消息写入成功");
                    mHandler.sendEmptyMessage(Constants.BT_MESSAGE_WRITE_SUCCESS);
                    try {
                        String msg = ((Message) event.getEventData()).getResponseMessage();
                        byte[] data = (byte[]) msg.getBytes();

                        if (data != null && data.length > 0) {
                            ByteManagerUtil.getInstance().writeByte(data);
                        }

                    } catch (Exception ex) {
                        Timber.e(ex);
                    }

                    break;
                }
                case MESSAGE_RESPONSE_TIME_OUT:
                    Timber.d("消息等待响应超时");
                    mHandler.sendEmptyMessage(Constants.MESSAGE_RESPONSE_TIME_OUT);
                    disconnectDevice();
                    break;

                case MESSAGE_WRITE_FAIL:
                    Timber.d("消息写入失败");
                    mHandler.sendEmptyMessage(Constants.BT_MESSAGE_WRITE_FAIL);
                    break;

                case RESPONSE_WITH_NO_MESSAGE:
                    try {
                        byte[] data = (byte[]) event.getEventData();
                        if (data != null && data.length > 0) {
                            ByteManagerUtil.getInstance().writeByte(data);
                        }

                    } catch (Exception ex) {
                        Timber.e(ex);
                    }
                    break;

                default:
                    break;
            }
        }
    }

    private class MyOnBytePackage implements OnBytePackage {
        @Override
        public void onPackageArrived(final byte[] data) {
            try {
                String str = new String(data, "utf-8");

                //Log.i(LogTag.INFO_TAG, "反馈结果===" + str);

                String temp = str.replace("\r\n", "");
                String result[] = temp.split(",");
                if (result[result.length - 1].equals("lock")) {
                    lockStatus = "lock";
                    startBluAuthenticate();
                    //return;
                } else if (result[result.length - 1].equals("unlock")) {
                    lockStatus = "unlock";
                    android.os.Message message = new android.os.Message();
                    message.what = Constants.MESSAGE_LOCK_REBOOT_DEVICE;
                    mHandler.sendMessage(message);
                    return;
                } else if (str.equals("Equipment Verify OK.\r\n")) {
                    android.os.Message message = new android.os.Message();
                    message.what = Constants.MESSAGE_LOCK_REBOOT_DEVICE;
                    mHandler.sendMessage(message);
                    return;
                } else if (str.startsWith("$$224") && str.endsWith("\r\n")) {
                    if (str.substring(0, str.length() - 2).equals("$$224ce")) {
                        startBluAuthenticate();//重新 认证
                    } else {
                        String[] strs = str.substring(0, str.length() - 2).split(",");
                        byte[] resultData = StringUtil.hexStringToBytes(strs[3]);
                        try {
                            String deskey = "12345678";
                            String strdes = new String(DesUtil.decrypt(resultData, deskey), "utf-8");
                            if (strdes.length() != 0) {
                                String desStr = StringUtil.bytesToHexString(DesUtil.encrypt(
                                        (StringUtil.reverseString(strdes.substring(0, 6)) +
                                                deskey).getBytes(), deskey));
                                String com = "##222," + SN + ",0," + desStr.toUpperCase() + "\r\n";
                                Message msg = new Message(UUID.randomUUID().toString(), com, true);
                                mdBluetoothManager.writeMessage(msg);
                                Timber.d("===-发送指令===" + com);
                                return;
                            }
                        } catch (Exception e) {
                            //CommonUtil.handlerException(MainActivity.this, e);
                        }
                    }
                } else if (str.startsWith("$$223")) {
                    String[] verifyReult = str.substring(0, str.length() - 2).split(",");
                    android.os.Message message = new android.os.Message();
                    message.what = Constants.VERIFY_RESULT;
                    message.obj = verifyReult[1];
                    mHandler.sendMessage(message);
                    Timber.d("认证结果===" + verifyReult[1]);

                } else if (str.startsWith("$$225") && str.endsWith("\r\n")) {
                    deviceLockStatusSub = BlueResultParserUtil.getDeviceLockStatusInfo(str);
                    if (deviceLockStatusSub.getLockStatus() == 0) {
                        lockStatus = "unlock";
                    } else {
                        lockStatus = "lock";
                    }
                } else {
                    //android.os.Message message = new android.os.Message();
                    //message.what = MESSAGE_QUERY_OSMOMETER_PARAMETER;
                    //message.obj = str;
                    //mHandler.sendMessage(message);
                    parserResult(str);
                }

                //else if (str.startsWith("$$119")) {
                //    mHandler.sendEmptyMessage(BT_RECOVERY_SUCCESS);
                //} else if (str.startsWith("$$0191")) {
                //    mHandler.sendEmptyMessage(MESSAGE_RESPONSE_SAVE_SETTINGS_SUCCESS);
                //}
                //else {
                //}

            } catch (Exception ex) {
                Timber.e(ex);
            }
        }
    }


    private void parserResult(String result) {
        if (result.equals("Please verify the equipment.\r\n")) {
            android.os.Message message = new android.os.Message();
            message.what = Constants.VERIFY_RESULT;
            message.obj = "0";
            mHandler.sendMessage(message);

        } else {
            try {
                Timber.d("--------返回指令结果-------" + result);
                CommandType type = StringUtil.extractCommandType(result);
                switch (type) {
                    case SYSTEM_RUN_STATE:  //014
                        //运行系统状态
                        systemRunStateSub = BlueResultParserUtil.getSystemRunState(result);
                        Timber.d("--------运行系统状态-------" + systemRunStateSub.toString());
                        break;

                    case SETTING_RAIN_PRECISION:  //121
                        //设置雨量计精度
                        settingRainPrecisionSub = BlueResultParserUtil.getRainPrecisionInfo(result);
                        Timber.d("--------设置雨量计精度-------" + settingRainPrecisionSub.toString());
                        setRianAccuryParameter.setRainAccury(String.valueOf(settingRainPrecisionSub.getPrecision()));
                        break;

                    case RAIN_STATION: //005
                        //雨量计开关
                        rainStationSub = BlueResultParserUtil.getRainStationInfo(result);
                        Timber.d("--------雨量计开关状态-------" + rainStationSub.getRainStation());
                        setSelectRainParameter.setRainSelect(rainStationSub.getRainStation().equals("开启"));
                        break;

                    case QUERY_OSMOMETER_PARAMETER:  //400
                        //查询数字式渗压计参数
                        queryOsmometerParameterSubInfo = BlueResultParserUtil.getQueryOsmometerParameter(result);
                        Timber.d("--------查询数字式渗压计参数-------" + queryOsmometerParameterSubInfo.toString());
                        break;

                    case DIGITAL_OSMOMETER_FUNCTION: //401
                        //开启/关闭数字式渗压计功能
                        digitalOsmometerFunctionSub = BlueResultParserUtil.getOsmoeterFunctionInfo(result);
                        Timber.d("--------开启/关闭数字式渗压计功能-------" + digitalOsmometerFunctionSub.toString());
                        if (digitalOsmometerFunctionSub.getOsmometerStatus() == 1) {
                            queryOsmometerParameterSubInfo.setOsmometerStatus("开启");
                        } else if (digitalOsmometerFunctionSub.getOsmometerStatus() == 2) {
                            queryOsmometerParameterSubInfo.setOsmometerStatus("关闭");
                        }
                        break;

                    case COLLECTOR_CONFIG://100
                        //获取采集器配置
                        collectorInfoSub = BlueResultParserUtil.getCollectorInfo(result);
                        Timber.d("--------获取采集器配置-------" + collectorInfoSub.toString());
                        send101Instruction(collectorInfoSub); //发送101指令
                        break;

                    case COLLECTOR_CHANNEL_SENSOR_PARAMETER: //101
                        Timber.d("--------101指令-------" + result);
                        mCollectorParamsInfoSub = BlueResultParserUtil.setCollectorParams(result);
                        mCollectorParamsInfoSubList.add(mCollectorParamsInfoSub);
                        Timber.d("size=" + mCollectorParamsInfoSubList.size() + "--------获取XX采集器YY通道的传感器参数-------" + mCollectorParamsInfoSub.toString());
                        break;

                    case RESTORE_FACTORY_SETTING:  //119
                        //恢复出厂设置
                        mHandler.sendEmptyMessage(Constants.BT_RECOVERY_SUCCESS);
                        Timber.d("--------恢复出厂设置-------" + type);
                        break;

                    case REBOOT_DEVICE:  //008
                        //重启设备
                        rebootDeviceSub = BlueResultParserUtil.getRebootDeviceMessage(result);
                        Timber.d("--------重启设备-------" + rebootDeviceSub.toString());
                        mHandler.sendEmptyMessage(Constants.MESSAGE_RESPONSE_REBOOT_DEVICE);
                        break;

                    case GET_ALL_SENSOR_CONFIG: {  //333
                        //所有配置信息
                        getAllSensorConfigInfo = BlueResultParserUtil.getAllBlueMessage(result);
                        Timber.d("--------所有配置信息-------" + getAllSensorConfigInfo.toString());

                        if (getAllSensorConfigInfo != null) {
                            collectorInfoSub = BlueResultParserUtil.getCollectorInfos(getAllSensorConfigInfo);
                        }
                        baseConfigInfoSub = BlueResultParserUtil.getBasicFromAllBlueMessage(result);
                        if (getAllSensorConfigInfo != null) {
                            switch (getAllSensorConfigInfo.getBaseConfig().getRainfallStation()) {
                                case RAIN_OPEN:
                                    setSelectRainParameter.setRainSelect(true);
                                    break;
                                case RAIN_CLOSE:
                                    setSelectRainParameter.setRainSelect(false);
                                    break;
                            }
                            setRianAccuryParameter.setRainAccury(String.valueOf(getAllSensorConfigInfo.getBaseConfig().getRainAccuracy() / 100));
                        }

                        switch (getAllSensorConfigInfo.getBaseConfig().getCollectorModel().name()) {
                            case "DS08":
                                collectorType = "02";
                                break;
                        }

                        if (!TextUtils.isEmpty(collectorType)) {
                            if (!isConnected) {
                                ToastUtil.showShortToast("蓝牙未连接");
                                return;
                            }

                            //根据采集器型号获取采集器配置 ##100 02
                            String collectorCommand = CommandManager.getInstance().getCommand(CommandType.COLLECTOR_CONFIG, null);
                            String collectorResult = collectorCommand.replace("\r\n", "") + collectorType + "\r\n";
                            Timber.d("发送获取采集器配置信息指令===" + collectorResult);
                            mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), collectorResult, true));
                        }
                        break;
                    }

                    case VERSION_MESSAGE:   //040
                        //获取版本信息
                        versionMessageSub = BlueResultParserUtil.getVersionMessage(result);
                        Timber.d("获取版本信息===" + versionMessageSub.toString());
                        break;

                    default:
                        break;
                }

                EventBus.getDefault().post("ParameterConfigFragment");

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
    }


    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case Constants.BT_CONNECT:
                    Timber.d("蓝牙已连接");
                    ToastUtil.showShortToast("蓝牙已连接");

                    if (isAdded()) {
                        mImgBluetooth.setImageDrawable(getResources().getDrawable(R.drawable.bar_item_blu_connect_yellow));
                    }

                    if (mLoadingDialog != null) {
                        mLoadingDialog.dismiss();
                    }

                    isConnected = true;
                    stopBluetooth = false;
                    MCloudApp.setIsBluetoothDeviceConnected(true);
                    //isLockStatus();
                    startBluAuthenticate();//蓝牙连接成功开始进行验证
                    hander.removeCallbacks(dismssConDialogRunnable);
                    mdBluetoothManager.stopScan();
                    break;

                case Constants.BT_DISCONNECTED:
                    if (isConnected) {
                        if (!isBlueMode) {
                            if (isAdded()) {
                                mImgBluetooth.setImageDrawable(getResources().getDrawable(R.drawable.bar_item_offline));
                            }
                        } else {
                            if (isAdded()) {
                                mImgBluetooth.setImageDrawable(getResources().getDrawable(R.drawable.bar_item_bt));
                            }
                        }

                        if (mLoadingDialog != null) {
                            mLoadingDialog.dismiss();
                        }
                        Timber.d("蓝牙连接已断开");
                        ToastUtil.showShortToast("蓝牙连接已断开!");

                        isConnected = false;
                        MCloudApp.setIsBluetoothDeviceConnected(false);
                        hander.removeCallbacks(dismssConDialogRunnable);
                        mdBluetoothManager.stopScan();
                        if (!stopBluetooth) {
                            Timber.d("ble 取消连接");
                            disconnectDevice();
                            //clearLocalStorage();
                            //断开蓝牙后重新连接
                            connectBluetooth();
                        }
                    }
                    break;

                case Constants.BT_MESSAGE_WRITE_SUCCESS:
                    ToastUtil.showShortToast("指令已发送");
                    break;

                case Constants.BT_MESSAGE_WRITE_FAIL:
                    ToastUtil.showShortToast("指令发送失败");
                    break;

                case Constants.BT_WRITE_TIME_OUT:
                    ToastUtil.showShortToast("指令发送超时");
                    break;

                case Constants.VERIFY_RESULT:
                    if (msg.obj.equals("1")) {
                        ToastUtil.showShortToast("蓝牙认证通过!");
                        sendDeviceStateComd();
                    } else {
                        ToastUtil.showShortToast("蓝牙认证失败!");
                        try {
                            Thread.sleep(1000);
                            disconnectDevice();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case Constants.MESSAGE_RESPONSE_TIME_OUT:
                    ToastUtil.showShortToast("消息等待响应超时！");
                    break;

                case Constants.REFRESH_RUN_STATE:
                    //loadWebView(runState);
                    break;

                case Constants.MESSAGE_RESPONSE_SAVE_SETTINGS_SUCCESS:
                    ToastUtil.showShortToast("设置信息已保存！");
                    //mWebView.loadUrl("javascript:restart()");
                    break;

                case Constants.BT_REQUEST_MTU_FAIL:
                    ToastUtil.showShortToast("MTU请求设置失败！");
                    break;

                case Constants.BT_SERVICE_FIND_FAIL:
                    ToastUtil.showShortToast("蓝牙服务发现失败！");
                    break;

                case Constants.BT_CHARACTERISTICS_FIND_FAIL:
                    ToastUtil.showShortToast("蓝牙特征读取失败！");
                    break;

                case Constants.BT_ENABLE_READ_FAIL:
                    ToastUtil.showShortToast("设置读取Descriptor失败！");
                    break;

                case Constants.BT_RECOVERY_SUCCESS:
                    ToastUtil.showShortToast("已恢复出厂设置！");
                    break;

                case Constants.MESSAGE_RESPONSE_REBOOT_DEVICE:
                    ToastUtil.showShortToast("已重启系统！");
                    break;

                case Constants.MESSAGE_LOCK_REBOOT_DEVICE:
                    ToastUtil.showShortToast("蓝牙通讯已就绪！");
                    sendDeviceStateComd();//unlock后发送指令
                    break;

                case Constants.MESSAGE_QUERY_OSMOMETER_PARAMETER:
                    //String result = (String) msg.obj;
                    //parserResult(result);
                    break;
                default:
                    break;
            }

            return false;
        }
    });


    /**
     * 发送蓝牙请求设备信息指令
     */
    public static void sendDeviceStateComd() {
        if (isConnected) {
            //获取所有配置  ##333
            final String allInfoCommand = CommandManager.getInstance().getCommand(CommandType.GET_ALL_SENSOR_CONFIG, null);
            //系统运行状态 ##014
            String runstateCommand = CommandManager.getInstance().getCommand(CommandType.SYSTEM_RUN_STATE, null);
            //查询数字式渗压计参数 ##400
            String shenyajiCommand = CommandManager.getInstance().getCommand(CommandType.QUERY_OSMOMETER_PARAMETER, null);
            //版本信息 ##040
            String versionCommand = CommandManager.getInstance().getCommand(CommandType.VERSION_MESSAGE, null);
            //获取服务器地址 ##200
            String serverCommand = CommandManager.getInstance().getCommand(CommandType.SERVER_ADDRESS, null);

            mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), allInfoCommand, true));
            Timber.d("发送所有配置指令===" + allInfoCommand);

            mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), runstateCommand, true));
            Timber.d("发送系统运行状态指令===" + runstateCommand);

            mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), shenyajiCommand, true));
            Timber.d("发送查询渗压计指令===" + shenyajiCommand);

            mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), versionCommand, true));
            Timber.d("发送版本信息指令===" + versionCommand);

        } else {
            if (!isConnected) {
                ToastUtil.showShortToast("蓝牙未连接");
            }
        }
    }

    /**
     * 蓝牙连接成功开始进行验证  lock
     */
    private void startBluAuthenticate() {
        String com = "##224," + SN + ",2\r\n";
        Message msg = new Message(UUID.randomUUID().toString(), com, true);
        mdBluetoothManager.writeMessage(msg);
        Timber.d("发送指令===" + com);
    }



    private void handleDeviceFind(BluetoothDeviceFindEventData eventData) {
        BluetoothDevice device = eventData.getNewDevice().getDevice();
        if (device.getName() == null)
            return;

        if (device.getName().contains(SN)) {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
            hander.removeCallbacks(dismssConDialogRunnable);
            mdBluetoothManager.stopScan();
            mdBluetoothManager.connectDevice(device, getActivity());
            if (null == mLoadingDialog.getDialog() || !mLoadingDialog.getDialog().isShowing()) {
                mLoadingDialog.showNoCancelDialog("正在连接设备：" + SN);
                hander.postDelayed(dismssConDialogRunnable, 10000);
            }
        }
    }


    /**
     * 发送101指令
     *
     * @param collectorInfoSub
     */
    private void send101Instruction(CollectorInfoSub collectorInfoSub) {
        for (int i = 0; i < collectorInfoSub.getAccessSum(); i++) {
            if (isConnected) {
                //##101XXYY\r\n：获取XX采集器YY通道的传感器参数
                String collectorCommand = CommandManager.getInstance().getCommand(CommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER, null);
                String count = com.shmedo.mcloudapp.util.StringUtil.formatTwo(i);
                String collectorResult = collectorCommand.replace("\r\n", "") + collectorType + count + "\r\n";
                mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), collectorResult, true));
                Timber.d("发送101采集器配置指令===" + collectorResult);

            } else {
                ToastUtil.showShortToast("蓝牙未连接");
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // 蓝牙已经开启
                if (resultCode == Activity.RESULT_OK) {
                    connectBluetooth();

                } else {
                    ToastUtil.showShortToast("蓝牙未启用");
                }
                break;

            default:
                break;
        }
    }

    /**
     * 是否切换连接模式
     */
    public void showChangeModle(String content, final String index) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder( getActivity())
                .title("温馨提示：")
                .content(content)
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        isConnected = false;
                        stopBluetooth = true;
                        MCloudApp.setIsBluetoothDeviceConnected(false);
                        disconnectDevice();

                        switch (index) {
                            case "1":
                                getActivity().finish();
                                break;

                            case "2":
                                mImgBluetooth.setImageDrawable(getResources().getDrawable(R.drawable.bar_item_bt));
                                break;
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    @Override
    public boolean onBackPressed() {
        if (isConnected) {
            showChangeModle(getResources().getString(R.string.finish_activity_disconnect_bluetooth_device), "1");

        } else {
            isConnected = false;
            stopBluetooth = true;
            MCloudApp.setIsBluetoothDeviceConnected(false);
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
            hander.removeCallbacks(dismssConDialogRunnable);
            mdBluetoothManager.stopScan();
            getActivity().finish();
        }

        return super.onBackPressed();
    }
}
