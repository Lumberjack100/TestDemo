package com.shmedo.mcloudapp.ui.activity.device;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;

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
 * 描述：    TODO
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


    protected Runnable dismssDialogRunnable = new Runnable() {
        @Override
        public void run() {
            dismissLoadingDialog();
            if (!TextUtils.isEmpty(errMsg))
                ToastUtils.show(errMsg);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = Objects.requireNonNull(bluetoothManager).getAdapter();
        mdBluetoothManager = MdBluetoothManager.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
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
    protected void startDiscoveryDevice() {
        //蓝牙未打开
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        //蓝牙已打开时，开始扫描蓝牙设备
        mdBluetoothManager.scanDevice(10, this);
        if (null != mBluetoothAdapter && mBluetoothAdapter.isEnabled()) {
            showLoadingDialog("正在搜索设备：" + SN);
            errMsg = "扫描超时，请稍后尝试";
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
        errMsg = "连接超时,请稍后尝试";
        hander.postDelayed(dismssDialogRunnable, 15000);
    }

    /**
     * ble 取消连接
     */
    public void disconnectDevice() {
        if (null != mdBluetoothManager) {
            mdBluetoothManager.disconnect();
            isAutoConnectBlue = false;
            MCloudApp.setIsBluetoothDeviceConnected(false);
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
                    dismissLoadingDialog();
                    hander.removeCallbacks(dismssDialogRunnable);
                    MCloudApp.setIsBluetoothDeviceConnected(false);
                    EventBus.getDefault().post(new BluetoothStateEvent(false));
//                    if (isAutoConnectBlue) {
//                        //断开蓝牙后重新连接
//                        findAndConnectBleDevice();
//                    }
                    break;

                case Constants.BT_MESSAGE_WRITE_SUCCESS:
//                    ToastUtils.show("指令已发送");
                    break;

                case Constants.BT_MESSAGE_WRITE_FAIL:
                    ToastUtils.show("指令发送失败");
                    break;

                case Constants.BT_WRITE_TIME_OUT:
                    ToastUtils.show("指令发送超时");
                    break;

                case Constants.VERIFY_RESULT:
                    dismissLoadingDialog();
                    hander.removeCallbacks(dismssDialogRunnable);

                    if (msg.obj.equals("1")) {
                        showLoadingDialog("查询设备配置参数...");
                        errMsg = "查询设备参数超时，请尝试重新连接";
                        hander.postDelayed(dismssDialogRunnable, 5000);
                        obtainDeviceStateCmd();

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
//                    ToastUtils.show("消息等待响应超时！");
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
//                    ToastUtils.show("蓝牙通讯已就绪！");
//                    Objects.requireNonNull(obtainDeviceStateCmdCallback).obtainDeviceStateCmd();
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
                    sendCommonMessage(Constants.VERIFY_RESULT, cmdArray[1]);
                    Timber.d("认证结果===" + cmdArray[1]);
                    return;
                }

                //需要验证设备
                if (cmdStr.equals("Please verify the equipment.\r\n")) {
                    sendCommonMessage(Constants.VERIFY_RESULT, "0");
                    return;
                }

                if (cmdStr.equals("Equipment Verify OK.\r\n")) {
                    sendCommonMessage(Constants.MESSAGE_LOCK_REBOOT_DEVICE, null);
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


    protected void parserResult(String cmdStr) {
        //查询计米轮参数应答
        if (cmdStr.startsWith("$$7022") && cmdStr.endsWith("\r\n")) {
            dismissLoadingDialog();
            hander.removeCallbacks(dismssDialogRunnable);
        }

        if ((cmdStr.startsWith("$$7011")
                || cmdStr.startsWith("$$2011")
                || cmdStr.startsWith("$$2012")
                || cmdStr.startsWith("$$7001")
                || cmdStr.startsWith("$$7003")) && cmdStr.endsWith("\r\n")) {
            isConfigChange = true;
        }

        //设置保存参数应答
        if (cmdStr.startsWith("$$0191") && cmdStr.endsWith("\r\n")) {
            ToastUtils.show("已发送保存命令,设备即将断开连接重启");
            isConfigChange = false;
            hander.removeCallbacks(dismssDialogRunnable);
            dismissLoadingDialog();
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


    private void sendCommonMessage(int what, Object obj) {
        android.os.Message message = new android.os.Message();
        message.what = what;
        if (obj != null) {
            message.obj = obj;
        }
        mHandler.sendMessage(message);
    }


    public void sendCommonCommand(String cmdStr) {
        Message msg = new Message(UUID.randomUUID().toString(), cmdStr, true);
        if (mdBluetoothManager != null) {
            mdBluetoothManager.writeMessage(msg);
        }
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
     * 蓝牙设备连接时,发送查询设备参数指令
     * 集成类可以重载覆盖父类的既有指令
     */
    protected void obtainDeviceStateCmd() {
        sendCommonCommand("##7010\r\n");
        Timber.d("发送查询工作模式指令===" + "##7010");

        hander.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendCommonCommand("##2001\r\n");
                Timber.d("发送查询服务器地址1指令===" + "##2001");
            }
        }, 200);

        hander.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendCommonCommand("##2002\r\n");
                Timber.d("发送查询服务器地址2指令===" + "##2002");
            }
        }, 200);

        hander.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendCommonCommand("##7000\r\n");
                Timber.d("发送查询采集器参数指令===" + "##7000");
            }
        }, 200);

        hander.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendCommonCommand("##7002\r\n");
                Timber.d("发送查询执行机构参数指令===" + "##7002");
            }
        }, 200);

        hander.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendCommonCommand("##7020\r\n");
                Timber.d("发送查询控制电机参数指令===" + "##7020");
            }
        }, 200);

        hander.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendCommonCommand("##7022\r\n");
                Timber.d("发送查询计米轮参数指令===" + "##7022");
            }
        }, 200);
    }


    protected void sendSaveParamCommand() {
        sendCommonCommand("##0191\r\n");
        showLoadingDialog("正在发送保存命令...");
        errMsg = "发送指令超时,请稍后尝试";
        hander.postDelayed(dismssDialogRunnable, 5000);
    }


    public void showSaveDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this)
                .title("温馨提示：")
                .content(content)
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
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
