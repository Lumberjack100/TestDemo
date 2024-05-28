package com.shmedo.lib.tcp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * 创建者：gonghe
 * 创建时间：2024/5/27
 * 描述： TODO
 */
class MedoTcpRepository(
    private val tcpManager: TcpManager,
    scope: CoroutineScope
) {
    private val _data = MutableSharedFlow<TcpManagerResult<String>>()
    val data = _data.asSharedFlow()

    init {
        tcpManager.data.onEach {
            _data.emit(it)
        }.launchIn(scope)
    }

    fun initTcpClient(host: String, port: Int) {
        tcpManager.initTcpClient(host, port)
    }

    fun initTcpClient(host: String, port: Int, isSendHeartBeat: Boolean, packetSeparator: String?) {
        tcpManager.initTcpClient(host, port, isSendHeartBeat, packetSeparator)
    }

    fun connect() {
        tcpManager.connect()
    }

    fun disconnect() {
        tcpManager.disconnect()
    }

    fun isConnected(): Boolean {
        return tcpManager.getConnectStatus()
    }

    fun sendMsgToServer(msg: String) {
        tcpManager.sendMsgToServer(msg)
    }
}