package com.shmedo.mcloudapp.util.bleutil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;

import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.bluetooth.BluetoothDeviceFindEventData;
import com.shmedo.mcloudapp.bluetooth.BluetoothEvent;
import com.shmedo.mcloudapp.bluetooth.BluetoothEventHandler;
import com.shmedo.mcloudapp.bluetooth.MdBluetoothManager;
import com.shmedo.mcloudapp.bluetooth.Message;
import com.shmedo.mcloudapp.entity.ble.MDevice;
import com.shmedo.mcloudapp.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

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
import static com.shmedo.mcloudapp.util.bleutil.Constants.MESSAGE_RESPONSE_REBOOT_DEVICE;
import static com.shmedo.mcloudapp.util.bleutil.Constants.MESSAGE_RESPONSE_SAVE_SETTINGS_SUCCESS;
import static com.shmedo.mcloudapp.util.bleutil.Constants.MESSAGE_RESPONSE_TIME_OUT;
import static com.shmedo.mcloudapp.util.bleutil.Constants.REFRESH_RUN_STATE;
import static com.shmedo.mcloudapp.util.bleutil.Constants.VERIFY_RESULT;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util.bleutil
 * 创建者:   gonghe
 * 创建时间:  2019-10-23
 * 描述：    TODO
 */
public class BleDeviceHelpUtil {

    private static BleDeviceHelpUtil instance = new BleDeviceHelpUtil();

    private Context mContext = MCloudApp.getContext();

    private BluetoothAdapter mBluetoothAdapter;

    private MdBluetoothManager mdBluetoothManager;

    private List<MDevice> list = new ArrayList<>();

    private String currentMessageId = "";




    public static BleDeviceHelpUtil getInstance()
    {
        return instance;
    }


    public BleDeviceHelpUtil()
    {
        initBluetooth();
    }



    /**
     * 初始化蓝牙
     */
    private void initBluetooth() {
        final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        MdBluetoothManager.init(mBluetoothAdapter, bluetoothManager);
        mdBluetoothManager = MdBluetoothManager.getInstance();
        mdBluetoothManager.setEventHandler(new MdBluetoothEventHandler());
    }

    private class MdBluetoothEventHandler implements BluetoothEventHandler {
        @Override
        public void handle(final BluetoothEvent event) {
            switch (event.getEventType()) {
                case DEVICE_FIND:
                    handleDeviceFind((BluetoothDeviceFindEventData) event.getEventData());
                    break;
                case CONNECTED: {
                    //ByteManagerUtil.init(new MyOnBytePackage());
                    mHandler.sendEmptyMessage(BT_CONNECT);
                    break;
                }
                case DISCONNECTED:
                    mHandler.sendEmptyMessage(BT_DISCONNECTED);

                    break;
                case REQUEST_MTU_FAIL: {
                    Timber.d("MTU请求设置失败");
                    mHandler.sendEmptyMessage(BT_REQUEST_MTU_FAIL);
                    break;
                }
                case SERVICE_FIND_FAIL: {
                    Timber.d("蓝牙服务发现失败");
                    mHandler.sendEmptyMessage(BT_SERVICE_FIND_FAIL);
                    break;
                }
                case CHARACTERISTICS_FIND_FAIL: {
                    Timber.d("特征读取失败");
                    mHandler.sendEmptyMessage(BT_CHARACTERISTICS_FIND_FAIL);
                    break;
                }
                case ENABLE_READ_SUCCESS: {
                    Timber.d("设置读取Descriptor成功");
                    mHandler.sendEmptyMessage(BT_ENABLE_READ_SUCCESS);
                    break;
                }
                case ENABLE_READ_FAIL: {
                    Timber.d("设置读取Descriptor失败");
                    mHandler.sendEmptyMessage(BT_ENABLE_READ_FAIL);
                    break;
                }
                case WRITE_TIME_OUT: {
                    Timber.d("写入等待超时");
                    disconnectDevice();
                    mHandler.sendEmptyMessage(BT_WRITE_TIME_OUT);
                    break;
                }
                case MESSAGE_WRITE_SUCCESS: {
                    Timber.d("消息写入成功");
                    mHandler.sendEmptyMessage(BT_MESSAGE_WRITE_SUCCESS);
                    try {
                        currentMessageId = ((Message) event.getEventData()).getMessageID();
                        Timber.d("消息id===" + currentMessageId);
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
                    Timber.w("消息等待响应超时");
                    mHandler.sendEmptyMessage(MESSAGE_RESPONSE_TIME_OUT);
                    disconnectDevice();
                    break;
                case MESSAGE_WRITE_FAIL: {
                    Timber.w("消息写入失败");
                    mHandler.sendEmptyMessage(BT_MESSAGE_WRITE_FAIL);
                    break;
                }
                case RESPONSE_WITH_NO_MESSAGE: {
                    try {
                        byte[] data = (byte[]) event.getEventData();
                        if (data != null && data.length > 0) {
                            ByteManagerUtil.getInstance().writeByte(data);
                        }

                    } catch (Exception ex) {
                        Timber.e(ex);
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }


    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case BT_CONNECT:
                    ToastUtil.showShortToast("蓝牙已连接");
//                    dismissLoadingDialog();
//                    isConneted = true;
//                    //startBluAuthenticate();//蓝牙连接成功开始进行验证
//                    hander.removeCallbacks(dismssDialogRunnable);
                    mdBluetoothManager.stopScan();
                    break;

                case BT_DISCONNECTED:
                    ToastUtil.showShortToast("蓝牙连接已断开!");
//                    dismissLoadingDialog();
//                    isConneted = false;
                    break;

                case BT_MESSAGE_WRITE_SUCCESS:
                    ToastUtil.showShortToast("已发送指令");
                    break;

                case BT_MESSAGE_WRITE_FAIL:
                    ToastUtil.showShortToast("发送指令失败");
                    break;

                case BT_WRITE_TIME_OUT:
                    ToastUtil.showShortToast("发送指令超时");
                    break;

                case VERIFY_RESULT:
                    if (msg.obj.equals("1")) {
                        ToastUtil.showShortToast("蓝牙认证通过!");
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

                case MESSAGE_RESPONSE_TIME_OUT:
                    ToastUtil.showShortToast("消息等待响应超时！");
                    break;

                case REFRESH_RUN_STATE:
                    break;

                case MESSAGE_RESPONSE_SAVE_SETTINGS_SUCCESS:
                    ToastUtil.showShortToast("设置信息已保存！");
                    break;

                case BT_REQUEST_MTU_FAIL:
                    ToastUtil.showShortToast("MTU请求设置失败！");
                    break;

                case BT_SERVICE_FIND_FAIL:
                    ToastUtil.showShortToast("蓝牙服务发现失败！");
                    break;

                case BT_CHARACTERISTICS_FIND_FAIL:
                    ToastUtil.showShortToast("蓝牙特征读取失败！");
                    break;

                case BT_ENABLE_READ_FAIL:
                    ToastUtil.showShortToast("设置读取Descriptor失败！");
                    break;

                case BT_RECOVERY_SUCCESS:
                    ToastUtil.showShortToast("已恢复出厂设置！");
                    break;

                case MESSAGE_RESPONSE_REBOOT_DEVICE:
                    ToastUtil.showShortToast("已重启系统！");
                    break;

                default:
                    break;
            }

            return false;
        }
    });

    private void handleDeviceFind(BluetoothDeviceFindEventData eventData) {
        if (list.contains(eventData.getNewDevice()) || eventData.getNewDevice().getDevice().getName() == null) {
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
     * ble 取消连接
     */
    private void disconnectDevice() {
        if (null != MdBluetoothManager.getInstance()) {
            MdBluetoothManager.getInstance().disconnect();
        }
    }


}
