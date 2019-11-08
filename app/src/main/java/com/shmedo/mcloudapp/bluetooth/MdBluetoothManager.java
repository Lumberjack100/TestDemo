package com.shmedo.mcloudapp.bluetooth;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.shmedo.das.utils.ByteManager;
import com.shmedo.das.utils.OnBytePackage;
import com.shmedo.mcloudapp.bluetooth.exception.ScanAlreadyStartException;
import com.shmedo.mcloudapp.entity.ble.MDevice;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.bleutil.BleHelpUtil;
import com.shmedo.mcloudapp.util.bleutil.Constants;
import com.shmedo.mcloudapp.util.bleutil.DescriptorParser;
import com.shmedo.mcloudapp.util.bleutil.GattAttributes;
import com.shmedo.mcloudapp.util.bleutil.LogTag;
import com.shmedo.mcloudapp.util.bleutil.ThreadUtil;
import com.shmedo.mcloudapp.util.bleutil.UUIDDatabase;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Created by Liudongdong on 18/1/31.
 */

public class MdBluetoothManager {
    /**
     * 每一个响应包的最大长度
     */
    private static final int MAX_LENGTH = 1024;
    /**
     * 响应包的拆分方式
     */
    private static final byte[] DEFAULT_SPLIT_BYTES = "\r\n".getBytes(StandardCharsets.UTF_8);

    private static final int ENABLE_PERMISSION = 1;
    /**
     * 50毫秒检查一次
     */
    private static final int CHECK_INTERVAL_MILLI = 50;
    /**
     * 等待蓝牙写入超时时间
     */
    private static final int WRITE_TIME_OUT_SECOND = 30;
    /**
     * 等待消息响应的超时时间
     */
    private static final int WAIT_FOR_RESPONSE_TIME_OUT_SECOND = 60;

    private static MdBluetoothManager bluetoothManager;

    public static MdBluetoothManager getInstance() {
        return bluetoothManager;
    }

    public static void init(BluetoothAdapter bluetoothAdapter, BluetoothManager androidBluetoothManager) {
        if (bluetoothManager == null) {
            bluetoothManager = new MdBluetoothManager(bluetoothAdapter, androidBluetoothManager);
        }

        bluetoothManager.initCheck();
    }

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager androidBluetoothManager;
    private BluetoothEventHandler eventHandler;
    private volatile boolean scan = false;
    private BluetoothAdapter.LeScanCallback leScanCallback = new MdLeScanCallback();
    private List<MDevice> devices;
    private BluetoothGatt gatt;
    private BluetoothDevice currentDevice;
    private MdBluetoothGattCallback gattCallback = new MdBluetoothGattCallback();
    private ByteManager byteManager = new ByteManager(DEFAULT_SPLIT_BYTES, new OnBytePackageArrivedImpl(), MAX_LENGTH);
    private WriteMessageManager writeMessageManager = WriteMessageManager.getInstance();
    private ScheduledExecutorService scheduledExecutorService;
    private volatile boolean isReadable = false;
    private volatile boolean isWritable = true;
    private Object syncRoot = new Object();
    private Timestamp lastWriteTime;

    private MdBluetoothManager(BluetoothAdapter bluetoothAdapter, BluetoothManager androidBluetoothManager) {
        if (bluetoothAdapter == null) {
            throw new IllegalArgumentException("蓝牙适配器不能为null");
        }
        this.bluetoothAdapter = bluetoothAdapter;
        this.androidBluetoothManager = androidBluetoothManager;
    }

    /**
     * 关联蓝牙事件处理程序
     *
     * @param eventHandler
     */
    public void setEventHandler(BluetoothEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public boolean isBluetoothEnable() {
        return this.bluetoothAdapter.enable();
    }

    /**
     * 当前设备是否连接
     *
     * @return
     */
    public boolean connected() {
        if (currentDevice == null || gatt == null) {
            return false;
        }
        return androidBluetoothManager.getConnectionState(currentDevice, BluetoothGatt.GATT)
                == BluetoothProfile.STATE_CONNECTED;
    }

    /**
     * 开始扫描蓝牙设备
     *
     * @param maxScanSecond 最大扫描时间，到时间后自动停止
     * @param activity
     */
    @RunOnUiThread
    public void scanDevice(int maxScanSecond, final Activity activity) {
        if (scan) {
            throw new ScanAlreadyStartException();
        }
        ThreadUtil.checkRunOnUiThread();
        checkPermission(activity);
        clearData();
        scan = true;
        bluetoothAdapter.startLeScan(leScanCallback);
        Executors.newScheduledThreadPool(1)
                .schedule(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (scan) {
                                        scan = false;
                                        bluetoothAdapter.stopLeScan(leScanCallback);
                                    }
                                }
                            });
                        } catch (Exception ex) {
                            Log.e(LogTag.ERROR_TAG, ex.getMessage(), ex);
                        }
                    }
                }, maxScanSecond, TimeUnit.SECONDS);
    }

    /**
     * 立即停止蓝牙扫描
     */
    @RunOnUiThread
    public void stopScan() {
        ThreadUtil.checkRunOnUiThread();
        if (scan) {
            scan = false;
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    /**
     * 连接设备
     *
     * @param device  远程蓝牙设备
     * @param context Activity上下文
     */
    public void connectDevice(BluetoothDevice device, Context context) {
        if (gatt != null) {
            try {
                gatt.close();
            } catch (Exception ex) {
                Log.e(LogTag.ERROR_TAG, ex.getMessage(), ex);
            }
        }
        isReadable = false;
        gatt = device.connectGatt(context, false, gattCallback);
        currentDevice = device;
    }

    public void disconnect() {
        clearData();
        if (gatt == null) {
            return;
        }
        gatt.disconnect();
        currentDevice = null;


        if (eventHandler == null)
            return;
        BluetoothEvent event = BluetoothEvent.builder()
                .setEventType(BluetoothEventType.DISCONNECTED)
                .build();
        eventHandler.handle(event);
    }

    public void writeMessage(Message msg) {
        synchronized (syncRoot) {
            writeMessageManager.addMessage(msg);
        }
    }

    @RunOnUiThread
    private void checkPermission(Activity context) {
        ThreadUtil.checkRunOnUiThread();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if ((context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                    (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                context.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, ENABLE_PERMISSION);
                context.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ENABLE_PERMISSION);
            }
        }
    }

    private void clearData() {
        scan = false;
        devices = new LinkedList<>();
        currentDevice = null;
        writeMessageManager.clear();
        isReadable = false;
        lastWriteTime = null;
        isWritable = true;
        byteManager.clear();
    }

    private void initCheck() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    checkBluetoothStatus();
                } catch (Exception ex) {
                    Log.e(LogTag.ERROR_TAG, ex.getMessage(), ex);
                }
            }
        }, CHECK_INTERVAL_MILLI, CHECK_INTERVAL_MILLI, TimeUnit.MILLISECONDS);
    }


    private void checkBluetoothStatus() {
        if ((!isReadable) || writeMessageManager.size() <= 0) {
            return;
        }
        if (checkWriteTimeout()) {
            BluetoothEvent event = BluetoothEvent.withEventType(BluetoothEventType.WRITE_TIME_OUT);
            fireEvent(event);
            return;
        }
        if (checkResponseTimeout()) {
            BluetoothEvent event = BluetoothEvent.withEventType(BluetoothEventType.MESSAGE_RESPONSE_TIME_OUT);
            fireEvent(event);
            return;
        }
        Message msg = null;
        BluetoothEvent event = null;
        byte[] writeBytes = null;
        synchronized (syncRoot) {
            msg = writeMessageManager.getFront();
            if (msg.isComplete()) {
                if (msg.getStatus() == Message.MessageWriteStatus.FAIL) {
                    BluetoothEventType eventType = BluetoothEventType.MESSAGE_WRITE_FAIL;
                    event = new BluetoothEvent(eventType, msg);
                    writeMessageManager.removeFront();
                } else {
                    if (!StringUtil.isEmpty(msg.getResponseMessage())) {
                        event = BluetoothEvent.builder().setEventType(BluetoothEventType.MESSAGE_WRITE_SUCCESS)
                                .setEventData(msg)
                                .build();
                        writeMessageManager.removeFront();
                    }
                }
            } else {
                //当前蓝牙可写
                if (isWritable) {
                    writeBytes = msg.getCurrentWriteBytes();
                    if (writeBytes != null) {
                        isWritable = false;
                        gattCallback.writeData(writeBytes);
                        Timestamp now = new Timestamp(System.currentTimeMillis());
                        msg.setLastWriteTime(now);
                        msg.setStatus(Message.MessageWriteStatus.SENDING);
                        lastWriteTime = now;
                    } else {
                        //消息没发完，蓝牙可写，待发送字节为NULL
                        event = BluetoothEvent.withEventType(BluetoothEventType.STATE_EXCEPTION);
                    }
                    //当前蓝牙不可写
                } else {
                    if (msg.isWriteTimeout(WRITE_TIME_OUT_SECOND)) {
                        event = BluetoothEvent.withEventType(BluetoothEventType.WRITE_TIME_OUT);
                    }
                }
            }
        }
        if (event != null) {
            fireEvent(event);
        }
    }

    private boolean checkWriteTimeout() {
        boolean result = false;
        BluetoothEventType eventType = null;
        synchronized (syncRoot) {
            if (isWritable) {
                result = false;
            } else {
                //已经写入了，在等待返回
                if (lastWriteTime == null) {
                    Log.e(LogTag.ERROR_TAG, "不可写，最后写入时间却为NULL，状态异常");
                    eventType = BluetoothEventType.STATE_EXCEPTION;
                } else {
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    int second = BleHelpUtil.sencondBetweenTimestamp(lastWriteTime, now);
                    if (second >= WRITE_TIME_OUT_SECOND) {
                        result = true;
                    } else {
                        result = false;
                    }
                }
            }
        }
        if (eventType != null) {
            fireEvent(BluetoothEvent.withEventType(eventType));
        }
        return result;
    }

    private boolean checkResponseTimeout() {
        Message message = writeMessageManager.getFront();
        if (message == null) {
            return false;
        }
        if (!message.isComplete()) {
            return false;
        }
        //消息写完了，最后的写入时间却为null
        if (lastWriteTime == null) {
            BluetoothEvent bluetoothEvent = BluetoothEvent.withEventType(BluetoothEventType.STATE_EXCEPTION);
            fireEvent(bluetoothEvent);
            return false;
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        int second = BleHelpUtil.sencondBetweenTimestamp(lastWriteTime, now);
        if (second >= WAIT_FOR_RESPONSE_TIME_OUT_SECOND) {
            return true;
        } else {
            return false;
        }
    }

    private void fireEvent(final BluetoothEvent event) {
        if (eventHandler != null) {
            eventHandler.handle(event);
        } else {
            Log.w(LogTag.WARN_TAG, "事件处理程序为NULL");
        }
    }


    private class MdLeScanCallback implements BluetoothAdapter.LeScanCallback {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            MDevice mDev = new MDevice(device, rssi);
            if (devices.contains(mDev))
                return;
            devices.add(mDev);
            if (eventHandler == null)
                return;
            BluetoothEvent event = BluetoothEvent.builder()
                    .setEventType(BluetoothEventType.DEVICE_FIND)
                    .setEventData(new BluetoothDeviceFindEventData(mDev, devices))
                    .build();
            eventHandler.handle(event);
        }
    }

    private class MdBluetoothGattCallback extends BluetoothGattCallback {
        private static final int PACKAGE_SIZE = 512;
        private static final String USR_SERVICE = "USR Service";
        //        private List<String> writeMessages = new LinkedList<>();
        private BluetoothGattCharacteristic readCharacteristic;
        private BluetoothGattCharacteristic writeCharacteristic;

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            BluetoothEventType eventType = null;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    requestMtu(gatt, PACKAGE_SIZE);
                } else {
                    throw new RuntimeException("运行版本太低");
                }
                eventType = BluetoothEventType.CONNECTED;
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED ||
                    newState == BluetoothProfile.STATE_DISCONNECTING) {
                eventType = BluetoothEventType.DISCONNECTED;
                gatt.close();
            }
            if (eventHandler == null || eventType == null)
                return;
            BluetoothEvent event = BluetoothEvent.builder()
                    .setEventType(eventType)
                    .build();
            eventHandler.handle(event);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private void requestMtu(BluetoothGatt gatt, int size) {
            gatt.requestMtu(size);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            // && mtu == PACKAGE_SIZE
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt.discoverServices();
            } else {
                Log.w(LogTag.WARN_TAG, "MTU设置失败");
                if (eventHandler == null)
                    return;
                BluetoothEvent event = BluetoothEvent.builder()
                        .setEventType(BluetoothEventType.REQUEST_MTU_FAIL)
                        .build();
                eventHandler.handle(event);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                BluetoothEvent event = BluetoothEvent.builder()
                        .setEventType(BluetoothEventType.SERVICE_FIND_FAIL)
                        .build();
                if (eventHandler != null) {
                    eventHandler.handle(event);
                }
                return;
            }
            List<BluetoothGattService> serviceList = gatt.getServices();
            BluetoothGattService usrGattService = null;
            for (BluetoothGattService gattService : serviceList) {
                String uuid = gattService.getUuid().toString();
                if (uuid.equals(GattAttributes.GENERIC_ACCESS_SERVICE) || uuid.equals(GattAttributes.GENERIC_ATTRIBUTE_SERVICE))
                    continue;
                String name = GattAttributes.lookup(gattService.getUuid().toString(), "UnkonwService");
                if (USR_SERVICE.equals(name)) {
                    usrGattService = gattService;
                }
            }
            if (usrGattService == null) {
                BluetoothEvent event = BluetoothEvent.builder()
                        .setEventType(BluetoothEventType.SERVICE_FIND_FAIL)
                        .build();
                if (eventHandler != null) {
                    eventHandler.handle(event);
                }
                return;
            }
            List<BluetoothGattCharacteristic> characteristicList = usrGattService.getCharacteristics();
            if (characteristicList == null || characteristicList.size() != 2) {
                BluetoothEvent event = BluetoothEvent
                        .withEventType(BluetoothEventType.CHARACTERISTICS_FIND_FAIL);
                if (eventHandler != null) {
                    eventHandler.handle(event);
                }
                return;
            }
            readCharacteristic = characteristicList.get(0);
            writeCharacteristic = characteristicList.get(1);
            enableRead(gatt, readCharacteristic);
        }

        private void enableRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID
                    .fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
            gatt.setCharacteristicNotification(characteristic, true);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            synchronized (syncRoot) {
                isWritable = true;
                Message msg = writeMessageManager.getFront();
                if (msg == null) {
                    return;
                }
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (msg.isDone()) {
                        msg.setStatus(Message.MessageWriteStatus.SUCCESS);
                    }
                } else {
                    //蓝牙数据写入失败
                    msg.setStatus(Message.MessageWriteStatus.FAIL);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] value = characteristic.getValue();
            if (value != null && value.length > 0) {
                //                BluetoothEvent event = new BluetoothEvent(BluetoothEventType.DATA_AVAILABLE, value);
                //                fireEvent(event);
                try {
                    byteManager.writeByte(value);
                } catch (Exception ex) {
                    //消息达到最大的字节数，仍然没有遇到完整包
                    BluetoothEvent event = BluetoothEvent.builder()
                            .setEventData(ex)
                            .setEventType(BluetoothEventType.STATE_EXCEPTION)
                            .build();
                    fireEvent(event);
                }
            }
        }


        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            System.out.println("onDescriptorRead ------------------->GATT_SUCC");

            if (status == BluetoothGatt.GATT_SUCCESS) {
                UUID descriptorUUID = descriptor.getUuid();
                Bundle mBundle = new Bundle();
                // Putting the byte value read for GATT Db
                mBundle.putByteArray(Constants.EXTRA_DESCRIPTOR_BYTE_VALUE,
                        descriptor.getValue());


                mBundle.putString(Constants.EXTRA_DESCRIPTOR_BYTE_VALUE_UUID,
                        descriptor.getUuid().toString());
                mBundle.putString(Constants.EXTRA_DESCRIPTOR_BYTE_VALUE_CHARACTERISTIC_UUID,
                        descriptor.getCharacteristic().getUuid().toString());
                if (descriptorUUID.equals(UUIDDatabase.UUID_CLIENT_CHARACTERISTIC_CONFIG)) {
                    String valueReceived = DescriptorParser
                            .getClientCharacteristicConfiguration(descriptor);
                    mBundle.putString(Constants.EXTRA_DESCRIPTOR_VALUE, valueReceived);
                }
                if (descriptorUUID.equals(UUIDDatabase.UUID_CHARACTERISTIC_EXTENDED_PROPERTIES)) {
                    HashMap<String, String> receivedValuesMap = DescriptorParser
                            .getCharacteristicExtendedProperties(descriptor);
                    String reliableWriteStatus = receivedValuesMap.get(Constants.firstBitValueKey);
                    String writeAuxillaryStatus = receivedValuesMap.get(Constants.secondBitValueKey);
                    mBundle.putString(Constants.EXTRA_DESCRIPTOR_VALUE, reliableWriteStatus + "\n"
                            + writeAuxillaryStatus);
                }
                if (descriptorUUID.equals(UUIDDatabase.UUID_CHARACTERISTIC_USER_DESCRIPTION)) {
                    String description = DescriptorParser
                            .getCharacteristicUserDescription(descriptor);
                    mBundle.putString(Constants.EXTRA_DESCRIPTOR_VALUE, description);
                }
                if (descriptorUUID.equals(UUIDDatabase.UUID_SERVER_CHARACTERISTIC_CONFIGURATION)) {
                    String broadcastStatus = DescriptorParser.
                            getServerCharacteristicConfiguration(descriptor);
                    mBundle.putString(Constants.EXTRA_DESCRIPTOR_VALUE, broadcastStatus);
                }
                if (descriptorUUID.equals(UUIDDatabase.UUID_REPORT_REFERENCE)) {
                    ArrayList<String> reportReferencealues = DescriptorParser.getReportReference(descriptor);
                    String reportReference;
                    String reportReferenceType;
                    if (reportReferencealues.size() == 2) {
                        reportReference = reportReferencealues.get(0);
                        reportReferenceType = reportReferencealues.get(1);
                        mBundle.putString(Constants.EXTRA_DESCRIPTOR_REPORT_REFERENCE_ID, reportReference);
                        mBundle.putString(Constants.EXTRA_DESCRIPTOR_REPORT_REFERENCE_TYPE, reportReferenceType);
                        mBundle.putString(Constants.EXTRA_DESCRIPTOR_VALUE, reportReference + "\n" +
                                reportReferenceType);
                    }

                }
                if (descriptorUUID.equals(UUIDDatabase.UUID_CHARACTERISTIC_PRESENTATION_FORMAT)) {
                    String value = DescriptorParser.getCharacteristicPresentationFormat(descriptor);
                    mBundle.putString(Constants.EXTRA_DESCRIPTOR_VALUE,
                            value);
                }
                String str = "";
            } else {
                System.out.println("onDescriptorRead ------------------->GATT_FAIL");
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            BluetoothEventType eventType = null;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                eventType = BluetoothEventType.ENABLE_READ_SUCCESS;
                if (descriptor.getUuid().equals(UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG))) {
                    isReadable = true;
                }
            } else {
                eventType = BluetoothEventType.ENABLE_READ_FAIL;
            }
            if (eventHandler == null)
                return;
            eventHandler.handle(BluetoothEvent.withEventType(eventType));
        }

        public void writeData(byte[] data) {
            if (data == null || data.length == 0)
                return;
            if (writeCharacteristic == null || gatt == null) {
                Log.w(LogTag.WARN_TAG, "没有读取到特征或者gatt,无法写入数据");
                return;
            }
            writeCharacteristic.setValue(data);
            gatt.writeCharacteristic(writeCharacteristic);
        }
    }

    private class OnBytePackageArrivedImpl implements OnBytePackage {
        private final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

        @Override
        public void onPackageArrived(byte[] data) {
            String result = new String(data, DEFAULT_CHARSET);
            Message message = MdBluetoothManager.this.writeMessageManager.getFront();
            if (message != null) {
                //消息还没发完，但是响应已经来了
                if (!message.isDone()) {
                    BluetoothEvent event = BluetoothEvent.withEventType(BluetoothEventType.STATE_EXCEPTION);
                    MdBluetoothManager.this.fireEvent(event);
                } else {
                    //消息已经发完
                    message.setResponseMessage(result);
                }
            } else {
                //响应来了，但是队列里没有消息
                BluetoothEvent bluetoothEvent = BluetoothEvent.builder()
                        .setEventData(result)
                        .setEventType(BluetoothEventType.RESPONSE_WITH_NO_MESSAGE)
                        .build();
                MdBluetoothManager.this.fireEvent(bluetoothEvent);
            }
        }
    }
}
