package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.cmd.entity.AuthenticationConfigEntity;
import com.shmedo.configlibrary.ble.cmd.entity.LogOutputEntity;
import com.shmedo.configlibrary.ble.cmd.entity.LowEnergyModelEntity;
import com.shmedo.configlibrary.ble.cmd.entity.SaveConfigInfoEntity;
import com.shmedo.configlibrary.ble.cmd.entity.ServerNumberEntity;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.LogOutputStatus;
import com.shmedo.configlibrary.ble.enums.LowEnergyModel;
import com.shmedo.configlibrary.ble.enums.SaveConfigMode;
import com.shmedo.configlibrary.ble.enums.ServerNumber;
import com.shmedo.configlibrary.ble.utils.DesUtil;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.event.BluetoothConnectStateEvent;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.bluetooth.BluetoothEvent;
import com.shmedo.mcloudapp.bluetooth.Message;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.BleViewModel;
import com.shmedo.mcloudapp.util.bleutil.Constants;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;

import org.greenrobot.eventbus.EventBus;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/10 <br/>
 * 描述：     TODO
 */
public abstract class BaseBleConnectFragment extends BaseFragment {

    public static final int REQUEST_ENABLE_BT = 0x001;//请求开启蓝牙

    public static final int SCAN_SPECIFIC_DEVICE_DELAY_MILLIS = 10000;//搜索指定蓝牙设备超时时间

    public static final int AUTHENTICATE_DELAY_MILLIS = 10000;//认证超时时间

    public static final int CONNECT_DELAY_MILLIS = 10000;//连接设备超时时间

    public static final int SEND_SINGLE_COMMAND_DELAY_MILLIS = 3000;//发送单条指令超时时间

    public static final int CONFIG_PARAMS_DELAY_MILLIS = 20000;//发送配置参数指令超时时间

    public static final int CONFIG_PARAMS_LONG_DELAY_MILLIS = 30000;//发送配置参数指令超时时间

//    private NewBleManager bleViewModel.bleManager = NewBleManager.getInstance();

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothLeScannerCompat scanner;

    private ScanCallback scanCallback = new MdLeScanCallback();

    private boolean mScanning;

    private static int authenticateNum = 0;

    private boolean isAutoConnectBlue = true;//是否自动连接蓝牙

    public boolean isExitMode = false;

    protected static boolean isLogOutputMode = false;//设备是否打开了内部日志输出模式

    protected String errMsg = "";

    private String SN = MCloudApp.getCurDeviceToken();

    private String macAddress = MCloudApp.getCurDeviceMacAddr();

    private static Handler uiHander = new Handler();

    private static Handler heartHander = new Handler();

    private static ProgressRunnable progressRunnable;

    private static HeartRunnable heartRunnable;

    protected BleViewModel bleViewModel;


    protected void updateViewStateByConnectState(boolean isConnected){

    }

    private class ProgressRunnable implements Runnable {
        @Override
        public void run() {
            dismissProgressDialog();
            progressRunnable = null;

            if (!TextUtils.isEmpty(errMsg)) {
                ToastUtils.show(errMsg);

                if (errMsg.contains("连接超时") || errMsg.contains("认证超时")) {
                    disconnectDevice();
                    MCloudApp.setIsBluetoothDeviceConnected(false);
                    EventBus.getDefault().post(new BluetoothConnectStateEvent(false));
                }
            }
        }
    }

    protected void startProgressRunnable(String dialogContent, long delayMillis) {
        showProgressDialog(dialogContent, null, null);
        if (progressRunnable == null) {
            progressRunnable = new ProgressRunnable();
            uiHander.postDelayed(progressRunnable, delayMillis);
        }
    }

    public void stopProgressRunnable() {
        dismissProgressDialog();
        uiHander.removeCallbacksAndMessages(null);
        progressRunnable = null;
    }


    /**
     * 发送心跳包任务
     */
    private class HeartRunnable implements Runnable {
        @Override
        public void run() {
            if (MCloudApp.isIsBluetoothDeviceConnected()) {
                sendHeartData();
                heartHander.postDelayed(this, 30000);
            }
        }
    }

    protected void startHeartRunnable() {
        if (heartRunnable == null) {
            heartRunnable = new HeartRunnable();
            heartHander.postDelayed(heartRunnable, 10000);
        }
    }

    protected void stopHeartRunnable() {
        heartHander.removeCallbacksAndMessages(null);
        heartRunnable = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        dismissProgressDialog();
        bleViewModel.clearLastValue();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBluetooth();
        bleViewModel = getApplicationScopeViewModel(BleViewModel.class);
    }


    /**
     * 初始化蓝牙
     */
    private void initBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = Objects.requireNonNull(bluetoothManager).getAdapter();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bleViewModel.getBluetoothEventLiveData().observeInFragment(this, new Observer<BluetoothEvent>() {
            @Override
            public void onChanged(BluetoothEvent bluetoothEvent) {
                if (!isActive) {
                    return;
                }
                handleBluetoothEvent(bluetoothEvent);
            }
        });

    }

    /**
     * 搜索并连接指定的蓝牙设备
     */
    public void findAndConnectSpecificDevice() {
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

        checkBluetoothPermissions();
    }

    private void checkBluetoothPermissions() {
        XPermissionUtils.requestPermissionsResult(mActivity, 200, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        //搜索附近蓝牙设备，避免指定的设备不在蓝牙范围内
                        startDiscoveryDevice();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        boolean allNeverAskAgain = XPermissionUtils.isAllNeverAskAgain(mActivity, deniedPermissions);
                        // 所有的权限都被勾上不再询问时，跳转到应用设置界面，引导用户手动打开权限
                        if (allNeverAskAgain) {
                            XPermissionUtils.showRefusePermissionDialog(mActivity, GlobalUtil.getString(R.string.message_permission_bluetooth_location_rational));
                        } else {
                            ToastUtils.show(GlobalUtil.getString(R.string.message_permission_location_denied));
                        }
                    }
                });
    }

    /**
     * 扫描蓝牙设备，主要用来判断要连接的设备是否能被搜索到
     */
    private void startDiscoveryDevice() {
        //蓝牙已打开时，开始扫描蓝牙设备
        scanLeDevice(true);
        errMsg = "未搜索到此设备，请稍后尝试";
        startProgressRunnable("正在搜索设备：" + SN, SCAN_SPECIFIC_DEVICE_DELAY_MILLIS);

        //因设备问题会造成长时间搜索设备，在此操作过程中无法中断和进行其他操作，进度框会长时间在页面停留
        //新增操作返回，中断当前蓝牙操作并关闭进度框
        if (progressDialog != null) {
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    stopProgressRunnable();
                    scanLeDevice(false);
                }
            });
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            uiHander.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    scanner.stopScan(scanCallback);
                }
            }, SCAN_SPECIFIC_DEVICE_DELAY_MILLIS);

            mScanning = true;
            initScan();
        } else {
            mScanning = false;
            if (scanner != null)
                scanner.stopScan(scanCallback);
        }
    }

    private void initScan() {
        if (scanner == null) {
            scanner = BluetoothLeScannerCompat.getScanner();
        }
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        scanner.startScan(null, settings, scanCallback);
    }

    private class MdLeScanCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, @NonNull ScanResult result) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BluetoothDevice device = result.getDevice();
                    if (TextUtils.isEmpty(device.getName())) {
                        return;
                    }

                    if (device.getName().contains(SN)) {
                        stopProgressRunnable();
                        doConnect(device);
                    }
                }
            });
        }

        @Override
        public void onBatchScanResults(@NonNull List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    }

    /**
     * ble 建立连接
     */
    private void doConnect(BluetoothDevice device) {
        scanLeDevice(false);
        bleViewModel.bleManager.connectDevice(device, getContext());
        errMsg = "连接超时,请稍后尝试";
        startProgressRunnable("正在连接设备：" + SN, CONNECT_DELAY_MILLIS);
    }

    /**
     * ble 取消连接
     */
    public void disconnectDevice() {
        Timber.w("disconnectDevice() 调用");
        if (null != bleViewModel.bleManager) {
            bleViewModel.bleManager.disconnect();
            isAutoConnectBlue = false;//
            authenticateNum = 0;
        }
    }

    /**
     * 连接失败时，中断一会儿再连接
     */
    private void autoConnectAfterDisconnect() {
        try {
            Thread.sleep(1000);
            disconnectDevice();
            isAutoConnectBlue = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void handleBluetoothEvent(BluetoothEvent event) {
        switch (event.getEventType()) {
            case CONNECTED:
//                ByteManagerUtil.init(new MyOnBytePackage());
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
                    if (!TextUtils.isEmpty(msg)) {
                        handleResponseMessage(msg);
                    }
//                    byte[] data = (byte[]) msg.getBytes();
//                    if (data.length > 0) {
//                        ByteManagerUtil.getInstance().writeByte(data);
//                    }
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
                if (isLogOutputMode) {
                    try {
//                        byte[] data = ((String) event.getEventData()).getBytes();
//                        if (data.length > 0) {
//                            ByteManagerUtil.getInstance().writeByte(data);
//                        }

                        String msg = ((String) event.getEventData());
                        if (!TextUtils.isEmpty(msg)) {
                            handleResponseMessage(msg);
                        }
                    } catch (Exception ex) {
                        Timber.e(ex);
                    }
                } else {
                    switchLogOutputMode(false);
                }
                break;

            default:
                break;
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case Constants.BT_CONNECT:
                    stopProgressRunnable();
                    MCloudApp.setIsBluetoothDeviceConnected(true);
                    updateViewStateByConnectState(true);
                    errMsg = "认证超时,请稍后尝试";
                    startProgressRunnable("蓝牙已连接,设备认证中...", AUTHENTICATE_DELAY_MILLIS);
                    setAuthenticateWay();//蓝牙连接成功开始进行验证
                    break;

                case Constants.BT_DISCONNECTED:
                    ToastUtils.show("蓝牙连接断开");
                    stopProgressRunnable();
                    stopHeartRunnable();
                    MCloudApp.setIsBluetoothDeviceConnected(false);
                    updateViewStateByConnectState(false);
                    //断开蓝牙后重新连接
//                    if (isAutoConnectBlue) {
//                        MCloudApp.getMainHandler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                findAndConnectSpecificDevice();
//                            }
//                        }, 1500);
//                    }
                    break;

                case Constants.BT_MESSAGE_WRITE_FAIL:
                    ToastUtils.show("指令发送失败");
                    break;

                case Constants.BT_WRITE_TIME_OUT:
                    ToastUtils.show("指令发送超时");
//                    autoConnectAfterDisconnect();
                    disconnectDevice();
                    break;

                case Constants.VERIFY_RESULT:
                    stopProgressRunnable();
                    //设备认证失败处理
                    if (!msg.obj.equals("1")) {
                        if (authenticateNum < 4) {
                            MCloudApp.getMainHandler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    setAuthenticateWay();//重新认证
                                }
                            }, 1000);
                        } else {
                            ToastUtils.show("设备认证失败!");
                            disconnectDevice();
                        }
                        break;
                    } else {
                        authenticateNum = 0;
                        isAutoConnectBlue = true;//
                        if (BaseBleConnectFragment.this instanceof BleConfigDeviceFragment) {
                            errMsg = "查询设备配置参数超时，请尝试重新连接";
                            startProgressRunnable("初始化设备配置信息...", CONFIG_PARAMS_DELAY_MILLIS);
                            queryDeviceConfigInfoCmd();
                        }
                    }
                    break;

                case Constants.MESSAGE_RESPONSE_TIME_OUT://消息等待响应超时
//                    autoConnectAfterDisconnect();
                    disconnectDevice();
                    break;

                case Constants.BT_REQUEST_MTU_FAIL:
//                    ToastUtils.show("MTU请求设置失败！");
                    break;

                case Constants.BT_SERVICE_FIND_FAIL:
//                    ToastUtils.show("蓝牙服务发现失败！");
                    break;

                case Constants.BT_CHARACTERISTICS_FIND_FAIL:
//                    ToastUtils.show("蓝牙特征读取失败！");
                    break;

                case Constants.BT_ENABLE_READ_FAIL:
//                    ToastUtils.show("设置读取Descriptor失败！");
                    break;

                case Constants.MESSAGE_LOCK_REBOOT_DEVICE:
                    Timber.i("蓝牙通讯已就绪");
                    stopProgressRunnable();
                    authenticateNum = 0;
                    isAutoConnectBlue = true;//
                    if (BaseBleConnectFragment.this instanceof BleConfigDeviceFragment) {
                        errMsg = "查询设备配置参数超时，请尝试重新连接";
                        startProgressRunnable("初始化设备配置信息...", CONFIG_PARAMS_DELAY_MILLIS);
                        queryDeviceConfigInfoCmd();
                    }
                    break;

                default:
                    break;
            }

            return false;
        }
    });


    private void handleResponseMessage(final String cmdStr) {
        try {
            if (!cmdStr.startsWith("$$")) {
                Timber.w("不匹配标准响应头的应答指令===%s", cmdStr);
            } else {
                Timber.i("应答指令===%s", cmdStr);
            }

            String cmdArray[] = cmdStr.replace("\r\n", "").split(",");

            if (cmdStr.startsWith("$$224")) {//认证方式
                if (cmdStr.replace("\r\n", "").endsWith(CommandResult.ERROR_END)) {
                    setAuthenticateWay();//重新认证
                    return;
                }
                sendAuthenticateCodeCmd(cmdArray[3]);
                return;

            } else if (cmdStr.startsWith("$$223")) {//设备登录验证结果指令
                sendHandleMessage(Constants.VERIFY_RESULT, cmdArray[1]);
                Timber.d("设备登录验证状态===%s", cmdArray[1].contains("1"));
                return;

            } else if (cmdStr.contains("Please verify the equipment.\r\n")) {
                sendHandleMessage(Constants.VERIFY_RESULT, "0");
                return;

            } else if (cmdStr.contains("Equipment Verify OK.\r\n")) {
                sendHandleMessage(Constants.MESSAGE_LOCK_REBOOT_DEVICE, null);
                return;

            } else {
                //过滤掉不匹配标准响应头的应答指令
                if (!isLogOutputMode && !cmdStr.startsWith("$$")) {
                    return;
                }

                uiHander.post(new Runnable() {
                    @Override
                    public void run() {
                        parseResponseMessage(cmdStr);
                    }
                });
            }

        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

//    public class MyOnBytePackage implements OnBytePackage {
//        @Override
//        public void onPackageArrived(final byte[] data) {
//            try {
//                final String cmdStr = new String(data, StandardCharsets.UTF_8);
//                if (!cmdStr.startsWith("$$")) {
//                    Timber.w("不匹配标准响应头的应答指令===%s", cmdStr);
//                } else {
//                    Timber.i("应答指令===%s", cmdStr);
//                }
//
//                String cmdArray[] = cmdStr.replace("\r\n", "").split(",");
//
//                if (cmdStr.startsWith("$$224")) {//认证方式
//                    if (cmdStr.replace("\r\n", "").endsWith(CommandResult.ERROR_END)) {
//                        setAuthenticateWay();//重新认证
//                        return;
//                    }
//                    sendAuthenticateCodeCmd(cmdArray[3]);
//                    return;
//
//                } else if (cmdStr.startsWith("$$223")) {//设备登录验证结果指令
//                    sendHandleMessage(Constants.VERIFY_RESULT, cmdArray[1]);
//                    Timber.d("设备登录验证状态===%s", cmdArray[1].contains("1"));
//                    return;
//
//                } else if (cmdStr.contains("Please verify the equipment.\r\n")) {
//                    sendHandleMessage(Constants.VERIFY_RESULT, "0");
//                    return;
//
//                } else if (cmdStr.contains("Equipment Verify OK.\r\n")) {
//                    sendHandleMessage(Constants.MESSAGE_LOCK_REBOOT_DEVICE, null);
//                    return;
//
//                } else {
//                    //过滤掉不匹配标准响应头的应答指令
//                    if (!isLogOutputMode && !cmdStr.startsWith("$$")) {
//                        return;
//                    }
//
//                    uiHander.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            parserResult(cmdStr);
//                        }
//                    });
//                }
//
//            } catch (Exception ex) {
//                Timber.e(ex);
//            }
//        }
//    }

    /**
     * 解析设备的参数指令
     */
    protected void parseResponseMessage(String cmdStr) {
        if (SN.endsWith("T")) {//ADME 设备应答指令预处理
            //ADME 设备初始化参数查询完成后，取消进度框
            if (cmdStr.startsWith("$$7002")) {
                stopProgressRunnable();
            }

        } else if (SN.endsWith("L")) {//DAS 设备应答指令预处理
            //DAS 设备初始化参数查询完成后，取消进度框
            if (cmdStr.startsWith("$$000")) {
                stopProgressRunnable();
                startHeartRunnable();
            }
        }

        //处理心跳包应答指令，不分发指令
        if (cmdStr.startsWith("$$888")) {
            return;
        }

        //设置保存参数应答
        if (cmdStr.startsWith("$$0192") && cmdStr.endsWith("\r\n")) {
            ToastUtils.show("已保存");
            if (isExitMode) {
                uiHander.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mActivity.finish();
                    }
                }, 3000);
            }
            return;
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
        }
    }

    /**
     * 查询 ADME 设备的配置参数信息
     */
    private void queryADMEConfigInfoCmd() {
        sendCommonCommand("##7010\r\n");
        Timber.d("查询工作模式指令===##7010");

        ServerNumberEntity serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_ONE.toInt());
        String cmdAddress1 = CommandManager.getInstance().getCommand(CommandType.SERVER_ADDRESS, serverNumberEntity);
        sendCommonCommand(cmdAddress1);
        Timber.d("查询服务器地址1指令===%s", cmdAddress1);

        serverNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_TWO.toInt());
        String cmdAddress2 = CommandManager.getInstance().getCommand(CommandType.SERVER_ADDRESS, serverNumberEntity);
        sendCommonCommand(cmdAddress2);
        Timber.d("查询服务器地址2指令===%s", cmdAddress2);

        sendCommonCommand("##7000\r\n");
        Timber.d("查询采集器参数指令===##7000");

        sendCommonCommand("##7002\r\n");
        Timber.d("查询执行机构参数指令===##7002");
    }

    /**
     * 查询 DAS 设备的配置参数信息
     */
    private void queryDASConfigInfoCmd() {
        //获取基础配置信息  ##000
        String command = CommandManager.getInstance().getCommand(CommandType.BASE_CONFIG);
        sendCommonCommandImmediately(command);
        Timber.d("获取基础配置信息指令===%s", command);
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
    protected void sendCommonCommand(final String cmdStr) {
        final Message msg = new Message(UUID.randomUUID().toString(), cmdStr);
        uiHander.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (bleViewModel.bleManager != null) {
                    bleViewModel.bleManager.writeMessage(msg);
                }
            }
        }, 200);
    }

    /**
     * 马上发送蓝牙指令
     *
     * @param cmdStr
     */
    protected void sendCommonCommandImmediately(final String cmdStr) {
        final Message msg = new Message(UUID.randomUUID().toString(), cmdStr);
        if (bleViewModel.bleManager != null) {
            bleViewModel.bleManager.writeMessage(msg);
        }
    }

    /**
     * 蓝牙连接成功,发送认证方式
     */
    private void setAuthenticateWay() {
        if (authenticateNum >= 4) {
            Timber.w("达到最大设定认证次数");
            ToastUtils.show("设备认证失败!");
            disconnectDevice();
            return;
        }
        authenticateNum++;
        AuthenticationConfigEntity configEntity = new AuthenticationConfigEntity(SN, 0);
        String command = CommandManager.getInstance().getCommand(CommandType.AUTHENTICATION_CONFIG, configEntity);
        sendCommonCommandImmediately("\r\n" + command);
        Timber.d("设置认证类型指令===%s", command);
    }

    /**
     * 开始认证流程
     */
    private void sendAuthenticateCodeCmd(String authenticateParam) {
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
                sendCommonCommandImmediately(cmd);
                Timber.d("设备登录验证指令===%s", cmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存配置信息指令
     */
    protected void saveConfigInfo() {
        SaveConfigInfoEntity saveConfigInfoEntity = new SaveConfigInfoEntity(SaveConfigMode.SAVE_REBOOT.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.SAVE_CONFIG_INFO, saveConfigInfoEntity);

        errMsg = "发送指令超时,请稍后尝试";
        startProgressRunnable("正在发送保存重启指令...", CONFIG_PARAMS_DELAY_MILLIS);
        sendCommonCommandImmediately(command);
        Timber.d("发送保存配置重启设备指令===%s", command);
    }

    protected void saveConfigInfoNoReboot() {
        SaveConfigInfoEntity saveConfigInfoEntity = new SaveConfigInfoEntity(SaveConfigMode.SAVE_NO_REBOOT.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.SAVE_CONFIG_INFO, saveConfigInfoEntity);
        sendCommonCommandImmediately(command);
        Timber.d("发送保存配置不重启设备指令===%s", command);
    }

    /**
     * 打开/关闭设备低功耗模式
     */
    protected void setLowEnergyModel(boolean isOpen) {
        LowEnergyModelEntity entity = new LowEnergyModelEntity(isOpen ? LowEnergyModel.ACTIVATE.toInt() : LowEnergyModel.STANDBY.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.LOW_ENERGY, entity);
        sendCommonCommandImmediately(command);
        Timber.d("打开/关闭设备低功耗模式指令===%s", command);
    }

    /**
     * 打开/关闭设备日志通过蓝牙输出
     */
    protected void switchLogOutputMode(boolean isOpen) {
        LogOutputEntity logOutputEntity = new LogOutputEntity(isOpen ? LogOutputStatus.OPEN.toInt() : LogOutputStatus.CLOSE.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.LOG_OUTPUT_STATUS, logOutputEntity);
        sendCommonCommandImmediately(command);
        Timber.d("设置日志输出状态指令===%s", command);
    }


    protected void queryDeviceVersionInfo() {
        String command = CommandManager.getInstance().getCommand(CommandType.VERSION_MESSAGE);
        sendCommonCommandImmediately(command);
        Timber.d("查询设备版本信息：%s", command);
    }

    /**
     * 发送心跳数据(未定义的指令)
     */
    protected void sendHeartData() {
        String command = CommandManager.getInstance().getCommand(CommandType.HEARTBEAT);
        sendCommonCommandImmediately(command);
        Timber.d("发送心跳数据：%s", command);
    }

    protected void showDisconnectDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(Objects.requireNonNull(getContext()))
                .title("温馨提示：")
                .content(content)
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        disconnectDevice();
                        if (isExitMode) {
                            mActivity.finish();
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    protected void warnNotYetSettingBeforeLeavePage() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(Objects.requireNonNull(getContext()))
                .title("温馨提示：")
                .content("您已经修改了参数，还未配置到设备，确定离开页面吗？")
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        mActivity.finish();
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    private void warnNotYetRebootToSaveParam() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(Objects.requireNonNull(getContext()))
                .title("温馨提示")
                .content(GlobalUtil.getString(R.string.reboot_device_save_param_warn))
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("立即重启")
                .negativeText("稍后重启")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
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
                        if (isExitMode) {
                            mActivity.finish();
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {// 蓝牙已经开启
            if (resultCode != Activity.RESULT_OK) {
                ToastUtils.show("蓝牙未启用");
                return;
            }
            findAndConnectSpecificDevice();
        }
    }


}
