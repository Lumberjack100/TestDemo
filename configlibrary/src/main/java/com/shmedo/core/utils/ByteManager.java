package com.shmedo.core.utils;

import com.shmedo.core.annotations.ThreadSafe;
import com.shmedo.core.interfaces.OnBytePackage;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Liudongdong on 18/1/19.
 */
@ThreadSafe
public class ByteManager {
    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    private byte[] splitByte;
    private List<Byte> bytes;
    private OnBytePackage onBytePackage;
    private int maxLength;

    /**
     * 按照splitByte将字节数组分包 比如"\r\n".getBytes(ascii字符集)
     *
     * @param splitByte     拆包byte
     * @param onBytePackage 数据包事件处理,为了不阻塞蓝牙数据写入，该事件运行在后台线程中
     */
    public ByteManager(byte[] splitByte, OnBytePackage onBytePackage) {
        if (splitByte == null || splitByte.length < 1)
            throw new IllegalArgumentException("拆包字节数组不能为空");
        if (onBytePackage == null)
            throw new IllegalArgumentException("数据包监听程序不能为空");
        this.splitByte = splitByte;
        this.onBytePackage = onBytePackage;
        this.bytes = new LinkedList<>();
        this.maxLength = 4096;
    }

    /**
     * @param splitByte    拆包byte
     * @param onBytePackage 数据包事件处理,为了不阻塞蓝牙数据写入，该事件运行在后台线程中
     * @param maxLength     每个包的最大字节长度，如果超过此长度仍然没有遇到分隔符，则会报异常
     */
    public ByteManager(byte[] splitByte, OnBytePackage onBytePackage, int maxLength) {
        this(splitByte, onBytePackage);
        this.maxLength = maxLength;
    }

    /**
     * 蓝牙数据直接写入到ByteManager
     *
     * @param data 数据
     */
    public synchronized void writeByte(byte[] data) {
        for (byte b : data) {
            bytes.add(b);
        }
        checkPackage();
    }

    private void checkPackage() {
        int length = bytes.size();
        if (length < splitByte.length)
            return;
        byte[] resultByte = null;
        for (int i = 0; i < length; i++) {
            if (i + splitByte.length > length)
                break;
            byte[] tempByte = byteListToArray(bytes.subList(i, i + splitByte.length));
            if (Arrays.equals(splitByte, tempByte)) {
                List<Byte> packageByte = new LinkedList<>(bytes.subList(0, i + splitByte.length));
                this.remove(i + splitByte.length);
                resultByte = byteListToArray(packageByte);
                break;
            }
        }
        final byte[] finalResultByte = resultByte;
        //有数据包
        if (finalResultByte != null) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    onBytePackage.onPackageArrived(finalResultByte);
                }
            });
            checkPackage();
        } else {
            if (bytes.size() > maxLength)
                throw new RuntimeException("字节管理器长度：" + bytes.size() + "已经超过最大长度，仍然没有遇到分隔符");
        }
    }

    public synchronized byte[] remainBytes() {
        return byteListToArray(bytes);
    }

    public synchronized void clear() {
        bytes.clear();
    }

    public synchronized int currentByteLenth() {
        return bytes.size();
    }

    private void remove(int removeLength) {
        for (int i = 0; i < removeLength; ++i)
            bytes.remove(0);
    }

    private byte[] byteListToArray(List<Byte> bytes) {
        if (bytes == null || bytes.size() == 0)
            return null;
        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); ++i) {
            result[i] = bytes.get(i);
        }
        return result;
    }
}
