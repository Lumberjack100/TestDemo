package com.shmedo.mcloudapp.ui.viewmodel.request

import androidx.lifecycle.viewModelScope
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.data.repository.DeviceInteractiveRepositoryImp
import com.shmedo.core.data.repository.LoggerRepositoryImp
import com.shmedo.core.model.DispatchCmdItem
import com.shmedo.core.model.DispatchRawCmdParam
import com.shmedo.core.model.QueryCmdResult
import com.shmedo.core.model.QueryCmdResultParam
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.model.CmdDispatch
import com.shmedo.mcloudapp.model.CmdResponseResultError
import com.shmedo.mcloudapp.model.CmdResponseResultSuccess
import com.shmedo.mcloudapp.model.CmdResponseResultTimeOut
import com.shmedo.mcloudapp.model.DispatchFailed
import com.shmedo.mcloudapp.model.DispatchSuccess
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseRequestViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException

/**
 * 物联网平台透传指令ViewModel
 *
 * 优化特点：
 * 1. 使用Flow代替回调管理指令状态
 * 2. 指令队列和批处理
 * 3. 幂等性处理
 * 4. 错误处理和自动重试
 */
class NetIOTCommandViewModel(
    private val deviceInteractiveRepositoryImp: DeviceInteractiveRepositoryImp,
    loggerRepositoryImp: LoggerRepositoryImp
) : BaseRequestViewModel(loggerRepositoryImp) {

    // 指令派发流
    private val _cmdDispatchFlow: MutableSharedFlow<CmdDispatch> = MutableSharedFlow()
    val cmdDispatchFlow = _cmdDispatchFlow.asSharedFlow()

    // 消息ID列表
    private val msgIDList = ArrayList<String>()

    // 指令跟踪映射，记录已发送指令的信息
    private val commandTracking = ConcurrentHashMap<String, CommandTrackingInfo>()

    // 正在执行的指令作业
    private var currentCommandJob: Job? = null

    // 指令队列，存储待执行的指令
    private val commandQueue = ArrayDeque<CommandQueueItem>()

    // 指令队列项
    private data class CommandQueueItem(
        val content: String, //指令内容
        val deviceTokenList: List<String>, //目标设备列表
        val priority: Int = 0, // 优先级，数字越大优先级越高
        val retryCount: Int = 0 // 重试次数
    )

    // 指令跟踪信息
    private data class CommandTrackingInfo(
        val msgIDs: List<String>,//消息 ID
        val timestamp: Long,//时间戳和设备
        val deviceTokens: List<String>//Token
    )

    /**
     * 分发原始指令
     * 1. 将指令封装成 CommandQueueItem 并加入指令队列
     * 2. 如果当前没有指令在执行，则处理队列
     */
    fun batchDispatchRawCmd(
        content: String,
        deviceTokenList: List<String>,
        priority: Int = 0
    ) {
        // 如果相同指令在2秒内已经被发送并且仍在处理中，则会跳过该重复指令，防止不必要的重复下发。
        val now = System.currentTimeMillis()
        val trackingInfo = commandTracking[content]
        if (trackingInfo != null && now - trackingInfo.timestamp < 2000) {
            Timber.d("跳过重复指令: $content")
            return
        }

        // 将指令加入队列
        val queueItem = CommandQueueItem(content, deviceTokenList, priority)
        addToQueue(queueItem)

        // 如果没有指令在执行，则开始执行队列
        if (currentCommandJob == null || currentCommandJob?.isActive == false) {
            processCommandQueue()
        }
    }

    /**
     * 添加指令到队列，根据优先级排序，高优先级的指令会先执行。
     */
    private fun addToQueue(item: CommandQueueItem) {
        // 查找合适的位置插入，保持队列按优先级排序
        val iterator = commandQueue.iterator()
        var index = 0

        while (iterator.hasNext()) {
            val queueItem = iterator.next()
            if (item.priority > queueItem.priority) {
                break
            }
            index++
        }

        // 在指定位置插入
        if (index >= commandQueue.size) {
            commandQueue.add(item)
        } else {
            val tempList = commandQueue.toMutableList()
            tempList.add(index, item)
            commandQueue.clear()
            commandQueue.addAll(tempList)
        }
    }

    /**
     * 按顺序处理队列中的指令
     * 1. 如果队列为空，将加载状态设置为空闲
     * 2. 如果队列不为空，取出队首指令并调用 executeCommand 执行
     */
    private fun processCommandQueue() {
        if (commandQueue.isEmpty()) {
            return
        }

        val queueItem = commandQueue.removeFirst()
        executeCommand(queueItem)
    }

    /**
     * 执行指令
     * 1. 设置加载状态为 Loading。
     * 2. 调用 deviceInteractiveRepositoryImp.batchDispatchRawCmd() 发送指令。
     * 3. 成功后，保存 msgIDList，更新 commandTracking，并发射 DispatchSuccess 事件。
     * 4. 调用 pollForCommandResult() 轮询指令结果。
     * 5. 捕获异常，发射 DispatchFailed 事件，并根据情况执行重试逻辑或者调用 processCommandQueue() 处理下一个指令。
     */
    private fun executeCommand(queueItem: CommandQueueItem) {
        currentCommandJob = viewModelScope.launch {
            try {
                val rawCmdParam = DispatchRawCmdParam(queueItem.content, queueItem.deviceTokenList)
                val jsonParam = MoshiUtil.toJson(rawCmdParam)
                val data: List<DispatchCmdItem> =
                    deviceInteractiveRepositoryImp.batchDispatchRawCmd(jsonParam)

                // 保存消息ID列表
                msgIDList.clear()
                msgIDList.addAll(data.map { it.msgID })

                // 记录指令跟踪信息
                commandTracking[queueItem.content] = CommandTrackingInfo(
                    msgIDs = msgIDList.toList(),
                    timestamp = System.currentTimeMillis(),
                    deviceTokens = queueItem.deviceTokenList
                )

                // 发送派发成功事件
                _cmdDispatchFlow.emit(DispatchSuccess(queueItem.content))

            } catch (e: CancellationException) {
                // 协程被取消，不处理
                Timber.d("指令执行被取消: ${queueItem.content}")
            } catch (e: Exception) {
                Timber.e(e)
                _cmdDispatchFlow.emit(DispatchFailed(queueItem.content, e.errorMsg))

                // 重试逻辑
//                if (queueItem.retryCount < 3) {  // 最多重试3次
//                    Timber.d("指令执行失败，重试 (${queueItem.retryCount + 1}/3): ${queueItem.content}")
//                    val retryItem = queueItem.copy(retryCount = queueItem.retryCount + 1)
//                    addToQueue(retryItem)
//                }

                // 继续处理队列中的下一个指令
                processCommandQueue()
            }
        }
    }

    /**
     * 此方法用于主动查询指令的结果
     * 1. 调用 pollForCommandResult() 进行轮询
     * 2. 处理可能发生的异常，并更新加载状态和发射错误事件
     */
    fun processCmdResult(cmdStr: String = "", otherMsgIDList: ArrayList<String> = arrayListOf()) {
        viewModelScope.launch {
            try {
                val parameter =
                    QueryCmdResultParam(if (otherMsgIDList.isEmpty()) msgIDList else otherMsgIDList)
                pollForCommandResult(cmdStr, MoshiUtil.toJson(parameter))
            } catch (e: CancellationException) {
                // 协程被取消，不处理
            } catch (e: Exception) {
                Timber.e(e)

                // 错误处理
                _cmdDispatchFlow.emit(
                    CmdResponseResultError(
                        cmdStr = cmdStr,
                        errorMsg = e.message ?: "Error"
                    )
                )

                // 继续处理队列中的下一个指令
                processCommandQueue()
            }
        }
    }

    /**
     * 轮询指令响应结果
     * 1. 它会尝试多次（默认20次，每次间隔500ms）调用 queryCmdResultByMsgID() 查询结果。
     * 2. 如果查询到成功结果 ( cmdStatus == 2 )，则发射 CmdResponseResultSuccess 事件。
     * 3. 如果超时未获得成功结果，则发射 CmdResponseResultTimeOut 事件。
     * 4. 如果轮询过程中发生错误，会发射 CmdResponseResultError 事件。
     * 5. 最后，继续处理队列。
     */
    private suspend fun pollForCommandResult(cmdStr: String = "", jsonParam: String? = null) {
        val parameter = jsonParam ?: MoshiUtil.toJson(QueryCmdResultParam(msgIDList))

        repeat(20) { // 尝试20次，每次间隔500ms，总共最多等待10秒
            delay(500)

            val cmdResult = queryCmdResultByMsgID(parameter)
            if (cmdResult.cmdStatus == 2) { // 状态2表示成功
                _cmdDispatchFlow.emit(CmdResponseResultSuccess(cmdResult))

                // 从跟踪中移除
                commandTracking.entries.removeIf { it.value.msgIDs.contains(cmdResult.msgID) }

                // 继续处理队列
                processCommandQueue()
                return
            }
        }

        // 轮询超时
        _cmdDispatchFlow.emit(CmdResponseResultTimeOut(cmdStr))

        // 继续处理队列中的下一个指令
        processCommandQueue()
    }

    /**
     * 查询指令结果
     */
    private suspend fun queryCmdResultByMsgID(jsonParam: String): QueryCmdResult {
        return withContext(Dispatchers.IO) {
            val data: List<QueryCmdResult> =
                deviceInteractiveRepositoryImp.queryCmdResultByMsgID(jsonParam)
            data.first()
        }
    }

    /**
     * 取消当前指令
     */
    fun cancelCurrentCommand() {
        currentCommandJob?.cancel()
        currentCommandJob = null

        // 继续处理队列
        processCommandQueue()
    }

    /**
     * 清空指令队列
     */
    fun clearCommandQueue() {
        commandQueue.clear()
        cancelCurrentCommand()
    }

    /**
     * 获取队列中指令数量
     */
    fun getQueueSize(): Int = commandQueue.size

    /**
     * 判断是否有指令在执行
     */
    fun isCommandRunning(): Boolean = currentCommandJob?.isActive == true
}