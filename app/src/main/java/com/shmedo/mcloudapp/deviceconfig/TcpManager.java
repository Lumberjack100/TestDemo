package com.shmedo.mcloudapp.deviceconfig;

import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.littlegreens.netty.client.NettyTcpClient;
import com.littlegreens.netty.client.listener.MessageStateListener;
import com.littlegreens.netty.client.listener.NettyClientListener;
import com.littlegreens.netty.client.status.ConnectState;
import com.shmedo.mcloudapp.deviceconfig.model.TcpConnectionState;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/4 <br/>
 * 描述：     TODO
 */
public class TcpManager implements NettyClientListener<String> {

    private UnPeekLiveData<TcpConnectionState> tcpConnectionState;

    private UnPeekLiveData<String> receivedMessage;

    private NettyTcpClient mNettyTcpClient;

    public TcpManager() {

    }

    public void initTcpClient(String host, int port) {
        mNettyTcpClient = new NettyTcpClient.Builder()
                .setHost(host)    //设置服务端地址
                .setTcpPort(port) //设置服务端端口号
                .setMaxReconnectTimes(5)    //设置最大重连次数
                .setReconnectIntervalTime(5)    //设置重连间隔时间。单位：秒
                .setSendheartBeat(true) //设置是否发送心跳
                .setHeartBeatInterval(5)    //设置心跳间隔时间。单位：秒
                .setHeartBeatData("I'm HeartBeatData") //设置心跳数据，可以是String类型，也可以是byte[]，以后设置的为准
                .setIndex(0)    //设置客户端标识.(因为可能存在多个tcp连接)
//                .setPacketSeparator("#")//用特殊字符，作为分隔符，解决粘包问题，默认是用换行符作为分隔符
                .setMaxPacketLong(1024)//设置一次发送数据的最大长度，默认是1024
                .build();

        mNettyTcpClient.setListener(this); //设置TCP监听
    }

    public ProtectedUnPeekLiveData<TcpConnectionState> getTcpConnectionState() {
        if (tcpConnectionState == null) {
            tcpConnectionState = new UnPeekLiveData<>();
        }
        return tcpConnectionState;
    }

    public ProtectedUnPeekLiveData<String> getReceivedMessage() {
        if (receivedMessage == null) {
            receivedMessage = new UnPeekLiveData.Builder<String>()
                    .setAllowNullValue(false)
                    .create();
        }

        return receivedMessage;
    }

    @Override
    public void onMessageResponseClient(String msg, int index) {
        Timber.d("onMessageResponseClient:%s", msg);
        receivedMessage.postValue(msg);
    }

    @Override
    public void onClientStatusConnectChanged(int statusCode, int index) {
        if (statusCode == ConnectState.STATUS_CONNECT_SUCCESS) {
            Timber.d("STATUS_CONNECT_SUCCESS:");
            tcpConnectionState.postValue(TcpConnectionState.CONNECT_SUCCESS);

        } else if (statusCode == ConnectState.STATUS_CONNECT_CLOSED) {
            Timber.d("STATUS_CONNECT_CLOSED:");
            tcpConnectionState.postValue(TcpConnectionState.CONNECT_CLOSED);

        } else if (statusCode == ConnectState.STATUS_CONNECT_ERROR) {
            Timber.d("STATUS_CONNECT_ERROR:");
            tcpConnectionState.postValue(TcpConnectionState.STATUS_CONNECT_ERROR);
        }
    }

    public void connect() {
        Timber.d("connect");
        if (!mNettyTcpClient.getConnectStatus()) {
            mNettyTcpClient.connect();//连接服务器
        } else {
            mNettyTcpClient.disconnect();
        }
    }

    public void disconnect() {
        mNettyTcpClient.disconnect();
    }

    public void sendMsgToServer(String msg) {
        mNettyTcpClient.sendMsgToServer(msg, new MessageStateListener() {
            @Override
            public void isSendSuccss(boolean isSuccess) {
                if (isSuccess) {
                    Timber.d("Write auth successful");
//                    logSend(msg);
                } else {
                    Timber.d("Write auth error");
                }
            }
        });
    }

    public void sendMsgToServer(String msg, MessageStateListener messageStateListener) {
        mNettyTcpClient.sendMsgToServer(msg, messageStateListener);
    }
}
