package com.shmedo.mcloudapp.ui.fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
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
import android.util.Log;
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
import com.shmedo.mcloudapp.entity.ble.MDevice;
import com.shmedo.mcloudapp.entity.ble.QueryOsmometerParameterSubInfo;
import com.shmedo.mcloudapp.entity.ble.RainStationSub;
import com.shmedo.mcloudapp.entity.ble.RebootDeviceSub;
import com.shmedo.mcloudapp.entity.ble.SettingRainPrecisionSub;
import com.shmedo.mcloudapp.entity.ble.SystemRunStateSub;
import com.shmedo.mcloudapp.entity.ble.VersionMessageSub;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.util.bleutil.BlueResultParserUtil;
import com.shmedo.mcloudapp.util.bleutil.ByteManagerUtil;
import com.shmedo.mcloudapp.util.bleutil.LogTag;
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

import static com.shmedo.das.das.cmd.CommandType.GET_ALL_SENSOR_CONFIG;
import static com.shmedo.das.das.cmd.CommandType.QUERY_OSMOMETER_PARAMETER;
import static com.shmedo.das.das.cmd.CommandType.SYSTEM_RUN_STATE;
import static com.shmedo.das.das.cmd.CommandType.VERSION_MESSAGE;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_CHARACTERISTICS_FIND_FAIL;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_CONNECT;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_DISCONNECTED;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_ENABLE_READ_FAIL;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_ENABLE_READ_SUCCESS;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_MESSAGE_WRITE_FAIL;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_MESSAGE_WRITE_SUCCESS;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_RECOVERY_SUCCESS;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_REQUEST_MTU_FAIL;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_SERVICE_FIND_FAIL;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_WRITE_TIME_OUT;
import static com.shmedo.mcloudapp.util.bleutil.Constants.MESSAGE_LOCK_REBOOT_DEVICE;
import static com.shmedo.mcloudapp.util.bleutil.Constants.MESSAGE_QUERY_OSMOMETER_PARAMETER;
import static com.shmedo.mcloudapp.util.bleutil.Constants.MESSAGE_RESPONSE_REBOOT_DEVICE;
import static com.shmedo.mcloudapp.util.bleutil.Constants.MESSAGE_RESPONSE_SAVE_SETTINGS_SUCCESS;
import static com.shmedo.mcloudapp.util.bleutil.Constants.MESSAGE_RESPONSE_TIME_OUT;
import static com.shmedo.mcloudapp.util.bleutil.Constants.REFRESH_RUN_STATE;
import static com.shmedo.mcloudapp.util.bleutil.Constants.VERIFY_RESULT;

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

    ParameterConfigFragment parameterConfigFragment;//参数配置
    QueryDataFragment queryDataFragment;        //查询数据
    DeviceDetailsFragment deviceDetailsFragment;//设备详情
    AdvanceSetFragment advanceSetFragment;      //高级设置

    private Unbinder unbinder;
    private LoadingDialog mLoadingDialog;
    private Handler hander;
    //当前模式是否是蓝牙模式
    private boolean isBluModle = true;
    public static boolean isConneted = false;
    private boolean stopBluetooth = false;
    public static MdBluetoothManager mdBluetoothManager;
    public static BluetoothAdapter mBluetoothAdapter;
    private List<MDevice> list = new ArrayList<>();
    private MaterialDialog mMaterialDialog;
    private MaterialDialog.Builder mBuilder;
    private String SN = "";
    private String currentMessageId = "";
    private String deviceInfo;

    public static QueryOsmometerParameterSubInfo mFuncSubInfo;
    public static SystemRunStateSub mSystemRunStateSub;
    public static DigitalOsmometerFunctionSub mOsmometerFunctionSub;
    public static RainStationSub mRainStationSub;
    public static SettingRainPrecisionSub settingRainPrecisionSub;
    public static RebootDeviceSub mRebootDeviceSub;
    public static CollectorInfoSub mCollectorInfoSub;
    public static List<CollectorSensorParamsInfoSub> mCollectorParamsInfoSubList = new ArrayList<>();
    public static CollectorSensorParamsInfoSub mCollectorParamsInfoSub;
    public static GetAllSensorConfigInfo mAllSensorConfigInfo;
    public static BaseConfigInfoSub mInfoSub;
    public static VersionMessageSub versionMessageSub;
    public static SetRainAccuryPage.SetRianAccuryParameter rainPage = new SetRainAccuryPage.SetRianAccuryParameter();
    public static SetRainSelectPage.SetSelectRainParameter setRainSelect = new SetRainSelectPage.SetSelectRainParameter();
    public static DeviceLockStatusSub deviceLockStatusSub;
    public static String collectorType = "";
    public static String lockStatus = "";
    //private boolean deviceTrue;

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

        if (Objects.requireNonNull(getActivity()).getIntent().getExtras().containsKey("device")) {
            //从MainActivity蓝牙列表跳转
            String name = getActivity().getIntent().getStringExtra("device");
            //deviceTrue = getActivity().getIntent().getBooleanExtra("deviceTrue", false);
            String deviceName = name.substring(3, name.length());
            deviceInfo = "MEDO," + deviceName + ",DAS";
            Timber.d("从蓝牙列表跳转,deviceInfo=" + deviceInfo);

        } else if (getActivity().getIntent().getExtras().containsKey("inputDevice")) {
            String deviceName = getActivity().getIntent().getStringExtra("inputDevice");
            String deviceType = getActivity().getIntent().getStringExtra("deviceType");
            deviceInfo = "MEDO," + deviceName + "," + deviceType;
            Timber.d("手动输入, deviceInfo=" + deviceInfo);

        } else if (getActivity().getIntent().getExtras().containsKey("ScanDevice")) {
            deviceInfo = getActivity().getIntent().getStringExtra("ScanDevice");
            Timber.d("扫一扫, deviceInfo=" + deviceInfo);
        }

        String[] scanData = deviceInfo.split(",");
        SN = scanData[1];

        connectBluetooth();
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
        setTabState(mTvParameter, R.drawable.szxd_wxz, getColor(R.color.font_main_79));
        setTabState(mTvQueryData, R.drawable.yxzt_wxz, getColor(R.color.font_main_79));
        setTabState(mTvDeviceDetails, R.drawable.xtgj_wxz, getColor(R.color.font_main_79));
        setTabState(mTvHighsetting, R.drawable.gjpz_wxz, getColor(R.color.font_main_79));

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


    @OnClick({R.id.back, R.id.img_bluetooth})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                if (isConneted) {
                    showChangeModle(getResources().getString(R.string.finish), "1");
                } else {
                    stopBluetooth = true;
                    hander.removeCallbacks(dismssDialogRunnable);
                    hander.removeCallbacks(dismssConDialogRunnable);
                    mdBluetoothManager.stopScan();
                    disconnectDevice();
                    getActivity().finish();
                }
                break;

            case R.id.img_bluetooth:
                if (isConneted) {
                    showChangeModle(getResources().getString(R.string.blue_model), "2");
                } else {
                    connectBluetooth();
                }
                break;
        }
    }

    private void connectBluetooth() {
        if (isBluModle && !isConneted) {
            initBluetooth();
            if (null != mBluetoothAdapter && mBluetoothAdapter.isEnabled()) {

                if (null != list && list.size() > 0) {
                    list.clear();
                }
                //deviceTrue为true，说明是从MainActivity中传过来的
                if (null == mLoadingDialog.getDialog() || !mLoadingDialog.getDialog().isShowing()) {
                    mLoadingDialog.showCancelDialog("正在连接蓝牙：" + SN);
                    hander.postDelayed(dismssDialogRunnable, 5000);
                }

            }
        } else if (isBluModle && isConneted) {
            showChangeModle(getResources().getString(R.string.blue_model), "2");
        } else {
            if (null != list && list.size() > 0) {
                list.clear();
            }
            initBluetooth();
        }
    }

    private void initBluetooth() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = Objects.requireNonNull(bluetoothManager).getAdapter();
        MdBluetoothManager.init(mBluetoothAdapter, bluetoothManager);
        mdBluetoothManager = MdBluetoothManager.getInstance();
        mdBluetoothManager.setEventHandler(new MdBluetoothEventHandler());

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        } else {
            mdBluetoothManager.scanDevice(20, getActivity());
        }
    }

    //自动连接蓝牙
    private void initBluetoothAdapter() {

        for (int i = 0; i < list.size(); i++) {
            if ((list.get(i).getDevice().getName()).contains(SN)) {
                //如果是连接状态，断开，重新连接

                mdBluetoothManager.connectDevice(list.get(i).getDevice(), getActivity());
                if (null != mLoadingDialog) {
                    if (getActivity().hasWindowFocus()) {
                        mLoadingDialog.showNoCancelDialog("正在连接..." + SN);
                    }
                }
                hander.postDelayed(dismssConDialogRunnable, 10000);
                break;

            }
        }
        if (mdBluetoothManager.connected()) {
            mdBluetoothManager.stopScan();
        }
    }

    private Runnable dismssDialogRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
                initBluetoothAdapter();
            }
            // disconnectDevice();
        }
    };
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

    /**
     * 是否切换连接模式
     */
    public void showChangeModle(String content, final String index) {
        mBuilder = new MaterialDialog.Builder(getActivity());
        mBuilder.title("温馨提示：")
                .content(content)
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消");
        mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
        mBuilder.onAny(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                if (which == DialogAction.POSITIVE) {
                    switch (index) {
                        case "1":
                            isConneted = false;
                            stopBluetooth = true;
                            disconnectDevice();
                            getActivity().finish();
                            break;
                        case "2":
                            isConneted = false;
                            stopBluetooth = true;
                            disconnectDevice();
                            mImgBluetooth.setImageDrawable(getResources().getDrawable(R.drawable.bar_item_bt));
                            mMaterialDialog.dismiss();
                            break;
                    }

                } else if (which == DialogAction.NEGATIVE) {
                    if (mBluetoothAdapter != null) {
                        mBluetoothAdapter.isEnabled();
                    }
                    mMaterialDialog.dismiss();
                }
            }
        });
    }

    private class MdBluetoothEventHandler implements BluetoothEventHandler {
        @Override
        public void handle(final BluetoothEvent event) {
            switch (event.getEventType()) {
                case DEVICE_FIND:
                    Timber.d("==DEVICE_FIND===" + ((BluetoothDeviceFindEventData) event.getEventData()).getNewDevice().getDevice().getName());
                    handleDeviceFind((BluetoothDeviceFindEventData) event.getEventData());
                    break;

                case CONNECTED:
                    ByteManagerUtil.init(new MyOnBytePackage());
                    mHandler.sendEmptyMessage(BT_CONNECT);
                    break;

                case DISCONNECTED:
                    mHandler.sendEmptyMessage(BT_DISCONNECTED);
                    break;

                case REQUEST_MTU_FAIL:
                    Log.e(LogTag.ERROR_TAG, "MTU请求设置失败");
                    //mHandler.sendEmptyMessage(BT_REQUEST_MTU_FAIL);
                    break;

                case SERVICE_FIND_FAIL:
                    Log.e(LogTag.ERROR_TAG, "蓝牙服务发现失败");
                    mHandler.sendEmptyMessage(BT_SERVICE_FIND_FAIL);
                    break;

                case CHARACTERISTICS_FIND_FAIL:
                    Log.e(LogTag.ERROR_TAG, "特征读取失败");
                    mHandler.sendEmptyMessage(BT_CHARACTERISTICS_FIND_FAIL);
                    break;

                case ENABLE_READ_SUCCESS:
                    Log.i(LogTag.INFO_TAG, "设置读取Descriptor成功");
                    mHandler.sendEmptyMessage(BT_ENABLE_READ_SUCCESS);
                    break;

                case ENABLE_READ_FAIL:
                    Log.e(LogTag.ERROR_TAG, "设置读取Descriptor失败");
                    mHandler.sendEmptyMessage(BT_ENABLE_READ_FAIL);
                    break;

                case WRITE_TIME_OUT:
                    Log.e(LogTag.ERROR_TAG, "写入等待超时");
                    disconnectDevice();
                    mHandler.sendEmptyMessage(BT_WRITE_TIME_OUT);
                    break;

                case MESSAGE_WRITE_SUCCESS: {
                    //Log.i(LogTag.INFO_TAG, "消息写入成功");
                    mHandler.sendEmptyMessage(BT_MESSAGE_WRITE_SUCCESS);
                    try {
                        currentMessageId = ((Message) event.getEventData()).getMessageID();
                        //Log.i(LogTag.INFO_TAG, "消息id===" + ((Message) event.getEventData()).getMessageID());
                        String msg = ((Message) event.getEventData()).getResponseMessage();
                        byte[] data = (byte[]) msg.getBytes();

                        if (data != null && data.length > 0) {
                            ByteManagerUtil.getInstance().writeByte(data);
                        }

                    } catch (Exception ex) {
                        Log.e(LogTag.ERROR_TAG, ex.getMessage(), ex);
                    }

                    break;
                }
                case MESSAGE_RESPONSE_TIME_OUT:
                    Log.e(LogTag.ERROR_TAG, "消息等待响应超时");
                    mHandler.sendEmptyMessage(MESSAGE_RESPONSE_TIME_OUT);
                    disconnectDevice();
                    break;

                case MESSAGE_WRITE_FAIL:
                    Log.e(LogTag.ERROR_TAG, "消息写入失败");
                    mHandler.sendEmptyMessage(BT_MESSAGE_WRITE_FAIL);
                    break;

                case RESPONSE_WITH_NO_MESSAGE:
                    try {
                        byte[] data = (byte[]) event.getEventData();
                        if (data != null && data.length > 0) {
                            ByteManagerUtil.getInstance().writeByte(data);
                        }

                    } catch (Exception ex) {
                        Log.e(LogTag.ERROR_TAG, ex.getMessage(), ex);
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
                    message.what = MESSAGE_LOCK_REBOOT_DEVICE;
                    mHandler.sendMessage(message);
                    return;
                } else if (str.equals("Equipment Verify OK.\r\n")) {
                    android.os.Message message = new android.os.Message();
                    message.what = MESSAGE_LOCK_REBOOT_DEVICE;
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
                                Log.i(LogTag.INFO_TAG, "===-发送指令===" + com);
                                return;
                            }
                        } catch (Exception e) {
                            //CommonUtil.handlerException(MainActivity.this, e);
                        }
                    }
                } else if (str.startsWith("$$223")) {
                    String[] verifyReult = str.substring(0, str.length() - 2).split(",");
                    android.os.Message message = new android.os.Message();
                    message.what = VERIFY_RESULT;
                    message.obj = verifyReult[1];
                    mHandler.sendMessage(message);
                    Log.i(LogTag.INFO_TAG, "认证结果===" + verifyReult[1]);
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
                Log.e(LogTag.INFO_TAG, ex.getMessage(), ex);
            }

        }
    }


    private void parserResult(String result) {
        if (result.equals("Please verify the equipment.\r\n")) {
            android.os.Message message = new android.os.Message();
            message.what = VERIFY_RESULT;
            message.obj = "0";
            mHandler.sendMessage(message);
        } else {
            try {
                CommandType type = StringUtil.extractCommandType(result);
                Log.i(LogTag.INFO_TAG, "--------返回指令结果-------" + result);
                switch (type) {
                    case SYSTEM_RUN_STATE: {  //014
                        //运行系统状态
                        mSystemRunStateSub = BlueResultParserUtil.getSystemRunState(result);
                        Log.i(LogTag.INFO_TAG, "--------运行系统状态-------" + mSystemRunStateSub.toString());
                        break;
                    }
                    case SETTING_RAIN_PRECISION: {  //121
                        //设置雨量计精度
                        settingRainPrecisionSub = BlueResultParserUtil.getRainPrecisionInfo(result);
                        Log.i(LogTag.INFO_TAG, "--------设置雨量计精度-------" + settingRainPrecisionSub.toString());
                        rainPage.setRainAccury(String.valueOf(settingRainPrecisionSub.getPrecision()));
                        break;
                    }
                    case RAIN_STATION: { //005
                        //雨量计开关
                        mRainStationSub = BlueResultParserUtil.getRainStationInfo(result);
                        Log.i(LogTag.INFO_TAG, "--------设置雨量计精度-------" + mRainStationSub.getRainStation());
                        if (mRainStationSub.getRainStation().equals("关闭")) {
                            setRainSelect.setRainSelect(false);
                        } else if (mRainStationSub.getRainStation().equals("开启")) {
                            setRainSelect.setRainSelect(true);
                        }
                        break;
                    }
                    case QUERY_OSMOMETER_PARAMETER: { //400
                        //查询数字式渗压计参数
                        mFuncSubInfo = BlueResultParserUtil.getQueryOsmometerParameter(result);
                        Log.i(LogTag.INFO_TAG, "--------查询数字式渗压计参数-------" + mFuncSubInfo.toString());
                        break;
                    }
                    case DIGITAL_OSMOMETER_FUNCTION: //401
                        //开启/关闭数字式渗压计功能
                        mOsmometerFunctionSub = BlueResultParserUtil.getOsmoeterFunctionInfo(result);
                        Log.i(LogTag.INFO_TAG, "--------开启/关闭数字式渗压计功能-------" + mOsmometerFunctionSub.toString());
                        if (mOsmometerFunctionSub.getOsmometerStatus() == 1) {
                            mFuncSubInfo.setOsmometerStatus("开启");
                        } else if (mOsmometerFunctionSub.getOsmometerStatus() == 2) {
                            mFuncSubInfo.setOsmometerStatus("关闭");
                        }
                        break;
                    case COLLECTOR_CONFIG:  //100
                        //获取采集器配置
                        mCollectorInfoSub = BlueResultParserUtil.getCollectorInfo(result);
                        Log.i(LogTag.INFO_TAG, "--------获取采集器配置-------" + mCollectorInfoSub.toString());
                        send101Instruction(mCollectorInfoSub); //发送101指令
                        break;
                    case COLLECTOR_CHANNEL_SENSOR_PARAMETER: //101
                        Log.i(LogTag.INFO_TAG, "--------101指令-------" + result);
                        mCollectorParamsInfoSub = BlueResultParserUtil.setCollectorParams(result);
                        mCollectorParamsInfoSubList.add(mCollectorParamsInfoSub);
                        Log.i(LogTag.INFO_TAG, "size=" + mCollectorParamsInfoSubList.size() + "--------获取XX采集器YY通道的传感器参数-------" + mCollectorParamsInfoSub.toString());
                        break;
                    case RESTORE_FACTORY_SETTING:  //119
                        //恢复出厂设置
                        mHandler.sendEmptyMessage(BT_RECOVERY_SUCCESS);
                        Log.i(LogTag.INFO_TAG, "--------恢复出厂设置-------" + type);
                        break;
                    case REBOOT_DEVICE: { //008
                        //重启设备
                        mRebootDeviceSub = BlueResultParserUtil.getRebootDeviceMessage(result);
                        Log.i(LogTag.INFO_TAG, "--------重启设备-------" + mRebootDeviceSub.toString());
                        mHandler.sendEmptyMessage(MESSAGE_RESPONSE_REBOOT_DEVICE);
                        break;
                    }
                    case GET_ALL_SENSOR_CONFIG: {   //333
                        //所有配置信息
                        mAllSensorConfigInfo = BlueResultParserUtil.getAllBlueMessage(result);
                        Log.i(LogTag.INFO_TAG, "--------所有配置信息-------" + mAllSensorConfigInfo.toString());
                        if (mAllSensorConfigInfo != null) {
                            mCollectorInfoSub = BlueResultParserUtil.getCollectorInfos(mAllSensorConfigInfo);
                        }
                        mInfoSub = BlueResultParserUtil.getBasicFromAllBlueMessage(result);
                        if (mAllSensorConfigInfo != null) {
                            switch (mAllSensorConfigInfo.getBaseConfig().getRainfallStation()) {
                                case RAIN_OPEN:
                                    setRainSelect.setRainSelect(true);
                                    break;
                                case RAIN_CLOSE:
                                    setRainSelect.setRainSelect(false);
                                    break;
                            }
                            rainPage.setRainAccury(String.valueOf(mAllSensorConfigInfo.getBaseConfig().getRainAccuracy() / 100));
                        }
                        switch (mAllSensorConfigInfo.getBaseConfig().getCollectorModel().name()) {
                            case "DS08":
                                collectorType = "02";
                                break;
                        }
                        if (!collectorType.equals("")) {
                            if (isConneted) {
                                //根据采集器型号获取采集器配置 ##100 02
                                String collectorCommand = CommandManager.getInstance().getCommand(CommandType.COLLECTOR_CONFIG, null);
                                String collectorResult = collectorCommand.replace("\r\n", "") + collectorType + "\r\n";
                                Log.i(LogTag.INFO_TAG, "发送采集器配置指令===" + collectorResult);
                                mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), collectorResult, true));
                            } else {
                                ToastUtil.showSToast("蓝牙未连接");
                            }
                        }
                        break;
                    }
                    case VERSION_MESSAGE: {  //040
                        //获取版本信息
                        versionMessageSub = BlueResultParserUtil.getVersionMessage(result);
                        Log.i(LogTag.INFO_TAG, "获取版本信息===" + versionMessageSub.toString());
                        break;
                    }
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
                case BT_CONNECT:
                    if (isAdded()) {
                        mImgBluetooth.setImageDrawable(
                                getResources().getDrawable(R.drawable.bar_item_blu_connect_yellow));
                    }
                    Log.i(LogTag.INFO_TAG, "蓝牙连接成功");
                    if (mLoadingDialog != null) {
                        mLoadingDialog.dismiss();
                    }
                    isConneted = true;
                    stopBluetooth = false;
                    ToastUtil.showSToast("蓝牙连接成功");
                    //isLockStatus();
                    startBluAuthenticate();//蓝牙连接成功开始进行验证
                    hander.removeCallbacks(dismssDialogRunnable);
                    hander.removeCallbacks(dismssConDialogRunnable);
                    mdBluetoothManager.stopScan();
                    break;
                case BT_DISCONNECTED:
                    if (isConneted) {
                        isConneted = false;
                        if (!isBluModle) {
                            if (isAdded()) {
                                mImgBluetooth.setImageDrawable(
                                        getResources().getDrawable(R.drawable.bar_item_offline));
                            }
                        } else {
                            if (isAdded()) {
                                mImgBluetooth.setImageDrawable(
                                        getResources().getDrawable(R.drawable.bar_item_bt));
                            }
                        }
                        Log.i(LogTag.INFO_TAG, "蓝牙连接已断开");
                        hander.removeCallbacks(dismssDialogRunnable);
                        hander.removeCallbacks(dismssConDialogRunnable);
                        mdBluetoothManager.stopScan();
                        ToastUtil.showSToast("蓝牙连接已断开!");
                        if (!stopBluetooth) {
                            Log.i(LogTag.INFO_TAG, "=======ble 取消连接=====2");
                            disconnectDevice();
                            //clearLocalStorage();
                            //断开蓝牙后重新连接
                            reStartBluetooth();
                        }
                        if (mLoadingDialog != null) {
                            mLoadingDialog.dismiss();
                        }

                    }
                    break;
                case BT_MESSAGE_WRITE_SUCCESS:
                    ToastUtil.showSToast("蓝牙发送指令成功");
                    break;
                case BT_MESSAGE_WRITE_FAIL:
                    ToastUtil.showSToast("蓝牙发送指令失败");
                    break;
                case BT_WRITE_TIME_OUT:
                    ToastUtil.showSToast("蓝牙发送指令超时");
                    break;
                case VERIFY_RESULT:
                    if (msg.obj.equals("1")) {
                        ToastUtil.showSToast("蓝牙认证通过!");
                        sendDeviceStateComd();
                    } else {
                        ToastUtil.showSToast("蓝牙认证失败!");
                        try {
                            Thread.sleep(1000);
                            disconnectDevice();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                    break;
                case MESSAGE_RESPONSE_TIME_OUT:
                    ToastUtil.showSToast("消息等待响应超时！");
                    break;
                case REFRESH_RUN_STATE:
                    //loadWebView(runState);
                    break;
                case MESSAGE_RESPONSE_SAVE_SETTINGS_SUCCESS:
                    ToastUtil.showSToast("保存设置信息成功！");
                    //mWebView.loadUrl("javascript:restart()");
                    break;
                case BT_REQUEST_MTU_FAIL:
                    ToastUtil.showSToast("MTU请求设置失败！");
                    break;
                case BT_SERVICE_FIND_FAIL:
                    ToastUtil.showSToast("蓝牙服务发现失败！");
                    break;
                case BT_CHARACTERISTICS_FIND_FAIL:
                    ToastUtil.showSToast("蓝牙特征读取失败！");
                    break;
                case BT_ENABLE_READ_FAIL:
                    ToastUtil.showSToast("设置读取Descriptor失败！");
                    break;
                case BT_RECOVERY_SUCCESS:
                    ToastUtil.showSToast("恢复出厂设置成功！");
                    break;
                case MESSAGE_RESPONSE_REBOOT_DEVICE:
                    ToastUtil.showSToast("重启系统成功！");
                    break;
                case MESSAGE_LOCK_REBOOT_DEVICE:
                    ToastUtil.showSToast("蓝牙通讯已就绪！");
                    sendDeviceStateComd();//unlock后发送指令
                    break;
                case MESSAGE_QUERY_OSMOMETER_PARAMETER:
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
     * 重新开启蓝牙
     */
    private void reStartBluetooth() {
        if (!isConneted) {
            initBluetooth();
            if (null != mBluetoothAdapter && mBluetoothAdapter.isEnabled()) {

                if (null != list && list.size() > 0) {
                    list.clear();
                }
                if (null == mLoadingDialog.getDialog() || !mLoadingDialog.getDialog().isShowing()) {
                    if (getActivity().hasWindowFocus()) {
                        mLoadingDialog.showNoCancelDialog("正在连接蓝牙...");
                    }
                    hander.postDelayed(dismssDialogRunnable, 5000);
                }

            }
        }
        initBluetoothAdapter();
    }

    /**
     * 发送蓝牙请求设备信息指令
     */
    public static void sendDeviceStateComd() {
        if (isConneted) {
            //获取所有配置  ##333
            final String allInfoCommand = CommandManager.getInstance().getCommand(GET_ALL_SENSOR_CONFIG, null);
            //系统运行状态 ##014
            String runstateCommand = CommandManager.getInstance().getCommand(SYSTEM_RUN_STATE, null);
            //查询数字式渗压计参数 ##400
            String shenyajiCommand = CommandManager.getInstance().getCommand(QUERY_OSMOMETER_PARAMETER, null);
            //版本信息 ##040
            String versionCommand = CommandManager.getInstance().getCommand(VERSION_MESSAGE, null);
            //获取服务器地址 ##200
            String serverCommand = CommandManager.getInstance().getCommand(CommandType.SERVER_ADDRESS, null);

            mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), allInfoCommand, true));
            Log.i(LogTag.INFO_TAG, "发送所有配置指令===" + allInfoCommand);
            mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), runstateCommand, true));
            Log.i(LogTag.INFO_TAG, "发送系统运行状态指令===" + runstateCommand);
            mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), shenyajiCommand, true));
            Log.i(LogTag.INFO_TAG, "发送查询渗压计指令===" + shenyajiCommand);
            mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), versionCommand, true));
            Log.i(LogTag.INFO_TAG, "发送版本信息指令===" + versionCommand);
        } else {
            if (!isConneted) {
                ToastUtil.showSToast("蓝牙未连接");
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
        Log.i(LogTag.INFO_TAG, "发送指令===" + com);

    }


    ///**
    // * 判断设备锁的状态
    // */
    //private void isLockStatus() {
    //    String com = "##224," + SN + ",2\r\n";
    //    Message msg = new Message(UUID.randomUUID().toString(), com, true);
    //    mdBluetoothManager.writeMessage(msg);
    //}
    private void handleDeviceFind(BluetoothDeviceFindEventData eventData) {
        if (list.contains(eventData.getNewDevice()) ||
                eventData.getNewDevice().getDevice().getName() == null) {
            return;
        }
        if (null != list && list.size() > 0) {
            for (MDevice mDevice : list) {
                if (eventData.getNewDevice()
                        .getDevice()
                        .getName()
                        .equals(mDevice.getDevice().getName())) {
                    return;
                }
            }
        }
        list.add(eventData.getNewDevice());
    }


    /**
     * 发送101指令
     *
     * @param collectorInfoSub
     */
    private void send101Instruction(CollectorInfoSub collectorInfoSub) {

        for (int i = 0; i < collectorInfoSub.getAccessSum(); i++) {
            if (isConneted) {
                //##101XXYY\r\n：获取XX采集器YY通道的传感器参数
                String collectorCommand = CommandManager.getInstance().getCommand(CommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER, null);
                String count = com.shmedo.mcloudapp.util.StringUtil.formatTwo(i);
                String collectorResult = collectorCommand.replace("\r\n", "") + collectorType + count + "\r\n";
                Log.i(LogTag.INFO_TAG, "发送101采集器配置指令===" + collectorResult);
                mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), collectorResult, true));
            } else {
                ToastUtil.showSToast("蓝牙未连接");
            }
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_ENABLE_BT:
                // 蓝牙已经开启
                if (resultCode == Activity.RESULT_OK) {
                    mdBluetoothManager.scanDevice(20, getActivity());

                } else {
                    ToastUtil.showSToast("蓝牙未启用");
                    onBackPressed();
                }
                break;
            default:
                break;
        }

    }


    @Override
    public boolean onBackPressed() {
        if (isConneted) {
            mBuilder = new MaterialDialog.Builder(getActivity());
            mBuilder.title("温馨提示：")
                    .content("当前设备正处于蓝牙交互中，是否确认退出？")
                    .contentColor(Color.parseColor("#000000"))
                    .canceledOnTouchOutside(false)
                    .positiveText("确定")
                    .negativeText("取消");
            mMaterialDialog = mBuilder.build();
            mMaterialDialog.show();
            mBuilder.onAny(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(
                        @NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    if (which == DialogAction.POSITIVE) {
                        stopBluetooth = true;

                        disconnectDevice();
                        getActivity().finish();
                    } else if (which == DialogAction.NEGATIVE) {

                        mMaterialDialog.dismiss();
                    }
                }
            });

        } else {
            stopBluetooth = true;
            hander.removeCallbacks(dismssDialogRunnable);
            hander.removeCallbacks(dismssConDialogRunnable);
            mdBluetoothManager.stopScan();
            disconnectDevice();
            getActivity().finish();
        }
        return super.onBackPressed();
    }
}
