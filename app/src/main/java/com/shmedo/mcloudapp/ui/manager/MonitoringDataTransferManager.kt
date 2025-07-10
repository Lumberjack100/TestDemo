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
import com.shmedo.mcloudapp.utils.DataFormatUtils
import com.shmedo.mcloudapp.utils.PerformanceMonitor
import com.shmedo.mcloudapp.utils.TransferErrorHandler
import com.shmedo.mcloudapp.utils.TransferLogger
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
 * 监测数据传输管理器 - 优化版本
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
    private var totalDataCount = 0

    // 动态更新的begin参数
    private var currentBeginTime: String = ""
    private var currentDeviceSn: String = ""

    // 性能监控
    private val performanceMonitor = PerformanceMonitor()
    
    // 错误处理配置
    private val retryConfig = TransferErrorHandler.RetryConfig(
        maxRetries = 3,
        initialDelay = 1000L,
        maxDelay = 5000L,
        multiplier = 2.0
    )

    // 常量
    private companion object {
        const val BATCH_SIZE = 100
        const val TIMEOUT_MS = 30000L
        const val MAX_CONCURRENT_OPERATIONS = 1
    }

    /**
     * 开始传输监测数据 - 优化版本
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
            totalDataCount = 0
            currentBeginTime = query.beginTime
            currentDeviceSn = extractDeviceSnFromQuery(query)

            try {
                _transferState.value = TransferState.Idle
                
                // 开始性能监控
                performanceMonitor.startMonitoring(currentDeviceSn)
                
                // 预检查
                performPreTransferChecks()
                
                // 记录传输开始
                TransferLogger.logTransferStart(query, currentDeviceSn)
                TransferLogger.logSystemInfo()
                TransferLogger.logBluetoothStatus(bleRepository.isConnected(), currentDeviceSn)

                // 开始查询数据
                performanceMonitor.measureExecutionTime("数据传输") {
                    queryDataWithRetry(query, onPageReceived)
                }

            } catch (e: Exception) {
                handleTransferError(e)
            } finally {
                isTransferring.set(false)
                performanceMonitor.stopMonitoring(currentDeviceSn)
            }
        }
    }

    /**
     * 预检查
     */
    private suspend fun performPreTransferChecks() {
        // 检查BLE连接
        if (!bleRepository.isConnected()) {
            throw Exception("蓝牙连接已断开")
        }
        
        // 检查存储空间
        checkStorageSpace()
    }
    
    /**
     * 检查存储空间
     */
    private fun checkStorageSpace() {
        val freeSpace = android.os.Environment.getDataDirectory().freeSpace
        val requiredSpace = 100 * 1024 * 1024L // 100MB
        
        if (freeSpace < requiredSpace) {
            throw Exception("存储空间不足")
        }
    }
    
    /**
     * 从查询中提取设备SN
     */
    private fun extractDeviceSnFromQuery(query: MonitoringDataQuery): String {
        return query.apiKey.take(8) // 使用apiKey的前8位作为设备标识
    }
    
    /**
     * 处理传输错误
     */
    private fun handleTransferError(exception: Throwable) {
        val transferError = TransferErrorHandler.mapExceptionToTransferError(exception)
        val userMessage = TransferErrorHandler.getUserFriendlyErrorMessage(transferError)
        
        // 记录错误日志
        val duration = if (startTime > 0) System.currentTimeMillis() - startTime else 0
        TransferLogger.logTransferError(
            error = userMessage,
            deviceSn = currentDeviceSn,
            duration = duration,
            context = exception.stackTraceToString().take(500)
        )
        
        performanceMonitor.recordTransferError(currentDeviceSn)
        _transferState.value = TransferState.Error(userMessage)
    }

    /**
     * 查询数据（带重试）- 优化版本
     */
    private suspend fun queryDataWithRetry(
        query: MonitoringDataQuery,
        onPageReceived: suspend (List<MonitoringDataItem>) -> Unit
    ) {
        var totalCount = 0
        var hasMoreData = true

        while (hasMoreData && coroutineContext.isActive) {
            try {
                // 使用错误处理工具执行查询
                val response = TransferErrorHandler.executeWithRetry(retryConfig) {
                    val currentQuery = query.copy(beginTime = currentBeginTime)
                    queryPage(currentQuery)
                }

                if (response.result) {
                    // 更新总条数
                    totalCount += response.currentPageData.size
                    totalDataCount += response.currentPageData.size

                    // 计算数据大小
                    val dataSize = response.currentPageData.sumOf { it.content.length.toLong() }
                    totalBytesTransferred += dataSize

                    // 记录性能数据
                    performanceMonitor.recordTransferData(currentDeviceSn, dataSize, response.currentPageData.size)
                    
                    // 监控内存使用
                    performanceMonitor.monitorMemoryUsage()

                    // 保存当前页数据
                    onPageReceived(response.currentPageData)

                    // 更新传输状态
                    updateTransferProgress(response)

                    // 检查是否已读取完毕
                    if (response.readEnd) {
                        Timber.tag("DataTransfer").i("服务器返回 readEnd=true，数据传输完成")
                        hasMoreData = false
                    } else if (response.currentPageData.isNotEmpty()) {
                        // 更新begin时间为当前批次数据的最大时间戳
                        updateBeginTime(response.currentPageData)
                    } else {
                        // 没有数据且没有readEnd标志，也停止传输
                        hasMoreData = false
                    }

                } else {
                    throw Exception(response.reason ?: "查询失败")
                }

            } catch (e: Exception) {
                performanceMonitor.recordTransferError(currentDeviceSn)
                throw e
            }
        }

        // 传输完成
        val duration = System.currentTimeMillis() - startTime
        TransferLogger.logTransferComplete(totalCount, currentDeviceSn, duration)
        
        // 记录性能指标
        performanceMonitor.getTransferMetrics(currentDeviceSn)?.let { metrics ->
            TransferLogger.logPerformanceMetrics(metrics)
        }
        
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
                delay(10000) // 10秒超时
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
     * 解析响应数据 TODO 目前使用模拟数据，后期设备支持后需要更改
     */
    private fun parseResponse(response: String): MonitoringDataResponse {
        return try {
            parseRealResponse(generateMockResponse())
        } catch (e: Exception) {
            Timber.e(e, "解析真实响应失败，使用模拟数据")
            // 生成模拟响应字符串并解析
            parseRealResponse(generateMockResponse())
        }
    }

    /**
     * 解析真实响应数据
     */
    private fun parseRealResponse(response: String): MonitoringDataResponse {
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
        val readEnd =
            extractParameter(response, "readend")?.equals("true", ignoreCase = true) ?: false

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
                            content = "{\"$sensorKey\":{\"$timeStr\":\"${
                                sensorData.getString(
                                    timeStr
                                )
                            }\"}}"
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
     * 格式：$cmd=md_getsensordata&datastreams=[{"222_2":{"2025-07-01 07:55:25":"-2073.765,0.000"}},{"222_11":{"2025-07-01 07:55:25":"-1355.635,0.000"}},{"222_1":{"2025-07-01 08:05:21":"-2468.713,0.000"}},{"222_1":{"2025-07-01 08:15:17":"-2468.569,0.000"}}{"103_5":{"2025-07-06 23:00:00":"0.835,-0.009,0.540,57.106,0.509,-32.935"}}]&apikey=123456&msgid=123456
     */
    private fun generateMockResponse(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = dateFormat.format(Date())

        val jsonArray = JSONArray()

        // 生成10个传感器数据
        for (i in 1..10) {
            // 生成传感器编号：222_随机数
            val sensorNumber = "222_${(1..7).random()}"

            // 生成3个浮点数的数据值
            val value1 = String.format("%.3f", (-3000..3000).random().toDouble() + Math.random())
            val value2 = String.format("%.3f", (-1000..1000).random().toDouble() + Math.random())
            val value3 = String.format("%.3f", (-500..500).random().toDouble() + Math.random())
            val dataValue = "$value1,$value2,$value3"

            // 构建传感器时间数据
            val sensorTimeData = JSONObject()
            sensorTimeData.put(currentTime, dataValue)

            // 添加到datastreams
            val sensorData = JSONObject()
            sensorData.put(sensorNumber, sensorTimeData)
            jsonArray.put(sensorData)
        }

        // 构建完整的响应字符串
        return buildString {
            append("\$cmd=md_getsensordata")
            // append("&readend=true")
            append("&datastreams=").append(jsonArray.toString())
            append("&apikey=123456")
            append("&msgid=123456")
        }
    }

    /**
     * 更新传输进度
     */
    private fun updateTransferProgress(response: MonitoringDataResponse) {
        val elapsedTime = System.currentTimeMillis() - startTime

        // 计算传输速度
        val speed = if (elapsedTime > 0) {
            val bytesPerSecond = totalBytesTransferred * 1000 / elapsedTime
            DataFormatUtils.formatTransferSpeed(bytesPerSecond)
        } else "计算中..."

        // 格式化已传输数据大小
        val transferredDataSize = DataFormatUtils.formatDataSize(totalBytesTransferred)

        // 计算进度（无法确定总量时使用不确定进度）
        val progress = 0f // 使用不确定进度

        _transferState.value = TransferState.Transferring(
            progress = progress,
            speed = speed,
            transferredDataSize = transferredDataSize,
            transferredDataCount = totalDataCount
        )
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