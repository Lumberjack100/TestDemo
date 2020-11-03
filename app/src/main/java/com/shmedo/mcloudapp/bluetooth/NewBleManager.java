package com.shmedo.mcloudapp.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.shmedo.configlibrary.ble.interfaces.OnBytePackage;
import com.shmedo.configlibrary.ble.utils.ByteManager;
import com.shmedo.mcloudapp.util.TimeUtil;
import com.shmedo.mcloudapp.util.bleutil.Constants;
import com.shmedo.mcloudapp.util.bleutil.DescriptorParser;
import com.shmedo.mcloudapp.util.bleutil.GattAttributes;
import com.shmedo.mcloudapp.util.bleutil.UUIDDatabase;

import org.greenrobot.eventbus.EventBus;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/10 <br/>
 * 描述：     TODO
 */
public class NewBleManager {
    /**
     * 每一个响应包的最大长度
     */
    private static final int MAX_LENGTH = 1024;
    /**
     * 响应包的拆分方式
     */
    private static final byte[] DEFAULT_SPLIT_BYTES = "\r\n".getBytes(StandardCharsets.UTF_8);

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
    private static final int WAIT_FOR_RESPONSE_TIME_OUT_SECOND = 120;

    private static NewBleManager bleManager = null;

    public static NewBleManager getInstance() {
        return bleManager;
    }

    public static void init() {
        if (bleManager == null) {
            bleManager = new NewBleManager();
        }

        bleManager.initCheck();
    }

    private BluetoothGatt mBluetoothGatt;
    private MdBluetoothGattCallback gattCallback = new MdBluetoothGattCallback();
    private ByteManager byteManager = new ByteManager(DEFAULT_SPLIT_BYTES, new OnBytePackageArrivedImpl(), MAX_LENGTH);
    private WriteMessageManager writeMessageManager = WriteMessageManager.getInstance();
    private ScheduledExecutorService scheduledExecutorService;
    private volatile boolean isReadable = false;
    private volatile boolean isWritable = true;
    private Object syncRoot = new Object();
    private Timestamp lastWriteTime;


    /**
     * 连接设备
     *
     * @param device  远程蓝牙设备
     * @param context Activity上下文
     */
    public void connectDevice(BluetoothDevice device, Context context) {
        if (mBluetoothGatt != null) {
            try {
                mBluetoothGatt.close();
            } catch (Exception ex) {
                Timber.e(ex);
            }
        }
        isReadable = false;
        mBluetoothGatt = device.connectGatt(context, false, gattCallback);
    }

    public void disconnect() {
        clearData();
        if (mBluetoothGatt == null) {
            return;
        }

        mBluetoothGatt.close();
        handleBluetoothEvent(BluetoothEventType.DISCONNECTED, null);
    }

    public void writeMessage(Message msg) {
        synchronized (syncRoot) {
            writeMessageManager.addMessage(msg);
        }
    }

    private void clearData() {
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
                    Timber.e(ex);
                }
            }
        }, CHECK_INTERVAL_MILLI, CHECK_INTERVAL_MILLI, TimeUnit.MILLISECONDS);
    }

    private void checkBluetoothStatus() {
        if ((!isReadable) || writeMessageManager.size() <= 0) {
            return;
        }

        if (checkWriteTimeout()) {
            handleBluetoothEvent(BluetoothEventType.WRITE_TIME_OUT, null);
            return;
        }

        if (checkResponseTimeout()) {
            handleBluetoothEvent(BluetoothEventType.MESSAGE_RESPONSE_TIME_OUT, null);
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
                    if (!TextUtils.isEmpty(msg.getResponseMessage())) {
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
                    Timber.e("不可写，最后写入时间却为NULL，状态异常");
                    eventType = BluetoothEventType.STATE_EXCEPTION;
                } else {
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    int second = TimeUtil.sencondBetweenTimestamp(lastWriteTime, now);
                    if (second >= WRITE_TIME_OUT_SECOND) {
                        result = true;
                    } else {
                        result = false;
                    }
                }
            }
        }

        if (eventType != null) {
            handleBluetoothEvent(eventType, null);
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
            handleBluetoothEvent(BluetoothEventType.STATE_EXCEPTION, null);
            return false;
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        int second = TimeUtil.sencondBetweenTimestamp(lastWriteTime, now);
        if (second >= WAIT_FOR_RESPONSE_TIME_OUT_SECOND) {
            return true;
        } else {
            return false;
        }
    }

    private void handleBluetoothEvent(BluetoothEventType eventType, Object eventData) {
        BluetoothEvent event = BluetoothEvent.builder()
                .setEventType(eventType)
                .setEventData(eventData)
                .build();

        notifyBluetoothEvent(event);
    }

    private void fireEvent(final BluetoothEvent event) {
        notifyBluetoothEvent(event);
    }

    private void notifyBluetoothEvent(final BluetoothEvent event) {
//        Timber.d(event.getEventType().name() + "->Thread Name: " + Thread.currentThread().getName() + ",Thread Id: " + Thread.currentThread().getId());
        EventBus.getDefault().post(event);
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
                    gatt.requestMtu(PACKAGE_SIZE);//
                } else {
                    throw new RuntimeException("运行版本太低");
                }
                eventType = BluetoothEventType.CONNECTED;
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED || newState == BluetoothProfile.STATE_DISCONNECTING) {
                eventType = BluetoothEventType.DISCONNECTED;
                gatt.close();
            }

            handleBluetoothEvent(eventType, null);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            // && mtu == PACKAGE_SIZE
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt.discoverServices();
            } else {
                Timber.e("MTU设置失败");
                handleBluetoothEvent(BluetoothEventType.REQUEST_MTU_FAIL, null);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                handleBluetoothEvent(BluetoothEventType.SERVICE_FIND_FAIL, null);
                return;
            }

            List<BluetoothGattService> serviceList = gatt.getServices();
            BluetoothGattService usrGattService = null;
            for (BluetoothGattService gattService : serviceList) {
                String uuid = gattService.getUuid().toString();
                if (uuid.equals(GattAttributes.GENERIC_ACCESS_SERVICE) || uuid.equals(GattAttributes.GENERIC_ATTRIBUTE_SERVICE))
                    continue;
                String name = GattAttributes.lookup(uuid, "UnkonwService");
                if (USR_SERVICE.equals(name)) {
                    usrGattService = gattService;
                }
            }
            if (usrGattService == null) {
                handleBluetoothEvent(BluetoothEventType.SERVICE_FIND_FAIL, null);
                return;
            }

            List<BluetoothGattCharacteristic> characteristicList = usrGattService.getCharacteristics();
            if (characteristicList == null || characteristicList.size() != 2) {
                handleBluetoothEvent(BluetoothEventType.CHARACTERISTICS_FIND_FAIL, null);
                return;
            }
            readCharacteristic = characteristicList.get(0);
            writeCharacteristic = characteristicList.get(1);
            enableRead(gatt, readCharacteristic);
        }

        private void enableRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
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
//                    String result = new String(value, StandardCharsets.UTF_8);
//                    Timber.d("onCharacteristicChanged:%s", result);
                    byteManager.writeByte(value);
                } catch (Exception ex) {
                    Timber.e(ex);
                    //消息达到最大的字节数，仍然没有遇到完整包
                    handleBluetoothEvent(BluetoothEventType.STATE_EXCEPTION, ex);
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

            handleBluetoothEvent(eventType, null);
        }

        public void writeData(byte[] data) {
//            Timber.d("调用writeCharacteristic");
            if (data == null || data.length == 0)
                return;
            if (writeCharacteristic == null || mBluetoothGatt == null) {
                Timber.w("没有读取到特征或者gatt,无法写入数据");
                return;
            }
            writeCharacteristic.setValue(data);
            mBluetoothGatt.writeCharacteristic(writeCharacteristic);
        }
    }

    private class OnBytePackageArrivedImpl implements OnBytePackage {
        private final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

        @Override
        public void onPackageArrived(byte[] data) {
            String result = new String(data, DEFAULT_CHARSET);
            Message message = NewBleManager.this.writeMessageManager.getFront();
            if (message != null) {
                //消息还没发完，但是响应已经来了
                if (!message.isDone()) {
                    handleBluetoothEvent(BluetoothEventType.STATE_EXCEPTION, null);
                } else {
                    //消息已经发完
                    message.setResponseMessage(result);
                }
            } else {
                //响应来了，但是队列里没有消息
                handleBluetoothEvent(BluetoothEventType.RESPONSE_WITH_NO_MESSAGE, result);
            }
        }
    }
}
