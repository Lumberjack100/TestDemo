package com.shmedo.mcloudapp.device.viewmodel.request

import androidx.lifecycle.viewModelScope
import com.shmedo.lib.core.base.viewmodel.BaseRequestViewModel
import com.shmedo.lib.core.data.repository.LoggerRepositoryImp
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.data.repository.remote.NetDataRepository
import com.shmedo.mcloudapp.device.common.CmdDispatch
import com.shmedo.mcloudapp.device.common.CmdResponseResultError
import com.shmedo.mcloudapp.device.common.CmdResponseResultSuccess
import com.shmedo.mcloudapp.device.common.CmdResponseResultTimeOut
import com.shmedo.mcloudapp.device.common.DispatchFailed
import com.shmedo.mcloudapp.device.common.DispatchSuccess
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
 * 创建时间：2023/9/4
 * 描述： 物联网平台透传指令
 */
class NetIOTCommandViewModel(private val loggerRepositoryImp: LoggerRepositoryImp) :
    BaseRequestViewModel(loggerRepositoryImp) {
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
                _cmdDispatchFlow.emit(DispatchSuccess(content))
            } catch (error: Throwable) {
                _cmdDispatchFlow.emit(DispatchFailed(content, error.errorMsg))
            }
        }
    }

    fun processCmdResult(cmdStr: String = "", otherMsgIDList: ArrayList<String> = arrayListOf()) {
        viewModelScope.launch {
            try {
                val parameter =
                    QueryCmdResultParam(if (otherMsgIDList.isEmpty()) msgIDList else otherMsgIDList)
                pollForCommandResult(cmdStr = cmdStr, jsonParam = MoshiUtil.toJson(parameter))
            } catch (e: Exception) {
                e.printStackTrace()
                // 错误处理
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
     * 轮询指令响应结果 10次
     */
    private suspend fun pollForCommandResult(cmdStr: String = "", jsonParam: String) {
        repeat(20) {
            delay(500) //延迟 500 毫秒
            val cmdResult: QueryCmdResult = queryCmdResultByMsgID(jsonParam)
            if (cmdResult.cmdStatus == 2) {
                _cmdDispatchFlow.emit(CmdResponseResultSuccess(cmdResult))
                return
            }
        }
        _cmdDispatchFlow.emit(CmdResponseResultTimeOut(cmdStr))
        return
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