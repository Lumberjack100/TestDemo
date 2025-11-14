package com.shmedo.mcloudapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.tcp.MedoTcpRepository
import com.shmedo.lib.tcp.TcpConnectClosed
import com.shmedo.lib.tcp.TcpConnectError
import com.shmedo.lib.tcp.TcpConnectedResult
import com.shmedo.lib.tcp.TcpIdleResult
import com.shmedo.lib.tcp.TcpSuccessDataResult
import com.shmedo.lib.tcp.TcpSuccessRawDataResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

/**
 * WiFi 设备 TCP 通信 ViewModel
 * 
 * 创建者: gonghe
 * 创建时间: 2024/12/15
 * 描述: 复用 lib_tcp 实现 WiFi 设备通信
 */
class WifiDeviceTcpViewModel(
    private val tcpRepository: MedoTcpRepository
) : ViewModel() {
    
    private val _connectionState = MutableStateFlow<TcpConnectionState>(TcpConnectionState.Disconnected)
    val connectionState: StateFlow<TcpConnectionState> = _connectionState
    
    private val _receivedData = MutableStateFlow<String?>(null)
    val receivedData: StateFlow<String?> = _receivedData
    
    private val _downloadProgress = MutableStateFlow<FileDownloadProgress>(FileDownloadProgress.Idle)
    val downloadProgress: StateFlow<FileDownloadProgress> = _downloadProgress
    
    companion object {
        const val DEFAULT_TCP_PORT = 8888 // IoT 设备默认 TCP 端口
    }
    
    init {
        // 监听 TCP 数据流
        tcpRepository.data.onEach { result ->
            when (result) {
                is TcpIdleResult -> {
                    // 空闲状态，无需处理
                }
                is TcpConnectedResult -> {
                    _connectionState.value = TcpConnectionState.Connected
                    Timber.i("TCP 连接成功")
                }
                is TcpConnectClosed -> {
                    _connectionState.value = TcpConnectionState.Disconnected
                    Timber.i("TCP 连接关闭")
                }
                is TcpConnectError -> {
                    _connectionState.value = TcpConnectionState.Error("连接失败")
                    Timber.e("TCP 连接错误")
                }
                is TcpSuccessDataResult -> {
                    handleReceivedData(result.data)
                }
                is TcpSuccessRawDataResult -> {
                    handleRawData(result.data)
                }
            }
        }.launchIn(viewModelScope)
    }
    
    /**
     * 连接到 WiFi 设备
     * 
     * @param deviceIp 设备 IP 地址（通过 WiFi 连接后获得）
     * @param port TCP 端口，默认 8888
     */
    fun connectToDevice(deviceIp: String, port: Int = DEFAULT_TCP_PORT) {
        try {
            _connectionState.value = TcpConnectionState.Connecting
            
            // 初始化 TCP 客户端（指令模式，使用换行符分隔）
            tcpRepository.initTcpClient(
                host = deviceIp,
                port = port,
                isSendHeartBeat = true,
                packetSeparator = "\n"
            )
            
            // 连接
            tcpRepository.connect()
            
            Timber.i("正在连接设备: $deviceIp:$port")
            
        } catch (e: Exception) {
            _connectionState.value = TcpConnectionState.Error(e.message ?: "连接失败")
            Timber.e(e, "连接设备失败")
        }
    }
    
    /**
     * 断开连接
     */
    fun disconnect() {
        tcpRepository.disconnect()
        _connectionState.value = TcpConnectionState.Disconnected
    }
    
    /**
     * 发送 IoT 指令
     * 
     * @param commandType 指令类型
     * @param params 指令参数
     */
    fun sendCommand(commandType: IOTCommandType, params: String = "") {
        if (!tcpRepository.isConnected()) {
            Timber.w("TCP 未连接，无法发送指令")
            return
        }
        
        val command = IOTCommandUtil.getCommand(commandType, params)
        Timber.d("发送指令: $command")
        tcpRepository.sendMsgToServer(command)
    }
    
    /**
     * 请求设备日志文件列表
     */
    fun requestLogFileList() {
        if (!tcpRepository.isConnected()) {
            Timber.w("TCP 未连接，无法请求日志列表")
            return
        }
        
        val command = "\$cmd=get_log_list"
        tcpRepository.sendMsgToServer(command)
    }
    
    /**
     * 请求下载日志文件
     * 
     * @param fileName 文件名
     */
    fun requestDownloadLogFile(fileName: String) {
        if (!tcpRepository.isConnected()) {
            Timber.w("TCP 未连接，无法下载日志")
            return
        }
        
        val command = "\$cmd=download_log&file=$fileName"
        tcpRepository.sendMsgToServer(command)
        
        _downloadProgress.value = FileDownloadProgress.Preparing(fileName)
    }
    
    /**
     * 处理接收到的文本数据
     */
    private fun handleReceivedData(data: String) {
        Timber.d("收到设备响应: $data")
        _receivedData.value = data
        
        // TODO: 解析设备响应
        // 例如：日志文件列表、指令执行结果等
    }
    
    /**
     * 处理接收到的原始数据（文件传输）
     */
    private fun handleRawData(data: ByteArray) {
        Timber.d("收到原始数据: ${data.size} bytes")
        
        // TODO: 实现文件接收逻辑
        // 1. 解析文件头（文件名、大小、MD5等）
        // 2. 接收文件内容
        // 3. 验证文件完整性
        // 4. 保存到本地
        
        _downloadProgress.value = FileDownloadProgress.Downloading(
            fileName = "device.log",
            progress = 50,
            receivedBytes = data.size.toLong(),
            totalBytes = data.size.toLong() * 2
        )
    }
}

/**
 * TCP 连接状态
 */
sealed class TcpConnectionState {
    object Disconnected : TcpConnectionState()
    object Connecting : TcpConnectionState()
    object Connected : TcpConnectionState()
    data class Error(val message: String) : TcpConnectionState()
}

/**
 * 文件下载进度
 */
sealed class FileDownloadProgress {
    object Idle : FileDownloadProgress()
    data class Preparing(val fileName: String) : FileDownloadProgress()
    data class Downloading(
        val fileName: String,
        val progress: Int,
        val receivedBytes: Long,
        val totalBytes: Long
    ) : FileDownloadProgress()
    data class Completed(val fileName: String, val filePath: String) : FileDownloadProgress()
    data class Error(val message: String) : FileDownloadProgress()
}

