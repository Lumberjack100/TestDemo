package com.shmedo.mcloudapp.ui.activity;

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
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.das.utils.DesUtil;
import com.shmedo.das.utils.OnBytePackage;
import com.shmedo.das.utils.StringUtil;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.bluetooth.BluetoothDeviceFindEventData;
import com.shmedo.mcloudapp.bluetooth.BluetoothEvent;
import com.shmedo.mcloudapp.bluetooth.BluetoothEventHandler;
import com.shmedo.mcloudapp.bluetooth.MdBluetoothManager;
import com.shmedo.mcloudapp.bluetooth.Message;
import com.shmedo.mcloudapp.entity.event.BluetoothStateEvent;
import com.shmedo.mcloudapp.model.Extras;
import com.shmedo.mcloudapp.ui.fragment.ADMEHomeFragment;
import com.shmedo.mcloudapp.ui.fragment.DeviceDetailsFragment;
import com.shmedo.mcloudapp.ui.fragment.QueryDataFragment;
import com.shmedo.mcloudapp.util.bleutil.ByteManagerUtil;
import com.shmedo.mcloudapp.util.bleutil.Constants;
import com.shmedo.mcloudapp.util.common.HandleBackUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp
 * 创建者:   gonghe
 * 创建时间:  2019-10-21
 * 描述：   ADME 设备配置页面
 */
public class ConfigADMEActivity extends BaseActivity {

    private static final int REQUEST_ENABLE_BT = 0x001;

    @BindView(R.id.tv_save)
    TextView mTvSave;

    @BindView(R.id.tv_parameter)
    TextView mTvParameter;

    @BindView(R.id.tv_query_data)
    TextView mTvQueryData;

    @BindView(R.id.tv_device_details)
    TextView mTvDeviceDetails;

    @BindView(R.id.tv_highsetting)
    TextView mTvHighsetting;

    private ADMEHomeFragment admeHomeFragment;
    private QueryDataFragment queryDataFragment;        //查询数据
    private DeviceDetailsFragment deviceDetailsFragment;//设备详情

    private MdBluetoothManager mdBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler hander;

    private boolean isBlueConnected = false;//蓝牙设备是否连接
    private boolean isAutoConnectBlue = true;//是否自动连接蓝牙
    public boolean isNeedSaveConfig = false;//如果对设备进行了设置，需要在用户退出页面前，提醒用户进行保存操作

    private String SN = "";
    private String deviceInfo;
    private String macAddress;


    public static void startActivity(Context context, String deviceInfo) {
        Intent intent = new Intent(context, ConfigADMEActivity.class);
        intent.putExtra(Extras.CUR_DEVICE_NAME, deviceInfo);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, String deviceInfo, String macAddress) {
        Intent intent = new Intent(context, ConfigADMEActivity.class);
        intent.putExtra(Extras.CUR_DEVICE_NAME, deviceInfo);
        intent.putExtra(Extras.DEVICE_MAC_ADDRESS, macAddress);
        context.startActivity(intent);
    }


    private Runnable dismssDialogRunnable = new Runnable() {
        @Override
        public void run() {
            dismissLoadingDialog();
        }
    };


    @Override
    protected int initContentView() {
        return R.layout.activity_config_adme;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initBluetooth();
        getIntentData();
    }


    private void getIntentData() {
        Intent intent = getIntent();
        if (intent.getExtras().containsKey(Extras.DEVICE_MAC_ADDRESS)) {
            macAddress = intent.getStringExtra(Extras.DEVICE_MAC_ADDRESS);
        }

        if (intent.getExtras().containsKey(Extras.CUR_DEVICE_NAME)) {
            deviceInfo = intent.getStringExtra(Extras.CUR_DEVICE_NAME);
            String[] scanData = deviceInfo.split(",");
            SN = scanData[1];

            findAndConnectBleDevice();
        }
    }

    private void initBluetooth() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = Objects.requireNonNull(bluetoothManager).getAdapter();
        MdBluetoothManager.init(mBluetoothAdapter, bluetoothManager);
        mdBluetoothManager = MdBluetoothManager.getInstance();
        mdBluetoothManager.setEventHandler(new MdBluetoothEventHandler());
    }


    private void initView() {
        hander = new Handler();
        mTvHighsetting.setVisibility(View.GONE);
        setDefaultFragment();
    }

    /**
     * set the default Fragment
     */
    private void setDefaultFragment() {
        switchFrgment(0);
        //set the defalut tab state
        setTabState(mTvParameter, R.drawable.szxd, getResources().getColor(R.color.colorPrimary));
    }


    @OnClick({R.id.back, R.id.tv_save, R.id.tv_parameter, R.id.tv_query_data, R.id.tv_device_details})
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;

            case R.id.tv_save:
                sendSaveConfigCommand();
                break;

//            case R.id.img_bluetooth:
//                if (isBlueConnected) {
//                    showChangeModle(getResources().getString(R.string.disconnect_bluetooth_device), "2");
//                } else {
//                    findAndConnectBleDevice();
//                }
//                break;

            case R.id.tv_parameter:
                resetTabState();//reset the tab state
                setTabState(mTvParameter, R.drawable.szxd, getResources().getColor(R.color.colorPrimary));
                switchFrgment(0);
                break;

            case R.id.tv_query_data:
                resetTabState();//reset the tab state
                setTabState(mTvQueryData, R.drawable.yxzt, getResources().getColor(R.color.colorPrimary));
                switchFrgment(1);
                break;

            case R.id.tv_device_details:
                resetTabState();//reset the tab state
                setTabState(mTvDeviceDetails, R.drawable.xtgj, getResources().getColor(R.color.colorPrimary));
                switchFrgment(2);
                break;
        }
    }


    /**
     * 扫描蓝牙设备，主要用来判断要连接的设备是否能被搜索到
     */
    private void startDiscoveryDevice() {
        //蓝牙未打开
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        //蓝牙已打开时，开始扫描蓝牙设备
        mdBluetoothManager.scanDevice(20, this);
        if (null != mBluetoothAdapter && mBluetoothAdapter.isEnabled()) {
            showLoadingDialog("正在搜索设备：" + SN);
            hander.postDelayed(dismssDialogRunnable, 10000);
        }
    }

    /**
     * 搜索并连接指定的蓝牙设备
     */
    public void findAndConnectBleDevice() {
        //通过蓝牙设备列表页面跳转过来时，直接连接设备
        if (!TextUtils.isEmpty(macAddress)) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);
            if (device != null) {
                doConnect(device);
            }
            return;
        }

        //搜索附近蓝牙设备，避免指定的设备不在蓝牙范围内
        startDiscoveryDevice();
    }


    /**
     * 处理发现的蓝牙设备
     */
    private void handleDeviceFind(BluetoothDeviceFindEventData eventData) {
        BluetoothDevice device = eventData.getNewDevice().getDevice();
        if (device.getName() == null)
            return;

        if (device.getName().contains(SN)) {
            dismissLoadingDialog();
            hander.removeCallbacks(dismssDialogRunnable);
            doConnect(device);
        }
    }

    /**
     * ble 建立连接
     */
    private void doConnect(BluetoothDevice device) {
        mdBluetoothManager.stopScan();
        mdBluetoothManager.connectDevice(device, this);
        showLoadingDialog("正在连接设备：" + SN);
        hander.postDelayed(dismssDialogRunnable, 10000);
    }

    /**
     * ble 取消连接
     */
    private void disconnectDevice() {
        if (null != mdBluetoothManager) {
            mdBluetoothManager.disconnect();
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
                    Timber.d("蓝牙连接断开");
                    mHandler.sendEmptyMessage(Constants.BT_DISCONNECTED);
                    break;

                case REQUEST_MTU_FAIL:
                    Timber.d("MTU请求设置失败");
                    mHandler.sendEmptyMessage(Constants.BT_REQUEST_MTU_FAIL);
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

                case MESSAGE_WRITE_SUCCESS:
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
                        byte[] data = ((String) event.getEventData()).getBytes();
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

    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case Constants.BT_CONNECT:
                    ToastUtils.show("蓝牙已连接");
                    dismissLoadingDialog();
                    hander.removeCallbacks(dismssDialogRunnable);
                    isBlueConnected = true;
                    isAutoConnectBlue = true;
                    MCloudApp.setIsBluetoothDeviceConnected(true);
                    EventBus.getDefault().post(new BluetoothStateEvent(true));
                    startBluAuthenticate();//蓝牙连接成功开始进行验证
                    break;

                case Constants.BT_DISCONNECTED:
                    ToastUtils.show("蓝牙连接已断开!");
                    dismissLoadingDialog();
                    isBlueConnected = false;
                    MCloudApp.setIsBluetoothDeviceConnected(false);
                    EventBus.getDefault().post(new BluetoothStateEvent(false));
//                        if (isAutoConnectBlue) {
//                            //clearLocalStorage();
//                            //断开蓝牙后重新连接
//                            findAndConnectBleDevice();
//                        }
                    break;

                case Constants.BT_MESSAGE_WRITE_SUCCESS:
                    ToastUtils.show("指令已发送");
                    break;

                case Constants.BT_MESSAGE_WRITE_FAIL:
                    ToastUtils.show("指令发送失败");
                    break;

                case Constants.BT_WRITE_TIME_OUT:
                    ToastUtils.show("指令发送超时");
                    break;

                case Constants.VERIFY_RESULT:
                    if (msg.obj.equals("1")) {
                        ToastUtils.show("蓝牙认证通过!");
                        sendDeviceStateComd();
                    } else {
                        ToastUtils.show("蓝牙认证失败!");
                        try {
                            Thread.sleep(1000);
                            disconnectDevice();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case Constants.MESSAGE_RESPONSE_TIME_OUT:
                    ToastUtils.show("消息等待响应超时！");
                    break;

                case Constants.REFRESH_RUN_STATE:
                    break;

                case Constants.MESSAGE_RESPONSE_SAVE_SETTINGS_SUCCESS:
                    ToastUtils.show("设置信息已保存！");
                    break;

                case Constants.BT_REQUEST_MTU_FAIL:
                    ToastUtils.show("MTU请求设置失败！");
                    break;

                case Constants.BT_SERVICE_FIND_FAIL:
                    ToastUtils.show("蓝牙服务发现失败！");
                    break;

                case Constants.BT_CHARACTERISTICS_FIND_FAIL:
                    ToastUtils.show("蓝牙特征读取失败！");
                    break;

                case Constants.BT_ENABLE_READ_FAIL:
                    ToastUtils.show("设置读取Descriptor失败！");
                    break;

                case Constants.BT_RECOVERY_SUCCESS:
                    break;

                case Constants.MESSAGE_RESPONSE_REBOOT_DEVICE:
                    break;

                case Constants.MESSAGE_LOCK_REBOOT_DEVICE:
                    ToastUtils.show("蓝牙通讯已就绪！");
                    sendDeviceStateComd();//unlock后发送指令
                    break;

                default:
                    break;
            }

            return false;
        }
    });


    private class MyOnBytePackage implements OnBytePackage {
        @Override
        public void onPackageArrived(final byte[] data) {
            try {
                String cmdStr = new String(data, "utf-8");
                Timber.d("应答指令===" + cmdStr);
                String cmdArray[] = cmdStr.replace("\r\n", "").split(",");

                if (cmdStr.startsWith("$$224") && cmdStr.endsWith("\r\n")) {
                    if (cmdStr.equals("$$224ce\r\n")) {
                        startBluAuthenticate();//重新认证
                        return;
                    }

                    byte[] resultData = StringUtil.hexStringToBytes(cmdArray[3]);
                    try {
                        String deskey = "12345678";
                        String strdes = new String(DesUtil.decrypt(resultData, deskey), "utf-8");
                        if (strdes.length() != 0) {
                            String desStr = StringUtil.bytesToHexString(DesUtil.encrypt((StringUtil.reverseString(strdes.substring(0, 6)) + deskey).getBytes(), deskey));
                            String cmd = "##222," + SN + ",0," + desStr.toUpperCase() + "\r\n";
                            Message msg = new Message(UUID.randomUUID().toString(), cmd, true);
                            mdBluetoothManager.writeMessage(msg);
                            Timber.d("发送指令===" + cmd);
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }

                //设备登录验证结果指令
                if (cmdStr.startsWith("$$223") && cmdStr.endsWith("\r\n")) {
                    sendHandleMessage(Constants.VERIFY_RESULT, cmdArray[1]);
                    Timber.d("认证结果===" + cmdArray[1]);
                    return;
                }

                //需要验证设备
                if (cmdStr.equals("Please verify the equipment.\r\n")) {
                    sendHandleMessage(Constants.VERIFY_RESULT, "0");
                    return;
                }

                if (cmdStr.equals("Equipment Verify OK.\r\n")) {
                    sendHandleMessage(Constants.MESSAGE_LOCK_REBOOT_DEVICE, null);
                    return;
                }

                if (cmdStr.startsWith("$$019e")) {
                    mHandler.sendEmptyMessage(Constants.MESSAGE_RESPONSE_SAVE_SETTINGS_SUCCESS);
                    isNeedSaveConfig = false;
                    return;
                }

                parserResult(cmdStr);

            } catch (Exception ex) {
                Timber.e(ex);
            }
        }
    }

    private void parserResult(String cmdStr) {
        //设置自动测量模式应答
        if (cmdStr.startsWith("$$7011") || cmdStr.startsWith("$$7012") || cmdStr.startsWith("$$2011") || cmdStr.startsWith("$$2012") || cmdStr.startsWith("$$7001") || cmdStr.startsWith("$$7003")) {
            isNeedSaveConfig = true;
        }

        EventBus.getDefault().post(cmdStr);
    }


    private void sendHandleMessage(int what, Object obj) {
        android.os.Message message = new android.os.Message();
        message.what = what;
        if (obj != null) {
            message.obj = obj;
        }
        mHandler.sendMessage(message);
    }

    /**
     * 发送蓝牙请求设备信息指令
     */
    public void sendDeviceStateComd() {
        //##7010，查询工作模式
        mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), "##7010\r\n", true));
        Timber.d("发送查询工作模式指令===" + "##7010");

        //##2001，查询服务器地址1
        mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), "##2001\r\n", true));
        Timber.d("发送查询服务器地址1指令===" + "##2001");

        //##2002，查询服务器地址2
        mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), "##2002\r\n", true));
        Timber.d("发送查询服务器地址2指令===" + "##2002");

        //##7000，查询采集器参数
        mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), "##7000\r\n", true));
        Timber.d("发送查询采集器参数指令===" + "##7000");

        //##7002，查询执行机构参数
        mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), "##7002\r\n", true));
        Timber.d("发送查询执行机构参数指令===" + "##7002");
    }

    /**
     * 蓝牙连接成功开始进行验证  lock
     */
    private void startBluAuthenticate() {
        String com = "##224," + SN + ",0\r\n";
        Message msg = new Message(UUID.randomUUID().toString(), com, true);
        mdBluetoothManager.writeMessage(msg);
        Timber.d("发送指令===" + com);
    }


    /**
     * 发送保存命令，让设备将配置参数写入存储器
     */
    private void sendSaveConfigCommand() {
        if (!isBlueConnected) {
            ToastUtils.show("设备已断开连接,无法发送保存命令");
            return;
        }

        Message msg = new Message(UUID.randomUUID().toString(), "##0191\n", true);
        mdBluetoothManager.writeMessage(msg);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // 蓝牙已经开启
                if (resultCode != Activity.RESULT_OK) {
                    ToastUtils.show("蓝牙未启用");
                    return;
                }
                findAndConnectBleDevice();
                break;

            default:
                break;
        }
    }


    /**
     * 是否切换连接模式
     */
    public void showChangeModle(String content, final String index) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this)
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
                        isBlueConnected = false;
                        isAutoConnectBlue = false;
                        MCloudApp.setIsBluetoothDeviceConnected(false);
                        disconnectDevice();
                        ConfigADMEActivity.this.finish();

                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }


    /**
     * revert the image color and text color to black
     */
    private void resetTabState() {
        setTabState(mTvParameter, R.drawable.szxd_wxz, getResources().getColor(R.color.font_main));
        setTabState(mTvQueryData, R.drawable.yxzt_wxz, getResources().getColor(R.color.font_main));
        setTabState(mTvDeviceDetails, R.drawable.xtgj_wxz, getResources().getColor(R.color.font_main));
        setTabState(mTvHighsetting, R.drawable.gjpz_wxz, getResources().getColor(R.color.font_main));
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
     * switch the fragment accordting to id
     */
    private void switchFrgment(int i) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch (i) {
            case 0:
                admeHomeFragment = new ADMEHomeFragment();
                transaction.replace(R.id.sub_content, admeHomeFragment);
                break;

            case 1:
                queryDataFragment = new QueryDataFragment();
                transaction.replace(R.id.sub_content, queryDataFragment);
                break;

            case 2:
                deviceDetailsFragment = new DeviceDetailsFragment();
                transaction.replace(R.id.sub_content, deviceDetailsFragment);
                break;

            default:
        }
        transaction.commit();
    }


    @Override
    public void onPause() {
        super.onPause();
        dismissLoadingDialog();
    }

    private boolean doSaveConfigBeforeLeave() {
        if (!isNeedSaveConfig) {
            return true;
        }

        showTipDialog("您还没有对设备的配置进行保存操作，请点击右上角保存按钮进行保存！");
        return false;
    }

    @Override
    public void onBackPressed() {
        if (!HandleBackUtil.handleBackPress(this)) {
            if (isBlueConnected) {
                if (doSaveConfigBeforeLeave()) {
                    showChangeModle(getResources().getString(R.string.finish_activity_disconnect_bluetooth_device), "1");
                }

            } else {
                dismissLoadingDialog();
                hander.removeCallbacks(dismssDialogRunnable);
                isBlueConnected = false;
                isAutoConnectBlue = false;
                MCloudApp.setIsBluetoothDeviceConnected(false);
                mdBluetoothManager.stopScan();
                this.finish();
            }
        }
    }
}
