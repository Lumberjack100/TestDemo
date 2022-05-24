package com.shmedo.mcloudapp.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.littlegreens.netty.client.listener.MessageStateListener;
import com.shmedo.mcloudapp.deviceconfig.model.TcpConnectionState;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/4 <br/>
 * 描述：     TODO #gh#
 */
public class TcpViewModel extends AndroidViewModel {
    private final TcpManager tcpManager;

    public TcpViewModel(@NonNull Application application) {
        super(application);
        tcpManager = new TcpManager();
    }

    public void initTcpClient(String host, int port) {
        tcpManager.initTcpClient(host, port);
    }

    public void initTcpClient(String host, int port, boolean isSendheartBeat, String packetSeparator) {
        tcpManager.initTcpClient(host, port, isSendheartBeat, packetSeparator);
    }

    public void connect() {
        tcpManager.connect();
    }

    public void disconnect() {
        tcpManager.disconnect();
    }

    public boolean getConnectStatus() {
        return tcpManager.getConnectStatus();
    }


    public ProtectedUnPeekLiveData<TcpConnectionState> getTcpConnectionState() {
        return tcpManager.getTcpConnectionState();
    }

    public ProtectedUnPeekLiveData<String> getReceivedMessage() {
        return tcpManager.getReceivedMessage();
    }

    public void clearLastReceivedMessage() {
        tcpManager.clearLastReceivedMessage();
    }

    public void sendMsgToServer(String msg) {
        tcpManager.sendMsgToServer(msg);
    }

    public void sendMsgToServer(String msg, MessageStateListener messageStateListener) {
        tcpManager.sendMsgToServer(msg, messageStateListener);
    }

//    @Override
//    protected void onCleared() {
//        super.onCleared();
//        if (tcpManager.getConnectStatus()) {
//            disconnect();
//        }
//    }
}
