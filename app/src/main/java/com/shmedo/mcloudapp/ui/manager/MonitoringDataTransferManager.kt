package com.shmedo.mcloudapp.ui.manager

import com.shmedo.core.model.MonitoringDataItem
import com.shmedo.core.model.MonitoringDataQuery
import com.shmedo.core.model.MonitoringDataResponse
import com.shmedo.core.model.TransferState
import com.shmedo.lib.ble.communicate.service.MedoBleRepository
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
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
import org.json.JSONArray
import org.json.JSONObject
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
    
    // 动态更新的begin参数
    private var currentBeginTime: String = ""

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
            currentBeginTime = query.beginTime // 初始化begin参数

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
        var totalCount = 0
        var hasMoreData = true

        while (hasMoreData && coroutineContext.isActive) {
            var retryCount = 0
            var success = false

            while (retryCount < maxRetryCount && !success && coroutineContext.isActive) {
                try {
                    // 使用当前的begin时间构建查询
                    val currentQuery = query.copy(beginTime = currentBeginTime)
                    val response = queryPage(currentQuery)

                    if (response.result) {
                        // 更新总条数
                        totalCount += response.currentPageData.size

                        // 保存当前页数据
                        onPageReceived(response.currentPageData)

                        // 更新传输状态
                        updateTransferProgress(response)

                        // 检查是否已读取完毕
                        if (response.readEnd) {
                            Timber.d("服务器返回 readend=true，数据传输完成")
                            hasMoreData = false
                        } else if (response.currentPageData.isNotEmpty()) {
                            // 更新begin时间为当前批次数据的最大时间戳
                            updateBeginTime(response.currentPageData)
                        } else {
                            // 没有数据且没有readEnd标志，也停止传输
                            hasMoreData = false
                        }

                        success = true
                    } else {
                        throw Exception(response.reason ?: "查询失败")
                    }

                } catch (e: Exception) {
                    retryCount++
                    if (retryCount >= maxRetryCount) {
                        throw e
                    }
                    Timber.w("查询数据失败，${retryCount}秒后重试...")
                    delay(retryDelayMs * retryCount)
                }
            }
        }

        // 传输完成
        _transferState.value = TransferState.Success(totalCount)
    }

    /**
     * 更新begin时间为数据中的最大时间戳
     */
    private fun updateBeginTime(dataList: List<MonitoringDataItem>) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val targetFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
            
            var maxTime = 0L
            
            for (item in dataList) {
                try {
                    // 解析JSON格式的传感器数据
                    val jsonObject = JSONObject(item.content)
                    val keys = jsonObject.keys()
                    
                    while (keys.hasNext()) {
                        val sensorKey = keys.next()
                        val sensorData = jsonObject.getJSONObject(sensorKey)
                        val timeKeys = sensorData.keys()
                        
                        while (timeKeys.hasNext()) {
                            val timeStr = timeKeys.next()
                            val time = dateFormat.parse(timeStr)?.time ?: 0
                            if (time > maxTime) {
                                maxTime = time
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "解析数据时间失败: ${item.content}")
                }
            }
            
            if (maxTime > 0) {
                currentBeginTime = targetFormat.format(Date(maxTime))
                Timber.d("更新begin时间为: $currentBeginTime")
            }
        } catch (e: Exception) {
            Timber.e(e, "更新begin时间失败")
        }
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
        return IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_DEVICE_SENSOR_HISTORY_DATA,
            buildString {
                append("begin=").append(query.beginTime)
                append("&end=").append(query.endTime)
                append("&apikey=").append(query.apiKey.ifEmpty { "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9" })
                append("&msgid=").append(query.msgId)
            }
        )
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
//        return try {
//            parseRealResponse(response)
//        } catch (e: Exception) {
//            Timber.e(e, "解析真实响应失败，使用模拟数据")
//            generateMockResponse()
//        }

        return generateMockResponse()
    }

    /**
     * 解析真实响应数据
     */
    private fun parseRealResponse(response: String): MonitoringDataResponse {
//        val result = iotParseManager.parse<Map<String, String>>(
//            response,
//            IOTCommandType.MD_GET_DEVICE_SENSOR_HISTORY_DATA
//        )

        // 解析指令响应格式: $cmd=md_getsensordata&datastreams=[...]&apikey=...&msgid=...
        if (!response.contains("cmd=md_getsensordata")) {
            throw Exception("非监测数据查询响应")
        }

        // 检查是否成功
        if (response.contains("result=fail")) {
            val reason = extractParameter(response, "reason") ?: "未知错误"
            return MonitoringDataResponse(
                currentPageData = emptyList(),
                result = false,
                reason = reason,
                readEnd = false
            )
        }

        // 检查是否已读取完毕
        val readEnd = extractParameter(response, "readend")?.equals("true", ignoreCase = true) ?: false
        
        // 提取datastreams参数
        val datastreamsStr = extractParameter(response, "datastreams")
        if (datastreamsStr.isNullOrEmpty()) {
            return MonitoringDataResponse(
                currentPageData = emptyList(),
                result = true,
                reason = if (readEnd) "数据读取完毕" else "无数据",
                readEnd = readEnd
            )
        }

        // 解析datastreams JSON数组
        val dataList = parseDataStreams(datastreamsStr)

        return MonitoringDataResponse(
            currentPageData = dataList,
            result = true,
            reason = if (readEnd) "数据读取完毕" else "数据获取成功",
            readEnd = readEnd
        )
    }

    /**
     * 提取命令参数
     */
    private fun extractParameter(response: String, paramName: String): String? {
        val pattern = "$paramName=([^&]+)".toRegex()
        return pattern.find(response)?.groupValues?.get(1)
    }

    /**
     * 解析datastreams JSON数组
     */
    private fun parseDataStreams(datastreamsStr: String): List<MonitoringDataItem> {
        val dataList = mutableListOf<MonitoringDataItem>()
        
        try {
            val jsonArray = JSONArray(datastreamsStr)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val keys = jsonObject.keys()
                
                while (keys.hasNext()) {
                    val sensorKey = keys.next()
                    val sensorData = jsonObject.getJSONObject(sensorKey)
                    val timeKeys = sensorData.keys()
                    
                    while (timeKeys.hasNext()) {
                        val timeStr = timeKeys.next()
                        val dataItem = MonitoringDataItem(
                            timeStr = timeStr,
                            content = "{\"$sensorKey\":{\"$timeStr\":\"${sensorData.getString(timeStr)}\"}}"
                        )
                        dataList.add(dataItem)
                    }
                }
            }
            
            // 按时间排序
            dataList.sortBy { it.timeStr }
        } catch (e: Exception) {
            Timber.e(e, "解析datastreams失败: $datastreamsStr")
        }
        
        return dataList
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
            currentPageData = listOf(mockDataItem),
            result = true,
            reason = "模拟数据返回成功",
            readEnd = false // 模拟数据默认不结束，可以根据需要调整
        )
    }

    /**
     * 更新传输进度
     */
    private fun updateTransferProgress(response: MonitoringDataResponse) {
        val elapsedTime = System.currentTimeMillis() - startTime
        val bytesTransferred = response.currentPageData.sumOf { it.content.length.toLong() }
        totalBytesTransferred += bytesTransferred

        // 计算传输速度
        val speed = if (elapsedTime > 0) {
            val bytesPerSecond = totalBytesTransferred * 1000 / elapsedTime
            formatSpeed(bytesPerSecond)
        } else "计算中..."

        // 格式化已传输数据大小
        val transferredDataSize = formatDataSize(totalBytesTransferred)

        // 计算进度（这里使用一个简单的进度计算，实际可能需要更复杂的逻辑）
        val progress = if (response.currentPageData.isNotEmpty()) 0.5f else 1.0f

        _transferState.value = TransferState.Transferring(
            progress = progress,
            speed = speed,
            transferredDataSize = transferredDataSize
        )
    }

    /**
     * 格式化数据大小
     */
    private fun formatDataSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / 1024 / 1024} MB"
        }
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
     * 停止传输
     */
    fun stopTransfer() {
        transferJob?.cancel()
        isTransferring.set(false)
        _transferState.value = TransferState.Idle
    }
} 