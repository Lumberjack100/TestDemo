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

/**
 * 创建者：gonghe
 * 创建时间：2023/9/4
 * 描述： 物联网平台透传指令
 */
class NetIOTCommandViewModel(
    private val deviceInteractiveRepositoryImp: DeviceInteractiveRepositoryImp,
    loggerRepositoryImp: LoggerRepositoryImp
) :
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
                    deviceInteractiveRepositoryImp.batchDispatchRawCmd(jsonParam)

                msgIDList.clear()
                msgIDList.addAll(data.map { it.msgID })
                _cmdDispatchFlow.emit(DispatchSuccess(content))
            } catch (error: Throwable) {
                Timber.e(error)
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
            } catch (error: Exception) {
                Timber.e(error)

                // 错误处理
                _cmdDispatchFlow.emit(
                    CmdResponseResultError(
                        cmdStr = cmdStr,
                        errorMsg = error.message ?: "Error"
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
                deviceInteractiveRepositoryImp.queryCmdResultByMsgID(jsonParam)
            val queryCmdResult = data[0]
            queryCmdResult
        }
    }
}