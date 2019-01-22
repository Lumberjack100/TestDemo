package com.shmedo.mcloudapp.bluetooth;

import com.shmedo.mcloudapp.util.bleutil.BleHelpUtil;
import java.sql.Timestamp;
import java.util.Arrays;

/**
 * Created by Liudongdong on 18/2/5.
 * 蓝牙发送消息，封装了自动拆包发送，无论原始消息多大，每包限定最多发送200字节
 */

public class Message {
    /**
     * 往蓝牙写入数据包的最大包大小
     */
    private static final int BLUETOOTH_MAX_WRITE_PACKAGE_SIZE = 200;
    /**
     * UUID
     */
    private String messageID;
    /**
     * 待写入蓝牙数据，不可为空
     */
    private String data;
    /**
     * 消息发送状态
     */
    private MessageWriteStatus status;
    /**
     * 当前待发送包序号
     */
    private int currentWriteIndex;
    private Timestamp lastWriteTime;
    /**
     * 原始消息的字节数组
     */
    private byte[] messageBytes;
    /**
     * 该消息是否需要接收响应
     */
    private boolean needResponse;
    /**
     * 响应消息
     */
    private String responseMessage;


    /**
     * 待发送蓝牙数据
     *
     * @param messageID 消息ID，UUID.toString()文字
     * @param data      待写入蓝牙数据
     */
    public Message(String messageID, String data) {
        this.messageID = messageID;
        this.data = data;
        this.status = MessageWriteStatus.TO_BE_SEND;
        this.needResponse = true;
        try {
            this.messageBytes = data.getBytes("us-ascii");
            this.currentWriteIndex = 0;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @param messageID    消息ID，UUID.toString()
     * @param data         写入蓝牙的数据
     * @param needResponse 该消息是否需要接收响应,必须为true
     */
    public Message(String messageID, String data, boolean needResponse) {
        this(messageID, data);
        this.needResponse = true;
    }

    public String getMessageID() {
        return messageID;
    }

    public String getData() {
        return data;
    }

    public MessageWriteStatus getStatus() {
        return status;
    }

    public void setStatus(MessageWriteStatus status) {
        this.status = status;
    }

    public Timestamp getLastWriteTime() {
        return lastWriteTime;
    }

    public void setLastWriteTime(Timestamp lastWriteTime) {
        this.lastWriteTime = lastWriteTime;
    }

    public boolean isNeedResponse() {
        return needResponse;
    }

    public void setNeedResponse(boolean needResponse) {
        this.needResponse = needResponse;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public boolean isWriteTimeout(int timeoutSecond) {
        if (lastWriteTime == null) {
            return false;
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        int second = BleHelpUtil.sencondBetweenTimestamp(lastWriteTime, now);
        return second > timeoutSecond;
    }

    public byte[] getCurrentWriteBytes() {
        if (isDone()) {
            return null;
        }
        int beginIndex = currentWriteIndex * BLUETOOTH_MAX_WRITE_PACKAGE_SIZE;
        int endIndex = ((currentWriteIndex + 1) * BLUETOOTH_MAX_WRITE_PACKAGE_SIZE)
                           > messageBytes.length ?
                       messageBytes.length : ((currentWriteIndex + 1) * BLUETOOTH_MAX_WRITE_PACKAGE_SIZE);
        byte[] tempByte = Arrays.copyOfRange(this.messageBytes, beginIndex, endIndex);
        currentWriteIndex++;
        return tempByte;
    }

    /**
     * 所有数据已经写入蓝牙
     *
     * @return
     */
    public boolean isDone() {
        return currentWriteIndex >= totalPackage();
    }

    /**
     * 所有数据已经写入蓝牙，且蓝牙状态已经返回
     *
     * @return
     */
    public boolean isComplete() {
        return this.status == MessageWriteStatus.SUCCESS || this.status == MessageWriteStatus.FAIL;
    }

    private int totalPackage() {
        return (this.messageBytes.length - 1) / BLUETOOTH_MAX_WRITE_PACKAGE_SIZE + 1;
    }


    public static enum MessageWriteStatus {
        /**
         * 发送成功
         */
        SUCCESS,
        /**
         * 发送失败
         */
        FAIL,
        /**
         * 待发送
         */
        TO_BE_SEND,
        /**
         * 发送中
         */
        SENDING
    }
}
