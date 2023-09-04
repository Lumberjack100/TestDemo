package com.shmedo.mcloudapp.device.viewmodel.request

import androidx.lifecycle.viewModelScope
import com.shmedo.lib.core.base.viewmodel.BaseViewModel
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.data.repository.remote.NetDataRepository
import com.shmedo.mcloudapp.device.CmdDispatch
import com.shmedo.mcloudapp.device.model.DispatchCmdItem
import com.shmedo.mcloudapp.device.model.DispatchRawCmdParam
import com.shmedo.mcloudapp.device.model.QueryCmdResult
import com.shmedo.mcloudapp.device.model.QueryCmdResultParam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/9/4
 *
 * 描述： TODO
 *
 *
 */
class IOTCommandViewModel : BaseViewModel() {
    private val _cmdDispatchFlow: MutableSharedFlow<CmdDispatch> = MutableSharedFlow()
    val cmdDispatchFlow = _cmdDispatchFlow.asSharedFlow()

    private var msgIDList: ArrayList<String> = ArrayList()

    fun batchDispatchRawCmd(
        content: String,
        deviceTokenList: List<String>
    ) {
        viewModelScope.launch {
            try {
                val rawCmdParam = DispatchRawCmdParam(content, deviceTokenList)
                val jsonParam = MoshiUtil.toJson(rawCmdParam)
                val data: List<DispatchCmdItem> =
                    NetDataRepository.instance.batchDispatchRawCmd(jsonParam)

                msgIDList.clear()
                msgIDList.addAll(data.map { it.msgID })
                _cmdDispatchFlow.emit(CmdDispatch.DispatchSuccess(content))
            } catch (error: Throwable) {
                _cmdDispatchFlow.emit(CmdDispatch.DispatchFailed(content, error.errorMsg))
            }
        }
    }

    fun processCmdResult() {
        viewModelScope.launch {
            try {
                val parameter = QueryCmdResultParam(msgIDList)
                val response = pollForCommandResult(MoshiUtil.toJson(parameter))
            } catch (e: Exception) {
                // 错误处理
                _cmdDispatchFlow.emit(CmdDispatch.CmdResponseResultError(e.message ?: "Error"))
            }
        }
    }

    /**
     * 轮询指令响应结果 10次
     */
    private suspend fun pollForCommandResult(jsonParam: String): Boolean {
        repeat(20) {
            delay(500) // 延迟1秒
            val cmdResult: QueryCmdResult = queryCmdResultByMsgID(jsonParam)
            if (cmdResult.cmdStatus == 2) {
                _cmdDispatchFlow.emit(CmdDispatch.CmdResponseResultSuccess(cmdResult))
                return true
            }
        }
        _cmdDispatchFlow.emit(CmdDispatch.CmdResponseResultTimeOut())
        return false
    }

    /**
     * 查询设备对下发/透传的指令响应结果的逻辑
     */
    private suspend fun queryCmdResultByMsgID(jsonParam: String): QueryCmdResult {
        return withContext(Dispatchers.IO) {
            // 查询设备对下发/透传的指令响应结果的逻辑
            val data: List<QueryCmdResult> =
                NetDataRepository.instance.queryCmdResultByMsgID(jsonParam)
            val queryCmdResult = data[0]
            queryCmdResult
        }
    }
}