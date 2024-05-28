package com.shmedo.lib.tcp.netty.client.handler

import com.shmedo.lib.tcp.netty.client.listener.NettyClientListener
import com.shmedo.lib.tcp.netty.client.status.ConnectState
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import timber.log.Timber

class NettyClientHandler(
    private val listener: NettyClientListener<String>,
    private val index: Int,
    private val isSendHeartBeat: Boolean,
    private val heartBeatData: Any?,
    private val packetDelimiters: String?
) : SimpleChannelInboundHandler<String>() {

    private val packetSeparator: String
        get() {
            return packetDelimiters ?: System.lineSeparator().toString()
        }


    /**
     * <p>设定IdleStateHandler心跳检测每x秒进行一次读检测，
     * 如果x秒内ChannelRead()方法未被调用则触发一次userEventTrigger()方法 </p>
     *
     * @param ctx ChannelHandlerContext
     * @param evt IdleStateEvent
     */
    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is IdleStateEvent) {
            if (evt.state() == IdleState.WRITER_IDLE) {   //发送心跳
                if (!isSendHeartBeat) {
                    Timber.e("不发送心跳")
                    return
                }
                if (heartBeatData == null) {
                    ctx.channel().writeAndFlush("Heartbeat$packetSeparator")
                } else {
                    when (heartBeatData) {
                        is String -> {
                            Timber.d("发送心跳包：$heartBeatData$packetSeparator")
                            ctx.channel().writeAndFlush("$heartBeatData$packetSeparator")
                        }

                        is ByteArray -> {
                            //  Timber.d( "userEventTriggered: byte")
                            val buf: ByteBuf = Unpooled.copiedBuffer(heartBeatData)
                            ctx.channel().writeAndFlush(buf)
                        }

                        else -> {
                            Timber.e("userEventTriggered: heartBeatData type error")
                        }
                    }
                }
            }
        }
    }

    /**
     * <p>客户端上线</p>
     *
     * @param ctx ChannelHandlerContext
     */
    override fun channelActive(ctx: ChannelHandlerContext) {
        Timber.e("channelActive")
//        NettyTcpClient.getInstance().setConnectStatus(true);
        listener.onClientStatusConnectChanged(ConnectState.STATUS_CONNECT_SUCCESS, index)
    }

    /**
     * <p>客户端下线</p>
     *
     * @param ctx ChannelHandlerContext
     */
    override fun channelInactive(ctx: ChannelHandlerContext) {
        Timber.e("channelInactive")
//        NettyTcpClient.getInstance().setConnectStatus(false);
//        listener.onServiceStatusConnectChanged(NettyClientListener.STATUS_CONNECT_CLOSED);
        // NettyTcpClient.getInstance().reconnect();
    }

    /**
     * 客户端收到消息
     *
     * @param channelHandlerContext ChannelHandlerContext
     * @param msg                   消息
     */
    override fun channelRead0(channelHandlerContext: ChannelHandlerContext, msg: String) {
        Timber.d("Received Data(channelRead0): length=${msg.toByteArray().size} bytes;content: $msg")
        listener.onMessageResponseClient(msg, index)
    }

    /**
     * @param ctx   ChannelHandlerContext
     * @param cause 异常
     */
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        // Close the connection when an exception is raised.
//        NettyTcpClient.getInstance().setConnectStatus(false);
        Timber.e("Callback exceptionCaught()")
        listener.onClientStatusConnectChanged(ConnectState.STATUS_CONNECT_ERROR, index)
        cause.printStackTrace()
        ctx.close()
    }
}
