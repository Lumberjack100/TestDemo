package com.shmedo.lib.tcp.netty.client

import android.os.SystemClock
import android.text.TextUtils
import com.shmedo.lib.tcp.netty.client.handler.NettyClientHandler
import com.shmedo.lib.tcp.netty.client.listener.MessageStateListener
import com.shmedo.lib.tcp.netty.client.listener.NettyClientListener
import com.shmedo.lib.tcp.netty.client.status.ConnectState
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.DelimiterBasedFrameDecoder
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.handler.timeout.IdleStateHandler
import io.netty.util.CharsetUtil
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Created by littleGreens on 2018-11-10.
 * TCP 客户端
 */
class NettyTcpClient private constructor(val host: String, val tcpPort: Int, val index: Int) {
    private var group: EventLoopGroup? = null
    private var listener: NettyClientListener<String>? = null
    private var channel: Channel? = null
    private var isConnect = false
    private var isConnecting = false
    private var isNeedReconnect = true

    var MAX_CONNECT_TIMES = 3//最大重连次数
    private var reconnectNum = MAX_CONNECT_TIMES
    private var reconnectIntervalTime: Long = CONNECT_TIMEOUT_MILLIS
    private var heartBeatInterval: Long = 5 //心跳间隔时间 单位秒
    private var isSendHeartBeat = false//是否发送心跳
    private var heartBeatData: Any? = null// 心跳数据，可以是String类型，也可以是byte[]
    private var packetSeparator: String? = null
    private var maxPacketLong = 1024

    companion object {
        const val CONNECT_TIMEOUT_MILLIS = 5000L
    }

    private fun setPacketSeparator(separator: String) {
        this.packetSeparator = separator
    }

    private fun setMaxPacketLong(maxPacketLong: Int) {
        this.maxPacketLong = maxPacketLong
    }

    fun getMaxConnectTimes() = MAX_CONNECT_TIMES

    fun getReconnectIntervalTime() = reconnectIntervalTime

    fun getHeartBeatInterval() = heartBeatInterval

    fun isSendHeartBeat() = isSendHeartBeat

    fun connect() {
        if (isConnecting) return

        thread(name = "client-Netty") {
            isNeedReconnect = true
            reconnectNum = MAX_CONNECT_TIMES
            connectServer()
        }
    }

    private fun connectServer() {
        synchronized(this) {
            if (!isConnect) {
                isConnecting = true
                group = NioEventLoopGroup()
                val bootstrap = Bootstrap().apply {
                    group(group)
                    option(ChannelOption.TCP_NODELAY, true) //屏蔽Nagle算法试图
                    option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS.toInt())
                    channel(NioSocketChannel::class.java)
                    handler(object : ChannelInitializer<SocketChannel>() {
                        override fun initChannel(ch: SocketChannel) {
                            if (isSendHeartBeat) {
                                ch.pipeline().addLast(
                                    "ping",
                                    IdleStateHandler(0, heartBeatInterval, 0, TimeUnit.SECONDS)
                                ) //5s未发送数据，回调userEventTriggered
                            }
                            //黏包处理,需要客户端、服务端配合
                            // 当 packetSeparator 为 null 时，进入原始模式，不添加任何帧解码器
                            if (packetSeparator != null) {
                                if (!TextUtils.isEmpty(packetSeparator)) {
                                    val delimiter = Unpooled.buffer().apply {
                                        writeBytes(packetSeparator!!.toByteArray())
                                    }
                                    ch.pipeline()
                                        .addLast(DelimiterBasedFrameDecoder(maxPacketLong, delimiter))
                                } else {
                                    ch.pipeline().addLast(LineBasedFrameDecoder(maxPacketLong))
                                }
                                ch.pipeline().addLast(StringEncoder(CharsetUtil.UTF_8))
                                ch.pipeline().addLast(StringDecoder(CharsetUtil.UTF_8))
                            }
                            // 原始模式：packetSeparator == null 时不添加编解码器，直接处理ByteBuf
                            ch.pipeline().addLast(
                                listener?.let {
                                    NettyClientHandler(
                                        it,
                                        index,
                                        isSendHeartBeat,
                                        heartBeatData,
                                        packetSeparator,
                                        packetSeparator == null // 传递是否为原始模式的标志
                                    )
                                }
                            )
                        }
                    })
                }

                try {
                    val channelFuture = bootstrap.connect(host, tcpPort).addListener { future ->
                        if (future.isSuccess) {
                            Timber.e("连接成功")
                            reconnectNum = MAX_CONNECT_TIMES
                            isConnect = true
                            channel = (future as ChannelFuture).channel()
                        } else {
                            Timber.e("连接失败")
                            isConnect = false
                        }
                        isConnecting = false
                    }.sync()

                    // Wait until the connection is closed.
                    channelFuture.channel().closeFuture().sync()
                    Timber.e("断开连接")
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isConnect = false
                    listener?.onClientStatusConnectChanged(
                        ConnectState.STATUS_CONNECT_CLOSED,
                        index
                    )
                    channel?.let {
                        if (it.isOpen) it.close()
                    }
                    group?.shutdownGracefully()
                    reconnect()
                }
            }
        }
    }

    fun disconnect() {
        Timber.e("call disconnect()")
        isNeedReconnect = false
        group?.shutdownGracefully()
    }

    fun reconnect() {
        Timber.e("call reconnect()")
        if (isNeedReconnect && reconnectNum > 0 && !isConnect) {
            reconnectNum--
            SystemClock.sleep(reconnectIntervalTime)
            if (isNeedReconnect && reconnectNum > 0 && !isConnect) {
                Timber.e("重新连接Tcp")
                connectServer()
            }
        }
    }

    /**
     * 异步发送
     *
     * @param data 要发送的数据
     * @param listener 发送结果回调
     * @return 方法执行结果
     */
    fun sendMsgToServer(data: String, listener: MessageStateListener): Boolean {
        val flag = channel != null && isConnect
        if (flag) {
            val separator =
                if (TextUtils.isEmpty(packetSeparator)) System.lineSeparator() else packetSeparator
            channel?.writeAndFlush("$data$separator")?.addListener { future ->
                listener.isSendSuccess(future.isSuccess)
            }
        }
        return flag
    }

    /**
     * 同步发送
     *
     * @param data 要发送的数据
     * @return 方法执行结果
     */
    fun sendMsgToServer(data: String): Boolean {
        val flag = channel != null && isConnect
        if (flag) {
            val separator =
                if (TextUtils.isEmpty(packetSeparator)) System.lineSeparator() else packetSeparator
            val channelFuture = channel?.writeAndFlush("$data$separator")?.awaitUninterruptibly()
            return channelFuture?.isSuccess == true
        }
        return false
    }

    fun sendMsgToServer(data: ByteArray, listener: MessageStateListener): Boolean {
        val flag = channel != null && isConnect
        if (flag) {
            val buf = Unpooled.copiedBuffer(data)
            channel?.writeAndFlush(buf)?.addListener { future ->
                listener.isSendSuccess(future.isSuccess)
            }
        }
        return flag
    }

    /**
     * 发送原始字节数据（无分隔符）
     */
    fun sendRawDataToServer(data: ByteArray, listener: MessageStateListener): Boolean {
        val flag = channel != null && isConnect
        if (flag) {
            val buf = Unpooled.copiedBuffer(data)
            channel?.writeAndFlush(buf)?.addListener { future ->
                listener.isSendSuccess(future.isSuccess)
            }
        }
        return flag
    }

    /**
     * 获取TCP连接状态
     */
    fun getConnectStatus() = isConnect

    fun isConnecting() = isConnecting

    fun setConnectStatus(status: Boolean) {
        this.isConnect = status
    }

    fun setListener(listener: NettyClientListener<String>) {
        this.listener = listener
    }

    fun strToByteArray(str: String?): ByteArray? {
        return str?.toByteArray()
    }

    /**
     * 构建者，创建NettyTcpClient
     */
    class Builder {
        private var MAX_CONNECT_TIMES = 3//最大重连次数
        private var reconnectIntervalTime: Long = 5000//重连间隔
        private var host: String? = null//服务器地址
        private var tcpPort: Int = 0//服务器端口
        private var index: Int = 0//客户端标识，(因为可能存在多个连接)
        private var isSendHeartBeat = false//是否发送心跳
        private var heartBeatInterval: Long = 5//心跳时间间隔
        private var heartBeatData: Any? = null//心跳数据，可以是String类型，也可以是byte[].
        private var packetSeparator: String? = null
        private var maxPacketLong = 1024

        fun setPacketSeparator(packetSeparator: String?) = apply {
            this.packetSeparator = packetSeparator
        }

        fun setMaxPacketLong(maxPacketLong: Int) = apply {
            this.maxPacketLong = maxPacketLong
        }

        fun setMaxReconnectTimes(reConnectTimes: Int) = apply {
            this.MAX_CONNECT_TIMES = reConnectTimes
        }

        fun setReconnectIntervalTime(reconnectIntervalTime: Long) = apply {
            this.reconnectIntervalTime = reconnectIntervalTime
        }

        fun setHost(host: String) = apply {
            this.host = host
        }

        fun setTcpPort(tcpPort: Int) = apply {
            this.tcpPort = tcpPort
        }

        fun setIndex(index: Int) = apply {
            this.index = index
        }

        fun setHeartBeatInterval(intervalTime: Long) = apply {
            this.heartBeatInterval = intervalTime
        }

        fun setSendHeartBeat(isSendheartBeat: Boolean) = apply {
            this.isSendHeartBeat = isSendheartBeat
        }

        fun setHeartBeatData(heartBeatData: Any?) = apply {
            this.heartBeatData = heartBeatData
        }

        fun build(): NettyTcpClient {
            return NettyTcpClient(host!!, tcpPort, index).apply {
                MAX_CONNECT_TIMES = this@Builder.MAX_CONNECT_TIMES
                reconnectIntervalTime = this@Builder.reconnectIntervalTime
                heartBeatInterval = this@Builder.heartBeatInterval
                isSendHeartBeat = this@Builder.isSendHeartBeat
                heartBeatData = this@Builder.heartBeatData
                packetSeparator = this@Builder.packetSeparator
                maxPacketLong = this@Builder.maxPacketLong
            }
        }
    }
}
