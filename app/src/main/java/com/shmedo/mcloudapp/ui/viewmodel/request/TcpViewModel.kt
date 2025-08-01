package com.shmedo.mcloudapp.ui.viewmodel.request

import androidx.lifecycle.viewModelScope
import com.shmedo.core.data.repository.LoggerRepositoryImp
import com.shmedo.lib.tcp.MedoTcpRepository
import com.shmedo.lib.tcp.TcpManagerResult
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseRequestViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * 创建者：gonghe
 * 创建时间：2024/5/27
 * 描述： TODO
 */
class TcpViewModel(
    private val medoTcpRepository: MedoTcpRepository,
    private val loggerRepositoryImp: LoggerRepositoryImp
) : BaseRequestViewModel(loggerRepositoryImp) {
    private val _data = MutableSharedFlow<TcpManagerResult<String>>()
    val data = _data.asSharedFlow()

    init {
        medoTcpRepository.data.onEach {
            _data.emit(it)
        }.launchIn(viewModelScope)
    }

    fun initTcpClient(host: String, port: Int) {
        medoTcpRepository.initTcpClient(host, port)
    }

    /**
     * 初始化原始模式TCP客户端（不使用分隔符）
     */
    fun initTcpClientRawMode(host: String, port: Int) {
        medoTcpRepository.initTcpClientRawMode(host, port)
    }

    fun initTcpClient(host: String, port: Int, isSendHeartBeat: Boolean, packetSeparator: String?) {
        medoTcpRepository.initTcpClient(host, port, isSendHeartBeat, packetSeparator)
    }

    fun connect() {
        medoTcpRepository.connect()
    }

    fun disconnect() {
        medoTcpRepository.disconnect()
    }

    fun isConnected(): Boolean {
        return medoTcpRepository.isConnected()
    }

    fun sendMsgToServer(cmdStr: String, timeMillis: Long = 0) {
        viewModelScope.launch {
            delay(timeMillis)
            medoTcpRepository.sendMsgToServer(cmdStr)
        }
    }

    /**
     * 发送原始字节数据到服务器
     */
    fun sendRawDataToServer(data: ByteArray) {
        medoTcpRepository.sendRawDataToServer(data)
    }
}