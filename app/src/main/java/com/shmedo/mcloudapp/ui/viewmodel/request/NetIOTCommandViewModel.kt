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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

/**
 * 物联网平台透传指令ViewModel - 优化版
 *
 * 优化特点：
 * 1. 专注于网络通信，移除队列管理逻辑
 * 2. 提供简洁的 suspend 函数接口
 * 3. 符合新通信架构设计理念
 * 4. 保持向后兼容性
 */
class NetIOTCommandViewModel(
    private val deviceInteractiveRepositoryImp: DeviceInteractiveRepositoryImp,
    loggerRepositoryImp: LoggerRepositoryImp
) : BaseRequestViewModel(loggerRepositoryImp) {

    // ===========================================
    // 新架构：简洁API (推荐使用)
    // ===========================================

    /**
     * 发送单条指令并等待响应 - 新架构专用API
     * @param command 指令内容
     * @param deviceTokens 设备Token列表
     * @param timeoutMs 超时时间(毫秒)，默认10秒
     * @return CommandResponse 指令响应结果
     */
    suspend fun sendCommandAndAwaitResponse(
        command: String,
        deviceTokens: List<String>,
        timeoutMs: Long = 10_000L
    ): CommandResponse {
        return withContext(Dispatchers.IO) {
            try {
                Timber.d("发送网络指令: $command, 设备: $deviceTokens")

                // 1. 派发指令
                val rawCmdParam = DispatchRawCmdParam(command, deviceTokens)
                val jsonParam = MoshiUtil.toJson(rawCmdParam)
                val dispatchResult: List<DispatchCmdItem> =
                    deviceInteractiveRepositoryImp.batchDispatchRawCmd(jsonParam)

                val msgIDs = dispatchResult.map { it.msgID }
                Timber.d("指令派发成功，消息ID: $msgIDs")

                // 2. 轮询响应结果
                val queryParam = QueryCmdResultParam(msgIDs)
                val queryJson = MoshiUtil.toJson(queryParam)

                val maxRetries = (timeoutMs / 500).toInt().coerceAtLeast(1) // 每500ms查询一次
                repeat(maxRetries) { attempt ->
                    delay(500)

                    try {
                        val results: List<QueryCmdResult> =
                            deviceInteractiveRepositoryImp.queryCmdResultByMsgID(queryJson)

                        val result = results.firstOrNull()
                        if (result?.cmdStatus == 2) { // 状态2表示成功
                            Timber.i("网络指令响应成功: ${result.responseContent}")
                            return@withContext CommandResponse.Success(
                                responseData = result.responseContent,
                                command = command,
                                msgID = result.msgID
                            )
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "查询指令结果异常，尝试 ${attempt + 1}/$maxRetries")
                    }
                }

                // 超时
                Timber.e("网络指令超时: $command")
                CommandResponse.Timeout(command, timeoutMs)

            } catch (e: CancellationException) {
                throw e // 重新抛出取消异常
            } catch (e: Exception) {
                Timber.e(e, "网络指令发送异常: $command")
                CommandResponse.Error(command, e.errorMsg, e)
            }
        }
    }


    // ===========================================
    // 原有API (保持向后兼容)
    // ===========================================

    // 指令派发流 - 用于兼容旧代码
    private val _cmdDispatchFlow: MutableSharedFlow<CmdDispatch> = MutableSharedFlow()
    val cmdDispatchFlow = _cmdDispatchFlow.asSharedFlow()

    // 消息ID列表 - 用于兼容旧代码
    private val msgIDList = ArrayList<String>()

    /**
     * 分发原始指令 - 旧API，保持兼容性
     * @deprecated 推荐使用 sendCommandAndAwaitResponse
     */
    fun batchDispatchRawCmd(
        content: String,
        deviceTokenList: List<String>,
        priority: Int = 0
    ) {
        viewModelScope.launch {
            try {
                val rawCmdParam = DispatchRawCmdParam(content, deviceTokenList)
                val jsonParam = MoshiUtil.toJson(rawCmdParam)
                val data: List<DispatchCmdItem> =
                    deviceInteractiveRepositoryImp.batchDispatchRawCmd(jsonParam)

                // 保存消息ID列表
                msgIDList.clear()
                msgIDList.addAll(data.map { it.msgID })

                // 发送派发成功事件
                _cmdDispatchFlow.emit(DispatchSuccess(content))

            } catch (e: CancellationException) {
                Timber.d("指令执行被取消: $content")
            } catch (e: Exception) {
                Timber.e(e)
                _cmdDispatchFlow.emit(DispatchFailed(content, e.errorMsg))
            }
        }
    }

    /**
     * 处理指令结果 - 旧API，保持兼容性
     * @deprecated 推荐使用 sendCommandAndAwaitResponse
     */
    fun processCmdResult(cmdStr: String = "", otherMsgIDList: ArrayList<String> = arrayListOf()) {
        viewModelScope.launch {
            try {
                val parameter = QueryCmdResultParam(
                    if (otherMsgIDList.isEmpty()) msgIDList else otherMsgIDList
                )
                pollForCommandResult(cmdStr, MoshiUtil.toJson(parameter))
            } catch (e: CancellationException) {
                // 协程被取消，不处理
            } catch (e: Exception) {
                Timber.e(e)
                _cmdDispatchFlow.emit(
                    CmdResponseResultError(
                        cmdStr = cmdStr,
                        errorMsg = e.message ?: "Error"
                    )
                )
            }
        }
    }

    /**
     * 轮询指令响应结果 - 旧API内部方法
     */
    private suspend fun pollForCommandResult(cmdStr: String = "", jsonParam: String? = null) {
        val parameter = jsonParam ?: MoshiUtil.toJson(QueryCmdResultParam(msgIDList))

        repeat(20) { // 尝试20次，每次间隔500ms
            delay(500)

            val cmdResult = queryCmdResultByMsgID(parameter)
            if (cmdResult.cmdStatus == 2) { // 状态2表示成功
                _cmdDispatchFlow.emit(CmdResponseResultSuccess(cmdResult))
                return
            }
        }

        // 轮询超时
        _cmdDispatchFlow.emit(CmdResponseResultTimeOut(cmdStr))
    }

    /**
     * 查询指令结果 - 内部方法
     */
    private suspend fun queryCmdResultByMsgID(jsonParam: String): QueryCmdResult {
        return withContext(Dispatchers.IO) {
            val data: List<QueryCmdResult> =
                deviceInteractiveRepositoryImp.queryCmdResultByMsgID(jsonParam)
            data.first()
        }
    }


    /**
     * 清空指令队列 - 旧API
     */
    fun clearCommandQueue() {
        msgIDList.clear()
        Timber.d("清空指令队列")
    }
}

/**
 * 指令响应结果密封类 - 新架构专用
 */
sealed class CommandResponse {
    /**
     * 指令执行成功
     * @param responseData 响应数据
     * @param command 原始指令
     * @param timestamp 执行时间戳
     */
    data class Success(
        val responseData: String,
        val command: String,
        val msgID: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : CommandResponse()

    /**
     * 指令执行错误
     * @param command 原始指令
     * @param errorMsg 错误详情
     */
    data class Error(
        val command: String,
        val errorMsg: String,
        val cause: Throwable? = null
    ) : CommandResponse()

    /**
     * 指令执行超时
     * @param command 原始指令
     * @param timeoutMs 超时时长(毫秒)
     */
    data class Timeout(
        val command: String,
        val timeoutMs: Long
    ) : CommandResponse()
}