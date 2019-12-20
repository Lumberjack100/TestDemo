package com.shmedo.mcloudapp.ui.activity.device;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.das.das.cmd.CommandManager;
import com.shmedo.das.das.cmd.CommandType;
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
import com.shmedo.mcloudapp.util.bleutil.ByteManagerUtil;
import com.shmedo.mcloudapp.util.bleutil.Constants;
import com.shmedo.mcloudapp.util.common.HandleBackUtil;

import org.greenrobot.eventbus.EventBus;

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

    public static final int REQUEST_ENABLE_BT = 0x001;

    private MdBluetoothManager mdBluetoothManager;

    private BluetoothAdapter mBluetoothAdapter;

    private MdBluetoothEventHandler mdBluetoothEventHandler = new MdBluetoothEventHandler();

    protected Handler hander = new Handler();

    private boolean isAutoConnectBlue = true;//是否自动连接蓝牙

    public boolean isConfigChange = false;

    public boolean isExitMode = false;

    private String errMsg = "";

    private String SN = MCloudApp.getCurDeviceToken();

    private String macAddress = MCloudApp.getCurDeviceMacAddr();

    private ProgressRunnable progressRunnable;


    private class ProgressRunnable implements Runnable {
        @Override
        public void run() {
            dismissLoadingDialog();
            if (!TextUtils.isEmpty(errMsg)) {
                ToastUtils.show(errMsg);

                if (errMsg.contains("连接超时")) {
                    disconnectDevice();
                    MCloudApp.setIsBluetoothDeviceConnected(false);
                    EventBus.getDefault().post(new BluetoothStateEvent(false));
                }
            }
        }
    }

    protected void startProgressRunnable(String dialogContent, long delayMillis) {
        showLoadingDialog(dialogContent);
        if (progressRunnable == null) {
            progressRunnable = new ProgressRunnable();
            hander.postDelayed(progressRunnable, delayMillis);
        }
    }

    protected void stopProgressRunnable() {
        dismissLoadingDialog();
        hander.removeCallbacks(progressRunnable);
        progressRunnable = null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = Objects.requireNonNull(bluetoothManager).getAdapter();
        mdBluetoothManager = MdBluetoothManager.getInstance();
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
     * 扫描蓝牙设备，主要用来判断要连接的设备是否能被搜索到
     */
    private void startDiscoveryDevice() {
        //蓝牙已打开时，开始扫描蓝牙设备
        mdBluetoothManager.scanDevice(10, this);
        if (null != mBluetoothAdapter && mBluetoothAdapter.isEnabled()) {
            errMsg = "未搜索到此设备，请稍后尝试";
            startProgressRunnable("正在搜索设备：" + SN, 10000);
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
            if (device != null) {
                doConnect(device);
            } else {
                Timber.w("Device not found.  Unable to connect.");
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
        startProgressRunnable("正在连接设备：" + SN, 15000);
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

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case Constants.BT_CONNECT:
                    isAutoConnectBlue = true;
                    MCloudApp.setIsBluetoothDeviceConnected(true);
                    EventBus.getDefault().post(new BluetoothStateEvent(true));
                    startBluAuthenticate();//蓝牙连接成功开始进行验证
                    break;

                case Constants.BT_DISCONNECTED:
                    ToastUtils.show("设备断开连接");
                    stopProgressRunnable();
                    MCloudApp.setIsBluetoothDeviceConnected(false);
                    EventBus.getDefault().post(new BluetoothStateEvent(false));
                    if (isAutoConnectBlue) {
                        //断开蓝牙后重新连接
                        findAndConnectBleDevice();
                    }
                    break;

                case Constants.BT_MESSAGE_WRITE_SUCCESS:
//                    ToastUtils.show("指令已发送");
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
                    if (msg.obj.equals("1")) {
                        errMsg = "查询设备参数超时，请尝试重新连接";
                        startProgressRunnable("查询设备配置参数...", 10000);
                        obtainDeviceConfigInfoCmd();
                        ToastUtils.show("蓝牙连接成功");
                    } else {
                        ToastUtils.show("蓝牙认证失败!");
                        setAutoConnectBlueAfterDisconnect();
                    }
                    break;

                case Constants.MESSAGE_RESPONSE_TIME_OUT://消息等待响应超时
                    setAutoConnectBlueAfterDisconnect();
                    break;

                case Constants.REFRESH_RUN_STATE:
                    break;

                case Constants.MESSAGE_RESPONSE_SAVE_SETTINGS_SUCCESS:
//                    ToastUtils.show("设置信息已保存！");
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
                    Timber.d("蓝牙通讯已就绪");
                    obtainDeviceConfigInfoCmd();
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
                final String cmdStr = new String(data, "utf-8");
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
                        if (!TextUtils.isEmpty(strdes)) {
                            String desStr = StringUtil.bytesToHexString(DesUtil.encrypt((StringUtil.reverseString(strdes.substring(0, 6)) + deskey).getBytes(), deskey));
                            String cmd = "##222," + SN + ",0," + desStr.toUpperCase() + "\r\n";
                            sendCommonCommand(cmd);
                            Timber.d("发送设备登录验证指令===" + cmd);
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
                    Timber.d("设备登录验证状态===" + cmdArray[1]);
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
     *
     * @param cmdStr
     */
    private void parserResult(String cmdStr) {
        if (SN.endsWith("T")) {//ADME 设备
            parserADMECmdResult(cmdStr);

        } else if (SN.endsWith("L")) {//DAS 设备
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
        if (cmdStr.startsWith("$$333") && cmdStr.endsWith("\r\n")) {
            stopProgressRunnable();
        }

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
                || (cmdStr.startsWith("$$202") && !cmdStr.equals("$$2020\r\n"))//网络链路通信协议
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
    protected void obtainDeviceConfigInfoCmd() {
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
        Timber.d("发送查询工作模式指令===" + "##7010");

        sendCommonCommand("##2001\r\n");
        Timber.d("发送查询服务器地址1指令===" + "##2001");

        sendCommonCommand("##2002\r\n");
        Timber.d("发送查询服务器地址2指令===" + "##2002");

        sendCommonCommand("##7000\r\n");
        Timber.d("发送查询采集器参数指令===" + "##7000");

        sendCommonCommand("##7002\r\n");
        Timber.d("发送查询执行机构参数指令===" + "##7002");
    }

    /**
     * 查询 DAS 设备的配置参数信息
     */
    private void queryDASConfigInfoCmd() {
        //获取所有配置  ##333
        String allInfoCommand = CommandManager.getInstance().getCommand(CommandType.GET_ALL_SENSOR_CONFIG, null);
        sendCommonCommand(allInfoCommand);
        Timber.d("发送获取所有配置指令===" + allInfoCommand);

        //系统运行状态 ##014
//        String runstateCommand = CommandManager.getInstance().getCommand(CommandType.SYSTEM_RUN_STATE, null);
//        sendCommonCommand(runstateCommand);
//        Timber.d("发送系统运行状态指令===" + runstateCommand);

        //查询数字式渗压计参数 ##400
//        String shenyajiCommand = CommandManager.getInstance().getCommand(CommandType.QUERY_OSMOMETER_PARAMETER, null);
//        sendCommonCommand(shenyajiCommand);
//        Timber.d("发送查询渗压计指令===" + shenyajiCommand);

        //版本信息 ##040
//        String versionCommand = CommandManager.getInstance().getCommand(CommandType.VERSION_MESSAGE, null);
//        sendCommonCommand(versionCommand);
//        Timber.d("发送版本信息指令===" + versionCommand);
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
     * 发送蓝牙指令,延迟200ms 后发送，以免同时发送多条指令带来问题
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
     * 蓝牙连接成功开始进行验证  lock
     */
    private void startBluAuthenticate() {
        String com = "\r\n##224," + SN + ",0\r\n";
        Message msg = new Message(UUID.randomUUID().toString(), com, true);
        mdBluetoothManager.writeMessage(msg);
        Timber.d("发送指令===" + com);
    }


    protected void sendSaveParamCommand() {
        errMsg = "发送指令超时,请稍后尝试";
        startProgressRunnable("正在发送保存命令...", 10000);
        sendCommonCommandImmediately("##0191\r\n");
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
                        sendSaveParamCommand();
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
