package com.shmedo.mcloudapp.ui.activity.device;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.cmd.CommandManager;
import com.shmedo.core.cmd.CommandResult;
import com.shmedo.core.cmd.entity.AuthenticationConfigEntity;
import com.shmedo.core.cmd.entity.BreakAlarmStatusEntity;
import com.shmedo.core.cmd.entity.LowEnergyModelEntity;
import com.shmedo.core.cmd.entity.SaveConfigInfoEntity;
import com.shmedo.core.cmd.entity.ServerNumberEntity;
import com.shmedo.core.cmd.entity.SetRemoteUpgradeEntity;
import com.shmedo.core.enums.BreakAlarmStatus;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.enums.LowEnergyModel;
import com.shmedo.core.enums.SaveConfigMode;
import com.shmedo.core.enums.ServerNumber;
import com.shmedo.core.enums.SetRemoteUpgrade;
import com.shmedo.core.interfaces.OnBytePackage;
import com.shmedo.core.utils.DesUtil;
import com.shmedo.core.utils.StringUtil;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.bluetooth.BluetoothDeviceFindEventData;
import com.shmedo.mcloudapp.bluetooth.BluetoothEvent;
import com.shmedo.mcloudapp.bluetooth.BluetoothEventHandler;
import com.shmedo.mcloudapp.bluetooth.MdBluetoothManager;
import com.shmedo.mcloudapp.bluetooth.Message;
import com.shmedo.mcloudapp.entity.event.BluetoothStateEvent;
import com.shmedo.mcloudapp.ui.activity.ConfigADMEActivity;
import com.shmedo.mcloudapp.ui.activity.ConfigDASActivity;
import com.shmedo.mcloudapp.util.bleutil.ByteManagerUtil;
import com.shmedo.mcloudapp.util.bleutil.Constants;
import com.shmedo.mcloudapp.util.common.HandleBackUtil;

import org.greenrobot.eventbus.EventBus;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device
 * 创建者:   gonghe
 * 创建时间:  2019-11-08
 */
public abstract class BaseDeviceConnectActivity extends BaseActivity {

    public static final int REQUEST_ENABLE_BT = 0x001;//请求开启蓝牙

    public static final int SCAN_SPECIFIC_DEVICE_DELAY_MILLIS = 10000;//搜索指定蓝牙设备超时时间

    public static final int AUTHENTICATE_DELAY_MILLIS = 15000;//认证超时时间

    public static final int CONNECT_DELAY_MILLIS = 20000;//连接设备超时时间

    public static final int SEND_SINGLE_COMMAND_DELAY_MILLIS = 3000;//发送单条指令超时时间

    public static final int CONFIG_PARAMS_DELAY_MILLIS = 25000;//发送配置参数指令超时时间


    private MdBluetoothManager mdBluetoothManager;

    private BluetoothAdapter mBluetoothAdapter;

    private MdBluetoothEventHandler mdBluetoothEventHandler = new MdBluetoothEventHandler();

    protected Handler hander = new Handler();

    private boolean isAutoConnectBlue = true;//是否自动连接蓝牙

    public boolean isConfigChange = false;

    public boolean isExitMode = false;

    public boolean isTimeOut = false;

    private String authenticateParam = "";

    protected String errMsg = "";

    protected String SN = MCloudApp.getCurDeviceToken();

    private String macAddress = MCloudApp.getCurDeviceMacAddr();

    private ProgressRunnable progressRunnable;

    private AuthenticateRunnable authenticateRunnable;

    private int authenticateNum = 0;


    private class ProgressRunnable implements Runnable {
        @Override
        public void run() {
            isTimeOut = true;
            dismissLoadingDialog();
            progressRunnable = null;

            if (!TextUtils.isEmpty(errMsg)) {
                ToastUtils.show(errMsg);

                if (errMsg.contains("连接超时") || errMsg.contains("认证超时")) {
                    stopAuthenticateRunnable();
                    disconnectDevice();
                    MCloudApp.setIsBluetoothDeviceConnected(false);
                    EventBus.getDefault().post(new BluetoothStateEvent(false));
                }
            }
        }
    }

    protected void startProgressRunnable(String dialogContent, long delayMillis) {
        isTimeOut = false;
        showLoadingDialog(dialogContent);
        if (progressRunnable == null) {
            progressRunnable = new ProgressRunnable();
            hander.postDelayed(progressRunnable, delayMillis);
        }
    }

    public void stopProgressRunnable() {
        dismissLoadingDialog();
        hander.removeCallbacks(progressRunnable);
        progressRunnable = null;
    }

    private class AuthenticateRunnable implements Runnable {
        @Override
        public void run() {
            startBleAuthenticate();
            if (authenticateNum <= 4) {
                startAuthenticateRunnable(3000);
            } else {
                stopAuthenticateRunnable();
            }
        }
    }

    private void startAuthenticateRunnable(long delayMillis) {
        if (authenticateRunnable == null) {
            authenticateRunnable = new AuthenticateRunnable();
        }

        authenticateNum++;
        hander.postDelayed(authenticateRunnable, delayMillis);
    }

    private void stopAuthenticateRunnable() {
        hander.removeCallbacks(authenticateRunnable);
        authenticateRunnable = null;
        authenticateNum = 0;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBluetooth();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mdBluetoothManager.addBluetoothEventHandler(mdBluetoothEventHandler);
        ByteManagerUtil.init(new MyOnBytePackage());
    }


    @Override
    protected void onPause() {
        super.onPause();
        mdBluetoothManager.removeBluetoothEventHandler(mdBluetoothEventHandler);
    }

    /**
     * 初始化蓝牙
     */
    private void initBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = Objects.requireNonNull(bluetoothManager).getAdapter();
        mdBluetoothManager = MdBluetoothManager.getInstance();
    }


    /**
     * 扫描蓝牙设备，主要用来判断要连接的设备是否能被搜索到
     */
    private void startDiscoveryDevice() {
        //蓝牙已打开时，开始扫描蓝牙设备
        mdBluetoothManager.scanDevice(SCAN_SPECIFIC_DEVICE_DELAY_MILLIS, this);
        if (null != mBluetoothAdapter && mBluetoothAdapter.isEnabled()) {
            errMsg = "未搜索到此设备，请稍后尝试";
            startProgressRunnable("正在搜索设备：" + SN, SCAN_SPECIFIC_DEVICE_DELAY_MILLIS);

            //因设备问题会造成长时间搜索设备，在此操作过程中无法中断和进行其他操作，进度框会长时间在页面停留
            //新增操作返回，中断当前蓝牙操作并关闭进度框
            if (loadingDialog != null) {
                loadingDialog.setCancelable(true);
                loadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        stopProgressRunnable();
                        mdBluetoothManager.stopScan();
                    }
                });
            }
        }
    }


    /**
     * 搜索并连接指定的蓝牙设备
     */
    public void findAndConnectBleDevice() {
        //蓝牙未打开
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        //通过蓝牙设备列表页面跳转过来时，直接连接设备
        if (!TextUtils.isEmpty(macAddress)) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);
            if (device == null) {
                Timber.e("Device not found.  Unable to connect.");
                return;
            }
            doConnect(device);
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
        if (TextUtils.isEmpty(SN) || TextUtils.isEmpty(device.getName()))
            return;

        if (device.getName().contains(SN)) {
            stopProgressRunnable();
            doConnect(device);
        }
    }

    /**
     * ble 建立连接
     */
    private void doConnect(BluetoothDevice device) {
        mdBluetoothManager.stopScan();
        mdBluetoothManager.connectDevice(device, this);
        errMsg = "连接超时,请稍后尝试";
        startProgressRunnable("正在连接设备：" + SN, CONNECT_DELAY_MILLIS);
    }

    /**
     * ble 取消连接
     */
    public void disconnectDevice() {
        if (null != mdBluetoothManager) {
            mdBluetoothManager.disconnect();
            isAutoConnectBlue = false;//
        }
    }

    /**
     * 连接失败时，中断一会儿再连接
     */
    private void setAutoConnectBlueAfterDisconnect() {
        try {
            Thread.sleep(1000);
            disconnectDevice();
            isAutoConnectBlue = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class MdBluetoothEventHandler implements BluetoothEventHandler {
        @Override
        public void handle(final BluetoothEvent event) {
            switch (event.getEventType()) {
                case DEVICE_FIND:
                    handleDeviceFind((BluetoothDeviceFindEventData) event.getEventData());
                    break;

                case CONNECTED:
//                    ByteManagerUtil.init(new MyOnBytePackage());
                    mHandler.sendEmptyMessage(Constants.BT_CONNECT);
                    break;

                case DISCONNECTED:
                    Timber.e("蓝牙连接断开");
                    mHandler.sendEmptyMessage(Constants.BT_DISCONNECTED);
                    break;

                case REQUEST_MTU_FAIL:
                    Timber.e("MTU请求设置失败");
                    mHandler.sendEmptyMessage(Constants.BT_REQUEST_MTU_FAIL);
                    break;

                case SERVICE_FIND_FAIL:
                    Timber.e("蓝牙服务发现失败");
                    mHandler.sendEmptyMessage(Constants.BT_SERVICE_FIND_FAIL);
                    break;

                case CHARACTERISTICS_FIND_FAIL:
                    Timber.e("特征读取失败");
                    mHandler.sendEmptyMessage(Constants.BT_CHARACTERISTICS_FIND_FAIL);
                    break;

                case ENABLE_READ_SUCCESS:
                    Timber.i("设置读取Descriptor成功");
                    mHandler.sendEmptyMessage(Constants.BT_ENABLE_READ_SUCCESS);
                    break;

                case ENABLE_READ_FAIL:
                    Timber.e("设置读取Descriptor失败");
                    mHandler.sendEmptyMessage(Constants.BT_ENABLE_READ_FAIL);
                    break;

                case WRITE_TIME_OUT:
                    Timber.e("写入等待超时");
                    mHandler.sendEmptyMessage(Constants.BT_WRITE_TIME_OUT);
                    break;

                case MESSAGE_WRITE_SUCCESS:
//                    Timber.i("消息写入成功");
                    mHandler.sendEmptyMessage(Constants.BT_MESSAGE_WRITE_SUCCESS);
                    try {
                        String msg = ((Message) event.getEventData()).getResponseMessage();
                        byte[] data = (byte[]) msg.getBytes();
                        if (data.length > 0) {
                            ByteManagerUtil.getInstance().writeByte(data);
                        }
                    } catch (Exception ex) {
                        Timber.e(ex);
                    }
                    break;

                case MESSAGE_RESPONSE_TIME_OUT:
                    Timber.e("消息等待响应超时");
                    mHandler.sendEmptyMessage(Constants.MESSAGE_RESPONSE_TIME_OUT);
                    break;

                case MESSAGE_WRITE_FAIL:
                    Timber.e("消息写入失败");
                    mHandler.sendEmptyMessage(Constants.BT_MESSAGE_WRITE_FAIL);
                    break;

                case RESPONSE_WITH_NO_MESSAGE:
                    Timber.i("RESPONSE_WITH_NO_MESSAGE");
                    try {
                        byte[] data = ((String) event.getEventData()).getBytes();
                        if (data.length > 0) {
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

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case Constants.BT_CONNECT:
                    isAutoConnectBlue = true;
                    MCloudApp.setIsBluetoothDeviceConnected(true);
                    EventBus.getDefault().post(new BluetoothStateEvent(true));
                    stopProgressRunnable();
                    errMsg = "认证超时,请稍后尝试";
                    startProgressRunnable("蓝牙已连接,设备认证中...", AUTHENTICATE_DELAY_MILLIS);
                    setBleAuthenticateWay();//蓝牙连接成功开始进行验证
                    break;

                case Constants.BT_DISCONNECTED:
                    ToastUtils.show("设备断开连接");
                    stopProgressRunnable();
                    MCloudApp.setIsBluetoothDeviceConnected(false);
                    EventBus.getDefault().post(new BluetoothStateEvent(false));
                    //断开蓝牙后重新连接
                    if (isAutoConnectBlue) {
                        findAndConnectBleDevice();
                    }
                    break;

                case Constants.BT_MESSAGE_WRITE_FAIL:
                    ToastUtils.show("指令发送失败");
                    break;

                case Constants.BT_WRITE_TIME_OUT:
                    ToastUtils.show("指令发送超时");
                    setAutoConnectBlueAfterDisconnect();
                    break;

                case Constants.VERIFY_RESULT:
                    stopProgressRunnable();
                    if (!msg.obj.equals("1")) {
                        ToastUtils.show("设备认证失败!");
                        setBleAuthenticateWay();//重新认证
                        break;
                    }

                    errMsg = "查询设备配置参数超时，请尝试重新连接";
                    if (BaseDeviceConnectActivity.this instanceof ConfigDASActivity || BaseDeviceConnectActivity.this instanceof ConfigADMEActivity) {
                        startProgressRunnable("初始化设备配置信息...", CONFIG_PARAMS_DELAY_MILLIS);
                        queryDeviceConfigInfoCmd();
                    }
                    break;

                case Constants.MESSAGE_RESPONSE_TIME_OUT://消息等待响应超时
                    setAutoConnectBlueAfterDisconnect();
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

                case Constants.MESSAGE_LOCK_REBOOT_DEVICE:
                    Timber.i("蓝牙通讯已就绪");
                    queryDeviceConfigInfoCmd();
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
                final String cmdStr = new String(data, StandardCharsets.UTF_8);
                Timber.d("应答指令===%s", cmdStr);
                String cmdArray[] = cmdStr.replace("\r\n", "").split(",");

                if (cmdStr.startsWith("$$224")) {//认证方式
                    if (cmdStr.replace("\r\n", "").endsWith(CommandResult.ERROR_END)) {
                        setBleAuthenticateWay();//重新认证
                        return;
                    }
                    authenticateParam = cmdArray[3];
                    startBleAuthenticate();
//                    startAuthenticateRunnable(3000);
                }

                //设备登录验证结果指令
                if (cmdStr.startsWith("$$223")) {
                    stopAuthenticateRunnable();
                    sendHandleMessage(Constants.VERIFY_RESULT, cmdArray[1]);
                    Timber.d("设备登录验证状态===%s", cmdArray[1].contains("1"));
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

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parserResult(cmdStr);
                    }
                });

            } catch (Exception ex) {
                Timber.e(ex);
            }
        }
    }


    /**
     * 解析设备的参数指令
     */
    private void parserResult(String cmdStr) {
        if (SN.endsWith("T")) {//ADME 设备应答指令预处理
            parserADMECmdResult(cmdStr);

        } else if (SN.endsWith("L")) {//DAS 设备应答指令预处理
            parserDASCmdResult(cmdStr);

        } else {//其他设备

            //TODO 其他设备
        }

        //设置保存参数应答
        if (cmdStr.startsWith("$$0191") && cmdStr.endsWith("\r\n")) {
            ToastUtils.show("已发送保存命令,设备即将断开连接重启");
            isConfigChange = false;
            stopProgressRunnable();
            disconnectDevice();

            if (isExitMode) {
                hander.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BaseDeviceConnectActivity.this.finish();
                    }
                }, 3000);
            }
        }

        EventBus.getDefault().post(cmdStr);
    }

    /**
     * 解析 ADME 的参数指令
     *
     * @param cmdStr
     */
    private void parserADMECmdResult(String cmdStr) {
        //查询执行机构参数应答
        if (cmdStr.startsWith("$$7002") && cmdStr.endsWith("\r\n")) {
            stopProgressRunnable();
        }

        if ((cmdStr.startsWith("$$2011")
                || cmdStr.startsWith("$$2012")
                || cmdStr.startsWith("$$7001")
                || cmdStr.startsWith("$$7003")) && cmdStr.endsWith("\r\n")) {
            isConfigChange = true;
        }
    }

    /**
     * 解析 DAS 的参数指令
     *
     * @param cmdStr
     */
    private void parserDASCmdResult(String cmdStr) {
        //查询数字式渗压计参数
//        if (cmdStr.startsWith("$$333") && cmdStr.endsWith("\r\n")) {
//            stopProgressRunnable();
//        }

        //此处是各个配置指令应答，表示已经更改配置了
        if ((cmdStr.startsWith("$$006")//调试模式
                || cmdStr.startsWith("$$005")//开关量功能
                || cmdStr.startsWith("$$121")//雨量计精度
                || cmdStr.startsWith("$$227")//断线报警器
                || (cmdStr.startsWith("$$40") && !cmdStr.equals("$$400\r\n"))//设置数字渗压计
                || cmdStr.startsWith("$$150")//采集器接入的传感器
                || cmdStr.startsWith("$$16")//采集器
                || cmdStr.startsWith("$$147")//采集器地址
                || cmdStr.startsWith("$$201")//平台服务器地址端口
                || (cmdStr.startsWith("$$202") && !cmdStr.equals("$$2020\r\n"))//网络中心通信协议
                || (cmdStr.startsWith("$$810") && !cmdStr.equals("$$8100\r\n"))//自动注册平台选择
                || cmdStr.startsWith("$$803")//自动注册平台参数
                || cmdStr.startsWith("$$807")//自动注册服务器地址端口
                || cmdStr.startsWith("$$805")//手动注册平台参数
                || (cmdStr.startsWith("$$809") && !cmdStr.equals("$$8090\r\n"))//MQTT KeepAlive 值
        ) && cmdStr.endsWith("\r\n")) {
            isConfigChange = true;
        }
    }


    /**
     * 查询设备的配置参数信息
     */
    private void queryDeviceConfigInfoCmd() {
        if (SN.endsWith("T")) {//ADME 设备
            queryADMEConfigInfoCmd();

        } else if (SN.endsWith("L")) {//DAS 设备
            queryDASConfigInfoCmd();

        } else {//其他设备

            //TODO 其他设备
        }
    }

    /**
     * 查询 ADME 设备的配置参数信息
     */
    private void queryADMEConfigInfoCmd() {
        sendCommonCommand("##7010\r\n");
        Timber.d("查询工作模式指令===" + "##7010");

        ServerNumberEntity serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_ONE.toInt());
        String cmdAddress1 = CommandManager.getInstance().getCommand(CommandType.SERVER_ADDRESS, serverNumberEntity);
        sendCommonCommand(cmdAddress1);
        Timber.d("查询服务器地址1指令===%s", cmdAddress1);

        serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_TWO.toInt());
        String cmdAddress2 = CommandManager.getInstance().getCommand(CommandType.SERVER_ADDRESS, serverNumberEntity);
        sendCommonCommand(cmdAddress2);
        Timber.d("查询服务器地址2指令===%s", cmdAddress2);

        sendCommonCommand("##7000\r\n");
        Timber.d("查询采集器参数指令===" + "##7000");

        sendCommonCommand("##7002\r\n");
        Timber.d("查询执行机构参数指令===" + "##7002");
    }

    /**
     * 查询 DAS 设备的配置参数信息
     */
    private void queryDASConfigInfoCmd() {
        //获取基础配置信息  ##000
        String command = CommandManager.getInstance().getCommand(CommandType.BASE_CONFIG);
        sendCommonCommandImmediately(command);
        Timber.d("获取基础配置信息指令===%s", command);

        //查询数字式渗压计参数 ##400
//        String shenyajiCommand = CommandManager.getInstance().getCommand(CommandType.QUERY_OSMOMETER_PARAMETER, null);
//        sendCommonCommand(shenyajiCommand);
//        Timber.d("查询渗压计指令===" + shenyajiCommand);
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
     * 延迟200ms发送蓝牙指令,以免同时发送多条指令带来问题
     *
     * @param cmdStr
     */
    public void sendCommonCommand(final String cmdStr) {
        final Message msg = new Message(UUID.randomUUID().toString(), cmdStr, true);
        hander.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mdBluetoothManager != null) {
                    mdBluetoothManager.writeMessage(msg);
                }
            }
        }, 200);
    }


    /**
     * 马上发送蓝牙指令
     *
     * @param cmdStr
     */
    public void sendCommonCommandImmediately(final String cmdStr) {
        final Message msg = new Message(UUID.randomUUID().toString(), cmdStr, true);
        if (mdBluetoothManager != null) {
            mdBluetoothManager.writeMessage(msg);
        }
    }

    /**
     * 蓝牙连接成功,发送认证方式
     */
    private void setBleAuthenticateWay() {
        AuthenticationConfigEntity configEntity = new AuthenticationConfigEntity(SN, 0);
        String command = CommandManager.getInstance().getCommand(CommandType.AUTHENTICATION_CONFIG, configEntity);
        sendCommonCommandImmediately("\r\n" + command);
        Timber.d("设置认证类型指令===%s", command);
    }

    /**
     * 开始认证流程
     */
    private void startBleAuthenticate() {
        Timber.d("解密前:%s", authenticateParam);
        byte[] resultData = StringUtil.hexStringToBytes(authenticateParam);
        try {
            String deskey = "12345678";
            //解密后认证码
            String strDecrypt = new String(DesUtil.decrypt(resultData, deskey), StandardCharsets.UTF_8);
            Timber.d("解密后:%s", strDecrypt);

            if (!TextUtils.isEmpty(strDecrypt)) {
                //反转6位随机码
                String reverseRandomCode = StringUtil.reverseString(strDecrypt.substring(0, 6));
                byte[] byteEncryt = DesUtil.encrypt((reverseRandomCode + deskey).getBytes(), deskey);
                //加密后认证码
                String strEncryt = StringUtil.bytesToHexString(byteEncryt);
                String cmd = "##222," + SN + ",0," + strEncryt.toUpperCase() + "\r\n";
                sendCommonCommand(cmd);
                Timber.d("设备登录验证指令===%s", cmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 保存配置信息指令
     */
    public void saveConfigInfo() {
        SaveConfigInfoEntity saveConfigInfoEntity = new SaveConfigInfoEntity(SaveConfigMode.SAVE_REBOOT.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.SAVE_CONFIG_INFO, saveConfigInfoEntity);

        errMsg = "发送指令超时,请稍后尝试";
        startProgressRunnable("正在发送保存命令...", CONFIG_PARAMS_DELAY_MILLIS);
        sendCommonCommandImmediately(command);
    }

    /**
     * 打开/关闭设备低功耗模式
     */
    public void setLowEnergyModel(boolean isOpen) {
        LowEnergyModelEntity entity = new LowEnergyModelEntity(isOpen ? LowEnergyModel.ACTIVATE.toInt() : LowEnergyModel.STANDBY.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.LOW_ENERGY, entity);
        sendCommonCommandImmediately(command);
    }

    /**
     * 设置断线报警器状态
     */
    public void setBreakAlarmStatus(BreakAlarmStatus breakAlarmStatus) {
        BreakAlarmStatusEntity entity = new BreakAlarmStatusEntity(breakAlarmStatus.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.BREAK_ALARM_STATUS, entity);
        sendCommonCommandImmediately(command);
        Timber.d("设置断线报警器指令==%s", command);
    }

    /**
     * 设置远程升级
     */
    public void setSetRemoteUpgrade(SetRemoteUpgrade remoteUpgrade, String address, int port) {
        SetRemoteUpgradeEntity setRemoteUpgradeEntity = new SetRemoteUpgradeEntity(remoteUpgrade.toInt(), address, port);
        String command = CommandManager.getInstance().getCommand(CommandType.SETTING_REMOTE_UPGRADE, setRemoteUpgradeEntity);
        sendCommonCommandImmediately(command);
        Timber.d("设置远程升级指令==%s", command);
    }


    public void showSaveDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this)
                .title("温馨提示：")
                .content(content)
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
                .neutralText("取消")
                .positiveText("保存")
                .negativeText("不保存")
                .negativeColor(Color.parseColor("#807B7B"))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        saveConfigInfo();
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        isConfigChange = false;
                        disconnectDevice();
                        if (isExitMode) {
                            BaseDeviceConnectActivity.this.finish();
                        }
                    }
                }).onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }


    private void showDisconnectDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this)
                .title("温馨提示：")
                .content(content)
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .negativeColor(Color.parseColor("#807B7B"))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        disconnectDevice();
                        BaseDeviceConnectActivity.this.finish();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    @Override
    protected void onDestroy() {
        dismissLoadingDialog();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!HandleBackUtil.handleBackPress(this)) {
            if (MCloudApp.isIsBluetoothDeviceConnected()) {
                if (isConfigChange) {
                    isExitMode = true;
                    showSaveDialog(getResources().getString(R.string.disconnect_bluetooth_device_save_param_warn));

                } else {
                    showDisconnectDialog(getResources().getString(R.string.finish_activity_disconnect_bluetooth_device));
                }
            } else {
                finish();
            }
        }
    }
}
