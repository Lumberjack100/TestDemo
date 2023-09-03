package com.shmedo.mcloudapp.device

import androidx.lifecycle.lifecycleScope
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.device.model.QueryCmdResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/9/3
 *
 * 描述： TODO
 *
 *
 */
abstract class BaseNetIotCommunicateFragment : BaseFragment() {
    var msgIDList: ArrayList<String> = ArrayList()

    /**
     * 指令下发失败处理
     */
    open fun doDispatchFailed(cmdStr: String, errorMsg: String) {

    }

    /**
     * 指令下发成功处理
     */
    open fun doDispatchSuccess(cmdStr: String) {}


    // 假设你在 ViewModel 或者其他 Lifecycle-aware 组件中
    fun dispatchCommand() {
        lifecycleScope.launch {
            try {
                // 下发指令
                val dispatchCmdResult = processDispatchRawCmd()
                if (dispatchCmdResult.isSuccessful) {
                    val response = pollForCommandResult()
                    if (response == null) {
                        // 响应超时
                        onQueryCmdResponseResultTimeOut()
                    } else {
                        // 处理响应结果
                        processCmdResult(response)
                    }
                }
            } catch (e: Exception) {
                // 错误处理
                onQueryCmdResponseResultError(e.message ?: "Error")
            }
        }
    }

    private suspend fun processDispatchRawCmd(): DispatchCmdResult {
        return withContext(Dispatchers.IO) {
            // 调用指令透传接口的逻辑
            ...
        }
    }

    private suspend fun pollForCommandResult(): QueryCmdResult? {
        repeat(10) {
            delay(1000) // 延迟1秒
            val cmdResult = queryCmdResultByMsgID()
            if (cmdResult?.getCmdStatus() == 2) {
                return cmdResult
            }
        }
        return null
    }

    private suspend fun queryCmdResultByMsgID(): QueryCmdResult? {
        return withContext(Dispatchers.IO) {
            // 查询设备对下发/透传的指令响应结果的逻辑
            ...
        }
    }

    private fun processCmdResult(queryCmdResult: QueryCmdResult) {
        withContext(Dispatchers.Main) {
            // 原来的 processCmdResult 逻辑
            ...
        }
    }
}