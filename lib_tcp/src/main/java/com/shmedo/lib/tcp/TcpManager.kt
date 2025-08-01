package com.shmedo.lib.tcp

import com.shmedo.lib.tcp.netty.client.NettyTcpClient
import com.shmedo.lib.tcp.netty.client.listener.MessageStateListener
import com.shmedo.lib.tcp.netty.client.listener.NettyClientListener
import com.shmedo.lib.tcp.netty.client.status.ConnectState
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber
import java.util.UUID

/**
 * 创建者：gonghe
 * 创建时间：2024/5/27
 * 描述： TODO
 */
class TcpManager : NettyClientListener<String> {
    private val maxPacketLong = 1024 * 60 //设置一次发送数据的最大长度 60K
    private val TCP_HEART_BEAT = "tcp_keepalive"

    //自定义心跳包指令
    private val heartBeat =
        "\$cmd=$TCP_HEART_BEAT&apikey=b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9&msgid=${
            UUID.randomUUID().toString()
        }"

    private val _data = MutableSharedFlow<TcpManagerResult<String>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val data = _data.asSharedFlow()


    private var mNettyTcpClient: NettyTcpClient? = null

    fun initTcpClient(host: String, port: Int) {
        initTcpClient(host, port, true, null)
    }

    fun initTcpClient(host: String, port: Int, isSendHeartBeat: Boolean, packetSeparator: String?) {
        mNettyTcpClient = NettyTcpClient.Builder()
            .setHost(host) //设置服务端地址
            .setTcpPort(port) //设置服务端端口号
            .setMaxReconnectTimes(3) //设置最大重连次数
            .setReconnectIntervalTime(3000) //设置重连间隔时间。单位：毫秒
            .setSendHeartBeat(isSendHeartBeat) //设置是否发送心跳
            .setHeartBeatInterval(20) //设置心跳间隔时间。单位：秒
            .setHeartBeatData(heartBeat) //设置心跳数据，可以是String类型，也可以是byte[]，以后设置的为准
            .setIndex(0) //设置客户端标识.(因为可能存在多个tcp连接)
            .setPacketSeparator(packetSeparator) //用特殊字符，作为分隔符，解决粘包问题，默认是用换行符作为分隔符
            .setMaxPacketLong(maxPacketLong) //设置一次发送数据的最大长度，默认是1024
            .build()

        mNettyTcpClient?.setListener(this) //设置TCP监听
    }

    /**
     * 当服务状态发生变化时触发
     * @param statusCode 状态变化
     * @param index tcp 客户端的标识，因为一个应用程序可能有很多个长链接
     */
    override fun onClientStatusConnectChanged(statusCode: Int, index: Int) {
        when (statusCode) {
            ConnectState.STATUS_CONNECT_SUCCESS -> {
                Timber.d("STATUS_CONNECT_SUCCESS:")
                _data.tryEmit(TcpConnectedResult())
            }

            ConnectState.STATUS_CONNECT_CLOSED -> {
                Timber.d("STATUS_CONNECT_CLOSED:")
                _data.tryEmit(TcpConnectClosed())
            }

            ConnectState.STATUS_CONNECT_ERROR -> {
                Timber.d("STATUS_CONNECT_ERROR:")
                _data.tryEmit(TcpConnectError())
            }
        }
    }

    /**
     * 当接收到系统消息
     * @param msg 消息
     * @param index tcp 客户端的标识，因为一个应用程序可能有很多个长链接
     */
    override fun onMessageResponseClient(msg: String, index: Int) {
        // 跳过心跳包数据的分发处理
        if (msg.contains(TCP_HEART_BEAT)) return

        // 检查是否为原始数据
        if (msg.startsWith("TCP_RAW:")) {
            // 移除RAW:前缀，获取原始数据
            val rawData = msg.substring(8)
            val byteArray = rawData.toByteArray(Charsets.UTF_8)
            _data.tryEmit(TcpSuccessRawDataResult(byteArray))
        } else {
            _data.tryEmit(TcpSuccessDataResult(msg))
        }
    }

    fun connect() {
        mNettyTcpClient?.getConnectStatus()?.let {
            if (!it) {
                mNettyTcpClient?.connect() //连接服务器
            }
        }
    }

    fun disconnect() {
        mNettyTcpClient?.disconnect()
    }

    fun getConnectStatus(): Boolean {
        return mNettyTcpClient?.getConnectStatus() == true
    }

    fun sendMsgToServer(msg: String) {
        Timber.d("发送消息: length${msg.toByteArray().size} bytes;content: $msg")
        mNettyTcpClient?.sendMsgToServer(msg, object : MessageStateListener {
            override fun isSendSuccess(isSuccess: Boolean) {
                if (isSuccess) {
                    Timber.d("Write auth successful")
                } else {
                    Timber.d("Write auth error")
                }
            }
        })
    }

    fun sendMsgToServer(msg: String, messageStateListener: MessageStateListener) {
        Timber.d("发送消息: length${msg.toByteArray().size} bytes;content: $msg")
        mNettyTcpClient?.sendMsgToServer(msg, messageStateListener)
    }

    /**
     * 发送原始字节数据到服务器
     */
    fun sendRawDataToServer(data: ByteArray) {
        Timber.d("发送原始数据: length=${data.size} bytes")
        mNettyTcpClient?.sendRawDataToServer(data, object : MessageStateListener {
            override fun isSendSuccess(isSuccess: Boolean) {
                if (isSuccess) {
                    Timber.d("Send raw data successful")
                } else {
                    Timber.d("Send raw data error")
                }
            }
        })
    }

}