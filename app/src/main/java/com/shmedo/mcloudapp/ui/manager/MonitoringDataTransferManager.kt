package com.shmedo.mcloudapp.ui.manager

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.model.MonitoringDataItem
import com.shmedo.core.model.MonitoringDataQuery
import com.shmedo.core.model.MonitoringDataResponse
import com.shmedo.core.model.TransferState
import com.shmedo.lib.ble.communicate.service.MedoBleRepository
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.md_cmd.utils.MDConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 监测数据传输管理器
 */
class MonitoringDataTransferManager(
    private val bleRepository: MedoBleRepository,
    private val iotParseManager: IOTParserManager,
    private val scope: CoroutineScope
) {
    private val _transferState = MutableStateFlow<TransferState>(TransferState.Idle)
    val transferState: StateFlow<TransferState> = _transferState.asStateFlow()

    private val isTransferring = AtomicBoolean(false)
    private var transferJob: Job? = null
    private var startTime = 0L
    private var totalBytesTransferred = 0L

    // 重试
    private val maxRetryCount = 2
    private val retryDelayMs = 2000L

    /**
     * 开始传输监测数据
     */
    fun startTransfer(
        query: MonitoringDataQuery,
        onPageReceived: suspend (List<MonitoringDataItem>) -> Unit
    ) {
        if (isTransferring.get()) {
            Timber.w("数据传输已在进行中")
            return
        }

        transferJob?.cancel()
        transferJob = scope.launch {
            isTransferring.set(true)
            startTime = System.currentTimeMillis()
            totalBytesTransferred = 0L

            try {
                _transferState.value = TransferState.Idle

                // 检查BLE连接
                if (!bleRepository.isConnected()) {
                    throw Exception("蓝牙未连接")
                }

                // 开始查询数据
                queryDataWithRetry(query, onPageReceived)

            } catch (e: Exception) {
                Timber.e(e, "数据传输失败")
                _transferState.value = TransferState.Error(e.message ?: "未知错误")
            } finally {
                isTransferring.set(false)
            }
        }
    }

    /**
     * 查询数据（带重试）
     */
    private suspend fun queryDataWithRetry(
        query: MonitoringDataQuery,
        onPageReceived: suspend (List<MonitoringDataItem>) -> Unit
    ) {
        var currentPage = 1
        var totalPage = 1
        var totalCount = 0

        while (currentPage <= totalPage && coroutineContext.isActive) {
            val pageQuery = query.copy(currentPage = currentPage)

            var retryCount = 0
            var success = false

            while (retryCount < maxRetryCount && !success && coroutineContext.isActive) {
                try {
                    val response = queryPage(pageQuery)

                    if (response.result) {
                        // 更新总页数和总条数
                        totalPage = response.totalPage
                        totalCount = response.totalCount

                        // 保存当前页数据
                        onPageReceived(response.currentPageData)

                        // 更新传输状态
                        updateTransferProgress(currentPage, totalPage, response)

                        success = true
                        currentPage++
                    } else {
                        throw Exception(response.reason ?: "查询失败")
                    }

                } catch (e: Exception) {
                    retryCount++
                    if (retryCount >= maxRetryCount) {
                        throw e
                    }
                    Timber.w("查询第${currentPage}页失败，${retryCount}秒后重试...")
                    delay(retryDelayMs * retryCount)
                }
            }
        }

        // 传输完成
        _transferState.value = TransferState.Success(totalCount)
    }

    /**
     * 查询单页数据
     */
    private suspend fun queryPage(query: MonitoringDataQuery): MonitoringDataResponse {
        return withContext(Dispatchers.IO) {
            val command = buildQueryCommand(query)

            // 发送命令
            bleRepository.sendData(command + MDConstants.COMMAND_FOOTER)

            // 等待响应
            val response = waitForResponse(query.msgId)

            // 解析响应
            parseResponse(response)
        }
    }

    /**
     * 构建查询命令
     */
    private fun buildQueryCommand(query: MonitoringDataQuery): String {
//        return IOTCommandUtil.getCommand(
//            IOTCommandType.MD_GET_DEVICE_SENSOR_HISTORY_DATA,
//            buildString {
//                append("begin=").append(query.beginTime)
//                append("&end=").append(query.endTime)
//                append("&currentPage=").append(query.currentPage)
//                append("&apikey=").append(query.apiKey.ifEmpty { "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9" })
//                append("&msgid=").append(query.msgId)
//            }
//        )

        return IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS,buildString {
            append("apikey=").append(query.apiKey.ifEmpty { "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9" })
            append("&msgid=").append(query.msgId)
        })
    }

    /**
     * 等待响应
     */
    private suspend fun waitForResponse(msgId: String): String {
        return suspendCancellableCoroutine { cont ->
            val timeoutJob = scope.launch {
                delay(30000) // 30秒超时
                if (cont.isActive) {
                    cont.resumeWithException(Exception("响应超时"))
                }
            }

            // 监听BLE响应
            val job = bleRepository.commandData
                .filter { it.response.contains(msgId) }
                .take(1)
                .onEach { commandData ->
                    timeoutJob.cancel()
                    if (cont.isActive) {
                        cont.resume(commandData.response)
                    }
                }
                .launchIn(scope)

            cont.invokeOnCancellation {
                timeoutJob.cancel()
                job.cancel()
            }
        }
    }

    /**
     * 解析响应数据
     */
    private fun parseResponse(response: String): MonitoringDataResponse {
        // TODO: 当设备支持实际指令后，替换为真实的解析逻辑
        return generateMockResponse()
    }

    /**
     * 生成模拟响应数据
     */
    private fun generateMockResponse(): MonitoringDataResponse {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = dateFormat.format(Date())

        // 模拟传感器数据
        val sensorNumber = "222_11"
        val sensorTime = currentTime
        val sensorData = "-1355.779,0.000"

        // 构造 content JSON 字符串
        val contentJson = "{\"$sensorNumber\":{\"$sensorTime\":\"$sensorData\"}}"

        // 创建模拟的监测数据项
        val mockDataItem = MonitoringDataItem(
            timeStr = currentTime,
            content = contentJson
        )

        return MonitoringDataResponse(
            totalCount = 5000,
            totalPage = 5000,
            pageSize = 1,
            currentPageData = listOf(mockDataItem),
            result = true,
            reason = "模拟数据返回成功"
        )
    }

    /**
     * 生成 真实响应数据
     */
    private fun generateRealResponse(response: String): MonitoringDataResponse {
        val result = iotParseManager.parse<Map<String, String>>(
            response,
            IOTCommandType.MD_GET_DEVICE_SENSOR_HISTORY_DATA
        )
        when (result) {
            is IOTCommandResult.Failure -> {
                throw Exception(result.message.ifEmpty { "查询失败" })
            }

            is IOTCommandResult.Success -> {
                // 解析数据列表
                val currentPageDataStr = result.data["currentPageData"] ?: "[]"
                val dataList = parsePageData(currentPageDataStr)

                return MonitoringDataResponse(
                    totalCount = result.data["totalCount"]?.toIntOrNull() ?: 0,
                    totalPage = result.data["totalPage"]?.toIntOrNull() ?: 0,
                    pageSize = result.data["pageSize"]?.toIntOrNull() ?: 0,
                    currentPageData = dataList,
                    result = true,
                    reason = result.data["reason"] ?: "",
                )
            }
        }
    }

    /**
     * 解析页面数据
     */
    private fun parsePageData(jsonStr: String): List<MonitoringDataItem> {
        return try {
            MoshiUtil.fromJson<List<MonitoringDataItem>>(jsonStr) ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "解析页面数据失败: $jsonStr")
            emptyList()
        }
    }

    /**
     * 更新传输进度
     */
    private fun updateTransferProgress(
        currentPage: Int,
        totalPage: Int,
        response: MonitoringDataResponse
    ) {
        val progress = currentPage.toFloat() / totalPage
        val elapsedTime = System.currentTimeMillis() - startTime
        val bytesTransferred = response.currentPageData.sumOf { it.content.length }
        totalBytesTransferred += bytesTransferred

        // 计算速度和剩余时间
        val speed = if (elapsedTime > 0) {
            val bytesPerSecond = totalBytesTransferred * 1000 / elapsedTime
            formatSpeed(bytesPerSecond)
        } else "计算中..."

        val remainingPages = totalPage - currentPage
        val averageTimePerPage = if (currentPage > 0) elapsedTime / currentPage else 0
        val remainingTime = formatTime(remainingPages * averageTimePerPage)

        _transferState.value = TransferState.Transferring(
            currentPage = currentPage,
            totalPage = totalPage,
            progress = progress,
            speed = speed,
            remainingTime = remainingTime
        )
    }

    /**
     * 格式化速度
     */
    private fun formatSpeed(bytesPerSecond: Long): String {
        return when {
            bytesPerSecond < 1024 -> "$bytesPerSecond B/s"
            bytesPerSecond < 1024 * 1024 -> "${bytesPerSecond / 1024} KB/s"
            else -> "${bytesPerSecond / 1024 / 1024} MB/s"
        }
    }

    /**
     * 格式化时间
     */
    private fun formatTime(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        return when {
            hours > 0 -> "${hours}小时${minutes % 60}分钟"
            minutes > 0 -> "${minutes}分钟${seconds % 60}秒"
            else -> "${seconds}秒"
        }
    }

    /**
     * 停止传输
     */
    fun stopTransfer() {
        transferJob?.cancel()
        isTransferring.set(false)
        _transferState.value = TransferState.Idle
    }
} 