package com.shmedo.mcloudapp.ui.viewmodel.request

import androidx.lifecycle.viewModelScope
import com.shmedo.core.data.repository.LoggerRepositoryImp
import com.shmedo.lib.cmd.base.md_cmd.utils.MDConstants
import com.shmedo.lib.tcp.MedoTcpRepository
import com.shmedo.lib.tcp.TcpConnectClosed
import com.shmedo.lib.tcp.TcpConnectError
import com.shmedo.lib.tcp.TcpConnectedResult
import com.shmedo.lib.tcp.TcpManagerResult
import com.shmedo.mcloudapp.communication.model.DeviceConnectionState
import com.shmedo.mcloudapp.communication.model.DeviceError
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseRequestViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/5/27
 * 描述：TCP通信ViewModel
 */
class TcpViewModel(
    private val medoTcpRepository: MedoTcpRepository,
    private val loggerRepositoryImp: LoggerRepositoryImp
) : BaseRequestViewModel(loggerRepositoryImp) {
    private val _data = MutableSharedFlow<TcpManagerResult<String>>()
    val data = _data.asSharedFlow()

    private val _connectionState =
        MutableStateFlow<DeviceConnectionState>(DeviceConnectionState.Disconnected)
    val connectionState: StateFlow<DeviceConnectionState> = _connectionState.asStateFlow()

    init {
        medoTcpRepository.data.onEach { result ->
            _data.emit(result)

            // 同时更新连接状态
            when (result) {
                is TcpConnectedResult -> {
                    _connectionState.value = DeviceConnectionState.Connected
                    Timber.i("TCP 连接成功")
                }

                is TcpConnectClosed -> {
                    _connectionState.value = DeviceConnectionState.Disconnected
                    Timber.i("TCP 连接关闭")
                }

                is TcpConnectError -> {
                    _connectionState.value =
                        DeviceConnectionState.Error(DeviceError.Tcp("未知TCP错误"))
                    Timber.e("TCP 连接错误")
                }

                else -> {
                    // 其他类型不影响连接状态
                }
            }
        }.launchIn(viewModelScope)
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

    /**
     * 连接到TCP设备
     * @param deviceIp 设备IP地址
     * @param port TCP端口
     */
    fun connectToDevice(deviceIp: String, port: Int) {
        try {
            _connectionState.value = DeviceConnectionState.Connecting

            // 初始化TCP客户端（指令模式，使用换行符分隔）
            medoTcpRepository.initTcpClient(
                host = deviceIp,
                port = port,
                isSendHeartBeat = true,
                packetSeparator = MDConstants.COMMAND_FOOTER
            )

            // 连接
            medoTcpRepository.connect()

            Timber.i("正在连接设备: $deviceIp:$port")

        } catch (e: Exception) {
            _connectionState.value =
                DeviceConnectionState.Error(DeviceError.Tcp(e.message ?: "连接失败"))
            Timber.e(e, "连接设备失败")
        }
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